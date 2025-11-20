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

import com.compactorbs.CompactOrbsConfig;
import com.compactorbs.CompactOrbsConstants;
import com.compactorbs.CompactOrbsConstants.Script;
import com.compactorbs.CompactOrbsConstants.Widgets.Classic;
import com.compactorbs.CompactOrbsConstants.Widgets.Modern;
import com.compactorbs.CompactOrbsConstants.Widgets.Orb;
import static com.compactorbs.CompactOrbsManager.horizontalOffset;
import static com.compactorbs.CompactOrbsManager.verticalOffset;
import static com.compactorbs.CompactOrbsManager.worldMapOffset;
import com.compactorbs.util.SetValue;
import com.compactorbs.util.ValueKey;
import com.compactorbs.widget.elements.Orbs;
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

	@Inject
	private CompactOrbsConfig config;

	public boolean targetRemapped = false;

	//update multiple widgets that match the script id (FORCE_UPDATE bypasses the check)
	public void remapTargets(Iterable<? extends TargetWidget> widgets, boolean modify, int scriptId)
	{
		for (TargetWidget target : widgets)
		{
			if (!shouldUpdateTarget(target, scriptId))
			{
				continue;
			}

			remapTarget(target, modify);
		}
	}

	//update a target widgets X/Y or position mode, based on layout, if necessary
	public void remapTarget(TargetWidget target, boolean modify)
	{
		Widget widget = getTargetWidget(target);
		if (widget == null)
		{
			return;
		}

		targetRemapped = false;

		//ignore the widget inspector (shares same container/index as grid master)
		if (target == Orbs.GRID_MASTER_ORB_CONTAINER &&
			widget.getSpriteId() == CompactOrbsConstants.Sprite.WIDGET_INSPECTOR)
		{
			return;
		}

		target.getPositions().forEach((key, value) ->
			setValue(widget, key, value, modify)
		);

		if (targetRemapped)
		{
			widget.revalidate();
		}
	}

	//sets the widgets X/Y or position mode as necessary
	private void setValue(Widget widget, ValueKey key, SetValue value, boolean modify)
	{
		if (value == null)
		{
			return;
		}

		//@index for the modified value, tied to enum OrbLayout()
		Integer v = value.get(modify, config.layout().getIndex());
		if (v == null)
		{
			return;
		}

		//handle dynamic offsets for widgets, based on layout and config settings
		int offset = getOffset(widget, key, v, modify);

		switch (key)
		{
			case X:
				updateValue(widget::getOriginalX, widget::setOriginalX, offset);
				break;
			case Y:
				updateValue(widget::getOriginalY, widget::setOriginalY, offset);
				break;
			case X_POSITION_MODE:
				updateValue(widget::getXPositionMode, widget::setXPositionMode, v);
				break;
			case Y_POSITION_MODE:
				updateValue(widget::getYPositionMode, widget::setYPositionMode, v);
				break;
		}
	}

	//sets a value only if it has changed
	public void updateValue(IntSupplier getter, IntConsumer setter, int value)
	{
		if (getter.getAsInt() != value)
		{
			setter.accept(value);
			targetRemapped = true;
		}
	}

	//returns the widgets offset based on key, layout, and config settings
	private int getOffset(Widget widget, ValueKey key, int value, boolean modify)
	{
		boolean isCompass = widget.getParentId() == Modern.COMPASS_PARENT || widget.getParentId() == Classic.COMPASS_PARENT;
		boolean isWikiContainer = widget.getParentId() == Orb.WIKI_ICON;
		boolean isWikiVanilla = widget.getParentId() == Orb.WIKI_CONTAINER_VANILLA;
		boolean isWorldMap = widget.getId() == Orb.WORLD_MAP;

		//wiki plugin banner, and vanilla wiki banner container
		if (isWikiContainer)
		{
			return value;
		}

		//vanilla wiki banner graphic
		if (isWikiVanilla)
		{
			//set x/y to 0 (graphic should be at the top of the container, will help with giant clickbox for menu options)
			//to prevent overlapping into orbs/compass when in compact layouts
			return 0;
		}

		int offset = (key == ValueKey.X ? verticalOffset : horizontalOffset);

		if (isCompass && key == ValueKey.X)
		{
			return value - offset;
		}

		if (isWorldMap)
		{
			int v = (modify ? (config.hideWorld() ? 0 : value + offset) : value);
			return v + worldMapOffset;
		}

		return (!modify ? value : value + offset);
	}

	//set visibility for target widgets, excluding the wiki banner (handled in updateWikiBanner)
	public void setTargetsHidden(boolean hidden, TargetWidget... widgets)
	{
		for (TargetWidget target : widgets)
		{
			if (target == Orbs.WIKI_VANILLA_CONTAINER || target == Orbs.WIKI_ICON_CONTAINER)
			{
				continue;
			}

			setHidden(target, hidden);
		}
	}

	//set visibility for a target widget and its children (if they exist), if necessary
	public void setHidden(TargetWidget target, boolean hidden)
	{
		Widget widget = getTargetWidget(target);
		if (widget == null)
		{
			return;
		}

		if (target == Orbs.GRID_MASTER_ORB_CONTAINER &&
			widget.getSpriteId() == CompactOrbsConstants.Sprite.WIDGET_INSPECTOR)
		{
			return;
		}

		if (hidden != widget.isHidden())
		{
			widget.setHidden(hidden);

			//specifically for compass menu options (could be others?)
			if (widget.getChildren() != null)
			{
				for (Widget child : widget.getChildren())
				{
					if (child != null)
					{
						child.setHidden(hidden);
					}
				}
			}
		}
	}

	//get the widget for the given TargetWidget
	public Widget getTargetWidget(TargetWidget target)
	{
		Widget widget = client.getWidget(target.getComponentId());
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

	//returns the current visible parent widget
	public Widget getCurrentParent()
	{
		Widget parent = getParent(Modern.ORBS);
		if (parent != null && !parent.isHidden())
		{
			return parent;
		}

		return getParent(Classic.ORBS);
	}

	//returns the parent widget for the given component ID
	//can exist and be hidden, so making sure to check for visibility
	public Widget getParent(int componentId)
	{
		Widget parent = client.getWidget(componentId);
		if (parent != null && !parent.isHidden())
		{
			return parent;
		}

		return null;
	}

	//remove all children from the given component id (if they exist, used for custom children)
	public void clearChildren(int componentId)
	{
		Widget widget = client.getWidget(componentId);
		if (widget != null)
		{
			Widget child = widget.getChild(0);
			if (child != null)
			{
				widget.deleteAllChildren();
			}
		}
	}

	//check if a target widget should be updated based on script id (or FORCE_UPDATE)
	private boolean shouldUpdateTarget(TargetWidget target, int scriptId)
	{
		return (scriptId == Script.FORCE_UPDATE) || target.getScriptId() == scriptId;
	}

	//check if the child is missing, or not a child of the given parent widget
	public boolean isMissing(Widget child, Widget parent)
	{
		return child == null || child.getParentId() != parent.getId();
	}

	//return the interface id from the component id
	public static int getInterfaceId(int componentId)
	{
		return componentId >> 16;
	}

	//return the child id from the component id
	public static int getChildId(int componentId)
	{
		return componentId & 0xff;
	}

	//create a simple graphic widget
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

	//create an interactive toggle button (with sprite, menu option, and optional event listeners)
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
