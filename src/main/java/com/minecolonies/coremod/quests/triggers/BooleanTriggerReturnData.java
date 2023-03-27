package com.minecolonies.coremod.quests.triggers;

/**
 * Wrapper around boolean for return data.
 */
public class BooleanTriggerReturnData implements ITriggerReturnData<Boolean>
{
    /**
     * If positive match.
     */
    private final boolean match;

    /**
     * Create a new return data obj.
     * @param match the match to return.
     */
    public BooleanTriggerReturnData(final boolean match)
    {
        this.match = match;
    }

    @Override
    public boolean isPositive()
    {
        return this.match;
    }

    @Override
    public Boolean get()
    {
        return this.match;
    }
}
