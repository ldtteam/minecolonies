package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ItemIcon;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.controls.TextField;
import com.ldtteam.blockout.views.ScrollingList;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.network.messages.server.colony.OpenInventoryMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.postbox.PostBoxRequestMessage;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window for the replace block GUI.
 */
public class WindowPostBox extends AbstractWindowRequestTree
{
    /**
     * Id of the deliver available button inside the GUI.
     */
    private static final String               TAG_BUTTON_DELIVER_AVAILABLE = "deliverAvailable";
    /**
     * String which displays full delivery.
     */
    private static final String               RED_X                        = "§n§4X";
    /**
     * String which displays partial delivery..
     */
    private static final String               APPROVE                      = "✓";
    /**
     * List of all item stacks in the game.
     */
    private final        List<ItemStack>      allItems                     = new ArrayList<>();
    /**
     * Resource scrolling list.
     */
    private final        ScrollingList        stackList;
    /**
     * The building view of this window.
     */
    private final        AbstractBuildingView buildingView;
    /**
     * The filter for the resource list.
     */
    private              String               filter                       = "";
    /**
     * Whether to deliver what's currently in the warehouse and then cancel order.
     */
    private              boolean              deliverAvailable;

    /**
     * Create the postBox GUI.
     *
     * @param buildingView the building view.
     */
    public WindowPostBox(final AbstractBuildingView buildingView)
    {
        super(buildingView.getID(), Constants.MOD_ID + WINDOW_POSTBOX, buildingView.getColony());
        this.buildingView = buildingView;
        this.stackList = findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class);
        registerButton(BUTTON_INVENTORY, this::inventoryClicked);
        registerButton(BUTTON_REQUEST, this::requestClicked);
        registerButton(TAG_BUTTON_DELIVER_AVAILABLE, this::deliverPartialClicked);
    }

    /**
     * Action when a button opening an inventory is clicked.
     */
    private void inventoryClicked()
    {
        Network.getNetwork().sendToServer(new OpenInventoryMessage(buildingView));
    }

    /**
     * Action executed when request is clicked.
     *
     * @param button the clicked button.
     */
    private void requestClicked(final Button button)
    {
        final int row = stackList.getListElementIndexByPane(button);
        final ItemStack stack = allItems.get(row);
        int qty = stack.getMaxStackSize();
        for (final Pane child : button.getParent().getChildren())
        {
            if (child.getID().equals(INPUT_QTY))
            {
                try
                {
                    qty = Integer.parseInt(((TextField) child).getText());
                }
                catch (final NumberFormatException ex)
                {
                    //Be quiet about it.
                }
            }
        }

        Network.getNetwork().sendToServer(new PostBoxRequestMessage(buildingView, stack.copy(), qty, deliverAvailable));
    }

    private void deliverPartialClicked(@NotNull final Button button)
    {

        if (button.getTextAsString().equals(RED_X))
        {
            button.setText(APPROVE);
            this.deliverAvailable = true;
        }
        else
        {
            button.setText(RED_X);
            this.deliverAvailable = false;
        }
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        findPaneOfTypeByID(TAG_BUTTON_DELIVER_AVAILABLE, Button.class).setText(RED_X);

        updateResources();
    }

    /**
     * Update the item list.
     */
    private void updateResources()
    {
        final Predicate<ItemStack> filterPredicate = stack -> filter.isEmpty()
                                                                || stack.getDescriptionId().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US))
                                                                || stack.getHoverName().getString().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US))
                                                                || (stack.getItem() instanceof EnchantedBookItem && EnchantedBookItem.getEnchantments(stack).getCompound(0).getString("id").contains(filter.toLowerCase(Locale.US)));
        allItems.clear();
        allItems.addAll(getBlockList(filterPredicate));
        allItems.sort(Comparator.comparingInt(s1 -> StringUtils.getLevenshteinDistance(s1.getHoverName().getString(), filter)));
        updateResourceList();
    }

    /**
     * Get the list of blocks which should be added.
     *
     * @param filterPredicate the predicate to filter all blocks for.
     * @return an immutable list of blocks.
     */
    private Collection<? extends ItemStack> getBlockList(final Predicate<ItemStack> filterPredicate)
    {
        if (filter.isEmpty())
        {
            return IColonyManager.getInstance().getCompatibilityManager().getListOfAllItems();
        }
        return IColonyManager.getInstance().getCompatibilityManager().getListOfAllItems().stream().filter(filterPredicate).collect(Collectors.toList());
    }

    /**
     * Updates the resource list in the GUI with the info we need.
     */
    private void updateResourceList()
    {
        stackList.enable();
        stackList.show();
        final List<ItemStack> tempRes = new ArrayList<>(allItems);

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
                final ItemStack resource = tempRes.get(index);
                final Text resourceLabel = rowPane.findPaneOfTypeByID(RESOURCE_NAME, Text.class);
                resourceLabel.setText(resource.getHoverName());
                rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(resource);
            }
        });
    }

    @Override
    public boolean onKeyTyped(final char ch, final int key)
    {
        final boolean result = super.onKeyTyped(ch, key);
        filter = findPaneOfTypeByID(INPUT_NAME, TextField.class).getText();
        updateResources();
        return result;
    }
}
