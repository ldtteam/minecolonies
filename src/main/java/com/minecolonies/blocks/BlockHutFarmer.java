package com.minecolonies.blocks;

import com.minecolonies.tileentities.TileEntityHutFarmer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockHutFarmer extends BlockHut
{
    protected BlockHutFarmer()
    {
        super();
    }

    @Override
    public String getName()
    {
        return "blockHutFarmer";
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2)
    {
        return new TileEntityHutFarmer();
    }
}