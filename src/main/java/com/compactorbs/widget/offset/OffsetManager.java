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

package com.compactorbs.widget.offset;

import com.compactorbs.CompactOrbsManager;
import com.compactorbs.util.ValueKey;
import com.compactorbs.widget.slot.SlotManager;
import net.runelite.api.widgets.Widget;

public class OffsetManager
{
	public static int getTargetOffset(Widget widget, ValueKey valueKey, int value, boolean compactLayout, CompactOrbsManager manager, SlotManager slotManager)
	{
		OffsetTarget offsetTarget = getTarget(widget);
		if (offsetTarget == null)
		{
			return getDefault(valueKey, value, compactLayout, manager.verticalOffset, manager.horizontalOffset);
		}

		return getOffset(offsetTarget, valueKey, value, compactLayout, manager, slotManager);
	}

	private static OffsetTarget getTarget(Widget widget)
	{
		Offsets offsets = Offsets.fromWidget(widget);
		return offsets != null ? offsets.offsetTarget() : null;
	}

	private static int getDefault(ValueKey valueKey, int value, boolean compactLayout, int xOffset, int yOffset)
	{
		if (!compactLayout)
		{
			return value;
		}
		return value + (valueKey == ValueKey.X ? xOffset : yOffset);
	}

	private static int getOffset(OffsetTarget target, ValueKey key, int value, boolean compactLayout, CompactOrbsManager manager, SlotManager slotManager)
	{
		return key == ValueKey.X
			? target.xOffset(value, compactLayout, manager, slotManager)
			: target.yOffset(value, compactLayout, manager, slotManager);
	}
}
