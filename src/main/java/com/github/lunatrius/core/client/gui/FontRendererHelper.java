package com.github.lunatrius.core.client.gui;

import net.minecraft.client.gui.FontRenderer;

public class FontRendererHelper {
	public static void drawLeftAlignedString(FontRenderer fontRenderer, String str, int x, int y, int color) {
		fontRenderer.drawStringWithShadow(str, x, y, color);
	}

	public static void drawCenteredString(FontRenderer fontRenderer, String str, int x, int y, int color) {
		fontRenderer.drawStringWithShadow(str, x - fontRenderer.getStringWidth(str) / 2, y, color);
	}

	public static void drawRightAlignedString(FontRenderer fontRenderer, String str, int x, int y, int color) {
		fontRenderer.drawStringWithShadow(str, x - fontRenderer.getStringWidth(str), y, color);
	}

}
