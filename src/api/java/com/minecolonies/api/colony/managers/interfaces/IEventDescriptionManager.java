package com.minecolonies.api.colony.managers.interfaces;

import com.minecolonies.api.colony.colonyEvents.descriptions.IColonyEventDescription;

import net.minecraft.nbt.CompoundNBT;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Interface for the event description manager, the event description manager deals the colony event log events.
 */
public interface IEventDescriptionManager
{
    /**
     * Adds an event description.
     * 
     * @param colonyEventDescription the event description to add.
     */
    void addEventDescription(IColonyEventDescription colonyEventDescription);

    /**
     * Returns the current list of colony events.
     * 
     * @return the list of colony events.
     */
    List<IColonyEventDescription> getEventDescriptions();

    /**
     * Reads the eventManager nbt and creates events from it
     *
     * @param compound the compound to read from.
     */
    void readFromNBT(@NotNull CompoundNBT compound);

    /**
     * Write the eventmanager and all events to NBT
     *
     * @param compound the compound to write to.
     */
    void writeToNBT(@NotNull CompoundNBT compound);
}
