package com.minecolonies.coremod.colony.requestsystem.management.manager;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.data.*;
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
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;
import com.minecolonies.coremod.colony.requestsystem.management.handlers.*;
import com.minecolonies.coremod.colony.requestsystem.management.manager.wrapped.WrappedStaticStateRequestManager;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import net.minecraft.nbt.CompoundNBT;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.Suppression.BIG_CLASS;

/**
 * Main class of the request system.
 * Default implementation of the IRequestManager interface.
 * <p>
 * Uses
 */

@SuppressWarnings(BIG_CLASS)
public class StandardRequestManager implements IStandardRequestManager
{
    ////---------------------------NBTTags-------------------------\\\\
    private static final String NBT_DATASTORE = "DataStores";
    private static final String NBT_ID_REQUEST_IDENTITIES = "RequestIdentitiesStoreId";
    private static final String NBT_ID_REQUEST_RESOLVER_IDENTITIES = "RequestResolverIdentitiesStoreId";
    private static final String NBT_ID_PROVIDER_ASSIGNMENTS = "ProviderAssignmentsStoreId";
    private static final String NBT_ID_REQUEST_RESOLVER_ASSIGNMENTS = "RequestResolverAssignmentsStoreId";
    private static final String NBT_ID_REQUESTABLE_TYPE_ASSIGNMENTS = "RequestableTypeAssignmentsStoreId";
    private static final String NBT_ID_PLAYER                        = "PlayerRequestResolverId";
    private static final String NBT_ID_RETRYING                      = "RetryingRequestResolverId";
    private static final String NBT_VERSION = "Version";
    ////---------------------------NBTTags-------------------------\\\\

    private IToken<?> requestIdentitiesDataStoreId;

    private IToken<?> requestResolverIdentitiesDataStoreId;

    private IToken<?> providerRequestResolverAssignmentDataStoreId;

    private IToken<?> requestResolverRequestAssignmentDataStoreId;

    private IToken<?> requestableTypeRequestResolverAssignmentDataStoreId;

    private IToken<?> playerRequestResolverId;

    private IToken<?> retryingRequestResolverId;

    private IDataStoreManager dataStoreManager;

    /**
     * Variable describing if the request manager itself is dirty.
     */
    private boolean dirty = true;

    /**
     * Colony of the manager.
     */
    @NotNull
    private final IColony colony;

    @NotNull
    private final Logger logger;

    @NotNull
    private final IUpdateHandler updateHandler = new UpdateHandler(this);

    @NotNull
    private final ITokenHandler tokenHandler = new TokenHandler(this);

    @NotNull
    private final IResolverHandler resolverHandler = new ResolverHandler(this);

    @NotNull
    private final IRequestHandler requestHandler = new RequestHandler(this);

    @NotNull
    private final IProviderHandler providerHandler = new ProviderHandler(this);

    private int version = -1;

    public StandardRequestManager(@NotNull final IColony colony)
    {
        this.colony = colony;
        this.logger = LogManager.getLogger(String.format("%s.requestsystem.%s", Constants.MOD_ID, colony.getID()));
        reset();
    }

    private void setup()
    {
        dataStoreManager = StandardFactoryController.getInstance().getNewInstance(TypeConstants.DATA_STORE_MANAGER);

        requestIdentitiesDataStoreId = registerDataStore(TypeConstants.REQUEST_IDENTITIES_DATA_STORE);
        requestResolverIdentitiesDataStoreId = registerDataStore(TypeConstants.REQUEST_RESOLVER_IDENTITIES_DATA_STORE);
        providerRequestResolverAssignmentDataStoreId = registerDataStore(TypeConstants.PROVIDER_REQUEST_RESOLVER_ASSIGNMENT_DATA_STORE);
        requestResolverRequestAssignmentDataStoreId = registerDataStore(TypeConstants.REQUEST_RESOLVER_REQUEST_ASSIGNMENT_DATA_STORE);
        requestableTypeRequestResolverAssignmentDataStoreId = registerDataStore(TypeConstants.REQUESTABLE_TYPE_REQUEST_RESOLVER_ASSIGNMENT_DATA_STORE);

        final IRequestResolver<?> playerRequestResolver = StandardFactoryController.getInstance().getNewInstance(TypeConstants.PLAYER_REQUEST_RESOLVER, this);
        final IRequestResolver<?> retryingRequestResolver = StandardFactoryController.getInstance().getNewInstance(TypeConstants.RETRYING_REQUEST_RESOLVER, this);

        getResolverHandler().registerResolver(playerRequestResolver);
        getResolverHandler().registerResolver(retryingRequestResolver);

        this.playerRequestResolverId = playerRequestResolver.getId();
        this.retryingRequestResolverId = retryingRequestResolver.getId();
    }

    private IToken<?> registerDataStore(TypeToken<? extends IDataStore> typeToken)
    {
        return dataStoreManager.get(StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN), typeToken)
                 .getId();
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
    public <T extends IRequestable> IToken<?> createRequest(@NotNull final IRequester requester, @NotNull final T object)
    {
        final IRequest<T> request = getRequestHandler().createRequest(requester, object);
        markDirty();
        return request.getId();
    }

    /**
     * Mark the request manager and colony as dirty.
     */
    @Override
    public void markDirty()
    {
        this.setDirty(true);
    }

    /**
     * Check if the request manager is dirty.
     * @return true if so.
     */
    @Override
    public boolean isDirty()
    {
        return dirty;
    }

    @Override
    public void setDirty(final boolean isDirty)
    {
        this.dirty = isDirty;

        if (this.isDirty())
        {
            colony.markDirty();
        }
    }

    /**
     * Method used to assign a request to a resolver.
     *
     * @param token The token of the request to assign.
     * @throws IllegalArgumentException when the token is not registered to a request, or is already assigned to a resolver.
     */
    @Override
    public void assignRequest(@NotNull final IToken<?> token)
    {
        getRequestHandler().assignRequest(getRequestHandler().getRequest(token));
        markDirty();
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
    public <T extends IRequestable> IToken<?> createAndAssignRequest(@NotNull final IRequester requester, @NotNull final T object)
    {
        final IToken<?> token = createRequest(requester, object);
        assignRequest(token);
        return token;
    }

    @Override
    @Nullable
    public IToken<?> reassignRequest(@NotNull final IToken<?> token, @NotNull final Collection<IToken<?>> resolverTokenBlackList)
    {
        final IRequest<?> request = getRequestHandler().getRequest(token);
        markDirty();
        return getRequestHandler().reassignRequest(request, resolverTokenBlackList);
    }

    @Nullable
    @Override
    public IRequest<?> getRequestForToken(@NotNull final IToken<?> token) throws IllegalArgumentException
    {
        final IRequest<?> internalRequest = getRequestHandler().getRequestOrNull(token);

        return internalRequest;
    }

    @NotNull
    @Override
    public IRequestResolver<?> getResolverForToken(@NotNull final IToken<?> token) throws IllegalArgumentException
    {
        return getResolverHandler().getResolver(token);
    }

    @Nullable
    @Override
    public IRequestResolver<?> getResolverForRequest(@NotNull final IToken<?> requestToken) throws IllegalArgumentException
    {
        final IRequest<?> request = getRequestHandler().getRequest(requestToken);

        return getResolverForToken(getResolverHandler().getResolverForRequest(request).getId());
    }

    /**
     * Method to update the state of a given request.
     *
     * @param token The token that represents a given request to update.
     * @param state The new state of that request.
     * @throws IllegalArgumentException when the token is unknown to this manager.
     */
    @Override
    public void updateRequestState(@NotNull final IToken<?> token, @NotNull final RequestState state)
    {
        final IRequest<?> request = getRequestHandler().getRequest(token);

        getLogger().debug("Updating request state from:" + token + ". With original state: " + request.getState() + " to : " + state);

        request.setState(new WrappedStaticStateRequestManager(this), state);
        markDirty();

        switch (request.getState())
        {
            case RESOLVED:
                getLogger().debug("Request resolved: " + token + ". Determining followup requests...");
                getRequestHandler().onRequestResolved(token);
                return;
            case COMPLETED:
                getLogger().debug("Request completed: " + token + ". Notifying parent and requester...");
                getRequestHandler().onRequestCompleted(token);
                return;
            case OVERRULED:
                getLogger().debug("Request overruled: " + token + ". Notifying parent, children and requester...");
                getRequestHandler().onRequestOverruled(token);
                return;
            case CANCELLED:
                getLogger().debug("Request cancelled: " + token + ". Notifying parent, children and requester...");
                getRequestHandler().onRequestCancelled(token);
                return;
            case RECEIVED:
                getLogger().debug("Request received: " + token + ". Removing from system...");
                getRequestHandler().cleanRequestData(token);
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
    public void onProviderAddedToColony(@NotNull final IRequestResolverProvider provider)
    {
        getProviderHandler().registerProvider(provider);
    }

    @Override
    public void overruleRequest(@NotNull final IToken<?> token, @Nullable final ItemStack stack)
    {
        final IRequest<?> request = getRequestHandler().getRequest(token);

        if (!ItemStackUtils.isEmpty(stack))
        {
            request.overrideCurrentDeliveries(ImmutableList.of(stack));
        }

        updateRequestState(token, RequestState.OVERRULED);
    }

    /**
     * Method used to indicate to this manager that Provider has been removed from the colony.
     *
     * @param provider The removed provider.
     */
    @Override
    public void onProviderRemovedFromColony(@NotNull final IRequestResolverProvider provider) throws IllegalArgumentException
    {
        getProviderHandler().removeProvider(provider);
    }

    /**
     * Method used to reassign requests based on a predicate.
     *
     * @param shouldTriggerReassign The predicate to determine if the request should be reassigned.
     */
    @Override
    public void onColonyUpdate(@NotNull final Predicate<IRequest> shouldTriggerReassign)
    {
        getResolverHandler().onColonyUpdate(shouldTriggerReassign);
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
        return (IPlayerRequestResolver) getResolverHandler().getResolver(playerRequestResolverId);
    }

    @NotNull
    @Override
    public IRetryingRequestResolver getRetryingRequestResolver()
    {
        return (IRetryingRequestResolver) getResolverHandler().getResolver(retryingRequestResolverId);
    }

    @NotNull
    @Override
    public IDataStoreManager getDataStoreManager()
    {
        return dataStoreManager;
    }

    @Override
    public void reset()
    {
        setup();

        version = -1;
        getUpdateHandler().handleUpdate();
    }

    /**
     * Method used to serialize the current request system to NBT.
     *
     * @return The NBTData that describes the current request system
     */
    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT systemCompound = new CompoundNBT();
        systemCompound.putInt(NBT_VERSION, version);

        systemCompound.put(NBT_DATASTORE, getFactoryController().serialize(dataStoreManager));
        systemCompound.put(NBT_ID_REQUEST_IDENTITIES, getFactoryController().serialize(requestIdentitiesDataStoreId));
        systemCompound.put(NBT_ID_REQUEST_RESOLVER_IDENTITIES, getFactoryController().serialize(requestResolverIdentitiesDataStoreId));
        systemCompound.put(NBT_ID_PROVIDER_ASSIGNMENTS, getFactoryController().serialize(providerRequestResolverAssignmentDataStoreId));
        systemCompound.put(NBT_ID_REQUEST_RESOLVER_ASSIGNMENTS, getFactoryController().serialize(requestResolverRequestAssignmentDataStoreId));
        systemCompound.put(NBT_ID_REQUESTABLE_TYPE_ASSIGNMENTS, getFactoryController().serialize(requestableTypeRequestResolverAssignmentDataStoreId));

        systemCompound.put(NBT_ID_PLAYER, getFactoryController().serialize(playerRequestResolverId));
        systemCompound.put(NBT_ID_RETRYING, getFactoryController().serialize(retryingRequestResolverId));

        return systemCompound;
    }

    /**
     * Method used to deserialize the data inside the given nbt tag into this request system.
     *
     * @param nbt The data to deserialize.
     */
    @Override
    public void deserializeNBT(final CompoundNBT nbt)
    {
        executeDeserializationStepOrMarkForUpdate(nbt,
          NBT_VERSION,
          CompoundNBT::getInt,
          v -> version = v);

        executeDeserializationStepOrMarkForUpdate(nbt,
          NBT_DATASTORE,
          CompoundNBT::getCompound,
          c -> dataStoreManager = getFactoryController().deserialize(c));

        executeDeserializationStepOrMarkForUpdate(nbt,
          NBT_ID_REQUEST_IDENTITIES,
          CompoundNBT::getCompound,
          c -> requestIdentitiesDataStoreId = getFactoryController().deserialize(c));
        executeDeserializationStepOrMarkForUpdate(nbt,
          NBT_ID_REQUEST_RESOLVER_IDENTITIES,
          CompoundNBT::getCompound,
          c -> requestResolverIdentitiesDataStoreId = getFactoryController().deserialize(c));
        executeDeserializationStepOrMarkForUpdate(nbt,
          NBT_ID_PROVIDER_ASSIGNMENTS,
          CompoundNBT::getCompound,
          c -> providerRequestResolverAssignmentDataStoreId = getFactoryController().deserialize(c));
        executeDeserializationStepOrMarkForUpdate(nbt,
          NBT_ID_REQUEST_RESOLVER_ASSIGNMENTS,
          CompoundNBT::getCompound,
          c -> requestResolverRequestAssignmentDataStoreId = getFactoryController().deserialize(c));
        executeDeserializationStepOrMarkForUpdate(nbt,
          NBT_ID_REQUESTABLE_TYPE_ASSIGNMENTS,
          CompoundNBT::getCompound,
          c -> requestableTypeRequestResolverAssignmentDataStoreId = getFactoryController().deserialize(c));

        executeDeserializationStepOrMarkForUpdate(nbt,
          NBT_ID_PLAYER,
          CompoundNBT::getCompound,
          c -> playerRequestResolverId = getFactoryController().deserialize(c));

        executeDeserializationStepOrMarkForUpdate(nbt,
          NBT_ID_RETRYING,
          CompoundNBT::getCompound,
          c -> retryingRequestResolverId = getFactoryController().deserialize(c));

        updateIfRequired();
    }

    private <T> void executeDeserializationStepOrMarkForUpdate(@NotNull final CompoundNBT nbt, @NotNull final String key, @NotNull final BiFunction<CompoundNBT, String, T> extractor, @NotNull final Consumer<T> valueConsumer)
    {
        if (!nbt.keySet().contains(key))
        {
            markForUpdate();
            return;
        }

        T base;
        try {
            base = extractor.apply(nbt, key);

        }
        catch (Exception ex)
        {
            markForUpdate();
            return;
        }

        valueConsumer.accept(base);
    }

    private void markForUpdate()
    {
        version = -1;
    }

    @Override
    public Logger getLogger()
    {
        return logger;
    }

    @Override
    public void tick()
    {
        this.getRetryingRequestResolver().updateManager(this);
        this.getRetryingRequestResolver().tick();
    }

    @NotNull
    @Override
    public IRequestIdentitiesDataStore getRequestIdentitiesDataStore()
    {
        return dataStoreManager.get(requestIdentitiesDataStoreId, TypeConstants.REQUEST_IDENTITIES_DATA_STORE);
    }

    @NotNull
    @Override
    public IRequestResolverIdentitiesDataStore getRequestResolverIdentitiesDataStore()
    {
        return dataStoreManager.get(requestResolverIdentitiesDataStoreId, TypeConstants.REQUEST_RESOLVER_IDENTITIES_DATA_STORE);
    }

    @NotNull
    @Override
    public IProviderResolverAssignmentDataStore getProviderResolverAssignmentDataStore()
    {
        return dataStoreManager.get(providerRequestResolverAssignmentDataStoreId, TypeConstants.PROVIDER_REQUEST_RESOLVER_ASSIGNMENT_DATA_STORE);
    }

    @NotNull
    @Override
    public IRequestResolverRequestAssignmentDataStore getRequestResolverRequestAssignmentDataStore()
    {
        return dataStoreManager.get(requestResolverRequestAssignmentDataStoreId, TypeConstants.REQUEST_RESOLVER_REQUEST_ASSIGNMENT_DATA_STORE);
    }

    @NotNull
    @Override
    public IRequestableTypeRequestResolverAssignmentDataStore getRequestableTypeRequestResolverAssignmentDataStore()
    {
        return dataStoreManager.get(requestableTypeRequestResolverAssignmentDataStoreId, TypeConstants.REQUESTABLE_TYPE_REQUEST_RESOLVER_ASSIGNMENT_DATA_STORE);
    }

    @Override
    public IProviderHandler getProviderHandler()
    {
        return providerHandler;
    }

    @Override
    public IRequestHandler getRequestHandler()
    {
        return requestHandler;
    }

    @Override
    public IResolverHandler getResolverHandler()
    {
        return resolverHandler;
    }

    @Override
    public ITokenHandler getTokenHandler()
    {
        return tokenHandler;
    }

    @Override
    public IUpdateHandler getUpdateHandler()
    {
        return updateHandler;
    }

    private void updateIfRequired()
    {
        if (version < updateHandler.getCurrentVersion())
        {
            reset();
        }
    }

    @Override
    public int getCurrentVersion()
    {
        return version;
    }

    @Override
    public void setCurrentVersion(final int currentVersion)
    {
        this.version = currentVersion;
    }
}
