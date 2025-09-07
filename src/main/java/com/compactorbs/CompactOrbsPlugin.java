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

import static com.compactorbs.CompactOrbsManager.FORCE_REMAP;
import static com.compactorbs.CompactOrbsManager.ORBS_UPDATE_ACTIVITY_ADVISOR;
import static com.compactorbs.CompactOrbsManager.ORBS_UPDATE_STORE;
import static com.compactorbs.CompactOrbsManager.ORBS_UPDATE_WORLD_MAP;
import static com.compactorbs.CompactOrbsManager.WIKI_ICON_UPDATE;
import com.google.inject.Provides;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Compact Orbs"
)
public class CompactOrbsPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private CompactOrbsManager manager;

	private static final Set<Integer> MINIMAP_UPDATE_SCRIPTS =
		Set.of(
			ORBS_UPDATE_WORLD_MAP,
			ORBS_UPDATE_STORE,
			ORBS_UPDATE_ACTIVITY_ADVISOR,
			WIKI_ICON_UPDATE
		);

	@Override
	protected void startUp() throws Exception
	{
		clientThread.invokeLater(() -> manager.build(FORCE_REMAP));
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientThread.invoke(manager::reset);
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() == InterfaceID.ORBS ||
			event.getGroupId() == InterfaceID.TOPLEVEL_OSRS_STRETCH ||
			event.getGroupId() == InterfaceID.TOPLEVEL_PRE_EOC)
		{
			manager.init(FORCE_REMAP);
		}
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		int scriptId = event.getScriptId();

		if (!MINIMAP_UPDATE_SCRIPTS.contains(scriptId))
		{
			return;
		}

		manager.init(scriptId);

	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals(CompactOrbsConfig.GROUP_NAME))
		{
			if (event.getKey().equals("hideToggle"))
			{
				clientThread.invokeLater(manager::updateToggleButtons);
			}
		}
	}

	@Provides
	CompactOrbsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CompactOrbsConfig.class);
	}
}
