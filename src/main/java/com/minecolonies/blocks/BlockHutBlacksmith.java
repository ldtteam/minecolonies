package com.minecolonies.blocks;

import com.minecolonies.tileentities.TileEntityHutBlacksmith;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockHutBlacksmith extends BlockHut
{
    protected BlockHutBlacksmith()
    {
        super();
    }

    @Override
    public String getName()
    {
        return "blockHutBlacksmith";
    }

    @Override
    public TileEntity createOldMineColoniesTileEntity(World var1, int var2)
    {
        return new TileEntityHutBlacksmith();
    }
}