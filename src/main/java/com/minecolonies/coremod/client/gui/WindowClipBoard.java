package com.minecolonies.coremod.client.gui;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.resolver.player.IPlayerRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.retrying.IRetryingRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.Constants;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.Image;
import com.ldtteam.blockout.controls.ItemIcon;
import com.ldtteam.blockout.controls.Label;
import com.ldtteam.blockout.views.ScrollingList;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.IColonyManager;
import com.minecolonies.coremod.colony.IColonyView;
import com.minecolonies.coremod.network.messages.UpdateRequestStateMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

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
     * Id of the resource add button.
     */
    private static final String REQUEST_CANCEL = "cancel";

    /**
     * Id of the detail button.
     */
    private static final String REQUEST_DETAIL = "detail";

    /**
     * Id of the short detail label.
     */
    private static final String REQUEST_SHORT_DETAIL = "shortDetail";

    /**
     * Resolver string.
     */
    private static final String DELIVERY_IMAGE = "deliveryImage";

    /**
     * Id of the requester label.
     */
    private static final String REQUESTER = "requester";

    /**
     * The divider for the life count.
     */
    private static final int LIFE_COUNT_DIVIDER = 30;
    /**
     * Scrollinglist of the resources.
     */
    private ScrollingList resourceList;
    /**
     * The colony id.
     */
    private final int colonyId;
    /**
     * Life count.
     */
    private int lifeCount = 0;

    /**
     * Constructor of the clipboard GUI.
     * @param colonyId the colony id to check the requests for.
     */
    public WindowClipBoard(final int colonyId)
    {
        super(Constants.MOD_ID + BUILD_TOOL_RESOURCE_SUFFIX);
        this.colonyId = colonyId;
    }

    /**
     * Constructor of the clipboard GUI.
     * @param s the description string of the style.
     * @param colony the colony id.
     */
    public WindowClipBoard(final String s, final IColonyView colony)
    {
        super(s);
        this.colonyId = colony.getID();
    }

    /**
     * Called when the window is opened.
     * Sets up the buttons for either hut mode or decoration mode.
     */
    @Override
    public void onOpened()
    {
        resourceList = findPaneOfTypeByID(WINDOW_ID_LIST_REQUESTS, ScrollingList.class);
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
                if(exampleStackDisplay != null)
                {
                    exampleStackDisplay.setItem(displayStacks.get((lifeCount / LIFE_COUNT_DIVIDER) % displayStacks.size()));
                }
            }
            else
            {
                final Image logo = findPaneOfTypeByID(DELIVERY_IMAGE, Image.class);
                logo.setVisible(true);
                logo.setImage(request.getDisplayIcon());
            }

            final IColonyView view = IColonyManager.getInstance().getColonyView(colonyId, Minecraft.getMinecraft().world.provider.getDimension());
            rowPane.findPaneOfTypeByID(REQUESTER, Label.class).setLabelText(request.getRequester().getDisplayName(view.getRequestManager(), request.getId()).getFormattedText());

            rowPane.findPaneOfTypeByID(REQUEST_SHORT_DETAIL, Label.class)
              .setLabelText(request.getShortDisplayString().getFormattedText().replace("§f", ""));
        });
    }

    /**
     * The requests to display.
     * @return the list of requests.
     */
    public ImmutableList<IRequest> getOpenRequests()
    {
        final ArrayList<IRequest<?>> requests = Lists.newArrayList();
        final IColonyView view = IColonyManager.getInstance().getColonyView(colonyId, Minecraft.getMinecraft().world.provider.getDimension());

        if (view == null)
        {
            return ImmutableList.of();
        }

        final IRequestManager requestManager = view.getRequestManager();

        if (requestManager == null)
        {
            return ImmutableList.of();
        }

        final IPlayerRequestResolver resolver = requestManager.getPlayerResolver();
        final IRetryingRequestResolver retryingRequestResolver = requestManager.getRetryingRequestResolver();

        final Set<IToken> requestTokens = new HashSet<>();
        requestTokens.addAll(resolver.getAllAssignedRequests());
        requestTokens.addAll(retryingRequestResolver.getAllAssignedRequests());

        requests.addAll(requestTokens.stream().map(requestManager::getRequestForToken).filter(Objects::nonNull).collect(Collectors.toSet()));

        final BlockPos playerPos = Minecraft.getMinecraft().player.getPosition();
        requests.sort(Comparator.comparing((IRequest request) -> request.getRequester().getLocation().getInDimensionLocation()
                .getDistance(playerPos.getX(), playerPos.getY(), playerPos.getZ()))
                .thenComparingInt((IRequest request) -> request.getId().hashCode()));

        return ImmutableList.copyOf(requests);
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
                super.onButtonClicked(button);
                break;
        }
    }

    private void detailedClicked(@NotNull final Button button)
    {
        final int row = resourceList.getListElementIndexByPane(button);

        if (getOpenRequests().size() > row && row >= 0)
        {
            @NotNull final WindowRequestDetail window = new WindowRequestDetail(this, getOpenRequests().get(row), colonyId);
            window.open();
        }
    }

    private void cancel(@NotNull final Button button)
    {
        final int row = resourceList.getListElementIndexByPane(button);

        if (getOpenRequests().size() > row && row >= 0)
        {
            @NotNull final IRequest request = getOpenRequests().get(row);
            Network.getNetwork().sendToServer(new UpdateRequestStateMessage(colonyId, request.getId(), RequestState.CANCELLED, null));
        }
    }
}
