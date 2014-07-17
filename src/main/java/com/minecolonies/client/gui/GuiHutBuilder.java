package com.minecolonies.client.gui;

import com.minecolonies.MineColonies;
import com.minecolonies.network.packets.OpenInventoryPacket;
import com.minecolonies.tileentities.TileEntityHutBuilder;
import com.minecolonies.util.LanguageHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class GuiHutBuilder extends GuiBase
{
    private final int BUTTON_INVENTORY = 4;
    private TileEntityHutBuilder builderHut;
    private final int span = 10;

    public GuiHutBuilder(TileEntityHutBuilder tileEntityHutBuilder, EntityPlayer player, World world, int x, int y, int z)
    {
        super(player, world, x, y, z);
        this.builderHut = tileEntityHutBuilder;
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
                MineColonies.packetPipeline.sendToServer(new OpenInventoryPacket(builderHut, builderHut.getName()));
                break;
        }
    }
}
