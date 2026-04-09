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

package com.compactorbs.widget.overlay.orb;

import com.compactorbs.CompactOrbsConstants.Layout;
import com.compactorbs.CompactOrbsConstants.Menu;
import com.compactorbs.CompactOrbsConstants.Sprite;
import com.compactorbs.CompactOrbsConstants.VarPlayer;
import com.compactorbs.CompactOrbsConstants.Widgets.MinimapOverlay.OrbConfig;
import com.compactorbs.CompactOrbsManager;
import com.compactorbs.widget.WidgetManager;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.client.util.ColorUtil;

public class OverlayOrbBuilder
{
	private final WidgetManager widgetManager;
	private final CompactOrbsManager manager;

	private final Widget parent;

	private int x;
	private int y;

	private Object[] varTransmitIds = new Object[0];
	private int[] varTriggers = new int[0];

	private int buttonId;
	private int textId;
	private int fillId;

	private int halfContainerId;
	private int halfFillId;

	private int emptyContainerId;
	private int emptyFillId;

	private int iconId;

	OverlayOrbBuilder(WidgetManager widgetManager, CompactOrbsManager manager, Widget parent)
	{
		this.widgetManager = widgetManager;
		this.manager = manager;
		this.parent = parent;
	}

	OverlayOrbBuilder setPos(int x, int y)
	{
		this.x = x;
		this.y = y;
		return this;
	}

	OverlayOrbBuilder setVarTransmit(Object... ids)
	{
		this.varTransmitIds = ids;
		return this;
	}

	OverlayOrbBuilder setVarTriggers(int... triggers)
	{
		this.varTriggers = triggers;
		return this;
	}

	OverlayOrbBuilder setButtonId(int componentId)
	{
		this.buttonId = componentId;
		return this;
	}

	OverlayOrbBuilder setTextId(int componentId)
	{
		this.textId = componentId;
		return this;
	}

	OverlayOrbBuilder setFillId(int componentId)
	{
		this.fillId = componentId;
		return this;
	}

	OverlayOrbBuilder setHalfIds(int containerId, int fillId)
	{
		this.halfContainerId = containerId;
		this.halfFillId = fillId;
		return this;
	}

	OverlayOrbBuilder setEmptyIds(int containerId, int fillId)
	{
		this.emptyContainerId = containerId;
		this.emptyFillId = fillId;
		return this;
	}

	OverlayOrbBuilder setIconId(int componentId)
	{
		this.iconId = componentId;
		return this;
	}

	public OverlayOrb buildXpOrb()
	{
		OverlayOrb xp = new OverlayOrb();

		xp.button = widgetManager.createGraphic(
			parent,
			WidgetManager.pos(x, y),
			WidgetManager.size(Layout.XP_ORB_SIZE, Layout.XP_ORB_SIZE),
			WidgetManager.sprite(!manager.isXpDropsEnabled() ? Sprite.XP_DROP : Sprite.XP_DROP_CLICKED),
			WidgetManager.hidden(manager.hideOverlayXPDrop()),
			WidgetManager.name(ColorUtil.wrapWithColorTag(Menu.SUFFIX_XP, Menu.COLOR)),
			WidgetManager.noClickThrough(),
			WidgetManager.listener(),
			WidgetManager.onOp(
				(JavaScriptCallback) event ->
					widgetManager.invokeMenuOp(buttonId, event.getOp())
			),
			widgetManager.onHoverWithVarTransmit(
				(w, hovering) ->
					w.setSpriteId(
						WidgetManager.resolveSprite(
							manager.xpDropButtonState(), hovering,
							Sprite.XP_DROP, Sprite.XP_DROP_HOVER,
							Sprite.XP_DROP_CLICKED, Sprite.XP_DROP_HOVER_CLICKED
						)
					),
				VarPlayer.XP_ORB_HOVERED
			),
			widgetManager.syncMenuOp(buttonId)
		);

		xp.buttonId = buttonId;
		return xp;
	}

	public OverlayOrb buildWorldMap()
	{
		OverlayOrb worldmap = new OverlayOrb();

		worldmap.container = widgetManager.createLayer(
			parent,
			WidgetManager.pos(x, y),
			WidgetManager.size(Layout.WORLD_MAP_FRAME_SIZE, Layout.WORLD_MAP_FRAME_SIZE),
			WidgetManager.posMode(WidgetPositionMode.ABSOLUTE_RIGHT, WidgetPositionMode.ABSOLUTE_TOP),
			WidgetManager.hidden(manager.hideOverlayWorldMap()),
			WidgetManager.listener()
		);

		worldmap.frame = widgetManager.createGraphic(
			worldmap.container,
			WidgetManager.pos(0, 0),
			WidgetManager.size(Layout.WORLD_MAP_FRAME_SIZE, Layout.WORLD_MAP_FRAME_SIZE),
			WidgetManager.posMode(WidgetPositionMode.ABSOLUTE_CENTER, WidgetPositionMode.ABSOLUTE_CENTER),
			WidgetManager.sprite(Sprite.WORLD_MAP_BACKING),
			WidgetManager.noClickThrough(),
			WidgetManager.listener()
		);

		worldmap.button = widgetManager.createGraphic(
			worldmap.container,
			WidgetManager.pos(0, 0),
			WidgetManager.size(Layout.WORLD_MAP_GLOBE_SIZE, Layout.WORLD_MAP_GLOBE_SIZE),
			WidgetManager.posMode(WidgetPositionMode.ABSOLUTE_CENTER, WidgetPositionMode.ABSOLUTE_CENTER),
			WidgetManager.sprite(Sprite.WORLD_MAP_GLOBE),
			WidgetManager.listener(),
			WidgetManager.onOp(
				(JavaScriptCallback) event ->
					widgetManager.invokeMenuOp(buttonId, event.getOp())
			),
			WidgetManager.onHover(
				event ->
				{
					worldmap.button.setSpriteId(Sprite.WORLD_MAP_GLOBE_HOVER);
					worldmap.button.setOpacity(0);
				},
				event ->
				{
					worldmap.button.setSpriteId(Sprite.WORLD_MAP_GLOBE);
					widgetManager.syncOpacity(worldmap.button, buttonId);
				}
			),
			widgetManager.syncMenuOp(buttonId)
		);

		worldmap.buttonId = buttonId;
		return worldmap;
	}

	public OverlayOrb buildLogoutX()
	{
		OverlayOrb logoutX = new OverlayOrb();

		logoutX.button = widgetManager.createGraphic(
			parent,
			WidgetManager.pos(x, y),
			WidgetManager.size(Layout.LOGOUT_X_WIDTH, Layout.LOGOUT_X_HEIGHT),
			WidgetManager.hidden(manager.hideOverlayLogoutX()),
			WidgetManager.posMode(WidgetPositionMode.ABSOLUTE_RIGHT, WidgetPositionMode.ABSOLUTE_TOP),
			WidgetManager.listener(),
			WidgetManager.onOp(
				(JavaScriptCallback) event ->
					widgetManager.invokeMenuOp(buttonId, event.getOp())
			),
			widgetManager.syncMenuOp(buttonId),
			widgetManager.syncSprite(buttonId)
		);

		logoutX.icon = widgetManager.createGraphic(
			parent,
			WidgetManager.pos(x, y),
			WidgetManager.size(Layout.LOGOUT_X_WIDTH, Layout.LOGOUT_X_HEIGHT),
			WidgetManager.hidden(manager.hideOverlayLogoutX()),
			WidgetManager.posMode(WidgetPositionMode.ABSOLUTE_RIGHT, WidgetPositionMode.ABSOLUTE_TOP),
			WidgetManager.sprite(Sprite.LOGOUT_X_BUTTON),
			WidgetManager.opacity(100)
		);

		logoutX.buttonId = buttonId;
		logoutX.iconId = iconId;
		return logoutX;
	}

	public HPOverlayOrb buildHalfOrb()
	{
		HPOverlayOrb orb = new HPOverlayOrb();
		return (HPOverlayOrb) buildOrb(orb, true);
	}

	public OverlayOrb buildOrb()
	{
		return buildOrb(new OverlayOrb(), false);
	}

	public OverlayOrb buildOrb(OverlayOrb orb, boolean half)
	{
		orb.container = widgetManager.createLayer(
			parent,
			WidgetManager.pos(x, y),
			WidgetManager.size(OrbConfig.LAYER_WIDTH, OrbConfig.LAYER_HEIGHT),
			WidgetManager.listener(),
			WidgetManager.onVarTransmit(varTransmitIds),
			WidgetManager.varTransmitTrigger(varTriggers)
		);

		orb.frame = widgetManager.createGraphic(
			orb.container,
			WidgetManager.pos(0, 0),
			WidgetManager.size(OrbConfig.LAYER_WIDTH, OrbConfig.LAYER_HEIGHT),
			WidgetManager.sprite(Sprite.ORB_FRAME),
			WidgetManager.listener(),
			WidgetManager.noClickThrough()
		);

		orb.button = widgetManager.createGraphic(
			orb.container,
			WidgetManager.pos(OrbConfig.BUTTON_X, OrbConfig.BUTTON_Y),
			WidgetManager.size(OrbConfig.BUTTON_WIDTH, OrbConfig.BUTTON_HEIGHT),
			WidgetManager.listener(),
			widgetManager.syncHidden(buttonId),
			widgetManager.syncName(buttonId),
			WidgetManager.onHover(
				mouseOver ->
				{
					if (orb.hasMenu())
					{
						orb.frame.setSpriteId(Sprite.ORB_FRAME_HOVERED);
					}
				},
				mouseLeave -> orb.frame.setSpriteId(Sprite.ORB_FRAME)
			)
		);

		orb.text = widgetManager.createText(
			orb.container,
			WidgetManager.text("", OrbConfig.TEXT_FONT_ID, true, OrbConfig.TEXT_X_ALIGNMENT, OrbConfig.TEXT_Y_ALIGNMENT),
			WidgetManager.pos(OrbConfig.TEXT_X, OrbConfig.TEXT_Y),
			WidgetManager.size(OrbConfig.TEXT_WIDTH, OrbConfig.TEXT_HEIGHT),
			widgetManager.syncColor(textId),
			widgetManager.syncText(textId)
		);

		orb.fill = widgetManager.createGraphic(
			orb.container,
			WidgetManager.pos(OrbConfig.EMPTY_X, OrbConfig.EMPTY_Y),
			WidgetManager.size(OrbConfig.EMPTY_SIZE, OrbConfig.EMPTY_SIZE),
			widgetManager.syncOpacity(fillId),
			widgetManager.syncSprite(fillId)
		);

		if (half)
		{
			orb.halfContainer = widgetManager.createLayer(
				orb.container,
				WidgetManager.pos(OrbConfig.EMPTY_X, OrbConfig.EMPTY_Y),
				WidgetManager.size(OrbConfig.EMPTY_SIZE / 2, OrbConfig.EMPTY_SIZE),
				widgetManager.syncHidden(halfContainerId)
			);

			orb.halfFill = widgetManager.createGraphic(
				orb.halfContainer,
				WidgetManager.pos(0, 0),
				WidgetManager.size(OrbConfig.EMPTY_SIZE, OrbConfig.EMPTY_SIZE),
				widgetManager.syncSprite(halfFillId)
			);
		}

		orb.emptyContainer = widgetManager.createLayer(
			orb.container,
			WidgetManager.pos(OrbConfig.EMPTY_X, OrbConfig.EMPTY_Y),
			WidgetManager.size(OrbConfig.EMPTY_SIZE, 0),
			widgetManager.syncHeight(emptyContainerId)
		);

		orb.emptyFill = widgetManager.createGraphic(
			orb.emptyContainer,
			WidgetManager.pos(0, 0),
			WidgetManager.size(OrbConfig.EMPTY_SIZE, OrbConfig.EMPTY_SIZE),
			widgetManager.syncSprite(emptyFillId)
		);

		orb.icon = widgetManager.createGraphic(
			orb.container,
			WidgetManager.pos(OrbConfig.EMPTY_X, OrbConfig.EMPTY_Y),
			WidgetManager.size(OrbConfig.EMPTY_SIZE, OrbConfig.EMPTY_SIZE),
			widgetManager.syncSprite(iconId)
		);

		orb.buttonId = buttonId;
		orb.textId = textId;
		orb.fillId = fillId;
		orb.emptyContainerId = emptyContainerId;
		orb.emptyFillId = emptyFillId;
		if (half)
		{
			orb.halfContainerId = halfContainerId;
			orb.halfFillId = halfFillId;
		}
		orb.iconId = iconId;
		return orb;
	}
}
