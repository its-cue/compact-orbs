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

package com.compactorbs.widget.layout.slot;

import com.compactorbs.CompactOrbsConfig;
import com.compactorbs.CompactOrbsConfig.FilteredOrb;
import com.compactorbs.CompactOrbsConstants.ConfigKeys;
import com.compactorbs.widget.TargetWidget;
import com.compactorbs.widget.elements.Compass;
import com.compactorbs.widget.elements.Orbs;
import com.compactorbs.widget.layout.slot.SlotManager.SlotLayoutMode;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Slot
{
	HP_SLOT(
		Map.of(
			SlotLayoutMode.COMPACT, new SlotConfig(
				ConfigKeys.HP_ORB_SLOT,
				CompactOrbsConfig::orbInHPSlot
			),
			SlotLayoutMode.VANILLA, new SlotConfig(
				ConfigKeys.HP_ORB_SLOT_VANILLA,
				CompactOrbsConfig::orbInHpSlotVanilla
			)
		),
		Orbs.HP_ORB_CONTAINER
	),
	PRAYER_SLOT(
		Map.of(
			SlotLayoutMode.COMPACT, new SlotConfig(
				ConfigKeys.PRAYER_ORB_SLOT,
				CompactOrbsConfig::orbInPrayerSlot
			),
			SlotLayoutMode.VANILLA, new SlotConfig(
				ConfigKeys.PRAYER_ORB_SLOT_VANILLA,
				CompactOrbsConfig::orbInPrayerSlotVanilla
			)
		),
		Orbs.PRAYER_ORB_CONTAINER
	),
	RUN_SLOT(
		Map.of(
			SlotLayoutMode.COMPACT, new SlotConfig(
				ConfigKeys.RUN_ORB_SLOT,
				CompactOrbsConfig::orbInRunSlot
			),
			SlotLayoutMode.VANILLA, new SlotConfig(
				ConfigKeys.RUN_ORB_SLOT_VANILLA,
				CompactOrbsConfig::orbInRunSlotVanilla
			)
		),
		Orbs.RUN_ORB_CONTAINER
	),
	SPEC_SLOT(
		Map.of(
			SlotLayoutMode.COMPACT, new SlotConfig(
				ConfigKeys.SPECIAL_ORB_SLOT,
				CompactOrbsConfig::orbInSpecialSlot
			),
			SlotLayoutMode.VANILLA, new SlotConfig(
				ConfigKeys.SPECIAL_ORB_SLOT_VANILLA,
				CompactOrbsConfig::orbInSpecialSlotVanilla
			)
		),
		Orbs.SPEC_ORB_CONTAINER
	),
	XP_SLOT(
		Orbs.XP_DROPS_CONTAINER
	),
	WORLD_MAP_SLOT(
		Orbs.WORLD_MAP_CONTAINER
	),
	STORE_SLOT(
		Orbs.STORE_ORB_CONTAINER
	),
	ACTIVITY_SLOT(
		Orbs.ACTIVITY_ORB_CONTAINER
	),
	WIKI_SLOT(
		Orbs.WIKI_ICON_CONTAINER
	),
	//compass modern/classic share same x/y/width/height, so either works
	COMPASS_SLOT(
		Compass.CLASSIC_COMPASS
	),
	//logout icon/stone share same x/y/width/height, so either works
	LOGOUT_X_SLOT(
		Orbs.LOGOUT_X_ICON
	);

	private final Map<SlotLayoutMode, SlotConfig> slotConfigMap;
	private final TargetWidget original;

	Slot(TargetWidget original)
	{
		this(Map.of(), original);
	}

	public TargetWidget getOrbByConfig(CompactOrbsConfig config, SlotLayoutMode layout)
	{
		SlotConfig entry = slotConfigMap.get(layout);
		if (entry == null)
		{
			return original;
		}

		FilteredOrb filtered = entry.getGetter().apply(config);
		if (filtered == null)
		{
			return original;
		}

		try
		{
			return Orbs.valueOf(filtered.name());
		}
		catch (IllegalArgumentException ex)
		{
			return original;
		}
	}

	private static final Map<String, SlotLayout> CONFIG_LOOKUP;

	//get a slot by its configKey (used in onConfigChanged)
	public static SlotLayout getSlotByConfigKey(String configKey)
	{
		return CONFIG_LOOKUP.get(configKey);
	}

	private static Map<String, SlotLayout> buildConfigLookup()
	{
		Map<String, SlotLayout> lookup = new HashMap<>();
		for (SlotLayoutMode layout : SlotLayoutMode.values())
		{
			for (Slot slot : Slot.values())
			{
				SlotConfig entry = slot.slotConfigMap.get(layout);
				if (entry != null)
				{
					lookup.put(entry.getConfigKey(), new SlotLayout(slot, layout));
				}
			}
		}
		return Map.copyOf(lookup);
	}

	static
	{
		CONFIG_LOOKUP = buildConfigLookup();
	}
}
