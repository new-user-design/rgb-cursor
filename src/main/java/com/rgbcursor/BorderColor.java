package com.rgbcursor;

import java.awt.Color;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BorderColor
{
	WHITE("White", Color.WHITE, true),
	// A drop shadow and softened corners blend into a black border and make it read thicker than the white one,
	// so the black border goes without them to look the same weight
	BLACK("Black", new Color(10, 10, 10), false);

	private final String name;
	private final Color color;
	private final boolean shadow;

	@Override
	public String toString()
	{
		return name;
	}
}
