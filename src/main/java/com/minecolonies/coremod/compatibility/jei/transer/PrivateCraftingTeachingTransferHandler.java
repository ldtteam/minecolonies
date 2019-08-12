package com.minecolonies.coremod.compatibility.jei.transer;

import com.google.common.collect.ImmutableSet;
import com.minecolonies.api.util.ItemStackUtils;
import com.ldtteam.structurize.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.Network;
import com.minecolonies.api.inventory.container.ContainerCrafting;
import com.minecolonies.coremod.network.messages.TransferRecipeCrafingTeachingMessage;
import io.netty.buffer.Unpooled;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeBook;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.GameRules;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class PrivateCraftingTeachingTransferHandler implements IRecipeTransferHandler<ContainerCrafting>
{
    private final IRecipeTransferHandlerHelper handlerHelper;

    public PrivateCraftingTeachingTransferHandler(final IRecipeTransferHandlerHelper handlerHelper)
    {
        this.handlerHelper = handlerHelper;
    }

    @Override
    public Class<ContainerCrafting> getContainerClass()
    {
        return ContainerCrafting.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(
            final ContainerCrafting craftingGUIBuilding,
            final IRecipeLayout recipeLayout,
            final PlayerEntity player,
            final boolean b,
            final boolean b1)
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
        if(craftingGUIBuilding.isComplete())
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
                        final String tooltipMessage = LanguageHandler.format("jei.tooltip.error.recipe.transfer.too.large.player.inventory");
                        return handlerHelper.createUserErrorForSlots(tooltipMessage, badIndexes);
                    }
                    guiIngredients.put(inputIndex, ingredient.getDisplayedIngredient());
                }
                inputIndex++;
            }
        }
        final PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        buffer.writeBoolean(craftingGUIBuilding.isComplete());
        final CraftingInventory craftMatrix = craftingGUIBuilding.getInv();

        if(craftingGUIBuilding.isComplete())
        {
            craftMatrix.setInventorySlotContents(0, guiIngredients.get(0));
            craftMatrix.setInventorySlotContents(1, guiIngredients.get(1));
            craftMatrix.setInventorySlotContents(2, guiIngredients.get(2));
            craftMatrix.setInventorySlotContents(3, guiIngredients.get(3));
            craftMatrix.setInventorySlotContents(4, guiIngredients.get(4));
            craftMatrix.setInventorySlotContents(5, guiIngredients.get(5));
            craftMatrix.setInventorySlotContents(6, guiIngredients.get(6));
            craftMatrix.setInventorySlotContents(7, guiIngredients.get(7));
            craftMatrix.setInventorySlotContents(8, guiIngredients.get(8));
        }
        else
        {
            craftMatrix.setInventorySlotContents(0, guiIngredients.get(0));
            craftMatrix.setInventorySlotContents(1, guiIngredients.get(1));
            craftMatrix.setInventorySlotContents(2, guiIngredients.get(3));
            craftMatrix.setInventorySlotContents(3, guiIngredients.get(4));

        }

        final Optional<ICraftingRecipe> recipe = player.world.getServer().getRecipeManager().getRecipe(IRecipeType.CRAFTING, craftMatrix, craftingGUIBuilding.getWorldObj());
        if (!recipe.isPresent())
        {
            return handlerHelper.createInternalError();
        }

        final RecipeBook book = MineColonies.proxy.getRecipeBookFromPlayer(player);
        if (craftingGUIBuilding.getWorldObj().getGameRules().getBoolean(GameRules.DO_LIMITED_CRAFTING) && !craftingGUIBuilding.getPlayer().isCreative()  && !book.isUnlocked(recipe.get()))
        {
            final String tooltipMessage = LanguageHandler.format(TranslationConstants.COM_MINECOLONIES_COREMOD_COMPAT_JEI_CRAFTIN_TEACHING_UNKNOWN_RECIPE);
            return handlerHelper.createUserErrorWithTooltip(tooltipMessage);
        }

        if (b1)
        {
            final TransferRecipeCrafingTeachingMessage message = new TransferRecipeCrafingTeachingMessage(guiIngredients, craftingGUIBuilding.isComplete());
            Network.getNetwork().sendToServer(message);
        }

        return null;
    }
}
