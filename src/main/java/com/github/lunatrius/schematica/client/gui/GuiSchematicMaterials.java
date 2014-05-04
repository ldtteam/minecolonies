package com.github.lunatrius.schematica.client.gui;

import com.github.lunatrius.schematica.Schematica;
import com.github.lunatrius.schematica.world.SchematicWorld;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GuiSchematicMaterials extends GuiScreen {
	private final GuiScreen prevGuiScreen;
	private GuiSchematicMaterialsSlot guiSchematicMaterialsSlot;

	private GuiButton btnDone = null;

	private final String strMaterialName = I18n.format("com.github.lunatrius.schematica.gui.materialname");
	private final String strMaterialAmount = I18n.format("com.github.lunatrius.schematica.gui.materialamount");

	protected final List<ItemStack> blockList;

	public GuiSchematicMaterials(GuiScreen guiScreen) {
		this.prevGuiScreen = guiScreen;
		SchematicWorld schematic = Schematica.proxy.getActiveSchematic();
		if (schematic != null) {
			this.blockList = schematic.getBlockList();
		} else {
			this.blockList = new ArrayList<ItemStack>();
		}
	}

	@Override
	public void initGui() {
		int id = 0;

		this.btnDone = new GuiButton(id++, this.width / 2 + 4, this.height - 30, 150, 20, I18n.format("com.github.lunatrius.schematica.gui.done"));
		this.buttonList.add(this.btnDone);

		this.guiSchematicMaterialsSlot = new GuiSchematicMaterialsSlot(this);
	}

	@Override
	protected void actionPerformed(GuiButton guiButton) {
		if (guiButton.enabled) {
			if (guiButton.id == this.btnDone.id) {
				this.mc.displayGuiScreen(this.prevGuiScreen);
			} else {
				this.guiSchematicMaterialsSlot.actionPerformed(guiButton);
			}
		}
	}

	@Override
	public void drawScreen(int x, int y, float partialTicks) {
		this.guiSchematicMaterialsSlot.drawScreen(x, y, partialTicks);

		drawString(this.fontRendererObj, this.strMaterialName, this.width / 2 - 108, 4, 0x00FFFFFF);
		drawString(this.fontRendererObj, this.strMaterialAmount, this.width / 2 + 108 - this.fontRendererObj.getStringWidth(this.strMaterialAmount), 4, 0x00FFFFFF);
		super.drawScreen(x, y, partialTicks);
	}
}
