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

import com.compactorbs.CompactOrbsConfig.TogglePlacement;
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
import com.compactorbs.widget.layout.offset.Offsets;
import com.compactorbs.widget.layout.slot.Slot;
import com.compactorbs.widget.layout.slot.SlotConfig;
import com.compactorbs.widget.layout.slot.SlotManager;
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
import net.runelite.api.Point;
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

	private int previousParentId = -1;

	//flags for shutdown, otherwise synced to the corresponding config state
	public boolean hideWorldMap;
	public boolean hideLogoutX;
	public boolean enableNoClickThrough;

	//limit the warning message from potentially being spammy
	public boolean hasSeenWikiWarning;

	//flag for the first "logged in" game state
	public boolean initialLoginPending;

	//update flag for the custom widgets
	public boolean pendingChildrenUpdate;

	//minimap overlay flag, when layer is ready for children
	public boolean pendingMinimapOverlayChildren;

	public boolean updateFixedMode;

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

		if (isFixedMode())
		{
			if (updateFixedMode)
			{
				widgetManager.setHidden(Widgets.MinimapOverlay.UNIVERSE, true);
				widgetManager.remapTargets(false, Script.FORCE_UPDATE, Orbs.values());
				updateNoClickThrough();
				setupMinimapContainer(false);
				updateFixedMode = false;
			}
			return;
		}

		build(scriptId);
	}

	private void build(int scriptId)
	{
		createCustomChildren();

		if (scriptId == Script.FORCE_UPDATE)
		{
			setupMinimapContainer(isMinimapHidden() && !isMinimapMinimized());
			widgetManager.setTargetsHidden((isMinimapHidden() && isCompassHidden()), Compass.values());
			widgetManager.remapTargets(isMinimapHidden(), Script.FORCE_UPDATE, Compass.values());
			widgetManager.remapTargets(isMinimapHidden(), Script.FORCE_UPDATE, Orbs.values());
		}
		else
		{
			widgetManager.remapTargets(isMinimapHidden(), scriptId, Orbs.values());
		}

		updateCustomChildren(pendingChildrenUpdate || scriptId == Script.FORCE_UPDATE);
		updateNoClickThrough();
	}

	//update positions (used when toggling orb visibility)
	public void updateLayout()
	{
		if (widgetManager.getCurrentParent() == null)
		{
			return;
		}

		if (isCompactLayout())
		{
			setupMinimapContainer(true);
			widgetManager.remapTargets(true, Script.FORCE_UPDATE, Compass.values());
			widgetManager.remapTargets(true, Script.FORCE_UPDATE, Orbs.values());

			updateCompassFrameChild();
			updateCompassToggleButton();
		}

		updateMinimapToggleButton();
	}

	public void reset()
	{
		hasSeenWikiWarning = false;
		pendingChildrenUpdate = false;
		hideWorldMap = false;
		hideLogoutX = false;
		enableNoClickThrough = false;

		hideByConfigMap.clear();
		hideByScriptMap.clear();
		slotManager.reset();
		clearCustomChildren();
		clearMinimapOverlayChildren();

		resetVisibility();
		resetPositioning();
		resetNoClickThrough();

		resetMinimapOverlayContainer();
	}

	private void resetVisibility()
	{
		widgetManager.setTargetsHidden(false, Orbs.values());
		widgetManager.setTargetsHidden(false, Compass.values());
		updateWikiBannerVisibility(false);
	}

	private void resetPositioning()
	{
		if (isFixedMode())
		{
			widgetManager.remapTargets(false, Script.FORCE_UPDATE, Orbs.WORLD_MAP_CONTAINER);
		}
		else if (isCompactLayout())
		{
			setupMinimapContainer(false);
			setupOrbsContainer();
		}

		widgetManager.remapTargets(false, Script.FORCE_UPDATE, Orbs.values());
		widgetManager.remapTargets(false, Script.FORCE_UPDATE, Compass.values());
	}

	private void resetNoClickThrough()
	{
		for (TargetWidget orb : Orbs.SWAPPABLE_ORBS)
		{
			//reset the orb layers noClickThrough
			widgetManager.setNoClickThrough(orb.getComponentId(), false);

			//reset the backing/buttons noClickThrough
			handleVanillaNoClickThrough(orb, false);

			//clear any proxy child that may be remaining
			widgetManager.clearChildren(orb.getComponentId());
		}
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
				setupMinimapContainer(toggle);
				setupOrbsContainer();
				widgetManager.remapTargets(toggle, Script.FORCE_UPDATE, Compass.values());
				widgetManager.remapTargets(toggle, Script.FORCE_UPDATE, Orbs.values());
				widgetManager.setTargetsHidden(hiddenCondition, Compass.values());
				updateNoClickThrough();
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
				widgetManager.remapTargets(remapCondition, Script.FORCE_UPDATE, Orbs.values());
				widgetManager.setTargetsHidden(toggle, Compass.values());
			}
		);
	}

	private void executeToggle(String key, Supplier<Boolean> getter, Consumer<Boolean> actions)
	{
		boolean toggle = !Boolean.TRUE.equals(getter.get());
		configManager.setConfiguration(ConfigGroup.GROUP_NAME, key, toggle);

		clientThread.invoke(() ->
		{
			actions.accept(toggle);
			updateCustomChildren(true);
		});
	}

	public void setupMinimapContainer(boolean compactLayout)
	{
		widgetManager.remapTargets(compactLayout, Script.FORCE_UPDATE, Minimap.CONTAINERS);
		widgetManager.setTargetsHidden(compactLayout, Minimap.COMPONENTS);
		widgetManager.revalidate(Minimap.COMPONENTS);
	}

	//TODO
	//Orb.UNIVERSE parent width/height mode seems to cause an issue when setting dimensions
	//causing the Orbs.UNIVERSE container be set to the dimensions of the tli (GAMEFRAME)
	//this should alleviate that by setting the size temporarily until restored by a clientscript (clicking a side panel tab, etc)
	public void setupOrbsContainer()
	{
		if (!isFixedMode() && !isMinimapMinimized() && isLoggedIn())
		{
			Widget orbsContainer = client.getWidget(Minimap.ORBS_UNIVERSE.getComponentId());
			Widget parent = client.getWidget((isClassicResizable() ? Minimap.CLASSIC_ORBS_CONTAINER : Minimap.MODERN_ORBS_CONTAINER).getComponentId());
			if (orbsContainer != null && parent != null)
			{
				widgetManager.updateValue(orbsContainer::getWidth, orbsContainer::setWidth, parent.getWidth());
				widgetManager.updateValue(orbsContainer::getHeight, orbsContainer::setHeight, parent.getHeight());
			}
		}
	}

	public void updateNoClickThrough()
	{
		boolean noClick = enableNoClickThrough || config.enableOrbSwapping();
		boolean compact = isCompactLayout();

		for (TargetWidget orb : Orbs.SWAPPABLE_ORBS)
		{
			widgetManager.setNoClickThrough(orb.getComponentId(), noClick && compact);

			if (!compact)
			{
				handleVanillaNoClickThrough(orb, noClick);
			}
			else
			{
				widgetManager.clearChildren(orb.getComponentId());
			}
		}
	}

	//TODO
	//in non-compact layouts, delegate the noClickThrough flag to the button widget instead of the layer/backing,
	//since under certain configurations when orb swapping, it may prevent a click where they overlap
	//if noClickThrough is enabled, and the button is hidden - create a noClick child in its place
	private void handleVanillaNoClickThrough(TargetWidget target, boolean noClickThrough)
	{
		Widget layer = client.getWidget(target.getComponentId());
		Widget backing = client.getWidget(Orbs.getBackingId(target));
		Widget button = client.getWidget(Orbs.getButtonId(target));
		if (layer == null || backing == null || button == null)
		{
			return;
		}

		widgetManager.setNoClickThrough(backing.getId(), !noClickThrough);
		widgetManager.setNoClickThrough(button.getId(), noClickThrough);

		if (!config.enableNoClickthrough() || !button.isHidden())
		{
			widgetManager.clearChildren(layer.getId());
			return;
		}

		if (layer.getChild(0) == null)
		{
			Widget noClick = layer.createChild(0, WidgetType.LAYER);
			noClick.setOriginalX(button.getOriginalX());
			noClick.setOriginalY(button.getOriginalY());
			noClick.setOriginalWidth(button.getOriginalWidth());
			noClick.setOriginalHeight(button.getOriginalHeight());
			noClick.setNoClickThrough(true);
			noClick.revalidate();
		}
	}

	//TODO - test if stale state can happen to the XP orb when hovering into the HP orb in safe mode (poisoned)
	//orb swapping seems to have introduced a possible de-sync under certain configurations, where an orbs backing frame
	//will remain in a hovered state when moving into another orbs bounds (overlapped) while triggering the graphic swapper script (44)
	//before it applied the correct state to the previous orb - to resolve this, ignore non-orb related swaps and reset the stale
	//backing sprite if it exists
	// - test example: swap order [HP, SPEC, RUN, PRAY], default view
	// - hovering RUN -> PRAY = never becomes stale
	// - hovering PRAY -> RUN = can become stale
	public void resolveOrbFrameMismatch()
	{
		if (isCompactLayout() || !config.enableOrbSwapping())
		{
			return;
		}

		final int id = client.getIntStack()[1] - 1;

		if (!Orbs.isSwappableOrb(id))
		{
			return;
		}

		final Point mouse = client.getMouseCanvasPosition();

		for (TargetWidget target : Orbs.SWAPPABLE_ORBS)
		{
			final Widget backing = client.getWidget(Orbs.getBackingId(target));
			final Widget button = client.getWidget(Orbs.getButtonId(target));

			if (backing == null || button == null)
			{
				continue;
			}

			final int spriteId = backing.getSpriteId();
			final boolean hovering = button.getBounds().contains(mouse.getX(), mouse.getY());

			if (!hovering && spriteId == Sprite.FRAME_HOVERED)
			{
				backing.setSpriteId(Sprite.FRAME);
			}
		}
	}

	//create the compass frame and toggle buttons, clearing them if the parent id changed,
	//and only creating widgets if missing from the current parent
	public void createCustomChildren()
	{
		Widget parent = widgetManager.getCurrentParent();
		if (parent == null)
		{
			clearCustomChildren();
			return;
		}

		if (parent.getId() != previousParentId)
		{
			clearCustomChildren();
			previousParentId = parent.getId();
		}

		if (widgetManager.exists(minimapButton, parent))
		{
			return;
		}

		minimapButton = parent.createChild(-1, WidgetType.GRAPHIC);
		minimapButton.setOriginalWidth(Layout.TOGGLE_BUTTON_SIZE);
		minimapButton.setOriginalHeight(Layout.TOGGLE_BUTTON_SIZE);
		minimapButton.setSpriteId(getSpriteId(config.hideMinimap()));
		minimapButton.setOpacity(Layout.OPACITY);
		minimapButton.setHidden(false);
		minimapButton.setHasListener(true);
		minimapButton.setAction(Menu.TOGGLE_OVERLAY, "");
		minimapButton.setOnOpListener(
			(JavaScriptCallback) e ->
			{
				switch (e.getOp() - 1)
				{
					case Menu.ABOVE_WALK_HERE:
					case Menu.BELOW_WALK_HERE:
						onMinimapToggle();
						break;

					case Menu.TOGGLE_OVERLAY:
						if (isCompactLayout())
						{
							configManager.setConfiguration(ConfigGroup.GROUP_NAME, ConfigKeys.ENABLE_MINIMAP_OVERLAY, !config.showMinimapInCompactView());
						}
						break;
				}
			}
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

		//compass frame moved to the compass container
		parent = client.getWidget(isClassicResizable() ? Classic.COMPASS_PARENT : Modern.COMPASS_PARENT);
		if (parent != null)
		{
			compassFrame = parent.createChild(-1, WidgetType.GRAPHIC);
			compassFrame.setOriginalWidth(Layout.COMPASS_FRAME_SIZE);
			compassFrame.setOriginalHeight(Layout.COMPASS_FRAME_SIZE);
			compassFrame.setSpriteId(Sprite.COMPASS_FRAME);
			compassFrame.setOpacity(Layout.OPACITY);
			compassFrame.setHidden(false);
		}

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

	private void updateCompassFrameChild()
	{
		if (compassFrame == null)
		{
			return;
		}

		compassFrame.setHidden(!isMinimapHidden() || isCompassHidden() || isMinimapMinimized());
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
			setMenuPriority(ConfigKeys.MINIMAP, minimapButton);
		}
		widgetManager.updateValue(minimapButton::getOriginalX, minimapButton::setOriginalX, getMinimapButtonX());
		widgetManager.updateValue(minimapButton::getOriginalY, minimapButton::setOriginalY, getMinimapButtonY());
		minimapButton.revalidate();
	}

	private void updateMinimapOverlayToggleOp()
	{
		String op = getMenuOp(config.showMinimapInCompactView() ? Menu.PREFIX_HIDE : Menu.PREFIX_SHOW, "Detached Minimap");
		minimapButton.setAction(Menu.TOGGLE_OVERLAY, isCompactLayout() && config.showToggleOnMinimapButton() ? op : "");
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
			setMenuPriority(ConfigKeys.COMPASS, compassButton);
		}
		widgetManager.updateValue(compassButton::getOriginalX, compassButton::setOriginalX, getCompassButtonX());
		widgetManager.updateValue(compassButton::getOriginalY, compassButton::setOriginalY, getCompassButtonY());
		compassButton.revalidate();
	}

	//clear any created children and reset previous parent id
	private void clearCustomChildren()
	{
		//clear toggle buttons
		widgetManager.clearChildren(Modern.ORBS);
		widgetManager.clearChildren(Classic.ORBS);

		//clear compass frame
		widgetManager.clearChildren(Modern.COMPASS_PARENT);
		widgetManager.clearChildren(Classic.COMPASS_PARENT);

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
		updateMinimapOverlayToggleOp();
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

	private void setMenuPriority(String key, Widget widget)
	{
		int index = Menu.ABOVE_WALK_HERE;
		if (config.rightClickToggleButtons())
		{
			index = Menu.BELOW_WALK_HERE;
		}
		widget.setAction(index == Menu.ABOVE_WALK_HERE ? Menu.BELOW_WALK_HERE : Menu.ABOVE_WALK_HERE, "");
		widget.setAction(index, getButtonMenuOp(key));
		widget.setNoClickThrough(!config.rightClickToggleButtons());
	}

	private String getMenuOp(String action, String target)
	{
		return action + " " + ColorUtil.wrapWithColorTag(target, Menu.COLOR);
	}

	public CompactOrbsLayout getCurrentLayout()
	{
		return config.layout();
	}

	//invert for readability
	public boolean isFixedMode()
	{
		return !client.isResized();
	}

	public boolean isClassicResizable()
	{

		return widgetManager.getCurrentParent().getId() != Modern.ORBS;
	}

	public boolean isLoggedIn()
	{
		return client.getGameState() == GameState.LOGGED_IN;
	}

	public boolean isCompactLayout()
	{
		return !isFixedMode() && isMinimapHidden() && !isMinimapMinimized();
	}

	public boolean isVerticalLeft()
	{
		return config.verticalAnchor().isLeft();
	}

	public boolean isVerticalRight()
	{
		return config.verticalAnchor().isRight();
	}

	public boolean isHorizontalBottom()
	{
		return config.horizontalAnchor().isBottom();
	}

	public boolean isHorizontalTop()
	{
		return config.horizontalAnchor().isTop();
	}

	public boolean applyVerticalHeightOffset()
	{
		if (isHorizontalTop() && isCompassHidden() && allowReordering())
		{
			if (hideLogoutX || isClassicResizable())
			{
				return config.enableVerticalHeightOffset() && allowReordering();
			}
		}
		return false;
	}

	public boolean allowReordering()
	{
		return !config.disableReordering();
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

	public boolean isWikiHidden()
	{
		return config.hideWiki();
	}

	public boolean shouldOffsetXpOrb()
	{
		return !isFixedMode() && (enableNoClickThrough || config.enableOrbSwapping());
	}

	public boolean hideMinimapToggle()
	{
		return config.hideMinimapToggle();
	}

	private boolean hideCustomToggles()
	{
		return (hideMinimapToggle() && config.hideCompassToggle()) || isMinimapMinimized();
	}

	private boolean hideMinimapOverlay()
	{
		return !(isMinimapOverlayEnabled() && isMinimapHidden() && !isMinimapMinimized()) || isFixedMode();
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

		return isClassicResizable() || !config.showOverlayLogoutX();
	}

	public boolean showOverlayLogoutX()
	{
		return config.showOverlayLogoutX();
	}

	public void handleLogoutXHiddenState(boolean configChanged)
	{
		if (isFixedMode() || isMinimapMinimized())
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
		//login screen NPE when 'loading interfaces %' occurs post update for the MINIMAP_TOGGLE varbit?
		if (!isLoggedIn())
		{
			return false;
		}

		return client.getVarbitValue(Varbit.MINIMAP_TOGGLE) == VarbitValue.MINIMAP_MINIMIZED;
	}

	public boolean isCutsceneActive()
	{
		return client.getVarbitValue(Varbit.CUTSCENE_STATUS) == VarbitValue.CUTSCENE_ACTIVE;
	}

	private int getMinimapButtonX()
	{
		int x = config.minimapTogglePlacement().getX();

		if (isMinimapHidden())
		{
			switch (getCurrentLayout())
			{
				case VERTICAL:
					x = Layout.Vertical.MAP_CONTAINER_WIDTH - Layout.TOGGLE_BUTTON_SIZE;
					break;

				case HORIZONTAL:
					x = Layout.Horizontal.MAP_CONTAINER_WIDTH - Layout.TOGGLE_BUTTON_SIZE;

					if (allowReordering())
					{
						if (isWikiHidden())
						{
							x -= 42;
						}
					}

					if (isVerticalLeft())
					{
						int offset = slotManager.getHiddenSize();
						x -= offset;
					}
					break;

				case HORIZONTAL_WIDE:
					x = Layout.HorizontalWide.MAP_CONTAINER_WIDTH - Layout.TOGGLE_BUTTON_SIZE - 7;
					break;
			}
		}
		else
		{
			//offset when store is hidden, and minimap is visible
			if (config.minimapTogglePlacement() == TogglePlacement.BELOW_MAP
				&& config.hideStore())
			{
				x -= 33;
			}
		}
		return x;
	}

	private int getMinimapButtonY()
	{
		int y = config.minimapTogglePlacement().getY();

		if (isMinimapHidden())
		{
			switch (getCurrentLayout())
			{
				case VERTICAL:
					y = slotManager.applyHiddenYOffset(Orbs.WIKI_ICON_CONTAINER,
						Layout.Vertical.MAP_CONTAINER_HEIGHT - Layout.TOGGLE_BUTTON_SIZE);

					if (isHorizontalTop()
						&& allowReordering())
					{
						if (getCurrentLayout().isLastVisible(Slot.WIKI_SLOT, slotManager.getHiddenCountAbove(Orbs.WIKI_ICON_CONTAINER)))
						{
							y += 4;
						}

						if (config.hideWiki() && !isClassicResizable() && !hideLogoutX
							&& slotManager.getHiddenCountAbove(Orbs.WIKI_ICON_CONTAINER) >= getCurrentLayout().getGroup(Slot.WIKI_SLOT).indexOf(Slot.WIKI_SLOT))
						{
							y -= 14;
						}
					}

					break;

				case HORIZONTAL:
					y = Layout.Horizontal.MAP_CONTAINER_HEIGHT - Layout.TOGGLE_BUTTON_SIZE;
					break;

				case HORIZONTAL_WIDE:
					y = Layout.HorizontalWide.MAP_CONTAINER_HEIGHT - Layout.TOGGLE_BUTTON_SIZE - 33;
					break;
			}
		}
		else
		{
			//offset when store is hidden and minimap is visible
			if (config.minimapTogglePlacement() == TogglePlacement.BELOW_MAP
				&& config.hideStore())
			{
				y -= 5;
			}
		}

		return y;
	}

	private int getCompassButtonX()
	{
		int anchor = (Layout.COMPASS_FRAME_SIZE - (Layout.TOGGLE_BUTTON_SIZE / 2)) + 1;
		int x = Offsets.MAP_MINIMAP.getOffset().getOffsetX()
			+ anchor;

		switch (getCurrentLayout())
		{
			case VERTICAL:
				if (enableNoClickThrough)
				{
					x += 1;
				}
				break;

			case HORIZONTAL:
				x += 1;

				if (allowReordering())
				{
					if (isCompassHidden())
					{
						if (isXpDropHidden() || hideWorldMap)
						{
							return getMinimapButtonX();
						}
					}
				}
				break;

			case HORIZONTAL_WIDE:
				x -= 2;
				break;
		}

		if (isVerticalRight())
		{
			x -= getCurrentLayout().getRightOffset();
		}

		return x;
	}

	private int getCompassButtonY()
	{
		int anchor = Layout.COMPASS_FRAME_SIZE - (Layout.TOGGLE_BUTTON_SIZE + 3);
		int y = Offsets.MAP_MINIMAP.getOffset().getOffsetY()
			+ anchor;

		switch (getCurrentLayout())
		{
			case VERTICAL:
				if (enableNoClickThrough)
				{
					y -= 2;
				}

				if (applyVerticalHeightOffset())
				{
					y -= 20;
				}
				break;

			case HORIZONTAL:
				if (allowReordering())
				{
					if (isCompassHidden())
					{
						if (isWikiHidden())
						{
							if (isXpDropHidden() || hideWorldMap)
							{
								return Layout.Horizontal.RUN_ORB_Y; //anchor y to the Run orb
							}
						}
					}
				}
				break;

			case HORIZONTAL_WIDE:
				return getMinimapButtonY();
		}

		if (isHorizontalBottom())
		{
			y -= getCurrentLayout().getBottomOffset();
		}

		return y;
	}

	//register an orb toggle entry in the config and script maps
	private void registerOrbToggle(String key, Supplier<Boolean> toggle, TargetWidget... targets)
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

	public void registerOrbToggleEntries()
	{
		registerOrbToggle(ConfigKeys.HIDE_HP, config::hideHp, Orbs.HP_ORB_CONTAINER);
		registerOrbToggle(ConfigKeys.HIDE_PRAYER, config::hidePray, Orbs.PRAYER_ORB_CONTAINER);
		registerOrbToggle(ConfigKeys.HIDE_RUN, config::hideRun, Orbs.RUN_ORB_CONTAINER);
		registerOrbToggle(ConfigKeys.HIDE_SPEC, config::hideSpec, Orbs.SPEC_ORB_CONTAINER);
		registerOrbToggle(ConfigKeys.HIDE_XP, config::hideXp, Orbs.XP_DROPS_CONTAINER);
		registerOrbToggle(ConfigKeys.HIDE_ACTIVITY, config::hideActivity, Orbs.ACTIVITY_ORB_CONTAINER);
		registerOrbToggle(ConfigKeys.HIDE_STORE, config::hideStore, Orbs.STORE_ORB_CONTAINER);
		registerOrbToggle(ConfigKeys.HIDE_LOGOUT_X, config::hideLogout, Orbs.LOGOUT_X_ICON, Orbs.LOGOUT_X_STONE);
		registerOrbToggle(ConfigKeys.HIDE_GRID, config::hideGrid, Orbs.GRID_MASTER_ORB_CONTAINER);
		registerOrbToggle(ConfigKeys.HIDE_WORLD, config::hideWorld, Orbs.WORLD_MAP_CONTAINER);
		registerOrbToggle(ConfigKeys.HIDE_WIKI, config::hideWiki, Orbs.WIKI_VANILLA_ICON, Orbs.WIKI_VANILLA_CONTAINER, Orbs.WIKI_ICON_CONTAINER);
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
				widgetManager.remapTarget(Orbs.WORLD_MAP_CONTAINER, isMinimapHidden() && !isFixedMode());
				break;

			//wiki banner config handling for onConfigChanged
			case ConfigKeys.HIDE_WIKI:
				updateWikiBannerVisibility(config.hideWiki());
				warnWikiPluginConflict();

				//update the minimap toggle button when in horizontal layout,
				//and minimap is hidden (offset is applied that needs updated)
				if (getCurrentLayout().isHorizontal() && isMinimapHidden())
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
		if (!hasSeenWikiWarning && isWikiPluginBannerActive() && config.hideWiki())
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

			hasSeenWikiWarning = true;
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
