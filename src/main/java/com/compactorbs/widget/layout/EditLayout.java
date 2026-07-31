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

package com.compactorbs.widget.layout;

import com.compactorbs.CompactOrbsConfig;
import static com.compactorbs.CompactOrbsConstants.Layout.EDIT_MODE_BACKGROUND_OPACITY;
import static com.compactorbs.CompactOrbsConstants.Layout.EDIT_MODE_HIDDEN_OPACITY;
import static com.compactorbs.CompactOrbsConstants.Layout.LOGOUT_X_ICON_OPACITY;
import static com.compactorbs.CompactOrbsConstants.Layout.ORBS_CONTAINER_OFFSET_Y;
import com.compactorbs.CompactOrbsConstants.MenuOp;
import static com.compactorbs.CompactOrbsConstants.MenuOp.RESET_ALL_OP_INDEX;
import com.compactorbs.CompactOrbsManager;
import com.compactorbs.widget.TargetWidget;
import com.compactorbs.widget.WidgetManager;
import com.compactorbs.widget.elements.Button;
import com.compactorbs.widget.elements.Compass;
import com.compactorbs.widget.elements.Minimap;
import com.compactorbs.widget.elements.Orbs;
import com.compactorbs.widget.layout.edit.Binding;
import com.compactorbs.widget.layout.edit.BindingManager;
import com.compactorbs.widget.layout.edit.drag.DragState;
import java.awt.Point;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import static net.runelite.api.widgets.WidgetConfig.DRAG;
import static net.runelite.api.widgets.WidgetConfig.DRAG_ON;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.util.ColorUtil;

public class EditLayout
{
	@Inject
	private Client client;

	@Inject
	private WidgetManager widgetManager;

	@Inject
	private CompactOrbsManager manager;

	@Inject
	private CompactOrbsConfig config;

	@Inject
	private DragState dragState;

	@Inject
	private BindingManager bindingManager;

	//format: {modern, classic}
	private static final TargetWidget[][] EDIT_TARGETS =
		{
			//same for all display modes
			{Orbs.HP_ORB_CONTAINER, null},
			{Orbs.PRAYER_ORB_CONTAINER, null},
			{Orbs.RUN_ORB_CONTAINER, null},
			{Orbs.SPEC_ORB_CONTAINER, null},
			{Orbs.STORE_ORB_CONTAINER, null},
			{Orbs.ACTIVITY_ORB_CONTAINER, null},
			{Orbs.WORLD_MAP_CONTAINER, null},
			{Orbs.WIKI_ICON_CONTAINER, null},
			{Orbs.XP_DROPS_CONTAINER, null},

			//not visible in classic
			{Orbs.LOGOUT_X_ICON, null},

			//differ between display modes
			{Button.MINIMAP_BUTTON_MODERN, Button.MINIMAP_BUTTON_CLASSIC},
			{Minimap.MODERN_MAP_MINIMAP, Minimap.CLASSIC_MAP_MINIMAP}
		};

	private boolean blockEditing(TargetWidget target)
	{
		return (manager.isClassicResizable() && target.isLogoutX()) ||
			(!manager.isCompactLayout() && target.isCompass()) ||
			(manager.isStoreOrbDisabled() && target == Orbs.STORE_ORB_CONTAINER) ||
			(manager.isActivityOrbDisabled() && target == Orbs.ACTIVITY_ORB_CONTAINER) ||
			(manager.isWikiBannerDisabled() && !manager.isWikiPluginBannerActive() && target.isWiki());
	}

	public void toggleEditMode(boolean state)
	{
		final Widget parent = widgetManager.getMapParent();
		if (parent == null)
		{
			manager.isEditingLayout = false;
			return;
		}

		if (state)
		{
			enableEditMode(parent);
		}
		else
		{
			closeEditMode(true);
		}
	}

	//effectively cancel the current edit-mode without saving changes
	public void cancelEditMode()
	{
		if (manager.isEditingLayout)
		{
			closeEditMode(false);
		}
	}

	private void enableEditMode(Widget parent)
	{
		bindingManager.clear();
		manager.isEditingLayout = true;
		setConfigFlags();
		manager.updateLayout(manager.isCompactLayout());
		updateBackground();
		parent.setNoClickThrough(true);

		for (TargetWidget[] targets : EDIT_TARGETS)
		{
			final TargetWidget target =
				manager.isClassicResizable() && targets[1] != null
					? targets[1] //classic
					: targets[0];//modern

			if (blockEditing(target))
			{
				continue;
			}

			final Widget bound = widgetManager.getTargetWidget(target);
			if (bound == null)
			{
				continue;
			}

			final Point handlerPos = toHandlerPosition(bound, parent);
			final Widget handler = widgetManager.createHandler(parent,
				handlerPos.x,
				handlerPos.y,
				bound.getOriginalWidth(), bound.getOriginalHeight(),
				bound.getXPositionMode(), bound.getYPositionMode(),
				true);

			final OrbToggle toggle = manager.toggleByTarget.get(target);
			if (toggle != null)
			{
				bindingManager.bind(
					handler,
					targets[0],
					targets[1],
					target.isLogoutX() ? Orbs.LOGOUT_X_STONE : null,
					toggle.hidden.get());

				final Binding binding = bindingManager.getByHandler(handler);
				if (binding != null)
				{
					final boolean hidden = binding.isHidden();
					setupHiddenOrbs(target, hidden);
					setupHandlerActions(handler, target, toggle, hidden);

					if (isCustomLayout() ||
						Orbs.isSwappableOrb(bound.getId()) && config.enableOrbSwapping())
					{
						//set draggable
						handler.setClickMask(DRAG | DRAG_ON);
					}
				}
			}
		}

		if (isCustomLayout())
		{
			dragState.boundIndicator = widgetManager.createIndicator(parent);
		}
	}

	private void setupHiddenOrbs(TargetWidget target, boolean isHidden)
	{
		if (!isHidden)
		{
			return;
		}

		widgetManager.setTargetOpacity(target, EDIT_MODE_HIDDEN_OPACITY);

		if (target.isCompass())
		{
			widgetManager.setTargetsHidden(false, Compass.values());
			widgetManager.setTargetsHidden(false,
				Minimap.MODERN_MAP_CONTAINER,
				Minimap.CLASSIC_MAP_CONTAINER);

			if (manager.isCompactLayout())
			{
				manager.compassFrame.setHidden(false);
			}
		}
		else if (target.isWiki())
		{
			manager.updateWikiBannerVisibility(false);
		}
		else
		{
			widgetManager.setHidden(target, false);
		}
	}

	private void setupHandlerActions(Widget handler, TargetWidget target, OrbToggle toggle, boolean isHidden)
	{
		handler.setAction(
			MenuOp.HANDLER_TOGGLE_OP_INDEX,
			manager.buildToggleOp(isHidden, toggle.name));

		if (isCustomLayout())
		{
			handler.setAction(
				MenuOp.RESET_POSITION_OP_INDEX,
				manager.buildMenuOp(MenuOp.RESET, toggle.name));
		}

		if (target.isMinimapButton())
		{
			handler.setAction(
				MenuOp.DISABLE_EDIT_MODE_OP_INDEX,
				manager.buildEditOp(manager.isEditingLayout));

			if (isCustomLayout())
			{
				handler.setAction(
					RESET_ALL_OP_INDEX,
					ColorUtil.wrapWithColorTag(MenuOp.RESET_ALL, MenuOp.RED));
			}
		}

		//handle the menu actions: each handler can hide/show, or reset its bound widget
		//the minimap button can also reset all bound widget positions, and disable edit mode respectively
		handler.setOnOpListener((JavaScriptCallback) e ->
		{
			final Binding binding = bindingManager.getByHandler(handler);
			if (binding == null)
			{
				return;
			}

			switch (e.getOp() - 1)
			{
				case MenuOp.HANDLER_TOGGLE_OP_INDEX:
					binding.setHidden(!binding.isHidden());
					handler.setAction(MenuOp.HANDLER_TOGGLE_OP_INDEX, manager.buildToggleOp(binding.isHidden(), toggle.name));
					widgetManager.setTargetOpacity(target, binding.isHidden() ? EDIT_MODE_HIDDEN_OPACITY : 0);
					break;

				case MenuOp.RESET_POSITION_OP_INDEX:
					manager.resetCustomPositionForTarget(binding, true);
					break;

				case RESET_ALL_OP_INDEX:
					manager.resetAllCustomPositions(true);
					break;

				case MenuOp.DISABLE_EDIT_MODE_OP_INDEX:
					toggleEditMode(false);
					break;
			}
		});
	}

	//handle whether edit-mode was triggered (save changes) or cancelled (config event, display mode changed, etc)
	private void closeEditMode(boolean save)
	{
		manager.isEditingLayout = false;

		setConfigFlags();

		for (Binding binding : bindingManager.all())
		{
			final TargetWidget target = getBoundTarget(binding);
			final OrbToggle toggle = manager.toggleByTarget.get(target);
			if (toggle != null)
			{
				if (save)
				{
					if (binding.isHidden() != toggle.hidden.get())
					{
						manager.saveConfig(toggle.key, binding.isHidden());
					}
				}

				manager.updateOrbByConfig(toggle.key);
			}

			widgetManager.setTargetOpacity(target, target.isLogoutX() ? LOGOUT_X_ICON_OPACITY : 0);
		}

		cleanupEditMode();
	}

	//restore the minimap to a clean state post-edit
	private void cleanupEditMode()
	{
		manager.updateLayout(manager.isCompactLayout());
		clearEditChildren();
		widgetManager.getMapParent().setNoClickThrough(manager.isEditingLayout);
		dragState.clear();
	}

	//remove handlers, indicators, and the background
	private void clearEditChildren()
	{
		if (!manager.isEditingLayout)
		{
			Widget parent = widgetManager.getMapParent();
			if (parent.getChildren() != null)
			{
				parent.deleteAllChildren();
			}

			parent = client.getWidget(Minimap.ORBS_UNIVERSE.getComponentId());
			if (parent != null)
			{
				if (parent.getChildren() != null)
				{
					parent.deleteAllChildren();
				}
			}
		}
	}

	//remove positional offsets for the world map/logout-x when hidden during edit-mode,
	//or restore them based on the config when exiting edit-mode
	private void setConfigFlags()
	{
		manager.hideWorldMap = !manager.isEditingLayout && config.hideWorld();
		manager.hideLogoutX = !manager.isEditingLayout && config.hideLogout();
	}

	private TargetWidget getBoundTarget(Binding binding)
	{
		return manager.isClassicResizable() && binding.getClassic() != null
			? binding.getClassic()
			: binding.getModern();
	}

	public void updateBackground()
	{
		final int parentId = manager.isCompactLayout()
			? Minimap.ORBS_UNIVERSE.getComponentId()
			: widgetManager.getMapParent().getId();

		final Widget parent = client.getWidget(parentId);
		if (parent == null)
		{
			return;
		}

		final Widget editBackground = parent.createChild(0, WidgetType.RECTANGLE);
		if (!manager.isCompactLayout())
		{
			editBackground
				.setOriginalX(parent.getOriginalX())
				.setOriginalY(parent.getOriginalY())
				.setOriginalWidth(parent.getOriginalWidth())
				.setOriginalHeight(parent.getOriginalHeight());
		}

		editBackground
			.setXPositionMode(parent.getXPositionMode())
			.setYPositionMode(parent.getYPositionMode())
			.setWidthMode(parent.getWidthMode())
			.setHeightMode(parent.getHeightMode())
			.setTextColor(0xff0000)
			.setFilled(true)
			.setOpacity(EDIT_MODE_BACKGROUND_OPACITY)
			.revalidate();
	}

	//translate the handlers position into coordinates for the bound widget
	public Point toBoundPosition(Widget handler, Widget boundParent)
	{
		int x = handler.getOriginalX();
		int y = handler.getOriginalY();
		if (handler.getParent() != boundParent)
		{
			Point offset = getLayoutOffset();
			x -= offset.x;
			y -= offset.y;
			if (!manager.isCompactLayout())
			{
				y -= ORBS_CONTAINER_OFFSET_Y;
			}
		}

		return new Point(x, y);
	}

	//translate the bound widgets position into coordinates for the handler
	public Point toHandlerPosition(Widget bound, Widget handlerParent)
	{
		int x = bound.getOriginalX();
		int y = bound.getOriginalY();
		if (bound.getParent() != handlerParent)
		{
			Point offset = getLayoutOffset();
			x += offset.x;
			y += offset.y;
			if (!manager.isCompactLayout())
			{
				y += ORBS_CONTAINER_OFFSET_Y;
			}
		}

		return new Point(x, y);
	}

	private Point getLayoutOffset()
	{
		int x = manager.isCompactLayout() && manager.isAnchorRight()
			? manager.getCurrentLayout().getRightOffset() : 0;

		int y = manager.isCompactLayout() && manager.isAnchorBottom()
			? manager.getCurrentLayout().getBottomOffset() : 0;

		return new Point(x, y);
	}

	public boolean isCustomLayout()
	{
		return manager.isCompactLayout() && manager.getCurrentLayout().isCustom();
	}
}
