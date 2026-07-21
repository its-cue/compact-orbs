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

import com.compactorbs.CompactOrbsConfig.TogglePlacement;
import com.compactorbs.CompactOrbsConstants.ConfigGroup;
import static com.compactorbs.CompactOrbsConstants.ConfigGroup.GROUP_NAME;
import com.compactorbs.CompactOrbsConstants.ConfigKeys;
import com.compactorbs.CompactOrbsConstants.Script;
import com.compactorbs.CompactOrbsConstants.Varbit;
import com.compactorbs.CompactOrbsConstants.Widgets;
import com.compactorbs.CompactOrbsConstants.Widgets.Classic;
import com.compactorbs.CompactOrbsConstants.Widgets.Fixed;
import com.compactorbs.CompactOrbsConstants.Widgets.Modern;
import com.compactorbs.CompactOrbsConstants.Widgets.Orb;
import com.compactorbs.widget.WidgetManager;
import com.compactorbs.widget.elements.Orbs;
import com.compactorbs.widget.layout.slot.Slot;
import com.compactorbs.widget.layout.slot.SlotLayout;
import com.compactorbs.widget.layout.slot.SlotManager;
import com.compactorbs.widget.overlay.MinimapOverlay;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
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
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;

@Slf4j
@PluginDescriptor(
	name = "Compact Orbs",
	description = "Minimize the minimap and reposition the orbs into a compact view.",
	tags = {"compact", "orbs", "layout", "hide", "minimap", "resizable", "classic", "modern", "world", "map", "wiki", "swap", "overlay", "orb", "fixed"},
	conflicts = {"Fixed Resizable Hybrid", "Orb Hider", "Minimap Hider", "Movable Orbs"}
)
public class CompactOrbsPlugin extends Plugin
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

	@Override
	protected void startUp() throws Exception
	{
		manager.updateConfig();
		overlayManager.add(minimapOverlay);
		keyManager.registerKeyListener(hotkeyListener);
		manager.registerOrbToggleEntries();

		if (!manager.isLoggedIn())
		{
			manager.initialLoginPending = true;
		}

		manager.updateFixedMode = true;
		manager.hideWorldMap = config.hideWorld();
		manager.hideLogoutX = config.hideLogout();
		manager.enableNoClickThrough = config.enableNoClickthrough();

		clientThread.invoke(() ->
		{
			slotManager.initSlots();

			if (manager.isLoggedIn())
			{
				manager.updateLogoutX();

				manager.init(Script.FORCE_UPDATE);
				manager.setupOrbsContainer();
				manager.setupMinimapOverlay();
			}
		});
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(minimapOverlay);

		keyManager.unregisterKeyListener(hotkeyListener);

		clientThread.invoke(manager::reset);
	}

	@Provides
	CompactOrbsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CompactOrbsConfig.class);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.HOPPING)
		{
			manager.initialLoginPending = true;
		}

		if (manager.isLoggedIn() && manager.initialLoginPending)
		{
			manager.initialLoginPending = false;
			manager.createCustomChildren();
		}
	}

	@Subscribe(priority = -1.0f)
	public void onScriptPostFired(ScriptPostFired event)
	{
		int scriptId = event.getScriptId();

		switch (scriptId)
		{
			case Script.GRAPHIC_SWAPPER:
				manager.resolveOrbFrameMismatch();
				break;

			case Script.TOPLEVEL_REDRAW:
				//check for an active cutscene
				manager.pendingChildrenUpdate = manager.isCutsceneActive();

			case Script.TOPLEVEL_SUBCHANGE:
			case Script.TOPLEVEL_SIDE_CUSTOMIZE:
				manager.updateLogoutX();
				manager.updateLogoutXOverlay();
				break;

			case Script.WIKI_ICON_INIT:
				manager.updateWikiBannerVisibility(config.hideWiki());
				break;

			case Script.ORBS_UPDATE_HEALTH:
			case Script.ORBS_UPDATE_SPECENERGY:
				if (!manager.isCompactLayout())
				{
					manager.updateNoClickThrough();
				}
				break;

			case Script.WORLD_MAP_UPDATE:
				if (manager.hideWorldMap)
				{
					widgetManager.remapTarget(Orbs.WORLD_MAP_CONTAINER, config.hideMinimap() && !manager.isFixedMode());
					return;
				}
			case Script.STORE_ORB_UPDATE:
			case Script.ACTIVITY_ORB_UPDATE:
			case Script.WIKI_ICON_UPDATE:
			case Script.GRID_MASTER_ORB_UPDATE:
				if (!manager.isMinimapMinimized())
				{
					manager.init(scriptId);
				}
				break;
		}
	}

	@Subscribe
	public void onScriptPreFired(ScriptPreFired event)
	{
		if (event.getScriptId() == Orbs.WORLD_MAP_TOOLTIP.getScriptId())
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
				manager.updateCustomChildren(true);
				manager.setupMinimapContainer(!manager.isMinimapMinimized());
				break;

			case Varbit.STORE_ORB_TOGGLE:
			case Varbit.ACTIVITY_ORB_TOGGLE:
				if (manager.allowReordering())
				{
					widgetManager.remapTargets(manager.isCompactLayout(), Script.FORCE_UPDATE, Orbs.values());
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
			case Fixed.ORBS >> 16:
				manager.updateFixedMode = true;
				break;

			case Orb.UNIVERSE >> 16:
			case Classic.ORBS >> 16:
			case Modern.ORBS >> 16:
				manager.init(Script.FORCE_UPDATE);
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
					widgetManager.remapTarget(Orbs.WIKI_ICON_CONTAINER, manager.isCompactLayout());
					manager.updateWikiBannerVisibility(config.hideWiki());
					manager.updateCustomChildren(true);
				});
			}
		}

		if (!group.equals(GROUP_NAME))
		{
			return;
		}

		SlotLayout slotLayout = Slot.getSlotByConfigKey(event.getKey());
		if (slotLayout != null)
		{
			clientThread.invokeLater(() ->
			{
				slotManager.applySlotSwap(slotLayout.getSlot(), slotLayout.getLayout());
				manager.updateNoClickThrough();
			});
			return;
		}

		switch (key)
		{
			case ConfigKeys.MINIMAP:
			case ConfigKeys.COMPASS:
			case ConfigKeys.HOTKEY_KEYBIND:
			case ConfigKeys.HOTKEY_TOGGLE_OPTION:
				//do nothing (prevent default behaviour)
				break;

			case ConfigKeys.MINIMAP_BUTTON_PLACEMENT:
			case ConfigKeys.ENABLE_OVERLAY_TOGGLE_OPTION:
				clientThread.invokeLater(manager::updateMinimapToggleButton);
				break;

			case ConfigKeys.RIGHT_CLICK_TOGGLE_BUTTONS:
				clientThread.invokeLater(() -> manager.updateCustomChildren(true));
				break;

			case ConfigKeys.ENABLE_ORB_SWAPPING:
				clientThread.invoke(() -> slotManager.generateSlots(true));
			case ConfigKeys.ENABLE_NO_CLICKTHROUGH:
				if (key.equals(ConfigKeys.ENABLE_NO_CLICKTHROUGH))
				{
					manager.enableNoClickThrough = config.enableNoClickthrough();
				}

				clientThread.invokeLater(() ->
				{
					manager.updateNoClickThrough();
					manager.updateCompassToggleButton();
					if (!manager.isCompactLayout())
					{
						widgetManager.remapTarget(Orbs.XP_DROPS_CONTAINER, manager.isCompactLayout());
					}
				});
				break;

			case ConfigKeys.ENABLE_MINIMAP_OVERLAY:
				clientThread.invokeLater(() -> manager.updateMinimapOverlayVisibility(true));
				break;

			case ConfigKeys.ENABLE_LOGOUT_X_OVERLAY:
				clientThread.invokeLater(() ->
				{
					manager.updateLogoutX();
					manager.updateLogoutXPosition();
					manager.updateLogoutXOverlay();
				});
				break;

			case ConfigKeys.ORB_LAYOUT:
			case ConfigKeys.VERTICAL_Y_ADJUSTMENT:
			case ConfigKeys.MINIMAP_TOGGLE_BUTTON:
			case ConfigKeys.COMPASS_TOGGLE_BUTTON:
			case ConfigKeys.HORIZONTAL_ANCHOR:
			case ConfigKeys.VERTICAL_ANCHOR:
				clientThread.invokeLater(() -> manager.updateLayout(manager.isCompactLayout()));
				break;

			//orb visibility
			default:
				clientThread.invokeLater(() ->
				{
					manager.updateOrbByConfig(event.getKey());
					manager.updateLayout(manager.isCompactLayout());
				});
				break;
		}
	}

	@Override
	public void resetConfiguration()
	{
		clientThread.invokeLater(() -> manager.updateLayout(manager.isCompactLayout()));
	}

	@Subscribe
	public void onProfileChanged(ProfileChanged event)
	{
		manager.updateConfig();
		clientThread.invokeLater(() -> manager.updateLayout(manager.isCompactLayout()));
	}

	@Subscribe
	public void onPluginChanged(PluginChanged event)
	{
		String plugin = event.getPlugin().getName();
		if (!plugin.equalsIgnoreCase(ConfigGroup.Wiki.GROUP_NAME))
		{
			return;
		}

		clientThread.invokeLater(() -> manager.updateLayout(manager.isCompactLayout()));
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		manager.addCustomMenuEntries(event.getMenuEntry());
	}

	private final HotkeyListener hotkeyListener = new HotkeyListener(() -> config.hotkeyKeybind())
	{
		@Override
		public void hotkeyPressed()
		{
			if (!manager.isFixedMode())
			{
				switch (config.toggleOption())
				{
					case MINIMAP:
						clientThread.invokeLater(manager::onMinimapToggle);
						break;

					case MINIMAP_BUTTON:
						manager.saveConfig(ConfigKeys.MINIMAP_TOGGLE_BUTTON, !config.hideMinimapToggle());
						break;

					case COMPASS_BUTTON:
						manager.saveConfig(ConfigKeys.COMPASS_TOGGLE_BUTTON, !config.hideCompassToggle());
						break;

					case BOTH_BUTTONS:
						boolean hidden = !(config.hideMinimapToggle() || config.hideCompassToggle());

						manager.saveConfig(ConfigKeys.MINIMAP_TOGGLE_BUTTON, hidden);
						manager.saveConfig(ConfigKeys.COMPASS_TOGGLE_BUTTON, hidden);
						break;

					case DETACHED_MINIMAP:
						manager.saveConfig(ConfigKeys.ENABLE_MINIMAP_OVERLAY, !config.showMinimapInCompactView());
						break;
				}
			}

		}
	};

}
