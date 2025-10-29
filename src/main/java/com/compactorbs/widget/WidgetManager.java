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

import static com.compactorbs.CompactOrbsManager.FORCE_REMAP;
import static com.compactorbs.CompactOrbsManager.TOP_LEVEL_MINIMAP_CHILD;
import com.compactorbs.util.SetValue;
import com.compactorbs.util.ValueKey;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
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
				if (!shouldUpdateWidget(target, scriptId))
				{
					continue;
				}

				/*if (scriptId != FORCE_REMAP)
				{
					log.debug("remap : {} - [{}.{}]: {}.{}, script: {}",
						modify,
						((Enum<?>) target).getDeclaringClass().getSimpleName(),
						target,
						target.getInterfaceId(),
						target.getChildId(),
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

	private boolean shouldUpdateWidget(TargetWidget target, int scriptId)
	{
		return (scriptId == FORCE_REMAP) || target.getScriptId() == scriptId;
	}

	private void setValue(Widget widget, ValueKey type, SetValue value, boolean modify)
	{
		if (value == null)
		{
			return;
		}

		Integer v = value.get(modify, 0);//TODO
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
			// target.getInterfaceId(), target.getChildId(), widget.isHidden(), hidden);

			//compass menu options
			if (widget.getChildren() != null)
			{
				for (Widget child : widget.getChildren())
				{
					if (child != null)
					{
						child.setHidden(hidden);
						//log.debug("child : {}.{}[{}], hidden: {}",
						// target.getInterfaceId(), target.getChildId(), child.getIndex(), hidden);
					}
				}
			}
		}
	}

	public Widget getTargetWidget(TargetWidget target)
	{
		Widget widget = client.getWidget(target.getInterfaceId(), target.getChildId());
		if (widget == null)
		{
			return null;
		}
		return (target.getArrayId() != -1 ? widget.getChild(target.getArrayId()) : widget);
	}

	public Widget getCurrentParent()
	{
		Widget modern = client.getWidget(InterfaceID.TOPLEVEL_PRE_EOC, TOP_LEVEL_MINIMAP_CHILD);
		if (modern != null && !modern.isHidden())
		{
			return modern;
		}

		Widget classic = client.getWidget(InterfaceID.TOPLEVEL_OSRS_STRETCH, TOP_LEVEL_MINIMAP_CHILD);
		if (classic != null && !classic.isHidden())
		{
			return classic;
		}

		return null;
	}

	public void clearChildren(int interfaceId, int childId)
	{
		Widget widget = client.getWidget(interfaceId, childId);
		if (widget != null)
		{
			//start at index 1, in case the widget inspector is open?
			Widget child = widget.getChild(1);
			if (child != null)
			{
				widget.deleteAllChildren();
			}
		}
	}

	public boolean isMissing(Widget child, Widget parent)
	{
		return child == null || child.getParentId() != parent.getId();
	}

	public Widget createGraphic(
		Widget parent,
		int x, int y,
		int width, int height,
		int opacity,
		boolean hidden,
		int spriteId)
	{
		Widget child = parent.createChild(-1, WidgetType.GRAPHIC);
		child
			.setOriginalX(x)
			.setOriginalY(y)
			.setOriginalWidth(width)
			.setOriginalHeight(height)
			.setOpacity(opacity)
			.setHidden(hidden)
			.setSpriteId(spriteId)
			.revalidate();

		return child;
	}

	public Widget createMenu(
		Widget parent,
		int x, int y,
		boolean hidden,
		String menuOp,
		JavaScriptCallback opListener,
		JavaScriptCallback mouseOver,
		JavaScriptCallback mouseLeave)
	{
		Widget child = parent.createChild(-1, WidgetType.GRAPHIC);
		child
			.setOriginalX(x)
			.setOriginalY(y)
			.setOriginalWidth(16)
			.setOriginalHeight(16)
			.setHasListener(true)
			.setHidden(hidden)
			.setAction(0, menuOp);

		child.setNoClickThrough(true);
		if (opListener != null)
		{
			child.setOnOpListener(opListener);
		}
		if (mouseOver != null)
		{
			child.setOnMouseOverListener(mouseOver);
		}
		if (mouseLeave != null)
		{
			child.setOnMouseLeaveListener(mouseLeave);
		}
		child.revalidate();

		return child;
	}
}
