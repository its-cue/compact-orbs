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
import com.compactorbs.CompactOrbsConfig.TogglePlacement;
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
import com.compactorbs.widget.offset.Offsets;
import com.compactorbs.widget.slot.Slot;
import com.compactorbs.widget.slot.SlotManager;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.util.ColorUtil;

@Slf4j
@Singleton
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

	@Inject
	private SlotManager slotManager;

	//store the parent id from the previous widget (modern vs classic): -1 = no parent
	private int previousParentId = -1;

	//offsets for vertical and horizontal layout
	public int verticalOffset;
	public int horizontalOffset;

	//reset shutdown flag for the world map orb
	public boolean hideWorldMap;

	//update flag for the custom widgets
	public boolean pendingChildrenUpdate;

	//custom widgets created when in compact layout
	private Widget compassFrame;
	private Widget minimapButton;
	private Widget compassButton;

	private final Map<String, Map.Entry<Supplier<Boolean>, TargetWidget[]>> hideByConfigMap = new HashMap<>();
	private final Map<Integer, Map.Entry<Supplier<Boolean>, TargetWidget[]>> hideByScriptMap = new HashMap<>();

	public Map<TargetWidget, Supplier<Boolean>> orbToToggle = new HashMap<>();

	public void init(int scriptId)
	{
		updateWikiBanner(config.hideWiki());

		updateOrbByScript(scriptId);

		if (!isResized())
		{
			if (previousParentId != -1 || slotManager.allowFixedModeUpdate)
			{
				//update on initial load, when in fixed mode
				widgetManager.remapTargets(false, Script.FORCE_UPDATE, Orbs.FIXED.toArray(Orbs[]::new));
				widgetManager.remapTarget(Orbs.WORLD_MAP_CONTAINER, false);

				//reset update flags
				previousParentId = -1;
				slotManager.allowFixedModeUpdate = false;
			}
			return;
		}

		build(scriptId);
	}

	private void build(int scriptId)
	{
		if (!isResized())
		{
			return;
		}

		getLayoutOffsets();

		createCustomChildren();

		if (scriptId == Script.FORCE_UPDATE)
		{
			widgetManager.setTargetsHidden(isMinimapHidden(), Minimap.values());
			widgetManager.setTargetsHidden((isMinimapHidden() && isCompassHidden()), Compass.values());
			widgetManager.remapTargets(isMinimapHidden(), Script.FORCE_UPDATE, Compass.values());
			widgetManager.remapTargets(isMinimapHidden(), Script.FORCE_UPDATE, Orbs.values());//?
		}
		else
		{
			widgetManager.remapTargets(isMinimapHidden(), scriptId, Orbs.values());
		}

		updateCustomChildren(pendingChildrenUpdate || scriptId == Script.FORCE_UPDATE);
	}

	public void reset()
	{
		verticalOffset = 0;
		horizontalOffset = 0;

		hideWorldMap = false;

		updateWikiBanner(false);

		clearCustomChildren();

		hideByConfigMap.clear();
		hideByScriptMap.clear();

		//set all orbs to visible
		widgetManager.setTargetsHidden(false, Orbs.values());

		slotManager.reset();

		if (!isResized())
		{
			widgetManager.remapTargets(false, Script.FORCE_UPDATE, Orbs.WORLD_MAP_CONTAINER);
		}

		if (isMinimapHidden())
		{
			widgetManager.setTargetsHidden(false, Minimap.values());

			if (isCompassHidden())
			{
				widgetManager.setTargetsHidden(false, Compass.values());
			}

			widgetManager.remapTargets(false, Script.FORCE_UPDATE, Compass.values());
		}

		//protect certain fixed mode orbs from being changed when in fixed mode
		widgetManager.remapTargets(false, Script.FORCE_UPDATE,
			(isResized() ? Orbs.values() : Orbs.FIXED.toArray(Orbs[]::new)));
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
			() -> widgetManager.remapTargets(remapCondition, Script.FORCE_UPDATE, Compass.values()),
			() -> widgetManager.setTargetsHidden(hiddenCondition, Compass.values()),
			() -> widgetManager.remapTargets(toggle, Script.FORCE_UPDATE, Orbs.values()),
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
			() -> widgetManager.remapTargets(remapCondition, Script.FORCE_UPDATE, Compass.values()),
			() -> widgetManager.setTargetsHidden(toggle, Compass.values()),
			() -> updateCustomChildren(true)
		);
	}

	//flip the config (key) and execute the chain of actions when using the custom toggle buttons
	private void executeToggle(Supplier<Boolean> getter, String key, Runnable... actions)
	{
		configManager.setConfiguration(ConfigGroup.GROUP_NAME, key, !Boolean.TRUE.equals(getter.get()));

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

		if (isMinimized())
		{
			//put logout button back when switching to native hiding
			widgetManager.remapTargets(false, Script.FORCE_UPDATE, Orbs.LOGOUT_X_ICON, Orbs.LOGOUT_X_STONE);
		}

		//make changes when pendingChildrenUpdate (or shouldUpdate = true is passed)
		if (shouldUpdate)
		{
			updateCompassFrameChild();
			updateMinimapToggleButton();
			updateCompassToggleButton();

			//clear update flag
			pendingChildrenUpdate = false;
		}
	}

	public void updateCompassFrameChild()
	{
		compassFrame.setHidden(hideCompassFrame());
		widgetManager.updateValue(compassFrame::getOriginalX, compassFrame::setOriginalX, getCompassFrameX());
		widgetManager.updateValue(compassFrame::getOriginalY, compassFrame::setOriginalY, getCompassFrameY());
		compassFrame.revalidate();
	}

	public void updateMinimapToggleButton()
	{
		minimapButton.setSpriteId(getSpriteId(!isMinimapHidden()));
		minimapButton.setHidden(hideCustomToggles() || config.hideMinimapToggle());
		minimapButton.setAction(0, getMenuOption(ConfigKeys.MINIMAP));
		widgetManager.updateValue(minimapButton::getOriginalX, minimapButton::setOriginalX, getMinimapButtonX());
		widgetManager.updateValue(minimapButton::getOriginalY, minimapButton::setOriginalY, getMinimapButtonY());
		minimapButton.revalidate();
	}

	public void updateCompassToggleButton()
	{
		compassButton.setSpriteId(getSpriteId(!isCompassHidden()));
		compassButton.setHidden((hideCustomToggles() || config.hideCompassToggle()) || !isMinimapHidden());
		compassButton.setAction(0, getMenuOption(ConfigKeys.COMPASS));
		widgetManager.updateValue(compassButton::getOriginalX, compassButton::setOriginalX, getCompassButtonX());
		widgetManager.updateValue(compassButton::getOriginalY, compassButton::setOriginalY, getCompassButtonY());
		compassButton.revalidate();
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
		if (isPluginWikiBanner(container))
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

	public boolean isPluginWikiBanner(Widget container)
	{
		if (container == null)
		{
			return false;
		}
		return (container.getDynamicChildren() != null || container.getDynamicChildren().length > 0);
	}

	private void getLayoutOffsets()
	{
		if (!isLoggedIn())
		{
			return;
		}

		verticalOffset = Layout.Vertical.RIGHT_OFFSET;

		if (isVerticalLayout() && isVerticalLeft())
		{
			verticalOffset = (isResized() && isMinimapHidden()) ?
				Layout.Vertical.LEFT_OFFSET : Layout.Vertical.RIGHT_OFFSET;
		}

		horizontalOffset = Layout.Horizontal.TOP_OFFSET;

		if (isHorizontalLayout() && isHorizontalBottom())
		{
			horizontalOffset = (isResized() && isMinimapHidden()) ?
				Layout.Horizontal.BOTTOM_OFFSET : Layout.Horizontal.TOP_OFFSET;
		}

		getWorldMapOffset();
	}

	public boolean updateWorldMap()
	{
		if (isResized())
		{
			return hideWorldMap;
		}

		Widget worldMap = client.getWidget(Orb.WORLD_MAP);
		if (worldMap == null)
		{
			return false;
		}

		return hideWorldMap;
	}

	public int getWorldMapOffset()
	{
		return (hideWorldMap ? 1 - Layout.WORLD_MAP_CONTAINER_WIDTH : 0);
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

	//returns true if client is in resizable mode
	public boolean isResized()
	{
		return client.isResized();
	}

	public boolean isLoggedIn()
	{
		return client.getGameState() == GameState.LOGGED_IN;
	}

	public boolean activityOrbIsVisibleSetting()
	{
		return client.getVarbitValue(Varbit.ACTIVITY_ORB_TOGGLE) == VarbitValue.ACTIVITY_ORB_VISIBLE;
	}

	public boolean storeOrbIsVisibleSetting()
	{
		return client.getVarbitValue(Varbit.STORE_ORB_TOGGLE) == VarbitValue.STORE_ORB_VISIBLE;
	}

	//returns the current layout (vertical or horizontal)
	public int getLayout()
	{
		return config.layout().getIndex();
	}

	//returns true if orb swapping is enabled
	public boolean enableOrbSwapping()
	{
		return config.enableOrbSwapping();
	}

	//returns true if layout is vertical
	public boolean isVerticalLayout()
	{
		return config.layout() == OrbLayout.VERTICAL;
	}

	//returns true if position is vertical left
	public boolean isVerticalLeft()
	{
		return config.verticalPosition() == VerticalPosition.LEFT;
	}

	//returns true if position is vertical right
	public boolean isVerticalRight()
	{
		return config.verticalPosition() == VerticalPosition.RIGHT;
	}

	//returns true if layout is horizontal
	public boolean isHorizontalLayout()
	{
		return config.layout() == OrbLayout.HORIZONTAL;
	}

	//returns true if position is horizontal bottom
	public boolean isHorizontalBottom()
	{
		return config.horizontalPosition() == HorizontalPosition.BOTTOM;
	}

	//returns true if layout is horizontal top
	public boolean isHorizontalTop()
	{
		return config.horizontalPosition() == HorizontalPosition.TOP;
	}

	//returns true if minimap is hidden
	public boolean isMinimapHidden()
	{
		return config.hideMinimap();
	}

	//returns true if compass is hidden
	public boolean isCompassHidden()
	{
		return config.hideCompass();
	}

	//returns true when compass frame should be hidden
	private boolean hideCompassFrame()
	{
		return !isMinimapHidden() || isCompassHidden() || isMinimized();
	}

	//returns true when toggle buttons should be hidden
	private boolean hideCustomToggles()
	{
		return (config.hideMinimapToggle() && config.hideCompassToggle()) || isMinimized();
	}

	//in-game native minimap hiding
	public boolean isMinimized()
	{
		return client.getVarbitValue(Varbit.MINIMAP_TOGGLE) == VarbitValue.MINIMAP_MINIMIZED;
	}

	//check if a cutscene is active
	public boolean isCutSceneActive()
	{
		return client.getVarbitValue(Varbit.CUTSCENE_STATUS) == VarbitValue.CUTSCENE_ACTIVE;
	}

	//custom children offset handling~ similar to OffsetTarget interface
	private int getCompassFrameX()
	{
		return Offsets.COMPASS.offsetTarget().getOffsetX() - Layout.FRAME_X_OFFSET;
	}

	private int getCompassFrameY()
	{
		return Offsets.COMPASS.offsetTarget().getOffsetY() - Layout.FRAME_Y_OFFSET;
	}

	private int getMinimapButtonX()
	{
		boolean vertical = isVerticalLayout() && isMinimapHidden();
		boolean horizontal = isHorizontalLayout() && isMinimapHidden();

		int x = config.minimapTogglePlacement().getX();

		if (config.minimapTogglePlacement() == TogglePlacement.BELOW_MAP
			&& config.hideStore()
			&& !isMinimapHidden())
		{
			//offset when store is hidden, and minimap is visible
			x -= 33;
		}

		if (vertical)
		{
			return Layout.DEFAULT_MINIMAP_BUTTON_X - verticalOffset;
		}

		if (horizontal)
		{
			x = Layout.DEFAULT_MINIMAP_BUTTON_X;

			if (config.hideWiki())
			{
				//offset when wiki is hidden, in horizontal layout
				x -= 40;
			}

			return x;
		}

		return x;
	}

	private int getMinimapButtonY()
	{
		boolean vertical = isVerticalLayout() && isMinimapHidden();
		boolean horizontal = isHorizontalLayout() && isMinimapHidden();

		int y = config.minimapTogglePlacement().getY();

		if (config.minimapTogglePlacement() == TogglePlacement.BELOW_MAP
			&& config.hideStore()
			&& !isMinimapHidden())
		{
			//offset when store is hidden and minimap is visible
			y -= 5;
		}

		if (vertical)
		{
			//original vertical y
			y = Layout.DEFAULT_MINIMAP_BUTTON_Y;

			if (isHorizontalTop())
			{
				y += horizontalOffset;

				//anchor minimap button to wiki icon container in vertical
				y = slotManager.applyHiddenOffset(Orbs.WIKI_ICON_CONTAINER, y);

				//apply an offset when the amount hidden, leaves wiki slot as the last
				//0-HP, 1-Pray, 2-Run, 3-Spec, 4-Wiki
				if (slotManager.getHiddenCountAbove(Orbs.WIKI_ICON_CONTAINER) == Slot.VERTICAL_RIGHT_COLUMN.indexOf(Slot.WIKI_SLOT))
				{
					//match wiki container offset @WikiContainerOffset
					y += 10;

					if (config.hideWiki())
					{
						//offset if wiki is hidden
						y -= 20;

						if (config.hideLogout())
						{
							//align to compass toggle, when X is hidden
							y -= 4;
						}
					}
				}
			}
			return y;
		}

		if (horizontal)
		{
			return (Layout.DEFAULT_MINIMAP_BUTTON_Y / 2) + horizontalOffset;
		}

		return y;
	}

	private int getCompassButtonX()
	{
		int x = Offsets.COMPASS.offsetTarget().getOffsetX()
			+ Layout.COMPASS_BUTTON_X_OFFSET;

		if (isHorizontalLayout())
		{
			x += Layout.COMPASS_BUTTON_HORIZONTAL_X_OFFSET;
		}
		return x;
	}

	private int getCompassButtonY()
	{
		int y = Offsets.COMPASS.offsetTarget().getOffsetY()
			+ Layout.COMPASS_BUTTON_Y_OFFSET;

		if (isHorizontalLayout())
		{
			y -= Layout.COMPASS_BUTTON_HORIZONTAL_Y_OFFSET;
		}

		return y;
	}

	//register an orb toggle entry in the config and script maps
	public void registerOrbToggle(String key, Supplier<Boolean> toggle, TargetWidget... targets)
	{
		hideByConfigMap.put(key, Map.entry(toggle, targets));

		int scriptId = Script.FORCE_UPDATE;

		for (TargetWidget target : targets)
		{
			//match orb to toggle, used for slots
			orbToToggle.put(target, toggle);

			if (scriptId == Script.FORCE_UPDATE && target instanceof Orbs)
			{
				scriptId = target.getScriptId();
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
			{
				//don't actually hide, since handled elsewhere
				if (key.equals(ConfigKeys.HIDE_WORLD) ||
					key.equals(ConfigKeys.HIDE_WIKI) ||
					key.equals(Script.WORLD_MAP_UPDATE) ||
					key.equals(Script.WIKI_ICON_UPDATE))
				{
					return;
				}

				widgetManager.setTargetsHidden(value.getKey().get(), value.getValue());
			}
		);
	}

	//apply toggle setting to orbs based on the config key in onConfigChanged
	public void updateOrbByConfig(String key)
	{
		//world map config handling for onConfigChanged
		if (key.equals(ConfigKeys.HIDE_WORLD))
		{
			hideWorldMap = config.hideWorld();
			widgetManager.remapTarget(Orbs.WORLD_MAP_CONTAINER, isMinimapHidden() && isResized());
		}
		//wiki banner config handling for onConfigChanged
		else if (key.equals(ConfigKeys.HIDE_WIKI))
		{
			updateWikiBanner(config.hideWiki());

			//update the minimap toggle button when in horizontal layout,
			//and minimap is hidden (offset is applied that needs updated)
			if (isHorizontalLayout() && isMinimapHidden())
			{
				updateMinimapToggleButton();
			}
		}
		else
		{
			Map.Entry<Supplier<Boolean>, TargetWidget[]> entry = hideByConfigMap.get(key);
			if (entry != null)
			{
				//set orb visibility based on config
				widgetManager.setTargetsHidden(entry.getKey().get(), entry.getValue());

				//update the minimap toggle button when hiding/showing store orb,
				//while minimap is hidden, and button position is below map
				if (key.equals(ConfigKeys.HIDE_STORE)
					&& config.minimapTogglePlacement() == TogglePlacement.BELOW_MAP
					&& !isMinimapHidden())
				{
					updateMinimapToggleButton();
				}
			}
		}
	}

	//update a slots config for the passed widget value @SlotManager
	public void updateConfigForSlot(Slot key, TargetWidget value)
	{
		if (key.getGetter() != null && key.getConfigKey() != null)
		{
			configManager.setConfiguration(ConfigGroup.GROUP_NAME, key.getConfigKey(), value);
		}
	}
}
