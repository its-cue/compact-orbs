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

import com.compactorbs.CompactOrbsConstants.Layout.Original;
import com.compactorbs.CompactOrbsConstants.Layout.Modified;
import com.compactorbs.CompactOrbsConstants.Script;
import com.compactorbs.CompactOrbsConstants.Widget.Modern;
import com.compactorbs.CompactOrbsConstants.Widget.Orb;
import com.compactorbs.util.SetValue;
import com.compactorbs.util.ValueKey;
import static com.compactorbs.util.ValueKey.X;
import static com.compactorbs.util.ValueKey.X_POSITION_MODE;
import static com.compactorbs.util.ValueKey.Y;
import com.compactorbs.widget.TargetWidget;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.widgets.WidgetPositionMode;

@Getter
@RequiredArgsConstructor
public enum Orbs implements TargetWidget
{

	XP_DROPS_CONTAINER(
		Orb.XP_DROPS,
		Script.FORCE_UPDATE,
		Map.of(
			X, new SetValue(Original.XP_DROPS_X, Modified.XP_DROPS_X),
			Y, new SetValue(Original.XP_DROPS_Y, Modified.XP_DROPS_Y),
			X_POSITION_MODE, new SetValue(WidgetPositionMode.ABSOLUTE_LEFT, WidgetPositionMode.ABSOLUTE_RIGHT)
		)
	),
	HP_ORB_CONTAINER(
		Orb.HP_ORB,
		Script.FORCE_UPDATE,
		Map.of(
			X, new SetValue(Original.HP_ORB_X, Modified.HP_ORB_X),
			Y, new SetValue(Original.HP_ORB_Y, Modified.HP_ORB_Y),
			X_POSITION_MODE, new SetValue(WidgetPositionMode.ABSOLUTE_LEFT, WidgetPositionMode.ABSOLUTE_RIGHT)
		)
	),
	PRAYER_ORB_CONTAINER(
		Orb.PRAY_ORB,
		Script.FORCE_UPDATE,
		Map.of(
			X, new SetValue(Original.PRAYER_ORB_X, Modified.PRAYER_ORB_X),
			Y, new SetValue(Original.PRAYER_ORB_Y, Modified.PRAYER_ORB_Y),
			X_POSITION_MODE, new SetValue(WidgetPositionMode.ABSOLUTE_LEFT, WidgetPositionMode.ABSOLUTE_RIGHT)
		)
	),
	RUN_ORB_CONTAINER(
		Orb.RUN_ORB,
		Script.FORCE_UPDATE,
		Map.of(
			X, new SetValue(Original.RUN_ORB_X, Modified.RUN_ORB_X),
			Y, new SetValue(Original.RUN_ORB_Y, Modified.RUN_ORB_Y),
			X_POSITION_MODE, new SetValue(WidgetPositionMode.ABSOLUTE_LEFT, WidgetPositionMode.ABSOLUTE_RIGHT)
		)
	),
	SPEC_ORB_CONTAINER(
		Orb.SPEC_ORB,
		Script.FORCE_UPDATE,
		Map.of(
			X, new SetValue(Original.SPEC_ORB_X, Modified.SPEC_ORB_X),
			Y, new SetValue(Original.SPEC_ORB_Y, Modified.SPEC_ORB_Y),
			X_POSITION_MODE, new SetValue(WidgetPositionMode.ABSOLUTE_LEFT, WidgetPositionMode.ABSOLUTE_RIGHT)
		)
	),
	STORE_ORB_CONTAINER(
		Orb.STORE_ORB,
		Script.STORE_ORB_UPDATE,
		Map.of(
			X, new SetValue(Original.STORE_ORB_X, Modified.STORE_ORB_X),
			Y, new SetValue(Original.STORE_ORB_Y, Modified.STORE_ORB_Y),
			X_POSITION_MODE, new SetValue(WidgetPositionMode.ABSOLUTE_LEFT, WidgetPositionMode.ABSOLUTE_RIGHT)
		)
	),
	ACTIVITY_ORB_CONTAINER(
		Orb.ACTIVITY_ORB,
		Script.ACTIVITY_ORB_UPDATE,
		Map.of(
			X, new SetValue(Original.ACTIVITY_ORB_X, Modified.ACTIVITY_ORB_X),
			Y, new SetValue(Original.ACTIVITY_ORB_Y, Modified.ACTIVITY_ORB_Y),
			X_POSITION_MODE, new SetValue(WidgetPositionMode.ABSOLUTE_LEFT, WidgetPositionMode.ABSOLUTE_RIGHT)
		)
	),
	WORLD_MAP_CONTAINER(
		Orb.WORLD_MAP,
		Script.WORLD_MAP_UPDATE,
		Map.of(
			X, new SetValue(Original.WORLD_MAP_X, Modified.WORLD_MAP_X),
			Y, new SetValue(Original.WORLD_MAP_Y, Modified.WORLD_MAP_Y)
		)
	),
	WIKI_ICON_CONTAINER(
		Orb.WIKI_ICON,
		Script.WIKI_ICON_UPDATE,
		Map.of(
			X, new SetValue(Original.WIKI_ICON_X, Modified.WIKI_ICON_X),
			Y, new SetValue(Original.WIKI_ICON_Y, Modified.WIKI_ICON_Y),
			X_POSITION_MODE, new SetValue(WidgetPositionMode.ABSOLUTE_RIGHT, WidgetPositionMode.ABSOLUTE_RIGHT)
		)
	),
	LOGOUT_X_ICON(
		Modern.LOGOUT_X_ICON,
		Script.FORCE_UPDATE,
		Map.of(
			X, new SetValue(Original.LOGOUT_X, Modified.LOGOUT_X),
			Y, new SetValue(Original.LOGOUT_Y, Modified.LOGOUT_Y)
		)
	),
	LOGOUT_X_STONE(
		Modern.LOGOUT_X_STONE,
		Script.FORCE_UPDATE,
		Map.of(
			X, new SetValue(Original.LOGOUT_X, Modified.LOGOUT_X),
			Y, new SetValue(Original.LOGOUT_Y, Modified.LOGOUT_Y)
		)
	),
	//temp game mode
	GRID_MASTER_ORB_CONTAINER(
		Orb.UNIVERSE, 0,
		Script.GRID_MASTER_ORB_UPDATE,
		Map.of(
			X, new SetValue(Original.GRID_MASTER_ORB_X, Modified.GRID_MASTER_ORB_X),
			Y, new SetValue(Original.GRID_MASTER_ORB_Y, Modified.GRID_MASTER_ORB_Y)
		)
	);

	private final int componentId, arrayId, scriptId;

	private final Map<ValueKey, SetValue> positions;

	Orbs(int componentId, int scriptId, Map<ValueKey, SetValue> positions)
	{
		this.componentId = componentId;
		this.arrayId = -1;
		this.scriptId = scriptId;
		this.positions = positions;
	}

	//only includes widgets that require updating
	public static final Set<Orbs> FIXED = EnumSet.of(
		HP_ORB_CONTAINER,
		PRAYER_ORB_CONTAINER,
		RUN_ORB_CONTAINER,
		SPEC_ORB_CONTAINER,
		XP_DROPS_CONTAINER
	);

	public static final Set<Orbs> ALL = EnumSet.allOf(Orbs.class);

}
