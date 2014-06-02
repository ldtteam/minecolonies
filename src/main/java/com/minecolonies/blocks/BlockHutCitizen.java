package com.minecolonies.blocks;

import com.minecolonies.tileentities.TileEntityHutCitizen;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockHutCitizen extends BlockHut
{
    public BlockHutCitizen()
    {
        super();
    }
    @Override
    public String getName()
    {
        return "blockHutCitizen";
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return new TileEntityHutCitizen();
    }
}
