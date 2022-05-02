package com.minecolonies.coremod.colony;

import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.buildings.registry.IBuildingDataManager;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.buildings.workerbuildings.ITownHallView;
import com.minecolonies.api.colony.managers.interfaces.*;
import com.minecolonies.api.colony.permissions.IPermissions;
import com.minecolonies.api.colony.permissions.Player;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.workorders.IWorkManager;
import com.minecolonies.api.colony.workorders.IWorkOrderView;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.research.IResearchManager;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.coremod.colony.managers.ResearchManager;
import com.minecolonies.coremod.colony.permissions.PermissionsView;
import com.minecolonies.coremod.colony.requestsystem.management.manager.StandardRequestManager;
import com.minecolonies.coremod.colony.workorders.AbstractWorkOrder;
import com.minecolonies.coremod.network.messages.PermissionsMessage;
import com.minecolonies.coremod.network.messages.server.colony.ColonyFlagChangeMessage;
import com.minecolonies.coremod.network.messages.server.colony.TownHallRenameMessage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_BANNER_PATTERNS;
import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;

/**
 * Client side representation of the Colony.
 */
@SuppressWarnings("PMD.ExcessiveClassLength")
public final class ColonyView implements IColonyView
{
    /**
     * Max allowed CompoundNBT in bytes
     */
    private static final int REQUEST_MANAGER_MAX_SIZE = 700000;

    //  General Attributes
    private final int                            id;
    private final Map<Integer, IWorkOrderView>   workOrders  = new HashMap<>();
    //  Administration/permissions
    @NotNull
    private final PermissionsView                permissions = new PermissionsView();
    @NotNull
    private final Map<BlockPos, IBuildingView>   buildings   = new HashMap<>();
    //  Citizenry
    @NotNull
    private final Map<Integer, ICitizenDataView> citizens    = new HashMap<>();
    private       Map<Integer, IVisitorViewData> visitors    = new HashMap<>();
    private       String                         name        = "Unknown";
    private       RegistryKey<World>             dimensionId;

    /**
     * Colony team color.
     */
    private TextFormatting teamColonyColor = TextFormatting.WHITE;

    /**
     * The colony flag (set to plain white as default)
     */
    private ListNBT colonyFlag = new BannerPattern.Builder()
      .addPattern(BannerPattern.BASE, DyeColor.WHITE)
      .toListTag();

    private BlockPos center = BlockPos.ZERO;

    /**
     * Defines if workers are hired manually or automatically.
     */
    private boolean manualHiring = false;

    /**
     * Defines if workers are housed manually or automatically.
     */
    private boolean manualHousing = false;

    /**
     * Defines if citizens can move in or not.
     */
    private boolean moveIn = true;

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
    private World world;

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
    private final IResearchManager manager;

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
     * Base constructor for a colony.
     *
     * @param id The current id for the colony.
     */
    private ColonyView(final int id)
    {
        this.id = id;
        this.manager = new ResearchManager(this);
    }

    /**
     * Create a ColonyView given a UUID and CompoundNBT.
     *
     * @param id Id of the colony view.
     * @return the new colony view.
     */
    @NotNull
    public static ColonyView createFromNetwork(final int id)
    {
        return new ColonyView(id);
    }

    /**
     * Populate an NBT compound for a network packet representing a ColonyView.
     *
     * @param colony            Colony to write data about.
     * @param buf               {@link PacketBuffer} to write data in.
     * @param hasNewSubscribers true if there is a new subscription.
     */
    public static void serializeNetworkData(@NotNull Colony colony, @NotNull PacketBuffer buf, boolean hasNewSubscribers)
    {
        //  General Attributes
        buf.writeUtf(colony.getName());
        buf.writeUtf(colony.getDimension().location().toString());
        buf.writeBlockPos(colony.getCenter());
        buf.writeBoolean(colony.isManualHiring());
        //  Citizenry
        buf.writeInt(colony.getCitizenManager().getMaxCitizens());
        buf.writeInt(colony.getCitizenManager().getPotentialMaxCitizens());

        final Set<Block> freeBlocks = colony.getFreeBlocks();
        final Set<BlockPos> freePos = colony.getFreePositions();
        final Map<BlockPos, BlockState> waypoints = colony.getWayPoints();

        buf.writeInt(freeBlocks.size());
        for (final Block block : freeBlocks)
        {
            buf.writeUtf(block.getRegistryName().toString());
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
        buf.writeBoolean(colony.isManualHousing());
        buf.writeBoolean(colony.canMoveIn());
        buf.writeUtf(colony.getTextureStyleId());

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

        CompoundNBT flagNBT = new CompoundNBT();
        flagNBT.put(TAG_BANNER_PATTERNS, colony.getColonyFlag());
        buf.writeNbt(flagNBT);

        buf.writeBoolean(colony.getProgressManager().isPrintingProgress());

        buf.writeLong(colony.getMercenaryUseTime());

        buf.writeUtf(colony.getStyle());
        buf.writeBoolean(colony.getRaiderManager().isRaided());
        buf.writeBoolean(colony.getRaiderManager().areSpiesEnabled());
        // ToDo: rework ally system
        final List<IColony> allies = new ArrayList<>();
        for (final Player player : colony.getPermissions().getFilteredPlayers(Rank::isColonyManager))
        {
            final IColony col = IColonyManager.getInstance().getIColonyByOwner(colony.getWorld(), player.getID());
            if (col != null)
            {
                for (final Player owner : colony.getPermissions().getPlayersByRank(colony.getPermissions().getRankOwner()))
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
        for (final Player player : colony.getPermissions().getFilteredPlayers(Rank::isHostile))
        {
            final IColony col = IColonyManager.getInstance().getIColonyByOwner(colony.getWorld(), player.getID());
            if (col != null)
            {
                for (final Player owner : colony.getPermissions().getPlayersByRank(colony.getPermissions().getRankOwner()))
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

        final CompoundNBT treeTag = new CompoundNBT();
        colony.getResearchManager().writeToNBT(treeTag);
        buf.writeNbt(treeTag);

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

        final CompoundNBT graveTag = new CompoundNBT();
        colony.getGraveManager().write(graveTag);
        buf.writeNbt(graveTag);     // this could be more efficient, but it should usually be short anyway
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
    public RegistryKey<World> getDimension()
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
        return manualHiring;
    }

    /**
     * Sets if workers should be hired manually.
     *
     * @param manualHiring true if manually.
     */
    @Override
    public void setManualHiring(final boolean manualHiring)
    {
        this.manualHiring = manualHiring;
    }

    @Override
    public CompoundNBT write(final CompoundNBT colonyCompound)
    {
        return new CompoundNBT();
    }

    @Override
    public void read(final CompoundNBT compound)
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
        return manualHousing;
    }

    /**
     * Sets if houses should be assigned manually.
     *
     * @param manualHousing true if manually.
     */
    @Override
    public void setManualHousing(final boolean manualHousing)
    {
        this.manualHousing = manualHousing;
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
        return moveIn;
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
    public void addLoadedChunk(final long chunkPos, final Chunk chunk)
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
     * Sets if citizens can move in.
     *
     * @param newMoveIn true if citizens can move in.
     */
    @Override
    public void setMoveIn(final boolean newMoveIn) {this.moveIn = newMoveIn;}

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
     * Returns a map of players in the colony. Key is the UUID, value is {@link Player}
     *
     * @return Map of UUID's and {@link Player}
     */
    @Override
    @NotNull
    public Map<UUID, Player> getPlayers()
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
     * @param buf               {@link PacketBuffer} to read from.
     * @param isNewSubscription Whether this is a new subscription of not.
     * @return null == no response.
     */
    @Override
    @Nullable
    public IMessage handleColonyViewMessage(@NotNull final PacketBuffer buf, @NotNull final World world, final boolean isNewSubscription)
    {
        this.world = world;
        //  General Attributes
        name = buf.readUtf(32767);
        dimensionId = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(buf.readUtf(32767)));
        center = buf.readBlockPos();
        manualHiring = buf.readBoolean();
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

        final int blockListSize = buf.readInt();
        for (int i = 0; i < blockListSize; i++)
        {
            freeBlocks.add(ForgeRegistries.BLOCKS.getValue(new ResourceLocation((buf.readUtf(32767)))));
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
        this.manualHousing = buf.readBoolean();
        this.moveIn = buf.readBoolean();
        this.textureStyle = buf.readUtf(32767);

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

        this.teamColonyColor = TextFormatting.values()[buf.readInt()];
        this.colonyFlag = buf.readNbt().getList(TAG_BANNER_PATTERNS, Constants.TAG_COMPOUND);

        this.printProgress = buf.readBoolean();

        this.mercenaryLastUseTime = buf.readLong();

        this.style = buf.readUtf(32767);

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
              RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(buf.readUtf(32767)))));
        }

        final int noOfFeuds = buf.readInt();
        for (int i = 0; i < noOfFeuds; i++)
        {
            feuds.add(new CompactColonyReference(buf.readUtf(32767),
              buf.readBlockPos(),
              buf.readInt(),
              false,
              RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(buf.readUtf(32767)))));
        }

        this.manager.readFromNBT(buf.readNbt());

        final int ticketChunkCount = buf.readInt();
        if (ticketChunkCount != -1)
        {
            ticketedChunks = new HashSet<>(ticketChunkCount);
            for (int i = 0; i < ticketChunkCount; i++)
            {
                ticketedChunks.add(buf.readLong());
            }
        }

        this.graveManager.read(buf.readNbt());

        return null;
    }

    /**
     * Update permissions.
     *
     * @param buf buffer containing permissions.
     * @return null == no response
     */
    @Override
    @Nullable
    public IMessage handlePermissionsViewMessage(@NotNull final PacketBuffer buf)
    {
        permissions.deserialize(buf);
        return null;
    }

    /**
     * Update a ColonyView's workOrders given a network data ColonyView update packet. This uses a full-replacement - workOrders do not get updated and are instead overwritten.
     *
     * @param buf Network data.
     * @return null == no response.
     */
    @Override
    @Nullable
    public IMessage handleColonyViewWorkOrderMessage(final PacketBuffer buf)
    {
        workOrders.clear();
        final int amount = buf.readInt();
        for (int i = 0; i < amount; i++)
        {
            @Nullable final IWorkOrderView workOrder = AbstractWorkOrder.createWorkOrderView(buf);
            if (workOrder != null)
            {
                workOrders.put(workOrder.getId(), workOrder);
            }
        }

        return null;
    }

    /**
     * Update a ColonyView's citizens given a network data ColonyView update packet. This uses a full-replacement - citizens do not get updated and are instead overwritten.
     *
     * @param id  ID of the citizen.
     * @param buf Network data.
     * @return null == no response.
     */
    @Override
    @Nullable
    public IMessage handleColonyViewCitizensMessage(final int id, final PacketBuffer buf)
    {
        final ICitizenDataView citizen = ICitizenDataManager.getInstance().createFromNetworkData(id, buf, this);
        if (citizen != null)
        {
            citizens.put(citizen.getId(), citizen);
        }

        return null;
    }

    @Override
    public void handleColonyViewVisitorMessage(final PacketBuffer visitorBuf, final boolean refresh)
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
    @Nullable
    public IMessage handleColonyViewRemoveCitizenMessage(final int citizen)
    {
        citizens.remove(citizen);
        return null;
    }

    /**
     * Remove a building from the ColonyView.
     *
     * @param buildingId location of the building.
     * @return null == no response.
     */
    @Override
    @Nullable
    public IMessage handleColonyViewRemoveBuildingMessage(final BlockPos buildingId)
    {
        final IBuildingView building = buildings.remove(buildingId);
        if (townHall == building)
        {
            townHall = null;
        }
        return null;
    }

    /**
     * Remove a workOrder from the ColonyView.
     *
     * @param workOrderId id of the workOrder.
     * @return null == no response
     */
    @Override
    @Nullable
    public IMessage handleColonyViewRemoveWorkOrderMessage(final int workOrderId)
    {
        workOrders.remove(workOrderId);
        return null;
    }

    /**
     * Update a ColonyView's buildings given a network data ColonyView update packet. This uses a full-replacement - buildings do not get updated and are instead overwritten.
     *
     * @param buildingId location of the building.
     * @param buf        buffer containing ColonyBuilding information.
     * @return null == no response.
     */
    @Override
    @Nullable
    public IMessage handleColonyBuildingViewMessage(final BlockPos buildingId, @NotNull final PacketBuffer buf)
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

        return null;
    }

    /**
     * Update a players permissions.
     *
     * @param player player username.
     */
    @Override
    public void addPlayer(final String player)
    {
        Network.getNetwork().sendToServer(new PermissionsMessage.AddPlayer(this, player));
    }

    /**
     * Remove player from colony permissions.
     *
     * @param player the UUID of the player to remove.
     */
    @Override
    public void removePlayer(final UUID player)
    {
        Network.getNetwork().sendToServer(new PermissionsMessage.RemovePlayer(this, player));
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
    public TextFormatting getTeamColonyColor()
    {
        return teamColonyColor;
    }

    /**
     * Getter for the pattern list of the colony flag
     *
     * @return the ListNBT of flag (banner) patterns
     */
    @Override
    public ListNBT getColonyFlag() {return colonyFlag;}

    /**
     * Sets the name of the view.
     *
     * @param name Name of the view.
     */
    @Override
    public void setName(final String name)
    {
        this.name = name;
        Network.getNetwork().sendToServer(new TownHallRenameMessage(this, name));
    }

    @NotNull
    @Override
    public IPermissions getPermissions()
    {
        return permissions;
    }

    @Override
    public boolean isCoordInColony(@NotNull final World w, @NotNull final BlockPos pos)
    {
        final Chunk chunk = w.getChunkAt(pos);
        final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null).orElseGet(null);
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
    public ScorePlayerTeam getTeam()
    {
        return world.getScoreboard().getPlayerTeam(getTeamName());
    }

    @Override
    public int getLastContactInHours()
    {
        return lastContactInHours;
    }

    @Override
    public World getWorld()
    {
        return world;
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
    public void removeVisitingPlayer(final PlayerEntity player)
    {
        /*
         * Intentionally left empty.
         */
    }

    @NotNull
    @Override
    public List<PlayerEntity> getMessagePlayerEntities()
    {
        return new ArrayList<>();
    }

    @Override
    public void addVisitingPlayer(final PlayerEntity player)
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void onWorldLoad(@NotNull final World w)
    {

    }

    @Override
    public void onWorldUnload(@NotNull final World w)
    {

    }

    @Override
    public void onServerTick(@NotNull final TickEvent.ServerTickEvent event)
    {

    }

    @NotNull
    @Override
    public IWorkManager getWorkManager()
    {
        return null;
    }

    @Override
    public void onWorldTick(@NotNull final TickEvent.WorldTickEvent event)
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

    /**
     * Get if progress should be printed.
     *
     * @return true if so.
     */
    @Override
    public boolean isPrintingProgress()
    {
        return printProgress;
    }

    @Override
    public boolean isRemote()
    {
        return true;
    }

    @Override
    public CompoundNBT getColonyTag()
    {
        return null;
    }

    @Override
    public boolean isColonyUnderAttack()
    {
        return false;
    }

    @Override
    public boolean isValidAttackingPlayer(final PlayerEntity entity)
    {
        return false;
    }

    @Override
    public void addGuardToAttackers(final AbstractEntityCitizen entityCitizen, final PlayerEntity followPlayer)
    {

    }

    @Override
    public void setColonyColor(final TextFormatting color)
    {

    }

    @Override
    public void setColonyFlag(ListNBT colonyFlag)
    {
        this.colonyFlag = colonyFlag;
        Network.getNetwork().sendToServer(new ColonyFlagChangeMessage(this, colonyFlag));
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
    public List<PlayerEntity> getImportantMessageEntityPlayers()
    {
        return new ArrayList<>();
    }

    /**
     * Get the style of the colony.
     *
     * @return the current default style.
     */
    @Override
    public String getStyle()
    {
        return style;
    }

    @Override
    public void setStyle(final String style)
    {
        // Not needed.
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
    public IProgressManager getProgressManager()
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
        mercenaryLastUseTime = world.getGameTime();
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
        return manager;
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
}
