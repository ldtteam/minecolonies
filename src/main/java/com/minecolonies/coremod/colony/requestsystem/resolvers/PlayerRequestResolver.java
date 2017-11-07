package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.IRequestManager;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.resolver.player.IPlayerRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.util.ServerUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Resolver that checks if a deliverable request is already in the building it is being requested from.
 */
public class PlayerRequestResolver implements IPlayerRequestResolver
{

    @NotNull
    private final ILocation location;

    @NotNull
    private final IToken token;

    @NotNull
    private final Set<IToken> assignedRequests = new HashSet<>();

    public PlayerRequestResolver(@NotNull final ILocation location, @NotNull final IToken token)
    {
        super();
        this.location = location;
        this.token = token;
    }

    @Override
    public int getPriority()
    {
        return 0;
    }

    @Override
    public TypeToken getRequestType()
    {
        return TypeToken.of(Object.class);
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
        if(colony instanceof Colony)
        {
            final List<EntityPlayer> players = new ArrayList<>(((Colony) colony).getMessageEntityPlayers());
            final EntityPlayer owner = ServerUtils.getPlayerFromUUID(colony.getWorld(), ((Colony) colony).getPermissions().getOwner());
            final TextComponentString colonyDescription = new TextComponentString(" at " + colony.getName() + ":");

            if (owner != null)
            {
                players.remove(owner);
                LanguageHandler.sendPlayerMessage(owner,
                        request.getRequester().getRequesterLocation().toString(), request.getDisplayString());
            }

            LanguageHandler.sendPlayersMessage(players,
                    request.getRequester().getRequesterLocation().toString(), colonyDescription, request.getDisplayString());
        }

        assignedRequests.add(request.getToken());
    }

    @Nullable
    @Override
    public IRequest getFollowupRequestForCompletion(@NotNull final IRequestManager manager, @NotNull final IRequest completedRequest)
    {
        if (assignedRequests.contains(completedRequest.getToken()))
            assignedRequests.remove(completedRequest.getToken());

        return null;
    }

    @Nullable
    @Override
    public IRequest onParentCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest request) throws IllegalArgumentException
    {
        return null;
    }

    @Nullable
    @Override
    public void onResolvingOverruled(@NotNull final IRequestManager manager, @NotNull final IRequest request) throws IllegalArgumentException
    {
        /**
         * Not especifically needed right now.
         */
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

    @Override
    public ImmutableList<IToken> getAllAssignedRequests()
    {
        return ImmutableList.copyOf(assignedRequests);
    }

    public void setAllAssignedRequests(final Set<IToken> assignedRequests)
    {
        this.assignedRequests.clear();
        this.assignedRequests.addAll(assignedRequests);
    }
}
