package com.minecolonies.client.gui;

import com.minecolonies.lib.Constants;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiBase extends GuiScreen {
    protected int middleX, middleY, xSize, ySize;
    protected final ResourceLocation background = new ResourceLocation(Constants.MODID + ":" + "textures/gui/guiInformatorBackground.png");

    protected void addElements() {
        middleX = (width / 2);
        middleY = (height - ySize) / 2;

        buttonList.clear();
        labelList.clear();
    }

    protected void addButton(int id, String text, int x, int y, int w, int h) {
        buttonList.add(new GuiButton(id, x, y, w, h, text));
    }

    protected void addLabel(String text, int x, int y) {
        labelList.add(new GuiModLabel(text, x, y));
    }

    public GuiBase() {
        xSize = 171;
        ySize = 247;
    }

    protected void drawGuiBackground() {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(background);
        int xCoord = (width - xSize) / 2;
        int yCoord = (height - ySize - 10) / 2;
        drawTexturedModalRect(xCoord, yCoord, 0, 0, xSize, ySize);
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        drawGuiBackground();

        int k;
        for (k = 0; k < this.buttonList.size(); ++k) {
            ((GuiButton) this.buttonList.get(k)).drawButton(this.mc, par1, par2);
        }
        for (k = 0; k < this.labelList.size(); ++k) {
            ((GuiModLabel) this.labelList.get(k)).drawLabel(this.mc);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        addElements();
        super.initGui();
    }
}
