package com.minecolonies.api.colony.requestsystem.token;

import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Factory for the standard request token.
 */
public class StandardTokenFactory implements ITokenFactory<UUID, StandardToken>
{

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_MSB = "Id_MSB";
    private static final String NBT_LSB = "Id_LSB";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    /**
     * Method to get the request type this factory can produce.
     *
     * @return The type of request this factory can produce.
     */
    @NotNull
    @Override
    public Class<? extends StandardToken> getFactoryOutputType()
    {
        return StandardToken.class;
    }

    /**
     * Used to determine which type of request this can produce.
     *
     * @return The class that represents the Type of Request this can produce.
     */
    @NotNull
    @Override
    public Class<? extends UUID> getFactoryInputType()
    {
        return UUID.class;
    }

    /**
     * Method to serialize a given constructable.
     *
     * @param controller The controller that can be used to serialize complicated types.
     * @param request    The request to serialize.
     * @return The serialized data of the given requets.
     */
    @NotNull
    @Override
    public NBTTagCompound serialize(@NotNull IFactoryController controller, @NotNull StandardToken request)
    {
        NBTTagCompound compound = new NBTTagCompound();

        compound.setLong(NBT_LSB, request.getIdentifier().getLeastSignificantBits());
        compound.setLong(NBT_MSB, request.getIdentifier().getMostSignificantBits());

        return compound;
    }

    /**
     * Method to deserialize a given constructable.
     *
     * @param controller The controller that can be used to deserialize complicated types.
     * @param nbt        The data of the request that should be deserialized.
     * @return The request that corresponds with the given data in the nbt
     */
    @NotNull
    @Override
    public StandardToken deserialize(@NotNull IFactoryController controller, @NotNull NBTTagCompound nbt)
    {
        UUID id = new UUID(nbt.getLong(NBT_MSB), nbt.getLong(NBT_LSB));

        return new StandardToken(id);
    }

    /**
     * Method to get a new instance of a token given the input and token.
     *
     * @param input The input to build a new token for.
     * @return The new output instance for a given input.
     */
    @NotNull
    @Override
    public StandardToken getNewInstance(@NotNull UUID input)
    {
        return new StandardToken(input);
    }
}
