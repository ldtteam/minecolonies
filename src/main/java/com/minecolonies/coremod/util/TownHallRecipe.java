package com.minecolonies.coremod.util;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.Log;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.stats.StatList;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * To check if a townhall can be crafted.
 */
public class TownHallRecipe extends ShapedRecipes
{
    /**
     * List of crafting ingredients.
     */
    public static NonNullList<Ingredient> ingredients = NonNullList.create();

    static
    {
        ingredients.add(0, Ingredient.fromItem(Item.getItemFromBlock(Blocks.PLANKS)));
        ingredients.add(1, Ingredient.fromItem(com.ldtteam.structurize.items.ModItems.buildTool));
        ingredients.add(2, Ingredient.fromItem(Item.getItemFromBlock(Blocks.PLANKS)));
        ingredients.add(3, Ingredient.fromItem(Item.getItemFromBlock(Blocks.PLANKS)));
        ingredients.add(4, Ingredient.fromItem(com.ldtteam.structurize.items.ModItems.buildTool));
        ingredients.add(5, Ingredient.fromItem(Item.getItemFromBlock(Blocks.PLANKS)));
        ingredients.add(6, Ingredient.fromItem(Item.getItemFromBlock(Blocks.PLANKS)));
        ingredients.add(7, Ingredient.fromItem(Item.getItemFromBlock(Blocks.PLANKS)));
        ingredients.add(8, Ingredient.fromItem(Item.getItemFromBlock(Blocks.PLANKS)));

    }

    /**
     * Creates the townHall reicpe.
     */
    public TownHallRecipe()
    {
        super(Constants.MOD_ID, 3, 3, ingredients, new ItemStack(ModBlocks.blockHutTownHall));
        this.setRegistryName(Constants.MOD_ID + ":townhall.recipe");
    }

    @Override
    public boolean matches(@NotNull final InventoryCrafting inventoryCrafting, @NotNull final World world)
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
                    final PlayerEntity player = (EntityPlayer) playerField.get().get(inventoryCrafting.eventHandler);
                    if (player instanceof EntityPlayerMP)
                    {
                        return ((EntityPlayerMP) player).getStatFile().readStat(Objects.requireNonNull(StatList.getObjectUseStats(ModItems.supplyChest))) > 0
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
    private boolean hasSufficientResources(final InventoryCrafting inv)
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

            for (final int oreId : OreDictionary.getOreIDs(stack))
            {
                if (OreDictionary.getOreName(oreId).contains("plankWood"))
                {
                    plankCount++;
                    break;
                }
            }
        }
        return hasBuildToolCount == 2 && plankCount == 7;
    }

    @NotNull
    @Override
    public ItemStack getCraftingResult(@NotNull final InventoryCrafting inventoryCrafting)
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
