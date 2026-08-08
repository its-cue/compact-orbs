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

import com.compactorbs.widget.TargetWidget;
import com.compactorbs.widget.elements.Compass;
import com.compactorbs.widget.elements.Orbs;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Slot
{
	HP_SLOT(Orbs.HP_ORB_CONTAINER),
	PRAYER_SLOT(Orbs.PRAYER_ORB_CONTAINER),
	RUN_SLOT(Orbs.RUN_ORB_CONTAINER),
	SPEC_SLOT(Orbs.SPEC_ORB_CONTAINER),
	XP_SLOT(Orbs.XP_DROPS_CONTAINER),
	WORLD_MAP_SLOT(Orbs.WORLD_MAP_CONTAINER),
	STORE_SLOT(Orbs.STORE_ORB_CONTAINER),
	ACTIVITY_SLOT(Orbs.ACTIVITY_ORB_CONTAINER),
	WIKI_SLOT(Orbs.WIKI_ICON_CONTAINER),
	COMPASS_SLOT(Compass.CLASSIC_COMPASS),
	LOGOUT_X_SLOT(Orbs.LOGOUT_X_ICON);

	private final TargetWidget defaultTarget;
}
