package com.minecolonies.api.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Class with package utils
 */
public final class PacketUtils
{
    /**
     * Private constructor to hide implicit one.
     */
    private PacketUtils()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Method for writing a UUID in a {@link ByteBuf}.
     *
     * @param buf  Buf to write in.
     * @param uuid UUID to write.
     */
    public static void writeUUID(@NotNull final PacketBuffer buf, @NotNull final UUID uuid)
    {
        buf.writeLong(uuid.getLeastSignificantBits());
        buf.writeLong(uuid.getMostSignificantBits());
    }

    /**
     * Method to read a UUID from a {@link ByteBuf}.
     *
     * @param buf Buf to read from.
     * @return The read UUID.
     */
    @NotNull
    public static UUID readUUID(@NotNull final PacketBuffer buf)
    {
        final long lsb = buf.readLong();
        final long msb = buf.readLong();
        return new UUID(msb, lsb);
    }
}
