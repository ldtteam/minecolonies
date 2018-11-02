package com.minecolonies.coremod.entity.ai.mobs.util;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.Structures;
import com.minecolonies.coremod.entity.ai.citizen.fisherman.Pond;
import com.minecolonies.coremod.util.StructureWrapper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.minecolonies.api.util.constant.ColonyConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.RAID_EVENT_MESSAGE;
import static com.minecolonies.api.util.constant.TranslationConstants.RAID_EVENT_MESSAGE_PIRATE;

/**
 * Utils for Colony mob events
 */
public final class MobEventsUtils
{
    /**
     * Spawn modifier to decrease the spawnrate.
     */
    private static final int SPAWN_MODIFIER   = 3;

    /**
     * Private constructor to hide the implicit public one.
     */
    private MobEventsUtils()
    {
    }

    public static void barbarianEvent(final World world, final Colony colony)
    {
        if (world == null || !colony.isCanHaveBarbEvents())
        {
            return;
        }

        final Horde horde = numberOfSpawns(colony);
        final int hordeSize = horde.hordeSize;
        if(hordeSize == 0)
        {
            return;
        }

        BlockPos targetSpawnPoint = calculateSpawnLocation(world, colony);
        Log.getLogger().info("[BarbarianEvent]: Spawning: " + targetSpawnPoint.getX() + " " + targetSpawnPoint.getZ());
        if (targetSpawnPoint.equals(colony.getCenter()))
        {
            return;
        }

        if (Configurations.gameplay.enableInDevelopmentFeatures)
        {
            LanguageHandler.sendPlayersMessage(
              colony.getMessageEntityPlayers(),
              "Horde Spawn Point: " + targetSpawnPoint);
        }
        colony.getBarbManager().addRaiderSpawnPoint(targetSpawnPoint);
        colony.markDirty();

        int raidNumber = HUGE_HORDE_MESSAGE_ID;
        String shipSize = BIG_PIRATE_SHIP;
        if(hordeSize < SMALL_HORDE_SIZE)
        {
            raidNumber = SMALL_HORDE_MESSAGE_ID;
            shipSize = SMALL_PIRATE_SHIP;
        }
        else if(hordeSize < MEDIUM_HORDE_SIZE)
        {
            raidNumber = MEDIUM_HORDE_MESSAGE_ID;
            shipSize = MEDIUM_PIRATE_SHIP;
        }
        else if(hordeSize < BIG_HORDE_SIZE)
        {
            raidNumber = BIG_HORDE_MESSAGE_ID;
            shipSize = MEDIUM_PIRATE_SHIP;
        }

        colony.setNightsSinceLastRaid(0);

        if (BlockPosUtil.getFloor(targetSpawnPoint, 0, world) == null)
        {
            targetSpawnPoint = new BlockPos(targetSpawnPoint.getX(), colony.getCenter().getY(), targetSpawnPoint.getZ());
            buildPlatform(targetSpawnPoint, world);
        }

        if ((world.getBlockState(targetSpawnPoint).getBlock() == Blocks.WATER && Pond.checkWater(world, targetSpawnPoint))
              || (world.getBlockState(targetSpawnPoint.down()).getBlock() == Blocks.WATER && Pond.checkWater(world, targetSpawnPoint.down()))
        )
        {
            StructureWrapper.loadAndPlaceStructureWithRotation(world, Structures.SCHEMATICS_PREFIX + "/Ships/" + shipSize, targetSpawnPoint.down(3), 0, Mirror.NONE, false);
            loadSpawners(world, targetSpawnPoint, shipSize);
            LanguageHandler.sendPlayersMessage(
              colony.getMessageEntityPlayers(),
              RAID_EVENT_MESSAGE_PIRATE + raidNumber, colony.getName());
            return;
        }

        LanguageHandler.sendPlayersMessage(
          colony.getMessageEntityPlayers(),
          RAID_EVENT_MESSAGE + raidNumber, colony.getName());

        MobSpawnUtils.spawn(BARBARIAN, horde.numberOfBarbarians, targetSpawnPoint, world);
        MobSpawnUtils.spawn(ARCHER, horde.numberOfArchers, targetSpawnPoint, world);
        MobSpawnUtils.spawn(CHIEF, horde.numberOfChiefs, targetSpawnPoint, world);
    }

    /**
     * Load pirate spawners on the ship.
     * @param world the world to load it in.
     * @param targetSpawnPoint the initital spawn point.
     * @param shipSize the size of the ship.
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
        }
    }

    /**
     * Setup a spawner.
     * @param location the location to set it up at.
     * @param world the world to place it in.
     * @param mob the mob to spawn.
     */
    private static void setupSpawner(final BlockPos location, final World world, final ResourceLocation mob)
    {
        world.setBlockState(location, Blocks.MOB_SPAWNER.getDefaultState());
        final TileEntityMobSpawner spawner = new TileEntityMobSpawner();
        spawner.getSpawnerBaseLogic().setEntityId(mob);

        world.setTileEntity(location, spawner);
    }


    private static void buildPlatform(final BlockPos target, final World world)
    {
        final IBlockState platformBlock = Blocks.WOODEN_SLAB.getDefaultState();

        for (int z = 0; z < 5; z++)
        {
            for (int x = 0; x < 5; x++)
            {
                int sum = x * x + z * z;
                if (sum < (5 * 5) / 4)
                {
                    world.setBlockState(new BlockPos(target.getX() + x, target.getY()-1, target.getZ() + z), platformBlock);
                    world.setBlockState(new BlockPos(target.getX() + x, target.getY()-1, target.getZ() -z), platformBlock);
                    world.setBlockState(new BlockPos(target.getX() -x, target.getY()-1, target.getZ() + z), platformBlock);
                    world.setBlockState(new BlockPos(target.getX() -x, target.getY()-1, target.getZ() -z), platformBlock);
                }
            }
        }
    }

    /**
     * Sets the number of spawns for each barbarian type
     *
     * @param colony The colony to get the RaidLevel from
     * @return the total horde strength.
     */
    private static Horde numberOfSpawns(final Colony colony)
    {
        if (colony.getCitizenManager().getCitizens().size() < MIN_CITIZENS_FOR_RAID)
        {
            return new Horde(0, 0, 0, 0);
        }

        final int raidLevel = Math.min(Configurations.gameplay.maxBarbarianSize,(int) ((getColonyRaidLevel(colony) / SPAWN_MODIFIER) * ((double) Configurations.gameplay.spawnBarbarianSize * 0.1)));
        final int numberOfChiefs = Math.max(1, (int) (raidLevel * CHIEF_BARBARIANS_MULTIPLIER));
        final int numberOfArchers = Math.max(1,(int) (raidLevel * ARCHER_BARBARIANS_MULTIPLIER));
        final int numberOfBarbarians = raidLevel - numberOfChiefs - numberOfArchers;

        return new Horde(raidLevel, numberOfBarbarians, numberOfArchers, numberOfChiefs);
    }

    /**
     * Calculate a random spawn point along the colony's border
     *
     * @param world  in the world.
     * @param colony the Colony to spawn the barbarians near.
     * @return Returns the random blockPos
     */
    private static BlockPos calculateSpawnLocation(final World world, @NotNull final Colony colony)
    {
        final Random random = new Random();
        final BlockPos pos = colony.getBarbManager().getRandomOutsiderInDirection(
          random.nextInt(2) < 1 ? EnumFacing.EAST : EnumFacing.WEST,
          random.nextInt(2) < 1 ? EnumFacing.NORTH : EnumFacing.SOUTH);

        if (pos.equals(colony.getCenter()))
        {
            Log.getLogger().info("Spawning at colony center: " + colony.getCenter().getX() + " " + colony.getCenter().getZ());
            return colony.getCenter();
        }

        return BlockPosUtil.findLand(pos, world);
    }

    /**
     * Takes a colony and spits out that colony's RaidLevel.
     *
     * @param colony The colony to use
     * @return an int describing the raid level
     */
    public static int getColonyRaidLevel(final Colony colony)
    {
        int levels = 0;

        @NotNull final List<CitizenData> citizensList = new ArrayList<>();
        citizensList.addAll(colony.getCitizenManager().getCitizens());

        for (@NotNull final CitizenData citizen : citizensList)
        {
            levels += citizen.getLevel();
        }

        return levels;
    }

    public static boolean isItTimeToRaid(final World world, final Colony colony)
    {
        if (colony.getCitizenManager().getCitizens().size() < NUMBER_OF_CITIZENS_NEEDED)
        {
            return false;
        }

        if (world.isDaytime() && !colony.isHasRaidBeenCalculated())
        {
            colony.getBarbManager().setHasRaidBeenCalculated(true);
            if (!colony.hasWillRaidTonight())
            {
                final boolean raid = raidThisNight(world, colony);
                if (Configurations.gameplay.enableInDevelopmentFeatures)
                {
                    LanguageHandler.sendPlayersMessage(
                      colony.getMessageEntityPlayers(),
                      "Will raid tonight: " + raid);
                }
                colony.getBarbManager().setWillRaidTonight(raid);
            }
            return false;
        }
        else if (colony.hasWillRaidTonight() && !world.isDaytime() && colony.isHasRaidBeenCalculated())
        {
            colony.getBarbManager().setHasRaidBeenCalculated(false);
            colony.getBarbManager().setWillRaidTonight(false);
            if (Configurations.gameplay.enableInDevelopmentFeatures)
            {
                LanguageHandler.sendPlayersMessage(
                  colony.getMessageEntityPlayers(),
                  "Night reached: raiding");
            }
            return true;
        }
        else if (!world.isDaytime() && colony.isHasRaidBeenCalculated())
        {
            colony.getBarbManager().setHasRaidBeenCalculated(false);
        }

        return false;
    }

    /**
     * Returns whether a raid should happen depending on the Config
     *
     * @param world The world in which the raid is possibly happening (Used to get a random number easily)
     * @return Boolean value on whether to act this night
     */
    private static boolean raidThisNight(final World world, final Colony colony)
    {
        return colony.getNightsSinceLastRaid() > Configurations.gameplay.minimumNumberOfNightsBetweenRaids
                && world.rand.nextDouble() < 1.0 / Configurations.gameplay.averageNumberOfNightsBetweenRaids;
    }

    /**
     * Class representing a horde attack.
     */
    private static class Horde
    {
        private final int numberOfBarbarians;
        private final int numberOfArchers;
        private final int numberOfChiefs;
        private final int hordeSize;

        public Horde(final int hordeSize, final int numberOfBarbarians, final int numberOfArchers, final int numberOfChiefs)
        {
            this.hordeSize = hordeSize;
            this.numberOfBarbarians = numberOfBarbarians;
            this.numberOfArchers = numberOfArchers;
            this.numberOfChiefs = numberOfChiefs;
        }
    }
}
