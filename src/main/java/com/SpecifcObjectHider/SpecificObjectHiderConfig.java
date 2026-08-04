package com.SpecifcObjectHider;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("specificobjecthider")
public interface SpecificObjectHiderConfig extends Config
{
	@ConfigItem(
			keyName = "showHideMenu",
			name = "Show Hide Option in Menu",
			description = "Adds 'Hide all options' to right-click menus on objects.",
			position = 1
	)
	default boolean showHideMenu()
	{
		return true;
	}

	@ConfigItem(
			keyName = "showPanel",
			name = "Enable Side Panel",
			description = "Shows or hides the plugin side menu panel in the RuneLite sidebar.",
			position = 2
	)
	default boolean showPanel()
	{
		return true;
	}
}