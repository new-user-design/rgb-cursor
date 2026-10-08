package com.rgbcursor;

import java.awt.Color;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.SpritePixels;

/**
 * Recolors the game's own click crosses (the yellow walk / red interact X) in place, keeping a copy of the
 * original pixels so they can be put back exactly. Must only be used from the client thread.
 */
@Singleton
class ClickCrossRecolor
{
	private final Client client;
	private final RgbCursorPlugin plugin;
	private final RgbCursorConfig config;

	// The sprite arrays we last recolored and their untouched pixels
	private int[][] recolored;
	private int[][] originals;
	private long lastKey = Long.MIN_VALUE;

	@Inject
	ClickCrossRecolor(Client client, RgbCursorPlugin plugin, RgbCursorConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
	}

	void update()
	{
		final CrossStyle style = config.crossStyle();
		if (style == CrossStyle.OFF)
		{
			restore();
			return;
		}

		final SpritePixels[] sprites = client.getCrossSprites();
		if (sprites == null || sprites.length == 0)
		{
			return;
		}

		if (!sameArrays(sprites))
		{
			// First run, or the game reloaded its sprites: remember the fresh originals
			restore();
			recolored = new int[sprites.length][];
			originals = new int[sprites.length][];
			for (int i = 0; i < sprites.length; i++)
			{
				int[] px = sprites[i] == null ? null : sprites[i].getPixels();
				recolored[i] = px;
				originals[i] = px == null ? null : px.clone();
			}
			lastKey = Long.MIN_VALUE;
		}

		final float hue = plugin.getHue(System.nanoTime());
		// Only redo the pixels when the visible color actually changes
		final long key = style == CrossStyle.RAINBOW
			? (long) (hue * CursorFrames.FRAME_COUNT) * 1000 + config.saturation()
			: -1 - ((((long) config.walkCrossColor().getRGB()) << 32) ^ (config.interactCrossColor().getRGB() & 0xFFFFFFFFL));
		if (key == lastKey)
		{
			return;
		}
		lastKey = key;

		final float saturation = config.saturation() / 100f;
		// The first half of the crosses are the yellow (walk) frames, the second half the red (interact) frames
		final int half = sprites.length / 2;
		for (int i = 0; i < sprites.length; i++)
		{
			if (recolored[i] == null)
			{
				continue;
			}

			final boolean interact = i >= half;
			final float[] target;
			if (style == CrossStyle.RAINBOW)
			{
				// Half a turn apart so walk and interact clicks never share a color
				target = new float[]{hue + (interact ? 0.5f : 0f), saturation, 1f};
			}
			else
			{
				Color c = interact ? config.interactCrossColor() : config.walkCrossColor();
				target = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
			}
			recolor(originals[i], recolored[i], target);
		}
	}

	void restore()
	{
		if (recolored != null)
		{
			for (int i = 0; i < recolored.length; i++)
			{
				if (recolored[i] != null && originals[i] != null && recolored[i].length == originals[i].length)
				{
					System.arraycopy(originals[i], 0, recolored[i], 0, originals[i].length);
				}
			}
		}
		recolored = null;
		originals = null;
		lastKey = Long.MIN_VALUE;
	}

	private boolean sameArrays(SpritePixels[] sprites)
	{
		if (recolored == null || recolored.length != sprites.length)
		{
			return false;
		}
		for (int i = 0; i < sprites.length; i++)
		{
			if ((sprites[i] == null ? null : sprites[i].getPixels()) != recolored[i])
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Applies the target hue and saturation while keeping each pixel's shading from the original sprite.
	 */
	private static void recolor(int[] src, int[] dst, float[] hsb)
	{
		int max = 1;
		for (int p : src)
		{
			max = Math.max(max, luminance(p));
		}

		for (int i = 0; i < src.length; i++)
		{
			int p = src[i];
			if (p == 0)
			{
				// 0 is transparent in game sprites
				dst[i] = 0;
				continue;
			}

			float value = Math.min(1f, (float) luminance(p) / max) * hsb[2];
			int rgb = Color.HSBtoRGB(hsb[0], hsb[1], value) & 0xFFFFFF;
			// Pure black would read as transparent, so nudge it
			dst[i] = rgb == 0 ? 0x010101 : rgb;
		}
	}

	private static int luminance(int p)
	{
		return (299 * ((p >> 16) & 0xFF) + 587 * ((p >> 8) & 0xFF) + 114 * (p & 0xFF)) / 1000;
	}
}
