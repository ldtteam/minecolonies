package com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent;

import com.google.common.collect.Lists;
import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.util.BlockInfo;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.IColonyRaidEvent;
import com.minecolonies.api.colony.colonyEvents.IColonyStructureSpawnEvent;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.CreativeBuildingStructureHandler;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.util.CreativeRaiderStructureHandler;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import static com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider.TAG_BLUEPRINTDATA;
import static com.minecolonies.api.colony.colonyEvents.NBTTags.TAG_EVENT_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_COLONY_ID;

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
     * @param world            the target world.
     * @param colony           the target colony.
     * @param shipSize         the size of the ship.
     * @param event            the raids event.
     * @param shipRotation     the shiprotation.
     * @return true if successful.
     */
    public static boolean spawnPirateShip(
      final BlockPos targetSpawnPoint,
      final World world,
      final IColony colony,
      final String shipSize,
      final IColonyRaidEvent event,
      final int shipRotation)
    {
        final CreativeRaiderStructureHandler
          structure = new CreativeRaiderStructureHandler(world, targetSpawnPoint, Structures.SCHEMATICS_PREFIX + SHIP_FOLDER + shipSize, new PlacementSettings(Mirror.NONE, BlockPosUtil.getRotationFromRotations(shipRotation)), true, event, colony.getID());

        if (!colony.getEventManager()
               .getStructureManager()
               .spawnTemporaryStructure(structure.getBluePrint(), Structures.SCHEMATICS_PREFIX + SHIP_FOLDER + shipSize, targetSpawnPoint, event.getID(), shipRotation, Mirror.NONE))
        {
            return false;
        }
        return true;
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
    public static void setupSpawner(final BlockPos location, final World world, final EntityType mob, final IColonyRaidEvent event, final int colonyId)
    {
        world.removeBlock(location, false);
        world.setBlockState(location, Blocks.SPAWNER.getDefaultState());
        final MobSpawnerTileEntity spawner = new MobSpawnerTileEntity();

        spawner.getSpawnerBaseLogic().activatingRangeFromPlayer = SPAWNER_DISTANCE;
        spawner.getSpawnerBaseLogic().setEntityType(mob);
        // Sets nbt for mobs to spawn, assumes colony in same dimension as mob.
        spawner.getSpawnerBaseLogic().spawnData.getNbt().putInt(TAG_EVENT_ID, event.getID());
        spawner.getSpawnerBaseLogic().spawnData.getNbt().putInt(TAG_COLONY_ID, colonyId);

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
    public static boolean canSpawnShipAt(final IColony colony, final BlockPos spawnPoint, final int raidLevel, final int rotation, final IColonyStructureSpawnEvent event)
    {
        if (spawnPoint.equals(colony.getCenter()) || spawnPoint.getY() > MineColonies.getConfig().getCommon().maxYForBarbarians.get())
        {
            return false;
        }

        final World world = colony.getWorld();
        final String shipSize = ShipSize.getShipForRaidLevel(raidLevel).schematicPrefix + event.getShipDesc();

        final CreativeBuildingStructureHandler
          structure = new CreativeBuildingStructureHandler(colony.getWorld(), spawnPoint, Structures.SCHEMATICS_PREFIX + SHIP_FOLDER + shipSize, new PlacementSettings(), true);
        structure.getBluePrint().rotateWithMirror(BlockPosUtil.getRotationFromRotations(rotation), Mirror.NONE, colony.getWorld());

        return canPlaceShipAt(spawnPoint, structure.getBluePrint(), world) || canPlaceShipAt(spawnPoint.down(), structure.getBluePrint(), world);
    }

    /**
     * Checks whether the structure fits the position.
     *
     * @param pos   the position to check
     * @param ship  the ship structure to check
     * @param world the world to use
     * @return true if ship fits
     */
    public static boolean canPlaceShipAt(final BlockPos pos, final Blueprint ship, final World world)
    {
        final BlockPos zeroPos = pos.subtract(ship.getPrimaryBlockOffset());

        int y = 3;
        final BlockInfo info = ship.getBlockInfoAsMap().getOrDefault(ship.getPrimaryBlockOffset(), null);
        if (info.getTileEntityData() != null)
        {
            final CompoundNBT teData = ship.getTileEntityData(pos, ship.getPrimaryBlockOffset());
            if (teData != null && teData.contains(TAG_BLUEPRINTDATA))
            {
                final TileEntity entity = TileEntity.create(info.getTileEntityData());
                if (entity instanceof IBlueprintDataProvider)
                {
                    for (final Map.Entry<BlockPos, List<String>> entry : ((IBlueprintDataProvider) entity).getPositionedTags().entrySet())
                    {
                        if (entry.getValue().contains("groundlevel"))
                        {
                            y = entry.getKey().getY() + 1;
                        }
                    }
                }
            }
        }

        return isSurfaceAreaMostlyMaterial(Lists.newArrayList(Material.WATER, Material.ICE), world, pos.getY(),
          zeroPos,
          new BlockPos(zeroPos.getX() + ship.getSizeX() - 1, zeroPos.getY(), zeroPos.getZ() + ship.getSizeZ() - 1),
          0.6);
    }

    /**
     * Returns true when most parts of the given area are water, more then 90%
     *
     * @param materials the materials.
     * @param world Blockacces to use
     * @param baseY the base y pos.
     * @param from  First corner of search rectangle
     * @param to    Second corner of search rectangle
     * @param percentRequired the required percentage.
     * @return true if enough water surface blocks are found
     */
    public static boolean isSurfaceAreaMostlyMaterial(
      @NotNull final List<Material> materials,
      @NotNull final World world,
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
                // Count surface waterblocks
                if (!materials.contains(world.getBlockState(new BlockPos(from.getX() + (x*xDir), baseY, from.getZ() + (z*zDir))).getMaterial()) || !world.isAirBlock(new BlockPos(from.getX() + (x*xDir), baseY + 1, from.getZ() + (z*zDir))))
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
