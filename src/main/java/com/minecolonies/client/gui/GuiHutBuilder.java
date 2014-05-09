package com.minecolonies.client.gui;

import com.minecolonies.tileentities.TileEntityHutBuilder;
import net.minecraft.client.resources.I18n;

public class GuiHutBuilder extends GuiBase {
    private TileEntityHutBuilder tileEntityHutBuilder;
    private int numberOfButtons = 4;
    private int idFireWorker = 0;
    private int idRecallWorker = 1;
    private int idBuildBuilding = 2;
    private int idRepairBuilding = 3;
    private int buttonWidth = 116;
    private int buttonHeight = 20;
    private int span = 10;

    public GuiHutBuilder(TileEntityHutBuilder tileEntityHutBuilder) {
        super();
        this.tileEntityHutBuilder = tileEntityHutBuilder;
    }

    @Override
    protected void addElements() {
        super.addElements();

        String buildersHut = I18n.format("com.minecolonies.gui.builderHut.buildersHut");
        String workerAssigned = I18n.format("com.minecolonies.gui.builderHut.workerAssigned");
        String worker = "John R. Jones"; //TODO replace with actual workername
        String workerLevel = I18n.format("com.minecolonies.gui.builderHut.workerLevel");
        String level = "xx (yy)"; //TODO replace with actual level
        String buildType = I18n.format("com.minecolonies.gui.builderHut.buildType");
        String bType = "xxxxxxxx"; //TODO replace with actual style

        addButton(idFireWorker, I18n.format("com.minecolonies.gui.builderHut.fire"), middleX - buttonWidth / 2, middleY + span + 46, buttonWidth, buttonHeight);
        addButton(idRecallWorker, I18n.format("com.minecolonies.gui.builderHut.recall"), middleX - buttonWidth / 2, middleY + span + 70, buttonWidth, buttonHeight);
        addButton(idBuildBuilding, I18n.format("com.minecolonies.gui.builderHut.build"), middleX - buttonWidth / 2, middleY + span + 102, buttonWidth, buttonHeight);
        addButton(idRepairBuilding, I18n.format("com.minecolonies.gui.builderHut.repair"), middleX - buttonWidth / 2, middleY + span + 126, buttonWidth, buttonHeight);

        addLabel(buildersHut, middleX - fontRendererObj.getStringWidth(buildersHut) / 2 + 3, middleY + span);
        addLabel(workerAssigned, middleX - fontRendererObj.getStringWidth(workerAssigned) + 5, middleY + span + 18);
        addLabel(worker, middleX + 10, middleY + span + 18);
        addLabel(workerLevel, middleX - fontRendererObj.getStringWidth(workerLevel) + 5, middleY + span + 28);
        addLabel(level, middleX + 10, middleY + span + 28);

        addLabel(buildType, middleX - fontRendererObj.getStringWidth(buildType) + 5, middleY + span + 160);
        addLabel(bType, middleX + 10, middleY + span + 160);
    }
}
