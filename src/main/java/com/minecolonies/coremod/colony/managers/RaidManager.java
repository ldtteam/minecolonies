package com.minecolonies.coremod.colony.managers;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.colony.colonyEvents.IColonyEvent;
import com.minecolonies.api.colony.colonyEvents.IColonyRaidEvent;
import com.minecolonies.api.colony.managers.interfaces.IRaiderManager;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingGuardTower;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingHome;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.HordeRaidEvent;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.amazonevent.AmazonRaidEvent;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.babarianEvent.BarbarianRaidEvent;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.babarianEvent.Horde;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.egyptianevent.EgyptianRaidEvent;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.norsemenevent.NorsemenRaidEvent;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.norsemenevent.NorsemenShipRaidEvent;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent.PirateRaidEvent;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent.ShipBasedRaiderUtils;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent.ShipSize;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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
    public static final double SPAWN_MODIFIER = 50;

    /**
     * Min distance to keep while spawning near buildings
     */
    private static final int MIN_BUILDING_SPAWN_DIST = 25;

    /**
     * Different biome ids.
     */
    private static final String DESERT_BIOME_ID = "desert";
    private static final String JUNGLE_BIOME_ID = "jungle";
    private static final String TAIGA_BIOME_ID  = "taiga";
    private static final double THIRTY_PERCENT  = 0.3d;
    private static final double TEN_PERCENT     = 0.1d;

    /**
     * Min and max for raid difficulty
     */
    private static final int MIN_RAID_DIFFICULTY    = 1;
    private static final int NORMAL_RAID_DIFFICULTY = 5;
    private static final int MAX_RAID_DIFFICULTY    = 10;

    /**
     * Difficulty nbt tag
     */
    private static final String TAG_RAID_DIFFICULTY = "difficulty";
    private static final String TAG_LOST_CITIZENS   = "lostCitizens";

    /**
     * Min required raidlevel
     */
    private static final int MIN_REQUIRED_RAIDLEVEL = 100;

    /**
     * Percentage increased amount of spawns per player
     */
    private static final double INCREASE_PER_PLAYER = 0.10;

    /**
     * The dynamic difficulty of raids for this colony
     */
    private int raidDifficulty = MIN_RAID_DIFFICULTY;

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
     * The amount of citizens lost in a raid, two for normal citizens one for guards
     */
    private int lostCitizens = 0;

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
            final int shipRotation = new Random().nextInt(3);
            final String homeBiomePath = colony.getWorld().getBiome(colony.getCenter()).getRegistryName().getPath();

            if (homeBiomePath.contains(TAIGA_BIOME_ID) && ShipBasedRaiderUtils.canSpawnShipAt(colony, targetSpawnPoint, amount, shipRotation, new NorsemenShipRaidEvent(colony)))
            {
                final NorsemenShipRaidEvent event = new NorsemenShipRaidEvent(colony);
                event.setSpawnPoint(targetSpawnPoint);
                event.setShipSize(ShipSize.getShipForRaiderAmount(amount));
                event.setShipRotation(shipRotation);
                colony.getEventManager().addEvent(event);
            }
            else if (ShipBasedRaiderUtils.canSpawnShipAt(colony, targetSpawnPoint, amount, shipRotation, new PirateRaidEvent(colony)))
            {
                final PirateRaidEvent event = new PirateRaidEvent(colony);
                event.setSpawnPoint(targetSpawnPoint);
                event.setShipSize(ShipSize.getShipForRaiderAmount(amount));
                event.setShipRotation(shipRotation);
                colony.getEventManager().addEvent(event);
            }
            else
            {
                final String biomePath = colony.getWorld().getBiome(targetSpawnPoint).getRegistryName().getPath();
                final HordeRaidEvent event;
                if (biomePath.contains(DESERT_BIOME_ID))
                {
                    event = new EgyptianRaidEvent(colony);
                }
                else if (biomePath.contains(JUNGLE_BIOME_ID))
                {
                    event = new AmazonRaidEvent(colony);
                }
                else if (biomePath.contains(TAIGA_BIOME_ID))
                {
                    event = new NorsemenRaidEvent(colony);
                }
                else
                {
                    event = new BarbarianRaidEvent(colony);
                }

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
            if (WorldUtil.isEntityBlockLoaded(colony.getWorld(), building.getPosition()))
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
        if (WorldUtil.isBlockLoaded(colony.getWorld(), spawnPos))
        {
            for (int i = 1; i <= random.nextInt(RAID_SPAWN_SEARCH_CHUNKS - 3) + 3; i++)
            {
                // Choose random between our two directions
                if (random.nextBoolean())
                {
                    if (WorldUtil.isBlockLoaded(colony.getWorld(), tempPos.offset(dir1, 16))
                          && WorldUtil.isBlockLoaded(colony.getWorld(), tempPos.offset(dir1, 32))
                          && WorldUtil.isBlockLoaded(colony.getWorld(), tempPos.offset(dir2, 16)))
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
                    if (WorldUtil.isBlockLoaded(colony.getWorld(), tempPos.offset(dir2, 16))
                          && WorldUtil.isBlockLoaded(colony.getWorld(), tempPos.offset(dir2, 32))
                          && WorldUtil.isBlockLoaded(colony.getWorld(), tempPos.offset(dir1, 16)))
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
    public int calculateRaiderAmount(final int raidLevel)
    {
        return Math.min(MineColonies.getConfig().getCommon().maxBarbarianSize.get(),
          (int) ((raidLevel / SPAWN_MODIFIER) * getRaidDifficultyModifier() * (1.0 + colony.getMessagePlayerEntities().size() * INCREASE_PER_PLAYER) * ((
            colony.getWorld().rand.nextDouble() * 0.5d) + 0.75)));
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
            if (nightsSinceLastRaid == 0)
            {
                final double lostPct = (double) lostCitizens / colony.getCitizenManager().getMaxCitizens();
                if (lostPct > THIRTY_PERCENT)
                {
                    raidDifficulty = Math.max(MIN_RAID_DIFFICULTY, raidDifficulty - 1);
                }
                else if (lostPct < TEN_PERCENT)
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
        if (WorldUtil.isDayTime(colony.getWorld()) && !colony.getRaiderManager().hasRaidBeenCalculated())
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
        else if (colony.getRaiderManager().willRaidTonight() && !WorldUtil.isDayTime(colony.getWorld()) && colony.getRaiderManager().hasRaidBeenCalculated())
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
        else if (!WorldUtil.isDayTime(colony.getWorld()) && colony.getRaiderManager().hasRaidBeenCalculated())
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
        int levels = colony.getCitizenManager().getCitizens().size() * 10;

        for (final IBuilding building : colony.getBuildingManager().getBuildings().values())
        {
            levels += building.getBuildingLevel() * building.getBuildingLevel();
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

    @Override
    public double getRaidDifficultyModifier()
    {
        return (raidDifficulty / (double) NORMAL_RAID_DIFFICULTY) * (MinecoloniesAPIProxy.getInstance().getConfig().getCommon().barbarianHordeDifficulty.get()
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
    public void write(final CompoundNBT compound)
    {
        compound.putBoolean(TAG_RAIDABLE, canHaveRaiderEvents());
        compound.putInt(TAG_NIGHTS_SINCE_LAST_RAID, getNightsSinceLastRaid());
        compound.putInt(TAG_RAID_DIFFICULTY, raidDifficulty);
        compound.putInt(TAG_LOST_CITIZENS, lostCitizens);
    }

    @Override
    public void read(final CompoundNBT compound)
    {
        if (compound.keySet().contains(TAG_RAIDABLE))
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

            raidDifficulty = compound.getInt(TAG_RAID_DIFFICULTY);
        lostCitizens = compound.getInt(TAG_LOST_CITIZENS);
    }

    @Override
    public int getLostCitizen()
    {
        return lostCitizens;
    }
}
