package com.rgbcursor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Writes a contact sheet of every shape and effect to a PNG, for checking the recolor without launching the client.
 */
public class CursorPreview
{
	public static void main(String[] args) throws Exception
	{
		final int cell = 80;
		final int[] picks = {0, 10, 20, 30, 40, 50};
		CursorShape[] shapes = CursorShape.values();
		RgbCursorEffect[] effects = RgbCursorEffect.values();
		BufferedImage sheet = new BufferedImage(picks.length * cell * effects.length, shapes.length * cell, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = sheet.createGraphics();
		// Half dark, half light background to judge contrast on both
		g.setColor(new Color(70, 60, 45));
		g.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());
		g.setColor(new Color(225, 215, 190));
		g.fillRect(0, sheet.getHeight() / 2, sheet.getWidth(), sheet.getHeight() / 2);
		for (int s = 0; s < shapes.length; s++)
		{
			for (int e = 0; e < effects.length; e++)
			{
				BufferedImage[] frames = CursorFrames.render(shapes[s], effects[e], 17, 1f, true, BorderColor.WHITE.getColor(), 2, new Point());
				for (int p = 0; p < picks.length; p++)
				{
					g.drawImage(frames[picks[p]], (e * picks.length + p) * cell + 4, s * cell + 4, null);
				}
			}
		}
		g.dispose();
		ImageIO.write(sheet, "png", new File(args[0]));
	}
}
