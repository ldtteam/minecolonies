package com.minecolonies.coremod.colony.managers;

import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.ColonyConstants.DEFAULT_SPAWN_RADIUS;
import static com.minecolonies.api.util.constant.ColonyConstants.MAX_SPAWN_RADIUS;
import static com.minecolonies.api.util.constant.Constants.HALF_A_CIRCLE;
import static com.minecolonies.api.util.constant.Constants.WHOLE_CIRCLE;

public class BarbarianManager implements IBarbarianManager
{
    /**
     * Whether there will be a raid in this colony tonight.
     */
    private boolean raidTonight = false;

    /**
     * Whether or not the raid has been calculated for today.
     */
    private boolean raidBeenCalculated = false;

    /**
     * Whether or not this colony may have Barbarian events. (set via command)
     */
    private boolean haveBarbEvents = true;

    /**
     * The colony of the manager.
     */
    private final Colony colony;

    /**
     * Creates the BarbarianManager for a colony.
     * @param colony the colony.
     */
    public BarbarianManager(final Colony colony)
    {
        this.colony = colony;
    }

    @Override
    public boolean canHaveBarbEvents()
    {
        return this.haveBarbEvents;
    }

    @Override
    public boolean hasRaidBeenCalculated()
    {
        return this.raidBeenCalculated;
    }

    @Override
    public boolean willRaidTonight()
    {
        return this.raidTonight;
    }

    @Override
    public void setCanHaveBarbEvents(final boolean canHave)
    {
        this.haveBarbEvents = canHave;
        colony.markDirty();
    }

    @Override
    public void setHasRaidBeenCalculated(final boolean hasSet)
    {
        this.raidBeenCalculated = hasSet;
    }

    @Override
    public void setWillRaidTonight(final boolean willRaid)
    {
        this.raidTonight = willRaid;
    }

    @Override
    public BlockPos getRandomOutsiderInDirection(final EnumFacing directionX, final EnumFacing directionZ)
    {
        final BlockPos center = colony.getCenter();
        final World world = colony.getWorld();
        final List<BlockPos> positions = colony.getWayPoints().keySet().stream().filter(
                pos -> isInDirection(directionX, directionZ, pos.subtract(center))).collect(Collectors.toList());
        positions.addAll(colony.getBuildingManager().getBuildings().keySet().stream().filter(
                pos -> isInDirection(directionX, directionZ, pos.subtract(center))).collect(Collectors.toList()));

        BlockPos thePos = center;
        double distance = 0;
        AbstractBuilding theBuilding = null;
        for (final BlockPos pos : positions)
        {
            final double currentDistance = center.distanceSq(pos);
            if (currentDistance > distance && world.isAreaLoaded(pos, DEFAULT_SPAWN_RADIUS))
            {
                distance = currentDistance;
                thePos = pos;
                theBuilding = colony.getBuildingManager().getBuilding(thePos);
            }
        }

        int minDistance = 0;
        if (theBuilding != null)
        {
            final Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> corners = theBuilding.getCorners();
            minDistance
                    = Math.max(corners.getFirst().getFirst() - corners.getFirst().getSecond(), corners.getSecond().getFirst() - corners.getSecond().getSecond());
        }

        if (thePos.equals(center))
        {
            return center;
        }

        int radius = DEFAULT_SPAWN_RADIUS;
        while (world.isAreaLoaded(thePos, radius))
        {
            radius += DEFAULT_SPAWN_RADIUS;
        }

        final int dist = Math.max(minDistance, Math.min(radius, MAX_SPAWN_RADIUS));
        thePos = thePos.offset(directionX, dist);
        thePos = thePos.offset(directionZ, dist);

        final int randomDegree = world.rand.nextInt((int) WHOLE_CIRCLE);
        final double rads = (double) randomDegree / HALF_A_CIRCLE * Math.PI;

        final double x = Math.round(thePos.getX() + 3 * Math.sin(rads));
        final double z = Math.round(thePos.getZ() + 3 * Math.cos(rads));

        Log.getLogger().info("Spawning at: " + x + " " + z);
        return new BlockPos(x, thePos.getY(), z);
    }


    /**
     * Check if a certain vector matches two directions.
     *
     * @param directionX the direction x.
     * @param directionZ the direction z.
     * @param vector     the vector.
     * @return true if so.
     */
    private static boolean isInDirection(final EnumFacing directionX, final EnumFacing directionZ, final BlockPos vector)
    {
        return EnumFacing.getFacingFromVector(vector.getX(), 0, 0) == directionX && EnumFacing.getFacingFromVector(0, 0, vector.getZ()) == directionZ;
    }
}
