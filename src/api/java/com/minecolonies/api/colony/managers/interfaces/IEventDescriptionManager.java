package com.minecolonies.api.colony.managers.interfaces;

import com.minecolonies.api.colony.colonyEvents.descriptions.IColonyEventDescription;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.List;

/**
 * Interface for the event description manager, the event description manager deals the colony event log events.
 */
public interface IEventDescriptionManager extends INBTSerializable<CompoundNBT>
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
}
