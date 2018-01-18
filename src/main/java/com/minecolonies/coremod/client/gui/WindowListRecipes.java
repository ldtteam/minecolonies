package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.ButtonHandler;
import com.minecolonies.blockout.controls.ItemIcon;
import com.minecolonies.blockout.views.ScrollingList;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.network.messages.AddRemoveRecipeMessage;
import com.minecolonies.coremod.network.messages.ChangeRecipePriorityMessage;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Window for the hiring or firing of a worker.
 */
public class WindowListRecipes extends Window implements ButtonHandler
{
    /**
     * Id of the done button in the GUI.
     */
    private static final String BUTTON_REMOVE = "remove";

    /**
     * Id of the cancel button in the GUI.
     */
    private static final String BUTTON_CANCEL = "cancel";

    /**
     * Id of the citizen list in the GUI.
     */
    private static final String RECIPE_LIST = "recipes";

    /**
     * Link to the xml file of the window.
     */
    private static final String BUILDING_NAME_RESOURCE_SUFFIX = ":gui/windowListRecipes.xml";

    /**
     * Button to change priority up in the gui.
     */
    private static final String BUTTON_UP = "up";

    /**
     * Button to change priority down in the gui.
     */
    private static final String BUTTON_DOWN = "down";

    /**
     * The output item icon.
     */
    private static final String OUTPUT_ICON = "output";

    /**
     * The item icon of the resource.
     */
    private static final String RESOURCE  = "resource%d";

    /**
     * Contains all the recipes.
     */
    private final List<IRecipeStorage> recipes = new ArrayList<>();

    /**
     * The view of the current building.
     */
    private final AbstractBuildingWorker.View building;

    /**
     * List of recipes which can be assigned.
     */
    private final ScrollingList recipeList;

    /**
     * Constructor for the window when the player wants to assign a worker for a certain home building.
     *
     * @param c          the colony view.
     * @param buildingId the building position.
     */
    public WindowListRecipes(final ColonyView c, final BlockPos buildingId)
    {
        super(Constants.MOD_ID + BUILDING_NAME_RESOURCE_SUFFIX);
        this.building = (AbstractBuildingWorker.View) c.getBuilding(buildingId);
        recipeList = findPaneOfTypeByID(RECIPE_LIST, ScrollingList.class);
        updateRecipes();
    }

    /**
     * Clears and resets/updates all recipes.
     */
    private void updateRecipes()
    {
        recipes.clear();
        recipes.addAll(building.getRecipes());
    }

    /**
     * Called when the GUI has been opened.
     * Will fill the fields and lists.
     */
    @Override
    public void onOpened()
    {
        updateRecipes();
        recipeList.enable();
        recipeList.show();
        //Creates a dataProvider for the homeless recipeList.
        recipeList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return recipes.size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                @NotNull final IRecipeStorage recipe = recipes.get(index);
                rowPane.findPaneOfTypeByID(OUTPUT_ICON, ItemIcon.class).setItem(recipe.getPrimaryOutput());

                for(int i = 0; i < recipe.getInput().size(); i++)
                {
                    rowPane.findPaneOfTypeByID(String.format(RESOURCE, i+1), ItemIcon.class).setItem(recipe.getInput().get(i));
                }

            }
        });
    }

    @Override
    public void onUpdate()
    {
        updateRecipes();
        window.findPaneOfTypeByID(RECIPE_LIST, ScrollingList.class).refreshElementPanes();
    }

    /**
     * Called when any button has been clicked.
     *
     * @param button the clicked button.
     */
    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        final int row = recipeList.getListElementIndexByPane(button) - 1;
        if (button.getID().equals(BUTTON_REMOVE))
        {
            final IRecipeStorage data = recipes.get(row+1);
            building.removeRecipe(row+1);
            MineColonies.getNetwork().sendToServer(new AddRemoveRecipeMessage(data, building, true));
        }
        else if (button.getID().equals(BUTTON_UP))
        {
            building.switchIndex(row, row + 1);
            MineColonies.getNetwork().sendToServer(new ChangeRecipePriorityMessage(building, row, true));
        }
        else if (button.getID().equals(BUTTON_DOWN))
        {
            building.switchIndex(row, row - 1);
            MineColonies.getNetwork().sendToServer(new ChangeRecipePriorityMessage(building, row, false));
        }
        else if (button.getID().equals(BUTTON_CANCEL))
        {
            building.openGui(false);
        }
    }
}
