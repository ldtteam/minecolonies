package com.github.lunatrius.core.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;

public class GuiNumericField extends GuiButton {
	private static final int DEFAULT_VALUE = 0;
	private static final int BUTTON_WIDTH = 12;

	private final GuiTextField guiTextField;
	private final GuiButton guiButtonDec;
	private final GuiButton guiButtonInc;

	private String previous = String.valueOf(DEFAULT_VALUE);
	private int minimum = Integer.MIN_VALUE;
	private int maximum = Integer.MAX_VALUE;
	private boolean wasFocused = false;

	public GuiNumericField(FontRenderer fontRenderer, int id, int x, int y) {
		this(fontRenderer, id, x, y, 100, 20);
	}

	public GuiNumericField(FontRenderer fontRenderer, int id, int x, int y, int width) {
		this(fontRenderer, id, x, y, width, 20);
	}

	public GuiNumericField(FontRenderer fontRenderer, int id, int x, int y, int width, int height) {
		super(id, 0, 0, 0, 0, "");
		this.guiTextField = new GuiTextField(fontRenderer, x, y + 1, width - BUTTON_WIDTH * 2 - 1, height - 2);
		this.guiButtonDec = new GuiButton(0, x + width - BUTTON_WIDTH * 2, y, BUTTON_WIDTH, height, "-");
		this.guiButtonInc = new GuiButton(1, x + width - BUTTON_WIDTH * 1, y, BUTTON_WIDTH, height, "+");

		setValue(DEFAULT_VALUE);
	}

	@Override
	public boolean mousePressed(Minecraft minecraft, int x, int y) {
		if (this.wasFocused && !this.guiTextField.isFocused()) {
			this.wasFocused = false;
			return true;
		}

		this.wasFocused = this.guiTextField.isFocused();

		return this.guiButtonDec.mousePressed(minecraft, x, y) || this.guiButtonInc.mousePressed(minecraft, x, y);
	}

	@Override
	public void drawButton(Minecraft minecraft, int x, int y) {
		if (this.visible) {
			this.guiTextField.drawTextBox();
			this.guiButtonInc.drawButton(minecraft, x, y);
			this.guiButtonDec.drawButton(minecraft, x, y);
		}
	}

	public void mouseClicked(int x, int y, int action) {
		Minecraft minecraft = Minecraft.getMinecraft();

		this.guiTextField.mouseClicked(x, y, action);

		if (this.guiButtonInc.mousePressed(minecraft, x, y)) {
			setValue(getValue() + 1);
		}

		if (this.guiButtonDec.mousePressed(minecraft, x, y)) {
			setValue(getValue() - 1);
		}
	}

	public boolean keyTyped(char character, int code) {
		if (!this.guiTextField.isFocused()) {
			return false;
		}

		int cursorPositionOld = this.guiTextField.getCursorPosition();

		this.guiTextField.textboxKeyTyped(character, code);

		String text = this.guiTextField.getText();
		int cursorPositionNew = this.guiTextField.getCursorPosition();

		if (text.length() == 0) {
			text = String.valueOf(DEFAULT_VALUE);
		}

		try {
			long value = Long.parseLong(text);
			boolean outOfRange = false;

			if (value > this.maximum) {
				value = this.maximum;
				outOfRange = true;
			} else if (value < this.minimum) {
				value = this.minimum;
				outOfRange = true;
			}

			text = String.valueOf(value);

			if (!text.equals(this.previous) || outOfRange) {
				this.guiTextField.setText(String.valueOf(value));
				this.guiTextField.setCursorPosition(cursorPositionNew);
			}

			this.previous = text;

			return true;
		} catch (NumberFormatException nfe) {
			this.guiTextField.setText(this.previous);
			this.guiTextField.setCursorPosition(cursorPositionOld);
		}

		return false;

	}

	public void updateCursorCounter() {
		this.guiTextField.updateCursorCounter();
	}

	public void setMinimum(int minimum) {
		this.minimum = minimum;
	}

	public int getMinimum() {
		return this.minimum;
	}

	public void setMaximum(int maximum) {
		this.maximum = maximum;
	}

	public int getMaximum() {
		return this.maximum;
	}

	public void setValue(int value) {
		if (value > this.maximum) {
			value = this.maximum;
		} else if (value < this.minimum) {
			value = this.minimum;
		}
		this.guiTextField.setText(String.valueOf(value));
	}

	public int getValue() {
		return Integer.parseInt(this.guiTextField.getText());
	}
}
