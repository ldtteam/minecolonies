package com.minecolonies.core.colony.events.raid.pirateEvent;

import com.google.common.collect.Lists;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.storage.StructurePacks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.IColonyRaidEvent;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.entity.pathfinding.PathfindingUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.Constants.STORAGE_STYLE;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_COLONY_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_EVENT_ID;

/**
 * Utils for Colony pirate events
 */
public final class ShipBasedRaiderUtils
{
    /**
     * Folder name for the pirate ship schematics
     */
    public static final String SHIP_FOLDER = "/ships/";

    /**
     * Distance at which spawners are active
     */
    private static final int SPAWNER_DISTANCE = 30;

    /**
     * Private constructor to hide the implicit public one.
     */
    private ShipBasedRaiderUtils()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Used to trigger a pirate event.
     *
     * @param targetSpawnPoint the target spawn point.
     * @param colony           the target colony.
     * @param blueprint        the ship blueprint.
     * @param event            the raids event.
     * @return true if successful.
     */
    public static boolean spawnPirateShip(
      final BlockPos targetSpawnPoint,
      final IColony colony,
      final Blueprint blueprint,
      final IColonyRaidEvent event)
    {
        return colony.getEventManager()
                .getStructureManager()
                .spawnTemporaryStructure(blueprint, targetSpawnPoint, event.getID());
    }

    /**
     * Setup a spawner.
     *
     * @param location the location to set it up at.
     * @param world    the world to place it in.
     * @param mob      the mob to spawn.
     * @param event    the event.
     * @param colonyId the colony id.
     */
    public static void setupSpawner(final BlockPos location, final Level world, final EntityType<?> mob, final IColonyRaidEvent event, final int colonyId)
    {
        world.removeBlock(location, false);
        world.setBlockAndUpdate(location, Blocks.SPAWNER.defaultBlockState());
        final SpawnerBlockEntity spawner = new SpawnerBlockEntity(location, Blocks.SPAWNER.defaultBlockState());

        spawner.getSpawner().requiredPlayerRange = SPAWNER_DISTANCE;
        spawner.getSpawner().setEntityId(mob, world, world.getRandom(), location);
        // Sets nbt for mobs to spawn, assumes colony in same dimension as mob.
        spawner.getSpawner().nextSpawnData.getEntityToSpawn().putInt(TAG_EVENT_ID, event.getID());
        spawner.getSpawner().nextSpawnData.getEntityToSpawn().putInt(TAG_COLONY_ID, colonyId);

        event.addSpawner(location);
        world.setBlockEntity(spawner);
    }

    /**
     * Checks whether a pirate event is possible at this place.
     *
     * @param colony     the colony.
     * @param spawnPoint the spawn point.
     * @param raidLevel  the raid level.
     * @param rotation   the rotation.
     * @return true if successful.
     */
    public static boolean canSpawnShipAt(final IColony colony, final BlockPos spawnPoint, final int raidLevel, final int rotation, final String shipName)
    {
        return canSpawnShipAt(colony, spawnPoint, raidLevel, rotation, shipName, 3);
    }

    /**
     * Checks whether a pirate event is possible at this place.
     *
     * @param colony     the colony.
     * @param spawnPoint the spawn point.
     * @param raidLevel  the raid level.
     * @param rotation   the rotation.
     * @param neededDepth the required depth.
     * @return true if successful.
     */
    public static boolean canSpawnShipAt(final IColony colony, final BlockPos spawnPoint, final int raidLevel, final int rotation, final String shipName, final int neededDepth)
    {
        if (spawnPoint.equals(colony.getCenter()))
        {
            return false;
        }

        final Level world = colony.getWorld();
        final String shipSize = ShipSize.getShipForRaiderAmount(raidLevel).schematicPrefix + shipName;

        final Blueprint blueprint = StructurePacks.getBlueprint(STORAGE_STYLE, "decorations" + SHIP_FOLDER + shipSize + ".blueprint");
        blueprint.rotateWithMirror(BlockPosUtil.getRotationFromRotations(rotation), Mirror.NONE, colony.getWorld());

        return canPlaceShipAt(spawnPoint, blueprint, world, neededDepth) || canPlaceShipAt(spawnPoint.below(), blueprint, world, neededDepth);
    }

    /**
     * Checks whether the structure fits the position.
     *
     * @param pos   the position to check
     * @param ship  the ship structure to check
     * @param world the world to use
     * @return true if ship fits
     */
    public static boolean canPlaceShipAt(final BlockPos pos, final Blueprint ship, final Level world)
    {
        return canPlaceShipAt(pos, ship, world, 3);
    }

    /**
     * Checks whether the structure fits the position.
     *
     * @param pos   the position to check
     * @param ship  the ship structure to check
     * @param world the world to use
     * @return true if ship fits
     */
    public static boolean canPlaceShipAt(final BlockPos pos, final Blueprint ship, final Level world, final int neededDepth)
    {
        final BlockPos zeroPos = pos.subtract(ship.getPrimaryBlockOffset());
        final List<Predicate<BlockState>> allowedShipMaterials = Lists.newArrayList();

        allowedShipMaterials.add(BlockBehaviour.BlockStateBase::liquid);
        allowedShipMaterials.add(state -> state.is(BlockTags.ICE));
        allowedShipMaterials.add(state -> !state.blocksMotion() && state.getFluidState().is(FluidTags.WATER));

        if (MineColonies.getConfig().getServer().skyRaiders.get())
        {
            allowedShipMaterials.add(BlockBehaviour.BlockStateBase::isAir);
        }

        if (isSurfaceAreaMostlyMaterial(allowedShipMaterials, world, pos.getY(),
          zeroPos,
          new BlockPos(zeroPos.getX() + ship.getSizeX() - 1, zeroPos.getY(), zeroPos.getZ() + ship.getSizeZ() - 1),
          0.85))
        {
            for (int i = 0; i < neededDepth; i++)
            {
                if (!PathfindingUtils.isLiquid(world.getBlockState(pos.below(i))))
                {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    /**
     * Returns true when most parts of the given area are water, more then 90%
     *
     * @param materials       the materials.
     * @param world           Blockacces to use
     * @param baseY           the base y pos.
     * @param from            First corner of search rectangle
     * @param to              Second corner of search rectangle
     * @param percentRequired the required percentage.
     * @return true if enough water surface blocks are found
     */
    public static boolean isSurfaceAreaMostlyMaterial(
      @NotNull final List<Predicate<BlockState>> materials,
      @NotNull final Level world,
      final int baseY, @NotNull final BlockPos from,
      @NotNull final BlockPos to,
      final double percentRequired)
    {
        final int xDist = Math.abs(from.getX() - to.getX());
        final int zDist = Math.abs(from.getZ() - to.getZ());

        int wrongMaterialBlocks = 0;
        final int neededMaterialBlocks = (int) (percentRequired * (xDist * zDist));
        final int wrongMaterialBlockThreshold = (xDist * zDist) - neededMaterialBlocks;

        int xDir = 1;
        int zDir = 1;
        if (from.getX() > to.getX())
        {
            xDir = -1;
        }

        if (from.getZ() > to.getZ())
        {
            zDir = -1;
        }

        // Check the area
        for (int x = 0; x < xDist; x++)
        {
            for (int z = 0; z < zDist; z++)
            {
                final BlockState state = world.getBlockState(new BlockPos(from.getX() + (x * xDir), baseY, from.getZ() + (z * zDir)));
                boolean suitableBlock = false;
                for (final Predicate<BlockState> pred : materials)
                {
                    if (pred.test(state))
                    {
                        suitableBlock = true;
                        break;
                    }
                }

                // Checks up to 5 blocks above for air/nonblocking
                if (suitableBlock)
                {
                    for (int i = 1; i <= 5; i++)
                    {
                        if (world.getBlockState(new BlockPos(from.getX() + (x * xDir), baseY + i, from.getZ() + (z * zDir))).blocksMotion())
                        {
                            suitableBlock = false;
                            break;
                        }
                    }
                }

                // Count surface waterblocks
                if (!suitableBlock)
                {
                    wrongMaterialBlocks++;
                    // Skip when we already found too many non water blocks
                    if (wrongMaterialBlocks > wrongMaterialBlockThreshold)
                    {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Returns a loaded blockpos towards the colony center
     *
     * @param startPos       the start position
     * @param colony         the colony to use
     * @param maxDistance    the max distance to go
     * @param maxDistancePos the position from where to calc the max distance.
     * @param minDistance    the min distance from colony center allowed
     * @param accuracy       the accuracy of steps to check in percent, min 1.
     * @return the position.
     */
    public static BlockPos getLoadedPositionTowardsCenter(
      final BlockPos startPos,
      final IColony colony,
      final int maxDistance,
      final BlockPos maxDistancePos,
      final int minDistance,
      final int accuracy)
    {
        return getLoadedPositionTowardsCenter(startPos, colony, maxDistance, maxDistancePos, minDistance, accuracy, false);
    }

    /**
     * Returns a loaded blockpos towards the colony center
     *
     * @param startPos       the start position
     * @param colony         the colony to use
     * @param maxDistance    the max distance to go
     * @param maxDistancePos the position from where to calc the max distance.
     * @param minDistance    the min distance from colony center allowed
     * @param accuracy       the accuracy of steps to check in percent, min 1.
     * @param underWater     if an underwater spawn pos is okay.
     * @return the position.
     */
    public static BlockPos getLoadedPositionTowardsCenter(
      final BlockPos startPos,
      final IColony colony,
      final int maxDistance,
      final BlockPos maxDistancePos,
      final int minDistance,
      final int accuracy,
      final boolean underWater)
    {
        if (accuracy < 1)
        {
            return null;
        }

        if (WorldUtil.isBlockLoaded(colony.getWorld(), startPos))
        {
            if (underWater)
            {
                return BlockPosUtil.findAround(colony.getWorld(),
                  startPos,
                  30,
                  3,
                  (world, pos) -> (world.getBlockState(pos).isSolid() || world.getBlockState(pos).liquid()) && !world.getBlockState(
                    pos.above()).blocksMotion() && !world.getBlockState(pos.above(2)).blocksMotion());
            }
            else
            {
                return BlockPosUtil.findAround(colony.getWorld(),
                  startPos,
                  30,
                  3,
                  (world, pos) -> (world.getBlockState(pos).isSolid() || world.getBlockState(pos).liquid()) && world.getBlockState(
                    pos.above()).isAir() && world.getBlockState(pos.above(2)).isAir());
            }
        }

        BlockPos diff = colony.getCenter().subtract(startPos);
        diff = new BlockPos(diff.getX() / accuracy, diff.getY() / accuracy, diff.getZ() / accuracy);

        final int sqMaxDist = maxDistance * maxDistance;
        final int sqMinDist = minDistance * minDistance;

        BlockPos tempPos = new BlockPos(startPos);

        for (int i = 0; i < accuracy; i++)
        {
            tempPos = tempPos.offset(diff);

            if (BlockPosUtil.getDistanceSquared2D(maxDistancePos, tempPos) > sqMaxDist || BlockPosUtil.getDistanceSquared2D(tempPos, colony.getCenter()) < sqMinDist)
            {
                return null;
            }

            if (WorldUtil.isBlockLoaded(colony.getWorld(), tempPos))
            {
                return BlockPosUtil.getFloor(tempPos, colony.getWorld());
            }
        }
        return null;
    }

    /**
     * Finds a spawnpoint for pirates on a ship
     *
     * @param spawnPos the ships spawnpoint
     * @param world    the world
     * @param radius   the radius to check for.
     * @return the position.
     */
    public static BlockPos findSpawnPosOnShip(final BlockPos spawnPos, final Level world, final int radius)
    {
        for (int y = 0; y <= radius * 2; y += 2)
        {
            for (int x = -radius; x <= radius; x++)
            {
                for (int z = -radius; z <= radius; z++)
                {
                    if (!world.getBlockState(spawnPos.offset(x, y, z)).blocksMotion() && !world.getBlockState(spawnPos.offset(x, y + 1, z)).blocksMotion())
                    {
                        return spawnPos.offset(x, y, z);
                    }
                }
            }
        }

        return spawnPos;
    }

    /**
     * Creates a list of waypoints of a path
     *
     * @param path
     * @param spacing min distance between waypoints
     * @return list of waypoints
     */
    public static List<BlockPos> createWaypoints(final Level world, final Path path, final int spacing)
    {
        List<BlockPos> wayPoints = new ArrayList<>();
        if (path == null)
        {
            return wayPoints;
        }

        BlockPos lastPoint = BlockPos.ZERO;
        for (int i = 0; i < path.getNodeCount(); i++)
        {
            final BlockPos point = path.getNode(i).asBlockPos();
            if (lastPoint.distManhattan(point) > spacing
                  && world.getBlockState(point).isAir())
            {
                wayPoints.add(point);
                lastPoint = point;
            }
        }

        return wayPoints;
    }

    /**
     * Chooses the next position to go to
     *
     * @param startPos  the Position we're starting from
     * @param target    original destination
     * @param wayPoints waypoints to compare
     * @return position to go to
     */
    public static BlockPos chooseWaypointFor(final List<BlockPos> wayPoints, final BlockPos startPos, final BlockPos target)
    {
        BlockPos closest = target;
        BlockPos secondClosest = target;
        for (final BlockPos wayPoint : wayPoints)
        {
            final int distToStart = wayPoint.distManhattan(startPos);
            if (distToStart > 5 && distToStart < closest.distManhattan(startPos))
            {
                secondClosest = closest;
                closest = wayPoint;
            }
        }

        if (secondClosest.distManhattan(target) < closest.distManhattan(target))
        {
            closest = secondClosest;
        }

        if (closest.distManhattan(target) >= startPos.distManhattan(target))
        {
            return target;
        }

        return closest;
    }
}
