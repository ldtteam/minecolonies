package com.minecolonies.coremod.colony.managers;

import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
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
     * @param colony the colony to assign them to.
     */
    void readFromNBT(@NotNull final NBTTagCompound compound, @NotNull final Colony colony);

    /**
     * Write the citizens to nbt.
     * @param citizenCompound the compound to write it to.
     */
    void writeToNBT(@NotNull final NBTTagCompound citizenCompound);

    /**
     * Sends packages to update the citizens.
     *
     * @param oldSubscribers    the existing subscribers.
     * @param hasNewSubscribers the new subscribers.
     * @param subscribers all subscribers
     * @param colony the colony of the citizens.
     */
    void sendPackets(@NotNull final Set<EntityPlayerMP> oldSubscribers,
            final boolean hasNewSubscribers,
            @NotNull final Set<EntityPlayerMP> subscribers,
            @NotNull final Colony colony);

    /**
     * Spawn a brand new Citizen.
     */
    void spawnCitizen(@NotNull final Colony colony);

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
     * Spawn a citizen if his entity is null.
     *
     * @param data Data to use to spawn citizen.
     * @param world the world to spawn it in.
     * @param buildingManager the building manager.
     * @param colony the colony.
     */
    void spawnCitizenIfNull(@Nullable final CitizenData data, @Nullable final World world, @NotNull final IBuildingManager buildingManager, @NotNull final Colony colony);

    /**
     * Spawn a citizen with specific citizen data.
     *
     * @param data Data to use to spawn citizen.
     * @param world the world to spawn it in.
     * @param buildingManager the building manager.
     * @param colony the colony.
     */
    void spawnCitizen(@NotNull final CitizenData data, @NotNull final World world, @NotNull final IBuildingManager buildingManager, @NotNull final Colony colony);

    /**
     * Removes a citizen from the colony.
     *
     * @param citizen Citizen data to remove.
     * @param colony the colony.
     */
    void removeCitizen(@NotNull final CitizenData citizen, @NotNull final Colony colony);

    /**
     * Get the first unemployed citizen.
     *
     * @return Citizen with no current job.
     */
    @Nullable
    CitizenData getJoblessCitizen();

    /**
     * Recalculates how many citizen can be in the colony.
     * @param colony the colony.
     */
    void calculateMaxCitizens(@NotNull final Colony colony);

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
     * Set the new max citizens.
     * @param newMaxCitizens the amount to set.
     */
    void setMaxCitizens(final int newMaxCitizens);

    /**
     * Check for the citizen happiness and update the colony happiness with it.
     */
    void checkCitizensForHappiness(final Colony colony);

    /**
     * Actions to execute on a specific world tick event.
     * @param event the event.
     * @param colony the colony to execute it for.
     */
    void onWorldTick(TickEvent.WorldTickEvent event, @NotNull final Colony colony);
}
