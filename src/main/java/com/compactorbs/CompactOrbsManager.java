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

import static com.compactorbs.CompactOrbsConfig.GROUP_NAME;
import com.compactorbs.widget.WidgetManager;
import com.compactorbs.widget.elements.Compass;
import com.compactorbs.widget.elements.Minimap;
import com.compactorbs.widget.elements.Orbs;
import java.awt.Color;
import java.util.function.Supplier;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.util.ColorUtil;

@Slf4j
public class CompactOrbsManager
{
	@Inject
	private Client client;

	@Inject
	private ConfigManager configManager;

	@Inject
	private CompactOrbsConfig config;

	@Inject
	private WidgetManager widgetManager;

	private int lastParentId = -1;

	public static final int FORCE_REMAP = -1;

	private boolean resetFixedOrbs = false;

	public static final int ORBS_UPDATE_WORLD_MAP = 1699;
	public static final int ORBS_UPDATE_STORE = 2396;
	public static final int ORBS_UPDATE_ACTIVITY_ADVISOR = 2480;
	public static final int WIKI_ICON_UPDATE = 3305;

	private static final int COMPASS_FRAME_SPRITE_ID = 5813;

	public static final int TOP_LEVEL_MINIMAP_CHILD = 33;

	public static final int COMPASS_X = 126;
	public static final int COMPASS_Y = 18;

	private Widget mapButton = null;
	private Widget mapMenu = null;
	private Widget compassButton = null;
	private Widget compassMenu = null;
	private Widget compassFrame = null;

	private static final String MINIMAP_CONFIG_KEY = "hideMinimap";
	private static final String COMPASS_CONFIG_KEY = "hideCompass";

	private static final Color MENU_COLOR = new Color(0xFF9040);

	public void init(int scriptId)
	{
		if (!client.isResized())
		{
			if (!resetFixedOrbs)
			{
				//reset only the required orbs when toggling to fixed mode display
				widgetManager.remapTarget(Orbs.FIXED, false, FORCE_REMAP);
				resetFixedOrbs = true;
			}
			return;
		}

		build(scriptId);
		resetFixedOrbs = false;
	}

	public void build(int scriptId)
	{
		if (!client.isResized())
		{
			return;
		}

		if (scriptId == FORCE_REMAP)
		{
			createCustomChildren();

			widgetManager.setHidden(Minimap.values(), isMinimapHidden());
			widgetManager.setHidden(Compass.values(), (isMinimapHidden() && isCompassHidden()));

			boolean remapCompassCondition = !isCompassHidden() && isMinimapHidden();
			widgetManager.remapAll(Compass.ALL, remapCompassCondition);

			widgetManager.remapAll(Orbs.ALL, isMinimapHidden());

			updateCustomChildren();
		}
		else
		{
			widgetManager.remapTarget(Orbs.ALL, isMinimapHidden(), scriptId);
		}
	}

	public void reset()
	{
		resetFixedOrbs = false;

		clearCustomChildren();

		if (isMinimapHidden())
		{
			widgetManager.setHidden(Minimap.values(), false);

			if (isCompassHidden())
			{
				widgetManager.setHidden(Compass.values(), false);
			}

			widgetManager.remapAll(Compass.ALL, false);
		}

		widgetManager.remapAll((client.isResized() ? Orbs.ALL : Orbs.FIXED), false);
	}

	private void onMinimapToggle()
	{
		boolean toggle = !isMinimapHidden();
		boolean remapCondition = toggle && !isCompassHidden();
		executeToggle(
			config::hideMinimap, MINIMAP_CONFIG_KEY,
			() -> widgetManager.setHidden(Minimap.values(), toggle),
			() -> widgetManager.remapAll(Compass.ALL, remapCondition),
			() -> widgetManager.setHidden(Compass.values(), toggle && isCompassHidden()),
			() -> widgetManager.remapAll(Orbs.ALL, toggle)
		);
	}

	private void onCompassToggle()
	{
		boolean toggle = !isCompassHidden();
		boolean remapCondition = toggle || isMinimapHidden();
		executeToggle(
			config::hideCompass, COMPASS_CONFIG_KEY,
			() -> widgetManager.remapAll(Compass.ALL, remapCondition),
			() -> widgetManager.setHidden(Compass.values(), toggle)
		);
	}

	private void executeToggle(Supplier<Boolean> getter, String key, Runnable... actions)
	{
		configManager.setConfiguration(GROUP_NAME, key, !Boolean.TRUE.equals(getter.get()));

		for (Runnable action : actions)
		{
			action.run();
		}

		updateCustomChildren();
	}

	private void createCustomChildren()
	{
		Widget parent = widgetManager.getCurrentParent();

		if (parent == null)
		{
			clearCustomChildren();
			lastParentId = -1;
			return;
		}

		if (parent.getId() != lastParentId)
		{
			clearCustomChildren();
			lastParentId = parent.getId();
		}

		//only create widgets if they're missing from the parent
		if (widgetManager.missing(compassFrame, parent))
		{
			final int MINIMAP_BUTTON_X = 190;
			final int MINIMAP_BUTTON_Y = 180;
			final int COMPASS_BUTTON_X = 156;
			final int COMPASS_BUTTON_Y = 32;
			final int BUTTON_SIZE = 17;
			final int COMPASS_FRAME_SIZE = 43;
			final int OPACITY_DEFAULT = 0;
			final int OPACITY_HOVERED = 160;
			final int OFFSET_X = 4;
			final int OFFSET_Y = 14;

			compassFrame = widgetManager.createGraphic(
				parent,
				COMPASS_X - OFFSET_X, COMPASS_Y - OFFSET_Y,
				COMPASS_FRAME_SIZE, COMPASS_FRAME_SIZE,
				OPACITY_DEFAULT,
				config.hideToggle(),
				COMPASS_FRAME_SPRITE_ID
			);

			mapButton = widgetManager.createGraphic(
				parent,
				MINIMAP_BUTTON_X, MINIMAP_BUTTON_Y,
				BUTTON_SIZE, BUTTON_SIZE,
				OPACITY_HOVERED,
				config.hideToggle(),
				getSpriteId(isMinimapHidden())
			);

			mapMenu = widgetManager.createMenu(
				parent,
				MINIMAP_BUTTON_X, MINIMAP_BUTTON_Y,
				config.hideToggle(),
				getMenuOption(MINIMAP_CONFIG_KEY),
				e -> onMinimapToggle(),
				e -> mapButton.setOpacity(OPACITY_DEFAULT),
				e -> mapButton.setOpacity(OPACITY_HOVERED)
			);

			compassButton = widgetManager.createGraphic(
				parent,
				COMPASS_BUTTON_X, COMPASS_BUTTON_Y,
				BUTTON_SIZE, BUTTON_SIZE,
				OPACITY_DEFAULT,
				config.hideToggle(),
				getSpriteId(isCompassHidden())
			);

			compassMenu = widgetManager.createMenu(
				parent,
				COMPASS_BUTTON_X, COMPASS_BUTTON_Y,
				config.hideToggle(),
				getMenuOption(COMPASS_CONFIG_KEY),
				e -> onCompassToggle(),
				null,
				null
			);
		}
	}

	public void updateCustomChildren()
	{
		boolean hideFrame = !isMinimapHidden() || isCompassHidden();
		boolean hideToggle = config.hideToggle();

		if (compassFrame != null)
		{
			compassFrame.setHidden(hideFrame);
		}

		if (mapButton != null)
		{
			mapButton.setSpriteId(getSpriteId(!isMinimapHidden()));
			mapButton.setHidden(hideToggle);

			if (mapMenu != null)
			{
				mapMenu.setAction(0, getMenuOption(MINIMAP_CONFIG_KEY));
				mapMenu.setHidden(hideToggle);
			}
		}

		if (compassButton != null)
		{
			compassButton.setSpriteId(getSpriteId(!isCompassHidden()));
			compassButton.setHidden(hideToggle || !isMinimapHidden());

			if (compassMenu != null)
			{
				compassMenu.setAction(0, getMenuOption(COMPASS_CONFIG_KEY));
				compassMenu.setHidden(hideToggle || !isMinimapHidden());
			}
		}
	}

	public void clearCustomChildren()
	{
		//modern
		widgetManager.clearChildren(InterfaceID.TOPLEVEL_PRE_EOC, TOP_LEVEL_MINIMAP_CHILD);

		//classic
		widgetManager.clearChildren(InterfaceID.TOPLEVEL_OSRS_STRETCH, TOP_LEVEL_MINIMAP_CHILD);

		lastParentId = -1;

		mapButton = null;
		mapMenu = null;
		compassButton = null;
		compassMenu = null;
		compassFrame = null;
	}

	//sprites mirror config state (visible = eye, hidden = crossed eye)
	private int getSpriteId(boolean toggle)
	{
		return !toggle ? SpriteID.GroundItemsVisibility._1 : SpriteID.GroundItemsVisibility._0;
	}

	private String getMenuOption(String key)
	{
		final boolean isMinimap = MINIMAP_CONFIG_KEY.equals(key);
		final boolean isHidden = isMinimap ? isMinimapHidden() : isCompassHidden();

		String target = isMinimap ? "Minimap" : "Compass";
		String action = isHidden ? "Show" : "Hide";

		return action + " " + ColorUtil.wrapWithColorTag(target, MENU_COLOR);
	}

	public boolean isMinimapHidden()
	{
		return config.hideMinimap();
	}

	public boolean isCompassHidden()
	{
		return config.hideCompass();
	}

}
