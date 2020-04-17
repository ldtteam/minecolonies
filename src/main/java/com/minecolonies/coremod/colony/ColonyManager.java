package com.minecolonies.coremod.colony;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.permissions.Player;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.compatibility.CompatibilityManager;
import com.minecolonies.api.compatibility.ICompatibilityManager;
import com.minecolonies.api.crafting.IRecipeManager;
import com.minecolonies.api.util.ChunkLoadStorage;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.requestsystem.management.manager.StandardRecipeManager;
import com.minecolonies.coremod.util.BackUpHelper;
import com.minecolonies.coremod.util.ChunkDataHelper;
import com.minecolonies.coremod.util.FurnaceRecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerMultiWorld;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

import static com.minecolonies.api.util.constant.ColonyManagerConstants.*;
import static com.minecolonies.api.util.constant.Constants.BLOCKS_PER_CHUNK;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_COMPATABILITY_MANAGER;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_UUID;
import static com.minecolonies.coremod.MineColonies.*;

/**
 * Singleton class that links colonies to minecraft.
 */
@SuppressWarnings("PMD.ExcessiveClassLength")
public final class ColonyManager implements IColonyManager
{
    /**
     * The list of colony views.
     */
    @NotNull
    private final Map<Integer, ColonyList<IColonyView>> colonyViews = new HashMap<>();

    /**
     * Recipemanager of this server.
     */
    private final IRecipeManager recipeManager = new StandardRecipeManager();

    /**
     * Creates a new compatibilityManager.
     */
    private final ICompatibilityManager compatibilityManager = new CompatibilityManager();

    /**
     * Pseudo unique id for the server
     */
    private UUID serverUUID = null;

    /**
     * Indicate if a schematic have just been downloaded.
     * Client only
     */
    private boolean schematicDownloaded = false;

    /**
     * If the manager finished loading already.
     */
    private boolean loaded = false;

    /**
     * Create a new Colony in the given world and at that location.
     *
     * @param w      World of the colony.
     * @param pos    Coordinate of the center of the colony.
     * @param player the player that creates the colony - owner.
     * @param style  the default style of the colony.
     */
    @Override
    public void createColony(@NotNull final World w, final BlockPos pos, @NotNull final PlayerEntity player, @NotNull final String style)
    {
        final IColonyManagerCapability cap = w.getCapability(COLONY_MANAGER_CAP, null).orElseGet(null);
        if (cap == null)
        {
            Log.getLogger().warn(MISSING_WORLD_CAP_MESSAGE);
            return;
        }

        final IColony colony = cap.createColony(w, pos);
        colony.setStyle(style);

        final String colonyName = LanguageHandler.format("com.minecolonies.coremod.gui.townHall.defaultName", player.getName().getFormattedText());
        colony.setName(colonyName);
        colony.getPermissions().setOwner(player);

        colony.getPackageManager().addImportantColonyPlayer((ServerPlayerEntity) player);
        colony.getPackageManager().addCloseSubscriber((ServerPlayerEntity) player);

        Log.getLogger().info(String.format("New Colony Id: %d by %s", colony.getID(), player.getName().getFormattedText()));

        if (colony.getWorld() == null)
        {
            Log.getLogger().error("Unable to claim chunks because of the missing world in the colony, please report this to the mod authors!", new Exception());
            return;
        }

        if (MineColonies.getConfig().getCommon().enableDynamicColonySizes.get())
        {
            ChunkDataHelper.claimColonyChunks(colony.getWorld(), true, colony.getID(), colony.getCenter(), colony.getDimension(), 2);
        }
        else
        {
            ChunkDataHelper.claimColonyChunks(colony.getWorld(), true, colony.getID(), colony.getCenter(), colony.getDimension());
        }
    }

    /**
     * Delete the colony in a world.
     *
     * @param id         the id of it.
     * @param canDestroy if can destroy the buildings.
     * @param world      the world.
     */
    @Override
    public void deleteColonyByWorld(final int id, final boolean canDestroy, final World world)
    {
        deleteColony(getColonyByWorld(id, world), canDestroy);
    }

    /**
     * Delete the colony by dimension.
     *
     * @param id         the id of it.
     * @param canDestroy if can destroy the buildings.
     * @param dimension  the dimension.
     */
    @Override
    public void deleteColonyByDimension(final int id, final boolean canDestroy, final int dimension)
    {
        deleteColony(getColonyByDimension(id, dimension), canDestroy);
    }

    /**
     * Delete a colony and purge all buildings and citizens.
     *
     * @param iColony     the colony to destroy.
     * @param canDestroy if the building outlines should be destroyed as well.
     */
    private void deleteColony(@Nullable final IColony iColony, final boolean canDestroy)
    {
        if(!(iColony instanceof Colony))
        {
            return;
        }

        final Colony colony = (Colony) iColony;
        final int id = colony.getID();
        final World world = colony.getWorld();
        try
        {
            ChunkDataHelper.claimColonyChunks(world, false, id, colony.getCenter(), colony.getDimension());
            Log.getLogger().info("Removing citizens for " + id);
            for (final ICitizenData citizenData : new ArrayList<>(colony.getCitizenManager().getCitizens()))
            {
                Log.getLogger().info("Kill Citizen " + citizenData.getName());
                citizenData.getCitizenEntity().ifPresent(entityCitizen -> {
                    entityCitizen.onDeath(CONSOLE_DAMAGE_SOURCE);
                });
            }

            Log.getLogger().info("Removing buildings for " + id);
            for (final IBuilding building : new ArrayList<>(colony.getBuildingManager().getBuildings().values()))
            {
                final BlockPos location = building.getPosition();
                Log.getLogger().info("Delete Building at " + location);
                if (canDestroy)
                {
                    building.deconstruct();
                }
                building.destroy();
                if (world.getBlockState(location).getBlock() instanceof AbstractBlockHut)
                {
                    Log.getLogger().info("Found Block, deleting " + world.getBlockState(location).getBlock());
                    world.removeBlock(location, false);
                }
            }

            try
            {
                MinecraftForge.EVENT_BUS.unregister(colony.getEventHandler());
            }
            catch (final NullPointerException e)
            {
                Log.getLogger().warn("Can't unregister the event handler twice");
            }

            Log.getLogger().info("Deleting colony: " + colony.getID());

            final IColonyManagerCapability cap = world.getCapability(COLONY_MANAGER_CAP, null).orElseGet(null);
            if (cap == null)
            {
                Log.getLogger().warn(MISSING_WORLD_CAP_MESSAGE);
                return;
            }

            cap.deleteColony(id);
            BackUpHelper.markColonyDeleted(colony.getID(),colony.getDimension());
            Log.getLogger().info("Successfully deleted colony: " + id);
        }
        catch (final RuntimeException e)
        {
            Log.getLogger().warn("Deleting Colony " + id + " errored:", e);
        }
    }

    @Override
    public void removeColonyView(final int id, final int dimension)
    {
        if (colonyViews.containsKey(dimension))
        {
            colonyViews.get(dimension).remove(id);
        }
    }

    /**
     * Get Colony by UUID.
     *
     * @param id ID of colony.
     * @return Colony with given ID.
     */
    @Override
    @Nullable
    public IColony getColonyByWorld(final int id, final World world)
    {
        final IColonyManagerCapability cap = world.getCapability(COLONY_MANAGER_CAP, null).orElseGet(null);
        if (cap == null)
        {
            Log.getLogger().warn(MISSING_WORLD_CAP_MESSAGE);
            return null;
        }
        return cap.getColony(id);
    }

    /**
     * Get Colony by UUID.
     *
     * @param id ID of colony.
     * @return Colony with given ID.
     */
    @Override
    @Nullable
    public IColony getColonyByDimension(final int id, final int dimension)
    {
        final World world = ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.getById(dimension));
        final IColonyManagerCapability cap = world.getCapability(COLONY_MANAGER_CAP, null).orElseGet(null);
        if (cap == null)
        {
            Log.getLogger().warn(MISSING_WORLD_CAP_MESSAGE);
            return null;
        }
        return cap.getColony(id);
    }

    /**
     * Get a AbstractBuilding by a World and coordinates.
     *
     * @param w   World.
     * @param pos Block position.
     * @return AbstractBuilding at the given location.
     */
    @Override
    public IBuilding getBuilding(@NotNull final World w, @NotNull final BlockPos pos)
    {
        @Nullable final IColony colony = getColonyByPosFromWorld(w, pos);
        if (colony != null)
        {
            final IBuilding building = colony.getBuildingManager().getBuilding(pos);
            if (building != null)
            {
                return building;
            }
        }
        Log.getLogger().warn("Colony at place is null! dim:" + w.getDimension().getType().getId() + " pos:" + pos);

        //  Fallback - there might be a AbstractBuilding for this block, but it's outside of it's owning colony's radius.
        for (@NotNull final IColony otherColony : getColonies(w))
        {
            final IBuilding building = otherColony.getBuildingManager().getBuilding(pos);
            if (building != null)
            {
                return building;
            }
        }

        return null;
    }

    /**
     * Get colony that contains a given coordinate from world.
     *
     * @param w   World.
     * @param pos coordinates.
     * @return Colony at the given location.
     */
    @Override
    public IColony getColonyByPosFromWorld(@NotNull final World w, @NotNull final BlockPos pos)
    {
        final Chunk centralChunk = w.getChunkAt(pos);
        final int id = centralChunk.getCapability(CLOSE_COLONY_CAP, null).orElseGet(null).getOwningColony();
        if (id == 0)
        {
            return null;
        }
        return getColonyByWorld(id, w);
    }

    /**
     * Get colony that contains a given coordinate from dimension.
     *
     * @param dim the dimension.
     * @param pos coordinates.
     * @return Colony at the given location.
     */
    @Override
    public IColony getColonyByPosFromDim(final int dim, @NotNull final BlockPos pos)
    {
        final World w = ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.getById(dim));
        return getColonyByPosFromWorld(w, pos);
    }

    /**
     * Check if a position is too close to another colony to found a new colony.
     *
     * @param w   World.
     * @param pos coordinates.
     * @return true if so.
     */
    @Override
    public boolean isTooCloseToColony(@NotNull final World w, @NotNull final BlockPos pos)
    {
        if (MineColonies.getConfig().getCommon().enableDynamicColonySizes.get())
        {
            return !ChunkDataHelper.canClaimChunksInRange(w, pos, MineColonies.getConfig().getCommon().minTownHallPadding.get());
        }
        final IChunkmanagerCapability worldCapability = w.getCapability(CHUNK_STORAGE_UPDATE_CAP, null).orElseGet(null);
        if (worldCapability == null)
        {
            return true;
        }
        final Chunk centralChunk = w.getChunkAt(pos);
        final IColonyTagCapability colonyCap = centralChunk.getCapability(CLOSE_COLONY_CAP, null).orElseGet(null);
        if (colonyCap == null)
        {
            return true;
        }
        final ChunkLoadStorage storage = worldCapability.getChunkStorage(centralChunk.getPos().x, centralChunk.getPos().z);
        if (storage != null)
        {
            storage.applyToCap(colonyCap, centralChunk);
        }
        return !colonyCap.getAllCloseColonies().isEmpty();
    }

    /**
     * Get all colonies in this world.
     *
     * @param w World.
     * @return a list of colonies.
     */
    @Override
    @NotNull
    public List<IColony> getColonies(@NotNull final World w)
    {
        final IColonyManagerCapability cap = w.getCapability(COLONY_MANAGER_CAP, null).orElseGet(null);
        if (cap == null)
        {
            Log.getLogger().warn(MISSING_WORLD_CAP_MESSAGE);
            return Collections.emptyList();
        }
        return cap.getColonies();
    }

    /**
     * Get all colonies in all worlds.
     *
     * @return a list of colonies.
     */
    @Override
    @NotNull
    public List<IColony> getAllColonies()
    {
        final List<IColony> allColonies = new ArrayList<>();
        for (final World world : ServerLifecycleHooks.getCurrentServer().getWorlds())
        {
            final IColonyManagerCapability cap = world.getCapability(COLONY_MANAGER_CAP, null).orElseGet(null);
            if (cap != null)
            {
                allColonies.addAll(cap.getColonies());
            }
        }
        return allColonies;
    }

    /**
     * Get all colonies in all worlds.
     *
     * @param abandonedSince time in hours since the last contact.
     * @return a list of colonies.
     */
    @Override
    @NotNull
    public List<IColony> getColoniesAbandonedSince(final int abandonedSince)
    {
        final List<IColony> sortedList = new ArrayList<>();
        for (final IColony colony : getAllColonies())
        {
            if (colony.getLastContactInHours() >= abandonedSince)
            {
                sortedList.add(colony);
            }
        }

        return sortedList;
    }

    /**
     * Get a AbstractBuilding by position.
     *
     * @param pos Block position.
     * @return Returns the view belonging to the building at (x, y, z).
     */
    @Override
    public IBuildingView getBuildingView(final int dimension, final BlockPos pos)
    {
        if (colonyViews.containsKey(dimension))
        {
            //  On client we will just check all known views
            for (@NotNull final IColonyView colony : colonyViews.get(dimension))
            {
                final IBuildingView building = colony.getBuilding(pos);
                if (building != null)
                {
                    return building;
                }
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
    @Override
    @Nullable
    public IColony getIColony(@NotNull final World w, @NotNull final BlockPos pos)
    {
        return w.isRemote ? getColonyView(w, pos) : getColonyByPosFromWorld(w, pos);
    }

    /**
     * Get Colony that contains a given (x, y, z).
     *
     * @param w   World.
     * @param pos coordinates.
     * @return returns the view belonging to the colony at x, y, z.
     */
    private IColonyView getColonyView(@NotNull final World w, @NotNull final BlockPos pos)
    {
        final Chunk centralChunk = w.getChunkAt(pos);
        final int id = centralChunk.getCapability(CLOSE_COLONY_CAP, null).orElseGet(null).getOwningColony();
        if (id == 0)
        {
            return null;
        }
        return getColonyView(id, w.getDimension().getType().getId());
    }

    /**
     * Side neutral method to get colony.
     * On clients it returns the view.
     * On servers it returns the colony itself.
     * {@link #getClosestColony(World, BlockPos)}
     *
     * @param w   World.
     * @param pos Block position.
     * @return View of colony or colony itself depending on side, closest to
     * coordinates.
     */
    @Override
    @Nullable
    public IColony getClosestIColony(@NotNull final World w, @NotNull final BlockPos pos)
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
    @Override
    @Nullable
    public IColonyView getClosestColonyView(@Nullable final World w, @Nullable final BlockPos pos)
    {
        if (w == null || pos == null)
        {
            return null;
        }

        final Chunk chunk = w.getChunkAt(pos);
        final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null).orElseGet(null);
        if (cap.getOwningColony() != 0)
        {
            return getColonyView(cap.getOwningColony(), w.getDimension().getType().getId());
        }
        else if (!cap.getAllCloseColonies().isEmpty())
        {
            @Nullable IColonyView closestColony = null;
            long closestDist = Long.MAX_VALUE;

            for (final int cId : cap.getAllCloseColonies())
            {
                final IColonyView c = getColonyView(cId, w.getDimension().getType().getId());
                if (c != null && c.getDimension() == w.getDimension().getType().getId())
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

        @Nullable IColonyView closestColony = null;
        long closestDist = Long.MAX_VALUE;

        if (colonyViews.containsKey(w.getDimension().getType().getId()))
        {
            for (@NotNull final IColonyView c : colonyViews.get(w.getDimension().getType().getId()))
            {
                if (c.getDimension() == w.getDimension().getType().getId() && c.getCenter() != null)
                {
                    final long dist = c.getDistanceSquared(pos);
                    if (dist < closestDist)
                    {
                        closestColony = c;
                        closestDist = dist;
                    }
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
    @Override
    public IColony getClosestColony(@NotNull final World w, @NotNull final BlockPos pos)
    {
        final Chunk chunk = w.getChunkAt(pos);
        final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null).orElseGet(null);
        if (cap.getOwningColony() != 0)
        {
            return getColonyByWorld(cap.getOwningColony(), w);
        }
        else if (!cap.getAllCloseColonies().isEmpty())
        {
            @Nullable IColony closestColony = null;
            long closestDist = Long.MAX_VALUE;

            for (final int cId : cap.getAllCloseColonies())
            {
                final IColony c = getColonyByWorld(cId, w);
                if (c != null && c.getDimension() == w.getDimension().getType().getId())
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

        @Nullable IColony closestColony = null;
        long closestDist = Long.MAX_VALUE;

        for (@NotNull final IColony c : getColonies(w))
        {
            if (c.getDimension() == w.getDimension().getType().getId())
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
    @Override
    @Nullable
    public IColony getIColonyByOwner(@NotNull final World w, @NotNull final PlayerEntity owner)
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
    @Override
    @Nullable
    public IColony getIColonyByOwner(@NotNull final World w, final UUID owner)
    {
        return w.isRemote ? getColonyViewByOwner(owner, w.getDimension().getType().getId()) : getColonyByOwner(owner);
    }

    /**
     * Returns a ColonyView with specific owner.
     *
     * @param owner     UUID of the owner.
     * @param dimension the dimension id.
     * @return ColonyView.
     */
    private IColony getColonyViewByOwner(final UUID owner, final int dimension)
    {
        if (colonyViews.containsKey(dimension))
        {
            for (@NotNull final IColonyView c : colonyViews.get(dimension))
            {
                final Player p = c.getPlayers().get(owner);
                if (p != null && p.getRank().equals(Rank.OWNER))
                {
                    return c;
                }
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

        return getAllColonies().stream()
                 .filter(c -> owner.equals(c.getPermissions().getOwner()))
                 .findFirst()
                 .orElse(null);
    }

    /**
     * Returns the minimum distance between two town halls, to not make colonies
     * collide.
     *
     * @return Minimum town hall distance.
     */
    @Override
    public int getMinimumDistanceBetweenTownHalls()
    {
        //  [TownHall](Radius)+(Padding)+(Radius)[TownHall]
        return getConfig().getCommon().minTownHallPadding.get() * BLOCKS_PER_CHUNK;
    }

    @Override
    public void onServerTick(@NotNull final TickEvent.ServerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            for (@NotNull final IColony c : getAllColonies())
            {
                c.onServerTick(event);
            }
        }
    }

    /**
     * Write colonies to NBT data for saving.
     *
     * @param compound NBT-Tag.
     */
    @Override
    public void write(@NotNull final CompoundNBT compound)
    {
        //Get the colonies NBT tags and store them in a ListNBT.
        if (serverUUID != null)
        {
            compound.putUniqueId(TAG_UUID, serverUUID);
        }

        final CompoundNBT compCompound = new CompoundNBT();
        compatibilityManager.write(compCompound);
        compound.put(TAG_COMPATABILITY_MANAGER, compCompound);

        compound.putBoolean(TAG_DISTANCE, true);
        final CompoundNBT recipeCompound = new CompoundNBT();
        recipeManager.write(recipeCompound);

        compound.put(RECIPE_MANAGER_TAG, recipeCompound);
    }

    /**
     * Read Colonies from saved NBT data.
     *
     * @param compound NBT Tag.
     */
    @Override
    public void read(@NotNull final CompoundNBT compound)
    {
        if (compound.hasUniqueId(TAG_UUID))
        {
            serverUUID = compound.getUniqueId(TAG_UUID);
        }

        if (compound.keySet().contains(TAG_COMPATABILITY_MANAGER))
        {
            compatibilityManager.read(compound.getCompound(TAG_COMPATABILITY_MANAGER));
        }

        recipeManager.read(compound.getCompound(RECIPE_MANAGER_TAG));
    }

    @Override
    public void onClientTick(@NotNull final TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END && Minecraft.getInstance().world == null && !colonyViews.isEmpty())
        {
            //  Player has left the game, clear the Colony View cache
            colonyViews.clear();
        }

        if (!compatibilityManager.isDiscoveredAlready() && ItemStackUtils.ISFOOD != null && FurnaceRecipes.getInstance().loaded())
        {
            compatibilityManager.discover(false);
        }
    }

    @Override
    public void onWorldTick(@NotNull final TickEvent.WorldTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            getColonies(event.world).forEach(c -> c.onWorldTick(event));
        }

        if (!compatibilityManager.isDiscoveredAlready() && FurnaceRecipes.getInstance().loaded())
        {
            compatibilityManager.discover(true);
        }
    }

    /**
     * When a world is loaded, Colonies in that world need to grab the reference
     * to the World. Additionally, when loading the first world, load the manager data.
     *
     * @param world World.
     */
    @Override
    public void onWorldLoad(@NotNull final World world)
    {
        if (!world.isRemote)
        {
            if (!loaded)
            {
                @NotNull final File file = BackUpHelper.getSaveLocation((ServerWorld) world);
                @Nullable final CompoundNBT data = BackUpHelper.loadNBTFromPath(file);
                if (data != null)
                {
                    read(data);
                }

                if (serverUUID == null)
                {
                    serverUUID = UUID.randomUUID();
                    Log.getLogger().info(String.format("New Server UUID %s", serverUUID));
                }
                else
                {
                    Log.getLogger().info(String.format("Server UUID %s", serverUUID));
                }
                loaded = true;
                BackUpHelper.loadMissingColonies();
            }

            for (@NotNull final IColony c : getColonies(world))
            {
                c.onWorldLoad(world);
            }
        }
    }

    /**
     * Get the Universal Unique ID for the server.
     *
     * @return the server Universal Unique ID for ther
     */
    @Override
    public UUID getServerUUID()
    {
        return serverUUID;
    }

    /**
     * Set the server UUID.
     *
     * @param uuid the universal unique id
     */
    @Override
    public void setServerUUID(final UUID uuid)
    {
        serverUUID = uuid;
    }

    /**
     * When a world unloads, all colonies in that world are informed.
     * Additionally, when the last world is unloaded, delete all colonies.
     *
     * @param world World.
     */
    @Override
    public void onWorldUnload(@NotNull final World world)
    {
        if (!world.isRemote && !(world instanceof ServerMultiWorld))
        {
            for (@NotNull final IColony c : getColonies(world))
            {
                c.onWorldUnload(world);
            }
            if (loaded)
            {
                BackUpHelper.backupColonyData();
                loaded = false;
            }
        }
    }

    /**
     * Sends view message to the right view.
     *
     * @param colonyId          ID of the colony.
     * @param colonyData        {@link PacketBuffer} with colony data.
     * @param isNewSubscription whether this is a new subscription or not.
     * @param dim               the dimension.
     */
    @Override
    public void handleColonyViewMessage(final int colonyId, @NotNull final PacketBuffer colonyData, @NotNull final World world, final boolean isNewSubscription, final int dim)
    {
        IColonyView view = getColonyView(colonyId, dim);
        if (view == null)
        {
            view = ColonyView.createFromNetwork(colonyId);
            if (colonyViews.containsKey(dim))
            {
                colonyViews.get(dim).add(view);
            }
            else
            {
                final ColonyList<IColonyView> list = new ColonyList<>();
                list.add(view);
                colonyViews.put(dim, list);
            }
        }
        view.handleColonyViewMessage(colonyData, world, isNewSubscription);
    }

    /**
     * Get ColonyView by ID.
     *
     * @param id        ID of colony.
     * @param dimension the dimension id.
     * @return The ColonyView belonging to the colony.
     */
    @Override
    public IColonyView getColonyView(final int id, final int dimension)
    {
        if (colonyViews.containsKey(dimension))
        {
            return colonyViews.get(dimension).get(id);
        }
        return null;
    }

    /**
     * Returns result of {@link ColonyView#handlePermissionsViewMessage(PacketBuffer)}
     * if {@link #getColonyView(int, int)}. gives a not-null result. If {@link
     * #getColonyView(int, int)} is null, returns null.
     *
     * @param colonyID ID of the colony.
     * @param data     {@link PacketBuffer} with colony data.
     * @param dim      the dimension.
     */
    @Override
    public void handlePermissionsViewMessage(final int colonyID, @NotNull final PacketBuffer data, final int dim)
    {
        final IColonyView view = getColonyView(colonyID, dim);
        if (view == null)
        {
            Log.getLogger().error(String.format("Colony view does not exist for ID #%d", colonyID), new Exception());
        }
        else
        {
            view.handlePermissionsViewMessage(data);
        }
    }

    /**
     * Returns result of {@link ColonyView#handleColonyViewCitizensMessage(int,
     * PacketBuffer)} if {@link #getColonyView(int, int)} gives a not-null result. If
     * {@link #getColonyView(int, int)} is null, returns null.
     *
     * @param colonyId  ID of the colony.
     * @param citizenId ID of the citizen.
     * @param buf       {@link PacketBuffer} with colony data.
     * @param dim       the dimension.
     */
    @Override
    public void handleColonyViewCitizensMessage(final int colonyId, final int citizenId, final PacketBuffer buf, final int dim)
    {
        final IColonyView view = getColonyView(colonyId, dim);
        if (view == null)
        {
            return;
        }
        view.handleColonyViewCitizensMessage(citizenId, buf);
    }

    /**
     * Returns result of {@link ColonyView#handleColonyViewWorkOrderMessage(PacketBuffer)}
     * (int, ByteBuf)} if {@link #getColonyView(int, int)} gives a not-null result.
     * If {@link #getColonyView(int, int)} is null, returns null.
     *
     * @param colonyId ID of the colony.
     * @param buf      {@link PacketBuffer} with colony data.
     * @param dim      the dimension.
     */
    @Override
    public void handleColonyViewWorkOrderMessage(final int colonyId, final PacketBuffer buf, final int dim)
    {
        final IColonyView view = getColonyView(colonyId, dim);
        if (view == null)
        {
            return;
        }
        view.handleColonyViewWorkOrderMessage(buf);
    }

    /**
     * Returns result of {@link ColonyView#handleColonyViewRemoveCitizenMessage(int)}
     * if {@link #getColonyView(int, int)} gives a not-null result. If {@link
     * #getColonyView(int, int)} is null, returns null.
     *
     * @param colonyId  ID of the colony.
     * @param citizenId ID of the citizen.
     * @param dim       the dimension.
     */
    @Override
    public void handleColonyViewRemoveCitizenMessage(final int colonyId, final int citizenId, final int dim)
    {
        final IColonyView view = getColonyView(colonyId, dim);
        if (view != null)
        {
            //  Can legitimately be NULL, because (to keep the code simple and fast), it is
            //  possible to receive a 'remove' notice before receiving the View.
            view.handleColonyViewRemoveCitizenMessage(citizenId);
        }
    }

    /**
     * Returns result of {@link ColonyView#handleColonyBuildingViewMessage(BlockPos,
     * PacketBuffer)} if {@link #getColonyView(int, int)} gives a not-null result. If
     * {@link #getColonyView(int, int)} is null, returns null.
     *
     * @param colonyId   ID of the colony.
     * @param buildingId ID of the building.
     * @param buf        {@link PacketBuffer} with colony data.
     * @param dim        the dimension.
     */
    @Override
    public void handleColonyBuildingViewMessage(final int colonyId, final BlockPos buildingId, @NotNull final PacketBuffer buf, final int dim)
    {
        final IColonyView view = getColonyView(colonyId, dim);
        if (view != null)
        {
            view.handleColonyBuildingViewMessage(buildingId, buf);
        }
        else
        {
            Log.getLogger().error(String.format("Colony view does not exist for ID #%d", colonyId), new Exception());
        }
    }

    /**
     * Returns result of {@link ColonyView#handleColonyViewRemoveBuildingMessage(BlockPos)}
     * if {@link #getColonyView(int, int)} gives a not-null result. If {@link
     * #getColonyView(int, int)} is null, returns null.
     *
     * @param colonyId   ID of the colony.
     * @param buildingId ID of the building.
     * @param dim        the dimension.
     */
    @Override
    public void handleColonyViewRemoveBuildingMessage(final int colonyId, final BlockPos buildingId, final int dim)
    {
        final IColonyView view = getColonyView(colonyId, dim);
        if (view != null)
        {
            //  Can legitimately be NULL, because (to keep the code simple and fast), it is
            //  possible to receive a 'remove' notice before receiving the View.
            view.handleColonyViewRemoveBuildingMessage(buildingId);
        }
    }

    /**
     * Returns result of {@link ColonyView#handleColonyViewRemoveWorkOrderMessage(int)}
     * if {@link #getColonyView(int, int)} gives a not-null result. If {@link
     * #getColonyView(int, int)} is null, returns null.
     *
     * @param colonyId    ID of the colony.
     * @param workOrderId ID of the workOrder.
     * @param dim         the dimension.
     */
    @Override
    public void handleColonyViewRemoveWorkOrderMessage(final int colonyId, final int workOrderId, final int dim)
    {
        final IColonyView view = getColonyView(colonyId, dim);
        if (view != null)
        {
            //  Can legitimately be NULL, because (to keep the code simple and fast), it is
            //  possible to receive a 'remove' notice before receiving the View.
            view.handleColonyViewRemoveWorkOrderMessage(workOrderId);
        }
    }

    /**
     * Whether or not a new schematic have been downloaded.
     *
     * @return True if a new schematic have been received.
     */
    @Override
    public boolean isSchematicDownloaded()
    {
        return schematicDownloaded;
    }

    /**
     * Set the schematic downloaded
     *
     * @param downloaded True if a new schematic have been received.
     */
    @Override
    public void setSchematicDownloaded(final boolean downloaded)
    {
        schematicDownloaded = downloaded;
    }

    /**
     * Check if a given coordinate is inside any other colony.
     *
     * @param world the world to check in.
     * @param pos   the position to check.
     * @return true if a colony has been found.
     */
    @Override
    public boolean isCoordinateInAnyColony(@NotNull final World world, final BlockPos pos)
    {
        final Chunk centralChunk = world.getChunkAt(pos);
        return centralChunk.getCapability(CLOSE_COLONY_CAP, null).orElseGet(null).getOwningColony() != 0;
    }

    /**
     * Get an instance of the compatibilityManager.
     *
     * @return the manager.
     */
    @Override
    public ICompatibilityManager getCompatibilityManager()
    {
        return compatibilityManager;
    }

    /**
     * Getter for the recipeManager.
     *
     * @return an IRecipeManager.
     */
    @Override
    public IRecipeManager getRecipeManager()
    {
        return recipeManager;
    }

    /**
     * Get the top colony id of all colonies.
     *
     * @return the top id.
     */
    @Override
    public int getTopColonyId()
    {
        int top = 0;
        for (final World world : ServerLifecycleHooks.getCurrentServer().getWorlds())
        {
            final IColonyManagerCapability cap = world.getCapability(COLONY_MANAGER_CAP, null).orElseGet(null);
            if (cap != null)
            {
                final int tempTop = cap.getTopID();
                if (tempTop > top)
                {
                    top = tempTop;
                }
            }
        }
        return top;
    }

    @Override
    public void resetColonyViews()
    {
        colonyViews.clear();
    }
}
