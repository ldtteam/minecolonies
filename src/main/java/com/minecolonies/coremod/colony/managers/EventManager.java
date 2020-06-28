package com.minecolonies.coremod.colony.managers;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.IColonyEntitySpawnEvent;
import com.minecolonies.api.colony.colonyEvents.IColonyEvent;
import com.minecolonies.api.colony.colonyEvents.registry.ColonyEventTypeRegistryEntry;
import com.minecolonies.api.colony.managers.interfaces.IEventManager;
import com.minecolonies.api.colony.managers.interfaces.IEventStructureManager;
import com.minecolonies.api.util.Log;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.minecolonies.api.colony.colonyEvents.EventStatus.*;
import static com.minecolonies.api.util.constant.Constants.MOD_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_NAME;

/**
 * Manager for all colony related events.
 */
public class EventManager implements IEventManager
{
    /**
     * NBT tags
     */
    private static final String TAG_EVENT_ID      = "event_currentID";
    private static final String TAG_EVENT_MANAGER = "event_manager";
    private static final String TAG_EVENT_LIST    = "events_list";

    /**
     * The current event ID this colony is at, unique for each event
     */
    private int currentEventID = 1;

    /**
     * Colony reference
     */
    private final IColony colony;

    /**
     * The current events on the colony, with unique ID and event.
     */
    private final Map<Integer, IColonyEvent> events = new HashMap<>();

    /**
     * The related structure manager, which takes care of structures for the events.
     */
    private final EventStructureManager structureManager;

    public EventManager(final IColony colony)
    {
        this.colony = colony;
        structureManager = new EventStructureManager(this, colony);
    }

    /**
     * Adds a event event to the colony.
     *
     * @param colonyEvent the event to add
     */
    @Override
    public void addEvent(final IColonyEvent colonyEvent)
    {
        if (colonyEvent.getID() == 0)
        {
            Log.getLogger().warn("missing ID for event:" + colonyEvent.getEventTypeID().getPath());
            return;
        }
        events.put(colonyEvent.getID(), colonyEvent);
        colony.markDirty();
    }

    /**
     * Increments the id, and returns the taken id.
     *
     * @return the next event Id.
     */
    @Override
    public int getAndTakeNextEventID()
    {
        if (currentEventID > Integer.MAX_VALUE - 100)
        {
            currentEventID = 1;
        }

        currentEventID++;
        colony.markDirty();
        return currentEventID - 1;
    }

    /**
     * Registers an entity with the given event.
     *
     * @param entity the entity to register.
     * @param eventID the event id to register it to.
     */
    @Override
    public void registerEntity(@NotNull final Entity entity, final int eventID)
    {
        final IColonyEvent event = events.get(eventID);
        if (!(event instanceof IColonyEntitySpawnEvent))
        {
            entity.remove();
            return;
        }
        ((IColonyEntitySpawnEvent) event).registerEntity(entity);
    }

    /**
     * Unregisters an entity with the given event
     *
     * @param entity the entity.
     * @param eventID the id of th eevent.
     */
    @Override
    public void unregisterEntity(@NotNull final Entity entity, final int eventID)
    {
        final IColonyEvent event = events.get(eventID);
        if (event instanceof IColonyEntitySpawnEvent)
        {
            ((IColonyEntitySpawnEvent) event).unregisterEntity(entity);
        }
    }

    /**
     * Triggers on entity death(killed by player/environment) of an entity
     *
     * @param entity the entity.
     * @param eventID the id of the event.
     */
    @Override
    public void onEntityDeath(final LivingEntity entity, final int eventID)
    {
        final IColonyEvent event = events.get(eventID);
        if (event instanceof IColonyEntitySpawnEvent)
        {
            ((IColonyEntitySpawnEvent) event).onEntityDeath(entity);
        }
    }

    @Override
    public void onTileEntityBreak(final int eventID, final TileEntity te)
    {
        final IColonyEvent event = events.get(eventID);
        if (event != null)
        {
            event.onTileEntityBreak(te);
        }
    }

    @Override
    public void onNightFall()
    {
        for (final IColonyEvent event : events.values())
        {
            event.onNightFall();
        }
    }

    /**
     * Gets an event by its ID
     *
     * @param ID the id of the event.
     * @return the event or null.
     */
    @Override
    public IColonyEvent getEventByID(final int ID)
    {
        return events.get(ID);
    }

    /**
     * Updates the current events.
     *
     * @param colony the colony being ticked.
     */
    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        final Iterator<IColonyEvent> iterator = events.values().iterator();
        while (iterator.hasNext())
        {
            final IColonyEvent event = iterator.next();

            if (event.getStatus() == DONE)
            {
                event.onFinish();
                structureManager.loadBackupForEvent(event.getID());
                colony.markDirty();
                iterator.remove();
            }
            else if (event.getStatus() == STARTING)
            {
                event.onStart();
            }
            else if (event.getStatus() == CANCELED)
            {
                colony.markDirty();
                iterator.remove();
            }
            else
            {
                event.onUpdate();
            }
        }
    }

    @Override
    public Map<Integer, IColonyEvent> getEvents()
    {
        return events;
    }

    @Override
    public void readFromNBT(@NotNull final CompoundNBT compound)
    {
        if (compound.contains(TAG_EVENT_MANAGER))
        {
            final CompoundNBT eventManagerNBT = compound.getCompound(TAG_EVENT_MANAGER);
            final ListNBT eventListNBT = eventManagerNBT.getList(TAG_EVENT_LIST, Constants.NBT.TAG_COMPOUND);
            for (final INBT base : eventListNBT)
            {
                final CompoundNBT tagCompound = (CompoundNBT) base;
                final ResourceLocation eventTypeID = new ResourceLocation(MOD_ID, tagCompound.getString(TAG_NAME));

                final ColonyEventTypeRegistryEntry registryEntry = MinecoloniesAPIProxy.getInstance().getColonyEventRegistry().getValue(eventTypeID);
                if (registryEntry == null)
                {
                    Log.getLogger().warn("Event is missing registryEntry!:" + eventTypeID.getPath());
                    continue;
                }

                final IColonyEvent colonyEvent = registryEntry.getEventCreator().apply(colony, tagCompound);
                events.put(colonyEvent.getID(), colonyEvent);
            }

            currentEventID = eventManagerNBT.getInt(TAG_EVENT_ID);
            structureManager.readFromNBT(compound);
        }
    }

    @Override
    public void writeToNBT(@NotNull final CompoundNBT compound)
    {
        final CompoundNBT eventManagerNBT = new CompoundNBT();
        final ListNBT eventListNBT = new ListNBT();
        for (final IColonyEvent event : events.values())
        {
            final CompoundNBT eventNBT = new CompoundNBT();
            eventNBT.putString(TAG_NAME, event.getEventTypeID().getPath());
            event.writeToNBT(eventNBT);
            eventListNBT.add(eventNBT);
        }

        eventManagerNBT.putInt(TAG_EVENT_ID, currentEventID);
        eventManagerNBT.put(TAG_EVENT_LIST, eventListNBT);
        compound.put(TAG_EVENT_MANAGER, eventManagerNBT);
        structureManager.writeToNBT(compound);
    }

    @Override
    public IEventStructureManager getStructureManager()
    {
        return structureManager;
    }
}

