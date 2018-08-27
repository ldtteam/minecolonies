package com.minecolonies.coremod.colony.managers.interfaces;

import com.minecolonies.coremod.colony.CitizenData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.gameevent.TickEvent;
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
     *  @param oldSubscribers    the existing subscribers.
     * @param hasNewSubscribers the new subscribers.
     * @param subscribers all subscribers
     */
    void sendPackets(
            @NotNull final Set<EntityPlayerMP> oldSubscribers,
            final boolean hasNewSubscribers,
            @NotNull final Set<EntityPlayerMP> subscribers);

    /**
     * Spawn a brand new Citizen.
     */
    void spawnCitizen();

    /**
     * Returns a map of citizens in the colony.
     * The map has ID as key, and citizen data as value.
     *
     * @return Map of citizens in the colony, with as key the citizen ID, and as
     * value the citizen data.
     */
    @NotNull
    Map<Integer, CitizenData> getCitizenMap();

    /**
     * Get citizen by ID.
     *
     * @param citizenId ID of the Citizen.
     * @return CitizenData associated with the ID, or null if it was not found.
     */
    CitizenData getCitizen(final int citizenId);

    /**
     * Spawn a citizen with specific citizen data.
     *
     * @param data Data to use to spawn citizen.
     * @param world the world to spawn it in.
     */
    default void spawnCitizen(final CitizenData data, @NotNull final World world)
    {
        this.spawnCitizen(data, world, false);
    }

    /**
     * Spawns a citizen with the specific citizen data.
     * @param data Data to use when spawn, null when new generation.
     * @param world THe world.
     * @param force True to skip max citizen test, false when not.
     */
    void spawnCitizen(final CitizenData data, @NotNull final World world, final boolean force);

    /**
     * Removes a citizen from the colony.
     *
     * @param citizen Citizen data to remove.
     */
    void removeCitizen(@NotNull final CitizenData citizen);

    /**
     * Get the first unemployed citizen.
     *
     * @return Citizen with no current job.
     */
    @Nullable
    CitizenData getJoblessCitizen();

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
    List<CitizenData> getCitizens();

    /**
     * Get max citizens of the colony.
     * @return the amount.
     */
    int getMaxCitizens();

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
     * Check for the citizen happiness and update the colony happiness with it.
     */
    void checkCitizensForHappiness();

    /**
     * Actions to execute on a specific world tick event.
     * @param event the event.
     */
    void onWorldTick(final TickEvent.WorldTickEvent event);

    /**
     * Call this to set all the citizens in the colony to mourn or not.
     * 
     * @param mourn boolean to indicate if citizen should mourn or not
     */
    void updateCitizenMourn(final boolean mourn);
}
