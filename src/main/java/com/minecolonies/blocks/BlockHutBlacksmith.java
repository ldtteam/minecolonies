package com.minecolonies.blocks;

import com.minecolonies.tileentities.TileEntityBlacksmith;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockHutBlacksmith extends BlockInformator
{
    public final String name = "blockHutBlacksmith";

    protected BlockHutBlacksmith()
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
        return new TileEntityBlacksmith();
    }
}