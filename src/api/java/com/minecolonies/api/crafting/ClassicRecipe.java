package com.minecolonies.api.crafting;

import net.minecraft.util.ResourceLocation;

/**
 * The Classic Recipe type
 */
public class ClassicRecipe extends AbstractRecipeType<IRecipeStorage>
{
    /**
     * Classic Recipe constructor
     */
    public ClassicRecipe(IRecipeStorage recipe)
    {
        super(recipe);
    }

    @Override
    public ResourceLocation getId()
    {
        return ModRecipeTypes.CLASSIC_ID;
    }
    
}
