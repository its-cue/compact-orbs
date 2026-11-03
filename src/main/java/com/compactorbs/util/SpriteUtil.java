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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import net.runelite.api.SpritePixels;

public final class SpriteUtil
{
	public static BufferedImage getSpriteImage(SpritePixels sprite)
	{
		if (sprite == null || sprite.getMaxWidth() <= 0 || sprite.getMaxHeight() <= 0)
		{
			return null;
		}

		BufferedImage image = new BufferedImage(sprite.getMaxWidth(), sprite.getMaxHeight(), BufferedImage.TYPE_INT_ARGB);

		Graphics2D graphics = image.createGraphics();
		try
		{
			graphics.drawImage(sprite.toBufferedImage(), sprite.getOffsetX(), sprite.getOffsetY(), null);
		}
		finally
		{
			graphics.dispose();
		}

		return image;
	}

	public static BufferedImage recolorImage(BufferedImage image, Color newColor)
	{
		float[] target = Color.RGBtoHSB(newColor.getRed(), newColor.getGreen(), newColor.getBlue(), null);

		float solidAmount = 1f - (newColor.getAlpha() / 255f);
		int solid = newColor.getRGB() & 0xFFFFFF;

		int width = image.getWidth();
		int height = image.getHeight();

		int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
		float[] original = new float[3];

		for (int i = 0; i < pixels.length; i++)
		{
			int argb = pixels[i];
			int alpha = (argb >>> 24) & 0xFF;

			if (alpha == 0)
			{
				continue;
			}

			Color.RGBtoHSB((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, original);

			int tinted = Color.HSBtoRGB(target[0], target[1], original[2]) & 0xFFFFFF;

			pixels[i] = (alpha << 24) | blend(tinted, solid, solidAmount);
		}

		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		result.setRGB(0, 0, width, height, pixels, 0, width);

		return result;
	}

	private static int blend(int from, int to, float t)
	{
		if (t <= 0f)
		{
			return from;
		}

		int r = Math.round(((from >> 16) & 0xFF) * (1 - t) + ((to >> 16) & 0xFF) * t);
		int g = Math.round(((from >> 8) & 0xFF) * (1 - t) + ((to >> 8) & 0xFF) * t);
		int b = Math.round((from & 0xFF) * (1 - t) + (to & 0xFF) * t);

		return (r << 16) | (g << 8) | b;
	}
}
