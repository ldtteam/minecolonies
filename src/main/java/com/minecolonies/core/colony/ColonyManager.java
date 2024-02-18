package com.minecolonies.core.colony;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.claim.ChunkClaimData;
import com.minecolonies.api.colony.claim.IChunkClaimData;
import com.minecolonies.api.colony.savedata.IServerColonySaveData;
import com.minecolonies.api.colony.event.ColonyViewUpdatedEvent;
import com.minecolonies.api.colony.managers.events.ColonyManagerLoadedEvent;
import com.minecolonies.api.colony.managers.events.ColonyManagerUnloadedEvent;
import com.minecolonies.api.colony.permissions.ColonyPlayer;
import com.minecolonies.api.compatibility.CompatibilityManager;
import com.minecolonies.api.compatibility.ICompatibilityManager;
import com.minecolonies.api.crafting.IRecipeManager;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ColonyUtils;
import com.minecolonies.api.util.DamageSourceKeys;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.client.gui.WindowReactivateBuilding;
import com.minecolonies.core.colony.requestsystem.management.manager.StandardRecipeManager;
import com.minecolonies.core.network.messages.client.colony.ColonyViewRemoveMessage;
import com.minecolonies.core.util.BackUpHelper;
import com.minecolonies.core.util.ChunkDataHelper;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.api.util.constant.ColonyManagerConstants.*;
import static com.minecolonies.api.util.constant.Constants.BLOCKS_PER_CHUNK;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_COMPATABILITY_MANAGER;
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
     * Global claim data from all colonies
     */
    private Map<ResourceKey<Level>, Long2ObjectMap<ChunkClaimData>> chunkClaimData = new HashMap<>();

    @Nullable
    private IServerColonySaveData getColonySaveData(final ServerLevel w)
    {
       return IServerColonySaveData.getSaveData(w);
    }

    @Override
    public IColony createColony(@NotNull final ServerLevel w, final BlockPos pos, @NotNull final Player player, @NotNull final String colonyName, @NotNull final String pack)
    {
        final IServerColonySaveData cap = getColonySaveData(w);
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
            Log.getLogger().error("Unable to claim chunks because of the missing world in the colony, please report this to the mod authors!", new Exception());
            return null;
        }

        ChunkDataHelper.claimColonyChunks(w, true, (Colony) colony, colony.getCenter());
        return colony;
    }

    @Override
    public void deleteColonyByWorld(final int id, final boolean canDestroy, final ServerLevel world)
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
        if (!(iColony instanceof final Colony colony))
        {
            return;
        }

        final int id = colony.getID();
        final ServerLevel world = colony.getWorld();

        if (world == null)
        {
            Log.getLogger().warn("Deleting Colony " + id + " errored: World is Null");
            return;
        }

        try
        {
            ChunkDataHelper.claimColonyChunks(world, false, colony, colony.getCenter());
            Log.getLogger().info("Removing citizens for " + id);
            for (final ICitizenData citizenData : new ArrayList<>(colony.getCitizenManager().getCitizens()))
            {
                Log.getLogger().info("Kill Citizen " + citizenData.getName());
                citizenData.getEntity().ifPresent(entityCitizen -> entityCitizen.die(world.damageSources().source(DamageSourceKeys.CONSOLE)));
            }

            Log.getLogger().info("Removing buildings for " + id);
            for (final IBuilding building : new ArrayList<>(colony.getBuildingManager().getBuildings().values()))
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
                NeoForge.EVENT_BUS.unregister(colony.getEventHandler());
            }
            catch (final NullPointerException e)
            {
                Log.getLogger().warn("Can't unregister the event handler twice");
            }

            Log.getLogger().info("Deleting colony: " + colony.getID());

            final IServerColonySaveData cap = getColonySaveData(world);
            if (cap == null)
            {
                Log.getLogger().warn(MISSING_WORLD_CAP_MESSAGE);
                return;
            }

            cap.deleteColony(id);
            BackUpHelper.markColonyDeleted(colony.getID(), colony.getDimension());
            colony.getImportantMessageEntityPlayers()
              .forEach(player -> new ColonyViewRemoveMessage(colony.getID(), colony.getDimension()).sendToPlayer((ServerPlayer) player));
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
            colonyViews.get(dimension).remove(id);
        }
    }

    @Override
    @Nullable
    public IColony getColonyByWorld(final int id, final Level world)
    {
        if (!(world instanceof final ServerLevel serverLevel))
        {
            return getColonyView(id, world.dimension());
        }
        final IServerColonySaveData cap = getColonySaveData(serverLevel);
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
        final ServerLevel world = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer().getLevel(registryKey);
        if (world == null)
        {
            return null;
        }
        final IServerColonySaveData cap = getColonySaveData(world);
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
            final IBuilding building = colony.getBuildingManager().getBuilding(pos);
            if (building != null)
            {
                return building;
            }
        }

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

    @Override
    public IColony getColonyByPosFromWorld(@Nullable final Level w, @NotNull final BlockPos pos)
    {
        if (w == null)
        {
            return null;
        }
        final LevelChunk centralChunk = w.getChunkAt(pos);
        final int id = ColonyUtils.getOwningColony(centralChunk);
        if (id == NO_COLONY_ID)
        {
            return null;
        }
        return getColonyByWorld(id, w);
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

        if (w.isClientSide())
        {
            return true;
        }

        return ChunkDataHelper.canClaimChunksInRange((ServerLevel) w, pos, getConfig().getServer().initialColonySize.get());
    }

    @Override
    @NotNull
    public List<IColony> getColonies(@NotNull final Level w)
    {
        if (!(w instanceof final ServerLevel serverLevel))
        {
            final ColonyList<IColonyView> list = colonyViews.get(w.dimension());
            return list == null || list.isEmpty() ? List.of() : list.getCopyAsList();
        }
        final IServerColonySaveData cap = getColonySaveData(serverLevel);
        if (cap == null)
        {
            Log.getLogger().warn(MISSING_WORLD_CAP_MESSAGE);
            return Collections.emptyList();
        }
        return cap.getColonies();
    }

    @Override
    @NotNull
    public List<IColony> getAllColonies()
    {
        final List<IColony> allColonies = new ArrayList<>();
        for (final ServerLevel world : net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer().getAllLevels())
        {
            final IServerColonySaveData cap = getColonySaveData(world);
            if (cap != null)
            {
                allColonies.addAll(cap.getColonies());
            }
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
                final IBuildingView building = colony.getBuilding(pos);
                if (building != null)
                {
                    return building;
                }
            }
        }

        return null;
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

    /**
     * Get Colony that contains a given (x, y, z).
     *
     * @param w   World.
     * @param pos coordinates.
     * @return returns the view belonging to the colony at x, y, z.
     */
    private IColonyView getColonyView(@NotNull final Level w, @NotNull final BlockPos pos)
    {
        final LevelChunk centralChunk = w.getChunkAt(pos);

        final int id = ColonyUtils.getOwningColony(centralChunk);
        if (id == 0)
        {
            return null;
        }
        return getColonyView(id, w.dimension());
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
        final int owningColony = ColonyUtils.getOwningColony(chunk);
        if (owningColony != NO_COLONY_ID)
        {
            return getColonyView(owningColony, w.dimension());
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
        final int owningColony = ColonyUtils.getOwningColony(chunk);
        if (owningColony != NO_COLONY_ID)
        {
            return getColonyByWorld(owningColony, w);
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
        if (event.phase == TickEvent.Phase.END && Minecraft.getInstance().level == null && !colonyViews.isEmpty())
        {
            //  Player has left the game, clear the Colony View cache
            colonyViews.clear();
        }
    }

    @Override
    public void onWorldTick(final TickEvent.@NotNull LevelTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END && !event.level.isClientSide)
        {
            getColonies(event.level).forEach(c -> c.onWorldTick(event));
        }
    }

    @Override
    public void onWorldLoad(@NotNull final Level w)
    {
        if (w instanceof final ServerLevel world)
        {
            for (@NotNull final IColony c : getColonies(world))
            {
                c.onWorldLoad(world);
            }

            NeoForge.EVENT_BUS.post(new ColonyManagerLoadedEvent(this));
        }
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

            NeoForge.EVENT_BUS.post(new ColonyManagerUnloadedEvent(this));
        }
    }

    @Override
    public void handleColonyViewMessage(
      final int colonyId,
      @NotNull final FriendlyByteBuf colonyData,
      final boolean isNewSubscription,
      final ResourceKey<Level> dim)
    {
        IColonyView view = getColonyView(colonyId, dim);
        if (view == null)
        {
            view = ColonyView.createFromNetwork(colonyId, dim);
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
        view.handleColonyViewMessage(colonyData, isNewSubscription);

        NeoForge.EVENT_BUS.post(new ColonyViewUpdatedEvent(view));
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
            view.handleColonyBuildingViewMessage(buildingId, buf);
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
            view.handleColonyViewRemoveBuildingMessage(buildingId);
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
        return ColonyUtils.getOwningColony(centralChunk) != NO_COLONY_ID;
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
        for (final ServerLevel world : ServerLifecycleHooks.getCurrentServer().getAllLevels())
        {
            final IServerColonySaveData cap = getColonySaveData(world);
            final int tempTop = cap == null ? 0 : cap.getTopID();
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
    }

    @Override
    public void addColonyDirect(final IColony colony, final ServerLevel world)
    {
        final IServerColonySaveData cap = getColonySaveData(world);
        if (cap != null)
        {
            cap.addColony(colony);
        }
    }

    @Override
    public void addClaimData(final IColony colony, final Long2ObjectMap<ChunkClaimData> claimData)
    {
        this.chunkClaimData.computeIfAbsent(colony.getDimension(), (k) -> new Long2ObjectOpenHashMap<>()).putAll(claimData);
    }

    @Nullable
    @Override
    public IChunkClaimData getClaimData(final ResourceKey<Level> dimension, final ChunkPos pos)
    {
        return this.chunkClaimData.getOrDefault(dimension, new Long2ObjectOpenHashMap<>()).getOrDefault(pos.toLong(), null);
    }

    @Override
    public void addNewChunk(final Colony colony, final ChunkPos pos, final ChunkClaimData chunkClaimData)
    {
        this.chunkClaimData.computeIfAbsent(colony.getDimension(), (k) -> new Long2ObjectOpenHashMap<>()).put(pos.toLong(), chunkClaimData);
    }
}
