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
     * @return
     */
    public EventStatus getStatus();

    /**
     * Sets the current event status
     *
     * @return
     */
    public void setStatus(final EventStatus status);

    /**
     * Returns this events ID.
     *
     * @return
     */
    public int getID();

    /**
     * The event type's id
     *
     * @return
     */
    public ResourceLocation getEventTypeID();

    /**
     * Sets the colony
     *
     * @param colony
     */
    public void setColony(@NotNull final IColony colony);

    /**
     * Writes the event to NBT
     *
     * @param compound
     * @return
     */
    public CompoundNBT writeToNBT(final CompoundNBT compound);

    /**
     * Reads the events values from NBT
     *
     * @param compound
     */
    public void readFromNBT(final CompoundNBT compound);

    /**
     *
     * Event triggers
     *
     */

    /**
     * Onupdate function, called every 25s / 500 ticks. Comes from colony -> eventmanager -> event.
     */
    default public void onUpdate() {}

    /**
     * Actions which are done on the start of the event.
     */
    default public void onStart() {}

    /**
     * Actions which are done on the finish/removal of the event.
     */
    default public void onFinish() {}

    /**
     * Called by tileentities relevant to the event on invalidation.
     *
     * @param te
     */
    default public void onTileEntityBreak(final TileEntity te) {}

    /**
     * Called on night fall, to execute special day-based logic.
     */
    default public void onNightFall() {}
}
