/*
 * Copyright (c) 2018 Abex
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

package com.compactorbs.widget.overlay.spec;

import com.compactorbs.CompactOrbsConfig;
import com.compactorbs.CompactOrbsConstants.RegenMeter;
import com.compactorbs.CompactOrbsConstants.Sprite;
import com.compactorbs.CompactOrbsConstants.Widgets;
import com.compactorbs.CompactOrbsManager;
import com.compactorbs.util.OrbUtil;
import static com.compactorbs.widget.overlay.spec.SpecTrackerOrb.ORB_SIZE;
import com.compactorbs.widget.overlay.spec.SpecTrackerOrb.OrbDisplayMode;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.annotations.Component;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class RegenMeterOverlay extends Overlay
{
	@Inject
	private Client client;

	@Inject
	private CompactOrbsManager manager;

	@Inject
	private CompactOrbsConfig config;

	@Inject
	private SpecColorRegistry specColors;

	@Inject
	private SpriteCache spriteCache;

	@Inject
	public RegenMeterOverlay()
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D g)
	{
		//might have to add HP later, in consideration of a planned feature down the line
		if (!config.showSpecRegenMeter())
		{
			return null;
		}

		Rectangle bounds = getOrbBounds(Widgets.Orb.SPEC_ORB, Widgets.Orb.NO_MAP_SPEC_ORB);
		if (bounds == null)
		{
			return null;
		}

		final SpecOrbType type = SpecOrbType.resolve(getSpecIconId());
		final int x = bounds.x + OrbDisplayMode.VANILLA.getX();
		final int y = bounds.y + OrbDisplayMode.VANILLA.getY();

		final Color color = specColors.color(type);
		final Color emptyColor = specColors.emptyColor(type);
		Color regenColor = specColors.regenColor(type);

		if (config.applyColorsToNativeSpec() ||
			config.showSpecTrackerOrb() && type != SpecOrbType.SPEC)
		{
			if (color != null || emptyColor != null)
			{
				Widget indicator = client.getWidget(Widgets.Orb.SPEC_ORB_INDICATOR);
				if (indicator == null || indicator.isHidden())
				{
					indicator = client.getWidget(Widgets.Orb.NO_MAP_SPEC_ORB_INDICATOR);
				}

				if (hasSpecAttack(indicator))
				{
					renderIndicator(g, x, y, color, isSpecActive(indicator));
					renderEmpty(g, x, y, type, emptyColor);
					renderIcon(g, x, y, type);
				}
			}
		}

		if (!config.applyColorsToNativeSpec())
		{
			if (type == SpecOrbType.SPEC && regenColor != null)
			{
				regenColor = RegenMeter.SPECIAL_COLOR;
			}
		}

		renderRegen(g, bounds, manager.getSpecOrbPercentage(type), regenColor);
		return null;
	}

	private void renderIndicator(Graphics2D g, int x, int y, Color color, boolean specActive)
	{
		int id = specActive ? Sprite.SPEC_INDICATOR_ACTIVE : Sprite.SPEC_INDICATOR;

		final BufferedImage indicator = spriteCache.getRecoloredImage(id, color);
		if (indicator != null)
		{
			g.drawImage(indicator, x, y, ORB_SIZE, ORB_SIZE, null);
		}
	}

	private void renderEmpty(Graphics2D g, int x, int y, SpecOrbType type, Color emptyColor)
	{
		final BufferedImage empty = spriteCache.getRecoloredImage(Sprite.EMPTY_INDICATOR, emptyColor);
		if (empty != null)
		{
			OrbUtil.drawEmpty(g, empty, x, y, ORB_SIZE, manager.getSpecOrbEnergyValues(type), type.getMax());
		}
	}

	private void renderIcon(Graphics2D g, int x, int y, SpecOrbType type)
	{
		final BufferedImage icon = spriteCache.getImage(type.getIconId());
		if (icon != null)
		{
			g.drawImage(icon, x, y, ORB_SIZE, ORB_SIZE, null);
		}
	}

	private void renderRegen(Graphics2D g, Rectangle bounds, double percent, Color color)
	{
		OrbUtil.drawRegenArc(g, bounds.x + OrbDisplayMode.VANILLA.getX(),
			bounds.y + (double) (bounds.height / 2 - ORB_SIZE / 2), ORB_SIZE, percent, color);
	}

	private Rectangle getOrbBounds(@Component int componentId, @Component int noOrbComponentId)
	{
		Widget widget = client.getWidget(componentId);

		if (widget == null || widget.isHidden())
		{
			widget = client.getWidget(noOrbComponentId);
		}

		return widget == null || widget.isHidden() ? null : widget.getBounds();
	}

	private boolean isSpecActive(Widget widget)
	{
		return widget != null && widget.getSpriteId() == Sprite.SPEC_INDICATOR_ACTIVE;
	}

	private boolean hasSpecAttack(Widget widget)
	{
		return widget != null && widget.getSpriteId() != Sprite.RUN_INDICATOR;
	}

	private int getSpecIconId()
	{
		Widget indicator = client.getWidget(Widgets.Orb.SPEC_ORB_ICON);
		if (indicator == null || indicator.isHidden())
		{
			indicator = client.getWidget(Widgets.Orb.NO_MAP_SPEC_ORB_ICON);
		}
		return indicator != null && !indicator.isHidden() ? indicator.getSpriteId() : -1;
	}

}
