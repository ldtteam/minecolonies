package com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent;

import com.google.common.collect.Lists;
import com.ldtteam.structures.helpers.Structure;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.MineColonies;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityType;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.colony.colonyEvents.NBTTags.TAG_EVENT_ID;
import static com.minecolonies.api.entity.ModEntities.*;
import static com.minecolonies.api.util.constant.ColonyConstants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_COLONY_ID;

/**
 * Utils for Colony pirate events
 */
public final class PirateEventUtils
{
    /**
     * Folder name for the pirate ship schematics
     */
    public static final String PIRATESHIP_FOLDER = "/ships/";

    /**
     * Distance at which spawners are active
     */
    private static final int SPAWNER_DISTANCE = 30;

    /**
     * Private constructor to hide the implicit public one.
     */
    private PirateEventUtils()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Used to trigger a pirate event.
     *
     * @param targetSpawnPoint the target spawn point.
     * @param world            the target world.
     * @param colony           the target colony.
     * @param shipSize         the size of the ship.
     * @param eventID          the id of the raids event.
     * @param shipRotation     the shiprotation.
     * @return true if successful.
     */
    public static boolean spawnPirateShip(
      final BlockPos targetSpawnPoint,
      final World world,
      final IColony colony,
      final String shipSize,
      final int eventID,
      final int shipRotation)
    {
        final Structure structure = new Structure(world, Structures.SCHEMATICS_PREFIX + PIRATESHIP_FOLDER + shipSize, new PlacementSettings());
        structure.rotate(BlockPosUtil.getRotationFromRotations(shipRotation), world, targetSpawnPoint, Mirror.NONE);

        if (!colony.getEventManager()
               .getStructureManager()
               .spawnTemporaryStructure(structure, Structures.SCHEMATICS_PREFIX + PIRATESHIP_FOLDER + shipSize, targetSpawnPoint, eventID, shipRotation, Mirror.NONE))
        {
            return false;
        }
        loadSpawners(world, targetSpawnPoint, shipSize, colony, eventID);
        return true;
    }

    /**
     * Load pirate spawners on the ship.
     *
     * @param world            the world to load it in.
     * @param targetSpawnPoint the initital spawn point.
     * @param shipSize         the size of the ship.
     */
    private static void loadSpawners(final World world, final BlockPos targetSpawnPoint, final String shipSize, final IColony colony, final int eventID)
    {
        switch (shipSize)
        {
            case SMALL_PIRATE_SHIP:
                setupSpawner(targetSpawnPoint.up(2).north(3), world, PIRATE, colony, eventID);
                break;
            case MEDIUM_PIRATE_SHIP:
                setupSpawner(targetSpawnPoint.up(3).north(12), world, CHIEFPIRATE, colony, eventID);
                setupSpawner(targetSpawnPoint.up(1).north(2), world, PIRATE, colony, eventID);
                setupSpawner(targetSpawnPoint.up(5).south(4), world, ARCHERPIRATE, colony, eventID);
                break;
            case BIG_PIRATE_SHIP:
                setupSpawner(targetSpawnPoint.up(3).south().north(2), world, PIRATE, colony, eventID);
                setupSpawner(targetSpawnPoint.up(3).north(3), world, PIRATE, colony, eventID);
                setupSpawner(targetSpawnPoint.down(1).south(3), world, PIRATE, colony, eventID);
                setupSpawner(targetSpawnPoint.down(1).north(7).east(2), world, PIRATE, colony, eventID);
                setupSpawner(targetSpawnPoint.down(1).north(10), world, PIRATE, colony, eventID);
                setupSpawner(targetSpawnPoint.up(2).south(10), world, PIRATE, colony, eventID);

                setupSpawner(targetSpawnPoint.up(3).north(12), world, CHIEFPIRATE, colony, eventID);
                setupSpawner(targetSpawnPoint.up(6).north(14), world, CHIEFPIRATE, colony, eventID);

                setupSpawner(targetSpawnPoint.up(9).north(15), world, ARCHERPIRATE, colony, eventID);
                setupSpawner(targetSpawnPoint.up(22).south().north(2), world, ARCHERPIRATE, colony, eventID);
                setupSpawner(targetSpawnPoint.up(6).south(9), world, ARCHERPIRATE, colony, eventID);
                break;
            default:
                Log.getLogger().warn("Invalid ship size detected!");
                break;
        }
    }

    /**
     * Setup a spawner.
     *
     * @param location the location to set it up at.
     * @param world    the world to place it in.
     * @param mob      the mob to spawn.
     */
    private static void setupSpawner(final BlockPos location, final World world, final EntityType mob, final IColony colony, final int eventID)
    {
        world.setBlockState(location, Blocks.SPAWNER.getDefaultState());
        final MobSpawnerTileEntity spawner = new MobSpawnerTileEntity();

        spawner.getSpawnerBaseLogic().activatingRangeFromPlayer = SPAWNER_DISTANCE;
        spawner.getSpawnerBaseLogic().setEntityType(mob);
        // Sets nbt for mobs to spawn, assumes colony in same dimension as mob.
        spawner.getSpawnerBaseLogic().spawnData.getNbt().putInt(TAG_EVENT_ID, eventID);
        spawner.getSpawnerBaseLogic().spawnData.getNbt().putInt(TAG_COLONY_ID, colony.getID());

        world.setTileEntity(location, spawner);
    }

    /**
     * Checks whether a pirate event is possible at this place.
     *
     * @param colony the colony.
     * @param spawnPoint the spawn point.
     * @param raidLevel the raid level.
     * @param rotation the rotation.
     * @return true if successful.
     */
    public static boolean canSpawnPirateEventAt(final IColony colony, final BlockPos spawnPoint, final int raidLevel, final int rotation)
    {
        if (spawnPoint.equals(colony.getCenter()) || spawnPoint.getY() > MineColonies.getConfig().getCommon().maxYForBarbarians.get())
        {
            return false;
        }

        final World world = colony.getWorld();
        final String shipSize = ShipSize.getShipForRaidLevel(raidLevel).schematicName;

        final Structure structure = new Structure(colony.getWorld(), Structures.SCHEMATICS_PREFIX + PIRATESHIP_FOLDER + shipSize, new PlacementSettings());
        structure.rotate(BlockPosUtil.getRotationFromRotations(rotation), colony.getWorld(), spawnPoint, Mirror.NONE);

        return canPlaceShipAt(spawnPoint, structure, world) || canPlaceShipAt(spawnPoint.down(), structure, world);
    }

    /**
     * Checks whether the structure fits the position.
     *
     * @param pos   the position to check
     * @param ship  the ship structure to check
     * @param world the world to use
     * @return true if ship fits
     */
    public static boolean canPlaceShipAt(final BlockPos pos, final Structure ship, final World world)
    {
        return isSurfaceAreaMostlyMaterial(Lists.newArrayList(Material.WATER, Material.ICE), world,
          pos.add(-ship.getOffset().getX(), 0, -ship.getOffset().getZ()),
          pos.add(ship.getWidth() - 1, 0, ship.getLength() - 1).subtract(ship.getOffset()),
          0.6);
    }

    /**
     * Returns true when most parts of the given area are water, more then 90%
     *
     * @param world Blockacces to use
     * @param from  First corner of search rectangle
     * @param to    Second corner of search rectangle
     * @param materials the materials.
     * @param percentRequired the required percentage.
     * @return true if enough water surface blocks are found
     */
    public static boolean isSurfaceAreaMostlyMaterial(
      @NotNull final List<Material> materials,
      @NotNull final World world,
      @NotNull final BlockPos from,
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
                // Count surface waterblocks
                if (!materials.contains(world.getBlockState(from.add(x * xDir, 0, z * zDir)).getMaterial()) || !world.isAirBlock(from.add(x * xDir, 1, z * zDir)))
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
        if (accuracy < 1)
        {
            return null;
        }

        if (colony.getWorld().isBlockPresent(startPos))
        {
            return BlockPosUtil.findLand(startPos, colony.getWorld());
        }

        BlockPos diff = colony.getCenter().subtract(startPos);
        diff = new BlockPos(diff.getX() / accuracy, diff.getY() / accuracy, diff.getZ() / accuracy);

        final int sqMaxDist = maxDistance * maxDistance;
        final int sqMinDist = minDistance * minDistance;

        BlockPos tempPos = new BlockPos(startPos);

        for (int i = 0; i < accuracy; i++)
        {
            tempPos = tempPos.add(diff);

            if (BlockPosUtil.getDistanceSquared2D(maxDistancePos, tempPos) > sqMaxDist || BlockPosUtil.getDistanceSquared2D(tempPos, colony.getCenter()) < sqMinDist)
            {
                return null;
            }

            if (colony.getWorld().isBlockPresent(tempPos))
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
     * @param radius the radius to check for.
     * @return the position.
     */
    public static BlockPos findSpawnPosOnShip(final BlockPos spawnPos, final World world, final int radius)
    {
        for (int y = 0; y <= radius * 2; y += 2)
        {
            for (int x = -radius; x <= radius; x++)
            {
                for (int z = -radius; z <= radius; z++)
                {
                    if (world.getBlockState(spawnPos.add(x, y, z)).getBlock() instanceof AirBlock && world.getBlockState(spawnPos.add(x, y + 1, z)).getBlock() instanceof AirBlock)
                    {
                        return spawnPos.add(x, y, z);
                    }
                }
            }
        }

        return spawnPos;
    }
}
