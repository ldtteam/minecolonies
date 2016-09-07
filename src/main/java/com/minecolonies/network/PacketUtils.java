package com.minecolonies.network;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PacketUtils
{
    /**
     * Method for writing a UUID in a {@link ByteBuf}
     *
     * @param buf  Buf to write in
     * @param uuid UUID to write
     */
    public static void writeUUID(@NotNull ByteBuf buf, @NotNull UUID uuid)
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
    @NotNull
    public static UUID readUUID(@NotNull ByteBuf buf)
    {
        long lsb = buf.readLong();
        long msb = buf.readLong();
        return new UUID(msb, lsb);
    }
}
