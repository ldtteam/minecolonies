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
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.requestsystem.requesters.BuildingBasedRequester;
import com.minecolonies.coremod.util.ServerUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
    public List<IToken> attemptResolve(@NotNull final IRequestManager manager, @NotNull final IRequest request)
    {
        if (canResolve(manager, request))
        {
            return Lists.newArrayList();
        }

        return null;
    }

    @Nullable
    @Override
    public void resolve(@NotNull final IRequestManager manager, @NotNull final IRequest request) throws RuntimeException
    {
        final IColony colony = manager.getColony();
        if (colony instanceof Colony)
        {
            if (Configurations.RequestSystem.creativeResolve &&
                  request.getRequest() instanceof IDeliverable &&
                  request.getRequester() instanceof BuildingBasedRequester &&
                  ((BuildingBasedRequester) request.getRequester()).getBuilding().isPresent() &&
                  ((BuildingBasedRequester) request.getRequester()).getBuilding().get() instanceof AbstractBuilding)
            {
                AbstractBuilding building = (AbstractBuilding) ((BuildingBasedRequester) request.getRequester()).getBuilding().get();
                Optional<CitizenData> citizenDataOptional = building.getCitizenForRequest(request.getToken());

                List<ItemStack> resolvablestacks = request.getDisplayStacks();
                if (!resolvablestacks.isEmpty() && citizenDataOptional.isPresent())
                {
                    ItemStack resolveStack = resolvablestacks.get(0);
                    resolveStack.stackSize = Math.min(((IDeliverable) request.getRequest()).getCount(), resolveStack.getMaxStackSize());
                    ItemStack remainingItemStack = InventoryUtils.addItemStackToItemHandlerWithResult(
                      new InvWrapper(citizenDataOptional.get().getCitizenEntity().getInventoryCitizen()),
                      resolveStack);

                    if (ItemStackUtils.isEmpty(remainingItemStack))
                    {
                        manager.updateRequestState(request.getToken(), RequestState.COMPLETED);
                        return;
                    }
                }
            }

            final List<EntityPlayer> players = new ArrayList<>(((Colony) colony).getMessageEntityPlayers());
            final EntityPlayer owner = ServerUtils.getPlayerFromUUID(colony.getWorld(), ((Colony) colony).getPermissions().getOwner());
            final TextComponentString colonyDescription = new TextComponentString(colony.getName() + ":");

            if(manager.getColony().getWorld().isDaytime())
            {
                if (owner != null)
                {
                    players.remove(owner);

                    LanguageHandler.sendPlayerMessage(owner, "com.minecolonies.requestsystem.playerresolver",
                            request.getRequester().getDisplayName(request.getToken()).getFormattedText(),
                            request.getShortDisplayString().getFormattedText(),
                            request.getRequester().getRequesterLocation().toString()
                    );
                }

                LanguageHandler.sendPlayersMessage(players, "com.minecolonies.requestsystem.playerresolver",
                        colonyDescription.getFormattedText() + " " + request.getRequester().getDisplayName(request.getToken()).getFormattedText(),
                        request.getShortDisplayString().getFormattedText(),
                        request.getRequester().getRequesterLocation().toString());
            }
        }

        assignedRequests.add(request.getToken());
    }

    @Nullable
    @Override
    public IRequest getFollowupRequestForCompletion(@NotNull final IRequestManager manager, @NotNull final IRequest completedRequest)
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
    public IRequest onRequestCancelledOrOverruled(@NotNull final IRequestManager manager, @NotNull final IRequest request) throws IllegalArgumentException
    {
        return getFollowupRequestForCompletion(manager, request);
    }

    @Override
    public int getPriority()
    {
        return AbstractRequestResolver.CONST_DEFAULT_RESOLVER_PRIORITY - 100;
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

    @NotNull
    @Override
    public void onRequestComplete(@NotNull final IToken token)
    {
        /**
         * Nothing to do here right now.
         */
    }

    @NotNull
    @Override
    public void onRequestCancelled(@NotNull final IToken token)
    {

    }

    @NotNull
    @Override
    public ITextComponent getDisplayName(@NotNull final IToken token)
    {
        return new TextComponentString("Player");
    }

    @Override
    public ImmutableList<IToken<?>> getAllAssignedRequests()
    {
        return ImmutableList.copyOf(assignedRequests);
    }

    public void setAllAssignedRequests(final Set<IToken<?>> assignedRequests)
    {
        this.assignedRequests.clear();
        this.assignedRequests.addAll(assignedRequests);
    }
}
