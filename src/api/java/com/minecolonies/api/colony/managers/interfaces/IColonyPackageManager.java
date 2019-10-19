package com.minecolonies.api.colony.managers.interfaces;

import net.minecraft.entity.player.EntityPlayerMP;
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
    Set<EntityPlayerMP> getSubscribers();

    /**
     * Update Subscribers with Colony, Citizen, and AbstractBuilding Views.
     */
    void updateSubscribers();

    /**
     * Update the colony view.
     */
    void sendColonyViewPackets();

    /**
     * Sends packages to update the permissions.
     */
    void sendPermissionsPackets();


    /**
     * Sends packages to update the workOrders.
     */
    void sendWorkOrderPackets();


    /**
     * Sends packages to update the schematics.
     */
    void sendSchematicsPackets();

    /**
     * Mark the package manager dirty.
     */
    void setDirty();

    /**
     * Add a new subsriber to the colony.
     * @param subscriber the subscriber to add.
     */
    void addSubscribers(@NotNull final EntityPlayerMP subscriber);

    void addOfficerSubscriber(@NotNull EntityPlayerMP subscriber);

    void removeOfficerSubscriber(@NotNull EntityPlayerMP subscriber);

    /**
     * Remove an old subsriber from the colony.
     * @param player the subscriber to remove.
     */
    void removeSubscriber(@NotNull final EntityPlayerMP player);

    Set<EntityPlayerMP> getOfficerSubscribers();
}
