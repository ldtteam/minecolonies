package com.minecolonies.api.entity.ai.pathfinding;

import net.minecraft.entity.MobEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * Interface which defines the walkToProxy.
 */
public interface IWalkToProxy
{
    /**
     * Leads the entity to a certain position due to proxies.
     *
     * @param target the position.
     * @param range  the range.
     * @return true if arrived.
     */
    boolean walkToBlock(@NotNull final BlockPos target, final int range);

    /**
     * Leads the entity to a certain position due to proxies.
     *
     * @param target the target position.
     * @param range  the range.
     * @param onMove entity on move or not?
     * @return true if arrived.
     */
    boolean walkToBlock(@NotNull final BlockPos target, final int range, final boolean onMove);

    /**
     * Get a list of waypoints depending on the entity.
     *
     * @return the set of waypoints.
     */
    Set<BlockPos> getWayPoints();

    /**
     * Check if for distance calculation the y level should be taken into account.
     *
     * @return true if so.
     */
    boolean careAboutY();

    /**
     * Try to get a specialized proxy to a certain target.
     *
     * @param target         the target.
     * @param distanceToPath the distance to it.
     * @return a special proxy point of existent, else null.
     */
    @Nullable
    BlockPos getSpecializedProxy(final BlockPos target, final double distanceToPath);

    /**
     * Getter for the proxyList.
     *
     * @return a copy of the list
     */
    List<BlockPos> getProxyList();

    /**
     * Add an entry to the proxy list.
     *
     * @param pos the position to add.
     */
    void addToProxyList(final BlockPos pos);

    /**
     * Method to call to detect if an entity living is at site with move.
     *
     * @param entity the entity to check.
     * @param x      the x value.
     * @param y      the y value.
     * @param z      the z value.
     * @param range  the range.
     * @return true if so.
     */
    boolean isLivingAtSiteWithMove(final MobEntity entity, final int x, final int y, final int z, final int range);

    /**
     * Getter for the entity accociated with the proxy.
     *
     * @return the entity.
     */
    MobEntity getEntity();

    /**
     * Getter for the current proxy.
     *
     * @return
     */
    BlockPos getCurrentProxy();

    /**
     * Reset the target of the proxy.
     */
    void reset();
}
