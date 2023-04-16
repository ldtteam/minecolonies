package com.minecolonies.api.entity.citizen.happiness;

import net.minecraft.nbt.CompoundTag;

/**
 * Interface describing possible happiness factors.
 */
public interface IHappinessModifier
{
    /**
     * Get the unique happiness id.
     *
     * @return the string id.
     */
    String getId();

    /**
     * get the factor of the happiness. value between 0 and 1 if negative. value above 1 if positive.
     *
     * @return the value of the factor.
     */
    double getFactor();

    /**
     * Get the weight of the happiness.
     *
     * @return the weight.
     */
    double getWeight();

    /**
     * Read the modifier from nbt.
     *
     * @param compoundNBT the compound to read it from.
     */
    void read(final CompoundTag compoundNBT);

    /**
     * Write it to NBT.
     *
     * @param compoundNBT the compound to write it to.
     */
    void write(final CompoundTag compoundNBT);

    /**
     * Called at the end of each day.
     */
    void dayEnd();

    /**
     * Reset the modifier.
     */
    void reset();

    /**
     * Get the days this is active.
     *
     * @return the days.
     */
    int getDays();

    /**
     * Method to set the values at a later day.
     * @param qty the supplier value.
     * @param days the period.
     */
    void setModifier(int qty, int days);
}
