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

package com.compactorbs.widget.slot;

import com.compactorbs.CompactOrbsConfig;
import com.compactorbs.CompactOrbsConstants;
import com.compactorbs.CompactOrbsManager;
import com.compactorbs.widget.TargetWidget;
import com.compactorbs.widget.WidgetManager;
import com.compactorbs.widget.elements.Orbs;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.widgets.Widget;

@Slf4j
@Singleton
public class SlotManager
{
	@Inject
	private CompactOrbsConfig config;

	@Inject
	private CompactOrbsManager manager;

	@Inject
	private WidgetManager widgetManager;

	//if previousId is -1, additional check to apply an update if in fixed mode atleast once
	public boolean allowFixedModeUpdate;

	//map of slots and their target widget (can be Orbs or Compass)
	public final EnumMap<Slot, TargetWidget> slotMap = new EnumMap<>(Slot.class);

	public void initSlots()
	{
		reset();

		allowFixedModeUpdate = true;

		updateSlots(manager.enableOrbSwapping());
	}

	//@onConfigChange used for config toggle to apply visual updates
	//always update regardless of manager.enableOrbSwapping();
	public void updateSlots(boolean onConfigChange)
	{
		for (Slot slot : Slot.values())
		{
			TargetWidget target = slot.getOriginal();

			if (target instanceof Orbs && manager.enableOrbSwapping())
			{
				TargetWidget configured = slot.getConfiguredOrbOf(config);
				if (configured != null)
				{
					target = configured;
				}
			}
			slotMap.put(slot, target);
		}

		remapTargetsForUpdate(onConfigChange);
	}

	public void applySlotUpdate(Slot slot)
	{
		//orb bound for the targeted slot
		TargetWidget incomingOrb = getConfiguredOrb(slot);

		//orb that is currently in the targeted slot
		TargetWidget outgoingOrb = getCurrentOrb(slot);

		if (incomingOrb == outgoingOrb)
		{
			return;
		}

		//get slot of the incoming orb for replacement
		Slot slotOfIncoming = getSlotOf(incomingOrb);

		slotMap.put(slot, incomingOrb);

		if (slotOfIncoming != slot)
		{
			slotMap.put(slotOfIncoming, outgoingOrb);
			manager.updateConfigForSlot(slotOfIncoming, outgoingOrb);
		}

		//apply visual change if swapping is enabled
		remapTargetsForUpdate(manager.enableOrbSwapping());
	}

	//make visual changes if swapping is true (onConfigChanged event is always true)
	//remapTargets only makes changes if loggedIn
	private void remapTargetsForUpdate(boolean updateVisual)
	{
		if (updateVisual)
		{
			widgetManager.remapTargets(
				manager.isMinimapHidden() && manager.isResized(),
				CompactOrbsConstants.Script.FORCE_UPDATE,
				Orbs.SWAPPABLE_ORBS.toArray(Orbs[]::new)
			);
		}
	}

	//return the height of the hidden orbs above
	public int getHiddenDimensionsAbove(TargetWidget target)
	{
		return computeHiddenOffset(target, false, false);
	}

	//return the height of the hidden orbs below
	public int getHiddenDimensionsBelow(TargetWidget target)
	{
		return computeHiddenOffset(target, false, true);
	}

	//return a count of the hidden orbs above
	public int getHiddenCountAbove(TargetWidget target)
	{
		return computeHiddenOffset(target, true, false);
	}

	//return a count of the hidden orbs below
	public int getHiddenCountBelow(TargetWidget target)
	{
		return computeHiddenOffset(target, true, true);
	}

	//return the amount of hidden orbs above or below a target widget
	// @dimension return hidden orb dimensions
	// @below returns hidden orb count (for above, or below)
	public int computeHiddenOffset(TargetWidget target, boolean count, boolean isBelow)
	{
		if (target == null)
		{
			return 0;
		}

		Slot targetSlot = getSlotOf(target);
		if (targetSlot == null)
		{
			return 0;
		}

		List<Slot> columnOrRow = (manager.isHorizontalLayout()
				? Slot.getRowOf(targetSlot)
				: Slot.getColumnOf(targetSlot));

		if (columnOrRow == null || columnOrRow.isEmpty())
		{
			return 0;
		}

		int targetIndex = columnOrRow.indexOf(targetSlot);
		if (targetIndex < 0)
		{
			return 0;
		}

		return isBelow
			? hiddenBelowOffset(columnOrRow, targetIndex, count)
			: hiddenAboveOffset(columnOrRow, targetIndex, count);
	}

	private int hiddenAboveOffset(List<Slot> columnOrRow, int targetIndex, boolean count)
	{
		int total = 0;

		for (int index = 0; index < targetIndex; index++)
		{
			total += getHiddenAmountForSlot(columnOrRow.get(index), count);
		}

		return total;
	}

	private int hiddenBelowOffset(List<Slot> columnOrRow, int targetIndex, boolean count)
	{
		int total = 0;

		for (int index = targetIndex + 1; index < columnOrRow.size(); index++)
		{
			//ignore wiki slot for /below/ calculation in vertical
			if (columnOrRow.get(index) != Slot.WIKI_SLOT)
			{
				total += getHiddenAmountForSlot(columnOrRow.get(index), count);
			}
		}

		return total;
	}

	private int getHiddenAmountForSlot(Slot slot, boolean count)
	{
		TargetWidget target = slotMap.get(slot);
		if (target == null)
		{
			return 0;
		}

		if (!isOriginalOrbHidden(target))
		{
			return 0;
		}

		return count ? 1 : getSlotSize(slot);
	}

	public int getSlotSize(Slot slot)
	{
		TargetWidget target = getOrbOrNull(slot);
		if (target == null)
		{
			return 0;
		}

		Widget widget = widgetManager.getTargetWidget(target);
		if (widget == null)
		{
			return 0;
		}

		return manager.isHorizontalLayout() ? widget.getOriginalWidth() : widget.getOriginalHeight();
	}

	public int getVerticalHiddenHeight()
	{
		if (manager.leaveEmptySpace() || manager.preventReordering())
		{
			return 0;
		}

		return Math.min(sumHiddenSize(Slot.VERTICAL_LEFT_COLUMN), sumHiddenSize(Slot.VERTICAL_RIGHT_COLUMN));
	}

	public int getHorizontalHiddenWidth()
	{
		if (manager.leaveEmptySpace() || manager.preventReordering())
		{
			return 0;
		}

		return Math.min(sumHiddenSize(Slot.HORIZONTAL_TOP_ROW), sumHiddenSize(Slot.HORIZONTAL_BOTTOM_ROW));
	}

	public int hiddenAboveOffset(TargetWidget target, int value)
	{
		if (target != null)
		{
			int sum = getHiddenDimensionsAbove(target);
			value -= sum;
		}

		return value;
	}

	public int hiddenBelowOffset(TargetWidget target, int value)
	{
		if (target != null)
		{
			int sum = getHiddenDimensionsBelow(target);
			value += sum;
		}

		return value;
	}

	public int applyHiddenYOffset(TargetWidget target, int y)
	{
		if (!manager.preventReordering())
		{
			if (manager.isHorizontalTop())
			{
				return hiddenAboveOffset(target, y);
			}
			else if (manager.isHorizontalBottom())
			{
				return hiddenBelowOffset(target, y);
			}
		}

		return y;
	}

	public int applyHiddenXOffset(TargetWidget target, int x)
	{
		if (!manager.preventReordering())
		{
			if (manager.isVerticalLeft())
			{
				return hiddenAboveOffset(target, x);
			}
			else if (manager.isVerticalRight())
			{
				return hiddenBelowOffset(target, x);
			}
		}

		return x;
	}

	private int sumHiddenSize(List<Slot> columnOrRow)
	{
		int total = 0;

		for (Slot slot : columnOrRow)
		{
			TargetWidget target = slotMap.get(slot);

			if (target != null && isCurrentOrbHidden(slot))
			{
				//ignore wiki slot
				if (slot != Slot.WIKI_SLOT)
				{
					total += getSlotSize(slot);
				}
			}
		}

		return total;
	}

	public boolean isCurrentOrbHidden(Slot slot)
	{
		TargetWidget target = getOrbOrNull(slot);
		if (target == null)
		{
			return false;
		}

		Supplier<Boolean> entry = manager.orbToToggle.get(target);
		return entry != null && entry.get();
	}

	private boolean isOriginalOrbHidden(TargetWidget target)
	{
		Slot slot = Slot.getSlotOf(target);
		if (slot == null)
		{
			return false;
		}

		TargetWidget original = slot.getOriginal();

		Supplier<Boolean> entry = manager.orbToToggle.get(original);
		return entry != null && entry.get();
	}

	public void reset()
	{
		slotMap.clear();
	}

	private TargetWidget getConfiguredOrb(Slot slot)
	{
		return slot.getConfiguredOrbOf(config);
	}

	public TargetWidget getCurrentOrb(Slot slot)
	{
		return slotMap.get(slot);
	}

	public Slot getSlotOf(TargetWidget target)
	{
		for (Map.Entry<Slot, TargetWidget> e : slotMap.entrySet())
		{
			if (e.getValue() == target)
			{
				return e.getKey();
			}
		}

		return null;
	}

	private TargetWidget getOrbOrNull(Slot slot)
	{
		if (slot == null)
		{
			return null;
		}

		return getCurrentOrb(slot);
	}
}
