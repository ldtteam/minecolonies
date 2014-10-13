package com.minecolonies.client.gui;

import com.minecolonies.MineColonies;
import com.minecolonies.colony.CitizenData;
import com.minecolonies.colony.buildings.BuildingBuilder;
import com.minecolonies.network.messages.OpenInventoryMessage;
import com.minecolonies.util.LanguageHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class GuiHutBuilder extends GuiBuilding
{
    private final int BUTTON_INVENTORY = 4;
    private BuildingBuilder.View builderHut;
    private final int span = 10;

    public GuiHutBuilder(BuildingBuilder.View building)
    {
        super(building);
        this.builderHut = building;
    }

    @Override
    protected void addElements()
    {
        String workerName = "";
        String workerLevel = "";

        if (builderHut.getWorkerId() != null)
        {
            CitizenData.View worker = builderHut.getColony().getCitizen(builderHut.getWorkerId());
            if (worker != null)
            {
                workerName = worker.getName();
                workerLevel = String.format("%d", worker.getLevel());
            }
        }

        super.addElements();

        addDefaultWorkerLayout(LanguageHandler.format("com.minecolonies.gui.workerHuts.buildersHut"), workerName, workerLevel, "xxxxxxxx", span);
        addBottomButton(BUTTON_INVENTORY, LanguageHandler.format("container.inventory"), buttonMiddleX, buttonWidth, buttonHeight);
    }

    @Override
    protected void actionPerformed(GuiButton guiButton)
    {
        super.actionPerformed(guiButton);

        switch(guiButton.id)
        {
            case BUTTON_INVENTORY:
                MineColonies.network.sendToServer(new OpenInventoryMessage(builderHut));
                break;
        }
    }
}
