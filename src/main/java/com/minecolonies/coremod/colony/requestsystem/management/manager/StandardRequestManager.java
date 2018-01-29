package com.minecolonies.coremod.colony.requestsystem.management.manager;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.data.*;
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
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;
import com.minecolonies.coremod.colony.requestsystem.management.handlers.*;
import com.minecolonies.coremod.colony.requestsystem.management.manager.wrapped.WrappedStaticStateRequestManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static com.minecolonies.api.util.constant.Suppression.*;

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
    private static final String NBT_PLAYER                        = "Player";
    private static final String NBT_RETRYING                      = "Retrying";
    private static final String NBT_VERSION = "Version";
    ////---------------------------NBTTags-------------------------\\\\

    private IToken<?> requestIdentitiesDataStoreId;

    private IToken<?> requestResolverIdentitiesDataStoreId;

    private IToken<?> providerRequestResolverAssignmentDataStoreId;

    private IToken<?> requestResolverRequestAssignmentDataStoreId;

    private IToken<?> requestableTypeRequestResolverAssignmentDataStoreId;

    private IDataStoreManager dataStoreManager;

    /**
     * Colony of the manager.
     */
    @NotNull
    private final IColony colony;
    /**
     * The fallback resolver used to resolve directly to the player.
     */
    @NotNull
    private IPlayerRequestResolver                       playerResolver          = null;
    /**
     * The fallback resolver used to resolve using retries.
     * Not all requests might support this feature, requests that do should implement {@link IRetryable} on their requestable.
     * Anything that implements {@link IDeliverable} is by definition retryable.
     */
    @NotNull
    private IRetryingRequestResolver retryingResolver = null;

    @NotNull
    private int version = -1;

    public StandardRequestManager(final IColony colony)
    {
        this.colony = colony;
        setup();

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

        setup();
    }

    private void setup()
    {
        dataStoreManager = StandardFactoryController.getInstance().getNewInstance(TypeConstants.DATA_STORE_MANAGER);

        requestIdentitiesDataStoreId = registerDataStore(TypeConstants.REQUEST_IDENTITIES_DATA_STORE);
        requestResolverIdentitiesDataStoreId = registerDataStore(TypeConstants.REQUEST_RESOLVER_IDENTITIES_DATA_STORE);
        providerRequestResolverAssignmentDataStoreId = registerDataStore(TypeConstants.PROVIDER_REQUEST_RESOLVER_ASSIGNMENT_DATA_STORE);
        requestResolverRequestAssignmentDataStoreId = registerDataStore(TypeConstants.REQUEST_RESOLVER_REQUEST_ASSIGNMENT_DATA_STORE);
        requestableTypeRequestResolverAssignmentDataStoreId = registerDataStore(TypeConstants.REQUESTABLE_TYPE_REQUEST_RESOLVER_ASSIGNMENT_DATA_STORE);
    }

    private IToken<?> registerDataStore(TypeToken<? extends IDataStore> typeToken)
    {
        return dataStoreManager.get(StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
          StandardFactoryController.getInstance().getNewInstance(typeToken))
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
    public void assignRequest(@NotNull final IToken<?> token)
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
        final IRequest<?> request = RequestHandler.getRequest(this, token);
        return RequestHandler.reassignRequest(this, request, resolverTokenBlackList);
    }

    @Nullable
    @Override
    public IRequest<?> getRequestForToken(@NotNull final IToken<?> token) throws IllegalArgumentException
    {
        final IRequest<?> internalRequest = RequestHandler.getRequestOrNull(this, token);

        if (internalRequest == null)
        {
            return null;
        }

        return internalRequest;
    }

    @NotNull
    @Override
    public IRequestResolver<?> getResolverForToken(@NotNull final IToken<?> token) throws IllegalArgumentException
    {
        return ResolverHandler.getResolver(this, token);
    }

    @Nullable
    @Override
    public IRequestResolver<?> getResolverForRequest(@NotNull final IToken<?> requestToken) throws IllegalArgumentException
    {
        final IRequest<?> request = RequestHandler.getRequest(this, requestToken);

        return getResolverForToken(ResolverHandler.getResolverForRequest(this, request).getRequesterId());
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
        final IRequest<?> request = RequestHandler.getRequest(this, token);

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
    public void overruleRequest(@NotNull final IToken<?> token, @Nullable final ItemStack stack)
    {
        final IRequest<?> request = RequestHandler.getRequest(this, token);

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
    public void onProviderAddedToColony(@NotNull final IRequestResolverProvider provider)
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

    @NotNull
    @Override
    public IDataStoreManager getDataStoreManager()
    {
        return dataStoreManager;
    }

    @Override
    public void reset()
    {
        dataStoreManager.removeAll();
        setup();

        this.playerResolver.onSystemReset();
        this.retryingResolver.onSystemReset();

        ResolverHandler.registerResolver(this, this.playerResolver);
        ResolverHandler.registerResolver(this, this.retryingResolver);

        version = -1;
        UpdateHandler.handleUpdate(this);
    }

    /**
     * Method used to serialize the current request system to NBT.
     *
     * @return The NBTData that describes the current request system
     */
    @Override
    public NBTTagCompound serializeNBT()
    {
        final NBTTagCompound systemCompound = new NBTTagCompound();

        if (this.playerResolver != null)
        {
            systemCompound.setTag(NBT_PLAYER, getFactoryController().serialize(playerResolver));
        }

        if (this.retryingResolver != null)
        {
            systemCompound.setTag(NBT_RETRYING, getFactoryController().serialize(retryingResolver));
        }

        systemCompound.setInteger(NBT_VERSION, version);

        systemCompound.setTag(NBT_DATASTORE, getFactoryController().serialize(dataStoreManager));
        systemCompound.setTag(NBT_ID_REQUEST_IDENTITIES, getFactoryController().serialize(requestIdentitiesDataStoreId));
        systemCompound.setTag(NBT_ID_REQUEST_RESOLVER_IDENTITIES, getFactoryController().serialize(requestResolverIdentitiesDataStoreId));
        systemCompound.setTag(NBT_ID_PROVIDER_ASSIGNMENTS, getFactoryController().serialize(providerRequestResolverAssignmentDataStoreId));
        systemCompound.setTag(NBT_ID_REQUEST_RESOLVER_ASSIGNMENTS, getFactoryController().serialize(requestResolverRequestAssignmentDataStoreId));
        systemCompound.setTag(NBT_ID_REQUESTABLE_TYPE_ASSIGNMENTS, getFactoryController().serialize(requestableTypeRequestResolverAssignmentDataStoreId));

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

        if (nbt.hasKey(NBT_VERSION))
        {
            version = nbt.getInteger(NBT_VERSION);
        }

        if (nbt.hasKey(NBT_DATASTORE))
        {
            dataStoreManager = getFactoryController().deserialize(nbt.getCompoundTag(NBT_DATASTORE));
            requestIdentitiesDataStoreId = getFactoryController().deserialize(nbt.getCompoundTag(NBT_ID_REQUEST_IDENTITIES));
            requestResolverIdentitiesDataStoreId = getFactoryController().deserialize(nbt.getCompoundTag(NBT_ID_REQUEST_RESOLVER_IDENTITIES));
            providerRequestResolverAssignmentDataStoreId = getFactoryController().deserialize(nbt.getCompoundTag(NBT_ID_PROVIDER_ASSIGNMENTS));
            requestResolverRequestAssignmentDataStoreId = getFactoryController().deserialize(nbt.getCompoundTag(NBT_ID_REQUEST_RESOLVER_ASSIGNMENTS));
            requestableTypeRequestResolverAssignmentDataStoreId = getFactoryController().deserialize(nbt.getCompoundTag(NBT_ID_REQUESTABLE_TYPE_ASSIGNMENTS));
        }
        else
        {
            setup();
        }

        UpdateHandler.handleUpdate(this );
    }

    @Override
    public void update()
    {
        this.retryingResolver.update();
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
