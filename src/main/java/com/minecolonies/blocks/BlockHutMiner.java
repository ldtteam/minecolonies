package com.minecolonies.blocks;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockHutMiner extends BlockInformator
{
    public final String name = "blockHutMiner";

    protected BlockHutMiner()
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
}
