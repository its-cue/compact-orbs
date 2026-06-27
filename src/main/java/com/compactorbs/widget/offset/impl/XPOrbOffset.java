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
public class XPOrbOffset implements OffsetTarget
{
	@Override
	public int xOffset(int x, boolean compactLayout, CompactOrbsManager manager, SlotManager slotManager)
	{
		if (!compactLayout)
		{
			return x;
		}

		if (manager.allowReordering())
		{
			if (manager.getCurrentLayout().isHorizontal())
			{
				if (manager.isVerticalLeft())
				{
					x -= slotManager.getHiddenSize();
				}

				if (manager.hideWorldMap)
				{
					x -= 31;

					if (manager.isCompassHidden())
					{
						x += 10;

						if (manager.isWikiHidden())
						{
							x -= 8;
						}
					}
				}
				else
				{
					if (manager.isCompassHidden())
					{
						x -= 28;
					}
				}
			}

			if (manager.getCurrentLayout().isHorizontalWide())
			{
				//move to logout x position
				if (manager.isClassicResizable() || manager.hideLogoutX)
				{
					x += 125;

					if (manager.hideWorldMap)
					{
						if (!manager.isWikiHidden())
						{
							x -= 31;

							if (manager.hideMinimapToggle())
							{
								x += Layout.TOGGLE_BUTTON_SIZE + 9;
							}
						}
					}
				}
				else
				{
					if (manager.hideWorldMap)
					{
						x += 94;
					}
				}
			}
		}

		return x;
	}

	@Override
	public int yOffset(int y, boolean compactLayout, CompactOrbsManager manager, SlotManager slotManager)
	{
		if (!compactLayout)
		{
			if (manager.shouldOffsetXpOrb())
			{
				y -= 2;
			}
			return y;
		}

		if (manager.allowReordering())
		{
			if (manager.getCurrentLayout().isVertical())
			{
				y = slotManager.applyHiddenYOffset(Orbs.XP_DROPS_CONTAINER, y);
			}

			if (manager.getCurrentLayout().isHorizontal())
			{
				if (manager.hideWorldMap)
				{
					y -= 6;

					if (manager.isCompassHidden())
					{
						y += 38;

						if (manager.isWikiHidden())
						{
							y -= 3;
						}
					}
				}
				else
				{
					if (manager.isCompassHidden())
					{
						y -= 2;
					}
				}
			}

			if (manager.getCurrentLayout().isHorizontalWide())
			{
				if (manager.isVerticalRight())
				{
					if (manager.isCompassHidden() ||
						manager.isClassicResizable() ||
						manager.hideWorldMap ||
						manager.hideLogoutX)
					{
						y -= Layout.TOGGLE_BUTTON_SIZE - 5;
					}
				}
			}
		}

		return y;
	}
}
