package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.resolver.player.IPlayerRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.requestsystem.requesters.BuildingBasedRequester;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static com.minecolonies.api.util.RSConstants.STANDARD_PLAYER_REQUEST_PRIORITY;

/**
 * Resolver that checks if a deliverable request is already in the building it is being requested from.
 */
public class StandardPlayerRequestResolver implements IPlayerRequestResolver
{

    @NotNull
    private final ILocation location;

    @NotNull
    private final IToken token;

    @NotNull
    private final Set<IToken<?>> assignedRequests = new HashSet<>();

    public StandardPlayerRequestResolver(@NotNull final ILocation location, @NotNull final IToken token)
    {
        super();
        this.location = location;
        this.token = token;
    }

    @Override
    public TypeToken<IRequestable> getRequestType()
    {
        return TypeConstants.REQUESTABLE;
    }

    @Override
    public void onRequestedRequestComplete(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        /**
         * Nothing to do here right now.
         */
    }

    @Override
    public boolean canResolveRequest(@NotNull final IRequestManager manager, final IRequest requestToCheck)
    {
        return !manager.getColony().getWorld().isRemote;
    }

    @Override
    public void onRequestedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {

    }

    @Nullable
    @Override
    public List<IToken<?>> attemptResolveRequest(@NotNull final IRequestManager manager, @NotNull final IRequest request)
    {
        if (canResolveRequest(manager, request))
        {
            return Lists.newArrayList();
        }

        return null;
    }

    @NotNull
    @Override
    public ITextComponent getRequesterDisplayName(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        return new StringTextComponent("Player");
    }

    @Override
    public void resolveRequest(@NotNull final IRequestManager manager, @NotNull final IRequest request) throws RuntimeException
    {
        final IColony colony = manager.getColony();
        if (colony instanceof Colony)
        {
            if (MinecoloniesAPIProxy.getInstance().getConfig().getCommon().creativeResolve.get() &&
                  request.getRequest() instanceof IDeliverable &&
                  request.getRequester() instanceof BuildingBasedRequester &&
                  ((BuildingBasedRequester) request.getRequester()).getBuilding(manager, request.getId()).isPresent() &&
                  ((BuildingBasedRequester) request.getRequester()).getBuilding(manager, request.getId()).get() instanceof AbstractBuilding)
            {
                final AbstractBuilding building = (AbstractBuilding) ((BuildingBasedRequester) request.getRequester()).getBuilding(manager, request.getId()).get();
                final Optional<ICitizenData> citizenDataOptional = building.getCitizenForRequest(request.getId());

                final List<ItemStack> resolvablestacks = request.getDisplayStacks();
                if (!resolvablestacks.isEmpty() && citizenDataOptional.isPresent())
                {
                    final ItemStack resolveStack = resolvablestacks.get(0);
                    resolveStack.setCount(Math.min(((IDeliverable) request.getRequest()).getCount(), resolveStack.getMaxStackSize()));
                    final ItemStack remainingItemStack = InventoryUtils.addItemStackToItemHandlerWithResult(
                      citizenDataOptional.get().getInventory(),
                      resolveStack);

                    if (ItemStackUtils.isEmpty(remainingItemStack))
                    {
                        manager.updateRequestState(request.getId(), RequestState.RESOLVED);
                        return;
                    }
                }
            }
        }
        assignedRequests.add(request.getId());
    }

    @Override
    public void onAssignedRequestBeingCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IRequestable> request)
    {
        assignedRequests.remove(request.getId());
    }

    @Override
    public void onAssignedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IRequestable> request)
    {

    }

    @Override
    public int getPriority()
    {
        return STANDARD_PLAYER_REQUEST_PRIORITY;
    }

    @Override
    public IToken getId()
    {
        return token;
    }

    @NotNull
    @Override
    public ILocation getLocation()
    {
        return location;
    }

    @Override
    public ImmutableList<IToken<?>> getAllAssignedRequests()
    {
        return ImmutableList.copyOf(assignedRequests);
    }

    @Override
    public void onSystemReset()
    {
        assignedRequests.clear();
    }

    @Override
    public void onColonyUpdate(@NotNull final IRequestManager manager, @NotNull final Predicate<IRequest> shouldTriggerReassign)
    {
        new ArrayList<>(assignedRequests).stream()
          .map(manager::getRequestForToken)
          .filter(shouldTriggerReassign)
          .filter(Objects::nonNull)
          .forEach(request ->
          {
              final IToken newResolverToken = manager.reassignRequest(request.getId(), ImmutableList.of(token));

              if (newResolverToken != null && !newResolverToken.equals(token))
              {
                  assignedRequests.remove(request.getId());
              }
          });
    }

    public void setAllAssignedRequests(final Set<IToken<?>> assignedRequests)
    {
        this.assignedRequests.clear();
        this.assignedRequests.addAll(assignedRequests);
    }
}
