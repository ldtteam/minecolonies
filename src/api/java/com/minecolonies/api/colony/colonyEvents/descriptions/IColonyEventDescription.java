package com.minecolonies.api.colony.colonyEvents.descriptions;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

/**
 * Description for an event that happened in the colony.
 */
public interface IColonyEventDescription
{
    /**
     * Gets this event types registry id.
     * 
     * @return this event types registry id.
     */
    ResourceLocation getEventTypeId();

    /**
     * Gets the name of this event type.
     * 
     * @return the name of this event type.
     */
    String getName();

    /**
     * Builds the string to show in the colony events list.
     * 
     * @return the string to show in the colony events list.
     */
    default String toDisplayString()
    {
        return String.format("%s at %d %d %d.%n", getName(), getEventPos().getX(), getEventPos().getY(), getEventPos().getZ());
    }

    /**
     * Returns the position at which this event occurred.
     * 
     * @return the position at which this event occurred.
     */
    BlockPos getEventPos();

    /**
     * Sets the position this event happened at.
     * 
     * @param pos the position this event happened at.
     */
    void setEventPos(BlockPos pos);

    /**
     * Writes the event to the given {@link CompoundNBT NBT Compound}.
     *
     * @param compound the {@link CompoundNBT} to write it to.
     * @return the {@link CompoundNBT}.
     */
    CompoundNBT writeToNBT(final CompoundNBT compound);

    /**
     * Reads the events values from the given {@link CompoundNBT NBT Compound}.
     *
     * @param compound the {@link CompoundNBT} to read it from.
     */
    void readFromNBT(final CompoundNBT compound);

    /**
     * Serializes this event to the given {@link PacketBuffer}.
     * 
     * @param buf the {@link PacketBuffer} to serialize to.
     */
    void serialize(final PacketBuffer buf);

    /**
     * Deserializes this event from the given {@link PacketBuffer}.
     * 
     * @param buf the {@link PacketBuffer} to deserialize from.
     */
    void deserialize(final PacketBuffer buf);
}
