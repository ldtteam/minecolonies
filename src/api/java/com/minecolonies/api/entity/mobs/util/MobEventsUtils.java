package com.minecolonies.api.entity.mobs.util;

import com.ldtteam.structures.helpers.Structure;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.util.LanguageHandler;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.minecolonies.api.util.constant.ColonyConstants.*;

/**
 * Utils for Colony mob events
 */
public final class MobEventsUtils
{
    /**
     * Spawn modifier to decrease the spawn-rate.
     */
    private static final int SPAWN_MODIFIER = 3;

    /**
     * Private constructor to hide the implicit public one.
     */
    private MobEventsUtils()
    {
    }

    public static void raiderEvent(final World world, final IColony colony)
    {
        if (world == null || !colony.isCanHaveBarbEvents())
        {
            return;
        }

        final Horde horde = createNewHordeFor(colony);
        final int hordeSize = horde.hordeSize;
        if (hordeSize == 0)
        {
            return;
        }

        BlockPos targetSpawnPoint = calculateSpawnLocation(world, colony);
        Log.getLogger().info("[BarbarianEvent]: Spawning: " + targetSpawnPoint.getX() + " " + targetSpawnPoint.getZ());
        if (targetSpawnPoint.equals(colony.getCenter()) || targetSpawnPoint.getY() > MinecoloniesAPIProxy.getInstance().getConfig().getCommon().maxYForBarbarians.get())
        {
            return;
        }

        if (MinecoloniesAPIProxy.getInstance().getConfig().getCommon().enableInDevelopmentFeatures.get())
        {
            LanguageHandler.sendPlayersMessage(
              colony.getMessagePlayerEntitys(),
              "Horde Spawn Point: " + targetSpawnPoint);
        }
        colony.getRaiderManager().addRaiderSpawnPoint(targetSpawnPoint);
        colony.markDirty();

        HordeSizeCalculator hordeSizeCalculator = new HordeSizeCalculator(hordeSize).invoke();
        int raidNumber = hordeSizeCalculator.getRaidNumber();
        String shipSize = hordeSizeCalculator.getShipSize();

        colony.setNightsSinceLastRaid(0);

        // Calculate size/offset of the pirate ship
        final Structure structure = new Structure(world, Structures.SCHEMATICS_PREFIX + PirateEventUtils.PIRATESHIP_FOLDER + shipSize, new PlacementSettings());
        structure.rotate(BlockPosUtil.getRotationFromRotations(0), world, targetSpawnPoint, Mirror.NONE);

        targetSpawnPoint = PlaceStructureAndFirePirateEvent(world, colony, targetSpawnPoint, raidNumber, shipSize, structure);
        if (targetSpawnPoint == null)
        {
            return;
        }

        BarbarianEventUtils.barbarianEvent(world, colony, targetSpawnPoint, raidNumber, horde);
    }

    /**
     * Checks if a raid is possible
     */
    public static boolean shouldRaid(final IColony colony)
    {
        return colony.getWorld().getDifficulty() != Difficulty.PEACEFUL
                 && MinecoloniesAPIProxy.getInstance().getConfig().getCommon().doBarbariansSpawn.get()
                 && colony.getRaiderManager().canHaveRaiderEvents()
                 && !colony.getPackageManager().getImportantColonyPlayers().isEmpty()
                 && MobEventsUtils.isItTimeToRaid(colony.getWorld(), colony);
    }

    /**
     * Try to start a raid on a colony
     *
     * @param colony colony to raid
     */
    public static void tryToRaidColony(final IColony colony)
    {
        if (shouldRaid(colony))
        {
            MobEventsUtils.raiderEvent(colony.getWorld(), colony);
        }
    }

    /**
     * Sets the number of spawns for each barbarian type
     *
     * @param colony The colony to get the RaidLevel from
     * @return the total horde strength.
     */
    private static Horde createNewHordeFor(final IColony colony)
    {
        if (colony.getCitizenManager().getCitizens().size() < MIN_CITIZENS_FOR_RAID)
        {
            return new Horde(0, 0, 0, 0);
        }

        final int raidLevel =
          Math.min(MinecoloniesAPIProxy.getInstance().getConfig().getCommon().maxBarbarianSize.get(), (int) ((getColonyRaidLevel(colony) / SPAWN_MODIFIER) * ((double) MinecoloniesAPIProxy.getInstance().getConfig().getCommon().spawnBarbarianSize.get() * 0.1)));
        final int numberOfChiefs = Math.max(1, (int) (raidLevel * CHIEF_BARBARIANS_MULTIPLIER));
        final int numberOfArchers = Math.max(1, (int) (raidLevel * ARCHER_BARBARIANS_MULTIPLIER));
        final int numberOfBarbarians = raidLevel - numberOfChiefs - numberOfArchers;

        return new Horde(raidLevel, numberOfBarbarians, numberOfArchers, numberOfChiefs);
    }

    /**
     * Returns true when most parts of the given area are water, > 90%
     *
     * @param world Blockacces to use
     * @param from  First corner of search rectangle
     * @param to    Second corner of search rectangle
     * @return true if enough water surface blocks are found
     */
    public static boolean isSurfaceAreaMostlyWater(
      @NotNull final IWorld world,
      @NotNull final BlockPos from,
      @NotNull final BlockPos to,
      @NotNull final double percentRequired)
    {
        final int xDist = Math.abs(from.getX() - to.getX());
        final int zDist = Math.abs(from.getZ() - to.getZ());

        int nonWaterBlocks = 0;
        final int neededWaterBlocks = (int) (percentRequired * (xDist * zDist));
        final int nonWaterBlockThreshold = (xDist * zDist) - neededWaterBlocks;

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
                if (world.getBlockState(from.add(x * xDir, 0, z * zDir)).getMaterial() != Material.WATER || !world.isAirBlock(from.add(x * xDir, 1, z * zDir)))
                {
                    nonWaterBlocks++;
                    // Skip when we already found too many non water blocks
                    if (nonWaterBlocks > nonWaterBlockThreshold)
                    {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Nullable
    private static BlockPos PlaceStructureAndFirePirateEvent(
      final World world,
      final IColony colony,
      final BlockPos targetSpawnPoint,
      final int raidNumber,
      final String shipSize,
      final Structure structure)
    {
        BlockPos spawnPoint = targetSpawnPoint;

        if ((world.getBlockState(targetSpawnPoint).getMaterial() == Material.WATER && isSurfaceAreaMostlyWater(world,
          targetSpawnPoint.add(-structure.getOffset().getX(), 0, -structure.getOffset().getZ()),
          targetSpawnPoint.add(structure.getWidth() - 1, 0, structure.getLength() - 1).subtract(structure.getOffset()),
          0.8))
              || (world.getBlockState(targetSpawnPoint.down()).getMaterial() == Material.WATER && isSurfaceAreaMostlyWater(world,
          targetSpawnPoint.add(-structure.getOffset().getX(), 0, -structure.getOffset().getZ()).down(),
          targetSpawnPoint.add(structure.getWidth() - 1, structure.getHeight(), structure.getLength() - 1).subtract(structure.getOffset()).down(),
          0.8))
        )
        {
            if (world.getBlockState(targetSpawnPoint).getMaterial() != Material.WATER)
            {
                spawnPoint = targetSpawnPoint.down();
            }
            PirateEventUtils.pirateEvent(targetSpawnPoint, world, colony, shipSize, raidNumber);
            return null;
        }
        return spawnPoint;
    }

    /**
     * Calculate a random spawn point along the colony's border
     *
     * @param world  in the world.
     * @param colony the Colony to spawn the barbarians near.
     * @return Returns the random blockPos
     */
    private static BlockPos calculateSpawnLocation(final World world, @NotNull final IColony colony)
    {
        final Random random = new Random();
        final BlockPos pos = colony.getRaiderManager().getRandomOutsiderInDirection(
          random.nextInt(2) < 1 ? Direction.EAST : Direction.WEST,
          random.nextInt(2) < 1 ? Direction.NORTH : Direction.SOUTH);

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
    public static int getColonyRaidLevel(final IColony colony)
    {
        int levels = 0;
        @NotNull final List<ICitizenData> citizensList = new ArrayList<>(colony.getCitizenManager().getCitizens());
        for (@NotNull final ICitizenData citizen : citizensList)
        {
            levels += citizen.getLevel();
        }

        return levels;
    }

    public static boolean isItTimeToRaid(final World world, final IColony colony)
    {
        if (colony.getCitizenManager().getCitizens().size() < NUMBER_OF_CITIZENS_NEEDED)
        {
            return false;
        }

        if (world.isDaytime() && !colony.isHasRaidBeenCalculated())
        {
            colony.getRaiderManager().setHasRaidBeenCalculated(true);
            if (!colony.hasWillRaidTonight())
            {
                final boolean raid = raidThisNight(world, colony);
                if (MinecoloniesAPIProxy.getInstance().getConfig().getCommon().enableInDevelopmentFeatures.get())
                {
                    LanguageHandler.sendPlayersMessage(
                      colony.getImportantMessageEntityPlayers(),
                      "Will raid tonight: " + raid);
                }
                colony.getRaiderManager().setWillRaidTonight(raid);
            }
            return false;
        }
        else if (colony.hasWillRaidTonight() && !world.isDaytime() && colony.isHasRaidBeenCalculated())
        {
            colony.getRaiderManager().setHasRaidBeenCalculated(false);
            colony.getRaiderManager().setWillRaidTonight(false);
            if (MinecoloniesAPIProxy.getInstance().getConfig().getCommon().enableInDevelopmentFeatures.get())
            {
                LanguageHandler.sendPlayersMessage(
                  colony.getImportantMessageEntityPlayers(),
                  "Night reached: raiding");
            }
            return true;
        }
        else if (!world.isDaytime() && colony.isHasRaidBeenCalculated())
        {
            colony.getRaiderManager().setHasRaidBeenCalculated(false);
        }

        return false;
    }

    /**
     * Returns whether a raid should happen depending on the Config
     *
     * @param world The world in which the raid is possibly happening (Used to get a random number easily)
     * @return Boolean value on whether to act this night
     */
    private static boolean raidThisNight(final World world, final IColony colony)
    {
        return colony.getNightsSinceLastRaid() > MinecoloniesAPIProxy.getInstance().getConfig().getCommon().minimumNumberOfNightsBetweenRaids.get()
                 && world.rand.nextDouble() < 1.0 / MinecoloniesAPIProxy.getInstance().getConfig().getCommon().averageNumberOfNightsBetweenRaids.get();
    }

    /**
     * Class representing a horde attack.
     */
    protected static class Horde
    {
        protected final int numberOfRaiders;
        protected final int numberOfArchers;
        protected final int numberOfBosses;
        protected final int hordeSize;

        /**
         * Create a new horde.
         *
         * @param hordeSize       the size.
         * @param numberOfRaiders the number of raiders.
         * @param numberOfArchers the number of archers.
         * @param numberOfBosses  the number of bosses.
         */
        Horde(final int hordeSize, final int numberOfRaiders, final int numberOfArchers, final int numberOfBosses)
        {
            this.hordeSize = hordeSize;
            this.numberOfRaiders = numberOfRaiders;
            this.numberOfArchers = numberOfArchers;
            this.numberOfBosses = numberOfBosses;
        }
    }

    private static class HordeSizeCalculator
    {
        private final int    hordeSize;
        private       int    raidNumber;
        private       String shipSize;

        public HordeSizeCalculator(final int hordeSize) {this.hordeSize = hordeSize;}

        public int getRaidNumber()
        {
            return raidNumber;
        }

        public String getShipSize()
        {
            return shipSize;
        }

        public HordeSizeCalculator invoke()
        {
            raidNumber = HUGE_HORDE_MESSAGE_ID;
            shipSize = BIG_PIRATE_SHIP;
            if (hordeSize < SMALL_HORDE_SIZE)
            {
                raidNumber = SMALL_HORDE_MESSAGE_ID;
                shipSize = SMALL_PIRATE_SHIP;
            }
            else if (hordeSize < MEDIUM_HORDE_SIZE)
            {
                raidNumber = MEDIUM_HORDE_MESSAGE_ID;
                shipSize = MEDIUM_PIRATE_SHIP;
            }
            else if (hordeSize < BIG_HORDE_SIZE)
            {
                raidNumber = BIG_HORDE_MESSAGE_ID;
                shipSize = MEDIUM_PIRATE_SHIP;
            }
            return this;
        }
    }
}
