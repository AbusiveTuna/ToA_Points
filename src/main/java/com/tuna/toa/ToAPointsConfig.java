package com.tuna.toa;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("toapointsoverlay")
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
	default boolean raidsUniqueChance()
	{
		return true;
	}

	@ConfigItem(
			keyName = "puzzlePointsAssumption",
			name = "Puzzle Points Assumption",
			description = "Gives 300 points after Scarabs puzzles, and 450 after ba-ba's puzzle",
			position = 3
	)
	default boolean puzzlePointsAssumption()
	{
		return true;
	}
	
	@ConfigItem(
			keyName = "roomPoints",
			name = "Display current room points",
			description = "For the fixed andys",
			position = 4
	)
	default boolean roomPoints()
	{
		return true;
	}
	
	@ConfigItem(
			keyName = "itemChance",
			name = "Display an overlay that shows your exact chance for each item",
			description = "For those that are obsessive about a certain item",
			position = 5
	)
	default boolean itemChance()
	{
		return true;
	}
	
	@ConfigItem(
			keyName = "alwaysShowItemChance",
			name = "Always display an overlay that shows your exact chance for each item",
			description = "For those that are obsessive about a certain item",
			position = 6
	)
	default boolean alwaysShowItemChance()
	{
		return false;
	}

}
