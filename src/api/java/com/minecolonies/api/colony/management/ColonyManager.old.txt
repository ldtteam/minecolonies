package com.minecolonies.api.colony.management;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyList;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.permissions.Player;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.Log;
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
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Singleton class that links colonies to minecraft.
 */
public final class ColonyManager implements IColonyManager
{
    /**
     * The file name of the minecolonies path.
     */
    private final String FILENAME_MINECOLONIES_PATH = "minecolonies";

    /**
     * The file name of the minecolonies.
     */
    private final String FILENAME_MINECOLONIES = "colonies.dat";

    /**
     * The file name pattern of the minecolonies backup.
     */
    private final String FILENAME_MINECOLONIES_BACKUP = "colonies-%s.dat";

    /**
     * The tag of the colonies.
     */
    private final String TAG_COLONIES = "colonies";
    /**
     * The tag of the pseudo unique identifier
     */
    private final String TAG_UUID     = "uuid";

    /**
     * The damage source used to kill citizens.
     */
    private final DamageSource CONSOLE_DAMAGE_SOURCE = new DamageSource("Console");
    /**
     * The list of all colonies.
     */
    @NotNull
    private final IColonyList<IColony> colonies;
    /**
     * The list of all colonies by world.
     */
    @NotNull
    private final Map<Integer, List<Colony>> coloniesByWorld = new HashMap<>();
    /**
     * The list of colony views.
     */
    @NotNull
    private final ColonyList<ColonyView>     colonyViews     = new ColonyList<>();

    /**
     * A buffer value to be sure to be outside of the colony.
     */
    private final int BUFFER = 10;

    /**
     * The last colony id.
     */
    private int topColonyId = 0;
    /**
     * Amount of worlds loaded.
     */
    private int     numWorldsLoaded;
    /**
     * Whether the colonyManager should persist data.
     */
    private boolean saveNeeded;

    /**
     * Indicate if a schematic have just been downloaded.
     * Client only
     */
    private          boolean schematicDownloaded = false;
    /**
     * Pseudo unique id for the server
     */
    private volatile UUID    serverUUID          = null;

    private ColonyManager()
    {
        //Hides default constructor.
    }

    @Override
    @NotNull
    public Colony createColony(@NotNull final World w, final BlockPos pos, @NotNull final EntityPlayer player)
    {
        final Colony colony = colonies.create(w, pos);

        addColonyByWorld(colony);

        final String colonyName = LanguageHandler.format("com.minecolonies.coremod.gui.townHall.defaultName", player.getDisplayNameString());
        colony.setName(colonyName);
        colony.getPermissions().setPlayerRank(player.getGameProfile().getId(), Rank.OWNER, w);

        colony.triggerAchievement(ModAchievements.achievementGetSupply);
        colony.triggerAchievement(ModAchievements.achievementTownhall);

        markDirty();

        Log.getLogger().info(String.format("New Colony Id: %d by %s", colony.getID(), player.getName()));

        return colony;
    }

    private void addColonyByWorld(Colony colony)
    {
        coloniesByWorld.computeIfAbsent(colony.getDimension(), ArrayList::new).add(colony);
    }

    @Override
    public void markDirty()
    {
        saveNeeded = true;
    }

    @Override
    public void deleteColony(final int id)
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
                final EntityCitizen entityCitizen = citizenData.getCitizen();
                if (entityCitizen != null)
                {
                    final World world = entityCitizen.getEntityWorld();
                    citizenData.getCitizen().onDeath(CONSOLE_DAMAGE_SOURCE);
                    colonyWorlds.add(world);
                }
            }
            Log.getLogger().info("Removing buildings for " + id);
            for (final IBuilding buildingCore : new ArrayList<>(colony.getBuildings().values()))
            {
                AbstractBuilding building = (AbstractBuilding) buildingCore;

                final BlockPos location = building.getLocation().getInDimensionLocation();
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

    @Override
    public Colony getColony(final int id)
    {
        return colonies.get(id);
    }

    @Override
    public void syncAllColoniesAchievements()
    {
        colonies.forEach(AchievementUtils::syncAchievements);
    }

    @Override
    public AbstractBuilding getBuilding(@NotNull final World w, @NotNull final BlockPos pos)
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
        for (@NotNull final Colony otherColony : getColonies(w))
        {
            final AbstractBuilding building = otherColony.getBuilding(pos);
            if (building != null)
            {
                return building;
            }
        }

        return null;
    }

    @Override
    public Colony getColony(@NotNull final World w, @NotNull final BlockPos pos)
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

    @Override
    @NotNull
    public List<Colony> getColonies(@NotNull final World w)
    {
        final List<Colony> coloniesInWorld = coloniesByWorld.get(w.provider.getDimension());
        if (coloniesInWorld == null)
        {
            return Collections.emptyList();
        }
        return coloniesInWorld;
    }

    @Override
    @NotNull
    public List<Colony> getColonies()
    {
        return colonies.getCopyAsList();
    }

    @Override
    public AbstractBuilding.View getBuildingView(final BlockPos pos)
    {
        //  On client we will just check all known views
        for (@NotNull final ColonyView colony : colonyViews)
        {
            final AbstractBuilding.View building = colony.getBuilding(pos);
            if (building != null)
            {
                return building;
            }
        }

        return null;
    }

    @Override
    @Nullable
    public IColony getColony(@NotNull final World w, @NotNull final BlockPos pos)
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
    private ColonyView getColonyView(@NotNull final World w, @NotNull final BlockPos pos)
    {
        for (@NotNull final ColonyView c : colonyViews)
        {
            if (c.isCoordInColony(w, pos))
            {
                return c;
            }
        }

        return null;
    }

    @Override
    @Nullable
    public IColony getClosestColony(@NotNull final World w, @NotNull final BlockPos pos)
    {
        return w.isRemote ? getClosestColonyView(w, pos) : getClosestColony(w, pos);
    }

    @Override
    @Nullable
    public ColonyView getClosestColonyView(@NotNull final World w, @NotNull final BlockPos pos)
    {
        @Nullable ColonyView closestColony = null;
        long closestDist = Long.MAX_VALUE;

        for (@NotNull final ColonyView c : colonyViews)
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

    @Override
    public Colony getClosestColony(@NotNull final World w, @NotNull final BlockPos pos)
    {
        @Nullable Colony closestColony = null;
        long closestDist = Long.MAX_VALUE;

        for (@NotNull final Colony c : getColonies(w))
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

    @Override
    @Nullable
    public IColony getColonyByOwner(@NotNull final World w, @NotNull final EntityPlayer owner)
    {
        return getColonyByOwner(w, w.isRemote ? owner.getUniqueID() : owner.getGameProfile().getId());
    }

    @Override
    @Nullable
    public IColony getColonyByOwner(@NotNull final World w, final UUID owner)
    {
        return w.isRemote ? getColonyViewByOwner(owner) : getColonyByOwner(owner);
    }

    /**
     * Returns a ColonyView with specific owner.
     *
     * @param owner UUID of the owner.
     * @return ColonyView.
     */
    private IColony getColonyViewByOwner(final UUID owner)
    {
        for (@NotNull final ColonyView c : colonyViews)
        {
            final Player p = c.getPlayers().get(owner);
            if (p != null && p.getRank().equals(Rank.OWNER))
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
    private IColony getColonyByOwner(@Nullable final UUID owner)
    {
        if (owner == null)
        {
            return null;
        }

        return colonies.stream()
                 .filter(c -> owner.equals(c.getPermissions().getOwner()))
                 .findFirst()
                 .orElse(null);
    }

    @Override
    public int getMinimumDistanceBetweenTownHalls()
    {
        //  [TownHall](Radius)+(Padding)+(Radius)[TownHall]
        return (2 * Configurations.workingRangeTownHall) + Configurations.townHallPadding;
    }

    @Override
    public void onServerTick(@NotNull final TickEvent.ServerTickEvent event)
    {
        for (@NotNull final Colony c : colonies)
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
    private void saveColonies()
    {
        @NotNull final NBTTagCompound compound = new NBTTagCompound();
        writeToNBT(compound);

        @NotNull final File file = getSaveLocation();
        saveNBTToPath(file, compound);

        saveNeeded = false;
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        @NotNull final NBTTagList colonyTagList = new NBTTagList();
        for (@NotNull final Colony colony : colonies)
        {
            @NotNull final NBTTagCompound colonyTagCompound = new NBTTagCompound();
            colony.writeToNBT(colonyTagCompound);
            colonyTagList.appendTag(colonyTagCompound);
        }
        compound.setTag(TAG_COLONIES, colonyTagList);
        if (serverUUID != null)
        {
            compound.setUniqueId(TAG_UUID, serverUUID);
        }
    }

    /**
     * Get save location for Minecolonies data, from the world/save directory.
     *
     * @return Save file for minecolonies.
     */
    @NotNull
    private File getSaveLocation()
    {
        @NotNull final File saveDir = new File(DimensionManager.getWorld(0).getSaveHandler().getWorldDirectory(), FILENAME_MINECOLONIES_PATH);
        return new File(saveDir, FILENAME_MINECOLONIES);
    }

    /**
     * Save an NBTTagCompound to a file.  Does so in a safe manner using an
     * intermediate tmp file.
     *
     * @param file     The destination file to write the data to.
     * @param compound The NBTTagCompound to write to the file.
     */
    private void saveNBTToPath(@Nullable final File file, @NotNull final NBTTagCompound compound)
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

    @Override
    public void onClientTick(@NotNull final TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END && Minecraft.getMinecraft().world == null && !colonyViews.isEmpty())
        {
            //  Player has left the game, clear the Colony View cache
            colonyViews.clear();
        }
    }

    @Override
    public void onWorldTick(@NotNull final TickEvent.WorldTickEvent event)
    {
        getColonies(event.world).forEach(c -> c.onWorldTick(event));
    }

    @Override
    public void onWorldLoad(@NotNull final World world)
    {
        if (!world.isRemote)
        {
            if (numWorldsLoaded == 0)
            {
                if (!backupColonyData())
                {
                    MineColonies.getLogger().error("Failed to save " + FILENAME_MINECOLONIES + " backup!");
                }

                //load the structures when we know where the world is
                Structures.init();

                @NotNull final File file = getSaveLocation();
                @Nullable final NBTTagCompound data = loadNBTFromPath(file);
                if (data != null)
                {
                    readFromNBT(data);
                }
                if (serverUUID == null)
                {
                    serverUUID = UUID.randomUUID();
                    Log.getLogger().info(String.format("New Server UUID %s", serverUUID));
                    markDirty();
                }
                else
                {
                    Log.getLogger().info(String.format("Server UUID %s", serverUUID));
                }
            }
            ++numWorldsLoaded;

            for (@NotNull final Colony c : getColonies(world))
            {
                c.onWorldLoad(world);
            }

            world.addEventListener(new ColonyManagerWorldAccess());
        }
    }

    @Override
    public boolean backupColonyData()
    {
        if (numWorldsLoaded > 0 && saveNeeded)
        {
            saveColonies();
        }

        @NotNull final File file = getSaveLocation();
        @NotNull final File targetFile = getBackupSaveLocation(new Date());
        if (!file.exists())
        {
            return true;
        }
        else if (targetFile.exists())
        {
            return false;
        }

        try
        {
            Files.copy(file.toPath(), targetFile.toPath());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return targetFile.exists();
    }

    /**
     * Load a file and return the data as an NBTTagCompound.
     *
     * @param file The path to the file.
     * @return the data from the file as an NBTTagCompound, or null.
     */
    private NBTTagCompound loadNBTFromPath(@Nullable final File file)
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

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        final NBTTagList colonyTags = compound.getTagList(TAG_COLONIES, NBT.TAG_COMPOUND);
        for (int i = 0; i < colonyTags.tagCount(); ++i)
        {
            @NotNull final Colony colony = Colony.loadColony(colonyTags.getCompoundTagAt(i));
            colonies.add(colony);

            addColonyByWorld(colony);
        }

        if (compound.hasUniqueId(TAG_UUID))
        {
            serverUUID = compound.getUniqueId(TAG_UUID);
        }

        Log.getLogger().info(String.format("Loaded %d colonies", colonies.size()));
    }

    /**
     * Get save location for Minecolonies backup data, from the world/save
     * directory.
     *
     * @return Save file for minecolonies.
     */
    @NotNull
    private File getBackupSaveLocation(Date date)
    {
        @NotNull final File saveDir = new File(DimensionManager.getWorld(0).getSaveHandler().getWorldDirectory(), FILENAME_MINECOLONIES_PATH);
        return new File(saveDir, String.format(FILENAME_MINECOLONIES_BACKUP, new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(date)));
    }

    @Override
    public void setServerUUID(final UUID uuid)
    {
        serverUUID = uuid;
    }

    @Override
    public UUID getServerUUID()
    {
        return serverUUID;
    }

    @Override
    public void onWorldSave(@NotNull final World world)
    {
        //We save when the first dimension is saved.
        if (!world.isRemote && world.provider.getDimension() == 0)
        {
            saveColonies();
        }
    }

    @Override
    public void onWorldUnload(@NotNull final World world)
    {
        if (!world.isRemote)
        {
            for (@NotNull final Colony c : getColonies(world))
            {
                c.onWorldUnload(world);
            }

            --numWorldsLoaded;
            if (numWorldsLoaded == 0)
            {
                colonies.clear();
                coloniesByWorld.clear();
            }
        }
    }

    @Override
    @Nullable
    public IMessage handleColonyViewMessage(final int colonyId, @NotNull final ByteBuf colonyData, final boolean isNewSubscription)
    {
        ColonyView view = getColonyView(colonyId);
        if (view == null)
        {
            view = ColonyView.createFromNetwork(colonyId);
            colonyViews.add(view);
        }

        return view.handleColonyViewMessage(colonyData, isNewSubscription);
    }

    @Override
    public ColonyView getColonyView(final int id)
    {
        return colonyViews.get(id);
    }

    @Override
    public IMessage handlePermissionsViewMessage(final int colonyID, @NotNull final ByteBuf data)
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

    @Override
    public IMessage handleColonyViewCitizensMessage(final int colonyId, final int citizenId, final ByteBuf buf)
    {
        final ColonyView view = getColonyView(colonyId);
        if (view == null)
        {
            return null;
        }
        return view.handleColonyViewCitizensMessage(citizenId, buf);
    }

    @Override
    public IMessage handleColonyViewWorkOrderMessage(final int colonyId, final ByteBuf buf)
    {
        final ColonyView view = getColonyView(colonyId);
        if (view == null)
        {
            return null;
        }
        return view.handleColonyViewWorkOrderMessage(buf);
    }

    @Override
    public IMessage handleColonyViewRemoveCitizenMessage(final int colonyId, final int citizenId)
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

    @Override
    public IMessage handleColonyBuildingViewMessage(
                                                     final int colonyId,
                                                     final BlockPos buildingLocation,
                                                     final IToken buildingId,
                                                     @NotNull final ByteBuf buf)
    {
        final ColonyView view = getColonyView(colonyId);
        if (view != null)
        {
            return view.handleColonyBuildingViewMessage(buildingLocation, buildingId, buf);
        }
        else
        {
            Log.getLogger().error(String.format("Colony view does not exist for ID #%d", colonyId));
            return null;
        }
    }

    @Override
    public IMessage handleColonyViewRemoveBuildingMessage(final int colonyId, final BlockPos buildingId)
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

    @Override
    public IMessage handleColonyViewRemoveWorkOrderMessage(final int colonyId, final int workOrderId)
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

    @Override
    public boolean isSchematicDownloaded()
    {
        return schematicDownloaded;
    }

    @Override
    public void setSchematicDownloaded(boolean downloaded)
    {
        schematicDownloaded = downloaded;
    }

    @Override
    public boolean isCoordinateInAnyColony(@NotNull final World world, final BlockPos pos)
    {
        for (@NotNull final ColonyView c : colonyViews)
        {
            if (c.getDimension() == world.provider.getDimension())
            {
                final long dist = c.getDistanceSquared(pos);
                if (dist < (Configurations.workingRangeTownHall + Configurations.townHallPadding + BUFFER))
                {
                    return true;
                }
            }
        }
        return false;
    }
}
