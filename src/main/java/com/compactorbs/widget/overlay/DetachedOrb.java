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
import com.compactorbs.widget.elements.Minimap;
import com.compactorbs.widget.elements.Orbs;
import com.compactorbs.widget.layout.edit.Binding;
import com.compactorbs.widget.layout.edit.BindingManager;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Point;
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
	private final SnapCorner snapcorner;
	private final BindingManager bindingManager;
	private final String name;
	private final int componentId;
	private final int index;

	private final Rectangle parentBounds = new Rectangle();
	private boolean revalidate;

	public DetachedOrb(Client client, CompactOrbsManager manager, SnapCorner snapcorner, BindingManager bindingManager, String name, int componentId, int index)
	{
		this.client = client;
		this.manager = manager;
		this.snapcorner = snapcorner;
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

		if (!manager.getCurrentLayout().isFloating() ||
			manager.isMinimapMinimized() ||
			manager.isFixedMode())
		{
			snapcorner.hasUpdated();
			return null;
		}

		//check whether the minimap is in the top_right snapcorner
		if (widget.getId() == Minimap.MODERN_MAP_MINIMAP.getComponentId() ||
			widget.getId() == Minimap.CLASSIC_MAP_MINIMAP.getComponentId())
		{
			snapcorner.minimapInCorner = getPreferredLocation() == null &&
				(getPreferredPosition() == null || getPreferredPosition() == getPosition());
		}

		//reposition the orbs while the minimap is inside the top_right snapcorner
		if (snapcorner.minimapInCorner)
		{
			snapcorner.getPosition(manager.isNativeDragActive, manager.followWhileDragging());

			if (manager.hasGraphicsReset())
			{
				snapcorner.hasUpdated();
			}

			snapcorner.update(manager.followWhileDragging());
		}

		//keep the compass hidden when the side panel is closed
		if (widget.getId() == Minimap.MODERN_MAP_MINIMAP.getComponentId() &&
			manager.isMinimapHidden() &&
			manager.hideOrbOnTabClose &&
			!manager.isEditingLayout &&
			!widget.isSelfHidden())
		{
			widget.setHidden(true);
		}

		//when the world map is hidden, don't put it in the snapcorner when its moved
		if (widget.getId() == Orbs.WORLD_MAP_CONTAINER.getComponentId() &&
			manager.hideWorldMap && !manager.isEditingLayout)
		{
			widget.setForcedPosition(getSnapcornerX(widget, parent), getSnapCornerY(widget) + getParentY(widget));
			return null;
		}

		final Rectangle bounds = getBounds();
		if (getPreferredLocation() != null || getPreferredPosition() != null ||
			(manager.isMinimapHidden() || !snapcorner.minimapInCorner))
		{
			// If the widget is manually moved or the snapcorner it is in is moved, force the widget to be in the snapcorner bounds
			widget.setForcedPosition(bounds.x - parent.x, bounds.y - parent.y); // the widget relative pos is relative to its parent
		}
		else if (widget.getXPositionMode() == WidgetPositionMode.ABSOLUTE_LEFT ||
			widget.getXPositionMode() == WidgetPositionMode.ABSOLUTE_RIGHT)
		{
			final int x = getSnapcornerX(widget, parent) + snapcorner.deltaX;
			final int y = getSnapCornerY(widget) + getParentY(widget) + snapcorner.deltaY;

			widget.setForcedPosition(x, y);
			bounds.setBounds(parent.x + x, parent.y + y, widget.getWidth(), widget.getHeight());
		}
		else
		{
			if (revalidate)
			{
				revalidate = false;
				widget.setForcedPosition(-1, -1);
				// Revalidate the widget to reposition it back to its normal location after an overlay reset
				widget.revalidate();
			}

			// Otherwise allow the widget to draw where it wants to, and update the overlay bounds to reflect that.
			Rectangle widgetBounds = widget.getBounds();
			bounds.setBounds(widgetBounds.x, widgetBounds.y, widgetBounds.width, widgetBounds.height);
		}

		//make sure the logout x stone follows the icon
		if (widget.getId() == Orbs.LOGOUT_X_ICON.getComponentId())
		{
			final Widget stone = client.getWidget(Orbs.LOGOUT_X_STONE.getComponentId());
			if (stone != null)
			{
				stone.setForcedPosition(widget.getRelativeX(), widget.getRelativeY());
			}
		}

		updateEditModeHandlerPos(widget);

		if (!manager.isMinimapHidden() && snapcorner.minimapInCorner)
		{
			//prevent the default snapcorner from being larger than it should be
			//when all the widgets are positioned inside it
			final Point mouse = client.getMouseCanvasPosition();
			final boolean hovered = bounds.contains(mouse.getX(), mouse.getY());
			if (getPreferredLocation() == null && getPreferredPosition() == null)
			{
				if ((widget.getId() == Minimap.MODERN_MAP_MINIMAP.getComponentId() ||
					widget.getId() == Minimap.CLASSIC_MAP_MINIMAP.getComponentId()))
				{
					return new Dimension(CompactOrbsConstants.Layout.Original.MAP_CONTAINER_WIDTH,
						CompactOrbsConstants.Layout.Original.MAP_CONTAINER_HEIGHT);
				}
				else
				{
					if (!hovered)
					{
						return new Dimension(0, 0);
					}
				}
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
		if (widget.getId() == Orbs.WORLD_MAP_CONTAINER.getComponentId())
		{
			if (manager.isEditingLayout)
			{
				return CompactOrbsConstants.Layout.Original.WORLD_MAP_Y;
			}

			if (manager.hideWorldMap)
			{
				return -39;//account for +10 from getParentY
			}
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

	public boolean isDetached()
	{
		return getPreferredLocation() != null ||
			(getPreferredPosition() != null && getPreferredPosition() != getPosition());
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

	private Rectangle getParentBounds(final Widget widget)
	{
		if (widget == null ||
			widget.isHidden() && widget.getId() != Minimap.MODERN_MAP_MINIMAP.getComponentId())
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

	public void updateEditModeHandlerPos(Widget widget)
	{
		if (widget != null)
		{
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
					int x = widget.getRelativeX();
					int y = widget.getRelativeY();

					//offset the handler so it sits ontop of the compass
					if (manager.isFloating() && !manager.isMinimapHidden() &&
						(widget.getId() == Minimap.MODERN_MAP_MINIMAP.getComponentId() ||
							widget.getId() == Minimap.CLASSIC_MAP_MINIMAP.getComponentId()))
					{
						x += 30;
						y += 1;
					}

					handler.setForcedPosition(x, y);
					break;
				}
			}
		}
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
