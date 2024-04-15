package com.minecolonies.core.client.gui;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Image;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Delivery;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.colony.workorders.IWorkOrderView;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.buildings.moduleviews.BuildingResourcesModuleView;
import com.minecolonies.core.colony.buildings.utils.BuildingBuilderResource;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.core.network.messages.server.colony.building.MarkBuildingDirtyMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.core.client.gui.modules.WindowBuilderResModule.*;
import static com.minecolonies.core.colony.buildings.utils.BuildingBuilderResource.RessourceAvailability.*;

/**
 * BOWindow for the resource list item.
 */
public class WindowResourceList extends AbstractWindowSkeleton
{
    /**
     * The view.
     */
    @NotNull
    private final BuildingBuilder.View builder;

    /**
     * List of resources needed.
     */
    @NotNull
    private final List<BuildingBuilderResource> resources = new ArrayList<>();

    /**
     * The snapshot of the previously clicked on warehouse.
     */
    @NotNull
    private final Map<String, Integer> warehouseSnapshot;

    /**
     * Constructor for the resource scroll window.
     *
     * @param builderView the building view for the builder.
     */
    public WindowResourceList(final @NotNull BuildingBuilder.View builderView, @NotNull final Map<String, Integer> warehouseSnapshot)
    {
        super(Constants.MOD_ID + RESOURCE_SCROLL_RESOURCE_SUFFIX);

        this.builder = builderView;
        this.warehouseSnapshot = warehouseSnapshot;
    }

    /**
     * Retrieve resources from the building to display in GUI.
     */
    private void pullResourcesFromHut()
    {
        final BuildingResourcesModuleView moduleView = builder.getModuleViewByType(BuildingResourcesModuleView.class);
        final Inventory inventory = this.mc.player.getInventory();
        final boolean isCreative = this.mc.player.isCreative();

        final List<Delivery> deliveries = new ArrayList<>();
        for (Map.Entry<Integer, Collection<IToken<?>>> entry : builder.getOpenRequestsByCitizen().entrySet())
        {
            addDeliveryRequestsToList(deliveries, ImmutableList.copyOf(entry.getValue()));
        }

        resources.clear();
        resources.addAll(moduleView.getResources().values());

        double supplied = 0;
        double total = 0;
        for (final BuildingBuilderResource resource : resources)
        {
            final int amountToSet;
            if (isCreative)
            {
                amountToSet = resource.getAmount();
            }
            else
            {
                amountToSet =
                  InventoryUtils.getItemCountInItemHandler(new InvWrapper(inventory),
                    stack -> !ItemStackUtils.isEmpty(stack) && ItemStackUtils.compareItemStacksIgnoreStackSize(stack, resource.getItemStack()));
            }

            resource.setPlayerAmount(amountToSet);

            resource.setAmountInDelivery(0);
            for (final Delivery delivery : deliveries)
            {
                if (ItemStackUtils.compareItemStacksIgnoreStackSize(resource.getItemStack(), delivery.getStack(), false, false))
                {
                    resource.setAmountInDelivery(resource.getAmountInDelivery() + delivery.getStack().getCount());
                }
            }
            supplied += Math.min(resource.getAvailable(), resource.getAmount());

            total += resource.getAmount();
        }

        if (total > 0)
        {
            findPaneOfTypeByID(LABEL_PROGRESS, Text.class).setText(Component.translatable("com.minecolonies.coremod.gui.progress.res",
              (int) ((supplied / total) * 100) + "%",
              moduleView.getProgress() + "%"));
        }

        resources.sort(new BuildingBuilderResource.ResourceComparator(NOT_NEEDED, HAVE_ENOUGH, IN_DELIVERY, NEED_MORE, DONT_HAVE));
    }

    /**
     * Adds the deliveries of the given tokens to the list
     *
     * @param requestList   list to add to
     * @param tokensToCheck tokens to check
     */
    private void addDeliveryRequestsToList(final List<Delivery> requestList, final ImmutableCollection<IToken<?>> tokensToCheck)
    {
        for (final IToken<?> token : tokensToCheck)
        {
            final IRequest<?> request = builder.getColony().getRequestManager().getRequestForToken(token);
            if (request != null)
            {
                if (request.getRequest() instanceof Delivery && ((Delivery) request.getRequest()).getTarget().getInDimensionLocation().equals(builder.getID()))
                {
                    requestList.add((Delivery) request.getRequest());
                }

                if (request.hasChildren())
                {
                    addDeliveryRequestsToList(requestList, request.getChildren());
                }
            }
        }
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        pullResourcesFromHut();

        final ScrollingList resourceList = findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class);
        resourceList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return resources.size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                updateResourcePane(index, rowPane);
            }
        });

        final BuildingResourcesModuleView moduleView = builder.getModuleViewByType(BuildingResourcesModuleView.class);

        //Make sure we have a fresh view
        Network.getNetwork().sendToServer(new MarkBuildingDirtyMessage(builder));

        findPaneOfTypeByID(LABEL_WORKERNAME, Text.class).setText(Component.literal(builder.getWorkerName()));
        if (moduleView.getWorkOrderId() > -1)
        {
            final IWorkOrderView workOrderView = moduleView.getBuildingView().getColony().getWorkOrder(moduleView.getWorkOrderId());
            if (workOrderView != null)
            {
                findPaneOfTypeByID(LABEL_CONSTRUCTION_NAME, Text.class).setText(Component.literal(workOrderView
                                                                                                    .getDisplayName()
                                                                                                    .getString()
                                                                                                    .replace("\n", " ")));
            }
        }
    }

    /**
     * Update one row pad with its resource information.
     *
     * @param index   index in the list of resources.
     * @param rowPane The Pane to use to display the information.
     */
    private void updateResourcePane(final int index, @NotNull final Pane rowPane)
    {
        final BuildingBuilderResource resource = resources.get(index);
        final Text resourceLabel = rowPane.findPaneOfTypeByID(RESOURCE_NAME, Text.class);
        final Text resourceMissingLabel = rowPane.findPaneOfTypeByID(RESOURCE_MISSING, Text.class);
        final Text neededLabel = rowPane.findPaneOfTypeByID(RESOURCE_AVAILABLE_NEEDED, Text.class);

        rowPane.findPaneOfTypeByID(IN_DELIVERY_ICON, Image.class).setVisible(false);
        rowPane.findPaneOfTypeByID(IN_DELIVERY_AMOUNT, Text.class).clearText();
        rowPane.findPaneOfTypeByID(IN_WAREHOUSE_ICON, Image.class).setVisible(false);
        rowPane.findPaneOfTypeByID(IN_WAREHOUSE_AMOUNT, Text.class).clearText();

        int resourceHashcode = resource.getItemStack().hasTag() ? resource.getItemStack().getTag().hashCode() : 0;
        int warehouseAmount = warehouseSnapshot.getOrDefault(resource.getItem().getDescriptionId() + "-" + resourceHashcode, 0);

        if (resource.getAmountInDelivery() > 0)
        {
            rowPane.findPaneOfTypeByID(IN_DELIVERY_ICON, Image.class).setVisible(true);
            rowPane.findPaneOfTypeByID(IN_DELIVERY_AMOUNT, Text.class).setText(Component.literal(String.valueOf(resource.getAmountInDelivery())));
        }
        else if (warehouseAmount > 0)
        {
            rowPane.findPaneOfTypeByID(IN_WAREHOUSE_ICON, Image.class).setVisible(true);
            rowPane.findPaneOfTypeByID(IN_DELIVERY_AMOUNT, Text.class).setText(Component.literal(String.valueOf(warehouseAmount)));
        }

        switch (resource.getAvailabilityStatus())
        {
            case DONT_HAVE:
                resourceLabel.setColors(RED);
                resourceMissingLabel.setColors(RED);
                neededLabel.setColors(RED);
                break;
            case NEED_MORE:
                resourceLabel.setColors(ORANGE);
                resourceMissingLabel.setColors(ORANGE);
                neededLabel.setColors(ORANGE);
                break;
            case HAVE_ENOUGH:
                resourceLabel.setColors(DARKGREEN);
                resourceMissingLabel.setColors(DARKGREEN);
                neededLabel.setColors(DARKGREEN);
                break;
            case NOT_NEEDED:
            default:
                resourceLabel.setColors(BLACK);
                resourceMissingLabel.setColors(BLACK);
                neededLabel.setColors(BLACK);
                break;
        }

        resourceLabel.setText(Component.literal(resource.getName()));
        final int missing = resource.getMissingFromPlayer();
        if (missing < 0)
        {
            resourceMissingLabel.setText(Component.literal(Integer.toString(missing)));
        }
        else
        {
            resourceMissingLabel.clearText();
        }

        neededLabel.setText(Component.literal(resource.getAvailable() + " / " + resource.getAmount()));
        rowPane.findPaneOfTypeByID(RESOURCE_ID, Text.class).setText(Component.literal(Integer.toString(index)));
        rowPane.findPaneOfTypeByID(RESOURCE_QUANTITY_MISSING, Text.class).setText(Component.literal(Integer.toString(resource.getAmount() - resource.getAvailable())));

        final ItemStack stack = new ItemStack(resource.getItem(), 1);
        stack.setTag(resource.getItemStack().getTag());
        rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(stack);
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        pullResourcesFromHut();
        window.findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class).refreshElementPanes();
    }
}
