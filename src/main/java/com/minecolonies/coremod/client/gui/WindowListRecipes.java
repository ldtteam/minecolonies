package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ButtonHandler;
import com.ldtteam.blockout.controls.ItemIcon;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.Box;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.blockout.views.Window;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.network.messages.server.colony.building.worker.AddRemoveRecipeMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.worker.ChangeRecipePriorityMessage;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
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
     * Id of the recipe list in the GUI.
     */
    private static final String RECIPE_LIST = "recipes";

    /**
     * Id of the recipe status label in the GUI.
     */
    private static final String RECIPE_STATUS="recipestatus";

    /**
     * Link to the xml file of the window.
     */
    private static final String BUILDING_NAME_RESOURCE_SUFFIX = ":gui/windowlistrecipes.xml";

    /**
     * The output item icon.
     */
    private static final String OUTPUT_ICON = "output";

    /**
     * The item icon of the resource.
     */
    private static final String RESOURCE = "res%d";

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

    private final Text recipeStatus;
    /**
     * Life count.
     */
    private int lifeCount = 0;

    /**
     * Constructor for the window when the player wants to see the list of a building's recipes.
     *
     * @param c          the colony view.
     * @param buildingId the building position.
     */
    public WindowListRecipes(final IColonyView c, final BlockPos buildingId)
    {
        super(Constants.MOD_ID + BUILDING_NAME_RESOURCE_SUFFIX);
        this.building = (AbstractBuildingWorker.View) c.getBuilding(buildingId);
        recipeList = findPaneOfTypeByID(RECIPE_LIST, ScrollingList.class);
        recipeStatus = findPaneOfTypeByID(RECIPE_STATUS, Text.class);
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

    @Override
    public void onOpened()
    {
        updateRecipes();
        recipeList.enable();
        recipeList.show();

        //Creates a dataProvider for the homeless recipeList.
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
                @NotNull final IRecipeStorage recipe = recipes.get(index);
                final ItemIcon icon = rowPane.findPaneOfTypeByID(OUTPUT_ICON, ItemIcon.class);
                List<ItemStack> displayStacks = recipe.getRecipeType().getOutputDisplayStacks();
                icon.setItem(displayStacks.get((lifeCount / LIFE_COUNT_DIVIDER) % (displayStacks.size())));

                if (!building.isRecipeAlterationAllowed())
                {
                    final Button removeButton = rowPane.findPaneOfTypeByID(BUTTON_REMOVE, Button.class);
                    if (removeButton != null)
                    {
                        removeButton.setVisible(false);
                    }
                }

                // Some special recipes might not include all necessary air blocks.
                if (recipe.getInput().size() < 4)
                {
                    for (int i = 0; i < 9; i++)
                    {
                        if (i < recipe.getInput().size())
                        {
                            final ItemStack displayItem = recipe.getInput().get(i).getItemStack();

                            rowPane.findPaneOfTypeByID(String.format(RESOURCE, i + 1), ItemIcon.class).setItem(displayItem);
                        }
                        else
                        {
                            rowPane.findPaneOfTypeByID(String.format(RESOURCE, i + 1), ItemIcon.class).setItem(ItemStack.EMPTY);
                        }
                    }
                }
                else if (recipe.getInput().size() == 4)
                {
                    rowPane.findPaneOfTypeByID(String.format(RESOURCE, 1), ItemIcon.class).setItem(recipe.getInput().get(0).getItemStack());
                    rowPane.findPaneOfTypeByID(String.format(RESOURCE, 2), ItemIcon.class).setItem(recipe.getInput().get(1).getItemStack());
                    rowPane.findPaneOfTypeByID(String.format(RESOURCE, 3), ItemIcon.class).setItem(ItemStack.EMPTY);
                    rowPane.findPaneOfTypeByID(String.format(RESOURCE, 4), ItemIcon.class).setItem(recipe.getInput().get(2).getItemStack());
                    rowPane.findPaneOfTypeByID(String.format(RESOURCE, 5), ItemIcon.class).setItem(recipe.getInput().get(3).getItemStack());
                    for (int i = 6; i < 9; i++)
                    {
                        rowPane.findPaneOfTypeByID(String.format(RESOURCE, i + 1), ItemIcon.class).setItem(ItemStack.EMPTY);
                    }
                }
                else
                {
                    for (int i = 0; i < 9; i++)
                    {
                        rowPane.findPaneOfTypeByID(String.format(RESOURCE, i + 1), ItemIcon.class).setItem(recipe.getInput().get(i).getItemStack());
                    }
                }
            }
        });
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        updateRecipes();
        if (!Screen.hasShiftDown())
        {
            lifeCount++;
        }
        recipeStatus.setText(LanguageHandler.format(TranslationConstants.RECIPE_STATUS,building.getRecipes().size(), building.getMaxRecipes()));
        window.findPaneOfTypeByID(RECIPE_LIST, ScrollingList.class).refreshElementPanes();
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        final int row = recipeList.getListElementIndexByPane(button);
        if (button.getID().equals(BUTTON_REMOVE) && building.isRecipeAlterationAllowed())
        {
            final IRecipeStorage data = recipes.get(row);
            building.removeRecipe(row);
            Network.getNetwork().sendToServer(new AddRemoveRecipeMessage(building, true, data));
        }
        else if (button.getID().equals(BUTTON_FORWARD))
        {
            building.switchIndex(row, row - 1);
            Network.getNetwork().sendToServer(new ChangeRecipePriorityMessage(building, row, true));
        }
        else if (button.getID().equals(BUTTON_BACKWARD))
        {
            building.switchIndex(row + 1, row);
            Network.getNetwork().sendToServer(new ChangeRecipePriorityMessage(building, row, false));
        }
        else if (button.getID().equals(BUTTON_CANCEL))
        {
            building.openGui(false);
        }
    }
}
