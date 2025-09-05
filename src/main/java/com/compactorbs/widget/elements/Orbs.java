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

package com.compactorbs.widget.elements;

import com.compactorbs.util.SetValue;
import com.compactorbs.util.ValueKey;
import static com.compactorbs.util.ValueKey.X;
import static com.compactorbs.util.ValueKey.X_POSITION_MODE;
import static com.compactorbs.util.ValueKey.Y;
import com.compactorbs.widget.TargetWidget;
import com.google.common.collect.ImmutableSet;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ScriptID;
import net.runelite.api.gameval.InterfaceID;

@Getter
@RequiredArgsConstructor
public enum Orbs implements TargetWidget
{

	XP_DROPS_CONTAINER(
		InterfaceID.ORBS, 5,
		ImmutableSet.of(ScriptID.TOPLEVEL_REDRAW),
		Map.of(
			X, new SetValue(0, 112),
			Y, new SetValue(17, 44)
		)
	),
	HP_ORB_CONTAINER(
		InterfaceID.ORBS, 6,
		ImmutableSet.of(ScriptID.TOPLEVEL_REDRAW),
		Map.of(
			X, new SetValue(0, 150),
			Y, new SetValue(37, 42)
		)
	),
	PRAYER_ORB_CONTAINER(
		InterfaceID.ORBS, 17,
		ImmutableSet.of(ScriptID.TOPLEVEL_REDRAW),
		Map.of(
			X, new SetValue(0, 150),
			Y, new SetValue(71, 76)
		)
	),
	RUN_ORB_CONTAINER(
		InterfaceID.ORBS, 25,
		ImmutableSet.of(ScriptID.TOPLEVEL_REDRAW),
		Map.of(
			X, new SetValue(10, 150),
			Y, new SetValue(103, 110)
		)
	),
	SPEC_ORB_CONTAINER(
		InterfaceID.ORBS, 33,
		ImmutableSet.of(ScriptID.TOPLEVEL_REDRAW),
		Map.of(
			X, new SetValue(32, 150),
			Y, new SetValue(128, 144)
		)
	),
	STORE_ORB_CONTAINER(
		InterfaceID.ORBS, 42,
		ImmutableSet.of(2396),
		Map.of(
			X, new SetValue(85, 109),
			Y, new SetValue(143, 105)
		)
	),
	ACTIVITY_ORB_CONTAINER(
		InterfaceID.ORBS, 47,
		ImmutableSet.of(2480),
		Map.of(
			X, new SetValue(55, 109),
			Y, new SetValue(162, 139)
		)
	),
	WORLD_MAP_CONTAINER(
		InterfaceID.ORBS, 48,
		ImmutableSet.of(3304, 1699),
		Map.of(
			X, new SetValue(0, 111),
			Y, new SetValue(115, 72),
			X_POSITION_MODE, new SetValue(2, 0)
		)
	),
	WIKI_CONTAINER(
		InterfaceID.ORBS, 49,
		ImmutableSet.of(3304, 3305),
		Map.of(
			X, new SetValue(0, 148),
			Y, new SetValue(135, 172),
			X_POSITION_MODE, new SetValue(2, 0)
		)
	),
	LOGOUT_X_ICON_CONTAINER(
		InterfaceID.TOPLEVEL_PRE_EOC, 35,
		ImmutableSet.of(ScriptID.TOPLEVEL_REDRAW),
		Map.of(
			X, new SetValue(2, 177),
			Y, new SetValue(2, 20),
			X_POSITION_MODE, new SetValue(2, 0)
		)
	),
	LOGOUT_X_STONE_CONTAINER(
		InterfaceID.TOPLEVEL_PRE_EOC, 34,
		ImmutableSet.of(ScriptID.TOPLEVEL_REDRAW),
		Map.of(
			X, new SetValue(2, 177),
			Y, new SetValue(2, 20),
			X_POSITION_MODE, new SetValue(2, 0)
		)
	);

	private final int interfaceId, childId;

	private final Set<Integer> scriptIds;

	private final Map<ValueKey, SetValue> positions;

	// exclude: store, world_map, activity advisor, logout_x_icon, logout_x_stone, wiki
	public static final Set<Orbs> FIXED = EnumSet.of(
		HP_ORB_CONTAINER,
		PRAYER_ORB_CONTAINER,
		RUN_ORB_CONTAINER,
		SPEC_ORB_CONTAINER,
		XP_DROPS_CONTAINER
	);

	public static final Set<Orbs> ALL = EnumSet.allOf(Orbs.class);

}
