package com.minecolonies.coremod.colony.requestsystem.management.manager;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requestable.IRetryable;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolverProvider;
import com.minecolonies.api.colony.requestsystem.resolver.player.IPlayerRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.retrying.IRetryingRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.Suppression;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;
import com.minecolonies.coremod.colony.requestsystem.management.handlers.LogHandler;
import com.minecolonies.coremod.colony.requestsystem.management.handlers.ProviderHandler;
import com.minecolonies.coremod.colony.requestsystem.management.handlers.RequestHandler;
import com.minecolonies.coremod.colony.requestsystem.management.handlers.ResolverHandler;
import com.minecolonies.coremod.colony.requestsystem.management.manager.wrapped.WrappedStaticStateRequestManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Main class of the request system.
 * Default implementation of the IRequestManager interface.
 * <p>
 * Uses
 */

@SuppressWarnings(Suppression.BIG_CLASS)
public class StandardRequestManager implements IStandardRequestManager
{
    ////---------------------------NBTTags-------------------------\\\\
    private static final String NBT_REQUEST_IDENTITY_MAP          = "Request_Identities";
    private static final String NBT_RESOLVER_REQUESTS_ASSIGNMENTS = "Resolver_Requests";
    private static final String NBT_PLAYER                        = "Player";
    private static final String NBT_RETRYING                      = "Retrying";

    private static final String NBT_TOKEN       = "Token";
    private static final String NBT_ASSIGNMENTS = "Assignments";

    private static final String NBT_REQUEST = "Request";
    ////---------------------------NBTTags-------------------------\\\\

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
    private final Map<IToken, Set<IToken>> resolverRequestMap = new HashMap<>();

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
     * Colony of the manager.
     */
    @NotNull
    private final IColony colony;
    /**
     * The fallback resolver used to resolve directly to the player.
     */
    @NotNull
    private       IPlayerRequestResolver                       playerResolver          = null;
    /**
     * The fallback resolver used to resolve using retries.
     * Not all requests might support this feature, requests that do should implement {@link IRetryable} on their requestable.
     * Anything that implements {@link IDeliverable} is by definition retryable.
     */
    @NotNull
    private IRetryingRequestResolver retryingResolver = null;

    public StandardRequestManager(final IColony colony)
    {
        this.colony = colony;

        this.playerResolver = getFactoryController().getNewInstance(TypeConstants.PLAYER_REQUEST_RESOLVER, this);
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
        {
            colony.markDirty();
        }

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
        {
            colony.markDirty();
        }
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
    @Nullable
    public IToken reassignRequest(@NotNull final IToken token, @NotNull final Collection<IToken> resolverTokenBlackList) throws IllegalArgumentException
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
    @Nullable
    @Override
    public <T extends IRequestable> IRequest<T> getRequestForToken(@NotNull final IToken token) throws IllegalArgumentException
    {
        final IRequest<T> internalRequest = RequestHandler.getRequestOrNull(this, token);

        if (internalRequest == null)
        {
            return null;
        }

        final NBTTagCompound requestData = getFactoryController().serialize(internalRequest);

        return getFactoryController().deserialize(requestData);
    }

    @NotNull
    @Override
    public <T extends IRequestable> IRequestResolver<T> getResolverForToken(@NotNull final IToken token) throws IllegalArgumentException
    {
        final IRequestResolver<T> resolver = ResolverHandler.getResolver(this, token);

        return getFactoryController().deserialize(getFactoryController().serialize(resolver));
    }

    @Nullable
    @Override
    public <T extends IRequestable> IRequestResolver<T> getResolverForRequest(@NotNull final IToken requestToken) throws IllegalArgumentException
    {
        final IRequest request = RequestHandler.getRequest(this, requestToken);

        return getResolverForToken(ResolverHandler.getResolverForRequest(this, request).getRequesterId());
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
        {
            colony.markDirty();
        }

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

    @Override
    public void overruleRequest(@NotNull final IToken token, @Nullable final ItemStack stack) throws IllegalArgumentException
    {
        final IRequest request = RequestHandler.getRequest(this, token);

        if (!ItemStackUtils.isEmpty(stack))
        {
            request.setDelivery(stack);
        }

        updateRequestState(token, RequestState.OVERRULED);
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

    /**
     * Get the player resolve.
     *
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
     * Method used to serialize the current request system to NBT.
     *
     * @return The NBTData that describes the current request system
     */
    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound systemCompound = new NBTTagCompound();

        if (this.playerResolver != null)
        {
            systemCompound.setTag(NBT_PLAYER, getFactoryController().serialize(playerResolver));
        }

        if (this.retryingResolver != null)
        {
            systemCompound.setTag(NBT_RETRYING, getFactoryController().serialize(retryingResolver));
        }

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
        {
            ResolverHandler.removeResolverInternal(this, this.playerResolver);
        }

        if (retryingResolver != null)
        {
            ResolverHandler.removeResolverInternal(this, this.retryingResolver);
        }

        if (nbt.hasKey(NBT_PLAYER))
        {
            this.playerResolver = getFactoryController().deserialize(nbt.getCompoundTag(NBT_PLAYER));
        }
        else
        {
            this.playerResolver = null;
        }

        if (nbt.hasKey(NBT_RETRYING))
        {
            this.retryingResolver = getFactoryController().deserialize(nbt.getCompoundTag(NBT_RETRYING));
            this.retryingResolver.updateManager(this);
        }
        else
        {
            this.retryingResolver = null;
        }

        if (this.playerResolver != null)
        {
            ResolverHandler.registerResolver(this, this.playerResolver);
        }

        if (this.retryingResolver != null)
        {
            ResolverHandler.registerResolver(this, this.retryingResolver);
        }

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
                //Since we use dynamic resolvers some might not exist on the client side.
                //If we would not do this check it would spam the log.
                if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
                {
                    Log.getLogger().error("Unknown resolver found in NBT Data. Something might be going wrong and requests might linger around!");
                }
                return;
            }

            NBTTagList assignmentsLists = assignmentCompound.getTagList(NBT_ASSIGNMENTS, Constants.NBT.TAG_COMPOUND);
            Set<IToken> assignedRequests = NBTUtils.streamCompound(assignmentsLists).map(tokenCompound -> {
                IToken assignedToken = getFactoryController().deserialize(tokenCompound);

                // Reverse mapping being restored.
                requestResolverMap.put(assignedToken, token);

                return assignedToken;
            }).collect(Collectors.toSet());

            resolverRequestMap.put(token, assignedRequests);
        });
    }

    @Override
    public void update()
    {
        this.retryingResolver.update();
    }

    @Override
    @NotNull
    public BiMap<IToken, IRequestResolverProvider> getProviderBiMap()
    {
        return providerBiMap;
    }

    @Override
    @NotNull
    public BiMap<IToken, IRequestResolver> getResolverBiMap()
    {
        return resolverBiMap;
    }

    @Override
    @NotNull
    public BiMap<IToken, IRequest> getRequestBiMap()
    {
        return requestBiMap;
    }

    @Override
    @NotNull
    public Map<IToken, ImmutableCollection<IToken>> getProviderResolverMap()
    {
        return providerResolverMap;
    }

    @Override
    @NotNull
    public Map<IToken, Set<IToken>> getResolverRequestMap()
    {
        return resolverRequestMap;
    }

    @Override
    @NotNull
    public Map<IToken, IToken> getRequestResolverMap()
    {
        return requestResolverMap;
    }

    @Override
    @NotNull
    public Map<TypeToken, Collection<IRequestResolver>> getRequestClassResolverMap()
    {
        return requestClassResolverMap;
    }

    @NotNull
    @Override
    public boolean isDataSimulation()
    {
        return false;
    }

    @NotNull
    @Override
    public boolean isResolvingSimulation()
    {
        return false;
    }
}
