package com.rgbcursor;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup(RgbCursorConfig.GROUP)
public interface RgbCursorConfig extends Config
{
	String GROUP = "rgbcursor";

	@ConfigSection(
		name = "Mouse trail",
		description = "Rainbow trail that follows the mouse",
		position = 10
	)
	String trailSection = "trail";

	@ConfigItem(
		keyName = "shape",
		name = "Shape",
		description = "Which cursor to color; image shapes are recolored by their brightness",
		position = 0
	)
	default CursorShape shape()
	{
		return CursorShape.ARROW;
	}

	@ConfigItem(
		keyName = "effect",
		name = "Effect",
		description = "How the cursor colors animate",
		position = 1
	)
	default RgbCursorEffect effect()
	{
		return RgbCursorEffect.RAINBOW;
	}

	@Range(min = 1, max = 30)
	@Units(Units.SECONDS)
	@ConfigItem(
		keyName = "cycleSeconds",
		name = "Cycle time",
		description = "Seconds for one full trip around the color wheel",
		position = 2
	)
	default int cycleSeconds()
	{
		return 3;
	}

	@Range(min = 0, max = 100)
	@Units(Units.PERCENT)
	@ConfigItem(
		keyName = "saturation",
		name = "Saturation",
		description = "Color intensity; 0% is plain white",
		position = 3
	)
	default int saturation()
	{
		return 100;
	}

	@Range(min = 12, max = 48)
	@Units(Units.PIXELS)
	@ConfigItem(
		keyName = "arrowSize",
		name = "Arrow size",
		description = "Height of the arrow; 17 matches the default macOS pointer (image shapes keep their own size)",
		position = 4
	)
	default int arrowSize()
	{
		return 17;
	}

	@ConfigItem(
		keyName = "outline",
		name = "Black border",
		description = "Give the arrow a thin black border so it stays visible on any background",
		position = 5
	)
	default boolean outline()
	{
		return true;
	}

	@ConfigItem(
		keyName = "cursorEnabled",
		name = "Color cursor",
		description = "Replace the mouse pointer with an animated RGB arrow",
		position = -1
	)
	default boolean cursorEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "trailEnabled",
		name = "Enable trail",
		description = "Draw a rainbow trail behind the mouse",
		position = 0,
		section = trailSection
	)
	default boolean trailEnabled()
	{
		return true;
	}

	@Range(min = 50, max = 2000)
	@Units(Units.MILLISECONDS)
	@ConfigItem(
		keyName = "trailDuration",
		name = "Trail length",
		description = "How long each part of the trail lingers before fading out",
		position = 1,
		section = trailSection
	)
	default int trailDuration()
	{
		return 350;
	}

	@Range(min = 1, max = 20)
	@Units(Units.PIXELS)
	@ConfigItem(
		keyName = "trailWidth",
		name = "Trail width",
		description = "Thickness of the trail at the cursor end",
		position = 2,
		section = trailSection
	)
	default int trailWidth()
	{
		return 5;
	}

	@Range(min = 0, max = 100)
	@Units(Units.PERCENT)
	@ConfigItem(
		keyName = "trailRainbowSpread",
		name = "Rainbow spread",
		description = "How much of the color wheel the trail spans from head to tail",
		position = 3,
		section = trailSection
	)
	default int trailRainbowSpread()
	{
		return 60;
	}
}
