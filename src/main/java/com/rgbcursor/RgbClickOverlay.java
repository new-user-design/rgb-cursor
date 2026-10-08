package com.rgbcursor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.ArrayDeque;
import java.util.Random;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Plays a short colored effect wherever the mouse is clicked. Clicks are picked up from the client's own
 * last-press timestamp, so no extra input listener is needed and everything stays on the client thread.
 */
class RgbClickOverlay extends Overlay
{
	private static final int MAX_EFFECTS = 24;
	private static final ClickEffectStyle[] RANDOM_STYLES = {
		ClickEffectStyle.RIPPLE, ClickEffectStyle.BURST, ClickEffectStyle.SPARKLE, ClickEffectStyle.FIREWORK
	};

	private static final class Effect
	{
		final int x;
		final int y;
		final long start;
		final ClickEffectStyle style;
		final long seed;

		Effect(int x, int y, long start, ClickEffectStyle style, long seed)
		{
			this.x = x;
			this.y = y;
			this.start = start;
			this.style = style;
			this.seed = seed;
		}
	}

	private final Client client;
	private final RgbCursorPlugin plugin;
	private final RgbCursorConfig config;
	private final ArrayDeque<Effect> effects = new ArrayDeque<>();
	private final Random random = new Random();
	private long lastPress = -1;

	@Inject
	RgbClickOverlay(Client client, RgbCursorPlugin plugin, RgbCursorConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ALWAYS_ON_TOP);
		setPriority(Overlay.PRIORITY_HIGHEST);
	}

	@Override
	public Dimension render(Graphics2D g)
	{
		final ClickEffectStyle setting = config.clickEffect();
		final long press = client.getMouseLastPressedMillis();
		if (setting == ClickEffectStyle.OFF)
		{
			effects.clear();
			lastPress = press;
			return null;
		}

		final long now = System.nanoTime();
		// The first frame only records the current press, so enabling the effect doesn't replay an old click
		if (lastPress != -1 && press != lastPress)
		{
			Point mouse = client.getMouseCanvasPosition();
			if (mouse != null && mouse.getX() >= 0 && mouse.getY() >= 0)
			{
				ClickEffectStyle style = setting == ClickEffectStyle.RANDOM
					? RANDOM_STYLES[random.nextInt(RANDOM_STYLES.length)]
					: setting;
				effects.addLast(new Effect(mouse.getX(), mouse.getY(), now, style, random.nextLong()));
				while (effects.size() > MAX_EFFECTS)
				{
					effects.removeFirst();
				}
			}
		}
		lastPress = press;

		final long lifetime = config.clickDuration() * 1_000_000L;
		while (!effects.isEmpty() && now - effects.peekFirst().start > lifetime)
		{
			effects.removeFirst();
		}
		if (effects.isEmpty())
		{
			return null;
		}

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		final float hue = plugin.getHue(now);
		for (Effect e : effects)
		{
			float t = Math.min(1f, (float) (now - e.start) / lifetime);
			draw(g, e, t, hue);
		}
		return null;
	}

	private void draw(Graphics2D g, Effect e, float t, float hue)
	{
		final float size = config.clickSize();
		final float ease = 1f - (1f - t) * (1f - t) * (1f - t);
		final float fade = 1f - t;
		final Random r = new Random(e.seed);

		switch (e.style)
		{
			case RIPPLE:
				// Two rings chasing each other outwards
				for (int ring = 0; ring < 2; ring++)
				{
					float rt = Math.max(0f, (t - ring * 0.25f) / (1f - ring * 0.25f));
					if (rt <= 0f)
					{
						continue;
					}
					float re = 1f - (1f - rt) * (1f - rt) * (1f - rt);
					float radius = size * re;
					g.setStroke(new BasicStroke(Math.max(1f, 3f * (1f - rt))));
					g.setColor(color(hue + ring * 0.15f, 1f - rt));
					g.draw(new Ellipse2D.Float(e.x - radius, e.y - radius, radius * 2, radius * 2));
				}
				break;
			case BURST:
			{
				// Rays shooting out from the click
				int rays = 12;
				float phase = r.nextFloat();
				g.setStroke(new BasicStroke(Math.max(1f, 2.5f * fade), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				for (int i = 0; i < rays; i++)
				{
					double angle = Math.PI * 2 * (i + phase) / rays;
					// Long rays that race outwards and shorten as they go
					float inner = size * (0.1f + 0.8f * ease);
					float outer = size * (0.5f + 0.5f * ease);
					g.setColor(color(hue + (float) i / rays, fade));
					g.draw(new Line2D.Double(
						e.x + Math.cos(angle) * inner, e.y + Math.sin(angle) * inner,
						e.x + Math.cos(angle) * outer, e.y + Math.sin(angle) * outer));
				}
				break;
			}
			case SPARKLE:
			{
				// Twinkling four-point stars scattered around the click
				int stars = 8;
				for (int i = 0; i < stars; i++)
				{
					double angle = r.nextDouble() * Math.PI * 2;
					double dist = size * (0.2 + 0.8 * r.nextDouble()) * (0.6 + 0.4 * ease);
					float delay = r.nextFloat() * 0.35f;
					float st = Math.max(0f, Math.min(1f, (t - delay) / (1f - delay)));
					float scale = (float) Math.sin(Math.PI * st) * (3f + 4f * r.nextFloat());
					if (scale <= 0.2f)
					{
						continue;
					}
					g.setColor(color(hue + (float) i / stars, 1f));
					g.fill(star(e.x + Math.cos(angle) * dist, e.y + Math.sin(angle) * dist, scale));
				}
				break;
			}
			case FIREWORK:
			{
				// A bright flash, then glowing sparks flung outwards that slow, fall and leave sagging trails
				if (t < 0.25f)
				{
					float flash = size * 0.35f * (1f - t / 0.25f);
					g.setColor(color(hue, 1f - t / 0.25f));
					g.fill(new Ellipse2D.Double(e.x - flash, e.y - flash, flash * 2, flash * 2));
				}
				int particles = 12;
				float glow = 1f - t * t;
				for (int i = 0; i < particles; i++)
				{
					double angle = Math.PI * 2 * i / particles + r.nextDouble() * 0.4;
					double speed = size * (0.9 + 0.5 * r.nextDouble());
					double gravity = size * 0.9;
					Color c = color(hue + (float) i / particles, glow);
					g.setColor(c);
					g.setStroke(new BasicStroke(Math.max(1f, 1.5f * glow), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
					// Trail made of the particle's recent positions so it curves with the fall
					Path2D.Double trail = new Path2D.Double();
					for (int k = 0; k <= 4; k++)
					{
						double tk = Math.max(0, t - k * 0.06);
						double ek = 1 - (1 - tk) * (1 - tk) * (1 - tk);
						double x = e.x + Math.cos(angle) * speed * ek;
						double y = e.y + Math.sin(angle) * speed * ek + gravity * tk * tk;
						if (k == 0)
						{
							trail.moveTo(x, y);
						}
						else
						{
							trail.lineTo(x, y);
						}
					}
					g.draw(trail);
					double px = e.x + Math.cos(angle) * speed * ease;
					double py = e.y + Math.sin(angle) * speed * ease + gravity * t * t;
					// Sparks twinkle as they burn out
					float dot = (float) (2.5 + 2.0 * glow * (0.6 + 0.4 * Math.sin(t * 30 + i)));
					g.fill(new Ellipse2D.Double(px - dot / 2, py - dot / 2, dot, dot));
				}
				break;
			}
			default:
				break;
		}
	}

	private Color color(float hue, float alpha)
	{
		int a = Math.max(0, Math.min(255, Math.round(255 * alpha)));
		if (config.clickRainbow())
		{
			int rgb = Color.HSBtoRGB(hue, config.saturation() / 100f, 1f) & 0xFFFFFF;
			return new Color((a << 24) | rgb, true);
		}
		Color c = config.clickColor();
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), a * c.getAlpha() / 255);
	}

	private static Path2D star(double cx, double cy, double r)
	{
		Path2D.Double p = new Path2D.Double();
		double inner = r * 0.3;
		for (int i = 0; i < 8; i++)
		{
			double angle = Math.PI / 4 * i - Math.PI / 2;
			double radius = i % 2 == 0 ? r : inner;
			double x = cx + Math.cos(angle) * radius;
			double y = cy + Math.sin(angle) * radius;
			if (i == 0)
			{
				p.moveTo(x, y);
			}
			else
			{
				p.lineTo(x, y);
			}
		}
		p.closePath();
		return p;
	}
}
