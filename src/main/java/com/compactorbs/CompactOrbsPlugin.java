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

import static com.compactorbs.CompactOrbsConstants.ConfigGroup.GROUP_NAME;
import com.compactorbs.CompactOrbsConstants.ConfigKeys;
import com.compactorbs.CompactOrbsConstants.Script;
import com.compactorbs.CompactOrbsConstants.Varbit;
import com.compactorbs.CompactOrbsConstants.Widgets.Classic;
import com.compactorbs.CompactOrbsConstants.Widgets.Modern;
import com.compactorbs.CompactOrbsConstants.Widgets.Orb;
import com.compactorbs.widget.elements.Compass;
import com.compactorbs.widget.overlay.MinimapOverlay;
import com.compactorbs.widget.slot.SlotLayout;
import com.compactorbs.widget.slot.SlotManager;
import com.compactorbs.widget.WidgetManager;
import com.compactorbs.widget.elements.Orbs;
import com.compactorbs.widget.slot.Slot;
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
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;

@Slf4j
@PluginDescriptor(
	name = "Compact Orbs",
	description = "Collapse the minimap orbs into a compact view.",
	tags = {"compact", "orbs", "layout", "hide", "minimap", "resizable", "classic", "modern", "world", "map", "wiki", "swap"},
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

		manager.hideWorldMap = config.hideWorld();

		clientThread.invoke(() ->
		{
			slotManager.initSlots();

			if (manager.isLoggedIn())
			{
				manager.init(Script.FORCE_UPDATE);
				manager.configureMinimapOverlayContainer(true);
			}
		});
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

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		int scriptId = event.getScriptId();

		if (scriptId == Script.TOP_LEVEL_REDRAW || scriptId == Script.TOP_LEVEL_SIDE_CUSTOMIZE)
		{
			//keep the logout X hidden
			if (config.hideLogout() && !manager.isMinimized())
			{
				widgetManager.setTargetsHidden(config.hideLogout(), Orbs.LOGOUT_X_ICON, Orbs.LOGOUT_X_STONE);
			}

			//flag updates for custom widgets when a cutscene is active
			if (scriptId == Script.TOP_LEVEL_REDRAW)
			{
				manager.pendingChildrenUpdate = manager.isCutSceneActive();
			}
		}

		//buff bar content script, fires frequently (lazy reset)
		if (scriptId == Script.BUFF_BAR_CONTENT_UPDATE)
		{
			if (minimapOverlay.hasUpdatedBounds() && manager.pendingMinimapOverlayChildren)
			{
				log.debug("buff bar is ready for children");

				manager.pendingMinimapOverlayChildren = false;
				clientThread.invokeLater(manager::createMinimapOverlayChildren);
			}
		}

		//don't make changes unless a script updates the minimap widgets,
		// or if the minimap is natively minimized
		if (!Script.MINIMAP_UPDATE_SCRIPTS.contains(scriptId) || manager.isMinimized())
		{
			return;
		}

		//override the in-game settings if enabled
		if (scriptId == Script.ACTIVITY_ORB_UPDATE &&
			manager.activityOrbIsVisibleSetting() &&
			config.hideActivity())
		{
			widgetManager.setHidden(Orbs.ACTIVITY_ORB_CONTAINER, config.hideActivity());
			return;
		}

		//override the in-game settings if enabled
		if (scriptId == Script.STORE_ORB_UPDATE &&
			manager.storeOrbIsVisibleSetting() &&
			config.hideStore())
		{
			widgetManager.setHidden(Orbs.STORE_ORB_CONTAINER, config.hideStore());
			return;
		}

		//identify the current wiki banner (vanilla vs plugin) and update accordingly
		if (scriptId == Script.WIKI_ICON_UPDATE)
		{
			manager.updateWikiBanner(config.hideWiki());
		}

		if (scriptId == Script.WORLD_MAP_UPDATE && manager.hideWorldMap)
		{
			widgetManager.remapTarget(Orbs.WORLD_MAP_CONTAINER, manager.isMinimapHidden() && manager.isResized());
			return;
		}

		manager.init(scriptId);
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		int id = event.getVarbitId();

		if (id == Varbit.MINIMAP_TOGGLE)
		{
			manager.updateCustomChildren(true);
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		int id = event.getGroupId();

		if (id == WidgetManager.getInterfaceId(Orb.UNIVERSE) ||
			id == WidgetManager.getInterfaceId(Classic.ORBS) ||
			id == WidgetManager.getInterfaceId(Modern.ORBS))
		{
			manager.init(Script.FORCE_UPDATE);
			manager.configureMinimapOverlayContainer(true);
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		String group = event.getGroup();
		String key = event.getKey();

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
				if (!manager.isLoggedIn())
				{
					return;
				}

				clientThread.invokeLater(() -> manager.updateCustomChildren(true));
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

			default:
				clientThread.invokeLater(() ->
				{
					manager.updateOrbByConfig(event.getKey());

					//return early if not logged in
					if (!manager.isLoggedIn())
					{
						return;
					}

					//when in compact layouts
					if (manager.isCompactMode())
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
