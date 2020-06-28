package com.minecolonies.api.advancements.building_add_recipe;

import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.util.ResourceLocation;

public class BuildingAddRecipeCriterionInstance extends CriterionInstance
{
    private ItemPredicate[] outputItemPredicates;
    private int craftingSize = -1;

    public BuildingAddRecipeCriterionInstance()
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_BUILDING_ADD_RECIPE));
    }

    public BuildingAddRecipeCriterionInstance(final ItemPredicate[] outputItemPredicates)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_BUILDING_ADD_RECIPE));

        this.outputItemPredicates = outputItemPredicates;
    }

    public BuildingAddRecipeCriterionInstance(final ItemPredicate[] outputItemPredicates, final int craftingSize)
    {
        super(new ResourceLocation(Constants.MOD_ID, Constants.CRITERION_BUILDING_ADD_RECIPE));

        this.outputItemPredicates = outputItemPredicates;
        this.craftingSize = craftingSize;
    }

    public boolean test(final IRecipeStorage recipeStorage)
    {
        if (this.outputItemPredicates != null)
        {
            boolean outputMatches = false;
            for (ItemPredicate itemPredicate : outputItemPredicates)
            {
                if (itemPredicate.test(recipeStorage.getPrimaryOutput()))
                {
                    outputMatches = true;
                    break;
                }
            }

            if (this.craftingSize != -1)
            {
                return outputMatches && this.craftingSize == recipeStorage.getGridSize();
            }

            return outputMatches;
        }

        return true;
    }
}
