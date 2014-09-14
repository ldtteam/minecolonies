package com.minecolonies.blocks;

import com.minecolonies.configuration.Configurations;
import com.minecolonies.tileentities.TileEntityTownHall;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockHutTownHall extends BlockHut
{
    protected BlockHutTownHall()
    {
        super();
        this.workingRange = Configurations.workingRangeTownhall;
    }

    @Override
    public String getName()
    {
        return "blockHutTownhall";
    }

    @Override
    public TileEntity createOldMineColoniesTileEntity(World world, int meta)
    {
        return new TileEntityTownHall();
    }
}
