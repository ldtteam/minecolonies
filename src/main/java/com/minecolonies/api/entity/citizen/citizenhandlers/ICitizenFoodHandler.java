package com.minecolonies.api.entity.citizen.citizenhandlers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;

import java.util.Queue;

/**
 * Citizen food handler interface.
 */
public interface ICitizenFoodHandler
{
    /**
     * Food happiness stats.
     * @param diversity number of diverse foods eaten.
     * @param quality number of quality foods eaten.
     */
    record CitizenFoodStats(int diversity, int quality) {}

    /**
     * Add last eaten food item.
     * @param item the last eaten food item.
     */
    void addLastEaten(Item item);

    /**
     * Get the last eaten food item.
     * @return the last eaten item.
     */
    Item getLastEaten();

    /**
     * Check when we last ate a given food item.
     * -1 if not eaten recently.
     * @param item the food item we last ate.
     * @return the index in the list or -1 for not recently or oldest food in queue
     */
    int checkLastEaten(Item item);

    /**
     * Get the food happiness stats
     */
    CitizenFoodStats getFoodHappinessStats();

    /**
     * Read from nbt.
     * @param compound to read it from.
     */
    void read(CompoundTag compound);

    /**
     * Write to nbt.
     * @param compound to write it to.
     */
    void write(CompoundTag compound);

    /**
     * Disease modifier based on the food values.
     * @param baseModifier the modifier to the original disease chance.
     * @return the modifier.
     */
    double getDiseaseModifier(double baseModifier);

    /**
     * If the citizen has a full food history to allow a good analysis.
     * @return true if so.
     */
    boolean hasFullFoodHistory();
}
