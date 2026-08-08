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
import com.compactorbs.CompactOrbsConstants.Layout;
import com.compactorbs.CompactOrbsConstants.MenuOp;
import com.compactorbs.CompactOrbsConstants.Script;
import com.compactorbs.CompactOrbsConstants.Sprite;
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

	public boolean isUpdatingProfile;
	public boolean isEditingLayout;
	public boolean hideWorldMap;
	public boolean hideLogoutX;
	public boolean enableNoClickThrough;
	public boolean hasSeenWikiWarning;
	public boolean isLoggingIn;
	public boolean isCutsceneActive;
	public boolean detachedMinimapParentIsReady;

	public Widget compassFrame;
	public Widget minimapButton;
	private Widget overlayCompass;
	private Widget overlayCompassLayer;
	private Widget overlayCompassNoClick;
	private Widget overlayCompassMenuOp;
	private Widget overlayMinimap;
	private Widget overlayMinimapFrame;
	private Widget overlayLogoutXStone;
	private Widget overlayLogoutXIcon;
	private final Map<TargetWidget, Widget> noClickThroughChildren = new HashMap<>();

	private final Map<String, OrbToggle> hideByConfigMap = new HashMap<>();
	private final Map<Integer, OrbToggle> hideByScriptMap = new HashMap<>();
	public final Map<TargetWidget, OrbToggle> toggleByTarget = new HashMap<>();

	//update on startup, onWidgetLoaded, and onScriptPostFired
	public void update(int scriptId)
	{
		updateWikiBannerVisibility(config.hideWiki());
		hideOrbByScript(scriptId);
		createCustomChildren();

		if (scriptId == Script.FORCE_UPDATE)
		{
			rebuildLayout();
			return;
		}

		widgetManager.remapTargetsByScriptId(scriptId, Orbs.values());
		updateNoClickThrough();
	}

	//rebuild the layout from state/config
	public void rebuildLayout()
	{
		updateCustomChildren();

		if (isFixedMode())
		{
			widgetManager.setHidden(MinimapOverlay.UNIVERSE, true);
			widgetManager.remapTargets(Orbs.values());
			updateNoClickThrough();
			return;
		}

		setupMinimapContainer(false);
		widgetManager.setTargetsHidden(isCompactLayout() && isCompassHidden(), Compass.values());
		widgetManager.remapTargets(Compass.values());
		widgetManager.remapTargets(Orbs.values());
		updateNoClickThrough();

		if (!isClassicResizable() && !isFixedMode())
		{
			hideLogout();
		}
	}

	//clear anything that can persist during runtime
	private void clearRuntimeState()
	{
		hasSeenWikiWarning = false;
		hideWorldMap = false;
		hideLogoutX = false;
		enableNoClickThrough = false;
		isUpdatingProfile = false;

		bindingManager.clear();
		hideByConfigMap.clear();
		hideByScriptMap.clear();
		toggleByTarget.clear();

		slotManager.reset();
	}

	//only called on plugin shutdown
	public void reset()
	{
		if (isEditingLayout)
		{
			//save configs on shutdown
			isUpdatingProfile = false;
			editLayout.toggleEditMode(false);
		}

		clearRuntimeState();

		clearCustomChildren();

		resetVisibility();
		resetPositioning();
		resetNoClickThrough();
	}

	//used for profile swap/full config reset
	public void rebuild(boolean reset)
	{
		if (isEditingLayout)
		{
			//do not save configs
			editLayout.toggleEditMode(false);
		}

		clearRuntimeState();

		registerOrbsHidden();
		hideAllOrbsByScript();
		slotManager.initSlots();

		if (reset)
		{
			resetSavedPositionConfigs();
		}
		rebuildLayout();
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
			widgetManager.remapTargets(true, Script.FORCE_UPDATE, Orbs.WORLD_MAP_CONTAINER);
		}

		setupMinimapContainer(true);

		widgetManager.remapTargets(true, Script.FORCE_UPDATE, Orbs.values());
		widgetManager.remapTargets(true, Script.FORCE_UPDATE, Compass.values());
	}

	private void resetNoClickThrough()
	{
		for (TargetWidget orb : Orbs.SWAPPABLE_ORBS)
		{
			widgetManager.setNoClickThrough(orb.getComponentId(), false);
			handleVanillaNoClickThrough(orb, false);
			widgetManager.clearChild(noClickThroughChildren.remove(orb));
		}
	}

	//toggle the minimap visibility, and update related widgets when using the custom toggle button
	public void onMinimapToggle()
	{
		saveConfig(ConfigKeys.MINIMAP, !isMinimapHidden());
		rebuildLayout();
	}

	public void setupMinimapContainer(boolean toDefault)
	{
		widgetManager.setTargetsHidden(!toDefault && isCompactLayout(), Minimap.COMPONENTS);
		widgetManager.remapTargets(toDefault, Script.FORCE_UPDATE, Minimap.CONTAINERS);
		widgetManager.revalidate(Minimap.COMPONENTS);

		//TODO - seems like revalidation causes the width/height issue (on the orbsContainer)
		//Orb.UNIVERSE parent width/height mode seems to cause an issue when setting dimensions
		//causing the Orbs.UNIVERSE container be set to the dimensions of the tli (GAMEFRAME)
		//this should alleviate that by setting the size temporarily until restored by a clientscript (clicking a side panel tab, etc)
		if (!isFixedMode() && !isMinimapMinimized())
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
		if (isFixedMode())
		{
			return;
		}

		boolean noClick = enableNoClickThrough ||
			(config.enableOrbSwapping() && isCompactLayout() && !getCurrentLayout().isCustom());

		for (TargetWidget orb : Orbs.SWAPPABLE_ORBS)
		{
			widgetManager.setNoClickThrough(orb.getComponentId(), noClick && isCompactLayout());

			if (!isCompactLayout())
			{
				handleVanillaNoClickThrough(orb, noClick);
			}
			else
			{
				widgetManager.clearChild(noClickThroughChildren.remove(orb));
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
			widgetManager.clearChild(noClickThroughChildren.remove(target));
			return;
		}

		Widget noClick = noClickThroughChildren.get(target);
		if (noClick == null)
		{
			noClick = widgetManager.createNoClick(layer, button);
			noClick.setNoClickThrough(true);
			noClick.revalidate();

			noClickThroughChildren.put(target, noClick);
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
			return;
		}

		if (minimapButton != null && minimapButton.getParent() != parent)
		{
			minimapButton = null;
		}

		if (minimapButton == null)
		{
			minimapButton = widgetManager.createMinimapButton(parent);
			minimapButton.setAction(MenuOp.OP_INDEX_0, buildToggleOp(isMinimapHidden(), MenuOp.MINIMAP_OP));
			minimapButton.setOnOpListener(
				(JavaScriptCallback) e ->
				{
					switch (e.getOp() - 1)
					{
						case MenuOp.OP_INDEX_0:
							if (!isFixedMode())
							{
								onMinimapToggle();
								minimapButton.setSpriteId(widgetManager.getSpriteId(!isMinimapHidden()));
							}
							break;

						case MenuOp.EDIT_MODE_OP_INDEX:
							if (isFixedMode())
							{
								editLayout.toggleEditMode(true);
							}
							break;
					}
				}
			);
			minimapButton.setOnMouseOverListener(
				(JavaScriptCallback) e ->
				{
					minimapButton.setOpacity(Layout.OPACITY_HOVER);
				}
			);
			minimapButton.setOnMouseLeaveListener(
				(JavaScriptCallback) e ->
					minimapButton.setOpacity(Layout.OPACITY)
			);
		}

		//compass frame moved to the compass container
		parent = client.getWidget(isClassicResizable() ? Classic.COMPASS_PARENT : Modern.COMPASS_PARENT);
		if (parent == null)
		{
			if (compassFrame != null)
			{
				compassFrame = null;
			}
			return;
		}

		if (compassFrame != null && compassFrame.getParent() != parent || isFixedMode())
		{
			compassFrame = null;
		}

		if (compassFrame == null && !isFixedMode())
		{
			compassFrame = widgetManager.createCompassFrame(parent);
		}
	}

	public void updateCustomChildren()
	{
		updateCompassFrameChild();
		updateMinimapToggleButton();
		widgetManager.setHidden(MinimapOverlay.UNIVERSE, hideMinimapOverlay());
	}

	private void updateCompassFrameChild()
	{
		if (compassFrame == null)
		{
			return;
		}

		log.debug("setting compass frame hidden: {}", !isMinimapHidden() || isCompassHidden() || isMinimapMinimized());
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
			if (!config.rightClickToggleButtons() || isFixedMode())
			{
				int index = MenuOp.OP_INDEX_0;
				if (isFixedMode())
				{
					index = MenuOp.EDIT_MODE_OP_INDEX;
				}
				minimapButton.setAction(
					index == MenuOp.OP_INDEX_0
						? MenuOp.EDIT_MODE_OP_INDEX
						: MenuOp.OP_INDEX_0, "");

				minimapButton.setAction(index,
					isFixedMode()
						? buildEditOp(false)
						: buildToggleOp(isMinimapHidden(), MenuOp.MINIMAP_OP));
			}

			minimapButton.setNoClickThrough(!config.rightClickToggleButtons());
			widgetManager.remapTargets(
				Button.MINIMAP_BUTTON_MODERN,
				Button.MINIMAP_BUTTON_CLASSIC,
				Button.MINIMAP_BUTTON_FIXED
			);
		}
	}

	//clear any created children and reset previous parent id
	void clearCustomChildren()
	{
		widgetManager.clearChild(minimapButton);
		widgetManager.clearChild(compassFrame);
		widgetManager.clearChild(overlayLogoutXIcon);
		widgetManager.clearChild(overlayLogoutXStone);

		//reset & clear the minimap overlay
		configureMinimapOverlayContainer(false);
		final Widget parent = client.getWidget(MinimapOverlay.UNIVERSE);
		if (parent != null)
		{
			parent.deleteAllChildren();
		}

		overlayCompass = null;
		overlayCompassLayer = null;
		overlayCompassNoClick = null;
		overlayCompassMenuOp = null;
		overlayMinimap = null;
		overlayMinimapFrame = null;
		overlayLogoutXStone = null;
		overlayLogoutXIcon = null;

		minimapButton = null;
		compassFrame = null;
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

		if (enabled && overlayMinimap != null)
		{
			return;
		}

		if (!enabled)
		{
			parent.setForcedPosition(-1, -1);
			parent.setHidden(false);
		}

		parent.setOriginalWidth(enabled ? Layout.MinimapOverlay.CONTAINER_WIDTH : 0);
		parent.setOriginalHeight(enabled ? Layout.MinimapOverlay.CONTAINER_HEIGHT : 0);
		parent.setWidthMode(enabled ? WidgetSizeMode.ABSOLUTE : WidgetSizeMode.MINUS);
		parent.setHeightMode(enabled ? WidgetSizeMode.ABSOLUTE : WidgetSizeMode.MINUS);
		parent.setXPositionMode(enabled ? WidgetPositionMode.ABSOLUTE_RIGHT : WidgetPositionMode.ABSOLUTE_CENTER);
		parent.setYPositionMode(enabled ? WidgetPositionMode.ABSOLUTE_TOP : WidgetPositionMode.ABSOLUTE_CENTER);
		parent.revalidate();

		detachedMinimapParentIsReady = true;
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
			final Widget mapNoClick = widgetManager.createOverlayNoClick(parent,
				Layout.MinimapOverlay.NO_CLICK_Y[index],
				Layout.MinimapOverlay.NO_CLICK_WIDTH[index],
				Layout.MinimapOverlay.NO_CLICK_HEIGHT[index]
			);
		}

		overlayCompass = widgetManager.createOverlayCompass(parent);
		overlayCompassLayer = widgetManager.createOverlayCompassLayer(parent);
		overlayCompassNoClick = widgetManager.createOverlayCompassNoClick(overlayCompassLayer);
		overlayCompassMenuOp = widgetManager.createOverlayCompassMenuOp(overlayCompassLayer);
		overlayMinimap = widgetManager.createOverlayMinimap(parent);
		overlayMinimapFrame = widgetManager.createOverlayMinimapFrame(parent);
		overlayLogoutXStone = widgetManager.createOverlayLogoutXStone(parent, hideOverlayLogoutX());
		overlayLogoutXIcon = widgetManager.createOverlayLogoutXIcon(parent, hideOverlayLogoutX());
	}

	public void setupMinimapOverlay()
	{
		configureMinimapOverlayContainer(true);
		clientThread.invokeLater(() ->
		{
			if (detachedMinimapParentIsReady)
			{
				createMinimapOverlayChildren();
				detachedMinimapParentIsReady = false;
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
			if (!isFixedMode())
			{
				if (!isEditingLayout)
				{
					if (config.rightClickToggleButtons())
					{
						entry
							.setOption(buildToggleOp(isMinimapHidden(), MenuOp.MINIMAP_OP))
							.setType(MenuAction.CC_OP_LOW_PRIORITY)
							.setDeprioritized(true);
					}

					if (isMinimapHidden() && config.showToggleOnMinimapButton())
					{
						menu.createMenuEntry(-2)
							.setOption(buildToggleOp(!config.showMinimapInCompactView(), MenuOp.DETACHED_OP))
							.setDeprioritized(config.rightClickToggleButtons())
							.setType(MenuAction.RUNELITE_LOW_PRIORITY)
							.onClick(e ->
								saveConfig(ConfigKeys.ENABLE_MINIMAP_OVERLAY, !config.showMinimapInCompactView())
							);
					}
				}

				int index = isCompactLayout() && config.showToggleOnMinimapButton() ? -3 : -2;
				menu.createMenuEntry(index)
					.setOption(buildEditOp(false))
					.setForceLeftClick(false)
					.setDeprioritized(config.rightClickToggleButtons())
					.setType(MenuAction.RUNELITE_LOW_PRIORITY)
					.onClick(e ->
						editLayout.toggleEditMode(true)
					);
			}
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
		if (parent == null || isFixedMode())
		{
			return false;
		}

		return parent.getId() == Classic.ORBS;
	}

	public boolean isLoggedIn()
	{
		return client.getGameState() == GameState.LOGGED_IN;
	}

	public boolean isCompactLayout()
	{
		return !isFixedMode() && isMinimapHidden() && !isMinimapMinimized();
	}

	public boolean isCustomLayout()
	{
		return getCurrentLayout().isCustom() && isCompactLayout();
	}

	public boolean isVanillaCustom()
	{
		return !config.enableOrbSwapping() && !isCompactLayout() && !isMinimapMinimized();
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
		if (isFixedMode())
		{
			return false;
		}

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

	boolean hideMinimapOverlay()
	{
		return !(isMinimapOverlayEnabled() && isMinimapHidden() && !isMinimapMinimized()) || isFixedMode();
	}

	public boolean isMinimapOverlayEnabled()
	{
		return config.showMinimapInCompactView();
	}

	//prevent unintended state changes for the logout-x since it is treated as a side icon/stone, i.e. don't unhide when it should be hidden
	public void hideLogout()
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
		widgetManager.remapTargets(Orbs.LOGOUT_X_STONE, Orbs.LOGOUT_X_ICON);
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

	public boolean getCutsceneStatus()
	{
		return client.getVarbitValue(Varbit.CUTSCENE_STATUS) == VarbitValue.CUTSCENE_ACTIVE;
	}

	public int getLayoutXOffset()
	{
		return isCompactLayout() && isAnchorRight()
			? getCurrentLayout().getRightOffset() : 0;
	}

	public int getLayoutYOffset()
	{
		return isCompactLayout() && isAnchorBottom()
			? getCurrentLayout().getBottomOffset() : 0;
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

	public boolean isHideConfig(String key)
	{
		return hideByConfigMap.containsKey(key);
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

	public void registerOrbsHidden()
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
			"Button",
			Button.MINIMAP_BUTTON_CLASSIC,
			Button.MINIMAP_BUTTON_MODERN,
			Button.MINIMAP_BUTTON_FIXED
		);

		registerOrbToggle(ConfigKeys.COMPASS, config::hideCompass, UpdateType.CONFIG,
			"Compass",
			Minimap.MODERN_MAP_MINIMAP,
			Minimap.CLASSIC_MAP_MINIMAP
		);
	}

	private void hideOrbByScript(int scriptId)
	{
		if (scriptId == Script.FORCE_UPDATE)
		{
			hideByConfigMap.values().forEach(this::hideOrb);
			return;
		}

		OrbToggle toggle = hideByScriptMap.get(scriptId);
		if (toggle != null)
		{
			hideOrb(toggle);
		}
	}

	private void hideOrb(OrbToggle toggle)
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

	public void hideAllOrbsByScript()
	{
		hideByConfigMap.keySet().forEach(this::hideOrbByConfig);
	}

	public void hideOrbByConfig(String key)
	{
		if (isUpdatingProfile || isEditingLayout || key.equals(ConfigKeys.COMPASS))
		{
			return;
		}

		switch (key)
		{
			case ConfigKeys.HIDE_WORLD:
				hideWorldMap = config.hideWorld();
				widgetManager.remapTargets(Orbs.WORLD_MAP_CONTAINER);
				break;

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
					hideLogout();
					updateLogoutXPosition();
				}
				break;

			default:
				OrbToggle toggle = hideByConfigMap.get(key);
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
				break;
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

	public boolean useSavedPosition(Widget widget, int index)
	{
		if (isCompactLayout() || config.enableOrbSwapping())
		{
			return false;
		}

		if (widget == null)
		{
			return false;
		}

		for (TargetWidget[] targets : EditLayout.EDIT_TARGETS)
		{
			boolean classic = targets.length > 1 && isClassicResizable() && targets[1] != null;
			boolean fixed = targets.length > 2 && isFixedMode() && targets[2] != null;

			final TargetWidget target =
				fixed ? targets[2] : classic ? targets[1] : targets[0];

			if (editLayout.blockEditing(target))
			{
				continue;
			}

			if (widget.getId() == target.getComponentId()
				&& index == target.getArrayId())
			{
				return true;
			}
		}

		return false;
	}

	public void resetTargetsSavedPosition(Binding binding, boolean remap)
	{
		if (isFixedMode())
		{
			clearSavedPosition(binding.getFixed());
			if (binding.getFixed() == null)
			{
				clearSavedPosition(binding.getModern());
			}
		}
		else
		{
			clearSavedPosition(binding.getModern());
			clearSavedPosition(binding.getClassic());
		}

		if (remap)
		{
			TargetWidget target = binding.get(this);
			if (target == null)
			{
				return;
			}

			widgetManager.remapTargets(target);

			Widget bound = widgetManager.getTargetWidget(target);
			if (bound != null)
			{
				Widget handler = binding.getHandler();
				if (handler != null)
				{
					int x = editLayout.setHandlerX(bound, handler.getParent());
					int y = editLayout.setHandlerY(bound, handler.getParent());

					binding.getHandler().setOriginalX(x);
					binding.getHandler().setOriginalY(y);
					binding.getHandler().revalidate();

					if (binding.getRelated() != null)
					{
						Widget related = widgetManager.getTargetWidget(binding.getRelated());
						related.setOriginalX(x);
						related.setOriginalY(y);
						related.revalidate();
					}
				}
			}
		}
	}

	private void clearSavedPosition(TargetWidget target)
	{
		if (target == null)
		{
			return;
		}

		String x = getSavedKey(target, ValueKey.X);
		String y = getSavedKey(target, ValueKey.Y);

		configManager.unsetConfiguration(ConfigGroup.GROUP_NAME, x);
		configManager.unsetConfiguration(ConfigGroup.GROUP_NAME, y);
	}

	//remap should only be false for profile swaps / full config resets
	public void resetAllSavedPositions(boolean remap)
	{
		for (Binding binding : bindingManager.all())
		{
			resetTargetsSavedPosition(binding, remap);
		}

		if (!remap)
		{
			//may be unnecessary since bindings are cleared on the next edit-mode
			bindingManager.clear();
		}
	}

	public void resetSavedPositionConfigs()
	{
		String[] prefixes =
			{
				ConfigKeys.CUSTOM_LAYOUT_PREFIX,
				ConfigKeys.VANILLA_LAYOUT_PREFIX,
				ConfigKeys.FIXED_LAYOUT_PREFIX
			};

		for (TargetWidget[] targets : EditLayout.EDIT_TARGETS)
		{
			for (TargetWidget target : targets)
			{
				if (target == null)
				{
					continue;
				}

				for (String prefix : prefixes)
				{
					configManager.unsetConfiguration(ConfigGroup.GROUP_NAME,
						buildSavedKey(prefix, target.getComponentId(), target.getArrayId(), ValueKey.X));

					configManager.unsetConfiguration(ConfigGroup.GROUP_NAME,
						buildSavedKey(prefix, target.getComponentId(), target.getArrayId(), ValueKey.Y));
				}
			}
		}
	}

	public int getSavedPosition(Widget widget, int index, ValueKey key)
	{
		if ((isCompactLayout() && !getCurrentLayout().isCustom()))
		{
			return -1;
		}
		else if (widget.getId() == Orbs.WORLD_MAP_CONTAINER.getComponentId())
		{
			if (hideWorldMap)
			{
				return -1;
			}
		}
		else if (widget.getId() == Orbs.LOGOUT_X_ICON.getComponentId())
		{
			if (hideLogoutX)
			{
				return -1;
			}
		}

		String configKey = buildSavedKey(getCurrentPrefix(), widget.getId(), index, key);
		Integer value = configManager.getConfiguration(ConfigGroup.GROUP_NAME, configKey, Integer.class);
		return value != null ? value : -1;
	}

	public void saveCurrentLayoutPosition(Widget bound, Binding binding)
	{
		if (bound == null || binding == null)
		{
			return;
		}

		int x = bound.getOriginalX();
		int y = bound.getOriginalY();

		if (isFixedMode())
		{
			savePosition(binding.getFixed(), x, y);
			if (binding.getFixed() == null)
			{
				savePosition(binding.getModern(), x, y);
			}
			return;
		}

		savePosition(binding.getModern(), x, y);
		savePosition(binding.getClassic(), x, y);
		savePosition(binding.getRelated(), x, y);
	}

	private void savePosition(TargetWidget target, int x, int y)
	{
		if (target != null)
		{
			saveConfig(getSavedKey(target, com.compactorbs.util.ValueKey.X), x);
			saveConfig(getSavedKey(target, com.compactorbs.util.ValueKey.Y), y);
		}
	}

	private String getSavedKey(TargetWidget target, ValueKey key)
	{
		int id = target.getComponentId();
		int index = target.getArrayId();
		return buildSavedKey(getCurrentPrefix(), id, index, key);
	}

	private String buildSavedKey(String prefix, int componentId, int index, ValueKey key)
	{
		String suffix = key == com.compactorbs.util.ValueKey.X ? "_x" : "_y";
		String id = componentId + "_" + index;
		return prefix + id + suffix;
	}

	public String getCurrentPrefix()
	{
		return isCompactLayout() ? ConfigKeys.CUSTOM_LAYOUT_PREFIX :
			!isFixedMode() ? ConfigKeys.VANILLA_LAYOUT_PREFIX : ConfigKeys.FIXED_LAYOUT_PREFIX;
	}

	public <T> void saveConfig(String key, T value)
	{
		configManager.setConfiguration(ConfigGroup.GROUP_NAME, key, value);
	}

	public void migrateConfigs()
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
