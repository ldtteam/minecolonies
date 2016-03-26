package com.minecolonies.colony;

import com.minecolonies.MineColonies;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.buildings.BuildingTownHall;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.network.messages.PermissionsMessage;
import com.minecolonies.network.messages.TownhallRenameMessage;
import com.minecolonies.util.ChunkCoordUtils;
import com.minecolonies.util.Utils;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ColonyView implements IColony
{
    //  General Attributes
    private final   int                                     id;
    private         String                                  name            = "Unknown";
    private         int                                     dimensionId;
    private         ChunkCoordinates                        center;

    //  Administration/permissions
    private         Permissions.View                        permissions     = new Permissions.View();
    //private int autoHostile = 0;//Off

    //  Buildings
    private         BuildingTownHall.View                   townhall;
    private         Map<ChunkCoordinates, Building.View>    buildings       = new HashMap<ChunkCoordinates, Building.View>();

    //  Citizenry
    private         Map<Integer, CitizenData.View>          citizens        = new HashMap<Integer, CitizenData.View>();
    private         int                                     maxCitizens     = 0;

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
     * @return      ID of the view
     */
    public int getID() { return id; }

    /**
     * Returns the dimension ID of the view
     *
     * @return      dimension ID of the view
     */
    public int getDimensionId() {
        return dimensionId;
    }
    //    public World getWorld() { return world != null ? world.get() : null; }

    /**
     * Sets the name of the view
     *
     * @param name  Name of the view
     */
    public void setName(String name)
    {
        this.name = name;
        MineColonies.getNetwork().sendToServer(new TownhallRenameMessage(this, name));
    }

    /**
     * Get the Town hall View for this ColonyView
     *
     * @return      {@link com.minecolonies.colony.buildings.BuildingTownHall.View} of the colony
     */
    public BuildingTownHall.View getTownhall() {
        return townhall;
    }

    /**
     * Get a Building.View for a given building (by coordinate-id) using raw x,y,z
     *
     * @param x     x-coordinate
     * @param y     y-coordinate
     * @param z     z-coordinate
     * @return      {@link com.minecolonies.colony.buildings.Building.View} of a Building for the given Coordinates/ID, or null
     */
    public Building.View getBuilding(int x, int y, int z)
    {
        return getBuilding(new ChunkCoordinates(x, y, z));
    }

    /**
     * Get a Building.View for a given building (by coordinate-id) using ChunkCoordinates
     *
     * @param buildingId        Coordinates/ID of the Building
     * @return                  {@link com.minecolonies.colony.buildings.Building.View} of a Building for the given Coordinates/ID, or null
     */
    public Building.View getBuilding(ChunkCoordinates buildingId)
    {
        return buildings.get(buildingId);
    }

    /**
     * Returns a map of players in the colony
     * Key is the UUID, value is {@link com.minecolonies.colony.permissions.Permissions.Player}
     *
     * @return  Map of UUID's and {@link com.minecolonies.colony.permissions.Permissions.Player}
     */
    public Map<UUID, Permissions.Player> getPlayers()
    {
        return permissions.getPlayers();
    }

    /**
     * Sets a specific permission to a rank. If the permission wasn't already set, it sends a message to the server
     *
     * @param rank          Rank to get the permission
     * @param action        Permission to get
     */
    public void setPermission(Permissions.Rank rank, Permissions.Action action) {
        if(permissions.setPermission(rank, action))
        {
            MineColonies.getNetwork().sendToServer(new PermissionsMessage.Permission(this, PermissionsMessage.MessageType.SET_PERMISSION, rank, action));
        }
    }

    /**
     * removes a specific permission to a rank. If the permission was set, it sends a message to the server
     *
     * @param rank
     * @param action
     */
    public void removePermission(Permissions.Rank rank, Permissions.Action action) {
        if(permissions.removePermission(rank, action))
        {
            MineColonies.getNetwork().sendToServer(new PermissionsMessage.Permission(this, PermissionsMessage.MessageType.REMOVE_PERMISSION, rank, action));
        }
    }

    /**
     *
     * Toggles a specific permission to a rank. Sends a message to the server
     *
     * @param rank
     * @param action
     */
    public void togglePermission(Permissions.Rank rank, Permissions.Action action) {
        permissions.togglePermission(rank, action);
        MineColonies.getNetwork().sendToServer(new PermissionsMessage.Permission(this, PermissionsMessage.MessageType.TOGGLE_PERMISSION, rank, action));
    }

//    public void addPlayer(String player, Permissions.Rank rank)

    /**
     * Returns the maximum amount of citizen in the colony
     *
     * @return  maximum amount of citizens
     */
    public int getMaxCitizens() {
        return maxCitizens;
    }

    public Map<Integer, CitizenData.View> getCitizens() {
        return Collections.unmodifiableMap(citizens);
    }

    public CitizenData.View getCitizen(int id) {
        return citizens.get(id);
    }

    /**
     * When the ColonyView's world is loaded, associate with it
     *
     * @param w     World that is loading
     */
    public void onWorldLoad(World w) {
//        if (w.provider.dimensionId == dimensionId)
//        {
//            world = new WeakReference<World>(w);
//        }
    }

    /**
     * Populate an NBT compound for a network packet representing a ColonyView
     *
     * @param colony        Colony to write data bout
     * @param buf           {@link ByteBuf} to write data in
     */
    static public void serializeNetworkData(Colony colony, ByteBuf buf, boolean isNewSubScription)
    {
        //  General Attributes
        ByteBufUtils.writeUTF8String(buf, colony.getName());
        buf.writeInt(colony.getDimensionId());
        ChunkCoordUtils.writeToByteBuf(buf, colony.getCenter());

        //  Citizenry
        buf.writeInt(colony.getMaxCitizens());
        //  Citizens are sent as a separate packet

        //  buf.writeInt(colony.getAutoHostile());
    }

    /**
     * Populate a ColonyView from the network data
     *
     * @param buf                   {@link ByteBuf} to read from
     * @param isNewSubscription     Whether this is a new subscription of not
     */
    public IMessage handleColonyViewMessage(ByteBuf buf, boolean isNewSubscription)
    {
        //  General Attributes
        name = ByteBufUtils.readUTF8String(buf);
        dimensionId = buf.readInt();
        center = ChunkCoordUtils.readFromByteBuf(buf);

        //  Citizenry
        maxCitizens = buf.readInt();

        if (isNewSubscription)
        {
            citizens.clear();
            townhall = null;
            buildings.clear();
        }

        //autoHostile = buf.readInt();

        //        if (world == null)
        //        {
        //            World w = DimensionManager.getWorld(dimensionId);
        //            if (w != null)
        //            {
        //                onWorldLoad(w);
        //            }
        //        }

        return null;
    }

    /**
     * //TODO document
     * @param buf
     * @return
     */
    public IMessage handlePermissionsViewMessage(ByteBuf buf)   //TODO why do we have return type IMessage, while we always return null
    {
        permissions.deserialize(buf);
        return null;
    }

    /**
     * Update a ColonyView's citizens given a network data ColonyView update packet
     * This uses a full-replacement - citizens do not get updated and are instead overwritten
     *
     * @param id        ID of the citizen
     * @param buf       Network data
     * @return          //todo document
     */
    public IMessage handleColonyViewCitizensMessage(int id, ByteBuf buf) //TODO why do we have return type IMessage, while we always return null
    {
        CitizenData.View citizen = CitizenData.createCitizenDataView(id, buf);
        if (citizen != null)
        {
            citizens.put(citizen.getID(), citizen);
        }

        return null;
    }

    /**
     * Remove a citizen from the ColonyView
     *
     * @return          //todo document
     */
    public IMessage handleColonyViewRemoveCitizenMessage(int citizen) //TODO why do we have return type IMessage, while we always return null
    {
        citizens.remove(citizen);
        return null;
    }

    /**
     * Remove a building from the ColonyView
     *
     * @return          //todo document
     */
    public IMessage handleColonyViewRemoveBuildingMessage(ChunkCoordinates buildingId) //TODO why do we have return type IMessage, while we always return null
    {
        Building.View building = buildings.remove(buildingId);
        if (townhall == building)
        {
            townhall = null;
        }
        return null;
    }

    /**
     * Update a ColonyView's buildings given a network data ColonyView update packet
     * This uses a full-replacement - buildings do not get updated and are instead overwritten
     *
     * @param buf
     * @return          //todo document
     */
    public IMessage handleColonyBuildingViewMessage(ChunkCoordinates buildingId, ByteBuf buf) //TODO why do we have return type IMessage, while we always return null
    {
        Building.View building = Building.createBuildingView(this, buildingId, buf);
        if (building != null)
        {
            buildings.put(building.getID(), building);

            if (building instanceof BuildingTownHall.View)
            {
                townhall = (BuildingTownHall.View)building;
            }
        }

        return null;
    }

    /**
     * @see  {@link #isCoordInColony(World, int, int, int)}
     *
     * @param w         World to check
     * @param coord     ChunkCoordinates to check
     * @return          True if inside colony, otherwise false
     */
    public boolean isCoordInColony(World w, ChunkCoordinates coord) {
        return isCoordInColony(w, coord.posX, coord.posY, coord.posZ);
    }

    @Override
    public boolean isCoordInColony(World w, int x, int y, int z) {
        //  Perform a 2D distance calculation, so pass center.posY as the Y
        return w.provider.dimensionId == dimensionId &&
                center.getDistanceSquared(x, center.posY, z) <= Utils.square(Configurations.workingRangeTownhall);
    }

    /**
     * @see {@link #getDistanceSquared(int, int, int)}
     *
     * @param coord     Chunk coordinate to get squared position
     * @return          Squared position from center
     */
    public float getDistanceSquared(ChunkCoordinates coord) {
        return getDistanceSquared(coord.posX, coord.posY, coord.posZ);
    }

    @Override
    public float getDistanceSquared(int posX, int posY, int posZ) {
        //  Perform a 2D distance calculation, so pass center.posY as the Y
        return center.getDistanceSquared(posX, center.posY, posZ);
    }

    //    }
//        MineColonies.network.sendToServer(new PermissionsMessage.RemovePlayer(id, player));
//    {
//    public void removePlayer(UUID player)
//
//    }
//        MineColonies.network.sendToServer(new PermissionsMessage.AddPlayer(id, player));
//    {
    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean hasTownhall() { return townhall != null; }

    @Override
    public Permissions.View getPermissions() { return permissions; }
}
