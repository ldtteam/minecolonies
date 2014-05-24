package com.minecolonies.blocks;

import com.minecolonies.MineColonies;
import com.minecolonies.lib.EnumGUI;
import com.minecolonies.tileentities.TileEntityHutWarehouse;
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
        return new TileEntityHutWarehouse();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int par6, float par7, float par8, float par9)
    {
        entityPlayer.openGui(MineColonies.instance, EnumGUI.WAREHOUSE.getID(), world, x, y, z);
        return true;
    }
}