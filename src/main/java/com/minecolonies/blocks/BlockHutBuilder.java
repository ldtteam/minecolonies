package com.minecolonies.blocks;

import com.minecolonies.MineColonies;
import com.minecolonies.lib.EnumGUI;
import com.minecolonies.tileentities.TileEntityHutBuilder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockHutBuilder extends BlockHut
{
    protected BlockHutBuilder()
    {
        super();
    }

    @Override
    public String getName()
    {
        return "blockHutBuilder";
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return new TileEntityHutBuilder();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int par6, float par7, float par8, float par9)
    {

        TileEntityHutBuilder tileEntityHutBuilder = (TileEntityHutBuilder) world.getTileEntity(x, y, z);
        if(tileEntityHutBuilder != null && tileEntityHutBuilder.isPlayerOwner(entityPlayer))
        {
            entityPlayer.openGui(MineColonies.instance, EnumGUI.BUILDER.getID(), world, x, y, z);
            return true;
        }
        return false;
    }
}
