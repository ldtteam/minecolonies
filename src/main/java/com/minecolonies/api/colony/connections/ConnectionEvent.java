package com.minecolonies.api.colony.connections;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_STATUS;

/**
 * Connected Event Data with:
 *
 * @param id              the colony id.
 * @param connectionEventType the event type enum.
 */
public record ConnectionEvent(int id, String name, ConnectionEventType connectionEventType)
{
    public CompoundTag serializeNBT()
    {
        final CompoundTag compoundTag = new CompoundTag();
        compoundTag.putInt(TAG_ID, id);
        compoundTag.putString(TAG_NAME, name);
        compoundTag.putInt(TAG_STATUS, connectionEventType.ordinal());
        return compoundTag;
    }

    public void serializeByteBuf(final FriendlyByteBuf buf)
    {
        buf.writeInt(id);
        buf.writeUtf(name);
        buf.writeInt(connectionEventType.ordinal());
    }

    public static ConnectionEvent deserializeNBT(final CompoundTag compoundTag)
    {
        return new ConnectionEvent(compoundTag.getInt(TAG_ID),
            compoundTag.getString(TAG_NAME),
            ConnectionEventType.values()[compoundTag.getInt(TAG_STATUS)]);
    }

    public static ConnectionEvent deserializeByteBuf(final FriendlyByteBuf buf)
    {
        return new ConnectionEvent(buf.readInt(), buf.readUtf(32767), ConnectionEventType.values()[buf.readInt()]);
    }
}
