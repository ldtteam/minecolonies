package com.minecolonies.coremod.colony.management.requestsystem.api.token;

import com.minecolonies.coremod.colony.management.requestsystem.api.factory.IFactory;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

/**
 * Marker interface used to specify a factory for requesttokens.
 * Restricts the output type of the general factory interface to IRequestToken
 * @param <T> The type of requesttoken.
 * @param <RT> The requesttoken type.
 */
public interface IRequestTokenFactory<T, RT extends IRequestToken<T,NBTTagCompound>> extends IFactory<T, RT> {

    /**
     * Method to get a new instance of the output given the input and additional context data.
     *
     * @param t       The input to build a new output for.
     * @param context The context of the token.
     * @return The new output instance for a given input.
     * @throws IllegalArgumentException is thrown when the factory cannot produce a new instance out of the given context and input.
     */
    @NotNull
    @Override
    default RT getNewInstance(@NotNull T t, @NotNull Object... context) throws IllegalArgumentException {
        if (context.length != 0)
            throw new IllegalArgumentException("Unsupported context - Too many parameters. None is needed.!");

        return this.getNewInstance(t);
    }

    /**
     * Method to get a new instance of a token given the input and token.
     *
     * @param input The input to build a new token for.
     * @return The new output instance for a given input.
     */
    @NotNull
    RT getNewInstance(@NotNull T input);

}
