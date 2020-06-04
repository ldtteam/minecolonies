package com.minecolonies.api.colony.requestsystem.token;

import org.jetbrains.annotations.NotNull;
import java.util.UUID;

/**
 * Internal implementation of the IToken interface.
 * Uses UUID to store the ID of the request.
 */
public class StandardToken implements IToken<UUID>
{
    @NotNull
    private final UUID id;

    /**
     * Creates a new token with a random id.
     */
    public StandardToken()
    {
        this(UUID.randomUUID());
    }

    /**
     * Creates a new token with the given id.
     *
     * @param id the UUID to create it from.
     */
    public StandardToken(@NotNull final UUID id)
    {
        this.id = id;
    }

    /**
     * The identifier used to represent a request.
     *
     * @return The identifier of the request that this token represents.
     */
    @Override
    public UUID getIdentifier()
    {
        return id;
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof IToken))
        {
            return false;
        }

        final IToken<?> that = (IToken<?>) o;

        return id.equals(that.getIdentifier());
    }

    @Override
    public String toString()
    {
        return "StandardToken{" + "id=" + id + '}';
    }
}
