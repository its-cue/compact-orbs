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

import com.compactorbs.CompactOrbsConstants.ConfigGroup;
import static com.compactorbs.CompactOrbsConstants.ConfigGroup.GROUP_NAME;
import com.compactorbs.CompactOrbsConstants.ConfigKeys;
import com.compactorbs.CompactOrbsConstants.Script;
import com.compactorbs.CompactOrbsConstants.Varbit;
import com.compactorbs.CompactOrbsConstants.Widgets.Classic;
import com.compactorbs.CompactOrbsConstants.Widgets.Modern;
import com.compactorbs.CompactOrbsConstants.Widgets.Orb;
import com.compactorbs.widget.WidgetManager;
import com.compactorbs.widget.elements.Compass;
import com.compactorbs.widget.elements.Orbs;
import com.compactorbs.widget.overlay.MinimapOverlay;
import com.compactorbs.widget.slot.Slot;
import com.compactorbs.widget.slot.SlotLayout;
import com.compactorbs.widget.slot.SlotManager;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.PluginChanged;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;

@Slf4j
@PluginDescriptor(
	name = "Compact Orbs",
	description = "Collapse the minimap orbs into a compact view.",
	tags = {"compact", "orbs", "layout", "hide", "minimap", "resizable", "classic", "modern", "world", "map", "wiki", "swap", "overlay"},
	conflicts = {"Fixed Resizable Hybrid", "Orb Hider", "Minimap Hider"}
)
public class CompactOrbsPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private CompactOrbsConfig config;

	@Inject
	private CompactOrbsManager manager;

	@Inject
	private ConfigManager configManager;

	@Inject
	private KeyManager keyManager;

	@Inject
	private MinimapOverlay minimapOverlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private WidgetManager widgetManager;

	@Inject
	private SlotManager slotManager;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(minimapOverlay);
		keyManager.registerKeyListener(hotkeyListener);
		registerOrbToggleEntries();

		manager.pendingChildrenUpdate = false;
		slotManager.allowFixedModeUpdate = true;
		manager.hideWorldMap = config.hideWorld();
		manager.hideLogoutX = config.hideLogout();

		clientThread.invoke(() ->
		{
			slotManager.initSlots();

			if (manager.isLoggedIn())
			{
				manager.handleLogoutXHiddenState(false);

				manager.init(Script.FORCE_UPDATE);
				manager.configureMinimapOverlayContainer(true);
			}
		});

		manager.resolveWikiBannerConflict(ConfigGroup.Wiki.GROUP_NAME, ConfigKeys.Wiki.SHOW_WIKI_MINIMAP_BUTTON);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(minimapOverlay);

		keyManager.unregisterKeyListener(hotkeyListener);

		clientThread.invoke(manager::reset);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (manager.isLoggedIn())
		{
			manager.createCustomChildren();

			slotManager.allowFixedModeUpdate = true;
		}
	}

	@Subscribe(priority = -1.0f)
	public void onScriptPostFired(ScriptPostFired event)
	{
		int scriptId = event.getScriptId();

		switch (scriptId)
		{
			case Script.TOPLEVEL_REDRAW:
				//check for an active cutscene
				manager.pendingChildrenUpdate = manager.isCutsceneActive();

			case Script.TOPLEVEL_SUBCHANGE:
			case Script.TOPLEVEL_SIDE_CUSTOMIZE:
				manager.handleLogoutXHiddenState(false);
				manager.updateOverlayLogoutX();
				break;

			case Script.BUFF_BAR_CONTENT_UPDATE:
				if (minimapOverlay.hasUpdatedBounds() && manager.pendingMinimapOverlayChildren)
				{
					log.debug("[StatBoostsHud] is ready for the minimap overlay children");

					manager.pendingMinimapOverlayChildren = false;
					clientThread.invokeLater(manager::createMinimapOverlayChildren);
				}
				break;

			case Script.WIKI_ICON_INIT:
				if (manager.isWikiBannerDisabled())
				{
					manager.updateWikiBannerVisibility(config.hideWiki());
				}
				break;

			case Script.WORLD_MAP_UPDATE:
				if (manager.hideWorldMap)
				{
					widgetManager.remapTarget(Orbs.WORLD_MAP_CONTAINER, config.hideMinimap() && manager.isResized());
					return;
				}
			case Script.STORE_ORB_UPDATE:
			case Script.ACTIVITY_ORB_UPDATE:
			case Script.WIKI_ICON_UPDATE:
			case Script.GRID_MASTER_ORB_UPDATE:
				if (!manager.isMinimapMinimized())
				{
					manager.init(scriptId);
				}
				break;
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		int varbitId = event.getVarbitId();

		switch (varbitId)
		{
			case Varbit.MINIMAP_TOGGLE:
				manager.updateCustomChildren(true);
				break;

			case Varbit.STORE_ORB_TOGGLE:
			case Varbit.ACTIVITY_ORB_TOGGLE:
				if (!config.disableReordering())
				{
					widgetManager.remapTargets(manager.isCompactLayout(), Script.FORCE_UPDATE, Orbs.values());
				}
				break;
		}
	}

	@Subscribe(priority = -1.0f)
	public void onWidgetLoaded(WidgetLoaded event)
	{
		int id = event.getGroupId();
		switch (id)
		{
			case Orb.UNIVERSE >> 16:
			case Classic.ORBS >> 16:
			case Modern.ORBS >> 16:
				manager.init(Script.FORCE_UPDATE);
				manager.configureMinimapOverlayContainer(true);
				break;
		}
	}

	@Subscribe(priority = -1.0f)
	public void onConfigChanged(ConfigChanged event)
	{
		String group = event.getGroup();
		String key = event.getKey();

		if (group.equals(ConfigGroup.Wiki.GROUP_NAME))
		{
			if (key.equals(ConfigKeys.Wiki.SHOW_WIKI_MINIMAP_BUTTON))
			{
				manager.resolveWikiBannerConflict(GROUP_NAME, ConfigKeys.HIDE_WIKI);

				clientThread.invokeLater(() ->
				{
					widgetManager.remapTarget(Orbs.WIKI_ICON_CONTAINER, manager.isCompactLayout());
					manager.updateWikiBannerVisibility(config.hideWiki());
				});
			}
		}

		if (!group.equals(GROUP_NAME))
		{
			return;
		}

		SlotLayout slotLayout = Slot.getSlotByConfigKey(event.getKey());
		if (slotLayout != null)
		{
			clientThread.invokeLater(() ->
				slotManager.applySlotSwap(slotLayout.getSlot(), slotLayout.getLayout())
			);
			return;
		}

		switch (key)
		{
			case ConfigKeys.MINIMAP:
				if (key.equals(manager.suppressConfigChangedKey))
				{
					return;
				}

				//rebuild to prevent stale layout when config is set to defaults
				clientThread.invokeLater(() -> manager.init(Script.FORCE_UPDATE));
				break;

			case ConfigKeys.COMPASS:
			case ConfigKeys.HOTKEY_TOGGLE:
			case ConfigKeys.HOTKEY_MINIMAP:
				//do nothing (prevent default behaviour)
				break;

			case ConfigKeys.MINIMAP_BUTTON_PLACEMENT:
				clientThread.invokeLater(manager::updateMinimapToggleButton);
				break;

			case ConfigKeys.MINIMAP_TOGGLE_BUTTON:
			case ConfigKeys.COMPASS_TOGGLE_BUTTON:
				clientThread.invokeLater(() -> manager.updateCustomChildren(true));
				break;

			case ConfigKeys.ENABLE_NO_CLICKTHROUGH:
				clientThread.invokeLater(() ->
				{
					widgetManager.setTargetsNoClickthrough(config.enableNoClickthrough() && manager.isCompactLayout(),
						Orbs.HP_ORB_CONTAINER, Orbs.PRAYER_ORB_CONTAINER, Orbs.RUN_ORB_CONTAINER, Orbs.SPEC_ORB_CONTAINER);

					manager.updateCompassToggleButton();
				});
				break;

			//update all slots
			case ConfigKeys.ENABLE_ORB_SWAPPING:
				clientThread.invoke(() -> slotManager.generateSlots(true));
				break;

			case ConfigKeys.ORB_LAYOUT:
			case ConfigKeys.HORIZONTAL:
			case ConfigKeys.VERTICAL:
				clientThread.invokeLater(() -> manager.init(Script.FORCE_UPDATE));
				break;

			case ConfigKeys.ENABLE_MINIMAP_OVERLAY:
				clientThread.invokeLater(() -> manager.updateMinimapOverlayVisibility());
				break;

			case ConfigKeys.ENABLE_LOGOUT_X_OVERLAY:
				clientThread.invokeLater(() ->
				{
					if (!config.showMinimapInCompactView())
					{
						return;
					}

					manager.handleLogoutXHiddenState(true);
				});
				break;

			case ConfigKeys.ENABLE_VERTICAL_HEIGHT_OFFSET:
			default:
				clientThread.invokeLater(() ->
				{
					manager.getLayoutOffsets();

					if (!key.equals(ConfigKeys.ENABLE_VERTICAL_HEIGHT_OFFSET))
					{
						manager.updateOrbByConfig(event.getKey());
					}

					if (manager.isCompactLayout() && widgetManager.getCurrentParent() != null)
					{
						//update the orbs positions when hiding/showing
						widgetManager.remapTargets(true, Script.FORCE_UPDATE, Orbs.values());

						//update the compass positions - to enable repositioning based on hidden/shown orbs
						widgetManager.remapTargets(true, Script.FORCE_UPDATE, Compass.values());

						//update custom children
						manager.updateCompassFrameChild();
						manager.updateCompassToggleButton();
						manager.updateMinimapToggleButton();
					}
				});
				break;
		}
	}

	@Subscribe
	public void onPluginChanged(PluginChanged event)
	{
		String plugin = event.getPlugin().getName();

		if (!plugin.equalsIgnoreCase(ConfigGroup.Wiki.GROUP_NAME))
		{
			return;
		}

		manager.resolveWikiBannerConflict(GROUP_NAME, ConfigKeys.HIDE_WIKI);

		//rebuild layout if the wiki plugin is turned on or off
		clientThread.invokeLater(() -> manager.init(Script.FORCE_UPDATE));
	}

	//register all orb hiding toggles on startup
	public void registerOrbToggleEntries()
	{
		manager.registerOrbToggle(ConfigKeys.HIDE_HP, config::hideHp, Orbs.HP_ORB_CONTAINER);
		manager.registerOrbToggle(ConfigKeys.HIDE_PRAYER, config::hidePray, Orbs.PRAYER_ORB_CONTAINER);
		manager.registerOrbToggle(ConfigKeys.HIDE_RUN, config::hideRun, Orbs.RUN_ORB_CONTAINER);
		manager.registerOrbToggle(ConfigKeys.HIDE_SPEC, config::hideSpec, Orbs.SPEC_ORB_CONTAINER);
		manager.registerOrbToggle(ConfigKeys.HIDE_XP, config::hideXp, Orbs.XP_DROPS_CONTAINER);
		manager.registerOrbToggle(ConfigKeys.HIDE_ACTIVITY, config::hideActivity, Orbs.ACTIVITY_ORB_CONTAINER);
		manager.registerOrbToggle(ConfigKeys.HIDE_STORE, config::hideStore, Orbs.STORE_ORB_CONTAINER);
		manager.registerOrbToggle(ConfigKeys.HIDE_LOGOUT_X, config::hideLogout, Orbs.LOGOUT_X_ICON, Orbs.LOGOUT_X_STONE);
		manager.registerOrbToggle(ConfigKeys.HIDE_GRID, config::hideGrid, Orbs.GRID_MASTER_ORB_CONTAINER);
		manager.registerOrbToggle(ConfigKeys.HIDE_WORLD, config::hideWorld, Orbs.WORLD_MAP_CONTAINER);
		manager.registerOrbToggle(ConfigKeys.HIDE_WIKI, config::hideWiki, Orbs.WIKI_VANILLA_GRAPHIC, Orbs.WIKI_VANILLA_CONTAINER, Orbs.WIKI_ICON_CONTAINER);
	}

	private final HotkeyListener hotkeyListener = new HotkeyListener(() -> config.toggleButtonHotkey())
	{
		@Override
		public void hotkeyPressed()
		{
			//prevent hotkey in fixed mode
			if (!manager.isResized())
			{
				return;
			}

			if (config.minimapHotkey())
			{
				clientThread.invokeLater(manager::onMinimapToggle);
				return;
			}

			boolean hidden = !(config.hideMinimapToggle() || config.hideCompassToggle());

			configManager.setConfiguration(GROUP_NAME, ConfigKeys.MINIMAP_TOGGLE_BUTTON, hidden);
			configManager.setConfiguration(GROUP_NAME, ConfigKeys.COMPASS_TOGGLE_BUTTON, hidden);

			clientThread.invokeLater(() -> manager.updateCustomChildren(true));
		}
	};

	@Provides
	CompactOrbsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CompactOrbsConfig.class);
	}
}
