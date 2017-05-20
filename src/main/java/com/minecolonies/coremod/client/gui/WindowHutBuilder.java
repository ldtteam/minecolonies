package com.minecolonies.coremod.client.gui;

import com.minecolonies.blockout.Color;
import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.Label;
import com.minecolonies.blockout.views.ScrollingList;
import com.minecolonies.blockout.views.SwitchView;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.utils.BuildingBuilderResource;
import com.minecolonies.coremod.colony.buildings.views.BuildingBuilderView;
import com.minecolonies.coremod.lib.Constants;
import com.minecolonies.coremod.network.messages.MarkBuildingDirtyMessage;
import com.minecolonies.coremod.network.messages.TransferItemsRequestMessage;
import com.minecolonies.coremod.util.InventoryUtils;
import com.minecolonies.coremod.util.Log;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Window for the builder hut.
 */
public class WindowHutBuilder extends AbstractWindowWorkerBuilding<BuildingBuilderView>
{
    /**
     * The builders gui file.
     */
    private static final String HUT_BUILDER_RESOURCE_SUFFIX        = ":gui/windowhutbuilder.xml";
    private static final String LIST_RESOURCES                     = "resources";
    private static final String PAGE_RESOURCES                     = "resourceActions";
    private static final String VIEW_PAGES                         = "pages";
    private static final String RESOURCE_NAME                      = "resourceName";
    private static final String RESOURCE_AVAILABLE_NEEDED          = "resourceAvailableNeeded";
    private static final String RESOURCE_MISSING                   = "resourceMissing";
    private static final String RESOURCE_ADD                       = "resourceAdd";
    private static final String RESOURCE_ID                        = "resourceId";
    private static final int    RESOURCE_ID_POSITION               = 4;
    private static final String RESOURCE_QUANTITY_MISSING          = "resourceQuantity";
    private static final int    RESOURCE_QUANTITY_MISSING_POSITION = 5;

    private static final int RED       = Color.getByName("red", 0);
    private static final int DARKGREEN = Color.getByName("darkgreen", 0);
    private static final int BLACK     = Color.getByName("black", 0);

    private final BuildingBuilderView builder;

    /**
     * List of ressources needed.
     */
    @NotNull
    private final List<BuildingBuilderResource> resources = new ArrayList<>();

    /**
     * Constructor for window builder hut.
     *
     * @param building {@link com.minecolonies.coremod.colony.buildings.views.BuildingBuilderView}.
     */
    public WindowHutBuilder(final BuildingBuilderView building)
    {
        super(building, Constants.MOD_ID + HUT_BUILDER_RESOURCE_SUFFIX);
        this.builder = building;
        pullResourcesFromHut();
        registerButton(RESOURCE_ADD, this::transferItems);
    }

    /**
     * Retrieve resources from the building to display in GUI.
     */
    private void pullResourcesFromHut()
    {
        final AbstractBuilding.View newView = builder.getColony().getBuilding(builder.getID());
        if (newView instanceof BuildingBuilderView)
        {
            final BuildingBuilderView updatedView = (BuildingBuilderView) newView;
            final InventoryPlayer inventory = this.mc.player.inventory;

            resources.clear();
            resources.addAll(updatedView.getResources().values());
            for (final BuildingBuilderResource resource : resources)
            {
                resource.setPlayerAmount(InventoryUtils.getItemCountInItemHandler(new InvWrapper(inventory), resource.getItem(), resource.getDamageValue()));
            }

            resources.sort(new BuildingBuilderResource.ResourceComparator());
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

        //Make sure we have a fresh view
        MineColonies.getNetwork().sendToServer(new MarkBuildingDirtyMessage(this.building));
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

        //position the addRessource Button to the right
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

        neededLabel.setLabelText(Integer.toString(resource.getAvailable()) + " / " + Integer.toString(resource.getAmount()));
        rowPane.findPaneOfTypeByID(RESOURCE_ID, Label.class).setLabelText(Integer.toString(index));
        rowPane.findPaneOfTypeByID(RESOURCE_QUANTITY_MISSING, Label.class).setLabelText(Integer.toString(resource.getAmount() - resource.getAvailable()));
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        final String currentPage = findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).getCurrentView().getID();
        if (currentPage.equals(PAGE_RESOURCES))
        {
            pullResourcesFromHut();
            window.findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class).refreshElementPanes();
        }
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
        return "com.minecolonies.coremod.gui.workerHuts.buildersHut";
    }

    /**
     * On Button click transfert Items.
     *
     * @param button the clicked button.
     */
    private void transferItems(@NotNull final Button button)
    {
        @NotNull final Label idLabel = (Label) button.getParent().getChildren().get(RESOURCE_ID_POSITION);
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
            @NotNull final ItemStack itemStack = new ItemStack(res.getItem(), 1, res.getDamageValue());
            @NotNull final Label quantityLabel = (Label) button.getParent().getChildren().get(RESOURCE_QUANTITY_MISSING_POSITION);
            final int quantity = Integer.parseInt(quantityLabel.getLabelText());
            MineColonies.getNetwork().sendToServer(new TransferItemsRequestMessage(this.building, itemStack, quantity));
        }
    }
}
