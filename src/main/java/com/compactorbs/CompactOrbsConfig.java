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
import com.compactorbs.widget.elements.Orbs;
import com.compactorbs.widget.overlay.spec.SpecTrackerOrb;
import java.awt.Color;
import java.awt.event.KeyEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.client.config.Alpha;
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
		DETACHED_MINIMAP,
		EDIT_MODE
	}

	//visible on the config panel
	@ConfigItem(
		keyName = ConfigKeys.ORB_LAYOUT,
		name = "Layout type",
		description = "Select a preferred layout type",
		position = 0
	)
	default CompactOrbsLayout layout()
	{
		//let predefined layouts be opt-in now
		return CompactOrbsLayout.FLOATING;
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
		description = "Reduce the vertical position of the current compact-layout by the set value (min: 0, max:200)",
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

	@ConfigItem(
		keyName = ConfigKeys.ENABLE_ORB_SWAPPING,
		name = "Enable orb swapping",
		description = "Will allow swapping of the main data orbs via edit-mode while enabled. Disables custom-positioning",
		section = layout,
		position = 6
	)
	default boolean enableOrbSwapping()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HIDE_MINIMAP_WITH_SIDE_PANEL,
		name = "Hide orbs with inventory tab",
		description = "Hide the layout when the side panel tab is hidden (only works in resizable-modern).",
		section = layout,
		position = 7
	)
	default boolean hideMinimapWithSidePanel()
	{
		return false;
	}

	@ConfigSection(
		name = "Special Orb QoL",
		description = "Enable and customize an additional orb that displays special attack energy or special stacks (for the Rondache or Soulreaper Axe)",
		closedByDefault = true,
		position = 2
	)
	String qol = "qol";

	@ConfigItem(
		keyName = ConfigKeys.HIDE_SPEC_ENERGY_METER,
		name = "Hide native spec energy meter",
		description = "Hide the native meter on the Special attack orb.",
		section = qol,
		position = 0
	)
	default boolean hideSpecEnergyMeter()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.SHOW_SPEC_REGEN_METER,
		name = "Show spec regen meter",
		description = "Show a ring around the special attack orb. (Disable `Regen Meters` spec. attack meter)",
		section = qol,
		position = 1
	)
	default boolean showSpecRegenMeter()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.APPLY_COLORS_TO_NATIVE_SPEC,
		name = "Apply colors to native spec orb",
		description = "Apply the colors below to the native spec orb. <br>" +
			"When the tracker orb is enabled, colors will be applied regardless for non-energy based specs.",
		section = qol,
		position = 2
	)
	default boolean applyColorsToNativeSpec()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.SHOW_SPEC_TRACKER_ORB,
		name = "Show tracker orb",
		description = "Show an additional orb that tracks spec energy state.",
		section = qol,
		position = 3
	)
	default boolean showSpecTrackerOrb()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.SHOW_SPEC_TRACKER_REGEN_METER,
		name = "Show tracker regen meter",
		description = "Show a ring around the tracker orb.",
		section = qol,
		position = 4
	)
	default boolean showTrackerOrbRegenMeter()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.PREVENT_TRACKER_ORB_CLICKTHROUGH,
		name = "Prevent tracker orb clickthrough",
		description = "Prevents clicks from passing through the tracker orb.",
		section = qol,
		position = 5
	)
	default boolean trackerOrbPreventClickThrough()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.SPEC_TRACKER_ORB_TEXT_COLOR,
		name = "Text color",
		description = "Recolor the text displayed on the tracker orb.",
		section = qol,
		position = 6
	)
	default Color specTrackerOrbTextColor()
	{
		return null;
	}

	@ConfigItem(
		keyName = ConfigKeys.SPEC_TRACKER_ORB_TEXT_SHADOW_COLOR,
		name = "Text shadow color",
		description = "Recolor the shadow/outline of the text displayed on the tracker orb.",
		section = qol,
		position = 7
	)
	default Color specTrackerOrbTextShadowColor()
	{
		return null;
	}

	@ConfigItem(
		keyName = ConfigKeys.SPEC_TRACKER_ORB_TEXT_SHADOW_STYLE,
		name = "Shadow style",
		description = "Show a drop shadow, or outline for the tracker orb text.",
		section = qol,
		position = 8
	)
	default SpecTrackerOrb.OrbTextShadowStyle specTrackerOrbTextShadowStyle()
	{
		return SpecTrackerOrb.OrbTextShadowStyle.VANILLA;
	}

	@ConfigItem(
		keyName = ConfigKeys.SPEC_TRACKER_ORB_MODE,
		name = "Orb size",
		description = "Show a vanilla sized orb or a compact version.",
		section = qol,
		position = 9
	)
	default SpecTrackerOrb.OrbDisplayMode displayMode()
	{
		return SpecTrackerOrb.OrbDisplayMode.VANILLA;
	}

	@Alpha
	@ConfigItem(
		keyName = ConfigKeys.SPEC_QOL_ORB_COLOR,
		name = "Orb color",
		description = "Recolor the orb filler for the native orb.",
		section = qol,
		position = 10
	)
	default Color specQoLOrbColor()
	{
		return null;
	}

	@Alpha
	@ConfigItem(
		keyName = ConfigKeys.SPEC_QOL_ORB_EMPTY_COLOR,
		name = "Orb empty color",
		description = "Recolor the empty background behind the orb filler for the native orb.",
		section = qol,
		position = 11
	)
	default Color specQoLOrbEmptyColor()
	{
		return null;
	}

	@Alpha
	@ConfigItem(
		keyName = ConfigKeys.SPEC_QOL_ORB_REGEN_COLOR,
		name = "Regen color",
		description = "Recolor the regen meter for the native orb.",
		section = qol,
		position = 12
	)
	default Color specQoLOrbRegenColor()
	{
		return null;
	}

	@Alpha
	@ConfigItem(
		keyName = ConfigKeys.SPEC_QOL_SRA_ORB_COLOR,
		name = "Soulreaper axe orb",
		description = "Recolor the orb filler for the Soulreaper axe.",
		section = qol,
		position = 13
	)
	default Color specQoLSraOrbColor()
	{
		return new Color(0x8D4BD3);
	}

	@Alpha
	@ConfigItem(
		keyName = ConfigKeys.SPEC_QOL_SRA_ORB_EMPTY_COLOR,
		name = "Soulreaper axe empty",
		description = "Recolor the empty background behind the filler for the Soulreaper axe.",
		section = qol,
		position = 14
	)
	default Color specQoLSraOrbEmptyColor()
	{
		return null;
	}

	@Alpha
	@ConfigItem(
		keyName = ConfigKeys.SPEC_QOL_SRA_ORB_REGEN_COLOR,
		name = "Soulreaper axe regen",
		description = "Recolor the regen meter for the Soulreaper axe.",
		section = qol,
		position = 15
	)
	default Color specQoLSraOrbRegenColor()
	{
		return new Color(0xcb45e6);
	}

	@Alpha
	@ConfigItem(
		keyName = ConfigKeys.SPEC_QOL_RONDACHE_ORB_COLOR,
		name = "Rondache orb",
		description = "Recolor the orb filler for the Rondache.",
		section = qol,
		position = 16
	)
	default Color specQoLRondacheOrbColor()
	{
		return new Color(0x5790b5);
	}

	@Alpha
	@ConfigItem(
		keyName = ConfigKeys.SPEC_QOL_RONDACHE_ORB_EMPTY_COLOR,
		name = "Rondache empty",
		description = "Recolor the empty background behind the filler for the Rondache.",
		section = qol,
		position = 17
	)
	default Color specQoLRondacheOrbEmptyColor()
	{
		return null;
	}

	@Alpha
	@ConfigItem(
		keyName = ConfigKeys.SPEC_QOL_RONDACHE_ORB_REGEN_COLOR,
		name = "Rondache meter",
		description = "Recolor the regen meter for the Rondache.",
		section = qol,
		position = 18
	)
	default Color specQoLRondacheOrbRegenColor()
	{
		return new Color(0x94CDF8);
	}

	@ConfigSection(
		name = "Toggle Button",
		description = "",
		closedByDefault = true,
		position = 3
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
		position = 4
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

	@ConfigItem(
		keyName = ConfigKeys.DETACH_UTILITY_ORBS,
		name = "Detach utility orbs",
		description = "Position the `utility` orbs around the detached minimap",
		section = minimapOverlay,
		position = 3
	)
	default boolean detachUtilityOrbs()
	{
		return false;
	}

	@ConfigSection(
		name = "Troubleshoot",
		description = "Useful if you're having issues or questions",
		closedByDefault = true,
		position = 100
	)
	String readme = "hideAndSwapUpdate";

	@ConfigItem(
		keyName = "hideAndSwapUpdate",
		name = "<html>If you're having issues, please read the plugin page for more information. <br/> <br/> " +
			"If you experience bugs or have feedback, feel free to open an issue on GitHub. <br/> <br/>" +
			" - You can access the plugin page by right-clicking the plugin name above and selecting `Support`</html>",
		description = "",
		section = readme
	)
	void readme();

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

	//orb swapping
	@ConfigItem(
		keyName = ConfigKeys.HP_ORB_SLOT,
		name = "Compact HP slot",
		description = "",
		hidden = true
	)
	default Orbs orbInHPSlot()
	{
		return Orbs.HP_ORB_CONTAINER;
	}

	@ConfigItem(
		keyName = ConfigKeys.PRAYER_ORB_SLOT,
		name = "Compact Prayer slot",
		description = "",
		hidden = true
	)
	default Orbs orbInPrayerSlot()
	{
		return Orbs.PRAYER_ORB_CONTAINER;
	}

	@ConfigItem(
		keyName = ConfigKeys.RUN_ORB_SLOT,
		name = "Compact Run slot",
		description = "",
		hidden = true
	)
	default Orbs orbInRunSlot()
	{
		return Orbs.RUN_ORB_CONTAINER;
	}

	@ConfigItem(
		keyName = ConfigKeys.SPECIAL_ORB_SLOT,
		name = "Compact Spec slot",
		description = "",
		hidden = true
	)
	default Orbs orbInSpecialSlot()
	{
		return Orbs.SPEC_ORB_CONTAINER;
	}

	@ConfigItem(
		keyName = ConfigKeys.HP_ORB_SLOT_VANILLA,
		name = "Vanilla HP slot",
		description = "",
		hidden = true
	)
	default Orbs orbInHpSlotVanilla()
	{
		return Orbs.HP_ORB_CONTAINER;
	}

	@ConfigItem(
		keyName = ConfigKeys.PRAYER_ORB_SLOT_VANILLA,
		name = "Vanilla Prayer slot",
		description = "",
		hidden = true
	)
	default Orbs orbInPrayerSlotVanilla()
	{
		return Orbs.PRAYER_ORB_CONTAINER;
	}

	@ConfigItem(
		keyName = ConfigKeys.RUN_ORB_SLOT_VANILLA,
		name = "Vanilla Run slot",
		description = "",
		hidden = true
	)
	default Orbs orbInRunSlotVanilla()
	{
		return Orbs.RUN_ORB_CONTAINER;
	}

	@ConfigItem(
		keyName = ConfigKeys.SPECIAL_ORB_SLOT_VANILLA,
		name = "Vanilla Spec slot",
		description = "",
		hidden = true
	)
	default Orbs orbInSpecialSlotVanilla()
	{
		return Orbs.SPEC_ORB_CONTAINER;
	}

	//orb hiding
	@ConfigItem(
		keyName = ConfigKeys.HIDE_HP,
		name = "Hide Hp",
		description = "",
		hidden = true
	)
	default boolean hideHp()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HIDE_PRAYER,
		name = "Hide Prayer",
		description = "",
		hidden = true
	)
	default boolean hidePray()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HIDE_RUN,
		name = "Hide Run",
		description = "",
		hidden = true
	)
	default boolean hideRun()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HIDE_SPEC,
		name = "Hide Special",
		description = "",
		hidden = true
	)
	default boolean hideSpec()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HIDE_XP,
		name = "Hide XP",
		description = "",
		hidden = true
	)
	default boolean hideXp()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HIDE_WORLD,
		name = "Hide World Map",
		description = "",
		hidden = true
	)
	default boolean hideWorld()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HIDE_STORE,
		name = "Hide Store",
		description = "",
		hidden = true
	)
	default boolean hideStore()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HIDE_ACTIVITY,
		name = "Hide Activity Advisor",
		description = "",
		hidden = true
	)
	default boolean hideActivity()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HIDE_WIKI,
		name = "Hide Wiki banner",
		description = "",
		hidden = true
	)
	default boolean hideWiki()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HIDE_LOGOUT_X,
		name = "Hide Logout-X",
		description = "",
		hidden = true
	)
	default boolean hideLogout()
	{
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.HIDE_GRID,
		name = "Hide Grid Master (Legacy)",
		description = "",
		hidden = true
	)
	default boolean hideGrid()
	{
		return false;
	}

}
