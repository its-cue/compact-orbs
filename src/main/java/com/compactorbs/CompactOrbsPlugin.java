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

import static com.compactorbs.CompactOrbsConstants.GROUP_NAME;
import com.compactorbs.CompactOrbsConstants.ConfigKeys;
import com.compactorbs.CompactOrbsConstants.Script;
import com.compactorbs.CompactOrbsConstants.Varbit;
import com.compactorbs.CompactOrbsConstants.VarbitValue;
import com.compactorbs.CompactOrbsConstants.Widget.Classic;
import com.compactorbs.CompactOrbsConstants.Widget.Modern;
import com.compactorbs.CompactOrbsConstants.Widget.Orb;
import com.compactorbs.widget.WidgetManager;
import com.google.inject.Provides;
import javax.inject.Inject;
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

@PluginDescriptor(
	name = "Compact Orbs",
	description = "Collapse the minimap orbs into a compact view.",
	tags = {"compact", "orbs", "hide", "minimap", "resizable", "classic", "modern"},
	conflicts = {"Fixed Resizable Hybrid"}
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

	@Override
	protected void startUp() throws Exception
	{
		keyManager.registerKeyListener(hotkeyListener);

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

		if (!Script.MINIMAP_UPDATE_SCRIPTS.contains(scriptId) || manager.isMinimized())
		{
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
				case ConfigKeys.TOGGLE_BUTTON:
				case ConfigKeys.HOTKEY_TOGGLE:
					clientThread.invokeLater(manager::updateCustomChildren);
					break;

				default:
					break;
			}
		}
	}

	private final HotkeyListener hotkeyListener = new HotkeyListener(() -> config.toggleButtonHotkey())
	{
		@Override
		public void hotkeyPressed()
		{
			configManager.setConfiguration(GROUP_NAME, ConfigKeys.TOGGLE_BUTTON, !Boolean.TRUE.equals(config.hideToggle()));
		}
	};

	@Provides
	CompactOrbsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CompactOrbsConfig.class);
	}
}
