/*
 * Copyright (c) 2025, cue <https://github.com/its-cue>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
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
import com.compactorbs.CompactOrbsConstants.RegenMeter;
import java.awt.Color;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class SpecColorRegistry
{
	@Inject
	private CompactOrbsConfig config;

	private final Map<SpecOrbType, SpecOrbColors> byType = new EnumMap<>(SpecOrbType.class);

	@Inject
	private void init()
	{
		register(
			SpecOrbType.SPEC,
			config::specQoLOrbColor,
			config::specQoLOrbEmptyColor,
			() -> config.specQoLOrbRegenColor() != null
				? config.specQoLOrbRegenColor()
				: RegenMeter.SPECIAL_COLOR
		);

		register(
			SpecOrbType.SRA,
			config::specQoLSraOrbColor,
			config::specQoLSraOrbEmptyColor,
			config::specQoLSraOrbRegenColor
		);

		register(
			SpecOrbType.RONDACHE,
			config::specQoLRondacheOrbColor,
			config::specQoLRondacheOrbEmptyColor,
			config::specQoLRondacheOrbRegenColor
		);
	}

	private void register(
		SpecOrbType key,
		Supplier<Color> color,
		Supplier<Color> emptyColor,
		Supplier<Color> regenColor)
	{
		SpecOrbColors specOrbColors = new SpecOrbColors(
			color,
			emptyColor,
			regenColor
		);

		byType.put(key, specOrbColors);
	}

	public Color color(SpecOrbType type)
	{
		return byType.get(type).getColor().get();
	}

	public Color emptyColor(SpecOrbType type)
	{
		return byType.get(type).getEmptyColor().get();
	}

	public Color regenColor(SpecOrbType type)
	{
		return byType.get(type).getRegenColor().get();
	}

	public void clear()
	{
		byType.clear();
	}
}
