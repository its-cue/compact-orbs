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

import static com.compactorbs.CompactOrbsConstants.GROUP_NAME;
import com.compactorbs.CompactOrbsConstants.ConfigKeys;
import com.compactorbs.CompactOrbsConstants.Layout;
import com.compactorbs.CompactOrbsConstants.Menu;
import com.compactorbs.CompactOrbsConstants.Script;
import com.compactorbs.CompactOrbsConstants.Sprite;
import com.compactorbs.CompactOrbsConstants.Widget.Classic;
import com.compactorbs.CompactOrbsConstants.Widget.Modern;
import com.compactorbs.widget.WidgetManager;
import com.compactorbs.widget.elements.Compass;
import com.compactorbs.widget.elements.Minimap;
import com.compactorbs.widget.elements.Orbs;
import java.util.List;
import java.util.function.Supplier;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
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

	//in-game native minimap hiding
	public boolean minimapMinimized;

	public boolean resetFixedOrbs = false;

	private Widget compassFrame = null;
	private Widget minimapButton = null;
	private Widget compassButton = null;

	public void init(int scriptId)
	{
		//revert changes made when in, or switching to, fixed mode
		if (!client.isResized())
		{
			if (!resetFixedOrbs)
			{
				//reset only the required orbs when toggling to fixed mode display
				widgetManager.remapTargets(Orbs.FIXED, false, Script.FORCE_UPDATE);
				resetFixedOrbs = true;
			}
			return;
		}
		resetFixedOrbs = false;

		build(scriptId);
	}

	public void build(int scriptId)
	{
		if (!client.isResized())
		{
			return;
		}

		if (scriptId == Script.FORCE_UPDATE)
		{
			createCustomChildren();

			widgetManager.setTargetsHidden(isMinimapHidden(), Minimap.values());
			widgetManager.setTargetsHidden((isMinimapHidden() && isCompassHidden()), Compass.values());
			widgetManager.remapTargets(Compass.ALL, isMinimapHidden(), Script.FORCE_UPDATE);
			widgetManager.remapTargets(Orbs.ALL, isMinimapHidden(), Script.FORCE_UPDATE);

			updateCustomChildren();
		}
		else
		{
			widgetManager.remapTargets(Orbs.ALL, isMinimapHidden(), scriptId);
		}
	}

	public void reset()
	{
		resetFixedOrbs = false;

		clearCustomChildren();

		if (isMinimapHidden())
		{
			widgetManager.setTargetsHidden(false, Minimap.values());

			if (isCompassHidden())
			{
				widgetManager.setTargetsHidden(false, Compass.values());
			}

			widgetManager.remapTargets(Compass.ALL, false, Script.FORCE_UPDATE);
		}

		widgetManager.remapTargets((client.isResized() ? Orbs.ALL : Orbs.FIXED), false, Script.FORCE_UPDATE);
	}

	private void onMinimapToggle()
	{
		boolean toggle = !isMinimapHidden();
		boolean remapCondition = toggle && !isCompassHidden();
		boolean hiddenCondition = toggle && isCompassHidden();
		executeToggle(
			config::hideMinimap, ConfigKeys.MINIMAP,
			() -> widgetManager.setTargetsHidden(toggle, Minimap.values()),
			() -> widgetManager.remapTargets(Compass.ALL, remapCondition, Script.FORCE_UPDATE),
			() -> widgetManager.setTargetsHidden(hiddenCondition, Compass.values()),
			() -> widgetManager.remapTargets(Orbs.ALL, toggle, Script.FORCE_UPDATE),
			this::updateCustomChildren
		);
	}

	private void onCompassToggle()
	{
		boolean toggle = !isCompassHidden();
		boolean remapCondition = toggle || isMinimapHidden();
		executeToggle(
			config::hideCompass, ConfigKeys.COMPASS,
			() -> widgetManager.remapTargets(Compass.ALL, remapCondition, Script.FORCE_UPDATE),
			() -> widgetManager.setTargetsHidden(toggle, Compass.values()),
			this::updateCustomChildren
		);
	}

	private void executeToggle(Supplier<Boolean> getter, String key, Runnable... actions)
	{
		configManager.setConfiguration(GROUP_NAME, key, !Boolean.TRUE.equals(getter.get()));

		for (Runnable action : actions)
		{
			action.run();
		}
	}

	public void createCustomChildren()
	{
		Widget parent = widgetManager.getCurrentParent();

		if (parent == null || parent.getId() != lastParentId)
		{
			clearCustomChildren();
			lastParentId = parent != null ? parent.getId() : -1;
			if (parent == null)
			{
				return;
			}
		}

		if (widgetManager.isMissing(compassFrame, parent))
		{
			compassFrame = widgetManager.createGraphic(
				parent,
				Layout.COMPASS_FRAME_X, Layout.COMPASS_FRAME_Y,
				Layout.FRAME_WIDTH, Layout.FRAME_HEIGHT,
				Layout.OPACITY,
				Sprite.COMPASS_FRAME
			);
		}

		if (widgetManager.isMissing(minimapButton, parent))
		{
			minimapButton = widgetManager.createToggleButton(
				parent,
				Layout.MINIMAP_BUTTON_X, Layout.MINIMAP_BUTTON_Y,
				Layout.TOGGLE_BUTTON_WIDTH, Layout.TOGGLE_BUTTON_HEIGHT,
				Layout.OPACITY,
				getSpriteId(isMinimapHidden()),
				getMenuOption(ConfigKeys.MINIMAP),
				e -> onMinimapToggle(),
				e -> minimapButton.setOpacity(Layout.OPACITY_HOVER),
				e -> minimapButton.setOpacity(Layout.OPACITY)
			);
		}

		if (widgetManager.isMissing(compassButton, parent))
		{
			compassButton = widgetManager.createToggleButton(
				parent,
				Layout.COMPASS_BUTTON_X, Layout.COMPASS_BUTTON_Y,
				Layout.TOGGLE_BUTTON_WIDTH, Layout.TOGGLE_BUTTON_HEIGHT,
				Layout.OPACITY,
				getSpriteId(isCompassHidden()),
				getMenuOption(ConfigKeys.COMPASS),
				e -> onCompassToggle(),
				e -> compassButton.setOpacity(Layout.OPACITY_HOVER),
				e -> compassButton.setOpacity(Layout.OPACITY)
			);
		}
	}

	public void updateCustomChildren()
	{
		boolean hideFrame = !isMinimapHidden() || isCompassHidden() || isMinimized();
		boolean hideToggle = config.hideToggle() || isMinimized();

		if (isMinimized())
		{
			//put logout button back when switching to native hiding
			widgetManager.remapTargets(
				List.of(Orbs.LOGOUT_X_ICON, Orbs.LOGOUT_X_STONE),
				false, Script.FORCE_UPDATE);
		}

		if (compassFrame != null)
		{
			compassFrame.setHidden(hideFrame);
		}

		if (minimapButton != null)
		{
			minimapButton.setSpriteId(getSpriteId(!isMinimapHidden()));
			minimapButton.setHidden(hideToggle);
			minimapButton.setAction(0, getMenuOption(ConfigKeys.MINIMAP));
		}

		if (compassButton != null)
		{
			compassButton.setSpriteId(getSpriteId(!isCompassHidden()));
			compassButton.setHidden(hideToggle || !isMinimapHidden());
			compassButton.setAction(0, getMenuOption(ConfigKeys.COMPASS));
		}
	}

	public void clearCustomChildren()
	{
		widgetManager.clearChildren(Modern.ORBS);
		widgetManager.clearChildren(Classic.ORBS);

		lastParentId = -1;

		compassFrame = null;
		minimapButton = null;
		compassButton = null;
	}

	//visible = open eye, hidden = crossed eye
	private int getSpriteId(boolean hidden)
	{
		return hidden ? Sprite.VISIBLE : Sprite.HIDDEN;
	}

	private String getMenuOption(String key)
	{
		final boolean isMinimap = ConfigKeys.MINIMAP.equals(key);
		final boolean isHidden = isMinimap ? isMinimapHidden() : isCompassHidden();

		String target = isMinimap ? Menu.SUFFIX_MINIMAP : Menu.SUFFIX_COMPASS;
		String action = isHidden ? Menu.PREFIX_SHOW : Menu.PREFIX_HIDE;

		return action + " " + ColorUtil.wrapWithColorTag(target, Menu.COLOR);
	}

	public boolean isMinimapHidden()
	{
		return config.hideMinimap();
	}

	public boolean isCompassHidden()
	{
		return config.hideCompass();
	}

	//in-game native minimap hiding
	public boolean isMinimized()
	{
		return minimapMinimized;
	}
}
