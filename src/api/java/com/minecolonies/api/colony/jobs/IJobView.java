package com.minecolonies.api.colony.jobs;

import com.minecolonies.api.colony.requestsystem.token.IToken;
import net.minecraft.network.PacketBuffer;

import java.util.Set;

public interface IJobView
{
    /**
     * Return a Localization textContent for the Job.
     *
     * @return localization textContent String.
     */
    String getName();

    /**
     * Get a set of async requests connected to this job.
     *
     * @return a set of ITokens.
     */
    Set<IToken<?>> getAsyncRequests();

    /**
     * Deserialize the job from the buffer.
     * @param buffer the buffer to read it from.
     */
    void deserialize(final PacketBuffer buffer);
}
