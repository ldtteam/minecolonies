package com.minecolonies.core.compatibility.jei.transfer;

import com.google.common.collect.ImmutableSet;
import com.minecolonies.api.inventory.container.ContainerCrafting;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.core.network.messages.server.TransferRecipeCraftingTeachingMessage;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

/**
 * JEI recipe transfer handler for teaching crafting recipes
 */
public class PrivateCraftingTeachingTransferHandler implements IRecipeTransferHandler<ContainerCrafting, CraftingRecipe>
{
    private final IRecipeTransferHandlerHelper handlerHelper;

    public PrivateCraftingTeachingTransferHandler(@NotNull final IRecipeTransferHandlerHelper handlerHelper)
    {
        this.handlerHelper = handlerHelper;
    }

    @NotNull
    @Override
    public Optional<MenuType<ContainerCrafting>> getMenuType()
    {
        return Optional.empty();
    }

    @NotNull
    @Override
    public Class<ContainerCrafting> getContainerClass()
    {
        return ContainerCrafting.class;
    }

    @NotNull
    @Override
    public RecipeType<CraftingRecipe> getRecipeType()
    {
        return RecipeTypes.CRAFTING;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(
            @NotNull final ContainerCrafting craftingGUIBuilding,
            @NotNull final CraftingRecipe recipe,
            @NotNull final IRecipeSlotsView recipeSlots,
            @NotNull final Player player,
            final boolean maxTransfer,
            final boolean doTransfer)
    {
        // compact the crafting grid into a 2x2 area
        final Map<Integer, ItemStack> guiIngredients = new HashMap<>();
        guiIngredients.put(0, ItemStackUtils.EMPTY);
        guiIngredients.put(1, ItemStackUtils.EMPTY);
        guiIngredients.put(3, ItemStackUtils.EMPTY);
        guiIngredients.put(4, ItemStackUtils.EMPTY);

        // indexes that do not fit into the player crafting grid
        final Set<Integer> badIndexes;
        if (craftingGUIBuilding.isComplete())
        {
            guiIngredients.put(2, ItemStackUtils.EMPTY);
            guiIngredients.put(5, ItemStackUtils.EMPTY);
            guiIngredients.put(6, ItemStackUtils.EMPTY);
            guiIngredients.put(7, ItemStackUtils.EMPTY);
            guiIngredients.put(8, ItemStackUtils.EMPTY);
            badIndexes = ImmutableSet.of();
        }
        else
        {
            badIndexes = ImmutableSet.of(2, 5, 6, 7, 8);
        }

        int inputIndex = 0;
        final List<IRecipeSlotView> slots = recipeSlots.getSlotViews(RecipeIngredientRole.INPUT);
        for (final IRecipeSlotView slot : slots)
        {
            if (slot.getAllIngredients().findAny().isPresent())
            {
                if (badIndexes.contains(inputIndex))
                {
                    final Component tooltipMessage = Component.translatable("jei.tooltip.error.recipe.transfer.too.large.player.getInventory()");
                    final List<IRecipeSlotView> badSlots = badIndexes.stream().map(index -> slots.get(index)).toList();
                    return handlerHelper.createUserErrorForMissingSlots(tooltipMessage, badSlots);
                }
                guiIngredients.put(inputIndex, slot.getDisplayedIngredient(VanillaTypes.ITEM_STACK).orElse(ItemStack.EMPTY));
            }
            inputIndex++;
        }

        if (doTransfer)
        {
            final CraftingContainer craftMatrix = craftingGUIBuilding.getInv();
            if (craftingGUIBuilding.isComplete())
            {
                craftMatrix.setItem(0, guiIngredients.get(0));
                craftMatrix.setItem(1, guiIngredients.get(1));
                craftMatrix.setItem(2, guiIngredients.get(2));
                craftMatrix.setItem(3, guiIngredients.get(3));
                craftMatrix.setItem(4, guiIngredients.get(4));
                craftMatrix.setItem(5, guiIngredients.get(5));
                craftMatrix.setItem(6, guiIngredients.get(6));
                craftMatrix.setItem(7, guiIngredients.get(7));
                craftMatrix.setItem(8, guiIngredients.get(8));
            }
            else
            {
                craftMatrix.setItem(0, guiIngredients.get(0));
                craftMatrix.setItem(1, guiIngredients.get(1));
                craftMatrix.setItem(2, guiIngredients.get(3));
                craftMatrix.setItem(3, guiIngredients.get(4));
            }

            final TransferRecipeCraftingTeachingMessage message = new TransferRecipeCraftingTeachingMessage(guiIngredients, craftingGUIBuilding.isComplete());
            message.sendToServer();
        }

        return null;
    }
}
