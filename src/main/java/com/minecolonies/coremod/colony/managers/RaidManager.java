package com.minecolonies.coremod.colony.managers;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.colony.colonyEvents.IColonyEvent;
import com.minecolonies.api.colony.colonyEvents.IColonyRaidEvent;
import com.minecolonies.api.colony.managers.interfaces.IRaiderManager;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.babarianEvent.BarbarianHorde;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.babarianEvent.BarbarianRaidEvent;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent.PirateEventUtils;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent.PirateRaidEvent;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent.ShipSize;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.ColonyConstants.*;
import static com.minecolonies.api.util.constant.Constants.HALF_A_CIRCLE;
import static com.minecolonies.api.util.constant.Constants.WHOLE_CIRCLE;

/**
 * Handles spawning hostile raid events.
 */
public class RaidManager implements IRaiderManager
{
    /**
     * Spawn modifier to decrease the spawn-rate.
     */
    public static final double SPAWN_MODIFIER = 1.5;

    /**
     * Whether there will be a raid in this colony tonight.
     */
    private boolean raidTonight = false;

    /**
     * Whether or not the raid has been calculated for today.
     */
    private boolean raidBeenCalculated = false;

    /**
     * Whether or not this colony may have Raider events. (set via command)
     */
    private boolean haveBarbEvents = true;

    /**
     * The amount of nights since the last raid.
     */
    private int nightsSinceLastRaid = 0;

    /**
     * Last raider spawnpoints.
     */
    private final List<BlockPos> lastSpawnPoints = new ArrayList<>();

    /**
     * The colony of the manager.
     */
    private final Colony colony;

    /**
     * Whether the spies are currently active, active spies mark enemies with glow.
     */
    private boolean spiesEnabled;

    /**
     * The last building position for raiders to walk to
     */
    private BlockPos lastBuilding;

    /**
     * The time the last building pos was used.
     */
    private int buildingPosUsage = 0;

    /**
     * Creates the RaidManager for a colony.
     *
     * @param colony the colony.
     */
    public RaidManager(final Colony colony)
    {
        this.colony = colony;
    }

    @Override
    public boolean canHaveRaiderEvents()
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
    public void setCanHaveRaiderEvents(final boolean canHave)
    {
        this.haveBarbEvents = canHave;
    }

    @Override
    public void addRaiderSpawnPoint(final BlockPos pos)
    {
        lastSpawnPoints.add(pos);
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
    public boolean areSpiesEnabled()
    {
        return spiesEnabled;
    }

    @Override
    public void setSpiesEnabled(final boolean enabled)
    {
        if (spiesEnabled != enabled)
        {
            colony.markDirty();
        }
        spiesEnabled = enabled;
    }

    @Override
    public void raiderEvent()
    {
        if (colony.getWorld() == null || !canRaid())
        {
            return;
        }

        int amount = calcBarbarianAmount();
        if (amount <= 0)
        {
            return;
        }

        // Splits into multiple raids if too large
        final int raidCount = Math.max(1, amount / BIG_HORDE_SIZE);

        final Set<BlockPos> spawnPoints = new HashSet<>();

        for (int i = 0; i < raidCount; i++)
        {
            final BlockPos targetSpawnPoint = calculateSpawnLocation();
            if (targetSpawnPoint.equals(colony.getCenter()) || targetSpawnPoint.getY() > Configurations.gameplay.maxYForBarbarians)
            {
                return;
            }

            spawnPoints.add(targetSpawnPoint);
        }

        amount = (int) Math.ceil((float) amount / spawnPoints.size());

        for (final BlockPos targetSpawnPoint : spawnPoints)
        {
            if (Configurations.gameplay.enableInDevelopmentFeatures)
            {
                LanguageHandler.sendPlayersMessage(
                  colony.getMessageEntityPlayers(),
                  "Horde Spawn Point: " + targetSpawnPoint);
            }

            if (PirateEventUtils.canSpawnPirateEventAt(colony, targetSpawnPoint, amount))
            {
                final PirateRaidEvent event = new PirateRaidEvent(colony);
                event.setSpawnPoint(targetSpawnPoint);
                event.setShipSize(ShipSize.getShipForRaidLevel(amount));
                colony.getEventManager().addEvent(event);
            }
            else
            {
                final BarbarianRaidEvent event = new BarbarianRaidEvent(colony);
                event.setSpawnPoint(targetSpawnPoint);
                event.setHorde(new BarbarianHorde(amount));
                colony.getEventManager().addEvent(event);
            }
            addRaiderSpawnPoint(targetSpawnPoint);
        }
        colony.markDirty();
    }

    /**
     * Calculate a random spawn point along the colony's border
     *
     * @return Returns the random blockPos
     */
    @Override
    public BlockPos calculateSpawnLocation()
    {
        final Random random = colony.getWorld().rand;
        final BlockPos pos = colony.getRaiderManager().getRandomOutsiderInDirection(
          random.nextInt(2) < 1 ? EnumFacing.EAST : EnumFacing.WEST,
          random.nextInt(2) < 1 ? EnumFacing.NORTH : EnumFacing.SOUTH);

        if (pos.equals(colony.getCenter()))
        {
            Log.getLogger().info("Spawning at colony center: " + colony.getCenter().getX() + " " + colony.getCenter().getZ());
            return colony.getCenter();
        }

        return BlockPosUtil.findLand(pos, colony.getWorld());
    }

    @Override
    public BlockPos getRandomOutsiderInDirection(final EnumFacing directionX, final EnumFacing directionZ)
    {
        final BlockPos center = colony.getCenter();
        final World world = colony.getWorld();

        if (world == null)
        {
            return center;
        }

        final List<BlockPos> positions = colony.getWayPoints().keySet().stream().filter(
          pos -> isInDirection(directionX, directionZ, pos.subtract(center))).collect(Collectors.toList());
        positions.addAll(colony.getBuildingManager().getBuildings().keySet().stream().filter(
          pos -> isInDirection(directionX, directionZ, pos.subtract(center))).collect(Collectors.toList()));

        BlockPos thePos = center;
        double distance = 0;
        IBuilding theBuilding = null;
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

        int radius = DEFAULT_SPAWN_RADIUS;
        while (radius < MAX_SPAWN_RADIUS && world.isAreaLoaded(thePos, radius))
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

        return new BlockPos(x, thePos.getY(), z);
    }

    @Override
    public List<BlockPos> getLastSpawnPoints()
    {
        return new ArrayList<>(lastSpawnPoints);
    }

    /**
     * Returns the colonies babarian level
     *
     * @return
     */
    @Override
    public int calcBarbarianAmount()
    {
        return Math.min(Configurations.gameplay.maxBarbarianSize,
          (int) ((getColonyRaidLevel() / SPAWN_MODIFIER) * ((double) Configurations.gameplay.spawnBarbarianSize * 0.1)));
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

    @Override
    public boolean isRaided()
    {
        for (final IColonyEvent event : colony.getEventManager().getEvents().values())
        {
            if (event instanceof IColonyRaidEvent && event.getStatus() == EventStatus.PROGRESSING)
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onNightFall()
    {
        if (!isRaided())
        {
            nightsSinceLastRaid++;
        }
        else
        {
            nightsSinceLastRaid = 0;
        }
    }

    @Override
    public int getNightsSinceLastRaid()
    {
        return nightsSinceLastRaid;
    }

    @Override
    public void setNightsSinceLastRaid(final int nightsSinceLastRaid)
    {
        this.nightsSinceLastRaid = nightsSinceLastRaid;
    }

    @Override
    public void tryToRaidColony(final IColony colony)
    {
        if (canRaid() && isItTimeToRaid())
        {
            raiderEvent();
        }
    }

    /**
     * Checks if a raid is possible
     */
    @Override
    public boolean canRaid()
    {
        return colony.getWorld().getDifficulty() != EnumDifficulty.PEACEFUL
                 && Configurations.gameplay.doBarbariansSpawn
                 && colony.getRaiderManager().canHaveRaiderEvents()
                 && !colony.getPackageManager().getImportantColonyPlayers().isEmpty();
    }

    @Override
    public boolean isItTimeToRaid()
    {
        if (colony.getCitizenManager().getCitizens().size() < NUMBER_OF_CITIZENS_NEEDED)
        {
            return false;
        }

        if (colony.getWorld().isDaytime() && !colony.getRaiderManager().hasRaidBeenCalculated())
        {
            colony.getRaiderManager().setHasRaidBeenCalculated(true);
            if (!colony.getRaiderManager().willRaidTonight())
            {
                final boolean raid = raidThisNight(colony.getWorld(), colony);
                if (Configurations.gameplay.enableInDevelopmentFeatures)
                {
                    LanguageHandler.sendPlayersMessage(
                      colony.getImportantMessageEntityPlayers(),
                      "Will raid tonight: " + raid);
                }
                colony.getRaiderManager().setWillRaidTonight(raid);
            }
            return false;
        }
        else if (colony.getRaiderManager().willRaidTonight() && !colony.getWorld().isDaytime() && colony.getRaiderManager().hasRaidBeenCalculated())
        {
            colony.getRaiderManager().setHasRaidBeenCalculated(false);
            colony.getRaiderManager().setWillRaidTonight(false);
            if (Configurations.gameplay.enableInDevelopmentFeatures)
            {
                LanguageHandler.sendPlayersMessage(
                  colony.getMessageEntityPlayers(),
                  "Night reached: raiding");
            }
            return true;
        }
        else if (!colony.getWorld().isDaytime() && colony.getRaiderManager().hasRaidBeenCalculated())
        {
            colony.getRaiderManager().setHasRaidBeenCalculated(false);
        }

        return false;
    }

    /**
     * Takes a colony and spits out that colony's RaidLevel.
     *
     * @return an int describing the raid level
     */
    public int getColonyRaidLevel()
    {
        int levels = 0;
        @NotNull final List<ICitizenData> citizensList = new ArrayList<>(colony.getCitizenManager().getCitizens());
        for (@NotNull final ICitizenData citizen : citizensList)
        {
            levels += citizen.getLevel() / 5;
        }

        for (final IBuilding building : colony.getBuildingManager().getBuildings().values())
        {
            levels += building.getBuildingLevel();
        }

        levels += citizensList.size();

        return levels;
    }

    /**
     * Returns whether a raid should happen depending on the Config
     *
     * @param world The world in which the raid is possibly happening (Used to get a random number easily)
     * @return Boolean value on whether to act this night
     */
    private static boolean raidThisNight(final World world, final IColony colony)
    {
        return colony.getRaiderManager().getNightsSinceLastRaid() > Configurations.gameplay.minimumNumberOfNightsBetweenRaids
                 && world.rand.nextDouble() < 1.0 / Configurations.gameplay.averageNumberOfNightsBetweenRaids;
    }

    @Override
    @NotNull
    public BlockPos getRandomBuilding()
    {
        buildingPosUsage++;
        if (buildingPosUsage > 3 || lastBuilding == null)
        {
            buildingPosUsage = 0;
            final Collection<IBuilding> buildingList = colony.getBuildingManager().getBuildings().values();
            final Object[] buildingArray = buildingList.toArray();
            if (buildingArray.length != 0)
            {
                final int rand = colony.getWorld().rand.nextInt(buildingArray.length);
                final IBuilding building = (IBuilding) buildingArray[rand];
                lastBuilding = building.getPosition();
            }
            else
            {
                lastBuilding = colony.getCenter();
            }
        }

        return lastBuilding;
    }
}
