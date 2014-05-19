package com.minecolonies.blocks;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockHutLumberjack extends BlockInformator
{
    public final String name = "blockHutLumberjack";

    protected BlockHutLumberjack()
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
