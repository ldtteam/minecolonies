package com.github.lunatrius.schematica.client.gui;

import com.github.lunatrius.schematica.Settings;
import com.github.lunatrius.schematica.lib.Reference;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

public class GuiSchematicSave extends GuiScreen {
	private final Settings settings = Settings.instance;
	@SuppressWarnings("unused")
	private final GuiScreen prevGuiScreen;

	private int centerX = 0;
	private int centerY = 0;

	private GuiButton btnPointA = null;

	private GuiButton btnDecAX = null;
	private GuiButton btnAmountAX = null;
	private GuiButton btnIncAX = null;

	private GuiButton btnDecAY = null;
	private GuiButton btnAmountAY = null;
	private GuiButton btnIncAY = null;

	private GuiButton btnDecAZ = null;
	private GuiButton btnAmountAZ = null;
	private GuiButton btnIncAZ = null;

	private GuiButton btnPointB = null;

	private GuiButton btnDecBX = null;
	private GuiButton btnAmountBX = null;
	private GuiButton btnIncBX = null;

	private GuiButton btnDecBY = null;
	private GuiButton btnAmountBY = null;
	private GuiButton btnIncBY = null;

	private GuiButton btnDecBZ = null;
	private GuiButton btnAmountBZ = null;
	private GuiButton btnIncBZ = null;

	private int incrementAX = 0;
	private int incrementAY = 0;
	private int incrementAZ = 0;

	private int incrementBX = 0;
	private int incrementBY = 0;
	private int incrementBZ = 0;

	private GuiButton btnEnable = null;
	private GuiButton btnSave = null;
	private GuiTextField tfFilename = null;

	private String filename = "";

	private final String strSaveSelection = I18n.format("com.github.lunatrius.schematica.gui.saveselection");
	private final String strX = I18n.format("com.github.lunatrius.schematica.gui.x");
	private final String strY = I18n.format("com.github.lunatrius.schematica.gui.y");
	private final String strZ = I18n.format("com.github.lunatrius.schematica.gui.z");

	public GuiSchematicSave(GuiScreen guiScreen) {
		this.prevGuiScreen = guiScreen;
	}

	@Override
	public void initGui() {
		this.centerX = this.width / 2;
		this.centerY = this.height / 2;

		this.buttonList.clear();

		int id = 0;

		this.btnPointA = new GuiButton(id++, this.centerX - 130, this.centerY - 55, 100, 20, I18n.format("com.github.lunatrius.schematica.gui.point.red"));
		this.buttonList.add(this.btnPointA);

		this.btnDecAX = new GuiButton(id++, this.centerX - 130, this.centerY - 30, 30, 20, I18n.format("com.github.lunatrius.schematica.gui.decrease"));
		this.buttonList.add(this.btnDecAX);

		this.btnAmountAX = new GuiButton(id++, this.centerX - 95, this.centerY - 30, 30, 20, Integer.toString(this.settings.increments[this.incrementAX]));
		this.buttonList.add(this.btnAmountAX);

		this.btnIncAX = new GuiButton(id++, this.centerX - 60, this.centerY - 30, 30, 20, I18n.format("com.github.lunatrius.schematica.gui.increase"));
		this.buttonList.add(this.btnIncAX);

		this.btnDecAY = new GuiButton(id++, this.centerX - 130, this.centerY - 5, 30, 20, I18n.format("com.github.lunatrius.schematica.gui.decrease"));
		this.buttonList.add(this.btnDecAY);

		this.btnAmountAY = new GuiButton(id++, this.centerX - 95, this.centerY - 5, 30, 20, Integer.toString(this.settings.increments[this.incrementAY]));
		this.buttonList.add(this.btnAmountAY);

		this.btnIncAY = new GuiButton(id++, this.centerX - 60, this.centerY - 5, 30, 20, I18n.format("com.github.lunatrius.schematica.gui.increase"));
		this.buttonList.add(this.btnIncAY);

		this.btnDecAZ = new GuiButton(id++, this.centerX - 130, this.centerY + 20, 30, 20, I18n.format("com.github.lunatrius.schematica.gui.decrease"));
		this.buttonList.add(this.btnDecAZ);

		this.btnAmountAZ = new GuiButton(id++, this.centerX - 95, this.centerY + 20, 30, 20, Integer.toString(this.settings.increments[this.incrementAZ]));
		this.buttonList.add(this.btnAmountAZ);

		this.btnIncAZ = new GuiButton(id++, this.centerX - 60, this.centerY + 20, 30, 20, I18n.format("com.github.lunatrius.schematica.gui.increase"));
		this.buttonList.add(this.btnIncAZ);

		this.btnPointB = new GuiButton(id++, this.centerX + 30, this.centerY - 55, 100, 20, I18n.format("com.github.lunatrius.schematica.gui.point.blue"));
		this.buttonList.add(this.btnPointB);

		this.btnDecBX = new GuiButton(id++, this.centerX + 30, this.centerY - 30, 30, 20, I18n.format("com.github.lunatrius.schematica.gui.decrease"));
		this.buttonList.add(this.btnDecBX);

		this.btnAmountBX = new GuiButton(id++, this.centerX + 65, this.centerY - 30, 30, 20, Integer.toString(this.settings.increments[this.incrementBX]));
		this.buttonList.add(this.btnAmountBX);

		this.btnIncBX = new GuiButton(id++, this.centerX + 100, this.centerY - 30, 30, 20, I18n.format("com.github.lunatrius.schematica.gui.increase"));
		this.buttonList.add(this.btnIncBX);

		this.btnDecBY = new GuiButton(id++, this.centerX + 30, this.centerY - 5, 30, 20, I18n.format("com.github.lunatrius.schematica.gui.decrease"));
		this.buttonList.add(this.btnDecBY);

		this.btnAmountBY = new GuiButton(id++, this.centerX + 65, this.centerY - 5, 30, 20, Integer.toString(this.settings.increments[this.incrementBY]));
		this.buttonList.add(this.btnAmountBY);

		this.btnIncBY = new GuiButton(id++, this.centerX + 100, this.centerY - 5, 30, 20, I18n.format("com.github.lunatrius.schematica.gui.increase"));
		this.buttonList.add(this.btnIncBY);

		this.btnDecBZ = new GuiButton(id++, this.centerX + 30, this.centerY + 20, 30, 20, I18n.format("com.github.lunatrius.schematica.gui.decrease"));
		this.buttonList.add(this.btnDecBZ);

		this.btnAmountBZ = new GuiButton(id++, this.centerX + 65, this.centerY + 20, 30, 20, Integer.toString(this.settings.increments[this.incrementBZ]));
		this.buttonList.add(this.btnAmountBZ);

		this.btnIncBZ = new GuiButton(id++, this.centerX + 100, this.centerY + 20, 30, 20, I18n.format("com.github.lunatrius.schematica.gui.increase"));
		this.buttonList.add(this.btnIncBZ);

		this.btnEnable = new GuiButton(id++, this.width - 210, this.height - 30, 50, 20, I18n.format(this.settings.isRenderingGuide ? "com.github.lunatrius.schematica.gui.disable" : "com.github.lunatrius.schematica.gui.enable"));
		this.buttonList.add(this.btnEnable);

		this.tfFilename = new GuiTextField(this.fontRendererObj, this.width - 155, this.height - 29, 100, 18);

		this.btnSave = new GuiButton(id++, this.width - 50, this.height - 30, 40, 20, I18n.format("com.github.lunatrius.schematica.gui.save"));
		this.btnSave.enabled = this.settings.isRenderingGuide;
		this.buttonList.add(this.btnSave);

		this.tfFilename.setMaxStringLength(1024);
		this.tfFilename.setText(this.filename);
	}

	@Override
	protected void actionPerformed(GuiButton guiButton) {
		if (guiButton.enabled) {
			if (guiButton.id == this.btnPointA.id) {
				this.settings.moveHere(this.settings.pointA);
				this.settings.updatePoints();
			} else if (guiButton.id == this.btnDecAX.id) {
				this.settings.pointA.x -= this.settings.increments[this.incrementAX];
				this.settings.updatePoints();
			} else if (guiButton.id == this.btnIncAX.id) {
				this.settings.pointA.x += this.settings.increments[this.incrementAX];
				this.settings.updatePoints();
			} else if (guiButton.id == this.btnAmountAX.id) {
				this.incrementAX = (this.incrementAX + 1) % this.settings.increments.length;
				this.btnAmountAX.displayString = Integer.toString(this.settings.increments[this.incrementAX]);
			} else if (guiButton.id == this.btnDecAY.id) {
				this.settings.pointA.y -= this.settings.increments[this.incrementAY];
				this.settings.updatePoints();
			} else if (guiButton.id == this.btnIncAY.id) {
				this.settings.pointA.y += this.settings.increments[this.incrementAY];
				this.settings.updatePoints();
			} else if (guiButton.id == this.btnAmountAY.id) {
				this.incrementAY = (this.incrementAY + 1) % this.settings.increments.length;
				this.btnAmountAY.displayString = Integer.toString(this.settings.increments[this.incrementAY]);
			} else if (guiButton.id == this.btnDecAZ.id) {
				this.settings.pointA.z -= this.settings.increments[this.incrementAZ];
				this.settings.updatePoints();
			} else if (guiButton.id == this.btnIncAZ.id) {
				this.settings.pointA.z += this.settings.increments[this.incrementAZ];
				this.settings.updatePoints();
			} else if (guiButton.id == this.btnAmountAZ.id) {
				this.incrementAZ = (this.incrementAZ + 1) % this.settings.increments.length;
				this.btnAmountAZ.displayString = Integer.toString(this.settings.increments[this.incrementAZ]);
			} else if (guiButton.id == this.btnPointB.id) {
				this.settings.moveHere(this.settings.pointB);
				this.settings.updatePoints();
			} else if (guiButton.id == this.btnDecBX.id) {
				this.settings.pointB.x -= this.settings.increments[this.incrementBX];
				this.settings.updatePoints();
			} else if (guiButton.id == this.btnIncBX.id) {
				this.settings.pointB.x += this.settings.increments[this.incrementBX];
				this.settings.updatePoints();
			} else if (guiButton.id == this.btnAmountBX.id) {
				this.incrementBX = (this.incrementBX + 1) % this.settings.increments.length;
				this.btnAmountBX.displayString = Integer.toString(this.settings.increments[this.incrementBX]);
			} else if (guiButton.id == this.btnDecBY.id) {
				this.settings.pointB.y -= this.settings.increments[this.incrementBY];
				this.settings.updatePoints();
			} else if (guiButton.id == this.btnIncBY.id) {
				this.settings.pointB.y += this.settings.increments[this.incrementBY];
				this.settings.updatePoints();
			} else if (guiButton.id == this.btnAmountBY.id) {
				this.incrementBY = (this.incrementBY + 1) % this.settings.increments.length;
				this.btnAmountBY.displayString = Integer.toString(this.settings.increments[this.incrementBY]);
			} else if (guiButton.id == this.btnDecBZ.id) {
				this.settings.pointB.z -= this.settings.increments[this.incrementBZ];
				this.settings.updatePoints();
			} else if (guiButton.id == this.btnIncBZ.id) {
				this.settings.pointB.z += this.settings.increments[this.incrementBZ];
				this.settings.updatePoints();
			} else if (guiButton.id == this.btnAmountBZ.id) {
				this.incrementBZ = (this.incrementBZ + 1) % this.settings.increments.length;
				this.btnAmountBZ.displayString = Integer.toString(this.settings.increments[this.incrementBZ]);
			} else if (guiButton.id == this.btnEnable.id) {
				this.settings.isRenderingGuide = !this.settings.isRenderingGuide && this.settings.isSaveEnabled;
				this.btnEnable.displayString = I18n.format(this.settings.isRenderingGuide ? "com.github.lunatrius.schematica.gui.disable" : "com.github.lunatrius.schematica.gui.enable");
				this.btnSave.enabled = this.settings.isRenderingGuide;
			} else if (guiButton.id == this.btnSave.id) {
				String path = this.tfFilename.getText() + ".schematic";
				if (this.settings.saveSchematic(Reference.schematicDirectory, path, this.settings.pointMin, this.settings.pointMax)) {
					this.filename = "";
					this.tfFilename.setText(this.filename);
				}
			}
		}
	}

	@Override
	protected void mouseClicked(int par1, int par2, int par3) {
		this.tfFilename.mouseClicked(par1, par2, par3);
		super.mouseClicked(par1, par2, par3);
	}

	@Override
	protected void keyTyped(char par1, int par2) {
		this.tfFilename.textboxKeyTyped(par1, par2);
		this.filename = this.tfFilename.getText();
		super.keyTyped(par1, par2);
	}

	@Override
	public void updateScreen() {
		this.tfFilename.updateCursorCounter();
		super.updateScreen();
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		// drawDefaultBackground();

		drawString(this.fontRendererObj, this.strSaveSelection, this.width - 205, this.height - 45, 0xFFFFFF);

		drawString(this.fontRendererObj, this.strX, this.centerX - 145, this.centerY - 24, 0xFFFFFF);
		drawString(this.fontRendererObj, Integer.toString((int) this.settings.pointA.x), this.centerX - 25, this.centerY - 24, 0xFFFFFF);

		drawString(this.fontRendererObj, this.strY, this.centerX - 145, this.centerY + 1, 0xFFFFFF);
		drawString(this.fontRendererObj, Integer.toString((int) this.settings.pointA.y), this.centerX - 25, this.centerY + 1, 0xFFFFFF);

		drawString(this.fontRendererObj, this.strZ, this.centerX - 145, this.centerY + 26, 0xFFFFFF);
		drawString(this.fontRendererObj, Integer.toString((int) this.settings.pointA.z), this.centerX - 25, this.centerY + 26, 0xFFFFFF);

		drawString(this.fontRendererObj, this.strX, this.centerX + 15, this.centerY - 24, 0xFFFFFF);
		drawString(this.fontRendererObj, Integer.toString((int) this.settings.pointB.x), this.centerX + 135, this.centerY - 24, 0xFFFFFF);

		drawString(this.fontRendererObj, this.strY, this.centerX + 15, this.centerY + 1, 0xFFFFFF);
		drawString(this.fontRendererObj, Integer.toString((int) this.settings.pointB.y), this.centerX + 135, this.centerY + 1, 0xFFFFFF);

		drawString(this.fontRendererObj, this.strZ, this.centerX + 15, this.centerY + 26, 0xFFFFFF);
		drawString(this.fontRendererObj, Integer.toString((int) this.settings.pointB.z), this.centerX + 135, this.centerY + 26, 0xFFFFFF);

		this.tfFilename.drawTextBox();

		super.drawScreen(par1, par2, par3);
	}
}
