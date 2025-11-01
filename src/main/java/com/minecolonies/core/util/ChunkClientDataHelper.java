package com.minecolonies.core.util;

import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.core.event.ClientChunkUpdatedEvent;
import com.minecolonies.api.util.ChunkCapData;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.minecolonies.api.colony.IColony.CLOSE_COLONY_CAP;

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
     * @param chunkCap the capability to add.
     */
    public static void addCapData(final ChunkCapData chunkCap)
    {
        chunkCapsToAdd.add(chunkCap);
    }

    /**
     * Applies saved data for late loading on client side
     *
     * @param chunk the chunk to apply it to.
     */
    public static void applyLate(final LevelChunk chunk)
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
    public static void applyCap(final ChunkCapData chunkCapData, final LevelChunk chunk)
    {
        final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null).orElseGet(null);
        if (cap != null)
        {
            cap.setOwningColony(chunkCapData.getOwningColony(), chunk);
            cap.setStaticColonyClaim(chunkCapData.getStaticColonyClaim());
        }

        MinecraftForge.EVENT_BUS.post(new ClientChunkUpdatedEvent(chunk));
    }
}
