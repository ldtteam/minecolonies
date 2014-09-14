package com.minecolonies.blocks;

import com.minecolonies.tileentities.TileEntityHutLumberjack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockHutLumberjack extends BlockHut
{
    protected BlockHutLumberjack()
    {
        super();
    }

    @Override
    public String getName()
    {
        return "blockHutLumberjack";
    }

    @Override
    public TileEntity createOldMineColoniesTileEntity(World var1, int var2)
    {
        return new TileEntityHutLumberjack();
    }
}
