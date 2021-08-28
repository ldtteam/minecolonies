package com.minecolonies.coremod.client.gui;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Image;
import com.ldtteam.blockout.controls.ItemIcon;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.ScrollingList;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Delivery;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.moduleviews.BuildingResourcesModuleView;
import com.minecolonies.coremod.colony.buildings.utils.BuildingBuilderResource;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.coremod.network.messages.server.colony.building.MarkBuildingDirtyMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.coremod.client.gui.modules.WindowBuilderResModule.*;

import static com.minecolonies.coremod.colony.buildings.utils.BuildingBuilderResource.RessourceAvailability.*;

/**
 * Window for the resource list item.
 */
public class WindowResourceList extends AbstractWindowSkeleton
{
    @Nullable
    private final BuildingBuilder.View builder;

    /**
     * List of resources needed.
     */
    @NotNull
    private final List<BuildingBuilderResource> resources = new ArrayList<>();

    /**
     * Constructor for the resource scroll window.
     *
     * @param colonyId    the colony id.
     * @param buildingPos the building position.
     */
    public WindowResourceList(final int colonyId, final BlockPos buildingPos)
    {
        super(Constants.MOD_ID + RESOURCE_SCROLL_RESOURCE_SUFFIX);
        final IColonyView colonyView = IColonyManager.getInstance().getColonyView(colonyId, Minecraft.getInstance().level.dimension());
        if (colonyView != null)
        {
            final IBuildingView buildingView = colonyView.getBuilding(buildingPos);
            if (buildingView instanceof BuildingBuilder.View)
            {
                this.builder = (BuildingBuilder.View) buildingView;
                return;
            }
        }

        this.builder = null;
    }

    /**
     * Retrieve resources from the building to display in GUI.
     */
    private void pullResourcesFromHut()
    {
        final IBuildingView newView = builder.getColony().getBuilding(builder.getID());
        if (newView instanceof BuildingBuilder.View)
        {
            final BuildingResourcesModuleView moduleView = newView.getModuleView(BuildingResourcesModuleView.class);
            final Inventory inventory = this.mc.player.inventory;
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
                        stack -> !ItemStackUtils.isEmpty(stack) && stack.sameItem(resource.getItemStack()));
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
                findPaneOfTypeByID(LABEL_PROGRESS, Text.class).setText(new TranslatableComponent("com.minecolonies.coremod.gui.progress.res", (int) ((supplied / total) * 100) + "%", moduleView.getProgress() + "%"));
            }

            resources.sort(new BuildingBuilderResource.ResourceComparator(NOT_NEEDED, HAVE_ENOUGH, IN_DELIVERY, NEED_MORE, DONT_HAVE));
        }
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

    @Override
    public void onOpened()
    {
        final LocalPlayer player =Minecraft.getInstance().player;
        if (this.builder == null)
        {
            player.sendMessage(new TranslatableComponent("com.minecolonies.coremod.resourcescroll.nobuilder"), player.getUUID());
            close();
            return;
        }
        super.onOpened();

        pullResourcesFromHut();

        final ScrollingList resourceList = findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class);
        if (resourceList == null)
        {
            player.sendMessage(new TranslatableComponent("com.minecolonies.coremod.resourcescroll.null"), player.getUUID());
            close();
            return;
        }
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

        final BuildingResourcesModuleView moduleView = builder.getModuleView(BuildingResourcesModuleView.class);

        //Make sure we have a fresh view
        Network.getNetwork().sendToServer(new MarkBuildingDirtyMessage(builder));

        findPaneOfTypeByID(LABEL_WORKERNAME, Text.class).setText(builder.getWorkerName());
        findPaneOfTypeByID(LABEL_CONSTRUCTION_NAME, Text.class).setText(moduleView.getConstructionName());
    }

    /**
     * Update one row pad with its resource informations.
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

        if (resource.getAmountInDelivery() > 0)
        {
            rowPane.findPaneOfTypeByID(IN_DELIVERY_ICON, Image.class).setVisible(true);
            rowPane.findPaneOfTypeByID(IN_DELIVERY_AMOUNT, Text.class).setText("" + resource.getAmountInDelivery());
        }
        else
        {
            rowPane.findPaneOfTypeByID(IN_DELIVERY_ICON, Image.class).setVisible(false);
            rowPane.findPaneOfTypeByID(IN_DELIVERY_AMOUNT, Text.class).setText("");
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

        resourceLabel.setText(resource.getName());
        final int missing = resource.getMissingFromPlayer();
        if (missing < 0)
        {
            resourceMissingLabel.setText(Integer.toString(missing));
        }
        else
        {
            resourceMissingLabel.clearText();
        }

        neededLabel.setText(resource.getAvailable() + " / " + resource.getAmount());
        rowPane.findPaneOfTypeByID(RESOURCE_ID, Text.class).setText(Integer.toString(index));
        rowPane.findPaneOfTypeByID(RESOURCE_QUANTITY_MISSING, Text.class).setText(Integer.toString(resource.getAmount() - resource.getAvailable()));

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
