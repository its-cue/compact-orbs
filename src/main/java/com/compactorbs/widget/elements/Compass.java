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

import static com.compactorbs.CompactOrbsManager.compassX;
import static com.compactorbs.CompactOrbsManager.compassY;
import com.compactorbs.util.SetValue;
import com.compactorbs.util.ValueKey;
import static com.compactorbs.util.ValueKey.X;
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
public enum Compass implements TargetWidget
{
	//classic resizable
	CLASSIC_COMPASS(
		InterfaceID.TOPLEVEL_OSRS_STRETCH, 29,
		Map.of(
			X, new SetValue(34, compassX),
			Y, new SetValue(5, compassY)
		)
	),
	CLASSIC_COMPASS_CONTAINER(
		InterfaceID.TOPLEVEL_OSRS_STRETCH, 31,
		Map.of(
			X, new SetValue(32, compassX - 2),
			Y, new SetValue(3, compassY - 2)
		)
	),

	//modern resizable
	MODERN_COMPASS(
		InterfaceID.TOPLEVEL_PRE_EOC, 29,
		Map.of(
			X, new SetValue(34, compassX),
			Y, new SetValue(5, compassY)
		)
	),
	MODERN_COMPASS_CONTAINER(
		InterfaceID.TOPLEVEL_PRE_EOC, 31,
		Map.of(
			X, new SetValue(32, compassX - 2),
			Y, new SetValue(3, compassY - 2)
		)
	);

	private final int interfaceId, childId, scriptId = -1;

	private final Map<ValueKey, SetValue> positions;

	public static final Set<Compass> ALL = EnumSet.allOf(Compass.class);

}
