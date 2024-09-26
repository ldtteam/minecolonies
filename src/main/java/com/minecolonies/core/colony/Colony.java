package com.minecolonies.core.colony;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.ISettingsModule;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.claim.ChunkClaimData;
import com.minecolonies.api.colony.claim.IChunkClaimData;
import com.minecolonies.api.colony.managers.interfaces.*;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.workorders.IWorkManager;
import com.minecolonies.api.compatibility.newstruct.BlueprintMapping;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.quests.IQuestManager;
import com.minecolonies.api.research.IResearchManager;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.api.util.constant.Suppression;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import com.minecolonies.core.colony.buildings.modules.SettingsModule;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.core.colony.events.raid.RaidManager;
import com.minecolonies.core.colony.managers.*;
import com.minecolonies.core.colony.permissions.ColonyPermissionEventHandler;
import com.minecolonies.core.colony.permissions.Permissions;
import com.minecolonies.core.colony.pvp.AttackingPlayer;
import com.minecolonies.core.colony.requestsystem.management.manager.StandardRequestManager;
import com.minecolonies.core.colony.workorders.WorkManager;
import com.minecolonies.core.datalistener.CitizenNameListener;
import com.minecolonies.core.network.messages.client.colony.ColonyViewRemoveWorkOrderMessage;
import com.minecolonies.core.quests.QuestManager;
import com.minecolonies.core.util.BackUpHelper;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.minecolonies.api.colony.ColonyState.*;
import static com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateConstants.MAX_TICKRATE;
import static com.minecolonies.api.util.constant.ColonyConstants.*;
import static com.minecolonies.api.util.constant.Constants.DEFAULT_STYLE;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.core.MineColonies.getConfig;

/**
 * This class describes a colony and contains all the data and methods for manipulating a Colony.
 */
@SuppressWarnings({Suppression.BIG_CLASS, Suppression.SPLIT_CLASS})
public class Colony implements IColony
{
    /**
     * The default style for the building.
     */
    private String pack = DEFAULT_STYLE;

    /**
     * ID of the colony.
     */
    private final int id;

    /**
     * Dimension of the colony.
     */
    private ResourceKey<Level> dimensionId;

    /**
     * List of loaded chunks for the colony.
     */
    private final ConcurrentHashMap<Long, Long> loadedChunks = new ConcurrentHashMap<>();

    /**
     * List of loaded chunks for the colony.
     */
    public Set<Long> ticketedChunks = new HashSet<>();

    private boolean ticketedChunksDirty = true;

    /**
     * List of chunks that have to be force loaded.
     */
    private final Set<Long> pendingChunks = new HashSet<>();

    /**
     * List of chunks pending for unloading, which have their tickets removed
     */
    private final Set<Long> pendingToUnloadChunks = new HashSet<>();

    /**
     * List of waypoints of the colony.
     */
    private final Map<BlockPos, BlockState> wayPoints = new HashMap<>();

    /**
     * Work Manager of the colony (Request System).
     */
    private final WorkManager workManager;

    /**
     * Building manager of the colony.
     */
    private final IRegisteredStructureManager buildingManager;

    /**
     * Grave manager of the colony.
     */
    private final IGraveManager graveManager;

    /**
     * Citizen manager of the colony.
     */
    private final ICitizenManager citizenManager;

    /**
     * Citizen manager of the colony.
     */
    private final IVisitorManager visitorManager;

    /**
     * Barbarian manager of the colony.
     */
    private final IRaiderManager raidManager;

    /**
     * Event manager of the colony.
     */
    private final IEventManager eventManager;

    /**
     * Reproduction manager of the colony.
     */
    private final IReproductionManager reproductionManager;

    /**
     * Event description manager of the colony.
     */
    private final IEventDescriptionManager eventDescManager;

    /**
     * The colony package manager.
     */
    private final IColonyPackageManager packageManager;

    /**
     * Event manager of the colony.
     */
    private final IStatisticsManager statisticManager;

    /**
     * Quest manager for this colony
     */
    private final IQuestManager questManager;

    /**
     * The colony permission object.
     */
    private final Permissions permissions;

    /**
     * The request manager assigned to the colony.
     */
    private IRequestManager requestManager;

    /**
     * The request manager assigned to the colony.
     */
    private final IResearchManager researchManager;

    /**
     * The Positions which players can freely interact.
     */
    private ImmutableSet<BlockPos> freePositions = ImmutableSet.of();

    /**
     * The Blocks which players can freely interact with.
     */
    private ImmutableSet<Block> freeBlocks = ImmutableSet.of();

    /**
     * Colony permission event handler.
     */
    private ColonyPermissionEventHandler eventHandler;

    /**
     * Whether this colony may be auto-deleted.
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
    private ServerLevel world = null;

    /**
     * The name of the colony.
     */
    private String name;

    /**
     * The center of the colony.
     */
    private final BlockPos center;

    /**
     * The NBTTag compound of the colony itself.
     */
    private CompoundTag colonyTag;

    /**
     * List of players visiting the colony.
     */
    private final List<Player> visitingPlayers = new ArrayList<>();

    /**
     * List of players attacking the colony.
     */
    private final List<AttackingPlayer> attackingPlayers = new ArrayList<>();

    /**
     * The colonies state machine
     */
    private ITickRateStateMachine<ColonyState> colonyStateMachine = null;

    /**
     * If the colony is dirty.
     */
    private boolean isDirty = true;

    /**
     * The colony team color.
     */
    private ChatFormatting colonyTeamColor = ChatFormatting.WHITE;

    /**
     * The colony flag, as a list of patterns.
     */
    private BannerPatternLayers colonyFlag;

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
     * The colony name style.
     */
    private String nameStyle = "default";

    /**
     * Current day of the colony.
     */
    private int day = 0;

    /**
     * Colony claim data.
     */
    private final Long2ObjectMap<ChunkClaimData> claimData = new Long2ObjectOpenHashMap<>();

    /**
     * Townhall settings module.
     */
    private final SettingsModule settingsModule = (SettingsModule) BuildingEntry.produceModuleWithoutBuilding(BuildingModules.TOWNHALL_SETTINGS.key);

    /**
     * Base constructor.
     *
     * @param id     The current id for the colony.
     * @param name The name of the colony.
     * @param world  The world the colony exists in.
     * @param center The center of the colony (location of Town Hall).
     */
    Colony(final int id, final String name, @Nullable final ServerLevel world, final BlockPos center)
    {
        this.id = id;
        this.name = name;
        this.center = center;

        this.workManager = new WorkManager(this);
        this.buildingManager = new RegisteredStructureManager(this);
        this.graveManager = new GraveManager(this);
        this.citizenManager = new CitizenManager(this);
        this.visitorManager = new VisitorManager(this);
        this.raidManager = new RaidManager(this);
        this.eventManager = new EventManager(this);
        this.reproductionManager = new ReproductionManager(this);
        this.eventDescManager = new EventDescriptionManager(this);
        this.packageManager = new ColonyPackageManager(this);
        this.statisticManager = new StatisticsManager();
        this.questManager = new QuestManager(this);
        this.permissions = new Permissions(this);
        this.researchManager = new ResearchManager(this);

        if (world != null)
        {
            this.colonyFlag = new BannerPatternLayers.Builder().add(Utils.getRegistryValue(BannerPatterns.BASE, world), DyeColor.WHITE).build();
            this.dimensionId = world.dimension();
            onWorldLoad(world);
            checkOrCreateTeam();
        }

        colonyStateMachine = new TickRateStateMachine<>(INACTIVE, e ->
        {
            Log.getLogger().warn("Exception triggered in colony:{} in dimension:{}", getID(), getDimension().location(), e);
            colonyStateMachine.setCurrentDelay(20 * 60 * 5);
        });

        colonyStateMachine.addTransition(new TickingTransition<>(INACTIVE, () -> true, this::updateState, UPDATE_STATE_INTERVAL));
        colonyStateMachine.addTransition(new TickingTransition<>(UNLOADED, () -> true, this::updateState, UPDATE_STATE_INTERVAL));
        colonyStateMachine.addTransition(new TickingTransition<>(ACTIVE, () -> true, this::updateState, UPDATE_STATE_INTERVAL));
        colonyStateMachine.addTransition(new TickingTransition<>(ACTIVE, citizenManager::tickCitizenData, () -> ACTIVE, TICKS_SECOND * 3));

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
            isDirty = true;
            return ACTIVE;
        }

        if (!packageManager.getImportantColonyPlayers().isEmpty() || forceLoadTimer > 0)
        {
            isDirty = true;
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
        getRequestManager().tick();
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
        questManager.onColonyTick();

        final long currTime = System.currentTimeMillis();
        if (lastOnlineTime != 0)
        {
            final long pastTime = currTime - lastOnlineTime;
            if (pastTime > ONE_HOUR_IN_MILLIS)
            {
                for (final IBuilding building : buildingManager.getBuildings().values())
                {
                    building.processOfflineTime(pastTime / 1000);
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
            for (final ServerPlayer sub : getPackageManager().getCloseSubscribers())
            {
                if (getPermissions().hasPermission(sub, Action.CAN_KEEP_COLONY_ACTIVE_WHILE_AWAY))
                {
                    this.forceLoadTimer = getConfig().getServer().loadtime.get() * 20 * 60;
                    pendingChunks.addAll(pendingToUnloadChunks);
                    for (final long pending : pendingChunks)
                    {
                        checkChunkAndRegisterTicket(pending, world.getChunk(ChunkPos.getX(pending), ChunkPos.getZ(pending)));
                    }

                    pendingToUnloadChunks.clear();
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
                        if (world instanceof ServerLevel)
                        {
                            final ChunkPos pos = new ChunkPos(chunkX, chunkZ);
                            ((ServerChunkCache) world.getChunkSource()).removeRegionTicket(KEEP_LOADED_TYPE, pos, 2, pos);
                            pendingToUnloadChunks.add(chunkPos);
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
    private void checkChunkAndRegisterTicket(final long chunkPos, final LevelChunk chunk)
    {
        if (forceLoadTimer > 0 && world instanceof ServerLevel)
        {
            if (!ticketedChunks.contains(chunkPos) && buildingManager.keepChunkColonyLoaded(chunk))
            {
                ticketedChunks.add(chunkPos);
                ticketedChunksDirty = true;
                world.getChunkSource().addRegionTicket(KEEP_LOADED_TYPE, chunk.getPos(), 2, chunk.getPos(), true);
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
            day++;
            citizenManager.onWakeUp();
        }
        return false;
    }

    /**
     * Updates the pvping playeres.
     */
    public void updateAttackingPlayers()
    {
        final List<Player> visitors = new ArrayList<>(visitingPlayers);

        //Clean up visiting player.
        for (final Player player : visitors)
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
                    MessageUtils.format(COLONY_DEFENDED_SUCCESS_MESSAGE, player.getPlayer().getName()).sendTo(this).forManagers();
                }
            }
        }
    }

    @Override
    public PlayerTeam getTeam()
    {
        // This getter will create the team if it doesn't exist. Could do something different though in the future.
        return checkOrCreateTeam();
    }

    /**
     * Check or create the team.
     */
    private PlayerTeam checkOrCreateTeam()
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
    public void setColonyColor(final ChatFormatting colonyColor)
    {
        if (this.world != null)
        {
            checkOrCreateTeam();
            this.colonyTeamColor = colonyColor;
            this.world.getScoreboard().getPlayerTeam(getTeamName()).setColor(colonyColor);
            this.world.getScoreboard().getPlayerTeam(getTeamName()).setPlayerPrefix(Component.literal(colonyColor.toString()));
        }
        this.markDirty();
    }

    /**
     * Set up the colony flag patterns for use in decorations etc
     *
     * @param colonyFlag the list of pattern-color pairs
     */
    @Override
    public void setColonyFlag(BannerPatternLayers colonyFlag)
    {
        this.colonyFlag = colonyFlag;
        markDirty();
    }

    /**
     * Load a saved colony.
     *
     * @param compound The NBT compound containing the colony's data.
     * @param world    the world to load it for.
     * @param provider
     * @return loaded colony.
     */
    @Nullable
    public static Colony loadColony(@NotNull final CompoundTag compound, @Nullable final ServerLevel world, final HolderLookup.@NotNull Provider provider)
    {
        try
        {
            final int id = compound.getInt(TAG_ID);
            final String name = compound.getString(TAG_NAME);
            final BlockPos center = BlockPosUtil.read(compound, TAG_CENTER);
            @NotNull final Colony c = new Colony(id, name, world, center);
            c.dimensionId = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(compound.getString(TAG_DIMENSION)));

            c.read(compound, provider);

            return c;
        }
        catch (final Exception e)
        {
            Log.getLogger().warn("Something went wrong loading a colony, please report this to the administrators", e);
        }
        return null;
    }

    /**
     * Read colony from saved data.
     *
     * @param compound compound to read from.
     */
    public void read(@NotNull final CompoundTag compound, @NotNull final HolderLookup.Provider provider)
    {
        dimensionId = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(compound.getString(TAG_DIMENSION)));

        mercenaryLastUse = compound.getLong(TAG_MERCENARY_TIME);
        additionalChildTime = compound.getInt(TAG_CHILD_TIME);

        // Permissions
        permissions.loadPermissions(compound);

        citizenManager.read(provider, compound.getCompound(TAG_CITIZEN_MANAGER));
        visitorManager.read(provider, compound);
        buildingManager.read(provider, compound.getCompound(TAG_BUILDING_MANAGER));

        // Recalculate max after citizens and buildings are loaded.
        citizenManager.afterBuildingLoad();

        graveManager.read(compound.getCompound(TAG_GRAVE_MANAGER));

        eventManager.readFromNBT(provider, compound);
        statisticManager.readFromNBT(compound);

        questManager.deserializeNBT(provider, compound.getCompound(TAG_QUEST_MANAGER));
        eventDescManager.deserializeNBT(provider, compound.getCompound(NbtTagConstants.TAG_EVENT_DESC_MANAGER));

        if (compound.contains(TAG_RESEARCH))
        {
            researchManager.readFromNBT(provider, compound.getCompound(TAG_RESEARCH));
            // now that buildings, colonists, and research are loaded, check for new autoStartResearch.
            // this is mostly for backwards compatibility with older saves, so players do not have to manually start newly added autostart researches that they've unlocked before the update.
            researchManager.checkAutoStartResearch();
        }

        //  Workload
        workManager.read(compound.getCompound(TAG_WORK));

        wayPoints.clear();
        // Waypoints
        final ListTag wayPointTagList = compound.getList(TAG_WAYPOINT, Tag.TAG_COMPOUND);
        for (int i = 0; i < wayPointTagList.size(); ++i)
        {
            final CompoundTag blockAtPos = wayPointTagList.getCompound(i);
            final BlockPos pos = BlockPosUtil.read(blockAtPos, TAG_WAYPOINT);
            final BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), blockAtPos);
            wayPoints.put(pos, state);
        }

        // Free blocks
        final Set<Block> tempFreeBlocks = new HashSet<>();
        final ListTag freeBlockTagList = compound.getList(TAG_FREE_BLOCKS, Tag.TAG_STRING);
        for (int i = 0; i < freeBlockTagList.size(); ++i)
        {
            tempFreeBlocks.add(BuiltInRegistries.BLOCK.get(ResourceLocation.parse(freeBlockTagList.getString(i))));
        }
        freeBlocks = ImmutableSet.copyOf(tempFreeBlocks);

        final Set<BlockPos> tempFreePositions = new HashSet<>();
        // Free positions
        final ListTag freePositionTagList = compound.getList(TAG_FREE_POSITIONS, Tag.TAG_COMPOUND);
        for (int i = 0; i < freePositionTagList.size(); ++i)
        {
            final CompoundTag blockTag = freePositionTagList.getCompound(i);
            final BlockPos block = BlockPosUtil.read(blockTag, TAG_FREE_POSITIONS);
            tempFreePositions.add(block);
        }
        freePositions = ImmutableSet.copyOf(tempFreePositions);

        packageManager.setLastContactInHours(compound.getInt(TAG_ABANDONED));

        if (compound.contains(TAG_STYLE))
        {
            this.pack = BlueprintMapping.getStyleMapping(compound.getString(TAG_STYLE));
        }
        else
        {
            this.pack = compound.getString(TAG_PACK);
        }

        raidManager.read(compound);

        if (compound.contains(TAG_AUTO_DELETE))
        {
            this.canColonyBeAutoDeleted = compound.getBoolean(TAG_AUTO_DELETE);
        }
        else
        {
            this.canColonyBeAutoDeleted = true;
        }

        if (compound.contains(TAG_TEAM_COLOR))
        {
            // This read can occur before the world is non-null, due to Minecraft's order of operations for capabilities.
            // As a result, setColonyColor proper must wait until onWorldLoad fires.
            this.colonyTeamColor = ChatFormatting.values()[compound.getInt(TAG_TEAM_COLOR)];
        }

        if (compound.contains(TAG_FLAG_PATTERNS))
        {
            this.setColonyFlag(Utils.deserializeCodecMess(BannerPatternLayers.CODEC, provider, compound.get(TAG_FLAG_PATTERNS)));
        }

        getRequestManager().reset();
        if (compound.contains(TAG_REQUESTMANAGER))
        {
            getRequestManager().deserializeNBT(provider, compound.getCompound(TAG_REQUESTMANAGER));
        }
        this.lastOnlineTime = compound.getLong(TAG_LAST_ONLINE);
        if (compound.contains(TAG_COL_TEXT))
        {
            this.textureStyle = compound.getString(TAG_COL_TEXT);
        }
        if (compound.contains(TAG_COL_NAME_STYLE))
        {
            this.nameStyle = compound.getString(TAG_COL_NAME_STYLE);
        }

        if (compound.contains(BuildingModules.TOWNHALL_SETTINGS.key))
        {
            settingsModule.deserializeNBT(provider, compound.getCompound(BuildingModules.TOWNHALL_SETTINGS.key));
        }

        @NotNull final ListTag claimTagList = compound.getList(TAG_CLAIM_DATA, Tag.TAG_COMPOUND);
        for (int i = 0; i < claimTagList.size(); i++)
        {
            @NotNull final CompoundTag chunkCompound = claimTagList.getCompound(i);
            final ChunkClaimData chunkClaimData = new ChunkClaimData();
            chunkClaimData.deserializeNBT(provider, chunkCompound.getCompound(TAG_CHUNK_CLAIM));
            claimData.put(chunkCompound.getLong(TAG_CHUNK_POS), chunkClaimData);
        }
        IColonyManager.getInstance().addClaimData(this, claimData);

        this.day = compound.getInt(COLONY_DAY);
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
    public CompoundTag write(@NotNull final CompoundTag compound, @NotNull final HolderLookup.Provider provider)
    {
        compound.putInt(DATA_VERSION_TAG, DATA_VERSION);

        //  Core attributes
        compound.putInt(TAG_ID, id);
        compound.putString(TAG_DIMENSION, dimensionId.location().toString());

        //  Basic data
        compound.putString(TAG_NAME, name);
        BlockPosUtil.write(compound, TAG_CENTER, center);

        compound.putLong(TAG_MERCENARY_TIME, mercenaryLastUse);

        compound.putInt(TAG_CHILD_TIME, additionalChildTime);

        // Permissions
        permissions.savePermissions(compound);

        final CompoundTag buildingCompound = new CompoundTag();
        buildingManager.write(provider, buildingCompound);
        compound.put(TAG_BUILDING_MANAGER, buildingCompound);

        final CompoundTag citizenCompound = new CompoundTag();
        citizenManager.write(provider, citizenCompound);
        compound.put(TAG_CITIZEN_MANAGER, citizenCompound);

        visitorManager.write(provider, compound);

        final CompoundTag graveCompound = new CompoundTag();
        graveManager.write(graveCompound);
        compound.put(TAG_GRAVE_MANAGER, graveCompound);

        //  Workload
        @NotNull final CompoundTag workManagerCompound = new CompoundTag();
        workManager.write(workManagerCompound);
        compound.put(TAG_WORK, workManagerCompound);

        eventManager.writeToNBT(provider, compound);
        statisticManager.writeToNBT(compound);

        compound.put(TAG_QUEST_MANAGER, questManager.serializeNBT(provider));
        compound.put(NbtTagConstants.TAG_EVENT_DESC_MANAGER, eventDescManager.serializeNBT(provider));
        raidManager.write(compound);

        @NotNull final CompoundTag researchManagerCompound = new CompoundTag();
        researchManager.writeToNBT(provider, researchManagerCompound);
        compound.put(TAG_RESEARCH, researchManagerCompound);

        // Waypoints
        @NotNull final ListTag wayPointTagList = new ListTag();
        for (@NotNull final Map.Entry<BlockPos, BlockState> entry : wayPoints.entrySet())
        {
            @NotNull final CompoundTag wayPointCompound = new CompoundTag();
            BlockPosUtil.write(wayPointCompound, TAG_WAYPOINT, entry.getKey());
            wayPointCompound.put(TAG_BLOCK, NbtUtils.writeBlockState(entry.getValue()));
            wayPointTagList.add(wayPointCompound);
        }
        compound.put(TAG_WAYPOINT, wayPointTagList);

        // Free blocks
        @NotNull final ListTag freeBlocksTagList = new ListTag();
        for (@NotNull final Block block : freeBlocks)
        {
            freeBlocksTagList.add(StringTag.valueOf(BuiltInRegistries.BLOCK.getKey(block).toString()));
        }
        compound.put(TAG_FREE_BLOCKS, freeBlocksTagList);

        // Free positions
        @NotNull final ListTag freePositionsTagList = new ListTag();
        for (@NotNull final BlockPos pos : freePositions)
        {
            @NotNull final CompoundTag wayPointCompound = new CompoundTag();
            BlockPosUtil.write(wayPointCompound, TAG_FREE_POSITIONS, pos);
            freePositionsTagList.add(wayPointCompound);
        }
        compound.put(TAG_FREE_POSITIONS, freePositionsTagList);

        compound.putInt(TAG_ABANDONED, packageManager.getLastContactInHours());
        compound.put(TAG_REQUESTMANAGER, getRequestManager().serializeNBT(provider));
        compound.putString(TAG_PACK, pack);
        compound.putBoolean(TAG_AUTO_DELETE, canColonyBeAutoDeleted);
        compound.putInt(TAG_TEAM_COLOR, colonyTeamColor.ordinal());
        compound.put(TAG_FLAG_PATTERNS, Utils.serializeCodecMess(BannerPatternLayers.CODEC, provider, colonyFlag));
        compound.putLong(TAG_LAST_ONLINE, lastOnlineTime);
        compound.putString(TAG_COL_TEXT, textureStyle);
        compound.putString(TAG_COL_NAME_STYLE, nameStyle);
        compound.putInt(COLONY_DAY, day);

        final CompoundTag settings = new CompoundTag();
        settingsModule.serializeNBT(provider, settings);
        compound.put(BuildingModules.TOWNHALL_SETTINGS.key, settings);

        @NotNull final ListTag claimTagList = new ListTag();
        for (final Long2ObjectMap.Entry<ChunkClaimData> chunkClaimData : claimData.long2ObjectEntrySet())
        {
            @NotNull final CompoundTag chunkCompound = new CompoundTag();
            chunkCompound.put(TAG_CHUNK_CLAIM, chunkClaimData.getValue().serializeNBT(provider));
            chunkCompound.putLong(TAG_CHUNK_POS, chunkClaimData.getLongKey());
            claimTagList.add(chunkCompound);
        }
        compound.put(TAG_CLAIM_DATA, claimTagList);

        this.colonyTag = compound;

        isDirty = false;
        return compound;
    }

    /**
     * Returns the dimension ID.
     *
     * @return Dimension ID.
     */
    public ResourceKey<Level> getDimension()
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
    public void onWorldLoad(@NotNull final ServerLevel w)
    {
        if (w.dimension() == dimensionId)
        {
            this.world = w;
            // Register a new event handler
            if (eventHandler == null)
            {
                eventHandler = new ColonyPermissionEventHandler(this);
                questManager.onWorldLoad();
                NeoForge.EVENT_BUS.register(eventHandler);

                // Recovery for missing static colony claims
                final IChunkClaimData data = claimData.get(ChunkPos.asLong(getCenter()));
                if (data == null || !data.getStaticClaimColonies().contains(getID()))
                {
                    BackUpHelper.reclaimChunks(this);
                }
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
    public void onWorldUnload(@NotNull final Level w)
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
            NeoForge.EVENT_BUS.unregister(eventHandler);
        }
        world = null;
    }

    @Override
    public void onServerTick(@NotNull final ServerTickEvent.Pre event)
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
        return freePositions;
    }

    /**
     * Get a copy of the freeBlocks list.
     *
     * @return the list of free to interact blocks.
     */
    public Set<Block> getFreeBlocks()
    {
        return freeBlocks;
    }

    /**
     * Add a new free to interact position.
     *
     * @param pos position to add.
     */
    public void addFreePosition(@NotNull final BlockPos pos)
    {
        ImmutableSet.Builder<BlockPos> builder = ImmutableSet.builder();
        builder.addAll(freePositions);
        builder.add(pos);
        freePositions = builder.build();
        markDirty();
    }

    /**
     * Add a new free to interact block.
     *
     * @param block block to add.
     */
    public void addFreeBlock(@NotNull final Block block)
    {
        ImmutableSet.Builder<Block> builder = ImmutableSet.builder();
        builder.addAll(freeBlocks);
        builder.add(block);
        freeBlocks = builder.build();
        markDirty();
    }

    /**
     * Remove a free to interact position.
     *
     * @param pos position to remove.
     */
    public void removeFreePosition(@NotNull final BlockPos pos)
    {
        ImmutableSet.Builder<BlockPos> builder = ImmutableSet.builder();
        for (final BlockPos tempPos : freePositions)
        {
            if (!pos.equals(tempPos))
            {
                builder.add(tempPos);
            }
        }
        freePositions = builder.build();
        markDirty();
    }

    /**
     * Remove a free to interact block.
     *
     * @param block state to remove.
     */
    public void removeFreeBlock(@NotNull final Block block)
    {
        ImmutableSet.Builder<Block> builder = ImmutableSet.builder();
        for (final Block tempBlock : freeBlocks)
        {
            if (block != tempBlock)
            {
                builder.add(tempBlock);
            }
        }
        freeBlocks = builder.build();
        markDirty();
    }

    /**
     * Any per-world-tick logic should be performed here. NOTE: If the Colony's world isn't loaded, it won't have a world tick. Use onServerTick for logic that should _always_
     * run.
     *
     * @param event {@link net.neoforged.neoforge.event.tick.LevelTickEvent}
     */
    @Override
    public void onWorldTick(@NotNull final LevelTickEvent.Pre event)
    {
        if (event.getLevel() != getWorld())
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
    public static boolean shallUpdate(final Level world, final int averageTicks)
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
                            || (world.isEmptyBlock(entry.getKey().below()) && !BlockUtils.isAnySolid(entry.getValue())))
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
    public boolean isCoordInColony(@NotNull final Level w, @NotNull final BlockPos pos)
    {
        if (w.dimension() != this.dimensionId)
        {
            return false;
        }


        final LevelChunk chunk = w.getChunkAt(pos);
        return ColonyUtils.getOwningColony(chunk) == this.getID();
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
                    if (sum >= level)
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
    public ServerLevel getWorld()
    {
        return world;
    }

    @NotNull
    @Override
    public IRequestManager getRequestManager()
    {
        if (requestManager == null)
        {
            requestManager = new StandardRequestManager(this);
        }
        return requestManager;
    }

    /**
     * Marks the instance dirty.
     */
    public void markDirty()
    {
        packageManager.setDirty();
        isDirty = true;
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
    public List<Player> getMessagePlayerEntities()
    {
        List<Player> players = new ArrayList<>();

        for (ServerPlayer player : packageManager.getCloseSubscribers())
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
    public List<Player> getImportantMessageEntityPlayers()
    {
        final Set<Player> playerList = new HashSet<>(getMessagePlayerEntities());

        for (final ServerPlayer player : packageManager.getImportantColonyPlayers())
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
        return !settingsModule.getSetting(BuildingTownHall.AUTO_HIRING_MODE).getValue();
    }

    /**
     * Getter which checks if houses should be manually allocated.
     *
     * @return true of false.
     */
    public boolean isManualHousing()
    {
        return !settingsModule.getSetting(BuildingTownHall.AUTO_HOUSING_MODE).getValue();
    }

    /**
     * Getter which checks if houses should be manually allocated.
     *
     * @return true of false.
     */
    public boolean canMoveIn()
    {
        return settingsModule.getSetting(BuildingTownHall.MOVE_IN).getValue();
    }

    /**
     * Send the message of a removed workOrder to the client.
     *
     * @param orderId the workOrder to remove.
     */
    public void removeWorkOrderInView(final int orderId)
    {
        //  Inform Subscribers of removed workOrder
        new ColonyViewRemoveWorkOrderMessage(this, orderId).sendToPlayer(packageManager.getCloseSubscribers());
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
            happinessSum += citizen.getCitizenHappinessHandler().getHappiness(citizen.getColony(), citizen);
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
    public String getStructurePack()
    {
        return pack;
    }

    /**
     * Setter for the default pack of the colony.
     *
     * @param style the default string.
     */
    @Override
    public void setStructurePack(final String style)
    {
        this.pack = style;
        this.markDirty();
    }

    /**
     * Get the buildingmanager of the colony.
     *
     * @return the buildingManager.
     */
    @Override
    public IRegisteredStructureManager getBuildingManager()
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
    public IStatisticsManager getStatisticsManager()
    {
        return statisticManager;
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
     * Get all visiting players.
     *
     * @return the list.
     */
    public ImmutableList<Player> getVisitingPlayers()
    {
        return ImmutableList.copyOf(visitingPlayers);
    }

    @Override
    public void addVisitingPlayer(final Player player)
    {
        final Rank rank = getPermissions().getRank(player);
        if (!rank.isColonyManager() && !visitingPlayers.contains(player) && settingsModule.getSetting(BuildingTownHall.ENTER_LEAVE_MESSAGES).getValue())
        {
            visitingPlayers.add(player);
            if (!this.getImportantMessageEntityPlayers().contains(player))
            {
                MessageUtils.format(ENTERING_COLONY_MESSAGE, this.getName()).sendTo(player);
            }
            MessageUtils.format(ENTERING_COLONY_MESSAGE_NOTIFY, player.getName()).sendTo(this, true).forManagers();
        }
    }

    @Override
    public void removeVisitingPlayer(final Player player)
    {
        if (visitingPlayers.contains(player) && settingsModule.getSetting(BuildingTownHall.ENTER_LEAVE_MESSAGES).getValue())
        {
            visitingPlayers.remove(player);
            if (!this.getImportantMessageEntityPlayers().contains(player))
            {
                MessageUtils.format(LEAVING_COLONY_MESSAGE, this.getName()).sendTo(player);
            }
            MessageUtils.format(LEAVING_COLONY_MESSAGE_NOTIFY, player.getName()).sendTo(this, true).forManagers();
        }
    }

    /**
     * Get the NBT tag of the colony.
     *
     * @return the tag of it.
     */
    @Override
    public CompoundTag getColonyTag()
    {
        try
        {
            if (this.colonyTag == null || this.isDirty)
            {
                this.write(new CompoundTag(), world.registryAccess());
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
    public boolean isValidAttackingPlayer(final Player player)
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
    public void addGuardToAttackers(final AbstractEntityCitizen IEntityCitizen, final Player player)
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
                    MessageUtils.format(COLONY_ATTACK_GUARD_GROUP_SIZE_MESSAGE, attackingPlayer.getPlayer().getName(), attackingPlayer.getGuards().size())
                      .sendTo(this)
                      .forManagers();
                }
                return;
            }
        }

        for (final Player visitingPlayer : visitingPlayers)
        {
            if (visitingPlayer.equals(player))
            {
                final AttackingPlayer attackingPlayer = new AttackingPlayer(visitingPlayer);
                attackingPlayer.addGuard(IEntityCitizen);
                attackingPlayers.add(attackingPlayer);
                MessageUtils.format(COLONY_ATTACK_START_MESSAGE, visitingPlayer.getName()).sendTo(this).forManagers();
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
     * @return the ChatFormatting enum color.
     */
    @Override
    public ChatFormatting getTeamColonyColor()
    {
        return colonyTeamColor;
    }

    /**
     * Getter for the colony flag patterns
     *
     * @return the list of pattern-color pairs
     */
    @Override
    public BannerPatternLayers getColonyFlag()
    {
        return colonyFlag;
    }

    /**
     * Set the colony to be dirty.
     *
     * @param dirty if dirty.
     */
    public void setDirty(final boolean dirty)
    {
        this.isDirty = dirty;
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
    public void addLoadedChunk(final long chunkPos, final LevelChunk chunk)
    {
        if (world instanceof ServerLevel
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
        this.loadedChunks.put(chunkPos, chunkPos);
    }

    @Override
    public void removeLoadedChunk(final long chunkPos)
    {
        loadedChunks.remove(chunkPos);
        pendingToUnloadChunks.remove(chunkPos);
    }

    @Override
    public int getLoadedChunkCount()
    {
        return loadedChunks.size();
    }

    @Override
    public Set<Long> getLoadedChunks()
    {
        return loadedChunks.keySet();
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
        if (MineColonies.getConfig().getServer().holidayFeatures.get() &&
              (LocalDateTime.now().getDayOfMonth() >= 29 && LocalDateTime.now().getMonth() == Month.OCTOBER)
                 || (LocalDateTime.now().getDayOfMonth() <= 2 && LocalDateTime.now().getMonth() == Month.NOVEMBER))
        {
            return "nether";
        }

        return this.textureStyle;
    }

    @Override
    public void setNameStyle(final String style)
    {
        this.nameStyle = style;
        this.markDirty();
    }

    @Override
    public String getNameStyle()
    {
        return this.nameStyle;
    }

    @Override
    public CitizenNameFile getCitizenNameFile()
    {
        return CitizenNameListener.nameFileMap.getOrDefault(nameStyle, CitizenNameListener.nameFileMap.get("default"));
    }

    /**
     * Check if we need to update the view's chunk ticket info
     *
     * @return true if dirty.
     */
    public boolean isTicketedChunksDirty()
    {
        return ticketedChunksDirty;
    }

    @Override
    public int getDay()
    {
        return day;
    }

    @Override
    public IQuestManager getQuestManager()
    {
        return questManager;
    }

    @Override
    public ICitizen getCitizen(final int id)
    {
        return citizenManager.getCivilian(id);
    }

    /**
     * Gets the colonies settings
     * @return
     */
    public ISettingsModule getSettings()
    {
        return settingsModule;
    }

    /**
     * Get the claim data from the colony.
     * @return the claim data map.
     */
    public Long2ObjectMap<ChunkClaimData> getClaimData()
    {
        return claimData;
    }

    public IChunkClaimData claimNewChunk(final ChunkPos pos)
    {
        final ChunkClaimData chunkClaimData = new ChunkClaimData();
        claimData.put(pos.toLong(), chunkClaimData);
        IColonyManager.getInstance().addNewChunk(this, pos, chunkClaimData);
        this.markDirty();
        return chunkClaimData;
    }
}
