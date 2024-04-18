package com.minecolonies.core.colony.managers;

import com.google.common.collect.EvictingQueue;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.expeditions.IExpeditionMember;
import com.minecolonies.api.colony.managers.interfaces.IColonyExpeditionManager;
import com.minecolonies.api.entity.visitor.ModVisitorTypes;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpedition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.Event.HasResult;
import net.minecraftforge.eventbus.api.Event.Result;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation for the colony expedition manager. From here all outgoing expeditions to external places are managed.
 */
public class ColonyExpeditionManager implements IColonyExpeditionManager
{
    /**
     * NBT tags.
     */
    private static final String TAG_ACTIVE_EXPEDITIONS         = "activeExpeditions";
    private static final String TAG_FINISHED_EXPEDITIONS       = "finishedExpeditions";
    private static final String TAG_RUINED_PORTAL_DISCOVER     = "isRuinedPortalDiscovered";
    private static final String TAG_STRONGHOLD_PORTAL_DISCOVER = "isStrongholdDiscovered";

    /**
     * The maximum amount of expeditions kept in the history.
     */
    private static final int MAX_EXPEDITION_HISTORY = 5;

    /**
     * The colony this manager is for.
     */
    private final IColony colony;

    /**
     * The currently active expedition(s).
     */
    private final Map<Integer, ColonyExpedition> activeExpeditions = new HashMap<>();

    /**
     * The currently finished expeditions.
     */
    private final EvictingQueue<ColonyExpedition> finishedExpeditions = EvictingQueue.create(MAX_EXPEDITION_HISTORY);

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
    public ColonyExpedition getExpedition(final int id)
    {
        return activeExpeditions.get(id);
    }

    @Override
    public boolean addExpedition(final ColonyExpedition expedition)
    {
        final IExpeditionMember<?> leader = expedition.getLeader();
        if (activeExpeditions.containsKey(leader.getId()))
        {
            return false;
        }

        activeExpeditions.put(expedition.getId(), expedition);
        updateCaches();

        colony.markDirty();
        dirty = true;
        return true;
    }

    @Override
    public void finishExpedition(final int id)
    {
        if (activeExpeditions.containsKey(id))
        {
            finishedExpeditions.add(activeExpeditions.remove(id));
            updateCaches();

            colony.markDirty();
            dirty = true;
        }
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

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = new CompoundTag();
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
                                                          expedition.write(expeditionItemCompound);
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
        final ListTag activeExpeditionsCompound = compound.getList(TAG_ACTIVE_EXPEDITIONS, Tag.TAG_COMPOUND);
        activeExpeditions.clear();
        activeExpeditions.putAll(NBTUtils.streamCompound(activeExpeditionsCompound)
                                   .map(ColonyExpedition::loadFromNBT)
                                   .collect(Collectors.toMap(ColonyExpedition::getId, v -> v)));

        final ListTag finishedExpeditionsCompound = compound.getList(TAG_FINISHED_EXPEDITIONS, Tag.TAG_COMPOUND);
        finishedExpeditions.clear();
        finishedExpeditions.addAll(NBTUtils.streamCompound(finishedExpeditionsCompound)
                                     .map(ColonyExpedition::loadFromNBT)
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
        finishedExpeditionsCache = finishedExpeditions.stream().toList();
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