package com.minecolonies.api.colony;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.Constants.NBT;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Datas about the happiness level
 */

public class HappinessData 
{
    /**
    *  TAGS for string pointer for the NBT field data.
    */
    private static final String TAG_TASKS = "happiness";
    private static final String TAG_GUARDS = "guards";
    private static final String TAG_HOUSING = "housing";
    private static final String TAG_SATURATION = "saturation";
    private static final String TAG_HOUSING_MODIFIER = "housingRatioModifier";
    private static final String TAG_DEATH_MODIFIER = "deathModifier";
    private static final String TAG_TOTAL_DEATH_MODIFIER = "totalDeathModifier";
    private static final String TAG_DEATH_NUMOFDAYS = "numOfDays";
    private static final String TAG_DEATH_NUMOFDAYS_LAST = "numOfDaysLast";
    private static final String TAG_DEATH_MODIFIER_VALUE = "modifier";
    private static final String TAG_DEATH_ADJUSTMENT_MODIFIER = "adjustmentModifier";

    /**
    * The max number of days a death adjusted the modifier
    */
    public static final int MAX_DAYS_DEATH_MODIFIER_LAST = 20;

    /**
     * The max number of days a death of a guard adjusts the modifier
     */
    public static final int MAX_DAYS_DEATH_MODIFIER_LAST_GUARDS = 5;

    /**
     * Max happiness for the colony
     */
    public static final int MAX_HAPPINESS = 10;

    /**
     * Min happiness for the colony
     */
    public static final int MIN_HAPPINESS = 1;

    /**
     * Max modifier amount for total deaths in colony.
     * Raids can cause many loses and colony could be 1.
     */
    public static final int MAX_DEATH_MODIFIER = 6;

    /**
     * Constant used to assign value
     */
    public static final int INCREASE = 1;
    public static final int STABLE   = 0;
    public static final int DECREASE = -1;


    /**
     * Ratio Guards/Citizens
     */
    private int guards;
    /**
     * Ratio Houses/Citizens
     */
    private int housing;
    /**
     * Average saturation for all citizens
     */
    private int saturation;

    /**
     * Indicates the housing ratio modifier.
     */
    private double housingRatioModifier;

    /**
     * Array of all the deaths that occur in the colony
     * for the alst 30 days.
     */
    private List<DeathModifierData> deathModifier = new ArrayList();

    /**
     * Get the Guards/Citizens ratio level.
     *
     * @return 1 if great, 0 if normal, -1 if bad
     */
    public int getGuards()
    {
        return guards;
    }

    /**
     * Set the Guards/Citizens ratio level.
     *
     * @param guards 1 if great, 0 if normal, -1 if bad
     */
    public void setGuards(final int guards)
    {
        this.guards = guards;
    }

    /**
     * Get the Houses/Citizens ratio level.
     *
     * @return 1 if great, 0 if normal, -1 if bad
     */
    public int getHousing()
    {
        return housing;
    }

    /**
     * Set the Houses/Citizens ratio level.
     *
     * @param housing 1 if great, 0 if normal, -1 if bad
     */
    public void setHousing(final int housing)
    {
        this.housing = housing;
    }

    /**
     * Get the average saturation level for all citizens in a Colony.
     *
     * @return 1 if great, 0 if normal, -1 if bad
     */
    public int getSaturation()
    {
        return saturation;
    }

    /**
     * Set the average saturation level for all citizens in a colony.
     *
     * @param saturation 1 if great, 0 if normal, -1 if bad
     */
    public void setSaturation(final int saturation)
    {
        this.saturation = saturation;
    }

    /**
     * {@inheritDoc}
     * @param byteBuf
     */
    public void fromBytes(final ByteBuf byteBuf)
    {
        this.guards = byteBuf.readInt();
        this.housing = byteBuf.readInt();
        this.saturation = byteBuf.readInt();
        this.housingRatioModifier = byteBuf.readDouble();


        final int numDeaths = byteBuf.readInt();
        for (int index = 0; index < numDeaths; index++)
        {
            final int numDays = byteBuf.readInt();
            final double modifier = byteBuf.readDouble();
            final int numDayslast = byteBuf.readInt();
            final double adjustment = byteBuf.readDouble();
            final DeathModifierData data = new DeathModifierData(numDays, modifier, numDayslast);
            data.setAdjustment(adjustment);
            deathModifier.add(data);
        }
    }

    /**
     * {@inheritDoc}
     * @param byteBuf
     */
    public void toBytes(final ByteBuf byteBuf)
    {
        byteBuf.writeInt(guards);
        byteBuf.writeInt(housing);
        byteBuf.writeInt(saturation);
        byteBuf.writeDouble(housingRatioModifier);

        byteBuf.writeInt(deathModifier.size());
        for (int index = 0; index < deathModifier.size(); index++)
        {
            byteBuf.writeInt(deathModifier.get(index).getDaysSinceEvent());
            byteBuf.writeDouble(deathModifier.get(index).getModifier());
            byteBuf.writeInt(deathModifier.get(index).getNumDaysLast());
            byteBuf.writeDouble(deathModifier.get(index).getAdjustmentModifier());
        }
    }

    /**
    * Reads in Happiness data from the NBT file.
    *
    * @param compound pointer to NBT fields
    */
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        final NBTTagList happinessTagList = compound.getTagList(TAG_TASKS, NBT.TAG_COMPOUND);
        final NBTTagCompound tagCompound = happinessTagList.getCompoundTagAt(0);

        guards = tagCompound.getInteger(TAG_GUARDS);
        housing = tagCompound.getInteger(TAG_HOUSING);
        saturation = tagCompound.getInteger(TAG_SATURATION);
        housingRatioModifier = tagCompound.getDouble(TAG_HOUSING_MODIFIER);

        final int numDeaths = tagCompound.getInteger(TAG_TOTAL_DEATH_MODIFIER);
        final NBTTagList deathTagList = tagCompound.getTagList(TAG_DEATH_MODIFIER, Constants.NBT.TAG_COMPOUND);
        deathModifier.clear();
        for (int i = 0; i < numDeaths; ++i)
        {
            final NBTTagCompound deathCompound = deathTagList.getCompoundTagAt(i);
            final int numDays = deathCompound.getInteger(TAG_DEATH_NUMOFDAYS);
            final double value = deathCompound.getDouble(TAG_DEATH_MODIFIER_VALUE);
            final int numDaysLast = deathCompound.getInteger(TAG_DEATH_NUMOFDAYS_LAST);
            final double adjustment = deathCompound.getDouble(TAG_DEATH_ADJUSTMENT_MODIFIER);
            final DeathModifierData data = new DeathModifierData(numDays, value, numDaysLast);
            data.setAdjustment(adjustment);
            deathModifier.add(data);
        }
    }


    /**
     * Store the level to nbt.
     *
     * @param compound compound to use.
     */
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        @NotNull final NBTTagList tasksTagList = new NBTTagList();
        @NotNull final NBTTagCompound taskCompound = new NBTTagCompound();
        taskCompound.setInteger(TAG_GUARDS, guards);
        taskCompound.setInteger(TAG_HOUSING, housing);
        taskCompound.setInteger(TAG_SATURATION, saturation);
        taskCompound.setDouble(TAG_HOUSING_MODIFIER, housingRatioModifier);

        taskCompound.setInteger(TAG_TOTAL_DEATH_MODIFIER, deathModifier.size());
        @NotNull final NBTTagList deathTagList = new NBTTagList();
        for (int i = 0; i < deathModifier.size(); i++)
        {
            @NotNull final NBTTagCompound deathCompound = new NBTTagCompound();
        	final DeathModifierData data = deathModifier.get(i);
        	deathCompound.setInteger(TAG_DEATH_NUMOFDAYS, data.getDaysSinceEvent());
            deathCompound.setDouble(TAG_DEATH_MODIFIER_VALUE, data.getModifier());
            deathCompound.setInteger(TAG_DEATH_NUMOFDAYS_LAST, data.getNumDaysLast());
            deathCompound.setDouble(TAG_DEATH_ADJUSTMENT_MODIFIER, data.getAdjustmentModifier());
            deathTagList.appendTag(deathCompound);
        }
        taskCompound.setTag(TAG_DEATH_MODIFIER, deathTagList);

        tasksTagList.appendTag(taskCompound);
        compound.setTag(TAG_TASKS, tasksTagList);
    }

    /**
     * Copying the values of the happinessdata into this object
     * @param data The values to copy
     */
    public void setValues(final HappinessData data)
    {
        this.guards = data.guards;
        this.saturation = data.saturation;
        this.housing = data.housing;

        this.deathModifier = data.deathModifier;
        this.housingRatioModifier = data.housingRatioModifier;
    }



    /**
     * Call when a citizen dies in the colony.  It will add the citizen death
     * to the death modifier for the next so many days.
     */
    public void setDeathModifier(final double modifier, final boolean isGuard)
    {
        int numDays = MAX_DAYS_DEATH_MODIFIER_LAST;
        if (isGuard)
        {
            numDays = MAX_DAYS_DEATH_MODIFIER_LAST_GUARDS;
        }
    	final DeathModifierData data = new DeathModifierData(0, modifier, numDays);
    	deathModifier.add(data);
    }

    /**
     * Call to retrieve the death modifier for the entire colony.
     *
     * @return returns the Death modifier
     */
    public double getDeathModifier()
    {
        double value = 0;
        for (int index = 0; index < deathModifier.size(); index++)
        {
            value += deathModifier.get(index).getModifier();
    	}

        if (value > MAX_DEATH_MODIFIER)
        {
            value = MAX_DEATH_MODIFIER;
        }

        return value;
    }


    /**
     * Called to set the housing Ratio Modifier.
     *
     * @param modifier the amount to add to the housing Raio Modifier for
     * the entire Colony.
     */
    public void setHousingModifier(final double modifier)
    {
        housingRatioModifier = modifier;
    }

    /**
     * Call to get the total Housing modifier for the Entire Colony.
     *
     * @return housing modifier total
     */
    public double getHousingModifier()
    {
        return housingRatioModifier;
    }

    /**
     * called once a day to process death modifiers.
     * After a period of time a citizen death is removed from the modifier
     *
     */
    public void processDeathModifiers()
    {
        for (int index = 0; index < deathModifier.size(); index++)
        {
            final DeathModifierData data = deathModifier.get(index);
            data.increaseDay();
            if (data.getDaysSinceEvent() > data.getNumDaysLast())
            {
                deathModifier.remove(index);
                index--;
            }
        }

    }


    public double getTotalHappinessModifier()
    {
        return housingRatioModifier + getDeathModifier();
    }


    /**
     * Class that holds the data related to a citizens death.
     *
     */
    public class DeathModifierData
    {
        /**
         * Number of days since the citizens death
         */
        private int daysSinceEvent;
        /**
         * modifier amount for the citizens death
         */
        private double modifier;
        
        final private int numDaysLast;

        private double adjustment;
        
        /**
         * @param daysSinceEvent
         *            number of days since the citizens death
         * @param modifier
         *            value of the modifier for the citizens death
         */
        public DeathModifierData(final int daysSinceEvent, final double modifier, final int numDaysLast)
        {
            this.daysSinceEvent = daysSinceEvent;
            this.modifier = modifier;
            this.numDaysLast = numDaysLast;
            this.adjustment = modifier / numDaysLast;
        }

        /**
         * @return number of days since the citizens death
         */
        public int getDaysSinceEvent()
        {
            return daysSinceEvent;
        }

        public double getAdjustmentModifier()
        {
            return adjustment;
        }

        public void setAdjustment(final double adjustment)
        {
            this.adjustment = adjustment;
        }

        public void setModifier(final double modifier)
        {
            this.modifier =modifier;
        }

        /**
         * @return the modifier amount for the citizens death
         */
        public double getModifier()
        {
            return modifier;
        }

        public int getNumDaysLast()
        {
            return numDaysLast;
        }

        /**
         * Increase the number of days since the citizens death and
         * update the modifier to reflect the current days modifier
         */
        public void increaseDay()
        {
            daysSinceEvent++;
            modifier -= adjustment;
        }
    }

}
