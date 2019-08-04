package com.minecolonies.api.colony.managers.interfaces;

import net.minecraft.entity.player.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Colony package manager, responsible to update views etc.
 */
public interface IColonyPackageManager
{
    /**
     * Get the last contact in hours from the colony.
     * @return the integer.
     */
    int getLastContactInHours();

    /**
     * Set the last contact in hours.
     * @param lastContactInHours the number to set.
     */
    void setLastContactInHours(int lastContactInHours);

    /**
     * Get all subscribers.
     * @return a copy of the hashset.
     */
    Set<ServerPlayerEntity> getSubscribers();

    /**
     * Update Subscribers with Colony, Citizen, and AbstractBuilding Views.
     */
    void updateSubscribers();

    /**
     * Update the colony view.
     * @param oldSubscribers the old subs.
     * @param hasNewSubscribers if there are new subs.
     */
    void sendColonyViewPackets(@NotNull final Set<ServerPlayerEntity> oldSubscribers, final boolean hasNewSubscribers);

    /**
     * Sends packages to update the permissions.
     *
     * @param oldSubscribers    the existing subscribers.
     * @param hasNewSubscribers the new subscribers.
     */
    void sendPermissionsPackets(@NotNull final Set<ServerPlayerEntity> oldSubscribers, final boolean hasNewSubscribers);


    /**
     * Sends packages to update the workOrders.
     *
     * @param oldSubscribers    the existing subscribers.
     * @param hasNewSubscribers the new subscribers.
     */
    void sendWorkOrderPackets(@NotNull final Set<ServerPlayerEntity> oldSubscribers, final boolean hasNewSubscribers);


    /**
     * Sends packages to update the schematics.
     *
     * @param hasNewSubscribers the new subscribers.
     */
    void sendSchematicsPackets(final boolean hasNewSubscribers);

    /**
     * Mark the package manager dirty.
     */
    void setDirty();

    /**
     * Add a new subsriber to the colony.
     * @param subscriber the subscriber to add.
     */
    void addSubscribers(@NotNull final ServerPlayerEntity subscriber);

    /**
     * Remove an old subsriber from the colony.
     * @param player the subscriber to remove.
     */
    void removeSubscriber(@NotNull final ServerPlayerEntity player);
}
