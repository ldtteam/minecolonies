package com.minecolonies.client.gui;

import com.minecolonies.tileentities.TileEntityHutBuilder;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;

public class GuiHutBuilder extends GuiBase
{
    private TileEntityHutBuilder tileEntityHutBuilder;
    private int numberOfButtons  = 4;
    private int idFireWorker     = 0;
    private int idRecallWorker   = 1;
    private int idBuildBuilding  = 2;
    private int idRepairBuilding = 3;
    private int middleX          = 0;
    private int middleY          = 0;
    private int buttonWidth      = 116;
    private int buttonHeight     = 20;
    private int span             = 10;

    public GuiHutBuilder(TileEntityHutBuilder tileEntityHutBuilder)
    {
        super();
        this.tileEntityHutBuilder = tileEntityHutBuilder;
    }

    protected void addButtons()
    {
        middleX = (width / 2);
        middleY = (height - ySize) / 2;

        buttonList.clear();
        buttonList.add(new GuiButton(idFireWorker, middleX - buttonWidth / 2, middleY + span + 46, buttonWidth, buttonHeight, I18n.format("com.minecolonies.gui.builderHut.fire")));
        buttonList.add(new GuiButton(idRecallWorker, middleX - buttonWidth / 2, middleY + span + 70, buttonWidth, buttonHeight, I18n.format("com.minecolonies.gui.builderHut.recall")));
        buttonList.add(new GuiButton(idBuildBuilding, middleX - buttonWidth / 2, middleY + span + 102, buttonWidth, buttonHeight, I18n.format("com.minecolonies.gui.builderHut.build")));
        buttonList.add(new GuiButton(idRepairBuilding, middleX - buttonWidth / 2, middleY + span + 126, buttonWidth, buttonHeight, I18n.format("com.minecolonies.gui.builderHut.repair")));
    }

    protected void drawGuiForeground()
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
}
