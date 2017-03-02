package com.minecolonies.coremod.colony.management.requestsystem.api.resolver;

import com.minecolonies.coremod.colony.management.requestsystem.api.factory.IFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Created by marcf on 2/27/2017.
 */
public interface IRequestResolverFactory<Resolver extends IRequestResolver> extends IFactory<Resolver, Resolver> {

    /**
     * Method to get the request type this factory can produce.
     *
     * @return The type of request this factory can produce.
     */
    @NotNull
    @Override
    default Class<? extends Resolver> getFactoryOutputType() {
        return getFactoryType();
    }

    /**
     * Used to determine which type of request this can produce.
     *
     * @return The class that represents the Type of Request this can produce.
     */
    @NotNull
    @Override
    default Class<? extends Resolver> getFactoryInputType() {
        return getFactoryType();
    }

    /**
     * Used to determine which type of requestresolver this can produce.
     *
     * @return The class that represents the Type of Requestresolver this can produce.
     */
    @NotNull
    Class<? extends Resolver> getFactoryType();
}
