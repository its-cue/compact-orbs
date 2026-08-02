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
		MINIMAP,
		DETACHED_MINIMAP
	}

	//hidden from the config panel
	@ConfigItem(
		keyName = ConfigKeys.MINIMAP,
		name = "Hide minimap",
		description = "",
		hidden = true
	)
	default boolean hideMinimap()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.COMPASS,
		name = "Hide compass",
		description = "",
		hidden = true
	)
	default boolean hideCompass()
	{
		return false;
	}

	//visible on the config panel
	@ConfigItem(
		keyName = ConfigKeys.ORB_LAYOUT,
		name = "Layout type",
		description = "Select the desired layout when the minimap is minimized",
		position = 0
	)
	default CompactOrbsLayout layout()
	{
		return CompactOrbsLayout.VERTICAL;
	}

	@ConfigSection(
		name = "Layout Options",
		description = "",
		position = 1
	)
	String layout = "layout";

	@ConfigItem(
		keyName = ConfigKeys.ENABLE_NO_CLICKTHROUGH,
		name = "Prevent orb clickthrough",
		description = "When enabled, this option will prevent clicks from passing through the data orbs",
		section = layout,
		position = 0
	)
	default boolean enableNoClickthrough()
	{
		return false;
	}

	@Range(min = 0, max = 200)
	@ConfigItem(
		keyName = ConfigKeys.VERTICAL_Y_ADJUSTMENT,
		name = "Vertical offset",
		description = "Reduce the vertical position of the current layout by the set value (min: 0, max:200)",
		section = layout,
		position = 1
	)
	default int verticalYAdjustment()
	{
		return 0;
	}

	@ConfigItem(
		keyName = ConfigKeys.VERTICAL_ANCHOR,
		name = "Vertical anchor",
		description = "Where the layout will snap to, and the direction orbs move while reordering",
		section = layout,
		position = 2
	)
	default VerticalAnchor verticalAnchor()
	{
		return VerticalAnchor.BOTTOM;
	}

	@ConfigItem(
		keyName = ConfigKeys.HORIZONTAL_ANCHOR,
		name = "Horizontal anchor",
		description = "Where the layout will snap to, and the direction orbs move while reordering",
		section = layout,
		position = 3
	)
	default HorizontalAnchor horizontalAnchor()
	{
		return HorizontalAnchor.RIGHT;
	}

	@ConfigItem(
		keyName = ConfigKeys.DISABLE_REORDERING,
		name = "Disable orb reordering",
		description = "Disable the reordering logic when orbs are hidden",
		section = layout,
		position = 4
	)
	default boolean disableReordering()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.LEAVE_EMPTY_SPACE,
		name = "Leave empty space",
		description = "Prevent other elements from shifting towards orbs that have been reordered (retains the gap between)",
		section = layout,
		position = 5
	)
	default boolean leaveEmptySpace()
	{
		return false;
	}

	@ConfigSection(
		name = "Toggle Button",
		description = "",
		closedByDefault = true,
		position = 2
	)
	String button = "button";

	@ConfigItem(
		keyName = ConfigKeys.MINIMAP_TOGGLE_BUTTON,
		name = "Hide the toggle button",
		description = "Hide/show the minimap toggle button",
		section = button,
		position = 0
	)
	default boolean hideMinimapToggle()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.RIGHT_CLICK_TOGGLE_BUTTONS,
		name = "Right click the toggle button",
		description = "Deprioritizes the toggle menu so it requires a right-click to interact with",
		section = button,
		position = 1
	)
	default boolean rightClickToggleButtons()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.MINIMAP_BUTTON_PLACEMENT,
		name = "Toggle location",
		description = "Select the desired location of the toggle button while the minimap is visible",
		section = button,
		position = 2
	)
	default TogglePlacement minimapTogglePlacement()
	{
		return TogglePlacement.DEFAULT;
	}

	@ConfigSection(
		name = "Hotkey",
		description = "",
		closedByDefault = true,
		position = 3
	)
	String hotkey = "hotkey";

	@ConfigItem(
		keyName = ConfigKeys.HOTKEY_KEYBIND,
		name = "Keybind",
		description = "Keybind used to toggle the option selected option <br>" +
			"-Warning: a modified is recommended (e.g. Shift, Ctrl, Alt)",
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
		description = "Select what the hotkey will control",
		section = hotkey,
		position = 1
	)
	default HotkeyOptions toggleOption()
	{
		return HotkeyOptions.MINIMAP_BUTTON;
	}

	@ConfigSection(
		name = "Orb Swapping",
		description = "",
		closedByDefault = true,
		position = 3
	)
	String swapping = "swapping";

	@ConfigItem(
		keyName = ConfigKeys.ENABLE_ORB_SWAPPING,
		name = "Enable orb swapping",
		description = "Data orbs can be swapped via edit-mode or the below configs",
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
		description = "",
		closedByDefault = true,
		position = 5
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
		name = "Detached Minimap",
		description = "",
		closedByDefault = true,
		position = 99
	)
	String minimapOverlay = "minimapOverlay";

	@ConfigItem(
		keyName = ConfigKeys.ENABLE_MINIMAP_OVERLAY,
		name = "Show while in compact view",
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
		description = "Show a functional Logout-X on the detached minimap (only works in resizable-modern) <br>",
		section = minimapOverlay,
		position = 2
	)
	default boolean showOverlayLogoutX()
	{
		return false;
	}

}
