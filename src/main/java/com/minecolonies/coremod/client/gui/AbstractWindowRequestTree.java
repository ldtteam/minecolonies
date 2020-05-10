package com.minecolonies.coremod.client.gui;

import com.google.common.collect.ImmutableList;
import com.ldtteam.blockout.Log;
import com.ldtteam.blockout.Pane;
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
import com.minecolonies.coremod.colony.requestsystem.requesters.IBuildingBasedRequester;
import com.minecolonies.coremod.network.messages.server.colony.UpdateRequestStateMessage;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.WindowConstants.*;

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
    private final IBuildingView building;

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

        if (building != null)
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
            building.onRequestedRequestCancelled(colony.getRequestManager(), request);
            Network.getNetwork().sendToServer(new UpdateRequestStateMessage(colony, request.getId(), RequestState.CANCELLED, null));
        }
        updateRequests();
    }

    /**
     * After request cancel has been clicked cancel it and update the server side.
     *
     * @param tRequest the request to cancel.
     */
    public void cancel(@NotNull final IRequest tRequest)
    {
        @NotNull final IRequest<?> request = (IRequest<?>) tRequest;
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

        if (building != null)
        {
            getOpenRequestsFromBuilding(building).forEach(r -> {
                constructTreeFromRequest(building, colony.getRequestManager(), r, treeElements, 0);
            });
        }

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
      @NotNull final IBuildingView buildingView,
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
    public ImmutableList<IRequest> getOpenRequestsFromBuilding(final IBuildingView building)
    {
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
            @NotNull final IRequest request = getOpenRequestTreeOfBuilding().get(row).getRequest();
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
    public void fulfill(@NotNull final IRequest request)
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

                rowPane.findPaneByID(REQUEST_FULLFIL).enable();

                final IRequest<?> request = wrapper.getRequest();
                final ItemIcon exampleStackDisplay = rowPane.findPaneOfTypeByID(LIST_ELEMENT_ID_REQUEST_STACK, ItemIcon.class);
                final List<ItemStack> displayStacks = request.getDisplayStacks();
                final Image logo = rowPane.findPaneOfTypeByID(DELIVERY_IMAGE, Image.class);

                if (!displayStacks.isEmpty())
                {
                    logo.setVisible(false);
                    exampleStackDisplay.setVisible(true);
                    exampleStackDisplay.setItem(displayStacks.get((lifeCount / LIFE_COUNT_DIVIDER) % displayStacks.size()));
                }
                else
                {
                    exampleStackDisplay.setVisible(false);
                    logo.setVisible(true);
                    logo.setImage(request.getDisplayIcon());
                }

                rowPane.findPaneOfTypeByID(REQUESTER, Label.class)
                  .setLabelText(request.getRequester().getRequesterDisplayName(colony.getRequestManager(), request).getFormattedText());
                rowPane.findPaneOfTypeByID(REQUEST_SHORT_DETAIL, Label.class)
                  .setLabelText(request.getShortDisplayString().getFormattedText().replace("§f", ""));

                if (wrapper.getDepth() > 0)
                {
                    rowPane.findPaneOfTypeByID(REQUEST_CANCEL, ButtonImage.class).hide();
                }
                else
                {
                    rowPane.findPaneOfTypeByID(REQUEST_CANCEL, ButtonImage.class).show();
                }

                if (wrapper.overruleable && canFulFill())
                {
                    if (wrapper.getDepth() > 0)
                    {
                        if (!(request.getRequester() instanceof IBuildingBasedRequester)
                              || !((IBuildingBasedRequester) request.getRequester())
                                    .getBuilding(colony.getRequestManager(),
                                      request.getId()).map(
                            iRequester -> iRequester.getLocation()
                                            .equals(building.getLocation())).isPresent())
                        {
                            rowPane.findPaneOfTypeByID(REQUEST_FULLFIL, ButtonImage.class).hide();
                        }
                        else
                        {
                            request.getRequestOfType(IDeliverable.class).ifPresent((IDeliverable requestRequest) -> {
                                if (!isCreative && !InventoryUtils.hasItemInItemHandler(new InvWrapper(inventory), requestRequest::matches))
                                {
                                    rowPane.findPaneOfTypeByID(REQUEST_FULLFIL, ButtonImage.class).hide();
                                }
                            });

                            if (!(request.getRequest() instanceof IDeliverable))
                            {
                                rowPane.findPaneOfTypeByID(REQUEST_FULLFIL, ButtonImage.class).hide();
                            }
                        }
                        rowPane.findPaneOfTypeByID(REQUEST_CANCEL, ButtonImage.class).hide();
                    }
                    else
                    {
                        request.getRequestOfType(IDeliverable.class).ifPresent((IDeliverable requestRequest) -> {
                            if (!isCreative && !InventoryUtils.hasItemInItemHandler(new InvWrapper(inventory), requestRequest::matches))
                            {
                                rowPane.findPaneOfTypeByID(REQUEST_FULLFIL, ButtonImage.class).hide();
                            }
                        });
                    }
                }
                else
                {
                    rowPane.findPaneOfTypeByID(REQUEST_FULLFIL, ButtonImage.class).hide();
                }
            }
        });
    }

    /**
     * Checks if the request is fulfillable
     */
    public Boolean fulfillable(final IRequest tRequest)
    {
        if (canFulFill())
        {
            @NotNull final IRequest<? extends IDeliverable> request = (IRequest<? extends IDeliverable>) tRequest;
            final Predicate<ItemStack> requestPredicate = stack -> request.getRequest().matches(stack);
            final int amount = request.getRequest().getCount();

            final int count = InventoryUtils.getItemCountInItemHandler(new InvWrapper(inventory), requestPredicate);

            if (!(tRequest.getRequest() instanceof IDeliverable))
            {
                return false;
            }

            if (!(request.getRequester() instanceof IBuildingBasedRequester)
                  || !((IBuildingBasedRequester) request.getRequester())
                        .getBuilding(colony.getRequestManager(),
                          request.getId()).map(
                iRequester -> iRequester.getLocation()
                                .equals(building.getLocation())).isPresent())
            {
                return false;
            }

            if (!isCreative && count <= 0)
            {
                return false;
            }

            if (!isCreative)
            {
                final List<Integer> slots = InventoryUtils.findAllSlotsInItemHandlerWith(new InvWrapper(inventory), requestPredicate);
                final int invSize = inventory.getSizeInventory() - 5; // 4 armour slots + 1 shield slot
                int slot = -1;
                for (final Integer possibleSlot : slots)
                {
                    if (possibleSlot < invSize)
                    {
                        slot = possibleSlot;
                        break;
                    }
                }
                if (slot == -1)
                {
                    return false; // We don't have one that isn't in our armour slot
                }
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Request wrapper class used to construct the request tree.
     */
    protected final class RequestWrapper
    {
        /**
         * The request.
         */
        private final IRequest request;

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
        public RequestWrapper(@NotNull final IRequest request, final int depth, @NotNull final IBuildingView buildingView)
        {
            this.request = request;
            this.depth = depth;
            this.overruleable = request.getRequester().getId().equals(buildingView.getId())
                                  || buildingView.getResolverIds().contains(request.getRequester().getId())
                                  || buildingView.getPosition().equals(request.getRequester().getLocation().getInDimensionLocation());
        }

        /**
         * Getter for the request.
         *
         * @return the request.
         */
        public IRequest getRequest()
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
