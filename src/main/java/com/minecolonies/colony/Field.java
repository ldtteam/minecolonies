package com.minecolonies.colony;

import com.minecolonies.util.BlockPosUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;

public class Field
{

    /**
     * Tag to store the location.
     */
    private static final String TAG_LOCATION = "location";

    /**
     * Tag to store if the field has been taken.
     */
    private static final String TAG_TAKEN = "taken";

    /**
     * The fields location.
     */
    private final BlockPos location;

    /**
     * The colony of the field.
     */
    private final Colony colony;

    /**
     * Has the field be taken by any worker?
     */
    private boolean free = true;

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
        Boolean free = compound.getBoolean(TAG_TAKEN);
        Field field = new Field(colony,pos);
        field.setFree(free);
        return field;
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

    public boolean isFree() {
        return free;
    }

    public void setFree(boolean free) {
        this.free = free;
    }
}
