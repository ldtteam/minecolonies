package com.minecolonies.blocks;

import com.minecolonies.tileentities.TileEntityHutStonemason;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockHutStonemason extends BlockHut
{
    protected BlockHutStonemason()
    {
        super();
    }

    @Override
    public String getName()
    {
        return "blockHutStonemason";
    }

    @Override
    public TileEntity createOldMineColoniesTileEntity(World var1, int var2)
    {
        return new TileEntityHutStonemason();
    }
}