package com.minecolonies.api.entity.ai.pathfinding;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.EntityUtils;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Abstract class for the walkToProxy.
 */
public abstract class AbstractWalkToProxy implements IWalkToProxy
{
    /**
     * The distance the entity can path directly without the proxy.
     */
    private static final int MIN_RANGE_FOR_DIRECT_PATH = 400;

    /**
     * The min distance a entity has to have to a proxy.
     */
    private static final int MIN_DISTANCE = 25;

    /**
     * The entity entity associated with the proxy.
     */
    private final EntityLiving entity;

    /**
     * The current proxy the citizen paths to.
     */
    private BlockPos currentProxy;

    /**
     * List of proxies the entity has to follow.
     */
    private final List<BlockPos> proxyList = new ArrayList<>();

    /**
     * Current target the entity has.
     */
    private BlockPos target;

    /**
     * Creates a walkToProxy for a certain entity.
     *
     * @param entity the entity.
     */
    protected AbstractWalkToProxy(final EntityLiving entity)
    {
        this.entity = entity;
    }

    /**
     * Leads the entity to a certain position due to proxies.
     *
     * @param target the position.
     * @param range  the range.
     * @return true if arrived.
     */
    public boolean walkToBlock(@NotNull final BlockPos target, final int range)
    {
        return walkToBlock(target, range, false);
    }

    /**
     * Take the direct path to a certain location.
     *
     * @param target the target position.
     * @param range  the range.
     * @param onMove entity on move or not?
     * @return true if arrived.
     */
    private boolean takeTheDirectPath(@NotNull final BlockPos target, final int range, final boolean onMove)
    {
        if (onMove)
        {
            final int targetY = careAboutY() ? entity.getPosition().getY() : target.getY();
            return isLivingAtSiteWithMove(entity, target.getX(), target.getY(), target.getZ(), range)
                    || EntityUtils.isLivingAtSite(entity, target.getX(), targetY, target.getZ(), range + 1);
        }
        else
        {
            return !EntityUtils.isLivingAtSite(entity, target.getX(), target.getY(), target.getZ(), range);
        }
    }

    /**
     * Leads the entity to a certain position due to proxies.
     *
     * @param target the target position.
     * @param range  the range.
     * @param onMove entity on move or not?
     * @return true if arrived.
     */
    public boolean walkToBlock(@NotNull final BlockPos target, final int range, final boolean onMove)
    {
        if (!target.equals(this.target))
        {
            this.resetProxyList();
            this.target = target;
        }

        final double distanceToPath = careAboutY()
                ? BlockPosUtil.getDistanceSquared2D(entity.getPosition(), target) : BlockPosUtil.getDistanceSquared(entity.getPosition(), target);

        if (distanceToPath <= MIN_RANGE_FOR_DIRECT_PATH)
        {
            if (distanceToPath <= MIN_DISTANCE)
            {
                currentProxy = null;
            }
            else
            {
                currentProxy = target;
            }

            this.resetProxyList();
            return takeTheDirectPath(target, range, onMove);
        }

        if (currentProxy == null)
        {
            currentProxy = fillProxyList(target, distanceToPath);
        }

        final double distanceToProxy = BlockPosUtil.getDistanceSquared2D(entity.getPosition(), currentProxy);
        final double distanceToNextProxy = proxyList.isEmpty() ? BlockPosUtil.getDistanceSquared2D(entity.getPosition(), target)
                : BlockPosUtil.getDistanceSquared2D(entity.getPosition(), proxyList.get(0));
        final double distanceProxyNextProxy = proxyList.isEmpty() ? BlockPosUtil.getDistanceSquared2D(currentProxy, target)
                : BlockPosUtil.getDistanceSquared2D(currentProxy, proxyList.get(0));
        if (distanceToProxy < MIN_DISTANCE || distanceToNextProxy < distanceProxyNextProxy)
        {
            if (proxyList.isEmpty())
            {
                currentProxy = target;
            }

            if (proxyList.isEmpty())
            {
                return takeTheDirectPath(target, range, onMove);
            }

            entity.getNavigator().clearPathEntity();
            currentProxy = proxyList.get(0);
            proxyList.remove(0);
        }

        if (currentProxy != null && !isLivingAtSiteWithMove(entity, currentProxy.getX(), currentProxy.getY(), currentProxy.getZ(), range))
        {
            //only walk to the block
            return !onMove;
        }

        return !onMove;
    }

    /**
     * Calculates a list of proxies to a certain target for a entity.
     *
     * @param target         the target.
     * @param distanceToPath the complete distance.
     * @return the first position to path to.
     */
    @NotNull
    private BlockPos fillProxyList(@NotNull final BlockPos target, final double distanceToPath)
    {
        @Nullable BlockPos proxyPoint = getSpecializedProxy(target, distanceToPath);

        if (proxyPoint == null)
        {
            proxyPoint = getProxy(target, entity.getPosition(), distanceToPath);
        }

        if (!proxyList.isEmpty())
        {
            proxyList.remove(0);
        }

        return proxyPoint;
    }

    /**
     * Reset the proxy.
     */
    private void resetProxyList()
    {
        currentProxy = null;
        proxyList.clear();
    }

    /**
     * Returns a proxy point to the goal.
     *
     * @param target         the target.
     * @param distanceToPath the total distance.
     * @return a proxy or, if not applicable null.
     */
    @NotNull
    protected BlockPos getProxy(@NotNull final BlockPos target, @NotNull final BlockPos position, final double distanceToPath)
    {
        double weight = Double.MAX_VALUE;
        BlockPos proxyPoint = null;

        for (final BlockPos wayPoint : getWayPoints())
        {
            final double simpleDistance = BlockPosUtil.getDistanceSquared(position, wayPoint);
            final double currentWeight = simpleDistance * simpleDistance + BlockPosUtil.getDistanceSquared(wayPoint, target);
            if (currentWeight < weight
                    && BlockPosUtil.getDistanceSquared2D(wayPoint, target) < distanceToPath
                    && simpleDistance > MIN_DISTANCE
                    && simpleDistance < distanceToPath
                    && !proxyList.contains(proxyPoint))
            {
                proxyPoint = wayPoint;
                weight = currentWeight;
            }
        }

        if (proxyList.contains(proxyPoint))
        {
            proxyPoint = null;
        }

        if (proxyPoint != null)
        {
            proxyList.add(proxyPoint);

            getProxy(target, proxyPoint, distanceToPath);

            return proxyList.get(0);
        }

        //No proxy point exists.
        return target;
    }

    /**
     * Get a list of waypoints depending on the entity.
     *
     * @return the set of waypoints.
     */
    public abstract Set<BlockPos> getWayPoints();

    /**
     * Check if for distance calculation the y level should be taken into account.
     *
     * @return true if so.
     */
    public abstract boolean careAboutY();

    /**
     * Try to get a specialized proxy to a certain target.
     *
     * @param target         the target.
     * @param distanceToPath the distance to it.
     * @return a special proxy point of existent, else null.
     */
    @Nullable
    public abstract BlockPos getSpecializedProxy(final BlockPos target, final double distanceToPath);

    /**
     * Getter for the proxyList.
     *
     * @return a copy of the list
     */
    public List<BlockPos> getProxyList()
    {
        return new ArrayList<>(proxyList);
    }

    /**
     * Add an entry to the proxy list.
     *
     * @param pos the position to add.
     */
    public void addToProxyList(final BlockPos pos)
    {
        proxyList.add(pos);
    }

    /**
     * Method to call to detect if an entity living is at site with move.
     * @param entity the entity to check.
     * @param x the x value.
     * @param y the y value.
     * @param z the z value.
     * @param range the range.
     * @return true if so.
     */
    public boolean isLivingAtSiteWithMove(final EntityLiving entity, final int x, final int y, final int z, final int range)
    {
        if(!EntityUtils.isLivingAtSiteWithMove(entity, x, y, z, range))
        {
            EntityUtils.tryMoveLivingToXYZ(entity, x, y, z);
            return false;
        }
        return true;
    }

    /**
     * Getter for the entity accociated with the proxy.
     *
     * @return the entity.
     */
    public EntityLiving getEntity()
    {
        return entity;
    }
}
