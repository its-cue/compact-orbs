/*
 * Copyright (c) 2025, cue <https://github.com/its-cue>
 * Copyright (c) 2025, ldavid432 <https://github.com/ldavid432/gauntlet-loot-popup>
 * Copyright (c) 2024, seacelery <https://github.com/seacelery/soulreaper-axe-qol>
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

package com.compactorbs.widget.overlay.spec;

import com.compactorbs.CompactOrbsConfig;
import com.compactorbs.CompactOrbsConstants.Enum;
import com.compactorbs.CompactOrbsConstants.InventoryId;
import com.compactorbs.CompactOrbsConstants.Sprite;
import com.compactorbs.CompactOrbsConstants.Varbit;
import com.compactorbs.CompactOrbsManager;
import com.compactorbs.util.OrbUtil;
import com.compactorbs.widget.elements.Orbs;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.WorldType;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

@Singleton
public class SpecTrackerOrb extends Overlay
{
	@Inject
	private Client client;

	@Inject
	private CompactOrbsManager manager;

	@Inject
	private CompactOrbsConfig config;

	@Inject
	private SpecColorRegistry specColors;

	@Inject
	private SpriteCache spriteCache;

	//frame/orb size
	private static final int WIDTH = 57;
	private static final int HEIGHT = 34;
	public static final int ORB_SIZE = 26;

	//per orb config
	private SpecOrbType type;

	private int energy;
	private int displayed;
	private int max;

	private boolean isSraEquipped;
	private boolean isRondacheEquipped;
	private boolean weaponHasSpec;
	private boolean shieldHasSpec;
	private boolean isVisible;

	@Getter
	@RequiredArgsConstructor
	public enum OrbDisplayMode
	{
		VANILLA("Original", 27, 4, WIDTH, HEIGHT, 0),
		SMALL_TEXT("Compact (text)", 0, 0, HEIGHT, HEIGHT, 4),
		SMALL_ICON("Compact (icon)", 0, 0, HEIGHT, HEIGHT, 4),
		SMALL_BOTH("Compact (both)", 0, 0, HEIGHT, HEIGHT, 4);

		final String name;
		final int x;
		final int y;
		final int width;
		final int height;
		final int offset;

		@Override
		public String toString()
		{
			return name;
		}

		public boolean isVanilla()
		{
			return this == VANILLA;
		}

		public boolean isSmallBoth()
		{
			return this == SMALL_BOTH;
		}

		public boolean showsIcon()
		{
			return this != SMALL_TEXT;
		}

		public boolean showsText()
		{
			return this != SMALL_ICON;
		}
	}

	@RequiredArgsConstructor
	public enum OrbTextShadowStyle
	{
		VANILLA("Vanilla"),
		OUTLINE("Outline"),
		OUTLINE_THICK("Outline (thick)");

		private final String name;

		@Override
		public String toString()
		{
			return name;
		}
	}

	@Inject
	public SpecTrackerOrb()
	{
		setPriority(Overlay.PRIORITY_LOW);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPosition(OverlayPosition.DYNAMIC);
		setMovable(true);
		setSnappable(true);
	}

	@Override
	public String getName()
	{
		return "COMPACT_ORBS_SPEC_TRACKER_ORB";
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showSpecTrackerOrb() || !isVisible || !isSpecOrbVisible())
		{
			return null;
		}

		final OrbDisplayMode mode = config.displayMode();
		final int x = mode.getX() + mode.getOffset();
		final int y = mode.getY() + mode.getOffset();
		final Color color = specColors.color(type);
		final Color emptyColor = specColors.emptyColor(type);

		renderFrame(graphics, mode);
		renderIndicator(graphics, x, y, color);
		renderEmpty(graphics, x, y, emptyColor);
		renderRegenMeter(graphics, x, y);
		renderIcon(graphics, mode, x, y);
		renderText(graphics, mode);

		return new Dimension(mode.getWidth(), mode.getHeight());
	}

	private void renderFrame(Graphics2D graphics, OrbDisplayMode mode)
	{
		final BufferedImage frame = spriteCache.getImage(mode.isVanilla() ? Sprite.FRAME : Sprite.FRAME_SMALL);
		if (frame != null)
		{
			graphics.drawImage(frame, 0, 0, null);
		}
	}

	private void renderIndicator(Graphics2D graphics, int x, int y, Color color)
	{
		final BufferedImage indicator = spriteCache.getRecoloredImage(Sprite.SPEC_INDICATOR, color);
		if (indicator != null)
		{
			graphics.drawImage(indicator, x, y, ORB_SIZE, ORB_SIZE, null);
		}
	}

	private void renderEmpty(Graphics2D graphics, int x, int y, Color emptyColor)
	{
		final BufferedImage empty = spriteCache.getRecoloredImage(Sprite.EMPTY_INDICATOR, emptyColor);
		if (empty != null)
		{
			OrbUtil.drawEmpty(graphics, empty, x, y, ORB_SIZE, energy, max);
		}
	}

	private void renderRegenMeter(Graphics2D graphics, int x, int y)
	{
		if (config.showTrackerOrbRegenMeter())
		{
			OrbUtil.drawRegenArc(graphics, x, y, ORB_SIZE, manager.getSpecOrbPercentage(type), specColors.regenColor(type));
		}
	}

	private void renderIcon(Graphics2D graphics, OrbDisplayMode mode, int x, int y)
	{
		if (!mode.showsIcon())
		{
			return;
		}

		final BufferedImage icon = spriteCache.getImage(type.getIconId());
		if (icon != null)
		{
			graphics.drawImage(icon, x, y - (mode.isSmallBoth() ? 2 : 0), ORB_SIZE, ORB_SIZE, null);
		}
	}

	private void renderText(Graphics2D graphics, OrbDisplayMode mode)
	{
		if (!mode.showsText())
		{
			return;
		}

		final String value = Integer.toString(displayed);

		graphics.setFont(FontManager.getRunescapeSmallFont());
		final int textWidth = graphics.getFontMetrics().stringWidth(value);

		final int x;
		final int y;
		if (mode.isVanilla())
		{
			x = 4 + (23 - textWidth) / 2;
			y = 10 + (mode.getHeight() / 2);
		}
		else
		{
			x = (mode.getWidth() / 2) - (textWidth / 2);
			y = (mode.isSmallBoth() ? 14 : 6) + (mode.getHeight() / 2);
		}

		renderTextShadow(graphics, value, x, y);

		final Color textColor = config.specTrackerOrbTextColor() != null
			? config.specTrackerOrbTextColor()
			: new Color(OrbUtil.calcColor(energy, max, true));

		graphics.setColor(textColor);
		graphics.drawString(value, x, y);
	}

	private void renderTextShadow(Graphics2D graphics, String value, int x, int y)
	{
		final OrbTextShadowStyle style = config.specTrackerOrbTextShadowStyle();
		final Color shadowColor = config.specTrackerOrbTextShadowColor() != null
			? config.specTrackerOrbTextShadowColor()
			: Color.BLACK;

		graphics.setColor(shadowColor);

		//vanilla shadow
		graphics.drawString(value, x + 1, y + 1);

		//outline
		if (style == OrbTextShadowStyle.OUTLINE || style == OrbTextShadowStyle.OUTLINE_THICK)
		{
			graphics.drawString(value, x - 1, y);
			graphics.drawString(value, x + 1, y);
			graphics.drawString(value, x, y - 1);
			graphics.drawString(value, x, y + 1);

			if (style == OrbTextShadowStyle.OUTLINE_THICK)
			{
				graphics.drawString(value, x - 1, y - 1);
				graphics.drawString(value, x + 1, y - 1);
				graphics.drawString(value, x - 1, y + 1);
			}
		}
	}

	public void updateWorn()
	{
		if (!config.showSpecTrackerOrb())
		{
			return;
		}

		int weapon = getWornItemId(EquipmentInventorySlot.WEAPON.getSlotIdx());
		int shield = getWornItemId(EquipmentInventorySlot.SHIELD.getSlotIdx());

		isSraEquipped = weapon
			== ItemID.SOULREAPER || weapon == ItemID.SOULREAPER_AXE_ORN;

		isRondacheEquipped = shield
			== ItemID.RONDACHE;

		weaponHasSpec = hasSpecAttack(weapon);
		shieldHasSpec = hasSpecAttack(shield);

		updateOrb();
	}

	public void updateOrb()
	{
		boolean hasSoulReaperStacks = manager.getSpecOrbEnergyValues(SpecOrbType.SRA) > 0;
		boolean hasRondacheStacks = manager.getSpecOrbEnergyValues(SpecOrbType.RONDACHE) > 0;

		if (hasSoulReaperStacks && !isSraEquipped)
		{
			type = SpecOrbType.SRA;
		}
		else if (isSraEquipped || (isRondacheEquipped && !weaponHasSpec))
		{
			type = SpecOrbType.SPEC;
		}
		else if ((isRondacheEquipped && shieldHasSpec) || hasRondacheStacks)
		{
			type = SpecOrbType.RONDACHE;
		}
		//TODO if DFS gets a special...
		else
		{
			isVisible = false;
			return;
		}

		energy = manager.getSpecOrbEnergyValues(type);
		displayed = energy / type.getDivisor();
		max = type.getMax();

		isVisible = true;
	}

	//should hide the tracker orb when the spec orb is hidden (hide on side panel, etc)
	private boolean isSpecOrbVisible()
	{
		Widget widget = client.getWidget(Orbs.SPEC_ORB_CONTAINER.getComponentId());
		if (widget == null)
		{
			return true;
		}

		return !widget.isHidden();
	}

	//from the cs2 - the orb counts as active when on a members world, or in LMS (for f2p)
	//while a worn item has a spec cost
	private boolean hasSpecAttack(int id)
	{
		if (id == -1)
		{
			return false;
		}

		int wornSpecCost = client.getEnum(Enum.SPECIAL_ATTACK_COST).getIntValue(id);

		boolean isMembers = client.getWorldType().contains(WorldType.MEMBERS);
		boolean isInBR = client.getVarbitValue(Varbit.BR_INGAME) != 0;

		return (isMembers || isInBR) && wornSpecCost > 0;
	}

	//get the worn equipment id in the designated slot
	private int getWornItemId(int slot)
	{
		ItemContainer worn = client.getItemContainer(InventoryId.WORN);
		if (worn == null)
		{
			return -1;
		}

		Item equipment = worn.getItem(slot);
		if (equipment == null)
		{
			return -1;
		}

		return equipment.getId();
	}
}
