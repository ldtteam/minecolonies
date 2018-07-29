package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.compatibility.CompatibilityManager;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.ItemIcon;
import com.minecolonies.blockout.controls.Label;
import com.minecolonies.blockout.views.ScrollingList;
import com.minecolonies.blockout.views.SwitchView;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingComposter;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class WindowHutComposter extends AbstractWindowWorkerBuilding<BuildingComposter.View>
{

    /**
     * Button leading the player to the next page.
     */
    private static final String BUTTON_PREV_PAGE = "prevPage";

    /**
     * Button leading the player to the previous page.
     */
    private static final String BUTTON_NEXT_PAGE = "nextPage";

    /**
     * Id of the the assign button inside the GUI.
     */
    private static final String TAG_BUTTON_ASSIGN = "addItem";

    /**
     * Id of the the fields list inside the GUI.
     */
    private static final String LIST_RECIPES = "items";

    /**
     * Button leading to the previous page.
     */
    private Button buttonPrevPage;

    /**
     * Button leading to the next page.
     */
    private Button buttonNextPage;

    /**
     * ScrollList with the fields.
     */
    private ScrollingList recipeList;

    /**
     * Tag of the pages view.
     */
    private static final String VIEW_PAGES = "pages";

    /**
     * Tag of the recipe name.
     */
    private static final String TAG_NAME = "name";

    /**
     * String which displays a non assigned item.
     */
    private static final String RED_X = "X";

    /**
     * String which displays an assigned item.
     */
    private static final String APPROVE = Character.toString((char)10003);;


    /**
     * Id of the icon inside the GUI.
     */
    private static final String TAG_ICON = "icon";

    /**
     * List of the items that can be used by the barrel
     */
    private ArrayList<ItemStorage> compostableItems;


    /**
     * Constructor for the window of the worker building.
     *
     * @param building class extending
     */
    public WindowHutComposter(final BuildingComposter.View building)
    {
        super(building, Constants.MOD_ID+":gui/windowHutComposter.xml");
        registerButton(BUTTON_PREV_PAGE, this::prevClicked);
        registerButton(BUTTON_NEXT_PAGE, this::nextClicked);
        registerButton(TAG_BUTTON_ASSIGN, this::assignClicked);

        compostableItems = (ArrayList<ItemStorage>) ColonyManager.getCompatibilityManager().getCopyOfCompostableItems();

    }

    /**
     * Fired when assign has been clicked in the field list.
     *
     * @param button clicked button.
     */
    private void assignClicked(@NotNull final Button button)
    {
        final int row = recipeList.getListElementIndexByPane(button);

        if (button.getLabel().equals(APPROVE))
        {
            button.setLabel(RED_X);
            building.removeCompostableItem(compostableItems.get(row));
        }
        else
        {
            button.setLabel(APPROVE);
            building.addCompostableItem(compostableItems.get(row));
        }

        window.findPaneOfTypeByID(LIST_RECIPES, ScrollingList.class).refreshElementPanes();

    }


    @Override
    public void onOpened()
    {
        super.onOpened();

        findPaneOfTypeByID(BUTTON_PREV_PAGE, Button.class).setEnabled(false);
        buttonPrevPage = findPaneOfTypeByID(BUTTON_PREV_PAGE, Button.class);
        buttonNextPage = findPaneOfTypeByID(BUTTON_NEXT_PAGE, Button.class);

        recipeList = findPaneOfTypeByID(LIST_RECIPES, ScrollingList.class);
        recipeList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return compostableItems.size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final ItemStorage item = compostableItems.get(index);
                final ItemStack stack = item.getItemStack();
                @NotNull final String owner =  stack.getDisplayName();

                rowPane.findPaneOfTypeByID(TAG_NAME, Label.class).setLabelText(owner);
                final Button assignButton = rowPane.findPaneOfTypeByID(TAG_BUTTON_ASSIGN, Button.class);

                if (building.isAllowedItem(item))
                {
                    assignButton.setLabel(APPROVE);
                }
                else
                {
                    assignButton.setLabel(RED_X);
                }

                rowPane.findPaneOfTypeByID(TAG_ICON, ItemIcon.class).setItem(stack);
            }
        });
    }

    @Override
    public String getBuildingName()
    {
        return "Composter Hut";
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
