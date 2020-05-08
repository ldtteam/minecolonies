package com.minecolonies.coremod.colony.managers;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.colony.colonyEvents.IColonyEvent;
import com.minecolonies.api.colony.colonyEvents.IColonyRaidEvent;
import com.minecolonies.api.colony.managers.interfaces.IRaiderManager;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingGuardTower;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingHome;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.amazonevent.AmazonRaidEvent;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.babarianEvent.Horde;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.babarianEvent.BarbarianRaidEvent;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.egyptianevent.EgyptianRaidEvent;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent.PirateEventUtils;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent.PirateRaidEvent;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent.ShipSize;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.util.constant.ColonyConstants.BIG_HORDE_SIZE;
import static com.minecolonies.api.util.constant.ColonyConstants.NUMBER_OF_CITIZENS_NEEDED;

/**
 * Handles spawning hostile raid events.
 */
public class RaidManager implements IRaiderManager
{
    /**
     * Spawn modifier to decrease the spawn-rate.
     */
    public static final double SPAWN_MODIFIER = 5;

    /**
     * Min distance to keep while spawning near buildings
     */
    private static final int MIN_BUILDING_SPAWN_DIST = 35;

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
            if (targetSpawnPoint == null || targetSpawnPoint.equals(colony.getCenter()) || targetSpawnPoint.getY() > MineColonies.getConfig().getCommon().maxYForBarbarians.get())
            {
                continue;
            }

            spawnPoints.add(targetSpawnPoint);
        }

        if (spawnPoints.isEmpty())
        {
            return;
        }

        amount = (int) Math.ceil((float) amount / spawnPoints.size());

        for (final BlockPos targetSpawnPoint : spawnPoints)
        {
            if (MineColonies.getConfig().getCommon().enableInDevelopmentFeatures.get())
            {
                LanguageHandler.sendPlayersMessage(
                  colony.getMessagePlayerEntities(),
                  "Horde Spawn Point: " + targetSpawnPoint);
            }

            // No rotation till spawners are moved into schematics
            final int pirateShipRotation = 0;
            if (PirateEventUtils.canSpawnPirateEventAt(colony, targetSpawnPoint, amount, pirateShipRotation))
            {
                final PirateRaidEvent event = new PirateRaidEvent(colony);
                event.setSpawnPoint(targetSpawnPoint);
                event.setShipSize(ShipSize.getShipForRaidLevel(amount));
                event.setShipRotation(pirateShipRotation);
                colony.getEventManager().addEvent(event);
            }
            else if (colony.getWorld().getBiome(colony.getCenter()).getRegistryName().getPath().contains("desert")
                       || colony.getWorld().getBiome(targetSpawnPoint).getRegistryName().getPath().contains("desert"))
            {
                final EgyptianRaidEvent event = new EgyptianRaidEvent(colony);
                event.setSpawnPoint(targetSpawnPoint);
                event.setHorde(new Horde(amount));
                colony.getEventManager().addEvent(event);
            }
            else if (colony.getWorld().getBiome(colony.getCenter()).getRegistryName().getPath().contains("jungle")
                       || colony.getWorld().getBiome(targetSpawnPoint).getRegistryName().getPath().contains("jungle"))
            {
                final AmazonRaidEvent event = new AmazonRaidEvent(colony);
                event.setSpawnPoint(targetSpawnPoint);
                event.setHorde(new Horde(amount));
                colony.getEventManager().addEvent(event);
            }
            else
            {
                final BarbarianRaidEvent event = new BarbarianRaidEvent(colony);
                event.setSpawnPoint(targetSpawnPoint);
                event.setHorde(new Horde(amount));
                colony.getEventManager().addEvent(event);
            }
            addRaiderSpawnPoint(targetSpawnPoint);
        }
        colony.markDirty();
    }

    private static final int MIN_RAID_CHUNK_DIST_CENTER = 5;

    /**
     * Calculate a random spawn point along the colony's border
     *
     * @return Returns the random blockPos
     */
    @Override
    public BlockPos calculateSpawnLocation()
    {
        List<IBuilding> loadedBuildings = new ArrayList<>();
        BlockPos locationSum = new BlockPos(0, 0, 0);
        int amount = 0;

        for (final IBuilding building : colony.getBuildingManager().getBuildings().values())
        {
            if (colony.getWorld().isBlockPresent(building.getPosition()))
            {
                loadedBuildings.add(building);
                amount++;
                locationSum = locationSum.add(building.getPosition());
            }
        }

        if (amount == 0)
        {
            Log.getLogger().info("Trying to spawn raid on colony with no loaded buildings, aborting!");
            return null;
        }

        // Calculate center on loaded buildings, to find a nice distance for raiders
        BlockPos calcCenter = new BlockPos(locationSum.getX() / amount, locationSum.getY() / amount, locationSum.getZ() / amount);

        final Random random = colony.getWorld().rand;

        BlockPos spawnPos = null;

        Direction direction1 = random.nextInt(2) < 1 ? Direction.EAST : Direction.WEST;
        Direction direction2 = random.nextInt(2) < 1 ? Direction.NORTH : Direction.SOUTH;

        for (int i = 0; i < 4; i++)
        {
            if (i > 0)
            {
                direction1 = direction1.rotateY();
                direction2 = direction2.rotateY();
            }

            spawnPos = findSpawnPointInDirections(calcCenter, direction1, direction2, loadedBuildings);
            if (spawnPos != null)
            {
                break;
            }
        }

        if (spawnPos == null)
        {
            return null;
        }

        return BlockPosUtil.findLand(spawnPos, colony.getWorld());
    }

    private final static int RAID_SPAWN_SEARCH_CHUNKS = 10;

    /**
     * Finds a spawnpoint randomly in a circular shape around the center Advances
     *
     * @param center          the center of the area to search for a spawn point
     * @param dir1            the first of the directions to look in
     * @param dir2            the second of the directions to look in
     * @param loadedBuildings a list of loaded buildings
     * @return the calculated position
     */
    private BlockPos findSpawnPointInDirections(final BlockPos center, final Direction dir1, final Direction dir2, final List<IBuilding> loadedBuildings)
    {
        final Random random = colony.getWorld().rand;

        BlockPos spawnPos = new BlockPos(center);

        // Do the min offset
        for (int i = 1; i <= MIN_RAID_CHUNK_DIST_CENTER; i++)
        {
            if (random.nextBoolean())
            {
                spawnPos = spawnPos.offset(dir1, 16);
            }
            else
            {
                spawnPos = spawnPos.offset(dir2, 16);
            }
        }

        BlockPos tempPos = new BlockPos(spawnPos);

        // Check if loaded
        if (colony.getWorld().isBlockPresent(spawnPos))
        {
            for (int i = 1; i <= random.nextInt(RAID_SPAWN_SEARCH_CHUNKS - 3) + 3; i++)
            {
                // Choose random between our two directions
                if (random.nextBoolean())
                {
                    if (colony.getWorld().isBlockPresent(tempPos.offset(dir1, 16)))
                    {
                        if (isValidSpawnPoint(tempPos.offset(dir1, 16), loadedBuildings))
                        {
                            spawnPos = tempPos.offset(dir1, 16);
                        }
                        tempPos = tempPos.offset(dir1, 16);
                    }
                    else
                    {
                        break;
                    }
                }
                else
                {
                    if (colony.getWorld().isBlockPresent(tempPos.offset(dir2, 16)))
                    {
                        if (isValidSpawnPoint(tempPos.offset(dir2, 16), loadedBuildings))
                        {
                            spawnPos = tempPos.offset(dir2, 16);
                        }
                        tempPos = tempPos.offset(dir2, 16);
                    }
                    else
                    {
                        break;
                    }
                }
            }

            if (isValidSpawnPoint(spawnPos, loadedBuildings))
            {
                return spawnPos;
            }
        }

        return null;
    }

    /**
     * Determines whether the given spawn point is allowed.
     *
     * @param spawnPos        the spawn point to check
     * @param loadedBuildings the loaded buildings
     * @return true if valid
     */
    private boolean isValidSpawnPoint(final BlockPos spawnPos, final List<IBuilding> loadedBuildings)
    {
        for (final IBuilding building : loadedBuildings)
        {
            if (building.getBuildingLevel() == 0)
            {
                continue;
            }

            int minDist = MIN_BUILDING_SPAWN_DIST;

            // Additional raid protection for certain buildings, towers can be used now to deal with unlucky - inwall spawns
            if (building instanceof BuildingGuardTower)
            {
                // 47/59/71/83/95
                minDist += building.getBuildingLevel() * 12;
            }
            else if (building instanceof BuildingHome)
            {
                // 39/43/47/51/55
                minDist += building.getBuildingLevel() * 4;
            }
            else if (building instanceof BuildingTownHall)
            {
                // 43/51/59/67/75
                minDist += building.getBuildingLevel() * 8;
            }
            else
            {
                // 37/39/41/43/45
                minDist += building.getBuildingLevel() * 2;
            }

            if (BlockPosUtil.getDistance2D(building.getPosition(), spawnPos) < minDist)
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public List<BlockPos> getLastSpawnPoints()
    {
        return new ArrayList<>(lastSpawnPoints);
    }

    /**
     * Returns the colonies babarian level
     *
     * @return the amount of barbarians.
     */
    @Override
    public int calcBarbarianAmount()
    {
        return Math.min(MineColonies.getConfig().getCommon().maxBarbarianSize.get(),
          (int) ((getColonyRaidLevel() / SPAWN_MODIFIER) * ((double) MineColonies.getConfig().getCommon().spawnBarbarianSize.get() * 0.2)));
    }

    /**
     * Check if a certain vector matches two directions.
     *
     * @param directionX the direction x.
     * @param directionZ the direction z.
     * @param vector     the vector.
     * @return true if so.
     */
    private static boolean isInDirection(final Direction directionX, final Direction directionZ, final BlockPos vector)
    {
        return Direction.getFacingFromVector(vector.getX(), 0, 0) == directionX && Direction.getFacingFromVector(0, 0, vector.getZ()) == directionZ;
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
     * 
     * @return whether a raid is possible
     */
    @Override
    public boolean canRaid()
    {
        return colony.getWorld().getDifficulty() != Difficulty.PEACEFUL
                 && MineColonies.getConfig().getCommon().doBarbariansSpawn.get()
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
                if (MineColonies.getConfig().getCommon().enableInDevelopmentFeatures.get())
                {
                    LanguageHandler.sendPlayersMessage(
                      colony.getImportantMessageEntityPlayers(),
                      "Will raid tonight: " + raid);
                }
                colony.getRaiderManager().setWillRaidTonight(raid);

                if (colony.getWorld().getBiome(colony.getCenter()).getRegistryName().getPath().contains("desert") && colony.getWorld().isRaining())
                {
                    return true;
                }
            }
            return false;
        }
        else if (colony.getRaiderManager().willRaidTonight() && !colony.getWorld().isDaytime() && colony.getRaiderManager().hasRaidBeenCalculated())
        {
            colony.getRaiderManager().setHasRaidBeenCalculated(false);
            colony.getRaiderManager().setWillRaidTonight(false);
            if (MineColonies.getConfig().getCommon().enableInDevelopmentFeatures.get())
            {
                LanguageHandler.sendPlayersMessage(
                  colony.getMessagePlayerEntities(),
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
            levels += citizen.getJobModifier() / 5;
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
     * @param world  The world in which the raid is possibly happening (Used to get a random number easily)
     * @param colony The colony to raid
     * @return Boolean value on whether to act this night
     */
    private boolean raidThisNight(final World world, final IColony colony)
    {
        if (nightsSinceLastRaid < MineColonies.getConfig().getCommon().minimumNumberOfNightsBetweenRaids.get())
        {
            return false;
        }

        if (nightsSinceLastRaid > MineColonies.getConfig().getCommon().averageNumberOfNightsBetweenRaids.get() + 2)
        {
            return true;
        }

        return world.rand.nextDouble() < 1.0 / (MineColonies.getConfig().getCommon().averageNumberOfNightsBetweenRaids.get() - MineColonies.getConfig()
                                                                                                                                 .getCommon().minimumNumberOfNightsBetweenRaids.get());
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
