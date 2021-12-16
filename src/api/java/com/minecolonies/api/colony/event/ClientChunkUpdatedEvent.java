package com.minecolonies.api.colony.event;

import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.NotNull;

/**
 * This event is fired client-side after the colony chunk capabilities are synched to the player;
 * i.e. there might be an update to which colony claims a particular chunk.  This is fired even if
 * no colony owns the chunk in question, as that may also be interesting; and even if the data did
 * not actually change, as it might still be new to the listener.
 */
public class ClientChunkUpdatedEvent extends Event
{
    private final Chunk chunk;

    /**
     * Constructs a chunk update event.
     *
     * @param chunk The chunk that was updated.
     */
    public ClientChunkUpdatedEvent(@NotNull final Chunk chunk)
    {
        this.chunk = chunk;
    }

    /**
     * Gets the chunk related to the event.
     */
    @NotNull
    public Chunk getChunk()
    {
        return this.chunk;
    }
}
