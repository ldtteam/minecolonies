package com.minecolonies.coremod.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.Suppression;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.achievements.MineColoniesAchievement;
import com.minecolonies.coremod.colony.buildings.*;
import com.minecolonies.coremod.colony.managers.BuildingManager;
import com.minecolonies.coremod.colony.managers.CitizenManager;
import com.minecolonies.coremod.colony.managers.IBuildingManager;
import com.minecolonies.coremod.colony.managers.ICitizenManager;
import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.colony.requestsystem.management.manager.StandardRequestManager;
import com.minecolonies.coremod.colony.workorders.AbstractWorkOrder;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.mobs.util.MobEventsUtils;
import com.minecolonies.coremod.network.messages.*;
import com.minecolonies.coremod.permissions.ColonyPermissionEventHandler;
import com.minecolonies.coremod.util.AchievementUtils;
import com.minecolonies.coremod.util.ColonyUtils;
import com.minecolonies.coremod.util.ServerUtils;
import net.minecraft.advancements.Advancement;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Tuple;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.ColonyConstants.*;
import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * This class describes a colony and contains all the data and methods for
 * manipulating a Colony.
 */
@SuppressWarnings({Suppression.BIG_CLASS, Suppression.SPLIT_CLASS})
public class Colony implements IColony
{
    /**
     * The default style for the building.
     */
    private String style = DEFAULT_STYLE;

    /**
     * Id of the colony.
     */
    private final int id;
    /**
     * Dimension of the colony.
     */
    private final int dimensionId;

    /**
     * List of waypoints of the colony.
     */
    private final Map<BlockPos, IBlockState> wayPoints = new HashMap<>();

    /**
     * List of achievements within the colony.
     */
    @NotNull
    private final List<Advancement> colonyAchievements;

    /**
     * Work Manager of the colony (Request System).
     */
    private final WorkManager workManager = new WorkManager(this);

    /**
     * Building manager of the colony.
     */
    private final IBuildingManager buildingManager = new BuildingManager();

    /**
     * Building manager of the colony.
     */
    private final ICitizenManager citizenManager = new CitizenManager();

    /**
     * The Positions which players can freely interact.
     */
    private final Set<BlockPos> freePositions = new HashSet<>();

    /**
     * The Blocks which players can freely interact with.
     */
    private final Set<Block> freeBlocks = new HashSet<>();

    /**
     * Colony permission event handler.
     */
    private final ColonyPermissionEventHandler eventHandler;

    /**
     * Whether there will be a raid in this colony tonight.
     */
    private boolean willRaidTonight = false;

    /**
     * The hours the colony is without contact with its players.
     */
    private int lastContactInHours = 0;

    /**
     * Whether or not the raid has been calculated for today.
     */
    private boolean hasRaidBeenCalculated = false;

    /**
     * Whether or not this colony may have Barbarian events. (set via command)
     */
    private boolean canHaveBarbEvents = true;

    /**
     * Whether or not this colony may be auto-deleted.
     */
    private boolean canColonyBeAutoDeleted = true;

    /**
     * Variable to determine if its currently day or night.
     */
    private boolean isDay = true;

    /**
     * Statistical values.
     */
    private int minedOres         = 0;
    private int minedDiamonds     = 0;
    private int harvestedWheat    = 0;
    private int harvestedPotatoes = 0;
    private int harvestedCarrots  = 0;
    private int killedMobs        = 0;
    private int builtHuts         = 0;
    private int caughtFish        = 0;
    private int felledTrees       = 0;
    private int plantedSaplings   = 0;

    /**
     * The world the colony currently runs on.
     */
    @Nullable
    private World world = null;

    /**
     * List of players subscribing to the colony.
     */
    @NotNull
    private Set<EntityPlayerMP> subscribers = new HashSet<>();

    /**
     * Variables taking care of updating the views.
     */
    private boolean isDirty          = false;

    /**
     * The hiring mode in the colony.
     */
    private boolean manualHiring = false;

    /**
     * The housing mode in the colony.
     */
    private boolean manualHousing = false;

    /**
     * The name of the colony.
     */
    private String name = "ERROR(Wasn't placed by player)";

    /**
     * The center of the colony.
     */
    private BlockPos center;

    /**
     * The colony permission object.
     */
    @NotNull
    private Permissions permissions;

    /**
     * Overall happyness of the colony.
     */
    private double overallHappiness = DEFAULT_OVERALL_HAPPYNESS;

    /**
     * Amount of ticks passed.
     */
    private int ticksPassed = 0;

    /**
     * The request manager assigned to the colony.
     */
    private IRequestManager requestManager;

    /**
     * Constructor for a newly created Colony.
     *
     * @param id The id of the colony to create.
     * @param w  The world the colony exists in.
     * @param c  The center of the colony (location of Town Hall).
     */
    @SuppressWarnings("squid:S2637")
    Colony(final int id, @NotNull final World w, final BlockPos c)
    {
        this(id, w);
        center = c;
        world = w;
        this.permissions = new Permissions(this);
        requestManager = new StandardRequestManager(this);
    }

    /**
     * Base constructor.
     *
     * @param id    The current id for the colony.
     * @param world The world the colony exists in.
     */
    protected Colony(final int id, final World world)
    {
        this.id = id;
        this.dimensionId = world.provider.getDimension();
        this.world = world;
        this.permissions = new Permissions(this);
        this.colonyAchievements = new ArrayList<>();

        // Register a new event handler
        eventHandler = new ColonyPermissionEventHandler(this);
        MinecraftForge.EVENT_BUS.register(eventHandler);

        for (final String s : Configurations.gameplay.freeToInteractBlocks)
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
    public static Colony loadColony(@NotNull final NBTTagCompound compound, @NotNull final World world)
    {
        final int id = compound.getInteger(TAG_ID);
        @NotNull final Colony c = new Colony(id, world);
        c.setName(compound.getString(TAG_NAME));
        c.center = BlockPosUtil.readFromNBT(compound, TAG_CENTER);
        c.setRequestManager();
        c.readFromNBT(compound);
        return c;
    }

    /**
     * Sets the request manager on colony load.
     */
    private void setRequestManager()
    {
        requestManager = new StandardRequestManager(this);
    }

    /**
     * Read colony from saved data.
     *
     * @param compound compound to read from.
     */
    private void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        manualHiring = compound.getBoolean(TAG_MANUAL_HIRING);

        // Permissions
        permissions.loadPermissions(compound);

        if(compound.hasKey(TAG_CITIZEN_MANAGER))
        {
            citizenManager.readFromNBT(compound.getCompoundTag(TAG_CITIZEN_MANAGER), this);
        }
        else
        {
            //Compatability with old version!
            citizenManager.readFromNBT(compound, this);
        }

        if(compound.hasKey(TAG_BUILDING_MANAGER))
        {
            buildingManager.readFromNBT(compound.getCompoundTag(TAG_BUILDING_MANAGER), this);
        }
        else
        {
            //Compatability with old version!
            buildingManager.readFromNBT(compound, this);
        }

        // Restore colony achievements
        final NBTTagList achievementTagList = compound.getTagList(TAG_ACHIEVEMENT_LIST, NBT.TAG_COMPOUND);
        for (int i = 0; i < achievementTagList.tagCount(); ++i)
        {
            final NBTTagCompound achievementCompound = achievementTagList.getCompoundTagAt(i);
            final String achievementKey = achievementCompound.getString(TAG_ACHIEVEMENT);

            //todo serialization
            /*final StatBase statBase = StatList.getOneShotStat(achievementKey);
             if (statBase instanceof Advancement)
            {
                colonyAchievements.add((Advancement) statBase);
            }*/
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

        if (compound.hasKey(TAG_HAPPINESS))
        {
            this.overallHappiness = compound.getDouble(TAG_HAPPINESS);
        }
        else
        {
            this.overallHappiness = AVERAGE_HAPPINESS;
        }
        lastContactInHours = compound.getInteger(TAG_ABANDONED);
        manualHousing = compound.getBoolean(TAG_MANUAL_HOUSING);

        if (compound.hasKey(TAG_REQUESTMANAGER))
        {
            this.requestManager.deserializeNBT(compound.getCompoundTag(TAG_REQUESTMANAGER));
        }

        if(compound.hasKey(TAG_STYLE))
        {
            this.style = compound.getString(TAG_STYLE);
        }
    }

    /**
     * Get the event handler assigned to the colony.
     *
     * @return the ColonyPermissionEventHandler.
     */
    public ColonyPermissionEventHandler getEventHandler()
    {
        return eventHandler;
    }

    /**
     * Write colony to save data.
     *
     * @param compound compound to write to.
     */
    protected void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        //  Core attributes
        compound.setInteger(TAG_ID, id);
        compound.setInteger(TAG_DIMENSION, dimensionId);

        //  Basic data
        compound.setString(TAG_NAME, name);
        BlockPosUtil.writeToNBT(compound, TAG_CENTER, center);

        compound.setBoolean(TAG_MANUAL_HIRING, manualHiring);

        // Permissions
        permissions.savePermissions(compound);

        final NBTTagCompound buildingCompound = new NBTTagCompound();
        buildingManager.writeToNBT(buildingCompound);
        compound.setTag(TAG_COMPATABILITY_MANAGER, buildingCompound);

        final NBTTagCompound citizenCompound = new NBTTagCompound();
        citizenManager.writeToNBT(citizenCompound);
        compound.setTag(TAG_COMPATABILITY_MANAGER, citizenCompound);

        //  Achievements
        @NotNull final NBTTagList achievementsTagList = new NBTTagList();
        for (@NotNull final Advancement achievement : this.colonyAchievements)
        {
            @NotNull final NBTTagCompound achievementCompound = new NBTTagCompound();
            //todo deserialization
            /*achievementCompound.setString(TAG_ACHIEVEMENT, achievement.);
            achievementsTagList.appendTag(achievementCompound);*/
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

        compound.setDouble(TAG_HAPPINESS, overallHappiness);
        compound.setInteger(TAG_ABANDONED, lastContactInHours);
        compound.setBoolean(TAG_MANUAL_HOUSING, manualHousing);
        compound.setTag(TAG_REQUESTMANAGER, getRequestManager().serializeNBT());
        compound.setString(TAG_STYLE, style);
    }

    /**
     * Returns the dimension ID.
     *
     * @return Dimension ID.
     */
    public int getDimension()
    {
        return dimensionId;
    }

    /**
     * Increment the statistic amount and trigger achievement.
     *
     * @param statistic the statistic.
     */
    public void incrementStatistic(@NotNull final String statistic)
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

    /**
     * Get the amount of statistic.
     *
     * @param statistic the statistic.
     * @return amount of statistic.
     */
    private int getStatisticAmount(@NotNull final String statistic)
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

    /**
     * increment statistic amount.
     *
     * @param statistic the statistic.
     */
    private void incrementStatisticAmount(@NotNull final String statistic)
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

    /**
     * When the Colony's world is loaded, associate with it.
     *
     * @param w World object.
     */
    public void onWorldLoad(@NotNull final World w)
    {
        if (w.provider.getDimension() == dimensionId)
        {
            world = w;
        }
    }

    /**
     * Unsets the world if the world unloads.
     *
     * @param w World object.
     */
    public void onWorldUnload(@NotNull final World w)
    {
        if (!w.equals(world))
        {
            /**
             * If the event world is not the colony world ignore. This might happen in interactions with other mods.
             * This should not be a problem for minecolonies as long as we take care to do nothing in that moment.
             */
            return;
        }

        world = null;
    }

    /**
     * Any per-server-tick logic should be performed here.
     *
     * @param event {@link net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent}
     */
    public void onServerTick(@NotNull final TickEvent.ServerTickEvent event)
    {
        buildingManager.tick(event);

        getRequestManager().update();

        if (event.phase == TickEvent.Phase.END)
        {
            updateSubscribers();
        }
    }

    /**
     * Update Subscribers with Colony, Citizen, and AbstractBuilding Views.
     */
    private void updateSubscribers()
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
          .forEach(subscribers::add);

        if (subscribers.isEmpty())
        {
            if (ticksPassed >= TICKS_HOUR)
            {
                ticksPassed = 0;
                lastContactInHours++;
            }
            ticksPassed++;
        }
        else
        {
            lastContactInHours = 0;
            ticksPassed = 0;
            lastContactInHours = 0;
        }

        //  Add nearby players
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

            citizenManager.sendPackets(oldSubscribers, hasNewSubscribers, subscribers, this);

            buildingManager.sendPackets(oldSubscribers, hasNewSubscribers, subscribers);

            //schematics
            if (Structures.isDirty())
            {
                sendSchematicsPackets(hasNewSubscribers);
                Structures.clearDirty();
            }
        }

        isDirty = false;
        permissions.clearDirty();

        buildingManager.clearDirty();
        citizenManager.clearDirty();
    }

    private void sendColonyViewPackets(@NotNull final Set<EntityPlayerMP> oldSubscribers, final boolean hasNewSubscribers)
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

    /**
     * Sends packages to update the permissions.
     *
     * @param oldSubscribers    the existing subscribers.
     * @param hasNewSubscribers the new subscribers.
     */
    private void sendPermissionsPackets(@NotNull final Set<EntityPlayerMP> oldSubscribers, final boolean hasNewSubscribers)
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

    /**
     * Sends packages to update the workOrders.
     *
     * @param oldSubscribers    the existing subscribers.
     * @param hasNewSubscribers the new subscribers.
     */
    private void sendWorkOrderPackets(@NotNull final Set<EntityPlayerMP> oldSubscribers, final boolean hasNewSubscribers)
    {
        if (getWorkManager().isDirty() || hasNewSubscribers)
        {
            for (final AbstractWorkOrder workOrder : getWorkManager().getWorkOrders().values())
            {
                subscribers.stream().filter(player -> workManager.isDirty() || !oldSubscribers.contains(player))
                  .forEach(player -> MineColonies.getNetwork().sendTo(new ColonyViewWorkOrderMessage(this, workOrder), player));
            }

            getWorkManager().setDirty(false);
        }
    }

    /**
     * Sends packages to update the schematics.
     *
     * @param hasNewSubscribers the new subscribers.
     */
    private void sendSchematicsPackets(final boolean hasNewSubscribers)
    {
        if (Structures.isDirty() || hasNewSubscribers)
        {
            subscribers.stream()
              .forEach(player -> MineColonies.getNetwork().sendTo(new ColonyStylesMessage(), player));
        }
    }

    /**
     * Get the Work Manager for the Colony.
     *
     * @return WorkManager for the Colony.
     */
    @NotNull
    public WorkManager getWorkManager()
    {
        return workManager;
    }

    /**
     * Get a copy of the freePositions list.
     *
     * @return the list of free to interact positions.
     */
    public Set<BlockPos> getFreePositions()
    {
        return new HashSet<>(freePositions);
    }

    /**
     * Get a copy of the freeBlocks list.
     *
     * @return the list of free to interact blocks.
     */
    public Set<Block> getFreeBlocks()
    {
        return new HashSet<>(freeBlocks);
    }

    /**
     * Add a new free to interact position.
     *
     * @param pos position to add.
     */
    public void addFreePosition(@NotNull final BlockPos pos)
    {
        freePositions.add(pos);
        markDirty();
    }

    /**
     * Add a new free to interact block.
     *
     * @param block block to add.
     */
    public void addFreeBlock(@NotNull final Block block)
    {
        freeBlocks.add(block);
        markDirty();
    }

    /**
     * Remove a free to interact position.
     *
     * @param pos position to remove.
     */
    public void removeFreePosition(@NotNull final BlockPos pos)
    {
        freePositions.remove(pos);
        markDirty();
    }

    /**
     * Remove a free to interact block.
     *
     * @param block state to remove.
     */
    public void removeFreeBlock(@NotNull final Block block)
    {
        freeBlocks.remove(block);
        markDirty();
    }

    /**
     * Any per-world-tick logic should be performed here.
     * NOTE: If the Colony's world isn't loaded, it won't have a world tick.
     * Use onServerTick for logic that should _always_ run.
     *
     * @param event {@link TickEvent.WorldTickEvent}
     */
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
            citizenManager.getCitizens()
              .stream()
              .filter(ColonyUtils::isCitizenMissingFromWorld)
              .forEach(CitizenData::clearCitizenEntity);

            //  Cleanup disappeared citizens
            //  It would be really nice if we didn't have to do this... but Citizens can disappear without dying!
            //  Every CITIZEN_CLEANUP_TICK_INCREMENT, cleanup any 'lost' citizens
            if (shallUpdate(event.world, CITIZEN_CLEANUP_TICK_INCREMENT) && areAllColonyChunksLoaded(event) && buildingManager.getTownHall() != null)
            {
                //  All chunks within a good range of the colony should be loaded, so all citizens should be loaded
                //  If we don't have any references to them, destroy the citizen
                citizenManager.getCitizens().forEach(citizenData -> citizenManager.spawnCitizenIfNull(citizenData, world, buildingManager, this));
            }

            //  Cleanup Buildings whose Blocks have gone AWOL
            buildingManager.cleanUpBuildings(event);

            //  Spawn Citizens
            if (buildingManager.getTownHall() != null && citizenManager.getCitizens().size() < citizenManager.getMaxCitizens())
            {
                int respawnInterval = Configurations.gameplay.citizenRespawnInterval * TICKS_SECOND;
                respawnInterval -= (SECONDS_A_MINUTE * buildingManager.getTownHall().getBuildingLevel());

                if ((event.world.getTotalWorldTime() + 1) % (respawnInterval + 1) == 0)
                {
                    citizenManager.spawnCitizen(this);
                }
            }

            if (shallUpdate(world, TICKS_SECOND)
                  && event.world.getDifficulty() != EnumDifficulty.PEACEFUL
                  && Configurations.gameplay.doBarbariansSpawn
                  && canHaveBarbEvents
                  && !world.getMinecraftServer().getPlayerList().getPlayers()
                        .stream().filter(permissions::isSubscriber).collect(Collectors.toList()).isEmpty()
                  && MobEventsUtils.isItTimeToRaid(event.world, this))
            {
                MobEventsUtils.barbarianEvent(event.world, this);
            }
        }

        buildingManager.onWorldTick(event);

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

    /**
     * Calculate randomly if the colony should update the citizens.
     * By mean they update it at CITIZEN_CLEANUP_TICK_INCREMENT.
     *
     * @param world the world.
     * @return a boolean by random.
     */
    private static boolean shallUpdate(final World world, final int averageTicks)
    {
        return world.getWorldTime() % (world.rand.nextInt(averageTicks * 2) + 1) == 0;
    }

    private boolean areAllColonyChunksLoaded(@NotNull final TickEvent.WorldTickEvent event)
    {
        final int distanceFromCenter = Configurations.gameplay.workingRangeTownHall + 48 /* 3 chunks */ + 15 /* round up a chunk */;
        for (int x = -distanceFromCenter; x <= distanceFromCenter; x += CONST_CHUNKSIZE)
        {
            for (int z = -distanceFromCenter; z <= distanceFromCenter; z += CONST_CHUNKSIZE)
            {
                if (!event.world.isBlockLoaded(new BlockPos(getCenter().getX() + x, 1, getCenter().getZ() + z)))
                {
                    return false;
                }
            }
        }
        return true;
    }

    private void updateOverallHappiness()
    {
        int guards = 1;
        int housing = 0;
        int workers = 1;
        double saturation = 0;
        for (final CitizenData citizen : citizenManager.getCitizens())
        {
            final AbstractBuildingWorker buildingWorker = citizen.getWorkBuilding();
            if (buildingWorker != null)
            {
                if (buildingWorker instanceof AbstractBuildingGuards)
                {
                    guards += buildingWorker.getBuildingLevel();
                }
                else
                {
                    workers += buildingWorker.getBuildingLevel();
                }
            }

            final AbstractBuilding home = citizen.getHomeBuilding();
            if (home != null)
            {
                housing += home.getBuildingLevel();
            }

            saturation += citizen.getSaturation();
        }

        final int averageHousing = housing / Math.max(1, citizenManager.getCitizens().size());

        if (averageHousing > 1)
        {
            increaseOverallHappiness(averageHousing * HAPPINESS_FACTOR);
        }

        final int averageSaturation = (int) (saturation / citizenManager.getCitizens().size());
        if (averageSaturation < WELL_SATURATED_LIMIT)
        {
            decreaseOverallHappiness((averageSaturation - WELL_SATURATED_LIMIT) * -HAPPINESS_FACTOR);
        }
        else if (averageSaturation > WELL_SATURATED_LIMIT)
        {
            increaseOverallHappiness((averageSaturation - WELL_SATURATED_LIMIT) * HAPPINESS_FACTOR);
        }

        final int relation = workers / guards;

        if (relation > 1)
        {
            decreaseOverallHappiness(relation * HAPPINESS_FACTOR);
        }
        markDirty();
    }

    /**
     * Update the waypoints after worldTicks.
     */
    private void updateWayPoints()
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
                    markDirty();
                }
            }
        }
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

    /**
     * Sets the name of the colony.
     * Marks dirty.
     *
     * @param n new name.
     */
    public void setName(final String n)
    {
        name = n;
        markDirty();
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
                 && BlockPosUtil.getDistanceSquared(center, new BlockPos(pos.getX(), center.getY(), pos.getZ())) <= MathUtils.square(Configurations.gameplay.workingRangeTownHall);
    }

    @Override
    public long getDistanceSquared(@NotNull final BlockPos pos)
    {
        return BlockPosUtil.getDistanceSquared2D(center, pos);
    }

    @Override
    public boolean hasTownHall()
    {
        return buildingManager.hasTownHall();
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
    public boolean hasWarehouse()
    {
        return buildingManager.hasWarehouse();
    }

    @Override
    public int getLastContactInHours()
    {
        return lastContactInHours;
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

    @Nullable
    @Override
    public IRequestManager getRequestManager()
    {
        return requestManager;
    }

    @Override
    public boolean hasWillRaidTonight()
    {
        return willRaidTonight;
    }

    /**
     * Marks the instance dirty.
     */
    public void markDirty()
    {
        ColonyManager.markDirty();
        isDirty = true;
    }

    @Override
    public boolean canBeAutoDeleted()
    {
        return canColonyBeAutoDeleted;
    }

    @Override
    public boolean isCanHaveBarbEvents()
    {
        return canHaveBarbEvents;
    }

    @Override
    public boolean isHasRaidBeenCalculated()
    {
        return hasRaidBeenCalculated;
    }

    public void setHasRaidBeenCalculated(final Boolean hasSet)
    {
        hasRaidBeenCalculated = hasSet;
    }

    @Nullable
    @Override
    public IRequester getRequesterBuildingForPosition(@NotNull final BlockPos pos)
    {
        return buildingManager.getBuilding(pos);
    }

    public void setCanHaveBarbEvents(final Boolean canHave)
    {
        this.canHaveBarbEvents = canHave;
    }

    /**
     * Increase the overall happiness by an amount, cap at max.
     *
     * @param amount the amount.
     */
    public void increaseOverallHappiness(final double amount)
    {
        this.overallHappiness = Math.min(this.overallHappiness + Math.abs(amount), MAX_OVERALL_HAPPINESS);
        this.markDirty();
    }

    /**
     * Decrease the overall happiness by an amount, cap at min.
     *
     * @param amount the amount.
     */
    public void decreaseOverallHappiness(final double amount)
    {
        this.overallHappiness = Math.max(this.overallHappiness - Math.abs(amount), MIN_OVERALL_HAPPINESS);
        this.markDirty();
    }

    @NotNull
    public List<EntityPlayer> getMessageEntityPlayers()
    {
        return ServerUtils.getPlayersFromUUID(this.world, this.getPermissions().getMessagePlayers());
    }

    /**
     * Checks if the achievements are valid.
     */
    public void checkAchievements()
    {
        // the colonies size
        final int size = this.citizenManager.getCitizens().size();

        //todo check those later again
        /*if (size >= ModAchievements.ACHIEVEMENT_SIZE_SETTLEMENT)
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
        }*/
    }

    /**
     * Triggers an achievement on this colony.
     * <p>
     * Will automatically sync to all players.
     *
     * @param achievement The achievement to trigger
     */
    public void triggerAchievement(@NotNull final MineColoniesAchievement achievement)
    {
        if (this.colonyAchievements.contains(achievement))
        {
            return;
        }

        //this.colonyAchievements.add(achievement);

        AchievementUtils.syncAchievements(this);
    }

    /**
     * Getter which checks if jobs should be manually allocated.
     *
     * @return true of false.
     */
    public boolean isManualHiring()
    {
        return manualHiring;
    }

    /**
     * Setter to set the job allocation manual or automatic.
     *
     * @param manualHiring true if manual, false if automatic.
     */
    public void setManualHiring(final boolean manualHiring)
    {
        this.manualHiring = manualHiring;
        markDirty();
    }

    /**
     * Getter which checks if houses should be manually allocated.
     *
     * @return true of false.
     */
    public boolean isManualHousing()
    {
        return manualHousing;
    }

    /**
     * Setter to set the house allocation manual or automatic.
     *
     * @param manualHousing true if manual, false if automatic.
     */
    public void setManualHousing(final boolean manualHousing)
    {
        this.manualHousing = manualHousing;
        markDirty();
    }

    /**
     * Send the message of a removed workOrder to the client.
     *
     * @param orderId the workOrder to remove.
     */
    public void removeWorkOrder(final int orderId)
    {
        //  Inform Subscribers of removed workOrder
        for (final EntityPlayerMP player : subscribers)
        {
            MineColonies.getNetwork().sendTo(new ColonyViewRemoveWorkOrderMessage(this, orderId), player);
        }
    }



    /**
     * Performed when a building of this colony finished his upgrade state.
     *
     * @param building The upgraded building.
     * @param level    The new level.
     */
    public void onBuildingUpgradeComplete(@NotNull final AbstractBuilding building, final int level)
    {
        building.onUpgradeComplete(level);
    }

    @NotNull
    public List<Advancement> getAchievements()
    {
        return Collections.unmodifiableList(this.colonyAchievements);
    }

    /**
     * Adds a waypoint to the colony.
     *
     * @param point the waypoint to add.
     * @param block the block at the waypoint.
     */
    public void addWayPoint(final BlockPos point, final IBlockState block)
    {
        wayPoints.put(point, block);
        markDirty();
    }

    /**
     * Returns a list of all wayPoints of the colony.
     *
     * @param position start position.
     * @param target   end position.
     * @return list of wayPoints.
     */
    @NotNull
    public List<BlockPos> getWayPoints(@NotNull final BlockPos position, @NotNull final BlockPos target)
    {
        final List<BlockPos> tempWayPoints = new ArrayList<>();
        tempWayPoints.addAll(wayPoints.keySet());
        tempWayPoints.addAll(buildingManager.getBuildings().keySet());

        final double maxX = Math.max(position.getX(), target.getX());
        final double maxZ = Math.max(position.getZ(), target.getZ());

        final double minX = Math.min(position.getX(), target.getX());
        final double minZ = Math.min(position.getZ(), target.getZ());

        final Iterator<BlockPos> iterator = tempWayPoints.iterator();
        while (iterator.hasNext())
        {
            final BlockPos p = iterator.next();
            final int x = p.getX();
            final int z = p.getZ();
            if (x < minX || x > maxX || z < minZ || z > maxZ)
            {
                iterator.remove();
            }
        }

        return tempWayPoints;
    }


    /**
     * Getter for overall happiness.
     *
     * @return the overall happiness.
     */
    public double getOverallHappiness()
    {
        return this.overallHappiness;
    }

    /**
     * Get all the waypoints of the colony.
     *
     * @return copy of hashmap.
     */
    public Map<BlockPos, IBlockState> getWayPoints()
    {
        return new HashMap<>(wayPoints);
    }

    public void setWillRaidTonight(final Boolean willRaid)
    {
        this.willRaidTonight = willRaid;
    }

    /**
     * This sets whether or not a colony can be automatically deleted Via command, or an on-tick check.
     *
     * @param canBeDeleted whether the colony is able to be deleted automatically
     */
    public void setCanBeAutoDeleted(final Boolean canBeDeleted)
    {
        this.canColonyBeAutoDeleted = canBeDeleted;
    }

    /**
     * Gets a random spot inside the colony, in the named direction, where the chunk is loaded.
     * @param directionX the first direction parameter.
     * @param directionZ the second direction paramter.
     * @return the position.
     */
    public BlockPos getRandomOutsiderInDirection(final EnumFacing directionX, final EnumFacing directionZ)
    {
        final List<BlockPos> positions = wayPoints.keySet().stream().filter(pos -> isInDirection(directionX, directionZ, pos.subtract(center))).collect(Collectors.toList());
        positions.addAll(buildingManager.getBuildings().keySet().stream().filter(pos -> isInDirection(directionX, directionZ, pos.subtract(center))).collect(Collectors.toList()));

        BlockPos thePos = center;
        double distance = 0;
        AbstractBuilding theBuilding = null;
        for (final BlockPos pos : positions)
        {
            final double currentDistance = center.distanceSq(pos);
            if (currentDistance > distance && world.isAreaLoaded(pos, DEFAULT_SPAWN_RADIUS))
            {
                distance = currentDistance;
                thePos = pos;
                theBuilding = buildingManager.getBuilding(thePos);
            }
        }

        int minDistance = 0;
        if (theBuilding != null)
        {
            final Tuple<Tuple<Integer, Integer>, Tuple<Integer, Integer>> corners = theBuilding.getCorners();
            minDistance
              = Math.max(corners.getFirst().getFirst() - corners.getFirst().getSecond(), corners.getSecond().getFirst() - corners.getSecond().getSecond());
        }

        if (thePos.equals(center))
        {
            return center;
        }

        int radius = DEFAULT_SPAWN_RADIUS;
        while (world.isAreaLoaded(thePos, radius))
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

        Log.getLogger().info("Spawning at: " + x + " " + z);
        return new BlockPos(x, thePos.getY(), z);
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

    /**
     * Getter for the default style of the colony.
     * @return the style string.
     */
    public String getStyle()
    {
        return style;
    }

    /**
     * Setter for the default style of the colony.
     * @param style the default string.
     */
    public void setStyle(final String style)
    {
        this.style = style;
    }

    /**
     * Get the buildingmanager of the colony.
     * @return the buildingManager.
     */
    public IBuildingManager getBuildingManager()
    {
        return buildingManager;
    }

    /**
     * Get the citizenManager of the colony.
     * @return the citizenManager.
     */
    public ICitizenManager getCitizenManager()
    {
        return citizenManager;
    }

    @NotNull
    public Set<EntityPlayerMP> getSubscribers()
    {
        return new HashSet<>(subscribers);
    }
}
