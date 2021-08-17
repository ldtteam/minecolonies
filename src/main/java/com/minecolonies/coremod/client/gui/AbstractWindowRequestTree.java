package com.minecolonies.coremod.client.gui;

import com.google.common.collect.ImmutableList;
import com.ldtteam.blockout.Log;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneBuilders;
import com.ldtteam.blockout.controls.*;
import com.ldtteam.blockout.views.Box;
import com.ldtteam.blockout.views.ScrollingList;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.citizen.MainWindowCitizen;
import com.minecolonies.coremod.colony.requestsystem.requesters.IBuildingBasedRequester;
import com.minecolonies.coremod.colony.requestsystem.requests.StandardRequests;
import com.minecolonies.coremod.network.messages.server.colony.UpdateRequestStateMessage;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.coremod.colony.requestsystem.requests.AbstractRequest.MISSING;

/**
 * Window for the request trees.
 */
public abstract class AbstractWindowRequestTree extends AbstractWindowSkeleton
{
    /**
     * The colony of the citizen.
     */
    protected final IColonyView colony;

    /**
     * Scrollinglist of the resources.
     */
    protected ScrollingList resourceList;

    /**
     * Inventory of the player.
     */
    private final PlayerInventory inventory = this.mc.player.inventory;

    /**
     * Is the player in creative or not.
     */
    private final boolean isCreative = this.mc.player.isCreative();

    /**
     * Life count.
     */
    private int lifeCount = 0;

    /**
     * The building position.
     */
    private @Nullable final IBuildingView building;

    /**
     * Constructor to initiate the window request tree windows.
     *
     * @param building citizen to bind the window to.
     * @param pane     the string name of the pane.
     * @param colony   the colony it belongs to.
     */
    public AbstractWindowRequestTree(final BlockPos building, final String pane, final IColonyView colony)
    {
        super(pane);
        this.colony = colony;
        this.building = colony.getBuilding(building);
        resourceList = findPaneOfTypeByID(WINDOW_ID_LIST_REQUESTS, ScrollingList.class);

        registerButton(REQUEST_DETAIL, this::detailedClicked);
        registerButton(REQUEST_CANCEL, this::cancel);

        if (canFulFill())
        {
            registerButton(REQUEST_FULLFIL, this::fulfill);
        }
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        if (!Screen.hasShiftDown())
        {
            lifeCount++;
        }
    }

    /**
     * Called when the gui is opened by an player.
     */
    @Override
    public void onOpened()
    {
        super.onOpened();

        if (resourceList != null)
        {
            updateRequests();
        }
        if (colony == null)
        {
            Log.getLogger().warn("Colony and/or building null, closing window.");
            close();
        }
    }

    /**
     * After request cancel has been clicked cancel it and update the server side.
     *
     * @param button the clicked button.
     */
    private void cancel(@NotNull final Button button)
    {
        final int row = resourceList.getListElementIndexByPane(button);

        if (getOpenRequestTreeOfBuilding().size() > row && row >= 0)
        {
            @NotNull final IRequest<?> request = getOpenRequestTreeOfBuilding().get(row).getRequest();
            cancel(request);
        }
        updateRequests();
    }

    /**
     * After request cancel has been clicked cancel it and update the server side.
     *
     * @param request the request to cancel.
     */
    protected void cancel(@NotNull final IRequest<?> request)
    {
        building.onRequestedRequestCancelled(colony.getRequestManager(), request);
        Network.getNetwork().sendToServer(new UpdateRequestStateMessage(colony, request.getId(), RequestState.CANCELLED, null));

        updateRequests();
    }

    /**
     * Get the open request tree of the building and construct it.
     *
     * @return an immutable list containing it.
     */
    protected ImmutableList<RequestWrapper> getOpenRequestTreeOfBuilding()
    {
        if (colony == null)
        {
            return ImmutableList.of();
        }

        final List<RequestWrapper> treeElements = new ArrayList<>();

        getOpenRequestsFromBuilding(building).forEach(r -> {
            constructTreeFromRequest(building, colony.getRequestManager(), r, treeElements, 0);
        });

        return ImmutableList.copyOf(treeElements);
    }

    /**
     * Construct the tree from the requests.
     *
     * @param buildingView the building in question.
     * @param manager      the colony request manager.
     * @param request      the request to construct the tree for.
     * @param list         the list which is returned.
     * @param currentDepth the current depth.
     */
    private void constructTreeFromRequest(
      @Nullable final IBuildingView buildingView,
      @NotNull final IRequestManager manager,
      @NotNull final IRequest<?> request,
      @NotNull final List<RequestWrapper> list,
      final int currentDepth)
    {
        list.add(new RequestWrapper(request, currentDepth, buildingView));
        if (request.hasChildren())
        {
            for (final Object o : request.getChildren())
            {
                if (o instanceof IToken<?>)
                {
                    final IToken<?> iToken = (IToken<?>) o;
                    final IRequest<?> childRequest = manager.getRequestForToken(iToken);

                    if (childRequest != null)
                    {
                        constructTreeFromRequest(buildingView, manager, childRequest, list, currentDepth + 1);
                    }
                }
            }
        }
    }

    /**
     * Get the open requests from the building.
     *
     * @param building the building to get them from.
     * @return the requests.
     */
    public ImmutableList<IRequest<?>> getOpenRequestsFromBuilding(final IBuildingView building)
    {
        if (building == null)
        {
            return ImmutableList.of();
        }
        return building.getOpenRequestsOfBuilding();
    }

    /**
     * On Button click transfert Items and fullfil.
     *
     * @param button the clicked button.
     */
    public void fulfill(@NotNull final Button button)
    {
        final int row = resourceList.getListElementIndexByPane(button);

        if (getOpenRequestTreeOfBuilding().size() > row && row >= 0)
        {
            @NotNull final IRequest<?> request = getOpenRequestTreeOfBuilding().get(row).getRequest();
            fulfill(request);
        }
        button.disable();
        updateRequests();
    }

    /**
     * Fulfill a given request.
     *
     * @param request the request to fulfill.
     */
    public void fulfill(@NotNull final IRequest<?> request)
    {
        /*
         * Override if can fulfill.
         */
    }

    /**
     * If the fulfill button should be displayed.
     *
     * @return true if so.
     */
    public boolean canFulFill()
    {
        return false;
    }

    /**
     * After request detail has been clicked open the window.
     *
     * @param button the clicked button.
     */
    private void detailedClicked(@NotNull final Button button)
    {
        final int row = resourceList.getListElementIndexByPane(button);

        if (getOpenRequestTreeOfBuilding().size() > row)
        {
            @NotNull final WindowRequestDetail window = new WindowRequestDetail(this, getOpenRequestTreeOfBuilding().get(row).getRequest(), colony.getID());
            window.open();
        }
    }

    /**
     * Updates request list.
     */
    protected void updateRequests()
    {
        resourceList.setDataProvider(new ScrollingList.DataProvider()
        {
            private List<RequestWrapper> requestWrappers = null;

            @Override
            public int getElementCount()
            {
                requestWrappers = getOpenRequestTreeOfBuilding();
                return requestWrappers.size();
            }

            @Override
            public void updateElement(final int index, final Pane rowPane)
            {
                if (index < 0 || index >= requestWrappers.size())
                {
                    return;
                }

                final RequestWrapper wrapper = requestWrappers.get(index);
                final Box wrapperBox = rowPane.findPaneOfTypeByID(WINDOW_ID_REQUEST_BOX, Box.class);
                wrapperBox.setPosition(wrapperBox.getX() + 2 * wrapper.getDepth(), wrapperBox.getY());
                wrapperBox.setSize(wrapperBox.getParent().getWidth() - 2 * wrapper.getDepth(), wrapperBox.getHeight());

                final Pane pane = rowPane.findPaneByID(REQUEST_FULLFIL);
                if (pane != null)
                {
                    rowPane.findPaneByID(REQUEST_FULLFIL).enable();
                }

                final IRequest<?> request = wrapper.getRequest();
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
                        logo.setImage(request.getDisplayIcon());
                        PaneBuilders.tooltipBuilder().hoverPane(logo).build().setText(request.getResolverToolTip(colony));
                    }
                }

                if(request instanceof StandardRequests.ItemTagRequest)
                {
                    if(!displayStacks.isEmpty())
                    {
                        rowPane.findPaneOfTypeByID(REQUEST_SHORT_DETAIL, Text.class).setText(
                          request.getDisplayStacks().get((lifeCount / LIFE_COUNT_DIVIDER) % displayStacks.size()).getHoverName());
                    }
                }
                else
                {
                    rowPane.findPaneOfTypeByID(REQUEST_SHORT_DETAIL, Text.class).setText(request.getShortDisplayString().getString().replace("§f", ""));
                }

                if (!cancellable(request))
                {
                    rowPane.findPaneOfTypeByID(REQUEST_CANCEL, ButtonImage.class).hide();
                }

                if (pane != null)
                {
                    if (!fulfillable(request))
                    {
                        pane.hide();
                    }
                    else
                    {
                        pane.show();
                    }
                }
            }
        });
    }

    /**
     * Checks if the request is fulfillable
     *
     * @param tRequest the request to check if it's fulfillable
     */
    public boolean fulfillable(final IRequest<?> tRequest)
    {
        if (!(tRequest.getRequest() instanceof IDeliverable))
        {
            return false;
        }

        final Predicate<ItemStack> requestPredicate = stack -> ((IRequest<? extends IDeliverable>) tRequest).getRequest().matches(stack);
        List<RequestWrapper> requestWrappers = getOpenRequestTreeOfBuilding();
        //RequestWrapper wrapper = requestWrappers.stream().filter(requestWrapper -> requestWrapper.getRequest().equals(tRequest)).findFirst().get();

        RequestWrapper wrapper = requestWrappers.stream().filter(requestWrapper -> requestWrapper.getRequest().equals(tRequest)).findFirst().orElse(null);
        if (wrapper == null)
        {
            return false;
        }

        // int depth = requestWrappers.stream().filter(requestWrapper -> requestWrapper.getRequest().equals(tRequest)).findFirst().get().getDepth();

        if (wrapper.overruleable && canFulFill())
        {
            if (wrapper.getDepth() > 0)
            {
                if (!(tRequest.getRequester() instanceof IBuildingBasedRequester)
                      || !((IBuildingBasedRequester) tRequest.getRequester())
                            .getBuilding(colony.getRequestManager(),
                              tRequest.getId()).map(
                    iRequester -> iRequester.getLocation()
                                    .equals(building.getLocation())).isPresent())
                {
                    return false;
                }
                else
                {
                    if (!isCreative && !InventoryUtils.hasItemInItemHandler(new InvWrapper(inventory), requestPredicate))
                    {
                        return false;
                    }
                }
            }
            else
            {
                if (!isCreative && !InventoryUtils.hasItemInItemHandler(new InvWrapper(inventory), requestPredicate))
                {
                    return false;
                }
            }

            if (this instanceof MainWindowCitizen && !((MainWindowCitizen) this).getCitizen().getInventory().hasSpace())
            {
                return false;
            }
        }
        else
        {
            return false;
        }

        return true;
    }

    /**
     * Checks if the request is cancellable
     *
     * @param tRequest the request to check if it's cancellable
     */
    public boolean cancellable(final IRequest<?> tRequest)
    {
        List<RequestWrapper> requestWrappers = getOpenRequestTreeOfBuilding();
        RequestWrapper wrapper = requestWrappers.stream().filter(requestWrapper -> requestWrapper.getRequest().equals(tRequest)).findFirst().orElse(null);
        if (wrapper == null)
        {
            return false;
        }

        if (wrapper.getDepth() > 0)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    /**
     * Request wrapper class used to construct the request tree.
     */
    protected static final class RequestWrapper
    {
        /**
         * The request.
         */
        private final IRequest<?> request;

        /**
         * The depth in the tree.
         */
        private final int depth;

        /**
         * If overruleable.
         */
        private final boolean overruleable;

        /**
         * Constructs an instance of the wrapper.
         *
         * @param request      the request.
         * @param depth        the depth.
         * @param buildingView the building it belongs to.
         */
        public RequestWrapper(@NotNull final IRequest<?> request, final int depth, @Nullable final IBuildingView buildingView)
        {
            this.request = request;
            this.depth = depth;
            this.overruleable = buildingView != null && (request.getRequester().getId().equals(buildingView.getId())
                                  || buildingView.getResolverIds().contains(request.getRequester().getId())
                                  || buildingView.getPosition().equals(request.getRequester().getLocation().getInDimensionLocation()));
        }

        /**
         * Getter for the request.
         *
         * @return the request.
         */
        public IRequest<?> getRequest()
        {
            return request;
        }

        /**
         * Getter for the depth.
         *
         * @return the depth.
         */
        public int getDepth()
        {
            return depth;
        }
    }
}
