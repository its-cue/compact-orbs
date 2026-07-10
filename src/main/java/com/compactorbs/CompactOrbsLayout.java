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

package com.compactorbs;

import com.compactorbs.CompactOrbsConstants.Layout;
import com.compactorbs.widget.layout.slot.Slot;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import net.runelite.api.widgets.Widget;

@Getter
public enum CompactOrbsLayout
{
	VERTICAL(
		"Vertical",
		Layout.Vertical.LAYOUT_ID,
		Layout.Original.MAP_CONTAINER_WIDTH - Layout.Vertical.MAP_CONTAINER_WIDTH,
		0,
		List.of(Slot.XP_SLOT, Slot.WORLD_MAP_SLOT, Slot.STORE_SLOT, Slot.ACTIVITY_SLOT),
		List.of(Slot.HP_SLOT, Slot.PRAYER_SLOT, Slot.RUN_SLOT, Slot.SPEC_SLOT, Slot.WIKI_SLOT)
	),
	HORIZONTAL(
		"Horizontal",
		Layout.Horizontal.LAYOUT_ID,
		0,
		Layout.Original.MAP_CONTAINER_HEIGHT - Layout.Horizontal.MAP_CONTAINER_HEIGHT,
		List.of(Slot.STORE_SLOT, Slot.HP_SLOT, Slot.RUN_SLOT),
		List.of(Slot.ACTIVITY_SLOT, Slot.PRAYER_SLOT, Slot.SPEC_SLOT)
	),
	HORIZONTAL_WIDE(
		"Horizontal-Wide",
		Layout.HorizontalWide.LAYOUT_ID,
		0,
		Layout.Original.MAP_CONTAINER_HEIGHT - Layout.HorizontalWide.MAP_CONTAINER_HEIGHT,
		List.of(),
		List.of(Slot.HP_SLOT, Slot.PRAYER_SLOT, Slot.RUN_SLOT, Slot.SPEC_SLOT)
	);

	//config drop down name
	private final String name;

	//layout index used to determine which modified values should be used
	private final int index;

	//list of orbs in relation to TOP row or LEFT column
	private final List<Slot> a;

	//list of orbs in relation to BOTTOM row or RIGHT column
	private final List<Slot> b;

	//offsets used to shift container positions to the relative anchor
	private final int rightOffset;
	private final int bottomOffset;

	private final Map<Slot, List<Slot>> lookup;

	CompactOrbsLayout(String name, int index, int rightOffset, int bottomOffset, List<Slot> a, List<Slot> b)
	{
		this.name = name;
		this.index = index;
		this.a = a;
		this.b = b;
		this.rightOffset = rightOffset;
		this.bottomOffset = bottomOffset;

		Map<Slot, List<Slot>> map = new EnumMap<>(Slot.class);
		for (Slot slot : a)
		{
			map.put(slot, a);
		}

		for (Slot slot : b)
		{
			map.put(slot, b);
		}

		this.lookup = Collections.unmodifiableMap(map);
	}

	@Override
	public String toString()
	{
		return name;
	}

	public List<Slot> getGroup(Slot slot)
	{
		return lookup.getOrDefault(slot, List.of());
	}

	public boolean isLastVisible(Slot slot, int hiddenCount)
	{
		List<Slot> group = getGroup(slot);
		return hiddenCount == group.indexOf(slot) && hiddenCount > 0;
	}

	//TODO
	public int getSlotDimension(Widget widget)
	{
		if (isHorizontal() || isHorizontalWide())
		{
			return widget.getOriginalWidth();
		}
		else
		{
			return widget.getOriginalHeight();
		}
	}

	public boolean isVertical()
	{
		return this == VERTICAL;
	}

	public boolean isHorizontal()
	{
		return this == HORIZONTAL;
	}

	public boolean isHorizontalWide()
	{
		return this == HORIZONTAL_WIDE;
	}
}
