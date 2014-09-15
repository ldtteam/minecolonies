package com.minecolonies.client.gui;

import com.minecolonies.MineColonies;
import com.minecolonies.colony.buildings.BuildingBuilder;
import com.minecolonies.network.messages.OpenInventoryMessage;
import com.minecolonies.util.LanguageHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class GuiHutBuilder extends GuiBase
{
    private final int BUTTON_INVENTORY = 4;
    private BuildingBuilder.View builderHut;
    private final int span = 10;

    public GuiHutBuilder(BuildingBuilder.View building, EntityPlayer player, World world, int x, int y, int z)
    {
        super(player, world, x, y, z, building);
        this.builderHut = building;
    }

    @Override
    protected void addElements()
    {
        super.addElements();

        addDefaultWorkerLayout(LanguageHandler.format("com.minecolonies.gui.workerHuts.buildersHut"), "John R. Jones", "xx (yy)", "xxxxxxxx", span);
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
