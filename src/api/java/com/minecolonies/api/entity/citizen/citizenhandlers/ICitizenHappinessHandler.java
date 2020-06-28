package com.minecolonies.api.entity.citizen.citizenhandlers;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.entity.citizen.happiness.IHappinessModifier;
import net.minecraft.nbt.CompoundNBT;

import java.util.List;

/**
 * The citizen happiness handler interface.
 */
public interface ICitizenHappinessHandler
{
    /**
     * Reset a modifier.
     * @param name the name of the modifier.
     */
    void resetModifier(final String name);

    /**
     * Get a modifier by the name.
     * @param name the name of the modifier to return.
     * @return the modifier.
     */
    IHappinessModifier getModifier(String name);

    /**
     * Process the happiness factors daily.
     * @param citizenData the citizen to process it for.
     */
    void processDailyHappiness(final ICitizenData citizenData);

    /**
     * Get the computed happiness of the citizen.
     * @return the happiness.
     */
    double getHappiness();

    /**
     * Read the handler from NBT.
     * @param compound the compound to read it from.
     */
    void read(CompoundNBT compound);

    /**
     * Write the handler to NBT.
     * @param compound the compound to write it to.
     */
    void write(CompoundNBT compound);

    /**
     * Get a list of all modifiers.
     * @return the list.
     */
    List<String> getModifiers();
}
