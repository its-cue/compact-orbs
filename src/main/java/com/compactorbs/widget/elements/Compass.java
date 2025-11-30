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

import com.compactorbs.CompactOrbsConstants.Layout;
import com.compactorbs.CompactOrbsConstants.Layout.Original;
import com.compactorbs.CompactOrbsConstants.Layout.Vertical;
import com.compactorbs.CompactOrbsConstants.Layout.Horizontal;
import com.compactorbs.CompactOrbsConstants.Widgets.Classic;
import com.compactorbs.CompactOrbsConstants.Widgets.Modern;
import com.compactorbs.util.SetValue;
import com.compactorbs.util.ValueKey;
import static com.compactorbs.util.ValueKey.X;
import static com.compactorbs.util.ValueKey.Y;
import com.compactorbs.widget.TargetWidget;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Compass implements TargetWidget
{
	CLASSIC_COMPASS(
		Classic.COMPASS,
		Map.of(
			X, new SetValue(
				Original.COMPASS_X,
				Vertical.COMPASS_X,
				Horizontal.COMPASS_X
			),
			Y, new SetValue(
				Original.COMPASS_Y,
				Vertical.COMPASS_Y,
				Horizontal.COMPASS_Y
			)
		)
	),
	CLASSIC_COMPASS_OPTIONS(
		Classic.COMPASS_OPTIONS,
		Map.of(
			X, new SetValue(
				Original.COMPASS_X - Layout.COMPASS_OPTIONS_OFFSET,
				Vertical.COMPASS_X - Layout.COMPASS_OPTIONS_OFFSET,
				Horizontal.COMPASS_X - Layout.COMPASS_OPTIONS_OFFSET
			),
			Y, new SetValue(
				Original.COMPASS_Y - Layout.COMPASS_OPTIONS_OFFSET,
				Vertical.COMPASS_Y - Layout.COMPASS_OPTIONS_OFFSET,
				Horizontal.COMPASS_Y - Layout.COMPASS_OPTIONS_OFFSET
			)
		)
	),

	MODERN_COMPASS(
		Modern.COMPASS,
		Map.of(
			X, new SetValue(
				Original.COMPASS_X,
				Vertical.COMPASS_X,
				Horizontal.COMPASS_X
			),
			Y, new SetValue(
				Original.COMPASS_Y,
				Vertical.COMPASS_Y,
				Horizontal.COMPASS_Y
			)
		)
	),
	MODERN_COMPASS_OPTIONS(
		Modern.COMPASS_OPTIONS,
		Map.of(
			X, new SetValue(
				Original.COMPASS_X - Layout.COMPASS_OPTIONS_OFFSET,
				Vertical.COMPASS_X - Layout.COMPASS_OPTIONS_OFFSET,
				Horizontal.COMPASS_X - Layout.COMPASS_OPTIONS_OFFSET
			),
			Y, new SetValue(
				Original.COMPASS_Y - Layout.COMPASS_OPTIONS_OFFSET,
				Vertical.COMPASS_Y - Layout.COMPASS_OPTIONS_OFFSET,
				Horizontal.COMPASS_Y - Layout.COMPASS_OPTIONS_OFFSET
			)
		)
	);

	private final int componentId;

	private final Map<ValueKey, SetValue> positionMap;

}
