package com.github.lunatrius.schematica.client.gui;

import com.github.lunatrius.schematica.Settings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

class GuiSchematicMaterialsSlot extends GuiSlot {
	private final FontRenderer fontRenderer = Settings.instance.minecraft.fontRenderer;
	private final TextureManager renderEngine = Settings.instance.minecraft.renderEngine;

	private final GuiSchematicMaterials guiSchematicMaterials;

	protected int selectedIndex = -1;

	public GuiSchematicMaterialsSlot(GuiSchematicMaterials par1) {
		super(Minecraft.getMinecraft(), par1.width, par1.height, 16, par1.height - 34, 24);
		this.guiSchematicMaterials = par1;
		this.selectedIndex = -1;
	}

	@Override
	protected int getSize() {
		return this.guiSchematicMaterials.blockList.size();
	}

	@Override
	protected void elementClicked(int index, boolean par2, int par3, int par4) {
		this.selectedIndex = index;
	}

	@Override
	protected boolean isSelected(int index) {
		return index == this.selectedIndex;
	}

	@Override
	protected void drawBackground() {
	}

	@Override
	protected void drawContainerBackground(Tessellator tessellator) {
	}

	@Override
	protected void drawSlot(int index, int x, int y, int par4, Tessellator tessellator, int par6, int par7) {
		ItemStack itemStack = this.guiSchematicMaterials.blockList.get(index);

		String itemName;
		String amount = Integer.toString(itemStack.stackSize);

		if (itemStack != null && itemStack.getItem() != null) {
			itemName = itemStack.getItem().getItemStackDisplayName(itemStack);
		} else {
			itemName = "Unknown";
		}

		drawItemStack(x, y, itemStack);

		this.guiSchematicMaterials.drawString(this.fontRenderer, itemName, x + 24, y + 6, 16777215);
		this.guiSchematicMaterials.drawString(this.fontRenderer, amount, x + 215 - this.fontRenderer.getStringWidth(amount), y + 6, 16777215);
	}

	private void drawItemStack(int x, int y, ItemStack itemStack) {
		drawItemStackSlot(x, y);

		if (itemStack != null && itemStack.getItem() != null) {
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			RenderHelper.enableGUIStandardItemLighting();
			Settings.renderItem.renderItemIntoGUI(this.fontRenderer, this.renderEngine, itemStack, x + 2, y + 2);
			RenderHelper.disableStandardItemLighting();
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		}
	}

	private void drawItemStackSlot(int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.renderEngine.bindTexture(Gui.statIcons);
		Tessellator var10 = Tessellator.instance;
		var10.startDrawingQuads();
		var10.addVertexWithUV(x + 1 + 0, y + 1 + 18, 0, 0 * 0.0078125F, 18 * 0.0078125F);
		var10.addVertexWithUV(x + 1 + 18, y + 1 + 18, 0, 18 * 0.0078125F, 18 * 0.0078125F);
		var10.addVertexWithUV(x + 1 + 18, y + 1 + 0, 0, 18 * 0.0078125F, 0 * 0.0078125F);
		var10.addVertexWithUV(x + 1 + 0, y + 1 + 0, 0, 0 * 0.0078125F, 0 * 0.0078125F);
		var10.draw();
	}
}
