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

package com.compactorbs.widget.slot;

import com.compactorbs.CompactOrbsConfig;
import com.compactorbs.CompactOrbsConfig.FilteredOrb;
import com.compactorbs.CompactOrbsConstants.ConfigKeys;
import com.compactorbs.widget.TargetWidget;
import com.compactorbs.widget.elements.Orbs;
import com.compactorbs.widget.elements.Compass;
import java.util.List;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum Slot
{
	HP_SLOT(
		ConfigKeys.HP_ORB_SLOT,
		CompactOrbsConfig::orbInHPSlot,
		Orbs.HP_ORB_CONTAINER
	),
	PRAYER_SLOT(
		ConfigKeys.PRAYER_ORB_SLOT,
		CompactOrbsConfig::orbInPrayerSlot,
		Orbs.PRAYER_ORB_CONTAINER
	),
	RUN_SLOT(
		ConfigKeys.RUN_ORB_SLOT,
		CompactOrbsConfig::orbInRunSlot,
		Orbs.RUN_ORB_CONTAINER
	),
	SPEC_SLOT(
		ConfigKeys.SPECIAL_ORB_SLOT,
		CompactOrbsConfig::orbInSpecialSlot,
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
	//only really a placeholder, for height, x and y (shares same with Compass.MODERN_COMPASS)
	COMPASS_SLOT(
		Compass.CLASSIC_COMPASS
	),
	//only really a placeholder, for height, x and y (shares same with Orbs.LOGOUT_X_STONE)
	LOGOUT_X_SLOT(
		Orbs.LOGOUT_X_ICON
	);

	//key for config, relative orb to slot
	private final String configKey;

	private final Function<CompactOrbsConfig, FilteredOrb> getter;

	private final TargetWidget original;

	Slot(TargetWidget original)
	{
		this(null, c -> null, original);
	}

	public TargetWidget configuredOrbOf(CompactOrbsConfig config)
	{
		FilteredOrb filtered = getter.apply(config);
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

	public static Slot slotOf(TargetWidget target)
	{
		for (Slot slot : values())
		{
			if (slot.getOriginal().equals(target))
			{
				return slot;
			}
		}

		return null;//may break swapping <-
	}

	public static List<Slot> getColumnOf(Slot slot)
	{
		if (VERTICAL_LEFT_COLUMN.contains(slot))
		{
			return VERTICAL_LEFT_COLUMN;
		}
		if (VERTICAL_RIGHT_COLUMN.contains(slot))
		{
			return VERTICAL_RIGHT_COLUMN;
		}

		log.debug("Slot {} is not in any column", slot);

		return null;
	}

	//vertical column left
	public static final List<Slot> VERTICAL_LEFT_COLUMN = List.of(
		XP_SLOT,
		WORLD_MAP_SLOT,
		STORE_SLOT,
		ACTIVITY_SLOT
	);

	//vertical column right
	public static final List<Slot> VERTICAL_RIGHT_COLUMN = List.of(
		HP_SLOT,
		PRAYER_SLOT,
		RUN_SLOT,
		SPEC_SLOT,
		WIKI_SLOT
	);

	//TODO - may not really be necessary, with swapping, unlike with vertical

	//left to right
	public static final List<Slot> HORIZONTAL_TOP_ROW = List.of(
		STORE_SLOT,
		HP_SLOT,
		RUN_SLOT
	);

	//left to right
	public static final List<Slot> HORIZONTAL_BOTTOM_ROW = List.of(
		ACTIVITY_SLOT,
		PRAYER_SLOT,
		SPEC_SLOT
	);

}
