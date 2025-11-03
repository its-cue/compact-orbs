/*
 * Copyright (c) 2025, cue <https://github.com/its-cue>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.compactorbs.widget;

import com.compactorbs.CompactOrbsConstants.Script;
import com.compactorbs.CompactOrbsConstants.Widget.Classic;
import com.compactorbs.CompactOrbsConstants.Widget.Modern;
import com.compactorbs.util.SetValue;
import com.compactorbs.util.ValueKey;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;

@Slf4j
public class WidgetManager
{
	@Inject
	private Client client;

	private boolean remapping = false;
	private final Object sync = new Object();

	public void remapTargets(Iterable<? extends TargetWidget> widgets, boolean modify, int scriptId)
	{
		synchronized (sync)
		{
			if (remapping)
			{
				return;
			}
			remapping = true;
		}

		try
		{
			for (TargetWidget target : widgets)
			{
				if (!shouldUpdateTarget(target, scriptId))
				{
					continue;
				}

				/*if (scriptId != Script.FORCE_UPDATE)
				{
					log.debug("remap : {} - [{}.{}]: {}.{}, script: {}",
						modify,
						((Enum<?>) target).getDeclaringClass().getSimpleName(),
						target,
						getInterfaceId(target.getComponentId()),
						getChildId(target.getComponentId()),
						scriptId
					);
				}*/

				remapTarget(target, modify);
			}
		}
		finally
		{
			synchronized (sync)
			{
				remapping = false;
			}
		}
	}

	public void remapTarget(TargetWidget target, boolean modify)
	{
		Widget widget = getTargetWidget(target);
		if (widget == null)
		{
			return;
		}

		target.getPositions().forEach((type, value) -> setValue(widget, type, value, modify));
		widget.revalidate();
	}

	private void setValue(Widget widget, ValueKey type, SetValue value, boolean modify)
	{
		if (value == null)
		{
			return;
		}

		//TODO : @index config based layout selection where: v = 0, h = 1?
		Integer v = value.get(modify, 0);
		if (v == null)
		{
			return;
		}

		switch (type)
		{
			case X:
				updateValue(widget::getOriginalX, widget::setOriginalX, v);
				break;
			case Y:
				updateValue(widget::getOriginalY, widget::setOriginalY, v);
				break;
			case X_POSITION_MODE:
				updateValue(widget::getXPositionMode, widget::setXPositionMode, v);
				break;
			case Y_POSITION_MODE:
				//updateValue(widget::getYPositionMode, widget::setYPositionMode, v);
				break;
		}
	}

	private void updateValue(IntSupplier getter, IntConsumer setter, int value)
	{
		if (getter.getAsInt() != value)
		{
			setter.accept(value);
		}
	}

	public void setTargetsHidden(boolean hidden, TargetWidget... widgets)
	{
		for (TargetWidget target : widgets)
		{
			setHidden(target, hidden);
		}
	}

	public void setHidden(TargetWidget target, boolean hidden)
	{
		Widget widget = getTargetWidget(target);
		if (widget == null)
		{
			return;
		}

		if (hidden != widget.isHidden())
		{
			widget.setHidden(hidden);
			//log.debug("widget : {}.{}, hidden: {}.{}",
			//	getInterfaceId(target.getComponentId()), getChildId(target.getComponentId()), widget.isHidden(), hidden);

			//compass menu options
			if (widget.getChildren() != null)
			{
				for (Widget child : widget.getChildren())
				{
					if (child != null)
					{
						child.setHidden(hidden);
						//log.debug("child : {}.{}[{}], hidden: {}",
						//	getInterfaceId(target.getComponentId()), getChildId(target.getComponentId()), child.getIndex(), hidden);
					}
				}
			}
		}
	}

	public Widget getTargetWidget(TargetWidget target)
	{
		Widget widget = client.getWidget(getInterfaceId(target.getComponentId()), getChildId(target.getComponentId()));
		if (widget == null)
		{
			return null;
		}
		if (target.getArrayId() == -1)
		{
			return widget;
		}
		return widget.getChild(target.getArrayId());
	}

	public Widget getCurrentParent()
	{
		Widget parent = getParent(Modern.ORBS);
		if (parent != null && !parent.isHidden())
		{
			return parent;
		}

		return getParent(Classic.ORBS);
	}

	private Widget getParent(int componentId)
	{
		Widget parent = client.getWidget(getInterfaceId(componentId), getChildId(componentId));
		return (parent != null && !parent.isHidden()) ? parent : null;
	}

	public void clearChildren(int componentId)
	{
		Widget widget = client.getWidget(getInterfaceId(componentId), getChildId(componentId));
		if (widget != null)
		{
			Widget child = widget.getChild(0);
			if (child != null)
			{
				widget.deleteAllChildren();
			}
		}
	}

	private boolean shouldUpdateTarget(TargetWidget target, int scriptId)
	{
		return (scriptId == Script.FORCE_UPDATE) || target.getScriptId() == scriptId;
	}

	public boolean isMissing(Widget child, Widget parent)
	{
		return child == null || child.getParentId() != parent.getId();
	}

	public static int getInterfaceId(int componentId)
	{
		return componentId >> 16;
	}

	public static int getChildId(int componentId)
	{
		return componentId & 0xff;
	}

	public Widget createGraphic(
		Widget parent,
		int x, int y,
		int width, int height,
		int opacity,
		int spriteId)
	{
		Widget graphic = parent.createChild(-1, WidgetType.GRAPHIC);
		graphic
			.setOriginalX(x)
			.setOriginalY(y)
			.setOriginalWidth(width)
			.setOriginalHeight(height)
			.setOpacity(opacity)
			.setHidden(false)
			.setSpriteId(spriteId);

		return graphic;
	}

	public Widget createToggleButton(
		Widget parent,
		int x, int y,
		int width, int height,
		int opacity,
		int spriteId,
		String menuOp,
		JavaScriptCallback opListener,
		JavaScriptCallback mouseOver,
		JavaScriptCallback mouseLeave)
	{
		Widget button = createGraphic(parent, x, y, width, height, opacity, spriteId);
		button.setHasListener(true);
		button.setNoClickThrough(true);

		if (menuOp != null)
		{
			button.setAction(0, menuOp);
		}
		if (opListener != null)
		{
			button.setOnOpListener(opListener);
		}
		if (mouseOver != null)
		{
			button.setOnMouseOverListener(mouseOver);
		}
		if (mouseLeave != null)
		{
			button.setOnMouseLeaveListener(mouseLeave);
		}

		return button;
	}
}
