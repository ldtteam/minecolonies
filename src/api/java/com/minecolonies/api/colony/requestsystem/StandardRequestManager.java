package com.minecolonies.api.colony.requestsystem;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requestable.IRetryable;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolverProvider;
import com.minecolonies.api.colony.requestsystem.resolver.player.IPlayerRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.retrying.IRetryingRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.ReflectionUtils;
import com.minecolonies.api.util.constant.Suppression;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Main class of the request system.
 * Default implementation of the IRequestManager interface.
 * <p>
 * Uses
 */

@SuppressWarnings(Suppression.BIG_CLASS)
public class StandardRequestManager implements IRequestManager
{
    ////---------------------------NBTTags-------------------------\\\\
    private static final String NBT_REQUEST_IDENTITY_MAP = "Request_Identities";
    private static final String NBT_RESOLVER_REQUESTS_ASSIGNMENTS = "Resolver_Requests";
    private static final String NBT_PLAYER = "Player";
    private static final String NBT_RETRYING = "Retrying";

    private static final String NBT_TOKEN = "Token";
    private static final String NBT_ASSIGNMENTS = "Assignments";

    private static final String NBT_REQUEST = "Request";
    ////---------------------------NBTTags-------------------------\\\\

    /**
     * Holds a map from requestable to its corresponding request type.
     */
    private static final BiMap<Class, Class> requestableMappings = HashBiMap.create();

    /**
     * BiMap that holds unique token to provider lookup.
     */
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
    private final Map<IToken, ImmutableCollection<IToken>> providerResolverMap = new HashMap<>();

    /**
     * Map that holds the requests that are linked to a given resolver.
     */
    @NotNull
    private final Map<IToken, Collection<IToken>> resolverRequestMap = new HashMap<>();

    /**
     * Map that holds the resolver that is linked to a given request.
     */
    @NotNull
    private final Map<IToken, IToken> requestResolverMap = new HashMap<>();

    /**
     * Map that holds the class that resolver can resolve. Used during lookup.
     */
    @NotNull
    private final Map<TypeToken, Collection<IRequestResolver>> requestClassResolverMap = new HashMap<>();
    /**
     * The fallback resolver used to resolve directly to the player.
     */
    @NotNull
    private IPlayerRequestResolver                             playerResolver = null;

    /**
     * The fallback resolver used to resolve using retries.
     * Not all requests might support this feature, requests that do should implement {@link IRetryable} on their requestable.
     * Anything that implements {@link IDeliverable} is by definition retryable.
     */
    @NotNull
    private IRetryingRequestResolver retryingResolver = null;
    /**
     * Colony of the manager.
     */
    @NotNull
    private final IColony colony;

    public static void registerRequestableTypeMapping(@NotNull final Class requestableType, @NotNull final Class requestType)
    {
        requestableMappings.put(requestableType, requestType);
    }

    public StandardRequestManager(final IColony colony)
    {
        this.colony = colony;

        final ILocation colonyLocation = getFactoryController().getNewInstance(TypeConstants.ILOCATION, colony.getCenter(), colony.getWorld().provider.getDimension());
        this.playerResolver = getFactoryController().getNewInstance(TypeConstants.PLAYER_REQUEST_RESOLVER, colonyLocation, getFactoryController().getNewInstance(TypeConstants.ITOKEN));
        this.retryingResolver = getFactoryController().getNewInstance(TypeConstants.RETRYING_REQUEST_RESOLVER, this);
        ResolverHandler.registerResolver(this, this.playerResolver);
        ResolverHandler.registerResolver(this, this.retryingResolver);
    }

    /**
     * Constructor for unit tests.
     */
    StandardRequestManager()
    {
        this.colony = null;
        this.playerResolver = null;
        this.retryingResolver = null;
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
        return colony;
    }

    /**
     * Method used to serialize the current request system to NBT.
     *
     * @return The NBTData that describes the current request system
     */
    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound systemCompound = new NBTTagCompound();

        if (this.playerResolver != null)
            systemCompound.setTag(NBT_PLAYER, getFactoryController().serialize(playerResolver));

        if (this.retryingResolver != null)
            systemCompound.setTag(NBT_RETRYING, getFactoryController().serialize(retryingResolver));

        NBTTagList requestIdentityList = new NBTTagList();
        requestBiMap.keySet().forEach(token -> {
            NBTTagCompound requestCompound = new NBTTagCompound();

            requestCompound.setTag(NBT_TOKEN, getFactoryController().serialize(token));
            requestCompound.setTag(NBT_REQUEST, getFactoryController().serialize(requestBiMap.get(token)));

            requestIdentityList.appendTag(requestCompound);
        });
        systemCompound.setTag(NBT_REQUEST_IDENTITY_MAP, requestIdentityList);

        NBTTagList resolverRequestAssignmentList = new NBTTagList();
        resolverRequestMap.keySet().forEach(token -> {
            NBTTagCompound assignmentCompound = new NBTTagCompound();

            assignmentCompound.setTag(NBT_TOKEN, getFactoryController().serialize(token));
            NBTTagList assignedList = new NBTTagList();
            resolverRequestMap.get(token).forEach(assignedToken -> assignedList.appendTag(getFactoryController().serialize(assignedToken)));
            assignmentCompound.setTag(NBT_ASSIGNMENTS, assignedList);

            resolverRequestAssignmentList.appendTag(assignmentCompound);
        });
        systemCompound.setTag(NBT_RESOLVER_REQUESTS_ASSIGNMENTS, resolverRequestAssignmentList);

        return systemCompound;
    }

    /**
     * Method used to deserialize the data inside the given nbt tag into this request system.
     *
     * @param nbt The data to deserialize.
     */
    @Override
    public void deserializeNBT(final NBTTagCompound nbt)
    {
        if (playerResolver != null)
            this.resolverBiMap.remove(playerResolver.getRequesterId());

        if (retryingResolver != null)
            this.resolverBiMap.remove(retryingResolver.getRequesterId());

        if (nbt.hasKey(NBT_PLAYER))
            this.playerResolver = getFactoryController().deserialize(nbt.getCompoundTag(NBT_PLAYER));
        else
            this.playerResolver = null;

        if (nbt.hasKey(NBT_RETRYING))
            this.retryingResolver = getFactoryController().deserialize(nbt.getCompoundTag(NBT_RETRYING));
        else
            this.retryingResolver = null;

        if (this.playerResolver != null)
            ResolverHandler.registerResolver(this, this.playerResolver);

        if (this.retryingResolver != null)
            ResolverHandler.registerResolver(this, this.retryingResolver);

        NBTTagList requestIdentityList = nbt.getTagList(NBT_REQUEST_IDENTITY_MAP, Constants.NBT.TAG_COMPOUND);
        requestBiMap.clear();
        NBTUtils.streamCompound(requestIdentityList).forEach(identityCompound -> {
            IToken token = getFactoryController().deserialize(identityCompound.getCompoundTag(NBT_TOKEN));
            IRequest request = getFactoryController().deserialize(identityCompound.getCompoundTag(NBT_REQUEST));

            requestBiMap.put(token, request);
        });

        NBTTagList resolverRequestAssignmentList = nbt.getTagList(NBT_RESOLVER_REQUESTS_ASSIGNMENTS, Constants.NBT.TAG_COMPOUND);
        resolverRequestMap.clear();
        requestResolverMap.clear();
        NBTUtils.streamCompound(resolverRequestAssignmentList).forEach(assignmentCompound -> {
            IToken token = getFactoryController().deserialize(assignmentCompound.getCompoundTag(NBT_TOKEN));
            if (!resolverBiMap.containsKey(token))
            {
                Log.getLogger().error("Unknown resolver found in NBT Data. Something might be going wrong and requests might linger around!");
                return;
            }

            NBTTagList assignmentsLists = assignmentCompound.getTagList(NBT_ASSIGNMENTS, Constants.NBT.TAG_COMPOUND);
            Collection<IToken> assignedRequests = NBTUtils.streamCompound(assignmentsLists).map(tokenCompound -> {
                IToken assignedToken = getFactoryController().deserialize(tokenCompound);

                // Reverse mapping being restored.
                requestResolverMap.put(assignedToken, token);

                return assignedToken;
            }).collect(Collectors.toList());

            resolverRequestMap.put(token, assignedRequests);
        });
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
        return StandardFactoryController.getInstance();
    }

    /**
     * Get the player resolve.
     * @return the player resolver object.
     */
    @NotNull
    @Override
    public IPlayerRequestResolver getPlayerResolver()
    {
        return this.playerResolver;
    }

    @NotNull
    @Override
    public IRetryingRequestResolver getRetryingRequestResolver()
    {
        return this.retryingResolver;
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
        final IRequest<T> request = RequestHandler.createRequest(this, requester, object);

        if (colony != null)
            colony.markDirty();

        return request.getToken();
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
        RequestHandler.assignRequest(this, RequestHandler.getRequest(this, token));

        if (colony != null)
            colony.markDirty();
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
        final IToken token = createRequest(requester, object);
        assignRequest(token);
        return token;
    }

    @Override
    public boolean reassignRequest(@NotNull final IToken token, @NotNull final Collection<IToken> resolverTokenBlackList) throws IllegalArgumentException
    {
        final IRequest request = RequestHandler.getRequest(this, token);
        return RequestHandler.reassignRequest(this, request, resolverTokenBlackList);
    }

    /**
     * Method to get a request for a given token.
     * <p>
     * Returned value is a defensive copy. However should not be modified!
     *
     * @param token The token to get a request for.
     * @return The request of the given type for that token.
     *
     * @throws IllegalArgumentException when either their is no request with that token, or the token does not produce a request of the given type T.
     */
    @SuppressWarnings(Suppression.UNCHECKED)
    @NotNull
    @Override
    public <T extends IRequestable> IRequest<T> getRequestForToken(@NotNull final IToken token) throws IllegalArgumentException
    {
        final IRequest<T> internalRequest = RequestHandler.getRequest(this, token);

        final NBTTagCompound requestData = getFactoryController().serialize(internalRequest);

        return getFactoryController().deserialize(requestData);
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
        final IRequest request = RequestHandler.getRequest(this, token);

        LogHandler.log("Updating request state from:" + token + ". With original state: " + request.getState() + " to : " + state);

        request.setState(new WrappedStaticStateRequestManager(this), state);

        if (colony != null)
            colony.markDirty();

        switch (request.getState())
        {
            case COMPLETED:
                LogHandler.log("Request completed: " + token + ". Notifying parent and requester...");
                RequestHandler.onRequestSuccessful(this, token);
                return;
            case OVERRULED:
                LogHandler.log("Request overruled: " + token + ". Notifying parent, children and requester...");
                RequestHandler.onRequestOverruled(this, token);
                break;
            case CANCELLED:
                LogHandler.log("Request cancelled: " + token + ". Notifying parent, children and requester...");
                RequestHandler.onRequestCancelled(this, token);
                return;
            case RECEIVED:
                LogHandler.log("Request received: " + token + ". Removing from system...");
                RequestHandler.cleanRequestData(this, token);
                return;
            default:
        }
    }

    /**
     * Method used to indicate to this manager that a new Provider has been added to the colony.
     *
     * @param provider The new provider.
     */
    @Override
    public void onProviderAddedToColony(@NotNull final IRequestResolverProvider provider) throws IllegalArgumentException
    {
        ProviderHandler.registerProvider(this, provider);
    }

    /**
     * Method used to indicate to this manager that Provider has been removed from the colony.
     *
     * @param provider The removed provider.
     */
    @Override
    public void onProviderRemovedFromColony(@NotNull final IRequestResolverProvider provider) throws IllegalArgumentException
    {
        ProviderHandler.removeProvider(this, provider);
    }

    @Override
    public void update()
    {
        this.retryingResolver.update();
    }

    /**
     * Class used to handle the inner workings of the request system with regards to providers.
     */
    private final static class ProviderHandler
    {

        /**
         * Method used to get the registered resolvers for a given provider.
         *
         * @param manager  The manager to pull the data from.
         * @param provider The provider you are requesting the resolvers for.
         * @return The registered resolvers that belong to the given provider.
         *
         * @throws IllegalArgumentException when the token is not belonging to a registered provider.
         */
        private static ImmutableCollection<IToken> getRegisteredResolvers(final StandardRequestManager manager, final IRequestResolverProvider provider)
          throws IllegalArgumentException
        {
            //Check if the token is registered.
            getProvider(manager, provider.getToken());

            return manager.providerResolverMap.get(provider.getToken());
        }

        /**
         * Method used to get a provider from a token.
         *
         * @param token The token to get the provider form.
         * @return The provider that corresponds to the given token
         *
         * @throws IllegalArgumentException when no provider is not registered with the given token.
         */
        private static IRequestResolverProvider getProvider(final StandardRequestManager manager, final IToken token) throws IllegalArgumentException
        {
            if (!manager.providerBiMap.containsKey(token))
            {
                throw new IllegalArgumentException("The given token for a provider is not registered");
            }

            return manager.providerBiMap.get(token);
        }

        /**
         * Method used to register a provider to a given manager.
         *
         * @param manager  The manager to register them to.
         * @param provider The provider that provides the resolvers.
         * @throws IllegalArgumentException is thrown when a provider is already registered.
         */
        private static void registerProvider(final StandardRequestManager manager, final IRequestResolverProvider provider) throws IllegalArgumentException
        {
            if (manager.providerBiMap.containsKey(provider.getToken()) ||
                  manager.providerBiMap.containsValue(provider))
            {
                throw new IllegalArgumentException("The given provider is already registered");
            }

            manager.providerBiMap.put(provider.getToken(), provider);

            final ImmutableList.Builder<IToken> resolverListBuilder = new ImmutableList.Builder<>();
            resolverListBuilder.addAll(ResolverHandler.registerResolvers(manager, provider.getResolvers()));

            manager.providerResolverMap.put(provider.getToken(), resolverListBuilder.build());
        }

        private static void removeProvider(final StandardRequestManager manager, final IToken token) throws IllegalArgumentException
        {
            removeProviderInternal(manager, token);
        }

        /**
         * Internal method that handles the reassignment
         *
         * @param manager The manager that is being modified.
         * @param token   The token of the provider that is being removed.
         * @throws IllegalArgumentException is thrown when the token is not registered to a provider, or when the data stored in the manager is in conflict.
         */
        @SuppressWarnings(Suppression.UNCHECKED)
        private static void removeProviderInternal(final StandardRequestManager manager, final IToken token) throws IllegalArgumentException
        {
            final IRequestResolverProvider provider = getProvider(manager, token);

            LogHandler.log("Removing provider: " + provider);

            //Get the resolvers that are being removed.
            final ImmutableCollection<IToken> assignedResolvers = getRegisteredResolvers(manager, token);
            for (final IToken resolverToken : assignedResolvers)
            {
                //If no requests are assigned to this resolver skip.
                if (!manager.resolverRequestMap.containsKey(resolverToken))
                {
                    continue;
                }

                //Skip if the resolver has no requests assigned.
                if (manager.resolverRequestMap.get(resolverToken).size() == 0)
                {
                    LogHandler.log("Removing resolver without assigned requests: " + resolverToken);
                    manager.resolverRequestMap.remove(resolverToken);

                    ResolverHandler.removeResolver(manager, resolverToken);

                    continue;
                }

                //Clone the original list to modify it during iteration, if need be.
                final Collection<IToken> assignedRequests = new ArrayList<>(manager.resolverRequestMap.get(resolverToken));
                LogHandler.log("Starting reassignment of already registered requests registered to resolver with token: " + resolverToken);

                //Get all assigned requests and reassign them.
                for (final IToken requestToken : assignedRequests)
                {
                    LogHandler.log("Removing assigned request: " + requestToken + " from resolver: " + resolverToken);

                    //No need to notify the resolver of the cancellation, It is getting removed anyway.
                    //In that case: All resources lost, restart on different resolver.
                    //Also cancel all registered child task:
                    manager.resolverRequestMap.get(resolverToken).remove(requestToken);
                    manager.requestResolverMap.remove(requestToken);

                    LogHandler.log("Cancelling all child requests of:" + requestToken);

                    //Check if the request has children.
                    final IRequest assignedRequest = RequestHandler.getRequest(manager, requestToken);
                    if (assignedRequest.hasChildren())
                    {
                        //Iterate over all children and call there onRequestCancelledOrOverruled method to get a new cleanup parent.
                        for (final Object objectToken :
                          assignedRequest.getChildren())
                        {
                            if (objectToken instanceof IToken)
                            {
                                final IToken childToken = (IToken) objectToken;
                                final IRequest childRequest = RequestHandler.getRequest(manager, childToken);

                                //Check if the child has been assigned. If not, no work done, no cleanup needed.
                                if (RequestHandler.isAssigned(manager, childToken))
                                {
                                    //Get the child request
                                    final IRequestResolver childResolver = ResolverHandler.getResolverForRequest(manager, childToken);
                                    final IRequest cleanUpRequest = childResolver.onRequestCancelledOrOverruled(manager, childRequest);

                                    //Switch out the parent, and add the old child to the followup request as new child
                                    if (cleanUpRequest != null)
                                    {
                                        cleanUpRequest.addChild(childToken);
                                        childRequest.setParent(cleanUpRequest.getToken());

                                        //Assign the new followup request if it is not assigned yet.
                                        if (!RequestHandler.isAssigned(manager, cleanUpRequest.getToken()))
                                        {
                                            RequestHandler.assignRequest(manager, cleanUpRequest);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    LogHandler.log("Starting reassignment of: " + requestToken + " - Assigned to: " + resolverToken);

                    RequestHandler.assignRequest(manager, assignedRequest, assignedResolvers);

                    if (assignedRequest.getState().ordinal() < RequestState.RECEIVED.ordinal())
                    {
                        LogHandler.log("Finished reassignment of: " + requestToken + " - Assigned to: " + manager.requestResolverMap.get(requestToken));
                    }

                }

                ResolverHandler.removeResolver(manager, resolverToken);

                LogHandler.log("Finished reassignment of already registered requests registered to resolver with token: " + resolverToken);
            }

            //Removing the data from the maps.
            manager.providerBiMap.remove(provider.getToken());
            manager.providerResolverMap.remove(provider.getToken());

            LogHandler.log("Removed provider: " + provider);
        }

        /**
         * Method used to get the registered resolvers for a given provider.
         *
         * @param manager The manager to pull the data from.
         * @param token   The token of the provider you are requesting the resolvers for.
         * @return The registered resolvers that belong to the given provider.
         *
         * @throws IllegalArgumentException when the token is not belonging to a registered provider.
         */
        private static ImmutableCollection<IToken> getRegisteredResolvers(final StandardRequestManager manager, final IToken token) throws IllegalArgumentException
        {
            //Check if the token is registered.
            getProvider(manager, token);

            return manager.providerResolverMap.get(token);
        }

        private static void removeProvider(final StandardRequestManager manager, final IRequestResolverProvider provider) throws IllegalArgumentException
        {
            final IRequestResolverProvider registeredProvider = getProvider(manager, provider.getToken());

            if (!registeredProvider.equals(provider))
            {
                throw new IllegalArgumentException("The given providers token is registered to a different provider!");
            }

            removeProviderInternal(manager, provider.getToken());
        }
    }

    /**
     * Class used to handle the inner workings of the request system with regards to resolvers.
     */
    private final static class ResolverHandler
    {

        /**
         * Method used to register multiple resolvers simultaneously
         * <p>
         * <p>
         * Is only used internally.
         * The method modifies the resolverBiMap that is used to track resolvers.
         * </p>
         *
         * @param manager   The manager to register the resolvers to.
         * @param resolvers The resolvers to register.
         * @return The tokens of the resolvers that when registered.
         *
         * @throws IllegalArgumentException is thrown when an IllegalArgumentException is thrown by the registerResolver method for any of the given Resolvers.
         */
        private static Collection<IToken> registerResolvers(final StandardRequestManager manager, final IRequestResolver... resolvers) throws IllegalArgumentException
        {
            return Arrays.stream(resolvers).map(resolver -> registerResolver(manager, resolver)).collect(Collectors.toList());
        }

        /**
         * Method used to register a resolver.
         * <p>
         * <p>
         * Is only used internally.
         * The method modifies the resolverBiMap that is used to track which resolver are registered.
         * </p>
         *
         * @param manager  The manager to register the resolver to.
         * @param resolver The resolver to register
         * @return The token of the newly registered resolver
         *
         * @throws IllegalArgumentException is thrown when either the token attached to the resolver is already registered or the resolver is already registered with a different
         *                                  token
         */
        private static IToken registerResolver(final StandardRequestManager manager, final IRequestResolver resolver) throws IllegalArgumentException
        {
            if (manager.resolverBiMap.containsKey(resolver.getRequesterId()))
            {
                throw new IllegalArgumentException("The token attached to this resolver is already registered. Cannot register twice!");
            }

            if (manager.resolverBiMap.containsValue(resolver))
            {
                throw new IllegalArgumentException("The given resolver is already registered with a different token. Cannot register twice!");
            }

            manager.resolverBiMap.put(resolver.getRequesterId(), resolver);

            Set<TypeToken> resolverTypes = ReflectionUtils.getSuperClasses(resolver.getRequestType());
            resolverTypes.remove(TypeConstants.OBJECT);
            resolverTypes.forEach(c -> {
                if (!manager.requestClassResolverMap.containsKey(c))
                {
                    manager.requestClassResolverMap.put(c, new ArrayList<>());
                }

                LogHandler.log("Registering resolver: " + resolver + " with request type: " + c);
                manager.requestClassResolverMap.get(c).add(resolver);
            });

            return resolver.getRequesterId();
        }

        /**
         * Method used to register multiple resolvers simultaneously
         * <p>
         * <p>
         * Is only used internally.
         * The method modifies the resolverBiMap that is used to track resolvers.
         * </p>
         *
         * @param manager   The manager to register the resolvers to.
         * @param resolvers The resolvers to register.
         * @return The tokens of the resolvers that when registered.
         *
         * @throws IllegalArgumentException is thrown when an IllegalArgumentException is thrown by the registerResolver method for any of the given Resolvers.
         */
        private static Collection<IToken> registerResolvers(final StandardRequestManager manager, final Collection<IRequestResolver> resolvers)
        {
            return resolvers.stream().map(resolver -> registerResolver(manager, resolver)).collect(Collectors.toList());
        }

        /**
         * Method used to remove a registered resolver.
         * <p>
         * <p>
         * Is only used internally.
         * The method modifies the resolverBiMap that is used to track resolvers.
         * </p>
         *
         * @param manager The manager to remove the resolver from.
         * @param token   The token of the resolver to remove.
         * @throws IllegalArgumentException is thrown when the given resolver is not registered or the token of the given resolver is not registered to the same resolver.
         */
        private static void removeResolver(final StandardRequestManager manager, final IToken token) throws IllegalArgumentException
        {
            if (!manager.resolverBiMap.containsKey(token))
            {
                throw new IllegalArgumentException("The token is unknown to this manager.");
            }

            removeResolver(manager, getResolver(manager, token));
        }

        /**
         * Method used to remove a registered resolver.
         * <p>
         * <p>
         * Is only used internally.
         * The method modifies the resolverBiMap that is used to track resolvers.
         * </p>
         *
         * @param manager  The manager to remove the resolver from.
         * @param resolver The resolver to remove
         * @throws IllegalArgumentException is thrown when the given resolver is not registered or the token of the given resolver is not registered to the same resolver.
         */
        private static void removeResolver(final StandardRequestManager manager, final IRequestResolver resolver) throws IllegalArgumentException
        {
            final IRequestResolver registeredResolver = getResolver(manager, resolver.getRequesterId());

            if (!registeredResolver.equals(resolver))
            {
                throw new IllegalArgumentException("The given resolver and the resolver that is registered with its token are not the same.");
            }

            if (manager.resolverRequestMap.containsKey(registeredResolver.getRequesterId()) && manager.resolverRequestMap.get(registeredResolver.getRequesterId()).size() > 0)
            {
                throw new IllegalArgumentException("Cannot remove a resolver that is still in use. Reassign all registered requests before removing");
            }


            manager.resolverBiMap.remove(resolver.getRequesterId());
            Set<TypeToken> requestTypes = ReflectionUtils.getSuperClasses(resolver.getRequestType());
            requestTypes.remove(TypeConstants.OBJECT);
            requestTypes.forEach(c -> {
                LogHandler.log("Removing resolver: " + resolver + " with request type: " + c);
                manager.requestClassResolverMap.get(c).remove(resolver);
            });
        }

        /**
         * Method to get a resolver from a given token.
         * <p>
         * <p>
         * Is only used internally.
         * Querries the resolverBiMap to get the resolver for a given Token.
         * </p>
         *
         * @param manager The manager to retrieve the resolver from.
         * @param token   The token of the resolver to look up.
         * @return The resolver registered with the given token.
         *
         * @throws IllegalArgumentException is thrown when the given token is not registered to any IRequestResolver
         */
        private static IRequestResolver getResolver(final StandardRequestManager manager, final IToken token) throws IllegalArgumentException
        {
            if (!manager.resolverBiMap.containsKey(token))
            {
                throw new IllegalArgumentException("The given token for a resolver is not known to this manager!");
            }

            LogHandler.log("Retrieving resolver for: " + token);

            return manager.resolverBiMap.get(token);
        }

        /**
         * Method used to remove a multiple registered resolvers.
         * <p>
         * <p>
         * Is only used internally.
         * The method modifies the resolverBiMap that is used to track resolvers.
         * </p>
         *
         * @param manager   The manager to remove the resolver from.
         * @param resolvers The resolvers to remove.
         * @throws IllegalArgumentException is thrown when removeResolver throws an IllegalArgumentException for any of the given resolvers.
         */
        private static void removeResolvers(final StandardRequestManager manager, final IRequestResolver... resolvers)
        {
            removeResolvers(manager, Arrays.asList(resolvers));
        }

        /**
         * Method used to remove a multiple registered resolvers.
         * <p>
         * <p>
         * Is only used internally.
         * The method modifies the resolverBiMap that is used to track resolvers.
         * </p>
         *
         * @param manager   The manager to remove the resolver from.
         * @param resolvers The resolvers to remove.
         * @throws IllegalArgumentException is thrown when removeResolver throws an IllegalArgumentException for any of the given resolvers.
         */
        private static void removeResolvers(final StandardRequestManager manager, final Iterable<IRequestResolver> resolvers)
        {
            resolvers.forEach((IRequestResolver resolver) ->
                                removeResolver(manager, resolver));
        }

        /**
         * Method used to add a request to a resolver.
         * <p>
         * <p>
         * Is only used internally.
         * The method modifies the resolverRequestMap that is used to track which resolver handles which request.
         * </p>
         *
         * @param manager  The manager to modify
         * @param resolver The resolver to add the request to.
         * @param request  The request to add to the resolver.
         */
        private static void addRequestToResolver(final StandardRequestManager manager, final IRequestResolver resolver, final IRequest request)
        {
            if (!manager.resolverRequestMap.containsKey(resolver.getRequesterId()))
            {
                manager.resolverRequestMap.put(resolver.getRequesterId(), new ArrayList<>());
            }

            LogHandler.log("Adding request: " + request + " to resolver: " + resolver);

            manager.resolverRequestMap.get(resolver.getRequesterId()).add(request.getToken());
            manager.requestResolverMap.put(request.getToken(), resolver.getRequesterId());

            request.setState(new WrappedStaticStateRequestManager(manager), RequestState.ASSIGNED);
        }

        /**
         * Method used to remove a request from a resolver.
         * <p>
         * <p>
         * Is only used internally.
         * The method modifies the resolverRequestMap that is used to track which resolver handles which request.
         * </p>
         *
         * @param manager  The manager to modify
         * @param resolver The resolver to remove the given request from.
         * @param request  The request to remove.
         * @throws IllegalArgumentException is thrown when the resolver is unknown, or when the given request is not registered to the given resolver.
         */
        private static void removeRequestFromResolver(final StandardRequestManager manager, final IRequestResolver resolver, final IRequest request) throws IllegalArgumentException
        {
            if (!manager.resolverRequestMap.containsKey(resolver.getRequesterId()))
            {
                throw new IllegalArgumentException("The given resolver is unknown to this Manager");
            }

            if (!manager.resolverRequestMap.get(resolver.getRequesterId()).contains(request.getToken()))
            {
                throw new IllegalArgumentException("The given request is not registered to the given resolver.");
            }

            LogHandler.log("Removing request: " + request + " from resolver: " + resolver);

            manager.resolverRequestMap.get(resolver.getRequesterId()).remove(request.getToken());
            manager.requestResolverMap.remove(request.getToken());
        }

        /**
         * Method used to get a resolver from a given request token.
         *
         * @param manager      The manager to get the resolver from.
         * @param requestToken The token of a request a the assigned resolver is requested for.
         * @return The resolver for the request with the given token.
         *
         * @throws IllegalArgumentException when the token is unknown or the request is not assigned yet.
         */
        private static IRequestResolver getResolverForRequest(final StandardRequestManager manager, final IToken requestToken) throws IllegalArgumentException
        {
            RequestHandler.getRequest(manager, requestToken);

            if (!manager.requestResolverMap.containsKey(requestToken))
            {
                throw new IllegalArgumentException("The given token belongs to a not resolved request");
            }

            return getResolver(manager, manager.requestResolverMap.get(requestToken));
        }

        /**
         * Method used to get a resolver from a given request token.
         *
         * @param manager The manager to get the resolver from.
         * @param request The request a the assigned resolver is requested for.
         * @return The resolver for the request.
         *
         * @throws IllegalArgumentException when the token is unknown or the request is not assigned yet.
         */
        private static IRequestResolver getResolverForRequest(final StandardRequestManager manager, final IRequest request)
        {
            RequestHandler.getRequest(manager, request.getToken());

            if (!manager.requestResolverMap.containsKey(request.getToken()))
            {
                throw new IllegalArgumentException("The given token belongs to a not resolved request");
            }

            return getResolver(manager, manager.requestResolverMap.get(request.getToken()));
        }
    }

    /**
     * Class used to handle the inner workings of the request system with regards to requests.
     */
    private final static class RequestHandler
    {

        @SuppressWarnings(Suppression.UNCHECKED)
        private static <Request extends IRequestable> IRequest<Request> createRequest(final StandardRequestManager manager, final IRequester requester, final Request request)
        {
            final IToken<UUID> token = TokenHandler.generateNewToken(manager);

            final IRequest<Request> constructedRequest = manager.getFactoryController().getNewInstance(TypeToken.of((Class<? extends IRequest<Request>>) requestableMappings.get(request.getClass())), request, token, requester);

            LogHandler.log("Creating request for: " + request + ", token: " + token + " and output: " + constructedRequest);

            registerRequest(manager, constructedRequest);

            return constructedRequest;
        }

        private static void registerRequest(final StandardRequestManager manager, final IRequest request) throws IllegalArgumentException
        {
            if (manager.requestBiMap.containsKey(request.getToken()) ||
                  manager.requestBiMap.containsValue(request))
            {
                throw new IllegalArgumentException("The given request is already known to this manager");
            }

            LogHandler.log("Registering request: " + request);

            manager.requestBiMap.put(request.getToken(), request);
        }

        /**
         * Method used to assign a given request to a resolver. Does not take any blacklist into account.
         *
         * @param manager The manager to modify.
         * @param request The request to assign
         * @throws IllegalArgumentException when the request is already assigned
         */
        @SuppressWarnings(Suppression.UNCHECKED)
        private static void assignRequest(final StandardRequestManager manager, final IRequest request) throws IllegalArgumentException
        {
            assignRequest(manager, request, Collections.EMPTY_LIST);
        }

        /**
         * Method used to assign a given request to a resolver. Does take a given blacklist of resolvers into account.
         *
         * @param manager                The manager to modify.
         * @param request                The request to assign.
         * @param resolverTokenBlackList Each resolver that has its token in this blacklist will be skipped when checking for a possible resolver.
         * @throws IllegalArgumentException is thrown when the request is unknown to this manager.
         */
        private static void assignRequest(final StandardRequestManager manager, final IRequest request, final IToken... resolverTokenBlackList) throws IllegalArgumentException
        {
            assignRequest(manager, request, Arrays.asList(resolverTokenBlackList));
        }

        /**
         * Method used to assign a given request to a resolver. Does take a given blacklist of resolvers into account.
         *
         * @param manager                The manager to modify.
         * @param request                The request to assign.
         * @param resolverTokenBlackList Each resolver that has its token in this blacklist will be skipped when checking for a possible resolver.
         * @return true when assigning successfully false when not.
         * @throws IllegalArgumentException is thrown when the request is unknown to this manager.
         */
        @SuppressWarnings(Suppression.UNCHECKED)
        private static boolean assignRequest(final StandardRequestManager manager, final IRequest request, final Collection<IToken> resolverTokenBlackList)
          throws IllegalArgumentException
        {
            //Check if the request is registered
            getRequest(manager, request.getToken());

            LogHandler.log("Starting resolver assignment search for request: " + request);

            request.setState(new WrappedStaticStateRequestManager(manager), RequestState.ASSIGNING);

            Set<TypeToken> requestTypes = ReflectionUtils.getSuperClasses(request.getRequestType());
            requestTypes.remove(TypeConstants.OBJECT);
            for(TypeToken requestType : requestTypes) {
                if (!manager.requestClassResolverMap.containsKey(requestType))
                    continue;

                Collection<IRequestResolver> resolversForRequestType = manager.requestClassResolverMap.get(requestType);
                resolversForRequestType = resolversForRequestType.stream().sorted(Comparator.comparing(IRequestResolver::getPriority)).collect(Collectors.toSet());

                for (final IRequestResolver resolver : resolversForRequestType)
                {
                    //Skip when the resolver is in the blacklist.
                    if (resolverTokenBlackList.contains(resolver.getRequesterId()))
                    {
                        continue;
                    }

                    //Skip if preliminary check fails
                    if (!resolver.canResolve(manager, request))
                    {
                        continue;
                    }

                    @Nullable final List<IToken> attemptResult = resolver.attemptResolve(new WrappedBlacklistAssignmentRequestManager(manager, resolverTokenBlackList), request);

                    //Skip if attempt failed (aka attemptResult == null)
                    if (attemptResult == null)
                    {
                        continue;
                    }

                    //Successfully found a resolver. Registering
                    LogHandler.log("Finished resolver assignment search for request: " + request + " successfully");
                    ResolverHandler.addRequestToResolver(manager, resolver, request);

                    for (final IToken childRequestToken :
                      attemptResult)
                    {
                        final IRequest childRequest = RequestHandler.getRequest(manager, childRequestToken);

                        childRequest.setParent(request.getToken());
                        request.addChild(childRequest.getToken());

                        if (!isAssigned(manager, childRequestToken))
                        {
                            assignRequest(manager, childRequest, resolverTokenBlackList);
                        }
                    }

                    if (request.getState().ordinal() < RequestState.IN_PROGRESS.ordinal())
                    {
                        request.setState(new WrappedStaticStateRequestManager(manager), RequestState.IN_PROGRESS);
                        if (!request.hasChildren())
                        {
                            resolveRequest(manager, request);
                        }
                    }

                    return true;
                }
            }

            return false;
        }

        /**
         * Method used to reassign the request to a resolver that is not in the given blacklist.
         * Cancels the request internally without notify the requester, and attempts a reassign. If the reassignment failed, it is assigned back to the orignal resolver.
         * @param manager The manager that is reassigning a request.
         * @param request The request that is being reassigned.
         * @param resolverTokenBlackList The blacklist to which not to assign the request.
         * @return True when the reassignment was successful, false when not.
         * @throws IllegalArgumentException Thrown when something went wrong.
         */
        private static boolean reassignRequest(final StandardRequestManager manager, final IRequest request, final Collection<IToken> resolverTokenBlackList) throws IllegalArgumentException
        {
            //Cancel the request to restart the search
            processInternalCancellation(manager, request.getToken());

            manager.updateRequestState(request.getToken(), RequestState.REPORTED);
            return assignRequest(manager, request, resolverTokenBlackList);
        }

        /**
         * Method used to check if a given request token is assigned to a resolver.
         *
         * @param manager The manager to check for.
         * @param token   The request token to check for.
         * @return True when the request token has been assigned, false when not.
         */
        private static boolean isAssigned(final StandardRequestManager manager, final IToken token)
        {
            return manager.requestResolverMap.containsKey(token);
        }

        /**
         * Method used to handle the successful resolving of a request.
         *
         * @param manager The manager that got notified of the successful resolving of the request.
         * @param token   The token of the request that got finished successfully.
         */
        @SuppressWarnings(Suppression.UNCHECKED)
        private static void onRequestSuccessful(final StandardRequestManager manager, final IToken token)
        {
            final IRequest request = getRequest(manager, token);
            final IRequestResolver resolver = ResolverHandler.getResolverForRequest(manager, token);

            request.getRequester().onRequestComplete(token);

            //Retrieve a followup request.
            final IRequest followupRequest = resolver.getFollowupRequestForCompletion(manager, request);

            //Check if the request has a parent
            if (request.hasParent())
            {
                final IRequest parentRequest = getRequest(manager, request.getParent());

                //Assign the followup to the parent as a child so that processing is still halted.
                if (followupRequest != null)
                {
                    parentRequest.addChild(followupRequest.getToken());
                }

                parentRequest.removeChild(request.getToken());

                if (!parentRequest.hasChildren() && parentRequest.getState() == RequestState.IN_PROGRESS)
                {
                    resolveRequest(manager, parentRequest);
                }

                request.setParent(null);
            }

            //Assign the followup request if need be
            if (followupRequest != null && !isAssigned(manager, followupRequest.getToken()))
            {
                assignRequest(manager, followupRequest);
            }
        }

        /**
         * Method used to handle requests that were overruled or cancelled.
         * Cancels all children first, handles the creation of clean up requests.
         *
         * @param manager The manager that got notified of the cancellation or overruling.
         * @param token   The token of the request that got cancelled or overruled
         */
        @SuppressWarnings(Suppression.UNCHECKED)
        private static void onRequestOverruled(final StandardRequestManager manager, final IToken token)
        {
            final IRequest request = getRequest(manager, token);

            if (!manager.requestResolverMap.containsKey(token))
            {
                manager.requestBiMap.remove(token);
                return;
            }

            //Lets cancel all our children first, else this would make a big fat mess.
            if (request.hasChildren())
            {
                final ImmutableCollection<IToken> currentChildren = request.getChildren();
                currentChildren.forEach(t -> onRequestCancelled(manager, t));
            }

            //Now lets get ourselfs a clean up.
            final IRequestResolver targetResolver = ResolverHandler.getResolverForRequest(manager, request);
            processParentReplacement(manager, request, targetResolver.onRequestCancelledOrOverruled(manager, request));

            //This will notify everyone :D
            manager.updateRequestState(token, RequestState.COMPLETED);
        }

        /**
         * Method used to handle requests that were overruled or cancelled.
         * Cancels all children first, handles the creation of clean up requests.
         *
         * @param manager The manager that got notified of the cancellation or overruling.
         * @param token   The token of the request that got cancelled or overruled
         */
        @SuppressWarnings(Suppression.UNCHECKED)
        private static void onRequestCancelled(final StandardRequestManager manager, final IToken token)
        {
            final IRequest request = RequestHandler.getRequest(manager, token);

            if (request == null)
            {
                return;
            }

            processInternalCancellation(manager, token);

            //Notify the requester.
            final IRequester requester = request.getRequester();
            requester.onRequestCancelled(token);

            cleanRequestData(manager, token);
        }

        /**
         * Method used to handle cancellation internally without notifying the requester that the request has been cancelled.
         *
         * @param manager The manager for which the cancellation is internally processed.
         * @param token The token which is internally processed.
         */
        private static void processInternalCancellation(final StandardRequestManager manager, final IToken token)
        {
            final IRequest request = getRequest(manager, token);

            if (!manager.requestResolverMap.containsKey(token))
            {
                manager.requestBiMap.remove(token);
                return;
            }

            //Lets cancel all our children first, else this would make a big fat mess.
            if (request.hasChildren())
            {
                final ImmutableCollection<IToken> currentChildren = request.getChildren();
                currentChildren.forEach(t -> onRequestCancelled(manager, t));
            }

            //Now lets get ourselfs a clean up.
            final IRequestResolver targetResolver = ResolverHandler.getResolverForRequest(manager, request);
            processParentReplacement(manager, request, targetResolver.onRequestCancelledOrOverruled(manager, request));

            manager.updateRequestState(token, RequestState.FINALIZING);
        }

        /**
         * Method used during clean up to process Parent replacement.
         *
         * @param manager The manager which is handling the cleanup.
         * @param target The target request, which gets their parent replaced.
         * @param newParent The new cleanup request used to cleanup the target when it is finished.
         */
        private static void processParentReplacement(final StandardRequestManager manager, final IRequest target, final IRequest newParent)
        {
            //Clear out the existing parent.
            if (target.hasParent())
            {
                final IRequest currentParent = RequestHandler.getRequest(manager, target.getParent());

                currentParent.removeChild(target.getToken());
                target.setParent(null);
            }

            if (newParent != null)
            {
                //Switch out the parent, and add the old child to the cleanup request as new child
                newParent.addChild(target.getToken());
                target.setParent(newParent.getToken());

                //Assign the new parent request if it is not assigned yet.
                if (!RequestHandler.isAssigned(manager, newParent.getToken()))
                {
                    RequestHandler.assignRequest(manager, newParent);
                }
            }
        }

        /**
         * Method used to resolve a request.
         * When this method is called the given request has to be assigned.
         *
         * @param manager The manager requesting the resolving.
         * @param request The request about to be resolved.
         * @throws IllegalArgumentException when the request is unknown, not resolved, or cannot be resolved.
         */
        @SuppressWarnings(Suppression.UNCHECKED)
        private static void resolveRequest(final StandardRequestManager manager, final IRequest request) throws IllegalArgumentException
        {
            getRequest(manager, request.getToken());
            if (!isAssigned(manager, request.getToken()))
            {
                throw new IllegalArgumentException("The given request is not resolved");
            }

            if (request.getState() != RequestState.IN_PROGRESS)
            {
                throw new IllegalArgumentException("The given request is not in the right state. Required: " + RequestState.IN_PROGRESS + " - Found:" + request.getState());
            }

            if (request.hasChildren())
            {
                throw new IllegalArgumentException("Cannot resolve request with open Children");
            }

            final IRequestResolver resolver = ResolverHandler.getResolverForRequest(manager, request);

            request.setState(new WrappedStaticStateRequestManager(manager), RequestState.IN_PROGRESS);
            resolver.resolve(manager, request);
        }

        /**
         * Method called when the given manager gets notified of the receiving of a given task by its requester.
         * All communication with the resolver should be aborted by this time, so overrullings and cancelations need to be processed,
         * before this method is called.
         *
         * @param manager The manager that got notified.
         * @param token   The token of the request.
         * @throws IllegalArgumentException Thrown when the token is unknown.
         */
        private static void cleanRequestData(final StandardRequestManager manager, final IToken token) throws IllegalArgumentException
        {
            LogHandler.log("Removing " + token + " from the Manager as it has been completed and its package has been received by the requester.");
            getRequest(manager, token);

            manager.requestBiMap.remove(token);

            if (isAssigned(manager, token))
            {
                manager.resolverRequestMap.get(manager.requestResolverMap.get(token)).remove(token);
                manager.requestResolverMap.remove(token);
            }
        }

        /**
         * Method used to get a registered request from a given token.
         *
         * @param token The token to query
         * @throws IllegalArgumentException when the token is unknown to the given manager.
         */
        private static IRequest getRequest(final StandardRequestManager manager, final IToken token) throws IllegalArgumentException
        {
            if (!manager.requestBiMap.containsKey(token))
            {
                throw new IllegalArgumentException("The given token is not registered as a request to this manager");
            }

            LogHandler.log("Retrieving the request for: " + token);

            return manager.requestBiMap.get(token);
        }
    }

    /**
     * Class used to handle the inner workings of the request system with regards to tokens.
     */
    private final static class TokenHandler
    {

        private static IToken generateNewToken(final StandardRequestManager manager)
        {
            //Force generic type to be correct.
            return manager.getFactoryController().getNewInstance(TypeConstants.ITOKEN, UUID.randomUUID());
        }
    }

    public final static class LogHandler
    {
        private static final Logger logger = LogManager.getLogger("Minecolonies:RequestSystem"); 
        
        public static void log(String logEntry)
        {
            if (Configurations.requestSystem.enableDebugLogging)
            {
                logger.info(logEntry);
            }
        }
    }

    /**
     * Wrapper class for a Manager.
     * Subclasses of this have custom behaviour on at least one method.
     */
    private static abstract class AbstractWrappedRequestManager implements IRequestManager
    {
        @NotNull
        protected final StandardRequestManager wrappedManager;

        public AbstractWrappedRequestManager(@NotNull final StandardRequestManager wrappedManager)
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
        public <T extends IRequestable> IToken createRequest(@NotNull final IRequester requester, @NotNull final T object) throws IllegalArgumentException
        {
            return wrappedManager.createRequest(requester, object);
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
        public <T extends IRequestable> IToken createAndAssignRequest(@NotNull final IRequester requester, @NotNull final T object) throws IllegalArgumentException
        {
            final IToken token = createRequest(requester, object);
            assignRequest(token);
            return token;
        }

        @Override
        public boolean reassignRequest(@NotNull final IToken token, @NotNull final Collection<IToken> resolverTokenBlackList) throws IllegalArgumentException
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
        @NotNull
        @Override
        public <T extends IRequestable> IRequest<T> getRequestForToken(@NotNull final IToken token) throws IllegalArgumentException
        {
            return RequestHandler.getRequest(wrappedManager, token);
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
            wrappedManager.updateRequestState(token, state);
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

        @Override
        public NBTTagCompound serializeNBT()
        {
            return wrappedManager.serializeNBT();
        }

        @Override
        public void deserializeNBT(final NBTTagCompound nbt)
        {
            wrappedManager.deserializeNBT(nbt);
        }

        @NotNull
        @Override
        public IPlayerRequestResolver getPlayerResolver()
        {
            return wrappedManager.getPlayerResolver();
        }

        @Override
        public void update()
        {
            wrappedManager.update();
        }

        @NotNull
        @Override
        public IRetryingRequestResolver getRetryingRequestResolver()
        {
            return wrappedManager.getRetryingRequestResolver();
        }
    }

    /**
     * Class used to handle internal reassignment changes.
     * Take the given blacklist into account when it assigns the requests.
     */
    private final static class WrappedBlacklistAssignmentRequestManager extends AbstractWrappedRequestManager
    {

        @NotNull
        private final Collection<IToken> blackListedResolvers;

        private WrappedBlacklistAssignmentRequestManager(@NotNull final StandardRequestManager wrappedManager, @NotNull final Collection<IToken> blackListedResolvers)
        {
            super(wrappedManager);
            this.blackListedResolvers = blackListedResolvers;
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
            RequestHandler.assignRequest(wrappedManager, RequestHandler.getRequest(wrappedManager, token), blackListedResolvers);
        }
    }

    /**
     * Class used to handle internal state changes that might cause a loop.
     * Simply returns without notifying its wrapped manager about the state change.
     */
    private final static class WrappedStaticStateRequestManager extends AbstractWrappedRequestManager
    {

        public WrappedStaticStateRequestManager(@NotNull final StandardRequestManager wrappedManager)
        {
            super(wrappedManager);
        }

        @NotNull
        @Override
        /**
         * Method to update the state of a given request.
         *
         * @param token The token that represents a given request to update.
         * @param state The new state of that request.
         * @throws IllegalArgumentException when the token is unknown to this manager.
         */
        public void updateRequestState(@NotNull final IToken token, @NotNull final RequestState state) throws IllegalArgumentException
        {
            //TODO: implement when link is created with workers
        }
    }
}
