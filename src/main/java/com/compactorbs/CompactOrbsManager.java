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

import com.compactorbs.CompactOrbsConfig.OrbLayout;
import com.compactorbs.CompactOrbsConfig.HorizontalPosition;
import com.compactorbs.CompactOrbsConfig.VerticalPosition;
import static com.compactorbs.CompactOrbsConstants.ConfigGroup.GROUP_NAME;
import com.compactorbs.CompactOrbsConstants.ConfigGroup;
import com.compactorbs.CompactOrbsConstants.ConfigKeys;
import com.compactorbs.CompactOrbsConstants.Layout;
import com.compactorbs.CompactOrbsConstants.Menu;
import com.compactorbs.CompactOrbsConstants.Script;
import com.compactorbs.CompactOrbsConstants.Sprite;
import com.compactorbs.CompactOrbsConstants.Varbit;
import com.compactorbs.CompactOrbsConstants.VarbitValue;
import com.compactorbs.CompactOrbsConstants.Widgets.Classic;
import com.compactorbs.CompactOrbsConstants.Widgets.Modern;
import com.compactorbs.CompactOrbsConstants.Widgets.Orb;
import com.compactorbs.widget.TargetWidget;
import com.compactorbs.widget.WidgetManager;
import com.compactorbs.widget.elements.Compass;
import com.compactorbs.widget.elements.Minimap;
import com.compactorbs.widget.elements.Orbs;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
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

	//store the parent id from the previous widget (modern vs classic): -1 = no parent
	private int previousParentId = -1;

	//offsets for vertical and horizontal layout
	public static int verticalOffset = 0;
	public static int horizontalOffset = 0;

	//offset used to hide the world map outside the container
	public static int worldMapOffset = 0;

	//update flag for the custom widgets
	public boolean pendingChildrenUpdate = false;

	//custom widgets created when in compact layout
	private Widget compassFrame = null;
	private Widget minimapButton = null;
	private Widget compassButton = null;

	private final Map<String, Map.Entry<Supplier<Boolean>, TargetWidget[]>> hideByConfigMap = new HashMap<>();
	private final Map<Integer, Map.Entry<Supplier<Boolean>, TargetWidget[]>> hideByScriptMap = new HashMap<>();

	public void init(int scriptId)
	{
		updateWikiBanner(config.hideWiki());
		updateOrbByScript(scriptId);

		if (!client.isResized())
		{
			updateWorldMap(true);

			//revert changes made when in, or switching to, fixed mode
			if (previousParentId != -1)
			{
				widgetManager.remapTargets(Orbs.FIXED, false, Script.FORCE_UPDATE);
				previousParentId = -1;
			}
			return;
		}

		build(scriptId);
	}

	private void build(int scriptId)
	{
		if (!client.isResized())
		{
			return;
		}

		getLayoutOffsets();

		createCustomChildren();

		if (scriptId == Script.FORCE_UPDATE)
		{
			widgetManager.setTargetsHidden(isMinimapHidden(), Minimap.values());
			widgetManager.setTargetsHidden((isMinimapHidden() && isCompassHidden()), Compass.values());
			widgetManager.remapTargets(Compass.ALL, isMinimapHidden(), Script.FORCE_UPDATE);
			widgetManager.remapTargets(Orbs.ALL, isMinimapHidden(), Script.FORCE_UPDATE);
		}
		else
		{
			widgetManager.remapTargets(Orbs.ALL, isMinimapHidden(), scriptId);
		}

		updateCustomChildren(pendingChildrenUpdate || scriptId == Script.FORCE_UPDATE);
	}

	public void reset()
	{
		verticalOffset = 0;
		horizontalOffset = 0;
		worldMapOffset = 0;
		updateWikiBanner(false);

		clearCustomChildren();

		hideByConfigMap.clear();
		hideByScriptMap.clear();

		//set all orbs to visible
		widgetManager.setTargetsHidden(false, Orbs.values());

		if (!client.isResized())
		{
			updateWorldMap(false);
		}

		if (isMinimapHidden())
		{
			widgetManager.setTargetsHidden(false, Minimap.values());

			if (isCompassHidden())
			{
				widgetManager.setTargetsHidden(false, Compass.values());
			}

			widgetManager.remapTargets(Compass.ALL, false, Script.FORCE_UPDATE);
		}

		//protect certain fixed mode orbs from being changed when in fixed mode
		widgetManager.remapTargets((client.isResized() ? Orbs.ALL : Orbs.FIXED), false, Script.FORCE_UPDATE);
	}

	//toggle the minimap visibility, and update related widgets when using the custom toggle button
	private void onMinimapToggle()
	{
		boolean toggle = !isMinimapHidden();
		boolean remapCondition = toggle && !isCompassHidden();
		boolean hiddenCondition = toggle && isCompassHidden();
		executeToggle(
			config::hideMinimap, ConfigKeys.MINIMAP,
			this::getLayoutOffsets,
			() -> widgetManager.setTargetsHidden(toggle, Minimap.values()),
			() -> widgetManager.remapTargets(Compass.ALL, remapCondition, Script.FORCE_UPDATE),
			() -> widgetManager.setTargetsHidden(hiddenCondition, Compass.values()),
			() -> widgetManager.remapTargets(Orbs.ALL, toggle, Script.FORCE_UPDATE),
			() -> updateCustomChildren(true)
		);
	}

	//toggle the compass visibility, and update related widgets when using the custom toggle button
	private void onCompassToggle()
	{
		boolean toggle = !isCompassHidden();
		boolean remapCondition = toggle || isMinimapHidden();
		executeToggle(
			config::hideCompass, ConfigKeys.COMPASS,
			this::getLayoutOffsets,
			() -> widgetManager.remapTargets(Compass.ALL, remapCondition, Script.FORCE_UPDATE),
			() -> widgetManager.setTargetsHidden(toggle, Compass.values()),
			() -> updateCustomChildren(true)
		);
	}

	//flip the config (key) and execute the chain of actions when using the custom toggle buttons
	private void executeToggle(Supplier<Boolean> getter, String key, Runnable... actions)
	{
		configManager.setConfiguration(GROUP_NAME, key, !Boolean.TRUE.equals(getter.get()));

		for (Runnable action : actions)
		{
			action.run();
		}
	}

	//create the compass frame and toggle buttons, clearing them if the parent id changed,
	//and only creating widgets if missing from the current parent
	public void createCustomChildren()
	{
		Widget parent = widgetManager.getCurrentParent();

		if (parent == null || parent.getId() != previousParentId)
		{
			clearCustomChildren();
			previousParentId = parent != null ? parent.getId() : -1;
			if (parent == null)
			{
				return;
			}
		}

		if (widgetManager.isMissing(compassFrame, parent))
		{
			compassFrame = widgetManager.createGraphic(
				parent,
				0, 0, //handled in updateCustomChildren()
				Layout.FRAME_WIDTH, Layout.FRAME_HEIGHT,
				Layout.OPACITY,
				Sprite.COMPASS_FRAME
			);
		}

		if (widgetManager.isMissing(minimapButton, parent))
		{
			minimapButton = widgetManager.createToggleButton(
				parent,
				0, 0, //handled in updateCustomChildren()
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
				0, 0, //handled in updateCustomChildren()
				Layout.TOGGLE_BUTTON_WIDTH, Layout.TOGGLE_BUTTON_HEIGHT,
				Layout.OPACITY,
				getSpriteId(isCompassHidden()),
				getMenuOption(ConfigKeys.COMPASS),
				e -> onCompassToggle(),
				e -> compassButton.setOpacity(Layout.OPACITY_HOVER),
				e -> compassButton.setOpacity(Layout.OPACITY)
			);

			//prevent de-sync
			pendingChildrenUpdate = true;
		}
	}

	//handle dynamic widget changes for visibility, positions, sprites, and menu actions for the
	//compass frame and toggle buttons (logout X being an exception in native minimap hiding)
	public void updateCustomChildren(boolean shouldUpdate)
	{
		//missing children -> create them; prevent updates until all exist
		if (compassFrame == null || compassButton == null || minimapButton == null)
		{
			//will trigger pendingChildrenUpdate when custom widgets are created
			//for the next updateCustomChildren call
			createCustomChildren();
			return;
		}

		boolean hideFrame = !isMinimapHidden() || isCompassHidden() || isMinimized();
		boolean hideToggles = (config.hideMinimapToggle() && config.hideCompassToggle()) || isMinimized();

		if (isMinimized())
		{
			//put logout button back when switching to native hiding
			widgetManager.remapTargets(
				List.of(Orbs.LOGOUT_X_ICON, Orbs.LOGOUT_X_STONE),
				false, Script.FORCE_UPDATE);
		}

		//make changes when pendingChildrenUpdate (or shouldUpdate = true is passed)
		if (shouldUpdate)
		{
			//compass frame
			compassFrame.setHidden(hideFrame);
			widgetManager.updateValue(compassFrame::getOriginalX, compassFrame::setOriginalX, getCompassFrameX());
			widgetManager.updateValue(compassFrame::getOriginalY, compassFrame::setOriginalY, getCompassFrameY());
			compassFrame.revalidate();

			//minimap button
			minimapButton.setSpriteId(getSpriteId(!isMinimapHidden()));
			minimapButton.setHidden(hideToggles || config.hideMinimapToggle());
			minimapButton.setAction(0, getMenuOption(ConfigKeys.MINIMAP));
			widgetManager.updateValue(minimapButton::getOriginalX, minimapButton::setOriginalX, getMinimapButtonX());
			widgetManager.updateValue(minimapButton::getOriginalY, minimapButton::setOriginalY, getMinimapButtonY());
			minimapButton.revalidate();

			//compass button
			compassButton.setSpriteId(getSpriteId(!isCompassHidden()));
			compassButton.setHidden((hideToggles || config.hideCompassToggle()) || !isMinimapHidden());
			compassButton.setAction(0, getMenuOption(ConfigKeys.COMPASS));
			widgetManager.updateValue(compassButton::getOriginalX, compassButton::setOriginalX, getCompassButtonX());
			widgetManager.updateValue(compassButton::getOriginalY, compassButton::setOriginalY, getCompassButtonY());
			compassButton.revalidate();

			//clear update flag
			pendingChildrenUpdate = false;
		}
	}

	//clear any created children and reset previous parent id
	private void clearCustomChildren()
	{
		widgetManager.clearChildren(Modern.ORBS);
		widgetManager.clearChildren(Classic.ORBS);

		previousParentId = -1;

		compassFrame = null;
		minimapButton = null;
		compassButton = null;
	}

	//show or hide the wiki banner (vanilla or plugin) based on which exists
	//vanilla = official wiki banner
	public void updateWikiBanner(boolean hidden)
	{
		boolean wikiPluginActive = Boolean.TRUE.equals(
			configManager.getConfiguration(ConfigGroup.RuneLite.GROUP_NAME,
				ConfigKeys.RuneLite.WIKI_PLUGIN, Boolean.class)
		);

		boolean showWikiMinimapButton =
			configManager.getConfiguration(ConfigGroup.Wiki.GROUP_NAME,
				ConfigKeys.Wiki.SHOW_WIKI_MINIMAP_BUTTON, Boolean.class
			);

		//container holding the vanilla or wiki banner
		Widget container = widgetManager.getTargetWidget(Orbs.WIKI_ICON_CONTAINER);
		if (container == null)
		{
			return;
		}

		Widget banner = null;
		boolean vanilla = true;

		//check to see if wiki banner exists
		if (container.getDynamicChildren() != null &&
			container.getDynamicChildren().length > 0)
		{
			banner = container.getChild(0);
			vanilla = false;
		}

		//wiki plugin banner doesn't exist, check vanilla
		if (banner == null)
		{
			banner = widgetManager.getTargetWidget(Orbs.WIKI_VANILLA_CONTAINER);
		}

		//guard if either are null
		if (banner == null)
		{
			return;
		}

		boolean shouldHide = hidden || (wikiPluginActive && showWikiMinimapButton && vanilla);
		banner.setHidden(shouldHide);
	}

	//hide world map without breaking the 'Ctrl+M' hotkey; resizable x/y handled via remapTargets
	//@modify - if changes should be made
	public void updateWorldMap(boolean modify)
	{
		//get the worldMapOffset for fixed/resizable
		getWorldMapOffset();

		//prevent fixed mode changes in resizable
		if (client.isResized())
		{
			return;
		}

		Widget worldMap = client.getWidget(Orb.WORLD_MAP);
		if (worldMap == null)
		{
			return;
		}

		int x = config.hideWorld() && modify ? worldMapOffset : Layout.FIXED_WORLD_MAP_X;
		int y = config.hideWorld() && modify ? worldMapOffset : Layout.FIXED_WORLD_MAP_Y;

		widgetManager.updateValue(worldMap::getOriginalX, worldMap::setOriginalX, x);
		widgetManager.updateValue(worldMap::getOriginalY, worldMap::setOriginalY, y);
		worldMap.revalidate();
	}

	private void getLayoutOffsets()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		verticalOffset = Layout.Vertical.RIGHT_OFFSET;
		horizontalOffset = Layout.Horizontal.TOP_OFFSET;

		if (isVerticalLayout() &&
			config.verticalPosition() == VerticalPosition.LEFT)
		{
			verticalOffset = (client.isResized() && isMinimapHidden()) ?
				Layout.Vertical.LEFT_OFFSET : Layout.Vertical.RIGHT_OFFSET;
		}

		if (isHorizontalLayout() &&
			config.horizontalPosition() == HorizontalPosition.BOTTOM)
		{
			horizontalOffset = (client.isResized() && isMinimapHidden()) ?
				Layout.Horizontal.BOTTOM_OFFSET : Layout.Horizontal.TOP_OFFSET;
		}

		getWorldMapOffset();
	}

	private void getWorldMapOffset()
	{
		worldMapOffset = (config.hideWorld() ? 1 - Layout.WORLD_MAP_CONTAINER_WIDTH : 0);
	}

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

	private boolean isVerticalLayout()
	{
		return config.layout() == OrbLayout.VERTICAL;
	}

	private boolean isHorizontalLayout()
	{
		return config.layout() == OrbLayout.HORIZONTAL;
	}

	public boolean isMinimapHidden()
	{
		return config.hideMinimap();
	}

	private boolean isCompassHidden()
	{
		return config.hideCompass();
	}

	//in-game native minimap hiding
	public boolean isMinimized()
	{
		return client.getVarbitValue(Varbit.MINIMAP_TOGGLE) == VarbitValue.MINIMAP_MINIMIZED;
	}

	private int getCompassX()
	{
		switch (config.layout())
		{
			case VERTICAL: return Layout.Vertical.COMPASS_X;
			case HORIZONTAL: return Layout.Horizontal.COMPASS_X;
			default: return Layout.Original.COMPASS_X;
		}
	}

	private int getCompassY()
	{
		switch (config.layout())
		{
			case VERTICAL: return Layout.Vertical.COMPASS_Y;
			case HORIZONTAL: return Layout.Horizontal.COMPASS_Y;
			default: return Layout.Original.COMPASS_Y;
		}
	}

	private int getCompassFrameX()
	{
		return (getCompassX() - Layout.FRAME_X_OFFSET) - verticalOffset;
	}

	private int getCompassFrameY()
	{
		return (getCompassY() - Layout.FRAME_Y_OFFSET) + horizontalOffset;
	}

	private int getMinimapButtonX()
	{
		return (isVerticalLayout() && isMinimapHidden())
			? Layout.MINIMAP_BUTTON_X - verticalOffset
			: Layout.MINIMAP_BUTTON_X;
	}

	private int getMinimapButtonY()
	{
		return (isHorizontalLayout() && isMinimapHidden())
			? (Layout.MINIMAP_BUTTON_Y / 2) + horizontalOffset
			: Layout.MINIMAP_BUTTON_Y;
	}

	private int getCompassButtonX()
	{
		int x = getCompassX()
			+ Layout.COMPASS_BUTTON_X_OFFSET;

		if (isHorizontalLayout())
		{
			x += Layout.COMPASS_BUTTON_HORIZONTAL_X_OFFSET;
		}

		return x - verticalOffset;
	}

	private int getCompassButtonY()
	{
		int y = getCompassY()
			+ Layout.COMPASS_BUTTON_Y_OFFSET;

		if (isHorizontalLayout())
		{
			y -= Layout.COMPASS_BUTTON_HORIZONTAL_Y_OFFSET;
		}

		return y + horizontalOffset;
	}

	//check if a cutscene is active
	public boolean isCutSceneActive()
	{
		return client.getVarbitValue(Varbit.CUTSCENE_STATUS) == VarbitValue.CUTSCENE_ACTIVE;
	}

	//register an orb toggle entry in the config and script maps
	public void registerOrbToggle(String key, Supplier<Boolean> toggle, TargetWidget... targets)
	{
		hideByConfigMap.put(key, Map.entry(toggle, targets));

		int scriptId = Script.FORCE_UPDATE;
		for (TargetWidget target : targets)
		{
			if (target instanceof Orbs)
			{
				scriptId = target.getScriptId();
				break;
			}
		}

		if (scriptId != Script.FORCE_UPDATE)
		{
			hideByScriptMap.put(scriptId, Map.entry(toggle, targets));
		}
	}

	//apply toggle setting to orbs based on the fired script id; pass FORCE_UPDATE for all toggles
	private void updateOrbByScript(int scriptId)
	{
		(scriptId == Script.FORCE_UPDATE ? hideByConfigMap : hideByScriptMap).forEach(
			(key, value) ->
				widgetManager.setTargetsHidden(value.getKey().get(), value.getValue())
		);
	}

	//apply toggle setting to orbs based on the config key in onConfigChanged
	public void updateOrbByConfig(String key)
	{
		Map.Entry<Supplier<Boolean>, TargetWidget[]> entry = hideByConfigMap.get(key);
		if (entry != null)
		{
			widgetManager.setTargetsHidden(entry.getKey().get(), entry.getValue());
		}
	}

}
