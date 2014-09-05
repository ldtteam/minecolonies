package com.minecolonies.colony;

import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.buildings.BuildingTownHall;
import com.minecolonies.lib.Constants;
import com.minecolonies.util.ChunkCoordUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

import java.lang.ref.WeakReference;
import java.util.*;

public class ColonyView
{
    private final UUID              id;
    private final int               dimensionId;

    //  General Attributes
    private String                  name   = "Unknown";
    private Set<UUID>               owners = new HashSet<UUID>();
    private WeakReference<World>    world;
    private ChunkCoordinates        center;

    //  Buildings
    private Map<ChunkCoordinates, Building.View> buildings = new HashMap<ChunkCoordinates, Building.View>();

    //  Citizenry
    private int       maxCitizens = Constants.DEFAULTMAXCITIZENS;
//    private Set<UUID> citizens    = new HashSet<UUID>();

    final static String TAG_ID           = "id";
    final static String TAG_NAME         = "name";
    final static String TAG_DIMENSION    = "dimension";
    final static String TAG_CENTER       = "center";
    final static String TAG_MAX_CITIZENS = "maxCitizens";
    final static String TAG_OWNERS       = "owners";
    final static String TAG_BUILDINGS    = "buidings";
    final static String TAG_CITIZENS     = "citizens";


    /**
     * Base constructor for a colony.
     *
     * @param uuid The current id for the colony
     * @param dim  The world the colony exists in
     */
    protected ColonyView(UUID uuid, int dim)
    {
        id = uuid;
        dimensionId = dim;
    }

    public UUID getID() { return id; }

    public int getDimensionId() { return dimensionId; }
    public World getWorld() { return world != null ? world.get() : null; }

    public String getName() { return name; }
    public void setName(String name) { /* CJJ TODO */ }

    public Building.View getBuilding(ChunkCoordinates buildingId) { return buildings.get(buildingId); }

    public Set<UUID> getOwners() { return Collections.unmodifiableSet(owners); }
    public boolean isOwner(UUID o) { return owners.contains(o); }
    public void addOwner(UUID o) { owners.add(o); }
    public void removeOwner(UUID o) { owners.remove(o); }

    /**
     * When the ColonyView's world is loaded, associate with it
     *
     * @param w
     */
    public void onWorldLoad(World w)
    {
        if (w.provider.dimensionId == dimensionId)
        {
            world = new WeakReference<World>(w);
        }
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
        compound.setString(TAG_ID, colony.getID().toString());
        compound.setInteger(TAG_DIMENSION, colony.getDimensionId());

        //  General Attributes
        compound.setString(TAG_NAME, colony.getName());
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

        //  Buildings
        NBTTagList buildingTagList = new NBTTagList();
        for (Building building : colony.getBuildings().values())
        {
            NBTTagCompound buildingCompound = new NBTTagCompound();
            building.writeToNBT(buildingCompound);
            buildingTagList.appendTag(buildingCompound);
        }
        compound.setTag(TAG_BUILDINGS, buildingTagList);
    }

    /**
     * Create a ColonyView given the network data
     * PLACEHOLDER - We will use eventually use PacketBuffers
     *
     * @param compound
     * @return
     */
    static public ColonyView createFromNetworkData(NBTTagCompound compound)
    {
        UUID id = UUID.fromString(compound.getString(TAG_ID));
        int dimensionId = compound.getInteger(TAG_DIMENSION);

        ColonyView view = new ColonyView(id, dimensionId);
        view.populateFromNetworkData(compound);
        return view;
    }

    /**
     * Populate a ColonyView from the network data
     * PLACEHOLDER - We will use eventually use PacketBuffers
     *
     * @param compound
     */
    private void populateFromNetworkData(NBTTagCompound compound)
    {
        //  General Attributes
        name = compound.getString(TAG_NAME);
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

        updateFromNetworkData(compound);
    }

    /**
     * Update a ColonyView's buildings given a network data ColonyView update packet
     * PLACEHOLDER - We will use eventually use PacketBuffers
     *
     * @param compound
     * @return
     */
    public void updateFromNetworkData(NBTTagCompound compound)
    {
        //  Update buildings by replacing them in-place; does not remove any buildings (a new ColonyView is required for that)
        if (compound.hasKey(TAG_BUILDINGS))
        {
            NBTTagList buildingTagList = compound.getTagList(TAG_BUILDINGS, NBT.TAG_COMPOUND);
            for (int i = 0; i < buildingTagList.tagCount(); ++i)
            {
                NBTTagCompound buildingCompound = buildingTagList.getCompoundTagAt(i);
                Building.View b = Building.createBuildingView(null, buildingCompound);    //  At the moment we are re-using the save/load code
                if (b != null)
                {
                    buildings.put(b.getLocation(), b);
                }
            }
        }
    }
}
