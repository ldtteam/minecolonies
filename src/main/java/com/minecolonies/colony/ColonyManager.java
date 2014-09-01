package com.minecolonies.colony;

import com.minecolonies.MineColonies;
import com.minecolonies.colony.buildings.Building;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants.NBT;

import java.io.*;
import java.util.*;

public class ColonyManager {
    private Map<UUID, Colony> colonies = new HashMap<UUID, Colony>();
    private Map<Integer, List<Colony>> coloniesByWorld = new HashMap<Integer, List<Colony>>();

    private int numWorldsLoaded;    //  Used to trigger loading/unloading colonies

    private static ColonyManager instance = new ColonyManager();

    final static String FILENAME_MINECOLONIES_PATH = "minecolonies";
    final static String FILENAME_MINECOLONIES = "colonies.dat";

    final static String TAG_COLONIES = "colonies";

    public static ColonyManager instance()
    {
        return instance;
    }

    public static void init()
    {
        Building.init();
    }

    private ColonyManager()
    {
    }

    /**
     * Create a new Colony in the given world and at that location
     *
     * @param w
     * @param coord
     * @return
     */
    public Colony createColony(
            World w,
            ChunkCoordinates coord)
    {
        Colony colony = new Colony(w, coord);
        colonies.put(colony.getID(), colony);

        if (!coloniesByWorld.containsKey(colony.getDimensionId()))
        {
            coloniesByWorld.put(colony.getDimensionId(), new ArrayList<Colony>());
        }

        coloniesByWorld.get(colony.getDimensionId()).add(colony);
        return colony;
    }

    /**
     * Get Colony by UUID
     *
     * @param id UUID of colony
     * @return
     */
    public Colony getColonyById(UUID id) { return colonies.get(id); }

    /**
     * Get Colony that contains a given ChunkCoordinates
     *
     * @param w
     * @param coord
     * @return
     */
    public Colony getColonyByCoord(
            World w,
            ChunkCoordinates coord)
    {
        List<Colony> coloniesInWorld = coloniesByWorld.get(w.provider.dimensionId);
        if (coloniesInWorld == null) return null;

        for (Colony c : coloniesInWorld)
        {
            if (c.isCoordInColony(w, coord)) return c;
        }

        return null;
    }

    /**
     * Get closest colony by ChunkCoordinate
     *
     * @param w
     * @param coord
     * @return
     */
    public Colony getClosestColony(World w, ChunkCoordinates coord)
    {
        return getClosestColony(w, coord.posX, coord.posY, coord.posZ);
    }

    /**
     * Get closest colony by x,y,z
     *
     * @param w
     * @param x
     * @param y
     * @param z
     * @return
     */
    public Colony getClosestColony(World w, int x, int y, int z)
    {
        List<Colony> coloniesInWorld = coloniesByWorld.get(w.provider.dimensionId);
        if (coloniesInWorld == null) return null;

        Colony closestColony = null;
        float closestDist = Float.MAX_VALUE;

        for (Colony c : coloniesInWorld)
        {
            if (c.getWorld() == w)
            {
                float dist = c.getDistanceSquared(x, y, z);
                if (dist < closestDist)
                {
                    closestColony = c;
                    closestDist = dist;
                }
            }
        }

        return closestColony;
    }

    public void onServerTick(
            TickEvent.ServerTickEvent event)
    {
        for (Colony c : colonies.values())
        {
            c.onServerTick(event);
        }
    }

    public void onWorldTick(
            TickEvent.WorldTickEvent event)
    {
        for (Colony c : colonies.values())
        {
            if (c.getWorld() == event.world)
            {
                c.onWorldTick(event);
            }
        }
    }

    /**
     * Read Colonies from saved NBT data
     *
     * @param compound
     */
    public void readFromNBT(
            NBTTagCompound compound)
    {
        NBTTagList colonyTags = compound.getTagList(TAG_COLONIES, NBT.TAG_COMPOUND);
        for (int i = 0; i < colonyTags.tagCount(); ++i)
        {
            Colony colony = Colony.createAndLoadColony(colonyTags.getCompoundTagAt(i));
            colonies.put(colony.getID(), colony);

            if (!coloniesByWorld.containsKey(colony.getDimensionId()))
            {
                coloniesByWorld.put(colony.getDimensionId(), new ArrayList<Colony>());
            }
            coloniesByWorld.get(colony.getDimensionId()).add(colony);
        }
    }

    /**
     * Write colonies to NBT data for saving
     *
     * @param compound
     */
    public void writeToNBT(
            NBTTagCompound compound)
    {
        NBTTagList colonyTagList = new NBTTagList();
        for(Colony colony : colonies.values())
        {
            NBTTagCompound colonyTagCompound = new NBTTagCompound();
            colony.writeToNBT(colonyTagCompound);
            colonyTagList.appendTag(colonyTagCompound);
        }
        compound.setTag(TAG_COLONIES, colonyTagList);
    }

    /**
     * Get save location for Minecolonies data, from the world/save directory
     *
     * @param world
     * @return
     */
    private File getSaveLocation(
            World world)
    {
        File saveDir = new File(DimensionManager.getWorld(0).getSaveHandler().getWorldDirectory(), FILENAME_MINECOLONIES_PATH);
        return new File(saveDir, FILENAME_MINECOLONIES);
    }

    /**
     * Load a file and return the data as an NBTTagCompound
     *
     * @param file The path to the file
     * @return the data from the file as an NBTTagCompound, or null
     */
    private NBTTagCompound loadNBTFromPath(
            File file)
    {
        try
        {
            if (file != null && file.exists())
            {
                return CompressedStreamTools.read(file);
//                return CompressedStreamTools.readCompressed(new FileInputStream(file));
            }
        }
        catch (IOException exception)
        {
            //  TODO LOG
        }
        return null;
    }

    /**
     * Save an NBTTagCompound to a file.  Does so in a safe manner using an intermediate tmp file
     *
     * @param file The destination file to write the data to
     * @param compound The NBTTagCompound to write to the file
     */
    private void saveNBTToPath(
            File file,
            NBTTagCompound compound)
    {
        try
        {
            if (file != null)
            {
                file.getParentFile().mkdir();
                CompressedStreamTools.safeWrite(compound, file);

//                File tempFile = new File(file.getAbsolutePath() + "_tmp");
//                tempFile.delete();
//
//                CompressedStreamTools.writeCompressed(compound, new DataOutputStream(new FileOutputStream(tempFile)));
//
//                file.delete();
//                tempFile.renameTo(file);
            }
        }
        catch (IOException exception)
        {
            //  TODO LOG
        }
    }

    /**
     * When a world is loaded, Colonies in that world need to grab the reference to the World
     * Additionally, when loading the first world, load all colonies.
     *
     * @param world
     */
    public void onWorldLoad(World world)
    {
        if (numWorldsLoaded == 0)
        {
            File file = getSaveLocation(world);
            NBTTagCompound data = loadNBTFromPath(file);
            if (data != null)
            {
                readFromNBT(data);
            }
        }
        ++numWorldsLoaded;

        List<Colony> worldColonies = coloniesByWorld.get(world.provider.dimensionId);
        if (worldColonies != null)
        {
            for (Colony c : worldColonies)
            {
                c.onWorldLoad(world);
            }
        }
    }

    public void onWorldSave(World world)
    {
        if (world.provider.dimensionId == 0)    //  For now, save when 0 saves...
        {
            NBTTagCompound compound = new NBTTagCompound();
            writeToNBT(compound);

            File file = getSaveLocation(world);
            saveNBTToPath(file, compound);
        }
    }

    /**
     * When a world unloads, all colonies in that world are informed
     * Additionally, when the last world is unloaded, delete all colonies
     *
     * @param world
     */
    public void onWorldUnload(World world)
    {
        List<Colony> worldColonies = coloniesByWorld.get(world.provider.dimensionId);
        if (worldColonies != null)
        {
            for (Colony c : worldColonies)
            {
                c.onWorldUnload(world);
            }
        }

        --numWorldsLoaded;
        if (numWorldsLoaded == 0)
        {
            colonies.clear();
            coloniesByWorld.clear();
        }
    }
}
