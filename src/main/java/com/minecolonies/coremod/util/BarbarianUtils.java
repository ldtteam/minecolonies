package com.minecolonies.coremod.util;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.entity.ai.mobs.AbstractEntityBarbarian;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Utils for the Barbarians
 */
public final class BarbarianUtils
{
    private static final String BARBARIAN = "minecolonies.Barbarian";
    private static final String ARCHER    = "minecolonies.ArcherBarbarian";
    private static final String CHIEF     = "minecolonies.ChiefBarbarian";

    private static int numberOfBarbarians = 0;
    private static int numberOfArchers    = 0;
    private static int numberOfChiefs     = 0;

    private static final int MAX_SIZE = Configurations.maxBarbarianHordeSize;

    private static final double BARBARIANS_MULTIPLIER        = 0.5;
    private static final double ARCHER_BARBARIANS_MULTIPLIER = 0.25;
    private static final double CHIEF_BARBARIANS_MULTIPLIER  = 0.1;

    private static final int PREFERRED_MAX_HORDE_SIZE = 40;
    private static final int PREFERRED_MAX_BARBARIANS = 22;
    private static final int PREFERRED_MAX_ARCHERS    = 16;
    private static final int PREFERRED_MAX_CHIEFS     = 2;

    private static final float WHOLE_CIRCLE  = 360.0F;
    private static final float HALF_A_CIRCLE = 180F;

    private static final double BARBARIAN_Y_COORD_SEARCH = 3.0D;

    /**
     * Private constructor to hide the implicit public one.
     */
    private BarbarianUtils()
    {
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
        citizensList.addAll(colony.getCitizens().values());

        for (@NotNull final CitizenData citizen : citizensList)
        {
            if (citizen.getJob() != null && citizen.getWorkBuilding() != null)
            {
                final int buildingLevel = citizen.getWorkBuilding().getBuildingLevel();
                levels += buildingLevel;
            }
        }

        if (colony.getTownHall() != null)
        {
            return (levels + colony.getTownHall().getBuildingLevel());
        }
        else
        {
            return 0;
        }
    }

    /**
     * Returns the closest barbarian to an entity.
     *
     * @param entity             The entity to test against
     * @param distanceFromEntity The distance to check for
     * @return the barbarian (if any) that is nearest
     */
    public static AbstractEntityBarbarian getClosestBarbarianToEntity(final Entity entity, final double distanceFromEntity)
    {
        final Optional<AbstractEntityBarbarian> barbarian = getBarbariansCloseToEntity(entity, distanceFromEntity).stream().findFirst();
        return barbarian.orElse(null);
    }

    /**
     * Returns the barbarians close to an entity.
     *
     * @param entity             The entity to test against
     * @param distanceFromEntity The distance to check for
     * @return the barbarians (if any) that is nearest
     */
    public static List<AbstractEntityBarbarian> getBarbariansCloseToEntity(final Entity entity, final double distanceFromEntity)
    {
        return CompatibilityUtils.getWorld(entity).getEntitiesWithinAABB(
          AbstractEntityBarbarian.class,
          entity.getEntityBoundingBox().expand(
            distanceFromEntity,
            BARBARIAN_Y_COORD_SEARCH,
            distanceFromEntity),
          Entity::isEntityAlive);
    }

    /**
     * Enacts the Raid Event.
     *
     * @param colony The colony for which the raid is occurring in.
     */
    public static void eventRaid(final Colony colony)
    {
        final World world = colony.getWorld();

        if (world == null)
        {
            return;
        }

        final boolean isDay = world.isDaytime();

        if (isDay)
        {
            if (colony.isHasRaided())
            {
                colony.setRaidWillHappen(raidThisNight(world));
            }
            colony.setHasRaided(false);
        }
        else if (!colony.isHasRaided())
        {
            if (colony.isWillRaid() && Configurations.doBarbariansSpawn && (world.getDifficulty() != EnumDifficulty.PEACEFUL))
            {
                numberOfSpawns(colony);

                final BlockPos targetSpawnPoint = calculateSpawnLocation(world, colony);

                if (Configurations.enableInDevelopmentFeatures)
                {
                    LanguageHandler.sendPlayersMessage(
                      colony.getMessageEntityPlayers(),
                      "Horde Spawn Point: " + targetSpawnPoint);
                }

                LanguageHandler.sendPlayersMessage(
                  colony.getMessageEntityPlayers(),
                  "event.minecolonies.raidMessage");

                BarbarianSpawnUtils.spawn(BARBARIAN, numberOfBarbarians, targetSpawnPoint, world);
                BarbarianSpawnUtils.spawn(ARCHER, numberOfArchers, targetSpawnPoint, world);
                BarbarianSpawnUtils.spawn(CHIEF, numberOfChiefs, targetSpawnPoint, world);
            }
            colony.setHasRaided(true);
        }
    }

    /**
     * Sets the number of spawns for each barbarian type
     *
     * @param colony The colony to get the RaidLevel from
     */
    private static void numberOfSpawns(final Colony colony)
    {
        final int raidLevel = getColonyRaidLevel(colony);

        numberOfBarbarians = (int) (BARBARIANS_MULTIPLIER * raidLevel);
        numberOfArchers = (int) (ARCHER_BARBARIANS_MULTIPLIER * raidLevel);
        numberOfChiefs = (int) (CHIEF_BARBARIANS_MULTIPLIER * raidLevel);

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

    /**
     * Calculate a random spawn point along the colony's border
     *
     * @param world  in the world.
     * @param colony the Colony to spawn the barbarians near.
     * @return Returns the random blockPos
     */
    private static BlockPos calculateSpawnLocation(final World world, final Colony colony)
    {
        final ColonyView colonyView = ColonyManager.getClosestColonyView(world, colony.getCenter());
        if (colonyView == null)
        {
            return null;
        }
        final BlockPos center = colonyView.getCenter();
        final int radius = Configurations.workingRangeTownHall;

        final int randomDegree = world.rand.nextInt((int) WHOLE_CIRCLE);

        final double rads = (double) randomDegree / HALF_A_CIRCLE * Math.PI;
        final double x = Math.round(center.getX() + radius * Math.sin(rads));
        final double z = Math.round(center.getZ() + radius * Math.cos(rads));
        return world.getTopSolidOrLiquidBlock(new BlockPos(x, center.getY(), z));
    }

    /**
     * Returns whether a raid should happen depending on the Config
     *
     * @param world The world in which the raid is possibly happening (Used to get a random number easily)
     * @return Boolean value on whether to act this night
     */
    private static boolean raidThisNight(final World world)
    {
        final float chance = (float) 1 / Configurations.averageNumberOfNightsBetweenRaids;
        final float randomFloat = world.rand.nextFloat();
        return randomFloat < chance;
    }
}
