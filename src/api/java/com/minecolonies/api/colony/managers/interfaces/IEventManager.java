package com.minecolonies.api.colony.managers.interfaces;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.IColonyEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Interface for the event manager, the event manager deals with all colony related events, such as raid events.
 */
public interface IEventManager
{
    /**
     * Adds an event
     *
     * @param colonyEvent even to add
     */
    void addEvent(IColonyEvent colonyEvent);

    /**
     * Gets and takes the next open event ID.
     *
     * @return int ID
     */
    int getAndTakeNextEventID();

    /**
     * Registers an entity with the given event
     * @param entity entity to register
     * @param eventID eventID to register on
     */
    void registerEntity(@NotNull Entity entity, int eventID);

    /**
     * Unregisters an entity with the given event
     * @param entity entity to unregister
     * @param eventID eventID to unregister on
     */
    void unregisterEntity(@NotNull Entity entity, int eventID);

    /**
     * Lets the event know if a certain entity died
     *
     * @param entity  entity that died
     * @param eventID eventID to forward the Death to
     */
    void onEntityDeath(EntityLiving entity, int eventID);

    /**
     * Allows events to react to nightfall, which is always calculated by the colony itself
     */
    void onNightFall();

    /**
     * Forwards a broken tileEntity to the given event, to react to TE breaks. The TE needs to call this on break
     *
     * @param eventID ID of the related event
     * @param te      tileentity to use
     */
    void onTileEntityBreak(int eventID, TileEntity te);

    /**
     * Update function, which is called from the colony every 500 ticks.
     * Used to update event states/remove them if needed.
     * Forwarded to events aswell to allow them tick based logic
     * @param colony
     */
    void onColonyTick(@NotNull IColony colony);

    /**
     * Gets an event by its id.
     *
     * @param ID event ID to get
     * @return event
     */
    IColonyEvent getEventByID(int ID);

    /**
     * Returns the full event Map
     * @return
     */
    Map<Integer, IColonyEvent> getEvents();

    /**
     * Reads the eventManager nbt and creates events from it
     * @param compound
     */
    void readFromNBT(@NotNull NBTTagCompound compound);

    /**
     * Write the eventmanager and all events to NBT
     * @param compound
     */
    void writeToNBT(@NotNull NBTTagCompound compound);

    /**
     * Returns the associated structure manager, which manages structure spawn/despawn for events.
     * @return
     */
    IEventStructureManager getStructureManager();
}
