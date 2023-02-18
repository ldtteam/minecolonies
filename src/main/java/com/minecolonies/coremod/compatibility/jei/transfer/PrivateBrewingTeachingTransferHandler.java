package com.minecolonies.coremod.compatibility.jei.transfer;

import com.minecolonies.api.inventory.container.ContainerCraftingBrewingstand;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.messages.server.TransferRecipeCraftingTeachingMessage;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.plugins.vanilla.brewing.JeiBrewingRecipe;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * JEI recipe transfer handler for teaching brewing recipes
 */
public class PrivateBrewingTeachingTransferHandler implements IRecipeTransferHandler<ContainerCraftingBrewingstand, JeiBrewingRecipe>
{
    private final IRecipeTransferHandlerHelper handlerHelper;

    public PrivateBrewingTeachingTransferHandler(@NotNull final IRecipeTransferHandlerHelper handlerHelper)
    {
        this.handlerHelper = handlerHelper;
    }

    @NotNull
    @Override
    public Class<ContainerCraftingBrewingstand> getContainerClass()
    {
        return ContainerCraftingBrewingstand.class;
    }

    @NotNull
    @Override
    public Class<JeiBrewingRecipe> getRecipeClass()
    {
        return JeiBrewingRecipe.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(@NotNull final ContainerCraftingBrewingstand craftingGUIBuilding,
                                               @NotNull final JeiBrewingRecipe recipe,
                                               @NotNull final IRecipeSlotsView recipeSlots,
                                               @NotNull final Player player,
                                               final boolean maxTransfer,
                                               final boolean doTransfer)
    {
        final IRecipeSlotView potionSlot = recipeSlots.getSlotViews(RecipeIngredientRole.INPUT).get(0);
        final IRecipeSlotView inputSlot = recipeSlots.getSlotViews(RecipeIngredientRole.INPUT).get(3);

        final Map<Integer, ItemStack> guiIngredients = new HashMap<>();
        guiIngredients.put(0, inputSlot.getDisplayedIngredient(VanillaTypes.ITEM).orElse(ItemStack.EMPTY));
        guiIngredients.put(1, potionSlot.getDisplayedIngredient(VanillaTypes.ITEM).orElse(ItemStack.EMPTY));

        if (doTransfer)
        {
            craftingGUIBuilding.setInput(guiIngredients.get(0));
            craftingGUIBuilding.setContainer(guiIngredients.get(1));

            final TransferRecipeCraftingTeachingMessage message = new TransferRecipeCraftingTeachingMessage(guiIngredients, false);
            Network.getNetwork().sendToServer(message);
        }

        return null;
    }
}
