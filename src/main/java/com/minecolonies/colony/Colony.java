package com.minecolonies.colony;

import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.buildings.BuildingTownHall;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.lib.Constants;
import com.minecolonies.util.ChunkCoordUtils;
import com.minecolonies.util.Utils;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

import java.lang.ref.WeakReference;
import java.util.*;

public class Colony {
    private final UUID  id;
    //private final int   dimension;

    private String      name = "ERROR(Wasn't placed by player)";
    private Set<UUID>   owners = new HashSet<UUID>();

    private final WeakReference<World> world;
    private ChunkCoordinates center;

    //  Buildings
    private BuildingTownHall townhall;
    private Map<ChunkCoordinates, Building> buildings = new HashMap<ChunkCoordinates, Building>();

    //  Citizenry
    private int         maxCitizens = Constants.DEFAULTMAXCITIZENS;
    private Set<UUID>   citizens = new HashSet<UUID>();

    final static String TAG_ID = "id";
    final static String TAG_NAME = "name";
    final static String TAG_DIMENSION = "dimension";
    final static String TAG_CENTER = "center";
    final static String TAG_MAX_CITIZENS = "maxCitizens";
    final static String TAG_OWNERS = "owners";
    final static String TAG_BUILDINGS = "buidings";
    final static String TAG_CITIZENS = "citizens";

    /**
     * Constructor for a brand new Colony.
     */
    public Colony(
            World w,
            ChunkCoordinates c)
    {
        this(UUID.randomUUID(), w);
        center = c;
    }

    /**
     * Constructor for a colony.
     * @param uuid
     */
    protected Colony(
            UUID uuid,
            World w)
    {
        id = uuid;
        world = new WeakReference<World>(w);
    }

    /**
     * Load a saved colony
     * @param compound
     * @return loaded colony
     */
    public static Colony createAndLoadColony(
            World world,
            NBTTagCompound compound)
    {
        UUID id = UUID.fromString(compound.getString(TAG_ID));
        Colony c = new Colony(id, world);
        c.readFromNBT(compound);
        return c;
    }

    /**
     * Read colony from saved data
     * @param compound
     */
    protected void readFromNBT(
            NBTTagCompound compound)
    {
        name = compound.getString(TAG_NAME);
        //dimension = compound.getInteger(TAG_DIMENSION);
        center = ChunkCoordUtils.readFromNBT(compound, TAG_CENTER);

        maxCitizens = compound.getInteger(TAG_MAX_CITIZENS);

        //  Owners
        NBTTagList ownerTagList = compound.getTagList(TAG_OWNERS, NBT.TAG_STRING);
        for (int i = 0; i < ownerTagList.tagCount(); ++i)
        {
            String owner = ownerTagList.getStringTagAt(i);
            owners.add(UUID.fromString(owner));
        }

        //  Buildings
        NBTTagList buildingTagList = compound.getTagList(TAG_BUILDINGS, NBT.TAG_COMPOUND);
        for (int i = 0; i < buildingTagList.tagCount(); ++i)
        {
            NBTTagCompound buildingCompound = buildingTagList.getCompoundTagAt(i);
            Building b = Building.createAndLoadBuilding(this, buildingCompound);
            if (b != null)
            {
                addBuilding(b);
            }
        }

        //  Citizens
        NBTTagList citizenTagList = compound.getTagList(TAG_CITIZENS, NBT.TAG_STRING);
        for (int i = 0; i < citizenTagList.tagCount(); ++i)
        {
            String owner = citizenTagList.getStringTagAt(i);
            citizens.add(UUID.fromString(owner));
        }
    }

    /**
     * Write colony to save data
     * @param compound
     */
    public boolean writeToNBT(
            NBTTagCompound compound)
    {
        World worldActual = world.get();
        if (worldActual == null)
        {
            return false;
        }

        compound.setString(TAG_ID, id.toString());
        compound.setString(TAG_NAME, name);
        compound.setInteger(TAG_DIMENSION, worldActual.provider.dimensionId);
        ChunkCoordUtils.writeToNBT(compound, TAG_CENTER, center);

        compound.setInteger(TAG_CITIZENS, maxCitizens);

        //  Owners
        NBTTagList ownerTagList = new NBTTagList();
        for(UUID owner : owners)
        {
            ownerTagList.appendTag(new NBTTagString(owner.toString()));
        }
        compound.setTag(TAG_OWNERS, ownerTagList);

        //  Buildings
        NBTTagList buildingTagList = new NBTTagList();
        for (Building b : buildings.values())
        {
            NBTTagCompound buildingCompound = new NBTTagCompound();
            b.writeToNBT(buildingCompound);
            buildingTagList.appendTag(buildingCompound);
        }
        compound.setTag(TAG_BUILDINGS, buildingTagList);

        //  Citizens
        NBTTagList citizenTagList = new NBTTagList();
        for(UUID citizen : citizens)
        {
            citizenTagList.appendTag(new NBTTagString(citizen.toString()));
        }
        compound.setTag(TAG_CITIZENS, citizenTagList);

        return true;
    }

    public UUID getID() { return id; }

    //public int getDimension() { return dimension; }
    public World getWorld() { return world.get(); }

    public String getName() { return name; }
    public void setName(String n)
    {
        name = n;
    }

    public Set<UUID> getOwners() { return Collections.unmodifiableSet(owners); }
    public boolean isOwner(UUID o) { return owners.contains(o); }
    public void addOwner(UUID o) { owners.add(o); }
    public void removeOwner(UUID o) { owners.remove(o); }

    public int getMaxCitizens() { return maxCitizens; }
    //public void setMaxCitizens();

    public Set<UUID> getCitizens() { return Collections.unmodifiableSet(citizens); }
    public boolean isCitizen(UUID c) { return citizens.contains(c); }

    public final ChunkCoordinates getCenter() { return center; }

    /**
     * Get citizen in Colony by ID
     * @param citizenId
     * @return
     */
    //public String getCitizen(UUID citizenId) {
    //    return citizens.get(citizenId);
    //}

    /**
     * Determine if a given chunk coordinate is considered to be within the colony's bounds
     * @param coord
     * @return
     */
    public boolean isCoordInColony(
            World w,
            ChunkCoordinates coord)
    {
        return w == world.get() &&
                center.getDistanceSquaredToChunkCoordinates(coord) <= Utils.square(Configurations.workingRangeTownhall);
    }

    public void onWorldLoad()
    {
        //  Nothing for now

//        if (w.provider.dimensionId == dimension)
//        {
//            world = new WeakReference<World>(w);
//        }
    }

    public void onWorldUnload()
    {
        //  Nothing for now
    }

    /**
     * Any per-server-tick logic should be performed here
     * @param event
     */
    public void onServerTick(
            TickEvent.ServerTickEvent event)
    {
    }

    /**
     * Any per-world-tick logic should be performed here
     * @param event
     */
    public void onWorldTick(
            TickEvent.WorldTickEvent event)
    {
    }

    /**
     * Get building in Colony by ID
     * @param buildingId
     * @return
     */
    public Building getBuilding(
            ChunkCoordinates buildingId)
    {
        return buildings.get(buildingId);
    }

    public void addBuilding(
            Building building)
    {
        buildings.put(building.GetLocation(), building);

        if (building instanceof BuildingTownHall)
        {
            //  Limit 1 town hall
            if (townhall == null)
            {
                townhall = (BuildingTownHall)building;
            }
        }
    }
}
