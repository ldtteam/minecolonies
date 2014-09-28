package com.minecolonies.colony;

import com.minecolonies.MineColonies;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.buildings.BuildingTownHall;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.messages.TownhallRenameMessage;
import com.minecolonies.util.ChunkCoordUtils;
import com.minecolonies.util.Utils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants.NBT;

import java.lang.ref.WeakReference;
import java.util.*;

public class ColonyView
{
    private final UUID              id;

    //  General Attributes
    private String                  name   = "Unknown";
    private int                     dimensionId;
    private ChunkCoordinates        center;
//    private WeakReference<World>    world;

    //  Administration
    private Set<UUID>               owners = new HashSet<UUID>();

    //  Buildings
    private Map<ChunkCoordinates, Building.View> buildings = new HashMap<ChunkCoordinates, Building.View>();

    //  Citizenry
    private int           maxCitizens    = Constants.DEFAULTMAXCITIZENS;
    private List<Integer> citizens       = new ArrayList<Integer>();

    final static String TAG_NAME         = "name";
    final static String TAG_DIMENSION    = "dimension";
    final static String TAG_CENTER       = "center";
    final static String TAG_MAX_CITIZENS = "maxCitizens";
    final static String TAG_OWNERS       = "owners";
    final static String TAG_CITIZENS     = "citizens";
    final static String TAG_BUILDINGS_REMOVED = "buildingsRemoved";


    /**
     * Base constructor for a colony.
     *
     * @param uuid The current id for the colony
     */
    protected ColonyView(UUID uuid)
    {
        id = uuid;
    }

    public UUID getID() { return id; }

    public int getDimensionId() { return dimensionId; }
//    public World getWorld() { return world != null ? world.get() : null; }

    public String getName() { return name; }
    public void setName(String name)
    {
        this.name = name;
        MineColonies.network.sendToServer(new TownhallRenameMessage(getID(), name));
    }

    public Building.View getBuilding(int x, int y, int z) { return getBuilding(new ChunkCoordinates(x, y, z)); }
    public Building.View getBuilding(ChunkCoordinates buildingId) { return buildings.get(buildingId); }

    public Set<UUID> getOwners() { return Collections.unmodifiableSet(owners); }
    public boolean isOwner(UUID o) { return owners.contains(o); }
    public boolean isOwner(EntityPlayer player) { return owners.contains(player.getUniqueID()); }
    public void addOwner(UUID o) { owners.add(o); }
    public void removeOwner(UUID o) { owners.remove(o); }

    public int getMaxCitizens() { return maxCitizens; }
    public List<Integer> getCitizens() { return Collections.unmodifiableList(citizens); }

    /**
     * When the ColonyView's world is loaded, associate with it
     *
     * @param w
     */
    public void onWorldLoad(World w)
    {
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
    static public void createNetworkData(Colony colony, NBTTagCompound compound)
    {
        //  General Attributes
        compound.setString(TAG_NAME, colony.getName());
        compound.setInteger(TAG_DIMENSION, colony.getDimensionId());
        ChunkCoordUtils.writeToNBT(compound, TAG_CENTER, colony.getCenter());

        //  Owners
        NBTTagList ownerTagList = new NBTTagList();
        for (UUID owner : colony.getOwners())
        {
            ownerTagList.appendTag(new NBTTagString(owner.toString()));
        }
        compound.setTag(TAG_OWNERS, ownerTagList);

        //  Citizenry
        compound.setInteger(TAG_MAX_CITIZENS, colony.getMaxCitizens());
        //  Citizens are sent as a separate packet

        //  Removed Buildings
        List<ChunkCoordinates> removedBuildings = colony.getRemovedBuildings();
        if (!removedBuildings.isEmpty())
        {
            NBTTagList buildingTagList = new NBTTagList();
            for (ChunkCoordinates id : removedBuildings)
            {
                ChunkCoordUtils.writeToNBTTagList(buildingTagList, id);
            }
            compound.setTag(TAG_BUILDINGS_REMOVED, buildingTagList);
        }
    }

    /**
     * Populate an NBT compound for a network packet representing a ColonyView
     * PLACEHOLDER - We will use eventually use PacketBuffers
     *
     * @param colony
     * @param compound
     */
    static public void createCitizenNetworkData(Colony colony, NBTTagCompound compound)
    {
        World world = DimensionManager.getWorld(colony.getDimensionId());
        if (world != null)
        {
            List<EntityCitizen> entities = colony.getActiveCitizenEntities();

            if (entities != null && !entities.isEmpty())
            {
                int[] entityIds = new int[entities.size()];

                for (int i = 0; i < entityIds.length; ++i)
                {
                    entityIds[i] = entities.get(i).getEntityId();
                }

                compound.setIntArray(TAG_CITIZENS, entityIds);
            }
        }
    }

    /**
     * Populate a ColonyView from the network data
     * PLACEHOLDER - We will use eventually use PacketBuffers
     *
     * @param compound
     */
    public IMessage handleColonyViewPacket(NBTTagCompound compound, boolean newSubscription)
    {
        //  General Attributes
        name = compound.getString(TAG_NAME);
        dimensionId = compound.getInteger(TAG_DIMENSION);
        center = ChunkCoordUtils.readFromNBT(compound, TAG_CENTER);

        //  Citizenry
        maxCitizens = compound.getInteger(TAG_MAX_CITIZENS);

        //  Owners
        NBTTagList ownerTagList = compound.getTagList(TAG_OWNERS, NBT.TAG_STRING);
        for (int i = 0; i < ownerTagList.tagCount(); ++i)
        {
            String owner = ownerTagList.getStringTagAt(i);
            owners.add(UUID.fromString(owner));
        }

        if (newSubscription)
        {
            buildings.clear();
        }
        else if (compound.hasKey(TAG_BUILDINGS_REMOVED))
        {
            NBTTagList buildingTagList = compound.getTagList(TAG_BUILDINGS_REMOVED, NBT.TAG_COMPOUND);
            for (int i = 0; i < buildingTagList.tagCount(); ++i)
            {
                ChunkCoordinates id = ChunkCoordUtils.readFromNBTTagList(buildingTagList, i);
                buildings.remove(id);
            }
        }

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

    public IMessage handleColonyViewCitizensPacket(NBTTagCompound compound)
    {
        int[] citizenIds = compound.getIntArray(TAG_CITIZENS);
        citizens.clear();
        for (int i : citizenIds)
        {
            citizens.add(i);
        }

        return null;
    }

    /**
     * Update a ColonyView's buildings given a network data ColonyView update packet
     * PLACEHOLDER - We will use eventually use PacketBuffers
     *
     * @param compound
     * @return
     */
    public IMessage handleColonyBuildingViewPacket(ChunkCoordinates buildingId, NBTTagCompound compound)
    {
        Building.View b = Building.createBuildingView(this, buildingId, compound);    //  At the moment we are re-using the save/load code
        if (b != null)
        {
            buildings.put(b.getLocation(), b);
        }

        return null;
    }

    /**
     * Determine if a given chunk coordinate is considered to be within the colony's bounds
     *
     * @param coord
     * @return
     */
    public boolean isCoordInColony(World w, ChunkCoordinates coord)
    {
        return isCoordInColony(w, coord.posX, coord.posY, coord.posZ);
    }

    public boolean isCoordInColony(World w, int x, int y, int z)
    {
        //  Perform a 2D distance calculation, so pass center.posY as the Y
        return w.provider.dimensionId == dimensionId &&
                center.getDistanceSquared(x, center.posY, z) <= Utils.square(Configurations.workingRangeTownhall);
    }

    public float getDistanceSquared(ChunkCoordinates coord)
    {
        return getDistanceSquared(coord.posX, coord.posY, coord.posZ);
    }

    public float getDistanceSquared(int posX, int posY, int posZ)
    {
        //  Perform a 2D distance calculation, so pass center.posY as the Y
        return center.getDistanceSquared(posX, center.posY, posZ);
    }
}
