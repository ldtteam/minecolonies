package com.minecolonies.client.gui;

import com.minecolonies.lib.Constants;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiBase extends GuiScreen {
    protected int middleX, middleY, xSize, ySize,
            buttonWidth = 116,
            buttonHeight = 20;
    protected final int idHireWorker = 0, //IDs for default layout
            idFireWorker = 1,
            idRecallWorker = 2,
            idBuildBuilding = 3,
            idRepairBuilding = 4;
    protected final ResourceLocation background = new ResourceLocation(Constants.MODID + ":" + "textures/gui/guiInformatorBackground.png");

    protected void addElements() {
        middleX = (width / 2);
        middleY = (height - ySize) / 2;

        buttonList.clear();
        labelList.clear();
    }

    protected void addButton(int id, String text, int x, int y, int w, int h) {
        addButton(id, text, x, y, w, h, true);
    }

    protected void addButton(int id, String text, int x, int y, int w, int h, boolean visible) {
        GuiButton button = new GuiButton(id, x, y, w, h, text);
        button.visible = visible;
        buttonList.add(id, button);
    }

    protected void addLabel(String text, int x, int y) {
        labelList.add(new GuiModLabel(text, x, y));
    }

    protected void addDefaultWorkerLayout(String hutName, String workerName, String level, String type) {
        addDefaultWorkerLayout(hutName, workerName, level, type, 0);
    }

    protected void addDefaultWorkerLayout(String hutName, String workerName, String level, String type, int span) {
        String workerAssigned = I18n.format("com.minecolonies.gui.workerHuts.workerAssigned");
        String workerLevel = I18n.format("com.minecolonies.gui.workerHuts.workerLevel");
        String buildType = I18n.format("com.minecolonies.gui.workerHuts.buildType");

        addLabel(hutName, middleX - fontRendererObj.getStringWidth(hutName) / 2 + 3, middleY + span);
        addLabel(workerAssigned, middleX - fontRendererObj.getStringWidth(workerAssigned) + 5, middleY + span + 18);
        addLabel(workerName, middleX + 10, middleY + span + 18);
        addLabel(workerLevel, middleX - fontRendererObj.getStringWidth(workerLevel) + 5, middleY + span + 28);
        addLabel(level, middleX + 10, middleY + span + 28);
        addButton(idHireWorker, I18n.format("com.minecolonies.gui.workerHuts.hire"), middleX - buttonWidth / 2, middleY + span + 46, buttonWidth, buttonHeight, false);
        addButton(idFireWorker, I18n.format("com.minecolonies.gui.workerHuts.fire"), middleX - buttonWidth / 2, middleY + span + 46, buttonWidth, buttonHeight);
        addButton(idRecallWorker, I18n.format("com.minecolonies.gui.workerHuts.recall"), middleX - buttonWidth / 2, middleY + span + 70, buttonWidth, buttonHeight);
        addButton(idBuildBuilding, I18n.format("com.minecolonies.gui.workerHuts.build"), middleX - buttonWidth / 2, middleY + span + 102, buttonWidth, buttonHeight);
        addButton(idRepairBuilding, I18n.format("com.minecolonies.gui.workerHuts.repair"), middleX - buttonWidth / 2, middleY + span + 126, buttonWidth, buttonHeight);
        addLabel(buildType, middleX - fontRendererObj.getStringWidth(buildType) + 5, middleY + span + 160);
        addLabel(type, middleX + 10, middleY + span + 160);
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
