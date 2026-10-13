/*
 * Copyright (c) 2018, Tomas Slusny <slusnucky@gmail.com>,
 * Copyright (c) 2025, cue <https://github.com/its-cue>
 *
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

import com.compactorbs.CompactOrbsConstants;
import com.compactorbs.CompactOrbsManager;
import com.compactorbs.widget.TargetWidget;
import com.compactorbs.widget.elements.Orbs;
import com.compactorbs.widget.layout.edit.Binding;
import com.compactorbs.widget.layout.edit.BindingManager;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

@Slf4j
public class DetachedOrb extends Overlay
{
	private final Client client;
	private final CompactOrbsManager manager;
	private final BindingManager bindingManager;
	private final String name;
	private final int componentId;
	private final int index;

	private final Rectangle parentBounds = new Rectangle();
	private boolean revalidate;

	private boolean hasPreferred = true;
	private boolean hiddenByTab;
	private boolean tabWasVisible;
	private boolean insideDefaultSnapcorner;

	public DetachedOrb(Client client, CompactOrbsManager manager, BindingManager bindingManager, String name, int componentId, int index)
	{
		this.client = client;
		this.manager = manager;
		this.bindingManager = bindingManager;
		this.name = name;
		this.componentId = componentId;
		this.index = index;
		setPriority(Overlay.PRIORITY_HIGHEST);
		setLayer(OverlayLayer.UNDER_WIDGETS);
		setPosition(OverlayPosition.CANVAS_TOP_RIGHT);
		setMovable(true);
		setSnappable(true);
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		updateOnTabHidden();

		if (!manager.getCurrentLayout().isFloating() ||
			manager.isMinimapMinimized() ||
			manager.isFixedMode())
		{
			clearForcedPosition();
			return null;
		}

		final Widget widget = getWidget();
		final Rectangle parent = getParentBounds(widget);

		if (parent.isEmpty())
		{
			return null;
		}

		if (widget == null)
		{
			return null;
		}

		//when the world map is hidden, don't put it in the snapcorner when its moved
		if (widget.getId() == Orbs.WORLD_MAP_CONTAINER.getComponentId() &&
			manager.hideWorldMap && !manager.isEditingLayout)
		{
			widget.setForcedPosition(getSnapcornerX(widget, parent), getSnapCornerY(widget) + getParentY(widget));
			hasPreferred = false;
			return null;
		}

		final Rectangle bounds = getBounds();
		if (getPreferredLocation() != null || getPreferredPosition() != null || (getPosition() != OverlayPosition.DYNAMIC && manager.snapCornerRepositioned))
		{
			// If the widget is manually moved or the snapcorner it is in is moved, force the widget to be in the snapcorner bounds
			widget.setForcedPosition(bounds.x - parent.x, bounds.y - parent.y);
			hasPreferred = false;
		}
		else if (widget.getXPositionMode() == WidgetPositionMode.ABSOLUTE_LEFT ||
			widget.getXPositionMode() == WidgetPositionMode.ABSOLUTE_RIGHT)
		{
			final int snapX = getSnapcornerX(widget, parent);
			final int snapY = getSnapCornerY(widget) + getParentY(widget);

			widget.setForcedPosition(snapX, snapY);
			hasPreferred = false;

			bounds.setBounds(parent.x + snapX, parent.y + snapY, widget.getWidth(), widget.getHeight());
		}
		else
		{
			if (revalidate || !hasPreferred)
			{
				hasPreferred = true;
				revalidate = false;
				widget.setForcedPosition(-1, -1);
				// Revalidate the widget to reposition it back to its normal location after an overlay reset
				widget.revalidate();
			}

			// Otherwise allow the widget to draw where it wants to, and update the overlay bounds to reflect that.
			Rectangle widgetBounds = widget.getBounds();
			bounds.setBounds(widgetBounds.x, widgetBounds.y, widgetBounds.width, widgetBounds.height);
		}

		if (widget.getId() == Orbs.LOGOUT_X_ICON.getComponentId())
		{
			final Widget stone = client.getWidget(Orbs.LOGOUT_X_STONE.getComponentId());
			if (stone != null)
			{
				stone.setForcedPosition(widget.getRelativeX(), widget.getRelativeY());
			}
		}

		if (manager.isEditingLayout)
		{
			for (Binding binding : bindingManager.all())
			{
				TargetWidget target = binding.get(manager);

				if (target == null || target.getComponentId() != widget.getId())
				{
					continue;
				}

				Widget handler = binding.getHandler();
				handler.setForcedPosition(widget.getRelativeX(), widget.getRelativeY());
				handler.setSize(widget.getWidth(), widget.getHeight());
				break;
			}
		}

		return new Dimension(widget.getWidth(), widget.getHeight());
	}

	private int getSnapcornerX(Widget widget, Rectangle parent)
	{
		//adjust LEFT aligned widgets when they reset to their snapcorner
		if (widget.getXPositionMode() == WidgetPositionMode.ABSOLUTE_LEFT)
		{
			final int offset = client.getCanvasWidth() - CompactOrbsConstants.Layout.Original.MAP_CONTAINER_WIDTH;

			return widget.getOriginalX() + offset;
		}

		int offset = widget.getOriginalX();
		if (widget.getParent().getId() == CompactOrbsConstants.Widgets.Orb.UNIVERSE)
		{
			//adjust wiki/world map by the difference between the universe and map container width
			offset = 4;
		}

		return parent.width - (widget.getWidth() + offset);
	}

	private int getSnapCornerY(Widget widget)
	{
		if (manager.isEditingLayout && widget.getId() == Orbs.WORLD_MAP_CONTAINER.getComponentId())
		{
			return CompactOrbsConstants.Layout.Original.WORLD_MAP_Y;
		}

		return widget.getOriginalY();
	}

	private int getParentY(Widget widget)
	{
		if (widget.getParent().getId() == CompactOrbsConstants.Widgets.Orb.UNIVERSE)
		{
			return CompactOrbsConstants.Layout.ORBS_CONTAINER_OFFSET_Y;
		}

		return 0;
	}

	private void updateOnTabHidden()
	{
		final boolean active = manager.hideOrbOnTabClose && !manager.isEditingLayout;

		if (active && !tabWasVisible)
		{
			insideDefaultSnapcorner = getPreferredLocation() == null &&
				(getPreferredPosition() == null || getPreferredPosition() == getPosition());
		}

		tabWasVisible = active;

		//hide an orb outside their default snapcorner
		final boolean hide = active && !insideDefaultSnapcorner;

		if (hide != hiddenByTab)
		{
			hiddenByTab = hide;
			manager.setOrbTabHidden(componentId, hide);
		}
	}

	private Widget getWidget()
	{
		final Widget widget = client.getWidget(componentId);
		if (index != -1 && widget != null)
		{
			return widget.getChild(index);
		}

		return widget;
	}

	private void clearForcedPosition()
	{
		if (hasPreferred)
		{
			return;
		}

		hasPreferred = true;
		revalidate = false;

		final Widget widget = getWidget();
		if (widget != null)
		{
			log.debug("reset forced position: {}}", widget.getId());

			widget.setForcedPosition(-1, -1);
			widget.revalidate();

			if (widget.getId() == Orbs.LOGOUT_X_ICON.getComponentId())
			{
				final Widget stone = client.getWidget(Orbs.LOGOUT_X_STONE.getComponentId());
				if (stone != null)
				{
					stone.setForcedPosition(-1, -1);
					stone.revalidate();
				}
			}
		}
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
			return parentBounds;
		}

		return getParentBounds(getWidget());
	}

	@Override
	public void revalidate()
	{
		revalidate = true;
	}
}
