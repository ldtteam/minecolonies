package com.minecolonies.api.colony.colonyEvents;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Interface for all colony event types.
 */
public interface IColonyEvent extends INBTSerializable<CompoundTag>
{
    /**
     * Returns the events current status
     *
     * @return the status.
     */
    EventStatus getStatus();

    /**
     * Sets the current event status
     *
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
    default void onTileEntityBreak(final BlockEntity te) { }

    /**
     * Called on night fall, to execute special day-based logic.
     */
    default void onNightFall() { }
}
