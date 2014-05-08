package com.minecolonies.client.gui;

import com.minecolonies.lib.Constants;
import com.minecolonies.tileentities.TileEntityHutBuilder;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Created by Nico on 08.05.2014.
 */
public class GuiHutBuilder extends GuiScreen
{
    private TileEntityHutBuilder tileEntityHutBuilder;
    private int numberOfButtons  = 4;
    private int idFireWorker     = 0;
    private int idRecallWorker   = 1;
    private int idBuildBuilding  = 2;
    private int idRepairBuilding = 3;
    private int xSize;
    private int ySize;
    private int middleX      = 0;
    private int middleY      = 0;
    private int buttonWidth  = 116;
    private int buttonHeight = 20;
    private int span         = 10;

    private final ResourceLocation background = new ResourceLocation(Constants.MODID + ":" + "textures/gui/guiInformatorBackground.png");

    public GuiHutBuilder(TileEntityHutBuilder tileEntityHutBuilder)
    {
        xSize = 171;
        ySize = 247;
        this.tileEntityHutBuilder = tileEntityHutBuilder;
    }

    @Override
    public void initGui()
    {
        addButtons();
        super.initGui();
    }

    private void addButtons()
    {
        middleX = (width / 2);
        middleY = (height - ySize) / 2;

        buttonList.clear();

        buttonList.add(new GuiButton(idFireWorker, middleX - buttonWidth / 2, middleY + span + 46, buttonWidth, buttonHeight, I18n.format("com.minecolonies.gui.builderHut.fire")));
        buttonList.add(new GuiButton(idRecallWorker, middleX - buttonWidth / 2, middleY + span + 70, buttonWidth, buttonHeight, I18n.format("com.minecolonies.gui.builderHut.recall")));
        buttonList.add(new GuiButton(idBuildBuilding, middleX - buttonWidth / 2, middleY + span + 102, buttonWidth, buttonHeight, I18n.format("com.minecolonies.gui.builderHut.build")));
        buttonList.add(new GuiButton(idRepairBuilding, middleX - buttonWidth / 2, middleY + span + 126, buttonWidth, buttonHeight, I18n.format("com.minecolonies.gui.builderHut.repair")));
        /*
        //Current Spec
        y += buttonHeight + buttonSpan;
        buttonList.add(new GuiButton(idRenameColony, middleX - buttonWidth / 2, middleY + y, buttonWidth, buttonHeight, I18n.format("com.minecolonies.gui.townhall.rename")));

        //Bottom navigation
        buttonList.add(new GuiButton(idInformation, middleX - 76, middleY + ySize - 34, 64, buttonHeight, I18n.format("com.minecolonies.gui.townhall.information")));
        buttonList.add(new GuiButton(idActions, middleX - 10, middleY + ySize - 34, 44, buttonHeight, I18n.format("com.minecolonies.gui.townhall.actions")));
        buttonList.add(new GuiButton(idSettings, middleX + xSize / 2 - 50, middleY + ySize - 34, 46, buttonHeight, I18n.format("com.minecolonies.gui.townhall.settings")));*/
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    private void drawGuiBackground()
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(background);
        int xCoord = (width - xSize) / 2;
        int yCoord = (height - ySize - 10) / 2;
        drawTexturedModalRect(xCoord, yCoord, 0, 0, xSize, ySize);
    }

    private void drawGuiForeground()
    {
        String buildersHut = I18n.format("com.minecolonies.gui.builderHut.buildersHut");
        String workerAssigned = I18n.format("com.minecolonies.gui.builderHut.workerAssigned");
        String worker = "John R. Jones"; //TODO replace with actual workername
        String workerLevel = I18n.format("com.minecolonies.gui.builderHut.workerLevel");
        String level = "xx (yy)"; //TODO replace with actual level
        String buildType = I18n.format("com.minecolonies.gui.builderHut.buildType");
        String bType = "xxxxxxxx"; //TODO replace with actual style

        fontRendererObj.drawString(buildersHut, middleX - fontRendererObj.getStringWidth(buildersHut) / 2 + 3, middleY + span, 0x000000);
        fontRendererObj.drawString(workerAssigned, middleX - fontRendererObj.getStringWidth(workerAssigned) + 5, middleY + span + 18, 0x000000);
        fontRendererObj.drawString(worker, middleX + 10, middleY + span + 18, 0x000000);
        fontRendererObj.drawString(workerLevel, middleX - fontRendererObj.getStringWidth(workerLevel) + 5, middleY + span + 28, 0x000000);
        fontRendererObj.drawString(level, middleX + 10, middleY + span + 28, 0x000000);

        fontRendererObj.drawString(buildType, middleX - fontRendererObj.getStringWidth(buildType) + 5, middleY + span + 160, 0x000000);
        fontRendererObj.drawString(bType, middleX + 10, middleY + span + 160, 0x000000);
    }

    @Override
    public void drawScreen(int par1, int par2, float par3)
    {
        drawGuiBackground();
        drawGuiForeground();

        for(int k = 0; k < this.buttonList.size(); ++k)
        {
            GuiButton guibutton = (GuiButton) this.buttonList.get(k);
            guibutton.drawButton(this.mc, par1, par2);
        }
    }
}
