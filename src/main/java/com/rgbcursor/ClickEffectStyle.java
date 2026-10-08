package com.rgbcursor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ClickEffectStyle
{
	OFF("Off"),
	RIPPLE("Ripple"),
	BURST("Burst"),
	SPARKLE("Sparkle"),
	FIREWORK("Firework"),
	RANDOM("Random");

	private final String name;

	@Override
	public String toString()
	{
		return name;
	}
}
