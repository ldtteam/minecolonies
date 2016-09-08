package com.minecolonies.network;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;

import java.util.UUID;

public class PacketUtils
{
    /**
     * Method for writing a UUID in a {@link ByteBuf}
     *
     * @param buf  Buf to write in
     * @param uuid UUID to write
     */
    public static void writeUUID(@Nonnull ByteBuf buf, @Nonnull UUID uuid)
    {
        buf.writeLong(uuid.getLeastSignificantBits());
        buf.writeLong(uuid.getMostSignificantBits());
    }

    /**
     * Method to read a UUID from a {@link ByteBuf}
     *
     * @param buf Buf to read from
     * @return The read UUID
     */
    @Nonnull
    public static UUID readUUID(@Nonnull ByteBuf buf)
    {
        long lsb = buf.readLong();
        long msb = buf.readLong();
        return new UUID(msb, lsb);
    }
}
