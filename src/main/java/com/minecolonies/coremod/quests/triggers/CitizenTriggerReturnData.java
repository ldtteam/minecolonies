package com.minecolonies.coremod.quests.triggers;

import com.minecolonies.api.colony.ICitizenData;

/**
 * Wrapper around a citizen id.
 */
public class CitizenTriggerReturnData implements ITriggerReturnData<ICitizenData>
{
    /**
     * The citizen id or - max int if negative.
     */
    private final ICitizenData match;

    /**
     * Create a new return data obj.
     * @param match citizen id.
     */
    public CitizenTriggerReturnData(final ICitizenData match)
    {
        this.match = match;
    }

    @Override
    public boolean isPositive()
    {
        return this.match != null;
    }

    @Override
    public ICitizenData get()
    {
        return match;
    }
}
