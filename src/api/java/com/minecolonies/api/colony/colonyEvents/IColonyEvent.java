package com.minecolonies.api.colony.colonyEvents;

import com.minecolonies.api.colony.IColony;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for all colony event types.
 */
public interface IColonyEvent
{
    /**
     * Returns the events current status
     *
     * @return the status.
     */
    EventStatus getStatus();

    /**
     * Sets the current event status
     * @param status the status to set.
     */
    void setStatus(final EventStatus status);

    /**
     * Returns this events ID.
     *
     * @return the event id.
     */
    int getID();

    /**
     * The event type's id
     *
     * @return the event type id.
     */
    ResourceLocation getEventTypeID();

    /**
     * Sets the colony
     *
     * @param colony the colony to set.
     */
    void setColony(@NotNull final IColony colony);

    /**
     * Writes the event to NBT
     *
     * @param compound the compound to write it to.
     * @return the compound.
     */
    CompoundNBT writeToNBT(final CompoundNBT compound);

    /**
     * Reads the events values from NBT
     *
     * @param compound the compound to read it from.
     */
    void readFromNBT(final CompoundNBT compound);

    /*
     *
     * Event triggers
     *
     */

    /**
     * Onupdate function, called every 25s / 500 ticks. Comes from colony then goes to the eventmanager and then to the event.
     */
    default void onUpdate() { }

    /**
     * Actions which are done on the start of the event.
     */
    default void onStart() { }

    /**
     * Actions which are done on the finish/removal of the event.
     */
    default void onFinish() { }

    /**
     * Called by tileentities relevant to the event on invalidation.
     *
     * @param te the broken Tile entity.
     */
    default void onTileEntityBreak(final TileEntity te) { }

    /**
     * Called on night fall, to execute special day-based logic.
     */
    default void onNightFall() { }
}
