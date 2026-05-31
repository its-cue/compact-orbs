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

import com.compactorbs.CompactOrbsConstants;
import com.compactorbs.CompactOrbsConstants.Layout.Horizontal;
import com.compactorbs.CompactOrbsConstants.Layout.HorizontalWide;
import com.compactorbs.CompactOrbsConstants.Layout.Original;
import com.compactorbs.CompactOrbsConstants.Layout.Vertical;
import com.compactorbs.CompactOrbsConstants.Widgets.Classic;
import com.compactorbs.CompactOrbsConstants.Widgets.Modern;
import com.compactorbs.CompactOrbsConstants.Widgets.Orb;
import com.compactorbs.util.SetValue;
import com.compactorbs.util.ValueKey;
import static com.compactorbs.util.ValueKey.HEIGHT;
import static com.compactorbs.util.ValueKey.WIDTH;
import static com.compactorbs.util.ValueKey.WIDTH_MODE;
import static com.compactorbs.util.ValueKey.X;
import static com.compactorbs.util.ValueKey.Y;
import com.compactorbs.widget.TargetWidget;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.widgets.WidgetSizeMode;

@Getter
@RequiredArgsConstructor
public enum Minimap implements TargetWidget
{
	//top level container
	CLASSIC_MAP_CONTAINER(
		Classic.MAP_CONTAINER,
		Map.of(
			WIDTH, new SetValue(
				Original.MAP_CONTAINER_WIDTH,
				Vertical.MAP_CONTAINER_WIDTH,
				Horizontal.MAP_CONTAINER_WIDTH,
				HorizontalWide.MAP_CONTAINER_WIDTH
			),
			HEIGHT, new SetValue(
				Original.MAP_CONTAINER_HEIGHT,
				Vertical.MAP_CONTAINER_HEIGHT,
				Horizontal.MAP_CONTAINER_HEIGHT,
				HorizontalWide.MAP_CONTAINER_HEIGHT
			)
		)
	),
	//container for the minimap / compass
	CLASSIC_MAP_MINIMAP(
		Classic.COMPASS_PARENT,
		Map.of(
			X, new SetValue(
				0,
				Vertical.COMPASS_X,
				Horizontal.COMPASS_X,
				HorizontalWide.COMPASS_X
			),
			Y, new SetValue(
				0,
				Vertical.COMPASS_Y,
				Horizontal.COMPASS_Y,
				HorizontalWide.COMPASS_Y
			),
			WIDTH, new SetValue(
				0,
				CompactOrbsConstants.Layout.COMPASS_FRAME_SIZE
			),
			HEIGHT, new SetValue(
				Original.MAP_CONTAINER_HEIGHT - 28,
				CompactOrbsConstants.Layout.COMPASS_FRAME_SIZE
			),
			WIDTH_MODE, new SetValue(
				WidgetSizeMode.MINUS,
				WidgetSizeMode.ABSOLUTE
			)
		)
	),
	CLASSIC_ORBS_CONTAINER(
		Classic.ORBS,
		Map.of(
			X, new SetValue(
				0
			),
			Y, new SetValue(
				10,
				0
			),
			WIDTH, new SetValue(
				Original.MAP_CONTAINER_WIDTH,
				Vertical.MAP_CONTAINER_WIDTH,
				Horizontal.MAP_CONTAINER_WIDTH,
				HorizontalWide.MAP_CONTAINER_WIDTH
			),
			HEIGHT, new SetValue(
				Original.MAP_CONTAINER_HEIGHT,
				Vertical.MAP_CONTAINER_HEIGHT,
				Horizontal.MAP_CONTAINER_HEIGHT,
				HorizontalWide.MAP_CONTAINER_HEIGHT
			)
		)
	),

	//top level container
	MODERN_MAP_CONTAINER(
		Modern.MAP_CONTAINER,
		Map.of(
			WIDTH, new SetValue(
				Original.MAP_CONTAINER_WIDTH,
				Vertical.MAP_CONTAINER_WIDTH,
				Horizontal.MAP_CONTAINER_WIDTH,
				HorizontalWide.MAP_CONTAINER_WIDTH
			),
			HEIGHT, new SetValue(
				Original.MAP_CONTAINER_HEIGHT,
				Vertical.MAP_CONTAINER_HEIGHT,
				Horizontal.MAP_CONTAINER_HEIGHT,
				HorizontalWide.MAP_CONTAINER_HEIGHT
			)
		)
	),
	//container for the minimap / compass
	MODERN_MAP_MINIMAP(
		Modern.COMPASS_PARENT,
		Map.of(
			X, new SetValue(
				0,
				Vertical.COMPASS_X,
				Horizontal.COMPASS_X,
				HorizontalWide.COMPASS_X
			),
			Y, new SetValue(
				0,
				Vertical.COMPASS_Y,
				Horizontal.COMPASS_Y,
				HorizontalWide.COMPASS_Y
			),
			WIDTH, new SetValue(
				0,
				CompactOrbsConstants.Layout.COMPASS_FRAME_SIZE
			),
			HEIGHT, new SetValue(
				Original.MAP_CONTAINER_HEIGHT - 28,
				CompactOrbsConstants.Layout.COMPASS_FRAME_SIZE
			),
			WIDTH_MODE, new SetValue(
				WidgetSizeMode.MINUS,
				WidgetSizeMode.ABSOLUTE
			)
		)
	),
	MODERN_ORBS_CONTAINER(
		Modern.ORBS,
		Map.of(
			X, new SetValue(
				0
			),
			Y, new SetValue(
				10,
				0
			),
			WIDTH, new SetValue(
				Original.MAP_CONTAINER_WIDTH,
				Vertical.MAP_CONTAINER_WIDTH,
				Horizontal.MAP_CONTAINER_WIDTH,
				HorizontalWide.MAP_CONTAINER_WIDTH
			),
			HEIGHT, new SetValue(
				Original.MAP_CONTAINER_HEIGHT,
				Vertical.MAP_CONTAINER_HEIGHT,
				Horizontal.MAP_CONTAINER_HEIGHT,
				HorizontalWide.MAP_CONTAINER_HEIGHT
			)
		)
	),

	ORBS_UNIVERSE(Orb.UNIVERSE),

	CLASSIC_NO_CLICK_0(Classic.MAP_NOCLICK_0),
	CLASSIC_NO_CLICK_1(Classic.MAP_NOCLICK_1),
	CLASSIC_NO_CLICK_2(Classic.MAP_NOCLICK_2),
	CLASSIC_NO_CLICK_3(Classic.MAP_NOCLICK_3),
	CLASSIC_NO_CLICK_4(Classic.MAP_NOCLICK_4),
	CLASSIC_NO_CLICK_5(Classic.MAP_NOCLICK_5),
	CLASSIC_MINIMAP_MASK(Classic.MINIMAP_MASK),
	CLASSIC_MINIMAP(Classic.MINIMAP),

	MODERN_NO_CLICK_0(Modern.MAP_NOCLICK_0),
	MODERN_NO_CLICK_1(Modern.MAP_NOCLICK_1),
	MODERN_NO_CLICK_2(Modern.MAP_NOCLICK_2),
	MODERN_NO_CLICK_3(Modern.MAP_NOCLICK_3),
	MODERN_NO_CLICK_4(Modern.MAP_NOCLICK_4),
	MODERN_NO_CLICK_5(Modern.MAP_NOCLICK_5),
	MODERN_MINIMAP_MASK(Modern.MINIMAP_MASK),
	MODERN_MINIMAP(Modern.MINIMAP);

	private final int componentId;

	private final Map<ValueKey, SetValue> valueMap;

	Minimap(int componentId)
	{
		this(componentId, Map.of());
	}

	public static final TargetWidget[] CONTAINERS =
		{
			CLASSIC_MAP_CONTAINER, MODERN_MAP_CONTAINER,
			CLASSIC_ORBS_CONTAINER, MODERN_ORBS_CONTAINER,
			CLASSIC_MAP_MINIMAP, MODERN_MAP_MINIMAP
		};

	public static final TargetWidget[] COMPONENTS =
		{
			CLASSIC_NO_CLICK_0, MODERN_NO_CLICK_0,
			CLASSIC_NO_CLICK_1, MODERN_NO_CLICK_1,
			CLASSIC_NO_CLICK_2, MODERN_NO_CLICK_2,
			CLASSIC_NO_CLICK_3, MODERN_NO_CLICK_3,
			CLASSIC_NO_CLICK_4, MODERN_NO_CLICK_4,
			CLASSIC_NO_CLICK_5, MODERN_NO_CLICK_5,
			CLASSIC_MINIMAP, MODERN_MINIMAP,
			CLASSIC_MINIMAP_MASK, MODERN_MINIMAP_MASK
		};
}
