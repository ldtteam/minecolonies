package com.minecolonies.coremod.entity.ai.util;

import com.minecolonies.coremod.util.InventoryUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import scala.actors.threadpool.Arrays;

import java.util.ArrayList;
import java.util.List;

/**
 * Class used to represent a recipe in minecolonies.
 */
public class RecipeStorage
{
    /**
     * Input required for the recipe.
     */
    private final List<ItemStack> input;

    /**
     * Output generated for the recipe.
     */
    private final List<ItemStack> output;

    /**
     * Grid size required for the recipe.
     */
    private final int gridSize;

    /**
     * Create an instance of the recipe storage.
     * @param input the list of input items (required for the recipe).
     * @param output the list of output items (produced by the recipe).
     * @param gridSize the required grid size to make it.
     */
    public RecipeStorage(final List<ItemStack> input, final int gridSize, final ItemStack...output)
    {
        this.input = new ArrayList<>(input);
        this.output = new ArrayList<>(Arrays.asList(output));
        this.gridSize = gridSize;
    }

    /**
     * Get the list of input items.
     * @return the copy of the list
     */
    public List<ItemStack> getInput()
    {
        return new ArrayList<>(input);
    }

    /**
     * Get the list of output items.
     * @return the copy of the list.
     */
    public List<ItemStack> getOutput()
    {
        return new ArrayList<>(output);
    }

    /**
     * Get the grid size.
     * @return the integer representing it. (2x2 = 4, 3x3 = 9, etc)
     */
    public int getGridSize()
    {
        return gridSize;
    }

    /**
     * Method to check if with the help of inventories this recipe can be fullfilled.
     * @param inventories the inventories to check.
     * @return true if possible, else false.
     */
    public boolean canFullFillRecipe(@NotNull final IItemHandler...inventories)
    {
        for(final ItemStack stack: input)
        {
            int amountNeeded = stack.stackSize;
            boolean hasStack = false;
            for(final IItemHandler handler: inventories)
            {
                hasStack = InventoryUtils.hasItemInItemHandler(handler, itemStack -> !InventoryUtils.isItemStackEmpty(itemStack) && itemStack.isItemEqual(stack));

                if(hasStack)
                {
                    final int count = InventoryUtils.getItemCountInItemHandler(handler, itemStack -> !InventoryUtils.isItemStackEmpty(itemStack) && itemStack.isItemEqual(stack));
                    if(count > amountNeeded)
                    {
                        break;
                    }
                    hasStack = false;
                    amountNeeded-= count;
                }
            }

            if(!hasStack)
            {
                return false;
            }
        }
        return true;
    }
}
