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
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    @NotNull
    private static       Map<Integer, Colony>       colonies                   = new HashMap<>();
    @NotNull
    private static       Map<Integer, List<Colony>> coloniesByWorld            = new HashMap<>();
    private static       int                        topColonyId                = 0;
    @NotNull
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
    @NotNull
    public static Colony createColony(@NotNull World w, BlockPos pos, @NotNull EntityPlayer player)
    {
        ++topColonyId;
        @NotNull Colony colony = new Colony(topColonyId, w, pos);
        colonies.put(colony.getID(), colony);

        if (!coloniesByWorld.containsKey(colony.getDimensionId()))
        {
            coloniesByWorld.put(colony.getDimensionId(), new ArrayList<>());
        }

        coloniesByWorld.get(colony.getDimensionId()).add(colony);

        String colonyName = LanguageHandler.format("com.minecolonies.gui.townHall.defaultName", player.getDisplayNameString());
        colony.setName(colonyName);
        colony.getPermissions().setPlayerRank(player.getGameProfile().getId(), Permissions.Rank.OWNER);

        colony.triggerAchievement(ModAchievements.achievementGetSupply);
        colony.triggerAchievement(ModAchievements.achievementBuildingTownhall);

        markDirty();

        Log.getLogger().info(String.format("New Colony Id: %d by %s", colony.getID(), player.getName()));

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
    public static AbstractBuilding getBuilding(@NotNull World w, @NotNull BlockPos pos)
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
        if (coloniesByWorld.containsKey(w.provider.getDimensionId()))
        {
            for (@NotNull Colony otherColony : coloniesByWorld.get(w.provider.getDimensionId()))
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
    public static Colony getColony(@NotNull World w, @NotNull BlockPos pos)
    {
        List<Colony> coloniesInWorld = coloniesByWorld.get(w.provider.getDimensionId());
        if (coloniesInWorld == null)
        {
            return null;
        }

        for (@NotNull Colony c : coloniesInWorld)
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
        for (@NotNull ColonyView colony : colonyViews.values())
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
    public static IColony getIColony(@NotNull World w, @NotNull BlockPos pos)
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
    private static ColonyView getColonyView(@NotNull World w, @NotNull BlockPos pos)
    {
        for (@NotNull ColonyView c : colonyViews.values())
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
    public static IColony getClosestIColony(@NotNull World w, @NotNull BlockPos pos)
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
    public static ColonyView getClosestColonyView(@NotNull World w, @NotNull BlockPos pos)
    {
        @Nullable ColonyView closestColony = null;
        long closestDist = Long.MAX_VALUE;

        for (@NotNull ColonyView c : colonyViews.values())
        {
            if (c.getDimensionId() == w.provider.getDimensionId())
            {
                long dist = c.getDistanceSquared(pos);
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
    private static Colony getClosestColony(@NotNull World w, @NotNull BlockPos pos)
    {
        List<Colony> coloniesInWorld = coloniesByWorld.get(w.provider.getDimensionId());
        if (coloniesInWorld == null)
        {
            return null;
        }

        @Nullable Colony closestColony = null;
        long closestDist = Long.MAX_VALUE;

        for (@NotNull Colony c : coloniesInWorld)
        {
            if (c.getDimensionId() == w.provider.getDimensionId())
            {
                long dist = c.getDistanceSquared(pos);
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
    public static IColony getIColonyByOwner(@NotNull World w, @NotNull EntityPlayer owner)
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
    public static IColony getIColonyByOwner(@NotNull World w, UUID owner)
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
        for (@NotNull ColonyView c : colonyViews.values())
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
    @Nullable
    private static IColony getColonyByOwner(@Nullable UUID owner)
    {
        if (owner == null)
        {
            return null;
        }

        return colonies.values()
                 .stream()
                 .filter(c -> owner.equals(c.getPermissions().getOwner()))
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
    public static void onServerTick(@NotNull TickEvent.ServerTickEvent event)
    {
        for (@NotNull Colony c : colonies.values())
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
        @NotNull NBTTagCompound compound = new NBTTagCompound();
        writeToNBT(compound);

        @NotNull File file = getSaveLocation();
        saveNBTToPath(file, compound);

        saveNeeded = false;
    }

    /**
     * Write colonies to NBT data for saving
     *
     * @param compound NBT-Tag
     */
    public static void writeToNBT(@NotNull NBTTagCompound compound)
    {
        @NotNull NBTTagList colonyTagList = new NBTTagList();
        for (@NotNull Colony colony : colonies.values())
        {
            @NotNull NBTTagCompound colonyTagCompound = new NBTTagCompound();
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
    @NotNull
    private static File getSaveLocation()
    {
        @NotNull File saveDir = new File(DimensionManager.getWorld(0).getSaveHandler().getWorldDirectory(), FILENAME_MINECOLONIES_PATH);
        return new File(saveDir, FILENAME_MINECOLONIES);
    }

    /**
     * Save an NBTTagCompound to a file.  Does so in a safe manner using an intermediate tmp file
     *
     * @param file     The destination file to write the data to
     * @param compound The NBTTagCompound to write to the file
     */
    private static void saveNBTToPath(@Nullable File file, @NotNull NBTTagCompound compound)
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
            Log.getLogger().error("Exception when saving ColonyManager", exception);
        }
    }

    /**
     * On Client tick, clears views when player left.
     *
     * @param event {@link net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent}
     */
    public static void onClientTick(@NotNull TickEvent.ClientTickEvent event)
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
                                    @NotNull TickEvent.WorldTickEvent event)
    {
        final Map<Integer, Colony> coloniesCopy = new HashMap<>(colonies);

        coloniesCopy.values().stream()
          .filter(c -> c.getDimensionId() == event.world.provider.getDimensionId())
          .forEach(c -> c.onWorldTick(event));
    }

    /**
     * When a world is loaded, Colonies in that world need to grab the reference to the World
     * Additionally, when loading the first world, load all colonies.
     *
     * @param world World
     */
    public static void onWorldLoad(@NotNull World world)
    {
        if (!world.isRemote)
        {
            if (numWorldsLoaded == 0)
            {
                @NotNull File file = getSaveLocation();
                @Nullable NBTTagCompound data = loadNBTFromPath(file);
                if (data != null)
                {
                    readFromNBT(data);
                }
            }
            ++numWorldsLoaded;

            List<Colony> worldColonies = coloniesByWorld.get(world.provider.getDimensionId());
            if (worldColonies != null)
            {
                for (@NotNull Colony c : worldColonies)
                {
                    c.onWorldLoad(world);
                }
            }

            world.addWorldAccess(new ColonyManagerWorldAccess());
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
            Log.getLogger().error("Exception when loading ColonyManger", exception);
        }
        return null;
    }

    /**
     * Read Colonies from saved NBT data
     *
     * @param compound NBT Tag
     */
    public static void readFromNBT(@NotNull NBTTagCompound compound)
    {
        NBTTagList colonyTags = compound.getTagList(TAG_COLONIES, NBT.TAG_COMPOUND);
        for (int i = 0; i < colonyTags.tagCount(); ++i)
        {
            @NotNull Colony colony = Colony.loadColony(colonyTags.getCompoundTagAt(i));
            colonies.put(colony.getID(), colony);

            if (!coloniesByWorld.containsKey(colony.getDimensionId()))
            {
                coloniesByWorld.put(colony.getDimensionId(), new ArrayList<>());
            }
            coloniesByWorld.get(colony.getDimensionId()).add(colony);

            topColonyId = Math.max(topColonyId, colony.getID());
        }

        Log.getLogger().info(String.format("Loaded %d colonies", colonies.size()));
    }

    /**
     * Saves data when world is saved
     *
     * @param world World
     */
    public static void onWorldSave(@NotNull World world)
    {
        //We save when the first dimension is saved.
        if (!world.isRemote && world.provider.getDimensionId() == 0)
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
    public static void onWorldUnload(@NotNull World world)
    {
        if (!world.isRemote)
        {
            List<Colony> worldColonies = coloniesByWorld.get(world.provider.getDimensionId());
            if (worldColonies != null)
            {
                for (@NotNull Colony c : worldColonies)
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
    public static IMessage handleColonyViewMessage(int colonyId, @NotNull ByteBuf colonyData, boolean isNewSubscription)
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
    public static IMessage handlePermissionsViewMessage(int colonyID, @NotNull ByteBuf data)
    {
        final ColonyView view = getColonyView(colonyID);
        if (view != null)
        {
            return view.handlePermissionsViewMessage(data);
        }
        else
        {
            Log.getLogger().error(String.format("Colony view does not exist for ID #%d", colonyID));
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
    public static IMessage handleColonyBuildingViewMessage(int colonyId, BlockPos buildingId, @NotNull ByteBuf buf)
    {
        final ColonyView view = getColonyView(colonyId);
        if (view != null)
        {
            return view.handleColonyBuildingViewMessage(buildingId, buf);
        }
        else
        {
            Log.getLogger().error(String.format("Colony view does not exist for ID #%d", colonyId));
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
