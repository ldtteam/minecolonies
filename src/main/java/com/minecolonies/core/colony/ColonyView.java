package com.minecolonies.core.colony;

import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.buildings.registry.IBuildingDataManager;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.buildings.workerbuildings.ITownHallView;
import com.minecolonies.api.colony.claim.ChunkClaimData;
import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.colony.managers.interfaces.*;
import com.minecolonies.api.colony.permissions.ColonyPlayer;
import com.minecolonies.api.colony.permissions.IPermissions;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.workorders.IWorkManager;
import com.minecolonies.api.colony.workorders.IWorkOrderView;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.quests.IQuestManager;
import com.minecolonies.api.research.IResearchManager;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ColonyUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.Utils;
import com.minecolonies.core.client.render.worldevent.ColonyBlueprintRenderer;
import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.core.colony.managers.ResearchManager;
import com.minecolonies.core.colony.managers.StatisticsManager;
import com.minecolonies.core.colony.permissions.PermissionsView;
import com.minecolonies.core.colony.requestsystem.management.manager.StandardRequestManager;
import com.minecolonies.core.colony.workorders.AbstractWorkOrder;
import com.minecolonies.core.datalistener.CitizenNameListener;
import com.minecolonies.core.network.messages.PermissionsMessage;
import com.minecolonies.core.network.messages.server.colony.ColonyFlagChangeMessage;
import com.minecolonies.core.network.messages.server.colony.TownHallRenameMessage;
import com.minecolonies.core.quests.QuestManager;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

/**
 * Client side representation of the Colony.
 */
@SuppressWarnings("PMD.ExcessiveClassLength")
public final class ColonyView implements IColonyView
{
    /**
     * Max allowed CompoundTag in bytes
     */
    private static final int REQUEST_MANAGER_MAX_SIZE = 700000;

    //  General Attributes
    private final int                            id;
    private final Map<Integer, IWorkOrderView>   workOrders  = new HashMap<>();
    private final Map<Integer, BlockPos>         workOrderClaimCache = new HashMap<>();
    private int                                  workOrderCachedCount;
    //  Administration/permissions
    @NotNull
    private final PermissionsView                permissions = new PermissionsView();
    @NotNull
    private final Map<BlockPos, IBuildingView>   buildings   = new HashMap<>();
    @NotNull
    private final Set<IField>                    fields      = new HashSet<>();
    //  Citizenry
    @NotNull
    private final Map<Integer, ICitizenDataView> citizens    = new HashMap<>();
    private       Map<Integer, IVisitorViewData> visitors    = new HashMap<>();
    private       String                         name        = "Unknown";
    private       ResourceKey<Level>                            dimensionId;

    /**
     * Colony team color.
     */
    private ChatFormatting teamColonyColor = ChatFormatting.WHITE;

    /**
     * The colony flag (set to plain white as default)
     */
    private BannerPatternLayers colonyFlag;


    private BlockPos center = BlockPos.ZERO;

    //  Buildings
    @Nullable
    private ITownHallView townHall;

    /**
     * The max citizen count.
     */
    private int citizenCount = 0;

    /**
     * The max citizen count considering guard towers.
     */
    private int citizenCountWithEmptyGuardTowers = 0;

    /**
     * Check if the colony has a warehouse.
     */
    private boolean hasColonyWarehouse;

    /**
     * Last barbarian spawnpoints.
     */
    private final List<BlockPos> lastSpawnPoints = new ArrayList<>();

    /**
     * The Positions which players can freely interact.
     */
    private final Set<BlockPos> freePositions = new HashSet<>();

    /**
     * The Blocks which players can freely interact with.
     */
    private final Set<Block> freeBlocks = new HashSet<>();

    /**
     * The Set of waypoints.
     */
    private final Map<BlockPos, BlockState> wayPoints = new HashMap<>();

    /**
     * The overall happiness of the colony.
     */
    private double overallHappiness = 5;

    /**
     * The hours the colony is without contact with its players.
     */
    private int lastContactInHours = 0;

    /**
     * The request manager on the colony view side.
     */
    private IRequestManager requestManager;

    /**
     * Wether the colony is raided
     */
    private boolean isUnderRaid;

    /**
     * The world.
     */
    private final ResourceKey<Level> world;

    /**
     * Print progress.
     */
    private boolean printProgress;

    /**
     * The last use time of the mercenaries.
     */
    private long mercenaryLastUseTime = 0;

    /**
     * The default style.
     */
    private String style = "";

    /**
     * The list of allies.
     */
    private List<CompactColonyReference> allies;

    /**
     * The list of feuds.
     */
    private List<CompactColonyReference> feuds;

    /**
     * The research effects of the colony.
     */
    private final IResearchManager researchManager;

    /**
     * Whether spies are active and highlight enemy positions.
     */
    private boolean   spiesEnabled;
    private Set<Long> ticketedChunks = new HashSet<>();

    /**
     * The texture style of the colony citizens.
     */
    private String textureStyle;

    /**
     * The grave manager on the client side.
     */
    private final IGraveManager graveManager = new GraveManagerView();

    /**
     * The list of name files.
     */
    private List<String> nameFileIds = new ArrayList<>();

    /**
     * The name style of the colony citizens.
     */
    private String nameStyle;

    /**
     * Statistic manager associated to the view.
     */
    private IStatisticsManager statisticManager = new StatisticsManager();

    /**
     * Client side quest manager.
     */
    private IQuestManager questManager;

    /**
     * Day in the colony.
     */
    private int day;

    /**
     * Base constructor for a colony.
     *
     * @param id The current id for the colony.
     */
    private ColonyView(final int id, final ResourceKey<Level> dim)
    {
        this.id = id;
        this.world = dim;
        this.researchManager = new ResearchManager(this);
        this.questManager = new QuestManager(this);
    }

    /**
     * Create a ColonyView given a UUID and CompoundTag.
     *
     * @param id Id of the colony view.
     * @return the new colony view.
     */
    @NotNull
    public static ColonyView createFromNetwork(final int id, final ResourceKey<Level> dim)
    {
        return new ColonyView(id, dim);
    }

    /**
     * Populate an NBT compound for a network packet representing a ColonyView.
     *
     * @param colony            Colony to write data about.
     * @param buf               {@link RegistryFriendlyByteBuf} to write data in.
     * @param hasNewSubscribers true if there is a new subscription.
     */
    public static void serializeNetworkData(@NotNull Colony colony, @NotNull RegistryFriendlyByteBuf buf, boolean hasNewSubscribers)
    {
        //  General Attributes
        buf.writeUtf(colony.getName());
        buf.writeUtf(colony.getDimension().location().toString());
        buf.writeBlockPos(colony.getCenter());
        //  Citizenry
        buf.writeInt(colony.getCitizenManager().getMaxCitizens());
        buf.writeInt(colony.getCitizenManager().getPotentialMaxCitizens());

        final Set<Block> freeBlocks = colony.getFreeBlocks();
        final Set<BlockPos> freePos = colony.getFreePositions();
        final Map<BlockPos, BlockState> waypoints = colony.getWayPoints();

        buf.writeInt(freeBlocks.size());
        for (final Block block : freeBlocks)
        {
            buf.writeUtf(BuiltInRegistries.BLOCK.getKey(block).toString());
        }

        buf.writeInt(freePos.size());
        for (final BlockPos block : freePos)
        {
            buf.writeBlockPos(block);
        }
        buf.writeDouble(colony.getOverallHappiness());
        buf.writeBoolean(colony.hasWarehouse());

        buf.writeInt(waypoints.size());
        for (final Map.Entry<BlockPos, BlockState> block : waypoints.entrySet())
        {
            buf.writeBlockPos(block.getKey());
            buf.writeInt(Block.getId(block.getValue()));
        }

        buf.writeInt(colony.getLastContactInHours());
        buf.writeUtf(colony.getTextureStyleId());

        buf.writeUtf(colony.getNameStyle());
        buf.writeInt(CitizenNameListener.nameFileMap.size());
        for (final String nameFileIndex : CitizenNameListener.nameFileMap.keySet())
        {
            buf.writeUtf(nameFileIndex);
        }
        //  Citizens are sent as a separate packet

        if (colony.getRequestManager() != null && (colony.getRequestManager().isDirty() || hasNewSubscribers))
        {
            final int preSize = buf.writerIndex();
            buf.writeBoolean(true);
            colony.getRequestManager().serialize(StandardFactoryController.getInstance(), buf);
            final int postSize = buf.writerIndex();
            if ((postSize - preSize) >= ColonyView.REQUEST_MANAGER_MAX_SIZE)
            {
                Log.getLogger().warn("Colony " + colony.getID() + " has a very big memory imprint, this could be a memory leak, please contact the mod author!");
            }
        }
        else
        {
            buf.writeBoolean(false);
        }

        buf.writeInt(colony.getRaiderManager().getLastSpawnPoints().size());
        for (final BlockPos block : colony.getRaiderManager().getLastSpawnPoints())
        {
            buf.writeBlockPos(block);
        }

        buf.writeInt(colony.getTeamColonyColor().ordinal());
        Utils.serializeCodecMess(BannerPatternLayers.STREAM_CODEC, buf, colony.getColonyFlag());
        buf.writeLong(colony.getMercenaryUseTime());

        buf.writeUtf(colony.getStructurePack());
        buf.writeBoolean(colony.getRaiderManager().isRaided());
        buf.writeBoolean(colony.getRaiderManager().areSpiesEnabled());
        // ToDo: rework ally system
        final List<IColony> allies = new ArrayList<>();
        for (final ColonyPlayer player : colony.getPermissions().getFilteredPlayers(Rank::isColonyManager))
        {
            final IColony col = IColonyManager.getInstance().getIColonyByOwner(colony.getWorld(), player.getID());
            if (col != null)
            {
                for (final ColonyPlayer owner : colony.getPermissions().getPlayersByRank(colony.getPermissions().getRankOwner()))
                {
                    if (col.getPermissions().getRank(owner.getID()).isColonyManager() && col.getID() != colony.getID())
                    {
                        allies.add(col);
                    }
                }
            }
        }

        buf.writeInt(allies.size());
        for (final IColony col : allies)
        {
            buf.writeUtf(col.getName());
            buf.writeBlockPos(col.getCenter());
            buf.writeInt(col.getID());
            buf.writeBoolean(col.hasTownHall());
            buf.writeUtf(col.getDimension().location().toString());
        }

        final List<IColony> feuds = new ArrayList<>();
        for (final ColonyPlayer player : colony.getPermissions().getFilteredPlayers(Rank::isHostile))
        {
            final IColony col = IColonyManager.getInstance().getIColonyByOwner(colony.getWorld(), player.getID());
            if (col != null)
            {
                for (final ColonyPlayer owner : colony.getPermissions().getPlayersByRank(colony.getPermissions().getRankOwner()))
                {
                    if (col.getPermissions().getRank(owner.getID()).isHostile())
                    {
                        feuds.add(col);
                    }
                }
            }
        }

        buf.writeInt(feuds.size());
        for (final IColony col : feuds)
        {
            buf.writeUtf(col.getName());
            buf.writeBlockPos(col.getCenter());
            buf.writeInt(col.getID());
            buf.writeUtf(col.getDimension().location().toString());
        }

        if (hasNewSubscribers || colony.isTicketedChunksDirty())
        {
            buf.writeInt(colony.getTicketedChunks().size());
            for (final long pos : colony.getTicketedChunks())
            {
                buf.writeLong(pos);
            }
        }
        else
        {
            buf.writeInt(-1);
        }

        final Long2ObjectMap<ChunkClaimData> colonyClaimData = colony.getClaimData();
        buf.writeInt(colonyClaimData.size());
        for (final Long2ObjectMap.Entry<ChunkClaimData> entry : colonyClaimData.long2ObjectEntrySet())
        {
            buf.writeLong(entry.getLongKey());
            buf.writeNbt(entry.getValue().serializeNBT(buf.registryAccess()));
        }

        final CompoundTag graveTag = new CompoundTag();
        colony.getGraveManager().write(graveTag);
        buf.writeNbt(graveTag);     // this could be more efficient, but it should usually be short anyway
        colony.getStatisticsManager().serialize(buf, hasNewSubscribers);
        buf.writeNbt(colony.getQuestManager().serializeNBT(buf.registryAccess()));
        buf.writeInt(colony.getDay());
    }

    /**
     * Get a copy of the freePositions list.
     *
     * @return the list of free to interact positions.
     */
    @Override
    public List<BlockPos> getFreePositions()
    {
        return new ArrayList<>(freePositions);
    }

    /**
     * Get a copy of the freeBlocks list.
     *
     * @return the list of free to interact blocks.
     */
    @Override
    public List<Block> getFreeBlocks()
    {
        return new ArrayList<>(freeBlocks);
    }

    /**
     * Add a new free to interact position.
     *
     * @param pos position to add.
     */
    @Override
    public void addFreePosition(@NotNull final BlockPos pos)
    {
        freePositions.add(pos);
    }

    /**
     * Add a new free to interact block.
     *
     * @param block block to add.
     */
    @Override
    public void addFreeBlock(@NotNull final Block block)
    {
        freeBlocks.add(block);
    }

    /**
     * Remove a free to interact position.
     *
     * @param pos position to remove.
     */
    @Override
    public void removeFreePosition(@NotNull final BlockPos pos)
    {
        freePositions.remove(pos);
    }

    /**
     * Remove a free to interact block.
     *
     * @param block state to remove.
     */
    @Override
    public void removeFreeBlock(@NotNull final Block block)
    {
        freeBlocks.remove(block);
    }

    @Override
    public void setCanBeAutoDeleted(final boolean canBeDeleted)
    {

    }

    /**
     * Returns the dimension ID of the view.
     *
     * @return dimension ID of the view.
     */
    @Override
    public ResourceKey<Level> getDimension()
    {
        return dimensionId;
    }

    /**
     * Getter for the manual hiring or not.
     *
     * @return the boolean true or false.
     */
    @Override
    public boolean isManualHiring()
    {
        return townHall != null && !townHall.getModuleView(BuildingModules.TOWNHALL_SETTINGS).getSetting(BuildingTownHall.AUTO_HIRING_MODE).getValue();
    }

    @Override
    public CompoundTag write(final CompoundTag colonyCompound, final HolderLookup.Provider provider)
    {
        return new CompoundTag();
    }

    @Override
    public void read(final CompoundTag compound, final HolderLookup.Provider provider)
    {
        //Noop
    }

    /**
     * Getter for the manual housing or not.
     *
     * @return the boolean true or false.
     */
    @Override
    public boolean isManualHousing()
    {
        return townHall != null && !townHall.getModuleView(BuildingModules.TOWNHALL_SETTINGS).getSetting(BuildingTownHall.AUTO_HOUSING_MODE).getValue();
    }

    @Override
    public void addWayPoint(final BlockPos pos, final BlockState newWayPointState)
    {

    }

    @Override
    public boolean isValidAttackingGuard(final AbstractEntityCitizen entity)
    {
        return false;
    }

    /**
     * Getter for letting citizens move in or not.
     *
     * @return the boolean true or false.
     */
    @Override
    public boolean canMoveIn()
    {
        return townHall != null && !townHall.getModuleView(BuildingModules.TOWNHALL_SETTINGS).getSetting(BuildingTownHall.MOVE_IN).getValue();
    }

    /**
     * Tries to use a given amount of additional growth-time for childs.
     *
     * @param amount amount to use
     * @return true if used up.
     */
    @Override
    public boolean useAdditionalChildTime(final int amount)
    {
        return false;
    }

    @Override
    public void updateHasChilds()
    {
    }

    @Override
    public void addLoadedChunk(final long chunkPos, final LevelChunk chunk)
    {

    }

    @Override
    public void removeLoadedChunk(final long chunkPos)
    {

    }

    @Override
    public int getLoadedChunkCount()
    {
        return 0;
    }

    @Override
    public Set<Long> getLoadedChunks()
    {
        return null;
    }

    @Override
    public ColonyState getState()
    {
        return null;
    }

    @Override
    public boolean isActive()
    {
        return true;
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
    }

    @Override
    public String getTextureStyleId()
    {
        return this.textureStyle;
    }

    /**
     * Get the town hall View for this ColonyView.
     *
     * @return {@link BuildingTownHall.View} of the colony.
     */
    @Override
    @Nullable
    public ITownHallView getTownHall()
    {
        return townHall;
    }

    /**
     * Get a AbstractBuilding.View for a given building (by coordinate-id) using raw x,y,z.
     *
     * @param x x-coordinate.
     * @param y y-coordinate.
     * @param z z-coordinate.
     * @return {@link AbstractBuildingView} of a AbstractBuilding for the given Coordinates/ID, or null.
     */
    @Override
    public IBuildingView getBuilding(final int x, final int y, final int z)
    {
        return getBuilding(new BlockPos(x, y, z));
    }

    /**
     * Get a AbstractBuilding.View for a given building (by coordinate-id) using ChunkCoordinates.
     *
     * @param buildingId Coordinates/ID of the AbstractBuilding.
     * @return {@link AbstractBuildingView} of a AbstractBuilding for the given Coordinates/ID, or null.
     */
    @Override
    public IBuildingView getBuilding(final BlockPos buildingId)
    {
        return buildings.get(buildingId);
    }

    /**
     * Returns a map of players in the colony. Key is the UUID, value is {@link ColonyPlayer}
     *
     * @return Map of UUID's and {@link ColonyPlayer}
     */
    @Override
    @NotNull
    public Map<UUID, ColonyPlayer> getPlayers()
    {
        return permissions.getPlayers();
    }

    /**
     * Returns the maximum amount of citizen in the colony.
     *
     * @return maximum amount of citizens.
     */
    @Override
    public int getCitizenCount()
    {
        return citizenCount;
    }

    @Override
    public int getCitizenCountLimit()
    {
        return citizenCountWithEmptyGuardTowers;
    }

    /**
     * Getter for the citizens map.
     *
     * @return a unmodifiable Map of the citizen.
     */
    @Override
    public Map<Integer, ICitizenDataView> getCitizens()
    {
        return Collections.unmodifiableMap(citizens);
    }

    /**
     * Getter for the workOrders.
     *
     * @return a unmodifiable Collection of the workOrders.
     */
    @Override
    public Collection<IWorkOrderView> getWorkOrders()
    {
        return Collections.unmodifiableCollection(workOrders.values());
    }

    @Override
    public IWorkOrderView getWorkOrder(final int id)
    {
        return workOrders.get(id);
    }

    /**
     * Gets the CitizenDataView for a citizen id.
     *
     * @param id the citizen id.
     * @return CitizenDataView for the citizen.
     */
    @Override
    public ICitizenDataView getCitizen(final int id)
    {
        if (id > 0)
        {
            return citizens.get(id);
        }
        else
        {
            return visitors.get(id);
        }
    }

    /**
     * Populate a ColonyView from the network data.
     *
     * @param buf               {@link RegistryFriendlyByteBuf} to read from.
     * @param isNewSubscription Whether this is a new subscription of not.
     * @return null == no response.
     */
    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleColonyViewMessage(@NotNull final RegistryFriendlyByteBuf buf, final boolean isNewSubscription)
    {
        this.colonyFlag = new BannerPatternLayers.Builder().add(Utils.getRegistryValue(BannerPatterns.BASE, this.getWorld()), DyeColor.WHITE).build();

        //  General Attributes
        name = buf.readUtf(32767);
        dimensionId = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(buf.readUtf(32767)));
        center = buf.readBlockPos();
        //  Citizenry
        citizenCount = buf.readInt();
        citizenCountWithEmptyGuardTowers = buf.readInt();

        if (isNewSubscription)
        {
            citizens.clear();
            townHall = null;
            buildings.clear();
        }

        freePositions.clear();
        freeBlocks.clear();
        wayPoints.clear();
        lastSpawnPoints.clear();
        nameFileIds.clear();

        final int blockListSize = buf.readInt();
        for (int i = 0; i < blockListSize; i++)
        {
            freeBlocks.add(BuiltInRegistries.BLOCK.get(ResourceLocation.parse((buf.readUtf(32767)))));
        }

        final int posListSize = buf.readInt();
        for (int i = 0; i < posListSize; i++)
        {
            freePositions.add(buf.readBlockPos());
        }
        this.overallHappiness = buf.readDouble();
        this.hasColonyWarehouse = buf.readBoolean();

        final int wayPointListSize = buf.readInt();
        for (int i = 0; i < wayPointListSize; i++)
        {
            wayPoints.put(buf.readBlockPos(), Block.stateById(buf.readInt()));
        }
        this.lastContactInHours = buf.readInt();
        this.textureStyle = buf.readUtf(32767);

        this.nameStyle = buf.readUtf(32767);
        final int nameFileIdSize = buf.readInt();
        for (int i = 0; i < nameFileIdSize; i++)
        {
            nameFileIds.add(buf.readUtf(32767));
        }

        if (buf.readBoolean())
        {
            this.requestManager = new StandardRequestManager(this);
            this.requestManager.deserialize(StandardFactoryController.getInstance(), buf);
        }

        final int barbSpawnListSize = buf.readInt();
        for (int i = 0; i < barbSpawnListSize; i++)
        {
            lastSpawnPoints.add(buf.readBlockPos());
        }
        Collections.reverse(lastSpawnPoints);

        this.teamColonyColor = ChatFormatting.values()[buf.readInt()];
        this.colonyFlag =  Utils.deserializeCodecMess(BannerPatternLayers.STREAM_CODEC, buf);
        this.mercenaryLastUseTime = buf.readLong();

        this.style = buf.readUtf(32767);
        if (isNewSubscription
              && StructurePacks.hasPack(this.style)
              && RenderingCache.getOrCreateBlueprintPreviewData("blueprint").getBlueprint() == null
              && Minecraft.getInstance().player != null
              && this.isCoordInColony(getWorld(), Minecraft.getInstance().player.blockPosition())
        )
        {
            StructurePacks.selectedPack = StructurePacks.getStructurePack(this.style);
        }

        this.isUnderRaid = buf.readBoolean();
        this.spiesEnabled = buf.readBoolean();

        this.allies = new ArrayList<>();
        this.feuds = new ArrayList<>();

        final int noOfAllies = buf.readInt();
        for (int i = 0; i < noOfAllies; i++)
        {
            allies.add(new CompactColonyReference(buf.readUtf(32767),
              buf.readBlockPos(),
              buf.readInt(),
              buf.readBoolean(),
              ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(buf.readUtf(32767)))));
        }

        final int noOfFeuds = buf.readInt();
        for (int i = 0; i < noOfFeuds; i++)
        {
            feuds.add(new CompactColonyReference(buf.readUtf(32767),
              buf.readBlockPos(),
              buf.readInt(),
              false,
              ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(buf.readUtf(32767)))));
        }

        final int ticketChunkCount = buf.readInt();
        if (ticketChunkCount != -1)
        {
            ticketedChunks = new HashSet<>(ticketChunkCount);
            for (int i = 0; i < ticketChunkCount; i++)
            {
                ticketedChunks.add(buf.readLong());
            }
        }

        final Long2ObjectMap<ChunkClaimData> colonyClaimData = new Long2ObjectOpenHashMap<>();
        int size = buf.readInt();
        for (int i = 0; i < size; i++)
        {
            final ChunkClaimData chunkClaimData = new ChunkClaimData();
            final long pos = buf.readLong();
            chunkClaimData.deserializeNBT(buf.registryAccess(), buf.readNbt());
            colonyClaimData.put(pos, chunkClaimData);
        }

        if (Minecraft.getInstance().getSingleplayerServer() == null)
        {
            IColonyManager.getInstance().addClaimData(this, colonyClaimData);
        }

        this.graveManager.read(buf.readNbt());
        this.statisticManager.deserialize(buf);
        this.questManager.deserializeNBT(buf.registryAccess(), buf.readNbt());
        this.day = buf.readInt();
    }

    /**
     * Update permissions.
     *
     * @param buf buffer containing permissions.
     * @return null == no response
     */
    @Override
    public void handlePermissionsViewMessage(@NotNull final RegistryFriendlyByteBuf buf)
    {
        permissions.deserialize(buf);
    }

    /**
     * Update a ColonyView's workOrders given a network data ColonyView update packet. This uses a full-replacement - workOrders do not get updated and are instead overwritten.
     *
     * @param buf Network data.
     * @return null == no response.
     */
    @Override
    public void handleColonyViewWorkOrderMessage(final RegistryFriendlyByteBuf buf)
    {
        boolean claimsChanged = false;

        workOrders.clear();
        final int amount = buf.readInt();
        for (int i = 0; i < amount; i++)
        {
            @Nullable final IWorkOrderView workOrder = AbstractWorkOrder.createWorkOrderView(buf);
            if (workOrder != null)
            {
                workOrders.put(workOrder.getId(), workOrder);

                final BlockPos oldClaimedBy = workOrderClaimCache.put(workOrder.getId(), workOrder.getClaimedBy());
                claimsChanged |= !Objects.equals(workOrder.getClaimedBy(), oldClaimedBy);
            }
        }

        if (claimsChanged || workOrders.size() != workOrderCachedCount)
        {
            workOrderCachedCount = workOrders.size();
            ColonyBlueprintRenderer.invalidateCache();
        }
    }

    /**
     * Update a ColonyView's citizens given a network data ColonyView update packet. The ICitizenManager makes sure to update citizens instead of replacing them.
     *
     * @param id  ID of the citizen.
     * @param buf Network data.
     * @return null == no response.
     */
    @Override
    public void handleColonyViewCitizensMessage(final int id, final RegistryFriendlyByteBuf buf)
    {
        final ICitizenDataView citizen = ICitizenDataManager.getInstance().createFromNetworkData(id, buf, this);
        if (citizen != null)
        {
            citizens.put(citizen.getId(), citizen);
        }
    }

    @Override
    public void handleColonyViewVisitorMessage(final RegistryFriendlyByteBuf visitorBuf, final boolean refresh)
    {
        final Map<Integer, IVisitorViewData> visitorCache = new HashMap<>(visitors);

        if (refresh)
        {
            visitors.clear();
        }

        int i = visitorBuf.readInt();
        for (int j = 0; j < i; j++)
        {
            final int id = visitorBuf.readInt();
            final IVisitorViewData dataView;
            if (visitorCache.containsKey(id))
            {
                dataView = visitorCache.get(id);
            }
            else
            {
                dataView = new VisitorDataView(id, this);
            }
            dataView.deserialize(visitorBuf);
            visitors.put(dataView.getId(), dataView);
        }
    }

    /**
     * Remove a citizen from the ColonyView.
     *
     * @param citizen citizen ID.
     * @return null == no response.
     */
    @Override
    public void handleColonyViewRemoveCitizenMessage(final int citizen)
    {
        citizens.remove(citizen);
    }

    /**
     * Remove a building from the ColonyView.
     *
     * @param buildingId location of the building.
     * @return null == no response.
     */
    @Override
    public void handleColonyViewRemoveBuildingMessage(final BlockPos buildingId)
    {
        final IBuildingView building = buildings.remove(buildingId);
        if (townHall == building)
        {
            townHall = null;
        }
    }

    /**
     * Remove a workOrder from the ColonyView.
     *
     * @param workOrderId id of the workOrder.
     * @return null == no response
     */
    @Override
    public void handleColonyViewRemoveWorkOrderMessage(final int workOrderId)
    {
        workOrders.remove(workOrderId);
    }

    /**
     * Update a ColonyView's buildings given a network data ColonyView update packet. This uses a full-replacement - buildings do not get updated and are instead overwritten.
     *
     * @param buildingId location of the building.
     * @param buf        buffer containing ColonyBuilding information.
     * @return null == no response.
     */
    @Override
    public void handleColonyBuildingViewMessage(final BlockPos buildingId, @NotNull final RegistryFriendlyByteBuf buf)
    {
        if (buildings.containsKey(buildingId))
        {
            //Read the string first to set up the buffer.
            buf.readUtf(32767);
            buildings.get(buildingId).deserialize(buf);
        }
        else
        {
            @Nullable final IBuildingView building = IBuildingDataManager.getInstance().createViewFrom(this, buildingId, buf);
            if (building != null)
            {
                buildings.put(building.getID(), building);

                if (building instanceof BuildingTownHall.View)
                {
                    townHall = (ITownHallView) building;
                }
            }
        }
    }

    @Override
    public void handleColonyViewResearchManagerUpdate(@NotNull final HolderLookup.Provider provider, final CompoundTag compoundTag)
    {
        this.researchManager.readFromNBT(provider, compoundTag);
    }

    @Override
    public void handleColonyFieldViewUpdateMessage(final Set<IField> fields)
    {
        this.fields.clear();
        this.fields.addAll(fields);
    }

    @Override
    public @NotNull List<IField> getFields(final Predicate<IField> matcher)
    {
        return fields.stream()
                 .filter(matcher)
                 .toList();
    }

    @Override
    public @Nullable IField getField(final Predicate<IField> matcher)
    {
        return getFields(matcher).stream()
                 .findFirst()
                 .orElse(null);
    }

    /**
     * Update a players permissions.
     *
     * @param player player username.
     */
    @Override
    public void addPlayer(final String player)
    {
        new PermissionsMessage.AddPlayer(this, player).sendToServer();
    }

    /**
     * Remove player from colony permissions.
     *
     * @param player the UUID of the player to remove.
     */
    @Override
    public void removePlayer(final UUID player)
    {
        new PermissionsMessage.RemovePlayer(this, player).sendToServer();
    }

    /**
     * Getter for the overall happiness.
     *
     * @return the happiness, a double.
     */
    @Override
    public double getOverallHappiness()
    {
        return overallHappiness;
    }

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
     * Getter for the team colony color.
     *
     * @return the color.
     */
    @Override
    public ChatFormatting getTeamColonyColor()
    {
        return teamColonyColor;
    }

    /**
     * Getter for the pattern list of the colony flag
     *
     * @return the ListNBT of flag (banner) patterns
     */
    @Override
    public BannerPatternLayers getColonyFlag() { return colonyFlag; }

    /**
     * Sets the name of the view.
     *
     * @param name Name of the view.
     */
    @Override
    public void setName(final String name)
    {
        this.name = name;
        new TownHallRenameMessage(this, name).sendToServer();
    }

    @NotNull
    @Override
    public IPermissions getPermissions()
    {
        return permissions;
    }

    @Override
    public boolean isCoordInColony(@NotNull final Level w, @NotNull final BlockPos pos)
    {
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
        return townHall != null;
    }

    /**
     * Returns the ID of the view.
     *
     * @return ID of the view.
     */
    @Override
    public int getID()
    {
        return id;
    }

    @Override
    public boolean hasWarehouse()
    {
        return hasColonyWarehouse;
    }

    @Override
    public boolean hasBuilding(final String name, final int level, final boolean singleBuilding)
    {
        int sum = 0;
        for (final IBuildingView building : buildings.values())
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
    public boolean isDay()
    {
        return false;
    }

    @Override
    public PlayerTeam getTeam()
    {
        return getWorld().getScoreboard().getPlayerTeam(getTeamName());
    }

    @Override
    public int getLastContactInHours()
    {
        return lastContactInHours;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Level getWorld()
    {
        final Level level = Minecraft.getInstance().level;
        if (level == null || !level.dimension().equals(world))
        {
            throw new IllegalStateException("Cannot get colony view level");
        }
        return level;
    }

    @NotNull
    @Override
    public IRequestManager getRequestManager()
    {
        //No request system on the client side.
        //At least for now.
        return requestManager;
    }

    @Override
    public void markDirty()
    {
        /*
         * Nothing to do here.
         */
    }

    @Override
    public boolean canBeAutoDeleted()
    {
        return false;
    }

    @Nullable
    @Override
    public IRequester getRequesterBuildingForPosition(@NotNull final BlockPos pos)
    {
        return getBuilding(pos);
    }

    @Override
    public void removeVisitingPlayer(final Player player)
    {
        /*
         * Intentionally left empty.
         */
    }

    @NotNull
    @Override
    public List<Player> getMessagePlayerEntities()
    {
        return new ArrayList<>();
    }

    @Override
    public void addVisitingPlayer(final Player player)
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void onWorldLoad(final ServerLevel w)
    {

    }

    @Override
    public void onWorldUnload(@NotNull final Level w)
    {

    }

    @Override
    public void onServerTick(@NotNull final ServerTickEvent.Pre event)
    {

    }

    @NotNull
    @Override
    public IWorkManager getWorkManager()
    {
        return null;
    }

    @Override
    public void onWorldTick(@NotNull final LevelTickEvent.Pre event)
    {

    }

    @Override
    public Map<BlockPos, BlockState> getWayPoints()
    {
        return wayPoints;
    }

    /**
     * Get a list of all barb spawn positions in the colony view.
     *
     * @return a copy of the list.
     */
    @Override
    public List<BlockPos> getLastSpawnPoints()
    {
        return new ArrayList<>(lastSpawnPoints);
    }

    @Override
    public boolean isRemote()
    {
        return true;
    }

    @Override
    public CompoundTag getColonyTag()
    {
        return null;
    }

    @Override
    public boolean isColonyUnderAttack()
    {
        return false;
    }

    @Override
    public boolean isValidAttackingPlayer(final Player entity)
    {
        return false;
    }

    @Override
    public void addGuardToAttackers(final AbstractEntityCitizen entityCitizen, final Player followPlayer)
    {

    }

    @Override
    public void setColonyColor(final ChatFormatting color)
    {

    }

    @Override
    public void setColonyFlag(BannerPatternLayers colonyFlag)
    {
        this.colonyFlag = colonyFlag;
        new ColonyFlagChangeMessage(this, colonyFlag).sendToServer();
    }

    /**
     * Get a list of all buildings.
     *
     * @return a list of their views.
     */
    @Override
    public List<IBuildingView> getBuildings()
    {
        return new ArrayList<>(buildings.values());
    }

    @NotNull
    @Override
    public List<Player> getImportantMessageEntityPlayers()
    {
        return new ArrayList<>();
    }

    /**
     * Get the style of the colony.
     *
     * @return the current default style.
     */
    @Override
    public String getStructurePack()
    {
        return style;
    }

    @Override
    public void setStructurePack(final String style)
    {
        this.style = style;
    }

    @Override
    public IRegisteredStructureManager getBuildingManager()
    {
        return null;
    }

    @Override
    public IGraveManager getGraveManager()
    {
        return this.graveManager;
    }

    @Override
    public ICitizenManager getCitizenManager()
    {
        return null;
    }

    @Override
    public IVisitorManager getVisitorManager()
    {
        return null;
    }

    @Override
    public IRaiderManager getRaiderManager()
    {
        return null;
    }

    @Override
    public IEventManager getEventManager()
    {
        return null;
    }

    @Override
    public IReproductionManager getReproductionManager()
    {
        return null;
    }

    @Override
    public IEventDescriptionManager getEventDescriptionManager()
    {
        return null;
    }

    @Override
    public IColonyPackageManager getPackageManager()
    {
        return null;
    }

    @Override
    public boolean isRaiding()
    {
        return this.isUnderRaid;
    }

    @Override
    public long getMercenaryUseTime()
    {
        return mercenaryLastUseTime;
    }

    @Override
    public void usedMercenaries()
    {
        mercenaryLastUseTime = getWorld().getGameTime();
    }

    @Override
    public List<CompactColonyReference> getAllies()
    {
        return allies;
    }

    @Override
    public List<CompactColonyReference> getFeuds()
    {
        return feuds;
    }

    @Override
    public IResearchManager getResearchManager()
    {
        return researchManager;
    }

    @Override
    public boolean areSpiesEnabled()
    {
        return spiesEnabled;
    }

    @Override
    public ICitizenDataView getVisitor(final int citizenId)
    {
        return visitors.get(citizenId);
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
    public List<String> getNameFileIds()
    {
        return this.nameFileIds;
    }

    @Override
    public CitizenNameFile getCitizenNameFile()
    {
        return CitizenNameListener.nameFileMap.getOrDefault(nameStyle, CitizenNameListener.nameFileMap.get("default"));
    }

    @Override
    public IStatisticsManager getStatisticsManager()
    {
        return statisticManager;
    }

    @Override
    public int getDay()
    {
        return this.day;
    }

    @Override
    public IQuestManager getQuestManager()
    {
        return this.questManager;
    }
}