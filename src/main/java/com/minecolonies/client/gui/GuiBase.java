package com.minecolonies.client.gui;

import com.minecolonies.lib.Constants;
import com.minecolonies.util.LanguageHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class GuiBase extends GuiScreen
{
    protected final int idHireWorker = 0, //IDs for default layout
            idFireWorker             = 1, idRecallWorker = 2, idBuildBuilding = 3, idRepairBuilding = 4;
    protected final ResourceLocation background                                  = new ResourceLocation(Constants.MODID + ":" + "textures/gui/guiHutBackground.png");
    protected       int              middleX, middleY, xSize, ySize, buttonWidth = 116, buttonHeight = 20, buttonSpan = 4;
    protected ArrayList<GuiModIcon> iconList;

    public GuiBase()
    {
        super();
        xSize = 171;
        ySize = 247;
        iconList = new ArrayList<GuiModIcon>();
    }

    protected void addElements()
    {
        middleX = (width / 2);
        middleY = (height - ySize) / 2;

        buttonList.clear();
        labelList.clear();
        iconList.clear();
    }

    protected GuiButton addButton(int id, String text, int x, int y, int w, int h)
    {
        return addButton(id, text, x, y, w, h, true);
    }

    protected GuiButton addButton(int id, String text, int x, int y, int w, int h, boolean visible)
    {
        GuiButton button = new GuiButton(id, x, y, w, h, text);
        button.visible = visible;
        buttonList.add(id, button);
        return button;
    }

    protected void addLabel(String text, int x, int y)
    {
        labelList.add(new GuiModLabel(text, x, y));
    }

    protected void addLabel(String text, int x, int y, int color)
    {
        labelList.add(new GuiModLabel(text, x, y, color));
    }

    protected void addIcon(ItemStack is, int x, int y)
    {
        iconList.add(new GuiModIcon(is, x, y));
    }

    protected void addDefaultWorkerLayout(String hutName, String workerName, String level, String type)
    {
        addDefaultWorkerLayout(hutName, workerName, level, type, 0);
    }

    protected void addDefaultWorkerLayout(String hutName, String workerName, String level, String type, int span)
    {
        String workerAssigned = LanguageHandler.format("com.minecolonies.gui.workerHuts.workerAssigned");
        String workerLevel = LanguageHandler.format("com.minecolonies.gui.workerHuts.workerLevel");
        String buildType = LanguageHandler.format("com.minecolonies.gui.workerHuts.buildType");

        addLabel(hutName, middleX - fontRendererObj.getStringWidth(hutName) / 2, middleY + span, 0xff0000);
        addLabel(workerAssigned, middleX - fontRendererObj.getStringWidth(workerAssigned) / 2, middleY + span + 18);
        addLabel(workerName, middleX - fontRendererObj.getStringWidth(workerName) / 2, middleY + span + 28);
        addLabel(workerLevel + " " + level, middleX - fontRendererObj.getStringWidth(workerLevel + " " + level) / 2, middleY + span + 44);
        addButton(idHireWorker, LanguageHandler.format("com.minecolonies.gui.workerHuts.hire"), middleX - buttonWidth / 2, middleY + span + 64, buttonWidth, buttonHeight, false);
        addButton(idFireWorker, LanguageHandler.format("com.minecolonies.gui.workerHuts.fire"), middleX - buttonWidth / 2, middleY + span + 64, buttonWidth, buttonHeight);
        addButton(idRecallWorker, LanguageHandler.format("com.minecolonies.gui.workerHuts.recall"), middleX - buttonWidth / 2, middleY + span + 88, buttonWidth, buttonHeight);
        addButton(idBuildBuilding, LanguageHandler.format("com.minecolonies.gui.workerHuts.build"), middleX - buttonWidth / 2, middleY + span + 120, buttonWidth, buttonHeight);
        addButton(idRepairBuilding, LanguageHandler.format("com.minecolonies.gui.workerHuts.repair"), middleX - buttonWidth / 2, middleY + span + 144, buttonWidth, buttonHeight);
        addLabel(buildType, middleX - fontRendererObj.getStringWidth(buildType) / 2, middleY + span + 172);
        addLabel(type, middleX - fontRendererObj.getStringWidth(type) / 2, middleY + span + 182);
    }

    protected void drawGuiBackground()
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(background);
        int xCoord = (width - xSize) / 2;
        int yCoord = (height - ySize - 10) / 2;
        drawTexturedModalRect(xCoord, yCoord, 0, 0, xSize, ySize);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float par3)
    {
        drawGuiBackground();

        int k;
        for(k = 0; k < this.buttonList.size(); k++)
        {
            ((GuiButton) this.buttonList.get(k)).drawButton(this.mc, mouseX, mouseY);
        }
        for(k = 0; k < this.labelList.size(); k++)
        {
            ((GuiModLabel) this.labelList.get(k)).drawLabel(this.mc);
        }
        for(k = 0; k < this.iconList.size(); k++)
        {
            (this.iconList.get(k)).drawIcon(this.mc, itemRender);
        }
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    public void initGui()
    {
        addElements();
        super.initGui();
    }

    @Override
    protected void keyTyped(char par1, int par2)
    {
        if(par2 == 1 || par2 == this.mc.gameSettings.keyBindInventory.getKeyCode())
        {
            this.mc.thePlayer.closeScreen();
        }
    }
}
