package com.minecolonies.colony;

import com.minecolonies.MineColonies;
import com.minecolonies.colony.buildings.Building;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

import java.io.*;
import java.util.*;

public class ColonyManager {
    private Map<UUID, Colony> colonies = new HashMap<UUID, Colony>();
    private Map<Integer, List<Colony>> coloniesByWorld = new HashMap<Integer, List<Colony>>();

    private static ColonyManager instance = new ColonyManager();

    final static String FILENAME_MINECOLONIES_PATH = "minecolonies";
    final static String FILENAME_MINECOLONIES_EXT = ".dat";
    final static String FILENAME_MINECOLONIES = "colonies" + FILENAME_MINECOLONIES_EXT;

    final static String TAG_COLONIES = "colonies";

    public static ColonyManager instance()
    {
        return instance;
    }

    public static void release()
    {
        instance.colonies.clear();
    }

    public static void init()
    {
        Building.registerBuildings();
    }

    public ColonyManager()
    {
    }

    public Colony createColony(
            World w,
            ChunkCoordinates coord)
    {
        Colony colony = new Colony(w, coord);
        colonies.put(colony.getID(), colony);
        coloniesByWorld.get(w.provider.dimensionId).add(colony);
        return colony;
    }

    public Colony getColonyByCoord(
            World w,
            ChunkCoordinates coord)
    {
        //    TODO - Optimize this
        for (Colony c : colonies.values())
        {
            if (c.isCoordInColony(w, coord)) return c;
        }

        return null;
    }

    public Colony getColonyById(
            UUID id)
    {
        return colonies.get(id);
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

    public List<Colony> readFromNBT(
            World world,
            NBTTagCompound compound)
    {
        List<Colony> newColonies = new ArrayList<Colony>();
        NBTTagList colonyTags = compound.getTagList(TAG_COLONIES, NBT.TAG_COMPOUND);
        for (int i = 0; i < colonyTags.tagCount(); ++i)
        {
            Colony colony = Colony.createAndLoadColony(world, colonyTags.getCompoundTagAt(i));
            colonies.put(colony.getID(), colony);
            //colony.onWorldLoad();
            newColonies.add(colony);
        }
        return newColonies;
    }

    public void writeToNBT(
            World world,
            NBTTagCompound compound)
    {
        NBTTagList colonyTagList = new NBTTagList();
        for(Colony colony : colonies.values())
        {
            if (colony.getWorld() == world)
            {
                NBTTagCompound colonyTagCompound = new NBTTagCompound();
                if (colony.writeToNBT(colonyTagCompound))
                {
                    colonyTagList.appendTag(colonyTagCompound);
                }
            }
        }
        compound.setTag(TAG_COLONIES, colonyTagList);
    }

    private File getSaveLocation(
            World world)
    {
        //  DimensionManager.getWorld(0)
        File saveDir = new File(world.getSaveHandler().getWorldDirectory(), FILENAME_MINECOLONIES_PATH);

        String worldSaveFolder = world.provider.getSaveFolder();
        if (worldSaveFolder != null)
        {
            return new File(saveDir, worldSaveFolder + FILENAME_MINECOLONIES_EXT);
        }

        return new File(saveDir, FILENAME_MINECOLONIES);
    }

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

    public void onWorldLoad(World world)
    {
        List<Colony> worldColonies = new ArrayList<Colony>();

        File file = getSaveLocation(world);
        NBTTagCompound data = loadNBTFromPath(file);
        if (data != null)
        {
            List<Colony> newColonies = readFromNBT(world, data);
            worldColonies.addAll(newColonies);

            for (Colony c : newColonies)
            {
                c.onWorldLoad();
            }
        }

        coloniesByWorld.put(world.provider.dimensionId, worldColonies);
    }

    public void onWorldSave(World world)
    {
        NBTTagCompound compound = new NBTTagCompound();
        writeToNBT(world, compound);

        File file = getSaveLocation(world);
        saveNBTToPath(file, compound);
    }

    public void onWorldUnload(World world)
    {
        for (Colony c : colonies.values())
        {
            if (c.getWorld() == world)
            {
                c.onWorldUnload();
            }
        }
    }
}
