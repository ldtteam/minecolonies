package com.minecolonies.api.colony.requestsystem.token;

import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Internal implementation of the IToken interface.
 * Uses UUID to store the ID of the request.
 */
public class StandardToken implements IToken<UUID, NBTTagCompound>
{

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_MSB = "Id_MSB";
    private static final String NBT_LSB = "Id_LSB";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    @NotNull
    private UUID id;

    /**
     * Creates a new token with a random id.
     */
    public StandardToken()
    {
        this(UUID.randomUUID());
    }

    /**
     * Creates a new token with the given id.
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
    public NBTTagCompound serializeNBT()
    {
        final NBTTagCompound compound = new NBTTagCompound();

        compound.setLong(NBT_LSB, id.getLeastSignificantBits());
        compound.setLong(NBT_MSB, id.getMostSignificantBits());

        return compound;
    }

    @Override
    public void deserializeNBT(final NBTTagCompound nbt)
    {
        this.id = new UUID(nbt.getLong(NBT_MSB), nbt.getLong(NBT_LSB));
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

        final IToken that = (IToken) o;

        return id.equals(that.getIdentifier());
    }

    @Override
    public String toString()
    {
        return "StandardToken{" +
                 "id=" + id +
                 '}';
    }
}
