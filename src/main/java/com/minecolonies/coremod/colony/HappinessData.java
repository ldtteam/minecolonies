package com.minecolonies.coremod.colony;

/**
 * Datas about the happiness level
 */
public class HappinessData
{
    /**
     * Constant used to assign value
     */
    public static final int INCREASE = 1;
    public static final int STABLE   = 0;
    public static final int DECREASE = -1;
    /**
     * Ratio Guards/Citizens
     */
    private             int guards;
    /**
     * Ratio Houses/Citizens
     */
    private             int housing;
    /**
     * Average saturation for all citizens
     */
    private             int saturation;

    /**
     * Creating a default constructor
     */
    public HappinessData()
    {
        /* Just empty */
    }

    /**
     * Get the Guards/Citizens ratio level
     *
     * @return 1 if great, 0 if normal, -1 if bad
     */
    public int getGuards()
    {
        return guards;
    }

    /**
     * Set the Guards/Citizens ratio level
     * @param guards 1 if great, 0 if normal, -1 if bad
     */
    public void setGuards(final int guards)
    {
        this.guards = guards;
    }

    /**
     * Get the Houses/Citizens ratio level
     * @return 1 if great, 0 if normal, -1 if bad
     */
    public int getHousing()
    {
        return housing;
    }

    /**
     * Set the Houses/Citizens ratio level
     * @param housing 1 if great, 0 if normal, -1 if bad
     */
    public void setHousing(final int housing)
    {
        this.housing = housing;
    }

    /**
     * Get the average saturation level for all citizens in a Colony
     * @return 1 if great, 0 if normal, -1 if bad
     */
    public int getSaturation()
    {
        return saturation;
    }

    /**
     * Set the average saturation level for all citizens in a colony
     * @param saturation 1 if great, 0 if normal, -1 if bad
     */
    public void setSaturation(final int saturation)
    {
        this.saturation = saturation;
    }
}
