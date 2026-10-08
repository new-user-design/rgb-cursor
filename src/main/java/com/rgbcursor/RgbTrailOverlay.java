package com.rgbcursor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayDeque;
import java.util.Iterator;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

class RgbTrailOverlay extends Overlay
{
	private static final class TrailPoint
	{
		final int x;
		final int y;
		final long time;

		TrailPoint(int x, int y, long time)
		{
			this.x = x;
			this.y = y;
			this.time = time;
		}
	}

	private final Client client;
	private final RgbCursorPlugin plugin;
	private final RgbCursorConfig config;
	private final ArrayDeque<TrailPoint> points = new ArrayDeque<>();

	@Inject
	RgbTrailOverlay(Client client, RgbCursorPlugin plugin, RgbCursorConfig config)
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
		if (!config.trailEnabled())
		{
			points.clear();
			return null;
		}

		final long now = System.nanoTime();
		final long lifetime = config.trailDuration() * 1_000_000L;

		final Point mouse = client.getMouseCanvasPosition();
		if (mouse != null && mouse.getX() >= 0 && mouse.getY() >= 0)
		{
			TrailPoint last = points.peekLast();
			if (last == null || last.x != mouse.getX() || last.y != mouse.getY())
			{
				points.addLast(new TrailPoint(mouse.getX(), mouse.getY(), now));
			}
		}

		while (!points.isEmpty() && now - points.peekFirst().time > lifetime)
		{
			points.removeFirst();
		}

		if (points.size() < 2)
		{
			return null;
		}

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final float headHue = plugin.getHue(now);
		final float spread = config.trailRainbowSpread() / 100f;
		final float saturation = config.saturation() / 100f;
		final int width = config.trailWidth();
		final boolean rainbow = config.trailRainbow();
		final Color solid = config.trailColor();
		final int baseAlpha = rainbow ? 255 : solid.getAlpha();

		Iterator<TrailPoint> it = points.iterator();
		TrailPoint prev = it.next();
		while (it.hasNext())
		{
			TrailPoint cur = it.next();
			// 0 at the cursor, 1 where the trail has fully faded
			float age = Math.min(1f, (float) (now - cur.time) / lifetime);
			float life = 1f - age;

			int rgb = rainbow ? Color.HSBtoRGB(headHue - spread * age, saturation, 1f) & 0xFFFFFF : solid.getRGB() & 0xFFFFFF;
			int alpha = Math.round(baseAlpha * life);
			g.setColor(new Color((alpha << 24) | rgb, true));
			g.setStroke(new BasicStroke(Math.max(1f, width * life), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g.drawLine(prev.x, prev.y, cur.x, cur.y);
			prev = cur;
		}

		return null;
	}
}
