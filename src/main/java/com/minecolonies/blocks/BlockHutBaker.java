package com.minecolonies.blocks;

import com.minecolonies.tileentities.TileEntityHutBaker;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockHutBaker extends BlockHut
{
    protected BlockHutBaker()
    {
        super();
    }

    @Override
    public String getName()
    {
        return "blockHutBaker";
    }

    @Override
    public TileEntity createOldMineColoniesTileEntity(World var1, int var2)
    {
        return new TileEntityHutBaker();
    }
}