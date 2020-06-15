package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Color;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ItemIcon;
import com.ldtteam.blockout.controls.Label;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.blockout.views.SwitchView;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.utils.BuildingBuilderResource;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.coremod.network.messages.server.colony.building.MarkBuildingDirtyMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.TransferItemsRequestMessage;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window for the builder hut.
 */
public class WindowHutBuilder extends AbstractWindowWorkerBuilding<BuildingBuilder.View>
{
    /**
     * Color constants for builder list.
     */
    public static final int RED       = Color.getByName("red", 0);
    public static final int DARKGREEN = Color.getByName("darkgreen", 0);
    public static final int BLACK     = Color.getByName("black", 0);

    /**
     * List of resources needed.
     */
    @NotNull
    private final List<BuildingBuilderResource> resources = new ArrayList<>();

    /**
     * Tick to update the list.
     */
    private int tick = 0;

    /**
     * Constructor for window builder hut.
     *
     * @param building {@link BuildingBuilder.View}.
     */
    public WindowHutBuilder(final BuildingBuilder.View building)
    {
        super(building, Constants.MOD_ID + HUT_BUILDER_RESOURCE_SUFFIX);
        pullResourcesFromHut();
        registerButton(RESOURCE_ADD, this::transferItems);
    }

    /**
     * Retrieve resources from the building to display in GUI.
     */
    private void pullResourcesFromHut()
    {
        final PlayerInventory inventory = this.mc.player.inventory;
        final boolean isCreative = this.mc.player.isCreative();

        resources.clear();
        resources.addAll(building.getResources().values());
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
                    stack -> !ItemStackUtils.isEmpty(stack) && stack.isItemEqual(resource.getItemStack()));
            }
            resource.setPlayerAmount(amountToSet);
        }

        resources.sort(new BuildingBuilderResource.ResourceComparator());
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

        //Make sure we have a fresh view
        Network.getNetwork().sendToServer(new MarkBuildingDirtyMessage(this.building));

        findPaneOfTypeByID(LABEL_CONSTRUCTION_NAME, Label.class).setLabelText(building.getConstructionName());
        findPaneOfTypeByID(LABEL_CONSTRUCTION_POS, Label.class).setLabelText(building.getConstructionPos());
        findPaneOfTypeByID(LABEL_PROGRESS, Label.class).setLabelText(building.getProgress());
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
        final Label resourceLabel = rowPane.findPaneOfTypeByID(RESOURCE_NAME, Label.class);
        final Label resourceMissingLabel = rowPane.findPaneOfTypeByID(RESOURCE_MISSING, Label.class);
        final Label neededLabel = rowPane.findPaneOfTypeByID(RESOURCE_AVAILABLE_NEEDED, Label.class);
        final Button addButton = rowPane.findPaneOfTypeByID(RESOURCE_ADD, Button.class);

        switch (resource.getAvailabilityStatus())
        {
            case DONT_HAVE:
                addButton.disable();
                resourceLabel.setColor(RED, RED);
                resourceMissingLabel.setColor(RED, RED);
                neededLabel.setColor(RED, RED);
                break;
            case NEED_MORE:
                addButton.enable();
                resourceLabel.setColor(RED, RED);
                resourceMissingLabel.setColor(RED, RED);
                neededLabel.setColor(RED, RED);
                break;
            case HAVE_ENOUGH:
                addButton.enable();
                resourceLabel.setColor(DARKGREEN, DARKGREEN);
                resourceMissingLabel.setColor(DARKGREEN, DARKGREEN);
                neededLabel.setColor(DARKGREEN, DARKGREEN);
                break;
            case NOT_NEEDED:
            default:
                addButton.disable();
                resourceLabel.setColor(BLACK, BLACK);
                resourceMissingLabel.setColor(BLACK, BLACK);
                neededLabel.setColor(BLACK, BLACK);
                break;
        }

        //position the addResource Button to the right
        final int buttonX = rowPane.getWidth() - addButton.getWidth() - (rowPane.getHeight() - addButton.getHeight()) / 2;
        final int buttonY = rowPane.getHeight() - addButton.getHeight() - 2;
        addButton.setPosition(buttonX, buttonY);

        resourceLabel.setLabelText(resource.getName());
        final int missing = resource.getMissingFromPlayer();
        if (missing < 0)
        {
            resourceMissingLabel.setLabelText(Integer.toString(missing));
        }
        else
        {
            resourceMissingLabel.setLabelText("");
        }

        neededLabel.setLabelText(resource.getAvailable() + " / " + resource.getAmount());
        rowPane.findPaneOfTypeByID(RESOURCE_ID, Label.class).setLabelText(Integer.toString(index));
        rowPane.findPaneOfTypeByID(RESOURCE_QUANTITY_MISSING, Label.class).setLabelText(Integer.toString(resource.getAmount() - resource.getAvailable()));

        final ItemStack stack = new ItemStack(resource.getItem(), 1);
        stack.setTag(resource.getItemStack().getTag());
        rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(stack);
    }

    /**
     * Returns the name of a building.
     *
     * @return Name of a building.
     */
    @NotNull
    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerhuts.buildersHut";
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        final String currentPage = findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).getCurrentView().getID();
        if (currentPage.equals(PAGE_RESOURCES))
        {
            if (tick++ == 20)
            {
                pullResourcesFromHut();
                tick = 0;
            }
            window.findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class).refreshElementPanes();
        }
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
        final Label idLabel = pane.findPaneOfTypeByID(RESOURCE_ID, Label.class);
        final int index = Integer.parseInt(idLabel.getLabelText());
        final BuildingBuilderResource res = resources.get(index);
        if (res == null)
        {
            Log.getLogger().warn("WindowHutBuilder.transferItems: Error - Could not find the resource.");
        }
        else
        {
            // The itemStack size should not be greater than itemStack.getMaxStackSize, We send 1 instead
            // and use quantity for the size
            @NotNull final ItemStack itemStack = res.getItemStack().copy();
            itemStack.setCount(1);
            final Label quantityLabel = pane.findPaneOfTypeByID(RESOURCE_QUANTITY_MISSING, Label.class);
            final int quantity = Integer.parseInt(quantityLabel.getLabelText());
            final int needed = res.getAmount() - res.getAvailable();
            res.setAvailable(Math.min(res.getAmount(), res.getAvailable() + res.getPlayerAmount()));
            res.setPlayerAmount(Math.max(0, res.getPlayerAmount() - needed));
            resources.sort(new BuildingBuilderResource.ResourceComparator());
            Network.getNetwork().sendToServer(new TransferItemsRequestMessage(this.building, itemStack, quantity, true));
        }
    }
}
