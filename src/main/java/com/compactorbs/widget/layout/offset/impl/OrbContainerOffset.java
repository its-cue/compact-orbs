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

package com.compactorbs.widget.layout.offset.impl;

import com.compactorbs.CompactOrbsManager;
import com.compactorbs.widget.layout.offset.OffsetTarget;
import com.compactorbs.widget.layout.slot.SlotManager;
import lombok.Getter;

@Getter
public class OrbContainerOffset implements OffsetTarget
{
	private int offsetX;
	private int offsetY;
	private int offsetWidth;
	private int offsetHeight;

	@Override
	public int xOffset(int x, boolean compact, CompactOrbsManager manager, SlotManager slotManager)
	{
		offsetX = x;

		if (!compact)
		{
			return offsetX;
		}

		if (manager.isVerticalRight())
		{
			offsetX += manager.getCurrentLayout().getRightOffset();
		}

		return offsetX;
	}

	@Override
	public int yOffset(int y, boolean compact, CompactOrbsManager manager, SlotManager slotManager)
	{
		offsetY = y;

		if (!compact)
		{
			return offsetY;
		}

		if (manager.isHorizontalBottom())
		{
			offsetY += manager.getCurrentLayout().getBottomOffset();
		}

		return offsetY;
	}

	@Override
	public int widthOffset(int w, boolean compact, CompactOrbsManager manager, SlotManager slotManager)
	{
		offsetWidth = w;
		return offsetWidth;
	}

	@Override
	public int heightOffset(int h, boolean compact, CompactOrbsManager manager, SlotManager slotManager)
	{
		offsetHeight = h;
		return offsetHeight;
	}

}