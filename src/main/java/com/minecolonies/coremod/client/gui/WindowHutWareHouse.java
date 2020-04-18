package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.*;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.tileentities.AbstractTileEntityRack;
import com.minecolonies.api.tileentities.TileEntityRack;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingWareHouse;
import com.minecolonies.coremod.colony.buildings.utils.BuildingBuilderResource;
import com.minecolonies.coremod.network.messages.MarkBuildingDirtyMessage;
import com.minecolonies.coremod.network.messages.RemoveMinimumStockFromBuildingMessage;
import com.minecolonies.coremod.network.messages.SortWarehouseMessage;
import com.minecolonies.coremod.network.messages.UpgradeWarehouseMessage;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.coremod.client.gui.WindowHutBuilder.*;

/**
 * Window for the home building.
 */
public class WindowHutWareHouse extends AbstractWindowBuilding<BuildingWareHouse.View>
{
    /**
     * List of all item stacks in the warehouse.
     */
    List<ItemStorage> allItems = new ArrayList<>();

    /**
     * Resource scrolling list.
     */
    private final ScrollingList stackList;
    /**
     * The filter for the resource list.
     */
    private       String        filter = "";

    /**
     * No Sorting stage. how it comes from Database so it gets feeded
     */
    public static final int NO_SORT         = 0;
    /**
     * Name Ascending
     */
    public static final int ASC_SORT        = 1;
    /**
     * Name Descending
     */
    public static final int DESC_SORT       = 2;
    /**
     * Itemcount Ascending
     */
    public static final int COUNT_ASC_SORT  = 3;
    /**
     * Itemcount Descending
     */
    public static final int COUNT_DESC_SORT = 4;
    /**
     * The sortDescriptor so how we want to sort
     */
    int sortDescriptor = 0;
    /**
     * The Stringdefine for the GUI page
     */
    public static final  String                 LIST_ALLINVENTORY          = "allinventory";
    /**
     * The Warehouse view
     */
    private final        BuildingWareHouse.View building;
    /**
     * Required building level for sorting.
     */
    private static final int                    BUILDING_LEVEL_FOR_SORTING = 3;

    /**
     * Limit reached label.
     */
    private static final String LABEL_LIMIT_REACHED = "com.minecolonies.coremod.gui.warehouse.limitreached";

    /**
     * Allow more upgrades of the storage.
     */
    private boolean allowMoreStorageUpgrades = false;

    /**
     * Resource scrolling list.
     */
    private final ScrollingList resourceList;

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
        registerButton("sortStorageFilter", this::setSortFlag);
        resourceList = window.findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class);
        this.stackList = findPaneOfTypeByID(LIST_ALLINVENTORY, ScrollingList.class);
        if (building.isBuildingMaxLevel() && building.canUpgradeStorage())
        {
            allowMoreStorageUpgrades = true;
        }
        registerButton(STOCK_ADD, this::addStock);
        if (building.hasReachedLimit())
        {
            final ButtonImage button = findPaneOfTypeByID(STOCK_ADD, ButtonImage.class);
            button.setLabel(LanguageHandler.format(LABEL_LIMIT_REACHED));
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
        Network.getNetwork().sendToServer(new RemoveMinimumStockFromBuildingMessage(tuple.getA().getItemStack(), building.getColony().getID(), building.getID()));
        updateStockList();
    }

    /**
     * Add the stock.
     */
    private void addStock()
    {
        if (!building.hasReachedLimit())
        {
            new WindowSelectRes(this, building.getColony().getID(), building.getID()).open();
        }
    }

    /**
     * Increments the sortDescriptor and sets the GUI Button accordingly Valid Stages 0 - 4 NO_SORT         0   No Sorting, like wysiwyg ASC_SORT        1   Name Ascending
     * DESC_SORT       2   Name Descending COUNT_ASC_SORT  3   Itemcount Ascending COUNT_DESC_SORT 4   Itemcount Descending
     **/
    private void setSortFlag()
    {
        sortDescriptor++;
        if (sortDescriptor > 4)
        {
            sortDescriptor = NO_SORT;
        }
        switch (sortDescriptor)
        {
            case NO_SORT:
                findPaneOfTypeByID("sortStorageFilter", ButtonImage.class).setLabel("v^");
                break;
            case ASC_SORT:
                findPaneOfTypeByID("sortStorageFilter", ButtonImage.class).setLabel("A^");
                break;
            case DESC_SORT:
                findPaneOfTypeByID("sortStorageFilter", ButtonImage.class).setLabel("Av");
                break;
            case COUNT_ASC_SORT:
                findPaneOfTypeByID("sortStorageFilter", ButtonImage.class).setLabel("1^");
                break;
            case COUNT_DESC_SORT:
                findPaneOfTypeByID("sortStorageFilter", ButtonImage.class).setLabel("1v");
                break;
            default:
                break;
        }

        updateResources();
    }

    @Override
    public void onOpened()
    {
        if (building.getBuildingLevel() < BUILDING_LEVEL_FOR_SORTING)
        {
            findPaneOfTypeByID(SORT_WAREHOUSE_BUTTON, ButtonImage.class).hide();
        }
        super.onOpened();

        updateResourcePane();
        updateStockList();
        updateResources();
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

        final Label resourceLabel = findPaneOfTypeByID(RESOURCE_NAME, Label.class);
        final Label resourceMissingLabel = findPaneOfTypeByID(RESOURCE_MISSING, Label.class);
        final Label neededLabel = findPaneOfTypeByID(RESOURCE_AVAILABLE_NEEDED, Label.class);
        final Button addButton = findPaneOfTypeByID(RESOURCE_ADD, Button.class);

        BuildingBuilderResource.RessourceAvailability availability = resource.getAvailabilityStatus();

        if (!allowMoreStorageUpgrades)
        {
            availability = BuildingBuilderResource.RessourceAvailability.NOT_NEEDED;
        }

        switch (availability)
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
        findPaneOfTypeByID(RESOURCE_QUANTITY_MISSING, Label.class).setLabelText(Integer.toString(resource.getAmount() - resource.getAvailable()));

        findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(new ItemStack(resource.getItem(), 1));
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

                final Label resourceLabel = rowPane.findPaneOfTypeByID(RESOURCE_NAME, Label.class);
                resourceLabel.setLabelText(resource.getDisplayName().getFormattedText());

                final Label quantityLabel = rowPane.findPaneOfTypeByID(QUANTITY_LABEL, Label.class);
                quantityLabel.setLabelText(String.valueOf(tempRes.get(index).getB()));

                rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(resource);
            }
        });
    }

    /**
     * Update the item list.
     */
    private void updateResources()
    {
        List<ItemStorage> filterItems = new ArrayList<>();
        final List<BlockPos> containerList = building.getContainerList();
        List<ItemStack> items = new ArrayList<>();
        int count = containerList.size();
        World world = building.getColony().getWorld();

        for (int s = 0; s < count; s++)
        {
            final TileEntity rack = world.getTileEntity(containerList.get(s));
            if (rack instanceof TileEntityRack)
            {

                Map<ItemStorage, Integer> storage = ((TileEntityRack) rack).getAllContent();

                for (final Map.Entry<ItemStorage, Integer> entry : storage.entrySet())
                {
                    items.add(new ItemStorage(entry.getKey().getItemStack(), entry.getValue(), false).getItemStack());
                }
            }
        }

        Map<ItemStorage, ItemStorage> storedItems = new HashMap<>();
        storedItems.clear();

        for (final ItemStack currentItem : items)
        {
            final ItemStorage currentStorage = new ItemStorage(currentItem, currentItem.getCount(), false);

            if (storedItems.containsKey(currentStorage))
            {
                final ItemStorage existing = storedItems.get(currentStorage);
                existing.setAmount(existing.getAmount() + currentStorage.getAmount());
            }
            else
            {
                storedItems.put(currentStorage, currentStorage);
            }
        }

        filterItems.clear();
        for (ItemStorage entry : storedItems.keySet())
        {
            filterItems.add(entry);
        }

        final Predicate<ItemStorage> filterPredicate = stack -> filter.isEmpty()
                                                                  || stack.getItemStack().getTranslationKey().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US))
                                                                  || stack.getItemStack()
                                                                       .getDisplayName()
                                                                       .getFormattedText()
                                                                       .toLowerCase(Locale.US)
                                                                       .contains(filter.toLowerCase(Locale.US));

        allItems.clear();
        if (filter.isEmpty())
        {
            allItems.addAll(filterItems);
        }
        else
        {
            allItems.addAll(filterItems.stream().filter(filterPredicate).collect(Collectors.toList()));
        }

        Comparator<ItemStorage> compareByName =
          (ItemStorage o1, ItemStorage o2) -> o1.getItemStack().getDisplayName().getFormattedText().compareTo(o2.getItemStack().getDisplayName().getFormattedText());
        Comparator<ItemStorage> compareByCount = (ItemStorage o1, ItemStorage o2) -> o1.getAmount() - o2.getAmount();
        switch (sortDescriptor)
        {
            case NO_SORT:
                break;
            case ASC_SORT:
                Collections.sort(allItems, compareByName);
                break;
            case DESC_SORT:
                Collections.sort(allItems, compareByName.reversed());
                break;
            case COUNT_ASC_SORT:
                Collections.sort(allItems, compareByCount);
                break;
            case COUNT_DESC_SORT:
                Collections.sort(allItems, compareByCount.reversed());
                break;
            default:
                break;
        }

        updateResourceList();
    }

    /**
     * Updates the resource list in the GUI with the info we need.
     */
    private void updateResourceList()
    {
        stackList.enable();

        //Creates a dataProvider for the unemployed stackList.
        stackList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return allItems.size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                ItemStorage resource = allItems.get(index);
                final Label resourceLabel = rowPane.findPaneOfTypeByID("ressName", Label.class);
                resourceLabel.setLabelText(resource.getItemStack().getDisplayName().getFormattedText());
                final Label qtys = rowPane.findPaneOfTypeByID("qtys", Label.class);
                qtys.setLabelText(Integer.toString(resource.getAmount()));
                Item imagesrc = resource.getItemStack().getItem();
                ItemStack image = new ItemStack(imagesrc, 1);
                rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(image);
            }
        });
    }

    @Override
    public boolean onKeyTyped(final char ch, final int key)
    {
        final boolean result = super.onKeyTyped(ch, key);
        filter = findPaneOfTypeByID("names", TextField.class).getText();
        updateResources();
        return result;
    }

    /**
     * On Button click transfer Items.
     */
    private void transferItems()
    {
        Network.getNetwork().sendToServer(new UpgradeWarehouseMessage(this.building));
        allowMoreStorageUpgrades = false;
        this.updateResourcePane();
    }

    /**
     * On button click for warehouse sorting.
     */
    private void sortWarehouse()
    {
        Network.getNetwork().sendToServer(new SortWarehouseMessage(this.building));
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
