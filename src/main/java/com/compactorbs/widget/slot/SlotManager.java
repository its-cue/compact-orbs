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

	//if previousId is -1 (guard only being in fixed mode)
	public boolean allowFixedModeUpdate;

	//map of slots and their target widget (can be Orbs or Compass)
	public final EnumMap<Slot, TargetWidget> slotMap = new EnumMap<>(Slot.class);

	public void initSlots()
	{
		slotMap.clear();

		allowFixedModeUpdate = true;

		updateSlots(manager.enableOrbSwapping());
	}

	//@swapping used for config toggle to apply visual updates
	//always update regardless of manager.enableOrbSwapping();
	public void updateSlots(boolean configChange)
	{
		for (Slot slot : Slot.values())
		{
			TargetWidget target = slot.getOriginal();

			//not
			if (target instanceof Orbs && manager.enableOrbSwapping())
			{
				TargetWidget configured = slot.configuredOrbOf(config);
				if (configured != null)
				{
					target = configured;
				}
			}
			slotMap.put(slot, target);
		}

		remapTargetsForUpdate(configChange);
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
	public int getHiddenHeightAbove(TargetWidget target)
	{
		return getHiddenRelative(target, true, false);
	}

	//return the height of the hidden orbs below
	public int getHiddenHeightBelow(TargetWidget target)
	{
		return getHiddenRelative(target, true, true);
	}

	//return a count of the hidden orbs above
	public int getHiddenCountAbove(TargetWidget target)
	{
		return getHiddenRelative(target, false, false);
	}

	//return a count of the hidden orbs below
	public int getHiddenCountBelow(TargetWidget target)
	{
		return getHiddenRelative(target, false, true);
	}

	//return the amount of hidden orbs above or below a target widget
	// @getHeight returns the height value of hidden orbs
	// @below returns the count of hidden orbs, above or below
	public int getHiddenRelative(TargetWidget target, boolean getHeight, boolean below)
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

		List<Slot> column = Slot.getColumnOf(targetSlot);
		if (column == null || column.isEmpty())
		{
			return 0;
		}

		int targetIndex = column.indexOf(targetSlot);
		if (targetIndex < 0)
		{
			return 0;
		}

		return below
			? calcHiddenBelow(column, targetIndex, getHeight)
			: calcHiddenAbove(column, targetIndex, getHeight);
	}

	private int calcHiddenAbove(List<Slot> column, int targetIndex, boolean getHeight)
	{
		int total = 0;

		for (int index = 0; index < targetIndex; index++)
		{
			total += getHiddenValueForSlot(column.get(index), getHeight);
		}

		return total;
	}

	private int calcHiddenBelow(List<Slot> column, int targetIndex, boolean getHeight)
	{
		int total = 0;

		for (int index = targetIndex + 1; index < column.size(); index++)
		{
			//ignore wiki slot for /below/ calculation
			if (column.get(index) != Slot.WIKI_SLOT)
			{
				total += getHiddenValueForSlot(column.get(index), getHeight);
			}
		}

		return total;
	}

	private int getHiddenValueForSlot(Slot slot, boolean getHeight)
	{
		TargetWidget target = slotMap.get(slot);
		if (target == null)
		{
			return 0;
		}

		if (!isOrbAtSlotHidden(target))
		{
			return 0;
		}

		return getHeight ? getSlotHeight(slot) : 1;
	}

	private boolean isOrbAtSlotHidden(TargetWidget target)
	{
		Slot slot = Slot.slotOf(target);

		assert slot != null;
		TargetWidget originalOrb = slot.getOriginal();

		Supplier<Boolean> entry = manager.orbToToggle.get(originalOrb);
		return entry != null && entry.get();
	}

	public int getSlotHeight(Slot slot)
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

		return widget.getOriginalHeight();
	}

	public boolean isOrbHidden(Slot slot)
	{
		TargetWidget target = getOrbOrNull(slot);
		if (target == null)
		{
			return false;
		}

		Supplier<Boolean> entry = manager.orbToToggle.get(target);
		return entry != null && entry.get();
	}

	public int getCeilingHeight()
	{
		int leftHeight = getTotalHeight(Slot.VERTICAL_LEFT_COLUMN);
		int rightHeight = getTotalHeight(Slot.VERTICAL_RIGHT_COLUMN);

		return Math.min(leftHeight, rightHeight);
	}

	private int getTotalHeight(List<Slot> column)
	{
		int totalHeight = 0;

		for (Slot slot : column)
		{
			TargetWidget target = slotMap.get(slot);

			if (target != null && isOrbHidden(slot))
			{
				//ignore wiki slot
				if (slot != Slot.WIKI_SLOT)
				{
					totalHeight += getSlotHeight(slot);
				}
			}
		}

		return totalHeight;
	}

	public int calcHiddenAbove(TargetWidget target, int y)
	{
		if (target != null)
		{
			int hiddenAboveHeight = getHiddenHeightAbove(target);
			y -= hiddenAboveHeight;
		}

		return y;
	}

	public int calcHiddenBelow(TargetWidget target, int y)
	{
		if (target != null)
		{
			int hiddenAboveHeight = getHiddenHeightBelow(target);
			y += hiddenAboveHeight;
		}

		return y;
	}

	public int applyHiddenOffset(TargetWidget target, int y)
	{
		//if horizontal is top, everything shifts to the top (bottom-up)
		if (manager.isHorizontalTop())
		{
			//get how many are hidden above, and adjust Y accordingly (this case, subtract)
			return calcHiddenAbove(target, y);
		}
		//if horizontal is bottom, everything shifts to the bottom (top-down)
		else if (manager.isHorizontalBottom())
		{
			//get how many are hidden below, and adjust Y accordingly (this case, add)
			return calcHiddenBelow(target, y);
		}

		return y;
	}

	public void reset()
	{
		slotMap.clear();
	}

	private TargetWidget getConfiguredOrb(Slot slot)
	{
		return slot.configuredOrbOf(config);
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
