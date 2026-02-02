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

import com.compactorbs.CompactOrbsConstants;
import com.compactorbs.CompactOrbsConstants.Script;
import com.compactorbs.CompactOrbsConstants.Widgets.Classic;
import com.compactorbs.CompactOrbsConstants.Widgets.Modern;
import com.compactorbs.CompactOrbsManager;
import com.compactorbs.util.SetValue;
import com.compactorbs.util.ValueKey;
import com.compactorbs.widget.elements.Orbs;
import com.compactorbs.widget.offset.OffsetManager;
import com.compactorbs.widget.slot.SlotManager;
import com.compactorbs.widget.slot.Slot;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.api.widgets.WidgetSizeMode;
import net.runelite.api.widgets.WidgetType;

@Slf4j
@Singleton
public class WidgetManager
{
	@Inject
	private Client client;

	@Inject
	private CompactOrbsManager manager;

	@Inject
	private SlotManager slotManager;

	private boolean targetRemapped = false;

	//update multiple widgets that match the script id (FORCE_UPDATE bypasses the check)
	public void remapTargets(boolean compactLayout, int scriptId, TargetWidget... widgets)
	{
		//prevent visual changes when not logged in
		if (!manager.isLoggedIn())
		{
			return;
		}

		//ensure slot layout correctness before remapping
		slotManager.updateCurrentSlotLayout();

		for (TargetWidget target : widgets)
		{
			if (!shouldUpdateTarget(target, scriptId))
			{
				continue;
			}

			remapTarget(target, compactLayout);
		}
	}

	//update a target widgets X/Y or position mode, based on layout, if necessary
	public void remapTarget(TargetWidget target, boolean compactLayout)
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

		getTarget(target).getValueMap().forEach((key, value) ->
			setValue(widget, key, value, compactLayout)
		);

		if (targetRemapped)
		{
			widget.revalidate();
		}
	}

	//returns a target widget
	private TargetWidget getTarget(TargetWidget target)
	{
		if (target instanceof Orbs)
		{
			return getSlotTarget(target);
		}

		return target;
	}

	//returns which orb the target should reference
	public TargetWidget getSlotTarget(TargetWidget target)
	{
		if (!Orbs.SWAPPABLE_ORBS.contains((Orbs) target) || !manager.enableOrbSwapping())
		{
			return target;
		}

		Slot slot = slotManager.findSlotByOrb(target, slotManager.currentSlotLayoutMode);
		if (slot == null)
		{
			return target;
		}

		return slot.getOriginal();
	}

	//sets the widgets X/Y or position mode as necessary
	private void setValue(Widget widget, ValueKey key, SetValue value, boolean compactLayout)
	{
		if (value == null)
		{
			return;
		}

		Integer v = value.get(compactLayout, manager.getLayout());
		if (v == null)
		{
			return;
		}

		switch (key)
		{
			case X:
				updateValue(widget::getOriginalX, widget::setOriginalX,
					OffsetManager.getTargetOffset(widget, key, v, compactLayout, manager, slotManager));
				break;
			case Y:
				updateValue(widget::getOriginalY, widget::setOriginalY,
					OffsetManager.getTargetOffset(widget, key, v, compactLayout, manager, slotManager));
				break;
			case WIDTH:
				updateValue(widget::getOriginalWidth, widget::setOriginalWidth, v);
				break;
			case HEIGHT:
				updateValue(widget::getOriginalHeight, widget::setOriginalHeight, v);
				break;
			case X_POSITION_MODE:
				updateValue(widget::getXPositionMode, widget::setXPositionMode, v);
				break;
			case Y_POSITION_MODE:
				updateValue(widget::getYPositionMode, widget::setYPositionMode, v);
				break;
			case WIDTH_MODE:
				updateValue(widget::getWidthMode, widget::setWidthMode, v);
				break;
			case HEIGHT_MODE:
				updateValue(widget::getHeightMode, widget::setHeightMode, v);
				break;

			default:
				throw new IllegalStateException("Unhandled ValueKey: " + key);
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

	//set visibility for target widgets, excluding the wiki banner (handled in updateWikiBanner)
	public void setTargetsHidden(boolean hidden, TargetWidget... widgets)
	{
		if (!manager.isLoggedIn())
		{
			return;
		}

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

	public void setHidden(int componentId, boolean hidden)
	{
		Widget widget = client.getWidget(componentId);
		if (widget == null)
		{
			return;
		}

		widget.setHidden(hidden);
	}

	public void revalidate(TargetWidget... widgets)
	{
		for (TargetWidget target : widgets)
		{
			Widget widget = client.getWidget(target.getComponentId());
			if (widget == null)
			{
				continue;
			}
			widget.revalidate();
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
	//can exist and be hidden, so check for visibility
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

	public void createMinimapNoClickLayer(Widget parent, int index, int y, int width, int height)
	{
		Widget widget = parent.createChild(index, WidgetType.LAYER);
		widget
			.setOriginalX(0)
			.setOriginalY(y)
			.setOriginalWidth(width)
			.setOriginalHeight(height)
			.setXPositionMode(WidgetPositionMode.ABSOLUTE_RIGHT)
			.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP)
			.setNoClickThrough(true);

		widget.revalidate();
	}

	public void createCompassMenuOp(Widget parent, int index, int x, int y)
	{
		//layer container
		Widget layer = parent.createChild(index, WidgetType.LAYER);
		layer
			.setOriginalX(x)
			.setOriginalY(y)
			.setOriginalWidth(36)
			.setOriginalHeight(36)
			.revalidate();

		Widget noClick = layer.createChild(0, WidgetType.TEXT);
		noClick
			.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setYPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setWidthMode(WidgetSizeMode.MINUS)
			.setHeightMode(WidgetSizeMode.MINUS);

		noClick.setNoClickThrough(true);
		noClick.setHasListener(true);
		noClick.revalidate();

		Widget menuOp = layer.createChild(1, WidgetType.TEXT);
		menuOp
			.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setYPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setWidthMode(WidgetSizeMode.MINUS)
			.setHeightMode(WidgetSizeMode.MINUS)
			.setAction(0, "");

		menuOp.setHasListener(true);

		//pointer args for the scripts
		var opindex0 = -2147483644;
		var component0 = -2147483645;
		var comsubid1 = -2147483643;

		menuOp.setOnOpListener(Script.TOPLEVEL_COMPASS_OP, opindex0);
		menuOp.setOnVarTransmitListener(Script.TOPLEVEL_COMPASS_SETOP, component0, comsubid1);
		menuOp.setVarTransmitTrigger(VarPlayerID.MAP_FLAGS_CACHED);
		menuOp.revalidate();
	}

	public void createMinimapElement(
		Widget parent, int index,
		int contentType,
		int spriteId,
		int x, int y,
		int width, int height,
		int xPosMode, int yPosMode)
	{
		Widget clone = parent.createChild(index, WidgetType.GRAPHIC);
		clone
			.setContentType(contentType)
			.setOriginalX(x)
			.setOriginalY(y)
			.setOriginalWidth(width)
			.setOriginalHeight(height)
			.setSpriteId(spriteId)
			.setXPositionMode(xPosMode)
			.setYPositionMode(yPosMode);

		clone.revalidate();
	}
}
