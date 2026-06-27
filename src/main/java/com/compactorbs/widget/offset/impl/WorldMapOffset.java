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

import com.compactorbs.CompactOrbsConstants.Layout;
import com.compactorbs.CompactOrbsManager;
import com.compactorbs.widget.elements.Orbs;
import com.compactorbs.widget.offset.OffsetTarget;
import com.compactorbs.widget.slot.SlotManager;
import lombok.Getter;

@Getter
public class WorldMapOffset implements OffsetTarget
{
	private int getHiddenOffset()
	{
		//this will keep 1 pixel inside the container -
		//enough to keep valid for hotkey use
		return -29;
	}

	@Override
	public int xOffset(int x, boolean compactLayout, CompactOrbsManager manager, SlotManager slotManager)
	{
		if (manager.hideWorldMap)
		{
			return getHiddenOffset();
		}

		if (!compactLayout)
		{
			return manager.isFixedMode() ? 10 : x;
		}

		if (manager.allowReordering())
		{
			if (manager.getCurrentLayout().isHorizontal())
			{
				if (manager.isCompassHidden())
				{
					x += 10;

					if (manager.isWikiHidden())
					{
						x -= 6;
					}
				}

				if (manager.isVerticalLeft())
				{
					x -= slotManager.getHiddenSize();
				}
			}

			if (manager.getCurrentLayout().isHorizontalWide())
			{
				if (manager.isClassicResizable() || manager.hideLogoutX)
				{
					if (manager.isXpDropHidden())
					{
						if (manager.hideMinimapToggle())
						{
							x += Layout.TOGGLE_BUTTON_SIZE + 9;

							if (manager.isWikiHidden())
							{
								x += 4;
							}
						}
						else
						{
							if (manager.isWikiHidden())
							{
								x += Layout.TOGGLE_BUTTON_SIZE + 13;
							}
						}
					}
				}
			}
		}

		return x;
	}

	@Override
	public int yOffset(int y, boolean compactLayout, CompactOrbsManager manager, SlotManager slotManager)
	{
		if (manager.hideWorldMap)
		{
			return getHiddenOffset();
		}

		if (!compactLayout)
		{
			return y;
		}

		if (manager.allowReordering())
		{
			if (manager.getCurrentLayout().isVertical())
			{
				y = slotManager.applyHiddenYOffset(Orbs.WORLD_MAP_CONTAINER, y);
			}

			if (manager.getCurrentLayout().isHorizontal())
			{
				if (manager.isCompassHidden())
				{
					y += 41;

					if (manager.isWikiHidden())
					{
						y -= 5;
					}
				}
			}
		}

		return y;
	}
}
