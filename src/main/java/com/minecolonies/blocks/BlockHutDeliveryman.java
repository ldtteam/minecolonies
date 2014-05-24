package com.minecolonies.blocks;

import com.minecolonies.MineColonies;
import com.minecolonies.lib.EnumGUI;
import com.minecolonies.tileentities.TileEntityBlacksmith;
import com.minecolonies.tileentities.TileEntityHutWorker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockHutDeliveryman extends BlockHut
{
    public final String name = "blockHutDeliveryman";

    protected BlockHutDeliveryman()
    {
        super();
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2)
    {
        //TODO
        return null;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int par6, float par7, float par8, float par9)
    {

        TileEntityHutWorker tileEntity = (TileEntityHutWorker) world.getTileEntity(x, y, z);//TODO change to TileEntityWarehouse when that is created
        if(tileEntity != null && tileEntity.isPlayerOwner(entityPlayer))
        {
            entityPlayer.openGui(MineColonies.instance, EnumGUI.WAREHOUSE.getID(), world, x, y, z);
            return true;
        }
        return false;
    }
}