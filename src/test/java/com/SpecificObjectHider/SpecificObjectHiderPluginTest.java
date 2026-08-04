package com.SpecificObjectHider;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class SpecificObjectHiderPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(SpecificObjectHiderPlugin.class);
		RuneLite.main(args);
	}
}
