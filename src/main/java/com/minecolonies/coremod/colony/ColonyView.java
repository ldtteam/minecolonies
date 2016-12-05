package com.minecolonies.coremod.colony;

import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.BuildingTownHall;
import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.colony.workorders.AbstractWorkOrder;
import com.minecolonies.coremod.configuration.Configurations;
import com.minecolonies.coremod.network.messages.PermissionsMessage;
import com.minecolonies.coremod.network.messages.TownHallRenameMessage;
import com.minecolonies.coremod.util.BlockPosUtil;
import com.minecolonies.coremod.util.MathUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Client side representation of the Colony.
 */
public final class ColonyView implements IColony
{
    //  General Attributes
    private final int id;
    private final Map<Integer, WorkOrderView> workOrders = new HashMap<>();
    //  Administration/permissions
    @NotNull
    private final Permissions.View permissions  = new Permissions.View();
    @NotNull
    private final Map<BlockPos, AbstractBuilding.View> buildings   = new HashMap<>();
    //  Citizenry
    @NotNull
    private final Map<Integer, CitizenDataView>        citizens    = new HashMap<>();
    private       String                      name       = "Unknown";
    private int      dimensionId;
    private BlockPos center;
    /**
     * Defines if workers are hired manually or automatically.
     */
    private       boolean          manualHiring = false;
    //  Buildings
    @Nullable
    private BuildingTownHall.View townHall;
    private       int                                  maxCitizens = 0;

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
     * @param isNewSubScription true if this is a new subscription.
     */
    public static void serializeNetworkData(@NotNull final Colony colony, @NotNull final ByteBuf buf, final boolean isNewSubScription)
    {
        //  General Attributes
        ByteBufUtils.writeUTF8String(buf, colony.getName());
        buf.writeInt(colony.getDimension());
        BlockPosUtil.writeToByteBuf(buf, colony.getCenter());
        buf.writeBoolean(colony.isManualHiring());
        //  Citizenry
        buf.writeInt(colony.getMaxCitizens());
        //  Citizens are sent as a separate packet
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
     * Get a AbstractBuilding.View for a given building (by coordinate-id) using raw x,y,z.
     *
     * @param x x-coordinate.
     * @param y y-coordinate.
     * @param z z-coordinate.
     * @return {@link AbstractBuilding.View} of a AbstractBuilding for the given Coordinates/ID, or null.
     */
    public AbstractBuilding.View getBuilding(final int x, final int y, final int z)
    {
        return getBuilding(new BlockPos(x, y, z));
    }

    /**
     * Get a AbstractBuilding.View for a given building (by coordinate-id) using ChunkCoordinates.
     *
     * @param buildingId Coordinates/ID of the AbstractBuilding.
     * @return {@link AbstractBuilding.View} of a AbstractBuilding for the given Coordinates/ID, or null.
     */
    public AbstractBuilding.View getBuilding(final BlockPos buildingId)
    {
        return buildings.get(buildingId);
    }

    /**
     * Returns a map of players in the colony.
     * Key is the UUID, value is {@link com.minecolonies.coremod.colony.permissions.Permissions.Player}
     *
     * @return Map of UUID's and {@link com.minecolonies.coremod.colony.permissions.Permissions.Player}
     */
    @NotNull
    public Map<UUID, Permissions.Player> getPlayers()
    {
        return permissions.getPlayers();
    }

    /**
     * Sets a specific permission to a rank. If the permission wasn't already set, it sends a message to the server.
     *
     * @param rank   Rank to get the permission.
     * @param action Permission to get.
     */
    public void setPermission(final Permissions.Rank rank, @NotNull final Permissions.Action action)
    {
        if (permissions.setPermission(rank, action))
        {
            MineColonies.getNetwork().sendToServer(new PermissionsMessage.Permission(this, PermissionsMessage.MessageType.SET_PERMISSION, rank, action));
        }
    }

    /**
     * removes a specific permission to a rank. If the permission was set, it sends a message to the server.
     *
     * @param rank   Rank to remove permission from.
     * @param action Action to remove permission of.
     */
    public void removePermission(final Permissions.Rank rank, @NotNull final Permissions.Action action)
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
    public void togglePermission(final Permissions.Rank rank, @NotNull final Permissions.Action action)
    {
        permissions.togglePermission(rank, action);
        MineColonies.getNetwork().sendToServer(new PermissionsMessage.Permission(this, PermissionsMessage.MessageType.TOGGLE_PERMISSION, rank, action));
    }

    /**
     * Returns the maximum amount of citizen in the colony.
     *
     * @return maximum amount of citizens.
     */
    public int getMaxCitizens()
    {
        return maxCitizens;
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
    public IMessage handleColonyViewMessage(@NotNull final ByteBuf buf, final boolean isNewSubscription)
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
    public IMessage handlePermissionsViewMessage(@NotNull final ByteBuf buf)
    {
        permissions.deserialize(buf);
        return null;
    }

    /**
     * Update a ColonyView's workOrders given a network data ColonyView update packet.
     * This uses a full-replacement - workOrders do not get updated and are instead overwritten.
     *
     * @param buf Network data.
     * @return null == no response.
     */
    public IMessage handleColonyViewWorkOrderMessage(final ByteBuf buf)
    {
        @Nullable final WorkOrderView workOrder = AbstractWorkOrder.createWorkOrderView(buf);
        workOrders.put(workOrder.getId(), workOrder);

        return null;
    }

    /**
     * Update a ColonyView's citizens given a network data ColonyView update packet.
     * This uses a full-replacement - citizens do not get updated and are instead overwritten.
     *
     * @param id  ID of the citizen.
     * @param buf Network data.
     * @return null == no response.
     */
    public IMessage handleColonyViewCitizensMessage(final int id, final ByteBuf buf)
    {
        final CitizenDataView citizen = CitizenData.createCitizenDataView(id, buf);
        if (citizen != null)
        {
            citizens.put(citizen.getID(), citizen);
        }

        return null;
    }

    /**
     * Remove a citizen from the ColonyView.
     *
     * @param citizen citizen ID.
     * @return null == no response.
     */
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
    public IMessage handleColonyViewRemoveBuildingMessage(final BlockPos buildingId)
    {
        final AbstractBuilding.View building = buildings.remove(buildingId);
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
     * @param buf        buffer containing ColonyBuilding information.
     * @return null == no response.
     */
    public IMessage handleColonyBuildingViewMessage(final BlockPos buildingId, @NotNull final ByteBuf buf)
    {
        @Nullable final AbstractBuilding.View building = AbstractBuilding.createBuildingView(this, buildingId, buf);
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
    public Permissions.View getPermissions()
    {
        return permissions;
    }

    @Override
    public boolean isCoordInColony(@NotNull final World w, @NotNull final BlockPos pos)
    {
        //  Perform a 2D distance calculation, so pass center.posY as the Y
        return w.provider.getDimension() == dimensionId
                 && BlockPosUtil.getDistanceSquared(center, new BlockPos(pos.getX(), center.getY(), pos.getZ())) <= MathUtils.square(Configurations.workingRangeTownHall);
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
}
