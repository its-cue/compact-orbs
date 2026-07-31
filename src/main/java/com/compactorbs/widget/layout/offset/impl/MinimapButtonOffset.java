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

import com.compactorbs.CompactOrbsConfig.TogglePlacement;
import com.compactorbs.CompactOrbsConstants.Layout;
import com.compactorbs.CompactOrbsManager;
import com.compactorbs.widget.elements.Orbs;
import com.compactorbs.widget.layout.offset.OffsetTarget;
import com.compactorbs.widget.layout.slot.Slot;
import com.compactorbs.widget.layout.slot.SlotManager;
import lombok.Getter;

@Getter
public class MinimapButtonOffset implements OffsetTarget
{
	@Override
	public int xOffset(int x, boolean compactLayout, CompactOrbsManager manager, SlotManager slotManager)
	{
		if (!compactLayout)
		{
			x = manager.getTogglePlacement().getX();

			if (manager.allowReordering() && !manager.isEditingLayout)
			{
				if (manager.getTogglePlacement() == TogglePlacement.BELOW_MAP
					&& manager.isStoreHidden() && !manager.isStoreOrbDisabled())
				{
					x -= 33;
				}
			}

			return x;
		}

		if (manager.getCurrentLayout().isHorizontal())
		{
			if (manager.allowReordering() && !manager.isEditingLayout)
			{
				if (manager.isWikiHidden())
				{
					x -= 42;
				}
			}

			if (manager.isAnchorLeft())
			{
				int offset = slotManager.getHiddenSize();
				x -= offset;
			}
		}

		return x;
	}

	@Override
	public int yOffset(int y, boolean compactLayout, CompactOrbsManager manager, SlotManager slotManager)
	{
		if (!compactLayout)
		{
			y = manager.getTogglePlacement().getY();

			if (!manager.isEditingLayout)
			{
				//offset when store is hidden and minimap is visible
				if (manager.allowReordering() &&
					manager.getTogglePlacement() == TogglePlacement.BELOW_MAP
					&& manager.isStoreHidden() && !manager.isStoreOrbDisabled())
				{
					y -= 5;
				}

				if (manager.getTogglePlacement() == TogglePlacement.ABOVE_XP)
				{
					if (manager.shouldOffsetXpOrb())
					{
						y -= 2;
					}
				}
			}

			return y;
		}

		if (manager.getCurrentLayout().isVertical())
		{
			y = slotManager.applyHiddenYOffset(Orbs.WIKI_ICON_CONTAINER,
				Layout.Vertical.MAP_CONTAINER_HEIGHT - Layout.TOGGLE_BUTTON_SIZE);

			if (manager.allowReordering() && !manager.isEditingLayout)
			{
				if (manager.isAnchorTop())
				{
					if (manager.getCurrentLayout().isLastVisible(Slot.WIKI_SLOT, slotManager.getHiddenCountAbove(Orbs.WIKI_ICON_CONTAINER)))
					{
						y += 4;
					}

					if (manager.isWikiHidden() && !manager.isClassicResizable() && !manager.hideLogoutX
						&& slotManager.getHiddenCountAbove(Orbs.WIKI_ICON_CONTAINER) >= manager.getCurrentLayout().getGroup(Slot.WIKI_SLOT).indexOf(Slot.WIKI_SLOT))
					{
						y -= 14;
					}
				}
			}
		}

		return y;
	}
}
