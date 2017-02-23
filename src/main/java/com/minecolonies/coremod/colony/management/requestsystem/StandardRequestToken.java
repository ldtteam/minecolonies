package com.minecolonies.coremod.colony.management.requestsystem;

import com.minecolonies.coremod.colony.management.requestsystem.api.IRequestToken;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Internal implementation of the IRequestToken interface.
 */
public class StandardRequestToken implements IRequestToken<UUID, NBTTagCompound> {

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_MSB = "Id_MSB";
    private static final String NBT_LSB = "Id_LSB";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    @NotNull
    private UUID id;

    /**
     * Creates a new token with a random id.
     */
    public StandardRequestToken() {
        this(UUID.randomUUID());
    }

    /**
     * Creates a new token with the given id.
     * @param id
     */
    public StandardRequestToken(@NotNull UUID id) {
        this.id = id;
    }

    /**
     * The identifier used to represent a request.
     *
     * @return The identifier of the request that this token represents.
     */
    @Override
    public UUID getIdentifier() {
        return id;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();

        compound.setLong(NBT_LSB, id.getLeastSignificantBits());
        compound.setLong(NBT_MSB, id.getMostSignificantBits());

        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.id = new UUID(nbt.getLong(NBT_MSB), nbt.getLong(NBT_LSB));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IRequestToken)) return false;

        IRequestToken that = (IRequestToken) o;

        return id.equals(that.getIdentifier());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
