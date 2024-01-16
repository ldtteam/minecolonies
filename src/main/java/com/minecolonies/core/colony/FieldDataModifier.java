package com.minecolonies.core.colony;

/**
 * class that holds data related to happiness modifier for the citizen with a Farmer job.
 */
public class FieldDataModifier
{
    /**
     * Indicated if the field can be farmed or not.
     */
    private boolean canFarm;

    /**
     * Number of days the fields has been inactiv.e
     */
    private int numberDaysInactive;

    /**
     * Call to set if the field can be farmed or not.
     *
     * @param value boolean to indicate if the field can be farmed
     */
    public void isCanFarm(final boolean value)
    {
        canFarm = value;
        if (value)
        {
            numberDaysInactive = 0;
        }
    }

    /**
     * Call to get indicater if the field can be farmed.
     *
     * @return returns a boolean indicating if a field can be farmed
     */
    public boolean isCanFarm()
    {
        return canFarm;
    }

    /**
     * Call in increase the number of days the field has be inactive. Meaning the farmer can't farm the field.
     */
    public void increaseInactiveDays()
    {
        numberDaysInactive++;
    }

    /**
     * Call to get the number of days the field has be inactive.
     *
     * @return int that return the number of days the fields has be inactive
     */
    public int getInactiveDays()
    {
        return numberDaysInactive;
    }

    /**
     * Call to set the number of days the field has be inactive.
     *
     * @param days value to set the number of inactive days for field too.
     */
    public void setInactiveDays(final int days)
    {
        numberDaysInactive = days;
    }
}
