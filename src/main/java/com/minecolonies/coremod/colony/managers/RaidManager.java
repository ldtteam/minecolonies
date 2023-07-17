package com.minecolonies.coremod.colony.managers;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.colony.colonyEvents.IColonyEvent;
import com.minecolonies.api.colony.colonyEvents.IColonyRaidEvent;
import com.minecolonies.api.colony.managers.interfaces.IRaiderManager;
import com.minecolonies.api.entity.pathfinding.PathResult;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.modules.LivingBuildingModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingGuardTower;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.HordeRaidEvent;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.amazonevent.AmazonRaidEvent;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.barbarianEvent.BarbarianRaidEvent;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.barbarianEvent.Horde;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.egyptianevent.EgyptianRaidEvent;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.norsemenevent.NorsemenRaidEvent;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.norsemenevent.NorsemenShipRaidEvent;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent.PirateGroundRaidEvent;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent.PirateRaidEvent;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent.ShipBasedRaiderUtils;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent.ShipSize;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.entity.pathfinding.Pathfinding;
import com.minecolonies.coremod.entity.pathfinding.pathjobs.PathJobRaiderPathing;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.colony.IColony.CLOSE_COLONY_CAP;
import static com.minecolonies.api.util.BlockPosUtil.DOUBLE_AIR_POS_SELECTOR;
import static com.minecolonies.api.util.BlockPosUtil.SOLID_AIR_POS_SELECTOR;
import static com.minecolonies.api.util.constant.ColonyConstants.BIG_HORDE_SIZE;
import static com.minecolonies.api.util.constant.Constants.DEFAULT_BARBARIAN_DIFFICULTY;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_NIGHTS_SINCE_LAST_RAID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_RAIDABLE;

/**
 * Handles spawning hostile raid events.
 */
public class RaidManager implements IRaiderManager
{
    /**
     * Spawn modifier to decrease the spawn-rate.
     */
    public static final double SPAWN_MODIFIER = 60;

    /**
     * Min distance to keep while spawning near buildings
     */
    private static final int MIN_BUILDING_SPAWN_DIST = 35;

    /**
     * Different biome ids.
     */
    private static final String DESERT_BIOME_ID = "desert";
    private static final String JUNGLE_BIOME_ID = "jungle";
    private static final String TAIGA_BIOME_ID  = "taiga";

    /**
     * Thresholds for reducing or increasing raid difficulty
     */
    private static final double LOST_CITIZEN_DIFF_REDUCE_PCT   = 0.15d;
    private static final double LOST_CITIZEN_DIFF_INCREASE_PCT = 0.05d;

    /**
     * Min and max for raid difficulty
     */
    private static final int MIN_RAID_DIFFICULTY = 1;
    private static final int MAX_RAID_DIFFICULTY = 14;

    /**
     * The minumum raid difficulty modifier
     */
    private static final double MIN_DIFFICULTY_MODIFIER = 0.2;

    /**
     * Difficulty nbt tag
     */
    private static final String TAG_RAID_DIFFICULTY = "difficulty";
    private static final String TAG_LOST_CITIZENS   = "lostCitizens";

    /**
     * Min required raidlevel
     */
    private static final int MIN_REQUIRED_RAIDLEVEL = 75;

    /**
     * Minimum block sqdistance to colony center allowed to spawn
     */
    private static final int MIN_RAID_BLOCK_DIST_CENTER_SQ = 5 * 5 * 16 * 16;

    /**
     * How many chunks distance a raid span searches additionally
     */
    private final static int RAID_SPAWN_SEARCH_CHUNKS = 10;

    /**
     * Percentage increased amount of spawns per player
     */
    private static final double INCREASE_PER_PLAYER = 0.05;

    /**
     * Chance to ignore biome selection
     */
    private static final int IGNORE_BIOME_CHANCE = 2;

    /**
     * THe initial raid difficulty
     */
    private static final int INITIAL_RAID_DIFFICULTY = 5;

    /**
     * The dynamic difficulty of raids for this colony
     */
    private int raidDifficulty = INITIAL_RAID_DIFFICULTY;

    /**
     * Whether there will be a raid in this colony tonight.
     */
    private boolean raidTonight = false;

    /**
     * Initial value for having barb events
     */
    private static boolean INITIAL_CAN_HAVE_BARB_EVENTS = true;

    /**
     * Whether or not this colony may have Raider events. (set via command)
     */
    private boolean haveBarbEvents = INITIAL_CAN_HAVE_BARB_EVENTS;

    /**
     * Initial nights since the last raid
     */
    private static final int INITIAL_NIGHTS_SINCE_LAST_RAID = 0;

    /**
     * The amount of nights since the last raid.
     */
    private int nightsSinceLastRaid = INITIAL_NIGHTS_SINCE_LAST_RAID;

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
     * The initially lost citizens
     */
    private static final int INITIAL_LOST_CITIZENS = 0;

    /**
     * The amount of citizens lost in a raid, two for normal citizens one for guards
     */
    private int lostCitizens = INITIAL_LOST_CITIZENS;

    /**
     * The initial next raid type
     */
    private static final String INITIAL_NEXT_RAID_TYPE = "";

    /**
     * The next raidType, or "" if the next raid should be determined from biome.
     */
    private String nextForcedType = INITIAL_NEXT_RAID_TYPE;

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
    public void setRaidNextNight(final boolean willRaid)
    {
        this.raidTonight = willRaid;
    }

    @Override
    public void setRaidNextNight(final boolean willRaid, final String raidType)
    {
        this.raidTonight = true;
        this.nextForcedType = raidType;
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
        raiderEvent("");
    }

    @Override
    public void raiderEvent(String raidType)
    {
        if (colony.getWorld() == null || !canRaid() || raidType == null)
        {
            return;
        }

        final int raidLevel = getColonyRaidLevel();
        int amount = calculateRaiderAmount(raidLevel);
        if (amount <= 0 || raidLevel < MIN_REQUIRED_RAIDLEVEL)
        {
            return;
        }

        // Splits into multiple raids if too large
        final int raidCount = Math.max(1, amount / BIG_HORDE_SIZE);

        final Set<BlockPos> spawnPoints = new HashSet<>();

        for (int i = 0; i < raidCount; i++)
        {
            final BlockPos targetSpawnPoint = calculateSpawnLocation();
            if (targetSpawnPoint == null || targetSpawnPoint.equals(colony.getCenter()) || targetSpawnPoint.getY() > MineColonies.getConfig().getServer().maxYForBarbarians.get()
                  || !colony.getWorld().getWorldBorder().isWithinBounds(targetSpawnPoint))
            {
                continue;
            }

            spawnPoints.add(targetSpawnPoint);
        }

        if (spawnPoints.isEmpty())
        {
            return;
        }

        nightsSinceLastRaid = 0;
        raidTonight = false;
        amount = (int) Math.ceil((float) amount / spawnPoints.size());

        for (final BlockPos targetSpawnPoint : spawnPoints)
        {
            if (MineColonies.getConfig().getServer().enableInDevelopmentFeatures.get())
            {
                MessageUtils.format(new TextComponent("Horde Spawn Point: " + targetSpawnPoint)).sendTo(colony).forAllPlayers();
            }

            if (colony.getWorld().getBlockState(targetSpawnPoint).getMaterial() == Material.AIR
                  && colony.getWorld().getBlockState(targetSpawnPoint.below()).getMaterial() == Material.AIR)
            {
                raidType = PirateRaidEvent.PIRATE_RAID_EVENT_TYPE_ID.getPath();
            }

            // No rotation till spawners are moved into schematics
            final int shipRotation = new Random().nextInt(3);
            final String homeBiomePath = colony.getWorld().getBiome(colony.getCenter()).value().getBiomeCategory().getName();
            final int rand = colony.getWorld().random.nextInt(100);
            if ((raidType.isEmpty() && (homeBiomePath.contains(TAIGA_BIOME_ID) || rand < IGNORE_BIOME_CHANCE)
                   || raidType.equals(NorsemenRaidEvent.NORSEMEN_RAID_EVENT_TYPE_ID.getPath()))
                  && ShipBasedRaiderUtils.canSpawnShipAt(colony,
              targetSpawnPoint,
              amount,
              shipRotation,
              NorsemenShipRaidEvent.SHIP_NAME))
            {
                final NorsemenShipRaidEvent event = new NorsemenShipRaidEvent(colony);
                event.setSpawnPoint(targetSpawnPoint);
                event.setShipSize(ShipSize.getShipForRaiderAmount(amount));
                event.setShipRotation(shipRotation);
                event.setSpawnPath(createSpawnPath(targetSpawnPoint));
                colony.getEventManager().addEvent(event);
            }
            else if (ShipBasedRaiderUtils.canSpawnShipAt(colony, targetSpawnPoint, amount, shipRotation, PirateRaidEvent.SHIP_NAME)
                       && (raidType.isEmpty() || raidType.equals(PirateRaidEvent.PIRATE_RAID_EVENT_TYPE_ID.getPath())))
            {
                final PirateRaidEvent event = new PirateRaidEvent(colony);
                event.setSpawnPoint(targetSpawnPoint);
                event.setShipSize(ShipSize.getShipForRaiderAmount(amount));
                event.setShipRotation(shipRotation);
                event.setSpawnPath(createSpawnPath(targetSpawnPoint));
                colony.getEventManager().addEvent(event);
            }
            else
            {
                final String biomePath = colony.getWorld().getBiome(targetSpawnPoint).value().getBiomeCategory().getName().toLowerCase();
                final HordeRaidEvent event;
                if (((biomePath.contains(DESERT_BIOME_ID) || (rand > IGNORE_BIOME_CHANCE && rand < IGNORE_BIOME_CHANCE * 2))
                       && raidType.isEmpty()) || raidType.equals(EgyptianRaidEvent.EGYPTIAN_RAID_EVENT_TYPE_ID.getPath()))
                {
                    event = new EgyptianRaidEvent(colony);
                }
                else if (((biomePath.contains(JUNGLE_BIOME_ID) || (rand > IGNORE_BIOME_CHANCE * 2 && rand < IGNORE_BIOME_CHANCE * 3)
                                                                    && raidType.isEmpty())) || (raidType.equals(AmazonRaidEvent.AMAZON_RAID_EVENT_TYPE_ID.getPath())))
                {
                    event = new AmazonRaidEvent(colony);
                }
                else if (((biomePath.contains(TAIGA_BIOME_ID) || (rand > IGNORE_BIOME_CHANCE * 3 && rand < IGNORE_BIOME_CHANCE * 4))
                            && raidType.isEmpty()) || raidType.equals(NorsemenRaidEvent.NORSEMEN_RAID_EVENT_TYPE_ID.getPath()))
                {
                    event = new NorsemenRaidEvent(colony);
                }
                else if (raidType.equals(PirateRaidEvent.PIRATE_RAID_EVENT_TYPE_ID.getPath()))
                {
                    event = new PirateGroundRaidEvent(colony);
                }
                else
                {
                    event = new BarbarianRaidEvent(colony);
                }

                event.setSpawnPoint(targetSpawnPoint);
                event.setHorde(new Horde(amount));

                event.setSpawnPath(createSpawnPath(targetSpawnPoint));
                colony.getEventManager().addEvent(event);
            }

            addRaiderSpawnPoint(targetSpawnPoint);
        }
        colony.markDirty();
    }

    /**
     * Creates and starts the pathjob towards this spawnpoint
     *
     * @param targetSpawnPoint
     * @return
     */
    private PathResult createSpawnPath(final BlockPos targetSpawnPoint)
    {
        final BlockPos closestBuildingPos = colony.getBuildingManager().getBestBuilding(targetSpawnPoint, IBuilding.class);
        final PathJobRaiderPathing job =
          new PathJobRaiderPathing(new ArrayList<>(colony.getBuildingManager().getBuildings().values()), colony.getWorld(), closestBuildingPos, targetSpawnPoint, 200);
        job.getResult().startJob(Pathfinding.getExecutor());
        return job.getResult();
    }

    /**
     * Calculate a random spawn point along the colony's border
     *
     * @return Returns the random blockPos
     */
    @Override
    public BlockPos calculateSpawnLocation()
    {
        BlockPos locationSum = new BlockPos(0, 0, 0);
        int amount = 0;

        for (final IBuilding building : colony.getBuildingManager().getBuildings().values())
        {
            if (WorldUtil.isEntityBlockLoaded(colony.getWorld(), building.getPosition()))
            {
                amount++;
                locationSum = locationSum.offset(building.getPosition());
            }
        }

        if (amount == 0)
        {
            Log.getLogger().info("Trying to spawn raid on colony with no loaded buildings, aborting!");
            return null;
        }

        // Calculate center on loaded buildings, to find a nice distance for raiders
        final BlockPos calcCenter = new BlockPos(locationSum.getX() / amount, locationSum.getY() / amount, locationSum.getZ() / amount);

        // Get a random point on a circle around the colony,far out for the direction
        final int degree = colony.getWorld().random.nextInt(360);
        int x = (int) Math.round(500 * Math.cos(Math.toRadians(degree)));
        int z = (int) Math.round(500 * Math.sin(Math.toRadians(degree)));
        final BlockPos advanceTowards = calcCenter.offset(x, 0, z);

        BlockPos spawnPos = null;
        final BlockPos closestBuilding = colony.getBuildingManager().getBestBuilding(advanceTowards, IBuilding.class);

        if (closestBuilding == null)
        {
            return null;
        }

        BlockPos worldSpawnPos = null;
        // 8 Tries
        for (int i = 0; i < 8; i++)
        {
            spawnPos = findSpawnPointInDirections(new BlockPos(closestBuilding.getX(), calcCenter.getY(), closestBuilding.getZ()), advanceTowards);
            if (spawnPos != null)
            {
                worldSpawnPos = BlockPosUtil.findAround(colony.getWorld(),
                  BlockPosUtil.getFloor(spawnPos, colony.getWorld()),
                  30,
                  3,
                  SOLID_AIR_POS_SELECTOR);
                if (worldSpawnPos != null || MineColonies.getConfig().getServer().skyRaiders.get())
                {
                    break;
                }
            }
        }

        if (spawnPos == null)
        {
            return null;
        }

        if (worldSpawnPos == null && MineColonies.getConfig().getServer().skyRaiders.get())
        {
            worldSpawnPos = BlockPosUtil.findAround(colony.getWorld(),
              BlockPosUtil.getFloor(spawnPos, colony.getWorld()),
              15,
              10,
              DOUBLE_AIR_POS_SELECTOR);
        }

        return worldSpawnPos;
    }

    /**
     * Finds a spawnpoint randomly in a circular shape around the center Advances
     *
     * @param start      the center of the area to search for a spawn point
     * @param advancePos The position we advance towards
     * @return the calculated position
     */
    private BlockPos findSpawnPointInDirections(
      final BlockPos start,
      final BlockPos advancePos)
    {
        BlockPos spawnPos = new BlockPos(start);
        BlockPos tempPos = new BlockPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        final Collection<IBuilding> buildings = colony.getBuildingManager().getBuildings().values();

        final int xDiff = Math.abs(start.getX() - advancePos.getX());
        final int zDiff = Math.abs(start.getZ() - advancePos.getZ());

        Vec3 xzRatio = new Vec3(xDiff * (start.getX() < advancePos.getX() ? 1 : -1), 0, zDiff * (start.getZ() < advancePos.getZ() ? 1 : -1));
        // Reduce ratio to 3 chunks a step
        xzRatio = xzRatio.normalize().scale(3);

        int validChunkCount = 0;
        for (int i = 0; i < 10; i++)
        {
            if (WorldUtil.isEntityBlockLoaded(colony.getWorld(), tempPos))
            {
                tempPos = tempPos.offset((int) (16 * xzRatio.x), 0, (int) (16 * xzRatio.z));

                if (WorldUtil.isEntityBlockLoaded(colony.getWorld(), tempPos))
                {
                    if (isValidSpawnPoint(buildings, tempPos) && !isOtherColony(tempPos.getX(), tempPos.getZ()))
                    {
                        spawnPos = tempPos;
                        validChunkCount++;
                        if (validChunkCount > 5)
                        {
                            return spawnPos;
                        }
                    }
                }
                else
                {
                    break;
                }
            }
            else
            {
                break;
            }
        }

        if (!spawnPos.equals(start))
        {
            return spawnPos;
        }

        return null;
    }

    /**
     * Check if the spawn position is within another colony
     *
     * @param pos
     * @return
     */
    private boolean isOtherColony(final int x, final int z)
    {
        final IColonyTagCapability cap = colony.getWorld().getChunk(x >> 4, z >> 4).getCapability(CLOSE_COLONY_CAP, null).orElseGet(null);
        return cap != null && cap.getOwningColony() != 0 && cap.getOwningColony() != colony.getID();
    }

    /**
     * Determines whether the given spawn point is allowed.
     *
     * @param spawnPos the spawn point to check
     * @return true if valid
     */
    public static boolean isValidSpawnPoint(final Collection<IBuilding> buildings, final BlockPos spawnPos)
    {
        for (final IBuilding building : buildings)
        {
            if (building.getBuildingLevel() == 0)
            {
                continue;
            }

            int minDist = MIN_BUILDING_SPAWN_DIST;

            // Additional raid protection for certain buildings, towers can be used now to deal with unlucky - inwall spawns
            if (building instanceof BuildingGuardTower)
            {
                minDist += building.getBuildingLevel() * 7;
            }
            else if (building.hasModule(LivingBuildingModule.class))
            {
                minDist += building.getBuildingLevel() * 4;
            }
            else if (building instanceof BuildingTownHall)
            {
                minDist += building.getBuildingLevel() * 8;
            }
            else
            {
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
     * Returns the colonies barbarian level
     *
     * @return the amount of barbarians.
     */
    @Override
    public int calculateRaiderAmount(final int raidLevel)
    {
        return 1 + Math.min(MineColonies.getConfig().getServer().maxBarbarianSize.get(),
          (int) ((raidLevel / SPAWN_MODIFIER) * getRaidDifficultyModifier() * (1.0 + colony.getMessagePlayerEntities().size() * INCREASE_PER_PLAYER) * ((
            colony.getWorld().random.nextDouble() * 0.5d) + 0.75)));
    }

    @Override
    public boolean isRaided()
    {
        for (final IColonyEvent event : colony.getEventManager().getEvents().values())
        {
            if (event instanceof IColonyRaidEvent && (event.getStatus() == EventStatus.PROGRESSING || event.getStatus() == EventStatus.PREPARING))
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
            if (nightsSinceLastRaid == 0)
            {
                final double lostPct = (double) lostCitizens / colony.getCitizenManager().getMaxCitizens();
                if (lostPct > LOST_CITIZEN_DIFF_REDUCE_PCT)
                {
                    raidDifficulty = Math.max(MIN_RAID_DIFFICULTY, raidDifficulty - (int) (lostPct / LOST_CITIZEN_DIFF_REDUCE_PCT));
                }
                else if (lostPct < LOST_CITIZEN_DIFF_INCREASE_PCT)
                {
                    raidDifficulty = Math.min(MAX_RAID_DIFFICULTY, raidDifficulty + 1);
                }
            }

            nightsSinceLastRaid++;
            lostCitizens = 0;
        }
        else
        {
            nightsSinceLastRaid = 0;
        }

        if (raidTonight)
        {
            raidTonight = false;
            raiderEvent(nextForcedType);
            nextForcedType = "";
        }
        else
        {
            determineRaidForNextDay();
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

    /**
     * Checks if a raid is possible
     *
     * @return whether a raid is possible
     */
    @Override
    public boolean canRaid()
    {
        return !WorldUtil.isPeaceful(colony.getWorld())
                 && MineColonies.getConfig().getServer().doBarbariansSpawn.get()
                 && colony.getRaiderManager().canHaveRaiderEvents()
                 && !colony.getPackageManager().getImportantColonyPlayers().isEmpty();
    }

    /**
     * Determines whether we raid on the next day
     */
    private void determineRaidForNextDay()
    {
        final boolean raid = canRaid() && raidThisNight(colony.getWorld(), colony);

        if (MineColonies.getConfig().getServer().enableInDevelopmentFeatures.get())
        {
            MessageUtils.format(new TextComponent("Will raid tomorrow: " + raid)).sendTo(colony).forAllPlayers();
        }

        setRaidNextNight(raid);
    }

    /**
     * Takes a colony and spits out that colony's RaidLevel.
     *
     * @return an int describing the raid level
     */
    public int getColonyRaidLevel()
    {
        int levels = colony.getCitizenManager().getCitizens().size() * 10;

        for (final IBuilding building : colony.getBuildingManager().getBuildings().values())
        {
            levels += building.getBuildingLevel() * 2;
        }

        return levels;
    }

    /**
     * Returns whether a raid should happen depending on the Config
     *
     * @param world  The world in which the raid is possibly happening (Used to get a random number easily)
     * @param colony The colony to raid
     * @return Boolean value on whether to act this night
     */
    private boolean raidThisNight(final Level world, final IColony colony)
    {
        if (nightsSinceLastRaid < MineColonies.getConfig().getServer().minimumNumberOfNightsBetweenRaids.get())
        {
            return false;
        }

        if (nightsSinceLastRaid > MineColonies.getConfig().getServer().averageNumberOfNightsBetweenRaids.get() + 2)
        {
            return true;
        }

        return world.random.nextDouble() < 1.0 / (MineColonies.getConfig().getServer().averageNumberOfNightsBetweenRaids.get() - MineColonies.getConfig()
          .getServer().minimumNumberOfNightsBetweenRaids.get());
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
                final int rand = colony.getWorld().random.nextInt(buildingArray.length);
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

    @Override
    public double getRaidDifficultyModifier()
    {
        return ((raidDifficulty / (double) 10) + MIN_DIFFICULTY_MODIFIER) * (MinecoloniesAPIProxy.getInstance().getConfig().getServer().barbarianHordeDifficulty.get()
                                                                               / (double) DEFAULT_BARBARIAN_DIFFICULTY) * (colony.getWorld().getDifficulty().getId() / 2d);
    }

    @Override
    public void onLostCitizen(final ICitizenData citizen)
    {
        if (!isRaided())
        {
            return;
        }

        if (citizen.getJob() instanceof AbstractJobGuard)
        {
            lostCitizens++;
        }
        else
        {
            lostCitizens += 2;
        }

        if (((double) lostCitizens / colony.getCitizenManager().getMaxCitizens()) > 0.5)
        {
            for (final IColonyEvent event : colony.getEventManager().getEvents().values())
            {
                event.setStatus(EventStatus.DONE);
            }
        }
    }

    @Override
    public void write(final CompoundTag compound)
    {
        compound.putBoolean(TAG_RAIDABLE, canHaveRaiderEvents());
        compound.putInt(TAG_NIGHTS_SINCE_LAST_RAID, getNightsSinceLastRaid());
        compound.putInt(TAG_RAID_DIFFICULTY, raidDifficulty);
        compound.putInt(TAG_LOST_CITIZENS, lostCitizens);
    }

    @Override
    public void read(final CompoundTag compound)
    {
        if (compound.getAllKeys().contains(TAG_RAIDABLE))
        {
            setCanHaveRaiderEvents(compound.getBoolean(TAG_RAIDABLE));
        }
        else
        {
            setCanHaveRaiderEvents(true);
        }

        if (compound.contains(TAG_NIGHTS_SINCE_LAST_RAID))
        {
            setNightsSinceLastRaid(compound.getInt(TAG_NIGHTS_SINCE_LAST_RAID));
        }

        raidDifficulty = Mth.clamp(compound.getInt(TAG_RAID_DIFFICULTY), MIN_RAID_DIFFICULTY, MAX_RAID_DIFFICULTY);
        lostCitizens = compound.getInt(TAG_LOST_CITIZENS);
    }

    @Override
    public int getLostCitizen()
    {
        return lostCitizens;
    }
}
