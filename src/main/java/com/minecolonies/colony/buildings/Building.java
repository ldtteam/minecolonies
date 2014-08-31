package com.minecolonies.colony.buildings;

import com.minecolonies.colony.Colony;
import com.minecolonies.util.ChunkCoordUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import com.minecolonies.colony.buildings.*;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class Building {
    private final ChunkCoordinates location;
    private final WeakReference<Colony> colony;

    private int                buildingLevel;

    private static Map<String, Class<?>> nameToClassMap = new HashMap<String, Class<?>>();
    private static Map<Class<?>, String> classToNameMap = new HashMap<Class<?>, String>();
    //private static Map<Class<?>, Class<?>> tileEntityClassToBuildingClassMap = new HashMap<Class<?>, Class<?>>();

    final static String TAG_TYPE = "type";
    final static String TAG_ID = "id";
    final static String TAG_LOCATION = "location";  //  Location is unique (within a Colony) and so can double as the Id

    /**
     * Add a given Building mapping
     * @param c class of object
     * @param name name of object
     */
    private static void addMapping(Class<?> c, String name)
    {
        if (nameToClassMap.containsKey(name))
        {
            throw new IllegalArgumentException("Duplicate type '" + name + "' when adding Building class mapping");
        }
        else
        {
            try
            {
                if (c.getDeclaredConstructor(Colony.class, ChunkCoordinates.class) != null)
                {
                    nameToClassMap.put(name, c);
                    classToNameMap.put(c, name);
                }
            }
            catch (NoSuchMethodException exception)
            {
                throw new IllegalArgumentException("Missing constructor for type '" + name + "' when adding Building class mapping");
            }
        }
    }

    public static void registerBuildings()
    {
        //addMapping(BuildingBaker.class, "Baker");
        //addMaping(BuildingBlacksmith.class, "Blacksmith");
        addMapping(BuildingBuilder.class, "Builder");
        //addMapping(BuildingCitizen.class, "Citizen");
        //addMapping(BuildingFarmer.class, "Farmer");
        //addMapping(BuildingLumberjack.class, "Lumberjack");
        //addMapping(BuildingMiner.class, "Miner");
        //addMapping(BuildingStonemason.class, "Stonemason");
        addMapping(BuildingTownHall.class, "Townhall");
        //addMapping(BuildingWarehouse.class, "Warehouse");
        //addMapping(BuildingWorker.class, "Worker");
    }

    protected Building(
            Colony c,
            ChunkCoordinates l)
    {
        location = new ChunkCoordinates(l);
        colony = new WeakReference<Colony>(c);
    }

    /**
     * Create and load a Building given it's saved NBTTagCompound
     * @param colony The owning colony
     * @param compound The saved data
     * @return
     */
    public static Building createAndLoadBuilding(
            Colony colony,
            NBTTagCompound compound)
    {
        Building building = null;
        Class<?> oclass = null;

        try
        {
            oclass = nameToClassMap.get(compound.getString(TAG_TYPE));

            if (oclass != null)
            {
                //UUID id = UUID.fromString(compound.getString("id"));
                ChunkCoordinates loc = ChunkCoordUtils.readFromNBT(compound, TAG_LOCATION);
                building = (Building)oclass.getDeclaredConstructor(Colony.class, ChunkCoordinates.class)
                        .newInstance(colony, loc);
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        if (building != null)
        {
            try
            {
                building.readFromNBT(compound);
            }
            catch (Exception ex)
            {
                //FMLLog.log(Level.ERROR, ex,
                //        "A Buidling %s(%s) has thrown an exception during loading, its state cannot be restored. Report this to the mod author",
                //        compound.getString(TAG_TYPE), oclass.getName());
                building = null;
            }
        }
        else
        {
            //logger.warn("Skipping unknown building of type " + compound.getString(TAG_TYPE));
        }

        return building;
    }

    public void readFromNBT(
            NBTTagCompound compound)
    {
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        String s = (String)classToNameMap.get(this.getClass());

        if (s == null)
        {
            throw new RuntimeException(this.getClass() + " is missing a mapping! This is a bug!");
        }
        else
        {
            compound.setString(TAG_TYPE, s);
            ChunkCoordUtils.writeToNBT(compound, TAG_LOCATION, location);
        }
    }

    //public UUID GetID() { return loc; }

    public ChunkCoordinates GetLocation() { return location; }

    public Colony GetColony() { return colony.get(); }
}
