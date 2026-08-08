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

package com.compactorbs.widget.layout.edit.drag;

import com.compactorbs.CompactOrbsConfig;
import static com.compactorbs.CompactOrbsConstants.Layout.ORBS_CONTAINER_OFFSET_Y;
import com.compactorbs.CompactOrbsManager;
import com.compactorbs.widget.TargetWidget;
import com.compactorbs.widget.WidgetManager;
import com.compactorbs.widget.elements.Minimap;
import com.compactorbs.widget.layout.edit.Binding;
import com.compactorbs.widget.layout.edit.BindingManager;
import com.compactorbs.widget.layout.slot.SlotManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.client.input.MouseListener;

public class DragListener implements MouseListener
{
	@Inject
	private Client client;

	@Inject
	private CompactOrbsManager manager;

	@Inject
	private CompactOrbsConfig config;

	@Inject
	private WidgetManager widgetManager;

	@Inject
	private DragState dragState;

	@Inject
	private BindingManager bindingManager;

	@Inject
	private SlotManager slotManager;

	public void updateDrag()
	{
		Widget dragged = client.getDraggedWidget();
		if (dragged != null)
		{
			Binding binding = bindingManager.getByHandler(dragged);
			if (binding != null)
			{
				Widget draggedOn = client.getDraggedOnWidget();

				Widget container = widgetManager.getMapParent();
				if (container != null)
				{
					Widget bound = widgetManager.getTargetWidget(binding.get(manager));
					if (!dragState.wasDragging)
					{
						dragState.offset.x = dragState.origin.x - (container.getBounds().x + dragged.getRelativeX());
						dragState.offset.y = dragState.origin.y - (container.getBounds().y + dragged.getRelativeY());
					}

					if (manager.isCustomLayout() || !config.enableOrbSwapping())
					{
						if (!manager.isCompactLayout() || manager.isFixedMode())
						{
							container = client.getWidget(Minimap.ORBS_UNIVERSE.getComponentId());
							if (container == null)
							{
								return;
							}
						}

						if (bound != null)
						{
							Point pos = getDragPosition(dragged, container.getBounds());

							Point handlerPos = toBoundPosition(dragged, bound, pos);
							setPosition(dragged, handlerPos);

							draggedOn = getIndicatorTarget(dragged, draggedOn, container);
							setIndicatorTo(draggedOn);
							updateDraggedIndicator(bound, handlerPos);

							setPosition(bound, pos);
							if (binding.getRelated() != null)
							{
								Widget related = widgetManager.getTargetWidget(binding.getRelated());
								setPosition(related, pos);
							}
						}
					}
					else
					{
						container = client.getWidget(Minimap.ORBS_UNIVERSE.getComponentId());
						if (container != null)
						{
							setIndicatorTo(draggedOn);
							if (bound != null)
							{
								Point pos = getDragPosition(dragged, container.getBounds());
								setPosition(bound, pos);

								dragState.lastDraggedOn = draggedOn;
							}
						}
					}
				}
			}

			dragState.wasDragging = true;
			dragState.lastDraggedHandler = dragged;
		}
	}

	public void finalizeDrag()
	{
		dragState.wasDragging = false;

		if (dragState.lastDraggedHandler != null)
		{
			Binding binding = bindingManager.getByHandler(dragState.lastDraggedHandler);
			if (binding != null)
			{
				Widget container = widgetManager.getMapParent();
				if (!manager.isCompactLayout() || manager.isFixedMode())
				{
					container = client.getWidget(Minimap.ORBS_UNIVERSE.getComponentId());
				}

				if (container != null)
				{
					Widget bound = widgetManager.getTargetWidget(binding.get(manager));
					if (bound != null)
					{
						if (manager.isCustomLayout() || !config.enableOrbSwapping())
						{
							manager.saveCurrentLayoutPosition(bound, binding);
						}
						else
						{
							Point pos = toHandlerPosition(dragState.lastDraggedHandler, bound);
							setPosition(bound, pos);

							swapDraggedOrb();
						}
					}
				}

				resetDrag();
			}
		}
	}

	private void swapDraggedOrb()
	{
		if (dragState.lastDraggedOn == null)
		{
			return;
		}

		Binding draggedBinding = bindingManager.getByHandler(dragState.lastDraggedHandler);
		Binding targetBinding = bindingManager.getByHandler(dragState.lastDraggedOn);
		if (draggedBinding == null || targetBinding == null)
		{
			return;
		}

		TargetWidget draggedOrb = draggedBinding.get(manager);
		TargetWidget targetOrb = targetBinding.get(manager);
		if (draggedOrb == null || targetOrb == null)
		{
			return;
		}

		final int x = dragState.lastDraggedHandler.getOriginalX();
		final int y = dragState.lastDraggedHandler.getOriginalY();

		setPosition(dragState.lastDraggedHandler,
			new Point(
				dragState.lastDraggedOn.getOriginalX(),
				dragState.lastDraggedOn.getOriginalY()
			)
		);

		setPosition(dragState.lastDraggedOn, new Point(x, y));

		slotManager.swap(draggedOrb, targetOrb);
	}

	private void resetDrag()
	{
		if (dragState.handlerIndicator != null)
		{
			dragState.handlerIndicator.setOpacity(255);
		}

		if (dragState.boundIndicator != null)
		{
			widgetManager.hideIndicator(dragState.boundIndicator);
		}

		dragState.handlerIndicator = null;
		dragState.lastDraggedHandler = null;
	}

	private void updateDraggedIndicator(Widget widget, Point pos)
	{
		if (dragState.boundIndicator != null)
		{
			dragState.boundIndicator.setXPositionMode(widget.getXPositionMode());

			widgetManager.showIndicator(
				dragState.boundIndicator,
				pos.x, pos.y,
				widget.getWidth(), widget.getHeight(),
				0xff00ff
			);
		}
	}

	private void setIndicatorTo(Widget widget)
	{
		if (dragState.handlerIndicator != null)
		{
			dragState.handlerIndicator.setOpacity(255);
		}

		dragState.handlerIndicator = null;

		if (widget != null &&
			bindingManager.getByHandler(widget) != null)
		{
			//distinction between draggedOn (yellow) or proximity/overlap (green)
			int col = widget == client.getDraggedOnWidget()
				? 0xffff00 : 0x00ff00;

			widget.setOpacity(0);
			widget.setTextColor(col);

			dragState.handlerIndicator = widget;
		}
	}

	private Widget getIndicatorTarget(Widget dragged, Widget draggedOn, Widget container)
	{
		if (draggedOn != null && draggedOn != dragged)
		{
			return draggedOn;
		}

		Point pos = getDragPosition(dragged, container.getBounds());
		return findOverlappingHandler(dragged, pos, container.getWidth());
	}

	private Widget findOverlappingHandler(Widget dragged, Point pos, int containerWidth)
	{
		int dragX = pos.x;
		int dragY = pos.y;
		int dragW = dragged.getWidth();
		int dragH = dragged.getHeight();

		if (dragged.getXPositionMode() == WidgetPositionMode.ABSOLUTE_RIGHT)
		{
			dragX = containerWidth - dragX - dragW;
		}

		Widget overlap = null;
		int maxArea = -1;

		for (Binding binding : bindingManager.all())
		{
			Widget handler = binding.getHandler();
			if (handler == null || handler == dragged)
			{
				continue;
			}

			int handlerX = handler.getOriginalX();
			if (manager.isFixedMode() && handler.getXPositionMode() == WidgetPositionMode.ABSOLUTE_RIGHT)
			{
				Widget bound = client.getWidget(binding.getModern().getComponentId());
				if (bound != null)
				{
					handlerX = handlerX - (handler.getParent().getWidth() - bound.getParent().getWidth());
				}
			}

			int handlerY = handler.getOriginalY();
			if (!manager.isCompactLayout() && !manager.isFixedMode())
			{
				handlerY -= ORBS_CONTAINER_OFFSET_Y;
			}

			if (handler.getXPositionMode() == WidgetPositionMode.ABSOLUTE_RIGHT)
			{
				handlerX = containerWidth - handlerX - handler.getWidth();
			}

			int overlapW = Math.min(dragX + dragW, handlerX + handler.getWidth()) - Math.max(dragX, handlerX);
			int overlapH = Math.min(dragY + dragH, handlerY + handler.getHeight()) - Math.max(dragY, handlerY);
			if ((overlapW >= -2 && overlapH >= 1) || (overlapW >= 1 && overlapH >= -2))
			{
				int area = Math.max(0, overlapW) * Math.max(0, overlapH);
				if (area > maxArea)
				{
					maxArea = area;
					overlap = handler;
				}
			}
		}

		return overlap;
	}

	private void setPosition(Widget widget, Point pos)
	{
		if (widget == null)
		{
			return;
		}

		widget.setOriginalX(pos.x);
		widget.setOriginalY(pos.y);
		widget.revalidate();
	}

	public Point getDragPosition(Widget dragged, Rectangle bounds)
	{
		int x = dragState.current.x - bounds.x - dragState.offset.x;
		int y = dragState.current.y - bounds.y - dragState.offset.y;

		int maxX = bounds.width - dragged.getWidth();
		int maxY = bounds.height - dragged.getHeight();

		if (dragged.getXPositionMode() == WidgetPositionMode.ABSOLUTE_RIGHT)
		{
			x = bounds.width - x - dragged.getWidth();
			maxX = bounds.width - dragged.getWidth();
		}

		return clamp(new Point(x, y), maxX, maxY);
	}

	private Point clamp(Point pos, int maxX, int maxY)
	{
		return new Point(
			Math.max(0, Math.min(pos.x, maxX)),
			Math.max(0, Math.min(pos.y, maxY))
		);
	}

	public Point toBoundPosition(Widget handler, Widget bound, Point bounds)
	{
		Point offset = getLayoutOffsets();
		if (handler.getParent() != bound.getParent())
		{
			if (!manager.isCompactLayout() && !manager.isFixedMode())
			{
				bounds = new Point(bounds.x, bounds.y + ORBS_CONTAINER_OFFSET_Y);
			}

			if (bound.getXPositionMode() == WidgetPositionMode.ABSOLUTE_RIGHT)
			{
				bounds = new Point(bounds.x + (handler.getParent().getWidth() - bound.getParent().getWidth()), bounds.y);
			}
		}

		return new Point(bounds.x + offset.x, bounds.y + offset.y);
	}

	public Point toHandlerPosition(Widget handler, Widget bound)
	{
		int x = handler.getOriginalX();
		int y = handler.getOriginalY();

		Point offset = getLayoutOffsets();
		if (bound.getParent() != handler.getParent())
		{
			if (!manager.isCompactLayout() && !manager.isFixedMode())
			{
				y = y - ORBS_CONTAINER_OFFSET_Y;
			}

			if (bound.getXPositionMode() == WidgetPositionMode.ABSOLUTE_RIGHT)
			{
				x = x - (handler.getParent().getWidth() - bound.getParent().getWidth());
			}
		}

		x -= offset.x;
		y -= offset.y;

		return new Point(x, y);
	}

	private Point getLayoutOffsets()
	{
		return new Point(manager.getLayoutXOffset(), manager.getLayoutYOffset());
	}

	@Override
	public MouseEvent mousePressed(MouseEvent event)
	{
		dragState.origin = event.getPoint();
		return event;
	}

	@Override
	public MouseEvent mouseDragged(MouseEvent event)
	{
		dragState.current = event.getPoint();
		return event;
	}

	@Override
	public MouseEvent mouseMoved(MouseEvent event)
	{
		dragState.current = event.getPoint();
		return event;
	}

	@Override
	public MouseEvent mouseClicked(MouseEvent event)
	{
		return event;
	}

	@Override
	public MouseEvent mouseReleased(MouseEvent event)
	{
		return event;
	}

	@Override
	public MouseEvent mouseEntered(MouseEvent event)
	{
		return event;
	}

	@Override
	public MouseEvent mouseExited(MouseEvent event)
	{
		return event;
	}
}
