package com.minecolonies.core.client.gui.modules;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.IStackBasedTask;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.AbstractWindowSkeleton;
import com.minecolonies.core.client.gui.WindowRequestDetail;
import com.minecolonies.core.colony.requestsystem.requests.StandardRequests;
import com.minecolonies.core.network.messages.server.colony.UpdateRequestStateMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.minecolonies.api.util.constant.TranslationConstants.DETAILS;
import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.core.colony.requestsystem.requests.AbstractRequest.MISSING;

public abstract class RequestTreeWindowModule implements IWindowWithLayoutModule
{
    private static final int AUTO_REFRESH_TICKS = 100;

    /**
     * The parenting window.
     */
    protected final AbstractWindowSkeleton parent;

    /**
     * The colony.
     */
    protected final IColonyView colony;

    /**
     * Inventory of the player.
     */
    private final Inventory inventory;

    /**
     * Is the player in creative or not.
     */
    private final boolean isCreative;

    /**
     * Scrolling list of the resources.
     */
    protected ScrollingList resourceList;

    /**
     * Life count.
     */
    private int lifeCount = 0;

    /**
     * The cached map of open requests.
     */
    private List<RequestWrapper> cachedOpenRequests;

    /**
     * Current ticks until next refresh.
     */
    private int ticks = 0;

    /**
     * Constructor to initiate the window request tree windows.
     *
     * @param parent the parenting window.
     * @param colony the colony we're located in.
     */
    public RequestTreeWindowModule(final AbstractWindowSkeleton parent, final IColonyView colony)
    {
        this.parent = parent;
        this.colony = colony;
        this.inventory = Minecraft.getInstance().player.getInventory();
        this.isCreative = Minecraft.getInstance().player.isCreative();
    }

    @Override
    public void onLayoutMounted(final Pane rootPane)
    {
        parent.registerButton(REQUEST_DETAIL, this::detailedClicked);
        parent.registerButton(REQUEST_CANCEL, this::cancel);
        parent.registerButton(REQUEST_FULFILL, this::onFulfill);

        resourceList = rootPane.findPaneOfTypeByID(WINDOW_ID_LIST_REQUESTS, ScrollingList.class);
        resourceList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return getCachedOpenRequests().size();
            }

            @Override
            public void updateElement(final int index, final Pane rowPane)
            {
                final RequestWrapper wrapper = getCachedOpenRequests().get(index);

                rowPane.setPosition(rowPane.getX() + 2 * wrapper.depth(), rowPane.getY());
                rowPane.setSize(rowPane.getParent().getWidth() - 2 * wrapper.depth(), rowPane.getHeight());

                final IRequest<?> request = wrapper.request();
                final ItemIcon exampleStackDisplay = rowPane.findPaneOfTypeByID(LIST_ELEMENT_ID_REQUEST_STACK, ItemIcon.class);
                final List<ItemStack> displayStacks = request.getDisplayStacks();
                final Image logo = rowPane.findPaneOfTypeByID(DELIVERY_IMAGE, Image.class);

                if (!displayStacks.isEmpty())
                {
                    logo.setVisible(false);
                    exampleStackDisplay.setVisible(true);
                    exampleStackDisplay.setItem(displayStacks.get((lifeCount / LIFE_COUNT_DIVIDER) % displayStacks.size()));
                    rowPane.findPaneOfTypeByID(REQUESTER, Text.class).setText(request.getRequester().getRequesterDisplayName(colony.getRequestManager(), request));
                }
                else
                {
                    exampleStackDisplay.setVisible(false);
                    if (!request.getDisplayIcon().equals(MISSING))
                    {
                        logo.setVisible(true);
                        logo.setImage(request.getDisplayIcon(), false);
                        PaneBuilders.tooltipBuilder().hoverPane(logo).build().setText(request.getResolverToolTip(colony));
                    }
                }

                if (request instanceof final IStackBasedTask stackBasedTask)
                {
                    final ItemIcon icon = rowPane.findPaneOfTypeByID("detailIcon", ItemIcon.class);
                    final ItemStack copyStack = stackBasedTask.getTaskStack().copy();
                    copyStack.setCount(stackBasedTask.getDisplayCount());
                    icon.setItem(copyStack);
                    icon.setVisible(true);
                    rowPane.findPaneOfTypeByID(REQUEST_SHORT_DETAIL, Text.class).setText(stackBasedTask.getDisplayPrefix().withStyle(ChatFormatting.BLACK));
                }
                else if (request instanceof StandardRequests.ItemTagRequest)
                {
                    rowPane.findPaneOfTypeByID("detailIcon", ItemIcon.class).setVisible(false);
                    if (!displayStacks.isEmpty())
                    {
                        rowPane.findPaneOfTypeByID(REQUEST_SHORT_DETAIL, Text.class)
                            .setText(request.getDisplayStacks().get((lifeCount / LIFE_COUNT_DIVIDER) % displayStacks.size()).getHoverName());
                    }
                }
                else
                {
                    rowPane.findPaneOfTypeByID("detailIcon", ItemIcon.class).setVisible(false);
                    rowPane.findPaneOfTypeByID(REQUEST_SHORT_DETAIL, Text.class)
                        .setText(Component.literal(request.getShortDisplayString().getString().replace("§f", "")).withStyle(ChatFormatting.BLACK));
                }

                PaneBuilders.tooltipBuilder().hoverPane(parent.findPaneByID(REQUEST_DETAIL)).build().setText(Component.translatable(DETAILS));
                if (!isCancellable(request))
                {
                    rowPane.findPaneOfTypeByID(REQUEST_CANCEL, ButtonImage.class).hide();
                }

                final Button fulfillButton = rowPane.findPaneOfTypeByID(REQUEST_FULFILL, ButtonImage.class);
                fulfillButton.setVisible(isFulfillable(request));
                fulfillButton.enable();
            }
        });
    }

    @Override
    @NotNull
    public final ResourceLocation getLayout()
    {
        return new ResourceLocation(Constants.MOD_ID, "gui/layouthuts/layoutrequeststree.xml");
    }

    @Override
    public void onUpdate()
    {
        if (!Screen.hasShiftDown())
        {
            lifeCount++;
        }
        ticks++;

        if (ticks >= AUTO_REFRESH_TICKS)
        {
            refreshOpenRequests();
            ticks = 0;
        }
    }

    /**
     * Refresh the map of open requests.
     */
    public final void refreshOpenRequests()
    {
        cachedOpenRequests = null;
    }

    /**
     * Internal method that allows safe reloading of the open requests.
     *
     * @return the list of requests.
     */
    private List<RequestWrapper> getCachedOpenRequests()
    {
        if (cachedOpenRequests == null)
        {
            final List<RequestWrapper> requests = new ArrayList<>();
            for (final IRequest<?> request : getOpenRequests())
            {
                constructTreeFromRequest(request, requests, 0);
            }
            cachedOpenRequests = requests;
        }
        return cachedOpenRequests;
    }

    /**
     * Construct the tree from the requests.
     *
     * @param request      the request to construct the tree for.
     * @param list         the list which is returned.
     * @param currentDepth the current depth.
     */
    private void constructTreeFromRequest(@NotNull final IRequest<?> request, @NotNull final List<RequestWrapper> list, final int currentDepth)
    {
        list.add(new RequestWrapper(request, currentDepth));
        if (request.hasChildren() && canDisplayChildRequests())
        {
            for (final IToken<?> token : request.getChildren())
            {
                final IRequest<?> childRequest = colony.getRequestManager().getRequestForToken(token);
                if (childRequest != null)
                {
                    constructTreeFromRequest(childRequest, list, currentDepth + 1);
                }
            }
        }
    }

    /**
     * Checks if the request is cancellable
     *
     * @param request the request to check if it's cancellable
     */
    public final boolean isCancellable(final IRequest<?> request)
    {
        final RequestWrapper wrapper = getCachedOpenRequests().stream().filter(f -> f.request().getId().equals(request.getId())).findFirst().orElse(null);
        if (wrapper == null)
        {
            return false;
        }

        return wrapper.depth() <= 0;
    }

    /**
     * Checks if the request is fulfillable
     *
     * @param request the request to check if it's fulfillable
     */
    public final boolean isFulfillable(final IRequest<?> request)
    {
        if (!(this instanceof IRequestTreeSupportsFulfill) || !(request.getRequest() instanceof IDeliverable deliverable))
        {
            return false;
        }

        final RequestWrapper wrapper = getCachedOpenRequests().stream().filter(f -> f.request().getId().equals(request.getId())).findFirst().orElse(null);
        if (wrapper == null)
        {
            return false;
        }

        return isCreative || InventoryUtils.hasItemInItemHandler(new InvWrapper(inventory), deliverable::matches);
    }

    /**
     * Get the open requests for this module to handle.
     *
     * @return an immutable list containing it.
     */
    protected abstract Collection<IRequest<?>> getOpenRequests();

    /**
     * If the request can show child requests as well.
     *
     * @return true if so.
     */
    protected boolean canDisplayChildRequests()
    {
        return true;
    }

    /**
     * After request cancel has been clicked cancel it and update the server side.
     *
     * @param button the clicked button.
     */
    private void cancel(@NotNull final Button button)
    {
        final int row = resourceList.getListElementIndexByPane(button);
        cancel(getCachedOpenRequests().get(row).request());
    }

    /**
     * After request cancel has been clicked cancel it and update the server side.
     *
     * @param request the request to cancel.
     */
    public final void cancel(@NotNull final IRequest<?> request)
    {
        Network.getNetwork().sendToServer(new UpdateRequestStateMessage(colony, request.getId(), RequestState.CANCELLED, null));
        refreshOpenRequests();
        onCancel(request);
    }

    /**
     * Additional call to execute upon cancellation of a request.
     */
    protected void onCancel(@NotNull final IRequest<?> request)
    {
    }

    /**
     * On Button click transfer Items and fulfill.
     *
     * @param button the clicked button.
     */
    private void onFulfill(@NotNull final Button button)
    {
        if (!(this instanceof IRequestTreeSupportsFulfill fulfill))
        {
            return;
        }

        final int row = resourceList.getListElementIndexByPane(button);

        if (getCachedOpenRequests().size() > row && row >= 0)
        {
            final IRequest<?> request = getCachedOpenRequests().get(row).request();
            try
            {
                fulfill.onFulfill(request);
            }
            catch (final Exception e)
            {
                Log.getLogger().warn("Failed to fulfill request. This could happen by double clicking the fulfill button.", e);
            }
        }
        button.disable();
        refreshOpenRequests();
    }

    /**
     * After request detail has been clicked open the window.
     *
     * @param button the clicked button.
     */
    private void detailedClicked(@NotNull final Button button)
    {
        final int row = resourceList.getListElementIndexByPane(button);
        openDetails(getCachedOpenRequests().get(row).request());
    }

    /**
     * Open the detail page for a given request.
     *
     * @param request the request instance.
     */
    public final void openDetails(final IRequest<?> request)
    {
        new WindowRequestDetail(parent, request, colony.getID(), this).open();
    }

    /**
     * Interface for setting that the request tree window supports fulfilling.
     */
    public interface IRequestTreeSupportsFulfill
    {
        void onFulfill(@NotNull final IRequest<?> request);
    }

    /**
     * Request wrapper class used to construct the request tree.
     *
     * @param request The request.
     * @param depth   The depth in the tree.
     */
    private record RequestWrapper(
        IRequest<?> request,
        int depth)
    {}
}
