package com.minecolonies.client.gui;

import com.minecolonies.tileentities.TileEntityBuildable;
import com.minecolonies.tileentities.TileEntityHutBuilder;
import com.minecolonies.util.LanguageHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class GuiHutBuilder extends GuiBase
{
    private final EntityPlayer         player;
    private final World                worldObj;
    private final int                  x;
    private final int                  y;
    private final int                  z;
    private       TileEntityHutBuilder tileEntity;
    private int numberOfButtons = 4;
    private int span            = 10;

    public GuiHutBuilder(TileEntityHutBuilder tileEntityHutBuilder, EntityPlayer player, World world, int x, int y, int z)
    {
        super();
        this.player = player;
        this.worldObj = world;
        this.x = x;
        this.y = y;
        this.z = z;
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
        switch(guiButton.id)
        {
            case idHireWorker:
                ((GuiButton) buttonList.get(idFireWorker)).visible = true;
                break;
            case idFireWorker:
                GuiButton hireButton = (GuiButton) buttonList.get(idHireWorker);
                if(hireButton.visible)
                {
                    hireButton.visible = false;
                }
                else
                {
                    guiButton.visible = false;
                    hireButton.visible = true;
                }
                break;
            case idRecallWorker:
                break;
            case idBuildBuilding:
                TileEntityBuildable tileEntityBuildable = (TileEntityBuildable) tileEntity;
                tileEntityBuildable.requestBuilding(player);
                break;
            case idRepairBuilding:
                break;
        }
    }
}
