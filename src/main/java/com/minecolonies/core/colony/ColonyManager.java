package com.minecolonies.core.colony;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.claims.ClaimInfo;
import com.minecolonies.api.colony.claims.ClaimReason;
import com.minecolonies.api.colony.claims.UnclaimReason;
import com.minecolonies.api.colony.permissions.ColonyPlayer;
import com.minecolonies.api.compatibility.CompatibilityManager;
import com.minecolonies.api.compatibility.ICompatibilityManager;
import com.minecolonies.api.crafting.IRecipeManager;
import com.minecolonies.api.eventbus.events.ColonyManagerLoadedModEvent;
import com.minecolonies.api.eventbus.events.ColonyManagerUnloadedModEvent;
import com.minecolonies.api.eventbus.events.colony.ColonyDeletedModEvent;
import com.minecolonies.api.eventbus.events.colony.ColonyViewUpdatedModEvent;
import com.minecolonies.api.sounds.SoundManager;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.DamageSourceKeys;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.WindowReactivateBuilding;
import com.minecolonies.core.colony.requestsystem.management.manager.StandardRecipeManager;
import com.minecolonies.core.network.messages.client.colony.ColonyViewRemoveMessage;
import com.minecolonies.core.util.BackUpHelper;
import com.minecolonies.core.util.ChunkDataHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.api.util.constant.ColonyManagerConstants.*;
import static com.minecolonies.api.util.constant.Constants.BLOCKS_PER_CHUNK;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_COMPATABILITY_MANAGER;
import static com.minecolonies.core.MineColonies.COLONY_MANAGER_CAP;
import static com.minecolonies.core.MineColonies.getConfig;

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
    private final Map<ResourceKey<Level>, ColonyList<IColonyView>> colonyViews = new HashMap<>();

    /**
     * Client-side cache of a chunk -> the colony view claiming it, one per dimension, so windows/screens that ask "who owns
     * this chunk" repeatedly (e.g. every frame) don't have to rescan every known colony view each time. If a chunk isn't in
     * the cache yet, we look it up and store the result the first time it's asked for.
     * {@link #invalidateOwningColonyView(ResourceKey, long)} clears the entry for a chunk whenever a colony view's
     * claims actually change, so this can never go stale in a way that matters.
     * <p>
     * The value is wrapped in {@code Optional} only because Guava's cache doesn't allow storing null (an unclaimed chunk
     * would otherwise have nothing to store). This is purely an internal detail of the cache and never shows up outside
     * this class.
     */
    @NotNull
    private final Map<ResourceKey<Level>, LoadingCache<Long, Optional<IColonyView>>> owningColonyViewCaches = new HashMap<>();

    /**
     * Recipemanager of this server.
     */
    private final IRecipeManager recipeManager = new StandardRecipeManager();

    /**
     * Creates a new compatibilityManager.
     */
    private final ICompatibilityManager compatibilityManager = new CompatibilityManager();

    /**
     * Indicate if a schematic have just been downloaded. Client only
     */
    private boolean schematicDownloaded = false;

    /**
     * If the manager finished loading already.
     */
    private boolean capLoaded = false;

    /**
     * Client side sound manager.
     */
    private SoundManager clientSoundManager;

    @Override
    public IColony createColony(@NotNull final Level w, final BlockPos pos, @NotNull final Player player, @NotNull final String colonyName, @NotNull final String pack)
    {
        final IColonyManagerCapability cap = w.getCapability(COLONY_MANAGER_CAP, null).resolve().orElse(null);
        if (cap == null)
        {
            Log.getLogger().warn(MISSING_WORLD_CAP_MESSAGE);
            return null;
        }

        final IColony colony = cap.createColony(w, pos);
        colony.setStructurePack(pack);

        colony.setName(colonyName);
        colony.getPermissions().setOwner(player);

        colony.getPackageManager().addImportantColonyPlayer((ServerPlayer) player);
        colony.getPackageManager().addCloseSubscriber((ServerPlayer) player);

        Log.getLogger().info(String.format("New Colony Id: %d by %s", colony.getID(), player.getName().getString()));

        if (colony.getWorld() == null)
        {
            Log.getLogger().error("Newly created colony has no world set, please report this to the mod authors!", new Exception());
            return null;
        }

        return colony;
    }

    @Override
    public void deleteColonyByWorld(final int id, final boolean canDestroy, final Level world)
    {
        deleteColony(getColonyByWorld(id, world), canDestroy);
    }

    @Override
    public void deleteColonyByDimension(final int id, final boolean canDestroy, final ResourceKey<Level> dimension)
    {
        deleteColony(getColonyByDimension(id, dimension), canDestroy);
    }

    /**
     * Delete a colony and purge all buildings and citizens.
     *
     * @param iColony    the colony to destroy.
     * @param canDestroy if the building outlines should be destroyed as well.
     */
    private void deleteColony(@Nullable final IColony iColony, final boolean canDestroy)
    {
        if (!(iColony instanceof Colony))
        {
            return;
        }

        final Colony colony = (Colony) iColony;
        final int id = colony.getID();
        final Level world = colony.getWorld();

        if (world == null)
        {
            Log.getLogger().warn("Deleting Colony " + id + " errored: World is Null");
            return;
        }

        try
        {
            Log.getLogger().info("Removing citizens for " + id);
            for (final ICitizenData citizenData : new ArrayList<>(colony.getCitizenManager().getCitizens()))
            {
                Log.getLogger().info("Kill Citizen " + citizenData.getName());
                citizenData.getEntity().ifPresent(entityCitizen -> entityCitizen.die(world.damageSources().source(DamageSourceKeys.CONSOLE)));
            }

            Log.getLogger().info("Removing buildings for " + id);
            for (final IBuilding building : new ArrayList<>(colony.getServerBuildingManager().getBuildings().values()))
            {
                try
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
                catch (final Exception ex)
                {
                    Log.getLogger().warn("Something went wrong deleting a building while deleting the colony!", ex);
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

            final IColonyManagerCapability cap = world.getCapability(COLONY_MANAGER_CAP, null).resolve().orElse(null);
            if (cap == null)
            {
                Log.getLogger().warn(MISSING_WORLD_CAP_MESSAGE);
                return;
            }

            IMinecoloniesAPI.getInstance().getEventBus().post(new ColonyDeletedModEvent(colony));
            cap.deleteColony(id);
            BackUpHelper.markColonyDeleted(colony.getID(), colony.getDimension());
            colony.getImportantMessageEntityPlayers()
              .forEach(player -> Network.getNetwork().sendToPlayer(new ColonyViewRemoveMessage(colony.getID(), colony.getDimension()), (ServerPlayer) player));
            Log.getLogger().info("Successfully deleted colony: " + id);
        }
        catch (final RuntimeException e)
        {
            Log.getLogger().warn("Deleting Colony " + id + " errored:", e);
        }
    }

    @Override
    public void removeColonyView(final int id, final ResourceKey<Level> dimension)
    {
        if (colonyViews.containsKey(dimension))
        {
            final ColonyList<IColonyView> colonies = colonyViews.get(dimension);
            final IColonyView removed = colonies.get(id);
            colonies.remove(id);
            if (removed != null)
            {
                for (final long chunkPos : removed.getClaimedChunks())
                {
                    invalidateOwningColonyView(dimension, chunkPos);
                }
            }
        }
    }

    @Override
    @Nullable
    public IColony getColonyByWorld(final int id, final Level world)
    {
        final IColonyManagerCapability cap = world.getCapability(COLONY_MANAGER_CAP, null).resolve().orElse(null);
        if (cap == null)
        {
            Log.getLogger().warn(MISSING_WORLD_CAP_MESSAGE);
            return null;
        }
        return cap.getColony(id);
    }

    @Override
    @Nullable
    public IColony getColonyByDimension(final int id, final ResourceKey<Level> registryKey)
    {
        final Level world = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer().getLevel(registryKey);
        if (world == null)
        {
            return null;
        }
        final IColonyManagerCapability cap = world.getCapability(COLONY_MANAGER_CAP, null).resolve().orElse(null);
        if (cap == null)
        {
            Log.getLogger().warn(MISSING_WORLD_CAP_MESSAGE);
            return null;
        }
        return cap.getColony(id);
    }

    @Override
    public IBuilding getBuilding(@NotNull final Level w, @NotNull final BlockPos pos)
    {
        @Nullable final IColony colony = getColonyByPosFromWorld(w, pos);
        if (colony != null)
        {
            final IBuilding building = colony.getServerBuildingManager().getBuilding(pos);
            if (building != null)
            {
                return building;
            }
        }

        //  Fallback - there might be a AbstractBuilding for this block, but it's outside of it's owning colony's radius.
        for (@NotNull final IColony otherColony : getColonies(w))
        {
            final IBuilding building = otherColony.getServerBuildingManager().getBuilding(pos);
            if (building != null)
            {
                return building;
            }
        }

        return null;
    }

    @Override
    public IColony getColonyByPosFromWorld(@Nullable final Level w, @NotNull final BlockPos pos)
    {
        if (w == null)
        {
            return null;
        }
        final LevelChunk centralChunk = w.getChunkAt(pos);
        final IColony colony = getOwningColony(w, centralChunk);
        if (colony == null)
        {
            return null;
        }
        return getColonyByWorld(colony.getID(), w);
    }

    @Override
    public IColony getColonyByPosFromDim(final ResourceKey<Level> registryKey, @NotNull final BlockPos pos)
    {
        return getColonyByPosFromWorld(ServerLifecycleHooks.getCurrentServer().getLevel(registryKey), pos);
    }

    @Override
    public boolean isFarEnoughFromColonies(@NotNull final Level w, @NotNull final BlockPos pos)
    {
        final int blockRange = Math.max(MineColonies.getConfig().getServer().minColonyDistance.get(), getConfig().getServer().initialColonySize.get()) << 4;
        final IColony closest = getClosestColony(w, pos);

        if (closest != null && BlockPosUtil.getDistance(pos, closest.getCenter()) < blockRange)
        {
            return false;
        }

        return ChunkDataHelper.canClaimChunksInRange(w,
          pos,
          getConfig().getServer().initialColonySize.get());
    }

    @Override
    @NotNull
    public List<IColony> getColonies(@NotNull final Level w)
    {
        final IColonyManagerCapability cap = w.getCapability(COLONY_MANAGER_CAP, null).resolve().orElse(null);
        if (cap == null)
        {
            Log.getLogger().warn(MISSING_WORLD_CAP_MESSAGE);
            return Collections.emptyList();
        }
        return cap.getColonies();
    }

    @Nullable
    @Override
    public IColony getOwningColony(@NotNull final Level world, final long chunkPos)
    {
        if (world.isClientSide())
        {
            return owningColonyViewCache(world.dimension()).getUnchecked(chunkPos).orElse(null);
        }

        final IColonyManagerCapability cap = world.getCapability(COLONY_MANAGER_CAP, null).resolve().orElse(null);
        if (cap == null)
        {
            Log.getLogger().warn(MISSING_WORLD_CAP_MESSAGE);
            return null;
        }
        return cap.getOwningColony(chunkPos);
    }

    /**
     * Gets (creating if needed) the owning-colony-view cache for a dimension.
     *
     * @param dimension the dimension.
     * @return the cache.
     */
    @NotNull
    private LoadingCache<Long, Optional<IColonyView>> owningColonyViewCache(@NotNull final ResourceKey<Level> dimension)
    {
        return owningColonyViewCaches.computeIfAbsent(dimension,
            k -> CacheBuilder.newBuilder().build(CacheLoader.from(chunkPos -> findOwningColonyView(dimension, chunkPos))));
    }

    /**
     * Scans every colony view known in a dimension for one claiming the given chunk. Only called by the cache when it doesn't
     * already have an answer for this chunk.
     *
     * @param dimension the dimension.
     * @param chunkPos  the chunk position, as {@code ChunkPos.asLong(x, z)}.
     * @return the owning colony view, if any.
     */
    @NotNull
    private Optional<IColonyView> findOwningColonyView(@NotNull final ResourceKey<Level> dimension, final long chunkPos)
    {
        final ColonyList<IColonyView> colonies = colonyViews.get(dimension);
        if (colonies == null)
        {
            return Optional.empty();
        }

        for (final IColonyView colony : colonies.getCopyAsList())
        {
            if (colony.getClaimedChunks().contains(chunkPos))
            {
                return Optional.of(colony);
            }
        }
        return Optional.empty();
    }

    /**
     * Clears the cached owning-colony-view answer for a chunk, in a dimension. Must be called whenever a colony view's claimed
     * chunks change, so the cache never returns a stale answer for that chunk.
     *
     * @param dimension the dimension.
     * @param chunkPos  the chunk position, as {@code ChunkPos.asLong(x, z)}.
     */
    void invalidateOwningColonyView(@NotNull final ResourceKey<Level> dimension, final long chunkPos)
    {
        final LoadingCache<Long, Optional<IColonyView>> cache = owningColonyViewCaches.get(dimension);
        if (cache != null)
        {
            cache.invalidate(chunkPos);
        }
    }

    @Override
    public boolean tryClaimChunkForColony(@NotNull final Level world, final long chunkPos, @NotNull final IColony requester, @NotNull final ClaimReason reason)
    {
        final IColonyManagerCapability cap = world.getCapability(COLONY_MANAGER_CAP, null).resolve().orElse(null);
        if (cap == null)
        {
            Log.getLogger().warn(MISSING_WORLD_CAP_MESSAGE);
            return false;
        }
        return cap.tryClaimChunk(chunkPos, requester.getID(), reason);
    }

    @Override
    public void unclaimChunkForColony(@NotNull final Level world, final long chunkPos, @NotNull final IColony owner, @NotNull final UnclaimReason reason)
    {
        final IColonyManagerCapability cap = world.getCapability(COLONY_MANAGER_CAP, null).resolve().orElse(null);
        if (cap == null)
        {
            Log.getLogger().warn(MISSING_WORLD_CAP_MESSAGE);
            return;
        }
        cap.unclaimChunk(chunkPos, owner.getID(), reason);
    }

    @NotNull
    @Override
    public Set<Long> getClaimedChunks(@NotNull final Level world, @NotNull final IColony colony)
    {
        final IColonyManagerCapability cap = world.getCapability(COLONY_MANAGER_CAP, null).resolve().orElse(null);
        if (cap == null)
        {
            Log.getLogger().warn(MISSING_WORLD_CAP_MESSAGE);
            return Collections.emptySet();
        }
        return cap.getClaimedChunks(colony.getID());
    }

    @Nullable
    @Override
    public ClaimInfo getClaimInfo(@NotNull final Level world, final long chunkPos, @NotNull final IColony colony)
    {
        final IColonyManagerCapability cap = world.getCapability(COLONY_MANAGER_CAP, null).resolve().orElse(null);
        if (cap == null)
        {
            Log.getLogger().warn(MISSING_WORLD_CAP_MESSAGE);
            return null;
        }
        return cap.getClaimInfo(chunkPos, colony.getID());
    }

    @Override
    @NotNull
    public List<IColony> getAllColonies()
    {
        final List<IColony> allColonies = new ArrayList<>();
        for (final Level world : net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer().getAllLevels())
        {
            world.getCapability(COLONY_MANAGER_CAP, null).ifPresent(c -> allColonies.addAll(c.getColonies()));
        }
        return allColonies;
    }

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

    @Override
    public IBuildingView getBuildingView(final ResourceKey<Level> dimension, final BlockPos pos)
    {
        if (colonyViews.containsKey(dimension))
        {
            //  On client we will just check all known views
            for (@NotNull final IColonyView colony : colonyViews.get(dimension))
            {
                final IBuildingView building = colony.getClientBuildingManager().getBuilding(pos);
                if (building != null)
                {
                    return building;
                }
            }
        }

        return null;
    }

    @Override
    @NotNull
    public List<IColony> getIColonies(@NotNull final Level w)
    {
        return w.isClientSide() ? new ArrayList<>(getColonyViews(w)) : getColonies(w);
    }

    @Override
    @Nullable
    public IColony getIColony(@NotNull final Level w, @NotNull final BlockPos pos)
    {
        return w.isClientSide ? getColonyView(w, pos) : getColonyByPosFromWorld(w, pos);
    }

    @Override
    public void openReactivationWindow(final BlockPos pos)
    {
        new WindowReactivateBuilding(pos).open();
    }

    @Override
    @NotNull
    public List<IColonyView> getColonyViews(@NotNull final Level w)
    {
        // this might be a subset of colonies since it's only those known to the player right now
        final ColonyList<IColonyView> colonies = colonyViews.get(w.dimension());
        return colonies == null ? List.of() : new ArrayList<>(colonies.getCopyAsList());
    }

    /**
     * Get Colony that contains a given (x, y, z).
     *
     * @param w   World.
     * @param pos coordinates.
     * @return returns the view belonging to the colony at x, y, z.
     */
    @Override
    public IColonyView getColonyView(@NotNull final Level w, @NotNull final BlockPos pos)
    {
        final LevelChunk centralChunk = w.getChunkAt(pos);

        final IColony colony = getOwningColony(w, centralChunk);
        if (colony == null)
        {
            return null;
        }
        return getColonyView(colony.getID(), w.dimension());
    }

    @Override
    @Nullable
    public IColony getClosestIColony(@NotNull final Level w, @NotNull final BlockPos pos)
    {
        return w.isClientSide ? getClosestColonyView(w, pos) : getClosestColony(w, pos);
    }

    @Override
    @Nullable
    public IColonyView getClosestColonyView(@Nullable final Level w, @Nullable final BlockPos pos)
    {
        if (w == null || pos == null)
        {
            return null;
        }

        final LevelChunk chunk = w.getChunkAt(pos);
        final IColony owningColony = getOwningColony(w, chunk);
        if (owningColony != null)
        {
            return getColonyView(owningColony.getID(), w.dimension());
        }

        @Nullable IColonyView closestColony = null;
        long closestDist = Long.MAX_VALUE;

        if (colonyViews.containsKey(w.dimension()))
        {
            for (@NotNull final IColonyView c : colonyViews.get(w.dimension()))
            {
                if (c.getDimension() == w.dimension() && c.getCenter() != null)
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

    @Override
    public IColony getClosestColony(@NotNull final Level w, @NotNull final BlockPos pos)
    {
        final LevelChunk chunk = w.getChunkAt(pos);
        final IColony owningColony = getOwningColony(w, chunk);
        if (owningColony != null)
        {
            return getColonyByWorld(owningColony.getID(), w);
        }

        @Nullable IColony closestColony = null;
        long closestDist = Long.MAX_VALUE;

        for (@NotNull final IColony c : getColonies(w))
        {
            if (c.getDimension() == w.dimension())
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
    public IColony getIColonyByOwner(@NotNull final Level w, @NotNull final Player owner)
    {
        return getIColonyByOwner(w, w.isClientSide ? owner.getUUID() : owner.getGameProfile().getId());
    }

    @Override
    @Nullable
    public IColony getIColonyByOwner(@NotNull final Level w, final UUID owner)
    {
        return w.isClientSide ? getColonyViewByOwner(owner, w.dimension()) : getColonyByOwner(owner);
    }

    /**
     * Returns a ColonyView with specific owner.
     *
     * @param owner     UUID of the owner.
     * @param dimension the dimension id.
     * @return ColonyView.
     */
    private IColony getColonyViewByOwner(final UUID owner, final ResourceKey<Level> dimension)
    {
        if (colonyViews.containsKey(dimension))
        {
            for (@NotNull final IColonyView c : colonyViews.get(dimension))
            {
                final ColonyPlayer p = c.getPlayers().get(owner);
                if (p != null && p.getRank().equals(c.getPermissions().getRankOwner()))
                {
                    return c;
                }
            }
        }

        return null;
    }

    @Nullable
    private IColony getColonyByOwner(@Nullable final UUID owner)
    {
        if (owner == null)
        {
            return null;
        }

        for (final IColony colony : getAllColonies())
        {
            if (colony.getPermissions().getOwner().equals(owner))
            {
                return colony;
            }
        }

        return null;
    }

    @Override
    public int getMinimumDistanceBetweenTownHalls()
    {
        //  [TownHall](Radius)+(Padding)+(Radius)[TownHall]
        return getConfig().getServer().minColonyDistance.get() * BLOCKS_PER_CHUNK;
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

    @Override
    public void write(@NotNull final CompoundTag compound)
    {
        //Get the colonies NBT tags and store them in a ListNBT.
        final CompoundTag compCompound = new CompoundTag();
        compatibilityManager.write(compCompound);
        compound.put(TAG_COMPATABILITY_MANAGER, compCompound);

        compound.putBoolean(TAG_DISTANCE, true);
        final CompoundTag recipeCompound = new CompoundTag();
        recipeManager.write(recipeCompound);

        compound.put(RECIPE_MANAGER_TAG, recipeCompound);
    }

    // File read for compat/recipe
    @Override
    public void read(@NotNull final CompoundTag compound)
    {
        if (compound.contains(TAG_COMPATABILITY_MANAGER))
        {
            compatibilityManager.read(compound.getCompound(TAG_COMPATABILITY_MANAGER));
        }

        recipeManager.read(compound.getCompound(RECIPE_MANAGER_TAG));
    }

    @Override
    public void onClientTick(@NotNull final TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            if (Minecraft.getInstance().level == null && !colonyViews.isEmpty())
            {
                //  Player has left the game, clear the Colony View cache
                colonyViews.clear();
                owningColonyViewCaches.clear();
            }


            if (clientSoundManager == null)
            {
                clientSoundManager = new SoundManager();
            }
            clientSoundManager.tick();
        }
    }

    @Override
    public void onWorldTick(final TickEvent.@NotNull LevelTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            for (final IColony colony : getColonies(event.level))
            {
                try
                {
                    colony.onWorldTick(event);
                }
                catch (final Exception ex)
                {
                    Log.getLogger().error("Something went wrong ticking colony: " + colony.getID(), ex);
                }
            }
        }
    }

    @Override
    public void onWorldLoad(@NotNull final Level world)
    {
        if (!world.isClientSide)
        {
            // Late-load restore if cap was not loaded
            if (!capLoaded)
            {
                BackUpHelper.loadMissingColonies();
                BackUpHelper.loadManagerBackup();
            }
            capLoaded = false;

            final IColonyManagerCapability cap = world.getCapability(COLONY_MANAGER_CAP, null).resolve().orElse(null);
            for (@NotNull final IColony c : getColonies(world))
            {
                c.onWorldLoad(world);

                // TODO: Remove on next version
                // A colony with no claims at all can only be a pre-refactor save that predates claim data existing (or,
                // in principle, a colony that somehow lost all its claims outright). Its world couldn't be set yet back
                // when claims were loaded from NBT, so the rebuild had to wait until now.
                if (cap != null && cap.getClaimedChunks(c.getID()).isEmpty())
                {
                    cap.reclaimChunks(c);
                    Log.getLogger().info("Data migration: Reclaiming chunks for colony {}", c.getID());
                }
            }

            IMinecoloniesAPI.getInstance().getEventBus().post(new ColonyManagerLoadedModEvent(this));
        }
    }

    @Override
    public void setCapLoaded()
    {
        this.capLoaded = true;
    }

    @Override
    public void onWorldUnload(@NotNull final Level world)
    {
        if (!world.isClientSide)
        {
            boolean hasColonies = false;
            for (@NotNull final IColony c : getColonies(world))
            {
                hasColonies = true;
                c.onWorldUnload(world);
            }

            if (hasColonies)
            {
                BackUpHelper.backupColonyData();
            }

            IMinecoloniesAPI.getInstance().getEventBus().post(new ColonyManagerUnloadedModEvent(this));
        }
    }

    @Override
    public void handleColonyViewMessage(
      final int colonyId,
      @NotNull final FriendlyByteBuf colonyData,
      @NotNull final Level world,
      final boolean isNewSubscription,
      final ResourceKey<Level> dim)
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

        IMinecoloniesAPI.getInstance().getEventBus().post(new ColonyViewUpdatedModEvent(view));
    }

    @Override
    public IColonyView getColonyView(final int id, final ResourceKey<Level> dimension)
    {
        if (colonyViews.containsKey(dimension))
        {
            return colonyViews.get(dimension).get(id);
        }
        return null;
    }

    @Override
    public void handlePermissionsViewMessage(final int colonyID, @NotNull final FriendlyByteBuf data, final ResourceKey<Level> dim)
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

    @Override
    public void handleColonyViewCitizensMessage(final int colonyId, final int citizenId, final FriendlyByteBuf buf, final ResourceKey<Level> dim)
    {
        final IColonyView view = getColonyView(colonyId, dim);
        if (view == null)
        {
            return;
        }
        view.handleColonyViewCitizensMessage(citizenId, buf);
    }

    @Override
    public void handleColonyViewWorkOrderMessage(final int colonyId, final FriendlyByteBuf buf, final ResourceKey<Level> dim)
    {
        final IColonyView view = getColonyView(colonyId, dim);
        if (view == null)
        {
            return;
        }
        view.handleColonyViewWorkOrderMessage(buf);
    }

    @Override
    public void handleColonyViewRemoveCitizenMessage(final int colonyId, final int citizenId, final ResourceKey<Level> dim)
    {
        final IColonyView view = getColonyView(colonyId, dim);
        if (view != null)
        {
            //  Can legitimately be NULL, because (to keep the code simple and fast), it is
            //  possible to receive a 'remove' notice before receiving the View.
            view.handleColonyViewRemoveCitizenMessage(citizenId);
        }
    }

    @Override
    public void handleColonyBuildingViewMessage(final int colonyId, final BlockPos buildingId, @NotNull final FriendlyByteBuf buf, final ResourceKey<Level> dim)
    {
        final IColonyView view = getColonyView(colonyId, dim);
        if (view != null)
        {
            view.getClientBuildingManager().handleColonyBuildingViewMessage(buildingId, buf);
        }
        else
        {
            Log.getLogger().error(String.format("Colony view does not exist for ID #%d", colonyId), new Exception());
        }
    }

    @Override
    public void handleColonyViewRemoveBuildingMessage(final int colonyId, final BlockPos buildingId, final ResourceKey<Level> dim)
    {
        final IColonyView view = getColonyView(colonyId, dim);
        if (view != null)
        {
            //  Can legitimately be NULL, because (to keep the code simple and fast), it is
            //  possible to receive a 'remove' notice before receiving the View.
            view.getClientBuildingManager().handleColonyViewRemoveBuildingMessage(buildingId);
        }
    }

    @Override
    public void handleColonyViewRemoveWorkOrderMessage(final int colonyId, final int workOrderId, final ResourceKey<Level> dim)
    {
        final IColonyView view = getColonyView(colonyId, dim);
        if (view != null)
        {
            //  Can legitimately be NULL, because (to keep the code simple and fast), it is
            //  possible to receive a 'remove' notice before receiving the View.
            view.handleColonyViewRemoveWorkOrderMessage(workOrderId);
        }
    }

    @Override
    public boolean isSchematicDownloaded()
    {
        return schematicDownloaded;
    }

    @Override
    public void setSchematicDownloaded(final boolean downloaded)
    {
        schematicDownloaded = downloaded;
    }

    @Override
    public boolean isCoordinateInAnyColony(@NotNull final Level world, final BlockPos pos)
    {
        final LevelChunk centralChunk = world.getChunkAt(pos);
        return getOwningColony(world, centralChunk) != null;
    }

    @Override
    public ICompatibilityManager getCompatibilityManager()
    {
        return compatibilityManager;
    }

    @Override
    public IRecipeManager getRecipeManager()
    {
        return recipeManager;
    }

    @Override
    public int getTopColonyId()
    {
        int top = 0;
        for (final Level world : ServerLifecycleHooks.getCurrentServer().getAllLevels())
        {
            final int tempTop = world.getCapability(COLONY_MANAGER_CAP, null).map(IColonyManagerCapability::getTopID).orElse(0);
            if (tempTop > top)
            {
                top = tempTop;
            }
        }
        return top;
    }

    @Override
    public void resetColonyViews()
    {
        colonyViews.clear();
        owningColonyViewCaches.clear();
    }
}
