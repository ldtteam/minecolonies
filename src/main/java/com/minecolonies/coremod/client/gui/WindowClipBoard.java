package com.minecolonies.coremod.client.gui;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.resolver.player.IPlayerRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.retrying.IRetryingRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.messages.server.colony.UpdateRequestStateMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import static com.minecolonies.api.util.constant.WindowConstants.CLIPBOARD_TOGGLE;

/**
 * ClipBoard window.
 */
public class WindowClipBoard extends AbstractWindowRequestTree
{
    /**
     * Resource suffix.
     */
    private static final String BUILD_TOOL_RESOURCE_SUFFIX = ":gui/windowclipboard.xml";

    /**
     * List of async request tokens.
     */
    private final List<IToken<?>> asyncRequest = new ArrayList<>();

    /**
     * The colony id.
     */
    private final IColonyView colony;

    /**
     * Hide or show not important requests.
     */
    private boolean hide = false;

    /**
     * Constructor of the clipboard GUI.
     *
     * @param colony the colony to check the requests for.
     */
    public WindowClipBoard(final IColonyView colony)
    {
        super(null, Constants.MOD_ID + BUILD_TOOL_RESOURCE_SUFFIX, colony);
        this.colony = colony;
        for (final ICitizenDataView view : this.colony.getCitizens().values())
        {
            if (view.getJobView() != null)
            {
                asyncRequest.addAll(view.getJobView().getAsyncRequests());
            }
        }
        registerButton(CLIPBOARD_TOGGLE, this::toggleImportant);
    }

    private void toggleImportant()
    {
        this.hide = !this.hide;
    }

    @Override
    public ImmutableList<IRequest<?>> getOpenRequestsFromBuilding(final IBuildingView building)
    {
        final ArrayList<IRequest<?>> requests = Lists.newArrayList();

        if (colony == null)
        {
            return ImmutableList.of();
        }

        final IRequestManager requestManager = colony.getRequestManager();

        if (requestManager == null)
        {
            return ImmutableList.of();
        }

        final IPlayerRequestResolver resolver = requestManager.getPlayerResolver();
        final IRetryingRequestResolver retryingRequestResolver = requestManager.getRetryingRequestResolver();

        final Set<IToken<?>> requestTokens = new HashSet<>();
        requestTokens.addAll(resolver.getAllAssignedRequests());
        requestTokens.addAll(retryingRequestResolver.getAllAssignedRequests());

        for (final IToken<?> token : requestTokens)
        {
            IRequest<?> request = requestManager.getRequestForToken(token);

            while (request != null && request.hasParent())
            {
                request = requestManager.getRequestForToken(request.getParent());
            }

            if (request != null && !requests.contains(request))
            {
                requests.add(request);
            }
        }

        if (hide)
        {
            requests.removeIf(req -> asyncRequest.contains(req.getId()));
        }

        final BlockPos playerPos = new BlockPos(Minecraft.getInstance().player.position());
        requests.sort(Comparator.comparing((IRequest<?> request) -> request.getRequester().getLocation().getInDimensionLocation()
                                                                      .distSqr(new Vector3i(playerPos.getX(), playerPos.getY(), playerPos.getZ())))
                        .thenComparingInt((IRequest<?> request) -> request.getId().hashCode()));

        return ImmutableList.copyOf(requests);
    }

    @Override
    public boolean fulfillable(final IRequest<?> tRequest)
    {
        return false;
    }

    @Override
    protected void cancel(@NotNull final IRequest<?> request)
    {
        Network.getNetwork().sendToServer(new UpdateRequestStateMessage(colony, request.getId(), RequestState.CANCELLED, null));
    }
}
