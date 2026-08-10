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
import com.compactorbs.widget.layout.HideOrbConfig;
import com.compactorbs.widget.layout.HideOrbRegistry;
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

	@Inject
	private HideOrbRegistry hideConfig;

	public enum SlotLayoutMode
	{
		COMPACT,
		VANILLA
	}

	@Getter
	private SlotLayoutMode currentLayoutMode;

	@Getter
	private int hiddenCountAbove;

	private final EnumMap<SlotLayoutMode, SlotLayout> layouts = new EnumMap<>(SlotLayoutMode.class);

	public void clear()
	{
		layouts.clear();
		currentLayoutMode = null;
	}

	public void init()
	{
		clear();

		for (SlotLayoutMode mode : SlotLayoutMode.values())
		{
			layouts.put(mode, new SlotLayout());
		}

		update();

		remapOrbPositions();
	}

	public void update()
	{
		updateCurrentLayoutMode();
		updateLayouts();
	}

	public void updateCurrentLayoutMode()
	{
		currentLayoutMode = manager.isCompactLayout()
			? SlotLayoutMode.COMPACT
			: SlotLayoutMode.VANILLA;
	}

	private void updateLayouts()
	{
		for (SlotLayoutMode mode : SlotLayoutMode.values())
		{
			SlotLayout layout = getLayout(mode);

			for (Slot slot : Slot.values())
			{
				TargetWidget target = manager.enableOrbSwapping
					? registry.resolve(slot, mode, config)
					: slot.getDefaultTarget();

				layout.set(slot, target);
			}
		}
	}

	private SlotLayout getLayout(SlotLayoutMode mode)
	{
		return layouts.get(mode);
	}

	private SlotLayout getCurrentLayout()
	{
		return getLayout(currentLayoutMode);
	}

	private TargetWidget getTarget(Slot slot)
	{
		return getCurrentLayout().get(slot);
	}

	public Slot findSlot(TargetWidget target)
	{
		for (Slot slot : Slot.values())
		{
			if (getTarget(slot) == target)
			{
				return slot;
			}
		}

		return null;
	}

	public void swap(TargetWidget first, TargetWidget second)
	{
		Slot firstSlot = findSlot(first);
		Slot secondSlot = findSlot(second);

		if (firstSlot == null || secondSlot == null)
		{
			return;
		}

		getCurrentLayout().swap(firstSlot, secondSlot);

		registry.save(currentLayoutMode, getCurrentLayout());

		remapOrbPositions();
	}

	private void remapOrbPositions()
	{
		if (!manager.isLoggedIn())
		{
			return;
		}

		widgetManager.remapTargets(Orbs.SWAPPABLE_ORBS);
	}

	private int getHiddenOffset(TargetWidget target, boolean isBelow)
	{
		if (target == null || !manager.allowReordering())
		{
			return 0;
		}

		Slot slot = findSlot(target);
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
			? getHiddenSize(group, targetIndex + 1, group.size())
			: getHiddenSize(group, 0, targetIndex);
	}

	private int getHiddenSize(List<Slot> group, int start, int end)
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

			if (isHidden(slot))
			{
				total += getTargetDimension(slot);
				hiddenCountAbove++;
			}
		}

		return total;
	}

	private boolean isHidden(Slot slot)
	{
		TargetWidget target = getTarget(slot);
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

		HideOrbConfig toggle = hideConfig.getByTarget(target);
		return toggle != null && toggle.getGetter().get();
	}

	private int getTargetDimension(Slot slot)
	{
		TargetWidget target = getTarget(slot);

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

		return widget.getOriginalHeight();
	}

	public int getHiddenSize()
	{
		if (config.leaveEmptySpace() || config.disableReordering() || manager.isEditingLayout)
		{
			return 0;
		}

		CompactOrbsLayout layout = manager.getCurrentLayout();
		return Math.min(
			getHiddenSize(layout.getA()),
			getHiddenSize(layout.getB())
		);
	}

	private int getHiddenSize(List<Slot> columnOrRow)
	{
		int total = 0;

		for (Slot slot : columnOrRow)
		{
			if (slot == Slot.WIKI_SLOT)
			{
				continue;
			}

			if (isHidden(slot))
			{
				total += getTargetDimension(slot);
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
			return x - getHiddenOffset(target, false);
		}

		if (manager.isAnchorRight())
		{
			return x + getHiddenOffset(target, true);
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
			return y - getHiddenOffset(target, false);
		}

		if (manager.isAnchorBottom())
		{
			return y + getHiddenOffset(target, true);
		}

		return y;
	}
}