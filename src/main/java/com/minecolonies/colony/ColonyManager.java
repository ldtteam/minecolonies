package com.minecolonies.colony;

import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.configuration.Configurations;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
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
    private static Map<UUID, Colony> colonies = new HashMap<UUID, Colony>();
    private static Map<Integer, List<Colony>> coloniesByWorld = new HashMap<Integer, List<Colony>>();

    private static Map<UUID, ColonyView> colonyViews = new HashMap<UUID, ColonyView>();

    private static int numWorldsLoaded;    //  Used to trigger loading/unloading colonies

    private final static String FILENAME_MINECOLONIES_PATH = "minecolonies";
    private final static String FILENAME_MINECOLONIES = "colonies.dat";

    private final static String TAG_COLONIES = "colonies";

    public static void init()
    {
    }

    /**
     * Create a new Colony in the given world and at that location
     *
     * @param w
     * @param coord
     * @return
     */
    public static Colony createColony(
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
    public static Colony getColonyById(UUID id) { return colonies.get(id); }

    /**
     * Get Colony that contains a given ChunkCoordinates
     *
     * @param w
     * @param coord
     * @return
     */
    public static Colony getColonyByCoord(World w, ChunkCoordinates coord)
    {
        return getColonyByCoord(w, coord.posX, coord.posY, coord.posZ);
    }


    public static Colony getColonyByCoord(World w, int x, int y, int z)
    {
        List<Colony> coloniesInWorld = coloniesByWorld.get(w.provider.dimensionId);
        if (coloniesInWorld == null) return null;

        for (Colony c : coloniesInWorld)
        {
            if (c.isCoordInColony(w, x, y, z)) return c;
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
    public static Colony getClosestColony(World w, ChunkCoordinates coord)
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
    public static Colony getClosestColony(World w, int x, int y, int z)
    {
        List<Colony> coloniesInWorld = coloniesByWorld.get(w.provider.dimensionId);
        if (coloniesInWorld == null) return null;

        Colony closestColony = null;
        float closestDist = Float.MAX_VALUE;

        for (Colony c : coloniesInWorld)
        {
            if (c.getDimensionId() == w.provider.dimensionId)
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

    public static List<Colony> getColoniesByOwner(UUID owner)
    {
        List<Colony> results = new ArrayList<Colony>();

        for (Colony c : colonies.values())
        {
            if (c.getPermissions().getOwner().equals(owner))//TODO is this what we want? Also improve
            {
                results.add(c);
            }
        }

        return results;
    }

    /**
     * Get a Building by a World and coordinates
     *
     * @param w
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static Building getBuilding(World w, int x, int y, int z)
    {
        ChunkCoordinates coords = new ChunkCoordinates(x, y, z);
        Colony colony = getColonyByCoord(w, coords);
        if (colony != null)
        {
            Building building = colony.getBuilding(coords);
            if (building != null)
            {
                return building;
            }
        }

        //  Fallback - there might be a Building for this block, but it's outside of it's owning colony's radius
        if (coloniesByWorld.containsKey(w.provider.dimensionId))
        {
            for (Colony otherColony : coloniesByWorld.get(w.provider.dimensionId))
            {
                Building building = otherColony.getBuilding(coords);
                if (building != null)
                {
                    return building;
                }
            }
        }

        return null;
    }

    /**
     * Get a Building by a World and coordinates
     *
     * @param w
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static Building.View getBuildingView(World w, int x, int y, int z)
    {
        //  On client we will just check all known views
        ChunkCoordinates coords = new ChunkCoordinates(x, y, z);
        for (ColonyView colony : colonyViews.values())
        {
            Building.View building = colony.getBuilding(coords);
            if (building != null)
            {
                return building;
            }
        }

        return null;
    }

    /**
     * Get ColonyView by UUID
     *
     * @param id UUID of colony
     * @return
     */
    public static ColonyView getColonyView(UUID id)
    {
        return colonyViews.get(id);
    }

    /**
     * Get Colony that contains a given ChunkCoordinates
     *
     * @param w
     * @param coord
     * @return
     */
    public static ColonyView getColonyView(World w, ChunkCoordinates coord)
    {
        return getColonyView(w, coord.posX, coord.posY, coord.posZ);
    }


    public static ColonyView getColonyView(World w, int x, int y, int z)
    {
        for (ColonyView c : colonyViews.values())
        {
            if (c.isCoordInColony(w, x, y, z)) return c;
        }

        return null;
    }

    public static ColonyView getClosestColonyView(World w, int x, int y, int z)
    {
        ColonyView closestColony = null;
        float closestDist = Float.MAX_VALUE;

        for (ColonyView c : colonyViews.values())
        {
            if (c.getDimensionId() == w.provider.dimensionId)
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

    public static List<ColonyView> getColonyViewsOwnedByPlayer(EntityPlayer player)
    {
        List<ColonyView> results = new ArrayList<ColonyView>();

        for (ColonyView c : colonyViews.values())
        {
            if (c.getPlayers().get(player.getGameProfile().getId()).equals(Permissions.Rank.OWNER))//TODO update for permissions
            {
                results.add(c);
            }
        }

        return results;
    }

    public static double getMinimumDistanceBetweenTownHalls()
    {
        //  [Townhall](Radius)+(Padding)+(Radius)[TownHall]
        return (2 * Configurations.workingRangeTownhall) + Configurations.townhallPadding;
    }

    /**
     * On server tick, tick every Colony
     * NOTE: Review this for performance
     *
     * @param event
     */
    public static void onServerTick(
            TickEvent.ServerTickEvent event)
    {
        for (Colony c : colonies.values())
        {
            c.onServerTick(event);
        }
    }

    public static void onClientTick(
            TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            if (Minecraft.getMinecraft().theWorld == null && !colonyViews.isEmpty())
            {
                //  Player has left the game, clear the Colony View cache
                colonyViews.clear();
            }
        }
    }

    /**
     * On world tick, tick every Colony in that world
     * NOTE: Review this for performance
     *
     * @param event
     */
    public static void onWorldTick(
            TickEvent.WorldTickEvent event)
    {
        for (Colony c : colonies.values())
        {
            if (c.getDimensionId() == event.world.provider.dimensionId)
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
    public static void readFromNBT(
            NBTTagCompound compound)
    {
        NBTTagList colonyTags = compound.getTagList(TAG_COLONIES, NBT.TAG_COMPOUND);
        for (int i = 0; i < colonyTags.tagCount(); ++i)
        {
            Colony colony = Colony.loadColony(colonyTags.getCompoundTagAt(i));
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
    public static void writeToNBT(
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
    private static File getSaveLocation(
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
    private static NBTTagCompound loadNBTFromPath(
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
    private static void saveNBTToPath(
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
    public static void onWorldLoad(World world)
    {
        if (!world.isRemote)
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
        else
        {
            for (ColonyView v : colonyViews.values())
            {
                v.onWorldLoad(world);
            }
        }
    }

    public static void onWorldSave(World world)
    {
        if (!world.isRemote &&
            world.provider.dimensionId == 0)    //  For now, save when 0 saves...
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
    public static void onWorldUnload(World world)
    {
        if (!world.isRemote)
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

    /**
     *
     * @param colonyId
     * @param colonyData
     */
    static public IMessage handleColonyViewPacket(UUID colonyId, NBTTagCompound colonyData, boolean isNewSubscription)
    {
        ColonyView view = getColonyView(colonyId);
        if (view == null)
        {
            view = ColonyView.createFromNBT(colonyId, colonyData);
            colonyViews.put(colonyId, view);
        }

        return view.handleColonyViewPacket(colonyData, isNewSubscription);
    }

    public static IMessage handlePermissionsViewPacket(UUID colonyID, NBTTagCompound data)
    {
        ColonyView view = getColonyView(colonyID);
        if(view != null)
        {
            return view.handlePermissionsViewPacket(data);
        }
        else
        {
            //TODO log, error
            return null;
        }
    }

    /**
     *
     * @param colonyId
     * @param colonyData
     */
    static public IMessage handleColonyViewCitizensPacket(UUID colonyId, UUID citizenId, NBTTagCompound colonyData)
    {
        ColonyView view = getColonyView(colonyId);
        if (view != null)
        {
            return view.handleColonyViewCitizensPacket(citizenId, colonyData);
        }

        return null;
    }

    /**
     *
     * @param colonyId The ID of the colony
     * @param buildingData The building data, or null if it was removed
     */
    static public IMessage handleColonyBuildingViewPacket(UUID colonyId, ChunkCoordinates buildingId, NBTTagCompound buildingData)
    {
        ColonyView view = getColonyView(colonyId);
        if (view != null)
        {
            return view.handleColonyBuildingViewPacket(buildingId, buildingData);
        }
        else
        {
            //  TODO - Log this.  We should have the colony
            return null;
        }
    }
}
