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
import net.runelite.client.config.Range;

@ConfigGroup(GROUP_NAME)
public interface CompactOrbsConfig extends Config
{
	enum HorizontalAnchor
	{
		LEFT, RIGHT;

		public boolean isLeft()
		{
			return this == LEFT;
		}

		public boolean isRight()
		{
			return this == RIGHT;
		}
	}

	enum VerticalAnchor
	{
		TOP, BOTTOM;

		public boolean isTop()
		{
			return this == TOP;
		}

		public boolean isBottom()
		{
			return this == BOTTOM;
		}
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
		BELOW_X(Layout.BELOW_X_MINIMAP_BUTTON_X, Layout.BELOW_X_MINIMAP_BUTTON_Y);

		private final int x;
		private final int y;
	}

	@Getter
	enum HotkeyOptions
	{
		MINIMAP_BUTTON,
		COMPASS_BUTTON,
		BOTH_BUTTONS,
		MINIMAP,
		DETACHED_MINIMAP
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
		name = "Layout",
		description = "Options for modifying the layouts and toggle buttons",
		position = 0
	)
	String compact = "compact";

	@ConfigItem(
		keyName = ConfigKeys.MINIMAP_BUTTON_PLACEMENT,
		name = "Toggle location",
		description = "Select where the minimap toggle button should be while not in compact view <br>" +
			"-Default: bottom right corner, below the minimap <br>" +
			"-Above Xp: above the Xp orb <br>" +
			"-Below Map: next to the Store orb, centered below the minimap <br>" +
			"-Below X: slightly below the Logout-X"
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
		name = "Disable the minimap button",
		description = "Hide/show the minimap toggle button",
		section = compact,
		position = 2
	)
	default boolean hideMinimapToggle()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.COMPASS_TOGGLE_BUTTON,
		name = "Disable the compass button",
		description = "Hide/show the compass toggle button",
		section = compact,
		position = 3
	)
	default boolean hideCompassToggle()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.RIGHT_CLICK_TOGGLE_BUTTONS,
		name = "Right click the toggle buttons",
		description = "Deprioritizes the toggle menu action so it requires a right click",
		section = compact,
		position = 4
	)
	default boolean rightClickToggleButtons()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.ORB_LAYOUT,
		name = "Current Layout",
		description = "The visible layout in compact view <br>" +
			"-Vertical: orbs stacked vertically & split between 2 columns (default) <br>" +
			"-Horizontal: orbs stacked and split between 2 rows horizontally <br>" +
			"-Horizontal-Wide: data orbs aligned to 1 row with remaining orbs/compass stacked above horizontally",
		section = compact,
		position = 5
	)
	default CompactOrbsLayout layout()
	{
		return CompactOrbsLayout.VERTICAL;
	}

	@ConfigItem(
		keyName = ConfigKeys.VERTICAL_ANCHOR,
		name = "Vertical anchor",
		description = "Snaps the layout and reorders orbs based on the selected anchor point",
		section = compact,
		position = 6
	)
	default VerticalAnchor verticalAnchor()
	{
		return VerticalAnchor.BOTTOM;
	}

	@ConfigItem(
		keyName = ConfigKeys.HORIZONTAL_ANCHOR,
		name = "Horizontal anchor",
		description = "Snaps the layout and reorders orbs based on the selected anchor point",
		section = compact,
		position = 7
	)
	default HorizontalAnchor horizontalAnchor()
	{
		return HorizontalAnchor.RIGHT;
	}

	@Range(min = 0, max = 200)
	@ConfigItem(
		keyName = ConfigKeys.VERTICAL_Y_ADJUSTMENT,
		name = "Vertical offset",
		description = "Adjust the layouts vertical position from the Bottom anchor point by the set value",
		section = compact,
		position = 8
	)
	default int verticalYAdjustment()
	{
		return 0;
	}

	@ConfigItem(
		keyName = ConfigKeys.DISABLE_REORDERING,
		name = "Disable orb reordering",
		description = "Disable reordering logic tied to orb visibility",
		section = compact,
		position = 9
	)
	default boolean disableReordering()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.LEAVE_EMPTY_SPACE,
		name = "Leave empty space",
		description = "Leaves empty space where orbs were moved instead of shifting other elements to fill the gap",
		section = compact,
		position = 10
	)
	default boolean leaveEmptySpace()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.ENABLE_NO_CLICKTHROUGH,
		name = "Prevent orb clickthrough",
		description = "Prevents clicks through the data orbs (slightly increases the non-clickable space around them)",
		section = compact,
		position = 11
	)
	default boolean enableNoClickthrough()
	{
		return false;
	}

	@ConfigSection(
		name = "Hotkey",
		description = "Hotkey settings",
		closedByDefault = true,
		position = 1
	)
	String hotkey = "hotkey";

	@ConfigItem(
		keyName = ConfigKeys.HOTKEY_KEYBIND,
		name = "Keybind",
		description = "Keybind used to toggle the option below <br>" +
			"-Warning: should use a modifier (e.g. Shift, Ctrl, Alt)",
		section = hotkey,
		position = 0
	)
	default Keybind hotkeyKeybind()
	{
		return new Keybind(KeyEvent.VK_INSERT, KeyEvent.SHIFT_DOWN_MASK);
	}

	@ConfigItem(
		keyName = ConfigKeys.HOTKEY_TOGGLE_OPTION,
		name = "Select toggle",
		description = "Select what the hotkey will hide/show <br>" +
			"-Minimap button: the minimap toggle button <br>" +
			"-Compass button: the compass toggle button <br>" +
			"-Both Buttons: both the toggle buttons  <br>" +
			"-Minimap: the minimap (toggling between compact view) <br>" +
			"-Detached Minimap: the detached minimap when in compact view",
		section = hotkey,
		position = 1
	)
	default HotkeyOptions toggleOption()
	{
		return HotkeyOptions.BOTH_BUTTONS;
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
		description = "Swap orbs based on the config below",
		section = swapping,
		position = 0
	)
	default boolean enableOrbSwapping()
	{
		return false;
	}

	@ConfigItem(
		keyName = "",
		name = "Compact slots:",
		description = "Slot order when in compact-view <br>" +
			"-Warning: each slot must contain a unique orb (otherwise slots will be reset)",
		section = swapping,
		position = 1
	)
	default void compactHeader()
	{
	}

	@ConfigItem(
		keyName = ConfigKeys.HP_ORB_SLOT,
		name = "  HP",
		description = "Select an orb to be in this slot",
		section = swapping,
		position = 2
	)
	default FilteredOrb orbInHPSlot()
	{
		return FilteredOrb.HP_ORB_CONTAINER;
	}

	@ConfigItem(
		keyName = ConfigKeys.PRAYER_ORB_SLOT,
		name = "  Prayer",
		description = "Select an orb to be in this slot",
		section = swapping,
		position = 3
	)
	default FilteredOrb orbInPrayerSlot()
	{
		return FilteredOrb.PRAYER_ORB_CONTAINER;
	}

	@ConfigItem(
		keyName = ConfigKeys.RUN_ORB_SLOT,
		name = "  Run",
		description = "Select an orb to be in this slot",
		section = swapping,
		position = 4
	)
	default FilteredOrb orbInRunSlot()
	{
		return FilteredOrb.RUN_ORB_CONTAINER;
	}

	@ConfigItem(
		keyName = ConfigKeys.SPECIAL_ORB_SLOT,
		name = "  Special",
		description = "Select an orb to be in this slot",
		section = swapping,
		position = 5
	)
	default FilteredOrb orbInSpecialSlot()
	{
		return FilteredOrb.SPEC_ORB_CONTAINER;
	}

	@ConfigItem(
		keyName = "",
		name = "\u200B",
		description = "",
		section = swapping,
		position = 6
	)
	default void slotDivider()
	{
	}

	@ConfigItem(
		keyName = "",
		name = "Vanilla slots:",
		description = "Slot order when the minimap is visible (supports Fixed mode) <br>" +
			"-Warning: each slot must contain a unique orb (otherwise slots will be reset)",
		section = swapping,
		position = 7
	)
	default void vanillaHeader()
	{
	}

	@ConfigItem(
		keyName = ConfigKeys.HP_ORB_SLOT_VANILLA,
		name = "  HP",
		description = "Select an orb to be in this slot",
		section = swapping,
		position = 8
	)
	default FilteredOrb orbInHpSlotVanilla()
	{
		return FilteredOrb.HP_ORB_CONTAINER;
	}

	@ConfigItem(
		keyName = ConfigKeys.PRAYER_ORB_SLOT_VANILLA,
		name = "  Prayer",
		description = "Select an orb to be in this slot",
		section = swapping,
		position = 9
	)
	default FilteredOrb orbInPrayerSlotVanilla()
	{
		return FilteredOrb.PRAYER_ORB_CONTAINER;
	}

	@ConfigItem(
		keyName = ConfigKeys.RUN_ORB_SLOT_VANILLA,
		name = "  Run",
		description = "Select an orb to be in this slot",
		section = swapping,
		position = 10
	)
	default FilteredOrb orbInRunSlotVanilla()
	{
		return FilteredOrb.RUN_ORB_CONTAINER;
	}

	@ConfigItem(
		keyName = ConfigKeys.SPECIAL_ORB_SLOT_VANILLA,
		name = "  Special",
		description = "Select an orb to be in this slot",
		section = swapping,
		position = 11
	)
	default FilteredOrb orbInSpecialSlotVanilla()
	{
		return FilteredOrb.SPEC_ORB_CONTAINER;
	}

	@ConfigSection(
		name = "Orb Visibility",
		description = "Options to hide or show orbs",
		closedByDefault = true,
		position = 3
	)
	String visibility = "visibility";

	@ConfigItem(
		keyName = ConfigKeys.HIDE_HP,
		name = "Hide Hp",
		description = "Hide/show the Hp orb",
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
		description = "Hide/show the Prayer orb",
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
		description = "Hide/show the Run orb",
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
		description = "Hide/show the Special orb",
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
		description = "Hide/show the Xp orb",
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
		description = "Hide/show the World map",
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
		description = "Hide/show the Store orb (should reflect the in-game setting)",
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
		description = "Hide/show the Activity Advisor orb (should reflect the in-game setting)",
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
		description = "Hide/show the Wiki banner (should reflect the in-game setting) <br>" +
			"Warning: if enabled, the Wiki plugins banner will also be hidden regardless of the plugins own config",
		section = visibility,
		position = 8
	)
	default boolean hideWiki()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HIDE_LOGOUT_X,
		name = "Hide Logout-X",
		description = "Hide/show the Logout-X (only in resizable-modern)",
		section = visibility,
		position = 9
	)
	default boolean hideLogout()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HIDE_GRID,
		name = "Hide Grid Master (Legacy)",
		description = "Hide/show the Grid Master orb (temporary game-mode)",
		section = visibility,
		position = 10
	)
	default boolean hideGrid()
	{
		return false;
	}

	@ConfigSection(
		name = "Minimap Overlay",
		description = "Options for the detached minimap",
		closedByDefault = true,
		position = 99
	)
	String minimapOverlay = "minimapOverlay";

	@ConfigItem(
		keyName = ConfigKeys.ENABLE_MINIMAP_OVERLAY,
		name = "Show minimap in compact view",
		description = "Show a functional minimap that is detached from the orbs while in compact view <br>" +
			"Warning: this minimap is not supported by plugins that display overlays on the minimap (names, marker tiles, lines, etc.)",
		section = minimapOverlay,
		position = 0
	)
	default boolean showMinimapInCompactView()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.ENABLE_OVERLAY_TOGGLE_OPTION,
		name = "Show toggle on the minimap button",
		description = "Display an option on the minimap button to hide/show the detached minimap",
		section = minimapOverlay,
		position = 1
	)
	default boolean showToggleOnMinimapButton()
	{
		return true;
	}

	@ConfigItem(
		keyName = ConfigKeys.ENABLE_LOGOUT_X_OVERLAY,
		name = "Show Logout-X",
		description = "Show a functional Logout-X on the detached minimap (only in resizable-modern) <br>",
		section = minimapOverlay,
		position = 2
	)
	default boolean showOverlayLogoutX()
	{
		return false;
	}

}
