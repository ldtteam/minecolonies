package com.minecolonies.coremod.colony.managers;

import com.minecolonies.coremod.colony.Colony;
import net.minecraft.entity.player.EntityPlayerMP;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static com.minecolonies.api.util.constant.ColonyConstants.MAX_SQ_DIST_OLD_SUBSCRIBER_UPDATE;
import static com.minecolonies.api.util.constant.ColonyConstants.MAX_SQ_DIST_SUBSCRIBER_UPDATE;
import static com.minecolonies.api.util.constant.Constants.TICKS_HOUR;

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
     * @param colony the colony of the manager.
     */
    void updateSubscribers(final Colony colony);

    /**
     * Update the colony view.
     * @param oldSubscribers the old subs.
     * @param hasNewSubscribers if there are new subs.
     * @param colony the colony of the manager.
     */
    void sendColonyViewPackets(@NotNull final Set<EntityPlayerMP> oldSubscribers, final boolean hasNewSubscribers, final Colony colony);

    /**
     * Sends packages to update the permissions.
     *
     * @param oldSubscribers    the existing subscribers.
     * @param hasNewSubscribers the new subscribers.
     * @param colony the colony of the manager.
     */
    void sendPermissionsPackets(@NotNull final Set<EntityPlayerMP> oldSubscribers, final boolean hasNewSubscribers, final Colony colony);


    /**
     * Sends packages to update the workOrders.
     *
     * @param oldSubscribers    the existing subscribers.
     * @param hasNewSubscribers the new subscribers.
     * @param colony the colony of the manager.
     */
    void sendWorkOrderPackets(@NotNull final Set<EntityPlayerMP> oldSubscribers, final boolean hasNewSubscribers, final Colony colony);


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
}
