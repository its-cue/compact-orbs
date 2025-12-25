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

import com.compactorbs.CompactOrbsConstants.Widgets.Classic;
import com.compactorbs.CompactOrbsConstants.Widgets.Modern;
import com.compactorbs.util.SetValue;
import com.compactorbs.util.ValueKey;
import com.compactorbs.widget.TargetWidget;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Minimap implements TargetWidget
{
	//classic-resizable
	CLASSIC_NO_CLICK_0(Classic.MAP_NOCLICK_0),
	CLASSIC_NO_CLICK_1(Classic.MAP_NOCLICK_1),
	CLASSIC_NO_CLICK_2(Classic.MAP_NOCLICK_2),
	CLASSIC_NO_CLICK_3(Classic.MAP_NOCLICK_3),
	CLASSIC_NO_CLICK_4(Classic.MAP_NOCLICK_4),
	CLASSIC_NO_CLICK_5(Classic.MAP_NOCLICK_5),
	CLASSIC_MINIMAP_MASK(Classic.MINIMAP_MASK),
	CLASSIC_MINIMAP(Classic.MINIMAP),

	//modern-resizable
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

}
