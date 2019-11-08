package com.minecolonies.api.colony.managers.interfaces;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ICitizenManager
{
    /**
     * Read the citizens from nbt.
     * @param compound the compound to read it from.
     */
    void readFromNBT(@NotNull final NBTTagCompound compound);

    /**
     * Write the citizens to nbt.
     * @param citizenCompound the compound to write it to.
     */
    void writeToNBT(@NotNull final NBTTagCompound citizenCompound);

    /**
     * Sends packages to update the citizens.
     *  @param closeSubscribers    the existing subscribers.
     * @param newSubscribers new subscribers
     */
    void sendPackets(
      @NotNull final Set<EntityPlayerMP> closeSubscribers,
      @NotNull final Set<EntityPlayerMP> newSubscribers);

    /**
     * Spawn a brand new Citizen.
     */
    void spawnOrCreateCitizen();

    /**
     * Returns a map of citizens in the colony.
     * The map has ID as key, and citizen data as value.
     *
     * @return Map of citizens in the colony, with as key the citizen ID, and as
     * value the citizen data.
     */
    @NotNull
    Map<Integer, ICitizenData> getCitizenMap();

    /**
     * Get citizen by ID.
     *
     * @param citizenId ID of the Citizen.
     * @return ICitizenData associated with the ID, or null if it was not found.
     */
    ICitizenData getCitizen(final int citizenId);

    /**
     * Spawn a citizen with specific citizen data.
     *
     * @param data Data to use to spawn citizen.
     * @param world the world to spawn it in.
     */
    default ICitizenData spawnOrCreateCitizen(final ICitizenData data, @NotNull final World world)
    {
        return this.spawnOrCreateCitizen(data, world, null, false);
    }

    /**
     * Spawn a citizen with specific citizen data.
     *
     * @param data     Data to use to spawn citizen.
     * @param world    the world to spawn it in.
     * @param spawnPos the Blockposition to spawn at
     */
    default ICitizenData spawnOrCreateCitizen(final ICitizenData data, @NotNull final World world, final BlockPos spawnPos)
    {
        return this.spawnOrCreateCitizen(data, world, spawnPos, false);
    }

    /**
     * Spawns a citizen with the specific citizen data.
     * @param data Data to use when spawn, null when new generation.
     * @param world THe world.
     * @param force True to skip max citizen test, false when not.
     */
    ICitizenData spawnOrCreateCitizen(final ICitizenData data, @NotNull final World world, final BlockPos spawnPos, final boolean force);

    /**
     * Creates Citizen Data for a new citizen
     *
     * @return new ICitizenData
     */
    ICitizenData createAndRegisterNewCitizenData();

    /**
     * Removes a citizen from the colony.
     *
     * @param citizen Citizen data to remove.
     */
    void removeCitizen(@NotNull final ICitizenData citizen);

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

    /**
     * Marks citizen data dirty.
     */
    void markCitizensDirty();

    /**
     * Clear dirty from all buildings.
     */
    void clearDirty();

    /**
     * Get all citizens.
     * @return a copy of the list of citizens.
     */
    List<ICitizenData> getCitizens();

    /**
     * Get max citizens of the colony.
     * @return the amount.
     */
    int getMaxCitizens();

    /**
     * Get potential max citizens of the colony.
     * The potential considers all available beds including not assigned guard towers.
     * @return the amount.
     */
    int getPotentialMaxCitizens();

    /**
     * Get the current amount of citizens, might be bigger then {@link #getMaxCitizens()}
     * @return The current amount of citizens in the colony.
     */
    int getCurrentCitizenCount();

    /**
     * Set the new max citizens.
     * @param newMaxCitizens the amount to set.
     */
    void setMaxCitizens(final int newMaxCitizens);

    /**
     * Set the new potential max citizens.
     * The potential considers all available beds including not assigned guard towers.
     * @param newMaxCitizens the potential amount to set.
     */
    void setPotentialMaxCitizens(final int newMaxCitizens);

    /**
     * Check for the citizen happiness and update the colony happiness with it.
     */
    void checkCitizensForHappiness();

    /**
     * Actions to execute on a colony tick.
     * @param colony the event.
     */
    void onColonyTick(final IColony colony);

    /**
     * Call this to set all the citizens in the colony to mourn or not.
     * 
     * @param mourn boolean to indicate if citizen should mourn or not
     */
    void updateCitizenMourn(final boolean mourn);
}
