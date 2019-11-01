package com.minecolonies.coremod.colony;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.HappinessData;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.managers.interfaces.*;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.workorders.IWorkManager;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.mobs.util.MobEventsUtils;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Suppression;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.managers.*;
import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.colony.pvp.AttackingPlayer;
import com.minecolonies.coremod.colony.requestsystem.management.manager.StandardRequestManager;
import com.minecolonies.coremod.colony.workorders.WorkManager;
import com.minecolonies.coremod.network.messages.ColonyViewRemoveWorkOrderMessage;
import com.minecolonies.coremod.permissions.ColonyPermissionEventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateConstants.MAX_TICKRATE;
import static com.minecolonies.api.util.constant.ColonyConstants.*;
import static com.minecolonies.api.util.constant.Constants.DEFAULT_STYLE;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;
import static com.minecolonies.coremod.colony.ColonyState.*;

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
     * List of loaded chunks for the colony.
     */
    private Set<Long> loadedChunks = new HashSet<>();

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
    private NBTTagCompound colonyTag;

    /**
     * List of players visiting the colony.
     */
    private final List<EntityPlayer> visitingPlayers = new ArrayList<>();

    /**
     * List of players attacking the colony.
     */
    private final List<AttackingPlayer> attackingPlayers = new ArrayList<>();

    /**
     * Datas about the happiness of a colony
     */
    private final HappinessData happinessData = new HappinessData();

    /**
     * The colonies state machine
     */
    private final ITickRateStateMachine colonyStateMachine;

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
     * The amount of additional child time gathered when the colony is not loaded.
     */
    private int additionalChildTime = 0;

    /**
     * Boolean whether the colony has childs.
     */
    private boolean hasChilds = false;

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
            onWorldLoad(world);
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


        colonyStateMachine = new TickRateStateMachine(INACTIVE, e -> {});

        colonyStateMachine.addTransition(new TickingTransition(INACTIVE, () -> true, this::updateState, UPDATE_STATE_INTERVAL));
        colonyStateMachine.addTransition(new TickingTransition(UNLOADED, () -> true, this::updateState, UPDATE_STATE_INTERVAL));
        colonyStateMachine.addTransition(new TickingTransition(ACTIVE, () -> true, this::updateState, UPDATE_STATE_INTERVAL));

        colonyStateMachine.addTransition(new TickingTransition(ACTIVE, this::updateSubscribers, () -> ACTIVE, UPDATE_SUBSCRIBERS_INTERVAL));
        colonyStateMachine.addTransition(new TickingTransition(ACTIVE, this::tickRequests, () -> ACTIVE, UPDATE_RS_INTERVAL));
        colonyStateMachine.addTransition(new TickingTransition(ACTIVE, this::checkDayTime, () -> ACTIVE, UPDATE_DAYTIME_INTERVAL));
        colonyStateMachine.addTransition(new TickingTransition(ACTIVE, this::updateWayPoints, () -> ACTIVE, CHECK_WAYPOINT_EVERY));
        colonyStateMachine.addTransition(new TickingTransition(ACTIVE, this::worldTickSlow , () -> ACTIVE, MAX_TICKRATE));
        colonyStateMachine.addTransition(new TickingTransition(UNLOADED,this::worldTickUnloaded, () -> UNLOADED, MAX_TICKRATE));
    }


    /**
     * Updates the state the colony is in.
     */
    private ColonyState updateState()
    {
        if (world == null)
        {
            return INACTIVE;
        }
        packageManager.updateAwayTime();

        if (!packageManager.getCloseSubscribers().isEmpty() || (loadedChunks.size() > 40 && !packageManager.getImportantColonyPlayers().isEmpty()))
        {
            isActive = true;
            return ACTIVE;
        }

        if (!packageManager.getImportantColonyPlayers().isEmpty())
        {
            isActive = true;
            return UNLOADED;
        }

        return INACTIVE;
    }

    /**
     * Updates the existing subscribers
     */
    private boolean updateSubscribers()
    {
        packageManager.updateSubscribers();
        return false;
    }

    /**
     * Ticks the request manager.
     */
    private boolean tickRequests()
    {
        if (getRequestManager() != null)
        {
            getRequestManager().update();
        }
        return false;
    }

    /**
     * Called every 500 ticks, for slower updates.
     */
    private boolean worldTickSlow()
    {
        buildingManager.cleanUpBuildings(this);
        MobEventsUtils.tryToRaidColony(this);
        citizenManager.onColonyTick(this);
        updateAttackingPlayers();
        raidManager.onColonyTick(this);
        buildingManager.onColonyTick(this);
        workManager.onColonyTick(this);

        updateChildTime();
        return false;
    }

    /**
     * Called every 500 ticks, for slower updates. Only ticked when the colony is not loaded.
     */
    private boolean worldTickUnloaded()
    {
        updateChildTime();
        return false;
    }

    /**
     * Adds 500 additional ticks to the child growth.
     * @return
     */
    private boolean updateChildTime()
    {
        if (hasChilds)
        {
            additionalChildTime+= 500;
        }
        else
        {
            additionalChildTime = 0;
        }
        return false;
    }

    /**
     * Updates the day and night detection.
     *
     * @return
     */
    private boolean checkDayTime()
    {
        if (isDay && !world.isDaytime())
        {
            isDay = false;
            nightsSinceLastRaid++;
            if (!packageManager.getCloseSubscribers().isEmpty())
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
        return false;
    }

    /**
     * Updates the pvping playeres.
     */
    public boolean updateAttackingPlayers()
    {
        final List<EntityPlayer> visitors = new ArrayList<>(visitingPlayers);

        //Clean up visiting player.
        for (final EntityPlayer player : visitors)
        {
            if (!packageManager.getCloseSubscribers().contains(player))
            {
                visitingPlayers.remove(player);
                attackingPlayers.remove(new AttackingPlayer(player));
            }
        }

        for (final AttackingPlayer player : attackingPlayers)
        {
            if (!player.getGuards().isEmpty())
            {
                player.refreshList(this);
                if (player.getGuards().isEmpty())
                {
                    LanguageHandler.sendPlayersMessage(getImportantMessageEntityPlayers(), "You successfully defended your colony against, " + player.getPlayer().getName());
                }
            }
        }
        return false;
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
    public static Colony loadColony(@NotNull final NBTTagCompound compound, @Nullable final World world)
    {
        try
        {
            final int id = compound.getInteger(TAG_ID);
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
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        manualHiring = compound.getBoolean(TAG_MANUAL_HIRING);
        dimensionId = compound.getInteger(TAG_DIMENSION);

        if (compound.hasKey(TAG_NEED_TO_MOURN))
        {
            needToMourn = compound.getBoolean(TAG_NEED_TO_MOURN);
            mourning = compound.getBoolean(TAG_MOURNING);
        }
        else
        {
            needToMourn = false;
            mourning = false;
        }

        boughtCitizenCost = compound.getInteger(TAG_BOUGHT_CITIZENS);
        mercenaryLastUse = compound.getLong(TAG_MERCENARY_TIME);
        additionalChildTime = compound.getInteger(TAG_CHILD_TIME);

        // Permissions
        permissions.loadPermissions(compound);

        if (compound.hasKey(TAG_CITIZEN_MANAGER))
        {
            citizenManager.readFromNBT(compound.getCompoundTag(TAG_CITIZEN_MANAGER));
        }
        else
        {
            //Compatability with old version!
            citizenManager.readFromNBT(compound);
        }

        if (compound.hasKey(TAG_BUILDING_MANAGER))
        {
            buildingManager.readFromNBT(compound.getCompoundTag(TAG_BUILDING_MANAGER));
        }
        else
        {
            //Compatability with old version!
            buildingManager.readFromNBT(compound);
        }

        if (compound.hasKey(TAG_STATS_MANAGER))
        {
            statsManager.readFromNBT(compound.getCompoundTag(TAG_STATS_MANAGER));
        }
        else
        {
            //Compatability with old version!
            statsManager.readFromNBT(compound);
        }

        if (compound.hasKey(TAG_PROGRESS_MANAGER))
        {
            progressManager.readFromNBT(compound);
        }

        if (compound.hasKey(TAG_HAPPINESS_MODIFIER))
        {
            colonyHappinessManager.setLockedHappinessModifier(Optional.of(compound.getDouble(TAG_HAPPINESS_MODIFIER)));
        }
        else
        {
            colonyHappinessManager.setLockedHappinessModifier(Optional.empty());
        }

        raidManager.readFromNBT(compound);

        //  Workload
        workManager.readFromNBT(compound.getCompoundTag(TAG_WORK));

        wayPoints.clear();
        // Waypoints
        final NBTTagList wayPointTagList = compound.getTagList(TAG_WAYPOINT, NBT.TAG_COMPOUND);
        for (int i = 0; i < wayPointTagList.tagCount(); ++i)
        {
            final NBTTagCompound blockAtPos = wayPointTagList.getCompoundTagAt(i);
            final BlockPos pos = BlockPosUtil.readFromNBT(blockAtPos, TAG_WAYPOINT);
            final IBlockState state = NBTUtil.readBlockState(blockAtPos);
            wayPoints.put(pos, state);
        }

        freeBlocks.clear();
        // Free blocks
        final NBTTagList freeBlockTagList = compound.getTagList(TAG_FREE_BLOCKS, NBT.TAG_STRING);
        for (int i = 0; i < freeBlockTagList.tagCount(); ++i)
        {
            freeBlocks.add(Block.getBlockFromName(freeBlockTagList.getStringTagAt(i)));
        }

        freePositions.clear();
        // Free positions
        final NBTTagList freePositionTagList = compound.getTagList(TAG_FREE_POSITIONS, NBT.TAG_COMPOUND);
        for (int i = 0; i < freePositionTagList.tagCount(); ++i)
        {
            final NBTTagCompound blockTag = freePositionTagList.getCompoundTagAt(i);
            final BlockPos block = BlockPosUtil.readFromNBT(blockTag, TAG_FREE_POSITIONS);
            freePositions.add(block);
        }

        happinessData.readFromNBT(compound);
        packageManager.setLastContactInHours(compound.getInteger(TAG_ABANDONED));
        manualHousing = compound.getBoolean(TAG_MANUAL_HOUSING);

        if (compound.hasKey(TAG_MOVE_IN))
        {
            moveIn = compound.getBoolean(TAG_MOVE_IN);
        }

        if (compound.hasKey(TAG_STYLE))
        {
            this.style = compound.getString(TAG_STYLE);
        }

        if (compound.hasKey(TAG_RAIDABLE))
        {
            this.raidManager.setCanHaveRaiderEvents(compound.getBoolean(TAG_RAIDABLE));
        }
        else
        {
            this.raidManager.setCanHaveRaiderEvents(true);
        }

        if (compound.hasKey(TAG_AUTO_DELETE))
        {
            this.canColonyBeAutoDeleted = compound.getBoolean(TAG_AUTO_DELETE);
        }
        else
        {
            this.canColonyBeAutoDeleted = true;
        }

        if (compound.hasKey(TAG_TEAM_COLOR))
        {
            this.setColonyColor(TextFormatting.values()[compound.getInteger(TAG_TEAM_COLOR)]);
        }

        this.requestManager.reset();
        if (compound.hasKey(TAG_REQUESTMANAGER))
        {
            this.requestManager.deserializeNBT(compound.getCompoundTag(TAG_REQUESTMANAGER));
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
    public NBTTagCompound writeToNBT(@NotNull final NBTTagCompound compound)
    {
        //  Core attributes
        compound.setInteger(TAG_ID, id);
        compound.setInteger(TAG_DIMENSION, dimensionId);

        //  Basic data
        compound.setString(TAG_NAME, name);
        BlockPosUtil.writeToNBT(compound, TAG_CENTER, center);

        compound.setBoolean(TAG_MANUAL_HIRING, manualHiring);
        compound.setBoolean(TAG_NEED_TO_MOURN, needToMourn);
        compound.setBoolean(TAG_MOURNING, mourning);

        // Bought citizen count
        compound.setInteger(TAG_BOUGHT_CITIZENS, boughtCitizenCost);

        compound.setLong(TAG_MERCENARY_TIME, mercenaryLastUse);

        compound.setInteger(TAG_CHILD_TIME, additionalChildTime);

        // Permissions
        permissions.savePermissions(compound);

        final NBTTagCompound buildingCompound = new NBTTagCompound();
        buildingManager.writeToNBT(buildingCompound);
        compound.setTag(TAG_BUILDING_MANAGER, buildingCompound);

        final NBTTagCompound citizenCompound = new NBTTagCompound();
        citizenManager.writeToNBT(citizenCompound);
        compound.setTag(TAG_CITIZEN_MANAGER, citizenCompound);

        colonyHappinessManager.getLockedHappinessModifier().ifPresent(d -> compound.setDouble(TAG_HAPPINESS_MODIFIER, d));

        final NBTTagCompound statsCompound = new NBTTagCompound();
        statsManager.writeToNBT(statsCompound);
        compound.setTag(TAG_STATS_MANAGER, statsCompound);

        //  Workload
        @NotNull final NBTTagCompound workManagerCompound = new NBTTagCompound();
        workManager.writeToNBT(workManagerCompound);
        compound.setTag(TAG_WORK, workManagerCompound);

        progressManager.writeToNBT(compound);
        raidManager.writeToNBT(compound);

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

        happinessData.writeToNBT(compound);
        compound.setInteger(TAG_ABANDONED, packageManager.getLastContactInHours());
        compound.setBoolean(TAG_MANUAL_HOUSING, manualHousing);
        compound.setBoolean(TAG_MOVE_IN, moveIn);
        compound.setTag(TAG_REQUESTMANAGER, getRequestManager().serializeNBT());
        compound.setString(TAG_STYLE, style);
        compound.setBoolean(TAG_RAIDABLE, raidManager.canHaveRaiderEvents());
        compound.setBoolean(TAG_AUTO_DELETE, canColonyBeAutoDeleted);
        compound.setInteger(TAG_TEAM_COLOR, colonyTeamColor.ordinal());
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
    @Override
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
    @Override
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
    @Override
    public void onServerTick(@NotNull final TickEvent.ServerTickEvent event)
    {

    }

    /**
     * Get the Work Manager for the Colony.
     *
     * @return WorkManager for the Colony.
     */
    @Override
    @NotNull
    public IWorkManager getWorkManager()
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
    @Override
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

        colonyStateMachine.tick();
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

    @Override
    public boolean areAllColonyChunksLoaded()
    {
        final float distanceFromCenter = Configurations.gameplay.workingRangeTownHallChunks;
        return getLoadedChunkCount() / (distanceFromCenter * distanceFromCenter) >= 0.9f;
    }

    /**
     * Update the waypoints after worldTicks.
     */
    private boolean updateWayPoints()
    {
        if (!wayPoints.isEmpty())
        {
            final Object[] entries = wayPoints.entrySet().toArray();
            final int stopAt = world.rand.nextInt(entries.length);
            final Object obj = entries[stopAt];

            if (obj instanceof Map.Entry && ((Map.Entry) obj).getKey() instanceof BlockPos && ((Map.Entry) obj).getValue() instanceof IBlockState)
            {
                @NotNull final BlockPos key = (BlockPos) ((Map.Entry) obj).getKey();
                if (world.isBlockLoaded(key))
                {
                    @NotNull final IBlockState value = (IBlockState) ((Map.Entry) obj).getValue();
                    final Block worldBlock = world.getBlockState(key).getBlock();
                    if (worldBlock != (value.getBlock()) && worldBlock != ModBlocks.blockConstructionTape)
                    {
                        wayPoints.remove(key);
                        markDirty();
                    }
                }
            }
        }
        return false;
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
    @Override
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

    @Override
    @NotNull
    public Set<EntityPlayer> getMessageEntityPlayers()
    {
        Set<EntityPlayer> players = new HashSet<>();

        for (EntityPlayerMP player : packageManager.getCloseSubscribers())
        {
            if (permissions.hasPermission(player, Action.RECEIVE_MESSAGES))
            {
                players.add(player);
            }
        }

        return players;
    }

    @Override
    @NotNull
    public Set<EntityPlayer> getImportantMessageEntityPlayers()
    {
        final Set<EntityPlayer> playerList = getMessageEntityPlayers();

        for (final EntityPlayerMP player : packageManager.getImportantColonyPlayers())
        {
            if (permissions.hasPermission(player, Action.RECEIVE_MESSAGES_FAR_AWAY))
            {
                playerList.add(player);
            }
        }
        return playerList;
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
     * @param newMoveIn true if can move in, false if can't move in.
     */
    public void setMoveIn(final boolean newMoveIn)
    {
        this.moveIn = newMoveIn;
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
        for (final EntityPlayerMP player : packageManager.getCloseSubscribers())
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
    @Override
    public void onBuildingUpgradeComplete(@Nullable final IBuilding building, final int level)
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
    public void addWayPoint(final BlockPos point, final IBlockState block)
    {
        wayPoints.put(point, block);
        this.markDirty();
    }

    /**
     * Getter for overall happiness.
     *
     * @return the overall happiness.
     */
    @Override
    public double getOverallHappiness()
    {
        if (citizenManager.getCitizens().size() <= 0)
        {
            return (HappinessData.MAX_HAPPINESS + HappinessData.MIN_HAPPINESS) / 2.0;
        }

        double happinesSum = 0;
        for (final ICitizenData citizen : citizenManager.getCitizens())
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
    @Override
    public Map<BlockPos, IBlockState> getWayPoints()
    {
        return new HashMap<>(wayPoints);
    }

    /**
     * This sets whether or not a colony can be automatically deleted Via command, or an on-tick check.
     *
     * @param canBeDeleted whether the colony is able to be deleted automatically
     */
    public void setCanBeAutoDeleted(final boolean canBeDeleted)
    {
        this.canColonyBeAutoDeleted = canBeDeleted;
        this.markDirty();
    }

    /**
     * Getter for the default style of the colony.
     *
     * @return the style string.
     */
    @Override
    public String getStyle()
    {
        return style;
    }

    /**
     * Setter for the default style of the colony.
     *
     * @param style the default string.
     */
    @Override
    public void setStyle(final String style)
    {
        this.style = style;
    }

    /**
     * Get the buildingmanager of the colony.
     *
     * @return the buildingManager.
     */
    @Override
    public IBuildingManager getBuildingManager()
    {
        return buildingManager;
    }

    /**
     * Get the citizenManager of the colony.
     *
     * @return the citizenManager.
     */
    @Override
    public ICitizenManager getCitizenManager()
    {
        return citizenManager;
    }

    /**
     * Get the colony happiness manager.
     *
     * @return the colony happiness manager.
     */
    @Override
    public IColonyHappinessManager getColonyHappinessManager()
    {
        return colonyHappinessManager;
    }

    /**
     * Get the statsManager of the colony.
     *
     * @return the statsManager.
     */
    @Override
    public IStatisticAchievementManager getStatsManager()
    {
        return statsManager;
    }

    /**
     * Get the barbManager of the colony.
     *
     * @return the barbManager.
     */
    @Override
    public IRaiderManager getRaiderManager()
    {
        return raidManager;
    }

    /**
     * Get the packagemanager of the colony.
     *
     * @return the manager.
     */
    @Override
    public IColonyPackageManager getPackageManager()
    {
        return packageManager;
    }

    /**
     * Get the progress manager of the colony.
     *
     * @return the manager.
     */
    @Override
    public IProgressManager getProgressManager()
    {
        return progressManager;
    }

    /**
     * Get all visiting players.
     *
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
        if (rank != Rank.OWNER && rank != Rank.OFFICER && !visitingPlayers.contains(player) && Configurations.gameplay.sendEnteringLeavingMessages)
        {
            visitingPlayers.add(player);
            LanguageHandler.sendPlayerMessage(player, ENTERING_COLONY_MESSAGE, this.getPermissions().getOwnerName());
            LanguageHandler.sendPlayersMessage(getImportantMessageEntityPlayers(), ENTERING_COLONY_MESSAGE_NOTIFY, player.getName(), this.getName());
        }
    }

    @Override
    public void removeVisitingPlayer(final EntityPlayer player)
    {
        if (!getMessageEntityPlayers().contains(player) && Configurations.gameplay.sendEnteringLeavingMessages)
        {
            visitingPlayers.remove(player);
            LanguageHandler.sendPlayerMessage(player, LEAVING_COLONY_MESSAGE, this.getPermissions().getOwnerName());
            LanguageHandler.sendPlayersMessage(getImportantMessageEntityPlayers(), LEAVING_COLONY_MESSAGE_NOTIFY, player.getName(), this.getName());
        }
    }

    /**
     * Get the NBT tag of the colony.
     *
     * @return the tag of it.
     */
    @Override
    public NBTTagCompound getColonyTag()
    {
        try
        {
            if (this.colonyTag == null || this.isActive)
            {
                this.writeToNBT(new NBTTagCompound());
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
    @Override
    public int getNightsSinceLastRaid()
    {
        return nightsSinceLastRaid;
    }

    /**
     * Setter for the nights since the last raid.
     *
     * @param nights the number of nights.
     */
    @Override
    public void setNightsSinceLastRaid(final int nights)
    {
        this.nightsSinceLastRaid = nights;
    }

    /**
     * call to figure out if the colony needs to mourn.
     *
     * @return a boolean indicating the colony needs to mourn
     */
    @Override
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
    @Override
    public void setNeedToMourn(final boolean needToMourn, final String name)
    {
        this.needToMourn = needToMourn;
        if (needToMourn)
        {
            LanguageHandler.sendPlayersMessage(getImportantMessageEntityPlayers(), COM_MINECOLONIES_COREMOD_MOURN, name);
        }
    }

    /**
     * Call to check if the colony is mourning.
     *
     * @return indicates if the colony is mourning
     */
    @Override
    public boolean isMourning()
    {
        return mourning;
    }

    /**
     * Is player part of a wave trying to invade the colony?
     *
     * @param player the player to check..
     * @return true if so.
     */
    public boolean isValidAttackingPlayer(final EntityPlayer player)
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
    public boolean isValidAttackingGuard(final AbstractEntityCitizen entity)
    {
        if (packageManager.getLastContactInHours() > 1)
        {
            return false;
        }

        return AttackingPlayer.isValidAttack(entity, this);
    }

    /**
     * Add a guard to the list of attacking guards.
     *
     * @param IEntityCitizen the citizen to add.
     */
    public void addGuardToAttackers(final AbstractEntityCitizen IEntityCitizen, final EntityPlayer player)
    {
        if (player == null)
        {
            return;
        }

        for (final AttackingPlayer attackingPlayer : attackingPlayers)
        {
            if (attackingPlayer.getPlayer().equals(player))
            {
                if (attackingPlayer.addGuard(IEntityCitizen))
                {
                    LanguageHandler.sendPlayersMessage(getImportantMessageEntityPlayers(),
                      "Beware, " + attackingPlayer.getPlayer().getName() + " has now: " + attackingPlayer.getGuards().size() + " guards!");
                }
                return;
            }
        }

        for (final EntityPlayer visitingPlayer : visitingPlayers)
        {
            if (visitingPlayer.equals(player))
            {
                final AttackingPlayer attackingPlayer = new AttackingPlayer(visitingPlayer);
                attackingPlayer.addGuard(IEntityCitizen);
                attackingPlayers.add(attackingPlayer);
                LanguageHandler.sendPlayersMessage(getImportantMessageEntityPlayers(), "Beware, " + visitingPlayer.getName() + " is attacking you and he brought guards.");
            }
        }
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

    @Override
    public boolean useAdditionalChildTime(final int amount)
    {
        if (additionalChildTime < amount)
        {
            return false;
        }
        else
        {
            additionalChildTime -= amount;
            return true;
        }
    }

    @Override
    public void updateHasChilds()
    {
        for(ICitizenData data: this.getCitizenManager().getCitizens())
        {
            if (data.isChild())
            {
                this.hasChilds = true;
                return;
            }
        }
        this.hasChilds = false;
    }

    @Override
    public void addLoadedChunk(final long chunkPos)
    {
        loadedChunks.add(chunkPos);
    }

    @Override
    public void removeLoadedChunk(final long chunkPos)
    {
        loadedChunks.remove(chunkPos);
    }

    @Override
    public int getLoadedChunkCount()
    {
        return loadedChunks.size();
    }
}
