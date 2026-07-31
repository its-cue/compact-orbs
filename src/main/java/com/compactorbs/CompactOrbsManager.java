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

import com.compactorbs.CompactOrbsConfig.HorizontalAnchor;
import com.compactorbs.CompactOrbsConfig.HotkeyOptions;
import com.compactorbs.CompactOrbsConfig.TogglePlacement;
import com.compactorbs.CompactOrbsConfig.VerticalAnchor;
import com.compactorbs.CompactOrbsConstants.ConfigGroup;
import com.compactorbs.CompactOrbsConstants.ConfigKeys;
import com.compactorbs.CompactOrbsConstants.Enum;
import com.compactorbs.CompactOrbsConstants.Layout;
import com.compactorbs.CompactOrbsConstants.MenuOp;
import com.compactorbs.CompactOrbsConstants.Script;
import com.compactorbs.CompactOrbsConstants.Sprite;
import com.compactorbs.CompactOrbsConstants.VarPlayer;
import com.compactorbs.CompactOrbsConstants.Varbit;
import com.compactorbs.CompactOrbsConstants.VarbitValue;
import com.compactorbs.CompactOrbsConstants.Widgets.Classic;
import com.compactorbs.CompactOrbsConstants.Widgets.MinimapOverlay;
import com.compactorbs.CompactOrbsConstants.Widgets.Modern;
import com.compactorbs.util.MigrateConfig;
import com.compactorbs.util.ValueKey;
import com.compactorbs.widget.TargetWidget;
import com.compactorbs.widget.WidgetManager;
import com.compactorbs.widget.elements.Button;
import com.compactorbs.widget.elements.Compass;
import com.compactorbs.widget.elements.Minimap;
import com.compactorbs.widget.elements.Orbs;
import com.compactorbs.widget.layout.EditLayout;
import com.compactorbs.widget.layout.OrbToggle;
import com.compactorbs.widget.layout.edit.Binding;
import com.compactorbs.widget.layout.edit.BindingManager;
import com.compactorbs.widget.layout.edit.drag.DragState;
import com.compactorbs.widget.layout.slot.Slot;
import com.compactorbs.widget.layout.slot.SlotConfig;
import com.compactorbs.widget.layout.slot.SlotManager;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Menu;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
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
import net.runelite.client.config.Keybind;
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

	@Inject
	private EditLayout editLayout;

	@Inject
	private DragState dragState;

	@Inject
	private BindingManager bindingManager;

	public boolean isEditingLayout;

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
	public Widget compassFrame;
	private Widget minimapButton;
	private Widget overlayLogoutXStone;
	private Widget overlayLogoutXIcon;

	private final Map<String, OrbToggle> hideByConfigMap = new HashMap<>();
	private final Map<Integer, OrbToggle> hideByScriptMap = new HashMap<>();
	public final Map<TargetWidget, OrbToggle> toggleByTarget = new HashMap<>();

	public void init(int scriptId)
	{
		updateWikiBannerVisibility(config.hideWiki());

		updateOrbByScript(scriptId);

		if (isFixedMode())
		{
			if (updateFixedMode)
			{
				widgetManager.setHidden(MinimapOverlay.UNIVERSE, true);
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

	public void updateLayout(boolean compact)
	{
		if (isFixedMode())
		{
			widgetManager.setHidden(MinimapOverlay.UNIVERSE, true);
			widgetManager.remapTargets(false, Script.FORCE_UPDATE, Orbs.values());
			updateNoClickThrough();
			setupMinimapContainer(false);
			return;
		}

		setupMinimapContainer(compact);
		setupOrbsContainer();
		widgetManager.remapTargets(compact, Script.FORCE_UPDATE, Compass.values());
		widgetManager.remapTargets(compact, Script.FORCE_UPDATE, Orbs.values());
		widgetManager.setTargetsHidden((compact && isCompassHidden()), Compass.values());
		updateCustomChildren(true);
		updateNoClickThrough();

		if (!isClassicResizable())
		{
			widgetManager.revalidate(Orbs.LOGOUT_X_ICON, Orbs.LOGOUT_X_STONE);
		}
		widgetManager.revalidate(Orbs.WORLD_MAP_CONTAINER);
	}

	public void reset()
	{
		hasSeenWikiWarning = false;
		pendingChildrenUpdate = false;
		hideWorldMap = false;
		hideLogoutX = false;
		enableNoClickThrough = false;

		editLayout.cancelEditMode();

		bindingManager.clear();
		hideByConfigMap.clear();
		hideByScriptMap.clear();
		toggleByTarget.clear();

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
		widgetManager.setTargetsHidden(hideStonesAndIcons(), Orbs.LOGOUT_X_ICON, Orbs.LOGOUT_X_STONE);
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
		saveConfig(ConfigKeys.MINIMAP, !isMinimapHidden());

		clientThread.invokeLater(() ->
		{
			setupMinimapContainer(isMinimapHidden());
			setupOrbsContainer();

			if (!isClassicResizable())
			{
				updateLogoutX();
			}

			widgetManager.remapTargets(isMinimapHidden(), Script.FORCE_UPDATE, Compass.values());
			widgetManager.remapTargets(isMinimapHidden(), Script.FORCE_UPDATE, Orbs.values());
			widgetManager.setTargetsHidden(isMinimapHidden() && isCompassHidden(), Compass.values());
			updateNoClickThrough();
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
		boolean compact = isCompactLayout();
		boolean noClick = enableNoClickThrough ||
			config.enableOrbSwapping() && compact && !getCurrentLayout().isCustom();

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
		Widget backing = client.getWidget(target.getBackingId());
		Widget button = client.getWidget(target.getButtonId());
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
			final Widget backing = client.getWidget(target.getBackingId());
			final Widget button = client.getWidget(target.getButtonId());

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

		minimapButton = parent.createChild(0, WidgetType.GRAPHIC);
		minimapButton.setOriginalWidth(Layout.TOGGLE_BUTTON_SIZE);
		minimapButton.setOriginalHeight(Layout.TOGGLE_BUTTON_SIZE);
		minimapButton.setSpriteId(getSpriteId(config.hideMinimap()));
		minimapButton.setOpacity(Layout.OPACITY);
		minimapButton.setHidden(false);
		minimapButton.setHasListener(true);
		minimapButton.setAction(0, buildToggleOp(isMinimapHidden(), MenuOp.MINIMAP_OP));
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
			compassFrame.revalidate();
		}

		//prevent de-sync
		pendingChildrenUpdate = true;
	}

	//handle dynamic widget changes for visibility, positions, sprites, and menu actions for the
	//compass frame and toggle buttons (logout X being an exception in native minimap hiding)
	public void updateCustomChildren(boolean shouldUpdate)
	{
		//missing children -> create them; prevent updates until all exist
		if (compassFrame == null || minimapButton == null)
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

			//treat minimap overlay as 'custom'
			updateMinimapOverlayVisibility(false);

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
	}

	public void updateMinimapToggleButton()
	{
		if (minimapButton == null)
		{
			return;
		}

		minimapButton.setHidden(config.hideMinimapToggle() || isMinimapMinimized());

		if (!config.hideMinimapToggle())
		{
			minimapButton.setSpriteId(getSpriteId(!isMinimapHidden()));
			if (!config.rightClickToggleButtons())
			{
				minimapButton.setAction(0, buildToggleOp(isMinimapHidden(), MenuOp.MINIMAP_OP));
			}
			minimapButton.setNoClickThrough(!config.rightClickToggleButtons());
			widgetManager.remapTargets(isCompactLayout(), Script.FORCE_UPDATE, Button.MINIMAP_BUTTON_MODERN, Button.MINIMAP_BUTTON_CLASSIC);
		}
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
	}

	private void clearMinimapOverlayChildren()
	{
		overlayLogoutXStone = null;
		overlayLogoutXIcon = null;
	}

	public void updateMinimapOverlayVisibility(boolean configChanged)
	{
		widgetManager.setHidden(MinimapOverlay.UNIVERSE, hideMinimapOverlay());

		if (configChanged)
		{
			updateLogoutX();
			updateLogoutXPosition();
			updateLogoutXOverlay();
		}
	}

	//set hooked layer back to default
	private void resetMinimapOverlayContainer()
	{
		configureMinimapOverlayContainer(false);
		widgetManager.clearChildren(MinimapOverlay.UNIVERSE);
	}

	//initial setup for the minimap overlay
	//@enabled - for startup/shutdown behaviour
	private void configureMinimapOverlayContainer(boolean enabled)
	{
		Widget parent = client.getWidget(MinimapOverlay.UNIVERSE);
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
	private void createMinimapOverlayChildren()
	{
		final Widget parent = client.getWidget(MinimapOverlay.UNIVERSE);
		if (parent == null)
		{
			return;
		}

		for (int index = 0; index < Layout.MinimapOverlay.NO_CLICK_Y.length; index++)
		{
			final Widget minimapNoClick = parent.createChild(-1, WidgetType.LAYER);
			minimapNoClick.setOriginalX(0);
			minimapNoClick.setOriginalY(Layout.MinimapOverlay.NO_CLICK_Y[index]);
			minimapNoClick.setOriginalWidth(Layout.MinimapOverlay.NO_CLICK_WIDTH[index]);
			minimapNoClick.setOriginalHeight(Layout.MinimapOverlay.NO_CLICK_HEIGHT[index]);
			minimapNoClick.setXPositionMode(WidgetPositionMode.ABSOLUTE_RIGHT);
			minimapNoClick.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
			minimapNoClick.setNoClickThrough(true);
			minimapNoClick.revalidate();
		}

		final int compassX = Layout.Original.COMPASS_X - (Layout.Original.MAP_CONTAINER_WIDTH - Layout.MinimapOverlay.CONTAINER_WIDTH);

		final Widget compass = parent.createChild(-1, WidgetType.GRAPHIC);
		compass.setContentType(Layout.MinimapOverlay.COMPASS_CONTENT);
		compass.setSpriteId(Sprite.COMPASS_MASK);
		compass.setOriginalX(compassX);
		compass.setOriginalY(Layout.Original.COMPASS_Y);
		compass.setOriginalWidth(Layout.Original.COMPASS_DIMENSION);
		compass.setOriginalHeight(Layout.Original.COMPASS_DIMENSION);
		compass.setXPositionMode(WidgetPositionMode.ABSOLUTE_LEFT);
		compass.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
		compass.revalidate();

		final Widget compassLayer = parent.createChild(-1, WidgetType.LAYER);
		compassLayer.setOriginalX(compassX);
		compassLayer.setOriginalY(Layout.Original.COMPASS_Y);
		compassLayer.setOriginalWidth(Layout.COMPASS_SIZE);
		compassLayer.setOriginalHeight(Layout.COMPASS_SIZE);
		compassLayer.revalidate();

		final Widget compassNoClick = compassLayer.createChild(-1, WidgetType.TEXT);
		compassNoClick.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER);
		compassNoClick.setYPositionMode(WidgetPositionMode.ABSOLUTE_CENTER);
		compassNoClick.setWidthMode(WidgetSizeMode.MINUS);
		compassNoClick.setHeightMode(WidgetSizeMode.MINUS);
		compassNoClick.setHasListener(true);
		compassNoClick.setNoClickThrough(true);
		compassNoClick.revalidate();

		final Widget compassMenuOp = compassLayer.createChild(-1, WidgetType.TEXT);
		compassMenuOp.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER);
		compassMenuOp.setYPositionMode(WidgetPositionMode.ABSOLUTE_CENTER);
		compassMenuOp.setWidthMode(WidgetSizeMode.MINUS);
		compassMenuOp.setHeightMode(WidgetSizeMode.MINUS);
		compassMenuOp.setHasListener(true);
		compassMenuOp.setOnOpListener(Script.TOPLEVEL_COMPASS_OP, Script.OPINDEX0);
		compassMenuOp.setOnVarTransmitListener(Script.TOPLEVEL_COMPASS_SETOP, Script.COMPONENT0, Script.COMSUBID1);
		compassMenuOp.setVarTransmitTrigger(VarPlayer.MAP_FLAGS_CACHED);
		compassMenuOp.revalidate();

		final Widget minimap = parent.createChild(-1, WidgetType.GRAPHIC);
		minimap.setContentType(Layout.MinimapOverlay.MINIMAP_CONTENT);
		minimap.setSpriteId(Sprite.MINIMAP_MASK);
		minimap.setOriginalX(Layout.Original.MINIMAP_X);
		minimap.setOriginalY(Layout.Original.MINIMAP_Y);
		minimap.setOriginalWidth(Layout.Original.MINIMAP_DIMENSION);
		minimap.setOriginalHeight(Layout.Original.MINIMAP_DIMENSION);
		minimap.setXPositionMode(WidgetPositionMode.ABSOLUTE_RIGHT);
		minimap.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
		minimap.revalidate();

		final Widget minimapFrame = parent.createChild(-1, WidgetType.GRAPHIC);
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
		overlayLogoutXStone.setAction(0, MenuOp.LOGOUT_OP);
		overlayLogoutXStone.setOnOpListener(Script.TOPLEVEL_SIDEBUTTON_OP, Script.OPINDEX0, Enum.TOPLEVEL_COMPONENTS, 10);
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

	public void setupMinimapOverlay()
	{
		configureMinimapOverlayContainer(true);
		clientThread.invokeLater(() ->
		{
			if (pendingMinimapOverlayChildren)
			{
				createMinimapOverlayChildren();
				pendingMinimapOverlayChildren = false;
				return true;
			}
			return false;
		});
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
		if (isWikiBannerDisabled() && !customWikiBanner(container) && !isEditingLayout)
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

	public String buildEditOp(boolean hidden)
	{
		return (hidden ? MenuOp.SAVE : MenuOp.ENABLE) + " "
			+ (hidden ? MenuOp.CHANGES : MenuOp.EDIT_MODE);
	}

	public String buildToggleOp(boolean hidden, String target)
	{
		return buildMenuOp(hidden ? MenuOp.SHOW : MenuOp.HIDE, target);
	}

	public String buildMenuOp(String action, String target)
	{
		return action + " " + ColorUtil.wrapWithColorTag(target, MenuOp.MENU_TARGET);
	}

	public void addCustomMenuEntries(MenuEntry entry)
	{
		if (entry.getType() != MenuAction.CC_OP)
		{
			return;
		}

		Menu menu = client.getMenu();
		Widget widget = entry.getWidget();
		if (widget == null)
		{
			return;
		}

		if (widget == overlayLogoutXStone)
		{
			menu.createMenuEntry(1)
				.setOption(MenuOp.WORLD_SWITCHER_OP)
				.setDeprioritized(false)
				.setType(MenuAction.RUNELITE)
				.onClick(e ->
				{
					Widget w = client.getWidget(InterfaceID.Worldswitcher.BUTTONS);
					if (w == null)
					{
						client.openWorldHopper();
					}
				});
		}
		else if (widget == minimapButton)
		{
			if (config.rightClickToggleButtons() && !isEditingLayout)
			{
				entry
					.setOption(buildToggleOp(isMinimapHidden(), MenuOp.MINIMAP_OP))
					.setType(MenuAction.CC_OP_LOW_PRIORITY)
					.setDeprioritized(true);
			}

			if (isCompactLayout())
			{
				if (config.showToggleOnMinimapButton())
				{
					menu.createMenuEntry(-2)
						.setOption(buildToggleOp(!config.showMinimapInCompactView(), MenuOp.DETACHED_OP))
						.setDeprioritized(config.rightClickToggleButtons() || isEditingLayout)
						.setType(MenuAction.RUNELITE_LOW_PRIORITY)
						.onClick(e ->
							saveConfig(ConfigKeys.ENABLE_MINIMAP_OVERLAY, !config.showMinimapInCompactView())
						);
				}
			}

			int index = isCompactLayout() && config.showToggleOnMinimapButton() ? -3 : -2;

			menu.createMenuEntry(index)
				.setOption(buildEditOp(isEditingLayout))
				.setDeprioritized(config.rightClickToggleButtons())
				.setType(isEditingLayout ? MenuAction.RUNELITE_LOW_PRIORITY : MenuAction.RUNELITE)
				.onClick(e ->
					editLayout.toggleEditMode(!isEditingLayout)
				);
		}
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
		Widget parent = widgetManager.getCurrentParent();
		if (parent == null)
		{
			return false;
		}

		return parent.getId() != Modern.ORBS;
	}

	public boolean isLoggedIn()
	{
		return client.getGameState() == GameState.LOGGED_IN;
	}

	public boolean isCompactLayout()
	{
		return !isFixedMode() && isMinimapHidden() && !isMinimapMinimized();
	}

	public boolean isAnchorLeft()
	{
		return config.horizontalAnchor().isLeft();
	}

	public boolean isAnchorRight()
	{
		return config.horizontalAnchor().isRight();
	}

	public boolean isAnchorTop()
	{
		return config.verticalAnchor().isTop();
	}

	public boolean isAnchorBottom()
	{
		return config.verticalAnchor().isBottom();
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

	public boolean isStoreHidden()
	{
		return config.hideStore();
	}

	public TogglePlacement getTogglePlacement()
	{
		return config.minimapTogglePlacement();
	}

	public boolean shouldOffsetXpOrb()
	{
		return !isFixedMode() && (enableNoClickThrough || config.enableOrbSwapping());
	}

	public boolean hideMinimapToggle()
	{
		return config.hideMinimapToggle();
	}

	private boolean hideMinimapOverlay()
	{
		return !(isMinimapOverlayEnabled() && isMinimapHidden() && !isMinimapMinimized()) || isFixedMode();
	}

	public boolean isMinimapOverlayEnabled()
	{
		return config.showMinimapInCompactView();
	}

	//prevent unintended state changes for the logout-x since it is treated as a side icon/stone, i.e. don't unhide when it should be hidden
	public void updateLogoutX()
	{
		if (isFixedMode() || isMinimapMinimized())
		{
			return;
		}

		boolean hidden = hideLogoutX && !isOverlayLogoutVisible() || hideStonesAndIcons();

		widgetManager.setTargetsHidden(hidden, Orbs.LOGOUT_X_STONE, Orbs.LOGOUT_X_ICON);
	}

	//offset the logout-x instead of using the hidden-flag (similarly to the world map) when the overlays
	//logout-x is visible (retaining the FPS/Ping status offset so it doesn't block the overlays logout-x)
	public void updateLogoutXPosition()
	{
		if (isFixedMode() || isMinimapMinimized())
		{
			return;
		}

		widgetManager.remapTargets(isCompactLayout(), Script.FORCE_UPDATE, Orbs.LOGOUT_X_STONE, Orbs.LOGOUT_X_ICON);
	}

	public void updateLogoutXOverlay()
	{
		if (overlayLogoutXStone == null || overlayLogoutXIcon == null)
		{
			return;
		}

		widgetManager.syncSprite(overlayLogoutXStone, Modern.LOGOUT_X_STONE);

		overlayLogoutXStone.setHidden(hideOverlayLogoutX());
		overlayLogoutXIcon.setHidden(hideOverlayLogoutX());
	}

	//restrict the overlays logout-x visibility to the same conditions as the original
	private boolean hideOverlayLogoutX()
	{
		if (widgetManager.getCurrentParent() == null)
		{
			return true;
		}

		return !config.showOverlayLogoutX() || isClassicResizable() || hideStonesAndIcons();
	}

	//similar to how the cs2 script hides/shows the logout-x by referencing the containers hidden state
	private boolean hideStonesAndIcons()
	{
		Widget w = client.getWidget(isClassicResizable() ? Classic.SIDE_TOP : Modern.SIDE_MOVABLE);
		if (w == null)
		{
			return false;
		}

		return w.isHidden();
	}

	//use offset-hiding instead of using the hidden-flag for the logout-x
	public boolean offsetLogoutX()
	{
		return isOverlayLogoutVisible() && !isMinimapMinimized();
	}

	boolean isOverlayLogoutVisible()
	{
		return isCompactLayout() &&
			isMinimapOverlayEnabled() && config.showOverlayLogoutX();
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

	public int clampVerticalY()
	{
		int y = 0;
		int limit = 0;

		if (isAnchorBottom() && !isEditingLayout)
		{
			y += config.verticalYAdjustment();

			switch (getCurrentLayout())
			{
				case VERTICAL:
					if (isCompassHidden() && (hideLogoutX || isClassicResizable()))
					{
						limit = 38 + slotManager.getHiddenSize();
					}

					if (y > limit)
					{
						y = limit;
					}
					break;

				case HORIZONTAL:
					if (hideLogoutX || isClassicResizable() || allowReordering() && hideWorldMap)
					{
						limit = 16;
					}
				case HORIZONTAL_WIDE:
					if (y > getCurrentLayout().getBottomOffset() + limit)
					{
						y = getCurrentLayout().getBottomOffset() + limit;
					}
					break;
			}
		}

		return y;
	}

	enum UpdateType
	{
		CONFIG,
		SCRIPT,
		BOTH
	}

	private void registerOrbToggle(
		String key,
		Supplier<Boolean> isHidden,
		UpdateType type,
		String name,
		TargetWidget... targets)
	{
		OrbToggle orbToggle = new OrbToggle(
			key,
			isHidden,
			name,
			targets
		);

		hideByConfigMap.put(key, orbToggle);

		int scriptId = Script.FORCE_UPDATE;

		for (TargetWidget target : targets)
		{
			toggleByTarget.put(target, orbToggle);

			if ((type == UpdateType.SCRIPT || type == UpdateType.BOTH) &&
				scriptId == Script.FORCE_UPDATE &&
				target instanceof Orbs)
			{
				scriptId = target.getScriptId();
			}
		}

		if (scriptId != Script.FORCE_UPDATE)
		{
			hideByScriptMap.put(scriptId, orbToggle);
		}
	}

	public void registerOrbToggleEntries()
	{
		registerOrbToggle(ConfigKeys.HIDE_HP, config::hideHp, UpdateType.BOTH,
			"HP orb",
			Orbs.HP_ORB_CONTAINER
		);

		registerOrbToggle(ConfigKeys.HIDE_PRAYER, config::hidePray, UpdateType.BOTH,
			"Prayer orb",
			Orbs.PRAYER_ORB_CONTAINER
		);

		registerOrbToggle(ConfigKeys.HIDE_RUN, config::hideRun, UpdateType.BOTH,
			"Run orb",
			Orbs.RUN_ORB_CONTAINER
		);

		registerOrbToggle(ConfigKeys.HIDE_SPEC, config::hideSpec, UpdateType.BOTH,
			"Special orb",
			Orbs.SPEC_ORB_CONTAINER
		);

		registerOrbToggle(ConfigKeys.HIDE_STORE, config::hideStore, UpdateType.BOTH,
			"Store",
			Orbs.STORE_ORB_CONTAINER
		);

		registerOrbToggle(ConfigKeys.HIDE_ACTIVITY, config::hideActivity, UpdateType.BOTH,
			"Activity advisor",
			Orbs.ACTIVITY_ORB_CONTAINER
		);

		registerOrbToggle(ConfigKeys.HIDE_WORLD, config::hideWorld, UpdateType.CONFIG,
			"World map",
			Orbs.WORLD_MAP_CONTAINER
		);

		registerOrbToggle(ConfigKeys.HIDE_WIKI, config::hideWiki, UpdateType.CONFIG,
			"Wiki banner",
			Orbs.WIKI_VANILLA_ICON,
			Orbs.WIKI_VANILLA_CONTAINER,
			Orbs.WIKI_ICON_CONTAINER
		);

		registerOrbToggle(ConfigKeys.HIDE_XP, config::hideXp, UpdateType.BOTH,
			"XP",
			Orbs.XP_DROPS_CONTAINER
		);

		registerOrbToggle(ConfigKeys.HIDE_LOGOUT_X, config::hideLogout, UpdateType.BOTH,
			"Logout",
			Orbs.LOGOUT_X_ICON,
			Orbs.LOGOUT_X_STONE
		);

		registerOrbToggle(ConfigKeys.MINIMAP_TOGGLE_BUTTON, config::hideMinimapToggle, UpdateType.CONFIG,
			"Minimap button",
			Button.MINIMAP_BUTTON_CLASSIC,
			Button.MINIMAP_BUTTON_MODERN
		);

		registerOrbToggle(ConfigKeys.COMPASS, config::hideCompass, UpdateType.CONFIG,
			"Compass",
			Minimap.MODERN_MAP_MINIMAP,
			Minimap.CLASSIC_MAP_MINIMAP
		);
	}

	private void updateOrbByScript(int scriptId)
	{
		if (scriptId == Script.FORCE_UPDATE)
		{
			hideByConfigMap.values().forEach(this::updateOrb);
			return;
		}

		OrbToggle toggle = hideByScriptMap.get(scriptId);
		if (toggle != null)
		{
			updateOrb(toggle);
		}
	}

	private void updateOrb(OrbToggle toggle)
	{
		if (toggle.key.equals(ConfigKeys.HIDE_LOGOUT_X) ||
			toggle.key.equals(ConfigKeys.HIDE_WORLD) ||
			toggle.key.equals(ConfigKeys.HIDE_WIKI))
		{
			return;
		}

		if (!isEditingLayout && toggle.key.equals(ConfigKeys.COMPASS))
		{
			return;
		}

		widgetManager.setTargetsHidden(
			toggle.hidden.get(),
			toggle.targets
		);
	}

	//apply toggle setting to orbs based on the config key in onConfigChanged
	public void updateOrbByConfig(String key)
	{
		if (isEditingLayout || key.equals(ConfigKeys.COMPASS))
		{
			return;
		}

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
				if (!isClassicResizable())
				{
					updateLogoutX();
					updateLogoutXPosition();
				}
				break;

			default:
				OrbToggle toggle = hideByConfigMap.get(key);
				if (toggle != null)
				{
					widgetManager.setTargetsHidden(
						toggle.hidden.get(),
						toggle.targets
					);

					//update the minimap toggle button when hiding/showing store orb,
					//while minimap is hidden, and button position is below map
					if (key.equals(ConfigKeys.HIDE_STORE)
						&& config.minimapTogglePlacement() == TogglePlacement.BELOW_MAP
						&& !isMinimapHidden()
						&& isLoggedIn())
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
			saveConfig(entry.getConfigKey(), value);
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

	public void resetCustomPositionForTarget(Binding binding, boolean remap)
	{
		clearCustomPosition(binding.getModern());
		clearCustomPosition(binding.getClassic());

		if (!remap)
		{
			return;
		}

		TargetWidget target = binding.get(isClassicResizable());
		if (target == null)
		{
			return;
		}

		resetCustomPosition(binding, target);
	}

	private void clearCustomPosition(TargetWidget target)
	{
		if (target == null)
		{
			return;
		}

		String x = getCustomLayoutKey(target, ValueKey.X);
		String y = getCustomLayoutKey(target, ValueKey.Y);

		configManager.unsetConfiguration(ConfigGroup.GROUP_NAME, x);
		configManager.unsetConfiguration(ConfigGroup.GROUP_NAME, y);
	}

	private void resetCustomPosition(Binding binding, TargetWidget target)
	{
		widgetManager.remapTarget(target, true);

		Widget bound = widgetManager.getTargetWidget(target);
		if (bound != null)
		{
			binding.getHandler().setOriginalX(bound.getOriginalX());
			binding.getHandler().setOriginalY(bound.getOriginalY());
			binding.getHandler().revalidate();

			if (binding.getRelated() != null)
			{
				Widget related = widgetManager.getTargetWidget(binding.getRelated());
				related.setOriginalX(bound.getOriginalX());
				related.setOriginalY(bound.getOriginalY());
				related.revalidate();
			}
		}
	}

	//remap should only be false for profile swaps / full config resets
	public void resetAllCustomPositions(boolean remap)
	{
		for (Binding binding : bindingManager.all())
		{
			resetCustomPositionForTarget(binding, remap);
		}

		if (!remap)
		{
			//may be unnecessary since bindings are cleared on the next edit-mode
			bindingManager.clear();
		}
	}

	public int getCustomPosition(int componentId, int index, ValueKey key)
	{
		String configKey = buildCustomLayoutKey(componentId, index, key);
		Integer value = configManager.getConfiguration(ConfigGroup.GROUP_NAME, configKey, Integer.class);
		return value != null ? value : -1;
	}

	public void saveCustomPosition(Widget bound, Binding binding)
	{
		if (bound == null || binding == null)
		{
			return;
		}

		int x = bound.getOriginalX();
		int y = bound.getOriginalY();

		if (binding.getModern() != null)
		{
			saveCustomPosition(binding.getModern(), x, y);

			if (binding.getClassic() != null)
			{
				saveCustomPosition(binding.getClassic(), x, y);
			}

			if (binding.getRelated() != null)
			{
				saveCustomPosition(binding.getRelated(), x, y);
			}
		}
	}

	private void saveCustomPosition(TargetWidget target, int x, int y)
	{
		saveConfig(getCustomLayoutKey(target, ValueKey.X), x);
		saveConfig(getCustomLayoutKey(target, ValueKey.Y), y);
	}

	private String buildCustomLayoutKey(int componentId, int index, ValueKey key)
	{
		String suffix = key == ValueKey.X ? "_x" : "_y";
		String id = componentId + "_" + index;
		return ConfigKeys.CUSTOM_LAYOUT_PREFIX + id + suffix;
	}

	private String getCustomLayoutKey(TargetWidget target, ValueKey key)
	{
		int id = target.getComponentId();
		int index = target.getArrayId();
		return buildCustomLayoutKey(id, index, key);
	}

	public <T> void saveConfig(String key, T value)
	{
		configManager.setConfiguration(ConfigGroup.GROUP_NAME, key, value);
	}

	public void updateConfig()
	{
		Integer version = configManager.getConfiguration(
			ConfigGroup.GROUP_NAME, ConfigKeys.CONFIG_VERSION, Integer.class);

		if (version == null)
		{
			version = 0;
		}

		if (version < ConfigGroup.CONFIG_VERSION)
		{
			migrateConfigs(
				new MigrateConfig<>(
					"hotkeyToggle",
					ConfigKeys.HOTKEY_KEYBIND,
					Keybind.class,
					Function.identity()
				),
				new MigrateConfig<>(
					"hotkeyMinimap",
					ConfigKeys.HOTKEY_TOGGLE_OPTION,
					Boolean.class,
					enabled ->
					{
						if (enabled)
						{
							return HotkeyOptions.MINIMAP;
						}
						return null;
					}
				),
				new MigrateConfig<>(
					"verticalPosition",
					ConfigKeys.HORIZONTAL_ANCHOR,
					HorizontalAnchor.class,
					HorizontalAnchor::name
				),
				new MigrateConfig<>(
					"horizontalPosition",
					ConfigKeys.VERTICAL_ANCHOR,
					VerticalAnchor.class,
					VerticalAnchor::name
				),
				new MigrateConfig<>(
					"enableVerticalHeightOffset",
					ConfigKeys.VERTICAL_Y_ADJUSTMENT,
					Boolean.class,
					enabled ->
					{
						if (enabled && getCurrentLayout().isVertical())
						{
							//max offset possible from the old config
							return 35;
						}
						return null;
					})
			);

			removeOldConfigs();

			saveConfig(ConfigKeys.CONFIG_VERSION, ConfigGroup.CONFIG_VERSION);
		}
	}

	private void migrateConfigs(MigrateConfig<?, ?>... configs)
	{
		for (MigrateConfig<?, ?> config : configs)
		{
			if (config.write(configManager))
			{
				config.unset(configManager);
			}
		}
	}

	private static final String[] REMOVED_KEYS = {
		//removed in 1.1 (d2f5247)
		"hideToggle",

		//removed in 1.7.5 (e60c595)
		"enableWorldMapOverlay",
		"enableXPDropOverlay",

		//removed in 1.9.0 (83a3a2d)
		"hideCompassButton"
	};

	private void removeOldConfigs()
	{
		for (String key : REMOVED_KEYS)
		{
			configManager.unsetConfiguration(ConfigGroup.GROUP_NAME, key);
		}
	}
}
