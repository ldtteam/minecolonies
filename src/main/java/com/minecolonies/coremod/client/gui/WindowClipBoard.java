package com.minecolonies.coremod.client.gui;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.requestsystem.RequestState;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.resolver.player.IPlayerRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.retrying.IRetryingRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.ItemIcon;
import com.minecolonies.blockout.controls.Label;
import com.minecolonies.blockout.views.ScrollingList;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.network.messages.UpdateRequestStateMessage;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ClipBoard window.
 */
public class WindowClipBoard extends AbstractWindowSkeleton
{
    /**
     * Resource suffix.
     */
    private static final String BUILD_TOOL_RESOURCE_SUFFIX = ":gui/windowclipboard.xml";

    /**
     * Requests list id.
     */
    private static final String WINDOW_ID_LIST_REQUESTS = "requests";

    /**
     * Requestst stack id.
     */
    private static final String LIST_ELEMENT_ID_REQUEST_STACK = "requestStack";

    /**
     * Target location id.
     */
    private static final String LIST_ELEMENT_ID_REQUEST_LOCATION = "targetLocation";

    /**
     * Id of the resource add button.
     */
    private static final String REQUEST_CANCEL = "cancel";

    /**
     * Id of the detail button
     */
    private static final String REQUEST_DETAIL = "detail";

    /**
     * Life count.
     */
    private int lifeCount = 0;

    /**
     * Scrollinglist of the resources.
     */
    private final ScrollingList resourceList;

    /**
     * The colony id.
     */
    private final int colonyId;

    public WindowClipBoard(@Nullable final int colonyId)
    {
        super(Constants.MOD_ID + BUILD_TOOL_RESOURCE_SUFFIX);
        resourceList = findPaneOfTypeByID(WINDOW_ID_LIST_REQUESTS, ScrollingList.class);
        this.colonyId = colonyId;
    }

    /**
     * Called when the window is opened.
     * Sets up the buttons for either hut mode or decoration mode.
     */
    @Override
    public void onOpened()
    {
        resourceList.setDataProvider(() -> getOpenRequests().size(), (index, rowPane) ->
        {
            final ImmutableList<IRequest> openRequests = getOpenRequests();
            if (index < 0 || index >= openRequests.size())
            {
                return;
            }

            final IRequest request = openRequests.get(index);
            final ItemIcon exampleStackDisplay = rowPane.findPaneOfTypeByID(LIST_ELEMENT_ID_REQUEST_STACK, ItemIcon.class);
            final List<ItemStack> displayStacks = request.getDisplayStacks();

            if (!displayStacks.isEmpty())
            {
                final ItemStack selectedStack = displayStacks.get((lifeCount / 30) % displayStacks.size());
                exampleStackDisplay.setItem(selectedStack);
            }
            else
            {
                exampleStackDisplay.setItem(ItemStackUtils.EMPTY);
            }
            rowPane.findPaneOfTypeByID("requester", Label.class).setLabelText(request.getRequester().toString());

            final Label targetLabel = rowPane.findPaneOfTypeByID(LIST_ELEMENT_ID_REQUEST_LOCATION, Label.class);
            targetLabel.setLabelText(request.getRequester().getDeliveryLocation().toString());
        });
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        if (!GuiScreen.isShiftKeyDown())
        {
            lifeCount++;
        }
    }

    private ImmutableList<IRequest> getOpenRequests()
    {
        final ImmutableList.Builder<IRequest> requests = ImmutableList.builder();
        final ColonyView view = ColonyManager.getColonyView(colonyId);

        if (view == null)
        {
            return requests.build();
        }

        final IPlayerRequestResolver resolver = view.getRequestManager().getPlayerResolver();
        final IRetryingRequestResolver retryingRequestResolver = view.getRequestManager().getRetryingRequestResolver();

        final Set<IToken> requestTokens = new HashSet<>();
        requestTokens.addAll(resolver.getAllAssignedRequests());
        requestTokens.addAll(retryingRequestResolver.getAllAssignedRequests());

        requests.addAll(requestTokens.stream().map(view.getRequestManager()::getRequestForToken).filter(Objects::nonNull).collect(Collectors.toSet()));

        return requests.build();
    }

    /**
     * Called when a button in the citizen has been clicked.
     *
     * @param button the clicked button.
     */
    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        switch (button.getID())
        {
            case REQUEST_DETAIL:
                detailedClicked(button);
                break;
            case REQUEST_CANCEL:
                cancel(button);
                break;
            default:
                break;
        }
    }

    private void detailedClicked(@NotNull final Button button)
    {
        final int row = resourceList.getListElementIndexByPane(button);

        if (getOpenRequests().size() > row && row >= 0)
        {
            @NotNull final WindowRequestDetail window = new WindowRequestDetail(null, getOpenRequests().get(row), colonyId);
            window.open();
        }
    }

    private void cancel(@NotNull final Button button)
    {
        final int row = resourceList.getListElementIndexByPane(button);

        if (getOpenRequests().size() > row && row >= 0)
        {
            @NotNull final IRequest request = getOpenRequests().get(row);
            MineColonies.getNetwork().sendToServer(new UpdateRequestStateMessage(colonyId, request.getToken(), RequestState.CANCELLED, null));
        }
    }
}
