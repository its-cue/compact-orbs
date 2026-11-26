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

import com.compactorbs.CompactOrbsConstants.ConfigKeys;
import com.compactorbs.CompactOrbsConstants.Layout;
import com.google.common.primitives.Ints;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
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
	private OverlayManager overlayManager;

	public Overlay cornerOverlay;

	private static final int TOTAL_HEIGHT = 0;
	private static final int MAX_WIDTH = 1;
	private static final int OVERLAY_COUNT = 2;

	public boolean minimapInCorner;

	// Core's snapcorner size/padding
	private static final int SNAP_CORNER_SIZE = 80;
	private static final int SNAP_CORNER_PADDING = 10;
	private static final int SNAP_PADDING = 2;

	// Minimum size when the minimap is inside the snapcorner (approximate)
	private static final int DEFAULT_BOX_W = Math.max(Layout.Original.MAP_CONTAINER_WIDTH, SNAP_CORNER_SIZE) + SNAP_CORNER_PADDING;
	private static final int DEFAULT_BOX_H = Math.max(Layout.Original.MAP_CONTAINER_HEIGHT + SNAP_PADDING, SNAP_CORNER_SIZE) + SNAP_CORNER_PADDING;

	private int curX;
	private int curY;

	private int boxW = DEFAULT_BOX_W;
	private int boxH = DEFAULT_BOX_H;

	//core configs
	@Getter
	@Setter
	private Point snapCornerLocation;
	@Getter
	@Setter
	private Point snapCornerPreferredLocation;

	public int deltaX;
	public int deltaY;

	public boolean nativeDragWasDetected;

	public void getPosition(boolean dragActive)
	{
		if (dragActive)
		{
			nativeDragWasDetected = true;
		}
		else
		{
			boxW = DEFAULT_BOX_W;
			boxH = DEFAULT_BOX_H;
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
		}

		final Dimension d = client.getRealDimensions();
		boolean followingDrag = false;

		if (cornerOverlay != null && dragActive)
		{
			final Rectangle box = cornerOverlay.getBounds();

			if (!box.isEmpty())
			{
				boxW = Math.max(box.width, DEFAULT_BOX_W);
				boxH = Math.max(box.height, DEFAULT_BOX_H);

				followingDrag = true;

				curX = box.x + box.width - d.width;
				curY = box.y;
			}
		}

		if (!followingDrag)
		{
			updateBoxSize();
			applySavedPosition();
		}

		applyDelta(d);
	}

	private void applySavedPosition()
	{
		if (snapCornerLocation != null)
		{
			curX = snapCornerLocation.x;
			curY = snapCornerLocation.y;
		}
		else
		{
			curX = 0;
			curY = 0;
		}
	}

	private void updateBoxSize()
	{
		final int[] stackTotals = {
			Layout.Original.MAP_CONTAINER_HEIGHT,
			Layout.Original.MAP_CONTAINER_WIDTH,
			1
		};

		overlayManager.anyMatch(o ->
		{
			if (o == cornerOverlay || o instanceof DetachedOrb || o.getPreferredLocation() != null)
			{
				return false;
			}

			final OverlayPosition position = o.getPreferredPosition() != null ? o.getPreferredPosition() : o.getPosition();
			final Rectangle b = o.getBounds();

			//ignore overlays whose positions are not in the CANVAS_TOP_RIGHT snapcorner, bounds are null or empty, or if the overlay rectangle is empty
			if (position != OverlayPosition.CANVAS_TOP_RIGHT || b == null || b.isEmpty())
			{
				return false;
			}

			//log.debug("overlays found: {}", o.getName());

			stackTotals[TOTAL_HEIGHT] += b.height;
			stackTotals[MAX_WIDTH] = Math.max(stackTotals[MAX_WIDTH], b.width);
			stackTotals[OVERLAY_COUNT]++;
			return false;
		});

		final int totalHeight = stackTotals[0];
		final int maxWidth = stackTotals[1];
		final int overlayCount = stackTotals[2];

		boxW = Math.max(maxWidth, SNAP_CORNER_SIZE) + SNAP_CORNER_PADDING;
		boxH = Math.max(totalHeight + overlayCount * SNAP_PADDING, SNAP_CORNER_SIZE) + SNAP_CORNER_PADDING;
	}

	private void applyDelta(Dimension d)
	{
		deltaX = Ints.constrainToRange(curX, Math.min(boxW - d.width, SNAP_CORNER_PADDING), SNAP_CORNER_PADDING);
		deltaY = Ints.constrainToRange(curY, 0, Math.max(0, d.height - (boxH - SNAP_CORNER_PADDING)));
	}

	//on shutdown or config/profile change
	public void clear()
	{
		cornerOverlay = null;
		snapCornerLocation = null;
		snapCornerPreferredLocation = null;
		deltaX = 0;
		deltaY = 0;
		curX = 0;
		curY = 0;
		boxW = DEFAULT_BOX_W;
		boxH = DEFAULT_BOX_H;
		nativeDragWasDetected = false;
	}
}
