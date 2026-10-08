package com.rgbcursor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CrossStyle
{
	OFF("Off (game default)"),
	RAINBOW("Rainbow"),
	CUSTOM("Custom colors");

	private final String name;

	@Override
	public String toString()
	{
		return name;
	}
}
