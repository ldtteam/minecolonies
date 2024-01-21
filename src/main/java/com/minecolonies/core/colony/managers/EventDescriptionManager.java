package com.minecolonies.core.colony.managers;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.descriptions.IColonyEventDescription;
import com.minecolonies.api.colony.colonyEvents.registry.ColonyEventDescriptionTypeRegistryEntry;
import com.minecolonies.api.colony.managers.interfaces.IEventDescriptionManager;
import com.minecolonies.api.util.Log;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import static com.minecolonies.api.util.constant.ColonyConstants.MAX_COLONY_EVENTS;
import static com.minecolonies.api.util.constant.Constants.MOD_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_NAME;

/**
 * Manager for all colony related events.
 */
public class EventDescriptionManager implements IEventDescriptionManager
{
    /**
     * NBT tags
     */
    private static final String TAG_EVENT_DESC_LIST    = "event_descs_list";

    /**
     * Colony reference
     */
    private final IColony colony;

    /**
     * The event descriptions of this colony.
     */
    private final LinkedList<IColonyEventDescription> eventDescs = new LinkedList<>();

    public EventDescriptionManager(final IColony colony)
    {
        this.colony = colony;
    }

    @Override
    public void addEventDescription(IColonyEventDescription colonyEventDescription)
    {
        if (eventDescs.size() >= MAX_COLONY_EVENTS)
        {
            eventDescs.removeFirst();
        }
        eventDescs.add(colonyEventDescription);
        if (colony.getBuildingManager().getTownHall() != null)
        {
            colony.getBuildingManager().getTownHall().markDirty();
        }
        else
        {
            colony.markDirty();
        }
    }

    @Override
    public List<IColonyEventDescription> getEventDescriptions()
    {
        return eventDescs;
    }

    @Override
    public void deserializeNBT(@NotNull final CompoundTag eventManagerNBT)
    {
        final ListTag eventDescListNBT = eventManagerNBT.getList(TAG_EVENT_DESC_LIST, Tag.TAG_COMPOUND);
        for (final Tag event : eventDescListNBT)
        {
            final CompoundTag eventCompound = (CompoundTag) event;
            final ResourceLocation eventTypeID = new ResourceLocation(MOD_ID, eventCompound.getString(TAG_NAME));

            final ColonyEventDescriptionTypeRegistryEntry registryEntry = MinecoloniesAPIProxy.getInstance().getColonyEventDescriptionRegistry().getValue(eventTypeID);
            if (registryEntry == null)
            {
                Log.getLogger().warn("Event is missing registryEntry!:" + eventTypeID.getPath());
                continue;
            }

            final IColonyEventDescription eventDescription = registryEntry.deserializeEventDescriptionFromNBT(eventCompound);
            eventDescs.add(eventDescription);
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag eventManagerNBT = new CompoundTag();
        final ListTag eventDescsListNBT = new ListTag();
        for (final IColonyEventDescription event : eventDescs)
        {
            final CompoundTag eventNBT = event.serializeNBT();
            eventNBT.putString(TAG_NAME, event.getEventTypeId().getPath());
            eventDescsListNBT.add(eventNBT);
        }

        eventManagerNBT.put(TAG_EVENT_DESC_LIST, eventDescsListNBT);
        return eventManagerNBT;
    }
}
