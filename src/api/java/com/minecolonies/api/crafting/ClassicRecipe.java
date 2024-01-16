package com.minecolonies.api.crafting;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * The Classic Recipe type
 */
public class ClassicRecipe extends AbstractRecipeType<IRecipeStorage>
{
    /**
     * Cache of item stacks for display
     */
    private final ArrayList<ItemStack> outputDisplayStacks = new ArrayList<>();

    /**
     * Classic Recipe constructor
     */
    public ClassicRecipe(IRecipeStorage recipe)
    {
        super(recipe);
    }

    @Override
    public List<ItemStack> getOutputDisplayStacks()
    {
        if(outputDisplayStacks.isEmpty())
        {
            if(!ItemStackUtils.isEmpty(recipe.getPrimaryOutput()) || recipe.getSecondaryOutputs().isEmpty())
            {
                outputDisplayStacks.addAll(super.getOutputDisplayStacks());
            }
            outputDisplayStacks.addAll(recipe.getSecondaryOutputs());
        }
        return ImmutableList.copyOf(outputDisplayStacks);
    }

    @Override
    public ResourceLocation getId()
    {
        return ModRecipeTypes.CLASSIC_ID;
    }
    
}
