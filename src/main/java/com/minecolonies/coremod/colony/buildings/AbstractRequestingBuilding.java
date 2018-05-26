package com.minecolonies.coremod.colony.buildings;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.data.IRequestSystemBuildingDataStore;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolverProvider;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.ReflectionUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.requestsystem.requesters.BuildingBasedRequester;
import com.minecolonies.coremod.colony.requestsystem.resolvers.BuildingRequestResolver;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.Suppression.*;

/**
 * Abstract class handling requests from the building side.
 */
public abstract class AbstractRequestingBuilding extends AbstractSchematicProvider implements IRequestResolverProvider, IRequester
{
    /**
     * The colony the building belongs to.
     */
    @NotNull
    protected final Colony colony;

    /**
     * The data store id for request system related data.
     */
    @NotNull
    private IToken<?> rsDataStoreToken;

    /**
     * The ID of the building. Needed in the request system to identify it.
     */
    private IRequester requester;

    /**
     * Constructor for the abstract class which receives the position and colony.
     * @param pos
     * @param colony
     */
    public AbstractRequestingBuilding(final BlockPos pos, final Colony colony)
    {
        super(pos);
        this.colony = colony;

        this.requester = StandardFactoryController.getInstance().getNewInstance(TypeToken.of(BuildingBasedRequester.class), this);
        setupRsDataStore();
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        loadRequestSystemFromNBT(compound);
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        writeRequestSystemToNBT(compound);
    }

    /**
     * Returns the colony of the building.
     *
     * @return {@link com.minecolonies.coremod.colony.Colony} of the current object.
     */
    @NotNull
    public Colony getColony()
    {
        return colony;
    }


    //------------------------- !START! RequestSystem handling for minecolonies buildings -------------------------//

    protected void writeRequestSystemToNBT(final NBTTagCompound compound)
    {
        compound.setTag(TAG_RS_BUILDING_DATASTORE, StandardFactoryController.getInstance().serialize(rsDataStoreToken));
    }

    protected void setupRsDataStore()
    {
        this.rsDataStoreToken = colony.getRequestManager()
                .getDataStoreManager()
                .get(
                        StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
                        TypeConstants.REQUEST_SYSTEM_BUILDING_DATA_STORE
                )
                .getId();
    }

    private void loadRequestSystemFromNBT(final NBTTagCompound compound)
    {
        if (compound.hasKey(TAG_REQUESTOR_ID))
        {
            this.requester = StandardFactoryController.getInstance().deserialize(compound.getCompoundTag(TAG_REQUESTOR_ID));
        }
        else
        {
            this.requester = StandardFactoryController.getInstance().getNewInstance(TypeToken.of(BuildingBasedRequester.class), this);
        }

        if (compound.hasKey(TAG_RS_BUILDING_DATASTORE))
        {
            this.rsDataStoreToken = StandardFactoryController.getInstance().deserialize(compound.getCompoundTag(TAG_RS_BUILDING_DATASTORE));
        }
        else
        {
            setupRsDataStore();
        }
    }

    private IRequestSystemBuildingDataStore getDataStore()
    {
        return colony.getRequestManager().getDataStoreManager().get(rsDataStoreToken, TypeConstants.REQUEST_SYSTEM_BUILDING_DATA_STORE);
    }

    private Map<TypeToken<?>, Collection<IToken<?>>> getOpenRequestsByRequestableType()
    {
        return getDataStore().getOpenRequestsByRequestableType();
    }

    protected Map<Integer, Collection<IToken<?>>> getOpenRequestsByCitizen()
    {
        return getDataStore().getOpenRequestsByCitizen();
    }

    private Map<Integer, Collection<IToken<?>>> getCompletedRequestsByCitizen()
    {
        return getDataStore().getCompletedRequestsByCitizen();
    }

    private Map<IToken<?>, Integer> getCitizensByRequest()
    {
        return getDataStore().getCitizensByRequest();
    }

    public <R extends IRequestable> IToken<?> createRequest(@NotNull final CitizenData citizenData, @NotNull final R requested)
    {
        final IToken requestToken = colony.getRequestManager().createRequest(requester, requested);

        addRequestToMaps(citizenData.getId(), requestToken, TypeToken.of(requested.getClass()));

        colony.getRequestManager().assignRequest(requestToken);

        markDirty();

        return requestToken;
    }

    /**
     * Internal method used to register a new Request to the request maps.
     * Helper method.
     *
     * @param citizenId    The id of the citizen.
     * @param requestToken The {@link IToken} that is used to represent the request.
     * @param requested    The class of the type that has been requested eg. {@code ItemStack.class}
     */
    private void addRequestToMaps(@NotNull final Integer citizenId, @NotNull final IToken requestToken, @NotNull final TypeToken requested)
    {
        if (!getOpenRequestsByRequestableType().containsKey(requested))
        {
            getOpenRequestsByRequestableType().put(requested, new ArrayList<>());
        }
        getOpenRequestsByRequestableType().get(requested).add(requestToken);

        getCitizensByRequest().put(requestToken, citizenId);

        if (!getOpenRequestsByCitizen().containsKey(citizenId))
        {
            getOpenRequestsByCitizen().put(citizenId, new ArrayList<>());
        }
        getOpenRequestsByCitizen().get(citizenId).add(requestToken);
    }

    public boolean hasWorkerOpenRequests(@NotNull final CitizenData citizen)
    {
        return !getOpenRequests(citizen).isEmpty();
    }

    @SuppressWarnings(RAWTYPES)
    public ImmutableList<IRequest> getOpenRequests(@NotNull final CitizenData data)
    {
        if (!getOpenRequestsByCitizen().containsKey(data.getId()))
        {
            return ImmutableList.of();
        }

        return ImmutableList.copyOf(getOpenRequestsByCitizen().get(data.getId())
                .stream()
                .map(getColony().getRequestManager()::getRequestForToken)
                .filter(Objects::nonNull)
                .iterator());
    }

    @SuppressWarnings(RAWTYPES)
    public boolean hasWorkerOpenRequestsFiltered(@NotNull final CitizenData citizen, @NotNull final Predicate<IRequest> selectionPredicate)
    {
        return getOpenRequests(citizen).stream().anyMatch(selectionPredicate);
    }

    public <R> boolean hasWorkerOpenRequestsOfType(@NotNull final CitizenData citizenData, final TypeToken<R> requestType)
    {
        return !getOpenRequestsOfType(citizenData, requestType).isEmpty();
    }

    @SuppressWarnings({GENERIC_WILDCARD, UNCHECKED, RAWTYPES})
    public <R> ImmutableList<IRequest<? extends R>> getOpenRequestsOfType(
            @NotNull final CitizenData citizenData,
            final TypeToken<R> requestType)
    {
        return ImmutableList.copyOf(getOpenRequests(citizenData).stream()
                .filter(request -> {
                    final Set<TypeToken> requestTypes = ReflectionUtils.getSuperClasses(request.getRequestType());
                    return requestTypes.contains(requestType);
                })
                .map(request -> (IRequest<? extends R>) request)
                .iterator());
    }

    public boolean hasCitizenCompletedRequests(@NotNull final CitizenData data)
    {
        return !getCompletedRequests(data).isEmpty();
    }

    @SuppressWarnings(RAWTYPES)
    public ImmutableList<IRequest> getCompletedRequests(@NotNull final CitizenData data)
    {
        if (!getCompletedRequestsByCitizen().containsKey(data.getId()))
        {
            return ImmutableList.of();
        }

        return ImmutableList.copyOf(getCompletedRequestsByCitizen().get(data.getId()).stream()
                .map(getColony().getRequestManager()::getRequestForToken).filter(Objects::nonNull).iterator());
    }

    @SuppressWarnings({GENERIC_WILDCARD, RAWTYPES, UNCHECKED})
    public <R> ImmutableList<IRequest<? extends R>> getCompletedRequestsOfType(@NotNull final CitizenData citizenData, final TypeToken<R> requestType)
    {
        return ImmutableList.copyOf(getCompletedRequests(citizenData).stream()
                .filter(request -> {
                    final Set<TypeToken> requestTypes = ReflectionUtils.getSuperClasses(request.getRequestType());
                    return requestTypes.contains(requestType);
                })
                .map(request -> (IRequest<? extends R>) request)
                .iterator());
    }

    @SuppressWarnings({GENERIC_WILDCARD, RAWTYPES, UNCHECKED})
    public <R> ImmutableList<IRequest<? extends R>> getCompletedRequestsOfTypeFiltered(
            @NotNull final CitizenData citizenData,
            final TypeToken<R> requestType,
            final Predicate<IRequest<? extends R>> filter)
    {
        return ImmutableList.copyOf(getCompletedRequests(citizenData).stream()
                .filter(request -> {
                    final Set<TypeToken> requestTypes = ReflectionUtils.getSuperClasses(request.getRequestType());
                    return requestTypes.contains(requestType);
                })
                .map(request -> (IRequest<? extends R>) request)
                .filter(filter)
                .iterator());
    }

    public void markRequestAsAccepted(@NotNull final CitizenData data, @NotNull final IToken<?> token)
    {
        if (!getCompletedRequestsByCitizen().containsKey(data.getId()) || !getCompletedRequestsByCitizen().get(data.getId()).contains(token))
        {
            throw new IllegalArgumentException("The given token " + token + " is not known as a completed request waiting for acceptance by the citizen.");
        }

        getCompletedRequestsByCitizen().get(data.getId()).remove(token);
        if (getCompletedRequestsByCitizen().get(data.getId()).isEmpty())
        {
            getCompletedRequestsByCitizen().remove(data.getId());
        }

        getColony().getRequestManager().updateRequestState(token, RequestState.RECEIVED);
        markDirty();
    }

    public void cancelAllRequestsOfCitizen(@NotNull final CitizenData data)
    {
        getOpenRequests(data).forEach(request ->
        {
            getColony().getRequestManager().updateRequestState(request.getToken(), RequestState.CANCELLED);

            if (getOpenRequestsByRequestableType().containsKey(TypeToken.of(request.getRequest().getClass())))
            {
                getOpenRequestsByRequestableType().get(TypeToken.of(request.getRequest().getClass())).remove(request.getToken());
                if (getOpenRequestsByRequestableType().get(TypeToken.of(request.getRequest().getClass())).isEmpty())
                {
                    getOpenRequestsByRequestableType().remove(TypeToken.of(request.getRequest().getClass()));
                }
            }

            getCitizensByRequest().remove(request.getToken());
        });

        getCompletedRequests(data).forEach(request -> getColony().getRequestManager().updateRequestState(request.getToken(), RequestState.RECEIVED));

        if (getOpenRequestsByCitizen().containsKey(data.getId()))
        {
            getOpenRequestsByCitizen().remove(data.getId());
        }

        if (getCompletedRequestsByCitizen().containsKey(data.getId()))
        {
            getCompletedRequestsByCitizen().remove(data.getId());
        }

        markDirty();
    }

    /**
     * Overrule the next open request with a give stack.
     * <p>
     * We squid:s135 which takes care that there are not too many continue statements in a loop since it makes sense here
     * out of performance reasons.
     *
     * @param stack the stack.
     */
    @SuppressWarnings("squid:S135")
    public void overruleNextOpenRequestWithStack(@NotNull final ItemStack stack)
    {
        if (ItemStackUtils.isEmpty(stack))
        {
            return;
        }

        for (final int citizenId : getOpenRequestsByCitizen().keySet())
        {
            final CitizenData data = getColony().getCitizenManager().getCitizen(citizenId);

            if (data == null)
            {
                continue;
            }

            final IRequest<? extends IDeliverable> target = getFirstOverullingRequestFromInputList(getOpenRequestsOfType(data, TypeConstants.DELIVERABLE), stack);

            if (target == null)
            {
                continue;
            }

            getColony().getRequestManager().overruleRequest(target.getToken(), stack.copy());
            return;
        }
    }

    @SuppressWarnings({GENERIC_WILDCARD, UNCHECKED, RAWTYPES})
    public <R> ImmutableList<IRequest<? extends R>> getOpenRequestsOfTypeFiltered(
            @NotNull final CitizenData citizenData,
            final TypeToken<R> requestType,
            final Predicate<IRequest<? extends R>> filter)
    {
        return ImmutableList.copyOf(getOpenRequests(citizenData).stream()
                .filter(request -> {
                    final Set<TypeToken> requestTypes = ReflectionUtils.getSuperClasses(request.getRequestType());
                    return requestTypes.contains(requestType);
                })
                .map(request -> (IRequest<? extends R>) request)
                .filter(filter)
                .iterator());
    }

    public boolean overruleNextOpenRequestOfCitizenWithStack(@NotNull final CitizenData citizenData, @NotNull final ItemStack stack)
    {
        if (ItemStackUtils.isEmpty(stack))
        {
            return false;
        }

        final IRequest<? extends IDeliverable> target = getFirstOverullingRequestFromInputList(getOpenRequestsOfType(citizenData, TypeConstants.DELIVERABLE),stack);

        if (target == null)
        {
            return false;
        }

        getColony().getRequestManager().overruleRequest(target.getToken(), stack.copy());
        return true;
    }

    private IRequest<? extends IDeliverable> getFirstOverullingRequestFromInputList(@NotNull final Collection<IRequest<? extends IDeliverable>> queue, @NotNull final ItemStack stack)
    {
        if (queue.isEmpty())
        {
            return null;
        }

        return queue
                .stream()
                .filter(request -> request.getRequest().matches(stack))
                .findFirst()
                .orElseGet(() ->
                        getFirstOverullingRequestFromInputList(queue
                                        .stream()
                                        .flatMap(r -> flattenDeliverableChildRequests(r).stream())
                                        .collect(Collectors.toList()),
                                stack));
    }

    private Collection<IRequest<? extends IDeliverable>> flattenDeliverableChildRequests(@NotNull final IRequest<? extends IDeliverable> request)
    {
        if (!request.hasChildren())
        {
            return ImmutableList.of();
        }

        return request.getChildren()
                .stream()
                .map(getColony().getRequestManager()::getRequestForToken)
                .filter(Objects::nonNull)
                .filter(request1 -> request1.getRequest() instanceof IDeliverable)
                .map(request1 -> (IRequest<? extends IDeliverable>) request1)
                .collect(Collectors.toList());
    }

    @Override
    public IToken<?> getRequesterId()
    {
        return getToken();
    }

    @Override
    public IToken<?> getToken()
    {
        return requester.getRequesterId();
    }

    @Override
    public ImmutableCollection<IRequestResolver<?>> getResolvers()
    {
        return ImmutableList.of(new BuildingRequestResolver(getRequester().getRequesterLocation(), getColony().getRequestManager().getFactoryController().getNewInstance(
                TypeConstants.ITOKEN)));
    }

    public IRequester getRequester()
    {
        return requester;
    }

    @NotNull
    @Override
    public ILocation getRequesterLocation()
    {
        return getRequester().getRequesterLocation();
    }

    @Override
    public void onRequestComplete(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        final Integer citizenThatRequested = getCitizensByRequest().remove(token);
        getOpenRequestsByCitizen().get(citizenThatRequested).remove(token);

        if (getOpenRequestsByCitizen().get(citizenThatRequested).isEmpty())
        {
            getOpenRequestsByCitizen().remove(citizenThatRequested);
        }

        final IRequest<?> requestThatCompleted = getColony().getRequestManager().getRequestForToken(token);
        getOpenRequestsByRequestableType().get(TypeToken.of(requestThatCompleted.getRequest().getClass())).remove(token);

        if (getOpenRequestsByRequestableType().get(TypeToken.of(requestThatCompleted.getRequest().getClass())).isEmpty())
        {
            getOpenRequestsByRequestableType().remove(TypeToken.of(requestThatCompleted.getRequest().getClass()));
        }

        if (!getCompletedRequestsByCitizen().containsKey(citizenThatRequested))
        {
            getCompletedRequestsByCitizen().put(citizenThatRequested, new ArrayList<>());
        }
        getCompletedRequestsByCitizen().get(citizenThatRequested).add(token);

        markDirty();
    }

    @Override
    @NotNull
    public void onRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IToken token)
    {
        final int citizenThatRequested = getCitizensByRequest().remove(token);
        getOpenRequestsByCitizen().get(citizenThatRequested).remove(token);

        if (getOpenRequestsByCitizen().get(citizenThatRequested).isEmpty())
        {
            getOpenRequestsByCitizen().remove(citizenThatRequested);
        }

        final IRequest<?> requestThatCompleted = getColony().getRequestManager().getRequestForToken(token);
        if (requestThatCompleted != null && getOpenRequestsByRequestableType().containsKey(TypeToken.of(requestThatCompleted.getRequest().getClass())))
        {
            getOpenRequestsByRequestableType().get(TypeToken.of(requestThatCompleted.getRequest().getClass())).remove(token);
            if (getOpenRequestsByRequestableType().get(TypeToken.of(requestThatCompleted.getRequest().getClass())).isEmpty())
            {
                getOpenRequestsByRequestableType().remove(TypeToken.of(requestThatCompleted.getRequest().getClass()));
            }
        }

        //Check if the citizen did not die.
        if (getColony().getCitizenManager().getCitizen(citizenThatRequested) != null)
        {
            getColony().getCitizenManager().getCitizen(citizenThatRequested).onRequestCancelled(token);
        }
        markDirty();
    }

    @NotNull
    @Override
    public ITextComponent getDisplayName(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        if (!getCitizensByRequest().containsKey(token))
        {
            return new TextComponentString("<UNKNOWN>");
        }

        final Integer citizenData = getCitizensByRequest().get(token);
        return new TextComponentString(this.getSchematicName() + " " + getColony().getCitizenManager().getCitizen(citizenData).getName());
    }

    public Optional<CitizenData> getCitizenForRequest(@NotNull final IToken token)
    {
        if (!getCitizensByRequest().containsKey(token) || getColony() == null)
        {
            return Optional.empty();
        }

        final int citizenID = getCitizensByRequest().get(token);
        if(getColony().getCitizenManager().getCitizen(citizenID) == null)
        {
            return Optional.empty();
        }

        return Optional.of(getColony().getCitizenManager().getCitizen(citizenID));
    }


    //------------------------- !END! RequestSystem handling for minecolonies buildings -------------------------//
}
