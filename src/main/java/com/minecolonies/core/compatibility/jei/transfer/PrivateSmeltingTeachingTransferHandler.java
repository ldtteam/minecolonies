package com.minecolonies.core.compatibility.jei.transfer;

import com.minecolonies.api.inventory.container.ContainerCraftingFurnace;
import com.minecolonies.core.network.messages.server.TransferRecipeCraftingTeachingMessage;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * JEI recipe transfer handler for teaching furnace recipes
 */
public class PrivateSmeltingTeachingTransferHandler implements IRecipeTransferHandler<ContainerCraftingFurnace, SmeltingRecipe>
{
    private final IRecipeTransferHandlerHelper handlerHelper;

    public PrivateSmeltingTeachingTransferHandler(@NotNull final IRecipeTransferHandlerHelper handlerHelper)
    {
        this.handlerHelper = handlerHelper;
    }

    @NotNull
    @Override
    public Optional<MenuType<ContainerCraftingFurnace>> getMenuType()
    {
        return Optional.empty();
    }

    @NotNull
    @Override
    public RecipeType<SmeltingRecipe> getRecipeType()
    {
        return RecipeTypes.SMELTING;
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
            @NotNull final SmeltingRecipe recipe,
            @NotNull final IRecipeSlotsView recipeSlots,
            @NotNull final Player player,
            final boolean maxTransfer,
            final boolean doTransfer)
    {
        // we only care about the first input ingredient for furnace recipes
        final ItemStack input = recipeSlots.getSlotViews(RecipeIngredientRole.INPUT).stream()
                .flatMap(slot -> slot.getDisplayedIngredient(VanillaTypes.ITEM_STACK).stream())
                .findFirst()
                .orElse(ItemStack.EMPTY);

        if (!input.isEmpty() && doTransfer)
        {
            craftingGUIBuilding.setFurnaceInput(input);

            final Map<Integer, ItemStack> guiIngredients = new HashMap<>();
            guiIngredients.put(0, input);
            final TransferRecipeCraftingTeachingMessage message = new TransferRecipeCraftingTeachingMessage(guiIngredients, false);
            message.sendToServer();
        }

        return null;
    }
}
