package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.ItemIcon;
import com.minecolonies.blockout.controls.Label;
import com.minecolonies.blockout.controls.TextField;
import com.minecolonies.blockout.views.ScrollingList;
import com.minecolonies.blockout.views.View;
import com.minecolonies.coremod.colony.buildings.views.AbstractFilterableListsView;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_OFF;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_ON;
import static com.minecolonies.api.util.constant.WindowConstants.*;
import static org.jline.utils.AttributedStyle.WHITE;

/**
 * GUI window for filterable lists as a list of compostable items, burnables, etc.
 */
public class ViewFilterableList
{
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
    public static final String BUTTON_SWITCH = "switch";

    /**
     * String describing on for the gui.
     */
    public static final String ON = LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_ON);

    /**
     * String describing off for the gui.
     */
    public static final String OFF = LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_OFF);

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
    protected final AbstractFilterableListsView building;

    /**
     * The parent window.
     */
    private final AbstractHutFilterableLists parent;

    /**
     * The filter for the resource list.
     */
    private String filter = "";

    /**
     * Check for inversion of the list.
     */
    private final boolean isInverted;

    /**
     * The specific view of this filterable list.
     */
    private final View window;

    /**
     * The id of this window.
     */
    private final String id;

    /**
     *
     * @param window the view this belongs to.
     * @param parent the parent window.
     * @param building the building it belongs to.
     * @param desc the description on the top of the page.
     * @param id the id of this window (page order of filterable lists).
     * @param isInverted if the list is inverted.
     */
    public ViewFilterableList(final View window, final AbstractHutFilterableLists parent, final AbstractFilterableListsView building, final String desc, final String id, final boolean isInverted)
    {
        this.window = window;
        this.id = id;

        resourceList = window.findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class);
        window.findPaneOfTypeByID(DESC_LABEL, Label.class).setLabelText(desc);
        this.building = building;
        this.isInverted = isInverted;
        this.parent = parent;
    }

    /**
     * To be called by the owning window on button click.
     * @param button the clicked button.
     */
    public void onButtonClick(final Button button)
    {
        if (Objects.equals(button.getID(), BUTTON_SWITCH))
        {
            switchClicked(button);
        }
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
                building.addItem(id, allItems.get(row));
            }
            else
            {
                building.removeItem(id, allItems.get(row));
            }
        }
        else
        {
            button.setLabel(ON);
            if (isInverted)
            {
                building.removeItem(id, allItems.get(row));
            }
            else
            {
                building.addItem(id, allItems.get(row));
            }
        }
        resourceList.refreshElementPanes();
    }

    /**
     * On opened, supposed to be called by the window this belongs to.
     */
    public void onOpened()
    {
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
    private Collection<? extends ItemStorage> getBlockList(final Predicate<ItemStack> filterPredicate)
    {
        return parent.getBlockList(filterPredicate, id);
    }

    /**
     * Add exceptions which do not match the predicate after scanning.
     */
    private List<ItemStorage> getExceptions()
    {
        return Collections.emptyList();
    }

    /**
     * On key typed, supposed to be called by the owning window.
     */
    public void onKeyTyped()
    {
        filter = window.findPaneOfTypeByID(INPUT_NAME, TextField.class).getText();
        updateResources();
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

                if ((isInverted && !building.isAllowedItem(id, new ItemStorage(resource))) || (!isInverted && building.isAllowedItem(id, new ItemStorage(resource))))
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
}
