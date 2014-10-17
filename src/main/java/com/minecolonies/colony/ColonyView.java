package com.minecolonies.colony;

import com.minecolonies.MineColonies;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.messages.PermissionsMessage;
import com.minecolonies.network.messages.TownhallRenameMessage;
import com.minecolonies.util.ChunkCoordUtils;
import com.minecolonies.util.Utils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

import java.util.*;

public class ColonyView {

    private final UUID id;

    //  General Attributes
    private String name = "Unknown";
    private int dimensionId;
    private ChunkCoordinates center;

    //  Administration/permissions
    private Permissions.View permissions;
    //private int autoHostile = 0;//Off

    //  Buildings
    private Map<ChunkCoordinates, Building.View> buildings = new HashMap<ChunkCoordinates, Building.View>();

    //  Citizenry
    private int maxCitizens = Constants.DEFAULTMAXCITIZENS;
    private Map<UUID, CitizenData.View> citizens = new HashMap<UUID, CitizenData.View>();

    private final static String TAG_NAME = "name";
    private final static String TAG_DIMENSION = "dimension";
    private final static String TAG_CENTER = "center";
    private final static String TAG_MAX_CITIZENS = "maxCitizens";
    private final static String TAG_CITIZENS_REMOVED = "citizensRemoved";
    private final static String TAG_BUILDINGS_REMOVED = "buildingsRemoved";
    private final static String TAG_AUTO_HOSTILE = "autoHostile";

    /**
     * Base constructor for a colony.
     *
     * @param id The current id for the colony
     */
    private ColonyView(UUID id) {
        this.id = id;
    }

    /**
     * Create a ColonyView given a UUID and NBTTagCompound
     *
     * @param id
     * @param compound
     * @return
     */
    public static ColonyView createFromNBT(UUID id, NBTTagCompound compound) {
        ColonyView view = new ColonyView(id);
        view.permissions = Permissions.createPermissionsView(compound);
        return view;
    }

    public UUID getID() {
        return id;
    }

    public int getDimensionId() {
        return dimensionId;
    }
    //    public World getWorld() { return world != null ? world.get() : null; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        MineColonies.network.sendToServer(new TownhallRenameMessage(getID(), name));
    }

    public Building.View getBuilding(int x, int y, int z) {
        return getBuilding(new ChunkCoordinates(x, y, z));
    }

    public Building.View getBuilding(ChunkCoordinates buildingId) {
        return buildings.get(buildingId);
    }

    public Map<UUID, Permissions.Rank> getPlayers() {
        return permissions.getPlayers();
    }

    public void setPermission(Permissions.Rank rank, Permissions.Action action) {
        if(permissions.setPermission(rank, action))
        {
            MineColonies.network.sendToServer(new PermissionsMessage.Permission(id, PermissionsMessage.MessageType.SET_PERMISSION, rank, action));
        }
    }

    public void removePermission(Permissions.Rank rank, Permissions.Action action) {
        if(permissions.removePermission(rank, action))
        {
            MineColonies.network.sendToServer(new PermissionsMessage.Permission(id, PermissionsMessage.MessageType.REMOVE_PERMISSION, rank, action));
        }
    }

    public void togglePermission(Permissions.Rank rank, Permissions.Action action) {
        permissions.togglePermission(rank, action);
        MineColonies.network.sendToServer(new PermissionsMessage.Permission(id, PermissionsMessage.MessageType.TOGGLE_PERMISSION, rank, action));
    }

    public void addPlayer(UUID player, Permissions.Rank rank) {
        permissions.addPlayer(player, rank);
        MineColonies.network.sendToServer(new PermissionsMessage.AddPlayer(id, player, rank));
    }

    public void removePlayer(UUID player) {
        permissions.removePlayer(player);
        MineColonies.network.sendToServer(new PermissionsMessage.RemovePlayer(id, player));
    }

    public int getMaxCitizens() {
        return maxCitizens;
    }

    public Map<UUID, CitizenData.View> getCitizens() {
        return Collections.unmodifiableMap(citizens);
    }

    public CitizenData.View getCitizen(UUID id) {
        return citizens.get(id);
    }

    /**
     * When the ColonyView's world is loaded, associate with it
     *
     * @param w
     */
    public void onWorldLoad(World w) {
        //        if (w.provider.dimensionId == dimensionId)
        //        {
        //            world = new WeakReference<World>(w);
        //        }
    }

    /**
     * Populate an NBT compound for a network packet representing a ColonyView
     * PLACEHOLDER - We will use eventually use PacketBuffers
     *
     * @param colony
     * @param compound
     */
    static public void createNetworkData(Colony colony, NBTTagCompound compound) {
        //  General Attributes
        compound.setString(TAG_NAME, colony.getName());
        compound.setInteger(TAG_DIMENSION, colony.getDimensionId());
        ChunkCoordUtils.writeToNBT(compound, TAG_CENTER, colony.getCenter());

        //  Citizenry
        compound.setInteger(TAG_MAX_CITIZENS, colony.getMaxCitizens());
        //  Citizens are sent as a separate packet

        //  Removed Citizens
        List<UUID> removedCitizens = colony.getRemovedCitizens();
        if (!removedCitizens.isEmpty()) {
            NBTTagList buildingTagList = new NBTTagList();
            for (UUID id : removedCitizens) {
                buildingTagList.appendTag(new NBTTagString(id.toString()));
            }
            compound.setTag(TAG_CITIZENS_REMOVED, buildingTagList);
        }

        //  Removed Buildings
        List<ChunkCoordinates> removedBuildings = colony.getRemovedBuildings();
        if (!removedBuildings.isEmpty()) {
            NBTTagList buildingTagList = new NBTTagList();
            for (ChunkCoordinates id : removedBuildings) {
                ChunkCoordUtils.writeToNBTTagList(buildingTagList, id);
            }
            compound.setTag(TAG_BUILDINGS_REMOVED, buildingTagList);
        }

        //compound.setInteger(TAG_AUTO_HOSTILE, colony.getAutoHostile());
    }

    /**
     * Populate a ColonyView from the network data
     * PLACEHOLDER - We will use eventually use PacketBuffers
     *
     * @param compound
     */
    public IMessage handleColonyViewPacket(NBTTagCompound compound, boolean isNewSubscription) {
        //  General Attributes
        name = compound.getString(TAG_NAME);
        dimensionId = compound.getInteger(TAG_DIMENSION);
        center = ChunkCoordUtils.readFromNBT(compound, TAG_CENTER);

        //  Citizenry
        maxCitizens = compound.getInteger(TAG_MAX_CITIZENS);

        if (isNewSubscription) {
            citizens.clear();
            buildings.clear();
        } else {
            if (compound.hasKey(TAG_CITIZENS_REMOVED)) {
                NBTTagList citizenTagList = compound.getTagList(TAG_CITIZENS_REMOVED, NBT.TAG_STRING);
                for (int i = 0; i < citizenTagList.tagCount(); ++i) {
                    UUID id = UUID.fromString(citizenTagList.getStringTagAt(i));
                    citizens.remove(id);
                }
            }

            if (compound.hasKey(TAG_BUILDINGS_REMOVED)) {
                NBTTagList buildingTagList = compound.getTagList(TAG_BUILDINGS_REMOVED, NBT.TAG_COMPOUND);
                for (int i = 0; i < buildingTagList.tagCount(); ++i) {
                    ChunkCoordinates id = ChunkCoordUtils.readFromNBTTagList(buildingTagList, i);
                    buildings.remove(id);
                }
            }
        }

        //autoHostile = compound.getInteger(TAG_AUTO_HOSTILE);

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

    public IMessage handlePermissionsViewPacket(NBTTagCompound data)
    {
        permissions = Permissions.createPermissionsView(data);
        return null;
    }

    /**
     * Update a ColonyView's citizens given a network data ColonyView update packet
     * This uses a full-replacement - citizens do not get updated and are instead overwritten
     * PLACEHOLDER - We will use eventually use PacketBuffers
     *
     * @param compound
     * @return
     */
    public IMessage handleColonyViewCitizensPacket(UUID id, NBTTagCompound compound) {
        CitizenData.View citizen = CitizenData.createCitizenDataView(id, compound);
        if (citizen != null) {
            citizens.put(citizen.getID(), citizen);
        }

        return null;
    }

    /**
     * Update a ColonyView's buildings given a network data ColonyView update packet
     * This uses a full-replacement - buildings do not get updated and are instead overwritten
     * PLACEHOLDER - We will use eventually use PacketBuffers
     *
     * @param compound
     * @return
     */
    public IMessage handleColonyBuildingViewPacket(ChunkCoordinates buildingId, NBTTagCompound compound) {
        Building.View building = Building.createBuildingView(this, buildingId, compound);    //  At the moment we are re-using the save/load code
        if (building != null) {
            buildings.put(building.getID(), building);
        }

        return null;
    }

    /**
     * Determine if a given chunk coordinate is considered to be within the colony's bounds
     *
     * @param coord
     * @return
     */
    public boolean isCoordInColony(World w, ChunkCoordinates coord) {
        return isCoordInColony(w, coord.posX, coord.posY, coord.posZ);
    }

    public boolean isCoordInColony(World w, int x, int y, int z) {
        //  Perform a 2D distance calculation, so pass center.posY as the Y
        return w.provider.dimensionId == dimensionId &&
                center.getDistanceSquared(x, center.posY, z) <= Utils.square(Configurations.workingRangeTownhall);
    }

    public float getDistanceSquared(ChunkCoordinates coord) {
        return getDistanceSquared(coord.posX, coord.posY, coord.posZ);
    }

    public float getDistanceSquared(int posX, int posY, int posZ) {
        //  Perform a 2D distance calculation, so pass center.posY as the Y
        return center.getDistanceSquared(posX, center.posY, posZ);
    }
}
