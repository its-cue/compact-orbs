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
import com.compactorbs.CompactOrbsManager;
import com.compactorbs.util.SetValue;
import com.compactorbs.util.ValueKey;
import com.compactorbs.widget.elements.Minimap;
import com.compactorbs.widget.elements.Orbs;
import com.compactorbs.widget.layout.offset.OffsetManager;
import com.compactorbs.widget.layout.slot.Slot;
import com.compactorbs.widget.layout.slot.SlotManager;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import static net.runelite.api.widgets.WidgetConfig.DRAG;
import static net.runelite.api.widgets.WidgetConfig.DRAG_ON;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.api.widgets.WidgetType;

@Slf4j
@Singleton
public class WidgetManager
{
	@Inject
	private Client client;

	@Inject
	private CompactOrbsConfig config;

	@Inject
	private CompactOrbsManager manager;

	@Inject
	private SlotManager slotManager;

	//update multiple widgets that match the script id (FORCE_UPDATE bypasses the check)
	public void remapTargets(boolean compactLayout, int scriptId, TargetWidget... widgets)
	{
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

		//ignore the widget inspector (shares same container/index as grid master)
		if (target == Orbs.GRID_MASTER_ORB_CONTAINER &&
			widget.getSpriteId() == CompactOrbsConstants.Sprite.WIDGET_INSPECTOR)
		{
			return;
		}

		boolean remapped = false;

		for (Map.Entry<ValueKey, SetValue> entry : getTarget(target).getValueMap().entrySet())
		{
			remapped |= setValue(widget, target.getArrayId(), entry.getKey(), entry.getValue(), compactLayout);
		}

		if (manager.isFixedMode())
		{
			applyFixedModeValues(widget, target);
		}

		if (remapped)
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
		if (!Orbs.SWAPPABLE_ORBS.contains((Orbs) target) || !config.enableOrbSwapping())
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
	private boolean setValue(Widget widget, int index, ValueKey key, SetValue value, boolean compactLayout)
	{
		int v = value.get(manager.getCurrentLayout(), compactLayout);
		v = OffsetManager.getTargetOffset(widget, index, key, v, compactLayout, manager, slotManager);

		switch (key)
		{
			case X:
				return updateValue(widget::getOriginalX, widget::setOriginalX, v);
			case Y:
				return updateValue(widget::getOriginalY, widget::setOriginalY, v);
			case WIDTH:
				return updateValue(widget::getOriginalWidth, widget::setOriginalWidth, v);
			case HEIGHT:
				return updateValue(widget::getOriginalHeight, widget::setOriginalHeight, v);
			case X_POSITION_MODE:
				return updateValue(widget::getXPositionMode, widget::setXPositionMode, v);
			case Y_POSITION_MODE:
				return updateValue(widget::getYPositionMode, widget::setYPositionMode, v);
			case WIDTH_MODE:
				return updateValue(widget::getWidthMode, widget::setWidthMode, v);
			case HEIGHT_MODE:
				return updateValue(widget::getHeightMode, widget::setHeightMode, v);
		}
		throw new IllegalStateException("Unhandled ValueKey (" + key + ") for widget: " + widget.getId());
	}

	//sets a value only if it has changed
	public boolean updateValue(IntSupplier getter, IntConsumer setter, int value)
	{
		if (getter.getAsInt() != value)
		{
			setter.accept(value);
			return true;
		}

		return false;
	}

	private void applyFixedModeValues(Widget widget, TargetWidget target)
	{
		if (target == Orbs.STORE_ORB_CONTAINER || target == Orbs.ACTIVITY_ORB_CONTAINER)
		{
			widget.setXPositionMode(WidgetPositionMode.ABSOLUTE_RIGHT);
		}
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

		setSelfHidden(widget, hidden);

		if (widget.getChildren() != null)
		{
			for (Widget child : widget.getChildren())
			{
				if (child != null)
				{
					setSelfHidden(child, hidden);
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

		setSelfHidden(widget, hidden);
	}

	public void setSelfHidden(Widget widget, boolean hidden)
	{
		if (hidden && !widget.isSelfHidden())
		{
			widget.setHidden(true);
		}
		else if (!hidden && widget.isSelfHidden())
		{
			widget.setHidden(false);
		}
	}

	public void setOpacity(int componentId, int opacity)
	{
		Widget widget = client.getWidget(componentId);
		if (widget == null)
		{
			return;
		}

		widget.setOpacity(opacity);

		if (widget.getChildren() != null)
		{
			for (Widget child : widget.getChildren())
			{
				if (child != null)
				{
					child.setOpacity(opacity);
				}
			}
		}
	}

	//set the opacity for the target and any related widgets
	public void setTargetOpacity(TargetWidget target, int opacity)
	{
		setOpacity(target.getComponentId(), opacity);

		if (target.getBackingId() != -1)
		{
			setOpacity(target.getBackingId(), opacity);
		}
		if (target.getButtonId() != -1)
		{
			setOpacity(target.getButtonId(), opacity);
		}
		if (target.getIndicatorId() != -1)
		{
			setOpacity(target.getIndicatorId(), opacity);
		}
		if (target.getIconId() != -1)
		{
			setOpacity(target.getIconId(), opacity);
		}
	}

	public void setNoClickThrough(int componentId, boolean noClickThrough)
	{
		Widget widget = client.getWidget(componentId);
		if (widget == null)
		{
			return;
		}

		if (widget.getNoClickThrough() != noClickThrough)
		{
			widget.setNoClickThrough(noClickThrough);
		}
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

	public Widget getMapParent()
	{
		int id = manager.isClassicResizable()
			? Minimap.CLASSIC_MAP_CONTAINER.getComponentId()
			: Minimap.MODERN_MAP_CONTAINER.getComponentId();
		return getParent(id);
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

	public void setDraggable(Widget widget)
	{
		widget.setClickMask(DRAG | DRAG_ON);
	}

	public Widget createHandler(Widget parent, int x, int y, int width, int height, int xMode, int yMode, boolean noClickThrough)
	{
		Widget child = parent.createChild(-1, WidgetType.RECTANGLE);
		child.setFilled(false);
		child.setOriginalX(x);
		child.setOriginalY(y);
		child.setOriginalWidth(width);
		child.setOriginalHeight(height);
		child.setXPositionMode(xMode);
		child.setYPositionMode(yMode);
		child.setOpacity(255);
		child.setNoClickThrough(noClickThrough);
		child.setHasListener(true);
		child.revalidate();
		return child;
	}

	public Widget createIndicator(Widget parent)
	{
		Widget child = parent.createChild(-1, WidgetType.RECTANGLE);
		child.setFilled(false);
		child.setNoClickThrough(false);
		child.setHasListener(false);
		child.setOpacity(255);
		child.revalidate();
		return child;
	}

	public void showIndicator(Widget widget, int x, int y, int width, int height, int color)
	{
		widget.setOriginalX(x);
		widget.setOriginalY(y);
		widget.setOriginalWidth(width);
		widget.setOriginalHeight(height);
		widget.setTextColor(color);
		widget.setOpacity(0);
		widget.revalidate();
	}

	public void hideIndicator(Widget widget)
	{
		widget.setOpacity(255);
		widget.revalidate();
	}

	public void syncSprite(Widget target, int componentId)
	{
		if (target == null)
		{
			return;
		}

		Widget widget = client.getWidget(componentId);
		if (widget == null)
		{
			return;
		}

		int spriteId = widget.getSpriteId();
		if (target.getSpriteId() != spriteId)
		{
			target.setSpriteId(spriteId);
		}
	}

	//check if a target widget should be updated based on script id (or FORCE_UPDATE)
	private boolean shouldUpdateTarget(TargetWidget target, int scriptId)
	{
		return (scriptId == Script.FORCE_UPDATE) || target.getScriptId() == scriptId;
	}

	public boolean exists(Widget child, Widget parent)
	{
		return child != null && child.getParentId() == parent.getId();
	}
}
