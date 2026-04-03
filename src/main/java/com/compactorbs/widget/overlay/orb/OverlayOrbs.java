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

package com.compactorbs.widget.overlay.orb;

import com.compactorbs.CompactOrbsConstants.Layout;
import com.compactorbs.CompactOrbsConstants.Script;
import com.compactorbs.CompactOrbsConstants.VarPlayer;
import com.compactorbs.CompactOrbsConstants.Widgets;
import com.compactorbs.CompactOrbsConstants.Widgets.MinimapOverlay.HpOrb;
import com.compactorbs.CompactOrbsConstants.Widgets.MinimapOverlay.OrbConfig;
import com.compactorbs.CompactOrbsConstants.Widgets.MinimapOverlay.PrayOrb;
import com.compactorbs.CompactOrbsConstants.Widgets.MinimapOverlay.RunOrb;
import com.compactorbs.CompactOrbsConstants.Widgets.MinimapOverlay.SpecOrb;
import com.compactorbs.CompactOrbsConstants.Widgets.MinimapOverlay.WorldMap;
import com.compactorbs.CompactOrbsManager;
import com.compactorbs.widget.WidgetManager;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;

public class OverlayOrbs
{
	private final WidgetManager widgetManager;
	private final CompactOrbsManager manager;

	@Inject
	public OverlayOrbs(
		WidgetManager widgetManager,
		CompactOrbsManager manager,
		Client client)
	{
		this.widgetManager = widgetManager;
		this.manager = manager;
	}

	private OverlayOrb create(
		Widget parent,
		Function<OverlayOrbBuilder, OverlayOrb> build,
		Consumer<OverlayOrbBuilder> consumer)
	{
		OverlayOrbBuilder builder = new OverlayOrbBuilder(widgetManager, manager, parent);
		consumer.accept(builder);
		return build.apply(builder);
	}

	public OverlayOrb createXP(Widget parent)
	{
		int x = manager.isOverlayHpHidden()
			? Layout.MinimapOverlay.XP_DROPS_X
			: Layout.Original.XP_DROPS_X;

		int y = manager.isOverlayHpHidden()
			? Layout.MinimapOverlay.XP_DROPS_Y
			: Layout.Original.XP_DROPS_Y;

		return create(
			parent,
			OverlayOrbBuilder::buildXpOrb,
			orb -> orb
				.setPos(x, y + OrbConfig.Y_OFFSET)
				.setButtonId(Widgets.Orb.XP_DROPS)
		);
	}

	public OverlayOrb createLogoutX(Widget parent)
	{
		return create(
			parent,
			OverlayOrbBuilder::buildLogoutX,
			orb -> orb
				.setPos(Layout.Original.LOGOUT_X, Layout.Original.LOGOUT_Y)
				.setButtonId(Widgets.Modern.LOGOUT_X_STONE)
				.setIconId(Widgets.Modern.LOGOUT_X_ICON)
		);
	}

	public OverlayOrb createWorldMap(Widget parent)
	{
		return create(
			parent,
			OverlayOrbBuilder::buildWorldMap,
			orb -> orb
				.setPos(
					Layout.Original.WORLD_MAP_X,
					Layout.Original.WORLD_MAP_Y
						+ OrbConfig.Y_OFFSET
				)
				.setButtonId(WorldMap.WORLDMAP)
		);
	}

	public HPOverlayOrb createHpOrb(Widget parent)
	{
		return (HPOverlayOrb) create(
			parent,
			OverlayOrbBuilder::buildHalfOrb,
			orb -> orb
				.setPos(
					Layout.Original.HP_ORB_X,
					Layout.Original.HP_ORB_Y +
						OrbConfig.Y_OFFSET
				)
				.setVarTransmit(Script.ORBS_UPDATE_HEALTH, -1,
					HpOrb.HEALTH_TEXT,
					HpOrb.ORB_HEALTH_EMPTY,
					HpOrb.ORB_HEALTH_EMPTY_GRAPHIC0,
					HpOrb.HEALTH_INDICATOR,
					HpOrb.HEALTH_EXTRAPOISONHALF,
					HpOrb.HEALTH_EXTRAPOISONHALF_CONTENTS,
					HpOrb.HEALTH_BACKING,
					HpOrb.HEALTHBUTTON,
					HpOrb.ORB_HEALTH_HEART_ICON
				)
				.setVarTriggers(
					VarPlayer.POISON,
					VarPlayer.DISEASE,
					VarPlayer.NIGHTMARE_TEMP,
					VarPlayer.WORN_ITEM_BONUS2,
					VarPlayer.BR_TEMP_1
				)
				.setButtonId(HpOrb.HEALTHBUTTON)
				.setTextId(HpOrb.HEALTH_TEXT)
				.setFillId(HpOrb.HEALTH_INDICATOR)
				.setHalfIds(
					HpOrb.HEALTH_EXTRAPOISONHALF,
					HpOrb.HEALTH_EXTRAPOISONHALF_CONTENTS
				)
				.setEmptyIds(
					HpOrb.ORB_HEALTH_EMPTY,
					HpOrb.ORB_HEALTH_EMPTY_GRAPHIC0
				)
				.setIconId(HpOrb.ORB_HEALTH_HEART_ICON)
		);
	}

	public OverlayOrb createPrayerOrb(Widget parent)
	{
		return create(
			parent,
			OverlayOrbBuilder::buildOrb,
			orb -> orb
				.setPos(
					Layout.Original.PRAYER_ORB_X,
					Layout.Original.PRAYER_ORB_Y
						+ OrbConfig.Y_OFFSET
				)
				.setVarTransmit(Script.ORBS_UPDATE_PRAYER, -1,
					PrayOrb.PRAYER_TEXT,
					PrayOrb.ORB_PRAYER_EMPTY,
					PrayOrb.PRAYER_INDICATOR,
					PrayOrb.PRAYER_ICON,
					PrayOrb.PRAYER_BACKING,
					PrayOrb.PRAYERBUTTON
				)
				.setVarTriggers(
					VarPlayer.ARMOURHITSOUND,
					VarPlayer.BR_TEMP_1,
					VarPlayer.CORRUPTION
				)
				.setButtonId(PrayOrb.PRAYERBUTTON)
				.setTextId(PrayOrb.PRAYER_TEXT)
				.setFillId(PrayOrb.PRAYER_INDICATOR)
				.setEmptyIds(
					PrayOrb.ORB_PRAYER_EMPTY,
					PrayOrb.ORB_PRAYER_EMPTY_GRAPHIC0
				)
				.setIconId(PrayOrb.PRAYER_ICON)
		);
	}

	public OverlayOrb createRunOrb(Widget parent)
	{
		return create(
			parent,
			OverlayOrbBuilder::buildOrb,
			orb -> orb
				.setPos(
					Layout.Original.RUN_ORB_X,
					Layout.Original.RUN_ORB_Y
						+ OrbConfig.Y_OFFSET
				)
				.setVarTransmit(Script.ORBS_UPDATE_RUNENERGY, -1,
					RunOrb.RUNENERGY_TEXT,
					RunOrb.ORB_RUNENERGY_EMPTY,
					RunOrb.RUNENERGY_INDICATOR,
					RunOrb.RUNENERGY_ICON,
					RunOrb.RUNENERGY_BACKING,
					RunOrb.RUNBUTTON
				)
				.setVarTriggers(
					VarPlayer.OPTION_RUN,
					VarPlayer.INFERNO_TEMP_NOPROTECT_TRANSMIT,
					VarPlayer.TUTORIAL,
					VarPlayer.TUTORIAL_2
				)
				.setButtonId(RunOrb.RUNBUTTON)
				.setTextId(RunOrb.RUNENERGY_TEXT)
				.setFillId(RunOrb.RUNENERGY_INDICATOR)
				.setEmptyIds(
					RunOrb.ORB_RUNENERGY_EMPTY,
					RunOrb.ORB_RUNENERGY_EMPTY_GRAPHIC0
				)
				.setIconId(RunOrb.RUNENERGY_ICON)
		);
	}

	public OverlayOrb createSpecOrb(Widget parent)
	{
		return create(
			parent,
			OverlayOrbBuilder::buildOrb,
			orb -> orb
				.setPos(
					Layout.Original.SPEC_ORB_X,
					Layout.Original.SPEC_ORB_Y
						+ OrbConfig.Y_OFFSET
				)
				.setVarTransmit(Script.ORBS_UPDATE_SPECENERGY, -1,
					SpecOrb.SPECENERGY_TEXT,
					SpecOrb.ORB_SPECENERGY_EMPTY,
					SpecOrb.SPECENERGY_INDICATOR,
					SpecOrb.SPECENERGY_BACKING,
					SpecOrb.SPECBUTTON
				)
				.setVarTriggers(
					VarPlayer.USE_SPECIAL_ATTACK,
					VarPlayer.CURRENT_SPEC_ENERGY,
					VarPlayer.SOULREAPER_STACKS,
					VarPlayer.WEAPON_OF_SOL_STACKS,
					VarPlayer.DUEL2ACCEPT,
					VarPlayer.BR_TEMP_1,
					VarPlayer.LOC4
				)
				.setButtonId(SpecOrb.SPECBUTTON)
				.setTextId(SpecOrb.SPECENERGY_TEXT)
				.setFillId(SpecOrb.SPECENERGY_INDICATOR)
				.setEmptyIds(
					SpecOrb.ORB_SPECENERGY_EMPTY,
					SpecOrb.ORB_SPECENERGY_EMPTY_GRAPHIC0
				)
				.setIconId(SpecOrb.SPECENERGY_ICON)
		);
	}
}
