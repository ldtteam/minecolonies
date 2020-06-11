package com.minecolonies.api.colony.buildings;

import com.minecolonies.api.colony.buildings.views.MobEntryView;
import com.minecolonies.api.colony.guardtype.GuardType;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.entity.ai.citizen.guards.GuardTask;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface IGuardBuilding extends IBuildingWorker
{
    /**
     * Worker gets this distance times building level away from his/her hut to
     * patrol.
     */
    int PATROL_DISTANCE = 30;

    /**
     * Check if a guard should take damage by a player..
     *
     * @param citizen the citizen.
     * @param player  the player.
     * @return false if in follow mode and following the player.
     */
    static boolean checkIfGuardShouldTakeDamage(final AbstractEntityCitizen citizen, final PlayerEntity player)
    {
        final IBuildingWorker buildingWorker = citizen.getCitizenColonyHandler().getWorkBuilding();
        if (!(buildingWorker instanceof  IGuardBuilding))
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
     * Get the guard's {@link GuardTask}.
     *
     * @return The task of the guard.
     */
    GuardTask getTask();

    /**
     * Set the guard's {@link GuardTask}.
     *
     * @param task The task to set.
     */
    void setTask(GuardTask task);

    /**
     * Returns a patrolTarget to patrol to.
     *
     * @param newTarget whether to search a new target
     * @return the position of the next target.
     */
    @Nullable
    BlockPos getNextPatrolTarget(final boolean newTarget);

    /**
     * Get an Defence bonus related to the building.
     *
     * @return an Integer.
     */
    int getDefenceBonus();

    /**
     * Get an Offence bonus related to the building.
     *
     * @return an Integer.
     */
    int getOffenceBonus();

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
     * Get the guard's {@link GuardType}.
     *
     * @return The job of the guard.
     */
    GuardType getGuardType();

    /**
     * Set the guard's {@link GuardType}.
     *
     * @param job The job to set.
     */
    void setGuardType(GuardType job);

    List<BlockPos> getPatrolTargets();

    /**
     * Get the guard's RetrieveOnLowHeath.
     *
     * @return if so.
     */
    boolean shallRetrieveOnLowHealth();

    /**
     * Set the guard's RetrieveOnLowHealth.
     *
     * @param retrieve true if retrieve.
     */
    void setRetrieveOnLowHealth(boolean retrieve);

    /**
     * Get whether the guard should patrol manually.
     *
     * @return if so.
     */
    boolean shallPatrolManually();

    /**
     * Set whether the guard should patrol manually.
     *
     * @param patrolManually true if manual.
     */
    void setPatrolManually(boolean patrolManually);

    /**
     * Whether the player will assign guards manually or not.
     *
     * @return true if so
     */
    boolean shallAssignManually();

    /**
     * Set whether the player is assigning guards manually.
     *
     * @param assignManually true if so
     */
    void setAssignManually(boolean assignManually);

    /**
     * Returns whether tight grouping in Follow mode is being used.
     *
     * @return whether tight grouping is being used.
     */
    boolean isTightGrouping();

    /**
     * Set whether to use tight grouping or lose grouping.
     *
     * @param tightGrouping - indicates if you are using tight grouping
     */
    void setTightGrouping(boolean tightGrouping);

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
     * Get the Map of mobs to attack.
     *
     * @return the map.
     */
    Map<ResourceLocation, MobEntryView> getMobsToAttack();

    /**
     * Set the Map of mobs to attack.
     *
     * @param list The new map.
     */
    void setMobsToAttack(List<MobEntryView> list);

    /**
     * Entity of player to follow.
     *
     * @return the PlayerEntity reference.
     */
    PlayerEntity getPlayerToFollowOrRally();

    /**
     * Sets the player to follow.
     *
     * @param player the player to follow.
     */
    void setPlayerToFollow(PlayerEntity player);

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
    void setRallyLocation(ILocation location);

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
}
