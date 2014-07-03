package com.minecolonies.blocks;

import com.minecolonies.tileentities.TileEntityHutMiner;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockHutMiner extends BlockHut
{
    protected BlockHutMiner()
    {
        super();
    }

    @Override
    public String getName()
    {
        return "blockHutMiner";
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2)
    {
        return new TileEntityHutMiner();
    }
}
