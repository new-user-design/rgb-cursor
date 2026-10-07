package com.rgbcursor;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RgbCursorPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(RgbCursorPlugin.class);
		RuneLite.main(args);
	}
}
