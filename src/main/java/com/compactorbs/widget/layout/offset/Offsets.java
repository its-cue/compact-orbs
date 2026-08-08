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

package com.compactorbs.widget.layout.offset;

import com.compactorbs.widget.TargetWidget;
import com.compactorbs.widget.elements.Button;
import com.compactorbs.widget.elements.Minimap;
import com.compactorbs.widget.elements.Orbs;
import com.compactorbs.widget.layout.offset.impl.ActivityOrbOffset;
import com.compactorbs.widget.layout.offset.impl.CompassOffset;
import com.compactorbs.widget.layout.offset.impl.HPOrbOffset;
import com.compactorbs.widget.layout.offset.impl.LogoutXOffset;
import com.compactorbs.widget.layout.offset.impl.MapContainerOffset;
import com.compactorbs.widget.layout.offset.impl.MinimapButtonOffset;
import com.compactorbs.widget.layout.offset.impl.OrbContainerOffset;
import com.compactorbs.widget.layout.offset.impl.PrayerOrbOffset;
import com.compactorbs.widget.layout.offset.impl.RunOrbOffset;
import com.compactorbs.widget.layout.offset.impl.SpecOrbOffset;
import com.compactorbs.widget.layout.offset.impl.StoreOrbOffset;
import com.compactorbs.widget.layout.offset.impl.WikiContainerOffset;
import com.compactorbs.widget.layout.offset.impl.WorldMapOffset;
import com.compactorbs.widget.layout.offset.impl.XPOrbOffset;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import net.runelite.api.widgets.Widget;

@Getter
public enum Offsets
{
	WIKI_CONTAINER(new WikiContainerOffset(),
		Orbs.WIKI_ICON_CONTAINER
	),
	LOGOUT(new LogoutXOffset(),
		Orbs.LOGOUT_X_ICON,
		Orbs.LOGOUT_X_STONE
	),
	HP(new HPOrbOffset(),
		Orbs.HP_ORB_CONTAINER
	),
	PRAYER(new PrayerOrbOffset(),
		Orbs.PRAYER_ORB_CONTAINER
	),
	RUN(new RunOrbOffset(),
		Orbs.RUN_ORB_CONTAINER
	),
	SPEC(new SpecOrbOffset(),
		Orbs.SPEC_ORB_CONTAINER
	),
	STORE(new StoreOrbOffset(),
		Orbs.STORE_ORB_CONTAINER
	),
	ACTIVITY(new ActivityOrbOffset(),
		Orbs.ACTIVITY_ORB_CONTAINER
	),
	XP(new XPOrbOffset(),
		Orbs.XP_DROPS_CONTAINER
	),
	WORLD_MAP(new WorldMapOffset(),
		Orbs.WORLD_MAP_CONTAINER
	),
	//top level container
	MAP_CONTAINER(new MapContainerOffset(),
		Minimap.CLASSIC_MAP_CONTAINER,
		Minimap.MODERN_MAP_CONTAINER
	),
	//minimap / compass container
	MAP_MINIMAP(new CompassOffset(),
		Minimap.CLASSIC_MAP_MINIMAP,
		Minimap.MODERN_MAP_MINIMAP
	),
	//orb parent container
	ORB_CONTAINER(new OrbContainerOffset(),
		Minimap.CLASSIC_ORBS_CONTAINER,
		Minimap.MODERN_ORBS_CONTAINER
	),
	MINIMAP_BUTTON(new MinimapButtonOffset(),
		Button.MINIMAP_BUTTON_CLASSIC,
		Button.MINIMAP_BUTTON_MODERN,
		Button.MINIMAP_BUTTON_FIXED
	);

	private final OffsetTarget offset;
	private final TargetWidget[] targets;

	Offsets(OffsetTarget offsetTarget, TargetWidget... targets)
	{
		this.offset = offsetTarget;
		this.targets = targets;
	}

	private static final Map<String, Offsets> BY_WIDGET_ID = new HashMap<>();

	static
	{
		for (Offsets key : values())
		{
			for (TargetWidget target : key.targets)
			{
				String id = target.getComponentId() + "_" + target.getArrayId();
				BY_WIDGET_ID.put(id, key);
			}
		}
	}

	public static Offsets get(Widget widget, int index)
	{
		String id = widget.getId() + "_" + index;
		return BY_WIDGET_ID.get(id);
	}
}
