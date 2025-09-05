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
import com.compactorbs.util.SetValue;
import com.compactorbs.util.ValueKey;
import com.compactorbs.widget.elements.Compass;
import com.compactorbs.widget.elements.Minimap;
import com.compactorbs.widget.elements.Orbs;
import com.compactorbs.widget.TargetWidget;
import java.awt.Color;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
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

	private int lastParentId = -1;

	public static final int FORCE_REMAP = -1;

	private boolean remapping = false;

	private static final int COMPASS_FRAME_SPRITE_ID = 5813;

	private static final int TOP_LEVEL_MINIMAP_CHILD = 33;

	public static final int compassX = 126;
	public static final int compassY = 18;

	private Widget mapButton = null;
	private Widget mapMenu = null;
	private Widget compassButton = null;
	private Widget compassMenu = null;
	private Widget compassFrame = null;

	private static final String MINIMAP_CONFIG_KEY = "hideMinimap";
	private static final String COMPASS_CONFIG_KEY = "hideCompass";

	private static final Color MENU_COLOR = new Color(0xFF9040);

	public void build(int scriptId)
	{
		//prevent changes if on fixed mode display
		if (!client.isResized())
		{
			return;
		}

		createToggleButtons();

		setHidden(Minimap.values(), isMinimapHidden());
		setHidden(Compass.values(), (isMinimapHidden() && isCompassHidden()));

		boolean remapCompassCondition = !isCompassHidden() && isMinimapHidden();
		remap(Compass.ALL, remapCompassCondition, scriptId);
		remap(Orbs.ALL, isMinimapHidden(), scriptId);

		updateToggleButtons();
	}

	public void reset()
	{
		clearToggleButtons();

		if (isMinimapHidden())
		{
			setHidden(Minimap.values(), false);
		}

		if (isMinimapHidden())
		{
			if (isCompassHidden())
			{
				setHidden(Compass.values(), false);
			}
			remap(Compass.ALL, false);
		}

		remap((client.isResized() ? Orbs.ALL : Orbs.FIXED), false);
	}

	private void onMinimapToggle()
	{
		boolean toggle = !isMinimapHidden();
		boolean remapCondition = toggle && !isCompassHidden();
		executeToggle(
			config::hideMinimap, MINIMAP_CONFIG_KEY,
			() -> setHidden(Minimap.values(), toggle),
			() -> remap(Compass.ALL, remapCondition),
			() -> setHidden(Compass.values(), toggle && isCompassHidden()),
			() -> remap(Orbs.ALL, toggle)
		);
	}

	private void onCompassToggle()
	{
		boolean toggle = !isCompassHidden();
		boolean remapCondition = toggle || isMinimapHidden();
		executeToggle(
			config::hideCompass, COMPASS_CONFIG_KEY,
			() -> remap(Compass.ALL, remapCondition),
			() -> setHidden(Compass.values(), toggle)
		);
	}

	private void executeToggle(Supplier<Boolean> getter, String key, Runnable... actions)
	{
		boolean current = Boolean.TRUE.equals(getter.get());
		boolean next = !current;

		configManager.setConfiguration(GROUP_NAME, key, next);

		for (Runnable action : actions)
		{
			action.run();
		}

		updateToggleButtons();
	}

	void remap(Iterable<? extends TargetWidget> widgets, boolean modify)
	{
		remap(widgets, modify, FORCE_REMAP);
	}

	void remap(Iterable<? extends TargetWidget> widgets, boolean modify, int scriptId)
	{

		if (remapping)
		{
			return;
		}

		remapping = true;

		try
		{
			for (TargetWidget target : widgets)
			{
				if (!shouldUpdateWidget(target, scriptId))
				{
					continue;
				}

				Widget widget = getWidget(target);
				if (widget == null)
				{
					continue;
				}

				if (scriptId != FORCE_REMAP)
				{
					log.debug("Script: {}, triggered remap : {}.{}", scriptId, ((Enum<?>) target).getDeclaringClass().getSimpleName(), target);
				}

				target.getPositions().forEach((type, value)
					-> setValue(widget, type, value, modify)
				);

				widget.revalidate();
			}
		}
		finally
		{
			remapping = false;
		}
	}

	private boolean shouldUpdateWidget(TargetWidget target, int scriptId)
	{
		return (scriptId == FORCE_REMAP) || target.getScriptIds().contains(scriptId);
	}

	public void setValue(Widget widget, ValueKey type, SetValue value, boolean modify)
	{
		if (value == null)
		{
			return;
		}

		Integer v = value.get(modify);
		if (v == null)
		{
			return;
		}

		switch (type)
		{
			case X:
				updateValue(widget::getOriginalX, widget::setOriginalX, v);
				break;
			case Y:
				updateValue(widget::getOriginalY, widget::setOriginalY, v);
				break;
			case X_POSITION_MODE:
				updateValue(widget::getXPositionMode, widget::setXPositionMode, v);
				break;
		}
	}

	private void updateValue(IntSupplier getter, IntConsumer setter, int value)
	{
		if (getter.getAsInt() != value)
		{
			setter.accept(value);
		}
	}

	private <T extends TargetWidget> void setHidden(T[] widgets, boolean hidden)
	{
		for (T target : widgets)
		{
			Widget widget = getWidget(target);
			if (widget != null)
			{
				widget.setHidden(hidden);
			}
		}
	}

	private void createToggleButtons()
	{
		Widget parent = getCurrentParent();

		if (parent == null)
		{
			clearToggleButtons();
			lastParentId = -1;
			return;
		}

		if (parent.getId() != lastParentId)
		{
			clearToggleButtons();
			lastParentId = parent.getId();
		}

		createButtons(parent);
	}

	private void createButtons(Widget parent)
	{
		//only create buttons if they're missing or the parent has changed
		if (mismatch(compassFrame, parent))
		{
			//compass frame when minimap is hidden
			compassFrame = createGraphic(
				parent, compassX - 4, compassY - 14, 43, 43, 0,
				config.hideToggle(), COMPASS_FRAME_SPRITE_ID
			);

			//minimap toggle button sprite
			mapButton = createGraphic(
				parent, 190, 180, 17, 17, 160,
				config.hideToggle(), getSpriteId(isMinimapHidden())
			);

			//compass toggle button sprite
			compassButton = createGraphic(
				parent, 155, 32, 17, 17, 0,
				config.hideToggle(), getSpriteId(isCompassHidden())
			);

			//minimap toggle button menu
			mapMenu = createMenu(
				parent,
				191, 179,
				config.hideToggle(),
				getMenuOption(MINIMAP_CONFIG_KEY),
				e -> onMinimapToggle(),
				e -> mapButton.setOpacity(0),
				e -> mapButton.setOpacity(160)
			);

			//compass toggle button menu
			compassMenu = createMenu(
				parent,
				155, 33,
				config.hideToggle(),
				getMenuOption(COMPASS_CONFIG_KEY),
				e -> onCompassToggle(),
				null,
				null
			);
		}
	}

	public void updateToggleButtons()
	{
		if (mapButton != null)
		{
			mapButton.setSpriteId(getSpriteId(!isMinimapHidden()));

			mapButton.setHidden(config.hideToggle());

			if (mapMenu != null)
			{
				mapMenu.setAction(0, getMenuOption(MINIMAP_CONFIG_KEY));
				mapMenu.setHidden(config.hideToggle());
			}
		}

		if (compassButton != null)
		{
			compassButton.setHidden(config.hideToggle() || !isMinimapHidden());

			compassButton.setSpriteId(getSpriteId(!isCompassHidden()));

			if (compassMenu != null)
			{
				compassMenu.setHidden(config.hideToggle() || !isMinimapHidden());
				compassMenu.setAction(0, getMenuOption(COMPASS_CONFIG_KEY));
			}
		}

		if (compassFrame != null)
		{
			compassFrame.setHidden(!isMinimapHidden() || isCompassHidden());
		}
	}

	public void clearToggleButtons()
	{
		Widget modern = client.getWidget(InterfaceID.TOPLEVEL_PRE_EOC, TOP_LEVEL_MINIMAP_CHILD);
		if (modern != null)
		{
			if (modern.getChild(1) != null)
			{
				modern.deleteAllChildren();
			}
		}

		Widget classic = client.getWidget(InterfaceID.TOPLEVEL_OSRS_STRETCH, TOP_LEVEL_MINIMAP_CHILD);
		if (classic != null)
		{
			if (classic.getChild(1) != null)
			{
				classic.deleteAllChildren();
			}
		}

		lastParentId = -1;

		mapButton = null;
		mapMenu = null;
		compassButton = null;
		compassMenu = null;
		compassFrame = null;
	}

	//sprite mirrors state (crossed out eye == hidden)
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

	private Widget getWidget(TargetWidget target)
	{
		return client.getWidget(target.getInterfaceId(), target.getChildId());
	}

	private Widget getCurrentParent()
	{
		Widget modern = client.getWidget(InterfaceID.TOPLEVEL_PRE_EOC, TOP_LEVEL_MINIMAP_CHILD);
		if (modern != null && !modern.isHidden())
		{
			return modern;
		}

		Widget classic = client.getWidget(InterfaceID.TOPLEVEL_OSRS_STRETCH, TOP_LEVEL_MINIMAP_CHILD);
		if (classic != null && !classic.isHidden())
		{
			return classic;
		}
		return null;
	}

	public boolean isMinimapHidden()
	{
		return config.hideMinimap();
	}

	public boolean isCompassHidden()
	{
		return config.hideCompass();
	}

	private boolean mismatch(Widget child, Widget parent)
	{
		return child == null || child.getParentId() != parent.getId();
	}

	private Widget createGraphic(
		Widget parent,
		int x, int y,
		int width, int height,
		int opacity, boolean hidden,
		int spriteId)
	{
		Widget child = parent.createChild(-1, WidgetType.GRAPHIC);
		child
			.setOriginalX(x)
			.setOriginalY(y)
			.setOriginalWidth(width)
			.setOriginalHeight(height)
			.setSpriteId(spriteId)
			.setOpacity(opacity)
			.setHidden(hidden);

		child.revalidate();
		return child;
	}

	private Widget createMenu(
		Widget parent,
		int x, int y,
		boolean hidden,
		String menuOp,
		JavaScriptCallback opListener,
		JavaScriptCallback mouseOver,
		JavaScriptCallback mouseLeave)
	{
		Widget child = parent.createChild(-1, WidgetType.GRAPHIC);
		child
			.setOriginalX(x)
			.setOriginalY(y)
			.setOriginalWidth(16)
			.setOriginalHeight(16)
			.setHasListener(true)
			.setHidden(hidden)
			.setAction(0, menuOp);

		child.setNoClickThrough(true);
		if (opListener != null)
		{
			child.setOnOpListener(opListener);
		}
		if (mouseOver != null)
		{
			child.setOnMouseOverListener(mouseOver);
		}
		if (mouseLeave != null)
		{
			child.setOnMouseLeaveListener(mouseLeave);
		}
		child.revalidate();
		return child;
	}

}
