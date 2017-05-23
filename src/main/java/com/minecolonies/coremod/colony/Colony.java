package com.minecolonies.coremod.colony;

import com.google.common.collect.ImmutableMap;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.colony.requestsystem.IRequestManager;
import com.minecolonies.api.colony.requestsystem.StandardRequestManager;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.workorder.IWorkOrder;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.entity.ai.citizen.farmer.Field;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.MathUtils;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.colony.buildings.*;
import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.citizen.builder.ConstructionTapeHelper;
import com.minecolonies.coremod.network.messages.*;
import com.minecolonies.coremod.permissions.ColonyPermissionEventHandler;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import com.minecolonies.coremod.util.AchievementUtils;
import com.minecolonies.coremod.util.ColonyUtils;
import com.minecolonies.coremod.util.EntityUtils;
import com.minecolonies.coremod.util.ServerUtils;
import com.minecolonies.structures.Structures;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class describes a colony and contains all the data and methods for
 * manipulating a Colony.
 */
public class Colony implements IColony
{
    //  Settings
    private static final int    CITIZEN_CLEANUP_TICK_INCREMENT = 5 * 20;
    private static final String TAG_ID                         = "id";
    private static final String TAG_NAME                       = "name";
    private static final String TAG_DIMENSION                  = "dimension";
    private static final String TAG_CENTER                     = "center";
    private static final String TAG_MAX_CITIZENS               = "maxCitizens";
    private static final String TAG_BUILDINGS                  = "buildings";
    private static final String TAG_CITIZENS                   = "citizens";
    private static final String TAG_ACHIEVEMENT                = "achievement";
    private static final String TAG_ACHIEVEMENT_LIST           = "achievementlist";
    private static final String TAG_WORK                       = "work";
    private static final String TAG_MANUAL_HIRING              = "manualHiring";
    private static final String TAG_WAYPOINT                   = "waypoints";
    private static final String TAG_FREE_BLOCKS                = "freeBlocks";
    private static final String TAG_FREE_POSITIONS             = "freePositions";

    //statistics tags
    private static final String TAG_STATISTICS            = "statistics";
    private static final String TAG_MINER_STATISTICS      = "minerStatistics";
    private static final String TAG_MINER_ORES            = "ores";
    private static final String TAG_MINER_DIAMONDS        = "diamonds";
    private static final String TAG_FARMER_STATISTICS     = "farmerStatistics";
    private static final String TAG_FARMER_WHEAT          = "wheat";
    private static final String TAG_FARMER_POTATOES       = "potatoes";
    private static final String TAG_FARMER_CARROTS        = "carrots";
    private static final String TAG_GUARD_STATISTICS      = "guardStatistics";
    private static final String TAG_GUARD_MOBS            = "mobs";
    private static final String TAG_BUILDER_STATISTICS    = "builderStatistics";
    private static final String TAG_BUILDER_HUTS          = "huts";
    private static final String TAG_FISHERMAN_STATISTICS  = "fishermanStatistics";
    private static final String TAG_FISHERMAN_FISH        = "fish";
    private static final String TAG_LUMBERJACK_STATISTICS = "lumberjackStatistics";
    private static final String TAG_LUMBERJACK_TREES      = "trees";
    private static final String TAG_LUMBERJACK_SAPLINGS   = "saplings";
    private static final int    NUM_ACHIEVEMENT_FIRST     = 1;
    private static final int    NUM_ACHIEVEMENT_SECOND    = 25;
    private static final int    NUM_ACHIEVEMENT_THIRD     = 100;
    private static final int    NUM_ACHIEVEMENT_FOURTH    = 500;
    private static final int    NUM_ACHIEVEMENT_FIFTH     = 1000;

    /**
     * Bonus happiness each factor added.
     */
    private static final double HAPPINESS_FACTOR = 0.1;

    /**
     * Saturation at which a citizen starts being happy.
     */
    private static final int WELL_SATURATED_LIMIT = 5;

    /**
     * Variable to determine if its currently day or night.
     */
    private boolean isDay = true;

    /**
     * Max overall happiness.
     */
    private static final double MAX_OVERALL_HAPPINESS = 10;

    /**
     * Min overall happiness.
     */
    private static final double MIN_OVERALL_HAPPINESS = 1;

    //private int autoHostile = 0;//Off
    private static final String TAG_FIELDS                        = "fields";
    private static final int    CHECK_WAYPOINT_EVERY              = 100;
    private static final double MAX_SQ_DIST_SUBSCRIBER_UPDATE     = MathUtils.square(Configurations.workingRangeTownHall + 16D);
    private static final double MAX_SQ_DIST_OLD_SUBSCRIBER_UPDATE = MathUtils.square(Configurations.workingRangeTownHall * 2D);
    private final int id;
    //  General Attributes
    private final int dimensionId;
    //  Buildings
    private final Map<BlockPos, Field>       fields    = new HashMap<>();
    //Additional Waypoints.
    private final Map<BlockPos, IBlockState> wayPoints = new HashMap<>();
    @NotNull
    private final List<Achievement> colonyAchievements;
    //  Workload and Jobs
    private final WorkManager                     workManager       = new WorkManager(this);
    @NotNull
    private final Map<BlockPos, AbstractBuilding> buildings         = new HashMap<>();
    //  Citizenry
    @NotNull
    private final Map<Integer, CitizenData>       citizens          = new HashMap<>();
    /**
     * The Positions which players can freely interact.
     */
    private final Set<BlockPos>                   freePositions     = new HashSet<>();
    /**
     * The Blocks which players can freely interact with.
     */
    private final Set<Block>                      freeBlocks        = new HashSet<>();
    private       int                             minedOres         = 0;
    private       int                             minedDiamonds     = 0;
    private       int                             harvestedWheat    = 0;
    private       int                             harvestedPotatoes = 0;
    private       int                             harvestedCarrots  = 0;
    private       int                             builtHuts         = 0;
    private       int                             caughtFish        = 0;
    private       int                             felledTrees       = 0;
    private       int                             plantedSaplings   = 0;
    //  Runtime Data
    @Nullable
    private       World                           world             = null;
    //  Updates and Subscriptions
    @NotNull
    private       Set<EntityPlayerMP>             subscribers       = new HashSet<>();
    private       boolean                         isDirty           = false;
    private       boolean                         isCitizensDirty   = false;
    private       boolean                         isBuildingsDirty  = false;
    private       boolean                         manualHiring      = false;
    private       boolean                         isFieldsDirty     = false;
    private       String                          name              = "ERROR(Wasn't placed by player)";
    private BlockPos         center;
    //  Administration/permissions
    @NotNull
    private Permissions      permissions;
    @Nullable
    private BuildingTownHall townHall;
    private int topCitizenId = 0;
    private int maxCitizens  = Configurations.maxCitizens;

    private       double          overallHappiness = 5;
    private       int             killedMobs       = 0;
    @NotNull
    //TODO: Serialization of requestmanagers.
    private final IRequestManager requestManager   = new StandardRequestManager(this);

    /**
     * Constructor for a newly created Colony.
     *
     * @param id The id of the colony to create.
     * @param w  The world the colony exists in.
     * @param c  The center of the colony (location of Town Hall).
     */
    Colony(final int id, @NotNull final World w, final BlockPos c)
    {
        this(id, w.provider.getDimension());
        center = c;
        world = w;
        this.permissions = new Permissions(this);
    }

    /**
     * Base constructor.
     *
     * @param id  The current id for the colony.
     * @param dim The world the colony exists in.
     */
    protected Colony(final int id, final int dim)
    {
        this.id = id;
        this.dimensionId = dim;
        this.permissions = new Permissions(this);
        this.colonyAchievements = new ArrayList<>();

        // Register a new event handler
        MinecraftForge.EVENT_BUS.register(new ColonyPermissionEventHandler(this));

        for (final String s : Configurations.freeToInteractBlocks)
        {
            final Block block = Block.getBlockFromName(s);
            if (block == null)
            {
                final BlockPos pos = BlockPosUtil.getBlockPosOfString(s);
                if (pos != null)
                {
                    freePositions.add(pos);
                }
            }
            else
            {
                freeBlocks.add(block);
            }
        }
    }

    /**
     * Load a saved colony.
     *
     * @param compound The NBT compound containing the colony's data.
     * @return loaded colony.
     */
    @NotNull
    public static Colony loadColony(@NotNull final NBTTagCompound compound)
    {
        final int id = compound.getInteger(TAG_ID);
        final int dimensionId = compound.getInteger(TAG_DIMENSION);
        @NotNull final Colony c = new Colony(id, dimensionId);
        c.readFromNBT(compound);
        return c;
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        name = compound.getString(TAG_NAME);
        center = BlockPosUtil.readFromNBT(compound, TAG_CENTER);

        manualHiring = compound.getBoolean(TAG_MANUAL_HIRING);
        maxCitizens = compound.getInteger(TAG_MAX_CITIZENS);

        // Permissions
        permissions.loadPermissions(compound);

        //  Citizens before Buildings, because Buildings track the Citizens
        final NBTTagList citizenTagList = compound.getTagList(TAG_CITIZENS, NBT.TAG_COMPOUND);
        for (int i = 0; i < citizenTagList.tagCount(); ++i)
        {
            final NBTTagCompound citizenCompound = citizenTagList.getCompoundTagAt(i);
            final CitizenData data = CitizenData.createFromNBT(citizenCompound, this);
            citizens.put(data.getId(), data);
            topCitizenId = Math.max(topCitizenId, data.getId());
        }

        //  Buildings
        final NBTTagList buildingTagList = compound.getTagList(TAG_BUILDINGS, NBT.TAG_COMPOUND);
        for (int i = 0; i < buildingTagList.tagCount(); ++i)
        {
            final NBTTagCompound buildingCompound = buildingTagList.getCompoundTagAt(i);
            @Nullable final AbstractBuilding b = AbstractBuilding.createFromNBT(this, buildingCompound);
            if (b != null)
            {
                addBuilding(b);
            }
        }

        // Fields
        final NBTTagList fieldTagList = compound.getTagList(TAG_FIELDS, NBT.TAG_COMPOUND);
        for (int i = 0; i < fieldTagList.tagCount(); ++i)
        {
            final NBTTagCompound fieldCompound = fieldTagList.getCompoundTagAt(i);
            final Field f = Field.createFromNBT(this, fieldCompound);
            addField(f);
        }

        // Restore colony achievements
        final NBTTagList achievementTagList = compound.getTagList(TAG_ACHIEVEMENT_LIST, NBT.TAG_COMPOUND);
        for (int i = 0; i < achievementTagList.tagCount(); ++i)
        {
            final NBTTagCompound achievementCompound = achievementTagList.getCompoundTagAt(i);
            final String achievementKey = achievementCompound.getString(TAG_ACHIEVEMENT);
            final StatBase statBase = StatList.getOneShotStat(achievementKey);
            if (statBase instanceof Achievement)
            {
                colonyAchievements.add((Achievement) statBase);
            }
        }

        //  Workload
        workManager.readFromNBT(compound.getCompoundTag(TAG_WORK));


        // Waypoints
        final NBTTagList wayPointTagList = compound.getTagList(TAG_WAYPOINT, NBT.TAG_COMPOUND);
        for (int i = 0; i < wayPointTagList.tagCount(); ++i)
        {
            final NBTTagCompound blockAtPos = wayPointTagList.getCompoundTagAt(i);
            final BlockPos pos = BlockPosUtil.readFromNBT(blockAtPos, TAG_WAYPOINT);
            final IBlockState state = NBTUtil.readBlockState(blockAtPos);
            wayPoints.put(pos, state);
        }

        //Statistics
        final NBTTagCompound statisticsCompound = compound.getCompoundTag(TAG_STATISTICS);
        final NBTTagCompound minerStatisticsCompound = statisticsCompound.getCompoundTag(TAG_MINER_STATISTICS);
        final NBTTagCompound farmerStatisticsCompound = statisticsCompound.getCompoundTag(TAG_FARMER_STATISTICS);
        final NBTTagCompound guardStatisticsCompound = statisticsCompound.getCompoundTag(TAG_FARMER_STATISTICS);
        final NBTTagCompound builderStatisticsCompound = statisticsCompound.getCompoundTag(TAG_BUILDER_STATISTICS);
        final NBTTagCompound fishermanStatisticsCompound = statisticsCompound.getCompoundTag(TAG_FISHERMAN_STATISTICS);
        final NBTTagCompound lumberjackStatisticsCompound = statisticsCompound.getCompoundTag(TAG_LUMBERJACK_STATISTICS);
        minedOres = minerStatisticsCompound.getInteger(TAG_MINER_ORES);
        minedDiamonds = minerStatisticsCompound.getInteger(TAG_MINER_DIAMONDS);
        harvestedCarrots = farmerStatisticsCompound.getInteger(TAG_FARMER_CARROTS);
        harvestedPotatoes = farmerStatisticsCompound.getInteger(TAG_FARMER_POTATOES);
        harvestedWheat = farmerStatisticsCompound.getInteger(TAG_FARMER_WHEAT);
        killedMobs = guardStatisticsCompound.getInteger(TAG_GUARD_MOBS);
        builtHuts = builderStatisticsCompound.getInteger(TAG_BUILDER_HUTS);
        caughtFish = fishermanStatisticsCompound.getInteger(TAG_FISHERMAN_FISH);
        felledTrees = lumberjackStatisticsCompound.getInteger(TAG_LUMBERJACK_TREES);
        plantedSaplings = lumberjackStatisticsCompound.getInteger(TAG_LUMBERJACK_SAPLINGS);

        // Free blocks
        final NBTTagList freeBlockTagList = compound.getTagList(TAG_FREE_BLOCKS, NBT.TAG_STRING);
        for (int i = 0; i < freeBlockTagList.tagCount(); ++i)
        {
            freeBlocks.add(Block.getBlockFromName(freeBlockTagList.getStringTagAt(i)));
        }

        // Free positions
        final NBTTagList freePositionTagList = compound.getTagList(TAG_FREE_POSITIONS, NBT.TAG_COMPOUND);
        for (int i = 0; i < freePositionTagList.tagCount(); ++i)
        {
            final NBTTagCompound blockTag = freePositionTagList.getCompoundTagAt(i);
            final BlockPos block = BlockPosUtil.readFromNBT(blockTag, TAG_FREE_POSITIONS);
            freePositions.add(block);
        }
    }

    @Override
    public void addBuilding(@NotNull final AbstractBuilding building)
    {
        buildings.put(building.getLocation().getInDimensionLocation(), building);
        building.markDirty();

        //  Limit 1 town hall
        if (building instanceof BuildingTownHall && townHall == null)
        {
            townHall = (BuildingTownHall) building;
        }
    }

    @Override
    public void addField(@NotNull final Field field)
    {
        fields.put(field.getID(), field);
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        //  Core attributes
        compound.setInteger(TAG_ID, id);
        compound.setInteger(TAG_DIMENSION, dimensionId);

        //  Basic data
        compound.setString(TAG_NAME, name);
        BlockPosUtil.writeToNBT(compound, TAG_CENTER, center);

        compound.setBoolean(TAG_MANUAL_HIRING, manualHiring);
        compound.setInteger(TAG_MAX_CITIZENS, maxCitizens);


        // Permissions
        permissions.savePermissions(compound);

        //  Buildings
        @NotNull final NBTTagList buildingTagList = new NBTTagList();
        for (@NotNull final AbstractBuilding b : buildings.values())
        {
            @NotNull final NBTTagCompound buildingCompound = new NBTTagCompound();
            b.writeToNBT(buildingCompound);
            buildingTagList.appendTag(buildingCompound);
        }
        compound.setTag(TAG_BUILDINGS, buildingTagList);

        // Fields
        @NotNull final NBTTagList fieldTagList = new NBTTagList();
        for (@NotNull final Field f : fields.values())
        {
            @NotNull final NBTTagCompound fieldCompound = new NBTTagCompound();
            f.writeToNBT(fieldCompound);
            fieldTagList.appendTag(fieldCompound);
        }
        compound.setTag(TAG_FIELDS, fieldTagList);

        //  Citizens
        @NotNull final NBTTagList citizenTagList = new NBTTagList();
        for (@NotNull final CitizenData citizen : citizens.values())
        {
            @NotNull final NBTTagCompound citizenCompound = new NBTTagCompound();
            citizen.writeToNBT(citizenCompound);
            citizenTagList.appendTag(citizenCompound);
        }
        compound.setTag(TAG_CITIZENS, citizenTagList);

        //  Achievements
        @NotNull final NBTTagList achievementsTagList = new NBTTagList();
        for (@NotNull final Achievement achievement : this.colonyAchievements)
        {
            @NotNull final NBTTagCompound achievementCompound = new NBTTagCompound();
            achievementCompound.setString(TAG_ACHIEVEMENT, achievement.statId);
            achievementsTagList.appendTag(achievementCompound);
        }
        compound.setTag(TAG_ACHIEVEMENT_LIST, achievementsTagList);

        //  Workload
        @NotNull final NBTTagCompound workManagerCompound = new NBTTagCompound();
        workManager.writeToNBT(workManagerCompound);
        compound.setTag(TAG_WORK, workManagerCompound);

        // Waypoints
        @NotNull final NBTTagList wayPointTagList = new NBTTagList();
        for (@NotNull final Map.Entry<BlockPos, IBlockState> entry : wayPoints.entrySet())
        {
            @NotNull final NBTTagCompound wayPointCompound = new NBTTagCompound();
            BlockPosUtil.writeToNBT(wayPointCompound, TAG_WAYPOINT, entry.getKey());
            NBTUtil.writeBlockState(wayPointCompound, entry.getValue());

            wayPointTagList.appendTag(wayPointCompound);
        }
        compound.setTag(TAG_WAYPOINT, wayPointTagList);

        // Statistics
        @NotNull final NBTTagCompound statisticsCompound = new NBTTagCompound();
        @NotNull final NBTTagCompound minerStatisticsCompound = new NBTTagCompound();
        @NotNull final NBTTagCompound farmerStatisticsCompound = new NBTTagCompound();
        @NotNull final NBTTagCompound guardStatisticsCompound = new NBTTagCompound();
        @NotNull final NBTTagCompound builderStatisticsCompound = new NBTTagCompound();
        @NotNull final NBTTagCompound fishermanStatisticsCompound = new NBTTagCompound();
        @NotNull final NBTTagCompound lumberjackStatisticsCompound = new NBTTagCompound();
        compound.setTag(TAG_STATISTICS, statisticsCompound);
        statisticsCompound.setTag(TAG_MINER_STATISTICS, minerStatisticsCompound);
        minerStatisticsCompound.setInteger(TAG_MINER_ORES, minedOres);
        minerStatisticsCompound.setInteger(TAG_MINER_DIAMONDS, minedDiamonds);
        statisticsCompound.setTag(TAG_FARMER_STATISTICS, farmerStatisticsCompound);
        farmerStatisticsCompound.setInteger(TAG_FARMER_CARROTS, harvestedCarrots);
        farmerStatisticsCompound.setInteger(TAG_FARMER_POTATOES, harvestedPotatoes);
        farmerStatisticsCompound.setInteger(TAG_FARMER_WHEAT, harvestedWheat);
        statisticsCompound.setTag(TAG_GUARD_STATISTICS, guardStatisticsCompound);
        guardStatisticsCompound.setInteger(TAG_GUARD_MOBS, killedMobs);
        statisticsCompound.setTag(TAG_BUILDER_STATISTICS, builderStatisticsCompound);
        builderStatisticsCompound.setInteger(TAG_BUILDER_HUTS, builtHuts);
        statisticsCompound.setTag(TAG_FISHERMAN_STATISTICS, fishermanStatisticsCompound);
        fishermanStatisticsCompound.setInteger(TAG_FISHERMAN_FISH, caughtFish);
        statisticsCompound.setTag(TAG_LUMBERJACK_STATISTICS, lumberjackStatisticsCompound);
        lumberjackStatisticsCompound.setInteger(TAG_LUMBERJACK_TREES, felledTrees);
        lumberjackStatisticsCompound.setInteger(TAG_LUMBERJACK_SAPLINGS, plantedSaplings);

        // Free blocks
        @NotNull final NBTTagList freeBlocksTagList = new NBTTagList();
        for (@NotNull final Block block : freeBlocks)
        {
            freeBlocksTagList.appendTag(new NBTTagString(block.getRegistryName().toString()));
        }
        compound.setTag(TAG_FREE_BLOCKS, freeBlocksTagList);

        // Free positions
        @NotNull final NBTTagList freePositionsTagList = new NBTTagList();
        for (@NotNull final BlockPos pos : freePositions)
        {
            @NotNull final NBTTagCompound wayPointCompound = new NBTTagCompound();
            BlockPosUtil.writeToNBT(wayPointCompound, TAG_FREE_POSITIONS, pos);
            freePositionsTagList.appendTag(wayPointCompound);
        }
        compound.setTag(TAG_FREE_POSITIONS, freePositionsTagList);
    }

    @Override
    public int getDimension()
    {
        return dimensionId;
    }

    @Override
    public void incrementStatistic(@NotNull String statistic)
    {
        final int statisticAmount = this.getStatisticAmount(statistic);
        incrementStatisticAmount(statistic);
        if (statisticAmount >= NUM_ACHIEVEMENT_FIRST)
        {
            TriggerColonyAchievements.triggerFirstAchievement(statistic, this);
        }
        if (statisticAmount >= NUM_ACHIEVEMENT_SECOND)
        {
            TriggerColonyAchievements.triggerSecondAchievement(statistic, this);
        }
        if (statisticAmount >= NUM_ACHIEVEMENT_THIRD)
        {
            TriggerColonyAchievements.triggerThirdAchievement(statistic, this);
        }
        if (statisticAmount >= NUM_ACHIEVEMENT_FOURTH)
        {
            TriggerColonyAchievements.triggerFourthAchievement(statistic, this);
        }
        if (statisticAmount >= NUM_ACHIEVEMENT_FIFTH)
        {
            TriggerColonyAchievements.triggerFifthAchievement(statistic, this);
        }
    }

    @Override
    public int getStatisticAmount(@NotNull String statistic)
    {
        switch (statistic)
        {
            case TAG_GUARD_MOBS:
                return killedMobs;
            case TAG_MINER_ORES:
                return minedOres;
            case TAG_MINER_DIAMONDS:
                return minedDiamonds;
            case TAG_BUILDER_HUTS:
                return builtHuts;
            case TAG_FISHERMAN_FISH:
                return caughtFish;
            case TAG_FARMER_WHEAT:
                return harvestedWheat;
            case TAG_FARMER_POTATOES:
                return harvestedPotatoes;
            case TAG_FARMER_CARROTS:
                return harvestedCarrots;
            case TAG_LUMBERJACK_SAPLINGS:
                return plantedSaplings;
            case TAG_LUMBERJACK_TREES:
                return felledTrees;
            default:
                return 0;
        }
    }

    @Override
    public void incrementStatisticAmount(@NotNull String statistic)
    {
        switch (statistic)
        {
            case TAG_GUARD_MOBS:
                killedMobs++;
                break;
            case TAG_MINER_ORES:
                minedOres++;
                break;
            case TAG_MINER_DIAMONDS:
                minedDiamonds++;
                break;
            case TAG_BUILDER_HUTS:
                builtHuts++;
                break;
            case TAG_FISHERMAN_FISH:
                caughtFish++;
                break;
            case TAG_FARMER_WHEAT:
                harvestedWheat++;
                break;
            case TAG_FARMER_POTATOES:
                harvestedPotatoes++;
                break;
            case TAG_FARMER_CARROTS:
                harvestedCarrots++;
                break;
            case TAG_LUMBERJACK_SAPLINGS:
                plantedSaplings++;
                break;
            case TAG_LUMBERJACK_TREES:
                felledTrees++;
                break;
            default:
                break;
        }
    }

    @Override
    public void markBuildingsDirty()
    {
        isBuildingsDirty = true;
    }

    @Override
    public void onWorldLoad(@NotNull final World w)
    {
        if (w.provider.getDimension() == dimensionId)
        {
            world = w;
        }
    }

    @Override
    public void onWorldUnload(@NotNull final World w)
    {
        if (!w.equals(world))
        {
            throw new IllegalStateException("Colony's world does not match the event.");
        }

        world = null;
    }

    @Override
    public void onServerTick(@NotNull final TickEvent.ServerTickEvent event)
    {
        for (@NotNull final AbstractBuilding b : buildings.values())
        {
            b.onServerTick(event);
        }

        if (event.phase == TickEvent.Phase.END)
        {
            updateSubscribers();
        }
    }

    @Override
    public void updateSubscribers()
    {
        // If the world or server is null, don't try to update the subscribers this tick.
        if (world == null || world.getMinecraftServer() == null)
        {
            return;
        }

        //  Recompute subscribers every frame (for now)
        //  Subscribers = Owners + Players within (double working town hall range)
        @NotNull final Set<EntityPlayerMP> oldSubscribers = subscribers;
        subscribers = new HashSet<>();

        // Add owners
        world.getMinecraftServer().getPlayerList().getPlayers()
          .stream()
          .filter(permissions::isSubscriber)
          .forEachOrdered(subscribers::add);

        for (final EntityPlayer o : world.playerEntities)
        {
            if (o instanceof EntityPlayerMP)
            {
                @NotNull final EntityPlayerMP player = (EntityPlayerMP) o;

                final double distance = player.getDistanceSq(center);
                if (distance < MAX_SQ_DIST_SUBSCRIBER_UPDATE
                      || (oldSubscribers.contains(player) && distance < MAX_SQ_DIST_OLD_SUBSCRIBER_UPDATE))
                {
                    // Players become subscribers if they come within 16 blocks of the edge of the colony
                    // Players remain subscribers while they remain within double the colony's radius
                    subscribers.add(player);
                }
            }
        }

        if (!subscribers.isEmpty())
        {
            //  Determine if any new subscribers were added this pass
            final boolean hasNewSubscribers = ColonyUtils.hasNewSubscribers(oldSubscribers, subscribers);

            //  Send each type of update packet as appropriate:
            //      - To Subscribers if the data changes
            //      - To New Subscribers even if it hasn't changed

            //ColonyView
            sendColonyViewPackets(oldSubscribers, hasNewSubscribers);

            //Permissions
            sendPermissionsPackets(oldSubscribers, hasNewSubscribers);

            //WorkOrders
            sendWorkOrderPackets(oldSubscribers, hasNewSubscribers);

            //Citizens
            sendCitizenPackets(oldSubscribers, hasNewSubscribers);

            //Buildings
            sendBuildingPackets(oldSubscribers, hasNewSubscribers);

            //Fields
            if (!isBuildingsDirty)
            {
                sendFieldPackets(hasNewSubscribers);
            }

            //schematics
            if (Structures.isDirty())
            {
                sendSchematicsPackets(hasNewSubscribers);
                Structures.clearDirty();
            }
        }

        isFieldsDirty = false;
        isDirty = false;
        isCitizensDirty = false;
        isBuildingsDirty = false;
        permissions.clearDirty();

        buildings.values().forEach(AbstractBuilding::clearDirty);
        citizens.values().forEach(CitizenData::clearDirty);
    }

    @Override
    public void sendColonyViewPackets(@NotNull final Set<EntityPlayerMP> oldSubscribers, final boolean hasNewSubscribers)
    {
        if (isDirty || hasNewSubscribers)
        {
            for (final EntityPlayerMP player : subscribers)
            {
                final boolean isNewSubscriber = !oldSubscribers.contains(player);
                if (isDirty || isNewSubscriber)
                {
                    MineColonies.getNetwork().sendTo(new ColonyViewMessage(this, isNewSubscriber), player);
                }
            }
        }
    }

    @Override
    public void sendPermissionsPackets(@NotNull final Set<EntityPlayerMP> oldSubscribers, final boolean hasNewSubscribers)
    {
        if (permissions.isDirty() || hasNewSubscribers)
        {
            subscribers
              .stream()
              .filter(player -> permissions.isDirty() || !oldSubscribers.contains(player)).forEach(player ->
            {
                final Rank rank = getPermissions().getRank(player);
                MineColonies.getNetwork().sendTo(new PermissionsMessage.View(this, rank), player);
            });
        }
    }

    @Override
    public void sendWorkOrderPackets(@NotNull final Set<EntityPlayerMP> oldSubscribers, final boolean hasNewSubscribers)
    {
        if (getWorkManager().isDirty() || hasNewSubscribers)
        {
            for (final IWorkOrder workOrder : getWorkManager().getWorkOrders().values())
            {
                subscribers.stream().filter(player -> workManager.isDirty() || !oldSubscribers.contains(player))
                  .forEach(player -> MineColonies.getNetwork().sendTo(new ColonyViewWorkOrderMessage(this, workOrder), player));
            }

            getWorkManager().setDirty(false);
        }
    }

    @Override
    public void sendCitizenPackets(@NotNull final Set<EntityPlayerMP> oldSubscribers, final boolean hasNewSubscribers)
    {
        if (isCitizensDirty || hasNewSubscribers)
        {
            for (@NotNull final CitizenData citizen : citizens.values())
            {
                if (citizen.isDirty() || hasNewSubscribers)
                {
                    subscribers.stream()
                      .filter(player -> citizen.isDirty() || !oldSubscribers.contains(player))
                      .forEach(player -> MineColonies.getNetwork().sendTo(new ColonyViewCitizenViewMessage(this, citizen), player));
                }
            }
        }
    }

    @Override
    public void sendBuildingPackets(@NotNull final Set<EntityPlayerMP> oldSubscribers, final boolean hasNewSubscribers)
    {
        if (isBuildingsDirty || hasNewSubscribers)
        {
            for (@NotNull final AbstractBuilding building : buildings.values())
            {
                if (building.isDirty() || hasNewSubscribers)
                {
                    subscribers.stream()
                      .filter(player -> building.isDirty() || !oldSubscribers.contains(player))
                      .forEach(player -> MineColonies.getNetwork().sendTo(new ColonyViewBuildingViewMessage(building), player));
                }
            }
        }
    }

    @Override
    public void sendSchematicsPackets(final boolean hasNewSubscribers)
    {
        if (Structures.isDirty() || hasNewSubscribers)
        {
            subscribers.stream()
              .forEach(player -> MineColonies.getNetwork().sendTo(new ColonyStylesMessage(), player));
        }
    }

    @Override
    public void sendFieldPackets(final boolean hasNewSubscribers)
    {
        if ((isFieldsDirty && !isBuildingsDirty) || hasNewSubscribers)
        {
            for (final AbstractBuilding building : buildings.values())
            {
                if (building instanceof BuildingFarmer)
                {
                    subscribers.forEach(player -> MineColonies.getNetwork().sendTo(new ColonyViewBuildingViewMessage(building), player));
                }
            }
        }
    }

    @Override
    @NotNull
    public WorkManager getWorkManager()
    {
        return workManager;
    }

    @Override
    public Set<BlockPos> getFreePositions()
    {
        return new HashSet<>(freePositions);
    }

    @Override
    public Set<Block> getFreeBlocks()
    {
        return new HashSet<>(freeBlocks);
    }

    @Override
    public void addFreePosition(@NotNull final BlockPos pos)
    {
        freePositions.add(pos);
        markDirty();
    }

    @Override
    public void addFreeBlock(@NotNull final Block block)
    {
        freeBlocks.add(block);
        markDirty();
    }

    @Override
    public void removeFreePosition(@NotNull final BlockPos pos)
    {
        freePositions.remove(pos);
        markDirty();
    }

    @Override
    public void removeFreeBlock(@NotNull final Block block)
    {
        freeBlocks.remove(block);
        markDirty();
    }

    @Override
    public void onWorldTick(@NotNull final TickEvent.WorldTickEvent event)
    {
        if (event.world != getWorld())
        {
            /**
             * If the event world is not the colony world ignore. This might happen in interactions with other mods.
             * This should not be a problem for minecolonies as long as we take care to do nothing in that moment.
             */
            return;
        }

        if (event.phase == TickEvent.Phase.START)
        {
            //  Detect CitizenData whose EntityCitizen no longer exist in world, and clear the mapping
            //  Consider handing this in an ChunkUnload Event instead?
            citizens.values()
              .stream()
              .filter(ColonyUtils::isCitizenMissingFromWorld)
              .forEach(CitizenData::clearCitizenEntity);

            //  Cleanup disappeared citizens
            //  It would be really nice if we didn't have to do this... but Citizens can disappear without dying!
            //  Every CITIZEN_CLEANUP_TICK_INCREMENT, cleanup any 'lost' citizens
            if ((event.world.getWorldTime() % CITIZEN_CLEANUP_TICK_INCREMENT) == 0 && areAllColonyChunksLoaded(event) && townHall != null)
            {
                //  All chunks within a good range of the colony should be loaded, so all citizens should be loaded
                //  If we don't have any references to them, destroy the citizen
                citizens.values().forEach(this::spawnCitizenIfNull);
            }

            //  Cleanup Buildings whose Blocks have gone AWOL
            cleanUpBuildings(event);

            //  Spawn Citizens
            if (townHall != null && citizens.size() < maxCitizens)
            {
                int respawnInterval = Configurations.citizenRespawnInterval * 20;
                respawnInterval -= (60 * townHall.getBuildingLevel());

                if (event.world.getWorldTime() % respawnInterval == 0)
                {
                    spawnCitizen();
                }
            }
        }

        //  Tick Buildings
        for (@NotNull final AbstractBuilding building : buildings.values())
        {
            building.onWorldTick(event);
        }

        if (isDay && !world.isDaytime())
        {
            isDay = false;
            updateOverallHappiness();
        }
        else if (!isDay && world.isDaytime())
        {
            isDay = true;
        }

        updateWayPoints();
        workManager.onWorldTick(event);
    }

    @Override
    public void updateOverallHappiness()
    {
        int guards = 1;
        int housing = 0;
        int workers = 1;
        double saturation = 0;
        for (final CitizenData citizen : citizens.values())
        {
            final AbstractBuildingWorker buildingWorker = citizen.getWorkBuilding();
            if (buildingWorker != null)
            {
                if (buildingWorker instanceof BuildingGuardTower)
                {
                    guards += buildingWorker.getBuildingLevel();
                }
                else
                {
                    workers += buildingWorker.getBuildingLevel();
                }
            }

            final BuildingHome home = citizen.getHomeBuilding();
            if (home != null)
            {
                housing += home.getBuildingLevel();
            }

            saturation += citizen.getSaturation();
        }

        final int averageHousing = housing / Math.max(1, citizens.size());

        if (averageHousing > 1)
        {
            increaseOverallHappiness(averageHousing * HAPPINESS_FACTOR);
        }

        final int averageSaturation = (int) (saturation / citizens.size());
        if (averageSaturation < WELL_SATURATED_LIMIT)
        {
            decreaseOverallHappiness((averageSaturation - WELL_SATURATED_LIMIT) * -HAPPINESS_FACTOR);
        }
        else if (averageSaturation > WELL_SATURATED_LIMIT)
        {
            increaseOverallHappiness((averageSaturation - WELL_SATURATED_LIMIT) * HAPPINESS_FACTOR);
        }

        int relation = guards / workers;

        if (relation > 1)
        {
            decreaseOverallHappiness(relation * HAPPINESS_FACTOR);
        }
        markDirty();
    }

    @Override
    public void updateWayPoints()
    {
        final Random rand = new Random();
        if (rand.nextInt(CHECK_WAYPOINT_EVERY) <= 1 && wayPoints.size() > 0)
        {
            final Object[] entries = wayPoints.entrySet().toArray();
            final int stopAt = rand.nextInt(entries.length);
            final Object obj = entries[stopAt];

            if (obj instanceof Map.Entry && ((Map.Entry) obj).getKey() instanceof BlockPos && ((Map.Entry) obj).getValue() instanceof IBlockState)
            {
                @NotNull final BlockPos key = (BlockPos) ((Map.Entry) obj).getKey();
                @NotNull final IBlockState value = (IBlockState) ((Map.Entry) obj).getValue();
                if (world != null && world.getBlockState(key).getBlock() != (value.getBlock()))
                {
                    wayPoints.remove(key);
                }
            }
        }
    }

    /**
     * Returns the world the colony is in.
     *
     * @return World the colony is in.
     */
    @Nullable
    public World getWorld()
    {
        return world;
    }

    @Override
    public boolean areAllColonyChunksLoaded(@NotNull final TickEvent.WorldTickEvent event)
    {
        final int distanceFromCenter = Configurations.workingRangeTownHall + 48 /* 3 chunks */ + 15 /* round up a chunk */;
        for (int x = -distanceFromCenter; x <= distanceFromCenter; x += 16)
        {
            for (int z = -distanceFromCenter; z <= distanceFromCenter; z += 16)
            {
                if (!event.world.isBlockLoaded(new BlockPos(getCenter().getX() + x, 128, getCenter().getZ() + z)))
                {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void cleanUpBuildings(@NotNull final TickEvent.WorldTickEvent event)
    {
        @Nullable final List<AbstractBuilding> removedBuildings = new ArrayList<>();

        //Need this list, we may enter he while we add a building in the real world.
        final List<AbstractBuilding> tempBuildings = new ArrayList<>(buildings.values());

        for (@NotNull final AbstractBuilding building : tempBuildings)
        {
            final BlockPos loc = building.getLocation().getInDimensionLocation();
            if (event.world.isBlockLoaded(loc) && !building.isMatchingBlock(event.world.getBlockState(loc).getBlock()))
            {
                //  Sanity cleanup
                removedBuildings.add(building);
            }
        }

        removedBuildings.forEach(AbstractBuilding::destroy);

        @NotNull final ArrayList<Field> tempFields = new ArrayList<>(fields.values());

        for (@NotNull final Field field : tempFields)
        {
            if (event.world.isBlockLoaded(field.getLocation()))
            {
                final ScarecrowTileEntity scarecrow = (ScarecrowTileEntity) event.world.getTileEntity(field.getID());
                if (scarecrow == null)
                {
                    fields.remove(field.getID());
                }
                else
                {
                    field.setInventoryField(scarecrow.getInventoryField());
                }
            }
        }

        markFieldsDirty();
    }

    @Override
    public void spawnCitizen()
    {
        spawnCitizen(null);
    }

    /**
     * Returns the center of the colony.
     *
     * @return Chunk Coordinates of the center of the colony.
     */
    @Override
    public BlockPos getCenter()
    {
        return center;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void setName(final String n)
    {
        name = n;
        markDirty();
    }

    @Override
    public void markDirty()
    {
        isDirty = true;
    }

    @NotNull
    @Override
    public Permissions getPermissions()
    {
        return permissions;
    }

    @Override
    public boolean isCoordInColony(@NotNull final World w, @NotNull final BlockPos pos)
    {
        //  Perform a 2D distance calculation, so pass center.posY as the Y
        return w.equals(getWorld())
                 && BlockPosUtil.getDistanceSquared(center, new BlockPos(pos.getX(), center.getY(), pos.getZ())) <= MathUtils.square(Configurations.workingRangeTownHall);
    }

    @Override
    public long getDistanceSquared(@NotNull final BlockPos pos)
    {
        return BlockPosUtil.getDistanceSquared2D(center, pos);
    }

    @Override
    public boolean hasTownHall()
    {
        return townHall != null;
    }

    /**
     * Returns the ID of the colony.
     *
     * @return Colony ID.
     */
    @Override
    public int getID()
    {
        return id;
    }

    @Override
    public void markFieldsDirty()
    {
        isFieldsDirty = true;
    }

    @Override
    public void spawnCitizen(final CitizenData data)
    {
        final BlockPos townHallLocation = townHall.getLocation().getInDimensionLocation();
        if (!world.isBlockLoaded(townHallLocation))
        {
            //  Chunk with TownHall Block is not loaded
            return;
        }

        final BlockPos spawnPoint = EntityUtils.getSpawnPoint(world, townHallLocation);

        if (spawnPoint != null)
        {
            final EntityCitizen entity = new EntityCitizen(world);

            CitizenData citizenData = data;
            if (citizenData == null)
            {
                //This ensures that citizen IDs are getting reused.
                //That's needed to prevent bugs when calling IDs that are not used.
                for (int i = 1; i <= this.getMaxCitizens(); i++)
                {
                    if (this.getCitizen(i) == null)
                    {
                        topCitizenId = i;
                        break;
                    }
                }

                citizenData = new CitizenData(topCitizenId, this);
                citizenData.initializeFromEntity(entity);

                citizens.put(citizenData.getId(), citizenData);

                if (getMaxCitizens() == getCitizens().size())
                {
                    //TODO: add Colony Name prefix?
                    LanguageHandler.sendPlayersMessage(
                      this.getMessageEntityPlayers(),
                      "tile.blockHutTownHall.messageMaxSize");
                }
            }
            entity.setColony(this, citizenData);

            entity.setPosition(spawnPoint.getX() + 0.5D, spawnPoint.getY() + 0.1D, spawnPoint.getZ() + 0.5D);
            world.spawnEntity(entity);

            checkAchievements();

            markCitizensDirty();
        }
    }

    @Override
    public int getMaxCitizens()
    {
        return maxCitizens;
    }

    @Override
    public ICitizenData getCitizen(final int citizenId)
    {
        return citizens.get(citizenId);
    }

    @Override
    @NotNull
    public Map<Integer, ICitizenData> getCitizens()
    {
        return Collections.unmodifiableMap(citizens);
    }

    @Override
    @NotNull
    public List<EntityPlayer> getMessageEntityPlayers()
    {
        return ServerUtils.getPlayersFromUUID(this.world, this.getPermissions().getMessagePlayers());
    }

    @Override
    public void checkAchievements()
    {
        // the colonies size
        final int size = this.citizens.size();

        if (size >= ModAchievements.ACHIEVEMENT_SIZE_SETTLEMENT)
        {
            this.triggerAchievement(ModAchievements.achievementSizeSettlement);
        }

        if (size >= ModAchievements.ACHIEVEMENT_SIZE_TOWN)
        {
            this.triggerAchievement(ModAchievements.achievementSizeTown);
        }

        if (size >= ModAchievements.ACHIEVEMENT_SIZE_CITY)
        {
            this.triggerAchievement(ModAchievements.achievementSizeCity);
        }

        if (size >= ModAchievements.ACHIEVEMENT_SIZE_METROPOLIS)
        {
            this.triggerAchievement(ModAchievements.achievementSizeMetropolis);
        }
    }

    @Override
    public void markCitizensDirty()
    {
        isCitizensDirty = true;
    }

    @Override
    public void triggerAchievement(@NotNull final Achievement achievement)
    {
        if (this.colonyAchievements.contains(achievement))
        {
            return;
        }

        this.colonyAchievements.add(achievement);

        AchievementUtils.syncAchievements(this);
    }

    @Override
    public void spawnCitizenIfNull(@NotNull final CitizenData data)
    {
        if (data.getCitizen() == null)
        {
            Log.getLogger().warn(String.format("Citizen #%d:%d has gone AWOL, respawning them!", this.getID(), data.getId()));
            spawnCitizen(data);
        }
    }

    @Override
    @Nullable
    public IBuilding getTownHall()
    {
        return townHall;
    }

    @Override
    @NotNull
    public Map<BlockPos, Field> getFields()
    {
        return Collections.unmodifiableMap(fields);
    }

    @Override
    public Field getField(final BlockPos fieldId)
    {
        return fields.get(fieldId);
    }

    @Override
    @Nullable
    public Field getFreeField(final String owner)
    {
        for (@NotNull final Field field : fields.values())
        {
            if (!field.isTaken())
            {
                field.setTaken(true);
                field.setOwner(owner);
                markFieldsDirty();
                return field;
            }
        }
        return null;
    }

    /**
     * Get building in Colony by ID.
     *
     * @param buildingId ID (coordinates) of the building to get.
     * @return AbstractBuilding belonging to the given ID.
     */
    @Override
    public AbstractBuilding getBuilding(final BlockPos buildingId)
    {
        return buildings.get(buildingId);
    }

    @Override
    @Nullable
    public <B extends AbstractBuilding> B getBuilding(final BlockPos buildingId, @NotNull final Class<B> type)
    {
        try
        {
            return type.cast(buildings.get(buildingId));
        }
        catch (final ClassCastException e)
        {
            Log.getLogger().warn("getBuilding called with wrong type: ", e);
            return null;
        }
    }

    @Override
    public void addNewField(final ScarecrowTileEntity tileEntity, final InventoryPlayer inventoryPlayer, final BlockPos pos, final World world)
    {
        @NotNull final Field field = new Field(tileEntity, inventoryPlayer, world, pos);
        //field.setCustomName(LanguageHandler.format("com.minecolonies.coremod.gui.scarecrow.user", LanguageHandler.format("com.minecolonies.coremod.gui.scarecrow.user.noone")));
        addField(field);
        field.calculateSize(world, pos);
        markFieldsDirty();
    }

    @Override
    @Nullable
    public AbstractBuilding addNewBuilding(@NotNull final TileEntityColonyBuilding tileEntity)
    {
        tileEntity.setColony(this);
        @Nullable final AbstractBuilding building = AbstractBuilding.create(this, tileEntity);
        if (building != null)
        {
            addBuilding(building);
            tileEntity.setBuilding(building);

            Log.getLogger().info(String.format("Colony %d - new AbstractBuilding for %s at %s",
              getID(),
              tileEntity.getBlockType().getClass(),
              tileEntity.getPosition()));
            if (tileEntity.isMirrored())
            {
                building.setMirror();
            }
            if (!tileEntity.getStyle().isEmpty())
            {
                building.setStyle(tileEntity.getStyle());
            }
            ConstructionTapeHelper.placeConstructionTape(building, world);
        }
        else
        {
            Log.getLogger().error(String.format("Colony %d unable to create AbstractBuilding for %s at %s",
              getID(),
              tileEntity.getBlockType().getClass(),
              tileEntity.getPosition()));
        }

        calculateMaxCitizens();
        ColonyManager.markDirty();

        return building;
    }

    @Override
    public void calculateMaxCitizens()
    {
        int newMaxCitizens = 0;

        for (final AbstractBuilding b : buildings.values())
        {
            if (b instanceof BuildingHome && b.getBuildingLevel() > 0)
            {
                newMaxCitizens += ((BuildingHome) b).getMaxInhabitants();
            }
        }
        // Have at least the minimum amount of citizens
        newMaxCitizens = Math.max(Configurations.maxCitizens, newMaxCitizens);
        if (maxCitizens != newMaxCitizens)
        {
            maxCitizens = newMaxCitizens;
            markDirty();
        }
    }

    @Override
    public void removeBuilding(@NotNull final AbstractBuilding building)
    {
        if (buildings.remove(building.getID()) != null)
        {
            for (final EntityPlayerMP player : subscribers)
            {
                MineColonies.getNetwork().sendTo(new ColonyViewRemoveBuildingMessage(this, building.getLocation().getInDimensionLocation()), player);
            }

            Log.getLogger().info(String.format("Colony %d - removed AbstractBuilding %s of type %s",
              getID(),
              building.getID(),
              building.getSchematicName()));
        }

        if (building instanceof BuildingTownHall)
        {
            townHall = null;
        }

        //Allow Citizens to fix up any data that wasn't fixed up by the AbstractBuilding's own onDestroyed
        for (@NotNull final CitizenData citizen : citizens.values())
        {
            citizen.onRemoveBuilding(building);
        }

        calculateMaxCitizens();

        ColonyManager.markDirty();
    }

    @Override
    public boolean isManualHiring()
    {
        return manualHiring;
    }

    @Override
    public void setManualHiring(final boolean manualHiring)
    {
        this.manualHiring = manualHiring;
        markDirty();
    }

    @Override
    public void removeCitizen(@NotNull final CitizenData citizen)
    {
        //Remove the Citizen
        citizens.remove(citizen.getId());

        for (@NotNull final AbstractBuilding building : buildings.values())
        {
            building.removeCitizen(citizen);
        }

        workManager.clearWorkForCitizen(citizen);

        //  Inform Subscribers of removed citizen
        for (final EntityPlayerMP player : subscribers)
        {
            MineColonies.getNetwork().sendTo(new ColonyViewRemoveCitizenMessage(this, citizen.getId()), player);
        }
    }

    @Override
    public void removeWorkOrder(final int orderId)
    {
        //  Inform Subscribers of removed workOrder
        for (final EntityPlayerMP player : subscribers)
        {
            MineColonies.getNetwork().sendTo(new ColonyViewRemoveWorkOrderMessage(this, orderId), player);
        }
    }

    @Override
    @Nullable
    public CitizenData getJoblessCitizen()
    {
        for (@NotNull final CitizenData citizen : citizens.values())
        {
            if (citizen.getWorkBuilding() == null)
            {
                return citizen;
            }
        }

        return null;
    }

    @Override
    public List<BlockPos> getDeliverymanRequired()
    {

        return citizens.values().stream()
                 .filter(citizen -> citizen.getWorkBuilding() != null && citizen.getJob() != null)
                 .filter(citizen -> !citizen.getJob().isMissingNeededItem())
                 .map(citizen -> citizen.getWorkBuilding().getLocation().getInDimensionLocation())
                 .collect(Collectors.toList());
    }

    @Override
    public void onBuildingUpgradeComplete(@NotNull final IBuilding building, final int level)
    {
        building.onUpgradeComplete(level);
    }

    @Override
    @NotNull
    public List<Achievement> getAchievements()
    {
        return Collections.unmodifiableList(this.colonyAchievements);
    }

    @Override
    public void removeField(final BlockPos pos)
    {
        this.markFieldsDirty();
        fields.remove(pos);
    }

    @Override
    public void addWayPoint(final BlockPos point, IBlockState block)
    {
        wayPoints.put(point, block);
    }

    @Override
    @NotNull
    public List<BlockPos> getWayPoints(@NotNull final BlockPos position, @NotNull final BlockPos target)
    {
        final List<BlockPos> tempWayPoints = new ArrayList<>();
        tempWayPoints.addAll(wayPoints.keySet());
        tempWayPoints.addAll(getBuildings().keySet());

        final double maxX = Math.max(position.getX(), target.getX());
        final double maxZ = Math.max(position.getZ(), target.getZ());

        final double minX = Math.min(position.getX(), target.getX());
        final double minZ = Math.min(position.getZ(), target.getZ());

        final List<BlockPos> wayPointsCopy = new ArrayList<>(tempWayPoints);
        for (final BlockPos p : wayPointsCopy)
        {
            final int x = p.getX();
            final int z = p.getZ();
            if (x < minX || x > maxX || z < minZ || z > maxZ)
            {
                tempWayPoints.remove(p);
            }
        }

        return tempWayPoints;
    }

    @Override
    public double getOverallHappiness()
    {
        return this.overallHappiness;
    }

    @Override
    public void increaseOverallHappiness(double amount)
    {
        this.overallHappiness = Math.min(this.overallHappiness + Math.abs(amount), MAX_OVERALL_HAPPINESS);
        this.markDirty();
    }

    @Override
    public void decreaseOverallHappiness(double amount)
    {
        this.overallHappiness = Math.max(this.overallHappiness - Math.abs(amount), MIN_OVERALL_HAPPINESS);
        this.markDirty();
    }

    /**
     * Returns a map with all buildings within the colony.
     * Key is ID (Coordinates), value is building object.
     *
     * @return Map with ID (coordinates) as key, and buildings as value.
     */
    @NotNull
    public ImmutableMap<BlockPos, IBuilding> getBuildings()
    {
        return ImmutableMap.copyOf(buildings);
    }

    /**
     * Returns the request manager for the colony.
     *
     * @return The request manager.
     */
    @NotNull
    @Override
    public IRequestManager getRequestManager() {
        return requestManager;
    }

    @NotNull
    @Override
    public IFactoryController getFactoryController()
    {
        return getRequestManager().getFactoryController();
    }
}
