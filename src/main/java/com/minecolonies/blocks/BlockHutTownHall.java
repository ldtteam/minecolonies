package com.minecolonies.blocks;

import com.minecolonies.configuration.Configurations;

public class BlockHutTownHall extends BlockHut
{
    public BlockHutTownHall()
    {
        super();
        this.workingRange = Configurations.workingRangeTownhall;
    }

    @Override
    public String getName()
    {
        return "blockHutTownHall";
    }
}
