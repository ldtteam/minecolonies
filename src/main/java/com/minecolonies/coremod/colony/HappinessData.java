package com.minecolonies.coremod.colony;

public class HappinessData
{
    public static final int INCREASE = 1;
    public static final int STABLE   = 0;
    public static final int DECREASE = -1;
    // -1 -> Bad/Decrease 0 -> Stable 1 -> Great/Increasing
    private             int guards;
    private             int housing;
    private             int saturation;

    public int getGuards()
    {
        return guards;
    }

    public void setGuards(final int guards)
    {
        this.guards = guards;
    }

    public int getHousing()
    {
        return housing;
    }

    public void setHousing(final int housing)
    {
        this.housing = housing;
    }

    public int getSaturation()
    {
        return saturation;
    }

    public void setSaturation(final int saturation)
    {
        this.saturation = saturation;
    }
}
