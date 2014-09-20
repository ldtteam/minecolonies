package com.minecolonies.blocks;

import com.minecolonies.configuration.Configurations;

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
}
