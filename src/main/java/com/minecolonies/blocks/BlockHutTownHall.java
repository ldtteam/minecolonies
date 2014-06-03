package com.minecolonies.blocks;

import com.minecolonies.MineColonies;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.lib.EnumGUI;
import com.minecolonies.tileentities.TileEntityTownHall;
import com.minecolonies.util.LanguageHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockHutTownHall extends BlockHut
{
    protected BlockHutTownHall()
    {
        super();
        this.workingRange = Configurations.workingRangeTownhall;
    }

    @Override
    public String getName()
    {
        return "blockHutTownhall";
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return new TileEntityTownHall();
    }

    @Override//TODO create a way for this to be in BlockHut
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int par6, float par7, float par8, float par9)
    {
        TileEntityTownHall tileEntityTownHall = (TileEntityTownHall) world.getTileEntity(x, y, z);
        if(tileEntityTownHall == null) return false;

        if(tileEntityTownHall.isPlayerOwner(entityPlayer))
        {
            entityPlayer.openGui(MineColonies.instance, EnumGUI.TOWNHALL.getID(), world, x, y, z);
        }
        else
        {
            LanguageHandler.sendPlayerLocalizedMessage(entityPlayer, "tile.blockHutTownhall.messageNoPermission", tileEntityTownHall.getCityName());
        }
        return true;
    }
}
