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

import com.compactorbs.CompactOrbsConfig;
import com.compactorbs.CompactOrbsManager;
import com.compactorbs.widget.WidgetManager;
import com.compactorbs.widget.elements.Minimap;
import com.compactorbs.widget.layout.slot.SlotManager;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;

@Slf4j
@Singleton
public class DetachedHandler
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private CompactOrbsConfig config;

	@Inject
	private CompactOrbsManager manager;

	@Inject
	private WidgetManager widgetManager;

	@Inject
	private SlotManager slotManager;

	private static final String CORE_CLASSIC_OVERLAY = "RESIZABLE_MINIMAP_STONES_WIDGET";
	private static final String CORE_MODERN_OVERLAY = "RESIZABLE_MINIMAP_WIDGET";

	private static final long DELAY_RESIZE = 150_000_000L;

	private final Map<String, Overlay> overlayRef = new HashMap<>();

	private final Rectangle union = new Rectangle();
	private final Rectangle snapshot = new Rectangle();

	@Getter
	private boolean cached;
	private boolean redraw;
	private boolean hasPreferred;

	@Getter
	private int snapshotRelX;
	@Getter
	private int snapshotRelY;
	@Getter
	private int detachedRelX;
	@Getter
	private int detachedRelY;
	@Getter
	private int unionWidth;
	@Getter
	private int unionHeight;

	private int lastContainerId = -1;

	private int canvasWidth;
	private int canvasHeight;
	private long changed;

	private int cacheAttempt;
	private boolean pendingCache;

	public void invalidate()
	{
		cancelSnapshot();
	}

	private void cancelSnapshot()
	{
		if (pendingCache)
		{
			cacheAttempt++;
			pendingCache = false;
		}
	}

	public void update(Rectangle bounds)
	{
		manager.isResizingCanvas = isResizingCanvas();

		if (!manager.isLoggedIn() || slotManager.getCurrentLayoutMode() == null)
		{
			return;
		}

		if (manager.isUpdatingProfile ||
			manager.isResizingCanvas ||
			manager.isEditingLayout ||
			manager.hideMinimapOverlay() ||
			!config.detachUtilityOrbs() ||
			manager.isNativeDragActive ||
			(manager.isSidePanelHidden() && config.hideMinimapWithSidePanel()))
		{
			restore(false);
			return;
		}

		Widget container = widgetManager.getMapParent();
		Widget orbs = widgetManager.getOrbsParent();
		if (container == null || orbs == null || container.isHidden())
		{
			restore(false);
			return;
		}

		if (container.getId() != lastContainerId)
		{
			lastContainerId = container.getId();
			restore(false);
			return;
		}

		if (!cached)
		{
			if (!pendingCache)
			{
				queueSnapshot();
			}
			return;
		}

		if (!redraw)
		{
			return;
		}

		union.setBounds(snapshot.union(bounds));

		container.setForcedPosition(
			union.x - container.getParent().getBounds().x,
			union.y - container.getParent().getBounds().y
		);
		container.revalidate();

		container.setWidth(union.width);
		container.setHeight(union.height);

		snapshotRelX = snapshot.x - union.x;
		snapshotRelY = snapshot.y - union.y;
		detachedRelX = bounds.x - union.x;
		detachedRelY = bounds.y - union.y;
		unionWidth = union.width;
		unionHeight = union.height;

		manager.rebuildLayout();

		redraw = false;
	}

	private void queueSnapshot()
	{
		pendingCache = true;
		final int id = ++cacheAttempt;
		final Rectangle match = new Rectangle();

		clientThread.invokeLater(() ->
		{
			if (id != cacheAttempt)
			{
				return true;
			}

			Widget container = widgetManager.getMapParent();
			if (container == null || container.isHidden())
			{
				pendingCache = false;
				return true;
			}

			Rectangle bounds = new Rectangle(
				container.getRelativeX(),
				container.getRelativeY(),
				container.getWidth(),
				container.getHeight());

			if (bounds.isEmpty() || !bounds.equals(match))
			{
				match.setBounds(bounds);
				return false;
			}

			removeCoreOverlay();

			hasPreferred = repositioned(
				manager.isClassicResizable() ? CORE_CLASSIC_OVERLAY : CORE_MODERN_OVERLAY);

			snapshot.setBounds(bounds);

			clientThread.invokeLater(() ->
			{
				if (id != cacheAttempt)
				{
					return;
				}
				pendingCache = false;
				cached = true;
				redraw = true;
			});
			return true;
		});
	}

	public void restore(boolean clear)
	{
		cancelSnapshot();

		if (cached)
		{
			if (!hasPreferred)
			{
				Widget container = widgetManager.getMapParent();
				if (container != null)
				{
					container.setForcedPosition(-1, -1);
					container.revalidate();
				}
			}

			widgetManager.revalidate(Minimap.MODERN_MAP_CONTAINER, Minimap.CLASSIC_MAP_CONTAINER);

			addCoreOverlay();

			manager.rebuildLayout();

			clientThread.invokeLater(() ->
			{
				cached = false;
			});
		}
		else
		{
			addCoreOverlay();
		}

		if (clear)
		{
			redraw = false;
			hasPreferred = false;
			lastContainerId = -1;
			snapshotRelX = 0;
			snapshotRelY = 0;
			detachedRelX = 0;
			detachedRelY = 0;
			unionWidth = 0;
			unionHeight = 0;
			snapshot.setBounds(0, 0, 0, 0);
			union.setBounds(0, 0, 0, 0);
		}
	}

	private boolean isResizingCanvas()
	{
		int w = client.getCanvasWidth();
		int h = client.getCanvasHeight();

		if (w != canvasWidth || h != canvasHeight)
		{
			canvasWidth = w;
			canvasHeight = h;
			changed = System.nanoTime();
		}

		return System.nanoTime() - changed < DELAY_RESIZE;
	}

	public void addCoreOverlay()
	{
		overlayRef.values().forEach(overlay ->
		{
			overlayManager.add(overlay);
			log.debug("added core overlay : {}", overlay.getName());
		});

		overlayRef.clear();
	}

	public void removeCoreOverlay()
	{
		overlayManager.removeIf(overlay ->
		{
			if (CORE_CLASSIC_OVERLAY.equals(overlay.getName()) ||
				CORE_MODERN_OVERLAY.equals(overlay.getName()))
			{
				overlayRef.put(overlay.getName(), overlay);
				log.debug("removing core overlay : {}", overlay.getName());
				return true;
			}
			return false;
		});
	}

	private boolean repositioned(String name)
	{
		Overlay overlay = overlayRef.get(name);
		return overlay != null &&
			(overlay.getPreferredLocation() != null ||
				overlay.getPreferredPosition() != null ||
				(overlay.getPosition() != OverlayPosition.DYNAMIC && manager.snapCornerRepositioned));
	}
}