package com.minecolonies.client.gui;

import com.minecolonies.tileentities.TileEntityHutBuilder;
import com.minecolonies.util.LanguageHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class GuiHutBuilder extends GuiBase
{
    private TileEntityHutBuilder tileEntity;
    private final int span = 10;

    public GuiHutBuilder(TileEntityHutBuilder tileEntityHutBuilder, EntityPlayer player, World world, int x, int y, int z)
    {
        super(player, world, x, y, z);
        this.tileEntity = tileEntityHutBuilder;
    }

    @Override
    protected void addElements()
    {
        super.addElements();

        addDefaultWorkerLayout(LanguageHandler.format("com.minecolonies.gui.workerHuts.buildersHut"), "John R. Jones", "xx (yy)", "xxxxxxxx", span);
    }

    @Override
    protected void actionPerformed(GuiButton guiButton)
    {
        super.actionPerformed(guiButton);
    }
}
