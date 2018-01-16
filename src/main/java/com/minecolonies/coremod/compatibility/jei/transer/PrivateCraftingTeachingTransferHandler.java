package com.minecolonies.coremod.compatibility.jei.transer;

import com.google.common.collect.ImmutableSet;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.inventory.CraftingGUIBuilding;
import com.minecolonies.coremod.network.messages.TransferRecipeCrafingTeachingMessage;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.translation.I18n;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class PrivateCraftingTeachingTransferHandler implements IRecipeTransferHandler<CraftingGUIBuilding>
{
    private final IRecipeTransferHandlerHelper handlerHelper;

    public PrivateCraftingTeachingTransferHandler(final IRecipeTransferHandlerHelper handlerHelper) {this.handlerHelper = handlerHelper;}

    @Override
    public Class<CraftingGUIBuilding> getContainerClass()
    {
        return CraftingGUIBuilding.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(
      final CraftingGUIBuilding craftingGUIBuilding, final IRecipeLayout recipeLayout, final EntityPlayer entityPlayer, final boolean b, final boolean b1)
    {
        IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();

        // indexes that do not fit into the player crafting grid
        Set<Integer> badIndexes = ImmutableSet.of(2, 5, 6, 7, 8);

        // compact the crafting grid into a 2x2 area
        Map<Integer, ItemStack> guiIngredients = new HashMap<>();
        int inputIndex = 0;
        for (IGuiIngredient<ItemStack> ingredient : itemStackGroup.getGuiIngredients().values()) {
            if (ingredient.isInput()) {
                if (!ingredient.getAllIngredients().isEmpty()) {
                    if (badIndexes.contains(inputIndex)) {
                        String tooltipMessage = I18n.translateToLocal("jei.tooltip.error.recipe.transfer.too.large.player.inventory");
                        return handlerHelper.createUserErrorWithTooltip(tooltipMessage);
                    }
                    guiIngredients.put(inputIndex, ingredient.getDisplayedIngredient());
                }
                inputIndex++;
            }
        }

        if (b1) {
            final TransferRecipeCrafingTeachingMessage message = new TransferRecipeCrafingTeachingMessage(guiIngredients);
            MineColonies.getNetwork().sendToServer(message);
        }

        return null;
    }
}
