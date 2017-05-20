package com.minecolonies.coremod.colony;

import com.google.common.collect.ImmutableMap;
import com.minecolonies.coremod.colony.buildings.IBuilding;
import com.minecolonies.coremod.colony.permissions.IPermissions;
import com.minecolonies.coremod.colony.requestsystem.IRequestManager;
import com.minecolonies.coremod.colony.requestsystem.factory.IFactoryController;
import net.minecraft.stats.Achievement;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Interface of the Colony and ColonyView which will have to implement the
 * following methods.
 */
public interface IColony
{

    /**
     * Returns the position of the colony.
     *
     * @return pos of the colony.
     */
    BlockPos getCenter();

    /**
     * Returns the name of the colony.
     *
     * @return Name of the colony.
     */
    String getName();

    /**
     * Returns the permissions of the colony.
     *
     * @return {@link IPermissions} of the colony.
     */
    IPermissions getPermissions();

    /**
     * Determine if a given chunk coordinate is considered to be within the
     * colony's bounds.
     *
     * @param w   World to check.
     * @param pos Block Position.
     * @return True if inside colony, otherwise false.
     */
    boolean isCoordInColony(World w, BlockPos pos);

    /**
     * Returns the squared (x, z) distance to the center.
     *
     * @param pos Block Position.
     * @return Squared distance to the center in (x, z) direction.
     */
    long getDistanceSquared(BlockPos pos);

    /**
     * Returns whether or not the colony has a town hall.
     *
     * @return whether or not the colony has a town hall.
     */
    boolean hasTownHall();

    /**
     * returns this colonies unique id.
     *
     * @return an int representing the id.
     */
    int getID();

    /**
     * Increment the statistic amount and trigger achievement.
     *
     * @param statistic the statistic.
     */
    void incrementStatistic(@NotNull String statistic);

    /**
     * Get the Work Manager for the Colony.
     *
     * @return WorkManager for the Colony.
     */
    @NotNull
    IWorkManager getWorkManager();

    /**
     * returns the World the colony is in.
     *
     * @return the World the colony is in.
     */
    @Nullable
    World getWorld();

    /**
     * Returns the max amount of citizens in the colony.
     *
     * @return Max amount of citizens.
     */
    int getMaxCitizens();

    IBuilding getBuilding(BlockPos pos);

    /**
     * Get citizen by ID.
     *
     * @param citizenId ID of the Citizen.
     * @return CitizenData associated with the ID, or null if it was not found.
     */
    ICitizenData getCitizen(int citizenId);

    List<BlockPos> getDeliverymanRequired();

    /**
     * Performed when a building of this colony finished his upgrade state.
     *
     * @param building The upgraded building.
     * @param level    The new level.
     */
    void onBuildingUpgradeComplete(@NotNull IBuilding building, int level);

    /**
     * Method to get the achievements of this colony.
     *
     * @return The achievements achieved by this colony.
     */
    @NotNull
    List<Achievement> getAchievements();

    /**
     * Returns the buildings in the colony
     *
     * @return The buildings in the colony
     */
    @NotNull
    ImmutableMap<BlockPos, IBuilding> getBuildings();

    /**
     * Returns the request manager for the colony.
     * @return The request manager.
     */
    @NotNull
    IRequestManager getRequestManager();

    /**
     * Method to get the factory controller for a given colony
     *
     * @return The factory controller.
     */
    @NotNull
    IFactoryController getFactoryController();
}
