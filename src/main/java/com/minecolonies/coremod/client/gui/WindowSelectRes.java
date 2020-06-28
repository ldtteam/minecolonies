package com.minecolonies.coremod.client.gui;

import com.google.common.collect.ImmutableList;
import com.ldtteam.blockout.Color;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.*;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.blockout.views.Window;
import com.ldtteam.structurize.api.util.ItemStackUtils;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.messages.server.colony.building.AddMinimumStockToBuildingMessage;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.minecolonies.api.util.constant.WindowConstants.BUTTON_SELECT;

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
    private final Predicate<ItemStack> test;

    /**
     * The filter string.
     */
    private String filter = "";

    /**
     * The origin window where we routed from.
     */
    private final Window origin;

    /**
     * The colony id this belongs to.
     */
    private final IBuildingView building;

    /**
     * Create a selection window with the origin window as input.
     * @param origin the origin.
     * @param building the building.
     * @param test the testing predicate for the selector.
     */
    public WindowSelectRes(final Window origin, final IBuildingView building, final Predicate<ItemStack> test)
    {
        super("minecolonies:gui/windowselectres.xml");
        this.resourceList = this.findPaneOfTypeByID("resources", ScrollingList.class);
        this.origin = origin;
        registerButton(BUTTON_DONE, this::doneClicked);
        registerButton(BUTTON_CANCEL, this::cancelClicked);
        registerButton(BUTTON_SELECT, this::selectClicked);
        this.findPaneOfTypeByID("qty", TextField.class).setText("1");
        this.findPaneOfTypeByID("resourceIcon", ItemIcon.class).setItem(new ItemStack(Items.AIR));
        this.findPaneOfTypeByID("resourceName", Label.class).setLabelText(new ItemStack(Items.AIR).getDisplayName().getUnformattedComponentText());
        this.building = building;
        this.test = test;
    }

    /**
     * Select button clicked.
     * @param button the clicked button.
     */
    private void selectClicked(final Button button)
    {
        final int row = this.resourceList.getListElementIndexByPane(button);
        final ItemStack to = this.allItems.get(row);
        this.findPaneOfTypeByID("resourceIcon", ItemIcon.class).setItem(to);
        this.findPaneOfTypeByID("resourceName", Label.class).setLabelText(to.getDisplayName().getUnformattedComponentText());
    }

    /**
     * Cancel clicked to close this window.
     */
    private void cancelClicked()
    {
        this.close();
        this.origin.open();
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
            Log.getLogger().warn("Invalid input in Selection Window for Quantity, defaulting to 1!");
        }

        if (!ItemStackUtils.isEmpty(to))
        {
            Network.getNetwork().sendToServer(new AddMinimumStockToBuildingMessage(building, to, qty));
            this.origin.open();
        }
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
        this.allItems.addAll(ImmutableList.copyOf(StreamSupport.stream(Spliterators.spliteratorUnknownSize(ForgeRegistries.ITEMS.iterator(), Spliterator.ORDERED), false)
                                                                 .map(ItemStack::new)
                                                                 .filter((stack) -> (test.test(stack) && (this.filter.isEmpty() || stack.getTranslationKey().toLowerCase(Locale.US)
                                                                                                                .contains(this.filter.toLowerCase(Locale.US)))))
                                                                 .collect(Collectors.toList())));
        this.updateResourceList();
    }

    @Override
    public boolean onKeyTyped(char ch, int key)
    {
        boolean result = super.onKeyTyped(ch, key);
        String name = this.findPaneOfTypeByID(INPUT_NAME, TextField.class).getText();
        if (!name.isEmpty())
        {
            this.filter = name;
        }

        this.updateResources();
        return result;
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
                ItemStack resource = tempRes.get(index);
                Label resourceLabel = rowPane.findPaneOfTypeByID("resourceName", Label.class);
                resourceLabel.setLabelText(resource.getDisplayName().getUnformattedComponentText());
                resourceLabel.setColor(WHITE, WHITE);
                (rowPane.findPaneOfTypeByID("resourceIcon", ItemIcon.class)).setItem(resource);
            }
        });
    }
}