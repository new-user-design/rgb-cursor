package com.rgbcursor;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import java.awt.Cursor;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.BeforeRender;
import net.runelite.client.callback.ClientThread;
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
	tags = {"cursor", "rgb", "rainbow", "mouse", "trail", "click", "cross"}
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
	private RgbClickOverlay clickOverlay;

	@Inject
	private ClickCrossRecolor crossRecolor;

	@Inject
	private ClientThread clientThread;

	@Inject
	private RgbCursorConfig config;

	private static final Set<String> CURSOR_KEYS = ImmutableSet.of(
		"cursorEnabled", "shape", "effect", "staticColor", "cycleSeconds", "saturation", "arrowSize", "outline", "borderColor");

	private final long startNanos = System.nanoTime();

	// Swing state below is only touched on the event dispatch thread
	private Timer timer;
	private Cursor[] frames;
	private int shownFrame = -1;

	@Override
	protected void startUp()
	{
		overlayManager.add(trailOverlay);
		overlayManager.add(clickOverlay);
		SwingUtilities.invokeLater(this::rebuildCursor);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(trailOverlay);
		overlayManager.remove(clickOverlay);
		// Hand the game back its original click crosses
		clientThread.invoke(crossRecolor::restore);
		SwingUtilities.invokeLater(this::stopCursor);
	}

	@Subscribe
	public void onBeforeRender(BeforeRender event)
	{
		crossRecolor.update();
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

		frames = CursorFrames.build(config.shape(), config.effect(), config.staticColor(), config.arrowSize(),
			config.saturation() / 100f, config.outline(), config.borderColor());

		if (frames.length == 1)
		{
			// Static cursor: set it once, nothing to animate
			clientUI.setCursor(frames[0]);
			return;
		}

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
		if (frames == null)
		{
			return;
		}

		if (timer != null)
		{
			timer.stop();
			timer = null;
		}
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
