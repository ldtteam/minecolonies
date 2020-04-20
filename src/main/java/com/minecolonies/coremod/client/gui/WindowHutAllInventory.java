package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.*;
import com.ldtteam.blockout.views.ScrollingList;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.tileentities.TileEntityRack;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;

import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.WindowConstants.*;
import com.minecolonies.api.util.constant.WindowConstants;
/**
 * Window for a hut name entry.
 */
public class WindowHutAllInventory extends AbstractWindowSkeleton
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
    private       String        filter         = "";
    /**
     * The sortDescriptor so how we want to sort
     */
    private       int           sortDescriptor = 0;

    /**
     * The building associated to the GUI.
     */
    private final IBuildingView building;

    /**
     * Constructor for a hut rename entry window.
     *
     * @param b {@link AbstractBuilding}
     */
    public WindowHutAllInventory(final IBuildingView b)
    {
        super(Constants.MOD_ID + HUT_ALL_INVENTORY_SUFFIX);
        this.building = b;
        registerButton(BUTTON_SORT, this::setSortFlag);
        this.stackList = findPaneOfTypeByID(LIST_ALLINVENTORY, ScrollingList.class);
        updateResources();
    }

    @Override
    public void onOpened()
    {

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
                findPaneOfTypeByID(BUTTON_SORT, ButtonImage.class).setLabel("v^");
                break;
            case ASC_SORT:
                findPaneOfTypeByID(BUTTON_SORT, ButtonImage.class).setLabel("A^");
                break;
            case DESC_SORT:
                findPaneOfTypeByID(BUTTON_SORT, ButtonImage.class).setLabel("Av");
                break;
            case COUNT_ASC_SORT:
                findPaneOfTypeByID(BUTTON_SORT, ButtonImage.class).setLabel("1^");
                break;
            case COUNT_DESC_SORT:
                findPaneOfTypeByID(BUTTON_SORT, ButtonImage.class).setLabel("1v");
                break;
            default:
                break;
        }

        updateResources();
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
            if (rack instanceof ChestTileEntity)
            {
                final int size = ((ChestTileEntity) rack).getSizeInventory();
                for (int slot = 0; slot < size; slot++)
                {
                    final ItemStack stack = ((ChestTileEntity) rack).getStackInSlot(slot);
                    if (!ItemStackUtils.isEmpty(stack))
                    {
                        items.add(stack.copy());

                    }

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
                final Label resourceLabel = rowPane.findPaneOfTypeByID("ressourceStackName", Label.class);
                resourceLabel.setLabelText(resource.getItemStack().getDisplayName().getFormattedText());
                final Label qtys = rowPane.findPaneOfTypeByID("quantities", Label.class);
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
}
