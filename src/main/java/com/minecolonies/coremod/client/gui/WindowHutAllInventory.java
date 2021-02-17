package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.*;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.tileentities.TileEntityRack;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.Utils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.event.HighlightManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.WindowConstants.*;

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
     * The previous window.
     */
    private final Window prev;

    /**
     * Constructor for a hut rename entry window.
     *
     * @param b    {@link AbstractBuilding}
     * @param prev the previous window.
     */
    public WindowHutAllInventory(final IBuildingView b, final Window prev)
    {
        super(Constants.MOD_ID + HUT_ALL_INVENTORY_SUFFIX);
        this.building = b;
        registerButton(BUTTON_SORT, this::setSortFlag);
        registerButton(BUTTON_BACK, this::back);
        this.stackList = findPaneOfTypeByID(LIST_ALLINVENTORY, ScrollingList.class);
        updateResources();
        registerButton(LOCATE, this::locate);
        this.prev = prev;
    }

    private void locate(final Button button)
    {
        final int row = stackList.getListElementIndexByPane(button);
        final ItemStorage storage = allItems.get(row);
        final List<BlockPos> containerList = building.getContainerList();

        for (BlockPos blockPos : containerList)
        {
            final TileEntity rack = Minecraft.getInstance().world.getTileEntity(blockPos);
            if (rack instanceof TileEntityRack)
            {
                if (((TileEntityRack) rack).hasItemStack(storage.getItemStack(), 1, false))
                {
                    HighlightManager.HIGHLIGHT_MAP.put("inventoryHighlight", new Tuple<>(blockPos, Minecraft.getInstance().world.getGameTime() + 120 * 20));
                    Minecraft.getInstance().player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.locating"), Minecraft.getInstance().player.getUniqueID());
                    close();
                    return;
                }
            }
        }
    }

    /**
     * On prev clicked.
     */
    private void back()
    {
        this.close();
        this.prev.open();
    }

    /**
     * Increments the sortDescriptor and sets the GUI Button accordingly Valid Stages
     * 0 - 4 NO_SORT
     * 0   No Sorting, like wysiwyg ASC_SORT
     * 1   Name Ascending DESC_SORT
     * 2   Name Descending COUNT_ASC_SORT
     * 3   Itemcount Ascending COUNT_DESC_SORT
     * 4   Itemcount Descending
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
                findPaneOfTypeByID(BUTTON_SORT, ButtonImage.class).setText("v^");
                break;
            case ASC_SORT:
                findPaneOfTypeByID(BUTTON_SORT, ButtonImage.class).setText("A^");
                break;
            case DESC_SORT:
                findPaneOfTypeByID(BUTTON_SORT, ButtonImage.class).setText("Av");
                break;
            case COUNT_ASC_SORT:
                findPaneOfTypeByID(BUTTON_SORT, ButtonImage.class).setText("1^");
                break;
            case COUNT_DESC_SORT:
                findPaneOfTypeByID(BUTTON_SORT, ButtonImage.class).setText("1v");
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
        final List<BlockPos> containerList = building.getContainerList();

        final Map<ItemStorage, Integer> storedItems = new HashMap<>();
        final World world = building.getColony().getWorld();
        containerList.add(building.getPosition());

        for (final BlockPos blockPos : containerList)
        {
            final TileEntity rack = world.getTileEntity(blockPos);
            if (rack instanceof TileEntityRack)
            {

                Map<ItemStorage, Integer> rackStorage = ((TileEntityRack) rack).getAllContent();

                for (final Map.Entry<ItemStorage, Integer> entry : rackStorage.entrySet())
                {
                    if (storedItems.containsKey(entry.getKey()))
                    {
                        storedItems.put(entry.getKey(), storedItems.get(entry.getKey()) + entry.getValue());
                    }
                    else
                    {
                        storedItems.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }

        final List<ItemStorage> filterItems = new ArrayList<>();
        storedItems.forEach((storage, amount) -> {
            storage.setAmount(amount);
            filterItems.add(storage);
        });
        final Predicate<ItemStorage> filterPredicate = stack -> filter.isEmpty()
                                                                  || stack.getItemStack().getTranslationKey().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US))
                                                                  || stack.getItemStack()
                                                                       .getDisplayName()
                                                                       .getString()
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

        final Comparator<ItemStorage> compareByName = Comparator.comparing((ItemStorage o) -> o.getItemStack().getDisplayName().getString());
        final Comparator<ItemStorage> compareByCount = Comparator.comparingInt(ItemStorage::getAmount);
        switch (sortDescriptor)
        {
            case NO_SORT:
                break;
            case ASC_SORT:
                allItems.sort(compareByName);
                break;
            case DESC_SORT:
                allItems.sort(compareByName.reversed());
                break;
            case COUNT_ASC_SORT:
                allItems.sort(compareByCount);
                break;
            case COUNT_DESC_SORT:
                allItems.sort(compareByCount.reversed());
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
                final ItemStorage resource = allItems.get(index);
                final Text resourceLabel = rowPane.findPaneOfTypeByID("ressourceStackName", Text.class);
                final String name = resource.getItemStack().getDisplayName().getString();
                resourceLabel.setText(name.substring(0, Math.min(17, name.length())));
                final Text qtys = rowPane.findPaneOfTypeByID("quantities", Text.class);
                if(!Screen.hasShiftDown())
                {
                    qtys.setText(Utils.format(resource.getAmount()));
                }
                else
                {
                    qtys.setText(Integer.toString(resource.getAmount()));
                }
                final Item imagesrc = resource.getItemStack().getItem();
                final ItemStack image = new ItemStack(imagesrc, 1);
                image.setTag(resource.getItemStack().getTag());
                rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(image);
            }
        });
    }

    @Override
    public boolean onKeyTyped(final char ch, final int key)
    {
        final boolean result = super.onKeyTyped(ch, key);
        if (result)
        {
            filter = findPaneOfTypeByID("names", TextField.class).getText();
            updateResources();
        }
        return result;
    }
}
