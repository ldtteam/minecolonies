package com.minecolonies.core.colony.managers;

import com.google.common.collect.EvictingQueue;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.expeditions.IExpedition;
import com.minecolonies.api.colony.managers.interfaces.IExpeditionManager;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.Event.HasResult;
import net.minecraftforge.eventbus.api.Event.Result;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation for the colony expedition manager. From here all outgoing expeditions to external places are managed.
 */
public class ExpeditionManager implements IExpeditionManager
{
    /**
     * The maximum amount of expeditions kept in the history, separated per expedition owner type.
     */
    private static final int MAX_EXPEDITION_HISTORY = 5;

    /**
     * The colony this manager is for.
     */
    private final IColony colony;

    /**
     * The currently registered expeditions.
     */
    private final Map<Class<?>, EvictingQueue<IExpedition>> expeditions = new HashMap<>();

    /**
     * Whether a ruined portal has been discovered by an expedition.
     */
    private boolean isRuinedPortalDiscovered;

    /**
     * Whether a stronghold has been discovered by an expedition.
     */
    private boolean isStrongholdDiscovered;

    /**
     * Default constructor.
     */
    public ExpeditionManager(final IColony colony)
    {
        this.colony = colony;
    }

    @Override
    public void addExpedition(final IExpedition expedition, final Class<?> owner)
    {
        this.expeditions.computeIfAbsent(owner, clazz -> EvictingQueue.create(MAX_EXPEDITION_HISTORY));
        this.expeditions.get(owner).add(expedition);
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

    /**
     * This event is fired by {@link ExpeditionManager#canGoToDimension(ResourceKey)}.
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