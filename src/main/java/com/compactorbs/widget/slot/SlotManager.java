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
import java.util.HashMap;
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

	public SlotLayoutMode currentSlotLayoutMode;

	public enum SlotLayoutMode
	{
		COMPACT,
		VANILLA
	}

	//if previousId is -1, additional check to apply an update if in fixed mode atleast once
	public boolean allowFixedModeUpdate;

	//map of slots and their target widget per layout mode
	private final EnumMap<SlotLayoutMode, EnumMap<Slot, TargetWidget>> slotLayoutMap = new EnumMap<>(SlotLayoutMode.class);

	public void initSlots()
	{
		validateSlotConfig();
		generateSlots(manager.enableOrbSwapping());
	}

	public void reset()
	{
		slotLayoutMap.clear();
	}

	public void generateSlots(boolean updateVisual)
	{
		reset();

		for (SlotLayoutMode layout : SlotLayoutMode.values())
		{
			for (Slot slot : Slot.values())
			{
				getSlotsByLayout(layout).put(slot, getOrbBySlot(slot, layout));
			}
		}

		remapTargetsForUpdate(updateVisual);
	}

	//handle config change event, swap orb to desired slot and update visuals if swapping is enabled
	public void applySlotSwap(Slot slot, SlotLayoutMode layout)
	{
		TargetWidget desired = slot.getOrbByConfig(config, layout);
		TargetWidget current = getSlotsByLayout(layout).get(slot);

		if (desired == current)
		{
			return;
		}

		swapSlots(layout, slot, desired, current);
		remapTargetsForUpdate(manager.enableOrbSwapping());
	}

	//place the incoming orb at current slot, move outgoing orb to source slot
	//update config for the slot receiving the outgoing orb
	private void swapSlots(SlotLayoutMode layout, Slot targetSlot, TargetWidget incoming, TargetWidget outgoing)
	{
		Slot sourceSlot = findSlotByOrb(incoming, layout);

		getSlotsByLayout(layout).put(targetSlot, incoming);

		if (sourceSlot != null && sourceSlot != targetSlot)
		{
			getSlotsByLayout(layout).put(sourceSlot, outgoing);
			manager.updateConfigForSlot(sourceSlot, outgoing, layout);
		}
	}

	//apply visual updates if swapping is enabled
	private void remapTargetsForUpdate(boolean updateVisual)
	{
		if (updateVisual)
		{
			widgetManager.remapTargets(
				manager.isMinimapHidden() && manager.isResized(),
				CompactOrbsConstants.Script.FORCE_UPDATE,
				//swappable orbs
				Orbs.HP_ORB_CONTAINER, Orbs.PRAYER_ORB_CONTAINER, Orbs.RUN_ORB_CONTAINER, Orbs.SPEC_ORB_CONTAINER,
				//other
				Orbs.ACTIVITY_ORB_CONTAINER, Orbs.STORE_ORB_CONTAINER
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

		Slot targetSlot = findSlotByOrb(target, SlotLayoutMode.COMPACT);
		if (targetSlot == null)
		{
			return 0;
		}

		List<Slot> columnOrRow = (manager.isHorizontalLayout()
			? Slot.getRowSlots(targetSlot)
			: Slot.getColumnSlots(targetSlot));

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
			TargetWidget orbInSlot = getCurrentSlots().get(columnOrRow.get(index));
			if (isOrbHidden(orbInSlot))
			{
				total += count ? 1 : getSlotSize(columnOrRow.get(index));
			}
		}

		return total;
	}

	private int hiddenBelowOffset(List<Slot> columnOrRow, int targetIndex, boolean count)
	{
		int total = 0;

		for (int index = targetIndex + 1; index < columnOrRow.size(); index++)
		{
			Slot slot = columnOrRow.get(index);
			if (slot == Slot.WIKI_SLOT)
			{
				continue;
			}

			TargetWidget orbInSlot = getCurrentSlots().get(slot);
			if (isOrbHidden(orbInSlot))
			{
				total += count ? 1 : getSlotSize(slot);
			}
		}

		return total;
	}

	private int getSlotSize(Slot slot)
	{
		TargetWidget target = getCurrentSlots().get(slot);
		if (target == null)
		{
			return 0;
		}

		Widget widget = widgetManager.getTargetWidget(target);
		if (widget == null)
		{
			return 0;
		}

		return manager.isHorizontalLayout()
			? widget.getOriginalWidth()
			: widget.getOriginalHeight();
	}

	public int getVerticalHiddenHeight()
	{
		if (manager.leaveEmptySpace() || manager.preventReordering())
		{
			return 0;
		}

		return Math.min(
			sumHiddenSize(Slot.VERTICAL_LEFT_COLUMN),
			sumHiddenSize(Slot.VERTICAL_RIGHT_COLUMN)
		);
	}

	public int getHorizontalHiddenWidth()
	{
		if (manager.leaveEmptySpace() || manager.preventReordering())
		{
			return 0;
		}

		return Math.min(
			sumHiddenSize(Slot.HORIZONTAL_TOP_ROW),
			sumHiddenSize(Slot.HORIZONTAL_BOTTOM_ROW)
		);
	}

	private int sumHiddenSize(List<Slot> columnOrRow)
	{
		int total = 0;

		for (Slot slot : columnOrRow)
		{
			if (slot == Slot.WIKI_SLOT)
			{
				continue;
			}

			TargetWidget target = getCurrentSlots().get(slot);

			if (isOrbHidden(target))
			{
				total += getSlotSize(slot);
			}
		}

		return total;
	}

	private int hiddenAboveOffset(TargetWidget target, int value)
	{
		if (target != null)
		{
			int sum = getHiddenDimensionsAbove(target);
			value -= sum;
		}

		return value;
	}

	private int hiddenBelowOffset(TargetWidget target, int value)
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

	private boolean isOrbHidden(TargetWidget target)
	{
		if (target == null)
		{
			return false;
		}

		Supplier<Boolean> entry = manager.orbToToggle.get(target);
		return entry != null && entry.get();
	}

	public Slot findSlotByOrb(TargetWidget target, SlotLayoutMode layout)
	{
		Map<Slot, TargetWidget> map = getSlotsByLayout(layout);
		if (map == null || map.isEmpty())
		{
			return null;
		}

		for (Map.Entry<Slot, TargetWidget> entry : map.entrySet())
		{
			if (entry.getValue() == target)
			{
				return entry.getKey();
			}
		}

		return null;
	}

	private TargetWidget getOrbBySlot(Slot slot, SlotLayoutMode layout)
	{
		TargetWidget target = slot.getOriginal();
		if (target instanceof Orbs && manager.enableOrbSwapping())
		{
			TargetWidget configured = slot.getOrbByConfig(config, layout);
			if (configured != null)
			{
				return configured;
			}
		}
		return target;
	}

	private Map<Slot, TargetWidget> getSlotsByLayout(SlotLayoutMode layout)
	{
		return slotLayoutMap.computeIfAbsent(layout, k -> new EnumMap<>(Slot.class));
	}

	private Map<Slot, TargetWidget> getCurrentSlots()
	{
		if (currentSlotLayoutMode == null)
		{
			updateCurrentSlotLayout();
		}

		return getSlotsByLayout(currentSlotLayoutMode);
	}

	public void updateCurrentSlotLayout()
	{
		if (manager.isCompactLayout())
		{
			currentSlotLayoutMode = SlotLayoutMode.COMPACT;
			return;
		}

		currentSlotLayoutMode = SlotLayoutMode.VANILLA;
	}

	//validate each slot in the config has a unique orb per layout
	private void validateSlotConfig()
	{
		for (SlotLayoutMode layout : SlotLayoutMode.values())
		{
			Map<TargetWidget, Slot> seen = new HashMap<>();
			boolean repeat = false;

			for (Slot slot : Slot.values())
			{
				if (slot.getSlotConfigMap().isEmpty())
				{
					continue;
				}

				TargetWidget orb = slot.getOrbByConfig(config, layout);
				if (seen.containsKey(orb))
				{
					log.debug("Non-unique orb {} found in layout: {}", orb, layout);
					repeat = true;
					break;
				}

				seen.put(orb, slot);
			}

			//should only happen if config changes were made while plugin was inactive
			//and slots in the same layout contain a repeat orb
			if (repeat)
			{
				for (Slot slot : Slot.values())
				{
					if (slot.getSlotConfigMap().isEmpty())
					{
						continue;
					}

					//reset all configs to default
					manager.updateConfigForSlot(slot, slot.getOriginal(), layout);
				}
			}
		}
	}
}
