package com.rgbcursor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Cursor shapes. Everything except the arrow reuses the images bundled with RuneLite's Custom Cursor plugin.
 */
@Getter
@RequiredArgsConstructor
public enum CursorShape
{
	ARROW("Arrow", null),
	DRAGON_SCIMITAR("Dragon scimitar", "cursor-dragon-scimitar.png"),
	DRAGON_DAGGER("Dragon dagger", "cursor-dragon-dagger.png"),
	DRAGON_DAGGER_POISON("Dragon dagger (p)", "cursor-dragon-dagger-p.png"),
	RS3_GOLD("RS3 gold", "cursor-rs3-gold.png"),
	RS3_SILVER("RS3 silver", "cursor-rs3-silver.png"),
	TROUT("Trout", "cursor-trout.png");

	private final String name;
	private final String resource;

	@Override
	public String toString()
	{
		return name;
	}
}
