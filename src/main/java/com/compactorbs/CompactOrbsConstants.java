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
import java.util.Set;
import net.runelite.api.ScriptID;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.ui.JagexColors;

public class CompactOrbsConstants
{
	public static final class ConfigGroup
	{
		public static final String GROUP_NAME = "compactorbs";

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
		public static final String MINIMAP = "hideMinimap";
		public static final String COMPASS = "hideCompass";
		public static final String HOTKEY_TOGGLE = "hotkeyToggle";
		public static final String HOTKEY_MINIMAP = "hotkeyMinimap";
		public static final String MINIMAP_TOGGLE_BUTTON = "hideMinimapButton";
		public static final String COMPASS_TOGGLE_BUTTON = "hideCompassButton";
		public static final String MINIMAP_BUTTON_PLACEMENT = "minimapButtonPlacement";
		public static final String ORB_LAYOUT = "orbLayout";
		public static final String HORIZONTAL = "horizontalPosition";
		public static final String VERTICAL = "verticalPosition";
		public static final String DISABLE_REORDERING = "disableReordering";
		public static final String LEAVE_EMPTY_SPACE = "leaveEmptySpace";
		public static final String ENABLE_ORB_SWAPPING = "enableOrbSwapping";
		public static final String HP_ORB_SLOT = "hpOrbSlot";
		public static final String PRAYER_ORB_SLOT = "prayerOrbSlot";
		public static final String RUN_ORB_SLOT = "runOrbSlot";
		public static final String SPECIAL_ORB_SLOT = "specialOrbSlot";
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

		public static final class Wiki
		{
			public static final String SHOW_WIKI_MINIMAP_BUTTON = "showWikiMinimapButton";
		}

		public static final class RuneLite
		{
			public static final String WIKI_PLUGIN = "wikiplugin";
		}
	}

	public static final class Varbit
	{
		public static final int MINIMAP_TOGGLE = VarbitID.MINIMAP_TOGGLE;
		public static final int ACTIVITY_ORB_TOGGLE = VarbitID.OPTION_CONTENT_RECOMMENDER_HIDE;
		public static final int STORE_ORB_TOGGLE = VarbitID.TLI_STOREBUTTON_TOGGLE_DESKTOP;
		public static final int CUTSCENE_STATUS = VarbitID.CUTSCENE_STATUS;
	}

	public static final class VarbitValue
	{
		// 1 (is minimized), 0 (not minimized)
		public static final int MINIMAP_MINIMIZED = 1;

		// 0 (is visible), 1 (not visible)
		public static final int ACTIVITY_ORB_VISIBLE = 0;

		// 1 (is visible), 0 (not visible)
		public static final int STORE_ORB_VISIBLE = 1;

		// 1 (active), 0 (inactive)
		public static final int CUTSCENE_ACTIVE = 1;
	}

	public static final class Script
	{
		//custom flag, used to trigger remapping without scriptId matching
		public static final int FORCE_UPDATE = -1;

		//logout X redraw when opening tabs/using hotkey
		public static final int TOP_LEVEL_REDRAW = ScriptID.TOPLEVEL_REDRAW;
		public static final int TOP_LEVEL_SIDE_CUSTOMIZE = 919;

		//buff bar widget, used for the minimap overlay
		public static final int BUFF_BAR_CONTENT_UPDATE = 4730;

		//relevant update scripts for the target orbs
		public static final int WORLD_MAP_UPDATE = 1700;
		public static final int STORE_ORB_UPDATE = 2396;
		public static final int ACTIVITY_ORB_UPDATE = 2480;
		public static final int WIKI_ICON_UPDATE = ScriptID.WIKI_ICON_UPDATE;
		public static final int GRID_MASTER_ORB_UPDATE = 8222;

		public static final Set<Integer> MINIMAP_UPDATE_SCRIPTS =
			Set.of(
				//orbs
				WORLD_MAP_UPDATE,
				STORE_ORB_UPDATE,
				ACTIVITY_ORB_UPDATE,
				WIKI_ICON_UPDATE,
				GRID_MASTER_ORB_UPDATE
			);
	}

	/* Layout positions, dimensions, and other style changes */
	public static final class Layout
	{
		/* Original positions for orb/compass widgets */
		public static final class Original
		{
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

			public static final int WIKI_VANILLA_X = 0;
			public static final int WIKI_VANILLA_Y = 0;

			public static final int LOGOUT_X = 2;
			public static final int LOGOUT_Y = 2;

			public static final int COMPASS_X = 34;
			public static final int COMPASS_Y = 5;
			public static final int COMPASS_DIMENSION = 35;

			public static final int MINIMAP_X = 6;
			public static final int MINIMAP_Y = 8;
			public static final int MINIMAP_DIMENSION = 152;
		}

		/* Vertical positions for orb/compass widgets */
		public static final class Vertical
		{
			public static final int XP_DROPS_X = 68;
			public static final int XP_DROPS_Y = 44;

			public static final int HP_ORB_X = 0;
			public static final int HP_ORB_Y = 41;

			public static final int PRAYER_ORB_X = 0;
			public static final int PRAYER_ORB_Y = 76;

			public static final int RUN_ORB_X = 0;
			public static final int RUN_ORB_Y = 111;

			public static final int SPEC_ORB_X = 0;
			public static final int SPEC_ORB_Y = 146;

			public static final int STORE_ORB_X = 64;
			public static final int STORE_ORB_Y = 103;

			public static final int ACTIVITY_ORB_X = 64;
			public static final int ACTIVITY_ORB_Y = 138;

			public static final int WORLD_MAP_X = 66;
			public static final int WORLD_MAP_Y = 72;

			public static final int WIKI_ICON_X = 20;
			public static final int WIKI_ICON_Y = 171;

			public static final int WIKI_VANILLA_X = 0;
			public static final int WIKI_VANILLA_Y = 10;

			public static final int LOGOUT_X = 8;
			public static final int LOGOUT_Y = 22;

			public static final int COMPASS_X = Original.COMPASS_X + 94;
			public static final int COMPASS_Y = Original.COMPASS_Y + 13;

			//vertical offset
			public static final int LEFT_OFFSET = 108;
			public static final int RIGHT_OFFSET = 0;
		}

		/* Horizontal positions for orb/compass widgets */
		public static final class Horizontal
		{
			public static final int XP_DROPS_X = 179;
			public static final int XP_DROPS_Y = 15;

			public static final int HP_ORB_X = 35;
			public static final int HP_ORB_Y = 25;

			public static final int PRAYER_ORB_X = 35;
			public static final int PRAYER_ORB_Y = 60;

			public static final int RUN_ORB_X = 92;
			public static final int RUN_ORB_Y = 25;

			public static final int SPEC_ORB_X = 92;
			public static final int SPEC_ORB_Y = 60;

			public static final int STORE_ORB_X = 0;
			public static final int STORE_ORB_Y = 25;

			public static final int ACTIVITY_ORB_X = 0;
			public static final int ACTIVITY_ORB_Y = 60;

			public static final int WORLD_MAP_X = 31;//148
			public static final int WORLD_MAP_Y = 6;

			public static final int WIKI_ICON_X = 149;
			public static final int WIKI_ICON_Y = 71;

			public static final int WIKI_VANILLA_X = 0;
			public static final int WIKI_VANILLA_Y = 10;

			public static final int LOGOUT_X = 1;
			public static final int LOGOUT_Y = 1;

			public static final int COMPASS_X = Original.COMPASS_X + 117;
			public static final int COMPASS_Y = Original.COMPASS_Y + 47;

			//horizontal offset
			public static final int TOP_OFFSET = 0;
			public static final int BOTTOM_OFFSET = 100;
		}

		public static final class MinimapOverlay
		{
			public static final int CONTAINER_WIDTH = 182;
			public static final int CONTAINER_HEIGHT = 166;

			//difference between original width, and clone width
			public static final int WIDTH_DIFF = 29;

			public static final int NO_CLICK_X = 0;
			public static final int[] NO_CLICK_Y =
				{
					4, 44, 100, 125, 140, 155
				};

			public static final int[] NO_CLICK_WIDTH =
				{
					178, 166, 161, 151, 141, 121
				};

			public static final int[] NO_CLICK_HEIGHT =
				{
					40, 56, 25, 15, 15, 11
				};

			public static final int MINIMAP_CONTENT = 1338;
			public static final int COMPASS_CONTENT = 1339;
		}

		//world map x and y when in fixed mode
		public static final int FIXED_WORLD_MAP_X = 10;
		public static final int FIXED_WORLD_MAP_Y = 115;

		//used when hiding the world map orb, to calc the offset so the hotkey still works
		public static final int WORLD_MAP_CONTAINER_WIDTH = 30;

		//compass menu op offset
		public static final int COMPASS_OPTIONS_OFFSET = 2;

		//offsets used to anchor the frame around the compass
		public static final int FRAME_X_OFFSET = 2;
		public static final int FRAME_Y_OFFSET = 12;

		//offsets used to anchor the toggle button around the compass sprite
		public static final int COMPASS_BUTTON_X_OFFSET = 31;
		public static final int COMPASS_BUTTON_Y_OFFSET = 14;

		//offsets used in horizontal layout, to position the compass button
		public static final int COMPASS_BUTTON_HORIZONTAL_X_OFFSET = 4;
		public static final int COMPASS_BUTTON_HORIZONTAL_Y_OFFSET = 22;

		//minimap toggle button locations @ToggleLocation
		public static final int DEFAULT_MINIMAP_BUTTON_X = 190;
		public static final int DEFAULT_MINIMAP_BUTTON_Y = 180;

		public static final int ABOVE_XP_MINIMAP_BUTTON_X = 8;
		public static final int ABOVE_XP_MINIMAP_BUTTON_Y = 0;

		public static final int BELOW_MAP_MINIMAP_BUTTON_X = 120;
		public static final int BELOW_MAP_MINIMAP_BUTTON_Y = 155;

		public static final int BELOW_X_MINIMAP_BUTTON_x = DEFAULT_MINIMAP_BUTTON_X;
		public static final int BELOW_X_MINIMAP_BUTTON_Y = 15;

		//toggle button dimensions
		public static final int TOGGLE_BUTTON_WIDTH = 17;
		public static final int TOGGLE_BUTTON_HEIGHT = 17;

		//compass frame dimensions
		public static final int FRAME_WIDTH = 43;
		public static final int FRAME_HEIGHT = 43;

		//toggle button opacity when onMouseOver || onMouseLeave
		public static final int OPACITY = 0;
		public static final int OPACITY_HOVER = 130;
	}

	public static final class Menu
	{
		//prefix menu options for the toggle buttons
		public static final String PREFIX_SHOW = "Show";
		public static final String PREFIX_HIDE = "Hide";

		//suffix menu options for the toggle buttons
		public static final String SUFFIX_MINIMAP = "Minimap";
		public static final String SUFFIX_COMPASS = "Compass";

		//suffix menu color for the toggle buttons
		public static final Color COLOR = JagexColors.MENU_TARGET;
	}

	public static final class Sprite
	{
		//minimap clone sprites
		public static final int COMPASS_MASK = SpriteID.RESIZE_COMPASS_MASK;
		public static final int MINIMAP_MASK = SpriteID.RESIZE_MAP_MASK;
		public static final int MINIMAP_FRAME = SpriteID.OSRS_STRETCH_MAPSURROUND;

		//border frame for the compass when the minimap is hidden
		public static final int COMPASS_FRAME = SpriteID.COMPASS_OUTLINE;

		//toggle button sprites
		public static final int HIDDEN = SpriteID.GroundItemsVisibility._1;
		public static final int VISIBLE = SpriteID.GroundItemsVisibility._0;

		//widget inspector sprite
		public static final int WIDGET_INSPECTOR = SpriteID.OptionsIcons._50;
	}

	public static final class Widgets
	{
		public static final class MinimapOverlay
		{
			//unused widget layer (official client only?) BuffBar (651.0)
			public static final int UNIVERSE = InterfaceID.BuffBar.UNIVERSE;
		}

		public static final class Orb
		{
			//orbs container interface
			public static final int UNIVERSE = InterfaceID.Orbs.UNIVERSE;

			//orb containers
			public static final int XP_DROPS = InterfaceID.Orbs.XP_DROPS;
			public static final int HP_ORB = InterfaceID.Orbs.ORB_HEALTH;
			public static final int PRAY_ORB = InterfaceID.Orbs.ORB_PRAYER;
			public static final int RUN_ORB = InterfaceID.Orbs.ORB_RUNENERGY;
			public static final int SPEC_ORB = InterfaceID.Orbs.ORB_SPECENERGY;
			public static final int ACTIVITY_ORB = InterfaceID.Orbs.ORB_CONTENTRECOM;
			public static final int STORE_ORB = InterfaceID.Orbs.ORB_STORE;
			public static final int WIKI_ICON = InterfaceID.Orbs.WIKI;
			public static final int WIKI_CONTAINER_VANILLA = InterfaceID.Orbs.WIKI_ICON;
			public static final int WIKI_ICON_VANILLA = InterfaceID.Orbs.WIKI_ICON_GRAPHIC;
			public static final int WORLD_MAP = InterfaceID.Orbs.ORB_WORLDMAP;
		}

		//classic-resizable widgets
		public static final class Classic
		{
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
		}

		//modern-resizable widgets
		public static final class Modern
		{
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
		}
	}
}
