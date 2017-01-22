package com.minecolonies.coremod.colony;

import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.blocks.AbstractBlockHut;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.configuration.Configurations;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.util.AchievementUtils;
import com.minecolonies.coremod.util.LanguageHandler;
import com.minecolonies.coremod.util.Log;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
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
    /**
     * The file name of the minecolonies path.
     */
    private static final String FILENAME_MINECOLONIES_PATH = "minecolonies";

    /**
     * The file name of the minecolonies.
     */
    private static final String FILENAME_MINECOLONIES = "colonies.dat";

    /**
     * The tag of the colonies.
     */
    private static final String                     TAG_COLONIES          = "colonies";
    /**
     * The damage source used to kill citizens.
     */
    private static final DamageSource               CONSOLE_DAMAGE_SOURCE = new DamageSource("Console");
    /**
     * The list of all colonies.
     */
    @NotNull
    private static final Map<Integer, Colony>       colonies              = new HashMap<>();
    /**
     * The list of all colonies by world.
     */
    @NotNull
    private static final Map<Integer, List<Colony>> coloniesByWorld       = new HashMap<>();
    /**
     * The last colony id.
     */
    private static       int                        topColonyId           = 0;
    /**
     * The list of colony views.
     */
    @NotNull
    private static final Map<Integer, ColonyView>   colonyViews           = new HashMap<>();
    /**
     * Amount of worlds loaded.
     */
    private static int numWorldsLoaded;
    /**
     * Whether the colonyManager should persist data.
     */
    private static boolean saveNeeded;

    private ColonyManager()
    {
        //Hides default constructor.
    }

    /**
     * Create a new Colony in the given world and at that location.
     *
     * @param w      World of the colony.
     * @param pos    Coordinate of the center of the colony.
     * @param player the player that creates the colony - owner.
     * @return The created colony.
     */
    @NotNull
    public static Colony createColony(@NotNull final World w, final BlockPos pos, @NotNull final EntityPlayer player)
    {
        ++topColonyId;
        @NotNull final Colony colony = new Colony(topColonyId, w, pos);
        colonies.put(colony.getID(), colony);

        if (!coloniesByWorld.containsKey(colony.getDimension()))
        {
            coloniesByWorld.put(colony.getDimension(), new ArrayList<>());
        }

        coloniesByWorld.get(colony.getDimension()).add(colony);

        final String colonyName = LanguageHandler.format("com.minecolonies.coremod.gui.townHall.defaultName", player.getDisplayNameString());
        colony.setName(colonyName);
        colony.getPermissions().setPlayerRank(player.getGameProfile().getId(), Permissions.Rank.OWNER, w);

        colony.triggerAchievement(ModAchievements.achievementGetSupply);
        colony.triggerAchievement(ModAchievements.achievementTownhall);

        markDirty();

        Log.getLogger().info(String.format("New Colony Id: %d by %s", colony.getID(), player.getName()));

        return colony;
    }

    /**
     * Specify that colonies should be saved.
     */
    public static void markDirty()
    {
        saveNeeded = true;
    }

    /**
     * Delete a colony and kill all citizens/purge all buildings.
     *
     * @param id the colonies id.
     */
    public static void deleteColony(final int id)
    {
        try
        {
            final Colony colony = getColony(id);
            Log.getLogger().info("Deleting colony " + id);
            colonies.remove(id);
            coloniesByWorld.get(colony.getDimension()).remove(colony);
            final Set<World> colonyWorlds = new HashSet<>();
            Log.getLogger().info("Removing citizens for " + id);
            for (final CitizenData citizenData : new ArrayList<>(colony.getCitizens().values()))
            {
                Log.getLogger().info("Kill Citizen " + citizenData.getName());
                final EntityCitizen entityCitizen = citizenData.getCitizenEntity();
                if (entityCitizen != null)
                {
                    final World world = entityCitizen.getEntityWorld();
                    citizenData.getCitizenEntity().onDeath(CONSOLE_DAMAGE_SOURCE);
                    colonyWorlds.add(world);
                }
            }
            Log.getLogger().info("Removing buildings for " + id);
            for (final AbstractBuilding building : new ArrayList<>(colony.getBuildings().values()))
            {

                final BlockPos location = building.getLocation();
                Log.getLogger().info("Delete Building at " + location);
                building.destroy();
                for (final World world : colonyWorlds)
                {
                    if (world.getBlockState(location).getBlock() instanceof AbstractBlockHut)
                    {
                        Log.getLogger().info("Found Block, deleting " + world.getBlockState(location).getBlock());
                        world.setBlockToAir(location);
                    }
                }
            }
            Log.getLogger().info("Done with " + id);
        }
        catch (final RuntimeException e)
        {
            Log.getLogger().warn("Deleting Colony " + id + " errored:", e);
        }
    }

    /**
     * Get Colony by UUID.
     *
     * @param id ID of colony.
     * @return Colony with given ID.
     */
    public static Colony getColony(final int id)
    {
        return colonies.get(id);
    }

    /**
     * Syncs the achievements for all colonies.
     */
    public static void syncAllColoniesAchievements()
    {
        colonies.values().forEach(AchievementUtils::syncAchievements);
    }

    /**
     * Get a AbstractBuilding by a World and coordinates.
     *
     * @param w   World.
     * @param pos Block position.
     * @return AbstractBuilding at the given location.
     */
    public static AbstractBuilding getBuilding(@NotNull final World w, @NotNull final BlockPos pos)
    {
        @Nullable final Colony colony = getColony(w, pos);
        if (colony != null)
        {
            final AbstractBuilding building = colony.getBuilding(pos);
            if (building != null)
            {
                return building;
            }
        }

        //  Fallback - there might be a AbstractBuilding for this block, but it's outside of it's owning colony's radius.
        if (coloniesByWorld.containsKey(w.provider.getDimension()))
        {
            for (@NotNull final Colony otherColony : coloniesByWorld.get(w.provider.getDimension()))
            {
                final AbstractBuilding building = otherColony.getBuilding(pos);
                if (building != null)
                {
                    return building;
                }
            }
        }

        return null;
    }

    /**
     * Get colony that contains a given coordinate.
     *
     * @param w   World.
     * @param pos coordinates.
     * @return Colony at the given location.
     */
    public static Colony getColony(@NotNull final World w, @NotNull final BlockPos pos)
    {
        final List<Colony> coloniesInWorld = coloniesByWorld.get(w.provider.getDimension());
        if (coloniesInWorld == null)
        {
            return null;
        }

        for (@NotNull final Colony c : coloniesInWorld)
        {
            if (c.isCoordInColony(w, pos))
            {
                return c;
            }
        }

        return null;
    }

    /**
     * Get all colonies in this world.
     *
     * @param w World.
     * @return a list of colonies.
     */
    @NotNull
    public static List<Colony> getColonies(@NotNull final World w)
    {
        final List<Colony> coloniesInWorld = coloniesByWorld.get(w.provider.getDimension());
        if (coloniesInWorld == null)
        {
            return new ArrayList<>();
        }
        return coloniesInWorld;
    }

    /**
     * Get all colonies in all worlds.
     *
     * @return a list of colonies.
     */
    @NotNull
    public static List<Colony> getColonies()
    {
        return new ArrayList<>(colonies.values());
    }

    /**
     * Get a AbstractBuilding by position.
     *
     * @param pos Block position.
     * @return Returns the view belonging to the building at (x, y, z).
     */
    public static AbstractBuilding.View getBuildingView(final BlockPos pos)
    {
        //  On client we will just check all known views
        for (@NotNull final ColonyView colony : colonyViews.values())
        {
            final AbstractBuilding.View building = colony.getBuilding(pos);
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
     * On servers it returns the colony itself.
     *
     * @param w   World.
     * @param pos coordinates.
     * @return View of colony or colony itself depending on side.
     */
    @Nullable
    public static IColony getIColony(@NotNull final World w, @NotNull final BlockPos pos)
    {
        return w.isRemote ? getColonyView(w, pos) : getColony(w, pos);
    }

    /**
     * Get Colony that contains a given (x, y, z).
     *
     * @param w   World.
     * @param pos coordinates.
     * @return returns the view belonging to the colony at x, y, z.
     */
    private static ColonyView getColonyView(@NotNull final World w, @NotNull final BlockPos pos)
    {
        for (@NotNull final ColonyView c : colonyViews.values())
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
     * @param w   World.
     * @param pos Block position.
     * @return View of colony or colony itself depending on side, closest to coordinates.
     */
    @Nullable
    public static IColony getClosestIColony(@NotNull final World w, @NotNull final BlockPos pos)
    {
        return w.isRemote ? getClosestColonyView(w, pos) : getClosestColony(w, pos);
    }

    /**
     * Returns the closest view {@link #getColonyView(World, BlockPos)}.
     *
     * @param w   World.
     * @param pos Block Position.
     * @return View of the closest colony.
     */
    @Nullable
    public static ColonyView getClosestColonyView(@NotNull final World w, @NotNull final BlockPos pos)
    {
        @Nullable ColonyView closestColony = null;
        long closestDist = Long.MAX_VALUE;

        for (@NotNull final ColonyView c : colonyViews.values())
        {
            if (c.getDimension() == w.provider.getDimension())
            {
                final long dist = c.getDistanceSquared(pos);
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
     * Get closest colony by x,y,z.
     *
     * @param w   World.
     * @param pos coordinates.
     * @return Colony closest to coordinates.
     */
    public static Colony getClosestColony(@NotNull final World w, @NotNull final BlockPos pos)
    {
        final List<Colony> coloniesInWorld = coloniesByWorld.get(w.provider.getDimension());
        if (coloniesInWorld == null)
        {
            return null;
        }

        @Nullable Colony closestColony = null;
        long closestDist = Long.MAX_VALUE;

        for (@NotNull final Colony c : coloniesInWorld)
        {
            if (c.getDimension() == w.provider.getDimension())
            {
                final long dist = c.getDistanceSquared(pos);
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
     * On servers it returns the colony itself.
     * <p>
     * Returns a colony or view with the given Player as owner.
     *
     * @param w     World.
     * @param owner Entity Player.
     * @return IColony belonging to specific player.
     */
    @Nullable
    public static IColony getIColonyByOwner(@NotNull final World w, @NotNull final EntityPlayer owner)
    {
        return getIColonyByOwner(w, w.isRemote ? owner.getUniqueID() : owner.getGameProfile().getId());
    }

    /**
     * Side neutral method to get colony.
     * On clients it returns the view.
     * On servers it returns the colony itself.
     * <p>
     * Returns a colony or view with given Player as owner.
     *
     * @param w     World
     * @param owner UUID of the owner.
     * @return IColony belonging to specific player.
     */
    @Nullable
    public static IColony getIColonyByOwner(@NotNull final World w, final UUID owner)
    {
        return w.isRemote ? getColonyViewByOwner(owner) : getColonyByOwner(owner);
    }

    /**
     * Returns a ColonyView with specific owner.
     *
     * @param owner UUID of the owner.
     * @return ColonyView.
     */
    private static IColony getColonyViewByOwner(final UUID owner)
    {
        for (@NotNull final ColonyView c : colonyViews.values())
        {
            final Permissions.Player p = c.getPlayers().get(owner);
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
     * @param owner UUID of the owner.
     * @return Colony that belong to given owner UUID.
     */
    @Nullable
    private static IColony getColonyByOwner(@Nullable final UUID owner)
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
     * @return Minimum town hall distance.
     */
    public static double getMinimumDistanceBetweenTownHalls()
    {
        //  [TownHall](Radius)+(Padding)+(Radius)[TownHall]
        return (2D * Configurations.workingRangeTownHall) + Configurations.townHallPadding;
    }

    /**
     * On server tick, tick every Colony.
     * NOTE: Review this for performance.
     *
     * @param event {@link net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent}
     */
    public static void onServerTick(@NotNull final TickEvent.ServerTickEvent event)
    {
        for (@NotNull final Colony c : colonies.values())
        {
            c.onServerTick(event);
        }

        if (saveNeeded)
        {
            saveColonies();
        }
    }

    /**
     * Save all the Colonies.
     */
    private static void saveColonies()
    {
        @NotNull final NBTTagCompound compound = new NBTTagCompound();
        writeToNBT(compound);

        @NotNull final File file = getSaveLocation();
        saveNBTToPath(file, compound);

        saveNeeded = false;
    }

    /**
     * Write colonies to NBT data for saving.
     *
     * @param compound NBT-Tag.
     */
    public static void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        @NotNull final NBTTagList colonyTagList = new NBTTagList();
        for (@NotNull final Colony colony : colonies.values())
        {
            @NotNull final NBTTagCompound colonyTagCompound = new NBTTagCompound();
            colony.writeToNBT(colonyTagCompound);
            colonyTagList.appendTag(colonyTagCompound);
        }
        compound.setTag(TAG_COLONIES, colonyTagList);
    }

    /**
     * Get save location for Minecolonies data, from the world/save directory.
     *
     * @return Save file for minecolonies.
     */
    @NotNull
    private static File getSaveLocation()
    {
        @NotNull final File saveDir = new File(DimensionManager.getWorld(0).getSaveHandler().getWorldDirectory(), FILENAME_MINECOLONIES_PATH);
        return new File(saveDir, FILENAME_MINECOLONIES);
    }

    /**
     * Save an NBTTagCompound to a file.  Does so in a safe manner using an intermediate tmp file.
     *
     * @param file     The destination file to write the data to.
     * @param compound The NBTTagCompound to write to the file.
     */
    private static void saveNBTToPath(@Nullable final File file, @NotNull final NBTTagCompound compound)
    {
        try
        {
            if (file != null)
            {
                file.getParentFile().mkdir();
                CompressedStreamTools.safeWrite(compound, file);
            }
        }
        catch (final IOException exception)
        {
            Log.getLogger().error("Exception when saving ColonyManager", exception);
        }
    }

    /**
     * On Client tick, clears views when player left.
     *
     * @param event {@link net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent}.
     */
    public static void onClientTick(@NotNull final TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END && Minecraft.getMinecraft().world == null && !colonyViews.isEmpty())
        {
            //  Player has left the game, clear the Colony View cache
            colonyViews.clear();
        }
    }

    /**
     * On world tick, tick every Colony in that world.
     * NOTE: Review this for performance.
     *
     * @param event {@link net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent}.
     */
    public static void onWorldTick(
                                    @NotNull final TickEvent.WorldTickEvent event)
    {
        final Map<Integer, Colony> coloniesCopy = new HashMap<>(colonies);

        coloniesCopy.values().stream()
          .filter(c -> c.getDimension() == event.world.provider.getDimension())
          .forEach(c -> c.onWorldTick(event));
    }

    /**
     * When a world is loaded, Colonies in that world need to grab the reference to the World.
     * Additionally, when loading the first world, load all colonies.
     *
     * @param world World.
     */
    public static void onWorldLoad(@NotNull final World world)
    {
        if (!world.isRemote)
        {
            if (numWorldsLoaded == 0)
            {
                @NotNull final File file = getSaveLocation();
                @Nullable final NBTTagCompound data = loadNBTFromPath(file);
                if (data != null)
                {
                    readFromNBT(data);
                }
            }
            ++numWorldsLoaded;

            final List<Colony> worldColonies = coloniesByWorld.get(world.provider.getDimension());
            if (worldColonies != null)
            {
                for (@NotNull final Colony c : worldColonies)
                {
                    c.onWorldLoad(world);
                }
            }

            world.addEventListener(new ColonyManagerWorldAccess());
        }
    }

    /**
     * Load a file and return the data as an NBTTagCompound.
     *
     * @param file The path to the file.
     * @return the data from the file as an NBTTagCompound, or null.
     */
    private static NBTTagCompound loadNBTFromPath(
                                                   @Nullable final File file)
    {
        try
        {
            if (file != null && file.exists())
            {
                return CompressedStreamTools.read(file);
            }
        }
        catch (final IOException exception)
        {
            Log.getLogger().error("Exception when loading ColonyManger", exception);
        }
        return null;
    }

    /**
     * Read Colonies from saved NBT data.
     *
     * @param compound NBT Tag.
     */
    public static void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        final NBTTagList colonyTags = compound.getTagList(TAG_COLONIES, NBT.TAG_COMPOUND);
        for (int i = 0; i < colonyTags.tagCount(); ++i)
        {
            @NotNull final Colony colony = Colony.loadColony(colonyTags.getCompoundTagAt(i));
            colonies.put(colony.getID(), colony);

            if (!coloniesByWorld.containsKey(colony.getDimension()))
            {
                coloniesByWorld.put(colony.getDimension(), new ArrayList<>());
            }
            coloniesByWorld.get(colony.getDimension()).add(colony);

            topColonyId = Math.max(topColonyId, colony.getID());
        }

        Log.getLogger().info(String.format("Loaded %d colonies", colonies.size()));
    }

    /**
     * Saves data when world is saved.
     *
     * @param world World.
     */
    public static void onWorldSave(@NotNull final World world)
    {
        //We save when the first dimension is saved.
        if (!world.isRemote && world.provider.getDimension() == 0)
        {
            saveColonies();
        }
    }

    /**
     * When a world unloads, all colonies in that world are informed.
     * Additionally, when the last world is unloaded, delete all colonies.
     *
     * @param world World.
     */
    public static void onWorldUnload(@NotNull final World world)
    {
        if (!world.isRemote)
        {
            final List<Colony> worldColonies = coloniesByWorld.get(world.provider.getDimension());
            if (worldColonies != null)
            {
                for (@NotNull final Colony c : worldColonies)
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
     * Sends view message to the right view.
     *
     * @param colonyId          ID of the colony.
     * @param colonyData        {@link ByteBuf} with colony data.
     * @param isNewSubscription whether this is a new subscription or not.
     * @return the response message.
     */
    @Nullable
    public static IMessage handleColonyViewMessage(final int colonyId, @NotNull final ByteBuf colonyData, final boolean isNewSubscription)
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
     * Get ColonyView by ID.
     *
     * @param id ID of colony.
     * @return The ColonyView belonging to the colony.
     */
    public static ColonyView getColonyView(final int id)
    {
        return colonyViews.get(id);
    }

    /**
     * Returns result of {@link ColonyView#handlePermissionsViewMessage(ByteBuf)} if {@link #getColonyView(int)}.
     * gives a not-null result. If {@link #getColonyView(int)} is null, returns null.
     *
     * @param colonyID ID of the colony.
     * @param data     {@link ByteBuf} with colony data.
     * @return result of {@link ColonyView#handlePermissionsViewMessage(ByteBuf)} or null.
     */
    public static IMessage handlePermissionsViewMessage(final int colonyID, @NotNull final ByteBuf data)
    {
        final ColonyView view = getColonyView(colonyID);
        if (view == null)
        {
            Log.getLogger().error(String.format("Colony view does not exist for ID #%d", colonyID));
            return null;
        }
        else
        {
            return view.handlePermissionsViewMessage(data);
        }
    }

    /**
     * Returns result of {@link ColonyView#handleColonyViewCitizensMessage(int, ByteBuf)} if {@link #getColonyView(int)}
     * gives a not-null result. If {@link #getColonyView(int)} is null, returns null.
     *
     * @param colonyId  ID of the colony.
     * @param citizenId ID of the citizen.
     * @param buf       {@link ByteBuf} with colony data.
     * @return result of {@link ColonyView#handleColonyViewCitizensMessage(int, ByteBuf)} or null.
     */
    public static IMessage handleColonyViewCitizensMessage(final int colonyId, final int citizenId, final ByteBuf buf)
    {
        final ColonyView view = getColonyView(colonyId);
        if (view == null)
        {
            return null;
        }
        return view.handleColonyViewCitizensMessage(citizenId, buf);
    }

    /**
     * Returns result of {@link ColonyView#handleColonyViewWorkOrderMessage(ByteBuf)} (int, ByteBuf)} if {@link #getColonyView(int)}
     * gives a not-null result. If {@link #getColonyView(int)} is null, returns null.
     *
     * @param colonyId ID of the colony.
     * @param buf      {@link ByteBuf} with colony data.
     * @return result of {@link ColonyView#handleColonyViewWorkOrderMessage(ByteBuf)} or null.
     */
    public static IMessage handleColonyViewWorkOrderMessage(final int colonyId, final ByteBuf buf)
    {
        final ColonyView view = getColonyView(colonyId);
        if (view == null)
        {
            return null;
        }
        return view.handleColonyViewWorkOrderMessage(buf);
    }

    /**
     * Returns result of {@link ColonyView#handleColonyViewRemoveCitizenMessage(int)} if {@link #getColonyView(int)}
     * gives a not-null result. If {@link #getColonyView(int)} is null, returns null.
     *
     * @param colonyId  ID of the colony.
     * @param citizenId ID of the citizen.
     * @return result of {@link ColonyView#handleColonyViewRemoveCitizenMessage(int)}  or null.
     */
    public static IMessage handleColonyViewRemoveCitizenMessage(final int colonyId, final int citizenId)
    {
        final ColonyView view = getColonyView(colonyId);
        if (view != null)
        {
            //  Can legitimately be NULL, because (to keep the code simple and fast), it is
            //  possible to receive a 'remove' notice before receiving the View.
            return view.handleColonyViewRemoveCitizenMessage(citizenId);
        }

        return null;
    }

    /**
     * Returns result of {@link ColonyView#handleColonyBuildingViewMessage(BlockPos, ByteBuf)} if {@link #getColonyView(int)}
     * gives a not-null result. If {@link #getColonyView(int)} is null, returns null.
     *
     * @param colonyId   ID of the colony.
     * @param buildingId ID of the building.
     * @param buf        {@link ByteBuf} with colony data.
     * @return result of {@link ColonyView#handleColonyBuildingViewMessage(BlockPos, ByteBuf)} or null.
     */
    public static IMessage handleColonyBuildingViewMessage(final int colonyId, final BlockPos buildingId, @NotNull final ByteBuf buf)
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
     * gives a not-null result. If {@link #getColonyView(int)} is null, returns null.
     *
     * @param colonyId   ID of the colony.
     * @param buildingId ID of the building.
     * @return result of {@link ColonyView#handleColonyViewRemoveBuildingMessage(BlockPos)}  or null.
     */
    public static IMessage handleColonyViewRemoveBuildingMessage(final int colonyId, final BlockPos buildingId)
    {
        final ColonyView view = getColonyView(colonyId);
        if (view != null)
        {
            //  Can legitimately be NULL, because (to keep the code simple and fast), it is
            //  possible to receive a 'remove' notice before receiving the View.
            return view.handleColonyViewRemoveBuildingMessage(buildingId);
        }

        return null;
    }

    /**
     * Returns result of {@link ColonyView#handleColonyViewRemoveWorkOrderMessage(int)} if {@link #getColonyView(int)}
     * gives a not-null result. If {@link #getColonyView(int)} is null, returns null.
     *
     * @param colonyId    ID of the colony.
     * @param workOrderId ID of the workOrder.
     * @return result of {@link ColonyView#handleColonyViewRemoveWorkOrderMessage(int)}  or null.
     */
    public static IMessage handleColonyViewRemoveWorkOrderMessage(final int colonyId, final int workOrderId)
    {
        final ColonyView view = getColonyView(colonyId);
        if (view != null)
        {
            //  Can legitimately be NULL, because (to keep the code simple and fast), it is
            //  possible to receive a 'remove' notice before receiving the View.
            return view.handleColonyViewRemoveWorkOrderMessage(workOrderId);
        }

        return null;
    }
}
