package com.rgbcursor;

import java.awt.Color;
import net.runelite.client.config.Alpha;
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
		description = "How the cursor colors animate; None keeps image shapes in their original colors and draws the arrow in the static color",
		position = 1
	)
	default RgbCursorEffect effect()
	{
		return RgbCursorEffect.RAINBOW;
	}

	@ConfigItem(
		keyName = "staticColor",
		name = "Static arrow color",
		description = "Arrow fill color when the effect is None",
		position = 2
	)
	default Color staticColor()
	{
		return Color.BLACK;
	}

	@Range(min = 1, max = 30)
	@Units(Units.SECONDS)
	@ConfigItem(
		keyName = "cycleSeconds",
		name = "Cycle time",
		description = "Seconds for one full trip around the color wheel",
		position = 3
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
		position = 4
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
		position = 5
	)
	default int arrowSize()
	{
		return 17;
	}

	@ConfigItem(
		keyName = "outline",
		name = "Border",
		description = "Give the arrow a thin border like the native macOS pointer",
		position = 6
	)
	default boolean outline()
	{
		return true;
	}

	@ConfigItem(
		keyName = "borderColor",
		name = "Border color",
		description = "Color of the arrow's border",
		position = 7
	)
	default BorderColor borderColor()
	{
		return BorderColor.WHITE;
	}

	@ConfigItem(
		keyName = "cursorEnabled",
		name = "Custom cursor",
		description = "Use this plugin's cursor. Turn off to keep your normal system cursor (the trail still works)",
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
		keyName = "trailRainbow",
		name = "Rainbow trail",
		description = "Color the trail with the rainbow; turn off to use the trail color instead",
		position = 1,
		section = trailSection
	)
	default boolean trailRainbow()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "trailColor",
		name = "Trail color",
		description = "Trail color when Rainbow trail is off",
		position = 2,
		section = trailSection
	)
	default Color trailColor()
	{
		return Color.WHITE;
	}

	@ConfigItem(
		keyName = "trailDuration",
		name = "Trail length",
		description = "How long each part of the trail lingers before fading out",
		position = 3,
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
		position = 4,
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
		position = 5,
		section = trailSection
	)
	default int trailRainbowSpread()
	{
		return 60;
	}
}
