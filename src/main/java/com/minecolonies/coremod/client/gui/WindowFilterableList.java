package com.minecolonies.coremod.client.gui;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.Log;
import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.ItemIcon;
import com.minecolonies.blockout.controls.Label;
import com.minecolonies.blockout.controls.TextField;
import com.minecolonies.blockout.views.ScrollingList;
import com.minecolonies.blockout.views.SwitchView;
import com.minecolonies.coremod.colony.buildings.views.FilterableListView;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_OFF;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_ON;
import static com.minecolonies.api.util.constant.WindowConstants.*;
import static org.jline.utils.AttributedStyle.WHITE;

/**
 * GUI window for filterable lists as a list of compostable items, burnables, etc.
 */
public abstract class WindowFilterableList<B extends FilterableListView> extends AbstractWindowWorkerBuilding<B>
{
    /**
     * Tag of the pages view.
     */
    private static final String VIEW_PAGES = "pages";

    /**
     * The GUI id.
     */
    private static final String WINDOW_FILTERABLE_LIST = ":gui/windowfilterablelist.xml";

    /**
     * The id of the "name" input field in the GUI.
     */
    private static final String INPUT_NAME = "input";

    /**
     * Description label Id.
     */
    public static final String DESC_LABEL = "desc";

    /**
     * Switch button Id.
     */
    private static final String BUTTON_SWITCH = "switch";

    /**
     * String describing on for the gui.
     */
    private static final String ON = LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_ON);

    /**
     * String describing off for the gui.
     */
    private static final String OFF = LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_OFF);

    /**
     * Button leading the player to the next page.
     */
    private static final String BUTTON_PREV_PAGE = "prevPage";

    /**
     * Button leading the player to the previous page.
     */
    private static final String BUTTON_NEXT_PAGE = "nextPage";

    /**
     * Button leading to the previous page.
     */
    private Button buttonPrevPage;

    /**
     * Button leading to the next page.
     */
    private Button buttonNextPage;

    /**
     * List of all item stacks in the game.
     */
    private final List<ItemStorage> allItems = new ArrayList<>();

    /**
     * Resource scrolling list.
     */
    private final ScrollingList resourceList;

    /**
     * The building this belongs to.
     */
    protected final FilterableListView building;

    /**
     * The filter for the resource list.
     */
    private String filter = "";

    /**
     * Check for inversion of the list.
     */
    protected boolean isInverted = false;

    /**
     * The filter predicate for the filterable list.
     */
    private Predicate<ItemStack> itemStackPredicate;

    /**
     * Public constructor to instantiate this window.
     *
     * @param building  the building to unselect from.
     * @param predicate the predicate to filter for.
     * @param desc      the description of the list.
     */
    public WindowFilterableList(final B building, final Predicate<ItemStack> predicate, final String desc)
    {
        super(building, Constants.MOD_ID + WINDOW_FILTERABLE_LIST);
        resourceList = findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class);
        this.itemStackPredicate = predicate;
        findPaneOfTypeByID(DESC_LABEL, Label.class).setLabelText(desc);
        this.building = building;

        registerButton(BUTTON_PREV_PAGE, this::prevClicked);
        registerButton(BUTTON_NEXT_PAGE, this::nextClicked);
        registerButton(BUTTON_SWITCH, this::switchClicked);
    }

    /**
     * Fired when assign has been clicked in the field list.
     *
     * @param button clicked button.
     */
    private void switchClicked(@NotNull final Button button)
    {
        final int row = resourceList.getListElementIndexByPane(button);

        if (button.getLabel().equals(ON))
        {
            button.setLabel(OFF);
            if (isInverted)
            {
                building.addItem(allItems.get(row));
            }
            else
            {
                building.removeItem(allItems.get(row));
            }
        }
        else
        {
            button.setLabel(ON);
            if (isInverted)
            {
                building.removeItem(allItems.get(row));
            }
            else
            {
                building.addItem(allItems.get(row));
            }
        }
        resourceList.refreshElementPanes();
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        findPaneOfTypeByID(BUTTON_PREV_PAGE, Button.class).setEnabled(false);
        buttonPrevPage = findPaneOfTypeByID(BUTTON_PREV_PAGE, Button.class);
        buttonNextPage = findPaneOfTypeByID(BUTTON_NEXT_PAGE, Button.class);
        updateResources();
    }

    /**
     * Update the item list.
     */
    private void updateResources()
    {
        final Predicate<ItemStack> filterPredicate = stack -> filter.isEmpty()
                                                                || stack.getTranslationKey().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US))
                                                                || stack.getDisplayName().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US));
        allItems.clear();
        allItems.addAll(getBlockList(filterPredicate));
        allItems.addAll(getExceptions().stream().filter(storage -> filterPredicate.test(storage.getItemStack())).collect(Collectors.toList()));
        updateResourceList();
    }

    /**
     * Get the list of blocks which should be added.
     *
     * @param filterPredicate the predicate to filter all blocks for.
     * @return an immutable list of blocks.
     */
    public Collection<? extends ItemStorage> getBlockList(final Predicate<ItemStack> filterPredicate)
    {
        return ImmutableList.copyOf(StreamSupport.stream(Spliterators.spliteratorUnknownSize(Item.REGISTRY.iterator(), Spliterator.ORDERED), false).flatMap(item -> {
            final NonNullList<ItemStack> stacks = NonNullList.create();
            try
            {
                item.getSubItems(CreativeTabs.SEARCH, stacks);
            }
            catch (final Exception ex)
            {
                Log.getLogger().warn("Failed to get sub items from: " + item.getRegistryName(), ex);
            }

            return stacks.stream().filter(itemStackPredicate.and(filterPredicate)).map(ItemStorage::new);
        }).collect(Collectors.toList()));
    }

    /**
     * Add exceptions which do not match the predicate after scanning.
     */
    public List<ItemStorage> getExceptions()
    {
        return Collections.emptyList();
    }

    @Override
    public boolean onKeyTyped(final char ch, final int key)
    {
        final boolean result = super.onKeyTyped(ch, key);
        filter = findPaneOfTypeByID(INPUT_NAME, TextField.class).getText();
        updateResources();
        return result;
    }

    /**
     * Updates the resource list in the GUI with the info we need.
     */
    private void updateResourceList()
    {
        resourceList.enable();
        resourceList.show();
        final List<ItemStorage> tempRes = new ArrayList<>(allItems);

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
                final ItemStack resource = tempRes.get(index).getItemStack();
                final Label resourceLabel = rowPane.findPaneOfTypeByID(RESOURCE_NAME, Label.class);
                resourceLabel.setLabelText(resource.getDisplayName());
                resourceLabel.setColor(WHITE, WHITE);
                rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(resource);

                final Button switchButton = rowPane.findPaneOfTypeByID(BUTTON_SWITCH, Button.class);

                if ((isInverted && !building.isAllowedItem(new ItemStorage(resource))) || (!isInverted && building.isAllowedItem(new ItemStorage(resource))))
                {
                    switchButton.setLabel(ON);
                }
                else
                {
                    switchButton.setLabel(OFF);
                }
            }
        });
    }

    /**
     * Action performed when previous button is clicked.
     */
    private void prevClicked()
    {
        findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).previousView();
        buttonPrevPage.setEnabled(false);
        buttonNextPage.setEnabled(true);
    }

    /**
     * Action performed when next button is clicked.
     */
    private void nextClicked()
    {
        findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).nextView();
        buttonPrevPage.setEnabled(true);
        buttonNextPage.setEnabled(false);
    }
}
