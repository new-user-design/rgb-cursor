package com.rgbcursor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import net.runelite.client.plugins.customcursor.CustomCursorPlugin;
import net.runelite.client.util.ImageUtil;

/**
 * Pre-renders one full color cycle of the cursor as native {@link Cursor}s,
 * so animating is just picking the next one instead of creating cursors every frame.
 */
final class CursorFrames
{
	static final int FRAME_COUNT = 60;

	// Classic pointer outline on a 12x19 grid, tip at (0, 0)
	private static final int[] ARROW_X = {0, 0, 4, 7, 9, 6, 11};
	private static final int[] ARROW_Y = {0, 16, 12, 18, 17, 11, 11};
	private static final double ARROW_HEIGHT = 18;

	private CursorFrames()
	{
	}

	static Cursor[] build(CursorShape shape, RgbCursorEffect effect, int size, float saturation, boolean outline)
	{
		final Toolkit toolkit = Toolkit.getDefaultToolkit();
		final Point hotspot = new Point();
		final BufferedImage[] images = render(shape, effect, size, saturation, outline, hotspot);
		final Cursor[] frames = new Cursor[images.length];
		for (int f = 0; f < images.length; f++)
		{
			BufferedImage img = images[f];
			// Place on a canvas of the platform's preferred cursor size so it isn't rescaled badly
			Dimension best = toolkit.getBestCursorSize(img.getWidth(), img.getHeight());
			BufferedImage canvas = new BufferedImage(Math.max(best.width, img.getWidth()), Math.max(best.height, img.getHeight()), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = canvas.createGraphics();
			g.drawImage(img, 0, 0, null);
			g.dispose();
			frames[f] = toolkit.createCustomCursor(canvas, hotspot, "rgb-cursor-" + f);
		}
		return frames;
	}

	/**
	 * Renders the recolored frames; {@code hotspot} receives the click point within each image.
	 */
	static BufferedImage[] render(CursorShape shape, RgbCursorEffect effect, int size, float saturation, boolean outline, Point hotspot)
	{
		final BufferedImage source = shape.getResource() == null
			? renderArrow(size, outline, hotspot)
			: loadImage(shape.getResource());
		final int w = source.getWidth();
		final int h = source.getHeight();
		final int[] srcPx = source.getRGB(0, 0, w, h, null, 0, w);

		// Recolor by brightness so shading and dark outlines survive. Scale against a high percentile rather than
		// the single brightest pixel, otherwise one highlight leaves the rest of a sprite (e.g. the scimitar) dim.
		final float[] value = new float[srcPx.length];
		final float[] opaque = new float[srcPx.length];
		int opaqueCount = 0;
		for (int i = 0; i < srcPx.length; i++)
		{
			int p = srcPx[i];
			value[i] = (0.299f * ((p >> 16) & 0xFF) + 0.587f * ((p >> 8) & 0xFF) + 0.114f * (p & 0xFF)) / 255f;
			if ((p >>> 24) > 0)
			{
				opaque[opaqueCount++] = value[i];
			}
		}
		Arrays.sort(opaque, 0, opaqueCount);
		final float ref = opaqueCount > 0 ? opaque[(int) (opaqueCount * 0.9f)] : 0f;
		if (ref > 0f)
		{
			for (int i = 0; i < value.length; i++)
			{
				value[i] = (float) Math.pow(Math.min(1f, value[i] / ref), 0.75);
			}
		}

		final BufferedImage[] frames = new BufferedImage[FRAME_COUNT];

		for (int f = 0; f < FRAME_COUNT; f++)
		{
			final float phase = (float) f / FRAME_COUNT;
			float brightness = 1f;
			if (effect == RgbCursorEffect.BREATHE)
			{
				// Two slow pulses per color cycle, never fully dark
				brightness = 0.6f + 0.4f * (float) (0.5 + 0.5 * Math.cos(phase * 4 * Math.PI));
			}

			final int[] px = new int[w * h];
			for (int y = 0; y < h; y++)
			{
				for (int x = 0; x < w; x++)
				{
					int i = y * w + x;
					int alpha = srcPx[i] >>> 24;
					if (alpha == 0)
					{
						continue;
					}

					// Hue runs diagonally across the cursor and scrolls with the phase in wave mode
					float hue = effect == RgbCursorEffect.WAVE ? phase - (float) (x + y) / (w + h) : phase;
					int rgb = Color.HSBtoRGB(hue, saturation, value[i] * brightness) & 0xFFFFFF;
					px[i] = (alpha << 24) | rgb;
				}
			}

			frames[f] = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			frames[f].setRGB(0, 0, w, h, px, 0, w);
		}

		return frames;
	}

	private static BufferedImage loadImage(String resource)
	{
		BufferedImage img = ImageUtil.loadImageResource(CustomCursorPlugin.class, resource);
		BufferedImage argb = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = argb.createGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();
		return argb;
	}

	private static BufferedImage renderArrow(int size, boolean outline, Point hotspot)
	{
		final float stroke = outline ? Math.max(1f, size / 14f) : 0f;
		final int pad = (int) Math.ceil(stroke) + 1;
		final double scale = size / ARROW_HEIGHT;
		final int w = (int) Math.ceil(12 * scale) + pad * 2;
		final int h = size + pad * 2;

		Path2D.Double arrow = new Path2D.Double();
		arrow.moveTo(ARROW_X[0], ARROW_Y[0]);
		for (int i = 1; i < ARROW_X.length; i++)
		{
			arrow.lineTo(ARROW_X[i], ARROW_Y[i]);
		}
		arrow.closePath();
		AffineTransform tx = new AffineTransform();
		tx.translate(pad, pad);
		tx.scale(scale, scale);
		Shape shape = tx.createTransformedShape(arrow);

		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.WHITE);
		g.fill(shape);
		if (outline)
		{
			g.setColor(new Color(20, 20, 20));
			g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g.draw(shape);
		}
		g.dispose();

		hotspot.setLocation(pad, pad);
		return img;
	}
}
