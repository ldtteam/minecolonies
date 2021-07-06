package com.minecolonies.coremod.client.gui.modules;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ButtonImage;
import com.ldtteam.blockout.controls.ItemIcon;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.AbstractModuleWindow;
import com.minecolonies.coremod.colony.buildings.moduleviews.CraftingModuleView;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.network.messages.server.colony.building.OpenCraftingGUIMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.worker.AddRemoveRecipeMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.worker.ChangeRecipePriorityMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window for the hiring or firing of a worker.
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
        Network.getNetwork().sendToServer(new ChangeRecipePriorityMessage(buildingView, row, false, module.getId()));
    }

    /**
     * Forward clicked.
     * @param button the clicked button.
     */
    private void forwardClicked(final Button button)
    {
        final int row = recipeList.getListElementIndexByPane(button);
        Network.getNetwork().sendToServer(new ChangeRecipePriorityMessage(buildingView, row, true, module.getId()));
    }

    /**
     * On remove recipe clicked.
     * @param button the clicked button.
     */
    private void removeClicked(final Button button)
    {
        final int row = recipeList.getListElementIndexByPane(button);
        final IRecipeStorage data = module.getRecipes().get(row);
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
        Minecraft.getInstance().player.openMenu((INamedContainerProvider) Minecraft.getInstance().level.getBlockEntity(pos));
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
        if (!Screen.hasShiftDown())
        {
            lifeCount++;
        }
        recipeStatus.setText(LanguageHandler.format(TranslationConstants.RECIPE_STATUS, module.getRecipes().size(), module.getMaxRecipes()));
        window.findPaneOfTypeByID(RECIPE_LIST, ScrollingList.class).refreshElementPanes();
    }
}
