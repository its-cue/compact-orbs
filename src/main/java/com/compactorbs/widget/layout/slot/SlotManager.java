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

import com.compactorbs.CompactOrbsConfig;
import com.compactorbs.CompactOrbsLayout;
import com.compactorbs.CompactOrbsManager;
import com.compactorbs.widget.TargetWidget;
import com.compactorbs.widget.WidgetManager;
import com.compactorbs.widget.elements.Orbs;
import com.compactorbs.widget.layout.OrbToggle;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.EnumMap;
import java.util.List;
import lombok.Getter;
import net.runelite.api.widgets.Widget;

@Singleton
public class SlotManager
{
	@Inject
	private CompactOrbsConfig config;

	@Inject
	private CompactOrbsManager manager;

	@Inject
	private WidgetManager widgetManager;

	@Inject
	private SlotRegistry registry;

	public enum SlotsLayoutMode
	{
		COMPACT,
		VANILLA
	}

	@Getter
	private SlotsLayoutMode currentLayoutMode;

	@Getter
	private int hiddenCountAbove;

	private final EnumMap<SlotsLayoutMode, SlotLayout> layouts = new EnumMap<>(SlotsLayoutMode.class);

	public void initSlots()
	{
		layouts.clear();

		updateCurrentLayout();

		for (SlotsLayoutMode mode : SlotsLayoutMode.values())
		{
			SlotLayout state = new SlotLayout();
			load(state, mode);
			layouts.put(mode, state);
		}

		updateOrbPositions();
	}

	public void updateCurrentLayout()
	{
		currentLayoutMode = manager.isCompactLayout()
			? SlotsLayoutMode.COMPACT
			: SlotsLayoutMode.VANILLA;
	}

	public SlotLayout getLayout(SlotsLayoutMode mode)
	{
		return layouts.get(mode);
	}

	public SlotLayout getCurrentLayout()
	{
		return getLayout(currentLayoutMode);
	}

	public TargetWidget get(Slot slot)
	{
		if (!manager.allowReordering())
		{
			return slot.getDefaultTarget();
		}

		return getCurrentLayout().get(slot);
	}

	public Slot find(TargetWidget target)
	{
		return getCurrentLayout().find(target);
	}

	public void swap(TargetWidget first, TargetWidget second)
	{
		Slot firstSlot = find(first);
		Slot secondSlot = find(second);

		if (firstSlot == null || secondSlot == null)
		{
			return;
		}

		SlotLayout layout = getCurrentLayout();
		layout.swap(firstSlot, secondSlot);

		save(currentLayoutMode);

		updateOrbPositions();
	}

	public void save(SlotsLayoutMode mode)
	{
		registry.save(mode, getLayout(mode));
	}

	public void reset()
	{
		getLayout(currentLayoutMode).reset();
	}

	private void updateOrbPositions()
	{
		if (!manager.isLoggedIn())
		{
			return;
		}

		widgetManager.remapTargets(Orbs.SWAPPABLE_ORBS);
	}

	private void load(SlotLayout state, SlotsLayoutMode mode)
	{
		for (Slot slot : Slot.values())
		{
			TargetWidget target =
				config.enableOrbSwapping()
					? registry.resolve(slot, mode, config)
					: slot.getDefaultTarget();

			state.set(slot, target);
		}
	}

	private int computeHiddenOffset(TargetWidget target, boolean isBelow)
	{
		if (target == null || !manager.allowReordering())
		{
			return 0;
		}

		Slot slot = find(target);
		if (slot == null)
		{
			return 0;
		}

		List<Slot> group = manager.getCurrentLayout().getGroup(slot);

		int targetIndex = group.indexOf(slot);
		if (targetIndex < 0)
		{
			return 0;
		}

		return isBelow
			? sumHiddenInRange(group, targetIndex + 1, group.size())
			: sumHiddenInRange(group, 0, targetIndex);
	}

	private int sumHiddenInRange(List<Slot> group, int start, int end)
	{
		int total = 0;
		hiddenCountAbove = 0;

		for (int index = start; index < end; index++)
		{
			Slot slot = group.get(index);

			if (slot == Slot.WIKI_SLOT && index > start)
			{
				continue;
			}

			if (isOrbHidden(slot))
			{
				total += getSlotDimension(slot);
				hiddenCountAbove++;
			}
		}

		return total;
	}

	private boolean isOrbHidden(Slot slot)
	{
		TargetWidget target = get(slot);
		if (target == null)
		{
			return false;
		}

		if (target == Orbs.ACTIVITY_ORB_CONTAINER && manager.isActivityOrbDisabled())
		{
			return config.hideActivity() && manager.allowReordering();
		}

		if (target == Orbs.STORE_ORB_CONTAINER && manager.isStoreOrbDisabled())
		{
			return config.hideStore() && manager.allowReordering();
		}

		OrbToggle toggle = manager.toggleByTarget.get(target);
		return toggle != null && toggle.hidden.get();
	}

	private int getSlotDimension(Slot slot)
	{
		TargetWidget target = get(slot);
		if (target == null)
		{
			return 0;
		}

		Widget widget = widgetManager.getTargetWidget(target);
		if (widget == null)
		{
			return 0;
		}

		if (manager.getCurrentLayout().isHorizontal() ||
			manager.getCurrentLayout().isHorizontalWide())
		{
			return widget.getOriginalWidth();
		}
		else
		{
			return widget.getOriginalHeight();
		}
	}

	public int getHiddenSize()
	{
		if (config.leaveEmptySpace() || config.disableReordering() || manager.isEditingLayout)
		{
			return 0;
		}

		CompactOrbsLayout layout = manager.getCurrentLayout();
		return Math.min(
			sumHiddenSize(layout.getA()),
			sumHiddenSize(layout.getB())
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

			if (isOrbHidden(slot))
			{
				total += getSlotDimension(slot);
			}
		}

		return total;
	}

	public int applyHiddenXOffset(TargetWidget target, int x)
	{
		if (!manager.allowReordering() || manager.isEditingLayout)
		{
			return x;
		}

		if (manager.isAnchorLeft())
		{
			return x - computeHiddenOffset(target, false);
		}

		if (manager.isAnchorRight())
		{
			return x + computeHiddenOffset(target, true);
		}

		return x;
	}

	public int applyHiddenYOffset(TargetWidget target, int y)
	{
		if (!manager.allowReordering() || manager.isEditingLayout)
		{
			return y;
		}

		if (manager.isAnchorTop())
		{
			return y - computeHiddenOffset(target, false);
		}

		if (manager.isAnchorBottom())
		{
			return y + computeHiddenOffset(target, true);
		}

		return y;
	}
}