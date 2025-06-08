package com.minecolonies.core.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.AbstractCrafting;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.FoodUtils;
import com.minecolonies.api.util.OptionalPredicate;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.core.util.FurnaceRecipes;
import com.minecolonies.core.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.core.colony.jobs.AbstractJobCrafter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static com.minecolonies.api.util.constant.Suppression.OVERRIDE_EQUALS;
import static com.minecolonies.api.util.constant.TagConstants.CRAFTING_COOK;
import static com.minecolonies.core.colony.buildings.modules.BuildingModules.CHEF_WORK;

/**
 * Class of the kitchen building.
 */
@SuppressWarnings(OVERRIDE_EQUALS)
public class BuildingKitchen extends AbstractBuilding
{
    /**
     * The cook string.
     */
    private static final String KITCHEN_DESC = "kitchen";

    /**
     * Max building level of the cook.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * Instantiates a new cook building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingKitchen(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return KITCHEN_DESC;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @Override
    protected boolean keepFood()
    {
        return false;
    }

    @Override
    public boolean canEat(final ItemStack stack)
    {
        final ICitizenData citizenData = getModule(CHEF_WORK).getFirstCitizen();
        if (citizenData != null)
        {
            final IRequest<? extends IRequestable> currentTask = ((AbstractJobCrafter<?, ?>) citizenData.getJob()).getCurrentTask();
            if (currentTask == null)
            {
                return super.canEat(stack);
            }
            final IRequestable request = currentTask.getRequest();
            if (request instanceof AbstractCrafting craftingRequest)
            {
                final IRecipeStorage recipe = IColonyManager.getInstance().getRecipeManager().getRecipe(craftingRequest.getRecipeID());
                if (recipe != null)
                {
                    if (recipe.getCleanedInput().contains(new ItemStorage(stack)))
                    {
                        return false;
                    }

                    if (ItemStack.isSameItem(recipe.getPrimaryOutput(), stack))
                    {
                        return false;
                    }
                }
            }
        }
        return super.canEat(stack);
    }

    public static class CraftingModule extends AbstractCraftingBuildingModule.Crafting
    {
        /**
         * Create a new module.
         *
         * @param jobEntry the entry of the job.
         */
        public CraftingModule(final JobEntry jobEntry)
        {
            super(jobEntry);
        }

        @NotNull
        @Override
        public OptionalPredicate<ItemStack> getIngredientValidator()
        {
            return CraftingUtils.getIngredientValidatorBasedOnTags(CRAFTING_COOK)
                    .combine(super.getIngredientValidator());
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe)) return false;

            final Optional<Boolean> isRecipeAllowed = CraftingUtils.isRecipeCompatibleBasedOnTags(recipe, CRAFTING_COOK);
            if (isRecipeAllowed.isPresent()) return isRecipeAllowed.get();

            final ItemStack output = recipe.getPrimaryOutput();
            return FoodUtils.EDIBLE.test(output)
                || FoodUtils.EDIBLE.test(FurnaceRecipes.getInstance()
                    .getSmeltingResult(output));
        }
    }

    public static class SmeltingModule extends AbstractCraftingBuildingModule.Smelting
    {
        /**
         * Create a new module.
         *
         * @param jobEntry the entry of the job.
         */
        public SmeltingModule(final JobEntry jobEntry)
        {
            super(jobEntry);
        }

        @NotNull
        @Override
        public OptionalPredicate<ItemStack> getIngredientValidator()
        {
            return CraftingUtils.getIngredientValidatorBasedOnTags(CRAFTING_COOK)
                    .combine(super.getIngredientValidator());
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe)) return false;
            return CraftingUtils.isRecipeCompatibleBasedOnTags(recipe, CRAFTING_COOK).orElse(FoodUtils.EDIBLE.test(recipe.getPrimaryOutput()));
        }
    }
}
