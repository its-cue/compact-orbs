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
import com.compactorbs.CompactOrbsConstants.Enum;
import com.compactorbs.CompactOrbsConstants.Layout;
import com.compactorbs.CompactOrbsConstants.Layout.MinimapOverlay;
import com.compactorbs.CompactOrbsConstants.Layout.Original;
import com.compactorbs.CompactOrbsConstants.MenuOp;
import com.compactorbs.CompactOrbsConstants.Script;
import com.compactorbs.CompactOrbsConstants.Sprite;
import com.compactorbs.CompactOrbsConstants.VarPlayer;
import com.compactorbs.CompactOrbsConstants.Widgets.Classic;
import com.compactorbs.CompactOrbsConstants.Widgets.Fixed;
import com.compactorbs.CompactOrbsConstants.Widgets.Modern;
import com.compactorbs.CompactOrbsLayout;
import com.compactorbs.CompactOrbsManager;
import com.compactorbs.util.SetValue;
import com.compactorbs.util.ValueKey;
import com.compactorbs.widget.elements.Minimap;
import com.compactorbs.widget.elements.Orbs;
import com.compactorbs.widget.layout.offset.OffsetManager;
import com.compactorbs.widget.layout.slot.Slot;
import com.compactorbs.widget.layout.slot.SlotManager;
import com.compactorbs.widget.layout.slot.SlotRegistry;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
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
	private CompactOrbsConfig config;

	@Inject
	private CompactOrbsManager manager;

	@Inject
	private SlotManager slotManager;

	@Inject
	private SlotRegistry slotRegistry;

	public void remapTargets(TargetWidget... targets)
	{
		remapTargets(false, Script.FORCE_UPDATE, targets);
	}

	public void remapTargetsByScriptId(int scriptId, TargetWidget... targets)
	{
		remapTargets(false, scriptId, targets);
	}

	//should only be called on shutdown with toDefault being true
	public void remapTargets(boolean toDefault, int scriptId, TargetWidget... targets)
	{
		if (!toDefault)
		{
			slotManager.updateCurrentLayoutMode();
		}

		for (TargetWidget target : targets)
		{
			if (!shouldUpdateTarget(target, scriptId))
			{
				continue;
			}

			remapTarget(toDefault, target);
		}
	}

	private void remapTarget(boolean toDefault, TargetWidget target)
	{
		if (target == null)
		{
			return;
		}

		Widget widget = getTargetWidget(target);
		if (widget == null)
		{
			return;
		}

		boolean remapped = false;
		for (Map.Entry<ValueKey, SetValue> entry : getTarget(target).getValueMap().entrySet())
		{
			remapped |= setValue(widget, target.getArrayId(), entry.getKey(), entry.getValue(), toDefault);
		}

		if (remapped)
		{
			widget.revalidate();
		}
	}

	private TargetWidget getTarget(TargetWidget target)
	{
		if (target instanceof Orbs)
		{
			return getSlotTarget(target);
		}

		return target;
	}

	private TargetWidget getSlotTarget(TargetWidget target)
	{
		if (!Orbs.isSwappableOrb(target.getComponentId()) || !manager.enableOrbSwapping)
		{
			return target;
		}

		Slot slot = slotManager.findSlot(target);
		if (slot == null)
		{
			return target;
		}

		return slot.getDefaultTarget();
	}

	private int getSavedValue(Widget widget, int index, ValueKey key, boolean useSavedPosition)
	{
		if (!useSavedPosition || (key != ValueKey.X && key != ValueKey.Y))
		{
			return -1;
		}

		return manager.getSavedPosition(widget, index, key);
	}

	private int getValue(Widget widget, int index, ValueKey key, SetValue value, boolean toDefault)
	{
		boolean useSavedPosition =
			!toDefault
				&& (manager.isCompactLayout()
				|| (manager.isVanillaCustom() && manager.useSavedPosition(widget, index)));

		CompactOrbsLayout layout =
			manager.isCompactLayout()
				? manager.getCurrentLayout()
				: null;

		int v = useSavedPosition
			? value.getModified(layout)
			: value.getOriginal();

		int saved = getSavedValue(widget, index, key, useSavedPosition);
		if (saved != -1)
		{
			return saved;
		}

		//offsets return fixed mode positions early (not stored in the Orbs enum)
		if (!toDefault || manager.isFixedMode())
		{
			v = OffsetManager.getTargetOffset(widget, index, key, v, manager, slotManager);
		}

		return v;
	}

	//the wiki banners container is too big (that's what she said) when the minimap is visible
	private int adjustValue(Widget widget, int index, ValueKey key, int value, boolean toDefault)
	{
		if (!manager.isVanillaCustom() || toDefault)
		{
			return value;
		}

		switch (key)
		{
			case Y:
				if (isWikiContainer(widget)
					&& manager.getSavedPosition(widget, index, key) == -1)
				{
					return value + 10;
				}
				break;

			case HEIGHT:
				if (isWikiContainer(widget))
				{
					return value - 20;
				}
				break;

			case X_POSITION_MODE:
				if (isWikiElement(widget))
				{
					return WidgetPositionMode.ABSOLUTE_LEFT;
				}
				break;

			case Y_POSITION_MODE:
				if (isWikiElement(widget))
				{
					return WidgetPositionMode.ABSOLUTE_TOP;
				}
				break;
		}

		return value;
	}

	private boolean setValue(Widget widget, int index, ValueKey key, SetValue value, boolean toDefault)
	{
		int v = getValue(widget, index, key, value, toDefault);
		v = adjustValue(widget, index, key, v, toDefault);

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

	public boolean updateValue(IntSupplier getter, IntConsumer setter, int value)
	{
		if (getter.getAsInt() != value)
		{
			setter.accept(value);
			return true;
		}

		return false;
	}

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

	public void setHidden(TargetWidget target, boolean hidden)
	{
		Widget widget = getTargetWidget(target);
		if (widget == null)
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
		if (manager.isFixedMode())
		{
			return getParent(Fixed.ORBS);
		}

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
		if (manager.isFixedMode())
		{
			return getParent(Fixed.MAP_CONTAINER);
		}
		else if (manager.isClassicResizable())
		{
			return getParent(Minimap.CLASSIC_MAP_CONTAINER.getComponentId());
		}
		else
		{
			return getParent(Minimap.MODERN_MAP_CONTAINER.getComponentId());
		}
	}

	public Widget getMinimapMask()
	{
		if (manager.isFixedMode())
		{
			return client.getWidget(Fixed.MINIMAP_MASK);
		}
		else if (manager.isClassicResizable())
		{
			return client.getWidget(Classic.MINIMAP_MASK);
		}
		else
		{
			return client.getWidget(Modern.MINIMAP_MASK);
		}
	}

	public void clearChild(Widget child)
	{
		if (child == null)
		{
			return;
		}

		Widget widget = child.getParent();
		if (widget == null)
		{
			return;
		}

		Widget[] children = widget.getChildren();
		if (children == null || children.length <= child.getIndex() || children[child.getIndex()] != child)
		{
			return;
		}

		children[child.getIndex()] = null;
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

	public void removeMinimapRendering()
	{
		Widget widget = getMinimapMask();
		if (widget == null)
		{
			return;
		}

		widget.setType(WidgetType.LAYER);
		widget.setContentType(0);
		widget.setSpriteId(-1);
	}

	public void restoreMinimapRendering()
	{
		Widget widget = getMinimapMask();
		if (widget == null)
		{
			return;
		}

		widget.setType(WidgetType.GRAPHIC);
		widget.setContentType(MinimapOverlay.MINIMAP_CONTENT);
		widget.setSpriteId(manager.isFixedMode() ? Sprite.FIXED_MINIMAP_MASK : Sprite.MINIMAP_MASK);
	}

	public Widget createMinimapButton(Widget parent)
	{
		Widget widget = parent.createChild(-1, WidgetType.GRAPHIC)
			.setOriginalX(5)
			.setOriginalWidth(Layout.TOGGLE_BUTTON_SIZE)
			.setOriginalHeight(Layout.TOGGLE_BUTTON_SIZE)
			.setSpriteId(getSpriteId(!manager.isMinimapHidden()))
			.setOpacity(Layout.OPACITY)
			.setHidden(false)
			.setHasListener(true);
		widget.revalidate();
		return widget;
	}

	public Widget createCompassFrame(Widget parent)
	{
		Widget widget = parent.createChild(-1, WidgetType.GRAPHIC)
			.setOriginalWidth(Layout.COMPASS_FRAME_SIZE)
			.setOriginalHeight(Layout.COMPASS_FRAME_SIZE)
			.setSpriteId(Sprite.COMPASS_FRAME)
			.setOpacity(Layout.OPACITY)
			.setHidden(false);
		widget.revalidate();
		return widget;
	}

	public Widget createNoClick(Widget parent, Widget child)
	{
		Widget widget = parent.createChild(0, WidgetType.LAYER)
			.setOriginalX(child.getOriginalX())
			.setOriginalY(child.getOriginalY())
			.setOriginalWidth(child.getOriginalWidth())
			.setOriginalHeight(child.getOriginalHeight());
		widget.setNoClickThrough(true);
		widget.revalidate();
		return widget;
	}

	public Widget createOverlayNoClick(Widget parent, int y, int width, int height)
	{
		Widget widget = parent.createChild(-1, WidgetType.LAYER)
			.setOriginalX(0)
			.setOriginalY(y)
			.setOriginalWidth(width)
			.setOriginalHeight(height)
			.setXPositionMode(WidgetPositionMode.ABSOLUTE_RIGHT)
			.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
		widget.setNoClickThrough(true);
		return widget;
	}

	public Widget createOverlayCompass(Widget parent)
	{
		Widget widget = parent.createChild(-1, WidgetType.GRAPHIC)
			.setContentType(MinimapOverlay.COMPASS_CONTENT)
			.setSpriteId(Sprite.COMPASS_MASK)
			.setOriginalX(Original.COMPASS_X - (Original.MAP_CONTAINER_WIDTH - MinimapOverlay.CONTAINER_WIDTH))
			.setOriginalY(Original.COMPASS_Y)
			.setOriginalWidth(Original.COMPASS_DIMENSION)
			.setOriginalHeight(Original.COMPASS_DIMENSION)
			.setXPositionMode(WidgetPositionMode.ABSOLUTE_LEFT)
			.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
		widget.revalidate();
		return widget;
	}

	public Widget createOverlayCompassLayer(Widget parent)
	{
		Widget widget = parent.createChild(-1, WidgetType.LAYER)
			.setOriginalX(Original.COMPASS_X - (Original.MAP_CONTAINER_WIDTH - MinimapOverlay.CONTAINER_WIDTH))
			.setOriginalY(Original.COMPASS_Y)
			.setOriginalWidth(Layout.COMPASS_SIZE)
			.setOriginalHeight(Layout.COMPASS_SIZE);
		widget.revalidate();
		return widget;
	}

	public Widget createOverlayCompassNoClick(Widget parent)
	{
		Widget widget = parent.createChild(-1, WidgetType.TEXT)
			.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setYPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setWidthMode(WidgetSizeMode.MINUS)
			.setHeightMode(WidgetSizeMode.MINUS)
			.setHasListener(true);
		widget.setNoClickThrough(true);
		widget.revalidate();
		return widget;
	}

	public Widget createOverlayCompassMenuOp(Widget parent)
	{
		Widget widget = parent.createChild(-1, WidgetType.TEXT)
			.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setYPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setWidthMode(WidgetSizeMode.MINUS)
			.setHeightMode(WidgetSizeMode.MINUS)
			.setHasListener(true);
		widget.setOnOpListener(Script.TOPLEVEL_COMPASS_OP, Script.OPINDEX0);
		widget.setOnVarTransmitListener(Script.TOPLEVEL_COMPASS_SETOP, Script.COMPONENT0, Script.COMSUBID1);
		widget.setVarTransmitTrigger(VarPlayer.MAP_FLAGS_CACHED);
		widget.revalidate();
		return widget;
	}

	public Widget createOverlayMinimap(Widget parent)
	{
		Widget widget = parent.createChild(-1, WidgetType.GRAPHIC)
			.setContentType(MinimapOverlay.MINIMAP_CONTENT)
			.setSpriteId(Sprite.MINIMAP_MASK)
			.setOriginalX(Original.MINIMAP_X)
			.setOriginalY(Original.MINIMAP_Y)
			.setOriginalWidth(Original.MINIMAP_DIMENSION)
			.setOriginalHeight(Original.MINIMAP_DIMENSION)
			.setXPositionMode(WidgetPositionMode.ABSOLUTE_RIGHT)
			.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
		widget.revalidate();
		return widget;
	}

	public Widget createOverlayMinimapFrame(Widget parent)
	{
		Widget widget = parent.createChild(-1, WidgetType.GRAPHIC)
			.setSpriteId(Sprite.MINIMAP_FRAME)
			.setOriginalWidth(MinimapOverlay.CONTAINER_WIDTH)
			.setOriginalHeight(MinimapOverlay.CONTAINER_HEIGHT)
			.setXPositionMode(WidgetPositionMode.ABSOLUTE_RIGHT)
			.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
		widget.revalidate();
		return widget;
	}

	public Widget createOverlayLogoutXStone(Widget parent, boolean hidden)
	{
		Widget widget = parent.createChild(-1, WidgetType.GRAPHIC)
			.setOriginalX(Original.LOGOUT_X)
			.setOriginalY(Original.LOGOUT_Y)
			.setOriginalWidth(Layout.LOGOUT_X_WIDTH)
			.setOriginalHeight(Layout.LOGOUT_X_HEIGHT)
			.setXPositionMode(WidgetPositionMode.ABSOLUTE_RIGHT)
			.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP)
			.setHidden(hidden)
			.setHasListener(true);
		widget.setAction(0, MenuOp.LOGOUT_OP);
		widget.setOnOpListener(Script.TOPLEVEL_SIDEBUTTON_OP, Script.OPINDEX0, Enum.TOPLEVEL_COMPONENTS, 10);
		syncSprite(widget, Modern.LOGOUT_X_STONE);
		widget.revalidate();
		return widget;
	}

	public Widget createOverlayLogoutXIcon(Widget parent, boolean hidden)
	{
		Widget widget = parent.createChild(-1, WidgetType.GRAPHIC)
			.setOriginalX(Original.LOGOUT_X)
			.setOriginalY(Original.LOGOUT_Y)
			.setOriginalWidth(Layout.LOGOUT_X_WIDTH)
			.setOriginalHeight(Layout.LOGOUT_X_HEIGHT)
			.setXPositionMode(WidgetPositionMode.ABSOLUTE_RIGHT)
			.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP)
			.setSpriteId(Sprite.LOGOUT_X_BUTTON)
			.setHidden(hidden)
			.setOpacity(100);
		widget.revalidate();
		return widget;
	}

	public int getSpriteId(boolean hidden)
	{
		return hidden ? Sprite.VISIBLE : Sprite.HIDDEN;
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

	private boolean isWikiContainer(Widget widget)
	{
		return widget.getId() == Orbs.WIKI_ICON_CONTAINER.getComponentId() &&
			widget.getIndex() == Orbs.WIKI_ICON_CONTAINER.getArrayId();
	}

	private boolean isWikiElement(Widget widget)
	{
		return (widget.getId() == Orbs.WIKI_PLUGIN_ICON.getComponentId() &&
			widget.getIndex() == Orbs.WIKI_PLUGIN_ICON.getArrayId()) ||

			(widget.getId() == Orbs.WIKI_VANILLA_CONTAINER.getComponentId() &&
				widget.getIndex() == Orbs.WIKI_VANILLA_CONTAINER.getArrayId()) ||

			(widget.getId() == Orbs.WIKI_VANILLA_ICON.getComponentId() &&
				widget.getIndex() == Orbs.WIKI_VANILLA_ICON.getArrayId());
	}

	//check if a target widget should be updated based on script id (or FORCE_UPDATE)
	private boolean shouldUpdateTarget(TargetWidget target, int scriptId)
	{
		return (scriptId == Script.FORCE_UPDATE) || target.getScriptId() == scriptId;
	}
}
