package com.minecolonies.api.colony.requestsystem.token;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.CompoundNBT;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * An abstract implementation of the {@link ITokenFactory} interface that handles serialization etc.
 */
public abstract class AbstractTokenFactory<I> implements ITokenFactory<I, StandardToken>
{
    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    public static final String NBT_MSB = "Id_MSB";
    public static final String NBT_LSB = "Id_LSB";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    @NotNull
    @Override
    public TypeToken<StandardToken> getFactoryOutputType()
    {
        return TypeConstants.STANDARDTOKEN;
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
    public CompoundNBT serialize(@NotNull final IFactoryController controller, @NotNull final StandardToken request)
    {
        final CompoundNBT compound = new CompoundNBT();

        compound.putLong(NBT_LSB, request.getIdentifier().getLeastSignificantBits());
        compound.putLong(NBT_MSB, request.getIdentifier().getMostSignificantBits());

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
    public StandardToken deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt)
    {
        final Long msb = nbt.getLong(NBT_MSB);
        final Long lsb = nbt.getLong(NBT_LSB);

        final UUID id = new UUID(msb, lsb);

        return new StandardToken(id);
    }
}
