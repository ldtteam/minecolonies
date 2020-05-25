package com.minecolonies.coremod.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IVisitorData;
import net.minecraft.nbt.CompoundNBT;

public class VisitorData extends CitizenData implements IVisitorData
{
    public static final String TAG_VISIT_TIME = "visitTime";

    /**
     * The time remaining for this entity visit
     */
    private int visitTime = 0;

    /**
     * Create a CitizenData given an ID. Used as a super-constructor or during loading.
     *
     * @param id     ID of the Citizen.
     * @param colony Colony the Citizen belongs to.
     */
    public VisitorData(final int id, final IColony colony)
    {
        super(id, colony);
    }

    /**
     * Gets the visit time
     *
     * @return time in ticks.
     */
    public int getVisitTime()
    {
        return visitTime;
    }

    /**
     * Gets the visit time
     */
    public void setVisitTime(final int visitTime)
    {
        this.visitTime = visitTime;
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        CompoundNBT compoundNBT = super.serializeNBT();
        compoundNBT.putInt(TAG_VISIT_TIME, visitTime);
        return compoundNBT;
    }

    @Override
    public void deserializeNBT(final CompoundNBT nbtTagCompound)
    {
        super.deserializeNBT(nbtTagCompound);
        visitTime = nbtTagCompound.getInt(TAG_VISIT_TIME);
    }
}
