package com.rgbcursor;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import java.awt.Cursor;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "RGB Cursor",
	description = "Animated rainbow mouse cursor and trail. Disable the Custom Cursor plugin while using this.",
	tags = {"cursor", "rgb", "rainbow", "mouse", "trail"}
)
public class RgbCursorPlugin extends Plugin
{
	@Inject
	private ClientUI clientUI;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private RgbTrailOverlay trailOverlay;

	@Inject
	private RgbCursorConfig config;

	private static final Set<String> CURSOR_KEYS = ImmutableSet.of(
		"cursorEnabled", "shape", "effect", "cycleSeconds", "saturation", "arrowSize", "outline");

	private final long startNanos = System.nanoTime();

	// Swing state below is only touched on the event dispatch thread
	private Timer timer;
	private Cursor[] frames;
	private int shownFrame = -1;

	@Override
	protected void startUp()
	{
		overlayManager.add(trailOverlay);
		SwingUtilities.invokeLater(this::rebuildCursor);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(trailOverlay);
		SwingUtilities.invokeLater(this::stopCursor);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		// Trail settings are read live each frame; only cursor settings need new frames
		if (RgbCursorConfig.GROUP.equals(event.getGroup()) && CURSOR_KEYS.contains(event.getKey()))
		{
			SwingUtilities.invokeLater(this::rebuildCursor);
		}
	}

	/**
	 * Current position on the color wheel (0..1), shared by the cursor and the trail so they stay in sync.
	 */
	float getHue(long nanos)
	{
		long cycle = config.cycleSeconds() * 1_000_000_000L;
		return (float) ((nanos - startNanos) % cycle) / cycle;
	}

	private void rebuildCursor()
	{
		stopCursor();
		if (!config.cursorEnabled())
		{
			return;
		}

		frames = CursorFrames.build(config.shape(), config.effect(), config.arrowSize(), config.saturation() / 100f, config.outline());

		int delay = Math.max(16, config.cycleSeconds() * 1000 / CursorFrames.FRAME_COUNT);
		timer = new Timer(delay, e -> tick());
		timer.setCoalesce(true);
		timer.start();
		tick();
	}

	private void tick()
	{
		if (frames == null)
		{
			return;
		}

		int frame = (int) (getHue(System.nanoTime()) * frames.length) % frames.length;
		if (frame != shownFrame)
		{
			shownFrame = frame;
			clientUI.setCursor(frames[frame]);
		}
	}

	private void stopCursor()
	{
		if (timer == null)
		{
			return;
		}

		timer.stop();
		timer = null;
		frames = null;
		shownFrame = -1;

		// Put back whatever cursor the client had (e.g. one set by Custom Cursor), or the system default
		Cursor previous = clientUI.getDefaultCursor();
		if (previous != null)
		{
			clientUI.setCursor(previous);
		}
		else
		{
			clientUI.resetCursor();
		}
	}

	@Provides
	RgbCursorConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RgbCursorConfig.class);
	}
}
