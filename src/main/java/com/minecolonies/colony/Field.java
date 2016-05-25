package com.minecolonies.colony;

import com.minecolonies.colony.materials.MaterialSystem;
import com.minecolonies.util.BlockPosUtil;
import com.minecolonies.util.Log;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;

public class Field
{
    private static final String TAG_LOCATION = "location";

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

    /**
     * Create and load a Building given it's saved NBTTagCompound
     *
     * @param colony    The owning colony
     * @param compound  The saved data
     * @return          {@link com.minecolonies.colony.buildings.Building} created from the compound.
     */
    public static Field createFromNBT(Colony colony, NBTTagCompound compound)
    {
        BlockPos pos = BlockPosUtil.readFromNBT(compound, TAG_LOCATION);
        return new Field(colony,pos);
    }

    /**
     * Save data to NBT compound.
     * Writes the {@link #location} value.
     *
     * @param compound      {@link net.minecraft.nbt.NBTTagCompound} to write data to
     */
    public void writeToNBT(NBTTagCompound compound)
    {
        BlockPosUtil.writeToNBT(compound, TAG_LOCATION, location);
    }

}
