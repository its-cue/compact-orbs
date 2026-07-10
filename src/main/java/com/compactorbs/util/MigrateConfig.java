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

import static com.compactorbs.CompactOrbsConstants.ConfigGroup.GROUP_NAME;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

@Slf4j
public class MigrateConfig<T, R>
{
	private final String oldKey;
	private final String newKey;
	private final Class<T> type;
	private final Function<T, R> transform;

	public MigrateConfig(String oldKey, String newKey, Class<T> type, Function<T, R> transform)
	{
		this.oldKey = oldKey;
		this.newKey = newKey;
		this.type = type;
		this.transform = transform;
	}

	public boolean write(ConfigManager configManager)
	{
		T value = configManager.getConfiguration(GROUP_NAME, oldKey, type);
		if (value == null)
		{
			return false;
		}

		R result = transform.apply(value);
		if (result != null)
		{
			log.debug("Migrating config key: {}.{}={} -> {}={}", GROUP_NAME, oldKey, value, newKey, result);
			configManager.setConfiguration(GROUP_NAME, newKey, result);
		}
		return true;
	}

	public void unset(ConfigManager configManager)
	{
		log.debug("Removing old config key: {}.{}", GROUP_NAME, oldKey);
		configManager.unsetConfiguration(GROUP_NAME, oldKey);
	}
}

