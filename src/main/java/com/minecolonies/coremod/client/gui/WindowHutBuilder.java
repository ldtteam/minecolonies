package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Color;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ItemIcon;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.blockout.views.SwitchView;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.workorders.WorkOrderView;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.utils.BuildingBuilderResource;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.coremod.network.messages.server.colony.building.MarkBuildingDirtyMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.TransferItemsRequestMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.builder.BuilderSelectWorkOrderMessage;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
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
    public static final int ORANGE    = Color.getByName("orange", 0);

    /**
     * The advancement location.
     */
    private static final ResourceLocation GUIDE_ADVANCEMENT = new ResourceLocation(Constants.MOD_ID, "minecolonies/check_out_guide");

    /**
     * Button for toggling manual mode.
     */
    private static final String BUTTON_MANUAL_MODE = "manualMode";

    /**
     * List of resources needed.
     */
    @NotNull
    private final List<BuildingBuilderResource> resources = new ArrayList<>();

    /**
     * If the guide should be attempted to be opened.
     */
    private final boolean needGuide;

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
        this(building, true);
    }

    /**
     * Constructor for window builder hut.
     *
     * @param needGuide if the guide should be opened.
     * @param building  {@link BuildingBuilder.View}.
     */
    public WindowHutBuilder(final BuildingBuilder.View building, final boolean needGuide)
    {
        super(building, Constants.MOD_ID + HUT_BUILDER_RESOURCE_SUFFIX);
        pullResourcesFromHut();

        registerButton(RESOURCE_ADD, this::transferItems);
        registerButton(BUTTON_MANUAL_MODE, this::manualModeClicked);
        registerButton(WORK_ORDER_SELECT, this::selectWorkOrder);

        this.needGuide = needGuide;

        Button buttonManualMode = findPaneOfTypeByID(BUTTON_MANUAL_MODE, Button.class);
        if (building.getManualMode())
        {
            buttonManualMode.setText(LanguageHandler.format(TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_BUILDER_MANUAL));
        }
        else
        {
            buttonManualMode.setText(LanguageHandler.format(TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_BUILDER_AUTOMATIC));
        }
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
        if (needGuide)
        {
            final Advancement ad = Minecraft.getInstance().player.connection.getAdvancementManager().getAdvancementList().getAdvancement(GUIDE_ADVANCEMENT);
            if (ad == null || !Minecraft.getInstance().player.connection.getAdvancementManager().advancementToProgress.getOrDefault(ad, new AdvancementProgress()).isDone())
            {
                close();
                new WindowHutGuide(building).open();
                return;
            }
        }
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

        final ScrollingList workOrdersList = findPaneOfTypeByID(LIST_WORK_ORDERS, ScrollingList.class);
        workOrdersList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return building.getBuildOrders().size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                updateAvailableWorkOrders(index, rowPane);
            }
        });

        //Make sure we have a fresh view
        Network.getNetwork().sendToServer(new MarkBuildingDirtyMessage(this.building));

        findPaneOfTypeByID(LABEL_CONSTRUCTION_NAME, Text.class).setText(building.getConstructionName());
        findPaneOfTypeByID(LABEL_CONSTRUCTION_POS, Text.class).setText(building.getConstructionPos());
        findPaneOfTypeByID(LABEL_PROGRESS, Text.class).setText(building.getProgress());
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
        final Button addButton = rowPane.findPaneOfTypeByID(RESOURCE_ADD, Button.class);

        switch (resource.getAvailabilityStatus())
        {
            case DONT_HAVE:
                addButton.disable();
                resourceLabel.setColors(RED);
                resourceMissingLabel.setColors(RED);
                neededLabel.setColors(RED);
                break;
            case NEED_MORE:
                addButton.enable();
                resourceLabel.setColors(ORANGE);
                resourceMissingLabel.setColors(ORANGE);
                neededLabel.setColors(ORANGE);
                break;
            case HAVE_ENOUGH:
                addButton.enable();
                resourceLabel.setColors(DARKGREEN);
                resourceMissingLabel.setColors(DARKGREEN);
                neededLabel.setColors(DARKGREEN);
                break;
            case NOT_NEEDED:
            default:
                addButton.disable();
                resourceLabel.setColors(BLACK);
                resourceMissingLabel.setColors(BLACK);
                neededLabel.setColors(BLACK);
                break;
        }

        //position the addResource Button to the right
        final int buttonX = rowPane.getWidth() - addButton.getWidth() - (rowPane.getHeight() - addButton.getHeight()) / 2;
        final int buttonY = rowPane.getHeight() - addButton.getHeight() - 2;
        addButton.setPosition(buttonX, buttonY);

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

    /**
     * Updates the available work orders page.
     * 
     * @param index   index in the list of resources.
     * @param rowPane The Pane to use to display the information.
     */
    private void updateAvailableWorkOrders(final int index, @NotNull final Pane rowPane)
    {
        final WorkOrderView order = building.getBuildOrders().get(index);

        rowPane.findPaneOfTypeByID(WORK_ORDER_NAME, Text.class).setText(order.get());
        rowPane.findPaneOfTypeByID(WORK_ORDER_POS, Text.class).setText(order.getPos().getX() + " " + order.getPos().getY() + " " + order.getPos().getZ());
        rowPane.findPaneOfTypeByID(WORK_ORDER_ID, Text.class).setText(Integer.toString(order.getId()));
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
        final Text idLabel = pane.findPaneOfTypeByID(RESOURCE_ID, Text.class);
        final int index = Integer.parseInt(idLabel.getTextAsString());
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
            final Text quantityLabel = pane.findPaneOfTypeByID(RESOURCE_QUANTITY_MISSING, Text.class);
            final int quantity = Integer.parseInt(quantityLabel.getTextAsString());
            final int needed = res.getAmount() - res.getAvailable();
            res.setAvailable(Math.min(res.getAmount(), res.getAvailable() + res.getPlayerAmount()));
            res.setPlayerAmount(Math.max(0, res.getPlayerAmount() - needed));
            resources.sort(new BuildingBuilderResource.ResourceComparator());
            Network.getNetwork().sendToServer(new TransferItemsRequestMessage(this.building, itemStack, quantity, true));
        }
    }

    /**
     * On manual mode button clicked.
     * 
     * @param button the button that just got clicked.
     */
    private void manualModeClicked(@NotNull final Button button)
    {
        if (button.getTextAsString().equals(LanguageHandler.format(TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_BUILDER_MANUAL)))
        {
            button.setText(LanguageHandler.format(TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_BUILDER_AUTOMATIC));
            building.setManualMode(false);
        }
        else
        {
            button.setText(LanguageHandler.format(TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_BUILDER_MANUAL));
            building.setManualMode(true);
        }
    }

    /**
     * On click select the clicked work order.
     * 
     * @param button the clicked button.
     */
    private void selectWorkOrder(@NotNull final Button button)
    {
        final Pane pane = button.getParent();

        final int id = Integer.parseInt(pane.findPaneOfTypeByID(WORK_ORDER_ID, Text.class).getTextAsString());

        button.disable();
        Network.getNetwork().sendToServer(new MarkBuildingDirtyMessage(building));

        Network.getNetwork().sendToServer(new BuilderSelectWorkOrderMessage(building, id));
    }
}
