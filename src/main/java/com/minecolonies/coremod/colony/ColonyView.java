package com.minecolonies.coremod.colony;

import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.registry.IBuildingDataManager;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.buildings.workerbuildings.ITownHallView;
import com.minecolonies.api.colony.managers.interfaces.*;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.permissions.IPermissions;
import com.minecolonies.api.colony.permissions.Player;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.workorders.IWorkManager;
import com.minecolonies.api.colony.workorders.WorkOrderView;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.coremod.colony.permissions.PermissionsView;
import com.minecolonies.coremod.colony.requestsystem.management.manager.StandardRequestManager;
import com.minecolonies.coremod.colony.workorders.AbstractWorkOrder;
import com.minecolonies.coremod.network.messages.PermissionsMessage;
import com.minecolonies.coremod.network.messages.TownHallRenameMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;

/**
 * Client side representation of the Colony.
 */
@SuppressWarnings("PMD.ExcessiveClassLength")
public final class ColonyView implements IColonyView
{
    /**
     * Max allowed NBTTagCompound in bytes 
     */
    private static final int MAX_BYTES_NBTCOMPOUND = (int) 1e6;

    //  General Attributes
    private final int                            id;
    private final Map<Integer, WorkOrderView>    workOrders    = new HashMap<>();
    //  Administration/permissions
    @NotNull
    private final PermissionsView                permissions   = new PermissionsView();
    @NotNull
    private final Map<BlockPos, IBuildingView>   buildings     = new HashMap<>();
    //  Citizenry
    @NotNull
    private final Map<Integer, ICitizenDataView> citizens      = new HashMap<>();
    /**
     * Datas about the happiness of a colony
     */
    private final HappinessData                  happinessData = new HappinessData();
    private       String                         name          = "Unknown";
    private       int                            dimensionId;

    /**
     * Colony team color.
     */
    private TextFormatting teamColonyColor = TextFormatting.WHITE;
    private BlockPos       center          = BlockPos.ORIGIN;

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
    private int           citizenCount = 0;

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
    private final Map<BlockPos, IBlockState> wayPoints = new HashMap<>();

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
     * The number of raiders in the horde.
     */
    private int horde;

    /**
     * The world.
     */
    private World world;

    /**
     * Print progress.
     */
    private boolean printProgress;

    /**
     * The cost of citizens bought
     */
    private int boughtCitizenCost;

    /**
     * The last use time of the mercenaries.
     */
    private long mercenaryLastUseTime = 0;

    /**
     * The default style.
     */
    private String style = "";

    /**
     * Base constructor for a colony.
     *
     * @param id The current id for the colony.
     */
    private ColonyView(final int id)
    {
        this.id = id;
    }

    /**
     * Create a ColonyView given a UUID and NBTTagCompound.
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
     * @param buf               {@link ByteBuf} to write data in.
     * @param hasNewSubscribers true if there is a new subscription.
     */
    public static void serializeNetworkData(@NotNull Colony colony, @NotNull ByteBuf buf, boolean hasNewSubscribers)
    {
        //  General Attributes
        ByteBufUtils.writeUTF8String(buf, colony.getName());
        buf.writeInt(colony.getDimension());
        BlockPosUtil.writeToByteBuf(buf, colony.getCenter());
        buf.writeBoolean(colony.isManualHiring());
        //  Citizenry
        buf.writeInt(colony.getCitizenManager().getMaxCitizens());

        final Set<Block> freeBlocks = colony.getFreeBlocks();
        final Set<BlockPos> freePos = colony.getFreePositions();
        final Map<BlockPos, IBlockState> waypoints = colony.getWayPoints();

        buf.writeInt(freeBlocks.size());
        for (final Block block : freeBlocks)
        {
            ByteBufUtils.writeUTF8String(buf, block.getRegistryName().toString());
        }

        buf.writeInt(freePos.size());
        for (final BlockPos block : freePos)
        {
            BlockPosUtil.writeToByteBuf(buf, block);
        }
        buf.writeDouble(colony.getOverallHappiness());
        buf.writeBoolean(colony.hasWarehouse());

        buf.writeInt(waypoints.size());
        for (final Map.Entry<BlockPos, IBlockState> block : waypoints.entrySet())
        {
            BlockPosUtil.writeToByteBuf(buf, block.getKey());
            ByteBufUtils.writeTag(buf, NBTUtil.writeBlockState(new NBTTagCompound(), block.getValue()));
        }

        buf.writeInt(colony.getLastContactInHours());
        buf.writeBoolean(colony.isManualHousing());
        buf.writeBoolean(colony.canMoveIn());
        //  Citizens are sent as a separate packet

        if (colony.getRequestManager() != null && (colony.getRequestManager().isDirty() || hasNewSubscribers))
        {
            final int preSize = buf.writerIndex();
            final int preState = buf.readerIndex();
            buf.writeBoolean(true);
            ByteBufUtils.writeTag(buf, colony.getRequestManager().serializeNBT());
            final int postSize = buf.writerIndex();
            if ((postSize - preSize) >= ColonyView.MAX_BYTES_NBTCOMPOUND)
            {
                colony.getRequestManager().reset();
                buf.setIndex(preState, preSize);
                buf.writeBoolean(true);
                ByteBufUtils.writeTag(buf, colony.getRequestManager().serializeNBT());
            }
        }
        else
        {
            buf.writeBoolean(false);
        }

        buf.writeInt(colony.getRaiderManager().getLastSpawnPoints().size());
        for (final BlockPos block : colony.getRaiderManager().getLastSpawnPoints())
        {
            BlockPosUtil.writeToByteBuf(buf, block);
        }

        buf.writeInt(colony.getTeamColonyColor().ordinal());

        buf.writeBoolean(colony.getProgressManager().isPrintingProgress());

        buf.writeInt(colony.getBoughtCitizenCost());
        buf.writeLong(colony.getMercenaryUseTime());

        ByteBufUtils.writeUTF8String(buf, colony.getStyle());
        buf.writeInt(colony.getRaiderManager().getHorde(colony.getWorld().getMinecraftServer().getWorld(colony.getDimension())).size());
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
    public int getDimension()
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
    public NBTTagCompound writeToNBT(final NBTTagCompound colonyCompound)
    {
        return new NBTTagCompound();
    }

    @Override
    public void readFromNBT(final NBTTagCompound compound)
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
    public void addWayPoint(final BlockPos pos, final IBlockState newWayPointState)
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
    public void addLoadedChunk(final long chunkPos)
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

    /**
     * Sets if citizens can move in.
     *
     * @param newMoveIn true if citizens can move in.
     */
    @Override
    public void setMoveIn(final boolean newMoveIn) { this.moveIn = newMoveIn; }

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
     * Get a AbstractBuilding.View for a given building (by coordinate-id) using
     * raw x,y,z.
     *
     * @param x x-coordinate.
     * @param y y-coordinate.
     * @param z z-coordinate.
     * @return {@link AbstractBuildingView} of a AbstractBuilding for the given
     * Coordinates/ID, or null.
     */
    @Override
    public IBuildingView getBuilding(final int x, final int y, final int z)
    {
        return getBuilding(new BlockPos(x, y, z));
    }

    /**
     * Get a AbstractBuilding.View for a given building (by coordinate-id) using
     * ChunkCoordinates.
     *
     * @param buildingId Coordinates/ID of the AbstractBuilding.
     * @return {@link AbstractBuildingView} of a AbstractBuilding for the given
     * Coordinates/ID, or null.
     */
    @Override
    public IBuildingView getBuilding(final BlockPos buildingId)
    {
        return buildings.get(buildingId);
    }

    /**
     * Returns a map of players in the colony. Key is the UUID, value is {@link
     * Player}
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
     * Sets a specific permission to a rank. If the permission wasn't already
     * set, it sends a message to the server.
     *
     * @param rank   Rank to get the permission.
     * @param action Permission to get.
     */
    @Override
    public void setPermission(final Rank rank, @NotNull final Action action)
    {
        if (permissions.setPermission(rank, action))
        {
            MineColonies.getNetwork().sendToServer(new PermissionsMessage.Permission(this, PermissionsMessage.MessageType.SET_PERMISSION, rank, action));
        }
    }

    /**
     * removes a specific permission to a rank. If the permission was set, it
     * sends a message to the server.
     *
     * @param rank   Rank to remove permission from.
     * @param action Action to remove permission of.
     */
    @Override
    public void removePermission(final Rank rank, @NotNull final Action action)
    {
        if (permissions.removePermission(rank, action))
        {
            MineColonies.getNetwork().sendToServer(new PermissionsMessage.Permission(this, PermissionsMessage.MessageType.REMOVE_PERMISSION, rank, action));
        }
    }

    /**
     * Toggles a specific permission to a rank. Sends a message to the server.
     *
     * @param rank   Rank to toggle permission of.
     * @param action Action to toggle permission of.
     */
    @Override
    public void togglePermission(final Rank rank, @NotNull final Action action)
    {
        permissions.togglePermission(rank, action);
        MineColonies.getNetwork().sendToServer(new PermissionsMessage.Permission(this, PermissionsMessage.MessageType.TOGGLE_PERMISSION, rank, action));
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
    public Collection<WorkOrderView> getWorkOrders()
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
        return citizens.get(id);
    }

    /**
     * Populate a ColonyView from the network data.
     *
     * @param buf               {@link ByteBuf} to read from.
     * @param isNewSubscription Whether this is a new subscription of not.
     * @return null == no response.
     */
    @Override
    @Nullable
    public IMessage handleColonyViewMessage(@NotNull final ByteBuf buf, @NotNull final World world, final boolean isNewSubscription)
    {
        this.world = world;
        //  General Attributes
        name = ByteBufUtils.readUTF8String(buf);
        dimensionId = buf.readInt();
        center = BlockPosUtil.readFromByteBuf(buf);
        manualHiring = buf.readBoolean();
        //  Citizenry
        citizenCount = buf.readInt();

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
            freeBlocks.add(Block.getBlockFromName(ByteBufUtils.readUTF8String(buf)));
        }

        final int posListSize = buf.readInt();
        for (int i = 0; i < posListSize; i++)
        {
            freePositions.add(BlockPosUtil.readFromByteBuf(buf));
        }
        this.overallHappiness = buf.readDouble();
        this.hasColonyWarehouse = buf.readBoolean();

        final int wayPointListSize = buf.readInt();
        for (int i = 0; i < wayPointListSize; i++)
        {
            wayPoints.put(BlockPosUtil.readFromByteBuf(buf), NBTUtil.readBlockState(ByteBufUtils.readTag(buf)));
        }
        this.lastContactInHours = buf.readInt();
        this.manualHousing = buf.readBoolean();
        this.moveIn = buf.readBoolean();

        if (buf.readBoolean())
        {
            final NBTTagCompound compound = ByteBufUtils.readTag(buf);
            this.requestManager = new StandardRequestManager(this);
            this.requestManager.deserializeNBT(compound);
        }

        final int barbSpawnListSize = buf.readInt();
        for (int i = 0; i < barbSpawnListSize; i++)
        {
            lastSpawnPoints.add(BlockPosUtil.readFromByteBuf(buf));
        }
        Collections.reverse(lastSpawnPoints);

        this.teamColonyColor = TextFormatting.values()[buf.readInt()];

        this.printProgress = buf.readBoolean();

        this.boughtCitizenCost = buf.readInt();

        this.mercenaryLastUseTime = buf.readLong();

        this.style = ByteBufUtils.readUTF8String(buf);
        this.horde = buf.readInt();
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
    public IMessage handlePermissionsViewMessage(@NotNull final ByteBuf buf)
    {
        permissions.deserialize(buf);
        return null;
    }

    /**
     * Update a ColonyView's workOrders given a network data ColonyView update
     * packet. This uses a full-replacement - workOrders do not get updated and
     * are instead overwritten.
     *
     * @param buf Network data.
     * @return null == no response.
     */
    @Override
    @Nullable
    public IMessage handleColonyViewWorkOrderMessage(final ByteBuf buf)
    {
        @Nullable final WorkOrderView workOrder = AbstractWorkOrder.createWorkOrderView(buf);
        if (workOrder != null)
        {
            workOrders.put(workOrder.getId(), workOrder);
        }

        return null;
    }

    /**
     * Update a ColonyView's citizens given a network data ColonyView update
     * packet. This uses a full-replacement - citizens do not get updated and
     * are instead overwritten.
     *
     * @param id  ID of the citizen.
     * @param buf Network data.
     * @return null == no response.
     */
    @Override
    @Nullable
    public IMessage handleColonyViewCitizensMessage(final int id, final ByteBuf buf)
    {
        final ICitizenDataView citizen = ICitizenDataManager.getInstance().createFromNetworkData(id, buf);
        if (citizen != null)
        {
            citizens.put(citizen.getId(), citizen);
        }

        return null;
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
     * Update a ColonyView's buildings given a network data ColonyView update
     * packet. This uses a full-replacement - buildings do not get updated and
     * are instead overwritten.
     *
     * @param buildingId location of the building.
     * @param buf        buffer containing ColonyBuilding information.
     * @return null == no response.
     */
    @Override
    @Nullable
    public IMessage handleColonyBuildingViewMessage(final BlockPos buildingId, @NotNull final ByteBuf buf)
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

        return null;
    }

    /**
     * Update the happiness values for a colony
     * @param happinessData The new values for happiness
     * @return null == no response.
     */
    @Override
    public IMessage handleHappinessDataMessage(final HappinessData happinessData)
    {
        this.happinessData.setValues(happinessData);
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
        MineColonies.getNetwork().sendToServer(new PermissionsMessage.AddPlayer(this, player));
    }

    /**
     * Remove player from colony permissions.
     *
     * @param player the UUID of the player to remove.
     */
    @Override
    public void removePlayer(final UUID player)
    {
        MineColonies.getNetwork().sendToServer(new PermissionsMessage.RemovePlayer(this, player));
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
     * @return the color.
     */
    @Override
    public TextFormatting getTeamColonyColor()
    {
        return teamColonyColor;
    }

    /**
     * Sets the name of the view.
     *
     * @param name Name of the view.
     */
    @Override
    public void setName(final String name)
    {
        this.name = name;
        MineColonies.getNetwork().sendToServer(new TownHallRenameMessage(this, name));
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
    public int getLastContactInHours()
    {
        return lastContactInHours;
    }

    @Override
    public World getWorld()
    {
        return world;
    }

    @Nullable
    @Override
    public IRequestManager getRequestManager()
    {
        //No request system on the client side.
        //At least for now.
        return requestManager;
    }

    @Override
    public boolean hasWillRaidTonight()
    {
        return false;
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

    @Override
    public boolean isCanHaveBarbEvents()
    {
        return false;
    }

    @Override
    public boolean isHasRaidBeenCalculated()
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
    public void removeVisitingPlayer(final EntityPlayer player)
    {
        /*
         * Intentionally left empty.
         */
    }

    @NotNull
    @Override
    public Set<EntityPlayer> getMessageEntityPlayers()
    {
        return new HashSet<>();
    }

    @Override
    public void onBuildingUpgradeComplete(@Nullable final IBuilding building, final int level)
    {

    }

    @Override
    public void addVisitingPlayer(final EntityPlayer player)
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

    @Override
    public void onWorldTick(@NotNull final TickEvent.WorldTickEvent event)
    {

    }

    @Override
    public boolean areAllColonyChunksLoaded()
    {
        return false;
    }

    @Override
    public Map<BlockPos, IBlockState> getWayPoints()
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
    public NBTTagCompound getColonyTag()
    {
        return null;
    }

    @Override
    public int getNightsSinceLastRaid()
    {
        return 0;
    }

    @Override
    public void setNightsSinceLastRaid(final int nights)
    {

    }

    @Override
    public boolean isNeedToMourn()
    {
        return false;
    }

    @Override
    public void setNeedToMourn(final boolean needToMourn, final String name)
    {

    }

    @Override
    public boolean isMourning()
    {
        return false;
    }

    @Override
    public boolean isColonyUnderAttack()
    {
        return false;
    }

    @Override
    public boolean isValidAttackingPlayer(final EntityPlayer entity)
    {
        return false;
    }

    @Override
    public void addGuardToAttackers(final AbstractEntityCitizen entityCitizen, final EntityPlayer followPlayer)
    {

    }

    @Override
    public void setColonyColor(final TextFormatting color)
    {

    }

    /**
     * Get a list of all buildings.
     * @return a list of their views.
     */
    @Override
    public List<IBuildingView> getBuildings()
    {
        return new ArrayList<>(buildings.values());
    }

    /**
     * Get the cost multiplier of buying a citizen.
     * @return the current cost.
     */
    @Override
    public int getBoughtCitizenCost()
    {
        return boughtCitizenCost;
    }

    @Override
    public void increaseBoughtCitizenCost()
    {

    }

    @Override
    public Set<EntityPlayer> getImportantMessageEntityPlayers()
    {
        return new HashSet<>();
    }

    /**
     * Get the style of the colony.
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
        ////////TODO: Figure out how to implement these on
    }

    @Override
    public IBuildingManager getBuildingManager()
    {
        return null;
    }

    @Override
    public ICitizenManager getCitizenManager()
    {
        return null;
    }

    @Override
    public IColonyHappinessManager getColonyHappinessManager()
    {
        return null;
    }

    @Override
    public IStatisticAchievementManager getStatsManager()
    {
        return null;
    }

    @Override
    public IRaiderManager getRaiderManager()
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
        return this.horde > 0;
    }

    @Override
    public long getMercenaryUseTime()
    {
        return mercenaryLastUseTime;
    }

    @Override
    public void usedMercenaries()
    {
        mercenaryLastUseTime = world.getTotalWorldTime();
    }
}
