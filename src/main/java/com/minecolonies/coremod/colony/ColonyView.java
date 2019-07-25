package com.minecolonies.coremod.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.permissions.Player;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.registry.BuildingRegistry;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.coremod.colony.permissions.PermissionsView;
import com.minecolonies.coremod.colony.requestsystem.management.manager.StandardRequestManager;
import com.minecolonies.coremod.colony.workorders.AbstractWorkOrder;
import com.minecolonies.coremod.colony.workorders.WorkOrderView;
import com.minecolonies.coremod.network.messages.PermissionsMessage;
import com.minecolonies.coremod.network.messages.TownHallRenameMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;

/**
 * Client side representation of the Colony.
 */
public final class ColonyView implements IColony
{
    /**
     * Max allowed CompoundNBT in bytes
     */
    private static final int MAX_BYTES_NBTCOMPOUND = (int) 1e6;

    //  General Attributes
    private final int id;
    private final Map<Integer, WorkOrderView>         workOrders  = new HashMap<>();
    //  Administration/permissions
    @NotNull
    private final PermissionsView                     permissions = new PermissionsView();
    @NotNull
    private final Map<BlockPos, AbstractBuildingView> buildings   = new HashMap<>();
    //  Citizenry
    @NotNull
    private final Map<Integer, CitizenDataView>       citizens    = new HashMap<>();
    private       String                              name        = "Unknown";
    private int      dimensionId;
    private BlockPos center = BlockPos.ORIGIN;

    /**
     * Colony team color.
     */
    private TextFormatting teamColonyColor = TextFormatting.WHITE;

    /**
     * Datas about the happiness of a colony
     */
    private final HappinessData                       happinessData      = new HappinessData();

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
    private BuildingTownHall.View townHall;
    private int citizenCount = 0;

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
    private final Set<BlockPos> wayPoints = new HashSet<>();

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
     * @param buf               {@link ByteBuf} to write data in.
     * @param hasNewSubscribers true if there is a new subscription.
     */
    public static void serializeNetworkData(@NotNull final Colony colony, @NotNull final ByteBuf buf, final boolean hasNewSubscribers)
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
        final Set<BlockPos> waypoints = colony.getWayPoints().keySet();

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
        for (final BlockPos block : waypoints)
        {
            BlockPosUtil.writeToByteBuf(buf, block);
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
            if ((postSize - preSize) >= MAX_BYTES_NBTCOMPOUND)
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
    public List<BlockPos> getFreePositions()
    {
        return new ArrayList<>(freePositions);
    }

    /**
     * Get a copy of the freeBlocks list.
     *
     * @return the list of free to interact blocks.
     */
    public List<Block> getFreeBlocks()
    {
        return new ArrayList<>(freeBlocks);
    }

    /**
     * Add a new free to interact position.
     *
     * @param pos position to add.
     */
    public void addFreePosition(@NotNull final BlockPos pos)
    {
        freePositions.add(pos);
    }

    /**
     * Add a new free to interact block.
     *
     * @param block block to add.
     */
    public void addFreeBlock(@NotNull final Block block)
    {
        freeBlocks.add(block);
    }

    /**
     * Remove a free to interact position.
     *
     * @param pos position to remove.
     */
    public void removeFreePosition(@NotNull final BlockPos pos)
    {
        freePositions.remove(pos);
    }

    /**
     * Remove a free to interact block.
     *
     * @param block state to remove.
     */
    public void removeFreeBlock(@NotNull final Block block)
    {
        freeBlocks.remove(block);
    }

    /**
     * Returns the dimension ID of the view.
     *
     * @return dimension ID of the view.
     */
    public int getDimension()
    {
        return dimensionId;
    }

    /**
     * Getter for the manual hiring or not.
     *
     * @return the boolean true or false.
     */
    public boolean isManualHiring()
    {
        return manualHiring;
    }

    /**
     * Sets if workers should be hired manually.
     *
     * @param manualHiring true if manually.
     */
    public void setManualHiring(final boolean manualHiring)
    {
        this.manualHiring = manualHiring;
    }

    /**
     * Getter for the manual housing or not.
     *
     * @return the boolean true or false.
     */
    public boolean isManualHousing()
    {
        return manualHousing;
    }

    /**
     * Sets if houses should be assigned manually.
     *
     * @param manualHousing true if manually.
     */
    public void setManualHousing(final boolean manualHousing)
    {
        this.manualHousing = manualHousing;
    }

    /**
     * Getter for letting citizens move in or not.
     *
     * @return the boolean true or false.
     */
    public boolean canMoveIn()
    {
        return moveIn;
    }

    /**
     * Sets if citizens can move in.
     *
     * @param moveIn true if citizens can move in.
     */
    public void setMoveIn(final boolean moveIn) { this.moveIn = moveIn; }

    /**
     * Get the town hall View for this ColonyView.
     *
     * @return {@link BuildingTownHall.View} of the colony.
     */
    @Nullable
    public BuildingTownHall.View getTownHall()
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
    public AbstractBuildingView getBuilding(final int x, final int y, final int z)
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
    public AbstractBuildingView getBuilding(final BlockPos buildingId)
    {
        return buildings.get(buildingId);
    }

    /**
     * Returns a map of players in the colony. Key is the UUID, value is {@link
     * Player}
     *
     * @return Map of UUID's and {@link Player}
     */
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
    public int getCitizenCount()
    {
        return citizenCount;
    }

    /**
     * Getter for the citizens map.
     *
     * @return a unmodifiable Map of the citizen.
     */
    public Map<Integer, CitizenDataView> getCitizens()
    {
        return Collections.unmodifiableMap(citizens);
    }

    /**
     * Getter for the workOrders.
     *
     * @return a unmodifiable Collection of the workOrders.
     */
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
    public CitizenDataView getCitizen(final int id)
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
            wayPoints.add(BlockPosUtil.readFromByteBuf(buf));
        }
        this.lastContactInHours = buf.readInt();
        this.manualHousing = buf.readBoolean();
        this.moveIn = buf.readBoolean();

        if (buf.readBoolean())
        {
            final CompoundNBT compound = ByteBufUtils.readTag(buf);
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
    @Nullable
    public IMessage handleColonyViewCitizensMessage(final int id, final ByteBuf buf)
    {
        final CitizenDataView citizen = CitizenData.createCitizenDataView(id, buf);
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
    @Nullable
    public IMessage handleColonyViewRemoveBuildingMessage(final BlockPos buildingId)
    {
        final AbstractBuildingView building = buildings.remove(buildingId);
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
    @Nullable
    public IMessage handleColonyBuildingViewMessage(final BlockPos buildingId, @NotNull final ByteBuf buf)
    {
        @Nullable final AbstractBuildingView building = BuildingRegistry.createBuildingView(this, buildingId, buf);
        if (building != null)
        {
            buildings.put(building.getID(), building);

            if (building instanceof BuildingTownHall.View)
            {
                townHall = (BuildingTownHall.View) building;
            }
        }

        return null;
    }

    /**
     * Update the happiness values for a colony
     * @param happinessData The new values for happiness
     * @return null == no response.
     */
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
    public void addPlayer(final String player)
    {
        MineColonies.getNetwork().sendToServer(new PermissionsMessage.AddPlayer(this, player));
    }

    /**
     * Remove player from colony permissions.
     *
     * @param player the UUID of the player to remove.
     */
    public void removePlayer(final UUID player)
    {
        MineColonies.getNetwork().sendToServer(new PermissionsMessage.RemovePlayer(this, player));
    }

    /**
     * Getter for the overall happiness.
     *
     * @return the happiness, a double.
     */
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
    public TextFormatting getTeamColonyColor()
    {
        return teamColonyColor;
    }

    /**
     * Sets the name of the view.
     *
     * @param name Name of the view.
     */
    public void setName(final String name)
    {
        this.name = name;
        MineColonies.getNetwork().sendToServer(new TownHallRenameMessage(this, name));
    }

    @NotNull
    @Override
    public PermissionsView getPermissions()
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
    public void removeVisitingPlayer(final PlayerEntity player)
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void addVisitingPlayer(final PlayerEntity player)
    {
        /*
         * Intentionally left empty.
         */
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
     * Get a list of all waypoints in the colony view.
     *
     * @return a copy of the list.
     */
    public Set<BlockPos> getWayPoints()
    {
        return new HashSet<>(wayPoints);
    }

    /**
     * Get a list of all barb spawn positions in the colony view.
     *
     * @return a copy of the list.
     */
    public List<BlockPos> getLastSpawnPoints()
    {
        return new ArrayList<>(lastSpawnPoints);
    }

    /**
     * Get if progress should be printed.
     * @return true if so.
     */
    public boolean isPrintingProgress()
    {
        return printProgress;
    }

    @Override
    public boolean isRemote()
    {
        return true;
    }

    /**
     * Get a list of all buildings.
     * @return a list of their views.
     */
    public List<AbstractBuildingView> getBuildings()
    {
        return new ArrayList<>(buildings.values());
    }

    /**
     * Get the cost multiplier of buying a citizen.
     * @return the current cost.
     */
    public int getBoughtCitizenCost()
    {
        return boughtCitizenCost;
    }

    /**
     * Get the style of the colony.
     * @return the current default style.
     */
    public String getStyle()
    {
        return style;
    }

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
