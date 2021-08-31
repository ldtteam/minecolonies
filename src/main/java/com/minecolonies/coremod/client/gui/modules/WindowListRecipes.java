package com.minecolonies.coremod.client.gui.modules;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.AbstractModuleWindow;
import com.minecolonies.coremod.colony.buildings.moduleviews.CraftingModuleView;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.network.messages.server.colony.building.OpenCraftingGUIMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.worker.AddRemoveRecipeMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.worker.ChangeRecipePriorityMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * BOWindow for the hiring or firing of a worker.
 */
public class WindowListRecipes extends AbstractModuleWindow
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
     * The output item icon.
     */
    private static final String OUTPUT_ICON = "output";

    /**
     * The item icon of the resource.
     */
    private static final String RESOURCE = "res%d";

    /**
     * The view of the current module.
     */
    private final CraftingModuleView module;

    /**
     * List of recipes which can be assigned.
     */
    private final ScrollingList recipeList;

    /**
     * Button to access the crafting grid.
     */
    private static final String BUTTON_CRAFTING = "crafting";

    /**
     * The recipe status.
     */
    private final Text recipeStatus;

    /**
     * Life count.
     */
    private int lifeCount = 0;

    /**
     * The constructor of the window.
     * @param view the building view.
     * @param name the layout file.
     */
    public WindowListRecipes(final IBuildingView view, final String name, final CraftingModuleView module)
    {
        super(view, name);
        this.module = module;
        recipeList = findPaneOfTypeByID(RECIPE_LIST, ScrollingList.class);
        recipeStatus = findPaneOfTypeByID(RECIPE_STATUS, Text.class);

        findPaneOfTypeByID(BUTTON_CRAFTING, ButtonImage.class).setVisible(module.isRecipeAlterationAllowed());
        findPaneOfTypeByID("recipestatus", Text.class).setVisible(module.isRecipeAlterationAllowed());

        super.registerButton(BUTTON_CRAFTING, this::craftingClicked);
        super.registerButton(BUTTON_REMOVE, this::removeClicked);
        super.registerButton(BUTTON_FORWARD, this::forwardClicked);
        super.registerButton(BUTTON_BACKWARD, this::backwardClicked);
    }

    /**
     * Backwards clicked in the button.
     * @param button the clicked button.
     */
    private void backwardClicked(final Button button)
    {
        final int row = recipeList.getListElementIndexByPane(button);
        module.switchOrder(row, row + 1);
        Network.getNetwork().sendToServer(new ChangeRecipePriorityMessage(buildingView, row, false, module.getId()));
        recipeList.refreshElementPanes();
    }

    /**
     * Forward clicked.
     * @param button the clicked button.
     */
    private void forwardClicked(final Button button)
    {
        final int row = recipeList.getListElementIndexByPane(button);
        module.switchOrder(row, row - 1);
        Network.getNetwork().sendToServer(new ChangeRecipePriorityMessage(buildingView, row, true, module.getId()));
        recipeList.refreshElementPanes();
    }

    /**
     * On remove recipe clicked.
     * @param button the clicked button.
     */
    private void removeClicked(final Button button)
    {
        final int row = recipeList.getListElementIndexByPane(button);
        final IRecipeStorage data = module.getRecipes().get(row);
        module.removeRecipe(row);
        recipeList.refreshElementPanes();
        Network.getNetwork().sendToServer(new AddRemoveRecipeMessage(buildingView, true, data, module.getId()));
    }

    /**
     * If crafting is clicked this happens. Override if needed.
     */
    public void craftingClicked()
    {
        if (!module.isRecipeAlterationAllowed())
        {
            // This should never happen, because the button is hidden. But if someone glitches into the interface, stop him here.
            return;
        }
        final BlockPos pos = buildingView.getPosition();
        Minecraft.getInstance().player.openMenu((MenuProvider) Minecraft.getInstance().level.getBlockEntity(pos));
        Network.getNetwork().sendToServer(new OpenCraftingGUIMessage((AbstractBuildingView) buildingView, module.getId()));
    }

    @Override
    public void onOpened()
    {
        recipeList.enable();
        recipeList.show();

        //Creates a dataProvider for the homeless recipeList.
        recipeList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return module.getRecipes().size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                @NotNull final IRecipeStorage recipe = module.getRecipes().get(index);
                final ItemIcon icon = rowPane.findPaneOfTypeByID(OUTPUT_ICON, ItemIcon.class);
                List<ItemStack> displayStacks = recipe.getRecipeType().getOutputDisplayStacks();
                icon.setItem(displayStacks.get((lifeCount / LIFE_COUNT_DIVIDER) % (displayStacks.size())));

                if (!module.canLearnCraftingRecipes() && !module.canLearnFurnaceRecipes())
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
                            rowPane.findPaneOfTypeByID(String.format(RESOURCE, i + 1), ItemIcon.class).setItem(getStackWithCount(recipe.getInput().get(i)));
                        }
                        else
                        {
                            rowPane.findPaneOfTypeByID(String.format(RESOURCE, i + 1), ItemIcon.class).setItem(ItemStack.EMPTY);
                        }
                    }
                }
                else if (recipe.getInput().size() == 4)
                {
                    rowPane.findPaneOfTypeByID(String.format(RESOURCE, 1), ItemIcon.class).setItem(getStackWithCount(recipe.getInput().get(0)));
                    rowPane.findPaneOfTypeByID(String.format(RESOURCE, 2), ItemIcon.class).setItem(getStackWithCount(recipe.getInput().get(1)));
                    rowPane.findPaneOfTypeByID(String.format(RESOURCE, 3), ItemIcon.class).setItem(ItemStack.EMPTY);
                    rowPane.findPaneOfTypeByID(String.format(RESOURCE, 4), ItemIcon.class).setItem(getStackWithCount(recipe.getInput().get(2)));
                    rowPane.findPaneOfTypeByID(String.format(RESOURCE, 5), ItemIcon.class).setItem(getStackWithCount(recipe.getInput().get(3)));
                    for (int i = 6; i < 9; i++)
                    {
                        rowPane.findPaneOfTypeByID(String.format(RESOURCE, i + 1), ItemIcon.class).setItem(ItemStack.EMPTY);
                    }
                }
                else
                {
                    for (int i = 0; i < 9; i++)
                    {
                        rowPane.findPaneOfTypeByID(String.format(RESOURCE, i + 1), ItemIcon.class).setItem(getStackWithCount(recipe.getInput().get(i)));
                    }
                }
            }
        });
    }

    /**
     * Setup the stack with count.
     * @param storage the storage to get it from.
     * @return the stack with the set count.
     */
    private ItemStack getStackWithCount(final ItemStorage storage)
    {
        final ItemStack displayItem = storage.getItemStack();
        displayItem.setCount(storage.getAmount());
        return displayItem;
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        if (!Screen.hasShiftDown())
        {
            lifeCount++;
        }
        recipeStatus.setText(new TranslatableComponent(TranslationConstants.RECIPE_STATUS, module.getRecipes().size(), module.getMaxRecipes()));
        window.findPaneOfTypeByID(RECIPE_LIST, ScrollingList.class).refreshElementPanes();
    }
}
