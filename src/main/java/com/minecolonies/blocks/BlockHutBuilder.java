package com.minecolonies.blocks;

import com.minecolonies.tileentities.TileEntityHutBuilder;
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
}
