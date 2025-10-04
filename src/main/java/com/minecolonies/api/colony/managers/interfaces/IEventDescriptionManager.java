package com.minecolonies.api.colony.managers.interfaces;

import com.minecolonies.api.colony.colonyEvents.descriptions.IColonyEventDescription;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Interface for the event description manager, the event description manager deals the colony event log events.
 */
public interface IEventDescriptionManager extends INBTSerializable<CompoundTag>
{
    /**
     * Adds an event description.
     * 
     * @param colonyEventDescription the event description to add.
     */
    void addEventDescription(IColonyEventDescription colonyEventDescription);

    /**
     * Compute news to print for the player.
     */
    void computeNews();

    /**
     * Serialize to bytebuf.
     * @param buf the buf to serialize it to.
     */
    void serialize(@NotNull FriendlyByteBuf buf);
}
