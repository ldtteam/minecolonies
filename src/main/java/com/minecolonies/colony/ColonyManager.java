package com.minecolonies.colony;

import com.minecolonies.achievements.ModAchievements;
import com.minecolonies.colony.buildings.AbstractBuilding;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.util.AchievementUtils;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.Log;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Singleton class that links colonies to minecraft.
 */
public final class ColonyManager
{
    private static final String                     FILENAME_MINECOLONIES_PATH = "minecolonies";
    private static final String                     FILENAME_MINECOLONIES      = "colonies.dat";
    private static final String                     TAG_COLONIES               = "colonies";
    @Nonnull
    private static       Map<Integer, Colony>       colonies                   = new HashMap<>();
    @Nonnull
    private static       Map<Integer, List<Colony>> coloniesByWorld            = new HashMap<>();
    private static       int                        topColonyId                = 0;
    @Nonnull
    private static       Map<Integer, ColonyView>   colonyViews                = new HashMap<>();
    // Used to trigger loading/unloading colonies
    private static int     numWorldsLoaded;
    private static boolean saveNeeded;

    private ColonyManager()
    {
        //Hides default constructor.
    }

    /**
     * Create a new Colony in the given world and at that location.
     *
     * @param w      World of the colony
     * @param pos    Coordinate of the center of the colony
     * @param player the player that creates the colony - owner.
     * @return The created colony
     */
    @Nonnull
    public static Colony createColony(@Nonnull World w, BlockPos pos, @Nonnull EntityPlayer player)
    {
        ++topColonyId;
        @Nonnull Colony colony = new Colony(topColonyId, w, pos);
        colonies.put(colony.getID(), colony);

        if (!coloniesByWorld.containsKey(colony.getDimension()))
        {
            coloniesByWorld.put(colony.getDimension(), new ArrayList<>());
        }

        coloniesByWorld.get(colony.getDimension()).add(colony);

        String colonyName = LanguageHandler.format("com.minecolonies.gui.townHall.defaultName", player.getDisplayNameString());
        colony.setName(colonyName);
        colony.getPermissions().setPlayerRank(player.getGameProfile().getId(), Permissions.Rank.OWNER);

        colony.addStat(ModAchievements.achievementGetSupply);
        colony.addStat(ModAchievements.achievementBuildingTownhall);

        markDirty();

        Log.logger.info(String.format("New Colony %d", colony.getID()));

        return colony;
    }

    /**
     * Syncs the achievements for all colonies.
     */
    public static void syncAllColoniesAchievements(){
        colonies.values().forEach(AchievementUtils::syncAchievements);
    }

    /**
     * Specify that colonies should be saved.
     */
    public static void markDirty()
    {
        saveNeeded = true;
    }

    /**
     * Get Colony by UUID
     *
     * @param id ID of colony
     * @return Colony with given ID
     */
    public static Colony getColony(int id)
    {
        return colonies.get(id);
    }

    /**
     * Get a AbstractBuilding by a World and coordinates
     *
     * @param w   World
     * @param pos Block position
     * @return AbstractBuilding at the given location
     */
    public static AbstractBuilding getBuilding(@Nonnull World w, @Nonnull BlockPos pos)
    {
        @Nullable Colony colony = getColony(w, pos);
        if (colony != null)
        {
            AbstractBuilding building = colony.getBuilding(pos);
            if (building != null)
            {
                return building;
            }
        }

        //  Fallback - there might be a AbstractBuilding for this block, but it's outside of it's owning colony's radius
        if (coloniesByWorld.containsKey(w.provider.getDimension()))
        {
            for (@Nonnull Colony otherColony : coloniesByWorld.get(w.provider.getDimension()))
            {
                AbstractBuilding building = otherColony.getBuilding(pos);
                if (building != null)
                {
                    return building;
                }
            }
        }

        return null;
    }

    /**
     * Get colony that contains a given coordinate
     *
     * @param w   World
     * @param pos coordinates
     * @return Colony at the given location
     */
    public static Colony getColony(@Nonnull World w, @Nonnull BlockPos pos)
    {
        List<Colony> coloniesInWorld = coloniesByWorld.get(w.provider.getDimension());
        if (coloniesInWorld == null)
        {
            return null;
        }

        for (@Nonnull Colony c : coloniesInWorld)
        {
            if (c.isCoordInColony(w, pos))
            {
                return c;
            }
        }

        return null;
    }

    /**
     * Get a AbstractBuilding by position.
     *
     * @param pos Block position
     * @return Returns the view belonging to the building at (x, y, z)
     */
    public static AbstractBuilding.View getBuildingView(BlockPos pos)
    {
        //  On client we will just check all known views
        for (@Nonnull ColonyView colony : colonyViews.values())
        {
            AbstractBuilding.View building = colony.getBuilding(pos);
            if (building != null)
            {
                return building;
            }
        }

        return null;
    }

    /**
     * Side neutral method to get colony.
     * On clients it returns the view.
     * On servers it returns the colony itself
     *
     * @param w   World
     * @param pos coordinates
     * @return View of colony or colony itself depending on side
     */
    @Nullable
    public static IColony getIColony(@Nonnull World w, @Nonnull BlockPos pos)
    {
        return w.isRemote ? getColonyView(w, pos) : getColony(w, pos);
    }

    /**
     * Get Colony that contains a given (x, y, z)
     *
     * @param w   World
     * @param pos coordinates
     * @return returns the view belonging to the colony at x, y, z,
     */
    private static ColonyView getColonyView(@Nonnull World w, @Nonnull BlockPos pos)
    {
        for (@Nonnull ColonyView c : colonyViews.values())
        {
            if (c.isCoordInColony(w, pos))
            {
                return c;
            }
        }

        return null;
    }

    /**
     * Side neutral method to get colony.
     * On clients it returns the view.
     * On servers it returns the colony itself.
     * {@link #getClosestColony(World, BlockPos)}
     *
     * @param w   World
     * @param pos Block position
     * @return View of colony or colony itself depending on side, closest to coordinates
     */
    @Nullable
    public static IColony getClosestIColony(@Nonnull World w, @Nonnull BlockPos pos)
    {
        return w.isRemote ? getClosestColonyView(w, pos) : getClosestColony(w, pos);
    }

    /**
     * Returns the closest view {@link #getColonyView(World, BlockPos)}.
     *
     * @param w   World
     * @param pos Block Position
     * @return View of the closest colony
     */
    @Nullable
    public static ColonyView getClosestColonyView(@Nonnull World w, @Nonnull BlockPos pos)
    {
        @Nullable ColonyView closestColony = null;
        float closestDist = Float.MAX_VALUE;

        for (@Nonnull ColonyView c : colonyViews.values())
        {
            if (c.getDimension() == w.provider.getDimension())
            {
                float dist = c.getDistanceSquared(pos);
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
     * Get closest colony by x,y,z
     *
     * @param w   World
     * @param pos coordinates
     * @return Colony closest to coordinates
     */
    private static Colony getClosestColony(@Nonnull World w, @Nonnull BlockPos pos)
    {
        List<Colony> coloniesInWorld = coloniesByWorld.get(w.provider.getDimension());
        if (coloniesInWorld == null)
        {
            return null;
        }

        @Nullable Colony closestColony = null;
        float closestDist = Float.MAX_VALUE;

        for (@Nonnull Colony c : coloniesInWorld)
        {
            if (c.getDimension() == w.provider.getDimension())
            {
                float dist = c.getDistanceSquared(pos);
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
     * Side neutral method to get colony.
     * On clients it returns the view.
     * On servers it returns the colony itself
     * <p>
     * Returns a colony or view with the given Player as owner
     *
     * @param w     World
     * @param owner Entity Player
     * @return IColony belonging to specific player
     */
    @Nullable
    public static IColony getIColonyByOwner(@Nonnull World w, @Nonnull EntityPlayer owner)
    {
        return getIColonyByOwner(w, w.isRemote ? owner.getUniqueID() : owner.getGameProfile().getId());
    }

    /**
     * Side neutral method to get colony.
     * On clients it returns the view.
     * On servers it returns the colony itself
     * <p>
     * Returns a colony or view with given Player as owner
     *
     * @param w     World
     * @param owner UUID of the owner
     * @return IColony belonging to specific player
     */
    @Nullable
    public static IColony getIColonyByOwner(@Nonnull World w, UUID owner)
    {
        return w.isRemote ? getColonyViewByOwner(owner) : getColonyByOwner(owner);
    }

    /**
     * Returns a ColonyView with specific owner.
     *
     * @param owner UUID of the owner
     * @return ColonyView
     */
    private static IColony getColonyViewByOwner(UUID owner)
    {
        for (@Nonnull ColonyView c : colonyViews.values())
        {
            Permissions.Player p = c.getPlayers().get(owner);
            if (p != null && p.getRank().equals(Permissions.Rank.OWNER))
            {
                return c;
            }
        }

        return null;
    }

    /**
     * Returns a Colony that has the given owner.
     *
     * @param owner UUID of the owner
     * @return Colony that belong to given owner UUID
     */
    private static IColony getColonyByOwner(UUID owner)
    {
        return colonies.values()
                 .stream()
                 .filter(c -> c.getPermissions().getOwner().equals(owner))
                 .findFirst()
                 .orElse(null);
    }

    /**
     * Returns the minimum distance between two town halls, to not make colonies collide.
     *
     * @return Minimum town hall distance
     */
    public static double getMinimumDistanceBetweenTownHalls()
    {
        //  [TownHall](Radius)+(Padding)+(Radius)[TownHall]
        return (2D * Configurations.workingRangeTownHall) + Configurations.townHallPadding;
    }

    /**
     * On server tick, tick every Colony.
     * NOTE: Review this for performance
     *
     * @param event {@link net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent}
     */
    public static void onServerTick(@Nonnull TickEvent.ServerTickEvent event)
    {
        for (@Nonnull Colony c : colonies.values())
        {
            c.onServerTick(event);
        }

        if (saveNeeded)
        {
            saveColonies();
        }
    }

    /**
     * Save all the Colonies
     */
    private static void saveColonies()
    {
        @Nonnull NBTTagCompound compound = new NBTTagCompound();
        writeToNBT(compound);

        @Nonnull File file = getSaveLocation();
        saveNBTToPath(file, compound);

        saveNeeded = false;
    }

    /**
     * Write colonies to NBT data for saving
     *
     * @param compound NBT-Tag
     */
    public static void writeToNBT(@Nonnull NBTTagCompound compound)
    {
        @Nonnull NBTTagList colonyTagList = new NBTTagList();
        for (@Nonnull Colony colony : colonies.values())
        {
            @Nonnull NBTTagCompound colonyTagCompound = new NBTTagCompound();
            colony.writeToNBT(colonyTagCompound);
            colonyTagList.appendTag(colonyTagCompound);
        }
        compound.setTag(TAG_COLONIES, colonyTagList);
    }

    /**
     * Get save location for Minecolonies data, from the world/save directory
     *
     * @return Save file for minecolonies
     */
    @Nonnull
    private static File getSaveLocation()
    {
        @Nonnull File saveDir = new File(DimensionManager.getWorld(0).getSaveHandler().getWorldDirectory(), FILENAME_MINECOLONIES_PATH);
        return new File(saveDir, FILENAME_MINECOLONIES);
    }

    /**
     * Save an NBTTagCompound to a file.  Does so in a safe manner using an intermediate tmp file
     *
     * @param file     The destination file to write the data to
     * @param compound The NBTTagCompound to write to the file
     */
    private static void saveNBTToPath(@Nullable File file, @Nonnull NBTTagCompound compound)
    {
        try
        {
            if (file != null)
            {
                file.getParentFile().mkdir();
                CompressedStreamTools.safeWrite(compound, file);
            }
        }
        catch (IOException exception)
        {
            Log.logger.error("Exception when saving ColonyManager", exception);
        }
    }

    /**
     * On Client tick, clears views when player left.
     *
     * @param event {@link net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent}
     */
    public static void onClientTick(@Nonnull TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END && Minecraft.getMinecraft().theWorld == null && !colonyViews.isEmpty())
        {
            //  Player has left the game, clear the Colony View cache
            colonyViews.clear();
        }
    }

    /**
     * On world tick, tick every Colony in that world.
     * NOTE: Review this for performance
     *
     * @param event {@link net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent}
     */
    public static void onWorldTick(
                                    @Nonnull TickEvent.WorldTickEvent event)
    {
        colonies.values().stream()
          .filter(c -> c.getDimension() == event.world.provider.getDimension())
          .forEach(c -> c.onWorldTick(event));
    }

    /**
     * When a world is loaded, Colonies in that world need to grab the reference to the World
     * Additionally, when loading the first world, load all colonies.
     *
     * @param world World
     */
    public static void onWorldLoad(@Nonnull World world)
    {
        if (!world.isRemote)
        {
            if (numWorldsLoaded == 0)
            {
                @Nonnull File file = getSaveLocation();
                @Nullable NBTTagCompound data = loadNBTFromPath(file);
                if (data != null)
                {
                    readFromNBT(data);
                }
            }
            ++numWorldsLoaded;

            List<Colony> worldColonies = coloniesByWorld.get(world.provider.getDimension());
            if (worldColonies != null)
            {
                for (@Nonnull Colony c : worldColonies)
                {
                    c.onWorldLoad(world);
                }
            }
            
            world.addEventListener(new ColonyManagerWorldAccess());
        }
    }

    /**
     * Load a file and return the data as an NBTTagCompound
     *
     * @param file The path to the file
     * @return the data from the file as an NBTTagCompound, or null
     */
    private static NBTTagCompound loadNBTFromPath(
                                                   @Nullable File file)
    {
        try
        {
            if (file != null && file.exists())
            {
                return CompressedStreamTools.read(file);
            }
        }
        catch (IOException exception)
        {
            Log.logger.error("Exception when loading ColonyManger", exception);
        }
        return null;
    }

    /**
     * Read Colonies from saved NBT data
     *
     * @param compound NBT Tag
     */
    public static void readFromNBT(@Nonnull NBTTagCompound compound)
    {
        NBTTagList colonyTags = compound.getTagList(TAG_COLONIES, NBT.TAG_COMPOUND);
        for (int i = 0; i < colonyTags.tagCount(); ++i)
        {
            @Nonnull Colony colony = Colony.loadColony(colonyTags.getCompoundTagAt(i));
            colonies.put(colony.getID(), colony);

            if (!coloniesByWorld.containsKey(colony.getDimension()))
            {
                coloniesByWorld.put(colony.getDimension(), new ArrayList<>());
            }
            coloniesByWorld.get(colony.getDimension()).add(colony);

            topColonyId = Math.max(topColonyId, colony.getID());
        }

        Log.logger.info(String.format("Loaded %d colonies", colonies.size()));
    }

    /**
     * Saves data when world is saved
     *
     * @param world World
     */
    public static void onWorldSave(@Nonnull World world)
    {
        //We save when the first dimension is saved.
        if (!world.isRemote && world.provider.getDimension() == 0)
        {
            saveColonies();
        }
    }

    /**
     * When a world unloads, all colonies in that world are informed
     * Additionally, when the last world is unloaded, delete all colonies
     *
     * @param world World
     */
    public static void onWorldUnload(@Nonnull World world)
    {
        if (!world.isRemote)
        {
            List<Colony> worldColonies = coloniesByWorld.get(world.provider.getDimension());
            if (worldColonies != null)
            {
                for (@Nonnull Colony c : worldColonies)
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
     * @return the response message.
     */
    @Nullable
    public static IMessage handleColonyViewMessage(int colonyId, @Nonnull ByteBuf colonyData, boolean isNewSubscription)
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
     * Get ColonyView by ID
     *
     * @param id ID of colony
     * @return The ColonyView belonging to the colony
     */
    public static ColonyView getColonyView(int id)
    {
        return colonyViews.get(id);
    }

    /**
     * Returns result of {@link ColonyView#handlePermissionsViewMessage(ByteBuf)} if {@link #getColonyView(int)}
     * gives a not-null result. If {@link #getColonyView(int)} is null, returns null
     *
     * @param colonyID ID of the colony
     * @param data     {@link ByteBuf} with colony data
     * @return result of {@link ColonyView#handlePermissionsViewMessage(ByteBuf)} or null
     */
    public static IMessage handlePermissionsViewMessage(int colonyID, @Nonnull ByteBuf data)
    {
        final ColonyView view = getColonyView(colonyID);
        if (view != null)
        {
            return view.handlePermissionsViewMessage(data);
        }
        else
        {
            Log.logger.error(String.format("Colony view does not exist for ID #%d", colonyID));
            return null;
        }
    }

    /**
     * Returns result of {@link ColonyView#handleColonyViewCitizensMessage(int, ByteBuf)} if {@link #getColonyView(int)}
     * gives a not-null result. If {@link #getColonyView(int)} is null, returns null
     *
     * @param colonyId  ID of the colony
     * @param citizenId ID of the citizen
     * @param buf       {@link ByteBuf} with colony data
     * @return result of {@link ColonyView#handleColonyViewCitizensMessage(int, ByteBuf)} or null
     */
    public static IMessage handleColonyViewCitizensMessage(int colonyId, int citizenId, ByteBuf buf)
    {
        final ColonyView view = getColonyView(colonyId);
        if (view != null)
        {
            return view.handleColonyViewCitizensMessage(citizenId, buf);
        }

        return null;
    }

    /**
     * Returns result of {@link ColonyView#handleColonyViewWorkOrderMessage(ByteBuf)} (int, ByteBuf)} if {@link #getColonyView(int)}
     * gives a not-null result. If {@link #getColonyView(int)} is null, returns null
     *
     * @param colonyId ID of the colony
     * @param buf      {@link ByteBuf} with colony data
     * @return result of {@link ColonyView#handleColonyViewWorkOrderMessage(ByteBuf)} or null
     */
    public static IMessage handleColonyViewWorkOrderMessage(int colonyId, ByteBuf buf)
    {
        final ColonyView view = getColonyView(colonyId);
        if (view != null)
        {
            return view.handleColonyViewWorkOrderMessage(buf);
        }

        return null;
    }

    /**
     * Returns result of {@link ColonyView#handleColonyViewRemoveCitizenMessage(int)} if {@link #getColonyView(int)}
     * gives a not-null result. If {@link #getColonyView(int)} is null, returns null
     *
     * @param colonyId  ID of the colony
     * @param citizenId ID of the citizen
     * @return result of {@link ColonyView#handleColonyViewRemoveCitizenMessage(int)}  or null
     */
    public static IMessage handleColonyViewRemoveCitizenMessage(int colonyId, int citizenId)
    {
        final ColonyView view = getColonyView(colonyId);
        if (view != null)
        {
            //  Can legitimately be NULL, because (to keep the code simple and fast), it is
            //  possible to receive a 'remove' notice before receiving the View
            return view.handleColonyViewRemoveCitizenMessage(citizenId);
        }

        return null;
    }

    /**
     * Returns result of {@link ColonyView#handleColonyBuildingViewMessage(BlockPos, ByteBuf)} if {@link #getColonyView(int)}
     * gives a not-null result. If {@link #getColonyView(int)} is null, returns null
     *
     * @param colonyId   ID of the colony
     * @param buildingId ID of the building
     * @param buf        {@link ByteBuf} with colony data
     * @return result of {@link ColonyView#handleColonyBuildingViewMessage(BlockPos, ByteBuf)} or null
     */
    public static IMessage handleColonyBuildingViewMessage(int colonyId, BlockPos buildingId, @Nonnull ByteBuf buf)
    {
        final ColonyView view = getColonyView(colonyId);
        if (view != null)
        {
            return view.handleColonyBuildingViewMessage(buildingId, buf);
        }
        else
        {
            Log.logger.error(String.format("Colony view does not exist for ID #%d", colonyId));
            return null;
        }
    }

    /**
     * Returns result of {@link ColonyView#handleColonyViewRemoveBuildingMessage(BlockPos)} if {@link #getColonyView(int)}
     * gives a not-null result. If {@link #getColonyView(int)} is null, returns null
     *
     * @param colonyId   ID of the colony
     * @param buildingId ID of the building
     * @return result of {@link ColonyView#handleColonyViewRemoveBuildingMessage(BlockPos)}  or null
     */
    public static IMessage handleColonyViewRemoveBuildingMessage(int colonyId, BlockPos buildingId)
    {
        final ColonyView view = getColonyView(colonyId);
        if (view != null)
        {
            //  Can legitimately be NULL, because (to keep the code simple and fast), it is
            //  possible to receive a 'remove' notice before receiving the View
            return view.handleColonyViewRemoveBuildingMessage(buildingId);
        }

        return null;
    }

    /**
     * Returns result of {@link ColonyView#handleColonyViewRemoveWorkOrderMessage(int)} if {@link #getColonyView(int)}
     * gives a not-null result. If {@link #getColonyView(int)} is null, returns null
     *
     * @param colonyId    ID of the colony
     * @param workOrderId ID of the workOrder
     * @return result of {@link ColonyView#handleColonyViewRemoveWorkOrderMessage(int)}  or null
     */
    public static IMessage handleColonyViewRemoveWorkOrderMessage(final int colonyId, final int workOrderId)
    {
        final ColonyView view = getColonyView(colonyId);
        if (view != null)
        {
            //  Can legitimately be NULL, because (to keep the code simple and fast), it is
            //  possible to receive a 'remove' notice before receiving the View
            return view.handleColonyViewRemoveWorkOrderMessage(workOrderId);
        }

        return null;
    }
}
