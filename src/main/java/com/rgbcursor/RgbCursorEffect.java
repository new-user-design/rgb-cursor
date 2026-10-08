package com.rgbcursor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RgbCursorEffect
{
	NONE("None (static)"),
	RAINBOW("Rainbow cycle"),
	WAVE("Gradient wave"),
	BREATHE("Breathing rainbow");

	private final String name;

	@Override
	public String toString()
	{
		return name;
	}
}
