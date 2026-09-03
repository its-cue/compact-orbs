/*
 * Copyright (c) 2025, cue <https://github.com/its-cue>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
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

package com.compactorbs.widget.layout;

import com.compactorbs.CompactOrbsConfig;
import com.compactorbs.CompactOrbsConstants.ConfigKeys;
import com.compactorbs.widget.TargetWidget;
import com.compactorbs.widget.elements.Button;
import com.compactorbs.widget.elements.Minimap;
import com.compactorbs.widget.elements.Orbs;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Singleton
public final class HideOrbRegistry
{
	@Inject
	private CompactOrbsConfig config;

	private final Map<String, HideOrbConfig> byConfig = new HashMap<>();
	private final Map<TargetWidget, HideOrbConfig> byTarget = new HashMap<>();

	public void registerAll()
	{
		register(
			ConfigKeys.HIDE_HP,
			config::hideHp,
			"HP orb",
			Orbs.HP_ORB_CONTAINER
		);

		register(
			ConfigKeys.HIDE_PRAYER,
			config::hidePray,
			"Prayer orb",
			Orbs.PRAYER_ORB_CONTAINER
		);

		register(
			ConfigKeys.HIDE_RUN,
			config::hideRun,
			"Run orb",
			Orbs.RUN_ORB_CONTAINER
		);

		register(
			ConfigKeys.HIDE_SPEC,
			config::hideSpec,
			"Special orb",
			Orbs.SPEC_ORB_CONTAINER
		);

		register(
			ConfigKeys.HIDE_STORE,
			config::hideStore,
			"Store",
			Orbs.STORE_ORB_CONTAINER
		);

		register(
			ConfigKeys.HIDE_ACTIVITY,
			config::hideActivity,
			"Activity advisor",
			Orbs.ACTIVITY_ORB_CONTAINER
		);

		register(
			ConfigKeys.HIDE_WORLD,
			config::hideWorld,
			"World map",
			Orbs.WORLD_MAP_CONTAINER
		);

		register(
			ConfigKeys.HIDE_WIKI,
			config::hideWiki,
			"Wiki banner",
			Orbs.WIKI_VANILLA_ICON,
			Orbs.WIKI_VANILLA_CONTAINER,
			Orbs.WIKI_ICON_CONTAINER
		);

		register(
			ConfigKeys.HIDE_XP,
			config::hideXp,
			"XP",
			Orbs.XP_DROPS_CONTAINER
		);

		register(
			ConfigKeys.HIDE_LOGOUT_X,
			config::hideLogout,
			"Logout",
			Orbs.LOGOUT_X_ICON,
			Orbs.LOGOUT_X_STONE
		);

		register(
			ConfigKeys.MINIMAP_TOGGLE_BUTTON,
			config::hideMinimapToggle,
			"Button",
			Button.MINIMAP_BUTTON_CLASSIC,
			Button.MINIMAP_BUTTON_MODERN,
			Button.MINIMAP_BUTTON_FIXED
		);

		register(
			ConfigKeys.COMPASS,
			config::hideCompass,
			"Compass",
			Minimap.MODERN_MAP_MINIMAP,
			Minimap.CLASSIC_MAP_MINIMAP
		);
	}

	private void register(
		String key,
		Supplier<Boolean> isHidden,
		String name,
		TargetWidget... targets)
	{
		HideOrbConfig hideOrbConfig = new HideOrbConfig(
			key,
			isHidden,
			name,
			targets
		);

		byConfig.put(key, hideOrbConfig);

		for (TargetWidget target : targets)
		{
			byTarget.put(target, hideOrbConfig);
		}
	}

	public boolean isHideConfig(String key)
	{
		return byConfig.containsKey(key);
	}

	public HideOrbConfig getByConfig(String key)
	{
		return byConfig.get(key);
	}

	public HideOrbConfig getByTarget(TargetWidget target)
	{
		return byTarget.get(target);
	}

	public Collection<HideOrbConfig> values()
	{
		return byConfig.values();
	}

	public Collection<String> configKeys()
	{
		return byConfig.keySet();
	}

	public void clear()
	{
		byConfig.clear();
		byTarget.clear();
	}
}
