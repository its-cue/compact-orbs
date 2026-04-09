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

package com.compactorbs.widget;

import com.compactorbs.CompactOrbsConstants;
import com.compactorbs.CompactOrbsConstants.Script;
import com.compactorbs.CompactOrbsConstants.Widgets.Classic;
import com.compactorbs.CompactOrbsConstants.Widgets.Modern;
import com.compactorbs.CompactOrbsManager;
import com.compactorbs.util.SetValue;
import com.compactorbs.util.ValueKey;
import com.compactorbs.widget.elements.Orbs;
import com.compactorbs.widget.offset.OffsetManager;
import com.compactorbs.widget.slot.Slot;
import com.compactorbs.widget.slot.SlotManager;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;

@Slf4j
@Singleton
public class WidgetManager
{
	@Inject
	private Client client;

	@Inject
	private CompactOrbsManager manager;

	@Inject
	private SlotManager slotManager;

	private boolean targetRemapped = false;

	//update multiple widgets that match the script id (FORCE_UPDATE bypasses the check)
	public void remapTargets(boolean compactLayout, int scriptId, TargetWidget... widgets)
	{
		//ensure slot layout correctness before remapping
		slotManager.updateCurrentSlotLayout();

		for (TargetWidget target : widgets)
		{
			if (!shouldUpdateTarget(target, scriptId))
			{
				continue;
			}

			remapTarget(target, compactLayout);
		}
	}

	//update a target widgets X/Y or position mode, based on layout, if necessary
	public void remapTarget(TargetWidget target, boolean compactLayout)
	{
		Widget widget = getTargetWidget(target);
		if (widget == null)
		{
			return;
		}

		targetRemapped = false;

		//ignore the widget inspector (shares same container/index as grid master)
		if (target == Orbs.GRID_MASTER_ORB_CONTAINER &&
			widget.getSpriteId() == CompactOrbsConstants.Sprite.WIDGET_INSPECTOR)
		{
			return;
		}

		getTarget(target).getValueMap().forEach((key, value) ->
			setValue(widget, key, value, compactLayout)
		);

		if (targetRemapped)
		{
			widget.revalidate();
		}
	}

	//returns a target widget
	private TargetWidget getTarget(TargetWidget target)
	{
		if (target instanceof Orbs)
		{
			return getSlotTarget(target);
		}

		return target;
	}

	//returns which orb the target should reference
	public TargetWidget getSlotTarget(TargetWidget target)
	{
		if (!Orbs.SWAPPABLE_ORBS.contains((Orbs) target) || !manager.enableOrbSwapping())
		{
			return target;
		}

		Slot slot = slotManager.findSlotByOrb(target, slotManager.currentSlotLayoutMode);
		if (slot == null)
		{
			return target;
		}

		return slot.getOriginal();
	}

	//sets the widgets X/Y or position mode as necessary
	private void setValue(Widget widget, ValueKey key, SetValue value, boolean compactLayout)
	{
		if (value == null)
		{
			return;
		}

		Integer v = value.get(compactLayout, manager.getLayout());
		if (v == null)
		{
			return;
		}

		switch (key)
		{
			case X:
				updateValue(widget::getOriginalX, widget::setOriginalX,
					OffsetManager.getTargetOffset(widget, key, v, compactLayout, manager, slotManager));
				break;
			case Y:
				updateValue(widget::getOriginalY, widget::setOriginalY,
					OffsetManager.getTargetOffset(widget, key, v, compactLayout, manager, slotManager));
				break;
			case WIDTH:
				updateValue(widget::getOriginalWidth, widget::setOriginalWidth, v);
				break;
			case HEIGHT:
				updateValue(widget::getOriginalHeight, widget::setOriginalHeight, v);
				break;
			case X_POSITION_MODE:
				updateValue(widget::getXPositionMode, widget::setXPositionMode, v);
				break;
			case Y_POSITION_MODE:
				updateValue(widget::getYPositionMode, widget::setYPositionMode, v);
				break;
			case WIDTH_MODE:
				updateValue(widget::getWidthMode, widget::setWidthMode, v);
				break;
			case HEIGHT_MODE:
				updateValue(widget::getHeightMode, widget::setHeightMode, v);
				break;

			default:
				throw new IllegalStateException("Unhandled ValueKey: " + key);
		}
	}

	//sets a value only if it has changed
	public void updateValue(IntSupplier getter, IntConsumer setter, int value)
	{
		if (getter.getAsInt() != value)
		{
			setter.accept(value);
			targetRemapped = true;
		}
	}

	//set visibility for target widgets, excluding the wiki banner (handled in updateWikiBanner)
	public void setTargetsHidden(boolean hidden, TargetWidget... widgets)
	{
		for (TargetWidget target : widgets)
		{
			if (target == Orbs.WIKI_VANILLA_CONTAINER || target == Orbs.WIKI_ICON_CONTAINER)
			{
				continue;
			}

			setHidden(target, hidden);
		}
	}

	//set visibility for a target widget and its children (if they exist), if necessary
	public void setHidden(TargetWidget target, boolean hidden)
	{
		Widget widget = getTargetWidget(target);
		if (widget == null)
		{
			return;
		}

		if (target == Orbs.GRID_MASTER_ORB_CONTAINER &&
			widget.getSpriteId() == CompactOrbsConstants.Sprite.WIDGET_INSPECTOR)
		{
			return;
		}

		widget.setHidden(hidden);

		//specifically for compass menu options (could be others?)
		if (widget.getChildren() != null)
		{
			for (Widget child : widget.getChildren())
			{
				if (child != null)
				{
					child.setHidden(hidden);
				}
			}
		}

	}

	public void setHidden(int componentId, boolean hidden)
	{
		Widget widget = client.getWidget(componentId);
		if (widget == null)
		{
			return;
		}

		widget.setHidden(hidden);
	}

	//get the widget for the given TargetWidget
	public Widget getTargetWidget(TargetWidget target)
	{
		Widget widget = client.getWidget(target.getComponentId());
		if (widget == null)
		{
			return null;
		}
		if (target.getArrayId() == -1)
		{
			return widget;
		}
		return widget.getChild(target.getArrayId());
	}

	//returns the current visible parent widget
	public Widget getCurrentParent()
	{
		Widget parent = getParent(Modern.ORBS);
		if (parent != null && !parent.isHidden())
		{
			return parent;
		}

		return getParent(Classic.ORBS);
	}

	//returns the parent widget for the given component ID
	//can exist and be hidden, so check for visibility
	public Widget getParent(int componentId)
	{
		Widget parent = client.getWidget(componentId);
		if (parent != null && !parent.isHidden())
		{
			return parent;
		}

		return null;
	}

	//remove all children from the given component id (if they exist, used for custom children)
	public void clearChildren(int componentId)
	{
		Widget widget = client.getWidget(componentId);
		if (widget != null)
		{
			Widget child = widget.getChild(0);
			if (child != null)
			{
				widget.deleteAllChildren();
			}
		}
	}

	public void syncMenuOp(Widget target, int componentId)
	{
		if (target == null)
		{
			return;
		}

		Widget widget = client.getWidget(componentId);
		if (widget == null)
		{
			return;
		}

		String[] actions = widget.getActions();
		if (actions == null)
		{
			return;
		}

		String[] targetActions = target.getActions();

		for (int i = 0; i < actions.length; i++)
		{
			String action = null;
			if (targetActions != null && i < targetActions.length)
			{
				action = targetActions[i];
			}

			if (!Objects.equals(action, actions[i]))
			{
				target.setAction(i, actions[i]);
			}
		}
	}

	public void syncSprite(Widget target, int componentId)
	{
		if (target == null)
		{
			return;
		}

		Widget widget = client.getWidget(componentId);
		if (widget == null)
		{
			return;
		}

		int spriteId = widget.getSpriteId();
		if (target.getSpriteId() != spriteId)
		{
			target.setSpriteId(spriteId);
		}
	}

	public void syncHidden(Widget target, int componentId)
	{
		if (target == null)
		{
			return;
		}

		Widget widget = client.getWidget(componentId);
		if (widget == null)
		{
			return;
		}

		boolean hidden = widget.isSelfHidden();
		if (target.isHidden() != hidden)
		{
			target.setHidden(hidden);
		}
	}

	//check if a target widget should be updated based on script id (or FORCE_UPDATE)
	private boolean shouldUpdateTarget(TargetWidget target, int scriptId)
	{
		return (scriptId == Script.FORCE_UPDATE) || target.getScriptId() == scriptId;
	}

	//check if the child is missing, or not a child of the given parent widget
	public boolean isMissing(Widget child, Widget parent)
	{
		return child == null || child.getParentId() != parent.getId();
	}

	//return the interface id from the component id
	public static int getInterfaceId(int componentId)
	{
		return componentId >> 16;
	}

	//return the child id from the component id
	public static int getChildId(int componentId)
	{
		return componentId & 0xff;
	}

	private Widget createWidget(
		Widget parent,
		int type,
		Consumer<Widget> config)
	{
		if (parent == null)
		{
			return null;
		}

		Widget widget = parent.createChild(-1, type);
		config.accept(widget);
		widget.revalidate();
		return widget;
	}

	@SafeVarargs
	public final Widget createLayer(Widget parent, Consumer<Widget>... configs)
	{
		return createWidget(parent, WidgetType.LAYER, config(configs));
	}


	@SafeVarargs
	public final Widget createText(Widget parent, Consumer<Widget>... configs)
	{
		return createWidget(parent, WidgetType.TEXT, config(configs));
	}

	@SafeVarargs
	public final Widget createGraphic(Widget parent, Consumer<Widget>... configs)
	{
		return createWidget(parent, WidgetType.GRAPHIC, config(configs));
	}

	@SafeVarargs
	public static Consumer<Widget> config(Consumer<Widget>... configs)
	{
		return w ->
		{
			for (Consumer<Widget> config : configs)
			{
				config.accept(w);
			}
		};
	}

	public static Consumer<Widget> contentType(int type)
	{
		return w -> w.setContentType(type);
	}

	public static Consumer<Widget> pos(int x, int y)
	{
		return w -> w.setOriginalX(x).setOriginalY(y);
	}

	public static Consumer<Widget> size(int width, int height)
	{
		return w -> w.setOriginalWidth(width).setOriginalHeight(height);
	}

	public static Consumer<Widget> posMode(int xMode, int yMode)
	{
		return w -> w.setXPositionMode(xMode).setYPositionMode(yMode);
	}

	public static Consumer<Widget> sizeMode(int widthMode, int heightMode)
	{
		return w -> w.setWidthMode(widthMode).setHeightMode(heightMode);
	}

	public static Consumer<Widget> opacity(int opacity)
	{
		return w -> w.setOpacity(opacity);
	}

	public static Consumer<Widget> hidden(boolean hidden)
	{
		return w -> w.setHidden(hidden);
	}

	public static Consumer<Widget> sprite(int spriteId)
	{
		return w -> w.setSpriteId(spriteId);
	}

	public static Consumer<Widget> listener()
	{
		return w -> w.setHasListener(true);
	}

	public static Consumer<Widget> noClickThrough()
	{
		return w -> w.setNoClickThrough(true);
	}

	public static Consumer<Widget> onOp(Object... objects)
	{
		return w ->
		{
			if (objects != null)
			{
				w.setOnOpListener(objects);
			}
		};
	}

	public static Consumer<Widget> onVarTransmit(Object... objects)
	{
		return w ->
		{
			if (objects != null)
			{
				w.setOnVarTransmitListener(objects);
			}
		};
	}

	public static Consumer<Widget> varTransmitTrigger(int... trigger)
	{
		return w -> w.setVarTransmitTrigger(trigger);
	}

	public static Consumer<Widget> onHover(JavaScriptCallback mouseOver, JavaScriptCallback mouseLeave)
	{

		return w ->
		{
			if (mouseOver != null)
			{
				w.setOnMouseOverListener(mouseOver);
			}

			if (mouseLeave != null)
			{
				w.setOnMouseLeaveListener(mouseLeave);
			}
		};
	}

	public static Consumer<Widget> action(int index, String action)
	{
		return w ->
		{
			if (action != null)
			{
				w.setAction(index, action);
			}
		};
	}

	public Consumer<Widget> syncMenuOp(int componentId)
	{
		return w -> syncMenuOp(w, componentId);
	}

	public Consumer<Widget> syncSprite(int componentId)
	{
		return w -> syncSprite(w, componentId);
	}

	public Widget createToggleButton(
		Widget parent,
		int width, int height,
		int opacity,
		int spriteId,
		String menuOp,
		JavaScriptCallback opListener,
		JavaScriptCallback mouseOver,
		JavaScriptCallback mouseLeave)
	{
		return createGraphic(
			parent,
			size(width, height),
			opacity(opacity),
			sprite(spriteId),
			hidden(false),
			action(0, menuOp),
			listener(),
			noClickThrough(),
			onOp(opListener),
			onHover(
				mouseOver,
				mouseLeave
			)
		);
	}
}
