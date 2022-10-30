package com.tuna.toa;

import com.tuna.toa.UniqueConfigOptions.UniqueConfigOptions;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("raidpointsoverlay")
public interface ToAPointsConfig extends Config
{

	@ConfigItem(
			keyName = "mvpAssumption",
			name = "MvP Points Assumption",
			description = "currently we don't calculate MVP, so this adds 300 points (solo mvp) per completed room.",
			position = 1
	)
	default boolean mvpAssumption()
	{
		return true;
	}
	@ConfigItem(
			keyName = "raidsUniqueChance",
			name = "Display the chance of an unique",
			description = "Displays the chance that a single unique could be in raid loot",
			position = 2
	)
	default UniqueConfigOptions raidsUniqueChance()
	{
		return UniqueConfigOptions.ON;
	}
}