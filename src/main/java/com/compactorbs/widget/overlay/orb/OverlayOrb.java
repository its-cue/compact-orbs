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
import com.compactorbs.CompactOrbsConstants.Sprite;
import com.compactorbs.CompactOrbsConstants.Widgets.MinimapOverlay.OrbConfig;
import static com.compactorbs.util.OrbCalc.calcColor;
import static com.compactorbs.util.OrbCalc.calcEmptyHeight;
import com.compactorbs.widget.WidgetManager;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;

@Slf4j
public class OverlayOrb
{
	Widget container;
	Widget frame;
	Widget button;
	Widget text;
	Widget fill;
	Widget halfContainer;
	Widget halfFill;
	Widget emptyContainer;
	Widget emptyFill;
	Widget icon;

	int buttonId;
	int textId;
	int fillId;
	int halfContainerId;
	int halfFillId;
	int emptyContainerId;
	int emptyFillId;
	int iconId;

	public boolean isHidden()
	{
		if (container != null)
		{
			return container.isHidden();
		}
		//xp orb uses button
		return button != null && button.isHidden();
	}

	public boolean hasMenu()
	{
		return button != null && !button.isHidden();
	}

	public void hideContainer(boolean hidden)
	{
		if (container != null)
		{
			container.setHidden(hidden);
		}
	}

	public void hideButton(boolean hidden)
	{
		if (button != null)
		{
			button.setHidden(hidden);
		}
	}

	public void syncXp(WidgetManager widgetManager, boolean isHpHidden)
	{
		widgetManager.syncMenuOp(button, buttonId);

		int x = isHpHidden
			? Layout.MinimapOverlay.XP_DROPS_X
			: Layout.Original.XP_DROPS_X;

		int y = isHpHidden
			? Layout.MinimapOverlay.XP_DROPS_Y
			: Layout.Original.XP_DROPS_Y;

		widgetManager.setPosition(button, x, y + OrbConfig.Y_OFFSET);
	}

	public void syncWorldMap(WidgetManager widgetManager, Client client)
	{
		widgetManager.syncMenuOp(button, buttonId);

		boolean hovering = button != null && button.contains(client.getMouseCanvasPosition());
		if (!hovering)
		{
			widgetManager.syncOpacity(button, buttonId);
		}
	}

	public void syncLogoutX(WidgetManager widgetManager, boolean hidden)
	{
		widgetManager.syncSprite(button, buttonId);
		widgetManager.syncMenuOp(button, buttonId);

		if (!hidden)
		{
			widgetManager.syncHidden(button, buttonId);
			widgetManager.syncHidden(icon, iconId);
		}
		else
		{
			if (button != null && !button.isHidden())
			{
				button.setHidden(true);
			}

			if (icon != null && !icon.isHidden())
			{
				icon.setHidden(true);
			}
		}
	}

	public void syncDataOrb(WidgetManager widgetManager)
	{
		widgetManager.syncHidden(button, buttonId);
		widgetManager.syncName(button, buttonId);
		widgetManager.syncOpacity(fill, fillId);
		widgetManager.syncSprite(fill, fillId);
		widgetManager.syncSprite(icon, iconId);
	}

	public void syncOrbDisplay(WidgetManager widgetManager)
	{
		widgetManager.syncColor(text, textId);
		widgetManager.syncText(text, textId);
		widgetManager.syncHeight(emptyContainer, emptyContainerId);
		widgetManager.syncSprite(emptyFill, emptyFillId);
	}

	public void updateOrbDisplay(WidgetManager widgetManager, int data, int current, int max)
	{
		String value = Integer.toString(data);

		if (text != null && !text.getText().equals(value))
		{
			widgetManager.setText(text, value);

			int color = calcColor(current, max);
			widgetManager.setColor(text, color);

			int height = calcEmptyHeight(26, current, max);
			widgetManager.setHeight(emptyContainer, height);

			//log.debug("updating overlay orb: {}, color: {}, height: {}", text, color, height);
		}
	}

	//spec orb disabled and enabled state logic based on equipped weapon
	public void handleSpecOrbState(boolean hasSpec, boolean isActive)
	{
		int spriteId;
		int opacity = 25;

		if (!hasSpec)
		{
			spriteId = Sprite.SPECIAL_ORB_FILL_DISABLED;
			opacity = 50;
			frame.setSpriteId(Sprite.ORB_FRAME);
		}
		else if (isActive)
		{
			spriteId = Sprite.SPECIAL_ORB_FILL_ACTIVATED;
		}
		else
		{
			spriteId = Sprite.SPECIAL_ORB_FILL;
		}

		if (button != null)
		{
			button.setHidden(!hasSpec);
		}

		if (fill.getSpriteId() != spriteId)
		{
			fill.setSpriteId(spriteId);
			fill.setOpacity(opacity);
		}
	}

	public void clear()
	{
		container = null;
		frame = null;
		button = null;
		text = null;
		fill = null;
		halfContainer = null;
		halfFill = null;
		emptyContainer = null;
		emptyFill = null;
		icon = null;
	}
}
