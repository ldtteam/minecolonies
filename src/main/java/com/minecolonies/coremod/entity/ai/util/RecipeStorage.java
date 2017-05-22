package com.minecolonies.coremod.entity.ai.util;

import net.minecraft.item.ItemStack;
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

    public boolean canFullFillRecipe()
}
