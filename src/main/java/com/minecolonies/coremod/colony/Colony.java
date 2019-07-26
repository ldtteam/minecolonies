package com.minecolonies.coremod.colony;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.BlockPosUtil;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Suppression;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.managers.*;
import com.minecolonies.coremod.colony.managers.interfaces.*;
import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.colony.pvp.AttackingPlayer;
import com.minecolonies.coremod.colony.requestsystem.management.manager.StandardRequestManager;
import com.minecolonies.coremod.colony.workorders.WorkManager;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.mobs.util.MobEventsUtils;
import com.minecolonies.coremod.network.messages.ColonyViewRemoveWorkOrderMessage;
import com.minecolonies.coremod.permissions.ColonyPermissionEventHandler;
import com.minecolonies.coremod.util.ServerUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerEntityMP;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
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
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;

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
    private int dimensionId;

    /**
     * List of waypoints of the colony.
     */
    private final Map<BlockPos, BlockState> wayPoints = new HashMap<>();

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
     * Colony happiness manager.
     */
    private final IColonyHappinessManager colonyHappinessManager = new ColonyHappinessManager();

    /**
     * Statistic and achievement manager manager of the colony.
     */
    private final IStatisticAchievementManager statsManager = new StatisticAchievementManager(this);

    /**
     * Barbarian manager of the colony.
     */
    private final IRaiderManager raidManager = new RaidManager(this);

    /**
     * The colony package manager.
     */
    private final IColonyPackageManager packageManager = new ColonyPackageManager(this);

    /**
     * The progress manager of the colony.
     */
    private final IProgressManager progressManager = new ProgressManager(this);

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
    private ColonyPermissionEventHandler eventHandler;

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
     * Whether citizens can move in or not.
     */
    private boolean moveIn = true;

    /**
     * The name of the colony.
     */
    private String name = "ERROR(Wasn't placed by player)";

    /**
     * The center of the colony.
     */
    private BlockPos center;

    /**
     * The amount of nights since the last raid.
     */
    private int nightsSinceLastRaid = 0;

    /**
     * The colony permission object.
     */
    @NotNull
    private Permissions permissions;

    /**
     * The request manager assigned to the colony.
     */
    private IRequestManager requestManager;

    /**
     * The NBTTag compound of the colony itself.
     */
    private CompoundNBT colonyTag;

    /**
     * List of players visiting the colony.
     */
    private final List<PlayerEntity> visitingPlayers = new ArrayList<>();

    /**
     * List of players attacking the colony.
     */
    private final List<AttackingPlayer> attackingPlayers = new ArrayList<>();

    /**
     * Datas about the happiness of a colony
     */
    private final HappinessData happinessData = new HappinessData();

    /**
     * Mournign parameters.
     */
    private boolean needToMourn = false;
    private boolean mourning    = false;

    /**
     * If the colony is dirty.
     */
    private boolean isActive = true;

    /**
     * The colony team color.
     */
    private TextFormatting colonyTeamColor = TextFormatting.WHITE;

    /**
     * The cost of citizens bought
     */
    private int boughtCitizenCost = 0;

    /**
     * The last time the mercenaries were used.
     */
    private long mercenaryLastUse = 0;

    /**
     * Constructor for a newly created Colony.
     *
     * @param id The id of the colony to create.
     * @param w  The world the colony exists in.
     * @param c  The center of the colony (location of Town Hall).
     */
    @SuppressWarnings("squid:S2637")
    Colony(final int id, @Nullable final World w, final BlockPos c)
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
    protected Colony(final int id, @Nullable final World world)
    {
        this.id = id;
        if (world != null)
        {
            this.dimensionId = world.provider.getDimension();
            this.world = world;
            checkOrCreateTeam();
        }
        this.permissions = new Permissions(this);

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
     * Check or create the team.
     */
    private void checkOrCreateTeam()
    {
        if (this.world.getScoreboard().getTeam(TEAM_COLONY_NAME + id) == null)
        {
            this.world.getScoreboard().createTeam(TEAM_COLONY_NAME + id);
            this.world.getScoreboard().getTeam(TEAM_COLONY_NAME + id).setAllowFriendlyFire(false);
        }
    }

    /**
     * Set up the colony color for team handling for pvp.
     *
     * @param colonyColor the colony color.
     */
    public void setColonyColor(final TextFormatting colonyColor)
    {
        if (this.world != null)
        {
            checkOrCreateTeam();
            this.colonyTeamColor = colonyColor;
            this.world.getScoreboard().getTeam(TEAM_COLONY_NAME + this.id).setColor(colonyColor);
            this.world.getScoreboard().getTeam(TEAM_COLONY_NAME + this.id).setPrefix(colonyColor.toString());
            this.markDirty();
        }
    }

    /**
     * Load a saved colony.
     *
     * @param compound The NBT compound containing the colony's data.
     * @return loaded colony.
     */
    @Nullable
    public static Colony loadColony(@NotNull final CompoundNBT compound, @Nullable final World world)
    {
        try
        {
            final int id = compound.getInt(TAG_ID);
            @NotNull final Colony c = new Colony(id, world);
            c.name = compound.getString(TAG_NAME);
            c.center = BlockPosUtil.readFromNBT(compound, TAG_CENTER);
            c.setRequestManager();
            c.readFromNBT(compound);

            if (c.getProgressManager().isPrintingProgress() && (c.getBuildingManager().getBuildings().size() > BUILDING_LIMIT_FOR_HELP
                                                                  || c.getCitizenManager().getCitizens().size() > CITIZEN_LIMIT_FOR_HELP))
            {
                c.getProgressManager().togglePrintProgress();
            }
            return c;
        }
        catch (final Exception e)
        {
            Log.getLogger().warn("Something went wrong loading a colony, please report this to the administrators", e);
        }
        return null;
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
    public void readFromNBT(@NotNull final CompoundNBT compound)
    {
        manualHiring = compound.getBoolean(TAG_MANUAL_HIRING);
        dimensionId = compound.getInt(TAG_DIMENSION);

        if (compound.keySet().contains(TAG_NEED_TO_MOURN))
        {
            needToMourn = compound.getBoolean(TAG_NEED_TO_MOURN);
            mourning = compound.getBoolean(TAG_MOURNING);
        }
        else
        {
            needToMourn = false;
            mourning = false;
        }

        boughtCitizenCost = compound.getInt(TAG_BOUGHT_CITIZENS);
        mercenaryLastUse = compound.getLong(TAG_MERCENARY_TIME);

        // Permissions
        permissions.loadPermissions(compound);

        if (compound.keySet().contains(TAG_CITIZEN_MANAGER))
        {
            citizenManager.readFromNBT(compound.getCompound(TAG_CITIZEN_MANAGER));
        }
        else
        {
            //Compatability with old version!
            citizenManager.readFromNBT(compound);
        }

        if (compound.keySet().contains(TAG_BUILDING_MANAGER))
        {
            buildingManager.readFromNBT(compound.getCompound(TAG_BUILDING_MANAGER));
        }
        else
        {
            //Compatability with old version!
            buildingManager.readFromNBT(compound);
        }

        if (compound.keySet().contains(TAG_STATS_MANAGER))
        {
            statsManager.readFromNBT(compound.getCompound(TAG_STATS_MANAGER));
        }
        else
        {
            //Compatability with old version!
            statsManager.readFromNBT(compound);
        }

        if (compound.keySet().contains(TAG_PROGRESS_MANAGER))
        {
            progressManager.readFromNBT(compound);
        }

        if (compound.keySet().contains(TAG_HAPPINESS_MODIFIER))
        {
            colonyHappinessManager.setLockedHappinessModifier(Optional.of(compound.getDouble(TAG_HAPPINESS_MODIFIER)));
        }
        else
        {
            colonyHappinessManager.setLockedHappinessModifier(Optional.empty());
        }

        raidManager.readFromNBT(compound);

        //  Workload
        workManager.readFromNBT(compound.getCompound(TAG_WORK));

        // Waypoints
        final ListNBT wayPointTagList = compound.getList(TAG_WAYPOINT, NBT.TAG_COMPOUND);
        for (int i = 0; i < wayPointTagList.size(); ++i)
        {
            final CompoundNBT blockAtPos = wayPointTagList.getCompound(i);
            final BlockPos pos = BlockPosUtil.readFromNBT(blockAtPos, TAG_WAYPOINT);
            final BlockState state = NBTUtil.readBlockState(blockAtPos);
            wayPoints.put(pos, state);
        }

        // Free blocks
        final ListNBT freeBlockTagList = compound.getList(TAG_FREE_BLOCKS, NBT.TAG_STRING);
        for (int i = 0; i < freeBlockTagList.size(); ++i)
        {
            freeBlocks.add(Block.getBlockFromName(freeBlockTagList.getStringTagAt(i)));
        }

        // Free positions
        final ListNBT freePositionTagList = compound.getList(TAG_FREE_POSITIONS, NBT.TAG_COMPOUND);
        for (int i = 0; i < freePositionTagList.size(); ++i)
        {
            final CompoundNBT blockTag = freePositionTagList.getCompound(i);
            final BlockPos block = BlockPosUtil.readFromNBT(blockTag, TAG_FREE_POSITIONS);
            freePositions.add(block);
        }

        happinessData.readFromNBT(compound);
        packageManager.setLastContactInHours(compound.getInt(TAG_ABANDONED));
        manualHousing = compound.getBoolean(TAG_MANUAL_HOUSING);

        if (compound.keySet().contains(TAG_MOVE_IN))
        {
            moveIn = compound.getBoolean(TAG_MOVE_IN);
        }

        if (compound.keySet().contains(TAG_STYLE))
        {
            this.style = compound.getString(TAG_STYLE);
        }

        if (compound.keySet().contains(TAG_RAIDABLE))
        {
            this.raidManager.setCanHaveRaiderEvents(compound.getBoolean(TAG_RAIDABLE));
        }
        else
        {
            this.raidManager.setCanHaveRaiderEvents(true);
        }

        if (compound.keySet().contains(TAG_AUTO_DELETE))
        {
            this.canColonyBeAutoDeleted = compound.getBoolean(TAG_AUTO_DELETE);
        }
        else
        {
            this.canColonyBeAutoDeleted = true;
        }

        if (compound.keySet().contains(TAG_TEAM_COLOR))
        {
            this.setColonyColor(TextFormatting.values()[compound.getInt(TAG_TEAM_COLOR)]);
        }

        this.requestManager.reset();
        if (compound.keySet().contains(TAG_REQUESTMANAGER))
        {
            this.requestManager.deserializeNBT(compound.getCompound(TAG_REQUESTMANAGER));
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
    public CompoundNBT write(@NotNull final CompoundNBT compound)
    {
        //  Core attributes
        compound.putInt(TAG_ID, id);
        compound.putInt(TAG_DIMENSION, dimensionId);

        //  Basic data
        compound.putString(TAG_NAME, name);
        BlockPosUtil.write(compound, TAG_CENTER, center);

        compound.putBoolean(TAG_MANUAL_HIRING, manualHiring);
        compound.putBoolean(TAG_NEED_TO_MOURN, needToMourn);
        compound.putBoolean(TAG_MOURNING, mourning);

        // Bought citizen count
        compound.putInt(TAG_BOUGHT_CITIZENS, boughtCitizenCost);

        compound.putLong(TAG_MERCENARY_TIME, mercenaryLastUse);

        // Permissions
        permissions.savePermissions(compound);

        final CompoundNBT buildingCompound = new CompoundNBT();
        buildingManager.write(buildingCompound);
        compound.put(TAG_BUILDING_MANAGER, buildingCompound);

        final CompoundNBT citizenCompound = new CompoundNBT();
        citizenManager.write(citizenCompound);
        compound.put(TAG_CITIZEN_MANAGER, citizenCompound);

        colonyHappinessManager.getLockedHappinessModifier().ifPresent(d -> compound.setDouble(TAG_HAPPINESS_MODIFIER, d));

        final CompoundNBT statsCompound = new CompoundNBT();
        statsManager.write(statsCompound);
        compound.put(TAG_STATS_MANAGER, statsCompound);

        //  Workload
        @NotNull final CompoundNBT workManagerCompound = new CompoundNBT();
        workManager.write(workManagerCompound);
        compound.put(TAG_WORK, workManagerCompound);

        progressManager.write(compound);
        raidManager.write(compound);

        // Waypoints
        @NotNull final ListNBT wayPointTagList = new ListNBT();
        for (@NotNull final Map.Entry<BlockPos, BlockState> entry : wayPoints.entrySet())
        {
            @NotNull final CompoundNBT wayPointCompound = new CompoundNBT();
            BlockPosUtil.write(wayPointCompound, TAG_WAYPOINT, entry.getKey());
            NBTUtil.writeBlockState(wayPointCompound, entry.getValue());

            wayPointTagList.add(wayPointCompound);
        }
        compound.put(TAG_WAYPOINT, wayPointTagList);

        // Free blocks
        @NotNull final ListNBT freeBlocksTagList = new ListNBT();
        for (@NotNull final Block block : freeBlocks)
        {
            freeBlocksTagList.add(new NBTTagString(block.getRegistryName().toString()));
        }
        compound.put(TAG_FREE_BLOCKS, freeBlocksTagList);

        // Free positions
        @NotNull final ListNBT freePositionsTagList = new ListNBT();
        for (@NotNull final BlockPos pos : freePositions)
        {
            @NotNull final CompoundNBT wayPointCompound = new CompoundNBT();
            BlockPosUtil.write(wayPointCompound, TAG_FREE_POSITIONS, pos);
            freePositionsTagList.add(wayPointCompound);
        }
        compound.put(TAG_FREE_POSITIONS, freePositionsTagList);

        happinessData.write(compound);
        compound.putInt(TAG_ABANDONED, packageManager.getLastContactInHours());
        compound.putBoolean(TAG_MANUAL_HOUSING, manualHousing);
        compound.putBoolean(TAG_MOVE_IN, moveIn);
        compound.put(TAG_REQUESTMANAGER, getRequestManager().serializeNBT());
        compound.putString(TAG_STYLE, style);
        compound.putBoolean(TAG_RAIDABLE, raidManager.canHaveRaiderEvents());
        compound.putBoolean(TAG_AUTO_DELETE, canColonyBeAutoDeleted);
        compound.putInt(TAG_TEAM_COLOR, colonyTeamColor.ordinal());
        this.colonyTag = compound;

        isActive = false;
        return compound;
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

    @Override
    public boolean isRemote()
    {
        return false;
    }

    /**
     * When the Colony's world is loaded, associate with it.
     *
     * @param w World object.
     */
    public void onWorldLoad(@NotNull final World w)
    {
        this.world = w;
        // Register a new event handler
        eventHandler = new ColonyPermissionEventHandler(this);
        MinecraftForge.EVENT_BUS.register(eventHandler);
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
        packageManager.updateSubscribers();

        if (packageManager.getSubscribers().isEmpty())
        {
            return;
        }
        isActive = true;

        buildingManager.tick(event);

        getRequestManager().update();

        final List<PlayerEntity> visitors = new ArrayList<>(visitingPlayers);

        //Clean up visiting player.
        for (final PlayerEntity player : visitors)
        {
            if (!packageManager.getSubscribers().contains(player))
            {
                visitingPlayers.remove(player);
                attackingPlayers.remove(new AttackingPlayer(player));
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
     * Get all the data indices about happiness
     *
     * @return An instance of {@link HappinessData} containing all the datas
     */
    public HappinessData getHappinessData()
    {
        return happinessData;
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

        // Clean up or spawn citizens.
        if (packageManager.getSubscribers().isEmpty())
        {
            return;
        }
        isActive = true;

        //  Cleanup Buildings whose Blocks have gone AWOL
        buildingManager.cleanUpBuildings(event);
        citizenManager.onWorldTick(event);

        if (shallUpdate(world, TICKS_SECOND)
              && event.world.getDifficulty() != EnumDifficulty.PEACEFUL
              && Configurations.gameplay.doBarbariansSpawn
              && raidManager.canHaveRaiderEvents()
              && !world.getMinecraftServer().getPlayerList().getPlayers()
                    .stream().filter(permissions::isSubscriber).collect(Collectors.toList()).isEmpty()
              && MobEventsUtils.isItTimeToRaid(event.world, this))
        {
            MobEventsUtils.raiderEvent(event.world, this);
        }

        if (shallUpdate(world, TICKS_SECOND))
        {
            for (final AttackingPlayer player : attackingPlayers)
            {
                if (!player.getGuards().isEmpty())
                {
                    player.refreshList(this);
                    if (player.getGuards().isEmpty())
                    {
                        LanguageHandler.sendPlayersMessage(getMessagePlayerEntitys(), "You successfully defended your colony against, " + player.getPlayer().getName());
                    }
                }
            }
        }

        raidManager.onWorldTick(world);
        buildingManager.onWorldTick(event);

        if (isDay && !world.isDaytime())
        {
            isDay = false;
            nightsSinceLastRaid++;
            if (!packageManager.getSubscribers().isEmpty())
            {
                citizenManager.checkCitizensForHappiness();
            }
            happinessData.processDeathModifiers();
            if (mourning)
            {
                mourning = false;
                citizenManager.updateCitizenMourn(false);
            }
        }
        else if (!isDay && world.isDaytime())
        {
            isDay = true;
            if (needToMourn)
            {
                needToMourn = false;
                mourning = true;
                citizenManager.updateCitizenMourn(true);
            }
        }

        updateWayPoints();
        workManager.onWorldTick(event);
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
        if (world != null && world.rand.nextInt(CHECK_WAYPOINT_EVERY) <= 1 && !wayPoints.isEmpty())
        {
            final Object[] entries = wayPoints.entrySet().toArray();
            final int stopAt = world.rand.nextInt(entries.length);
            final Object obj = entries[stopAt];

            if (obj instanceof Map.Entry && ((Map.Entry) obj).getKey() instanceof BlockPos && ((Map.Entry) obj).getValue() instanceof BlockState)
            {
                @NotNull final BlockPos key = (BlockPos) ((Map.Entry) obj).getKey();
                if (world.isBlockLoaded(key))
                {
                    @NotNull final BlockState value = (BlockState) ((Map.Entry) obj).getValue();
                    if (world.getBlockState(key).getBlock() != (value.getBlock()))
                    {
                        wayPoints.remove(key);
                        markDirty();
                    }
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
        if (w.provider.getDimension() != this.dimensionId)
        {
            return false;
        }

        final Chunk chunk = w.getChunk(pos);
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
        return raidManager.willRaidTonight();
    }

    @Override
    public boolean isCanHaveBarbEvents()
    {
        return raidManager.canHaveRaiderEvents();
    }

    @Override
    public boolean isHasRaidBeenCalculated()
    {
        return raidManager.hasRaidBeenCalculated();
    }

    /**
     * Marks the instance dirty.
     */
    public void markDirty()
    {
        packageManager.setDirty();
        isActive = true;
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

    @NotNull
    public List<PlayerEntity> getMessagePlayerEntitys()
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
        progressManager.progressEmploymentModeChange();
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
     * Getter which checks if houses should be manually allocated.
     *
     * @return true of false.
     */
    public boolean canMoveIn()
    {
        return moveIn;
    }

    /**
     * Setter to set the citizen moving in.
     *
     * @param moveIn true if can move in, false if can't move in.
     */
    public void setMoveIn(final boolean moveIn)
    {
        this.moveIn = moveIn;
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
        for (final PlayerEntityMP player : packageManager.getSubscribers())
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
    public void onBuildingUpgradeComplete(@Nullable final AbstractBuilding building, final int level)
    {
        if (building != null)
        {
            building.onUpgradeComplete(level);
            this.markDirty();
        }
    }

    /**
     * Adds a waypoint to the colony.
     *
     * @param point the waypoint to add.
     * @param block the block at the waypoint.
     */
    public void addWayPoint(final BlockPos point, final BlockState block)
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
        if (citizenManager.getCitizens().size() <= 0)
        {
            return (HappinessData.MAX_HAPPINESS + HappinessData.MIN_HAPPINESS) / 2.0;
        }

        double happinesSum = 0;
        for (final CitizenData citizen : citizenManager.getCitizens())
        {
            happinesSum += citizen.getCitizenHappinessHandler().getHappiness();
        }
        final double happinessAverage = happinesSum / citizenManager.getCitizens().size();
        return Math.min(happinessAverage + happinessData.getTotalHappinessModifier(), HappinessData.MAX_HAPPINESS);
    }

    /**
     * Get all the waypoints of the colony.
     *
     * @return copy of hashmap.
     */
    public Map<BlockPos, BlockState> getWayPoints()
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
     *
     * @return the style string.
     */
    public String getStyle()
    {
        return style;
    }

    /**
     * Setter for the default style of the colony.
     *
     * @param style the default string.
     */
    public void setStyle(final String style)
    {
        this.style = style;
    }

    /**
     * Get the buildingmanager of the colony.
     *
     * @return the buildingManager.
     */
    public IBuildingManager getBuildingManager()
    {
        return buildingManager;
    }

    /**
     * Get the citizenManager of the colony.
     *
     * @return the citizenManager.
     */
    public ICitizenManager getCitizenManager()
    {
        return citizenManager;
    }

    /**
     * Get the colony happiness manager.
     *
     * @return the colony happiness manager.
     */
    public IColonyHappinessManager getColonyHappinessManager()
    {
        return colonyHappinessManager;
    }

    /**
     * Get the statsManager of the colony.
     *
     * @return the statsManager.
     */
    public IStatisticAchievementManager getStatsManager()
    {
        return statsManager;
    }

    /**
     * Get the barbManager of the colony.
     *
     * @return the barbManager.
     */
    public IRaiderManager getRaiderManager()
    {
        return raidManager;
    }

    /**
     * Get the packagemanager of the colony.
     *
     * @return the manager.
     */
    public IColonyPackageManager getPackageManager()
    {
        return packageManager;
    }

    /**
     * Get the progress manager of the colony.
     *
     * @return the manager.
     */
    public IProgressManager getProgressManager()
    {
        return progressManager;
    }

    /**
     * Get all visiting players.
     *
     * @return the list.
     */
    public ImmutableList<PlayerEntity> getVisitingPlayers()
    {
        return ImmutableList.copyOf(visitingPlayers);
    }

    @Override
    public void addVisitingPlayer(final PlayerEntity player)
    {
        final Rank rank = getPermissions().getRank(player);
        if (rank != Rank.OWNER && rank != Rank.OFFICER && !visitingPlayers.contains(player) && Configurations.gameplay.sendEnteringLeavingMessages)
        {
            visitingPlayers.add(player);
            LanguageHandler.sendPlayerMessage(player, ENTERING_COLONY_MESSAGE, this.getPermissions().getOwnerName());
            LanguageHandler.sendPlayersMessage(getMessagePlayerEntitys(), ENTERING_COLONY_MESSAGE_NOTIFY, player.getName(), this.getName());
        }
    }

    @Override
    public void removeVisitingPlayer(final PlayerEntity player)
    {
        if (!getMessagePlayerEntitys().contains(player) && Configurations.gameplay.sendEnteringLeavingMessages)
        {
            visitingPlayers.remove(player);
            LanguageHandler.sendPlayerMessage(player, LEAVING_COLONY_MESSAGE, this.getPermissions().getOwnerName());
            LanguageHandler.sendPlayersMessage(getMessagePlayerEntitys(), LEAVING_COLONY_MESSAGE_NOTIFY, player.getName(), this.getName());
        }
    }

    /**
     * Get the NBT tag of the colony.
     *
     * @return the tag of it.
     */
    public CompoundNBT getColonyTag()
    {
        try
        {
            if (this.colonyTag == null || this.isActive)
            {
                this.write(new CompoundNBT());
            }
        }
        catch (final Exception e)
        {
            Log.getLogger().warn("Something went wrong persisting colony: " + id, e);
        }
        return this.colonyTag;
    }

    /**
     * Getter for the nights since the last raid.
     *
     * @return the number of nights.
     */
    public int getNightsSinceLastRaid()
    {
        return nightsSinceLastRaid;
    }

    /**
     * Setter for the nights since the last raid.
     *
     * @param nights the number of nights.
     */
    public void setNightsSinceLastRaid(final int nights)
    {
        this.nightsSinceLastRaid = nights;
    }

    /**
     * call to figure out if the colony needs to mourn.
     *
     * @return a boolean indicating the colony needs to mourn
     */
    public boolean isNeedToMourn()
    {
        return needToMourn;
    }

    /**
     * Call to set if the colony needs to mourn or not.
     *
     * @param needToMourn indicate if the colony needs to mourn
     * @param name        Name of citizen that died
     */
    public void setNeedToMourn(final boolean needToMourn, final String name)
    {
        this.needToMourn = needToMourn;
        if (needToMourn)
        {
            LanguageHandler.sendPlayersMessage(getMessagePlayerEntitys(), COM_MINECOLONIES_COREMOD_MOURN, name);
        }
    }

    /**
     * Call to check if the colony is mourning.
     *
     * @return indicates if the colony is mourning
     */
    public boolean isMourning()
    {
        return mourning;
    }

    /**
     * Add a guard to the list of attacking guards.
     *
     * @param entityCitizen the citizen to add.
     */
    public void addGuardToAttackers(final EntityCitizen entityCitizen, final PlayerEntity player)
    {
        if (player == null)
        {
            return;
        }

        for (final AttackingPlayer attackingPlayer : attackingPlayers)
        {
            if (attackingPlayer.getPlayer().equals(player))
            {
                if (attackingPlayer.addGuard(entityCitizen))
                {
                    LanguageHandler.sendPlayersMessage(getMessagePlayerEntitys(),
                      "Beware, " + attackingPlayer.getPlayer().getName() + " has now: " + attackingPlayer.getGuards().size() + " guards!");
                }
                return;
            }
        }

        for (final PlayerEntity visitingPlayer : visitingPlayers)
        {
            if (visitingPlayer.equals(player))
            {
                final AttackingPlayer attackingPlayer = new AttackingPlayer(visitingPlayer);
                attackingPlayer.addGuard(entityCitizen);
                attackingPlayers.add(attackingPlayer);
                LanguageHandler.sendPlayersMessage(getMessagePlayerEntitys(), "Beware, " + visitingPlayer.getName() + " is attacking you and he brought guards.");
            }
        }
    }

    /**
     * Is player part of a wave trying to invade the colony?
     *
     * @param player the player to check..
     * @return true if so.
     */
    public boolean isValidAttackingPlayer(final PlayerEntity player)
    {
        if (packageManager.getLastContactInHours() > 1)
        {
            return false;
        }

        for (final AttackingPlayer attackingPlayer : attackingPlayers)
        {
            if (attackingPlayer.getPlayer().equals(player))
            {
                return attackingPlayer.isValidAttack(this);
            }
        }
        return false;
    }

    /**
     * Check if attack of guard is valid.
     *
     * @param entity the guard entity.
     * @return true if so.
     */
    public boolean isValidAttackingGuard(final EntityCitizen entity)
    {
        if (packageManager.getLastContactInHours() > 1)
        {
            return false;
        }

        return AttackingPlayer.isValidAttack(entity, this);
    }

    /**
     * Check if the colony is currently under attack by another player.
     *
     * @return true if so.
     */
    public boolean isColonyUnderAttack()
    {
        return !attackingPlayers.isEmpty();
    }

    /**
     * Getter for the colony team color.
     *
     * @return the TextFormatting enum color.
     */
    public TextFormatting getTeamColonyColor()
    {
        return colonyTeamColor;
    }

    /**
     * Set the colony to be active.
     *
     * @param isActive if active.
     */
    public void setActive(final boolean isActive)
    {
        this.isActive = isActive;
    }

    /**
     * Get the amount of citizens bought
     *
     * @return amount
     */
    public int getBoughtCitizenCost()
    {
        return boughtCitizenCost;
    }

    /**
     * Increases the amount of citizens that have been bought
     */
    public void increaseBoughtCitizenCost()
    {
        boughtCitizenCost = Math.min(1 + (int) Math.ceil(boughtCitizenCost * 1.5), STACKSIZE);
        markDirty();
    }

    /**
     * Save the time when mercenaries are used, to set a cooldown.
     */
    @Override
    public void usedMercenaries()
    {
        mercenaryLastUse = world.getTotalWorldTime();
        markDirty();
    }

    /**
     * Get the last time mercenaries were used.
     */
    @Override
    public long getMercenaryUseTime()
    {
        return mercenaryLastUse;
    }

}
