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
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.mobs.AbstractEntityRaiderMob;
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
import com.minecolonies.coremod.entity.ai.citizen.guard.AbstractEntityAIGuard;
import com.minecolonies.coremod.entity.pathfinding.Pathfinding;
import com.minecolonies.coremod.entity.pathfinding.pathjobs.PathJobRaiderPathing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.colony.IColony.CLOSE_COLONY_CAP;
import static com.minecolonies.api.util.BlockPosUtil.DOUBLE_AIR_POS_SELECTOR;
import static com.minecolonies.api.util.BlockPosUtil.SOLID_AIR_POS_SELECTOR;
import static com.minecolonies.api.util.constant.ColonyConstants.BIG_HORDE_SIZE;
import static com.minecolonies.api.util.constant.Constants.DEFAULT_BARBARIAN_DIFFICULTY;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;

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
    public static final int MIN_REQUIRED_RAIDLEVEL = 75;

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
     * The initial next raid type
     */
    private static final String INITIAL_NEXT_RAID_TYPE = "";

    /**
     * The next raidType, or "" if the next raid should be determined from biome.
     */
    private String nextForcedType = INITIAL_NEXT_RAID_TYPE;

    /**
     * List which keeps track of raid historical data
     */
    private List<RaidHistory> raidHistories = new ArrayList<>();

    /**
     * If ships will be allowed or not.
     */
    private boolean allowShips = true;

    /**
     * Passing through raid timer.
     */
    private long passingThroughRaidTime = 0;

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
    public void setRaidNextNight(final boolean willRaid, final String raidType, final boolean allowShips)
    {
        this.raidTonight = willRaid;
        this.nextForcedType = raidType;
        this.allowShips = allowShips;
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
        raiderEvent("", false);
    }

    @Override
    public RaidSpawnResult raiderEvent(String raidType, final boolean overrideConfig, final boolean allowShips)
    {
        if (colony.getWorld() == null || raidType == null)
        {
            return RaidSpawnResult.ERROR;
        }
        else if (!canRaid(overrideConfig))
        {
            return RaidSpawnResult.CANNOT_RAID;
        }

        final int raidLevel = getColonyRaidLevel();
        int amount = calculateRaiderAmount(raidLevel);
        if (amount <= 0 || raidLevel < MIN_REQUIRED_RAIDLEVEL)
        {
            return RaidSpawnResult.TOO_SMALL;
        }

        // Splits into multiple raids if too large
        final int raidCount = Math.max(1, amount / BIG_HORDE_SIZE);

        final Set<BlockPos> spawnPoints = new HashSet<>();

        for (int i = 0; i < raidCount; i++)
        {
            final BlockPos targetSpawnPoint = calculateSpawnLocation();
            if (targetSpawnPoint == null || targetSpawnPoint.equals(colony.getCenter())
                  || !colony.getWorld().getWorldBorder().isWithinBounds(targetSpawnPoint))
            {
                continue;
            }

            spawnPoints.add(targetSpawnPoint);
        }

        if (spawnPoints.isEmpty())
        {
            return RaidSpawnResult.NO_SPAWN_POINT;
        }

        raidHistories.add(new RaidHistory(amount, colony.getWorld().getGameTime()));
        nightsSinceLastRaid = 0;
        raidTonight = false;
        amount = (int) Math.ceil((float) amount / spawnPoints.size());

        for (final BlockPos targetSpawnPoint : spawnPoints)
        {
            IColonyRaidEvent raidEvent = null;

            if (MineColonies.getConfig().getServer().enableInDevelopmentFeatures.get())
            {
                MessageUtils.format(Component.literal("Horde Spawn Point: " + targetSpawnPoint)).sendTo(colony).forAllPlayers();
            }

            if (MineColonies.getConfig().getServer().skyRaiders.get() &&
                  colony.getWorld().getBlockState(targetSpawnPoint).isAir()
                  && colony.getWorld().getBlockState(targetSpawnPoint.below()).isAir())
            {
                raidType = PirateRaidEvent.PIRATE_RAID_EVENT_TYPE_ID.getPath();
            }

            // No rotation till spawners are moved into schematics
            final int shipRotation = colony.getWorld().random.nextInt(4);
            final Holder<Biome> biome = colony.getWorld().getBiome(colony.getCenter());
            final int rand = colony.getWorld().random.nextInt(100);
            if (allowShips && (raidType.isEmpty() && (biome.is(BiomeTags.IS_TAIGA) || rand < IGNORE_BIOME_CHANCE)
                   || raidType.equals(NorsemenRaidEvent.NORSEMEN_RAID_EVENT_TYPE_ID.getPath()))
                  && ShipBasedRaiderUtils.canSpawnShipAt(colony, targetSpawnPoint, amount, shipRotation, NorsemenShipRaidEvent.SHIP_NAME))
            {
                final NorsemenShipRaidEvent event = new NorsemenShipRaidEvent(colony);
                event.setSpawnPoint(targetSpawnPoint);
                event.setShipSize(ShipSize.getShipForRaiderAmount(amount));
                event.setShipRotation(shipRotation);
                event.setSpawnPath(createSpawnPath(targetSpawnPoint));
                raidEvent = event;
                colony.getEventManager().addEvent(event);
            }
            else if (allowShips && ShipBasedRaiderUtils.canSpawnShipAt(colony, targetSpawnPoint, amount, shipRotation, PirateRaidEvent.SHIP_NAME)
                       && (raidType.isEmpty() || raidType.equals(PirateRaidEvent.PIRATE_RAID_EVENT_TYPE_ID.getPath())))
            {
                final PirateRaidEvent event = new PirateRaidEvent(colony);
                event.setSpawnPoint(targetSpawnPoint);
                event.setShipSize(ShipSize.getShipForRaiderAmount(amount));
                event.setShipRotation(shipRotation);
                event.setSpawnPath(createSpawnPath(targetSpawnPoint));
                raidEvent = event;
                colony.getEventManager().addEvent(event);
            }
            else
            {
                final HordeRaidEvent event;
                if (((biome.is(BiomeTags.HAS_DESERT_PYRAMID) || (rand > IGNORE_BIOME_CHANCE && rand < IGNORE_BIOME_CHANCE * 2))
                       && raidType.isEmpty()) || raidType.equals(EgyptianRaidEvent.EGYPTIAN_RAID_EVENT_TYPE_ID.getPath()))
                {
                    event = new EgyptianRaidEvent(colony);
                }
                else if (((biome.is(BiomeTags.IS_JUNGLE) || (rand > IGNORE_BIOME_CHANCE * 2 && rand < IGNORE_BIOME_CHANCE * 3))
                                                              && raidType.isEmpty()) || (raidType.equals(AmazonRaidEvent.AMAZON_RAID_EVENT_TYPE_ID.getPath())))
                {
                    event = new AmazonRaidEvent(colony);
                }
                else if (((biome.is(BiomeTags.IS_TAIGA) || (rand > IGNORE_BIOME_CHANCE * 3 && rand < IGNORE_BIOME_CHANCE * 4))
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
                raidEvent = event;
                colony.getEventManager().addEvent(event);
            }

            raidHistories.get(raidHistories.size() - 1).spawnData.add(new RaidSpawnInfo(raidEvent.getEventTypeID(), targetSpawnPoint));
        }
        colony.markDirty();
        return RaidSpawnResult.SUCCESS;
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
                        if (validChunkCount > 2 || i > 2)
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
     * @param x the x param
     * @param z the z param.
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
        if (raidHistories.isEmpty())
        {
            return List.of();
        }

        final RaidHistory last = raidHistories.get(raidHistories.size() - 1);
        return last.spawnData.stream().map(raidSpawnInfo -> raidSpawnInfo.spawnpos).collect(Collectors.toList());
    }

    /**
     * Returns the colonies barbarian level
     *
     * @return the amount of barbarians.
     */
    @Override
    public int calculateRaiderAmount(final int raidLevel)
    {
        return 1 + Math.min(MineColonies.getConfig().getServer().maxRaiders.get(),
          (int) ((raidLevel / SPAWN_MODIFIER) * getRaidDifficultyModifier() * (1.0 + colony.getMessagePlayerEntities().size() * INCREASE_PER_PLAYER) * ((
            colony.getWorld().random.nextDouble() * 0.5d) + 0.75)));
    }

    @Override
    public boolean isRaided()
    {
        if (colony.getWorld().getGameTime() <= passingThroughRaidTime)
        {
            return true;
        }
        for (final IColonyEvent event : colony.getEventManager().getEvents().values())
        {
            if (event instanceof IColonyRaidEvent raidEvent && raidEvent.isRaidActive())
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
                if (!raidHistories.isEmpty())
                {
                    RaidHistory history = raidHistories.get(raidHistories.size() - 1);
                    final double lostPct = (double) history.lostCitizens / colony.getCitizenManager().getMaxCitizens();
                    if (lostPct > LOST_CITIZEN_DIFF_REDUCE_PCT)
                    {
                        raidDifficulty = Math.max(MIN_RAID_DIFFICULTY, raidDifficulty - (int) (lostPct / LOST_CITIZEN_DIFF_REDUCE_PCT));
                    }
                    else if (lostPct < LOST_CITIZEN_DIFF_INCREASE_PCT)
                    {
                        raidDifficulty = Math.min(MAX_RAID_DIFFICULTY, raidDifficulty + 1);
                    }
                }
            }

            nightsSinceLastRaid++;
        }
        else
        {
            nightsSinceLastRaid = 0;
        }

        if (raidTonight)
        {
            raidTonight = false;
            final boolean overrideConfig = !nextForcedType.isEmpty();
            raiderEvent(nextForcedType, overrideConfig, allowShips);
            nextForcedType = INITIAL_NEXT_RAID_TYPE;
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

    @Override
    public boolean canRaid()
    {
        return canRaid(false);
    }

    @Override
    public boolean canRaid(final boolean override)
    {
        return !WorldUtil.isPeaceful(colony.getWorld())
                 && (MineColonies.getConfig().getServer().enableColonyRaids.get() || override)
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
            MessageUtils.format(Component.literal("Will raid tomorrow: " + raid)).sendTo(colony).forAllPlayers();
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
        if (buildingPosUsage > 6 || lastBuilding == null)
        {
            buildingPosUsage = 0;
            final Collection<IBuilding> buildingList = colony.getBuildingManager().getBuildings().values();
            final Object[] buildingArray = buildingList.toArray();
            if (buildingArray.length != 0)
            {
                final int rand = colony.getWorld().random.nextInt(buildingArray.length);
                final IBuilding building = (IBuilding) buildingArray[rand];

                if (lastBuilding != null)
                {
                    // Alerts guards of raiders reaching a building
                    final List<AbstractEntityCitizen> possibleGuards = new ArrayList<>();

                    for (final ICitizenData entry : colony.getCitizenManager().getCitizens())
                    {
                        if (entry.getEntity().isPresent()
                              && entry.getJob() instanceof AbstractJobGuard
                              && BlockPosUtil.getDistanceSquared(entry.getEntity().get().blockPosition(), lastBuilding) < 75 * 75 && entry.getJob().getWorkerAI() != null)
                        {
                            if (((AbstractEntityAIGuard<?, ?>) entry.getJob().getWorkerAI()).canHelp(building.getPosition()))
                            {
                                possibleGuards.add(entry.getEntity().get());
                            }
                        }
                    }

                    possibleGuards.sort(Comparator.comparingInt(guard -> (int) lastBuilding.distSqr(guard.blockPosition())));

                    for (int i = 0; i < possibleGuards.size() && i <= 3; i++)
                    {
                        ((AbstractEntityAIGuard<?, ?>) possibleGuards.get(i).getCitizenData().getJob().getWorkerAI()).setNextPatrolTarget(lastBuilding);
                    }
                }

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
        return ((raidDifficulty / (double) 10) + MIN_DIFFICULTY_MODIFIER) * (MinecoloniesAPIProxy.getInstance().getConfig().getServer().raidDifficulty.get()
                                                                               / (double) DEFAULT_BARBARIAN_DIFFICULTY) * (colony.getWorld().getDifficulty().getId() / 2d);
    }

    @Override
    public void onLostCitizen(final ICitizenData citizen)
    {
        if (!isRaided())
        {
            return;
        }

        if (raidHistories.isEmpty())
        {
            return;
        }

        final RaidHistory history = raidHistories.get(raidHistories.size() - 1);
        if (citizen.getJob() instanceof AbstractJobGuard)
        {
            history.lostCitizens++;
        }
        else
        {
            history.lostCitizens += 2;
        }

        if (((double) history.lostCitizens / colony.getCitizenManager().getMaxCitizens()) > 0.5)
        {
            for (final IColonyEvent event : colony.getEventManager().getEvents().values())
            {
                if (event instanceof IColonyRaidEvent raidEvent)
                {
                    raidEvent.setStatus(EventStatus.DONE);
                    raidEvent.setMercyEnd();
                }
            }
        }
    }

    @Override
    public void write(final CompoundTag compound)
    {
        compound.putBoolean(TAG_RAIDABLE, canHaveRaiderEvents());
        compound.putInt(TAG_NIGHTS_SINCE_LAST_RAID, getNightsSinceLastRaid());
        compound.putInt(TAG_RAID_DIFFICULTY, raidDifficulty);

        ListTag nbtList = new ListTag();
        for (final RaidHistory history : raidHistories)
        {
            nbtList.add(history.write());
        }
        compound.put(TAG_RAID_HISTORY, nbtList);
    }

    @Override
    public void read(final CompoundTag compound)
    {
        if (compound.contains(TAG_RAIDABLE))
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

        if (compound.contains(TAG_RAID_HISTORY))
        {
            raidHistories.clear();
            ListTag nbtList = compound.getList(TAG_RAID_HISTORY, Tag.TAG_COMPOUND);
            for (final Tag tag : nbtList)
            {
                raidHistories.add(RaidHistory.fromNBT((CompoundTag) tag));
            }
        }
    }

    @Override
    public int getLostCitizen()
    {
        if (raidHistories.isEmpty())
        {
            return 0;
        }

        return raidHistories.get(raidHistories.size() - 1).lostCitizens;
    }

    @Override
    public void onRaiderDeath(final AbstractEntityRaiderMob entity)
    {
        final RaidHistory last = getLastRaid();
        if (last != null)
        {
            last.deadRaiders++;
        }
    }

    /**
     * List of raid histories
     *
     * @return
     */
    public RaidHistory getLastRaid()
    {
        if (raidHistories.isEmpty())
        {
            return null;
        }

        return raidHistories.get(raidHistories.size() - 1);
    }

    @Override
    public void setPassThroughRaid()
    {
        passingThroughRaidTime = colony.getWorld().getGameTime() + TICKS_SECOND * 20;
    }

    /**
     * Gets all raid histories
     * @return
     */
    public List<RaidHistory> getAllRaids()
    {
        return new ArrayList<>(raidHistories);
    }

    /**
     * Data holder for raid history
     */
    public static class RaidHistory
    {
        /**
         * Serialization constants
         */
        static final String TAG_LOSTCITIZENS = "lostCitizens";
        static final String TAG_RAIDERAMOUNT = "raiderAmount";
        static final String TAG_RAIDTIME     = "raidTime";
        static final String TAG_SPAWNINFO    = "spawnInfo";

        /**
         * Lost citizens during the raid
         */
        public int lostCitizens = 0;

        /**
         * Total amount of raiders spawned for all raids
         */
        public final int raiderAmount;

        /**
         * Total amount of raiders killed all raids
         */
        public int deadRaiders = 0;

        /**
         * World time at which the raid occured
         */
        public final long raidTime;

        /**
         * List of raid types and their spawnpoints
         */
        public final List<RaidSpawnInfo> spawnData = new ArrayList<>();

        public RaidHistory(final int raiderAmount, final long raidTime)
        {
            this.raidTime = raidTime;
            this.raiderAmount = raiderAmount;
        }

        private CompoundTag write()
        {
            CompoundTag tag = new CompoundTag();
            tag.putInt(TAG_LOSTCITIZENS, lostCitizens);
            tag.putInt(TAG_RAIDERAMOUNT, raiderAmount);
            tag.putLong(TAG_RAIDTIME, raidTime);
            ListTag nbtList = new ListTag();
            for (final RaidSpawnInfo raidSpawnInfo : spawnData)
            {
                nbtList.add(raidSpawnInfo.write());
            }
            tag.put(TAG_SPAWNINFO, nbtList);
            return tag;
        }

        private static RaidHistory fromNBT(final CompoundTag tag)
        {
            RaidHistory history = new RaidHistory(tag.getInt(TAG_RAIDERAMOUNT), tag.getLong(TAG_RAIDTIME));
            history.lostCitizens = tag.getInt(TAG_LOST_CITIZENS);
            ListTag nbtList = tag.getList(TAG_SPAWNINFO, Tag.TAG_COMPOUND);
            for (final Tag entry : nbtList)
            {
                history.spawnData.add(RaidSpawnInfo.fromNBT((CompoundTag) entry));
            }

            return history;
        }

        @Override
        public String toString()
        {
            return "Raid on: " + raidTime / 24000L
                     + "\nRaiders spawned: " + raiderAmount
                     + "\nRaiders killed: " + deadRaiders
                     + "\nCitizens lost: " + lostCitizens
                     + "\nSpawns:" + spawnData.stream().map(Object::toString).collect(Collectors.joining("\n"));
        }
    }

    /**
     * Data holder for raid spawns
     */
    public static class RaidSpawnInfo
    {
        /**
         * Serialization constants
         */
        static final String TAG_RAIDTYPE = "raidtype";

        /**
         * Id of the raid type
         */
        public final ResourceLocation raidType;

        /**
         * Position of the raid spawn
         */
        public final BlockPos spawnpos;

        public RaidSpawnInfo(final ResourceLocation raidType, final BlockPos spawnpos)
        {
            this.raidType = raidType;
            this.spawnpos = spawnpos;
        }

        private CompoundTag write()
        {
            CompoundTag tag = new CompoundTag();
            tag.putString(TAG_RAIDTYPE, raidType.toString());
            tag.putInt("x", spawnpos.getX());
            tag.putInt("y", spawnpos.getY());
            tag.putInt("z", spawnpos.getZ());
            return tag;
        }

        public static RaidSpawnInfo fromNBT(final CompoundTag tag)
        {
            return new RaidSpawnInfo(new ResourceLocation(tag.getString(TAG_RAIDTYPE)), new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")));
        }

        public String toString()
        {
            return "Type: " + raidType.toString() + " pos: " + spawnpos.toShortString();
        }
    }
}
