package com.minecolonies.coremod.compatibility.jei.transfer;

import com.minecolonies.api.inventory.container.ContainerCraftingBrewingstand;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.messages.server.TransferRecipeCraftingTeachingMessage;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * JEI recipe transfer handler for teaching brewing recipes
 */
public class PrivateBrewingTeachingTransferHandler implements IRecipeTransferHandler<ContainerCraftingBrewingstand>
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

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(
            @NotNull final ContainerCraftingBrewingstand craftingGUIBuilding,
            @NotNull final Object recipe,
            @NotNull final IRecipeLayout recipeLayout,
            @NotNull final PlayerEntity player,
            final boolean maxTransfer,
            final boolean doTransfer)
    {
        final IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();

        final IGuiIngredient<ItemStack> container = itemStackGroup.getGuiIngredients().get(0);
        final IGuiIngredient<ItemStack> ingredient = itemStackGroup.getGuiIngredients().get(3);

        final Map<Integer, ItemStack> guiIngredients = new HashMap<>();
        guiIngredients.put(0, ingredient.getDisplayedIngredient());
        guiIngredients.put(1, container.getDisplayedIngredient());

        if (doTransfer)
        {
            craftingGUIBuilding.setInput(ingredient.getDisplayedIngredient());
            craftingGUIBuilding.setContainer(container.getDisplayedIngredient());

            final TransferRecipeCraftingTeachingMessage message = new TransferRecipeCraftingTeachingMessage(guiIngredients, false);
            Network.getNetwork().sendToServer(message);
        }

        return null;
    }
}
