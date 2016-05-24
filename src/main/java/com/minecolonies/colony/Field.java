package com.minecolonies.colony;

import net.minecraft.util.BlockPos;

public class Field
{
    private         final       BlockPos                    location;
    private         final       Colony                      colony;


    public Field(Colony colony, BlockPos location)
    {
        this.location = location;
        this.colony   = colony;
    }

    /**
     * Returns the {@link BlockPos} of the current object, also used as ID
     *
     * @return          {@link BlockPos} of the current object
     */
    public BlockPos getID()
    {
        return location; //  Location doubles as ID
    }

    /**
     * Returns the colony of the field
     *
     * @return          {@link com.minecolonies.colony.Colony} of the current object
     */
    public Colony getColony()
    {
        return colony;
    }

}
