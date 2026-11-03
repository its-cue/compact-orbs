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

package com.compactorbs.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;

public final class OrbUtil
{
	//from cs2 [proc,rgb_to_hex] // 246
	private static int rgbToHex(int r, int g, int b)
	{
		r = Math.max(0, Math.min(0xff, r));
		g = Math.max(0, Math.min(0xff, g));
		b = Math.max(0, Math.min(0xff, b));
		return (r << 16) | (g << 8) | b;
	}

	//from cs2 [proc,orbs_update] // 449
	public static int calcColor(int current, int max, boolean rgb)
	{
		if (max <= 0)
		{
			max = 1;
		}

		if (current < 0)
		{
			current = 0;
		}

		int half = max / 2;

		if (half == 0)
		{
			return current >= max ? rgbToHex(0, 255, 0) : rgbToHex(255, 0, 0);
		}

		int r = current > half ? 255 - 255 * (current - half) / half : 255;
		int g = current > half ? 255 : current * 255 / half;

		return rgb ? ((r << 16) | (g << 8)) : rgbToHex(r, g, 0);
	}

	//calc height in cc_setsize, from [proc,orbs_update] // 449
	public static int calcEmptyHeight(int input, int current, int max)
	{
		if (max <= 0)
		{
			max = 1;
		}

		if (current <= 0)
		{
			current = 0;
		}

		if (current > max)
		{
			current = max;
		}

		return input * (max - current) / max;
	}

	public static void drawRegenArc(Graphics2D g, double x, double y, double diameter, double percent, Color color)
	{
		Graphics2D arc = (Graphics2D) g.create();
		try
		{
			arc.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
			final Stroke STROKE = new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
			arc.setStroke(STROKE);
			arc.setColor(color);
			arc.draw(new Arc2D.Double(x, y, diameter, diameter, 90d, -360d * percent, Arc2D.OPEN));
		}
		finally
		{
			arc.dispose();
		}
	}

	public static void drawEmpty(Graphics2D g, BufferedImage empty, int x, int y, int size, int energy, int max)
	{
		Graphics2D clipped = (Graphics2D) g.create();
		try
		{
			clipped.clipRect(x, y, size, calcEmptyHeight(size, energy, max));
			clipped.drawImage(empty, x, y, size, size, null);
		}
		finally
		{
			clipped.dispose();
		}
	}

	public static Color brighter(int color)
	{
		float[] hsv = new float[3];
		Color.RGBtoHSB(color >>> 16, (color >> 8) & 0xFF, color & 0xFF, hsv);
		return Color.getHSBColor(hsv[0], 1.f, 1.f);
	}
}
