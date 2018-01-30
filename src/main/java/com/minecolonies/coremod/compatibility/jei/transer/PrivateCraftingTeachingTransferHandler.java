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
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

import javax.annotation.Nullable;
import java.util.*;

public class PrivateCraftingTeachingTransferHandler implements IRecipeTransferHandler<CraftingGUIBuilding>
{
    private final IRecipeTransferHandlerHelper handlerHelper;

    public PrivateCraftingTeachingTransferHandler(final IRecipeTransferHandlerHelper handlerHelper)
    {
        this.handlerHelper = handlerHelper;
    }

    @Override
    public Class<CraftingGUIBuilding> getContainerClass()
    {
        return CraftingGUIBuilding.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(
            final CraftingGUIBuilding craftingGUIBuilding,
            final IRecipeLayout recipeLayout,
            final EntityPlayer entityPlayer,
            final boolean b,
            final boolean b1)
    {
        final IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();

        // indexes that do not fit into the player crafting grid
        final Set<Integer> badIndexes = ImmutableSet.of(2, 5, 6, 7, 8);

        // compact the crafting grid into a 2x2 area
        final Map<Integer, ItemStack> guiIngredients = new HashMap<>();
        guiIngredients.put(0, ItemStackUtils.EMPTY);
        guiIngredients.put(1, ItemStackUtils.EMPTY);
        guiIngredients.put(3, ItemStackUtils.EMPTY);
        guiIngredients.put(4, ItemStackUtils.EMPTY);

        int inputIndex = 0;
        for (final IGuiIngredient<ItemStack> ingredient : itemStackGroup.getGuiIngredients().values())
        {
            if (ingredient.isInput())
            {
                if (!ingredient.getAllIngredients().isEmpty())
                {
                    if (badIndexes.contains(inputIndex))
                    {
                        final String tooltipMessage = I18n.translateToLocal("jei.tooltip.error.recipe.transfer.too.large.player.inventory");
                        return handlerHelper.createUserErrorForSlots(tooltipMessage, badIndexes);
                    }
                    guiIngredients.put(inputIndex, ingredient.getDisplayedIngredient());
                }
                inputIndex++;
            }
        }

        final InventoryCrafting craftMatrix = new InventoryCrafting(new Container()
        {
            @Override
            public boolean canInteractWith(final EntityPlayer entityPlayer)
            {
                return false;
            }
        }, 2, 2);

        craftMatrix.setInventorySlotContents(0, guiIngredients.get(0));
        craftMatrix.setInventorySlotContents(1, guiIngredients.get(1));
        craftMatrix.setInventorySlotContents(2, guiIngredients.get(3));
        craftMatrix.setInventorySlotContents(3, guiIngredients.get(4));

        if (b1)
        {
            final TransferRecipeCrafingTeachingMessage message = new TransferRecipeCrafingTeachingMessage(guiIngredients);
            MineColonies.getNetwork().sendToServer(message);
        }

        return null;
    }
}
