package com.minecolonies.api.colony;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.StringNBT;
import org.jetbrains.annotations.Nullable;

/**
 * Container for all the grave data
 */
public class GraveData implements IGraveData
{
    /**
     * NBTTag to store the last grave owner.
     */
    private static final String TAG_CITIZEN_NBT = "citizennbt";

    /**
     * NBTTag to store the name of the last grave owner name.
     */
    private static final String TAG_CITIZEN_NAME = "citizenname";

    /**
     * NBTTag to store the name of the last grave owner job name.
     */
    private static final String TAG_CITIZEN_JOB_NAME = "citizenjobname";

    /**
     * The data NBT of the citizen .
     */
    @Nullable
    private CompoundNBT citizenDataNBT = null;

    /**
     * The name of the citizen.
     */
    @Nullable
    private String citizenName = null;

    /**
     * The name of the job of the citizen
     */
    @Nullable
    private String citizenJobName = null;

    /**
     * get the data NBT of the citizen .
     */
    @Nullable
    public CompoundNBT getCitizenDataNBT()
    {
        return citizenDataNBT;
    }

    /**
     * Set data NBT of the citizen .
     */
    public void setCitizenDataNBT(@Nullable final CompoundNBT citizenDataNBT)
    {
        this.citizenDataNBT = citizenDataNBT;
    }

    /**
     * Get the name of the citizen.
     */
    @Nullable
    public String getCitizenName()
    {
        return citizenName;
    }

    /**
     * Set the name of the citizen.
     */
    public void setCitizenName(@Nullable final String citizenName)
    {
        this.citizenName = citizenName;
    }

    /**
     * Get the name of the job of the citizen
     */
    @Nullable
    public String getCitizenJobName()
    {
        return citizenJobName;
    }

    /**
     * Set the name of the job of the citizen
     */
    public void setCitizenJobName(@Nullable final String citizenJobName)
    {
        this.citizenJobName = citizenJobName;
    }

    @Override
    public void read(CompoundNBT compound)
    {
        citizenDataNBT  = compound.getAllKeys().contains(TAG_CITIZEN_NBT) ? compound.getCompound(TAG_CITIZEN_NBT) : null;
        citizenName     = compound.getAllKeys().contains(TAG_CITIZEN_NAME) ? compound.getString(TAG_CITIZEN_NAME) : null;
        citizenJobName  = compound.getAllKeys().contains(TAG_CITIZEN_JOB_NAME) ? compound.getString(TAG_CITIZEN_JOB_NAME) : null;
    }

    @Override
    public CompoundNBT write()
    {
        final CompoundNBT compound = new CompoundNBT();
        if (citizenDataNBT != null) { compound.put(TAG_CITIZEN_NBT, citizenDataNBT); }
        if (citizenName != null)    { compound.put(TAG_CITIZEN_NAME, StringNBT.valueOf(citizenName)); }
        if (citizenJobName != null) { compound.put(TAG_CITIZEN_JOB_NAME,StringNBT.valueOf(citizenJobName)); }

        return compound;
    }
}
