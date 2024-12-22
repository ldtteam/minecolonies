package com.minecolonies.core.event;

import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.NotNull;

/**
 * This event is fired client-side after the colony chunk capabilities are synched to the player;
 * i.e. there might be an update to which colony claims a particular chunk.  This is fired even if
 * no colony owns the chunk in question, as that may also be interesting; and even if the data did
 * not actually change, as it might still be new to the listener.
 */
public final class ClientChunkUpdatedEvent extends Event
{
    private final LevelChunk chunk;

    /**
     * Constructs a chunk update event.
     *
     * @param chunk The chunk that was updated.
     */
    public ClientChunkUpdatedEvent(@NotNull final LevelChunk chunk)
    {
        this.chunk = chunk;
    }

    /**
     * Gets the chunk related to the event.
     */
    @NotNull
    public LevelChunk getChunk()
    {
        return this.chunk;
    }
}
