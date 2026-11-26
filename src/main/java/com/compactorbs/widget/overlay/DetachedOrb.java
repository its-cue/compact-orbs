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

import com.compactorbs.CompactOrbsConstants.Layout;
import com.compactorbs.CompactOrbsConstants.Widgets;
import com.compactorbs.CompactOrbsManager;
import com.compactorbs.widget.TargetWidget;
import com.compactorbs.widget.WidgetManager;
import com.compactorbs.widget.elements.Minimap;
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
	private final WidgetManager widgetManager;
	private final SnapCorner snapcorner;
	private final BindingManager bindingManager;
	private final TargetWidget target;
	private final String name;
	private final int componentId;
	private final int index;

	private final Rectangle parentBounds = new Rectangle();
	private boolean minimapWasHidden;
	private boolean revalidate;

	public DetachedOrb(
		Client client,
		CompactOrbsManager manager,
		WidgetManager widgetManager,
		SnapCorner snapcorner,
		BindingManager bindingManager,
		TargetWidget target,
		String name, int componentId, int index)
	{
		this.client = client;
		this.manager = manager;
		this.widgetManager = widgetManager;
		this.snapcorner = snapcorner;
		this.bindingManager = bindingManager;
		this.name = name;
		this.componentId = componentId;
		this.index = index;
		this.target = target;

		setPriority(2.0f);
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

		if (!manager.isFloatingActive() ||
			manager.isMinimapMinimized() ||
			manager.isFixedMode())
		{
			return null;
		}

		final boolean minimapHidden = manager.isMinimapHidden();
		if (minimapHidden != minimapWasHidden)
		{
			minimapWasHidden = minimapHidden;
		}

		if (isMinimap(widget))
		{
			snapcorner.minimapInCorner = !isDetached() && !minimapHidden;
		}

		final boolean applySnapCornerPosition =
			!isDetached() && (minimapHidden || !snapcorner.minimapInCorner);

		final boolean applyLayoutPosition =
			!isDetached() && !minimapHidden && snapcorner.minimapInCorner;

		if (!isMinimap(widget))
		{
			final OverlayPosition position = applyLayoutPosition ? OverlayPosition.DYNAMIC : OverlayPosition.CANVAS_TOP_RIGHT;
			if (getPosition() != position)
			{
				setPosition(position);
				setMovable(true);
			}
		}

		final Rectangle bounds = getBounds();
		if (isDetached() || applySnapCornerPosition)
		{
			if (getPreferredLocation() != null ||
				getPreferredPosition() != null ||
				(getPosition() != OverlayPosition.DYNAMIC && (minimapHidden || !snapcorner.minimapInCorner)))
			{
				widget.setForcedPosition(bounds.x, bounds.y);

				//can stay true on minimap toggle... not sure if anything but oh well
				if (revalidate)
				{
					revalidate = false;
				}
			}
		}
		else
		{
			final int x = getSnapCornerX(widget, parent) + snapcorner.deltaX;
			final int y = getSnapCornerY(widget) + snapcorner.deltaY;

			if (revalidate || minimapWasHidden)
			{
				revalidate = false;
				minimapWasHidden = false;
				widget.setForcedPosition(-1, -1);
				widgetManager.remapTargets(target);
			}

			widget.setForcedPosition(x, y);

			Rectangle widgetBounds = widget.getBounds();
			bounds.setBounds(
				manager.isNativeDragActive ? x : widgetBounds.x,
				manager.isNativeDragActive ? y : widgetBounds.y,
				widget.getWidth(), widget.getHeight());
		}

		if (widget.getId() == Orbs.LOGOUT_X_ICON.getComponentId())
		{
			final Widget stone = client.getWidget(Orbs.LOGOUT_X_STONE.getComponentId());
			if (stone != null)
			{
				stone.setForcedPosition(widget.getRelativeX(), widget.getRelativeY());
			}
		}

		updateEditModeHandlerPos(widget);

		if (isMinimap(widget) && !manager.isMinimapHidden())
		{
			return new Dimension(Layout.Original.MAP_CONTAINER_WIDTH, Layout.Original.MAP_CONTAINER_HEIGHT);
		}

		return new Dimension(widget.getWidth(), widget.getHeight());
	}

	private int getSnapCornerX(Widget widget, Rectangle parent)
	{
		int x = widget.getOriginalX();
		if (widget.getXPositionMode() == WidgetPositionMode.ABSOLUTE_LEFT)
		{
			final int offset = parent.width - Layout.Original.MAP_CONTAINER_WIDTH;
			return x + offset;
		}

		int offset = 0;
		if (widget.getParent().getId() == Widgets.Orb.UNIVERSE)
		{
			offset = 4;
		}

		return parent.width - (x + widget.getWidth() + offset);
	}

	private int getSnapCornerY(Widget widget)
	{
		int y = widget.getOriginalY();
		if (widget.getParent().getId() == Widgets.Orb.UNIVERSE)
		{
			y = y + Layout.ORBS_CONTAINER_OFFSET_Y;
		}

		return y;
	}

	public boolean isDetached()
	{
		return getPreferredLocation() != null ||
			(getPreferredPosition() != null && getPreferredPosition() != OverlayPosition.CANVAS_TOP_RIGHT);
	}

	private boolean isMinimap(Widget widget)
	{
		return widget.getId() == Minimap.MODERN_MAP_MINIMAP.getComponentId() ||
			widget.getId() == Minimap.CLASSIC_MAP_MINIMAP.getComponentId();
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

	//exclude the minimap, since its container is used for the compass
	private boolean isWidgetHidden(Widget widget)
	{
		return widget.isHidden() && !isMinimap(widget);
	}

	private boolean hideCompass(Widget widget)
	{
		boolean isCompass = isMinimap(widget);

		return !manager.isEditingLayout && isCompass && manager.isCompassHidden() && manager.isMinimapHidden();
	}

	//world map hiding uses an offset rather than setHidden()
	private boolean hideWorldMap(Widget widget)
	{
		return widget.getId() == Orbs.WORLD_MAP_CONTAINER.getComponentId() && manager.hideWorldMap;
	}

	//wiki container doesn't get directly hidden, only its children
	private boolean hideWiki(Widget widget)
	{
		boolean childHidden = true;
		if (widget.getId() == Orbs.WIKI_ICON_CONTAINER.getComponentId())
		{
			//wiki plugin child is index 0
			Widget child = widget.getChild(0);
			if (child == null)
			{
				//vanilla child icon id
				child = client.getWidget(Widgets.Orb.WIKI_ICON_VANILLA);
			}

			if (child != null)
			{
				childHidden = child.isHidden();
			}
		}

		return widget.getId() == Orbs.WIKI_ICON_CONTAINER.getComponentId() &&
			childHidden && !manager.isEditingLayout;
	}

	private Rectangle getParentBounds(final Widget widget)
	{
		if (widget == null ||
			isWidgetHidden(widget) ||
			hideCompass(widget) ||
			hideWorldMap(widget) ||
			hideWiki(widget))
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
					if (manager.isFloating() && !manager.isMinimapHidden() && isMinimap(widget))
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
