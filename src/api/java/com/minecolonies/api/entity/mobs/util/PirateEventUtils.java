package com.minecolonies.api.entity.mobs.util;

import com.ldtteam.structures.helpers.Structure;
import com.ldtteam.structurize.items.ItemScanTool;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InstantStructurePlacer;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.Log;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static com.minecolonies.api.util.constant.ColonyConstants.*;
import static com.minecolonies.api.util.constant.Constants.BLOCKS_PER_CHUNK;
import static com.minecolonies.api.util.constant.TranslationConstants.RAID_EVENT_MESSAGE_PIRATE;

/**
 * Utils for Colony pirate events
 */
public final class PirateEventUtils
{
    /**
     * Folder name for the pirate ship schematics
     */
    public static final String PIRATESHIP_FOLDER = "/Ships/";

    /**
     * Colony range divider in which distance to load the pirate spawners.
     */
    private static final int SPAWNER_DISTANCE_DIVIDER = 4;

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
     * @param raidNumber       the size of the raid.
     */
    public static void pirateEvent(final BlockPos targetSpawnPoint, final World world, final IColony colony, final String shipSize, final int raidNumber)
    {
        final Structure structure = new Structure(world, Structures.SCHEMATICS_PREFIX + PIRATESHIP_FOLDER + shipSize, new PlacementSettings());
        structure.rotate(BlockPosUtil.getRotationFromRotations(0), world, targetSpawnPoint, Mirror.NONE);

        if (!ItemScanTool.saveStructureOnServer(world,
          targetSpawnPoint.add(structure.getWidth() - 1, structure.getHeight(), structure.getLength() - 1).subtract(structure.getOffset()),
          targetSpawnPoint.down(3).subtract(structure.getOffset()),
          Structures.SCHEMATICS_PREFIX + PIRATESHIP_FOLDER + shipSize + colony.getID() + colony.getDimension() + targetSpawnPoint.down(3)))
        {
            // No event if we didnt successfully save the surroundings before
            Log.getLogger().info("Failed to save schematics for Pirate ship spawn");
            return;
        }
        colony.getRaiderManager().registerRaiderOriginSchematic(Structures.SCHEMATICS_PREFIX + PIRATESHIP_FOLDER + shipSize, targetSpawnPoint.down(3), world.getWorldTime());
        InstantStructurePlacer.loadAndPlaceStructureWithRotation(world, Structures.SCHEMATICS_PREFIX + PIRATESHIP_FOLDER + shipSize, targetSpawnPoint.down(3), 0, Mirror.NONE, false);
        loadSpawners(world, targetSpawnPoint, shipSize);
        LanguageHandler.sendPlayersMessage(
          colony.getImportantMessageEntityPlayers(),
          RAID_EVENT_MESSAGE_PIRATE + raidNumber, colony.getName());
    }

    /**
     * Load pirate spawners on the ship.
     *
     * @param world            the world to load it in.
     * @param targetSpawnPoint the initital spawn point.
     * @param shipSize         the size of the ship.
     */
    private static void loadSpawners(final World world, final BlockPos targetSpawnPoint, final String shipSize)
    {
        switch (shipSize)
        {
            case SMALL_PIRATE_SHIP:
                setupSpawner(targetSpawnPoint.up(2).north(), world, PIRATE);
                break;
            case MEDIUM_PIRATE_SHIP:
                setupSpawner(targetSpawnPoint.up(3).north(10), world, PIRATE_CHIEF);
                setupSpawner(targetSpawnPoint.up(1), world, PIRATE);
                setupSpawner(targetSpawnPoint.up(5).south(6), world, PIRATE_ARCHER);
                break;
            case BIG_PIRATE_SHIP:
                setupSpawner(targetSpawnPoint.up(3).south(), world, PIRATE);
                setupSpawner(targetSpawnPoint.up(3).north(), world, PIRATE);
                setupSpawner(targetSpawnPoint.down(1).south(5), world, PIRATE);
                setupSpawner(targetSpawnPoint.down(1).north(5).east(2), world, PIRATE);
                setupSpawner(targetSpawnPoint.down(1).north(8), world, PIRATE);
                setupSpawner(targetSpawnPoint.up(2).south(12), world, PIRATE);

                setupSpawner(targetSpawnPoint.up(3).north(10), world, PIRATE_CHIEF);
                setupSpawner(targetSpawnPoint.up(6).north(12), world, PIRATE_CHIEF);

                setupSpawner(targetSpawnPoint.up(9).north(13), world, PIRATE_ARCHER);
                setupSpawner(targetSpawnPoint.up(22).south(), world, PIRATE_ARCHER);
                setupSpawner(targetSpawnPoint.up(6).south(11), world, PIRATE_ARCHER);
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
    private static void setupSpawner(final BlockPos location, final World world, final ResourceLocation mob)
    {
        world.setBlockState(location, Blocks.MOB_SPAWNER.getDefaultState());
        final TileEntityMobSpawner spawner = new TileEntityMobSpawner();

        spawner.getSpawnerBaseLogic().activatingRangeFromPlayer = Configurations.gameplay.workingRangeTownHallChunks / SPAWNER_DISTANCE_DIVIDER * BLOCKS_PER_CHUNK;
        spawner.getSpawnerBaseLogic().setEntityId(mob);

        world.setTileEntity(location, spawner);
    }
}
