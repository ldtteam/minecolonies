package com.minecolonies.core.colony.managers;

import com.google.common.collect.EvictingQueue;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.expeditions.ExpeditionFinishedStatus;
import com.minecolonies.api.colony.expeditions.ExpeditionStatus;
import com.minecolonies.api.colony.expeditions.IExpeditionMember;
import com.minecolonies.api.colony.managers.interfaces.expeditions.ColonyExpedition;
import com.minecolonies.api.colony.managers.interfaces.expeditions.CreatedExpedition;
import com.minecolonies.api.colony.managers.interfaces.expeditions.FinishedExpedition;
import com.minecolonies.api.colony.managers.interfaces.expeditions.IColonyExpeditionManager;
import com.minecolonies.api.entity.visitor.ModVisitorTypes;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.core.client.gui.generic.ResourceItem.ResourceAvailability;
import com.minecolonies.core.colony.expeditions.colony.requirements.ColonyExpeditionRequirement;
import com.minecolonies.core.colony.expeditions.colony.requirements.ColonyExpeditionRequirement.RequirementHandler;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionType;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionTypeManager;
import com.minecolonies.core.items.ItemExpeditionSheet.ExpeditionSheetContainerManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.Event.HasResult;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Implementation for the colony expedition manager. From here all outgoing expeditions to external places are managed.
 */
public class ColonyExpeditionManager implements IColonyExpeditionManager
{
    /**
     * NBT tags.
     */
    private static final String TAG_CREATED_EXPEDITIONS         = "createdExpeditions";
    private static final String TAG_CREATED_EXPEDITION_ID       = "id";
    private static final String TAG_CREATED_EXPEDITION_TYPE_ID  = "expeditionTypeId";
    private static final String TAG_CREATED_EXPEDITION_ACCEPTED = "accepted";
    private static final String TAG_ACTIVE_EXPEDITIONS          = "activeExpeditions";
    private static final String TAG_FINISHED_EXPEDITIONS        = "finishedExpeditions";
    private static final String TAG_FINISHED_EXPEDITION_DATA    = "data";
    private static final String TAG_FINISHED_EXPEDITION_STATUS  = "status";
    private static final String TAG_RUINED_PORTAL_DISCOVER      = "isRuinedPortalDiscovered";
    private static final String TAG_STRONGHOLD_PORTAL_DISCOVER  = "isStrongholdDiscovered";

    /**
     * The maximum amount of expeditions kept in the history.
     */
    private static final int MAX_EXPEDITION_HISTORY = 5;

    /**
     * The colony this manager is for.
     */
    private final IColony colony;

    /**
     * The currently created expedition(s).
     */
    private final Map<Integer, CreatedExpedition> createdExpeditions = new HashMap<>();

    /**
     * The currently active expedition(s).
     */
    private final Map<Integer, ColonyExpedition> activeExpeditions = new HashMap<>();

    /**
     * The currently finished expeditions.
     */
    private final EvictingQueue<FinishedExpedition> finishedExpeditions = EvictingQueue.create(MAX_EXPEDITION_HISTORY);

    /**
     * Whether a ruined portal has been discovered by an expedition.
     */
    private boolean isRuinedPortalDiscovered;

    /**
     * Whether a stronghold has been discovered by an expedition.
     */
    private boolean isStrongholdDiscovered;

    /**
     * The cached list of active expeditions for {@link ColonyExpeditionManager#getActiveExpeditions()}.
     */
    private List<ColonyExpedition> activeExpeditionsCache;

    /**
     * The cached list of finished expeditions for {@link ColonyExpeditionManager#getFinishedExpeditions()} ()}.
     */
    private List<ColonyExpedition> finishedExpeditionsCache;

    /**
     * Whether this manager is dirty.
     */
    private boolean dirty;

    /**
     * Default constructor.
     */
    public ColonyExpeditionManager(final IColony colony)
    {
        this.colony = colony;
    }

    @Override
    public List<ColonyExpedition> getActiveExpeditions()
    {
        return activeExpeditionsCache;
    }

    @Override
    public List<ColonyExpedition> getFinishedExpeditions()
    {
        return finishedExpeditionsCache;
    }

    @Override
    @Nullable
    public CreatedExpedition getCreatedExpedition(final int id)
    {
        return createdExpeditions.get(id);
    }

    @Override
    @Nullable
    public ColonyExpedition getActiveExpedition(final int id)
    {
        return activeExpeditions.get(id);
    }

    @Override
    @Nullable
    public FinishedExpedition getFinishedExpedition(final int id)
    {
        return finishedExpeditions.stream().filter(f -> f.expedition().getId() == id).findFirst().orElse(null);
    }

    @Override
    @NotNull
    public ExpeditionStatus getExpeditionStatus(final int id)
    {
        if (createdExpeditions.containsKey(id))
        {
            return createdExpeditions.get(id).accepted() ? ExpeditionStatus.ACCEPTED : ExpeditionStatus.CREATED;
        }

        if (activeExpeditions.containsKey(id))
        {
            return ExpeditionStatus.ONGOING;
        }

        if (finishedExpeditions.stream().anyMatch(finishedExpedition -> finishedExpedition.expedition().getId() == id))
        {
            return ExpeditionStatus.FINISHED;
        }

        return ExpeditionStatus.UNKNOWN;
    }

    @Override
    public boolean addExpedition(final int id, final ResourceLocation expeditionTypeId)
    {
        if (createdExpeditions.containsKey(id))
        {
            return false;
        }

        createdExpeditions.put(id, new CreatedExpedition(id, expeditionTypeId, false));
        updateCaches();

        colony.markDirty();
        dirty = true;
        return true;
    }

    @Override
    public boolean acceptExpedition(final int id)
    {
        final boolean exists = createdExpeditions.containsKey(id);
        if (exists)
        {
            createdExpeditions.put(id, createdExpeditions.get(id).accept());
            updateCaches();

            colony.markDirty();
            dirty = true;
        }
        return exists;
    }

    @Override
    public boolean startExpedition(final int id, final List<IExpeditionMember<?>> members, final List<ItemStack> equipment)
    {
        final boolean exists = createdExpeditions.containsKey(id);
        if (exists && createdExpeditions.get(id).accepted())
        {
            activeExpeditions.put(id, createdExpeditions.get(id).createExpedition(members, equipment));
            createdExpeditions.remove(id);
            updateCaches();

            colony.markDirty();
            dirty = true;
        }
        return exists;
    }

    @Override
    public boolean finishExpedition(final int id, final ExpeditionFinishedStatus status)
    {
        final boolean exists = activeExpeditions.containsKey(id);
        if (exists)
        {
            finishedExpeditions.add(new FinishedExpedition(activeExpeditions.remove(id), status));
            updateCaches();

            colony.markDirty();
            dirty = true;
        }
        return exists;
    }

    @Override
    public void removeCreatedExpedition(final int id)
    {
        createdExpeditions.remove(id);
    }

    @Override
    public boolean canStartNewExpedition()
    {
        return colony.getVisitorManager().getCivilianDataMap().values().stream()
                 .noneMatch(f -> f.getVisitorType().equals(ModVisitorTypes.expeditionary.get()));
    }

    @Override
    public boolean canGoToDimension(final ResourceKey<Level> dimension)
    {
        final ExpeditionDimensionAllowedEvent event = new ExpeditionDimensionAllowedEvent(dimension);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getResult().equals(Result.ALLOW))
        {
            return true;
        }

        if (dimension.equals(Level.OVERWORLD))
        {
            return true;
        }
        else if (dimension.equals(Level.NETHER))
        {
            return isRuinedPortalDiscovered
                     || colony.getBuildingManager().getFirstBuildingMatching(building -> building.getBuildingType().equals(ModBuildings.netherWorker.get())) != null;
        }
        else if (dimension.equals(Level.END))
        {
            return isStrongholdDiscovered;
        }
        return false;
    }

    @Override
    public boolean meetsRequirements(final ResourceLocation expeditionTypeId, final ExpeditionSheetContainerManager inventory)
    {
        return parseExpeditionAndRunActual(expeditionTypeId, inventory, this::meetsRequirements, false);
    }

    @Override
    public boolean meetsRequirements(final ColonyExpeditionType expeditionType, final ExpeditionSheetContainerManager inventory)
    {
        return expeditionType.requirements().stream()
                 .map(m -> m.createHandler(new InvWrapper(inventory)))
                 .anyMatch(f -> f.getAvailabilityStatus().equals(ResourceAvailability.NOT_NEEDED))
                 && inventory.getMembers().size() >= expeditionType.guards();
    }

    @Override
    public List<ItemStack> extractItemsFromSheet(final ResourceLocation expeditionTypeId, final ExpeditionSheetContainerManager containerManager)
    {
        return parseExpeditionAndRunActual(expeditionTypeId, containerManager, this::extractItemsFromSheet, List.of());
    }

    @Override
    public List<ItemStack> extractItemsFromSheet(final ColonyExpeditionType expeditionType, final ExpeditionSheetContainerManager containerManager)
    {
        final List<ItemStack> items = new ArrayList<>();

        final IItemHandler handler = new InvWrapper(containerManager);
        for (final ColonyExpeditionRequirement requirement : expeditionType.requirements())
        {
            final RequirementHandler requirementHandler = requirement.createHandler(handler);
            if (!requirementHandler.shouldConsumeOnStart())
            {
                items.addAll(InventoryUtils.filterItemHandler(handler, requirementHandler.getItemPredicate()));
            }
        }

        return InventoryUtils.processItemStackListAndMerge(items);
    }

    @Override
    public void unlockNether()
    {
        isRuinedPortalDiscovered = true;
    }

    @Override
    public void unlockEnd()
    {
        isStrongholdDiscovered = true;
    }

    @Override
    public boolean isDirty()
    {
        return dirty;
    }

    @Override
    public void setDirty(final boolean dirty)
    {
        this.dirty = dirty;
    }

    /**
     * Helper method to parse the underlying expedition type and execute the full method.
     *
     * @param expeditionTypeId the expedition type id.
     * @param arg              the extra argument.
     * @param func             the function runnable.
     * @param def              the default return value if the expedition can't be found.
     * @param <T>              the return type.
     * @param <A>              the extra argument.
     * @return the original result from the actual method.
     */
    private <T, A> T parseExpeditionAndRunActual(final ResourceLocation expeditionTypeId, final A arg, final BiFunction<ColonyExpeditionType, A, T> func, final T def)
    {
        final ColonyExpeditionType expeditionType = ColonyExpeditionTypeManager.getInstance().getExpeditionType(expeditionTypeId);
        if (expeditionType == null)
        {
            Log.getLogger().warn("Expedition type with id {} does not exist", expeditionTypeId);
            return def;
        }

        return func.apply(expeditionType, arg);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = new CompoundTag();
        final ListTag createdExpeditionsCompound = createdExpeditions.entrySet().stream()
                                                     .map(expedition -> {
                                                         final CompoundTag expeditionItemCompound = new CompoundTag();
                                                         expeditionItemCompound.putInt(TAG_CREATED_EXPEDITION_ID, expedition.getKey());
                                                         expeditionItemCompound.putString(TAG_CREATED_EXPEDITION_TYPE_ID, expedition.getValue().expeditionTypeId().toString());
                                                         expeditionItemCompound.putBoolean(TAG_CREATED_EXPEDITION_ACCEPTED, expedition.getValue().accepted());
                                                         return expeditionItemCompound;
                                                     })
                                                     .collect(NBTUtils.toListNBT());
        compound.put(TAG_CREATED_EXPEDITIONS, createdExpeditionsCompound);

        final ListTag activeExpeditionsCompound = activeExpeditions.values().stream()
                                                    .map(expedition -> {
                                                        final CompoundTag expeditionItemCompound = new CompoundTag();
                                                        expedition.write(expeditionItemCompound);
                                                        return expeditionItemCompound;
                                                    })
                                                    .collect(NBTUtils.toListNBT());
        compound.put(TAG_ACTIVE_EXPEDITIONS, activeExpeditionsCompound);

        final ListTag finishedExpeditionsCompound = finishedExpeditions.stream()
                                                      .map(expedition -> {
                                                          final CompoundTag expeditionItemCompound = new CompoundTag();
                                                          final CompoundTag expeditionDataCompound = new CompoundTag();
                                                          expedition.expedition().write(expeditionDataCompound);
                                                          expeditionItemCompound.put(TAG_FINISHED_EXPEDITION_DATA, expeditionDataCompound);
                                                          expeditionItemCompound.putString(TAG_FINISHED_EXPEDITION_STATUS, expedition.status().name());
                                                          return expeditionItemCompound;
                                                      })
                                                      .collect(NBTUtils.toListNBT());
        compound.put(TAG_FINISHED_EXPEDITIONS, finishedExpeditionsCompound);

        compound.putBoolean(TAG_RUINED_PORTAL_DISCOVER, isRuinedPortalDiscovered);
        compound.putBoolean(TAG_STRONGHOLD_PORTAL_DISCOVER, isStrongholdDiscovered);
        return compound;
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        final ListTag createdExpeditionsCompound = compound.getList(TAG_CREATED_EXPEDITIONS, Tag.TAG_COMPOUND);
        createdExpeditions.clear();
        createdExpeditions.putAll(NBTUtils.streamCompound(createdExpeditionsCompound)
                                    .map(expeditionItemCompound -> {
                                        final int id = expeditionItemCompound.getInt(TAG_CREATED_EXPEDITION_ID);
                                        final ResourceLocation expeditionTypeId = new ResourceLocation(expeditionItemCompound.getString(TAG_CREATED_EXPEDITION_TYPE_ID));
                                        final boolean accepted = expeditionItemCompound.getBoolean(TAG_CREATED_EXPEDITION_ACCEPTED);
                                        return new CreatedExpedition(id, expeditionTypeId, accepted);
                                    })
                                    .collect(Collectors.toMap(CreatedExpedition::id, v -> v)));

        final ListTag activeExpeditionsCompound = compound.getList(TAG_ACTIVE_EXPEDITIONS, Tag.TAG_COMPOUND);
        activeExpeditions.clear();
        activeExpeditions.putAll(NBTUtils.streamCompound(activeExpeditionsCompound)
                                   .map(ColonyExpedition::loadFromNBT)
                                   .collect(Collectors.toMap(ColonyExpedition::getId, v -> v)));

        final ListTag finishedExpeditionsCompound = compound.getList(TAG_FINISHED_EXPEDITIONS, Tag.TAG_COMPOUND);
        finishedExpeditions.clear();
        finishedExpeditions.addAll(NBTUtils.streamCompound(finishedExpeditionsCompound)
                                     .map((expeditionItemCompound) -> {
                                         final ColonyExpedition expeditionCompound = ColonyExpedition.loadFromNBT(expeditionItemCompound.getCompound(TAG_FINISHED_EXPEDITION_DATA));
                                         final ExpeditionFinishedStatus status = ExpeditionFinishedStatus.valueOf(expeditionItemCompound.getString(TAG_FINISHED_EXPEDITION_STATUS));
                                         return new FinishedExpedition(expeditionCompound, status);
                                     })
                                     .toList());

        updateCaches();

        isRuinedPortalDiscovered = compound.getBoolean(TAG_RUINED_PORTAL_DISCOVER);
        isStrongholdDiscovered = compound.getBoolean(TAG_STRONGHOLD_PORTAL_DISCOVER);
    }

    /**
     * Update the cache lists for the list getters.
     */
    private void updateCaches()
    {
        activeExpeditionsCache = activeExpeditions.values().stream().toList();
        finishedExpeditionsCache = finishedExpeditions.stream().map(FinishedExpedition::expedition).collect(Collectors.toList());
        Collections.reverse(finishedExpeditionsCache);
    }

    /**
     * This event is fired by {@link ColonyExpeditionManager#canGoToDimension(ResourceKey)}.
     * This allows other mods to control whether a dimension is allowed to send expedition to from the colony.
     * <p>
     * Set the result to {@link net.minecraftforge.eventbus.api.Event.Result#ALLOW}, otherwise the dimension is deemed as not allowed.
     */
    @HasResult
    private static class ExpeditionDimensionAllowedEvent extends Event
    {
        /**
         * The requested dimension.
         */
        private final ResourceKey<Level> dimension;

        /**
         * Internal constructor.
         *
         * @param dimension the requested dimension.
         */
        private ExpeditionDimensionAllowedEvent(final ResourceKey<Level> dimension)
        {
            this.dimension = dimension;
        }

        /**
         * The requested dimension.
         *
         * @return the level resource key.
         */
        public ResourceKey<Level> getDimension()
        {
            return dimension;
        }
    }
}