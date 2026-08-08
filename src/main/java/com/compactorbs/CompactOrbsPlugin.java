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

import com.compactorbs.CompactOrbsConfig.HotkeyOptions;
import com.compactorbs.CompactOrbsConfig.TogglePlacement;
import com.compactorbs.CompactOrbsConstants.ConfigGroup;
import static com.compactorbs.CompactOrbsConstants.ConfigGroup.GROUP_NAME;
import com.compactorbs.CompactOrbsConstants.ConfigKeys;
import static com.compactorbs.CompactOrbsConstants.Layout.EDIT_MODE_HIDDEN_OPACITY;
import com.compactorbs.CompactOrbsConstants.Script;
import com.compactorbs.CompactOrbsConstants.Varbit;
import com.compactorbs.CompactOrbsConstants.Widgets;
import com.compactorbs.CompactOrbsConstants.Widgets.Orb;
import com.compactorbs.widget.WidgetManager;
import com.compactorbs.widget.elements.Compass;
import com.compactorbs.widget.elements.Orbs;
import com.compactorbs.widget.layout.EditLayout;
import com.compactorbs.widget.layout.edit.drag.DragListener;
import com.compactorbs.widget.layout.edit.drag.DragState;
import com.compactorbs.widget.layout.slot.SlotManager;
import com.compactorbs.widget.layout.slot.SlotRegistry;
import com.compactorbs.widget.overlay.MinimapOverlay;
import com.google.inject.Provides;
import java.awt.event.KeyEvent;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.PluginChanged;
import net.runelite.client.events.ProfileChanged;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Compact Orbs",
	description = "Minimize the minimap and reposition the orbs into a compact view.",
	tags = {"compact", "orbs", "layout", "hide", "minimap", "resizable", "classic", "modern", "world", "map", "wiki", "swap", "overlay", "orb", "fixed"},
	conflicts = {"Fixed Resizable Hybrid", "Orb Hider", "Minimap Hider", "Movable Orbs"}
)
public class CompactOrbsPlugin extends Plugin implements KeyListener
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private CompactOrbsConfig config;

	@Inject
	private CompactOrbsManager manager;

	@Inject
	private ConfigManager configManager;

	@Inject
	private KeyManager keyManager;

	@Inject
	private MinimapOverlay minimapOverlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private WidgetManager widgetManager;

	@Inject
	private SlotManager slotManager;

	@Inject
	private SlotRegistry slotRegistry;

	@Inject
	private EditLayout editLayout;

	@Inject
	private DragListener dragListener;

	@Inject
	private DragState dragState;

	@Inject
	private MouseManager mouseManager;

	@Override
	protected void startUp() throws Exception
	{
		manager.migrateConfigs();
		overlayManager.add(minimapOverlay);
		keyManager.registerKeyListener(this);
		mouseManager.registerMouseListener(dragListener);
		manager.registerOrbsHidden();

		if (!manager.isLoggedIn())
		{
			manager.isLoggingIn = true;
		}

		manager.isUpdatingProfile = false;
		manager.isEditingLayout = false;
		manager.hideWorldMap = config.hideWorld();
		manager.hideLogoutX = config.hideLogout();
		manager.enableNoClickThrough = config.enableNoClickthrough();

		clientThread.invoke(() ->
		{
			slotManager.initSlots();

			if (manager.isLoggedIn())
			{
				manager.update(Script.FORCE_UPDATE);
				manager.setupMinimapOverlay();
			}
		});
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(minimapOverlay);
		keyManager.unregisterKeyListener(this);
		mouseManager.unregisterMouseListener(dragListener);
		clientThread.invoke(manager::reset);
	}

	@Provides
	CompactOrbsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CompactOrbsConfig.class);
	}

	@Subscribe
	public void onClientTick(ClientTick event)
	{
		//don't check for drag events unless layout editing is enabled
		if (!manager.isEditingLayout)
		{
			return;
		}

		//gated by client.getDraggedWidget(), with actual changes only if the dragged widget is expected
		dragListener.updateDrag();

		//onWidgetDrag works, but is finicky
		//ex: moving a widget to 0,0 in bounds, then release the mouse to stop the drag (position updated)
		//grab and drag the same widget, with it's ending position the same as it was before (no change)
		//onWidgetDrag event will not fire -> which would potentially leave things stale (indicators/position)
		//TODO - still unsure if it's just the implementation...
		if (client.getMouseCurrentButton() == 0 && dragState.wasDragging)
		{
			dragListener.finalizeDrag();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.HOPPING ||
			event.getGameState() == GameState.LOGIN_SCREEN ||
			event.getGameState() == GameState.LOGGING_IN)
		{
			manager.isLoggingIn = true;
			manager.clearCustomChildren();
		}
		else if (manager.isLoggedIn() && manager.isLoggingIn)
		{
			manager.isLoggingIn = false;
			manager.createCustomChildren();
		}
	}

	@Subscribe(priority = -1.0f)
	public void onScriptPostFired(ScriptPostFired event)
	{
		int scriptId = event.getScriptId();

		//prevent unwanted changes while in edit-mode
		if (manager.isEditingLayout &&
			(scriptId == Script.TOPLEVEL_REDRAW ||
				scriptId == Script.PROC_TOPLEVEL_SUBCHANGE ||
				scriptId == Script.TOPLEVEL_SIDE_CUSTOMIZE ||
				scriptId == Script.WIKI_ICON_INIT ||
				scriptId == Script.WORLD_MAP_UPDATE ||
				scriptId == Script.WIKI_ICON_UPDATE ||
				scriptId == Script.STORE_ORB_UPDATE ||
				scriptId == Script.ACTIVITY_ORB_UPDATE))
		{
			widgetManager.remapTargets(
				Orbs.WIKI_ICON_CONTAINER,
				Orbs.STORE_ORB_CONTAINER,
				Orbs.ACTIVITY_ORB_CONTAINER);

			widgetManager.setTargetsHidden(false, Compass.values());
			if (manager.isCompactLayout())
			{
				if (manager.compassFrame != null)
				{
					manager.compassFrame.setHidden(false);
				}
			}
			return;
		}

		switch (scriptId)
		{
			case Script.GRAPHIC_SWAPPER:
				manager.resolveOrbFrameMismatch();
				break;

			case Script.TOPLEVEL_SUBCHANGE:
				//ideally for display mode change
				if (manager.isEditingLayout)
				{
					editLayout.toggleEditMode(false);
				}
				break;

			case Script.TOPLEVEL_REDRAW:
				//check for an active cutscene
				manager.isCutsceneActive = manager.getCutsceneStatus();

			case Script.PROC_TOPLEVEL_SUBCHANGE:
			case Script.TOPLEVEL_SIDE_CUSTOMIZE:
				manager.hideLogout();
				manager.updateLogoutXOverlay();
				break;

			case Script.WIKI_ICON_INIT:
				manager.updateWikiBannerVisibility(config.hideWiki());
				break;

			case Script.ORBS_UPDATE_SPECENERGY:
				if (manager.isEditingLayout && config.hideSpec())
				{
					//prevent the script from setting the opacity back to 25
					widgetManager.setTargetOpacity(Orbs.SPEC_ORB_CONTAINER, EDIT_MODE_HIDDEN_OPACITY);
				}
			case Script.ORBS_UPDATE_HEALTH:
				if (!manager.isCompactLayout())
				{
					manager.updateNoClickThrough();
				}
				break;

			case Script.WORLD_MAP_UPDATE:
				if (manager.hideWorldMap)
				{
					widgetManager.remapTargets(Orbs.WORLD_MAP_CONTAINER);
					return;
				}
			case Script.STORE_ORB_UPDATE:
			case Script.ACTIVITY_ORB_UPDATE:
			case Script.WIKI_ICON_UPDATE:
			case Script.GRID_MASTER_ORB_UPDATE:
				if (!manager.isMinimapMinimized())
				{
					manager.update(scriptId);
				}
				break;
		}
	}

	@Subscribe
	public void onScriptPreFired(ScriptPreFired event)
	{
		int scriptId = event.getScriptId();

		if (scriptId == Orbs.WORLD_MAP_TOOLTIP.getScriptId())
		{
			int id = client.getIntStack()[2];
			int tooltipId = Orbs.WORLD_MAP_TOOLTIP.getComponentId();
			if (id == tooltipId)
			{
				boolean hidden = manager.isCompactLayout() || manager.hideWorldMap;
				widgetManager.setHidden(tooltipId, hidden);
			}
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		int varbitId = event.getVarbitId();

		switch (varbitId)
		{
			case Varbit.MINIMAP_TOGGLE:
				widgetManager.remapTargets(Orbs.LOGOUT_X_ICON, Orbs.LOGOUT_X_STONE);
				manager.updateCustomChildren();
				manager.setupMinimapContainer(false);
				break;

			case Varbit.STORE_ORB_TOGGLE:
			case Varbit.ACTIVITY_ORB_TOGGLE:
				//might need to add other varbit triggers (toggle wiki, toggle data orbs?)
				if (manager.isEditingLayout)
				{
					editLayout.toggleEditMode(false);
				}

				if (manager.allowReordering())
				{
					widgetManager.remapTargets(Orbs.values());

					if (config.minimapTogglePlacement() == TogglePlacement.BELOW_MAP)
					{
						manager.updateMinimapToggleButton();
					}
				}
				break;
		}
	}

	@Subscribe(priority = -1.0f)
	public void onWidgetLoaded(WidgetLoaded event)
	{
		int id = event.getGroupId();
		switch (id)
		{
			case Orb.UNIVERSE >> 16:
				manager.update(Script.FORCE_UPDATE);
				break;

			case Widgets.MinimapOverlay.UNIVERSE >> 16:
				manager.setupMinimapOverlay();
				break;
		}
	}

	@Subscribe(priority = -1.0f)
	public void onConfigChanged(ConfigChanged event)
	{
		String group = event.getGroup();
		String key = event.getKey();

		if (group.equals(ConfigGroup.Wiki.GROUP_NAME))
		{
			if (key.equals(ConfigKeys.Wiki.SHOW_WIKI_MINIMAP_BUTTON))
			{
				manager.warnWikiPluginConflict();
				clientThread.invokeLater(() ->
				{
					if (manager.isEditingLayout)
					{
						editLayout.toggleEditMode(false);
					}
					widgetManager.remapTargets(Orbs.WIKI_ICON_CONTAINER);
					manager.updateWikiBannerVisibility(config.hideWiki());
					manager.updateCustomChildren();
				});
			}
		}

		if (!group.equals(GROUP_NAME))
		{
			return;
		}

		if (slotRegistry.isSwapConfig(key) || manager.isHideConfig(key))
		{
			if (key.equals(ConfigKeys.MINIMAP_TOGGLE_BUTTON) && !manager.isEditingLayout)
			{
				clientThread.invokeLater(() ->
				{
					manager.hideOrbByConfig(key);
					if (!manager.isFixedMode())
					{
						manager.rebuildLayout();
					}
				});
			}
			return;
		}

		if (manager.isEditingLayout && !manager.isUpdatingProfile)
		{
			clientThread.invokeLater(() ->
			{
				if (!key.contains(manager.getCurrentPrefix()))
				{
					editLayout.toggleEditMode(false);
				}
			});
		}

		switch (key)
		{
			case ConfigKeys.COMPASS:
				clientThread.invokeLater(() ->
				{
					manager.setupMinimapContainer(false);
					widgetManager.setTargetsHidden(manager.isCompassHidden(), Compass.values());
					widgetManager.remapTargets(Compass.values());
					widgetManager.remapTargets(Orbs.values());
				});
				break;

			case ConfigKeys.MINIMAP_BUTTON_PLACEMENT:
			case ConfigKeys.ENABLE_OVERLAY_TOGGLE_OPTION:
				clientThread.invokeLater(manager::updateMinimapToggleButton);
				break;

			case ConfigKeys.RIGHT_CLICK_TOGGLE_BUTTONS:
				clientThread.invokeLater(manager::updateCustomChildren);
				break;

			case ConfigKeys.ENABLE_ORB_SWAPPING:
			case ConfigKeys.ENABLE_NO_CLICKTHROUGH:
				clientThread.invokeLater(() ->
				{
					if (key.equals(ConfigKeys.ENABLE_NO_CLICKTHROUGH))
					{
						manager.enableNoClickThrough = config.enableNoClickthrough();
					}
					else
					{
						slotManager.initSlots();
						manager.rebuildLayout();
					}

					manager.updateNoClickThrough();

					if (!manager.isCompactLayout())
					{
						widgetManager.remapTargets(Orbs.XP_DROPS_CONTAINER);
					}
				});
				break;

			case ConfigKeys.ENABLE_MINIMAP_OVERLAY:
				clientThread.invokeLater(() ->
				{
					widgetManager.setHidden(Widgets.MinimapOverlay.UNIVERSE, manager.hideMinimapOverlay());
					manager.hideLogout();
					manager.updateLogoutXPosition();
					manager.updateLogoutXOverlay();
				});
				break;

			case ConfigKeys.ENABLE_LOGOUT_X_OVERLAY:
				clientThread.invokeLater(() ->
				{
					manager.hideLogout();
					manager.updateLogoutXPosition();
					manager.updateLogoutXOverlay();
				});
				break;

			case ConfigKeys.ORB_LAYOUT:
			case ConfigKeys.VERTICAL_Y_ADJUSTMENT:
			case ConfigKeys.HORIZONTAL_ANCHOR:
			case ConfigKeys.VERTICAL_ANCHOR:
			case ConfigKeys.DISABLE_REORDERING:
			case ConfigKeys.LEAVE_EMPTY_SPACE:
				clientThread.invokeLater(() ->
				{
					if (!manager.isFixedMode())
					{
						manager.rebuildLayout();
					}
				});
				break;
		}
	}

	@Override
	public void resetConfiguration()
	{
		manager.isUpdatingProfile = true;
		clientThread.invoke(() -> manager.rebuild(true));
	}

	@Subscribe
	public void onProfileChanged(ProfileChanged event)
	{
		manager.migrateConfigs();

		manager.isUpdatingProfile = true;
		clientThread.invoke(() -> manager.rebuild(false));
	}

	@Subscribe
	public void onPluginChanged(PluginChanged event)
	{
		String plugin = event.getPlugin().getName();
		if (!plugin.equalsIgnoreCase(ConfigGroup.Wiki.GROUP_NAME))
		{
			return;
		}

		clientThread.invokeLater(() ->
		{
			if (manager.isEditingLayout)
			{
				editLayout.toggleEditMode(false);
			}
			else
			{
				manager.rebuildLayout();
			}
			manager.updateWikiBannerVisibility(config.hideWiki());
		});
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		manager.addCustomMenuEntries(event.getMenuEntry());
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		if (manager.isEditingLayout)
		{
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE || config.hotkeyKeybind().matches(e))
			{
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					e.consume();
				}
				clientThread.invokeLater(() -> editLayout.toggleEditMode(false));
			}
			return;
		}

		if (!config.hotkeyKeybind().matches(e))
		{
			return;
		}

		//during or after a cutscene, wait until the layout has settled before allowing toggles
		if (manager.isCutsceneActive)
		{
			return;
		}

		//let the hotkey toggle the minimap button in fixed mode if EDIT_MODE is not the selected option
		if (config.toggleOption() == HotkeyOptions.MINIMAP_BUTTON
			|| manager.isFixedMode() && config.toggleOption() != HotkeyOptions.EDIT_MODE)
		{
			manager.saveConfig(ConfigKeys.MINIMAP_TOGGLE_BUTTON, !config.hideMinimapToggle());
			return;
		}

		switch (config.toggleOption())
		{
			case MINIMAP:
				clientThread.invokeLater(manager::onMinimapToggle);
				break;

			case DETACHED_MINIMAP:
				manager.saveConfig(ConfigKeys.ENABLE_MINIMAP_OVERLAY, !config.showMinimapInCompactView());
				break;

			case EDIT_MODE:
				clientThread.invokeLater(() -> editLayout.toggleEditMode(true));
				break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
	}
}
