package com.minecolonies.api.colony.colonyEvents.descriptions;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * Description for an event that happened in the colony.
 */
public interface IColonyEventDescription extends INBTSerializable<CompoundTag>
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
     * Serializes this event to the given {@link RegistryFriendlyByteBuf}.
     * 
     * @param buf the {@link RegistryFriendlyByteBuf} to serialize to.
     */
    void serialize(final RegistryFriendlyByteBuf buf);

    /**
     * Deserializes this event from the given {@link RegistryFriendlyByteBuf}.
     * 
     * @param buf the {@link RegistryFriendlyByteBuf} to deserialize from.
     */
    void deserialize(final RegistryFriendlyByteBuf buf);
}
