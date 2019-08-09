package com.minecolonies.coremod.util;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

/**
 * To check if a townhall can be crafted.
 */
public class TownHallRecipe extends ShapedRecipe
{
    /**
     * List of crafting ingredients.
     */
    public static NonNullList<Ingredient> ingredients = NonNullList.create();

    static
    {
        ingredients.add(0, Ingredient.fromItems(Items.OAK_PLANKS));
        ingredients.add(1, Ingredient.fromItems(com.ldtteam.structurize.items.ModItems.buildTool));
        ingredients.add(2, Ingredient.fromItems(Items.OAK_PLANKS));
        ingredients.add(3, Ingredient.fromItems(Items.OAK_PLANKS));
        ingredients.add(4, Ingredient.fromItems(com.ldtteam.structurize.items.ModItems.buildTool));
        ingredients.add(5, Ingredient.fromItems(Items.OAK_PLANKS));
        ingredients.add(6, Ingredient.fromItems(Items.OAK_PLANKS));
        ingredients.add(7, Ingredient.fromItems(Items.OAK_PLANKS));
        ingredients.add(8, Ingredient.fromItems(Items.OAK_PLANKS));

    }

    /**
     * Creates the townHall reicpe.
     */
    public TownHallRecipe()
    {
        super(new ResourceLocation(Constants.MOD_ID, "townhall.recipe"),"" , 3, 3, ingredients, new ItemStack(ModBlocks.blockHutTownHall));
    }

    @Override
    public boolean matches(@NotNull final CraftingInventory inventoryCrafting, @NotNull final World world)
    {
        if (inventoryCrafting.field_70465_c != null)
        {
            try
            {

                final Optional<Field> playerField = Arrays.stream(inventoryCrafting.field_70465_c.getClass().getDeclaredFields())
                                                      .filter(string -> string.getName().equals("player") || string.getName().equals("field_192390_i"))
                                                      .findFirst();
                if (playerField.isPresent())
                {
                    playerField.get().setAccessible(true);
                    final PlayerEntity player = (PlayerEntity) playerField.get().get(inventoryCrafting.field_70465_c);
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
        return height*width >= 9;
    }

    @NotNull
    @Override
    public ItemStack getRecipeOutput()
    {
        return new ItemStack(ModBlocks.blockHutTownHall);
    }
}
