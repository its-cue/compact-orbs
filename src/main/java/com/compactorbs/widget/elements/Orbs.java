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

import com.compactorbs.CompactOrbsConstants.Layout.Horizontal;
import com.compactorbs.CompactOrbsConstants.Layout.Original;
import com.compactorbs.CompactOrbsConstants.Layout.Vertical;
import com.compactorbs.CompactOrbsConstants.Script;
import com.compactorbs.CompactOrbsConstants.Widgets.Modern;
import com.compactorbs.CompactOrbsConstants.Widgets.Orb;
import com.compactorbs.util.SetValue;
import com.compactorbs.util.ValueKey;
import static com.compactorbs.util.ValueKey.X;
import static com.compactorbs.util.ValueKey.X_POSITION_MODE;
import static com.compactorbs.util.ValueKey.Y;
import static com.compactorbs.util.ValueKey.Y_POSITION_MODE;
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
		Map.of(
			X, new SetValue(
				Original.XP_DROPS_X,
				Vertical.XP_DROPS_X,
				Horizontal.XP_DROPS_X
			),
			Y, new SetValue(
				Original.XP_DROPS_Y,
				Vertical.XP_DROPS_Y,
				Horizontal.XP_DROPS_Y
			),
			X_POSITION_MODE, new SetValue(
				WidgetPositionMode.ABSOLUTE_LEFT,
				WidgetPositionMode.ABSOLUTE_RIGHT,
				WidgetPositionMode.ABSOLUTE_LEFT
			)
		)
	),
	HP_ORB_CONTAINER(
		Orb.HP_ORB,
		Map.of(
			X, new SetValue(
				Original.HP_ORB_X,
				Vertical.HP_ORB_X,
				Horizontal.HP_ORB_X
			),
			Y, new SetValue(
				Original.HP_ORB_Y,
				Vertical.HP_ORB_Y,
				Horizontal.HP_ORB_Y
			),
			X_POSITION_MODE, new SetValue(
				WidgetPositionMode.ABSOLUTE_LEFT,
				WidgetPositionMode.ABSOLUTE_RIGHT,
				WidgetPositionMode.ABSOLUTE_LEFT
			)
		)
	),
	PRAYER_ORB_CONTAINER(
		Orb.PRAY_ORB,
		Map.of(
			X, new SetValue(
				Original.PRAYER_ORB_X,
				Vertical.PRAYER_ORB_X,
				Horizontal.PRAYER_ORB_X
			),
			Y, new SetValue(
				Original.PRAYER_ORB_Y,
				Vertical.PRAYER_ORB_Y,
				Horizontal.PRAYER_ORB_Y
			),
			X_POSITION_MODE, new SetValue(
				WidgetPositionMode.ABSOLUTE_LEFT,
				WidgetPositionMode.ABSOLUTE_RIGHT,
				WidgetPositionMode.ABSOLUTE_LEFT
			)
		)
	),
	RUN_ORB_CONTAINER(
		Orb.RUN_ORB,
		Map.of(
			X, new SetValue(
				Original.RUN_ORB_X,
				Vertical.RUN_ORB_X,
				Horizontal.RUN_ORB_X),
			Y, new SetValue(
				Original.RUN_ORB_Y,
				Vertical.RUN_ORB_Y,
				Horizontal.RUN_ORB_Y
			),
			X_POSITION_MODE, new SetValue(
				WidgetPositionMode.ABSOLUTE_LEFT,
				WidgetPositionMode.ABSOLUTE_RIGHT,
				WidgetPositionMode.ABSOLUTE_LEFT
			)
		)
	),
	SPEC_ORB_CONTAINER(
		Orb.SPEC_ORB,
		Map.of(
			X, new SetValue(
				Original.SPEC_ORB_X,
				Vertical.SPEC_ORB_X,
				Horizontal.SPEC_ORB_X
			),
			Y, new SetValue(
				Original.SPEC_ORB_Y,
				Vertical.SPEC_ORB_Y,
				Horizontal.SPEC_ORB_Y
			),
			X_POSITION_MODE, new SetValue(
				WidgetPositionMode.ABSOLUTE_LEFT,
				WidgetPositionMode.ABSOLUTE_RIGHT,
				WidgetPositionMode.ABSOLUTE_LEFT
			)
		)
	),
	STORE_ORB_CONTAINER(
		Orb.STORE_ORB,
		Script.STORE_ORB_UPDATE,
		Map.of(
			X, new SetValue(
				Original.STORE_ORB_X,
				Vertical.STORE_ORB_X,
				Horizontal.STORE_ORB_X
			),
			Y, new SetValue(
				Original.STORE_ORB_Y,
				Vertical.STORE_ORB_Y,
				Horizontal.STORE_ORB_Y
			),
			X_POSITION_MODE, new SetValue(
				WidgetPositionMode.ABSOLUTE_LEFT,
				WidgetPositionMode.ABSOLUTE_RIGHT,
				WidgetPositionMode.ABSOLUTE_LEFT
			)
		)
	),
	ACTIVITY_ORB_CONTAINER(
		Orb.ACTIVITY_ORB,
		Script.ACTIVITY_ORB_UPDATE,
		Map.of(
			X, new SetValue(
				Original.ACTIVITY_ORB_X,
				Vertical.ACTIVITY_ORB_X,
				Horizontal.ACTIVITY_ORB_X
			),
			Y, new SetValue(
				Original.ACTIVITY_ORB_Y,
				Vertical.ACTIVITY_ORB_Y,
				Horizontal.ACTIVITY_ORB_Y
			),
			X_POSITION_MODE, new SetValue(
				WidgetPositionMode.ABSOLUTE_LEFT,
				WidgetPositionMode.ABSOLUTE_RIGHT,
				WidgetPositionMode.ABSOLUTE_LEFT
			)
		)
	),
	WORLD_MAP_CONTAINER(
		Orb.WORLD_MAP,
		Script.WORLD_MAP_UPDATE,
		Map.of(
			X, new SetValue(
				Original.WORLD_MAP_X,
				Vertical.WORLD_MAP_X,
				Horizontal.WORLD_MAP_X
			),
			Y, new SetValue(
				Original.WORLD_MAP_Y,
				Vertical.WORLD_MAP_Y,
				Horizontal.WORLD_MAP_Y
			)
		)
	),
	//wiki plugin banner
	WIKI_ICON_CONTAINER(
		Orb.WIKI_ICON,
		Script.WIKI_ICON_UPDATE,
		Map.of(
			X, new SetValue(
				Original.WIKI_ICON_X,
				Vertical.WIKI_ICON_X,
				Horizontal.WIKI_ICON_X
			),
			Y, new SetValue(
				Original.WIKI_ICON_Y,
				Vertical.WIKI_ICON_Y,
				Horizontal.WIKI_ICON_Y
			),
			X_POSITION_MODE, new SetValue(
				WidgetPositionMode.ABSOLUTE_RIGHT,
				WidgetPositionMode.ABSOLUTE_RIGHT,
				WidgetPositionMode.ABSOLUTE_LEFT
			),
			Y_POSITION_MODE, new SetValue(
				WidgetPositionMode.ABSOLUTE_TOP,
				WidgetPositionMode.ABSOLUTE_TOP
			)
		)
	),
	//vanilla wiki banner
	WIKI_VANILLA(
		Orb.WIKI_ICON_VANILLA,
		Script.WIKI_ICON_UPDATE,
		Map.of(
			X, new SetValue(
				Original.WIKI_VANILLA_X,
				Vertical.WIKI_VANILLA_X,
				Horizontal.WIKI_VANILLA_X
			),
			Y, new SetValue(
				Original.WIKI_VANILLA_Y,
				Vertical.WIKI_VANILLA_Y,
				Horizontal.WIKI_VANILLA_Y
			),
			X_POSITION_MODE, new SetValue(
				WidgetPositionMode.ABSOLUTE_CENTER,
				WidgetPositionMode.ABSOLUTE_RIGHT,
				WidgetPositionMode.ABSOLUTE_LEFT
			),
			Y_POSITION_MODE, new SetValue(
				WidgetPositionMode.ABSOLUTE_CENTER,
				WidgetPositionMode.ABSOLUTE_RIGHT
			)
		)
	),
	LOGOUT_X_ICON(
		Modern.LOGOUT_X_ICON,
		Map.of(
			X, new SetValue(
				Original.LOGOUT_X,
				Vertical.LOGOUT_X,
				Horizontal.LOGOUT_X
			),
			Y, new SetValue(
				Original.LOGOUT_Y,
				Vertical.LOGOUT_Y,
				Horizontal.LOGOUT_Y
			)
		)
	),
	LOGOUT_X_STONE(
		Modern.LOGOUT_X_STONE,
		Map.of(
			X, new SetValue(
				Original.LOGOUT_X,
				Vertical.LOGOUT_X,
				Horizontal.LOGOUT_X
			),
			Y, new SetValue(
				Original.LOGOUT_Y,
				Vertical.LOGOUT_Y,
				Horizontal.LOGOUT_Y
			)
		)
	),
	GRID_MASTER_ORB_CONTAINER(
		Orb.UNIVERSE, 0,
		Script.GRID_MASTER_ORB_UPDATE,
		Map.of(
			X, new SetValue(
				Original.ACTIVITY_ORB_X,
				Vertical.ACTIVITY_ORB_X,
				Horizontal.ACTIVITY_ORB_X
			),
			Y, new SetValue(
				Original.ACTIVITY_ORB_Y,
				Vertical.ACTIVITY_ORB_Y,
				Horizontal.ACTIVITY_ORB_Y
			),
			X_POSITION_MODE, new SetValue(
				WidgetPositionMode.ABSOLUTE_LEFT,
				WidgetPositionMode.ABSOLUTE_RIGHT,
				WidgetPositionMode.ABSOLUTE_LEFT
			)
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

	Orbs(int componentId, Map<ValueKey, SetValue> positions)
	{
		this.componentId = componentId;
		this.arrayId = -1;
		this.scriptId = -1;
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
