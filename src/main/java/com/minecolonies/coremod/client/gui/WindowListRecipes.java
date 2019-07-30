package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.ButtonHandler;
import com.minecolonies.blockout.controls.ItemIcon;
import com.minecolonies.blockout.views.Box;
import com.minecolonies.blockout.views.ScrollingList;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.IColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.IBuildingWorker;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.network.messages.AddRemoveRecipeMessage;
import com.minecolonies.coremod.network.messages.ChangeRecipePriorityMessage;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window for the hiring or firing of a worker.
 */
public class WindowListRecipes extends Window implements ButtonHandler
{
    /**
     * Id of the citizen list in the GUI.
     */
    private static final String RECIPE_LIST = "recipes";

    /**
     * Link to the xml file of the window.
     */
    private static final String BUILDING_NAME_RESOURCE_SUFFIX = ":gui/windowListRecipes.xml";

    /**
     * The output item icon.
     */
    private static final String OUTPUT_ICON = "output";

    /**
     * The item icon of the resource.
     */
    private static final String RESOURCE  = "resource%d";

    /**
     * The item icon of the 3x3 resource.
     */
    private static final String RES  = "res%d";

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
    public WindowListRecipes(final IColonyView c, final BlockPos buildingId)
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
                final ItemIcon icon = rowPane.findPaneOfTypeByID(OUTPUT_ICON, ItemIcon.class);
                icon.setItem(recipe.getPrimaryOutput());

                final String name;
                if(recipe.getInput().size() <= 4)
                {
                    name = RESOURCE;
                }
                else
                {
                    name = RES;
                    rowPane.findPaneOfTypeByID("3x3", Box.class).setVisible(true);
                    rowPane.findPaneOfTypeByID("2x2", Box.class).setVisible(false);
                    icon.setPosition(80, 17);
                }

                for(int i = 0; i < recipe.getInput().size(); i++)
                {
                    rowPane.findPaneOfTypeByID(String.format(name, i+1), ItemIcon.class).setItem(recipe.getInput().get(i));
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
        else if (button.getID().equals(BUTTON_FORWARD))
        {
            building.switchIndex(row, row + 1);
            MineColonies.getNetwork().sendToServer(new ChangeRecipePriorityMessage(building, row, true));
        }
        else if (button.getID().equals(BUTTON_BACKWARD))
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
