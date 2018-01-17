package com.minecolonies.coremod.colony;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.Suppression;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.*;
import com.minecolonies.coremod.colony.managers.*;
import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.colony.requestsystem.management.manager.StandardRequestManager;
import com.minecolonies.coremod.entity.ai.mobs.util.MobEventsUtils;
import com.minecolonies.coremod.network.messages.*;
import com.minecolonies.coremod.permissions.ColonyPermissionEventHandler;
import com.minecolonies.coremod.util.ServerUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.ColonyConstants.*;
import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;
import static com.minecolonies.coremod.colony.ColonyManager.FILENAME_COLONY;
import static com.minecolonies.coremod.colony.ColonyManager.FILENAME_MINECOLONIES_PATH;

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
     * Work Manager of the colony (Request System).
     */
    private final WorkManager workManager = new WorkManager(this);

    /**
     * Building manager of the colony.
     */
    private final IBuildingManager buildingManager = new BuildingManager(this);

    /**
     * Citizen manager of the colony.
     */
    private final ICitizenManager citizenManager = new CitizenManager(this);

    /**
     * Statistic and achievement manager manager of the colony.
     */
    private final IStatisticAchievementManager statsManager = new StatisticAchievementManager(this);

    /**
     * Barbarian manager of the colony.
     */
    private final IBarbarianManager barbarianManager = new BarbarianManager(this);

    /**
     * The colony package manager.
     */
    private final IColonyPackageManager packageManager = new ColonyPackageManager(this);

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
     * Whether or not this colony may be auto-deleted.
     */
    private boolean canColonyBeAutoDeleted = true;

    /**
     * Variable to determine if its currently day or night.
     */
    private boolean isDay = true;

    /**
     * The world the colony currently runs on.
     */
    @Nullable
    private World world = null;

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
     * The request manager assigned to the colony.
     */
    private IRequestManager requestManager;

    /**
     * The NBTTag compound of the colony itself.
     */
    private NBTTagCompound colonyTag;

    /**
     * Field to check if the colony is dirty.
     */
    private boolean isDirty = false;

    /**
     * List of players visiting the colony.
     */
    private final List<EntityPlayer> visitingPlayers = new ArrayList<>();

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
        c.name = compound.getString(TAG_NAME);
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
            citizenManager.readFromNBT(compound.getCompoundTag(TAG_CITIZEN_MANAGER));
        }
        else
        {
            //Compatability with old version!
            citizenManager.readFromNBT(compound);
        }

        if(compound.hasKey(TAG_BUILDING_MANAGER))
        {
            buildingManager.readFromNBT(compound.getCompoundTag(TAG_BUILDING_MANAGER));
        }
        else
        {
            //Compatability with old version!
            buildingManager.readFromNBT(compound);
        }

        if(compound.hasKey(TAG_STATS_MANAGER))
        {
            statsManager.readFromNBT(compound.getCompoundTag(TAG_STATS_MANAGER));
        }
        else
        {
            //Compatability with old version!
            statsManager.readFromNBT(compound);
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

        this.overallHappiness = compound.getDouble(TAG_HAPPINESS);
        packageManager.setLastContactInHours(compound.getInteger(TAG_ABANDONED));
        manualHousing = compound.getBoolean(TAG_MANUAL_HOUSING);

        if (compound.hasKey(TAG_REQUESTMANAGER))
        {
            this.requestManager.deserializeNBT(compound.getCompoundTag(TAG_REQUESTMANAGER));
        }

        if(compound.hasKey(TAG_STYLE))
        {
            this.style = compound.getString(TAG_STYLE);
        }

        if(compound.hasKey(TAG_RAIDABLE))
        {
            this.barbarianManager.setCanHaveBarbEvents(compound.getBoolean(TAG_RAIDABLE));
        }
        else
        {
            this.barbarianManager.setCanHaveBarbEvents(true);
        }

        if(compound.hasKey(TAG_AUTO_DELETE))
        {
            this.canColonyBeAutoDeleted = compound.getBoolean(TAG_AUTO_DELETE);
        }
        else
        {
            this.canColonyBeAutoDeleted = true;
        }

        this.colonyTag = compound;
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
        compound.setTag(TAG_BUILDING_MANAGER, buildingCompound);

        final NBTTagCompound citizenCompound = new NBTTagCompound();
        citizenManager.writeToNBT(citizenCompound);
        compound.setTag(TAG_CITIZEN_MANAGER, citizenCompound);

        final NBTTagCompound statsCompound = new NBTTagCompound();
        statsManager.writeToNBT(statsCompound);
        compound.setTag(TAG_STATS_MANAGER, statsCompound);

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
        compound.setInteger(TAG_ABANDONED, packageManager.getLastContactInHours());
        compound.setBoolean(TAG_MANUAL_HOUSING, manualHousing);
        compound.setTag(TAG_REQUESTMANAGER, getRequestManager().serializeNBT());
        compound.setString(TAG_STYLE, style);
        compound.setBoolean(TAG_RAIDABLE, barbarianManager.canHaveBarbEvents());
        compound.setBoolean(TAG_AUTO_DELETE, canColonyBeAutoDeleted);

        this.colonyTag = compound;
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
            packageManager.updateSubscribers();
        }

        final List<EntityPlayer> visitors = new ArrayList<>(visitingPlayers);
        //Clean up visiting player.
        for(final EntityPlayer player: visitors)
        {
            if(!packageManager.getSubscribers().contains(player))
            {
                visitingPlayers.remove(player);
            }
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

            //  Cleanup Buildings whose Blocks have gone AWOL
            buildingManager.cleanUpBuildings(event);

            // Clean up or spawn citizens.
            citizenManager.onWorldTick(event);

            if (shallUpdate(world, TICKS_SECOND)
                  && event.world.getDifficulty() != EnumDifficulty.PEACEFUL
                  && Configurations.gameplay.doBarbariansSpawn
                  && barbarianManager.canHaveBarbEvents()
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
            citizenManager.checkCitizensForHappiness();
        }
        else if (!isDay && world.isDaytime())
        {
            isDay = true;
        }

        updateWayPoints();
        workManager.onWorldTick(event);

        if(this.isDirty && shallUpdate(world, CLEANUP_TICK_INCREMENT))
        {
            this.isDirty = false;
            @NotNull final File saveDir = new File(DimensionManager.getWorld(0).getSaveHandler().getWorldDirectory(), FILENAME_MINECOLONIES_PATH);
            ColonyManager.saveNBTToPath(new File(saveDir, String.format(FILENAME_COLONY, this.getID())), this.getColonyTag());
        }
    }

    /**
     * Calculate randomly if the colony should update the citizens.
     * By mean they update it at CLEANUP_TICK_INCREMENT.
     *
     * @param world the world.
     * @return a boolean by random.
     */
    public static boolean shallUpdate(final World world, final int averageTicks)
    {
        return world.getWorldTime() % (world.rand.nextInt(averageTicks * 2) + 1) == 0;
    }

    public boolean areAllColonyChunksLoaded(@NotNull final TickEvent.WorldTickEvent event)
    {
        final int distanceFromCenter = Configurations.gameplay.workingRangeTownHallChunks * BLOCKS_PER_CHUNK + 48 /* 3 chunks */ + BLOCKS_PER_CHUNK - 1 /* round up a chunk */;
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
        final Chunk chunk = w.getChunkFromBlockCoords(pos);
        final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null);
        return cap.getOwningColony() == this.getID();
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
        return packageManager.getLastContactInHours();
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
        return barbarianManager.willRaidTonight();
    }

    @Override
    public boolean isCanHaveBarbEvents()
    {
        return barbarianManager.canHaveBarbEvents();
    }

    @Override
    public boolean isHasRaidBeenCalculated()
    {
        return barbarianManager.hasRaidBeenCalculated();
    }

    /**
     * Marks the instance dirty.
     */
    public void markDirty()
    {
        packageManager.setDirty();
        colonyTag = null;
        this.isDirty = true;
    }

    @Override
    public boolean canBeAutoDeleted()
    {
        return canColonyBeAutoDeleted;
    }

    @Nullable
    @Override
    public IRequester getRequesterBuildingForPosition(@NotNull final BlockPos pos)
    {
        return buildingManager.getBuilding(pos);
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
    public void removeWorkOrderInView(final int orderId)
    {
        //  Inform Subscribers of removed workOrder
        for (final EntityPlayerMP player : packageManager.getSubscribers())
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
        this.markDirty();
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
        this.markDirty();
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

    /**
     * This sets whether or not a colony can be automatically deleted Via command, or an on-tick check.
     *
     * @param canBeDeleted whether the colony is able to be deleted automatically
     */
    public void setCanBeAutoDeleted(final Boolean canBeDeleted)
    {
        this.canColonyBeAutoDeleted = canBeDeleted;
        this.markDirty();
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

    /**
     * Get the statsManager of the colony.
     * @return the statsManager.
     */
    public IStatisticAchievementManager getStatsManager()
    {
        return statsManager;
    }

    /**
     * Get the barbManager of the colony.
     * @return the barbManager.
     */
    public IBarbarianManager getBarbManager()
    {
        return barbarianManager;
    }

    /**
     * Get the packagemanager of the colony.
     * @return the manager.
     */
    public IColonyPackageManager getPackageManager()
    {
        return packageManager;
    }

    /**
     * Get all visiting players.
     * @return the list.
     */
    public ImmutableList<EntityPlayer> getVisitingPlayers()
    {
        return ImmutableList.copyOf(visitingPlayers);
    }

    @Override
    public void addVisitingPlayer(final EntityPlayer player)
    {
        final Rank rank = getPermissions().getRank(player);
        if(rank != Rank.OWNER && rank != Rank.OFFICER && !visitingPlayers.contains(player))
        {
            visitingPlayers.add(player);
            LanguageHandler.sendPlayerMessage(player, ENTERING_COLONY_MESSAGE, this.getPermissions().getOwnerName());
            LanguageHandler.sendPlayersMessage(getMessageEntityPlayers(), ENTERING_COLONY_MESSAGE_NOTIFY, player.getName());
        }
    }

    @Override
    public void removeVisitingPlayer(final EntityPlayer player)
    {
        if(!getMessageEntityPlayers().contains(player))
        {
            visitingPlayers.remove(player);
            LanguageHandler.sendPlayerMessage(player, LEAVING_COLONY_MESSAGE, this.getPermissions().getOwnerName());
            LanguageHandler.sendPlayersMessage(getMessageEntityPlayers(), LEAVING_COLONY_MESSAGE_NOTIFY, player.getName());
        }
    }

    /**
     * Get the NBT tag of the colony.
     * @return the tag of it.
     */
    public NBTTagCompound getColonyTag()
    {
        if(this.colonyTag == null)
        {
            this.writeToNBT(new NBTTagCompound());
        }
        return this.colonyTag;
    }
}
