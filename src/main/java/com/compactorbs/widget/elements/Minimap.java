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

import com.compactorbs.widget.TargetWidget;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ScriptID;
import net.runelite.api.gameval.InterfaceID;

@Getter
@RequiredArgsConstructor
public enum Minimap implements TargetWidget
{
	//classic_resizable
	CLASSIC_NO_CLICK_0(InterfaceID.TOPLEVEL_OSRS_STRETCH, 23),
	CLASSIC_NO_CLICK_1(InterfaceID.TOPLEVEL_OSRS_STRETCH, 24),
	CLASSIC_NO_CLICK_2(InterfaceID.TOPLEVEL_OSRS_STRETCH, 25),
	CLASSIC_NO_CLICK_3(InterfaceID.TOPLEVEL_OSRS_STRETCH, 26),
	CLASSIC_NO_CLICK_4(InterfaceID.TOPLEVEL_OSRS_STRETCH, 27),
	CLASSIC_NO_CLICK_5(InterfaceID.TOPLEVEL_OSRS_STRETCH, 28),

	CLASSIC_MINIMAP_MASK(InterfaceID.TOPLEVEL_OSRS_STRETCH, 30),
	CLASSIC_MINIMAP(InterfaceID.TOPLEVEL_OSRS_STRETCH, 32),

	//modern resizable
	MODERN_NO_CLICK_0(InterfaceID.TOPLEVEL_PRE_EOC, 23),
	MODERN_NO_CLICK_1(InterfaceID.TOPLEVEL_PRE_EOC, 24),
	MODERN_NO_CLICK_2(InterfaceID.TOPLEVEL_PRE_EOC, 25),
	MODERN_NO_CLICK_3(InterfaceID.TOPLEVEL_PRE_EOC, 26),
	MODERN_NO_CLICK_4(InterfaceID.TOPLEVEL_PRE_EOC, 27),
	MODERN_NO_CLICK_5(InterfaceID.TOPLEVEL_PRE_EOC, 28),

	MODERN_MINIMAP_MASK(InterfaceID.TOPLEVEL_PRE_EOC, 30),
	MODERN_MINIMAP(InterfaceID.TOPLEVEL_PRE_EOC, 32);

	private final int interfaceId, childId, scriptId = -1;


}
