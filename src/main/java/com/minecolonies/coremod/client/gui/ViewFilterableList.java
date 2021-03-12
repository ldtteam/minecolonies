package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ItemIcon;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.controls.TextField;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.blockout.views.View;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.modules.ItemListModuleView;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
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
    protected final AbstractBuildingView building;

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
     * @param window     the view this belongs to.
     * @param parent     the parent window.
     * @param building   the building it belongs to.
     * @param desc       the description on the top of the page.
     * @param id         the id of this window (page order of filterable lists).
     * @param isInverted if the list is inverted.
     */
    public ViewFilterableList(
      final View window,
      final AbstractHutFilterableLists parent,
      final AbstractBuildingWorker.View building,
      final String desc,
      final String id,
      final boolean isInverted)
    {
        this.window = window;
        this.id = id;

        resourceList = window.findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class);
        window.findPaneOfTypeByID(DESC_LABEL, Text.class).setText(desc);
        this.building = building;
        this.isInverted = isInverted;
        this.parent = parent;
    }

    /**
     * To be called by the owning window on button click.
     *
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
        if (button.getTextAsString().equals(ON))
        {
            button.setText(OFF);
            if (isInverted)
            {
                building.getModuleViewMatching(ItemListModuleView.class, view -> view.getId().equals(id)).ifPresent(m -> m.addItem(allItems.get(row)));
            }
            else
            {
                building.getModuleViewMatching(ItemListModuleView.class, view -> view.getId().equals(id)).ifPresent(m -> m.removeItem(allItems.get(row)));
            }
        }
        else
        {
            button.setText(ON);
            if (isInverted)
            {
                building.getModuleViewMatching(ItemListModuleView.class, view -> view.getId().equals(id)).ifPresent(m -> m.removeItem(allItems.get(row)));
            }
            else
            {
                building.getModuleViewMatching(ItemListModuleView.class, view -> view.getId().equals(id)).ifPresent(m -> m.addItem(allItems.get(row)));
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
                                                                || stack.getDisplayName().getString().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US));
        allItems.clear();
        allItems.addAll(getBlockList(filterPredicate));
        allItems.addAll(getExceptions().stream().filter(storage -> filterPredicate.test(storage.getItemStack())).collect(Collectors.toList()));

        allItems.sort((o1, o2) -> {

            boolean o1Allowed = building.getModuleViewMatching(ItemListModuleView.class, view -> view.getId().equals(id))
                                  .map(m -> m.isAllowedItem(new ItemStorage(o1.getItemStack()))).orElse(false);

            boolean o2Allowed = building.getModuleViewMatching(ItemListModuleView.class, view -> view.getId().equals(id))
                                  .map(m -> m.isAllowedItem(new ItemStorage(o2.getItemStack()))).orElse(false);

            if(!o1Allowed && o2Allowed)
            {
                return isInverted ? -1 : 1;
            }
            else if(o1Allowed && !o2Allowed)
            {
                return isInverted ? 1 : -1;
            }
            else
            {
                return 0;
            }
        });

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
     *
     * @return a list of {@link ItemStorage}s that do not match the predicate.
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
                final Text resourceLabel = rowPane.findPaneOfTypeByID(RESOURCE_NAME, Text.class);
                resourceLabel.setText(resource.getDisplayName());
                resourceLabel.setColors(WHITE);
                rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(resource);
                final boolean isAllowedItem  = building.getModuleViewMatching(ItemListModuleView.class, view -> view.getId().equals(id))
                                                 .map(m -> m.isAllowedItem(new ItemStorage(resource))).orElse(!isInverted);
                final Button switchButton = rowPane.findPaneOfTypeByID(BUTTON_SWITCH, Button.class);

                if ((isInverted && !isAllowedItem) || (!isInverted && isAllowedItem))
                {
                    switchButton.setText(ON);
                }
                else
                {
                    switchButton.setText(OFF);
                }
            }
        });
    }
}
