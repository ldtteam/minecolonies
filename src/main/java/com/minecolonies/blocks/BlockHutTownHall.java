package com.minecolonies.blocks;

import com.minecolonies.configuration.Configurations;

/**
 * Hut for the town hall.
 * Sets the working range for the town hall in the constructor
 */
public class BlockHutTownHall extends AbstractBlockHut
{
    protected BlockHutTownHall()
    {
        super();
        //Sets the working range to whatever the config is set to
        this.workingRange = Configurations.workingRangeTownHall;
    }

    @Override
    public String getName()
    {
        return "blockHutTownHall";
    }
}
