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
import static com.compactorbs.CompactOrbsConstants.ConfigGroup.GROUP_NAME;
import com.compactorbs.CompactOrbsConstants.ConfigKeys;
import com.compactorbs.widget.TargetWidget;
import com.compactorbs.widget.elements.Orbs;
import com.compactorbs.widget.layout.slot.SlotManager.SlotLayoutMode;
import com.google.inject.Inject;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import net.runelite.client.config.ConfigManager;

@Singleton
public class SlotRegistry
{
	@Inject
	private ConfigManager configManager;

	private final Map<SlotLayoutMode, EnumMap<Slot, SlotConfig>> configs =
		Map.of(
			SlotLayoutMode.COMPACT, compactConfigs(),
			SlotLayoutMode.VANILLA, vanillaConfigs()
		);

	private final Set<String> slotConfigKeys =
		configs.values()
			.stream()
			.flatMap(map -> map.values().stream())
			.map(SlotConfig::getConfigKey)
			.collect(Collectors.toUnmodifiableSet());

	private static EnumMap<Slot, SlotConfig> compactConfigs()
	{
		EnumMap<Slot, SlotConfig> configs = new EnumMap<>(Slot.class);
		configs.put(Slot.HP_SLOT,
			new SlotConfig(
				ConfigKeys.HP_ORB_SLOT,
				CompactOrbsConfig::orbInHPSlot)
		);
		configs.put(Slot.PRAYER_SLOT,
			new SlotConfig(
				ConfigKeys.PRAYER_ORB_SLOT,
				CompactOrbsConfig::orbInPrayerSlot)
		);
		configs.put(Slot.RUN_SLOT,
			new SlotConfig(
				ConfigKeys.RUN_ORB_SLOT,
				CompactOrbsConfig::orbInRunSlot)
		);
		configs.put(Slot.SPEC_SLOT,
			new SlotConfig(
				ConfigKeys.SPECIAL_ORB_SLOT,
				CompactOrbsConfig::orbInSpecialSlot)
		);
		return configs;
	}

	private static EnumMap<Slot, SlotConfig> vanillaConfigs()
	{
		EnumMap<Slot, SlotConfig> configs = new EnumMap<>(Slot.class);
		configs.put(
			Slot.HP_SLOT,
			new SlotConfig(
				ConfigKeys.HP_ORB_SLOT_VANILLA,
				CompactOrbsConfig::orbInHpSlotVanilla)
		);
		configs.put(Slot.PRAYER_SLOT,
			new SlotConfig(
				ConfigKeys.PRAYER_ORB_SLOT_VANILLA,
				CompactOrbsConfig::orbInPrayerSlotVanilla)
		);
		configs.put(Slot.RUN_SLOT,
			new SlotConfig(
				ConfigKeys.RUN_ORB_SLOT_VANILLA,
				CompactOrbsConfig::orbInRunSlotVanilla)
		);
		configs.put(Slot.SPEC_SLOT,
			new SlotConfig(
				ConfigKeys.SPECIAL_ORB_SLOT_VANILLA,
				CompactOrbsConfig::orbInSpecialSlotVanilla)
		);
		return configs;
	}

	public TargetWidget resolve(Slot slot, SlotLayoutMode layout, CompactOrbsConfig config)
	{
		SlotConfig slotConfig = getConfig(layout, slot);
		if (slotConfig == null)
		{
			return slot.getDefaultTarget();
		}

		Orbs orb = slotConfig.getGetter().apply(config);
		return orb != null ? orb : slot.getDefaultTarget();
	}

	public SlotConfig getConfig(SlotLayoutMode layout, Slot slot)
	{
		return configs.get(layout).get(slot);
	}

	public void save(SlotLayoutMode layout, SlotLayout state)
	{
		for (Slot slot : Slot.values())
		{
			save(layout, slot, state.get(slot));
		}
	}

	private void save(SlotLayoutMode layout, Slot slot, TargetWidget target)
	{
		SlotConfig slotConfig = getConfig(layout, slot);

		if (slotConfig == null)
		{
			return;
		}

		configManager.setConfiguration(GROUP_NAME, slotConfig.getConfigKey(), target);
	}

	public boolean isSwapConfig(String key)
	{
		return slotConfigKeys.contains(key);
	}

}
