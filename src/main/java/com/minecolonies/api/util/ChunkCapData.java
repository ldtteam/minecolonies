package com.minecolonies.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Wrapper class for chunk pos and colony cap data
 */
public class ChunkCapData
{
    /**
     * Chunk coordinates
     */
    public final int x;
    public final int z;

    /**
     * The colony owning this chunk.
     */
    private final int owningColony;

    /**
     * The close colonies to this chunk.
     */
    private final List<Integer> staticColonyClaim;

    /**
     * Full claim data of buildings.
     */
    private final Map<Integer, Set<BlockPos>> allClaimBuildings;

    public ChunkCapData(final int x, final int z)
    {
        this.x = x;
        this.z = z;
        this.owningColony = 0;
        this.staticColonyClaim = new ArrayList<>();
        this.allClaimBuildings = new HashMap<>();
    }


    public ChunkCapData(final int x, final int z, final int owningColony, final List<Integer> staticColonyClaim, final @NotNull Map<Integer, Set<BlockPos>> allClaimingBuildings)
    {
        this.x = x;
        this.z = z;
        this.owningColony = owningColony;
        this.staticColonyClaim = staticColonyClaim;
        this.allClaimBuildings = allClaimingBuildings;
    }

    /**
     * Writes data to a buffer
     *
     * @param buf the buffer to write it to.
     */
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(x);
        buf.writeInt(z);
        buf.writeInt(owningColony);
        buf.writeInt(staticColonyClaim.size());
        for (final Integer id : staticColonyClaim)
        {
            buf.writeInt(id);
        }
        //todo in the future, when we need it, we can also sync the claimed buildings. We don't do this atm as its a bunch of data.
    }

    /**
     * Creates the data from a buffer
     *
     * @param buffer the bytebuffer.
     * @return the cap data.
     */
    public static ChunkCapData fromBytes(@NotNull final FriendlyByteBuf buffer)
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

        return new ChunkCapData(x, z, owning, closeColonies, new HashMap<>());
    }

    /**
     * Getter for static claim.
     * @return list.
     */
    public List<Integer> getStaticColonyClaim()
    {
        return staticColonyClaim;
    }

    /**
     * Getter for owning colony id.
     * @return the id.
     */
    public int getOwningColony()
    {
        return owningColony;
    }

    /**
     * Getter for all claimed buildings.
     * @return the map.
     */
    public Map<Integer, Set<BlockPos>> getAllClaimingBuildings()
    {
        return allClaimBuildings;
    }
}
