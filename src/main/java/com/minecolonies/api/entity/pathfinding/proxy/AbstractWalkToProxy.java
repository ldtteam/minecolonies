package com.minecolonies.api.entity.pathfinding.proxy;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.EntityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

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
     * Range to the proxy.
     */
    private static final int PROXY_RANGE = 3;

    /**
     * The entity entity associated with the proxy.
     */
    private final Mob entity;

    /**
     * List of proxies the entity has to follow.
     */
    private final List<BlockPos> proxyList = new ArrayList<>();

    /**
     * The current proxy the citizen paths to.
     */
    private BlockPos currentProxy;

    /**
     * Current target the entity has.
     */
    private BlockPos target;

    /**
     * Creates a walkToProxy for a certain entity.
     *
     * @param entity the entity.
     */
    protected AbstractWalkToProxy(final Mob entity)
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
        return walkToBlock(target, range, true);
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

        final BlockPos pos = BlockPos.containing(entity.getX(), entity.getY(), entity.getZ());
        final double distanceToPath = careAboutY()
                                        ? BlockPosUtil.getDistanceSquared(pos, target) : BlockPosUtil.getDistanceSquared2D(pos, target);

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

            proxyList.clear();
            return takeTheDirectPath(target, range, onMove);
        }

        if (currentProxy == null)
        {
            currentProxy = fillProxyList(target, distanceToPath);
        }

        final double distanceToProxy = BlockPosUtil.getDistanceSquared(pos, currentProxy);
        final double distanceToNextProxy = proxyList.isEmpty() ? BlockPosUtil.getDistanceSquared(pos, target)
                                             : BlockPosUtil.getDistanceSquared(pos, proxyList.get(0));
        final double distanceProxyNextProxy = proxyList.isEmpty() ? BlockPosUtil.getDistanceSquared(currentProxy, target)
                                                : BlockPosUtil.getDistanceSquared(currentProxy, proxyList.get(0));

        if (distanceToProxy < MIN_DISTANCE || distanceToNextProxy < distanceProxyNextProxy)
        {
            if (proxyList.isEmpty())
            {
                currentProxy = target;
                return takeTheDirectPath(target, range, onMove);
            }

            currentProxy = proxyList.get(0);
            proxyList.remove(0);
        }

        if (currentProxy != null && !isLivingAtSiteWithMove(entity, currentProxy.getX(), currentProxy.getY(), currentProxy.getZ(), PROXY_RANGE))
        {
            //only walk to the block
            return !onMove;
        }

        return !onMove;
    }

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
     *
     * @param entity the entity to check.
     * @param x      the x value.
     * @param y      the y value.
     * @param z      the z value.
     * @param range  the range.
     * @return true if so.
     */
    public boolean isLivingAtSiteWithMove(final Mob entity, final int x, final int y, final int z, final int range)
    {
        if (!EntityUtils.isLivingAtSiteWithMove(entity, x, y, z, range))
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
    public Mob getEntity()
    {
        return entity;
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
        final boolean arrived;
        final BlockPos pos = BlockPos.containing(entity.getX(), entity.getY(), entity.getZ());

        if (onMove)
        {
            final int targetY = careAboutY() ? target.getY() : entity.blockPosition().getY();
            arrived = isLivingAtSiteWithMove(entity, target.getX(), target.getY(), target.getZ(), range)
                        || EntityUtils.isLivingAtSite(entity, target.getX(), targetY, target.getZ(), range + 1);
        }
        else
        {
            arrived = !EntityUtils.isLivingAtSite(entity, target.getX(), target.getY(), target.getZ(), range);
        }

        if (arrived)
        {
            this.target = null;
        }
        return arrived;
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
        final BlockPos pos = BlockPos.containing(entity.getX(), entity.getY(), entity.getZ());

        if (proxyPoint == null)
        {
            proxyPoint = getProxy(target, pos, distanceToPath);
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
     * @param position       the position.
     * @param distanceToPath the total distance.
     * @return a proxy or, if not applicable null.
     */
    @NotNull
    protected BlockPos getProxy(@NotNull final BlockPos target, @NotNull final BlockPos position, final double distanceToPath)
    {
        double weight = Double.MAX_VALUE;
        BlockPos proxyPoint = null;
        double distance = Double.MAX_VALUE;

        for (final BlockPos wayPoint : getWayPoints())
        {
            final double simpleDistance = careAboutY() ? BlockPosUtil.getDistanceSquared(position, wayPoint) : BlockPosUtil.getDistanceSquared2D(position, wayPoint);
            final double targetDistance = careAboutY() ? BlockPosUtil.getDistanceSquared(wayPoint, target) : BlockPosUtil.getDistanceSquared2D(wayPoint, target);
            final double currentWeight = simpleDistance * simpleDistance + targetDistance + targetDistance;
            if (currentWeight < weight
                  && targetDistance * 1.5 < distanceToPath
                  && simpleDistance > MIN_DISTANCE
                  && simpleDistance < distanceToPath
                  && !proxyList.contains(wayPoint))
            {
                proxyPoint = wayPoint;
                weight = currentWeight;
                distance = targetDistance;
            }
        }

        if (proxyList.contains(proxyPoint))
        {
            return target;
        }

        if (proxyPoint != null)
        {
            proxyList.add(proxyPoint);

            getProxy(target, proxyPoint, distance);

            return proxyList.get(0);
        }

        //No proxy point exists.
        return target;
    }

    @Override
    public BlockPos getCurrentProxy()
    {
        return currentProxy;
    }

    @Override
    public void reset()
    {
        this.target = null;
    }
}
