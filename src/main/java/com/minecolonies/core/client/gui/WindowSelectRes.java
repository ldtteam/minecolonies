package com.minecolonies.core.client.gui;

import com.ldtteam.blockui.Color;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.controls.TextField;
import com.ldtteam.blockui.views.BOWindow;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.WindowConstants.BUTTON_SELECT;
import static com.minecolonies.api.util.constant.WindowConstants.NAME_LABEL;

public class WindowSelectRes extends AbstractWindowSkeleton
{
    /**
     * Static vars.
     */
    private static final String BUTTON_DONE   = "done";
    private static final String BUTTON_CANCEL = "cancel";
    private static final String INPUT_NAME    = "name";
    private static final int    WHITE         = Color.getByName("white", 0);

    /**
     * All game items in a list.
     */
    private final List<ItemStack> allItems = new ArrayList<>();

    /**
     * Resource list to render.
     */
    private final ScrollingList resourceList;

    /**
     * Predicate to test for.
     */
    private final Predicate<ItemStack>           test;

    /**
     * The consumer that receives the block quantity.
     */
    private final BiConsumer<ItemStack, Integer> consumer;

    /**
     * The filter string.
     */
    private String filter = "";

    /**
     * Update delay.
     */
    private int tick;

    /**
     * Create a selection window with the origin window as input.
     *
     * @param origin the origin.
     * @param test   the testing predicate for the selector.
     */
    public WindowSelectRes(final BOWindow origin, final Predicate<ItemStack> test, final BiConsumer<ItemStack, Integer> consumer, final boolean displayQty)
    {
        super("minecolonies:gui/windowselectres.xml", origin);
        this.resourceList = this.findPaneOfTypeByID("resources", ScrollingList.class);
        registerButton(BUTTON_DONE, this::doneClicked);
        registerButton(BUTTON_CANCEL, this::cancelClicked);
        registerButton(BUTTON_SELECT, this::selectClicked);
        this.findPaneOfTypeByID("qty", TextField.class).setText("1");
        if (!displayQty)
        {
            this.findPaneOfTypeByID("qty", TextField.class).hide();
            this.findPaneOfTypeByID("qtylabel", Text.class).hide();
        }
        this.findPaneOfTypeByID("resourceIcon", ItemIcon.class).setItem(new ItemStack(Items.AIR));
        this.findPaneOfTypeByID("resourceName", Text.class).setText(new ItemStack(Items.AIR).getHoverName());
        this.test = test;
        this.consumer = consumer;

        window.findPaneOfTypeByID(NAME_LABEL, TextField.class).setHandler(input -> {
            final String newFilter = input.getText();
            if (!newFilter.equals(filter))
            {
                filter = newFilter;
                this.tick = 10;
            }
        });
    }

    /**
     * Select button clicked.
     *
     * @param button the clicked button.
     */
    private void selectClicked(final Button button)
    {
        final int row = this.resourceList.getListElementIndexByPane(button);
        final ItemStack to = this.allItems.get(row);
        this.findPaneOfTypeByID("resourceIcon", ItemIcon.class).setItem(to);
        this.findPaneOfTypeByID("resourceName", Text.class).setText(to.getHoverName());
    }

    /**
     * Cancel clicked to close this window.
     */
    private void cancelClicked()
    {
        this.close();
    }

    /**
     * Done clicked to reopen the origin window.
     */
    private void doneClicked()
    {
        final ItemStack to = this.findPaneOfTypeByID("resourceIcon", ItemIcon.class).getItem();
        int qty = 1;
        try
        {
            qty = Integer.parseInt(this.findPaneOfTypeByID("qty", TextField.class).getText());
        }
        catch (final NumberFormatException ex)
        {
            Log.getLogger().warn("Invalid input in Selection BOWindow for Quantity, defaulting to 1!");
        }

        this.consumer.accept(to, qty);
        this.close();
    }

    @Override
    public void onOpened()
    {
        this.updateResources();
    }

    /**
     * Update the list of resources.
     */
    private void updateResources()
    {
        this.allItems.clear();

        for (final ItemStack stack : ItemStackUtils.allItemsPlusInventory(Minecraft.getInstance().player))
        {
            if (test.test(stack) && (this.filter.isEmpty()
                                       || stack.getDescriptionId().toLowerCase(Locale.US).contains(this.filter.toLowerCase(Locale.US))
                                       || stack.getHoverName().getString().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US))))
            {
                this.allItems.add(stack);
            }
        }

        allItems.sort(Comparator.comparingInt(s1 -> StringUtils.getLevenshteinDistance(s1.getHoverName().getString(), filter)));
        this.updateResourceList();
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        if (tick > 0 && --tick == 0)
        {
            updateResources();
        }
    }

    /**
     * Fill the resource list.
     */
    private void updateResourceList()
    {
        this.resourceList.enable();
        this.resourceList.show();
        final List<ItemStack> tempRes = new ArrayList<>(this.allItems);
        this.resourceList.setDataProvider(new ScrollingList.DataProvider()
        {
            public int getElementCount()
            {
                return tempRes.size();
            }

            public void updateElement(int index, @NotNull Pane rowPane)
            {
                final ItemStack resource = tempRes.get(index);
                final Text resourceLabel = rowPane.findPaneOfTypeByID("resourceName", Text.class);
                resourceLabel.setText(resource.getHoverName());
                resourceLabel.setColors(WHITE);
                rowPane.findPaneOfTypeByID("resourceIcon", ItemIcon.class).setItem(resource);
            }
        });
    }
}