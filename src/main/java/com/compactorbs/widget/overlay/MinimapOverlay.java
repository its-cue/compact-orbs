/*
 * Copyright (c) 2018, Tomas Slusny <slusnucky@gmail.com>
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

import com.compactorbs.CompactOrbsConstants.Widgets;
import com.compactorbs.CompactOrbsManager;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

@Slf4j
public class MinimapOverlay extends Overlay
{
	private final Client client;
	private final CompactOrbsManager manager;

	private final int componentId = Widgets.MinimapOverlay.UNIVERSE;
	private final Rectangle parentBounds = new Rectangle();
	private boolean revalidate;

	@Inject
	private MinimapOverlay(Client client, CompactOrbsManager manager)
	{
		this.client = client;
		this.manager = manager;
		setPriority(Overlay.PRIORITY_HIGHEST);
		setLayer(OverlayLayer.UNDER_WIDGETS);
		setPosition(OverlayPosition.CANVAS_TOP_RIGHT);
		setMovable(true);
		setSnappable(true);
	}

	@Override
	public String getName()
	{
		return "COMPACT_ORBS_MINIMAP_OVERLAY";
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		final Widget widget = client.getWidget(componentId);
		final Rectangle parent = getParentBounds(widget);

		if (parent.isEmpty())
		{
			return null;
		}

		if (widget == null)
		{
			return null;
		}

		if (manager.isFixedMode()
			|| !manager.isMinimapOverlayEnabled()
			|| !manager.isMinimapHidden()
			|| manager.isMinimapMinimized())
		{
			return null;
		}

		final Rectangle bounds = getBounds();
		if (getPreferredLocation() != null || getPreferredPosition() != null || (getPosition() != OverlayPosition.DYNAMIC && manager.snapCornerRepositioned))
		{
			// If the widget is manually moved or the snapcorner it is in is moved, force the widget to be in the snapcorner bounds
			widget.setForcedPosition(bounds.x - parent.x, bounds.y - parent.y); // the widget relative pos is relative to its parent
		}
		else
		{
			if (revalidate)
			{
				revalidate = false;
				log.debug("Revalidating : {}", getName());
				widget.setForcedPosition(-1, -1);
				// Revalidate the widget to reposition it back to its normal location after an overlay reset
				widget.revalidate();
			}

			// Otherwise allow the widget to draw where it wants to, and update the overlay bounds to reflect that.
			Rectangle widgetBounds = widget.getBounds();
			bounds.setBounds(widgetBounds.x, widgetBounds.y, widgetBounds.width, widgetBounds.height);
		}

		return new Dimension(widget.getWidth(), widget.getHeight());
	}

	private Rectangle getParentBounds(final Widget widget)
	{
		if (widget == null || widget.isHidden())
		{
			parentBounds.setBounds(0, 0, 0, 0);
			return parentBounds;
		}

		final Widget parent = widget.getParent();

		if (parent == null)
		{
			var d = client.getRealDimensions();
			parentBounds.setBounds(0, 0, d.width, d.height);
		}
		else
		{
			parentBounds.setBounds(parent.getBounds());
		}

		return parentBounds;
	}

	@Override
	public Rectangle getParentBounds()
	{
		if (!client.isClientThread())
		{
			// During overlay drag this is called on the EDT, so we just
			// cache and reuse the last known parent bounds.
			return parentBounds;
		}

		final Widget widget = client.getWidget(componentId);
		return getParentBounds(widget);
	}

	@Override
	public void revalidate()
	{
		// Revalidate must be called on the client thread, so defer til next frame
		revalidate = true;
	}
}
