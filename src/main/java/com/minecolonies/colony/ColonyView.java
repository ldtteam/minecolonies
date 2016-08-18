package com.minecolonies.colony;

import com.minecolonies.MineColonies;
import com.minecolonies.colony.buildings.AbstractBuilding;
import com.minecolonies.colony.buildings.BuildingTownHall;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.colony.workorders.AbstractWorkOrder;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.network.messages.ColonyViewWorkOrderMessage;
import com.minecolonies.network.messages.PermissionsMessage;
import com.minecolonies.network.messages.TownHallRenameMessage;
import com.minecolonies.util.BlockPosUtil;
import com.minecolonies.util.MathUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.*;

/**
 * Client side representation of the Colony.
 */
public final class ColonyView implements IColony
{
    //  General Attributes
    private final   int                                     id;
    private         String                                  name            = "Unknown";
    private         int                                     dimensionId;
    private         BlockPos                                center;

    /**
     * Defines if workers are hired manually or automatically.
     */
    private boolean manualHiring = false;

    //  Administration/permissions
    private Permissions.View permissions = new Permissions.View();

    //  Buildings
    private BuildingTownHall.View townHall;
    private Map<BlockPos, AbstractBuilding.View> buildings = new HashMap<>();

    //  Citizenry
    private Map<Integer, CitizenDataView> citizens = new HashMap<>();
    private Map<Integer, WorkOrderView> workOrders = new HashMap<>();

    private int maxCitizens = 0;

    /**
     * Base constructor for a colony.
     *
     * @param id The current id for the colony
     */
    private ColonyView(int id)
    {
        this.id = id;
    }

    /**
     * Create a ColonyView given a UUID and NBTTagCompound
     *
     * @param id    Id of the colony view
     * @return      the new colony view
     */
    public static ColonyView createFromNetwork(int id)
    {
        return new ColonyView(id);
    }

    /**
     * Returns the ID of the view
     *
     * @return ID of the view
     */
    public int getID()
    {
        return id;
    }

    /**
     * Returns the dimension ID of the view
     *
     * @return      dimension ID of the view
     */
    public int getDimensionId()
    {
        return dimensionId;
    }

    /**
     * Sets the name of the view
     *
     * @param name  Name of the view
     */
    public void setName(String name)
    {
        this.name = name;
        MineColonies.getNetwork().sendToServer(new TownHallRenameMessage(this, name));
    }

    /**
     * Getter for the manual hiring or not.
     * @return the boolean true or false.
     */
    public boolean isManualHiring()
    {
        return manualHiring;
    }

    /**
     * Sets if workers should be hired manually
     * @param manualHiring true if manually.
     */
    public void setManualHiring(boolean manualHiring)
    {
        this.manualHiring = manualHiring;
    }

    /**
     * Get the town hall View for this ColonyView
     *
     * @return {@link BuildingTownHall.View} of the colony
     */
    public BuildingTownHall.View getTownHall()
    {
        return townHall;
    }

    /**
     * Get a AbstractBuilding.View for a given building (by coordinate-id) using raw x,y,z
     *
     * @param x     x-coordinate
     * @param y     y-coordinate
     * @param z     z-coordinate
     * @return      {@link AbstractBuilding.View} of a AbstractBuilding for the given Coordinates/ID, or null
     */
    public AbstractBuilding.View getBuilding(int x, int y, int z)
    {
        return getBuilding(new BlockPos(x, y, z));
    }

    /**
     * Get a AbstractBuilding.View for a given building (by coordinate-id) using ChunkCoordinates
     *
     * @param buildingId        Coordinates/ID of the AbstractBuilding
     * @return                  {@link AbstractBuilding.View} of a AbstractBuilding for the given Coordinates/ID, or null
     */
    public AbstractBuilding.View getBuilding(BlockPos buildingId)
    {
        return buildings.get(buildingId);
    }

    /**
     * Returns a map of players in the colony
     * Key is the UUID, value is {@link com.minecolonies.colony.permissions.Permissions.Player}
     *
     * @return                  Map of UUID's and {@link com.minecolonies.colony.permissions.Permissions.Player}
     */
    public Map<UUID, Permissions.Player> getPlayers()
    {
        return permissions.getPlayers();
    }

    /**
     * Sets a specific permission to a rank. If the permission wasn't already set, it sends a message to the server
     *
     * @param rank              Rank to get the permission
     * @param action            Permission to get
     */
    public void setPermission(Permissions.Rank rank, Permissions.Action action)
    {
        if (permissions.setPermission(rank, action))
        {
            MineColonies.getNetwork().sendToServer(new PermissionsMessage.Permission(this, PermissionsMessage.MessageType.SET_PERMISSION, rank, action));
        }
    }

    /**
     * removes a specific permission to a rank. If the permission was set, it sends a message to the server
     *
     * @param rank              Rank to remove permission from
     * @param action            Action to remove permission of
     */
    public void removePermission(Permissions.Rank rank, Permissions.Action action)
    {
        if (permissions.removePermission(rank, action))
        {
            MineColonies.getNetwork().sendToServer(new PermissionsMessage.Permission(this, PermissionsMessage.MessageType.REMOVE_PERMISSION, rank, action));
        }
    }

    /**
     *
     * Toggles a specific permission to a rank. Sends a message to the server
     *
     * @param rank      Rank to toggle permission of
     * @param action    Action to toggle permission of
     */
    public void togglePermission(Permissions.Rank rank, Permissions.Action action)
    {
        permissions.togglePermission(rank, action);
        MineColonies.getNetwork().sendToServer(new PermissionsMessage.Permission(this, PermissionsMessage.MessageType.TOGGLE_PERMISSION, rank, action));
    }


    /**
     * Returns the maximum amount of citizen in the colony
     *
     * @return          maximum amount of citizens
     */
    public int getMaxCitizens()
    {
        return maxCitizens;
    }

    /**
     * Getter for the citizens map
     * @return a unmodifiable Map of the citizen.
     */
    public Map<Integer, CitizenDataView> getCitizens()
    {
        return Collections.unmodifiableMap(citizens);
    }

    /**
     * Getter for the workOrders.
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
    public CitizenDataView getCitizen(int id)
    {
        return citizens.get(id);
    }

    /**
     * Populate an NBT compound for a network packet representing a ColonyView.
     *
     * @param colony        Colony to write data about.
     * @param buf           {@link ByteBuf} to write data in.
     * @param isNewSubScription true if this is a new subscription.
     */
    public static void serializeNetworkData(Colony colony, ByteBuf buf, boolean isNewSubScription)
    {
        //  General Attributes
        ByteBufUtils.writeUTF8String(buf, colony.getName());
        buf.writeInt(colony.getDimensionId());
        BlockPosUtil.writeToByteBuf(buf, colony.getCenter());
        buf.writeBoolean(colony.isManualHiring());
        //  Citizenry
        buf.writeInt(colony.getMaxCitizens());
        //  Citizens are sent as a separate packet
    }

    /**
     * Populate a ColonyView from the network data
     *
     * @param buf                   {@link ByteBuf} to read from
     * @param isNewSubscription     Whether this is a new subscription of not
     * @return null == no response
     */
    public IMessage handleColonyViewMessage(ByteBuf buf, boolean isNewSubscription)
    {
        //  General Attributes
        name = ByteBufUtils.readUTF8String(buf);
        dimensionId = buf.readInt();
        center = BlockPosUtil.readFromByteBuf(buf);
        manualHiring = buf.readBoolean();
        //  Citizenry
        maxCitizens = buf.readInt();

        if (isNewSubscription)
        {
            citizens.clear();
            townHall = null;
            buildings.clear();
        }

        return null;
    }

    /**
     * Update permissions.
     *
     * @param buf buffer containing permissions.
     * @return null == no response
     */
    public IMessage handlePermissionsViewMessage(ByteBuf buf)
    {
        permissions.deserialize(buf);
        return null;
    }

    /**
     * Update a ColonyView's workOrders given a network data ColonyView update packet.
     * This uses a full-replacement - workOrders do not get updated and are instead overwritten.
     *
     * @param buf       Network data
     * @return          null == no response
     */
    public IMessage handleColonyViewWorkOrderMessage(ByteBuf buf)
    {
        WorkOrderView workOrder = AbstractWorkOrder.createWorkOrderView(buf);
        workOrders.put(workOrder.getId(), workOrder);

        return null;
    }

    /**
     * Update a ColonyView's citizens given a network data ColonyView update packet
     * This uses a full-replacement - citizens do not get updated and are instead overwritten
     *
     * @param id        ID of the citizen
     * @param buf       Network data
     * @return          null == no response
     */
    public IMessage handleColonyViewCitizensMessage(int id, ByteBuf buf)
    {
        CitizenDataView citizen = CitizenData.createCitizenDataView(id, buf);
        if (citizen != null)
        {
            citizens.put(citizen.getID(), citizen);
        }

        return null;
    }

    /**
     * Remove a citizen from the ColonyView.
     *
     * @param citizen citizen ID
     * @return          null == no response
     */
    public IMessage handleColonyViewRemoveCitizenMessage(int citizen)
    {
        citizens.remove(citizen);
        return null;
    }

    /**
     * Remove a building from the ColonyView.
     *
     * @param buildingId location of the building.
     * @return          null == no response
     */
    public IMessage handleColonyViewRemoveBuildingMessage(BlockPos buildingId)
    {
        AbstractBuilding.View building = buildings.remove(buildingId);
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
     * @return          null == no response
     */
    public IMessage handleColonyViewRemoveWorkOrderMessage(final int workOrderId)
    {
        workOrders.remove(workOrderId);

        return null;
    }


    /**
     * Update a ColonyView's buildings given a network data ColonyView update packet.
     * This uses a full-replacement - buildings do not get updated and are instead overwritten.
     *
     * @param buildingId location of the building.
     * @param buf buffer containing ColonyBuilding information.
     * @return          null == no response
     */
    public IMessage handleColonyBuildingViewMessage(BlockPos buildingId, ByteBuf buf)
    {
        AbstractBuilding.View building = AbstractBuilding.createBuildingView(this, buildingId, buf);
        if (building != null)
        {
            buildings.put(building.getID(), building);

            if (building instanceof BuildingTownHall.View)
            {
                townHall = (BuildingTownHall.View)building;
            }
        }

        return null;
    }

    /**
     * Update a players permissions.
     *
     * @param player player username.
     */
    public void addPlayer(String player)
    {
        MineColonies.getNetwork().sendToServer(new PermissionsMessage.AddPlayer(this, player));
    }

    /**
     * Remove player from colony permissions.
     *
     * @param player the UUID of the player to remove.
     */
    public void removePlayer(UUID player)
    {
        MineColonies.getNetwork().sendToServer(new PermissionsMessage.RemovePlayer(this, player));
    }

    @Override
    public boolean isCoordInColony(World w, BlockPos pos)
    {
        //  Perform a 2D distance calculation, so pass center.posY as the Y
        return w.provider.getDimensionId() == dimensionId &&
               BlockPosUtil.getDistanceSquared(center, new BlockPos(pos.getX(), center.getY(), pos.getZ())) <= MathUtils.square(Configurations.workingRangeTownHall);
    }

    @Override
    public float getDistanceSquared(BlockPos pos)
    {
        //  Perform a 2D distance calculation, so pass center.posY as the Y
        return BlockPosUtil.getDistanceSquared(center, new BlockPos(pos.getX(), center.getY(), pos.getZ()));
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public boolean hasTownHall()
    {
        return townHall != null;
    }

    @Override
    public Permissions.View getPermissions()
    {
        return permissions;
    }

}
