package com.minecolonies.network;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

public class PacketUtils
{
    public static void writeUUID(ByteBuf buf, UUID uuid)
    {
        buf.writeLong(uuid.getLeastSignificantBits());
        buf.writeLong(uuid.getMostSignificantBits());
    }

    public static UUID readUUID(ByteBuf buf)
    {
        long lsb = buf.readLong();
        long msb = buf.readLong();
        return new UUID(msb, lsb);
    }
}
