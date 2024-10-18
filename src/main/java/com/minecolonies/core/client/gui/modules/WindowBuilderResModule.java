package com.minecolonies.core.client.gui.modules;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.workorders.IWorkOrderView;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.AbstractModuleWindow;
import com.minecolonies.core.client.gui.generic.ResourceItem;
import com.minecolonies.core.client.gui.generic.ResourceItem.ResourceComparator;
import com.minecolonies.core.colony.buildings.moduleviews.BuildingResourcesModuleView;
import com.minecolonies.core.colony.buildings.utils.BuildingBuilderResource;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.core.network.messages.server.colony.building.MarkBuildingDirtyMessage;
import com.minecolonies.core.network.messages.server.colony.building.TransferItemsRequestMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * BOWindow for the builder hut.
 */
public class WindowBuilderResModule extends AbstractModuleWindow
{
    /**
     * List of resources needed.
     */
    @NotNull
    private final List<BuildingBuilderResource> resources = new ArrayList<>();

    /**
     * The module belonging to this.
     */
    private final BuildingResourcesModuleView moduleView;

    /**
     * Tick to update the list.
     */
    private int tickToInventoryUpdate = 0;

    /**
     * Constructor for window builder hut.
     *
     * @param building  {@link BuildingBuilder.View}.
     */
    public WindowBuilderResModule(final String res, final IBuildingView building, final BuildingResourcesModuleView moduleView)
    {
        super(building, res);
        this.moduleView = moduleView;
        findPaneOfTypeByID(DESC_LABEL, Text.class).setText(Component.translatable(moduleView.getDesc().toLowerCase(Locale.US)));

        pullResourcesFromHut();

        registerButton(RESOURCE_ADD, this::transferItems);
    }

    /**
     * Retrieve resources from the building to display in GUI.
     */
    private void pullResourcesFromHut()
    {
        final Inventory inventory = this.mc.player.getInventory();
        final boolean isCreative = this.mc.player.isCreative();

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
            supplied += Math.min(resource.getAmountAvailable(), resource.getAmount());

            total += resource.getAmount();
        }

        if (total > 0)
        {
            findPaneOfTypeByID(LABEL_PROGRESS, Text.class).setText(Component.translatable("com.minecolonies.coremod.gui.progress.res", (int) ((supplied / total) * 100) + "%", moduleView.getProgress() + "%"));
        }

        resources.sort(new ResourceComparator());
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
                ResourceItem.updateResourcePane(resources.get(index), mc.player, index, rowPane);
            }
        });

        //Make sure we have a fresh view
        Network.getNetwork().sendToServer(new MarkBuildingDirtyMessage(this.buildingView));

        if (moduleView.getWorkOrderId() > -1)
        {
            final IWorkOrderView workOrder = moduleView.getBuildingView().getColony().getWorkOrder(moduleView.getWorkOrderId());
            if (workOrder != null)
            {
                final Text pane = findPaneOfTypeByID(LABEL_CONSTRUCTION_NAME, Text.class);
                final Component text = Component.literal(workOrder.getDisplayName().getString().replace("\n", " "));
                pane.setText(text);
                PaneBuilders.tooltipBuilder().hoverPane(pane).build().setText(text);
            }
        }
        findPaneOfTypeByID(STEP_PROGRESS, Text.class).setText(Component.translatable("com.minecolonies.coremod.gui.progress.step", moduleView.getCurrentStage(), moduleView.getTotalStages()));
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        if (tickToInventoryUpdate++ == 20)
        {
            pullResourcesFromHut();
            tickToInventoryUpdate = 0;
        }
        window.findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class).refreshElementPanes();
    }

    /**
     * On Button click transfert Items.
     *
     * @param button the clicked button.
     */
    private void transferItems(@NotNull final Button button)
    {
        final Pane pane = button.getParent();
        button.disable();
        final Text idLabel = pane.findPaneOfTypeByID(RESOURCE_ID, Text.class);
        final int index = Integer.parseInt(idLabel.getTextAsString());
        final BuildingBuilderResource res = resources.get(index);
        if (res == null)
        {
            Log.getLogger().warn("WindowHutBuilder.transferItems: Error - Could not find the resource.");
        }
        else
        {
            // Delay updates to allow conflicts with network data 3s until server data is loaded
            tickToInventoryUpdate = -20 * 3;

            // The itemStack size should not be greater than itemStack.getMaxStackSize, We send 1 instead
            // and use quantity for the size
            @NotNull final ItemStack itemStack = res.getItemStack().copy();
            itemStack.setCount(1);
            final Text quantityLabel = pane.findPaneOfTypeByID(RESOURCE_QUANTITY_MISSING, Text.class);
            final int quantity = Integer.parseInt(quantityLabel.getTextAsString());
            final int needed = res.getAmount() - res.getAmountAvailable();
            res.setAvailable(Math.min(res.getAmount(), res.getAmountAvailable() + res.getAmountPlayer()));
            res.setPlayerAmount(Math.max(0, res.getAmountPlayer() - needed));
            resources.sort(new ResourceComparator());
            Network.getNetwork().sendToServer(new TransferItemsRequestMessage(this.buildingView, itemStack, quantity, true));
        }
    }
}
