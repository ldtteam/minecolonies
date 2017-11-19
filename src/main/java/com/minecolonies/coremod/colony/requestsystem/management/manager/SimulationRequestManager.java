package com.minecolonies.coremod.colony.requestsystem.management.manager;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
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
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * ------------ Class not Documented ------------
 */
final class SimulationRequestManager implements IStandardRequestManager
{



    @NotNull
    @Override
    public BiMap<IToken, IRequestResolverProvider> getProviderBiMap()
    {
        return null;
    }

    @NotNull
    @Override
    public BiMap<IToken, IRequestResolver> getResolverBiMap()
    {
        return null;
    }

    @NotNull
    @Override
    public BiMap<IToken, IRequest> getRequestBiMap()
    {
        return null;
    }

    @NotNull
    @Override
    public Map<IToken, ImmutableCollection<IToken>> getProviderResolverMap()
    {
        return null;
    }

    @NotNull
    @Override
    public Map<IToken, Set<IToken>> getResolverRequestMap()
    {
        return null;
    }

    @NotNull
    @Override
    public Map<IToken, IToken> getRequestResolverMap()
    {
        return null;
    }

    @NotNull
    @Override
    public Map<TypeToken, Collection<IRequestResolver>> getRequestClassResolverMap()
    {
        return null;
    }

    @NotNull
    @Override
    public boolean isSimulation()
    {
        return false;
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
        return null;
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
        return null;
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
    public <T extends IRequestable> IToken createRequest(@NotNull final IRequester requester, @NotNull final T object) throws IllegalArgumentException
    {
        return null;
    }

    /**
     * Method used to assign a request to a resolver.
     *
     * @param token The token of the request to assign.
     * @throws IllegalArgumentException when the token is not registered to a request, or is already assigned to a resolver.
     */
    @NotNull
    @Override
    public void assignRequest(@NotNull final IToken token) throws IllegalArgumentException
    {

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
    public <T extends IRequestable> IToken createAndAssignRequest(@NotNull final IRequester requester, @NotNull final T object) throws IllegalArgumentException
    {
        return null;
    }

    /**
     * Method used to reassign a given request.
     *
     * @param token The token of the request that should be reassigned.
     * @return The token of the resolver that has gotten the assignment, null if none was found.
     *
     * @throws IllegalArgumentException when the token is not known to this manager.
     */
    @Nullable
    @Override
    public IToken reassignRequest(@NotNull final IToken token, @NotNull final Collection<IToken> resolverTokenBlackList) throws IllegalArgumentException
    {
        return null;
    }

    /**
     * Method to get a request for a given token.
     *
     * @param token The token to get a request for.
     * @return The request of the given type for that token.
     *
     * @throws IllegalArgumentException when the token does not produce a request of the given type T.
     */
    @Nullable
    @Override
    public <T extends IRequestable> IRequest<T> getRequestForToken(@NotNull final IToken token) throws IllegalArgumentException
    {
        return null;
    }

    /**
     * Method to get a resolver from its token.
     *
     * @param token@return The resolver registered with the given token.
     * @throws IllegalArgumentException when the token is unknown.
     */
    @NotNull
    @Override
    public <T extends IRequestable> IRequestResolver<T> getResolverForToken(@NotNull final IToken token) throws IllegalArgumentException
    {
        return null;
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
    public <T extends IRequestable> IRequestResolver<T> getResolverForRequest(@NotNull final IToken requestToken) throws IllegalArgumentException
    {
        return null;
    }

    /**
     * Method to update the state of a given request.
     *
     * @param token The token that represents a given request to update.
     * @param state The new state of that request.
     * @throws IllegalArgumentException when the token is unknown to this manager.
     */
    @NotNull
    @Override
    public void updateRequestState(@NotNull final IToken token, @NotNull final RequestState state) throws IllegalArgumentException
    {

    }

    /**
     * Method used to overrule a request.
     * Updates the state and sets the delivery if applicable.
     *
     * @param token The token of the request that is being overruled.
     * @param stack The stack that should be treated as delivery. If no delivery is possible, this is null.
     * @throws IllegalArgumentException Thrown when either token does not match to a request.
     */
    @Override
    public void overruleRequest(@NotNull final IToken token, @Nullable final ItemStack stack) throws IllegalArgumentException
    {

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

    }

    /**
     * Get the player resolve.
     *
     * @return the player resolver object.
     */
    @NotNull
    @Override
    public IPlayerRequestResolver getPlayerResolver()
    {
        return null;
    }

    /**
     * Get the retrying request resolver.
     *
     * @return The retrying request resolver.
     */
    @NotNull
    @Override
    public IRetryingRequestResolver getRetryingRequestResolver()
    {
        return null;
    }

    /**
     * Like the old updateEntity(), except more generic.
     */
    @Override
    public void update()
    {

    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        return null;
    }

    @Override
    public void deserializeNBT(final NBTTagCompound nbt)
    {

    }
}
