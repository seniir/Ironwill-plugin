package com.ironwill;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("ironwill")
public interface IronwillConfig extends Config
{
	@ConfigItem(
		keyName = "entries",
		name = "Acquired Items",
		description = "List of acquired items with KC and date"
	)
	default String entries()
	{
		return "";
	}

	@ConfigItem(
			keyName = "entries",
			name = "",
			description = ""
	)
	void entries(String entries);
}