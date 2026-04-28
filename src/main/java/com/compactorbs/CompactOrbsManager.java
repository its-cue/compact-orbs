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

package com.compactorbs;

import com.compactorbs.CompactOrbsConfig.HorizontalPosition;
import com.compactorbs.CompactOrbsConfig.OrbLayout;
import com.compactorbs.CompactOrbsConfig.TogglePlacement;
import com.compactorbs.CompactOrbsConfig.VerticalPosition;
import com.compactorbs.CompactOrbsConstants.ConfigGroup;
import com.compactorbs.CompactOrbsConstants.ConfigKeys;
import com.compactorbs.CompactOrbsConstants.Enum;
import com.compactorbs.CompactOrbsConstants.Layout;
import com.compactorbs.CompactOrbsConstants.Menu;
import com.compactorbs.CompactOrbsConstants.Script;
import com.compactorbs.CompactOrbsConstants.Sprite;
import com.compactorbs.CompactOrbsConstants.VarPlayer;
import com.compactorbs.CompactOrbsConstants.Varbit;
import com.compactorbs.CompactOrbsConstants.VarbitValue;
import com.compactorbs.CompactOrbsConstants.Widgets;
import com.compactorbs.CompactOrbsConstants.Widgets.Classic;
import com.compactorbs.CompactOrbsConstants.Widgets.MinimapOverlay;
import com.compactorbs.CompactOrbsConstants.Widgets.Modern;
import com.compactorbs.widget.TargetWidget;
import com.compactorbs.widget.WidgetManager;
import com.compactorbs.widget.elements.Compass;
import com.compactorbs.widget.elements.Minimap;
import com.compactorbs.widget.elements.Orbs;
import com.compactorbs.widget.offset.Offsets;
import com.compactorbs.widget.slot.Slot;
import com.compactorbs.widget.slot.SlotConfig;
import com.compactorbs.widget.slot.SlotManager;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.api.widgets.WidgetSizeMode;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.util.ColorUtil;

@Slf4j
@Singleton
public class CompactOrbsManager
{
	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ConfigManager configManager;

	@Inject
	private CompactOrbsConfig config;

	@Inject
	private WidgetManager widgetManager;

	@Inject
	private SlotManager slotManager;

	//store the parent id from the previous widget (modern vs classic): -1 = no parent
	private int previousParentId = -1;

	//offsets for vertical and horizontal layout
	public int verticalOffset;
	public int horizontalOffset;

	//reset shutdown flag for the world map orb
	public boolean hideWorldMap;
	public boolean hideLogoutX;

	//flag for the first "logged in" game state
	public boolean initialLoginPending;

	//update flag for the custom widgets
	public boolean pendingChildrenUpdate;

	//minimap overlay flag, when layer is ready for children
	public boolean pendingMinimapOverlayChildren;

	//prevent an onConfigChanged event for a specific key
	public String suppressConfigChangedKey;

	//custom widgets created when in compact layout
	private Widget compassFrame;
	private Widget minimapButton;
	private Widget compassButton;

	private Widget overlayLogoutXStone;
	private Widget overlayLogoutXIcon;

	private final Map<String, Map.Entry<Supplier<Boolean>, TargetWidget[]>> hideByConfigMap = new HashMap<>();
	private final Map<Integer, Map.Entry<Supplier<Boolean>, TargetWidget[]>> hideByScriptMap = new HashMap<>();

	public final Map<TargetWidget, Supplier<Boolean>> orbToToggle = new HashMap<>();

	public void init(int scriptId)
	{
		updateWikiBannerVisibility(config.hideWiki());

		updateOrbByScript(scriptId);

		if (!isResized())
		{
			if (previousParentId != -1 || slotManager.allowFixedModeUpdate)
			{
				//update on initial load, when in fixed mode
				widgetManager.remapTargets(false, Script.FORCE_UPDATE, Orbs.values());

				//always hide the minimap overlay in fixed mode
				widgetManager.setHidden(Widgets.MinimapOverlay.UNIVERSE, true);

				//reset update flags
				previousParentId = -1;
				slotManager.allowFixedModeUpdate = false;
			}
			return;
		}

		build(scriptId);
	}

	private void build(int scriptId)
	{
		if (!isResized())
		{
			return;
		}

		getLayoutOffsets();

		createCustomChildren();

		if (scriptId == Script.FORCE_UPDATE)
		{
			widgetManager.setTargetsHidden(isMinimapHidden(), Minimap.values());
			widgetManager.setTargetsHidden((isMinimapHidden() && isCompassHidden()), Compass.values());
			widgetManager.remapTargets(isMinimapHidden(), Script.FORCE_UPDATE, Compass.values());
			widgetManager.remapTargets(isMinimapHidden(), Script.FORCE_UPDATE, Orbs.values());
		}
		else
		{
			widgetManager.remapTargets(isMinimapHidden(), scriptId, Orbs.values());
		}

		updateCustomChildren(pendingChildrenUpdate || scriptId == Script.FORCE_UPDATE);

		widgetManager.setTargetsNoClickthrough(config.enableNoClickthrough() && isCompactLayout(),
			Orbs.HP_ORB_CONTAINER, Orbs.PRAYER_ORB_CONTAINER, Orbs.RUN_ORB_CONTAINER, Orbs.SPEC_ORB_CONTAINER);
	}

	public void reset()
	{
		verticalOffset = 0;
		horizontalOffset = 0;

		hideWorldMap = false;
		hideLogoutX = false;

		clearCustomChildren();
		clearMinimapOverlayChildren();

		hideByConfigMap.clear();
		hideByScriptMap.clear();

		//set all orbs to visible
		widgetManager.setTargetsHidden(false, Orbs.values());

		updateWikiBannerVisibility(false);

		slotManager.reset();

		if (!isResized())
		{
			widgetManager.remapTargets(false, Script.FORCE_UPDATE, Orbs.WORLD_MAP_CONTAINER);
		}

		if (isMinimapHidden())
		{
			widgetManager.setTargetsHidden(false, Minimap.values());

			if (isCompassHidden())
			{
				widgetManager.setTargetsHidden(false, Compass.values());
			}

			widgetManager.remapTargets(false, Script.FORCE_UPDATE, Compass.values());
		}

		resetMinimapOverlayContainer();

		widgetManager.remapTargets(false, Script.FORCE_UPDATE, Orbs.values());

		widgetManager.setTargetsNoClickthrough(false,
			Orbs.HP_ORB_CONTAINER, Orbs.PRAYER_ORB_CONTAINER, Orbs.RUN_ORB_CONTAINER, Orbs.SPEC_ORB_CONTAINER);
	}

	//toggle the minimap visibility, and update related widgets when using the custom toggle button
	public void onMinimapToggle()
	{
		boolean toggle = !isMinimapHidden();
		boolean hiddenCondition = toggle && isCompassHidden();
		executeToggle(
			ConfigKeys.MINIMAP,
			config::hideMinimap,
			t ->
			{
				widgetManager.setTargetsHidden(toggle, Minimap.values());
				widgetManager.remapTargets(toggle, Script.FORCE_UPDATE, Compass.values());

				widgetManager.setTargetsHidden(hiddenCondition, Compass.values());
				widgetManager.remapTargets(toggle, Script.FORCE_UPDATE, Orbs.values());
			}
		);
	}

	//toggle the compass visibility, and update related widgets when using the custom toggle button
	private void onCompassToggle()
	{
		boolean toggle = !isCompassHidden();
		boolean remapCondition = toggle || isMinimapHidden();
		executeToggle(
			ConfigKeys.COMPASS,
			config::hideCompass,
			t ->
			{
				widgetManager.remapTargets(remapCondition, Script.FORCE_UPDATE, Compass.values());

				if (isVerticalLayout())
				{
					if (config.hideLogout() || widgetManager.getCurrentParent().getId() != Modern.ORBS)
					{
						widgetManager.remapTargets(remapCondition, Script.FORCE_UPDATE, Orbs.values());
					}
				}

				widgetManager.setTargetsHidden(toggle, Compass.values());
			}
		);
	}

	private void executeToggle(String key, Supplier<Boolean> getter, Consumer<Boolean> actions)
	{
		boolean toggle = !Boolean.TRUE.equals(getter.get());

		suppressConfigChangedKey = key;

		configManager.setConfiguration(ConfigGroup.GROUP_NAME, key, toggle);

		clientThread.invoke(() ->
		{
			getLayoutOffsets();
			actions.accept(toggle);
			updateCustomChildren(true);
			suppressConfigChangedKey = null;
		});
	}

	//create the compass frame and toggle buttons, clearing them if the parent id changed,
	//and only creating widgets if missing from the current parent
	public void createCustomChildren()
	{
		Widget parent = widgetManager.getCurrentParent();

		if (parent == null || parent.getId() != previousParentId)
		{
			clearCustomChildren();
			previousParentId = parent != null ? parent.getId() : -1;
			if (parent == null)
			{
				return;
			}
		}

		if (!widgetManager.isMissing(compassFrame, parent))
		{
			return;
		}

		compassFrame = parent.createChild(-1, WidgetType.GRAPHIC);
		compassFrame.setOriginalWidth(Layout.COMPASS_FRAME_SIZE);
		compassFrame.setOriginalHeight(Layout.COMPASS_FRAME_SIZE);
		compassFrame.setSpriteId(Sprite.COMPASS_FRAME);
		compassFrame.setOpacity(Layout.OPACITY);
		compassFrame.setHidden(false);

		minimapButton = parent.createChild(-1, WidgetType.GRAPHIC);
		minimapButton.setOriginalWidth(Layout.TOGGLE_BUTTON_SIZE);
		minimapButton.setOriginalHeight(Layout.TOGGLE_BUTTON_SIZE);
		minimapButton.setSpriteId(getSpriteId(config.hideMinimap()));
		minimapButton.setOpacity(Layout.OPACITY);
		minimapButton.setHidden(false);
		minimapButton.setHasListener(true);
		minimapButton.setAction(0, getButtonMenuOp(ConfigKeys.MINIMAP));
		minimapButton.setOnOpListener(
			(JavaScriptCallback) e ->
				onMinimapToggle()
		);
		minimapButton.setOnMouseOverListener(
			(JavaScriptCallback) e ->
			{
				if (config.rightClickToggleButtons())
				{
					//dont show on hover to distinguish between left / right click
					return;
				}
				minimapButton.setOpacity(Layout.OPACITY_HOVER);
			}
		);
		minimapButton.setOnMouseLeaveListener(
			(JavaScriptCallback) e ->
				minimapButton.setOpacity(Layout.OPACITY)
		);

		compassButton = parent.createChild(-1, WidgetType.GRAPHIC);
		compassButton.setOriginalWidth(Layout.TOGGLE_BUTTON_SIZE);
		compassButton.setOriginalHeight(Layout.TOGGLE_BUTTON_SIZE);
		compassButton.setSpriteId(getSpriteId(config.hideCompass()));
		compassButton.setOpacity(Layout.OPACITY);
		compassButton.setHidden(false);
		compassButton.setHasListener(true);
		compassButton.setAction(0, getButtonMenuOp(ConfigKeys.COMPASS));
		compassButton.setOnOpListener(
			(JavaScriptCallback) e ->
				onCompassToggle()
		);
		compassButton.setOnMouseOverListener(
			(JavaScriptCallback) e ->
			{
				if (config.rightClickToggleButtons())
				{
					return;
				}
				compassButton.setOpacity(Layout.OPACITY_HOVER);
			}
		);
		compassButton.setOnMouseLeaveListener(
			(JavaScriptCallback) e ->
				compassButton.setOpacity(Layout.OPACITY)
		);

		//prevent de-sync
		pendingChildrenUpdate = true;
	}

	//handle dynamic widget changes for visibility, positions, sprites, and menu actions for the
	//compass frame and toggle buttons (logout X being an exception in native minimap hiding)
	public void updateCustomChildren(boolean shouldUpdate)
	{
		//missing children -> create them; prevent updates until all exist
		if (compassFrame == null || compassButton == null || minimapButton == null)
		{
			//will trigger pendingChildrenUpdate when custom widgets are created
			//for the next updateCustomChildren call
			createCustomChildren();
			return;
		}

		if (isMinimapMinimized())
		{
			//put logout button back when switching to native hiding
			widgetManager.remapTargets(false, Script.FORCE_UPDATE, Orbs.LOGOUT_X_ICON, Orbs.LOGOUT_X_STONE);
		}

		//make changes when pendingChildrenUpdate (or shouldUpdate = true is passed)
		if (shouldUpdate)
		{
			updateCompassFrameChild();
			updateMinimapToggleButton();
			updateCompassToggleButton();

			//treat minimap overlay as 'custom'
			updateMinimapOverlayVisibility();

			//clear update flag
			pendingChildrenUpdate = false;
		}
	}

	public void updateCompassFrameChild()
	{
		if (compassFrame == null)
		{
			return;
		}

		compassFrame.setHidden(hideCompassFrame());
		widgetManager.updateValue(compassFrame::getOriginalX, compassFrame::setOriginalX, getCompassFrameX());
		widgetManager.updateValue(compassFrame::getOriginalY, compassFrame::setOriginalY, getCompassFrameY());
		compassFrame.revalidate();
	}

	public void updateMinimapToggleButton()
	{
		if (minimapButton == null)
		{
			return;
		}

		minimapButton.setSpriteId(getSpriteId(!isMinimapHidden()));
		minimapButton.setHidden(hideCustomToggles() || config.hideMinimapToggle());

		if (!config.hideMinimapToggle())
		{
			int index = 0;
			if (config.rightClickToggleButtons())
			{
				index = 5;
			}
			minimapButton.setAction(index == 0 ? 5 : 0, "");
			minimapButton.setAction(index, getButtonMenuOp(ConfigKeys.MINIMAP));
		}

		minimapButton.setNoClickThrough(!config.rightClickToggleButtons());
		widgetManager.updateValue(minimapButton::getOriginalX, minimapButton::setOriginalX, getMinimapButtonX());
		widgetManager.updateValue(minimapButton::getOriginalY, minimapButton::setOriginalY, getMinimapButtonY());
		minimapButton.revalidate();
	}

	public void updateCompassToggleButton()
	{
		if (compassButton == null)
		{
			return;
		}

		compassButton.setSpriteId(getSpriteId(!isCompassHidden()));
		compassButton.setHidden((hideCustomToggles() || config.hideCompassToggle()) || !isMinimapHidden());

		if (!config.hideCompassToggle())
		{
			int index = 0;
			if (config.rightClickToggleButtons())
			{
				index = 5;//below 'walk here'
			}
			compassButton.setAction(index == 0 ? 5 : 0, "");
			compassButton.setAction(index, getButtonMenuOp(ConfigKeys.COMPASS));
		}

		minimapButton.setNoClickThrough(!config.rightClickToggleButtons());
		widgetManager.updateValue(compassButton::getOriginalX, compassButton::setOriginalX, getCompassButtonX());
		widgetManager.updateValue(compassButton::getOriginalY, compassButton::setOriginalY, getCompassButtonY());
		compassButton.revalidate();
	}

	//clear any created children and reset previous parent id
	private void clearCustomChildren()
	{
		widgetManager.clearChildren(Modern.ORBS);
		widgetManager.clearChildren(Classic.ORBS);

		previousParentId = -1;

		compassFrame = null;
		minimapButton = null;
		compassButton = null;
	}

	private void clearMinimapOverlayChildren()
	{
		overlayLogoutXStone = null;
		overlayLogoutXIcon = null;
	}

	//update visibility of the minimap overlay
	public void updateMinimapOverlayVisibility()
	{
		widgetManager.setHidden(Widgets.MinimapOverlay.UNIVERSE, hideMinimapOverlay());
	}

	//set hooked layer back to default
	private void resetMinimapOverlayContainer()
	{
		configureMinimapOverlayContainer(false);
		widgetManager.clearChildren(MinimapOverlay.UNIVERSE);
	}

	//initial setup for the minimap overlay
	//@enabled - for startup/shutdown behaviour
	public void configureMinimapOverlayContainer(boolean enabled)
	{
		Widget parent = client.getWidget(Widgets.MinimapOverlay.UNIVERSE);
		if (parent == null)
		{
			return;
		}

		//don't modify if it already has been modified
		if (enabled && parent.getXPositionMode() == WidgetPositionMode.ABSOLUTE_RIGHT)
		{
			return;
		}

		if (!enabled)
		{
			parent.setForcedPosition(-1, -1);
			parent.setHidden(false);
		}

		//set layer to be used for the minimap overlay
		parent.setOriginalWidth(enabled ? Layout.MinimapOverlay.CONTAINER_WIDTH : 0);
		parent.setOriginalHeight(enabled ? Layout.MinimapOverlay.CONTAINER_HEIGHT : 0);
		parent.setWidthMode(enabled ? WidgetSizeMode.ABSOLUTE : WidgetSizeMode.MINUS);
		parent.setHeightMode(enabled ? WidgetSizeMode.ABSOLUTE : WidgetSizeMode.MINUS);
		parent.setXPositionMode(enabled ? WidgetPositionMode.ABSOLUTE_RIGHT : WidgetPositionMode.ABSOLUTE_CENTER);
		parent.setYPositionMode(enabled ? WidgetPositionMode.ABSOLUTE_TOP : WidgetPositionMode.ABSOLUTE_CENTER);
		parent.revalidate();

		//flagged as ready for children
		pendingMinimapOverlayChildren = true;
	}

	//create necessary widgets for the minimap overlay
	public void createMinimapOverlayChildren()
	{
		Widget parent = client.getWidget(MinimapOverlay.UNIVERSE);
		if (parent == null)
		{
			return;
		}

		for (int index = 0; index < Layout.MinimapOverlay.NO_CLICK_Y.length; index++)
		{
			Widget minimapNoClick = parent.createChild(-1, WidgetType.LAYER);
			minimapNoClick.setOriginalX(0);
			minimapNoClick.setOriginalY(Layout.MinimapOverlay.NO_CLICK_Y[index]);
			minimapNoClick.setOriginalWidth(Layout.MinimapOverlay.NO_CLICK_WIDTH[index]);
			minimapNoClick.setOriginalHeight(Layout.MinimapOverlay.NO_CLICK_HEIGHT[index]);
			minimapNoClick.setXPositionMode(WidgetPositionMode.ABSOLUTE_RIGHT);
			minimapNoClick.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
			minimapNoClick.setNoClickThrough(true);
			minimapNoClick.revalidate();
		}

		int compassX = Layout.Original.COMPASS_X - (Layout.Original.MAP_CONTAINER_WIDTH - Layout.MinimapOverlay.CONTAINER_WIDTH);

		Widget compass = parent.createChild(-1, WidgetType.GRAPHIC);
		compass.setContentType(Layout.MinimapOverlay.COMPASS_CONTENT);
		compass.setSpriteId(Sprite.COMPASS_MASK);
		compass.setOriginalX(compassX);
		compass.setOriginalY(Layout.Original.COMPASS_Y);
		compass.setOriginalWidth(Layout.Original.COMPASS_DIMENSION);
		compass.setOriginalHeight(Layout.Original.COMPASS_DIMENSION);
		compass.setXPositionMode(WidgetPositionMode.ABSOLUTE_LEFT);
		compass.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
		compass.revalidate();

		Widget compassLayer = parent.createChild(-1, WidgetType.LAYER);
		compassLayer.setOriginalX(compassX);
		compassLayer.setOriginalY(Layout.Original.COMPASS_Y);
		compassLayer.setOriginalWidth(Layout.COMPASS_SIZE);
		compassLayer.setOriginalHeight(Layout.COMPASS_SIZE);
		compassLayer.revalidate();

		Widget compassNoClick = compassLayer.createChild(-1, WidgetType.TEXT);
		compassNoClick.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER);
		compassNoClick.setYPositionMode(WidgetPositionMode.ABSOLUTE_CENTER);
		compassNoClick.setWidthMode(WidgetSizeMode.MINUS);
		compassNoClick.setHeightMode(WidgetSizeMode.MINUS);
		compassNoClick.setHasListener(true);
		compassNoClick.setNoClickThrough(true);
		compassNoClick.revalidate();

		Widget compassMenuOp = compassLayer.createChild(-1, WidgetType.TEXT);
		compassMenuOp.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER);
		compassMenuOp.setYPositionMode(WidgetPositionMode.ABSOLUTE_CENTER);
		compassMenuOp.setWidthMode(WidgetSizeMode.MINUS);
		compassMenuOp.setHeightMode(WidgetSizeMode.MINUS);
		compassMenuOp.setHasListener(true);
		compassMenuOp.setOnOpListener(Script.TOPLEVEL_COMPASS_OP, Script.OPINDEX0);
		compassMenuOp.setOnVarTransmitListener(Script.TOPLEVEL_COMPASS_SETOP, Script.COMPONENT0, Script.COMSUBID1);
		compassMenuOp.setVarTransmitTrigger(VarPlayer.MAP_FLAGS_CACHED);
		compassMenuOp.revalidate();

		Widget minimap = parent.createChild(-1, WidgetType.GRAPHIC);
		minimap.setContentType(Layout.MinimapOverlay.MINIMAP_CONTENT);
		minimap.setSpriteId(Sprite.MINIMAP_MASK);
		minimap.setOriginalX(Layout.Original.MINIMAP_X);
		minimap.setOriginalY(Layout.Original.MINIMAP_Y);
		minimap.setOriginalWidth(Layout.Original.MINIMAP_DIMENSION);
		minimap.setOriginalHeight(Layout.Original.MINIMAP_DIMENSION);
		minimap.setXPositionMode(WidgetPositionMode.ABSOLUTE_RIGHT);
		minimap.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
		minimap.revalidate();

		Widget minimapFrame = parent.createChild(-1, WidgetType.GRAPHIC);
		minimapFrame.setSpriteId(Sprite.MINIMAP_FRAME);
		minimapFrame.setOriginalWidth(Layout.MinimapOverlay.CONTAINER_WIDTH);
		minimapFrame.setOriginalHeight(Layout.MinimapOverlay.CONTAINER_HEIGHT);
		minimapFrame.setXPositionMode(WidgetPositionMode.ABSOLUTE_RIGHT);
		minimapFrame.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
		minimapFrame.revalidate();

		overlayLogoutXStone = parent.createChild(-1, WidgetType.GRAPHIC);
		overlayLogoutXStone.setOriginalX(Layout.Original.LOGOUT_X);
		overlayLogoutXStone.setOriginalY(Layout.Original.LOGOUT_Y);
		overlayLogoutXStone.setOriginalWidth(Layout.LOGOUT_X_WIDTH);
		overlayLogoutXStone.setOriginalHeight(Layout.LOGOUT_X_HEIGHT);
		overlayLogoutXStone.setXPositionMode(WidgetPositionMode.ABSOLUTE_RIGHT);
		overlayLogoutXStone.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
		overlayLogoutXStone.setHidden(hideOverlayLogoutX());
		overlayLogoutXStone.setHasListener(true);
		overlayLogoutXStone.setOnOpListener(
			(JavaScriptCallback) e ->
			{
				switch (e.getOp())
				{
					case 1: //Logout
						int SIDE_PANEL_LOGOUT = 10;
						client.runScript(Script.TOPLEVEL_SIDEBUTTON_OP, e.getOp(), Enum.TOPLEVEL_COMPONENTS, SIDE_PANEL_LOGOUT);
						break;

					case 2: //World switcher
						Widget w = client.getWidget(InterfaceID.Worldswitcher.BUTTONS);
						if (w == null)
						{
							client.openWorldHopper();
						}
						break;
				}
			}
			//Script.TOPLEVEL_SIDEBUTTON_OP, Script.OPINDEX0, Enum.TOPLEVEL_COMPONENTS, 10
		);
		widgetManager.syncMenuOp(overlayLogoutXStone, Modern.LOGOUT_X_STONE);
		widgetManager.syncSprite(overlayLogoutXStone, Modern.LOGOUT_X_STONE);
		overlayLogoutXStone.revalidate();

		overlayLogoutXIcon = parent.createChild(-1, WidgetType.GRAPHIC);
		overlayLogoutXIcon.setOriginalX(Layout.Original.LOGOUT_X);
		overlayLogoutXIcon.setOriginalY(Layout.Original.LOGOUT_Y);
		overlayLogoutXIcon.setOriginalWidth(Layout.LOGOUT_X_WIDTH);
		overlayLogoutXIcon.setOriginalHeight(Layout.LOGOUT_X_HEIGHT);
		overlayLogoutXIcon.setXPositionMode(WidgetPositionMode.ABSOLUTE_RIGHT);
		overlayLogoutXIcon.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
		overlayLogoutXIcon.setSpriteId(Sprite.LOGOUT_X_BUTTON);
		overlayLogoutXIcon.setHidden(hideOverlayLogoutX());
		overlayLogoutXIcon.setOpacity(100);
		overlayLogoutXIcon.revalidate();
	}

	public void updateOverlayLogoutX()
	{
		if (overlayLogoutXStone == null || overlayLogoutXIcon == null)
		{
			return;
		}

		widgetManager.syncSprite(overlayLogoutXStone, Modern.LOGOUT_X_STONE);
		widgetManager.syncMenuOp(overlayLogoutXStone, Modern.LOGOUT_X_STONE);

		if (!hideOverlayLogoutX())
		{
			widgetManager.syncHidden(overlayLogoutXStone, Modern.LOGOUT_X_STONE);
			widgetManager.syncHidden(overlayLogoutXIcon, Modern.LOGOUT_X_ICON);
		}
		else
		{
			if (!overlayLogoutXStone.isHidden())
			{
				overlayLogoutXStone.setHidden(true);
			}

			if (!overlayLogoutXIcon.isHidden())
			{
				overlayLogoutXIcon.setHidden(true);
			}
		}
	}

	public void updateWikiBannerVisibility(boolean hidden)
	{
		Widget container = widgetManager.getTargetWidget(Orbs.WIKI_ICON_CONTAINER);
		if (container == null)
		{
			return;
		}

		Widget banner = widgetManager.getTargetWidget(Orbs.WIKI_VANILLA_CONTAINER);
		if (customWikiBanner(container))
		{
			banner = container.getChild(0);
		}

		if (banner == null)
		{
			return;
		}

		//vanilla banner should be hidden if the in-game setting is disabled
		if (isWikiBannerDisabled() && !customWikiBanner(container))
		{
			hidden = true;
		}

		banner.setHidden(hidden);
	}

	private boolean customWikiBanner(Widget container)
	{
		if (container == null)
		{
			return false;
		}
		return (container.getDynamicChildren() != null && container.getDynamicChildren().length > 0);
	}

	void getLayoutOffsets()
	{
		boolean isCompactLayout = (isResized() && isMinimapHidden());

		//zero out
		verticalOffset = 0;
		horizontalOffset = 0;

		if (isVerticalLayout())
		{
			if (isCompactLayout)
			{
				if (isVerticalLeft())
				{
					verticalOffset = Layout.Vertical.LEFT_OFFSET;
				}
				else
				{
					verticalOffset = Layout.Vertical.RIGHT_OFFSET;
				}
			}

			if (config.hideCompass())
			{
				if (config.hideLogout() || widgetManager.getCurrentParent().getId() != Modern.ORBS)
				{
					if (config.enableVerticalHeightOffset() && !config.disableReordering())
					{
						horizontalOffset -= 26;
					}
				}
			}
		}

		if (isHorizontalLayout())
		{
			if (isCompactLayout)
			{
				if (isHorizontalBottom())
				{
					horizontalOffset = Layout.Horizontal.BOTTOM_OFFSET;
				}
				else
				{
					horizontalOffset = Layout.Horizontal.TOP_OFFSET;
				}
			}
		}

		getWorldMapOffset();
	}

	public int getWorldMapOffset()
	{
		return (hideWorldMap ? 1 - Layout.WORLD_MAP_CONTAINER_WIDTH : 0);
	}

	private int getSpriteId(boolean hidden)
	{
		return hidden ? Sprite.VISIBLE : Sprite.HIDDEN;
	}

	private String getButtonMenuOp(String key)
	{
		final boolean isMinimap = ConfigKeys.MINIMAP.equals(key);
		final boolean isHidden = isMinimap ? isMinimapHidden() : isCompassHidden();

		String target = isMinimap ? Menu.SUFFIX_MINIMAP : Menu.SUFFIX_COMPASS;
		String action = isHidden ? Menu.PREFIX_SHOW : Menu.PREFIX_HIDE;

		return getMenuOp(action, target);
	}

	String getMenuOp(String action, String target)
	{
		return action + " " + ColorUtil.wrapWithColorTag(target, Menu.COLOR);
	}

	public int getLayout()
	{
		return config.layout().getIndex();
	}

	public boolean isResized()
	{
		return client.isResized();
	}

	public boolean isLoggedIn()
	{
		return client.getGameState() == GameState.LOGGED_IN;
	}

	public boolean isHopping()
	{
		return client.getGameState() == GameState.HOPPING;
	}

	public boolean isCompactLayout()
	{
		return isResized() && isMinimapHidden() && !isMinimapMinimized();
	}

	public boolean enableOrbSwapping()
	{
		return config.enableOrbSwapping();
	}

	public boolean isVerticalLayout()
	{
		return config.layout() == OrbLayout.VERTICAL;
	}

	public boolean isVerticalLeft()
	{
		return config.verticalPosition() == VerticalPosition.LEFT;
	}

	public boolean isVerticalRight()
	{
		return config.verticalPosition() == VerticalPosition.RIGHT;
	}

	public boolean isHorizontalLayout()
	{
		return config.layout() == OrbLayout.HORIZONTAL;
	}

	public boolean isHorizontalBottom()
	{
		return config.horizontalPosition() == HorizontalPosition.BOTTOM;
	}

	public boolean isHorizontalTop()
	{
		return config.horizontalPosition() == HorizontalPosition.TOP;
	}

	public boolean preventReordering()
	{
		return config.disableReordering();
	}

	public boolean leaveEmptySpace()
	{
		return config.leaveEmptySpace();
	}

	public boolean isMinimapHidden()
	{
		return config.hideMinimap();
	}

	public boolean isCompassHidden()
	{
		return config.hideCompass();
	}

	public boolean isXpDropHidden()
	{
		return config.hideXp();
	}

	private boolean hideCompassFrame()
	{
		return !isMinimapHidden() || isCompassHidden() || isMinimapMinimized();
	}

	private boolean hideCustomToggles()
	{
		return (config.hideMinimapToggle() && config.hideCompassToggle()) || isMinimapMinimized();
	}

	private boolean hideMinimapOverlay()
	{
		return !(isMinimapOverlayEnabled() && isMinimapHidden() && !isMinimapMinimized()) || !isResized();
	}

	public boolean isMinimapOverlayEnabled()
	{
		return config.showMinimapInCompactView();
	}

	public boolean hideOverlayLogoutX()
	{
		if (widgetManager.getCurrentParent() == null)
		{
			return true;
		}

		return widgetManager.getCurrentParent().getId() != Modern.ORBS || !config.showOverlayLogoutX();
	}

	public boolean showOverlayLogoutX()
	{
		return config.showOverlayLogoutX();
	}

	public void handleLogoutXHiddenState(boolean configChanged)
	{
		if (!isResized() || isMinimapMinimized())
		{
			return;
		}

		if (configChanged)
		{
			widgetManager.remapTargets(isCompactLayout(), Script.FORCE_UPDATE, Orbs.LOGOUT_X_STONE, Orbs.LOGOUT_X_ICON);

			//only reliable way i could come up with to prevent stale hidden state for each logout x
			//could always just let the scripts handle the update, but it would need to be triggered
			//which felt bad during a toggle event
			client.runScript(Script.TOPLEVEL_SIDE_CUSTOMIZE, Enum.TOPLEVEL_COMPONENTS);
		}
		else
		{
			if (hideLogoutX && (!showOverlayLogoutX() || !isMinimapOverlayEnabled()))
			{
				widgetManager.setTargetsHidden(true, Orbs.LOGOUT_X_STONE, Orbs.LOGOUT_X_ICON);
			}
		}
	}

	public boolean isActivityOrbDisabled()
	{
		return client.getVarbitValue(Varbit.ACTIVITY_ORB_TOGGLE) != VarbitValue.ACTIVITY_ORB_VISIBLE;
	}

	public boolean isStoreOrbDisabled()
	{
		return client.getVarbitValue(Varbit.STORE_ORB_TOGGLE) != VarbitValue.STORE_ORB_VISIBLE;
	}

	public boolean isWikiBannerDisabled()
	{
		return client.getVarbitValue(Varbit.WIKI_ICON_TOGGLE) != VarbitValue.WIKI_ICON_VISIBLE;
	}

	public boolean isMinimapMinimized()
	{
		return client.getVarbitValue(Varbit.MINIMAP_TOGGLE) == VarbitValue.MINIMAP_MINIMIZED;
	}

	public boolean isCutsceneActive()
	{
		return client.getVarbitValue(Varbit.CUTSCENE_STATUS) == VarbitValue.CUTSCENE_ACTIVE;
	}

	//custom children offset handling~ similar to OffsetTarget interface
	private int getCompassFrameX()
	{
		return Offsets.COMPASS.getOffset().getOffsetX() - Layout.FRAME_X_OFFSET;
	}

	private int getCompassFrameY()
	{
		return Offsets.COMPASS.getOffset().getOffsetY() - Layout.FRAME_Y_OFFSET;
	}

	private int getMinimapButtonX()
	{
		boolean vertical = isVerticalLayout() && isMinimapHidden();
		boolean horizontal = isHorizontalLayout() && isMinimapHidden();

		int x = config.minimapTogglePlacement().getX();

		if (config.minimapTogglePlacement() == TogglePlacement.BELOW_MAP
			&& config.hideStore()
			&& !isMinimapHidden())
		{
			//offset when store is hidden, and minimap is visible
			x -= 33;
		}

		if (vertical)
		{
			return Layout.DEFAULT_MINIMAP_BUTTON_X - verticalOffset;
		}

		if (horizontal)
		{
			x = Layout.DEFAULT_MINIMAP_BUTTON_X;

			if (config.hideWiki() && !preventReordering())
			{
				//offset when wiki is hidden, in horizontal layout
				x -= 40;
			}

			if (isVerticalLeft())
			{
				int hiddenWidth = slotManager.getHorizontalHiddenWidth();
				x -= hiddenWidth;
			}

			return x;
		}

		return x;
	}

	private int getMinimapButtonY()
	{
		boolean vertical = isVerticalLayout() && isMinimapHidden();
		boolean horizontal = isHorizontalLayout() && isMinimapHidden();

		int y = config.minimapTogglePlacement().getY();

		if (config.minimapTogglePlacement() == TogglePlacement.BELOW_MAP
			&& config.hideStore()
			&& !isMinimapHidden())
		{
			//offset when store is hidden and minimap is visible
			y -= 5;
		}

		if (vertical)
		{
			//original vertical y
			y = Layout.DEFAULT_MINIMAP_BUTTON_Y;

			if (isHorizontalTop())
			{
				y += horizontalOffset;

				//anchor minimap button to wiki icon container in vertical
				y = slotManager.applyHiddenYOffset(Orbs.WIKI_ICON_CONTAINER, y);

				//apply an offset when the amount hidden, leaves wiki slot as the last
				//0-HP, 1-Pray, 2-Run, 3-Spec, 4-Wiki
				if (slotManager.getHiddenCountAbove(Orbs.WIKI_ICON_CONTAINER) == Slot.VERTICAL_RIGHT_COLUMN.indexOf(Slot.WIKI_SLOT)
					&& !preventReordering())
				{
					//match wiki container offset @WikiContainerOffset
					y += 10;

					if (config.hideWiki())
					{
						//offset if wiki is hidden
						y -= 20;

						if (config.hideLogout())
						{
							//align to compass toggle, when X is hidden
							y -= 4;
						}
					}
				}
			}
			return y;
		}

		if (horizontal)
		{
			return ((Layout.DEFAULT_MINIMAP_BUTTON_Y) / 2) + horizontalOffset - 10; //- 10: apply the horizontal Y change in constants
		}

		return y;
	}

	private int getCompassButtonX()
	{
		int x = Offsets.COMPASS.getOffset().getOffsetX()
			+ Layout.COMPASS_BUTTON_X_OFFSET;

		if (isHorizontalLayout())
		{
			x += Layout.COMPASS_BUTTON_HORIZONTAL_X_OFFSET;
		}

		if (isVerticalLayout())
		{
			if (config.enableNoClickthrough())
			{
				x += 3;
			}
		}

		return x;
	}

	private int getCompassButtonY()
	{
		int y = Offsets.COMPASS.getOffset().getOffsetY()
			+ Layout.COMPASS_BUTTON_Y_OFFSET;

		if (isHorizontalLayout())
		{
			y -= Layout.COMPASS_BUTTON_HORIZONTAL_Y_OFFSET;
		}

		if (isVerticalLayout())
		{
			if (config.enableNoClickthrough())
			{
				y -= 4;
			}
		}

		return y;
	}

	//register an orb toggle entry in the config and script maps
	public void registerOrbToggle(String key, Supplier<Boolean> toggle, TargetWidget... targets)
	{
		hideByConfigMap.put(key, Map.entry(toggle, targets));

		int scriptId = Script.FORCE_UPDATE;

		for (TargetWidget target : targets)
		{
			//match orb to toggle, used for slots
			orbToToggle.put(target, toggle);

			if (scriptId == Script.FORCE_UPDATE && target instanceof Orbs)
			{
				scriptId = target.getScriptId();
			}
		}

		if (scriptId != Script.FORCE_UPDATE)
		{
			hideByScriptMap.put(scriptId, Map.entry(toggle, targets));
		}
	}

	//apply toggle setting to orbs based on the fired script id; pass FORCE_UPDATE for all toggles
	private void updateOrbByScript(int scriptId)
	{
		(scriptId == Script.FORCE_UPDATE ? hideByConfigMap : hideByScriptMap).forEach(
			(key, value) ->
			{
				//don't actually hide, since handled elsewhere
				if (key.equals(ConfigKeys.HIDE_LOGOUT_X) ||
					key.equals(ConfigKeys.HIDE_WORLD) ||
					key.equals(ConfigKeys.HIDE_WIKI) ||
					key.equals(Script.WORLD_MAP_UPDATE) ||
					key.equals(Script.WIKI_ICON_UPDATE))
				{
					return;
				}

				widgetManager.setTargetsHidden(value.getKey().get(), value.getValue());
			}
		);
	}

	//apply toggle setting to orbs based on the config key in onConfigChanged
	public void updateOrbByConfig(String key)
	{
		//world map config handling for onConfigChanged
		switch (key)
		{
			case ConfigKeys.HIDE_WORLD:
				hideWorldMap = config.hideWorld();
				widgetManager.remapTarget(Orbs.WORLD_MAP_CONTAINER, isMinimapHidden() && isResized());
				break;

			//wiki banner config handling for onConfigChanged
			case ConfigKeys.HIDE_WIKI:
				updateWikiBannerVisibility(config.hideWiki());
				warnWikiPluginConflict();

				//update the minimap toggle button when in horizontal layout,
				//and minimap is hidden (offset is applied that needs updated)
				if (isHorizontalLayout() && isMinimapHidden())
				{
					updateMinimapToggleButton();
				}
				break;

			case ConfigKeys.HIDE_LOGOUT_X:
				hideLogoutX = config.hideLogout();
				handleLogoutXHiddenState(true);
				break;

			default:
				Map.Entry<Supplier<Boolean>, TargetWidget[]> entry = hideByConfigMap.get(key);
				if (entry != null)
				{
					//set orb visibility based on config
					widgetManager.setTargetsHidden(entry.getKey().get(), entry.getValue());

					//update the minimap toggle button when hiding/showing store orb,
					//while minimap is hidden, and button position is below map
					if (key.equals(ConfigKeys.HIDE_STORE)
						&& config.minimapTogglePlacement() == TogglePlacement.BELOW_MAP
						&& !isMinimapHidden() && isLoggedIn())
					{
						updateMinimapToggleButton();
					}
				}
				break;
		}
	}

	//update a slots config for the passed widget value @SlotManager
	public void updateConfigForSlot(Slot key, TargetWidget value, SlotManager.SlotLayoutMode layout)
	{
		SlotConfig entry = key.getSlotConfigMap().get(layout);
		if (entry != null && entry.getConfigKey() != null)
		{
			configManager.setConfiguration(ConfigGroup.GROUP_NAME, entry.getConfigKey(), value);
		}
	}

	public boolean isWikiPluginBannerActive()
	{
		boolean wikiPluginActive = Boolean.TRUE.equals(
			configManager.getConfiguration(ConfigGroup.RuneLite.GROUP_NAME, ConfigKeys.RuneLite.WIKI_PLUGIN, Boolean.class)
		);

		boolean showWikiMinimapButton = Boolean.TRUE.equals(
			configManager.getConfiguration(ConfigGroup.Wiki.GROUP_NAME, ConfigKeys.Wiki.SHOW_WIKI_MINIMAP_BUTTON, Boolean.class)
		);

		return wikiPluginActive && showWikiMinimapButton;
	}

	public void warnWikiPluginConflict()
	{
		if (isWikiPluginBannerActive() && config.hideWiki())
		{
			sendMessage(msg ->
				msg
					.append("the ")
					.append(ChatColorType.HIGHLIGHT)
					.append("`Hide Wiki banner` ")
					.append(ChatColorType.NORMAL)
					.append("setting is overriding the Wiki plugin's ")
					.append(ChatColorType.HIGHLIGHT)
					.append("`Show wiki button under minimap` ")
					.append(ChatColorType.NORMAL)
					.append("setting.")
			);
		}
	}

	private void sendMessage(Consumer<ChatMessageBuilder> consumer)
	{
		if (!isLoggedIn())
		{
			return;
		}

		ChatMessageBuilder builder = new ChatMessageBuilder()
			.append("[")
			.append(ChatColorType.HIGHLIGHT)
			.append("Compact Orbs")
			.append(ChatColorType.NORMAL)
			.append("] ");

		consumer.accept(builder);

		String input = builder.build();

		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.CONSOLE)
			.runeLiteFormattedMessage(input)
			.build());
	}
}
