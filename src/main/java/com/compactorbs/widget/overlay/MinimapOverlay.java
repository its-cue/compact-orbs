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
		if (widget == null)
		{
			return null;
		}

		final Rectangle parent = getParentBounds(widget);
		if (parent.isEmpty())
		{
			return null;
		}

		if (!manager.isMinimapOverlayEnabled() //not enabled
			|| !manager.isMinimapHidden() //map is visible
			|| !manager.isResized() //fixed mode
			|| manager.isMinimapMinimized()) //native minimized map
		{
			return null;
		}

		final Rectangle bounds = getBounds();

		// OverlayRenderer sets the overlay bounds to where it would like the overlay to render at prior to calling
		// render(). If the overlay has a preferred location or position set we update the widget position to that.
		if (getPreferredLocation() != null || getPreferredPosition() != null)
		{
			// The widget relative pos is relative to the parent
			widget.setForcedPosition(bounds.x - parent.x, bounds.y - parent.y);
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

			// Update the overlay bounds to the widget bounds so the drag overlay renders correctly.
			// Note OverlayManager uses original bounds reference to render managing mode and for
			// onMouseOver, so update the existing bounds vs. replacing the reference.
			Rectangle widgetBounds = widget.getBounds();
			bounds.setBounds(widgetBounds.x, widgetBounds.y, widgetBounds.width, widgetBounds.height);
		}

		return new Dimension(widget.getWidth(), widget.getHeight());
	}

	//returns true if overlay bounds matches widget bounds
	public boolean hasUpdatedBounds()
	{
		final Widget widget = client.getWidget(componentId);
		if (widget == null)
		{
			return false;
		}

		final Rectangle overlay = getBounds();
		return overlay.equals(widget.getBounds());
	}

	private Rectangle getParentBounds(final Widget widget)
	{
		if (widget == null)
		{
			parentBounds.setBounds(new Rectangle());
			return parentBounds;
		}

		final Widget parent = widget.getParent();
		final Rectangle bounds;

		if (parent == null)
		{
			bounds = new Rectangle(client.getRealDimensions());
		}
		else
		{
			bounds = parent.getBounds();
		}

		parentBounds.setBounds(bounds);
		return bounds;
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
