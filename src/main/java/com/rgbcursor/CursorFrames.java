package com.rgbcursor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

/**
 * Pre-renders one full color cycle of the arrow cursor as native {@link Cursor}s,
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

	static Cursor[] build(RgbCursorEffect effect, int size, float saturation, boolean outline)
	{
		final float stroke = outline ? Math.max(1f, size / 14f) : 0f;
		final int pad = (int) Math.ceil(stroke) + 1;

		// Ask the platform for a cursor canvas at least as big as the arrow; it may round up
		Dimension best = Toolkit.getDefaultToolkit().getBestCursorSize(size + pad * 2, size + pad * 2);
		final int w = Math.max(best.width, 1);
		final int h = Math.max(best.height, 1);
		final double scale = Math.min(size, Math.min(w, h) - pad * 2) / ARROW_HEIGHT;

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
		java.awt.Shape shape = tx.createTransformedShape(arrow);

		// Anti-aliased fill coverage, recolored per frame
		BufferedImage mask = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = mask.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.WHITE);
		g.fill(shape);
		g.dispose();

		BufferedImage border = null;
		if (outline)
		{
			border = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			g = border.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(new Color(20, 20, 20));
			g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g.draw(shape);
			g.dispose();
		}

		final int[] maskPx = mask.getRGB(0, 0, w, h, null, 0, w);
		final Point hotspot = new Point(pad, pad);
		final Toolkit toolkit = Toolkit.getDefaultToolkit();
		final Cursor[] frames = new Cursor[FRAME_COUNT];

		for (int f = 0; f < FRAME_COUNT; f++)
		{
			final float phase = (float) f / FRAME_COUNT;
			float brightness = 1f;
			if (effect == RgbCursorEffect.BREATHE)
			{
				// Two slow pulses per color cycle, never fully dark
				brightness = 0.6f + 0.4f * (float) (0.5 + 0.5 * Math.cos(phase * 4 * Math.PI));
			}

			final int solid = Color.HSBtoRGB(phase, saturation, brightness) & 0xFFFFFF;
			final int[] px = new int[w * h];
			for (int y = 0; y < h; y++)
			{
				for (int x = 0; x < w; x++)
				{
					int i = y * w + x;
					int alpha = maskPx[i] >>> 24;
					if (alpha == 0)
					{
						continue;
					}

					int rgb = solid;
					if (effect == RgbCursorEffect.WAVE)
					{
						// Hue runs diagonally across the arrow and scrolls with the phase
						float hue = phase - (float) (x + y) / (w + h);
						rgb = Color.HSBtoRGB(hue, saturation, 1f) & 0xFFFFFF;
					}
					px[i] = (alpha << 24) | rgb;
				}
			}

			BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			img.setRGB(0, 0, w, h, px, 0, w);
			if (border != null)
			{
				Graphics2D ig = img.createGraphics();
				ig.drawImage(border, 0, 0, null);
				ig.dispose();
			}

			frames[f] = toolkit.createCustomCursor(img, hotspot, "rgb-cursor-" + f);
		}

		return frames;
	}
}
