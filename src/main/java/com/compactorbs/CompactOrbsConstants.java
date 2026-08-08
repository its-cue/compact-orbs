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

import java.awt.Color;
import net.runelite.api.ScriptID;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.ui.JagexColors;

public class CompactOrbsConstants
{
	public static final class ConfigGroup
	{
		public static final String GROUP_NAME = "compactorbs";
		public static final int CONFIG_VERSION = 2;

		public static final class Wiki
		{
			public static final String GROUP_NAME = "wiki";
		}

		public static final class RuneLite
		{
			public static final String GROUP_NAME = "runelite";
		}
	}

	public static final class ConfigKeys
	{
		public static final String CONFIG_VERSION = "configVersion";
		public static final String MINIMAP = "hideMinimap";
		public static final String COMPASS = "hideCompass";
		public static final String HOTKEY_KEYBIND = "hotkeyKeybind";
		public static final String HOTKEY_TOGGLE_OPTION = "hotkeyToggleOption";
		public static final String MINIMAP_TOGGLE_BUTTON = "hideMinimapButton";
		public static final String RIGHT_CLICK_TOGGLE_BUTTONS = "rightClickToggleButtons";
		public static final String MINIMAP_BUTTON_PLACEMENT = "minimapButtonPlacement";
		public static final String ORB_LAYOUT = "orbLayout";
		public static final String VERTICAL_ANCHOR = "verticalAnchor";
		public static final String HORIZONTAL_ANCHOR = "horizontalAnchor";
		public static final String VERTICAL_Y_ADJUSTMENT = "verticalYAdjustment";
		public static final String DISABLE_REORDERING = "disableReordering";
		public static final String LEAVE_EMPTY_SPACE = "leaveEmptySpace";
		public static final String ENABLE_NO_CLICKTHROUGH = "enableNoClickthrough";
		public static final String ENABLE_ORB_SWAPPING = "enableOrbSwapping";
		public static final String HP_ORB_SLOT = "hpOrbSlot";
		public static final String PRAYER_ORB_SLOT = "prayerOrbSlot";
		public static final String RUN_ORB_SLOT = "runOrbSlot";
		public static final String SPECIAL_ORB_SLOT = "specialOrbSlot";
		public static final String HP_ORB_SLOT_VANILLA = "hpOrbSlotVanilla";
		public static final String PRAYER_ORB_SLOT_VANILLA = "prayerOrbSlotVanilla";
		public static final String RUN_ORB_SLOT_VANILLA = "runOrbSlotVanilla";
		public static final String SPECIAL_ORB_SLOT_VANILLA = "specialOrbSlotVanilla";
		public static final String HIDE_HP = "hideHp";
		public static final String HIDE_PRAYER = "hidePrayer";
		public static final String HIDE_RUN = "hideRun";
		public static final String HIDE_SPEC = "hideSpec";
		public static final String HIDE_XP = "hideXp";
		public static final String HIDE_WORLD = "hideWorld";
		public static final String HIDE_ACTIVITY = "hideActivity";
		public static final String HIDE_STORE = "hideStore";
		public static final String HIDE_WIKI = "hideWiki";
		public static final String HIDE_LOGOUT_X = "hideLogoutX";
		public static final String HIDE_GRID = "hideGrid";
		public static final String ENABLE_MINIMAP_OVERLAY = "enableMinimapOverlay";
		public static final String ENABLE_OVERLAY_TOGGLE_OPTION = "enableOverlayToggleOption";
		public static final String ENABLE_LOGOUT_X_OVERLAY = "enableLogoutXOverlay";

		//edit-modes custom positioning
		public static final String CUSTOM_LAYOUT_PREFIX = "custom_layout_";
		public static final String VANILLA_LAYOUT_PREFIX = "vanilla_layout_";
		public static final String FIXED_LAYOUT_PREFIX = "fixed_layout_";

		public static final class Wiki
		{
			public static final String SHOW_WIKI_MINIMAP_BUTTON = "showWikiMinimapButton";
		}

		public static final class RuneLite
		{
			public static final String WIKI_PLUGIN = "wikiplugin";
		}
	}

	public static final class Enum
	{
		//contains the toplevel component ids (key: widget, value: widget)
		public static final int TOPLEVEL_COMPONENTS = 1131;
	}

	public static final class Varbit
	{
		public static final int CUTSCENE_STATUS = VarbitID.CUTSCENE_STATUS;
		public static final int MINIMAP_TOGGLE = VarbitID.MINIMAP_TOGGLE;

		//orb in-game settings
		public static final int ACTIVITY_ORB_TOGGLE = VarbitID.OPTION_CONTENT_RECOMMENDER_HIDE;
		public static final int STORE_ORB_TOGGLE = VarbitID.TLI_STOREBUTTON_TOGGLE_DESKTOP;
		public static final int WIKI_ICON_TOGGLE = VarbitID.WIKI_ICON_DISABLED;
	}

	public static final class VarPlayer
	{
		//var trigger for the compass menu options
		public static final int MAP_FLAGS_CACHED = VarPlayerID.MAP_FLAGS_CACHED;
	}

	public static final class VarbitValue
	{
		public static final int CUTSCENE_ACTIVE = 1;
		public static final int MINIMAP_MINIMIZED = 1;

		//orb in-game settings
		public static final int ACTIVITY_ORB_VISIBLE = 0;
		public static final int STORE_ORB_VISIBLE = 1;
		public static final int WIKI_ICON_VISIBLE = 0;
	}

	public static final class Script
	{
		//trigger remapping without the need for a valid scriptId
		public static final int FORCE_UPDATE = -1;

		//script that swaps a graphic with another, ex: for orb frame hovering
		public static final int GRAPHIC_SWAPPER = 44;

		//scripts related to noclickthrough updating
		public static final int ORBS_UPDATE_HEALTH = 446;
		public static final int ORBS_UPDATE_SPECENERGY = 2069;

		//relevant orb update scripts
		public static final int TOPLEVEL_SUBCHANGE = 903;
		public static final int TOPLEVEL_REDRAW = ScriptID.TOPLEVEL_REDRAW;
		public static final int PROC_TOPLEVEL_SUBCHANGE = 908;
		public static final int TOPLEVEL_SIDEBUTTON_OP = 914;
		public static final int TOPLEVEL_SIDE_CUSTOMIZE = 919;
		public static final int WORLD_MAP_UPDATE = 1700;
		public static final int TOOLTIP_MOUSE_RELEASE = 837;
		public static final int STORE_ORB_UPDATE = 2396;
		public static final int ACTIVITY_ORB_UPDATE = 2480;
		public static final int WIKI_ICON_INIT = 3304;
		public static final int WIKI_ICON_UPDATE = ScriptID.WIKI_ICON_UPDATE;
		public static final int GRID_MASTER_ORB_UPDATE = 8222;
		public static final int TOPLEVEL_COMPASS_OP = 1050;
		public static final int TOPLEVEL_COMPASS_SETOP = 7044;

		//cs2
		public static final int COMSUBID1 = -2147483643;
		public static final int OPINDEX0 = -2147483644;
		public static final int COMPONENT0 = -2147483645;
	}

	public static final class Layout
	{
		public static final class Original
		{
			public static final int MAP_CONTAINER_WIDTH = 211;
			public static final int MAP_CONTAINER_HEIGHT = 207;

			public static final int ORBS_CONTAINER_WIDTH = 207;
			public static final int ORBS_CONTAINER_HEIGHT = 197;

			public static final int XP_DROPS_X = 0;
			public static final int XP_DROPS_Y = 17;

			public static final int HP_ORB_X = 0;
			public static final int HP_ORB_Y = 37;

			public static final int PRAYER_ORB_X = 0;
			public static final int PRAYER_ORB_Y = 71;

			public static final int RUN_ORB_X = 10;
			public static final int RUN_ORB_Y = 103;

			public static final int SPEC_ORB_X = 32;
			public static final int SPEC_ORB_Y = 128;

			public static final int STORE_ORB_X = 85;
			public static final int STORE_ORB_Y = 143;

			public static final int ACTIVITY_ORB_X = 55;
			public static final int ACTIVITY_ORB_Y = 162;

			public static final int WORLD_MAP_X = 0;
			public static final int WORLD_MAP_Y = 115;

			public static final int WIKI_ICON_X = 0;
			public static final int WIKI_ICON_Y = 135;

			public static final int WIKI_HEIGHT = 34;

			public static final int LOGOUT_X = 2;
			public static final int LOGOUT_Y = 2;

			public static final int COMPASS_X = 34;
			public static final int COMPASS_Y = 5;
			public static final int COMPASS_DIMENSION = 35;

			public static final int COMPASS_OP_X = COMPASS_X - 2;
			public static final int COMPASS_OP_Y = COMPASS_Y - 2;

			public static final int MINIMAP_X = 6;
			public static final int MINIMAP_Y = 8;
			public static final int MINIMAP_DIMENSION = 152;
		}

		public static final class Vertical
		{
			public static final int LAYOUT_ID = 0;

			public static final int MAP_CONTAINER_WIDTH = 96;
			public static final int MAP_CONTAINER_HEIGHT = 193;

			public static final int XP_DROPS_X = 7;
			public static final int XP_DROPS_Y = 42;

			public static final int HP_ORB_X = 39;
			public static final int HP_ORB_Y = 39;

			public static final int PRAYER_ORB_X = 39;
			public static final int PRAYER_ORB_Y = 73;

			public static final int RUN_ORB_X = 39;
			public static final int RUN_ORB_Y = 107;

			public static final int SPEC_ORB_X = 39;
			public static final int SPEC_ORB_Y = 141;

			public static final int STORE_ORB_X = 3;
			public static final int STORE_ORB_Y = 101;

			public static final int ACTIVITY_ORB_X = 3;
			public static final int ACTIVITY_ORB_Y = 136;

			public static final int WORLD_MAP_X = 5;
			public static final int WORLD_MAP_Y = 70;

			public static final int WIKI_ICON_X = 38;
			public static final int WIKI_ICON_Y = 178;

			public static final int LOGOUT_X = 71;
			public static final int LOGOUT_Y = 2;

			public static final int COMPASS_X = Original.COMPASS_X - 14;
			public static final int COMPASS_Y = 1;
		}

		public static final class Horizontal
		{
			public static final int LAYOUT_ID = 1;

			public static final int MAP_CONTAINER_WIDTH = 209;
			public static final int MAP_CONTAINER_HEIGHT = 102;

			public static final int XP_DROPS_X = 179;
			public static final int XP_DROPS_Y = 25;

			public static final int HP_ORB_X = 35;
			public static final int HP_ORB_Y = 33;

			public static final int PRAYER_ORB_X = 35;
			public static final int PRAYER_ORB_Y = 68;

			public static final int RUN_ORB_X = 93;
			public static final int RUN_ORB_Y = 33;

			public static final int SPEC_ORB_X = 93;
			public static final int SPEC_ORB_Y = 68;

			public static final int STORE_ORB_X = 0;
			public static final int STORE_ORB_Y = 34;

			public static final int ACTIVITY_ORB_X = 0;
			public static final int ACTIVITY_ORB_Y = 68;

			public static final int WORLD_MAP_X = 146;
			public static final int WORLD_MAP_Y = 16;

			public static final int WIKI_ICON_X = 151;
			public static final int WIKI_ICON_Y = 88;

			public static final int LOGOUT_X = 182;
			public static final int LOGOUT_Y = 1;

			public static final int COMPASS_X = Original.COMPASS_X + 115;
			public static final int COMPASS_Y = Original.COMPASS_Y + 41;
		}

		public static final class HorizontalWide
		{
			public static final int LAYOUT_ID = 2;

			public static final int MAP_CONTAINER_WIDTH = 228;
			public static final int MAP_CONTAINER_HEIGHT = 79;

			public static final int XP_DROPS_X = 75;
			public static final int XP_DROPS_Y = 12;

			public static final int HP_ORB_X = 0;
			public static final int HP_ORB_Y = 45;

			public static final int PRAYER_ORB_X = 57;
			public static final int PRAYER_ORB_Y = 45;

			public static final int RUN_ORB_X = 114;
			public static final int RUN_ORB_Y = 45;

			public static final int SPEC_ORB_X = 171;
			public static final int SPEC_ORB_Y = 45;

			public static final int STORE_ORB_X = 37;
			public static final int STORE_ORB_Y = 10;

			public static final int ACTIVITY_ORB_X = 0;
			public static final int ACTIVITY_ORB_Y = 10;

			public static final int WORLD_MAP_X = 167;
			public static final int WORLD_MAP_Y = 0;

			public static final int WIKI_ICON_X = 162;
			public static final int WIKI_ICON_Y = 31;

			public static final int LOGOUT_X = 200;
			public static final int LOGOUT_Y = 2;

			public static final int COMPASS_X = Original.COMPASS_X + 76;
			public static final int COMPASS_Y = 1;
		}

		public static final class Custom
		{
			public static final int LAYOUT_ID = 3;
		}

		public static final class MinimapOverlay
		{
			public static final int CONTAINER_WIDTH = 182;
			public static final int CONTAINER_HEIGHT = 166;
			public static final int[] NO_CLICK_Y = {4, 44, 100, 125, 140, 155};
			public static final int[] NO_CLICK_WIDTH = {178, 166, 161, 151, 141, 121};
			public static final int[] NO_CLICK_HEIGHT = {40, 56, 25, 15, 15, 11};

			public static final int MINIMAP_CONTENT = 1338;
			public static final int COMPASS_CONTENT = 1339;
		}

		//minimap toggle button locations @ToggleLocation
		public static final int DEFAULT_MINIMAP_BUTTON_X = 190;
		public static final int DEFAULT_MINIMAP_BUTTON_Y = 176;

		public static final int ABOVE_XP_MINIMAP_BUTTON_X = 8;
		public static final int ABOVE_XP_MINIMAP_BUTTON_Y = 0;

		public static final int BELOW_MAP_MINIMAP_BUTTON_X = 120;
		public static final int BELOW_MAP_MINIMAP_BUTTON_Y = 155;

		public static final int BELOW_X_MINIMAP_BUTTON_X = DEFAULT_MINIMAP_BUTTON_X;
		public static final int BELOW_X_MINIMAP_BUTTON_Y = 15;

		//toggle button dimensions
		public static final int TOGGLE_BUTTON_SIZE = 17;

		//compass frame dimensions
		public static final int COMPASS_FRAME_SIZE = 43;

		//misc widget dimensions
		public static final int COMPASS_SIZE = 36;
		public static final int LOGOUT_X_WIDTH = 26;
		public static final int LOGOUT_X_HEIGHT = 23;

		//toggle button opacity when onMouseOver || onMouseLeave
		public static final int OPACITY = 0;
		public static final int OPACITY_HOVER = 130;

		public static final int ORBS_CONTAINER_OFFSET_Y = 10;
		public static final int LOGOUT_X_ICON_OPACITY = 100;
		public static final int EDIT_MODE_HIDDEN_OPACITY = 160;
		public static final int EDIT_MODE_BACKGROUND_OPACITY = 220;
	}

	public static final class MenuOp
	{
		public static final String SHOW = "Show";
		public static final String HIDE = "Hide";

		public static final String MINIMAP_OP = "Minimap";
		public static final String DETACHED_OP = "Detached " + MINIMAP_OP;

		public static final String LOGOUT_OP = "Logout";
		public static final String WORLD_SWITCHER_OP = "World switcher";

		public static final String ENABLE = "Enable";
		public static final String EDIT_MODE = "edit mode";

		public static final String SAVE = "Save";
		public static final String CHANGES = "changes";

		public static final String RESET = "Reset";
		public static final String RESET_ALL = RESET + " all positions";

		public static final Color MENU_TARGET = JagexColors.MENU_TARGET;
		public static final Color RED = JagexColors.CHAT_FC_TEXT_TRANSPARENT_BACKGROUND;

		public static final int OP_INDEX_0 = 0;
		public static final int HANDLER_TOGGLE_OP_INDEX = 6;
		public static final int RESET_POSITION_OP_INDEX = 7;
		public static final int RESET_ALL_OP_INDEX = 8;
		public static final int EDIT_MODE_OP_INDEX = 9;
	}

	public static final class Sprite
	{
		//orb related sprites
		public static final int FRAME = SpriteID.OrbFrame.FRAME;
		public static final int FRAME_HOVERED = SpriteID.OrbFrame.FRAME_HOVERED;

		//for edit-mode
		public static final int FIXED_MINIMAP_MASK = SpriteID.FIXED_MAP_MASK;

		//minimap overlay sprites
		public static final int COMPASS_MASK = SpriteID.RESIZE_COMPASS_MASK;
		public static final int MINIMAP_MASK = SpriteID.RESIZE_MAP_MASK;
		public static final int MINIMAP_FRAME = SpriteID.OSRS_STRETCH_MAPSURROUND;
		public static final int LOGOUT_X_BUTTON = SpriteID.CloseButtons._7;
		public static final int COMPASS_FRAME = SpriteID.COMPASS_OUTLINE;

		//toggle button sprites
		public static final int HIDDEN = SpriteID.GroundItemsVisibility._1;
		public static final int VISIBLE = SpriteID.GroundItemsVisibility._0;
	}

	public static final class Widgets
	{
		public static final class MinimapOverlay
		{
			//seems BuffBar could be used for the leagues relic display?
			//can instead use StatBoostsHud (seems to be mobile/steam only)
			//script can stay the same as entry since it fires frequently
			public static final int UNIVERSE = InterfaceID.StatBoostsHud.UNIVERSE;//InterfaceID.BuffBar.UNIVERSE;
		}

		//orb widget ids
		public static final class Orb
		{
			public static final int UNIVERSE = InterfaceID.Orbs.UNIVERSE;

			public static final int XP_DROPS = InterfaceID.Orbs.XP_DROPS;

			public static final int HP_ORB = InterfaceID.Orbs.ORB_HEALTH;
			public static final int HP_ORB_BACKING = InterfaceID.Orbs.HEALTH_BACKING;
			public static final int HP_ORB_BUTTON = InterfaceID.Orbs.HEALTHBUTTON;
			public static final int HP_ORB_INDICATOR = InterfaceID.Orbs.HEALTH_INDICATOR;
			public static final int HP_ORB_ICON = InterfaceID.Orbs.ORB_HEALTH_HEART_ICON;
			public static final int HP_ORB_EMPTY = InterfaceID.Orbs.HEALTH_EMPTY_CONTENTS;

			public static final int PRAY_ORB = InterfaceID.Orbs.ORB_PRAYER;
			public static final int PRAY_ORB_BACKING = InterfaceID.Orbs.PRAYER_BACKING;
			public static final int PRAY_ORB_BUTTON = InterfaceID.Orbs.PRAYERBUTTON;
			public static final int PRAY_ORB_INDICATOR = InterfaceID.Orbs.PRAYER_INDICATOR;
			public static final int PRAY_ORB_ICON = InterfaceID.Orbs.PRAYER_ICON;
			public static final int PRAY_ORB_EMPTY = InterfaceID.Orbs.ORB_PRAYER_EMPTY_GRAPHIC0;

			public static final int RUN_ORB = InterfaceID.Orbs.ORB_RUNENERGY;
			public static final int RUN_ORB_BACKING = InterfaceID.Orbs.RUNENERGY_BACKING;
			public static final int RUN_ORB_BUTTON = InterfaceID.Orbs.RUNBUTTON;
			public static final int RUN_ORB_INDICATOR = InterfaceID.Orbs.RUNENERGY_INDICATOR;
			public static final int RUN_ORB_ICON = InterfaceID.Orbs.RUNENERGY_ICON;
			public static final int RUN_ORB_EMPTY = InterfaceID.Orbs.ORB_RUNENERGY_EMPTY_GRAPHIC0;

			public static final int SPEC_ORB = InterfaceID.Orbs.ORB_SPECENERGY;
			public static final int SPEC_ORB_BACKING = InterfaceID.Orbs.SPECENERGY_BACKING;
			public static final int SPEC_ORB_BUTTON = InterfaceID.Orbs.SPECBUTTON;
			public static final int SPEC_ORB_INDICATOR = InterfaceID.Orbs.SPECENERGY_INDICATOR;
			public static final int SPEC_ORB_ICON = InterfaceID.Orbs.SPECENERGY_ICON;
			public static final int SPEC_ORB_EMPTY = InterfaceID.Orbs.ORB_SPECENERGY_EMPTY_GRAPHIC0;

			public static final int ACTIVITY_ORB = InterfaceID.Orbs.ORB_CONTENTRECOM;
			public static final int ACTIVITY_ORB_BACKING = InterfaceID.Orbs.CR_BACKING;
			public static final int ACTIVITY_ORB_INDICATOR = InterfaceID.Orbs.CR_INDICATOR;
			public static final int ACTIVITY_ORB_ICON = InterfaceID.Orbs.CR_ICON;

			public static final int STORE_ORB = InterfaceID.Orbs.ORB_STORE;
			public static final int STORE_ORB_BACKING = InterfaceID.Orbs.STORE_BACKING;
			public static final int STORE_ORB_INDICATOR = InterfaceID.Orbs.STORE_INDICATOR;
			public static final int STORE_ORB_ICON = InterfaceID.Orbs.STORE_ICON;

			public static final int WIKI_ICON = InterfaceID.Orbs.WIKI;
			public static final int WIKI_CONTAINER_VANILLA = InterfaceID.Orbs.WIKI_ICON;
			public static final int WIKI_ICON_VANILLA = InterfaceID.Orbs.WIKI_ICON_GRAPHIC;

			public static final int WORLD_MAP = InterfaceID.Orbs.ORB_WORLDMAP;
			public static final int WORLD_MAP_BACKING = InterfaceID.Orbs.WORLDMAP_BACKING;
			public static final int WORLD_MAP_ICON = InterfaceID.Orbs.WORLDMAP;
			public static final int WORLD_MAP_TOOLTIP = InterfaceID.Orbs.TOOLTIP;
		}

		//fixed mode widget ids
		public static final class Fixed
		{
			public static final int MAP_CONTAINER = InterfaceID.Toplevel.MAPCONTAINER;
			public static final int MINIMAP_MASK = InterfaceID.Toplevel.MINIMAP;
			public static final int ORBS = InterfaceID.Toplevel.ORBS;
		}

		//classic-resizable widget ids
		public static final class Classic
		{
			public static final int MAP_CONTAINER = InterfaceID.ToplevelOsrsStretch.MAP_CONTAINER;
			public static final int ORBS = InterfaceID.ToplevelOsrsStretch.ORBS;
			public static final int MAP_NOCLICK_0 = InterfaceID.ToplevelOsrsStretch.MAP_NOCLICK_0;
			public static final int MAP_NOCLICK_1 = InterfaceID.ToplevelOsrsStretch.MAP_NOCLICK_1;
			public static final int MAP_NOCLICK_2 = InterfaceID.ToplevelOsrsStretch.MAP_NOCLICK_2;
			public static final int MAP_NOCLICK_3 = InterfaceID.ToplevelOsrsStretch.MAP_NOCLICK_3;
			public static final int MAP_NOCLICK_4 = InterfaceID.ToplevelOsrsStretch.MAP_NOCLICK_4;
			public static final int MAP_NOCLICK_5 = InterfaceID.ToplevelOsrsStretch.MAP_NOCLICK_5;
			public static final int MINIMAP_MASK = InterfaceID.ToplevelOsrsStretch.MINIMAP;
			public static final int MINIMAP = InterfaceID.ToplevelOsrsStretch.MAP_MINIMAP_GRAPHIC9;
			public static final int COMPASS = InterfaceID.ToplevelOsrsStretch.MAP_MINIMAP_GRAPHIC6;
			public static final int COMPASS_OPTIONS = InterfaceID.ToplevelOsrsStretch.COMPASSCLICK;
			public static final int COMPASS_PARENT = InterfaceID.ToplevelOsrsStretch.MAP_MINIMAP;
			public static final int SIDE_TOP = InterfaceID.ToplevelOsrsStretch.SIDE_TOP;
		}

		//modern-resizable widget ids
		public static final class Modern
		{
			public static final int MAP_CONTAINER = InterfaceID.ToplevelPreEoc.MAP_CONTAINER;
			public static final int ORBS = InterfaceID.ToplevelPreEoc.ORBS;
			public static final int MAP_NOCLICK_0 = InterfaceID.ToplevelPreEoc.MAP_NOCLICK_0;
			public static final int MAP_NOCLICK_1 = InterfaceID.ToplevelPreEoc.MAP_NOCLICK_1;
			public static final int MAP_NOCLICK_2 = InterfaceID.ToplevelPreEoc.MAP_NOCLICK_2;
			public static final int MAP_NOCLICK_3 = InterfaceID.ToplevelPreEoc.MAP_NOCLICK_3;
			public static final int MAP_NOCLICK_4 = InterfaceID.ToplevelPreEoc.MAP_NOCLICK_4;
			public static final int MAP_NOCLICK_5 = InterfaceID.ToplevelPreEoc.MAP_NOCLICK_5;
			public static final int MINIMAP_MASK = InterfaceID.ToplevelPreEoc.MINIMAP;
			public static final int MINIMAP = InterfaceID.ToplevelPreEoc.MAP_MINIMAP_GRAPHIC9;
			public static final int COMPASS = InterfaceID.ToplevelPreEoc.MAP_MINIMAP_GRAPHIC6;
			public static final int COMPASS_OPTIONS = InterfaceID.ToplevelPreEoc.COMPASSCLICK;
			public static final int COMPASS_PARENT = InterfaceID.ToplevelPreEoc.MAP_MINIMAP;
			public static final int LOGOUT_X_ICON = InterfaceID.ToplevelPreEoc.ICON10;
			public static final int LOGOUT_X_STONE = InterfaceID.ToplevelPreEoc.STONE10;
			public static final int SIDE_MOVABLE = InterfaceID.ToplevelPreEoc.SIDE_MOVABLE;
		}
	}
}
