/*
 * Copyright (c) 2025, cue <https://github.com/its-cue>
 * Copyright (c) 2025, ldavid432 <https://github.com/ldavid432/gauntlet-loot-popup>
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

import com.compactorbs.util.SpriteUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.AbstractMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.SpritePixels;

@Singleton
public class SpriteCache
{
	@Inject
	private Client client;

	private static final int MAX_IMAGES = 16;
	private static final int MAX_RECOLORED = 16;

	//adapted from gauntlet-loot-popup
	private final Cache<Integer, BufferedImage> imageCache = CacheBuilder.newBuilder()
		.maximumSize(MAX_IMAGES)
		.build();

	private final Cache<Map.Entry<Integer, Integer>, BufferedImage> recoloredCache = CacheBuilder.newBuilder()
		.maximumSize(MAX_RECOLORED)
		.build();

	public void clear()
	{
		imageCache.invalidateAll();
		recoloredCache.invalidateAll();
	}

	public BufferedImage getImage(int id)
	{
		BufferedImage image = imageCache.getIfPresent(id);
		if (image != null)
		{
			return image;
		}

		image = loadSprite(id);
		if (image != null)
		{
			imageCache.put(id, image);
		}

		return image;
	}

	public BufferedImage getRecoloredImage(int id, Color color)
	{
		BufferedImage base = getImage(id);
		if (base == null || color == null)
		{
			return base;
		}

		Map.Entry<Integer, Integer> key = new AbstractMap.SimpleImmutableEntry<>(id, color.getRGB());
		BufferedImage recolored = recoloredCache.getIfPresent(key);
		if (recolored == null)
		{
			recolored = SpriteUtil.recolorImage(base, color);
			recoloredCache.put(key, recolored);
		}

		return recolored;
	}

	private BufferedImage loadSprite(int id)
	{
		SpritePixels sprite = client.getSpriteOverrides().get(id);
		if (sprite == null)
		{
			SpritePixels[] sprites = client.getSprites(client.getIndexSprites(), id, 0);
			if (sprites == null || sprites.length == 0)
			{
				return null;
			}

			sprite = sprites[0];
		}

		return SpriteUtil.getSpriteImage(sprite);
	}
}
