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
import com.compactorbs.CompactOrbsConstants.VarbitValue;
import com.compactorbs.CompactOrbsConstants.Widgets.Classic;
import com.compactorbs.CompactOrbsConstants.Widgets.Modern;
import com.compactorbs.CompactOrbsConstants.Widgets.Orb;
import com.compactorbs.widget.WidgetManager;
import com.compactorbs.widget.elements.Orbs;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
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
import net.runelite.client.util.HotkeyListener;

@Slf4j
@PluginDescriptor(
	name = "Compact Orbs",
	description = "Collapse the minimap orbs into a compact view.",
	tags = {"compact", "orbs", "hide", "minimap", "resizable", "classic", "modern", "world", "map", "wiki"},
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
	private WidgetManager widgetManager;

	@Override
	protected void startUp() throws Exception
	{
		keyManager.registerKeyListener(hotkeyListener);

		registerOrbToggleEntries();

		if (client.getGameState() == GameState.LOGGED_IN)
		{
			clientThread.invoke(() -> manager.minimapMinimized = (client.getVarbitValue(Varbit.MINIMAP_TOGGLE) == VarbitValue.MINIMAP_MINIMIZED));

			clientThread.invokeLater(() -> manager.init(Script.FORCE_UPDATE));
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		keyManager.unregisterKeyListener(hotkeyListener);

		clientThread.invoke(manager::reset);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			manager.createCustomChildren();
		}
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		int scriptId = event.getScriptId();

		//make sure the logout X stays hidden
		if (scriptId == Script.TOP_LEVEL_REDRAW || scriptId == Script.TOP_LEVEL_SIDE_CUSTOMIZE)
		{
			if (config.hideLogout() && !manager.isMinimized())
			{
				widgetManager.setTargetsHidden(config.hideLogout(), Orbs.LOGOUT_X_ICON, Orbs.LOGOUT_X_STONE);
			}
		}

		if (!Script.MINIMAP_UPDATE_SCRIPTS.contains(scriptId) || manager.isMinimized())
		{
			return;
		}

		//override the in-game settings
		if (scriptId == Script.ACTIVITY_ORB_UPDATE &&
			client.getVarbitValue(Varbit.ACTIVITY_ORB_TOGGLE) == VarbitValue.ACTIVITY_ORB_VISIBLE &&
			config.hideActivity())
		{
			widgetManager.setHidden(Orbs.ACTIVITY_ORB_CONTAINER, config.hideActivity());
			return;
		}

		//override the in-game settings
		if (scriptId == Script.STORE_ORB_UPDATE &&
			client.getVarbitValue(Varbit.STORE_ORB_TOGGLE) == VarbitValue.STORE_ORB_VISIBLE &&
			config.hideStore())
		{
			widgetManager.setHidden(Orbs.STORE_ORB_CONTAINER, config.hideStore());
			return;
		}

		if (scriptId == Script.WIKI_ICON_UPDATE || scriptId == Script.WIKI_CONTAINER_UPDATE)
		{
			manager.updateWikiBanner(config.hideWiki());
		}

		manager.init(scriptId);
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		int id = event.getVarbitId();

		if (id == Varbit.MINIMAP_TOGGLE)
		{
			//hide custom buttons when native minimap hiding is active
			manager.minimapMinimized = (event.getValue() == VarbitValue.MINIMAP_MINIMIZED);
			manager.updateCustomChildren();
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
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		String group = event.getGroup();
		String key = event.getKey();

		if (group.equals(GROUP_NAME))
		{
			switch (key)
			{
				case ConfigKeys.MINIMAP_TOGGLE_BUTTON:
				case ConfigKeys.COMPASS_TOGGLE_BUTTON:
					clientThread.invokeLater(manager::updateCustomChildren);
					break;

				case ConfigKeys.ORB_LAYOUT:
				case ConfigKeys.HORIZONTAL:
				case ConfigKeys.VERTICAL:
					clientThread.invokeLater(() -> manager.init(Script.FORCE_UPDATE));
					break;

				case ConfigKeys.HIDE_WIKI:
					clientThread.invokeLater(() -> manager.updateWikiBanner(config.hideWiki()));
					break;

				case ConfigKeys.HIDE_WORLD:
					//returns early if !client.isResized, but still gets worldMapOffset for both
					clientThread.invokeLater(() -> manager.updateWorldMap(true));

					if (client.isResized())
					{
						clientThread.invokeLater(() -> widgetManager.remapTarget(Orbs.WORLD_MAP_CONTAINER, manager.isMinimapHidden()));
					}
					break;

				default:
					clientThread.invokeLater(() -> manager.updateOrbByConfig(event.getKey()));
					break;
			}
		}
	}

	//register all orb toggles on startup
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
		//wiki banner and world map are excluded for special handling; updateWikiBanner, updateWorldMap
	}

	private final HotkeyListener hotkeyListener = new HotkeyListener(() -> config.toggleButtonHotkey())
	{
		@Override
		public void hotkeyPressed()
		{
			boolean hidden = !(config.hideMinimapToggle() || config.hideCompassToggle());

			configManager.setConfiguration(GROUP_NAME, ConfigKeys.MINIMAP_TOGGLE_BUTTON, hidden);
			configManager.setConfiguration(GROUP_NAME, ConfigKeys.COMPASS_TOGGLE_BUTTON, hidden);

			clientThread.invokeLater(manager::updateCustomChildren);
		}
	};

	@Provides
	CompactOrbsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CompactOrbsConfig.class);
	}
}
