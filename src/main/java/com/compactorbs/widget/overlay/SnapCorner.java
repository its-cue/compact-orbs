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

package com.compactorbs.widget.overlay;

import com.compactorbs.CompactOrbsConstants.ConfigGroup;
import com.compactorbs.CompactOrbsConstants.ConfigKeys;
import com.compactorbs.CompactOrbsConstants.Layout;
import com.google.common.primitives.Ints;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;

@Slf4j
@Singleton
public class SnapCorner
{
	@Inject
	private Client client;

	@Inject
	private ConfigManager configManager;

	@Inject
	private OverlayManager overlayManager;

	public Overlay cornerOverlay;

	//cores snapcorner size/padding
	private static final int SNAP_CORNER_SIZE = 80;
	private static final int SNAP_CORNER_PADDING = 10;
	private static final int SNAP_PADDING = 2;

	public boolean updated;
	public boolean minimapInCorner = true;

	public int deltaX;
	public int deltaY;

	private int lastWidth;
	private int lastHeight;

	public boolean nativeDragWasDetected;

	public Point getSnapCornerLocation()
	{
		return configManager.getConfiguration(
			ConfigGroup.Core.RUNELITE,
			ConfigKeys.Core.SNAPCORNER_PREFIX + OverlayPosition.CANVAS_TOP_RIGHT + ConfigKeys.Core.SNAPCORNER_CONFIG_LOCATION,
			Point.class);
	}

	public boolean isSnapCornerRepositioned()
	{
		return getSnapCornerLocation() != null;
	}

	public void hasUpdated()
	{
		updated = true;
	}

	public void update(boolean minimapHidden)
	{
		if (minimapHidden && !updated)
		{
			deltaX = 0;
			deltaY = 0;
			updated = true;
			return;
		}

		final Dimension d = client.getRealDimensions();
		if (!updated && d.width == lastWidth && d.height == lastHeight)
		{
			return;
		}

		updated = false;
		lastWidth = d.width;
		lastHeight = d.height;
		updateDelta(d);
	}

	public void updateDelta(Dimension d)
	{
		//in TOP_RIGHT corner
		if (getSnapCornerLocation() == null)
		{
			deltaX = 0;
			deltaY = 0;
			return;
		}

		//can't get the real size, but we can pass what it would be if the minimap was the only thing inside
		final int lastsx = Layout.Original.MAP_CONTAINER_WIDTH;
		final int lastsy = Layout.Original.MAP_CONTAINER_HEIGHT + SNAP_PADDING;
		final int boxW = Math.max(lastsx, SNAP_CORNER_SIZE) + SNAP_CORNER_PADDING;

		final int absx = Ints.constrainToRange(d.width + getSnapCornerLocation().x - boxW,
			0,
			Math.max(0, d.width - lastsx)
		);

		deltaX = absx + boxW - d.width;
		deltaY = Ints.constrainToRange(getSnapCornerLocation().y,
			0,
			Math.max(0, d.height - lastsy)
		);
	}

	public void getPosition(boolean dragActive, boolean minimapHidden)
	{
		if (dragActive)
		{
			nativeDragWasDetected = true;
		}

		if (!dragActive || minimapHidden)
		{
			if (cornerOverlay != null)
			{
				cornerOverlay = null;
			}
			return;
		}

		if (cornerOverlay == null)
		{
			overlayManager.anyMatch(overlay ->
			{
				if (overlay.getName().equals(ConfigKeys.Core.CORNER_OVERLAY_PREFIX + OverlayPosition.CANVAS_TOP_RIGHT.name()))
				{
					cornerOverlay = overlay;
					log.debug("overlay match found: {} ", OverlayPosition.CANVAS_TOP_RIGHT.name());
					return true;
				}
				return false;
			});

			//don't proceed until the overlay has been set
			if (cornerOverlay == null)
			{
				return;
			}
		}

		if (!minimapInCorner)
		{
			return;
		}

		final Rectangle box = cornerOverlay.getBounds();
		if (cornerOverlay == null || box.isEmpty())
		{
			return;
		}

		final Dimension d = client.getRealDimensions();

		deltaX = box.x + box.width - d.width;
		deltaY = box.y;
	}

	public void clear()
	{
		updated = false;
		cornerOverlay = null;
		lastWidth = -1;
		lastHeight = -1;
	}
}
