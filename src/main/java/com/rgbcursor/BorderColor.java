package com.rgbcursor;

import java.awt.Color;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BorderColor
{
	WHITE("White", Color.WHITE),
	BLACK("Black", new Color(10, 10, 10));

	private final String name;
	private final Color color;

	@Override
	public String toString()
	{
		return name;
	}
}
