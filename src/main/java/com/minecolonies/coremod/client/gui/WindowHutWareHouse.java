package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneBuilders;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ButtonImage;
import com.ldtteam.blockout.controls.ItemIcon;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.utils.BuildingBuilderResource;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingWareHouse;
import com.minecolonies.coremod.network.messages.server.colony.building.MarkBuildingDirtyMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.RemoveMinimumStockFromBuildingMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.warehouse.SortWarehouseMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.warehouse.UpgradeWarehouseMessage;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.TranslationConstants.WAREHOUSE_SORTED;
import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.coremod.client.gui.WindowHutBuilder.*;

/**
 * Window for the home building.
 */
public class WindowHutWareHouse extends AbstractWindowBuilding<BuildingWareHouse.View>
{

    /**
     * The Warehouse view
     */
    private final BuildingWareHouse.View building;

    /**
     * Required building level for sorting.
     */
    private static final int BUILDING_LEVEL_FOR_SORTING = 3;

    /**
     * Limit reached label.
     */
    private static final String LABEL_LIMIT_REACHED = "com.minecolonies.coremod.gui.warehouse.limitreached";

    /**
     * Resource scrolling list.
     */
    private final ScrollingList resourceList;

    /**
     * If further upgrades should be locked.
     */
    private boolean lockUpgrade = false;

    /**
     * Constructor for window warehouse hut.
     *
     * @param building {@link BuildingWareHouse.View}.
     */
    public WindowHutWareHouse(final BuildingWareHouse.View building)
    {
        super(building, Constants.MOD_ID + HUT_WAREHOUSE_RESOURCE_SUFFIX);
        this.building = building;
        registerButton(RESOURCE_ADD, this::transferItems);
        registerButton(SORT_WAREHOUSE_BUTTON, this::sortWarehouse);

        resourceList = window.findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class);

        registerButton(STOCK_ADD, this::addStock);
        if (building.hasReachedLimit())
        {
            final ButtonImage button = findPaneOfTypeByID(STOCK_ADD, ButtonImage.class);
            button.setText(LanguageHandler.format(LABEL_LIMIT_REACHED));
            button.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/builderhut/builder_button_medium_dark.png"));
        }

        registerButton(STOCK_REMOVE, this::removeStock);
    }

    /**
     * Remove the stock.
     *
     * @param button the button.
     */
    private void removeStock(final Button button)
    {
        final int row = resourceList.getListElementIndexByPane(button);
        final Tuple<ItemStorage, Integer> tuple = building.getStock().get(row);
        building.getStock().remove(row);
        Network.getNetwork().sendToServer(new RemoveMinimumStockFromBuildingMessage(building, tuple.getA().getItemStack()));
        updateStockList();
    }

    /**
     * Add the stock.
     */
    private void addStock()
    {
        if (!building.hasReachedLimit())
        {
            new WindowSelectRes(this, building, stack -> true).open();
        }
    }

    @Override
    public void onOpened()
    {
        if (building.getBuildingLevel() < BUILDING_LEVEL_FOR_SORTING)
        {
            final ButtonImage sortButton = findPaneOfTypeByID(SORT_WAREHOUSE_BUTTON, ButtonImage.class);
            PaneBuilders.tooltipBuilder()
                .append(new TranslationTextComponent("com.minecolonies.coremod.gui.warehouse.sort.disabled.1", BUILDING_LEVEL_FOR_SORTING))
                .appendNL(new TranslationTextComponent("com.minecolonies.coremod.gui.warehouse.sort.disabled.2", BUILDING_LEVEL_FOR_SORTING))
                .hoverPane(sortButton)
                .build();
            sortButton.disable();
        }

        super.onOpened();

        updateResourcePane();
        updateStockList();
        //Make sure we have a fresh view
        Network.getNetwork().sendToServer(new MarkBuildingDirtyMessage(this.building));
    }

    /**
     * Update one row pad with its resource informations.
     */
    private void updateResourcePane()
    {
        final BuildingBuilderResource resource = new BuildingBuilderResource(new ItemStack(Blocks.EMERALD_BLOCK, 1), 1);

        final int amountToSet;
        final PlayerInventory inventory = this.mc.player.inventory;
        final boolean isCreative = this.mc.player.isCreative();
        if (isCreative)
        {
            amountToSet = resource.getAmount();
        }
        else
        {
            amountToSet = InventoryUtils.getItemCountInItemHandler(new InvWrapper(inventory), resource.getItem());
        }
        resource.setPlayerAmount(amountToSet);

        final Text resourceLabel = findPaneOfTypeByID(RESOURCE_NAME, Text.class);
        final Text resourceMissingLabel = findPaneOfTypeByID(RESOURCE_MISSING, Text.class);
        final Text neededLabel = findPaneOfTypeByID(RESOURCE_AVAILABLE_NEEDED, Text.class);
        final Button addButton = findPaneOfTypeByID(RESOURCE_ADD, Button.class);

        BuildingBuilderResource.RessourceAvailability availability = resource.getAvailabilityStatus();

        if (building.getStorageUpgradeLevel() >= BuildingWareHouse.MAX_STORAGE_UPGRADE || building.getBuildingLevel() < building.getBuildingMaxLevel() || lockUpgrade)
        {
            availability = BuildingBuilderResource.RessourceAvailability.NOT_NEEDED;
        }

        findPaneOfTypeByID(UPGRADE_PROGRESS_LABEL, Text.class).setText(LanguageHandler.format("com.minecolonies.coremod.gui.xofz",
          building.getStorageUpgradeLevel(),
          BuildingWareHouse.MAX_STORAGE_UPGRADE));

        switch (availability)
        {
            case DONT_HAVE:
                addButton.disable();
                resourceLabel.setColors(RED);
                resourceMissingLabel.setColors(RED);
                neededLabel.setColors(RED);
                break;
            case NEED_MORE:
                addButton.enable();
                resourceLabel.setColors(RED);
                resourceMissingLabel.setColors(RED);
                neededLabel.setColors(RED);
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
                if (building.getBuildingLevel() < building.getBuildingMaxLevel())
                {
                    resourceLabel.hide();
                    resourceMissingLabel.hide();
                    neededLabel.hide();
                    addButton.setText(new StringTextComponent("X").setStyle(Style.EMPTY.setFormatting(TextFormatting.DARK_RED)));
                    PaneBuilders.tooltipBuilder()
                        .append(new TranslationTextComponent("com.minecolonies.coremod.gui.warehouse.upgrade.disabled.1", building.getBuildingMaxLevel()))
                        .appendNL(new TranslationTextComponent("com.minecolonies.coremod.gui.warehouse.upgrade.disabled.2", building.getBuildingMaxLevel()))
                        .hoverPane(addButton)
                        .build();
                }
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
        findPaneOfTypeByID(RESOURCE_QUANTITY_MISSING, Text.class).setText(Integer.toString(resource.getAmount() - resource.getAvailable()));

        if(building.getBuildingLevel() >= building.getBuildingMaxLevel())
        {
            final ItemStack resourceStackOfOne = new ItemStack(resource.getItem(), 1);
            resourceStackOfOne.setTag(resource.getItemStack().getTag());
            findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(resourceStackOfOne);
        }
    }

    /**
     * Updates the resource list in the GUI with the info we need.
     */
    private void updateStockList()
    {
        resourceList.enable();
        resourceList.show();
        final List<Tuple<ItemStorage, Integer>> tempRes = new ArrayList<>(building.getStock());

        //Creates a dataProvider for the unemployed resourceList.
        resourceList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return tempRes.size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final ItemStack resource = tempRes.get(index).getA().getItemStack().copy();
                resource.setCount(resource.getMaxStackSize());

                rowPane.findPaneOfTypeByID(RESOURCE_NAME, Text.class).setText(resource.getDisplayName());
                rowPane.findPaneOfTypeByID(QUANTITY_LABEL, Text.class).setText(String.valueOf(tempRes.get(index).getB()));
                rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(resource);
            }
        });
    }

    /**
     * On Button click transfer Items.
     */
    private void transferItems()
    {
        Network.getNetwork().sendToServer(new UpgradeWarehouseMessage(this.building));
        building.incrementStorageUpgrade();
        lockUpgrade = true;
        this.updateResourcePane();
    }

    /**
     * On button click for warehouse sorting.
     */
    private void sortWarehouse()
    {
        if (building.getBuildingLevel() >= BUILDING_LEVEL_FOR_SORTING)
        {
            Network.getNetwork().sendToServer(new SortWarehouseMessage(this.building));
            LanguageHandler.sendPlayerMessage(Minecraft.getInstance().player, WAREHOUSE_SORTED);
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
        return "com.minecolonies.coremod.gui.workerhuts.buildingWareHouse";
    }
}
