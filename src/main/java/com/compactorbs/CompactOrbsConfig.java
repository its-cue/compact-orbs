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
import com.compactorbs.CompactOrbsConstants.Layout;
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
		LEFT, RIGHT
	}

	enum HorizontalPosition
	{
		TOP, BOTTOM
	}

	//limit which slots can be swapped
	@RequiredArgsConstructor
	enum FilteredOrb
	{
		//must match Orbs enum naming,
		//ex: Orbs.HP_ORB_CONTAINER == FilteredOrb.HP_ORB_CONTAINER
		HP_ORB_CONTAINER("Hp"),
		PRAYER_ORB_CONTAINER("Prayer"),
		RUN_ORB_CONTAINER("Run"),
		SPEC_ORB_CONTAINER("Special");

		private final String name;

		@Override
		public String toString()
		{
			return name;
		}
	}

	@Getter
	@RequiredArgsConstructor
	enum TogglePlacement
	{
		DEFAULT(Layout.DEFAULT_MINIMAP_BUTTON_X, Layout.DEFAULT_MINIMAP_BUTTON_Y),
		ABOVE_XP(Layout.ABOVE_XP_MINIMAP_BUTTON_X, Layout.ABOVE_XP_MINIMAP_BUTTON_Y),
		BELOW_MAP(Layout.BELOW_MAP_MINIMAP_BUTTON_X, Layout.BELOW_MAP_MINIMAP_BUTTON_Y),
		BELOW_X(Layout.BELOW_X_MINIMAP_BUTTON_x, Layout.BELOW_X_MINIMAP_BUTTON_Y);

		private final int x;
		private final int y;
	}

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

	@ConfigSection(
		name = "Compact Layouts",
		description = "Options for modifying the layouts and toggle buttons",
		position = 0
	)
	String compact = "compact";

	@ConfigItem(
		keyName = ConfigKeys.MINIMAP_BUTTON_PLACEMENT,
		name = "Toggle location",
		description = "Change the location of the minimap toggle button when the minimap is visible <br>" +
			"DEFAULT: bottom right, below the minimap <br>" +
			"ABOVE_XP: above the XP drops orb <br>" +
			"BELOW_MAP: centered below the minimap, next to the Store orb <br>" +
			"BELOW_X: right below where the Logout X would be"
		,
		section = compact,
		position = 1
	)
	default TogglePlacement minimapTogglePlacement()
	{
		return TogglePlacement.DEFAULT;
	}

	@ConfigItem(
		keyName = ConfigKeys.MINIMAP_TOGGLE_BUTTON,
		name = "Hide minimap button",
		description = "Toggle the visibility of the toggle button for the minimap",
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
		description = "Toggle the visibility of the toggle button for the compass",
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
		name = "Horizontal direction",
		description = "Shift orbs from top-down, or bottom-up <br>"
			+ "Also dictates layouts position in the minimap container",
		section = compact,
		position = 5
	)
	default HorizontalPosition horizontalPosition()
	{
		return HorizontalPosition.BOTTOM;
	}

	//change to account for alignment, not position
	@ConfigItem(
		keyName = ConfigKeys.VERTICAL,
		name = "Vertical direction",
		description = "Shift orbs from left, or right <br>"
			+ "Also dictates layouts position in the minimap container",
		section = compact,
		position = 6
	)
	default VerticalPosition verticalPosition()
	{
		return VerticalPosition.RIGHT;
	}

	@ConfigItem(
		keyName = ConfigKeys.DISABLE_REORDERING,
		name = "Disable orb reordering",
		description = "Prevents the automatic reordering of visible orbs filling gaps left by hidden orbs <br>"
			+ "Vertical-LEFT: will reorder orbs from right to left <br>"
			+ "Vertical-RIGHT: will reorder orbs from left to right <br>"
			+ "Horizontal-TOP: will reorder orbs from top to bottom <br>"
			+ "Horizontal-BOTTOM: will reorder orbs from bottom to top <br>",
		section = compact,
		position = 7
	)
	default boolean disableReordering()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.LEAVE_EMPTY_SPACE,
		name = "Leave empty space",
		description = "Preserves the empty space between reordered orbs, and non-reordered UI elements, for a `floating` effect",
		section = compact,
		position = 8
	)
	default boolean leaveEmptySpace()
	{
		return false;
	}

	@ConfigSection(
		name = "Hotkey",
		description = "Hotkey settings",
		position = 1
	)
	String hotkey = "hotkey";

	@ConfigItem(
		keyName = ConfigKeys.HOTKEY_TOGGLE,
		name = "Hotkey",
		description = "Configurable hotkey that hides/shows the toggle-button eyes, or the minimap (with `Toggle minimap via Hotkey` enabled) <br>"
			+ "default: shift + insert",
		section = hotkey,
		position = 0
	)
	default Keybind toggleButtonHotkey()
	{
		return new Keybind(KeyEvent.VK_INSERT, KeyEvent.SHIFT_DOWN_MASK);
	}

	@ConfigItem(
		keyName = ConfigKeys.HOTKEY_MINIMAP,
		name = "Toggle minimap via Hotkey",
		description = "Repurpose the Hotkey to toggle the visibility of the minimap, without the use of the toggle button",
		section = hotkey,
		position = 1
	)
	default boolean minimapHotkey()
	{
		return false;
	}

	@ConfigSection(
		name = "Orb Swapping",
		description = "Options to swap orbs around",
		closedByDefault = true,
		position = 2
	)
	String swapping = "swapping";

	@ConfigItem(
		keyName = ConfigKeys.ENABLE_ORB_SWAPPING,
		name = "Enable orb swapping",
		description = "Enable swapping orb positions with each other <br>"
			+ "Only supports Hp, Prayer, Run, and Special orb",
		section = swapping,
		position = 0
	)
	default boolean enableOrbSwapping()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HP_ORB_SLOT,
		name = "HP slot",
		description = "Select which orb should be in the HP Orb slot",
		section = swapping,
		position = 1
	)
	default FilteredOrb orbInHPSlot()
	{
		return FilteredOrb.HP_ORB_CONTAINER;
	}

	@ConfigItem(
		keyName = ConfigKeys.PRAYER_ORB_SLOT,
		name = "Prayer slot",
		description = "Select which orb should be in the Prayer Orb slot",
		section = swapping,
		position = 2
	)
	default FilteredOrb orbInPrayerSlot()
	{
		return FilteredOrb.PRAYER_ORB_CONTAINER;
	}

	@ConfigItem(
		keyName = ConfigKeys.RUN_ORB_SLOT,
		name = "Run slot",
		description = "Select which orb should be in the Run Orb slot",
		section = swapping,
		position = 3
	)
	default FilteredOrb orbInRunSlot()
	{
		return FilteredOrb.RUN_ORB_CONTAINER;
	}

	@ConfigItem(
		keyName = ConfigKeys.SPECIAL_ORB_SLOT,
		name = "Special slot",
		description = "Select which orb should be in the Special Orb slot",
		section = swapping,
		position = 4
	)
	default FilteredOrb orbInSpecialSlot()
	{
		return FilteredOrb.SPEC_ORB_CONTAINER;
	}

	@ConfigSection(
		name = "Orb Visibility",
		description = "Options to hide or show orbs",
		position = 3
	)
	String visibility = "visibility";

	@ConfigItem(
		keyName = ConfigKeys.HIDE_HP,
		name = "Hide Hp",
		description = "Toggle visibility of the HP orb",
		section = visibility,
		position = 0
	)
	default boolean hideHp()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HIDE_PRAYER,
		name = "Hide Prayer",
		description = "Toggle visibility of the Prayer orb",
		section = visibility,
		position = 1
	)
	default boolean hidePray()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HIDE_RUN,
		name = "Hide Run",
		description = "Toggle visibility of the Run energy orb",
		section = visibility,
		position = 2
	)
	default boolean hideRun()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HIDE_SPEC,
		name = "Hide Special",
		description = "Toggle visibility of the Special attack energy orb",
		section = visibility,
		position = 3
	)
	default boolean hideSpec()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HIDE_XP,
		name = "Hide XP",
		description = "Toggle visibility of the XP drops orb",
		section = visibility,
		position = 4
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
		section = visibility,
		position = 5
	)
	default boolean hideWorld()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HIDE_STORE,
		name = "Hide Store",
		description = "Toggle the visibility of the Store orb <br>"
			+ "In-game setting must be be enabled",
		section = visibility,
		position = 6
	)
	default boolean hideStore()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HIDE_ACTIVITY,
		name = "Hide Activity Advisor",
		description = "Toggle the visibility of the Activity Advisor orb <br>"
			+ "In-game setting must be be enabled",
		section = visibility,
		position = 7
	)
	default boolean hideActivity()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HIDE_WIKI,
		name = "Hide Wiki banner",
		description = "Toggle the visibility of the Wiki banner <br>"
			+ "In-game setting must be be enabled",
		section = visibility,
		position = 8
	)
	default boolean hideWiki()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HIDE_LOGOUT_X,
		name = "Hide Logout X",
		description = "Toggle the visibility of the Logout-X when in resizable-modern display mode",
		section = visibility,
		position = 9
	)
	default boolean hideLogout()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HIDE_GRID,
		name = "Hide Grid Master",
		description = "Toggle the visibility of the Grid Master orb (temporary game-mode)",
		section = visibility,
		position = 10
	)
	default boolean hideGrid()
	{
		return false;
	}

}
