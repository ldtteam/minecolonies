package com.minecolonies.coremod.compatibility.jei.transfer;

import com.minecolonies.api.inventory.container.ContainerCraftingFurnace;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.messages.server.TransferRecipeCraftingTeachingMessage;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
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
 * JEI recipe transfer handler for teaching furnace recipes
 */
public class PrivateSmeltingTeachingTransferHandler implements IRecipeTransferHandler<ContainerCraftingFurnace>
{
    private final IRecipeTransferHandlerHelper handlerHelper;

    public PrivateSmeltingTeachingTransferHandler(@NotNull final IRecipeTransferHandlerHelper handlerHelper)
    {
        this.handlerHelper = handlerHelper;
    }

    @NotNull
    @Override
    public Class<ContainerCraftingFurnace> getContainerClass()
    {
        return ContainerCraftingFurnace.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(
            @NotNull final ContainerCraftingFurnace craftingGUIBuilding,
            @NotNull final Object recipe,
            @NotNull final IRecipeLayout recipeLayout,
            @NotNull final PlayerEntity player,
            final boolean maxTransfer,
            final boolean doTransfer)
    {
        // we only care about the first input ingredient for furnace recipes
        final ItemStack input = recipeLayout.getItemStacks().getGuiIngredients().values().stream()
                .filter(ingredient -> ingredient.isInput() && !ingredient.getAllIngredients().isEmpty())
                .map(IGuiIngredient::getDisplayedIngredient)
                .findFirst()
                .orElse(ItemStack.EMPTY);

        if (!input.isEmpty() && doTransfer)
        {
            craftingGUIBuilding.setFurnaceInput(input);

            final Map<Integer, ItemStack> guiIngredients = new HashMap<>();
            guiIngredients.put(0, input);
            final TransferRecipeCraftingTeachingMessage message = new TransferRecipeCraftingTeachingMessage(guiIngredients, false);
            Network.getNetwork().sendToServer(message);
        }

        return null;
    }
}
