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
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
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

	// Outer edge (border included) of the macOS pointer in points at its native 17pt height, tip at (0, 0)
	private static final double[] ARROW_X = {0, 0, 3.0, 4.9, 7.8, 6.1, 9.8};
	private static final double[] ARROW_Y = {0, 13.5, 11.4, 16.6, 16.0, 10.8, 10.8};
	private static final double ARROW_HEIGHT = 16.6;
	private static final double ARROW_WIDTH = 9.8;

	private static final Color SHADOW = new Color(0, 0, 0, 110);

	/**
	 * A cursor source split into a layer that gets recolored and a fixed layer drawn beneath it
	 * (the arrow's border and drop shadow), so the border keeps its color instead of turning rainbow.
	 */
	private static final class Layers
	{
		BufferedImage under;
		BufferedImage tint;
	}

	private CursorFrames()
	{
	}

	static Cursor[] build(CursorShape shape, RgbCursorEffect effect, int size, float saturation, boolean outline, BorderColor borderColor)
	{
		final Toolkit toolkit = Toolkit.getDefaultToolkit();
		final Point hotspot = new Point();
		final BufferedImage[] lo = render(shape, effect, size, saturation, outline, borderColor, 1, hotspot);
		// A 2x set lets HiDPI (Retina) screens show a sharp cursor instead of an upscaled one
		final BufferedImage[] hi = render(shape, effect, size, saturation, outline, borderColor, 2, new Point());

		final Cursor[] frames = new Cursor[FRAME_COUNT];
		for (int f = 0; f < FRAME_COUNT; f++)
		{
			// Place on a canvas of the platform's preferred cursor size so it isn't rescaled badly
			Dimension best = toolkit.getBestCursorSize(lo[f].getWidth(), lo[f].getHeight());
			int w = Math.max(best.width, lo[f].getWidth());
			int h = Math.max(best.height, lo[f].getHeight());
			BaseMultiResolutionImage img = new BaseMultiResolutionImage(onCanvas(lo[f], w, h), onCanvas(hi[f], w * 2, h * 2));
			frames[f] = toolkit.createCustomCursor(img, hotspot, "rgb-cursor-" + f);
		}
		return frames;
	}

	/**
	 * Renders the recolored frames at the given pixel scale; {@code hotspot} receives the click point in 1x pixels.
	 */
	static BufferedImage[] render(CursorShape shape, RgbCursorEffect effect, int size, float saturation, boolean outline, BorderColor borderColor, int scale, Point hotspot)
	{
		final Layers layers = shape.getResource() == null
			? renderArrow(size, outline, borderColor, scale, hotspot)
			: loadImage(shape, scale, hotspot);
		final BufferedImage source = layers.tint;
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

			BufferedImage frame = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = frame.createGraphics();
			if (layers.under != null)
			{
				g.drawImage(layers.under, 0, 0, null);
			}
			BufferedImage tinted = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			tinted.setRGB(0, 0, w, h, px, 0, w);
			g.drawImage(tinted, 0, 0, null);
			g.dispose();
			frames[f] = frame;
		}

		return frames;
	}

	private static BufferedImage onCanvas(BufferedImage img, int w, int h)
	{
		BufferedImage canvas = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = canvas.createGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();
		return canvas;
	}

	private static Layers loadImage(CursorShape shape, int scale, Point hotspot)
	{
		BufferedImage img = shape.isCore()
			? ImageUtil.loadImageResource(CustomCursorPlugin.class, shape.getResource())
			: ImageUtil.loadImageResource(CursorFrames.class, "cursors/" + shape.getResource());
		hotspot.setLocation(shape.getHotspotX(), shape.getHotspotY());
		// Nearest-neighbour upscale keeps the pixel-art sprites crisp at 2x
		BufferedImage argb = new BufferedImage(img.getWidth() * scale, img.getHeight() * scale, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = argb.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g.drawImage(img, 0, 0, argb.getWidth(), argb.getHeight(), null);
		g.dispose();

		Layers layers = new Layers();
		layers.tint = argb;
		return layers;
	}

	private static Layers renderArrow(int size, boolean outline, BorderColor borderColor, int scale, Point hotspot)
	{
		// Geometry is laid out in 1x pixels, then everything is multiplied by scale so 1x and 2x line up exactly
		final double border = outline ? size / 17.0 : 0;
		final double blur = size / 16.0;
		final double shadowDrop = size / 20.0;
		final int pad = (int) Math.ceil(blur * 2) + 1;
		final double unit = size / ARROW_HEIGHT;
		final int w1 = (int) Math.ceil(ARROW_WIDTH * unit) + pad * 2;
		final int h1 = size + pad * 2 + (int) Math.ceil(shadowDrop);
		final int w = w1 * scale;
		final int h = h1 * scale;

		Path2D.Double arrow = new Path2D.Double();
		arrow.moveTo(ARROW_X[0], ARROW_Y[0]);
		for (int i = 1; i < ARROW_X.length; i++)
		{
			arrow.lineTo(ARROW_X[i], ARROW_Y[i]);
		}
		arrow.closePath();
		AffineTransform tx = new AffineTransform();
		tx.scale(scale, scale);
		tx.translate(pad, pad);
		tx.scale(unit, unit);
		final Shape outer = tx.createTransformedShape(arrow);

		// The colored fill is the outline inset by the border width; the border is the full outline beneath it,
		// traced with a hairline round stroke to soften the corners slightly like macOS does
		Shape fill = outer;
		if (outline)
		{
			Area inset = new Area(outer);
			inset.subtract(new Area(new BasicStroke((float) (border * 2 * scale)).createStrokedShape(outer)));
			fill = inset;
		}
		final BasicStroke soften = new BasicStroke((float) (0.6 * scale), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

		Layers layers = new Layers();

		// Soft drop shadow of the whole silhouette, then the border on top of it
		final boolean shadow = !outline || borderColor.isShadow();
		Graphics2D g;
		if (shadow)
		{
			BufferedImage silhouette = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			g = antialiased(silhouette);
			g.setColor(SHADOW);
			g.translate(0, shadowDrop * scale);
			g.fill(outer);
			g.dispose();
			layers.under = blur(silhouette, (int) Math.round(blur * scale));
		}
		else
		{
			layers.under = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		}

		if (outline)
		{
			g = antialiased(layers.under);
			g.setColor(borderColor.getColor());
			g.fill(outer);
			if (borderColor.isShadow())
			{
				g.setStroke(soften);
				g.draw(outer);
			}
			g.dispose();
		}

		// The fill is drawn white so the brightness-based recolor turns it fully saturated
		layers.tint = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		g = antialiased(layers.tint);
		g.setColor(Color.WHITE);
		g.fill(fill);
		if (!outline)
		{
			g.setStroke(soften);
			g.draw(fill);
		}
		g.dispose();

		hotspot.setLocation(pad, pad);
		return layers;
	}

	private static Graphics2D antialiased(BufferedImage img)
	{
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		return g;
	}

	private static BufferedImage blur(BufferedImage img, int radius)
	{
		if (radius < 1)
		{
			return img;
		}

		int n = radius * 2 + 1;
		float[] row = new float[n];
		Arrays.fill(row, 1f / n);
		// Two separable box passes approximate a gaussian
		BufferedImage out = img;
		for (int pass = 0; pass < 2; pass++)
		{
			out = new ConvolveOp(new Kernel(n, 1, row), ConvolveOp.EDGE_NO_OP, null).filter(out, null);
			out = new ConvolveOp(new Kernel(1, n, row), ConvolveOp.EDGE_NO_OP, null).filter(out, null);
		}
		return out;
	}
}
