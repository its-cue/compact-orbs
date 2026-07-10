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

import com.compactorbs.CompactOrbsConstants.Layout;
import com.compactorbs.CompactOrbsManager;
import com.compactorbs.widget.elements.Orbs;
import com.compactorbs.widget.layout.offset.OffsetTarget;
import com.compactorbs.widget.layout.slot.Slot;
import com.compactorbs.widget.layout.slot.SlotManager;
import lombok.Getter;

@Getter
public class CompassOffset implements OffsetTarget
{
	private int offsetX;
	private int offsetY;

	@Override
	public int xOffset(int x, boolean compactLayout, CompactOrbsManager manager, SlotManager slotManager)
	{
		if (!compactLayout)
		{
			return x;
		}

		if (manager.isAnchorRight())
		{
			x += manager.getCurrentLayout().getRightOffset();
		}

		if (manager.allowReordering())
		{
			if (manager.getCurrentLayout().isVertical())
			{
				if ((manager.isClassicResizable() || manager.hideLogoutX)
					&& manager.getCurrentLayout().isLastVisible(
					Slot.WIKI_SLOT, slotManager.getHiddenCountAbove(Orbs.WIKI_ICON_CONTAINER)))
				{
					x += 18;
				}
			}

			if (manager.getCurrentLayout().isHorizontal()
				&& manager.isAnchorLeft())
			{
				x -= slotManager.getHiddenSize();
			}

			if (manager.getCurrentLayout().isHorizontalWide()
				&& manager.isAnchorRight())
			{
				if (manager.isClassicResizable() || manager.hideLogoutX)
				{
					if (manager.hideMinimapToggle() && (manager.isXpDropHidden() || manager.hideWorldMap))
					{
						x += Layout.TOGGLE_BUTTON_SIZE;
					}

					if (manager.isWikiHidden())
					{
						if (manager.isXpDropHidden())
						{
							x += 20;
						}

						if (manager.hideWorldMap)
						{
							x += 20;
						}
					}
				}
			}
		}

		offsetX = x;
		return offsetX;
	}

	@Override
	public int yOffset(int y, boolean compactLayout, CompactOrbsManager manager, SlotManager slotManager)
	{
		if (!compactLayout)
		{
			return y;
		}

		if (manager.isAnchorBottom())
		{
			y += manager.getCurrentLayout().getBottomOffset() - manager.clampVerticalY();
		}

		if (manager.allowReordering())
		{
			if (manager.getCurrentLayout().isVertical()
				&& manager.isAnchorBottom())
			{
				if ((manager.isClassicResizable() || manager.hideLogoutX)
					&& manager.getCurrentLayout().isLastVisible(
					Slot.WIKI_SLOT, slotManager.getHiddenCountAbove(Orbs.WIKI_ICON_CONTAINER)))
				{
					y = Layout.Vertical.MAP_CONTAINER_HEIGHT - Layout.COMPASS_FRAME_SIZE - 17;
				}
				else
				{
					y += slotManager.getHiddenSize();
				}
			}
		}

		offsetY = y;
		return offsetY;
	}
}