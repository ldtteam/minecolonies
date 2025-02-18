package com.minecolonies.api.colony.managers.interfaces;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.entity.citizen.happiness.IHappinessModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The interface of the citizen manager.
 */
public interface ICitizenManager extends IEntityManager
{

    /**
     * Spawn a brand new Citizen.
     */
    void spawnOrCreateCitizen();

    /**
     * Spawn a citizen with specific citizen data.
     *
     * @param data  Data to use to spawn citizen.
     * @param world the world to spawn it in.
     * @return the citizen data of the spawned citizen.
     */
    default ICitizenData spawnOrCreateCitizen(final ICitizenData data, @NotNull final Level world)
    {
        return this.spawnOrCreateCivilian(data, world, null, false);
    }

    /**
     * Spawn a citizen with specific citizen data.
     *
     * @param data     Data to use to spawn citizen.
     * @param world    the world to spawn it in.
     * @param spawnPos the Blockposition to spawn at
     * @return the new citizen.
     */
    default ICitizenData spawnOrCreateCitizen(final ICitizenData data, @NotNull final Level world, final BlockPos spawnPos)
    {
        return this.spawnOrCreateCivilian(data, world, spawnPos, false);
    }

    /**
     * Get the first unemployed citizen.
     *
     * @return Citizen with no current job.
     */
    @Nullable
    ICitizenData getJoblessCitizen();

    /**
     * Recalculates how many citizen can be in the colony.
     */
    void calculateMaxCitizens();

    @Override
    ICitizenData createAndRegisterCivilianData();

    /**
     * Resurrect a citizen from its saved NBT.
     *
     * @param compoundNBT the saved citizen NBT
     * @param resetId if true, will calculate a new citizen ID
     * @param world
     * @param spawnPos position where to resurrect the citizen
     * @return the citizenData of the resurrected citizen
     */
    ICitizenData resurrectCivilianData(@NotNull final CompoundTag compoundNBT, final boolean resetId, @NotNull final Level world, final BlockPos spawnPos);

    /**
     * Get all citizens.
     *
     * @return a copy of the list of citizens.
     */
    List<ICitizenData> getCitizens();

    /**
     * Get max citizens of the colony.
     *
     * @return the amount.
     */
    int getMaxCitizens();

    /**
     * Get potential max citizens of the colony. The potential considers all available beds including not assigned guard towers.
     *
     * @return the amount.
     */
    int getPotentialMaxCitizens();

    /**
     * Get the max citizens based on the research.
     *
     * @return the max.
     */
    double maxCitizensFromResearch();

    /**
     * Get the current amount of citizens, might be bigger then {@link #getMaxCitizens()}
     *
     * @return The current amount of citizens in the colony.
     */
    int getCurrentCitizenCount();

    /**
     * Set the new max citizens.
     *
     * @param newMaxCitizens the amount to set.
     */
    void setMaxCitizens(final int newMaxCitizens);

    /**
     * Set the new potential max citizens. The potential considers all available beds including not assigned guard towers.
     *
     * @param newMaxCitizens the potential amount to set.
     */
    void setPotentialMaxCitizens(final int newMaxCitizens);

    /**
     * Check for the citizen happiness and update the colony happiness with it.
     */
    void checkCitizensForHappiness();

    /**
     * Tick the citizen data of all active citizens.
     */
    boolean tickCitizenData(int tickRate);

    /**
     * Call this to set all the citizens in the colony to mourn or not.
     *
     * @param mourn boolean to indicate if citizen should mourn or not
     */
    void updateCitizenMourn(final ICitizenData data, final boolean mourn);

    /**
     * Call this to set all citizens asleep
     *
     * @param sleep boolean to indicate whether the citizens are all asleep
     */
    void updateCitizenSleep(final boolean sleep);

    /**
     * Get a random citizen.
     *
     * @return the random citizen.
     */
    ICitizenData getRandomCitizen();

    /**
     * Inject a modifier into all citizens
     *
     * @param modifier the modifier.
     */
    void injectModifier(final IHappinessModifier modifier);

    /**
     * Call this when citizens sleep
     */
    void onCitizenSleep();

    @Override
    ICitizenData getCivilian(final int citizenId);

    /**
     * Called in the morning.
     */
    void onWakeUp();

    /**
     * Post building load actions
     */
    void afterBuildingLoad();
}
