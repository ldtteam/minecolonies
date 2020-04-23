package com.minecolonies.coremod.colony.requestsystem.management.manager.wrapped;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.data.IDataStoreManager;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolverProvider;
import com.minecolonies.api.colony.requestsystem.resolver.player.IPlayerRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.retrying.IRetryingRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * Wrapper class for a Manager.
 * Subclasses of this have custom behaviour on at least one method.
 */
public abstract class AbstractWrappedRequestManager implements IRequestManager
{
    @NotNull
    protected final IStandardRequestManager wrappedManager;

    public AbstractWrappedRequestManager(@NotNull final IStandardRequestManager wrappedManager)
    {
        this.wrappedManager = wrappedManager;
    }

    /**
     * The colony this manager manages the requests for.
     *
     * @return The colony this manager manages the requests for.
     */
    @NotNull
    @Override
    public IColony getColony()
    {
        return wrappedManager.getColony();
    }

    /**
     * Method used to get the FactoryController of the RequestManager.
     *
     * @return The FactoryController of this RequestManager.
     */
    @NotNull
    @Override
    public IFactoryController getFactoryController()
    {
        return wrappedManager.getFactoryController();
    }

    /**
     * Method to create a request for a given object
     *
     * @param requester The requester.
     * @param object    The Object that is being requested.
     * @return The token representing the request.
     *
     * @throws IllegalArgumentException is thrown when this manager cannot produce a request for the given types.
     */
    @NotNull
    @Override
    public <T extends IRequestable> IToken<?> createRequest(@NotNull final IRequester requester, @NotNull final T object) throws IllegalArgumentException
    {
        return wrappedManager.createRequest(requester, object);
    }

    /**
     * Method used to assign a request to a resolver.
     *
     * @param token The token of the request to assign.
     * @throws IllegalArgumentException when the token is not registered to a request, or is already assigned to a resolver.
     */
    @Override
    public void assignRequest(@NotNull final IToken token) throws IllegalArgumentException
    {
        wrappedManager.assignRequest(token);
    }

    /**
     * Method used to create and immediately assign a request.
     *
     * @param requester The requester of the requestable.
     * @param object    The requestable
     * @return The token that represents the request.
     *
     * @throws IllegalArgumentException when either createRequest or assignRequest have thrown an IllegalArgumentException
     */
    @NotNull
    @Override
    public <T extends IRequestable> IToken<?> createAndAssignRequest(@NotNull final IRequester requester, @NotNull final T object) throws IllegalArgumentException
    {
        final IToken<?> token = createRequest(requester, object);
        assignRequest(token);
        return token;
    }

    @Override
    public IToken<?> reassignRequest(@NotNull final IToken<?> token, @NotNull final Collection<IToken<?>> resolverTokenBlackList) throws IllegalArgumentException
    {
        return wrappedManager.reassignRequest(token, resolverTokenBlackList);
    }

    /**
     * Method to get a request for a given token.
     *
     * @param token The token to get a request for.
     * @return The request of the given type for that token.
     *
     * @throws IllegalArgumentException when either their is no request with that token, or the token does not produce a request of the given type T.
     */
    @Nullable
    @Override
    public IRequest<?> getRequestForToken(@NotNull final IToken<?> token) throws IllegalArgumentException
    {
        return wrappedManager.getRequestHandler().getRequestOrNull(token);
    }

    /**
     * Method to get a resolver from its token.
     *
     * @param token@return The resolver registered with the given token.
     * @throws IllegalArgumentException when the token is unknown.
     */
    @NotNull
    @Override
    public IRequestResolver<?> getResolverForToken(@NotNull final IToken<?> token) throws IllegalArgumentException
    {
        return wrappedManager.getResolverForToken(token);
    }

    /**
     * Method to get a resolver for a given request.
     *
     * @param requestToken The token of the request to get resolver for.
     * @return Null if the request is not yet resolved, or else the assigned resolver.
     *
     * @throws IllegalArgumentException Thrown when the token is unknown.
     */
    @Nullable
    @Override
    public IRequestResolver<?> getResolverForRequest(@NotNull final IToken<?> requestToken) throws IllegalArgumentException
    {
        return wrappedManager.getResolverForRequest(requestToken);
    }

    /**
     * Method to update the state of a given request.
     *
     * @param token The token that represents a given request to update.
     * @param state The new state of that request.
     * @throws IllegalArgumentException when the token is unknown to this manager.
     */
    @Override
    public void updateRequestState(@NotNull final IToken<?> token, @NotNull final RequestState state) throws IllegalArgumentException
    {
        wrappedManager.updateRequestState(token, state);
    }

    @Override
    public void overruleRequest(@NotNull final IToken<?> token, @Nullable final ItemStack stack) throws IllegalArgumentException
    {
        wrappedManager.overruleRequest(token, stack);
    }

    /**
     * Method used to indicate to this manager that a new Provider has been added to the colony.
     *
     * @param provider The new provider.
     * @throws IllegalArgumentException is thrown when a provider with the same token is already registered.
     */
    @Override
    public void onProviderAddedToColony(@NotNull final IRequestResolverProvider provider) throws IllegalArgumentException
    {
        wrappedManager.onProviderAddedToColony(provider);
    }

    /**
     * Method used to indicate to this manager that Provider has been removed from the colony.
     *
     * @param provider The removed provider.
     * @throws IllegalArgumentException is thrown when no provider with the same token is registered.
     */
    @Override
    public void onProviderRemovedFromColony(@NotNull final IRequestResolverProvider provider) throws IllegalArgumentException
    {
        wrappedManager.onProviderRemovedFromColony(provider);
    }

    @NotNull
    @Override
    public IPlayerRequestResolver getPlayerResolver()
    {
        return wrappedManager.getPlayerResolver();
    }

    @NotNull
    @Override
    public IRetryingRequestResolver getRetryingRequestResolver()
    {
        return wrappedManager.getRetryingRequestResolver();
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        return wrappedManager.serializeNBT();
    }

    @Override
    public void deserializeNBT(final CompoundNBT nbt)
    {
        wrappedManager.deserializeNBT(nbt);
    }

    @Override
    public void tick()
    {
        wrappedManager.tick();
    }

    @NotNull
    @Override
    public IDataStoreManager getDataStoreManager()
    {
        return wrappedManager.getDataStoreManager();
    }

    @Override
    public void reset()
    {
        wrappedManager.reset();
    }

    @Override
    public boolean isDirty()
    {
        return wrappedManager.isDirty();
    }

    @Override
    public void setDirty(final boolean isDirty)
    {
        wrappedManager.setDirty(isDirty);
    }

    @Override
    public void markDirty()
    {
        wrappedManager.markDirty();
    }

    @Override
    public void onColonyUpdate(@NotNull final Predicate<IRequest> shouldTriggerReassign)
    {
        throw new UnsupportedOperationException("This method cannot be used by Wrapped Request Managers!");
    }

    @Override
    public Logger getLogger()
    {
        return wrappedManager.getLogger();
    }
}
