package com.minecolonies.api.colony.buildings;

import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public interface IGuardBuilding extends IBuilding
{
    /**
     * Worker gets this distance times building level away from his/her hut to patrol.
     */
    int PATROL_DISTANCE = 30;

    /**
     * Check if a guard should take damage by a player..
     *
     * @param citizen the citizen.
     * @param player  the player.
     * @return false if in follow mode and following the player.
     */
    static boolean checkIfGuardShouldTakeDamage(final AbstractEntityCitizen citizen, final Player player)
    {
        final IBuilding buildingWorker = citizen.getCitizenColonyHandler().getWorkBuilding();
        if (!(buildingWorker instanceof IGuardBuilding))
        {
            return true;
        }
        if (player.equals(((IGuardBuilding) buildingWorker).getPlayerToFollowOrRally()))
        {
            return false;
        }
        return true;
    }

    /**
     * Get the guard's task.
     *
     * @return The task of the guard.
     */
    String getTask();

    /**
     * Returns a patrolTarget to patrol to.
     *
     * @param newTarget whether to search a new target
     * @return the position of the next target.
     */
    @Nullable
    BlockPos getNextPatrolTarget(final boolean newTarget);

    /**
     * Called when a guard is at the current patrol point
     *
     * @param guard guard which arrived
     */
    void arrivedAtPatrolPoint(AbstractEntityCitizen guard);

    /**
     * Getter for the patrol distance the guard currently has.
     *
     * @return The distance in whole numbers.
     */
    int getPatrolDistance();

    /**
     * Get the guard's RetrieveOnLowHeath.
     *
     * @return if so.
     */
    boolean shallRetrieveOnLowHealth();
    /**
     * Get whether the guard should patrol manually.
     *
     * @return if so.
     */
    boolean shallPatrolManually();

    /**
     * Returns whether tight grouping in Follow mode is being used.
     *
     * @return whether tight grouping is being used.
     */
    boolean isTightGrouping();

    /**
     * Get the position the guard should guard.
     *
     * @return the {@link BlockPos} of the guard position.
     */
    BlockPos getGuardPos();

    /**
     * Set where the guard should guard.
     *
     * @param guardPos the {@link BlockPos} to guard.
     */
    void setGuardPos(BlockPos guardPos);

    /**
     * Entity of player to follow or rally.
     *
     * @return the PlayerEntity reference.
     */
    Player getPlayerToFollowOrRally();

    /**
     * Sets the player to follow.
     *
     * @param player the player to follow.
     */
    void setPlayerToFollow(Player player);

    /**
     * Location to to rally to.
     *
     * @return the ILocation reference.
     */
    ILocation getRallyLocation();

    /**
     * Sets the location to rally.
     *
     * @param location The location to rally to.
     */
    void setRallyLocation(final ILocation location);

    /**
     * Gets the position to follow.
     *
     * @return the position the guard is supposed to be while following.
     */
    BlockPos getPositionToFollow();

    /**
     * Adds new patrolTargets.
     *
     * @param target the target to add
     */
    void addPatrolTargets(BlockPos target);

    /**
     * Resets the patrolTargets list.
     */
    void resetPatrolTargets();

    /**
     * Get the Vision bonus range for the building level
     *
     * @return an integer for the additional range.
     */
    int getBonusVision();

    /**
     * Populates the mobs list from the ForgeRegistries.
     */
    void calculateMobs();

    /**
     * If we have to calculate a new target manually.
     *
     * @return true if so.
     */
    boolean requiresManualTarget();

    /**
     * Sets a one time consumed temporary next position to patrol towards
     *
     * @param pos Position to set
     */
    void setTempNextPatrolPoint(BlockPos pos);

    /**
     * Get the position of the assigned mine
     *
     * @return the coords of the assigned mine
     */
    BlockPos getMinePos();
}
