package com.minecolonies.coremod.util;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.proxy.CommonProxy;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

/**
 * To check if a townhall can be crafted.
 */
public class TownHallRecipe extends SpecialRecipe
{
    public TownHallRecipe(final ResourceLocation resourceLocation)
    {
        super(resourceLocation);
    }

    @Override
    public boolean matches(@NotNull final CraftingInventory inventoryCrafting, @NotNull final World world)
    {
        if (inventoryCrafting.eventHandler != null)
        {
            try
            {

                final Optional<Field> playerField = Arrays.stream(inventoryCrafting.eventHandler.getClass().getDeclaredFields())
                                                      .filter(string -> string.getName().equals("player") || string.getName().equals("field_192390_i"))
                                                      .findFirst();
                if (playerField.isPresent())
                {
                    playerField.get().setAccessible(true);
                    final PlayerEntity player = (PlayerEntity) playerField.get().get(inventoryCrafting.eventHandler);
                    if (player instanceof ServerPlayerEntity)
                    {
                        return ((ServerPlayerEntity) player).getStats().getValue(Stats.ITEM_USED.get(ModItems.supplyChest)) > 0
                                 && hasSufficientResources(inventoryCrafting);
                    }
                    else
                    {
                        return hasSufficientResources(inventoryCrafting);
                    }
                }
                return false;
            }
            catch (final IllegalAccessException e)
            {
                return false;
            }
        }
        return false;
    }

    /**
     * Check if the inv has enough resources for the townhall recipe.
     *
     * @param inv the inv to check in.
     * @return true if so.
     */
    private boolean hasSufficientResources(final CraftingInventory inv)
    {
        int plankCount = 0;
        int hasBuildToolCount = 0;
        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            final ItemStack stack = inv.getStackInSlot(i).copy();
            if (ItemStackUtils.isEmpty(stack))
            {
                continue;
            }

            if (stack.getItem() == com.ldtteam.structurize.items.ModItems.buildTool)
            {
                hasBuildToolCount++;
            }

            if (stack.getItem().isIn(ItemTags.PLANKS))
            {
                plankCount++;
            }
        }
        return hasBuildToolCount == 2 && plankCount == 7;
    }

    @NotNull
    @Override
    public ItemStack getCraftingResult(@NotNull final CraftingInventory inventoryCrafting)
    {
        return getRecipeOutput();
    }

    @Override
    public boolean canFit(final int height, final int width)
    {
        return height >= 3 && width >= 3;
    }

    @NotNull
    @Override
    public ItemStack getRecipeOutput()
    {
        return new ItemStack(ModBlocks.blockHutTownHall);
    }

    @NotNull
    @Override
    public IRecipeSerializer<?> getSerializer()
    {
        return CommonProxy.SPECIAL_REC;
    }
}
