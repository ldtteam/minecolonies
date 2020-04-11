package com.minecolonies.coremod.util;

import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.util.Log;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;

/**
 * Helper class for late applying caps on client side, due to loading order
 */
public class ChunkClientDataHelper
{
    /**
     * The later to apply chunks
     */
    private static final List<ChunkCapData> chunkCapsToAdd = new ArrayList<>();

    /**
     * Adds a colony chunk data entry
     *
     * @param chunkCap
     */
    public static void addCapData(final ChunkCapData chunkCap)
    {
        chunkCapsToAdd.add(chunkCap);
    }

    /**
     * Applies saved data for late loading on client side
     */
    public static void applyLate(final Chunk chunk)
    {
        if (chunkCapsToAdd.isEmpty())
        {
            return;
        }

        final Iterator<ChunkCapData> iterator = chunkCapsToAdd.iterator();

        while (iterator.hasNext())
        {
            final ChunkCapData chunkCapData = iterator.next();
            if (chunk.getPos().x == chunkCapData.x && chunk.getPos().z == chunkCapData.z)
            {
                Log.getLogger().info("Loading cached data for chunk:" + chunk.getPos());
                applyCap(chunkCapData, chunk);
                iterator.remove();
            }
        }
    }

    /**
     * Applies the data tuple to the respective chunk
     *
     * @param chunkCapData colony data to apply
     * @param chunk        the chunk to apply to
     */
    public static void applyCap(final ChunkCapData chunkCapData, final Chunk chunk)
    {
        final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null).orElseGet(null);
        if (cap != null)
        {
            cap.setOwningColony(chunkCapData.owningColony, chunk);
            cap.setCloseColonies(chunkCapData.closeColonies);
        }
    }
}
