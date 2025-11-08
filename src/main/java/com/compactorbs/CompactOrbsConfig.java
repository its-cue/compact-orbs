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

import static com.compactorbs.CompactOrbsConstants.ConfigGroup.GROUP_NAME;
import com.compactorbs.CompactOrbsConstants.ConfigKeys;
import java.awt.event.KeyEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Keybind;

@ConfigGroup(GROUP_NAME)
public interface CompactOrbsConfig extends Config
{
	@Getter
	@RequiredArgsConstructor
	enum OrbLayout
	{
		VERTICAL(0),
		HORIZONTAL(1);

		private final int index;
	}

	enum VerticalPosition
	{
		LEFT, RIGHT;
	}

	enum HorizontalPosition
	{
		TOP, BOTTOM;
	}

	@ConfigSection(
		name = "Compact Settings",
		description = "Options to modify the layout and toggle buttons",
		position = 0
	)
	String compact = "compact";

	@ConfigItem(
		keyName = ConfigKeys.MINIMAP,
		name = "Hide minimap",
		description = "Enable the ability to collapse the minimap to reposition the orbs",
		hidden = true
	)
	default boolean hideMinimap()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.COMPASS,
		name = "Hide compass",
		description = "Enable the ability to hide the compass, only when the minimap is hidden",
		hidden = true
	)
	default boolean hideCompass()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HOTKEY_TOGGLE,
		name = "Hotkey",
		description = "Toggle the visibility of the in-game toggle buttons via hotkey",
		section = compact,
		position = 1
	)
	default Keybind toggleButtonHotkey()
	{
		return new Keybind(KeyEvent.VK_INSERT, KeyEvent.SHIFT_DOWN_MASK);
	}

	@ConfigItem(
		keyName = ConfigKeys.MINIMAP_TOGGLE_BUTTON,
		name = "Hide minimap button",
		description = "Toggle the visibility of toggle button for the minimap",
		section = compact,
		position = 2
	)
	default boolean hideMinimapToggle()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.COMPASS_TOGGLE_BUTTON,
		name = "Hide compass button",
		description = "Toggle the visibility of toggle button for the compass",
		section = compact,
		position = 3
	)
	default boolean hideCompassToggle()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.ORB_LAYOUT,
		name = "Layout",
		description = "Switch between a compact vertical or horizontal layout",
		section = compact,
		position = 4
	)
	default OrbLayout layout()
	{
		return OrbLayout.VERTICAL;
	}

	@ConfigItem(
		keyName = ConfigKeys.HORIZONTAL,
		name = "Horizontal position",
		description = "Draw orbs from the top or bottom when using the compact horizontal layout",
		section = compact,
		position = 5
	)
	default HorizontalPosition horizontalPosition()
	{
		return HorizontalPosition.BOTTOM;
	}

	@ConfigItem(
		keyName = ConfigKeys.VERTICAL,
		name = "Vertical position",
		description = "Draw orbs from left or right when using the compact vertical layout",
		section = compact,
		position = 6
	)
	default VerticalPosition verticalPosition()
	{
		return VerticalPosition.RIGHT;
	}

	@ConfigItem(
		keyName = ConfigKeys.HIDE_HP,
		name = "Hide Hp",
		description = "Toggle visibility of the HP orb",
		position = 1
	)
	default boolean hideHp()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HIDE_PRAYER,
		name = "Hide Prayer",
		description = "Toggle visibility of the Prayer orb",
		position = 2
	)
	default boolean hidePray()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HIDE_RUN,
		name = "Hide Run",
		description = "Toggle visibility of the Run energy orb",
		position = 3
	)
	default boolean hideRun()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HIDE_SPEC,
		name = "Hide Special",
		description = "Toggle visibility of the Special attack energy orb",
		position = 4
	)
	default boolean hideSpec()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HIDE_XP,
		name = "Hide XP",
		description = "Toggle visibility of the XP drops orb",
		position = 5
	)
	default boolean hideXp()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HIDE_WORLD,
		name = "Hide World Map",
		description = "Toggle the visibility of the World Map <br>"
			+ " Will retain hotkey functionality 'Ctrl + M', if in-game setting is enabled",
		position = 6
	)
	default boolean hideWorld()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HIDE_ACTIVITY,
		name = "Hide Activity Advisor",
		description = "Toggle the visibility of the Activity Advisor orb <br>"
			+ "In-game setting must be be enabled",
		position = 7
	)
	default boolean hideActivity()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HIDE_STORE,
		name = "Hide Store",
		description = "Toggle the visibility of the Store orb <br>"
			+ "In-game setting must be be enabled",
		position = 8
	)
	default boolean hideStore()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HIDE_WIKI,
		name = "Hide Wiki banner",
		description = "Toggle the visibility of the Wiki banner <br>"
			+ "In-game setting must be be enabled",
		position = 9
	)
	default boolean hideWiki()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HIDE_LOGOUT_X,
		name = "Hide Logout X",
		description = "Toggle the visibility of the Logout-X when in resizable-modern display mode",
		position = 10
	)
	default boolean hideLogout()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HIDE_GRID,
		name = "Hide Grid Master",
		description = "Toggle the visibility of the Grid Master orb",
		position = 11
	)
	default boolean hideGrid()
	{
		return false;
	}

}
