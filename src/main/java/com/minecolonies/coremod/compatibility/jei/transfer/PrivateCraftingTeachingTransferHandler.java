package com.minecolonies.coremod.compatibility.jei.transfer;

import com.google.common.collect.ImmutableSet;
import com.minecolonies.api.inventory.container.ContainerCrafting;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.messages.server.TransferRecipeCraftingTeachingMessage;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * JEI recipe transfer handler for teaching crafting recipes
 */
public class PrivateCraftingTeachingTransferHandler implements IRecipeTransferHandler<ContainerCrafting>
{
   private final IRecipeTransferHandlerHelper handlerHelper;

    public PrivateCraftingTeachingTransferHandler(@NotNull final IRecipeTransferHandlerHelper handlerHelper)
    {
        this.handlerHelper = handlerHelper;
    }

    @NotNull
    @Override
    public Class<ContainerCrafting> getContainerClass()
    {
        return ContainerCrafting.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(
            @NotNull final ContainerCrafting craftingGUIBuilding,
            @NotNull final Object recipe,
            @NotNull final IRecipeLayout recipeLayout,
            @NotNull final PlayerEntity player,
            final boolean maxTransfer,
            final boolean doTransfer)
    {
        final IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();

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
        for (final IGuiIngredient<ItemStack> ingredient : itemStackGroup.getGuiIngredients().values())
        {
            if (ingredient.isInput())
            {
                if (!ingredient.getAllIngredients().isEmpty())
                {
                    if (badIndexes.contains(inputIndex))
                    {
                        final ITextComponent tooltipMessage = new TranslationTextComponent("jei.tooltip.error.recipe.transfer.too.large.player.inventory");
                        return handlerHelper.createUserErrorForSlots(tooltipMessage, badIndexes);
                    }
                    guiIngredients.put(inputIndex, ingredient.getDisplayedIngredient());
                }
                inputIndex++;
            }
        }

        if (doTransfer)
        {
            final CraftingInventory craftMatrix = craftingGUIBuilding.getInv();
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
            Network.getNetwork().sendToServer(message);
        }

        return null;
    }
}
