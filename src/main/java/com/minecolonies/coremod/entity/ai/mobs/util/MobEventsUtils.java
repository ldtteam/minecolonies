package com.minecolonies.coremod.entity.ai.mobs.util;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.minecolonies.api.util.constant.ColonyConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.RAID_EVENT_MESSAGE;

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

        final BlockPos targetSpawnPoint = calculateSpawnLocation(world, colony);
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
        colony.getBarbManager().addBarbarianSpawnPoint(targetSpawnPoint);

        int raidNumber = HUGE_HORDE_MESSAGE_ID;
        if(hordeSize < SMALL_HORDE_SIZE)
        {
            raidNumber = SMALL_HORDE_MESSAGE_ID;
        }
        else if(hordeSize < MEDIUM_HORDE_SIZE)
        {
            raidNumber = MEDIUM_HORDE_MESSAGE_ID;
        }
        else if(hordeSize < BIG_HORDE_SIZE)
        {
            raidNumber = BIG_HORDE_MESSAGE_ID;
        }
        LanguageHandler.sendPlayersMessage(
                colony.getMessageEntityPlayers(),
                RAID_EVENT_MESSAGE + raidNumber, colony.getName());

        colony.setNightsSinceLastRaid(0);

        BarbarianSpawnUtils.spawn(BARBARIAN, horde.numberOfBarbarians, targetSpawnPoint, world);
        BarbarianSpawnUtils.spawn(ARCHER, horde.numberOfArchers, targetSpawnPoint, world);
        BarbarianSpawnUtils.spawn(CHIEF, horde.numberOfChiefs, targetSpawnPoint, world);
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

        final int raidLevel = getColonyRaidLevel(colony);

        int numberOfBarbarians = (int) (BARBARIANS_MULTIPLIER * raidLevel / SPAWN_MODIFIER);
        int numberOfArchers = (int) (ARCHER_BARBARIANS_MULTIPLIER * raidLevel / SPAWN_MODIFIER);
        int numberOfChiefs = (int) (CHIEF_BARBARIANS_MULTIPLIER * raidLevel / SPAWN_MODIFIER);

        int hordeTotal = numberOfBarbarians + numberOfArchers + numberOfChiefs;

        if (hordeTotal > PREFERRED_MAX_HORDE_SIZE && MAX_SIZE == PREFERRED_MAX_HORDE_SIZE)
        {
            //set the preferred horde style if the total spawns is greater that the config's max size
            numberOfBarbarians = PREFERRED_MAX_BARBARIANS;
            numberOfArchers = PREFERRED_MAX_ARCHERS;
            numberOfChiefs = PREFERRED_MAX_CHIEFS;
        }
        else if (hordeTotal > MAX_SIZE)
        {
            //Equalize the spawns so that there is less spawns than the config's max size
            numberOfBarbarians = equalizeBarbarianSpawns(hordeTotal, numberOfBarbarians);
            hordeTotal = numberOfArchers + numberOfBarbarians + numberOfChiefs;
            numberOfArchers = equalizeBarbarianSpawns(hordeTotal, numberOfArchers);
            hordeTotal = numberOfArchers + numberOfBarbarians + numberOfChiefs;
            numberOfChiefs = equalizeBarbarianSpawns(hordeTotal, numberOfChiefs);
        }

        return new Horde(hordeTotal, numberOfBarbarians, numberOfArchers, numberOfChiefs);
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

    /**
     * Reduces barbarian spawns to less than the maximum allowed (set via the config)
     *
     * @param total    The current horde size
     * @param numberOf The number of barbarians which we are reducing
     * @return the new number that the barbarians should be set to.
     */
    private static int equalizeBarbarianSpawns(final int total, final int numberOf)
    {
        int returnValue = numberOf;
        if (total > MAX_SIZE)
        {
            returnValue = total - MAX_SIZE;

            if (returnValue < 0)
            {
                return 0;
            }
            return returnValue;
        }
        return returnValue;
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
