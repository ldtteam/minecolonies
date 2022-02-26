package com.minecolonies.coremod.colony;

import com.google.common.collect.ImmutableList;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.ColonyState;
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
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.research.IResearchManager;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.api.util.constant.Suppression;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.managers.*;
import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.colony.pvp.AttackingPlayer;
import com.minecolonies.coremod.colony.requestsystem.management.manager.StandardRequestManager;
import com.minecolonies.coremod.colony.workorders.WorkManager;
import com.minecolonies.coremod.network.messages.client.colony.ColonyViewRemoveWorkOrderMessage;
import com.minecolonies.coremod.permissions.ColonyPermissionEventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.nbt.StringNBT;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.api.colony.ColonyState.*;
import static com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateConstants.MAX_TICKRATE;
import static com.minecolonies.api.util.constant.ColonyConstants.*;
import static com.minecolonies.api.util.constant.Constants.DEFAULT_STYLE;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;
import static com.minecolonies.coremod.MineColonies.getConfig;

/**
 * This class describes a colony and contains all the data and methods for manipulating a Colony.
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
    private RegistryKey<World> dimensionId;

    /**
     * List of loaded chunks for the colony.
     */
    private Set<Long> loadedChunks = new HashSet<>();

    /**
     * List of loaded chunks for the colony.
     */
    public Set<Long> ticketedChunks = new HashSet<>();

    private boolean ticketedChunksDirty = true;

    /**
     * List of chunks that have to be be force loaded.
     */
    private Set<Long> pendingChunks = new HashSet<>();

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
     * Grave manager of the colony.
     */
    private final IGraveManager graveManager = new GraveManager(this);

    /**
     * Citizen manager of the colony.
     */
    private final ICitizenManager citizenManager = new CitizenManager(this);

    /**
     * Citizen manager of the colony.
     */
    private final IVisitorManager visitorManager = new VisitorManager(this);

    /**
     * Barbarian manager of the colony.
     */
    private final IRaiderManager raidManager = new RaidManager(this);

    /**
     * Event manager of the colony.
     */
    private final IEventManager eventManager = new EventManager(this);

    /**
     * Reproduction manager of the colony.
     */
    private final IReproductionManager reproductionManager = new ReproductionManager(this);

    /**
     * Event description manager of the colony.
     */
    private final IEventDescriptionManager eventDescManager = new EventDescriptionManager(this);

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
     * The colony permission object.
     */
    @NotNull
    private Permissions permissions;

    /**
     * The request manager assigned to the colony.
     */
    private IRequestManager requestManager;

    /**
     * The request manager assigned to the colony.
     */
    private IResearchManager researchManager;

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
     * The colonies state machine
     */
    private final ITickRateStateMachine<ColonyState> colonyStateMachine;

    /**
     * If the colony is dirty.
     */
    private boolean isActive = true;

    /**
     * The colony team color.
     */
    private TextFormatting colonyTeamColor = TextFormatting.WHITE;

    /**
     * The colony flag, as a list of patterns.
     */
    private ListNBT colonyFlag = new BannerPattern.Builder()
                                   .addPattern(BannerPattern.BASE, DyeColor.WHITE)
                                   .toListTag();

    /**
     * The last time the mercenaries were used.
     */
    private long mercenaryLastUse = 0;

    /**
     * The amount of additional child time gathered when the colony is not loaded.
     */
    private int additionalChildTime = 0;

    /**
     * The maximum amount of additional child time to be stored when the colony is not loaded.
     */
    private static final int maxAdditionalChildTime = 70000;

    /**
     * Boolean whether the colony has childs.
     */
    private boolean hasChilds = false;

    /**
     * Last time the server was online.
     */
    public long lastOnlineTime = 0;

    /**
     * The force chunk load timer.
     */
    private int forceLoadTimer = 0;

    /**
     * The texture set of the colony.
     */
    private String textureStyle = "default";

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
        researchManager = new ResearchManager(this);
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
            this.dimensionId = world.dimension();
            onWorldLoad(world);
            checkOrCreateTeam();
        }
        this.permissions = new Permissions(this);
        researchManager = new ResearchManager(this);
        colonyStateMachine = new TickRateStateMachine<>(INACTIVE, e -> {});

        colonyStateMachine.addTransition(new TickingTransition<>(INACTIVE, () -> true, this::updateState, UPDATE_STATE_INTERVAL));
        colonyStateMachine.addTransition(new TickingTransition<>(UNLOADED, () -> true, this::updateState, UPDATE_STATE_INTERVAL));
        colonyStateMachine.addTransition(new TickingTransition<>(ACTIVE, () -> true, this::updateState, UPDATE_STATE_INTERVAL));
        colonyStateMachine.addTransition(new TickingTransition<>(ACTIVE, () -> true, () -> {
            this.getCitizenManager().tickCitizenData();
            return null;
        }, TICKS_SECOND));

        colonyStateMachine.addTransition(new TickingTransition<>(ACTIVE, this::updateSubscribers, () -> ACTIVE, UPDATE_SUBSCRIBERS_INTERVAL));
        colonyStateMachine.addTransition(new TickingTransition<>(ACTIVE, this::tickRequests, () -> ACTIVE, UPDATE_RS_INTERVAL));
        colonyStateMachine.addTransition(new TickingTransition<>(ACTIVE, this::checkDayTime, () -> ACTIVE, UPDATE_DAYTIME_INTERVAL));
        colonyStateMachine.addTransition(new TickingTransition<>(ACTIVE, this::updateWayPoints, () -> ACTIVE, CHECK_WAYPOINT_EVERY));
        colonyStateMachine.addTransition(new TickingTransition<>(ACTIVE, this::worldTickSlow, () -> ACTIVE, MAX_TICKRATE));
        colonyStateMachine.addTransition(new TickingTransition<>(UNLOADED, this::worldTickUnloaded, () -> UNLOADED, MAX_TICKRATE));
    }

    /**
     * Updates the state the colony is in.
     *
     * @return the new colony state.
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

        if (!packageManager.getImportantColonyPlayers().isEmpty() || forceLoadTimer > 0)
        {
            isActive = true;
            return UNLOADED;
        }

        return INACTIVE;
    }

    /**
     * Updates the existing subscribers
     *
     * @return false
     */
    private boolean updateSubscribers()
    {
        packageManager.updateSubscribers();
        return false;
    }

    /**
     * Ticks the request manager.
     *
     * @return false
     */
    private boolean tickRequests()
    {
        if (getRequestManager() != null)
        {
            getRequestManager().tick();
        }
        return false;
    }

    /**
     * Called every 500 ticks, for slower updates.
     *
     * @return false
     */
    private boolean worldTickSlow()
    {
        buildingManager.cleanUpBuildings(this);
        citizenManager.onColonyTick(this);
        visitorManager.onColonyTick(this);
        updateAttackingPlayers();
        eventManager.onColonyTick(this);
        buildingManager.onColonyTick(this);
        graveManager.onColonyTick(this);
        workManager.onColonyTick(this);
        reproductionManager.onColonyTick(this);

        final long currTime = System.currentTimeMillis();
        if (lastOnlineTime != 0)
        {
            final long pastTime = currTime - lastOnlineTime;
            if (pastTime > ONE_HOUR_IN_MILLIS)
            {
                for (final IBuilding building : buildingManager.getBuildings().values())
                {
                    building.processOfflineTime(pastTime/1000);
                }
            }
        }
        lastOnlineTime = currTime;

        updateChildTime();
        updateChunkLoadTimer();
        return false;
    }

    /**
     * Check if we can unload the colony now.
     * Update chunk unload timer and releases chunks when it hits 0.
     */
    private void updateChunkLoadTimer()
    {
        if (getConfig().getServer().forceLoadColony.get())
        {
            for (final ServerPlayerEntity sub : getPackageManager().getCloseSubscribers())
            {
                if (getPermissions().hasPermission(sub, Action.CAN_KEEP_COLONY_ACTIVE_WHILE_AWAY))
                {
                    this.forceLoadTimer = CHUNK_UNLOAD_DELAY;
                    for (final long pending : pendingChunks)
                    {
                        checkChunkAndRegisterTicket(pending, world.getChunk(ChunkPos.getX(pending), ChunkPos.getZ(pending)));
                    }

                    pendingChunks.clear();
                    return;
                }
            }

            if (this.forceLoadTimer > 0)
            {
                this.forceLoadTimer -= MAX_TICKRATE;
                if (this.forceLoadTimer <= 0)
                {
                    for (final long chunkPos : this.ticketedChunks)
                    {
                        final int chunkX = ChunkPos.getX(chunkPos);
                        final int chunkZ = ChunkPos.getZ(chunkPos);
                        if (world instanceof ServerWorld)
                        {
                            final ChunkPos pos = new ChunkPos(chunkX, chunkZ);
                            ((ServerChunkProvider) world.getChunkSource()).removeRegionTicket(KEEP_LOADED_TYPE, pos, 2, pos);
                        }
                    }
                    ticketedChunks.clear();
                    ticketedChunksDirty = true;
                }
            }
        }
    }

    /**
     * Checks the chunk and registers a ticket for it if needed
     *
     * @param chunkPos chunk position to check
     */
    private void checkChunkAndRegisterTicket(final long chunkPos, final Chunk chunk)
    {
        if (forceLoadTimer > 0 && world instanceof ServerWorld)
        {
            if (!ticketedChunks.contains(chunkPos) && buildingManager.isWithinBuildingZone(chunk))
            {
                ticketedChunks.add(chunkPos);
                ticketedChunksDirty = true;
                ((ServerChunkProvider) world.getChunkSource()).addRegionTicket(KEEP_LOADED_TYPE, chunk.getPos(), 2, chunk.getPos());
            }
        }
    }

    /**
     * Called every 500 ticks, for slower updates. Only ticked when the colony is not loaded.
     *
     * @return false
     */
    private boolean worldTickUnloaded()
    {
        updateChildTime();
        updateChunkLoadTimer();
        return false;
    }

    /**
     * Adds 500 additional ticks to the child growth.
     */
    private void updateChildTime()
    {
        if (hasChilds && additionalChildTime < maxAdditionalChildTime)
        {
            additionalChildTime += MAX_TICKRATE;
        }
        else
        {
            additionalChildTime = 0;
        }
    }

    /**
     * Updates the day and night detection.
     *
     * @return false
     */
    private boolean checkDayTime()
    {
        if (isDay && !WorldUtil.isDayTime(world))
        {
            isDay = false;
            eventManager.onNightFall();
            raidManager.onNightFall();
            if (!packageManager.getCloseSubscribers().isEmpty())
            {
                citizenManager.checkCitizensForHappiness();
            }

            citizenManager.updateCitizenSleep(false);
        }
        else if (!isDay && WorldUtil.isDayTime(world))
        {
            isDay = true;
            citizenManager.onWakeUp();
        }
        return false;
    }

    /**
     * Updates the pvping playeres.
     */
    public void updateAttackingPlayers()
    {
        final List<PlayerEntity> visitors = new ArrayList<>(visitingPlayers);

        //Clean up visiting player.
        for (final PlayerEntity player : visitors)
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
                    LanguageHandler.sendPlayersMessage(getImportantMessageEntityPlayers(), COLONY_DEFENDED_SUCCESS_MESSAGE, player.getPlayer().getName().getString());
                }
            }
        }
    }

    @Override
    public ScorePlayerTeam getTeam()
    {
        // This getter will create the team if it doesn't exist. Could do something different though in the future.
        return checkOrCreateTeam();
    }

    /**
     * Check or create the team.
     */
    private ScorePlayerTeam checkOrCreateTeam()
    {
        if (this.world.getScoreboard().getPlayerTeam(getTeamName()) == null)
        {
            this.world.getScoreboard().addPlayerTeam(getTeamName());
            this.world.getScoreboard().getPlayerTeam(getTeamName()).setAllowFriendlyFire(false);
        }
        return this.world.getScoreboard().getPlayerTeam(getTeamName());
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
            this.world.getScoreboard().getPlayerTeam(getTeamName()).setColor(colonyColor);
            this.world.getScoreboard().getPlayerTeam(getTeamName()).setPlayerPrefix(new StringTextComponent(colonyColor.toString()));
        }
        this.markDirty();
    }

    /**
     * Set up the colony flag patterns for use in decorations etc
     *
     * @param colonyFlag the list of pattern-color pairs
     */
    @Override
    public void setColonyFlag(ListNBT colonyFlag)
    {
        this.colonyFlag = colonyFlag;
        markDirty();
    }

    /**
     * Load a saved colony.
     *
     * @param compound The NBT compound containing the colony's data.
     * @param world    the world to load it for.
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
            c.center = BlockPosUtil.read(compound, TAG_CENTER);
            c.dimensionId = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(compound.getString(TAG_DIMENSION)));

            c.setRequestManager();
            c.read(compound);

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
    public void read(@NotNull final CompoundNBT compound)
    {
        manualHiring = compound.getBoolean(TAG_MANUAL_HIRING);
        dimensionId = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(compound.getString(TAG_DIMENSION)));

        mercenaryLastUse = compound.getLong(TAG_MERCENARY_TIME);
        additionalChildTime = compound.getInt(TAG_CHILD_TIME);

        // Permissions
        permissions.loadPermissions(compound);

        citizenManager.read(compound.getCompound(TAG_CITIZEN_MANAGER));
        visitorManager.read(compound);
        buildingManager.read(compound.getCompound(TAG_BUILDING_MANAGER));

        // Recalculate max after citizens and buildings are loaded.
        citizenManager.calculateMaxCitizens();

        graveManager.read(compound.getCompound(TAG_GRAVE_MANAGER));

        if (compound.getAllKeys().contains(TAG_PROGRESS_MANAGER))
        {
            progressManager.read(compound);
        }

        eventManager.readFromNBT(compound);
        eventDescManager.deserializeNBT(compound.getCompound(NbtTagConstants.TAG_EVENT_DESC_MANAGER));

        if (compound.getAllKeys().contains(TAG_RESEARCH))
        {
            researchManager.readFromNBT(compound.getCompound(TAG_RESEARCH));
            // now that buildings, colonists, and research are loaded, check for new autoStartResearch.
            // this is mostly for backwards compatibility with older saves, so players do not have to manually start newly added autostart researches that they've unlocked before the update.
            researchManager.checkAutoStartResearch();
        }

        //  Workload
        workManager.read(compound.getCompound(TAG_WORK));

        wayPoints.clear();
        // Waypoints
        final ListNBT wayPointTagList = compound.getList(TAG_WAYPOINT, NBT.TAG_COMPOUND);
        for (int i = 0; i < wayPointTagList.size(); ++i)
        {
            final CompoundNBT blockAtPos = wayPointTagList.getCompound(i);
            final BlockPos pos = BlockPosUtil.read(blockAtPos, TAG_WAYPOINT);
            final BlockState state = NBTUtil.readBlockState(blockAtPos);
            wayPoints.put(pos, state);
        }

        // Free blocks
        freeBlocks.clear();
        final ListNBT freeBlockTagList = compound.getList(TAG_FREE_BLOCKS, NBT.TAG_STRING);
        for (int i = 0; i < freeBlockTagList.size(); ++i)
        {
            freeBlocks.add(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(freeBlockTagList.getString(i))));
        }

        freePositions.clear();
        // Free positions
        final ListNBT freePositionTagList = compound.getList(TAG_FREE_POSITIONS, NBT.TAG_COMPOUND);
        for (int i = 0; i < freePositionTagList.size(); ++i)
        {
            final CompoundNBT blockTag = freePositionTagList.getCompound(i);
            final BlockPos block = BlockPosUtil.read(blockTag, TAG_FREE_POSITIONS);
            freePositions.add(block);
        }

        packageManager.setLastContactInHours(compound.getInt(TAG_ABANDONED));
        manualHousing = compound.getBoolean(TAG_MANUAL_HOUSING);

        if (compound.getAllKeys().contains(TAG_MOVE_IN))
        {
            moveIn = compound.getBoolean(TAG_MOVE_IN);
        }

        if (compound.getAllKeys().contains(TAG_STYLE))
        {
            this.style = compound.getString(TAG_STYLE);
        }

        raidManager.read(compound);

        if (compound.getAllKeys().contains(TAG_AUTO_DELETE))
        {
            this.canColonyBeAutoDeleted = compound.getBoolean(TAG_AUTO_DELETE);
        }
        else
        {
            this.canColonyBeAutoDeleted = true;
        }

        if (compound.getAllKeys().contains(TAG_TEAM_COLOR))
        {
            // This read can occur before the world is non-null, due to Minecraft's order of operations for capabilities.
            // As a result, setColonyColor proper must wait until onWorldLoad fires.
            this.colonyTeamColor = TextFormatting.values()[compound.getInt(TAG_TEAM_COLOR)];
        }

        if (compound.getAllKeys().contains(TAG_FLAG_PATTERNS))
        {
            this.setColonyFlag(compound.getList(TAG_FLAG_PATTERNS, Constants.TAG_COMPOUND));
        }

        this.requestManager.reset();
        if (compound.getAllKeys().contains(TAG_REQUESTMANAGER))
        {
            this.requestManager.deserializeNBT(compound.getCompound(TAG_REQUESTMANAGER));
        }
        this.lastOnlineTime = compound.getLong(TAG_LAST_ONLINE);
        if (compound.contains(TAG_COL_TEXT))
        {
            this.textureStyle = compound.getString(TAG_COL_TEXT);
        }
        else if (this.style.contains("medieval"))
        {
            //todo remove in 1.17. This allows players to keep their current medieval colonies.
            this.textureStyle = "medieval";
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
        compound.putString(TAG_DIMENSION, dimensionId.location().toString());

        //  Basic data
        compound.putString(TAG_NAME, name);
        BlockPosUtil.write(compound, TAG_CENTER, center);

        compound.putBoolean(TAG_MANUAL_HIRING, manualHiring);
        compound.putLong(TAG_MERCENARY_TIME, mercenaryLastUse);

        compound.putInt(TAG_CHILD_TIME, additionalChildTime);

        // Permissions
        permissions.savePermissions(compound);

        final CompoundNBT buildingCompound = new CompoundNBT();
        buildingManager.write(buildingCompound);
        compound.put(TAG_BUILDING_MANAGER, buildingCompound);

        final CompoundNBT citizenCompound = new CompoundNBT();
        citizenManager.write(citizenCompound);
        compound.put(TAG_CITIZEN_MANAGER, citizenCompound);

        visitorManager.write(compound);

        final CompoundNBT graveCompound = new CompoundNBT();
        graveManager.write(graveCompound);
        compound.put(TAG_GRAVE_MANAGER, graveCompound);

        //  Workload
        @NotNull final CompoundNBT workManagerCompound = new CompoundNBT();
        workManager.write(workManagerCompound);
        compound.put(TAG_WORK, workManagerCompound);

        progressManager.write(compound);
        eventManager.writeToNBT(compound);
        compound.put(NbtTagConstants.TAG_EVENT_DESC_MANAGER, eventDescManager.serializeNBT());
        raidManager.write(compound);

        @NotNull final CompoundNBT researchManagerCompound = new CompoundNBT();
        researchManager.writeToNBT(researchManagerCompound);
        compound.put(TAG_RESEARCH, researchManagerCompound);

        // Waypoints
        @NotNull final ListNBT wayPointTagList = new ListNBT();
        for (@NotNull final Map.Entry<BlockPos, BlockState> entry : wayPoints.entrySet())
        {
            @NotNull final CompoundNBT wayPointCompound = new CompoundNBT();
            BlockPosUtil.write(wayPointCompound, TAG_WAYPOINT, entry.getKey());
            wayPointCompound.put(TAG_BLOCK, NBTUtil.writeBlockState(entry.getValue()));
            wayPointTagList.add(wayPointCompound);
        }
        compound.put(TAG_WAYPOINT, wayPointTagList);

        // Free blocks
        @NotNull final ListNBT freeBlocksTagList = new ListNBT();
        for (@NotNull final Block block : freeBlocks)
        {
            freeBlocksTagList.add(StringNBT.valueOf(block.getRegistryName().toString()));
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

        compound.putInt(TAG_ABANDONED, packageManager.getLastContactInHours());
        compound.putBoolean(TAG_MANUAL_HOUSING, manualHousing);
        compound.putBoolean(TAG_MOVE_IN, moveIn);
        compound.put(TAG_REQUESTMANAGER, getRequestManager().serializeNBT());
        compound.putString(TAG_STYLE, style);
        compound.putBoolean(TAG_AUTO_DELETE, canColonyBeAutoDeleted);
        compound.putInt(TAG_TEAM_COLOR, colonyTeamColor.ordinal());
        compound.put(TAG_FLAG_PATTERNS, colonyFlag);
        compound.putLong(TAG_LAST_ONLINE, lastOnlineTime);
        compound.putString(TAG_COL_TEXT, textureStyle);
        this.colonyTag = compound;

        isActive = false;
        return compound;
    }

    /**
     * Returns the dimension ID.
     *
     * @return Dimension ID.
     */
    public RegistryKey<World> getDimension()
    {
        return dimensionId;
    }

    @Override
    public boolean isRemote()
    {
        return false;
    }

    @Override
    public IResearchManager getResearchManager()
    {
        return this.researchManager;
    }

    /**
     * When the Colony's world is loaded, associate with it.
     *
     * @param w World object.
     */
    @Override
    public void onWorldLoad(@NotNull final World w)
    {
        if (w.dimension() == dimensionId)
        {
            this.world = w;
            // Register a new event handler
            if (eventHandler == null)
            {
                eventHandler = new ColonyPermissionEventHandler(this);
                MinecraftForge.EVENT_BUS.register(eventHandler);
            }
            setColonyColor(this.colonyTeamColor);
        }
    }

    /**
     * Unsets the world if the world unloads.
     *
     * @param w World object.
     */
    @Override
    public void onWorldUnload(@NotNull final World w)
    {
        if (w != world)
        {
            /*
             * If the event world is not the colony world ignore. This might happen in interactions with other mods.
             * This should not be a problem for minecolonies as long as we take care to do nothing in that moment.
             */
            return;
        }

        if (eventHandler != null)
        {
            MinecraftForge.EVENT_BUS.unregister(eventHandler);
        }
        world = null;
    }

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
     * Any per-world-tick logic should be performed here. NOTE: If the Colony's world isn't loaded, it won't have a world tick. Use onServerTick for logic that should _always_
     * run.
     *
     * @param event {@link TickEvent.WorldTickEvent}
     */
    @Override
    public void onWorldTick(@NotNull final TickEvent.WorldTickEvent event)
    {
        if (event.world != getWorld())
        {
            /*
             * If the event world is not the colony world ignore. This might happen in interactions with other mods.
             * This should not be a problem for minecolonies as long as we take care to do nothing in that moment.
             */
            return;
        }

        colonyStateMachine.tick();
    }

    /**
     * Calculate randomly if the colony should update the citizens. By mean they update it at CLEANUP_TICK_INCREMENT.
     *
     * @param world        the world.
     * @param averageTicks the average ticks to upate it.
     * @return a boolean by random.
     */
    public static boolean shallUpdate(final World world, final int averageTicks)
    {
        return world.getGameTime() % (world.random.nextInt(averageTicks * 2) + 1) == 0;
    }

    /**
     * Update the waypoints after worldTicks.
     *
     * @return false
     */
    private boolean updateWayPoints()
    {
        if (!wayPoints.isEmpty() && world != null)
        {
            final int randomPos = world.random.nextInt(wayPoints.size());
            int count = 0;
            for (final Map.Entry<BlockPos, BlockState> entry : wayPoints.entrySet())
            {
                if (count++ == randomPos)
                {
                    if (WorldUtil.isBlockLoaded(world, entry.getKey()))
                    {
                        final Block worldBlock = world.getBlockState(entry.getKey()).getBlock();
                        if (
                          ((worldBlock != (entry.getValue().getBlock()) && entry.getValue().getBlock() != ModBlocks.blockWayPoint) && worldBlock != ModBlocks.blockConstructionTape)
                            || (world.isEmptyBlock(entry.getKey().below()) && !entry.getValue().getMaterial().isSolid()))
                        {
                            wayPoints.remove(entry.getKey());
                            markDirty();
                        }
                    }
                    return false;
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
     * Sets the name of the colony. Marks dirty.
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
        if (w.dimension() != this.dimensionId)
        {
            return false;
        }

        final Chunk chunk = w.getChunkAt(pos);
        final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null).resolve().orElse(null);
        return cap != null && cap.getOwningColony() == this.getID();
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
    public boolean hasBuilding(final String name, final int level, boolean singleBuilding)
    {
        int sum = 0;
        for (final IBuilding building : this.getBuildingManager().getBuildings().values())
        {
            if (building.getBuildingType().getRegistryName().getPath().equalsIgnoreCase(name))
            {
                if (singleBuilding)
                {
                    if (building.getBuildingLevel() >= level)
                    {
                        return true;
                    }
                }
                else
                {
                    sum += building.getBuildingLevel();
                    if(sum >= level)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
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

    @NotNull
    @Override
    public IRequestManager getRequestManager()
    {
        return requestManager;
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
    public List<PlayerEntity> getMessagePlayerEntities()
    {
        List<PlayerEntity> players = new ArrayList<>();

        for (ServerPlayerEntity player : packageManager.getCloseSubscribers())
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
    public List<PlayerEntity> getImportantMessageEntityPlayers()
    {
        final Set<PlayerEntity> playerList = new HashSet<>(getMessagePlayerEntities());

        for (final ServerPlayerEntity player : packageManager.getImportantColonyPlayers())
        {
            if (permissions.hasPermission(player, Action.RECEIVE_MESSAGES_FAR_AWAY))
            {
                playerList.add(player);
            }
        }
        return new ArrayList<>(playerList);
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
        for (final ServerPlayerEntity player : packageManager.getCloseSubscribers())
        {
            Network.getNetwork().sendToPlayer(new ColonyViewRemoveWorkOrderMessage(this, orderId), player);
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
     * Getter for overall happiness.
     *
     * @return the overall happiness.
     */
    @Override
    public double getOverallHappiness()
    {
        if (citizenManager.getCitizens().size() <= 0)
        {
            return 5.5;
        }

        double happinessSum = 0;
        for (final ICitizenData citizen : citizenManager.getCitizens())
        {
            happinessSum += citizen.getCitizenHappinessHandler().getHappiness(citizen.getColony());
        }
        return happinessSum / citizenManager.getCitizens().size();
    }

    /**
     * Get all the waypoints of the colony.
     *
     * @return copy of hashmap.
     */
    @Override
    public Map<BlockPos, BlockState> getWayPoints()
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
        this.markDirty();
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
     * Get the graveManager of the colony.
     *
     * @return the graveManager.
     */
    @Override
    public IGraveManager getGraveManager()
    {
        return graveManager;
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
     * Get the visitor manager of the colony.
     *
     * @return the visitor manager.
     */
    @Override
    public IVisitorManager getVisitorManager()
    {
        return visitorManager;
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

    @Override
    public IEventManager getEventManager()
    {
        return eventManager;
    }

    @Override
    public IReproductionManager getReproductionManager()
    {
        return reproductionManager;
    }

    @Override
    public IEventDescriptionManager getEventDescriptionManager()
    {
        return eventDescManager;
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
    public ImmutableList<PlayerEntity> getVisitingPlayers()
    {
        return ImmutableList.copyOf(visitingPlayers);
    }

    @Override
    public void addVisitingPlayer(final PlayerEntity player)
    {
        final Rank rank = getPermissions().getRank(player);
        if (!rank.isColonyManager() && !visitingPlayers.contains(player) && MineColonies.getConfig().getServer().sendEnteringLeavingMessages.get())
        {
            visitingPlayers.add(player);
            LanguageHandler.sendPlayerMessage(player, ENTERING_COLONY_MESSAGE, this.getPermissions().getOwnerName());
            LanguageHandler.sendPlayersMessage(getImportantMessageEntityPlayers(), ENTERING_COLONY_MESSAGE_NOTIFY, player.getName().getString(), this.getName());
        }
    }

    @Override
    public void removeVisitingPlayer(final PlayerEntity player)
    {
        if (!getMessagePlayerEntities().contains(player) && MineColonies.getConfig().getServer().sendEnteringLeavingMessages.get())
        {
            visitingPlayers.remove(player);
            LanguageHandler.sendPlayerMessage(player, LEAVING_COLONY_MESSAGE, this.getPermissions().getOwnerName());
            LanguageHandler.sendPlayersMessage(getImportantMessageEntityPlayers(), LEAVING_COLONY_MESSAGE_NOTIFY, player.getName().getString(), this.getName());
        }
    }

    /**
     * Get the NBT tag of the colony.
     *
     * @return the tag of it.
     */
    @Override
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
    public void addGuardToAttackers(final AbstractEntityCitizen IEntityCitizen, final PlayerEntity player)
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
                    LanguageHandler.sendPlayersMessage(getImportantMessageEntityPlayers(), COLONY_ATTACK_GUARD_GROUP_SIZE_MESSAGE,
                            attackingPlayer.getPlayer().getName().getString(), attackingPlayer.getGuards().size());
                }
                return;
            }
        }

        for (final PlayerEntity visitingPlayer : visitingPlayers)
        {
            if (visitingPlayer.equals(player))
            {
                final AttackingPlayer attackingPlayer = new AttackingPlayer(visitingPlayer);
                attackingPlayer.addGuard(IEntityCitizen);
                attackingPlayers.add(attackingPlayer);
                LanguageHandler.sendPlayersMessage(getImportantMessageEntityPlayers(), COLONY_ATTACK_START_MESSAGE, visitingPlayer.getName().getString());
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
     * Getter for the colony flag patterns
     *
     * @return the list of pattern-color pairs
     */
    @Override
    public ListNBT getColonyFlag() { return colonyFlag; }

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
     * Save the time when mercenaries are used, to set a cooldown.
     */
    @Override
    public void usedMercenaries()
    {
        mercenaryLastUse = world.getGameTime();
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
        for (ICitizenData data : this.getCitizenManager().getCitizens())
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
    public void addLoadedChunk(final long chunkPos, final Chunk chunk)
    {
        if (world instanceof ServerWorld
              && getConfig().getServer().forceLoadColony.get())
        {
            if (this.forceLoadTimer > 0)
            {
                checkChunkAndRegisterTicket(chunkPos, chunk);
            }
            else
            {
                this.pendingChunks.add(chunkPos);
            }
        }
        this.loadedChunks.add(chunkPos);
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

    @Override
    public ColonyState getState()
    {
        return colonyStateMachine.getState();
    }

    @Override
    public boolean isActive()
    {
        return colonyStateMachine.getState() != INACTIVE;
    }

    @Override
    public boolean isDay()
    {
        return isDay;
    }

    @Override
    public Set<Long> getTicketedChunks()
    {
        return ticketedChunks;
    }

    @Override
    public void setTextureStyle(final String style)
    {
        this.textureStyle = style;
        this.markDirty();
    }

    @Override
    public String getTextureStyleId()
    {
        return this.textureStyle;
    }

    @Override
    public void notifyPlayers(final ITextComponent component)
    {
        for (final PlayerEntity player : getMessagePlayerEntities())
        {
            player.sendMessage(component, player.getUUID());
        }
    }

    @Override
    public void notifyColonyManagers(final ITextComponent component)
    {
        for (final PlayerEntity player : getImportantMessageEntityPlayers())
        {
            player.sendMessage(component, player.getUUID());
        }
    }

    /**
     * Check if we need to update the view's chunk ticket info
     *
     * @return
     */
    public boolean isTicketedChunksDirty()
    {
        return ticketedChunksDirty;
    }
}
