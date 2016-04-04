package com.minecolonies.colony;

import com.minecolonies.MineColonies;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.EntityCitizen;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.IWorldAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants.NBT;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ColonyManager
{
    private static          Map<Integer, Colony>       colonies                     = new HashMap<>();
    private static          Map<Integer, List<Colony>> coloniesByWorld              = new HashMap<>();
    private static          int                        topColonyId                  = 0;

    private static          Map<Integer, ColonyView>   colonyViews                  = new HashMap<>();

    private static          int                         numWorldsLoaded;    //  Used to trigger loading/unloading colonies
    private static          boolean                     saveNeeded;

    private static final    String                      FILENAME_MINECOLONIES_PATH  = "minecolonies";
    private static final    String                      FILENAME_MINECOLONIES       = "colonies.dat";
    private static final    String                      TAG_COLONIES                = "colonies";

    /**
     * Create a new Colony in the given world and at that location
     *
     * @param w         World of the colony
     * @param coord     Coordinate of the center of the colony
     * @return          The created colony
     */
    public static Colony createColony(World w, ChunkCoordinates coord)
    {
        Colony colony = new Colony(++topColonyId, w, coord);
        colonies.put(colony.getID(), colony);

        if (!coloniesByWorld.containsKey(colony.getDimensionId()))
        {
            coloniesByWorld.put(colony.getDimensionId(), new ArrayList<>());
        }

        coloniesByWorld.get(colony.getDimensionId()).add(colony);

        markDirty();

        MineColonies.logger.info(String.format("New Colony %d", colony.getID()));

        return colony;
    }

    /**
     * Get Colony by UUID
     *
     * @param id    ID of colony
     * @return      Colony with given ID
     */
    public static Colony getColony(int id) { return colonies.get(id); }

    /**
     * Get Colony that contains a given ChunkCoordinates
     *
     * @param w         World
     * @param coord     Coordinate of a place in the colony to get
     * @return          Colony at the given location
     */
    public static Colony getColony(World w, ChunkCoordinates coord)
    {
        return getColony(w, coord.posX, coord.posY, coord.posZ);
    }

    /**
     * Get colony that contains a given coordinate
     *
     * @param w     World
     * @param x     x-coordinate
     * @param y     y-coordiante
     * @param z     z-coordinate
     * @return      Colony at the given location
     */
    public static Colony getColony(World w, int x, int y, int z)
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
     * @param w         World
     * @param coord     coordinates to get closes colony by
     * @return          Colony closest to coordinates
     */
    public static Colony getClosestColony(World w, ChunkCoordinates coord)
    {
        return getClosestColony(w, coord.posX, coord.posY, coord.posZ);
    }

    /**
     * Get closest colony by x,y,z
     *
     * @param w     World
     * @param x     x-coordinate
     * @param y     y-coordinate
     * @param z     z-coordinate
     * @return      Colony closest to coordinates
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

    /**
     * Returns a list of colonies that has the given owner
     *
     * @param owner     UUID of the owner
     * @return          List of colonies that belong to given owner UUID
     */
    public static List<Colony> getColoniesByOwner(UUID owner)
    {

        //TODO is this what we want? Also improve

        return colonies.values().stream()
                       .filter(c -> c.getPermissions().getOwner().equals(owner))
                       .collect(Collectors.toList());
    }

    /**
     * Get a Building by a World and coordinates
     *
     * @param w     World
     * @param x     x-coordinate
     * @param y     y-coordinate
     * @param z     z-coordinate
     * @return      Building at the given location
     */
    public static Building getBuilding(World w, int x, int y, int z)
    {
        ChunkCoordinates coords = new ChunkCoordinates(x, y, z);
        Colony colony = getColony(w, coords);
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
     * @param w     World
     * @param x     x-coordinate
     * @param y     y-coordinate
     * @param z     z-coordinate
     * @return      Returns the view belonging to the building at (x, y, z)
     */
    public static Building.View getBuildingView(World w, int x, int y, int z)
    //todo why do we have a world object if we dont use it
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
     * Get ColonyView by ID
     *
     * @param id    ID of colony
     * @return      The ColonyView belonging to the colony
     */
    public static ColonyView getColonyView(int id)
    {
        return colonyViews.get(id);
    }

    /**
     * Get Colony that contains a given ChunkCoordinates
     *
     * @param w         World
     * @param coord     Coordinates
     * @return          returns the view belonging to the colony at given chunk coordinates
     */
    public static ColonyView getColonyView(World w, ChunkCoordinates coord)
    {
        return getColonyView(w, coord.posX, coord.posY, coord.posZ);
    }


    /**
     * Get Colony that contains a given (x, y, z)
     *
     * @param w         World
     * @param x         x-coordinate
     * @param y         y-coordinate
     * @param z         z-coordinate
     * @return          returns the view belonging to the colony at x, y, z,
     */
    public static ColonyView getColonyView(World w, int x, int y, int z)
    {
        for (ColonyView c : colonyViews.values())
        {
            if (c.isCoordInColony(w, x, y, z)) return c;
        }

        return null;
    }

    /**
     * Returns the closest view
     * @see {@link #getColonyView(World, int, int, int)}
     *
     * @param w     World
     * @param x     x-coordinate
     * @param y     y-coordinate
     * @param z     z-coordinate
     * @return      View of the closest colony
     */
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

    /**
     * Returns list of views of colony with specific owner
     *
     * @param player        EntityPlayer
     * @return              List of colony views
     */
    public static List<ColonyView> getColonyViewsByOwner(EntityPlayer player)
    {
        return getColonyViewsByOwner(player.getGameProfile().getId());
    }

    /**
     * Returns a list of views of colonies with specific owner
     *
     * @param owner     UUID of the owner
     * @return          List of colony views
     */
    public static List<ColonyView> getColonyViewsByOwner(UUID owner)
    {
        List<ColonyView> results = new ArrayList<>();

        for (ColonyView c : colonyViews.values())
        {
            Permissions.Player p = c.getPlayers().get(owner);
            if (p != null && p.rank.equals(Permissions.Rank.OWNER))
            {
                results.add(c);
            }
        }

        return results;
    }

    //  IColony Side-neutral

    /**
     * Side netural method to get colony.
     * On clients it returns the view.
     * On servers it returns the colony itself
     *
     * @param world     World object
     * @param id        ID of the colony
     * @return          View of colony or colony itself depending on side
     */
    public static IColony getIColony(World world, int id)
    {
        return world.isRemote ? getColonyView(id) : getColony(id);
    }

    /**
     * @see {@link #getIColony(World, int)}
     *
     * @param w         World
     * @param x         x-coordinate
     * @param y         y-coordinate
     * @param z         z-coordinate
     * @return          View of colony or colony itself depending on side
     */
    public static IColony getIColony(World w, int x, int y, int z)
    {
        return w.isRemote ? getColonyView(w, x, y, z) : getColony(w, x, y, z);
    }

    /**
     * @see {@link #getIColony(World, int)}
     *
     * @param w         World
     * @param coord     Coordinates of the colony
     * @return          View of colony or colony itself depending on side
     */
    public static IColony getIColony(World w, ChunkCoordinates coord)
    {
        return getIColony(w, coord.posX, coord.posY, coord.posZ);
    }

    /**
     * See {@link #getIColony(World, int)} and {@link #getClosestColony(World, int, int, int)}
     *
     * @param w         World
     * @param x         x-coordinate
     * @param y         y-coordinate
     * @param z         z-coordinate
     * @return          View of colony or colony itself depending on side, closest to coordinates
     */
    public static IColony getClosestIColony(World w, int x, int y, int z)
    {
        return w.isRemote ? getClosestColonyView(w, x, y, z) : getClosestColony(w, x, y, z);
    }

    /**
     * @see {@link #getIColony(World, int)}
     * Returns a list of colonies or views with given Player as owner
     *
     * @param w         World
     * @param owner     Entity Player
     * @return          List of IColonies belonging to specific player
     */
    public static List<? extends IColony> getIColoniesByOwner(World w, EntityPlayer owner)
    {
        return getIColoniesByOwner(w, w.isRemote ? owner.getUniqueID() : owner.getGameProfile().getId());
    }

    /**
     * @see {@link #getIColony(World, int)}
     * Returns a list of colonies or views with given Player as owner
     *
     * @param w         World
     * @param owner     UUID of the owner
     * @return          List of IColonies belonging to specific player
     */
    public static List<? extends IColony> getIColoniesByOwner(World w, UUID owner)
    {
        return w.isRemote ? getColonyViewsByOwner(owner) : getColoniesByOwner(owner);
    }

    /**
     * Returns the minimum distance between two townhalls, to not make colonies collide
     *
     * @return          Minimum town hall distance
     */
    public static double getMinimumDistanceBetweenTownHalls()
    {
        //  [Townhall](Radius)+(Padding)+(Radius)[TownHall]
        return (2 * Configurations.workingRangeTownhall) + Configurations.townhallPadding;
    }

    /**
     * On server tick, tick every Colony
     * NOTE: Review this for performance
     *
     * @param event     {@link cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent}
     */
    public static void onServerTick(TickEvent.ServerTickEvent event)
    {
        for (Colony c : colonies.values())
        {
            c.onServerTick(event);
        }

        if (saveNeeded)
        {
            saveColonies();
        }
    }

    /**
     * On Client tick, clears views when player left
     *
     * @param event     {@link cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent}
     */
    public static void onClientTick(TickEvent.ClientTickEvent event)
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
     * @param event     {@link cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent}
     */
    public static void onWorldTick(
            TickEvent.WorldTickEvent event)
    {
        colonies.values().stream()
                .filter(c -> c.getDimensionId() == event.world.provider.dimensionId)
                .forEach(c -> c.onWorldTick(event));
    }

    /**
     * Read Colonies from saved NBT data
     *
     * @param compound   NBT Tag
     */
    public static void readFromNBT(NBTTagCompound compound)
    {
        NBTTagList colonyTags = compound.getTagList(TAG_COLONIES, NBT.TAG_COMPOUND);
        for (int i = 0; i < colonyTags.tagCount(); ++i)
        {
            Colony colony = Colony.loadColony(colonyTags.getCompoundTagAt(i));
            colonies.put(colony.getID(), colony);

            if (!coloniesByWorld.containsKey(colony.getDimensionId()))
            {
                coloniesByWorld.put(colony.getDimensionId(), new ArrayList<>());
            }
            coloniesByWorld.get(colony.getDimensionId()).add(colony);

            topColonyId = Math.max(topColonyId, colony.getID());
        }

        MineColonies.logger.info(String.format("Loaded %d colonies", colonies.size()));
    }

    /**
     * Write colonies to NBT data for saving
     *
     * @param compound      NBT-Tag
     */
    public static void writeToNBT(NBTTagCompound compound)
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
     * @return      Save file for minecolonies
     */
    private static File getSaveLocation()
    {
        File saveDir = new File(DimensionManager.getWorld(0).getSaveHandler().getWorldDirectory(), FILENAME_MINECOLONIES_PATH);
        return new File(saveDir, FILENAME_MINECOLONIES);
    }

    /**
     * Load a file and return the data as an NBTTagCompound
     *
     * @param file  The path to the file
     * @return      the data from the file as an NBTTagCompound, or null
     */
    private static NBTTagCompound loadNBTFromPath(
            File file)
    {
        try
        {
            if (file != null && file.exists())
            {
                return CompressedStreamTools.read(file);
                //return CompressedStreamTools.readCompressed(new FileInputStream(file));
            }
        } catch (IOException exception)
        {
            MineColonies.logger.error("Exception when loading ColonyManger", exception);
        }
        return null;
    }

    /**
     * Save an NBTTagCompound to a file.  Does so in a safe manner using an intermediate tmp file
     *
     * @param file      The destination file to write the data to
     * @param compound  The NBTTagCompound to write to the file
     */
    private static void saveNBTToPath(File file, NBTTagCompound compound)
    {
        try
        {
            if (file != null)
            {
                file.getParentFile().mkdir();
                CompressedStreamTools.safeWrite(compound, file);

                /*
                File tempFile = new File(file.getAbsolutePath() + "_tmp");
                tempFile.delete();

                CompressedStreamTools.writeCompressed(compound, new DataOutputStream(new FileOutputStream(tempFile)));

                file.delete();
                tempFile.renameTo(file);
                */
            }
        } catch (IOException exception)
        {
            MineColonies.logger.error("Exception when saving ColonyManager", exception);
        }
    }

    /**
     * Save all the Colonies
     */
    private static void saveColonies()
    {
        NBTTagCompound compound = new NBTTagCompound();
        writeToNBT(compound);

        File file = getSaveLocation();
        saveNBTToPath(file, compound);

        saveNeeded = false;
    }

    public static void markDirty()
    {
        saveNeeded = true;
    }

    /**
     * When a world is loaded, Colonies in that world need to grab the reference to the World
     * Additionally, when loading the first world, load all colonies.
     *
     * @param world     World
     */
    public static void onWorldLoad(World world)
    {
        if (!world.isRemote)
        {
            if (numWorldsLoaded == 0)
            {
                File file = getSaveLocation();
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

            world.addWorldAccess(new ColonyManagerWorldAccess());
        }
        else
        {
            for (ColonyView v : colonyViews.values())
            {
                v.onWorldLoad(world);
            }
        }
    }

    /**
     * Saves data when world is saved
     *
     * @param world     World
     */
    public static void onWorldSave(World world)
    {
        if (!world.isRemote &&
            world.provider.dimensionId == 0)    //  For now, save when 0 saves...
        {
            saveColonies();
        }
    }

    /**
     * When a world unloads, all colonies in that world are informed
     * Additionally, when the last world is unloaded, delete all colonies
     *
     * @param world     World
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
     * Sends view message to the right view
     *
     * @param colonyId          ID of the colony
     * @param colonyData        {@link ByteBuf} with colony data
     * @param isNewSubscription whether this is a new subscription or not
     */
    public static IMessage handleColonyViewMessage(int colonyId, ByteBuf colonyData, boolean isNewSubscription)
    {
        ColonyView view = getColonyView(colonyId);
        if (view == null)
        {
            view = ColonyView.createFromNetwork(colonyId);
            colonyViews.put(colonyId, view);
        }

        return view.handleColonyViewMessage(colonyData, isNewSubscription);
    }

    /**
     * Returns result of {@link ColonyView#handlePermissionsViewMessage(ByteBuf)} if {@link #getColonyView(int)}
     * gives a not-null result. If {@link #getColonyView(int)} is null, returns null
     *
     * @param colonyID      ID of the colony
     * @param data          {@link ByteBuf} with colony data
     * @return              result of {@link ColonyView#handlePermissionsViewMessage(ByteBuf)} or null
     */
    public static IMessage handlePermissionsViewMessage(int colonyID, ByteBuf data)
    {
        ColonyView view = getColonyView(colonyID);
        if(view != null)
        {
            return view.handlePermissionsViewMessage(data);
        }
        else
        {
            MineColonies.logger.error(String.format("Colony view does not exist for ID #%d", colonyID));
            return null;
        }
    }

    /**
     * Returns result of {@link ColonyView#handleColonyViewCitizensMessage(int, ByteBuf)} if {@link #getColonyView(int)}
     * gives a not-null result. If {@link #getColonyView(int)} is null, returns null
     *
     * @param colonyId      ID of the colony
     * @param citizenId     ID of the citizen
     * @param buf           {@link ByteBuf} with colony data
     * @return              result of {@link ColonyView#handleColonyViewCitizensMessage(int, ByteBuf)} or null
     */
    public static IMessage handleColonyViewCitizensMessage(int colonyId, int citizenId, ByteBuf buf)
    {
        ColonyView view = getColonyView(colonyId);
        if (view != null)
        {
            return view.handleColonyViewCitizensMessage(citizenId, buf);
        }

        return null;
    }

    /**
     * Returns result of {@link ColonyView#handleColonyViewRemoveCitizenMessage(int)} if {@link #getColonyView(int)}
     * gives a not-null result. If {@link #getColonyView(int)} is null, returns null
     *
     * @param colonyId      ID of the colony
     * @param citizenId     ID of the citizen
     * @return              result of {@link ColonyView#handleColonyViewRemoveCitizenMessage(int)}  or null
     */
    public static IMessage handleColonyViewRemoveCitizenMessage(int colonyId, int citizenId)
    {
        ColonyView view = getColonyView(colonyId);
        if (view != null)
        {
            //  Can legitimately be NULL, because (to keep the code simple and fast), it is
            //  possible to receive a 'remove' notice before receiving the View
            return view.handleColonyViewRemoveCitizenMessage(citizenId);
        }

        return null;
    }

    /**
     * Returns result of {@link ColonyView#handleColonyBuildingViewMessage(ChunkCoordinates, ByteBuf)} if {@link #getColonyView(int)}
     * gives a not-null result. If {@link #getColonyView(int)} is null, returns null
     *
     * @param colonyId      ID of the colony
     * @param buildingId    ID of the building
     * @param buf           {@link ByteBuf} with colony data
     * @return              result of {@link ColonyView#handleColonyBuildingViewMessage(ChunkCoordinates, ByteBuf)} or null
     */
    static public IMessage handleColonyBuildingViewMessage(int colonyId, ChunkCoordinates buildingId, ByteBuf buf)
    {
        ColonyView view = getColonyView(colonyId);
        if (view != null)
        {
            return view.handleColonyBuildingViewMessage(buildingId, buf);
        }
        else
        {
            MineColonies.logger.error(String.format("Colony view does not exist for ID #%d", colonyId));
            return null;
        }
    }

    /**
     * Returns result of {@link ColonyView#handleColonyViewRemoveBuildingMessage(ChunkCoordinates)} if {@link #getColonyView(int)}
     * gives a not-null result. If {@link #getColonyView(int)} is null, returns null
     *
     * @param colonyId      ID of the colony
     * @param buildingId    ID of the building
     * @return              result of {@link ColonyView#handleColonyViewRemoveBuildingMessage(ChunkCoordinates)}  or null
     */
    static public IMessage handleColonyViewRemoveBuildingMessage(int colonyId, ChunkCoordinates buildingId)
    {
        ColonyView view = getColonyView(colonyId);
        if (view != null)
        {
            //  Can legitimately be NULL, because (to keep the code simple and fast), it is
            //  possible to receive a 'remove' notice before receiving the View
            return view.handleColonyViewRemoveBuildingMessage(buildingId);
        }

        return null;
    }

    public static class ColonyManagerWorldAccess implements IWorldAccess
    {
        @Override
        public void markBlockForUpdate(int p_147586_1_, int p_147586_2_, int p_147586_3_) {}

        @Override
        public void markBlockForRenderUpdate(int p_147588_1_, int p_147588_2_, int p_147588_3_) {}

        @Override
        public void markBlockRangeForRenderUpdate(int p_147585_1_, int p_147585_2_, int p_147585_3_, int p_147585_4_, int p_147585_5_, int p_147585_6_) {}

        @Override
        public void playSound(String p_72704_1_, double p_72704_2_, double p_72704_4_, double p_72704_6_, float p_72704_8_, float p_72704_9_) {}

        @Override
        public void playSoundToNearExcept(EntityPlayer p_85102_1_, String p_85102_2_, double p_85102_3_, double p_85102_5_, double p_85102_7_, float p_85102_9_, float p_85102_10_) {}

        @Override
        public void spawnParticle(String p_72708_1_, double p_72708_2_, double p_72708_4_, double p_72708_6_, double p_72708_8_, double p_72708_10_, double p_72708_12_) {}

        @Override
        public void onEntityCreate(Entity entity)
        {
            if (entity instanceof EntityCitizen)
            {
                ((EntityCitizen) entity).updateColonyServer();
            }
        }

        @Override
        public void onEntityDestroy(Entity entity)
        {
            if (entity instanceof EntityCitizen)
            {
                CitizenData citizen = ((EntityCitizen) entity).getCitizenData();
                if (citizen != null)
                {
                    citizen.setCitizenEntity(null);
                }
            }
        }

        @Override public void playRecord(String p_72702_1_, int p_72702_2_, int p_72702_3_, int p_72702_4_) {}

        @Override public void broadcastSound(int p_82746_1_, int p_82746_2_, int p_82746_3_, int p_82746_4_, int p_82746_5_) {}

        @Override public void playAuxSFX(EntityPlayer p_72706_1_, int p_72706_2_, int p_72706_3_, int p_72706_4_, int p_72706_5_, int p_72706_6_) {}

        @Override public void destroyBlockPartially(int p_147587_1_, int p_147587_2_, int p_147587_3_, int p_147587_4_, int p_147587_5_) {}

        @Override public void onStaticEntitiesChanged() {}
    }
}
