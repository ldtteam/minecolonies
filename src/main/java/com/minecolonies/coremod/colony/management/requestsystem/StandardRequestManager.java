package com.minecolonies.coremod.colony.management.requestsystem;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.minecolonies.coremod.colony.IColony;
import com.minecolonies.coremod.colony.management.requestsystem.api.IRequestManager;
import com.minecolonies.coremod.colony.management.requestsystem.api.RequestState;
import com.minecolonies.coremod.colony.management.requestsystem.api.factory.IFactoryController;
import com.minecolonies.coremod.colony.management.requestsystem.api.location.ILocatable;
import com.minecolonies.coremod.colony.management.requestsystem.api.request.IRequest;
import com.minecolonies.coremod.colony.management.requestsystem.api.resolver.IRequestResolver;
import com.minecolonies.coremod.colony.management.requestsystem.api.resolver.IRequestResolverProvider;
import com.minecolonies.coremod.colony.management.requestsystem.api.token.IToken;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by marcf on 2/27/2017.
 */
public class StandardRequestManager implements IRequestManager {

    @NotNull
    private final BiMap<IToken, IRequestResolverProvider> providerBiMap = HashBiMap.create();
    
    /**
     * BiMap that holds unique token to resolver lookup.
     */
    @NotNull
    private final BiMap<IToken, IRequestResolver> resolverBiMap = HashBiMap.create();

    /**
     * BiMap that holds unique token to request lookup.
     */
    @NotNull
    private final BiMap<IToken, IRequest> requestBiMap = HashBiMap.create();

    /**
     * Map that holds the resolvers that are linked to a given provider.
     */
    @NotNull
    private final HashMap<IToken, ImmutableCollection<IToken>> providerResolverMap = new HashMap<> ();

    /**
     * Map that holds the requests that are linked to a given resolver.
     */
    @NotNull
    private final HashMap<IToken, Collection<IToken>> resolverRequestMap = new HashMap<>();


    @NotNull
    private IColony colony;

    /**
     * The colony this manager manages the requests for.
     *
     * @return The colony this manager manages the requests for.
     */
    @NotNull
    @Override
    public IColony getColony() {
        return null;
    }

    /**
     * Method used to get the FactoryController of the RequestManager.
     *
     * @return The FactoryController of this RequestManager.
     */
    @NotNull
    @Override
    public IFactoryController getFactoryController() {
        return null;
    }

    /**
     * Method to create a request for a given object
     *
     * @param requester The requester.
     * @param object    The Object that is being requested.
     * @return The token representing the request.
     * @throws IllegalArgumentException is thrown when this manager cannot produce a request for the given types.
     */
    @NotNull
    @Override
    public <T> IToken createRequest(@NotNull ILocatable requester, @NotNull T object) throws IllegalArgumentException {
        return null;
    }

    /**
     * Method to get a request for a given token.
     *
     * @param token The token to get a request for.
     * @return The request of the given type for that token.
     * @throws IllegalArgumentException when either their is no request with that token, or the token does not produce a request of the given type T.
     */
    @NotNull
    @Override
    public <T> IRequest<T> getRequestForToken(@NotNull IToken token) throws IllegalArgumentException {
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
    public void updateRequestState(@NotNull IToken token, @NotNull RequestState state) throws IllegalArgumentException {

    }

    /**
     * Method used to indicate to this manager that a new Provider has been added to the colony.
     *
     * @param provider The new provider.
     */
    @Override
    public void onProviderAddedToColony(@NotNull IRequestResolverProvider provider) throws IllegalArgumentException {
        if (providerResolverMap.containsKey(provider.getToken()))
            throw new IllegalArgumentException("The given token: " + provider.getResolvers() + "is already registered.");

        ImmutableList.Builder<IRequestResolver> resolverListBuilder = new ImmutableList.Builder<>();
        resolverListBuilder.addAll(provider.getResolvers());

        providerResolverMap.put(provider.getToken(), resolverListBuilder.build());
    }

    /**
     * Method used to indicate to this manager that Provider has been removed from the colony.
     *
     * @param provider The removed provider.
     */
    @Override
    public void onProviderRemovedFromColony(@NotNull IRequestResolverProvider provider) throws IllegalArgumentException {
        List<IToken> providerRequests =
    }

    /**
     * Method used to serialize the current request system to NBT.
     * @return The NBTData that describes the current request system
     */
    @Override
    public NBTTagCompound serializeNBT() {
        return null;
    }

    /**
     * Method used to deserialize the data inside the given nbt tag into this request system.
     * @param nbt The data to deserialize.
     */
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {

    }

    /**
     * Class used to handle the inner workings of the request system with regards to providers.
     */
    private final static class ProviderHandler {

        /**
         * Method used to get a provider from a token.
         * @param token The token to get the provider form.
         * @return The provider that corresponds to the given token
         * @throws IllegalArgumentException when no provider is not registered with the given token.
         */
        private static IRequestResolverProvider getProvider(StandardRequestManager manager, IToken token) throws IllegalArgumentException {
            if (!manager.providerBiMap.containsKey(token))
                throw new IllegalArgumentException("The given token for a provider is not registered");

            return manager.providerBiMap.get(token);
        }

        /**
         * Method used to get the registered resolvers for a given provider.
         * @param manager The manager to pull the data from.
         * @param token The token of the provider you are requesting the resolvers for.
         * @return The registered resolvers that belong to the given provider.
         * @throws IllegalArgumentException when the token is not belonging to a registered provider.
         */
        private static ImmutableCollection<IToken> getRegisteredResolvers(StandardRequestManager manager, IToken token) throws IllegalArgumentException {
            //Check if the token is registered.
            getProvider(manager, token);

            return manager.providerResolverMap.get(token);
        }

        /**
         * Method used to get the registered resolvers for a given provider.
         * @param manager The manager to pull the data from.
         * @param provider The provider you are requesting the resolvers for.
         * @return The registered resolvers that belong to the given provider.
         * @throws IllegalArgumentException when the token is not belonging to a registered provider.
         */
        private static ImmutableCollection<IToken> getRegisteredResolvers(StandardRequestManager manager, IRequestResolverProvider provider) throws IllegalArgumentException {
            //Check if the token is registered.
            getProvider(manager, provider.getToken());

            return manager.providerResolverMap.get(provider.getToken());
        }

        /**
         * Method used to register a provider to a given manager.
         * @param manager The manager to register them to.
         * @param provider The provider that provides the resolvers.
         * @throws IllegalArgumentException is thrown when a provider is already registered.
         */
        private static void registerProvider(StandardRequestManager manager, IRequestResolverProvider provider) throws IllegalArgumentException {
            if (manager.providerBiMap.containsKey(provider.getToken()) ||
                    manager.providerBiMap.containsValue(provider))
                throw new IllegalArgumentException("The given provider is already registered");

            manager.providerBiMap.put(provider.getToken(), provider);

            ImmutableList.Builder<IToken> resolverListBuilder = new ImmutableList.Builder<>();
            resolverListBuilder.addAll(ResolverHandler.registerResolvers(manager, provider.getResolvers()));

            manager.providerResolverMap.put(provider.getToken(), resolverListBuilder.build());
        }

        private static void removeProvider(StandardRequestManager manager, IToken token) throws IllegalArgumentException {
            IRequestResolverProvider provider = getProvider(manager, token);

            ImmutableCollection<IToken> assignedResolvers = getRegisteredResolvers(manager, token);
            for(IToken resolverToken : assignedResolvers) {

            }

        }

        private static void removeProvider(StandardRequestManager manager, IRequestResolverProvider provider) throws IllegalArgumentException {
            IRequestResolverProvider registeredProvider = getProvider(manager, provider.getToken());

            if (!registeredProvider.equals(provider))
                throw new IllegalArgumentException("The given providers token is registered to a different provider!");


        }
    }

    /**
     * Class used to handle the inner workings of the request system with regards to resolvers.
     */
    private final static class ResolverHandler {

        /**
         * Method to get a resolver from a given token.
         *
         * <p>
         *     Is only used internally.
         *     Querries the resolverBiMap to get the resolver for a given Token.
         * </p>
         *
         * @param manager The manager to retrieve the resolver from.
         * @param token The token of the resolver to look up.
         * @return The resolver registered with the given token.
         * @throws IllegalArgumentException is thrown when the given token is not registered to any IRequestResolver
         */
        private static IRequestResolver getResolver(StandardRequestManager manager, IToken token) throws IllegalArgumentException {
            if (!manager.resolverBiMap.containsKey(token))
                throw new IllegalArgumentException("The given token for a resolver is not known to this manager!");

            return manager.resolverBiMap.get(token);
        }

        /**
         * Method used to register a resolver.
         *
         * <p>
         *     Is only used internally.
         *     The method modifies the resolverBiMap that is used to track which resolver are registered.
         * </p>
         *
         * @param manager The manager to register the resolver to.
         * @param resolver The resolver to register
         * @return The token of the newly registered resolver
         * @throws IllegalArgumentException is thrown when either the token attached to the resolver is already registered or the resolver is already registered with a different token
         */
        private static IToken registerResolver(StandardRequestManager manager, IRequestResolver resolver) throws IllegalArgumentException {
            if (manager.resolverBiMap.containsKey(resolver.getToken()))
                throw new IllegalArgumentException("The token attached to this resolver is already registered. Cannot register twice!");

            if (manager.resolverBiMap.containsValue(resolver))
                throw new IllegalArgumentException("The given resolver is already registered with a different token. Cannot register twice!");

            manager.resolverBiMap.put(resolver.getToken(), resolver);

            return resolver.getToken();
        }

        /**
         * Method used to register multiple resolvers simultaneously
         *
         * <p>
         *     Is only used internally.
         *     The method modifies the resolverBiMap that is used to track resolvers.
         * </p>
         * 
         * @param manager The manager to register the resolvers to.
         * @param resolvers The resolvers to register.
         * @return The tokens of the resolvers that when registered.
         * @throws IllegalArgumentException is thrown when an IllegalArgumentException is thrown by the registerResolver method for any of the given Resolvers.
         */
        private static Collection<IToken> registerResolvers(StandardRequestManager manager, IRequestResolver... resolvers) throws IllegalArgumentException {
            return Arrays.stream(resolvers).map(resolver -> registerResolver(manager, resolver)).collect(Collectors.toList());
        }

        /**
         * Method used to register multiple resolvers simultaneously
         *
         * <p>
         *     Is only used internally.
         *     The method modifies the resolverBiMap that is used to track resolvers.
         * </p>
         *
         * @param manager The manager to register the resolvers to.
         * @param resolvers The resolvers to register.
         * @return The tokens of the resolvers that when registered.
         * @throws IllegalArgumentException is thrown when an IllegalArgumentException is thrown by the registerResolver method for any of the given Resolvers.
         */
        private static Collection<IToken> registerResolvers(StandardRequestManager manager, Collection<IRequestResolver> resolvers) {
            return resolvers.stream().map(resolver -> registerResolver(manager, resolver)).collect(Collectors.toList());
        }

        /**
         * Method used to remove a registered resolver.
         * 
         * <p>
         *     Is only used internally.
         *     The method modifies the resolverBiMap that is used to track resolvers.
         * </p>
         * 
         * @param manager The manager to remove the resolver from.
         * @param resolver The resolver to remove
         * @throws IllegalArgumentException is thrown when the given resolver is not registered or the token of the given resolver is not registered to the same resolver.
         */
        private static void removeResolver(StandardRequestManager manager, IRequestResolver resolver) throws IllegalArgumentException {
            IRequestResolver registeredResolver = getResolver(manager, resolver.getToken());

            if (!registeredResolver.equals(resolver))
                throw new IllegalArgumentException("The given resolver and the resolver that is registered with its token are not the same.");

            manager.resolverBiMap.remove(registeredResolver.getToken());
        }

        /**
         * Method used to remove a registered resolver.
         *
         * <p>
         *     Is only used internally.
         *     The method modifies the resolverBiMap that is used to track resolvers.
         * </p>
         *
         * @param manager The manager to remove the resolver from.
         * @param token The token of the resolver to remove.
         * @throws IllegalArgumentException is thrown when the given resolver is not registered or the token of the given resolver is not registered to the same resolver.
         */
        private static void removeResolver(StandardRequestManager manager, IToken token) throws IllegalArgumentException {
            if (!manager.resolverBiMap.containsKey(token))
                throw new IllegalArgumentException("The token is unknown to this manager.");

            manager.resolverBiMap.remove(token);
        }

        /**
         * Method used to remove a multiple registered resolvers.
         *
         * <p>
         *     Is only used internally.
         *     The method modifies the resolverBiMap that is used to track resolvers.
         * </p>
         *
         * @param manager The manager to remove the resolver from.
         * @param resolvers The resolvers to remove.
         * @throws IllegalArgumentException is thrown when removeResolver throws an IllegalArgumentException for any of the given resolvers.
         */
        private static void removeResolvers(StandardRequestManager manager, IRequestResolver... resolvers) {
            removeResolvers(manager, Arrays.asList(resolvers));
        }

        /**
         * Method used to remove a multiple registered resolvers.
         *
         * <p>
         *     Is only used internally.
         *     The method modifies the resolverBiMap that is used to track resolvers.
         * </p>
         *
         * @param manager The manager to remove the resolver from.
         * @param resolvers The resolvers to remove.
         * @throws IllegalArgumentException is thrown when removeResolver throws an IllegalArgumentException for any of the given resolvers.
         */
        private static void removeResolvers(StandardRequestManager manager, Iterable<IRequestResolver> resolvers) {
            resolvers.forEach((IRequestResolver resolver) -> {
                removeResolver(manager, resolver);
            });
        }

        /**
         * Method used to add a request to a resolver.
         *
         * <p>
         *     Is only used internally.
         *     The method modifies the resolverRequestMap that is used to track which resolver handles which request.
         * </p>
         *
         * @param manager The manager to modify
         * @param resolver The resolver to add the request to.
         * @param request The request to add to the resolver.
         */
        private static void addRequestToResolver(StandardRequestManager manager, IRequestResolver resolver, IRequest request) {
            if (!manager.resolverRequestMap.containsKey(resolver.getToken()))
                manager.resolverRequestMap.put(resolver.getToken(), new ArrayList<>());

            manager.resolverRequestMap.get(resolver.getToken()).add(request.getToken());
        }

        /**
         * Method used to remove a request from a resolver.
         *
         * <p>
         *     Is only used internally.
         *     The method modifies the resolverRequestMap that is used to track which resolver handles which request.
         * </p>
         *
         * @param manager The manager to modify
         * @param resolver The resolver to remove the given request from.
         * @param request The request to remove.
         * @throws IllegalArgumentException is thrown when the resolver is unknown, or when the given request is not registered to the given resolver.
         */
        private static void removeRequestFromResolver(StandardRequestManager manager, IRequestResolver resolver, IRequest request) throws IllegalArgumentException {
            if (!manager.resolverRequestMap.containsKey(resolver.getToken()))
                throw new IllegalArgumentException("The given resolver is unknown to this Manager");

            if (!manager.resolverRequestMap.get(resolver.getToken()).contains(request.getToken()))
                throw new IllegalArgumentException("The given request is not registered to the given resolver.");

            manager.resolverRequestMap.get(resolver.getToken()).remove(request.getToken());
        }
    }


}
