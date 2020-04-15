package com.minecolonies.coremod.entity.citizen.happiness;

import net.minecraft.nbt.CompoundNBT;

/**
 * Interface describing possible happiness factors.
 */
public interface IHappinessModifier
{
    /**
     * Get the unique happiness id.
     * @return the string id.
     */
    String getId();

    /**
     * get the factor of the happiness.
     * value between 0 and 1 if negative.
     * value above 1 if positive.
     * @return the value of the factor.
     */
    double getFactor();

    /**
     * Get the weight of the happiness.
     * @return the weight.
     */
    double getWeight();

    /**
     * Read the modifier from nbt.
     * @param compoundNBT the compound to read it from.
     */
    void read(final CompoundNBT compoundNBT);

    /**
     * Write it to NBT.
     * @param compoundNBT the compound to write it to.
     */
    void write(final CompoundNBT compoundNBT);

    /**
     * Called at the end of each day.
     */
    void dayEnd();

    /**
     * Reset the modifier.
     */
    void reset();

    /**
     * Trigger interactions if necessary.
     */
    void triggerInteractions();

    /**
     * Get the lang string of the happiness factor.
     * @return the lang string.
     */
    default String getLangString()
    {
        return "com.minecolonies.coremod.happiness.factor." + getId();
    }
}
