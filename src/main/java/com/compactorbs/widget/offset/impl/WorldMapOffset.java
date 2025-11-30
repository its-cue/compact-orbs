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

package com.compactorbs.widget.offset.impl;

import com.compactorbs.CompactOrbsConstants;
import com.compactorbs.CompactOrbsManager;
import com.compactorbs.widget.elements.Orbs;
import com.compactorbs.widget.offset.OffsetTarget;
import com.compactorbs.widget.slot.SlotManager;
import lombok.Getter;

@Getter
public class WorldMapOffset implements OffsetTarget
{
	@Override
	public int xOffset(int value, boolean compactLayout, CompactOrbsManager manager, SlotManager slotManager)
	{
		int x = CompactOrbsConstants.Layout.FIXED_WORLD_MAP_X;
		int v = value;

		if (!compactLayout)
		{
			v = (manager.updateWorldMap()
				? manager.getWorldMapOffset() : !manager.isResized()
				? x : v
			);

			return v;
		}

		v = (manager.hideWorldMap ? 0 : v + manager.verticalOffset);

		return v + manager.getWorldMapOffset();
	}

	@Override
	public int yOffset(int value, boolean compactLayout, CompactOrbsManager manager, SlotManager slotManager)
	{
		int y = CompactOrbsConstants.Layout.FIXED_WORLD_MAP_Y;

		int v = value;

		if (!compactLayout)
		{
			v = (manager.updateWorldMap()
				? manager.getWorldMapOffset() : !manager.isResized()
				? y : v
			);

			return v;
		}

		y = v + manager.horizontalOffset;

		if (manager.isVerticalLayout())
		{
			y = slotManager.applyHiddenOffset(Orbs.WORLD_MAP_CONTAINER, y);
		}

		if(manager.hideWorldMap)
		{
			y = 0;
		}

		return y + manager.getWorldMapOffset();
	}
}
