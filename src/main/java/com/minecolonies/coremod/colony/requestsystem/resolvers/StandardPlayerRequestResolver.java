package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.resolver.player.IPlayerRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.requestsystem.requesters.BuildingBasedRequester;
import com.minecolonies.coremod.util.ServerUtils;
import com.minecolonies.coremod.util.text.NonSiblingFormattingTextComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.items.wrapper.InvWrapper;
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
    public boolean canResolve(@NotNull final IRequestManager manager, final IRequest requestToCheck)
    {
        return !manager.getColony().getWorld().isRemote;
    }

    @Nullable
    @Override
    public List<IToken<?>> attemptResolve(@NotNull final IRequestManager manager, @NotNull final IRequest request)
    {
        if (canResolve(manager, request))
        {
            return Lists.newArrayList();
        }

        return null;
    }

    @Override
    public void resolve(@NotNull final IRequestManager manager, @NotNull final IRequest request) throws RuntimeException
    {
        final IColony colony = manager.getColony();
        if (colony instanceof Colony)
        {
            if (Configurations.requestSystem.creativeResolve &&
                    request.getRequest() instanceof IDeliverable &&
                    request.getRequester() instanceof BuildingBasedRequester &&
                    ((BuildingBasedRequester) request.getRequester()).getBuilding(manager, request.getToken()).isPresent() &&
                    ((BuildingBasedRequester) request.getRequester()).getBuilding(manager, request.getToken()).get() instanceof AbstractBuilding)
            {
                final AbstractBuilding building = (AbstractBuilding) ((BuildingBasedRequester) request.getRequester()).getBuilding(manager, request.getToken()).get();
                final Optional<CitizenData> citizenDataOptional = building.getCitizenForRequest(request.getToken());

                final List<ItemStack> resolvablestacks = request.getDisplayStacks();
                if (!resolvablestacks.isEmpty() && citizenDataOptional.isPresent())
                {
                    final ItemStack resolveStack = resolvablestacks.get(0);
                    resolveStack.setCount(Math.min(((IDeliverable) request.getRequest()).getCount(), resolveStack.getMaxStackSize()));
                    final ItemStack remainingItemStack = InventoryUtils.addItemStackToItemHandlerWithResult(
                            new InvWrapper(citizenDataOptional.get().getInventory()),
                            resolveStack);

                    if (ItemStackUtils.isEmpty(remainingItemStack))
                    {
                        manager.updateRequestState(request.getToken(), RequestState.COMPLETED);
                        return;
                    }
                }
            }

            final List<PlayerEntity> players = new ArrayList<>(((Colony) colony).getMessagePlayerEntitys());
            final PlayerEntity owner = ServerUtils.getPlayerFromUUID(colony.getWorld(), ((Colony) colony).getPermissions().getOwner());
            final StringTextComponent colonyDescription = new StringTextComponent(colony.getName() + ":");

            final ILocation requester = request.getRequester().getRequesterLocation();
            final AbstractBuilding building = ((Colony) colony).getBuildingManager().getBuilding(requester.getInDimensionLocation());

            if (building == null || (building.getCitizenForRequest(request.getToken()).isPresent() && !building.getCitizenForRequest(request.getToken())
                                                                                                         .get()
                                                                                                         .isRequestAsync(request.getToken())))
            {
                if (manager.getColony().getWorld().isDaytime())
                {
                    if (owner != null)
                    {
                        players.remove(owner);

                        LanguageHandler.sendPlayerMessage(owner, "com.minecolonies.requestsystem.playerresolver",
                          request.getRequester().getDisplayName(manager, request.getToken()).getFormattedText(),
                          getRequestMessage(request).getFormattedText(),
                          request.getRequester().getRequesterLocation().toString()
                        );
                    }
                    LanguageHandler.sendPlayersMessage(players, "com.minecolonies.requestsystem.playerresolver",
                      colonyDescription.getFormattedText() + " " + request.getRequester().getDisplayName(manager, request.getToken()).getFormattedText(),
                      getRequestMessage(request).getFormattedText(),
                      request.getRequester().getRequesterLocation().toString());
                }
            }

        }
        assignedRequests.add(request.getToken());
    }

    private ITextComponent getRequestMessage(@NotNull final IRequest request)
    {
        final ITextComponent component = new NonSiblingFormattingTextComponent();
        component.appendSibling(request.getShortDisplayString());
        component.getStyle().setColor(TextFormatting.WHITE);
        return component;
    }

    @Nullable
    @Override
    public List<IRequest<?>> getFollowupRequestForCompletion(@NotNull final IRequestManager manager, @NotNull final IRequest completedRequest)
    {
        //This is not what this method is for, but this is the closest we are getting right now, so why not.
        if (assignedRequests.contains(completedRequest.getToken()))
        {
            assignedRequests.remove(completedRequest.getToken());
        }

        return null;
    }

    @Nullable
    @Override
    public IRequest<?> onRequestCancelled(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IRequestable> request)
    {
        getFollowupRequestForCompletion(manager, request);
        return null;
    }

    @Override
    public void onRequestBeingOverruled(
      @NotNull final IRequestManager manager, @NotNull final IRequest<? extends IRequestable> request)
    {
        getFollowupRequestForCompletion(manager, request);
    }

    @Override
    public int getPriority()
    {
        return STANDARD_PLAYER_REQUEST_PRIORITY;
    }

    @Override
    public IToken getRequesterId()
    {
        return token;
    }

    @NotNull
    @Override
    public ILocation getRequesterLocation()
    {
        return location;
    }

    @Override
    public void onRequestComplete(@NotNull final IRequestManager manager,@NotNull final IToken token)
    {
        /**
         * Nothing to do here right now.
         */
    }

    @Override
    public void onRequestCancelled(@NotNull final IRequestManager manager,@NotNull final IToken token)
    {

    }

    @NotNull
    @Override
    public ITextComponent getDisplayName(@NotNull final IRequestManager manager, @NotNull final IToken token)
    {
        return new StringTextComponent("Player");
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
                    final IToken newResolverToken = manager.reassignRequest(request.getToken(), ImmutableList.of(token));

                    if (newResolverToken != null && !newResolverToken.equals(token))
                    {
                        assignedRequests.remove(request.getToken());
                    }
                });
    }

    public void setAllAssignedRequests(final Set<IToken<?>> assignedRequests)
    {
        this.assignedRequests.clear();
        this.assignedRequests.addAll(assignedRequests);
    }
}
