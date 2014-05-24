package com.minecolonies.blocks;

import com.minecolonies.tileentities.TileEntityHutStonemason;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockHutStonemason extends BlockHut
{
    public final String name = "blockHutStonemason";

    protected BlockHutStonemason()
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
        return new TileEntityHutStonemason();
    }
}