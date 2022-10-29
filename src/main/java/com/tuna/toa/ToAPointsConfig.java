package com.tuna.toa;

import com.tuna.toa.UniqueConfigOptions.UniqueConfigOptions;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("raidpointsoverlay")
public interface ToAPointsConfig extends Config
{
	@ConfigItem(
			keyName = "raidsUniqueChance",
			name = "Display the chance of an unique",
			description = "Displays the chance that a single unique could be in raid loot"
	)
	default UniqueConfigOptions raidsUniqueChance()
	{
		return UniqueConfigOptions.BOTH;
	}
}