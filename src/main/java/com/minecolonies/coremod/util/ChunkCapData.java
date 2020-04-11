package com.minecolonies.coremod.util;

import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper class for chunk pos and
 */
public class ChunkCapData
{
    /**
     * Chunk coordinates
     */
    public final int x;
    public final int z;

    public final int           owningColony;
    public final List<Integer> closeColonies;

    public ChunkCapData(final int x, final int z, final int owningColony, final List<Integer> closeColonies)
    {
        this.x = x;
        this.z = z;
        this.owningColony = owningColony;
        this.closeColonies = closeColonies;
    }

    /**
     * Writes data to a buffer
     *
     * @param buf
     */
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(x);
        buf.writeInt(z);
        buf.writeInt(owningColony);
        buf.writeInt(closeColonies.size());
        for (final Integer id : closeColonies)
        {
            buf.writeInt(id);
        }
    }

    /**
     * Creates the data from a buffer
     *
     * @param buffer
     * @return
     */
    public static ChunkCapData fromBytes(@NotNull final PacketBuffer buffer)
    {
        int x = buffer.readInt();
        int z = buffer.readInt();
        int owning = buffer.readInt();
        int size = buffer.readInt();

        List<Integer> closeColonies = new ArrayList<>();
        for (int i = 0; i < size; i++)
        {
            closeColonies.add(buffer.readInt());
        }

        return new ChunkCapData(x, z, owning, closeColonies);
    }
}
