package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.ItemIcon;
import com.minecolonies.blockout.controls.Label;
import com.minecolonies.blockout.views.ScrollingList;
import com.minecolonies.blockout.views.SwitchView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBaker;
import com.minecolonies.coremod.entity.ai.citizen.baker.BakerRecipes;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_BAKER;

import java.util.List;

/**
 * Window for the fisherman hut.
 */
public class WindowHutBaker extends AbstractWindowWorkerBuilding<BuildingBaker.View>
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
    private static final String TAG_BUTTON_ASSIGN = "assignRecipe";

    /**
     * Id of the the fields list inside the GUI.
     */
    private static final String LIST_RECIPES = "recipes";

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
     * String which displays the release of a field.
     */
    private static final String RED_X = "X";

    /**
     * String which displays adding a field.
     */
    private static final String APPROVE = " ";


    /**
     * Id of the icon inside the GUI.
     */
    private static final String TAG_ICON = "icon";

    /**
     * Constructor for the window of the fisherman.
     *
     * @param building {@link BuildingBaker.View}.
     */
    public WindowHutBaker(final BuildingBaker.View building)
    {
        super(building, Constants.MOD_ID + ":gui/windowHutBaker.xml");
        registerButton(BUTTON_PREV_PAGE, this::prevClicked);
        registerButton(BUTTON_NEXT_PAGE, this::nextClicked);
        registerButton(TAG_BUTTON_ASSIGN, this::assignClicked);
    }

    /**
     * Fired when assign has been clicked in the field list.
     *
     * @param button clicked button.
     */
    private void assignClicked(@NotNull final Button button)
    {
        final int row = recipeList.getListElementIndexByPane(button);

        if (button.getLabel().equals(RED_X))
        {
            button.setLabel(APPROVE);
            building.setRecipeAllowed(row, false,building.getID());
        }
        else
        {
            button.setLabel(RED_X);
            building.setRecipeAllowed(row, true,building.getID());
        }

        window.findPaneOfTypeByID(LIST_RECIPES, ScrollingList.class).refreshElementPanes();
    	
    }


    @Override
    public void onOpened()
    {
        super.onOpened();

        final List<IRecipeStorage> recipes = BakerRecipes.getRecipes();

        findPaneOfTypeByID(BUTTON_PREV_PAGE, Button.class).setEnabled(false);
        buttonPrevPage = findPaneOfTypeByID(BUTTON_PREV_PAGE, Button.class);
        buttonNextPage = findPaneOfTypeByID(BUTTON_NEXT_PAGE, Button.class);

        recipeList = findPaneOfTypeByID(LIST_RECIPES, ScrollingList.class);
        recipeList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return recipes.size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final IRecipeStorage recipe = recipes.get(index);
                    final ItemStack stack = recipe.getPrimaryOutput();
                    @NotNull final String owner =  stack.getDisplayName();

                    rowPane.findPaneOfTypeByID(TAG_NAME, Label.class).setLabelText(owner);
                    final Button assignButton = rowPane.findPaneOfTypeByID(TAG_BUTTON_ASSIGN, Button.class);

                    if (building.isRecipeAllowed(index))
                    {
                        assignButton.setLabel(RED_X);
                    }
                    else
                    {
                        assignButton.setLabel(APPROVE);
                    }

                    rowPane.findPaneOfTypeByID(TAG_ICON, ItemIcon.class).setItem(stack);
            }
        });
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
        return COM_MINECOLONIES_COREMOD_GUI_BAKER;
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

