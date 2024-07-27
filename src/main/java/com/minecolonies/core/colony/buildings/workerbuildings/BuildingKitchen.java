package com.minecolonies.core.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.crafting.GenericRecipe;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.OptionalPredicate;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.core.colony.buildings.modules.CraftingWorkerBuildingModule;
import com.minecolonies.core.util.FurnaceRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static com.minecolonies.api.util.ItemStackUtils.ISFOOD;
import static com.minecolonies.api.util.constant.Suppression.OVERRIDE_EQUALS;
import static com.minecolonies.api.util.constant.TagConstants.CRAFTING_COOK;

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
            return ItemStackUtils.CAN_EAT.test(output)
                    || ItemStackUtils.CAN_EAT.test(FurnaceRecipes.getInstance()
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
            return CraftingUtils.isRecipeCompatibleBasedOnTags(recipe, CRAFTING_COOK).orElse(ItemStackUtils.CAN_EAT.test(recipe.getPrimaryOutput()));
        }

        @Override
        @Nullable
        public IRecipeStorage getFirstRecipe(final Predicate<ItemStack> stackPredicate)
        {
            if (building.getModuleMatching(CraftingWorkerBuildingModule.class, m -> m.getJobEntry() == jobEntry).getAssignedCitizen().isEmpty())
            {
                return null;
            }

            //First, do the normal check against taught recipes, and return those if found
            IRecipeStorage storage = super.getFirstRecipe(stackPredicate);
            if (storage != null)
            {
                return storage;
            }


            //If we didn't have a stored recipe, see if there is a smelting recipe that is also a food output, and use it.
            storage = FurnaceRecipes.getInstance().getFirstSmeltingRecipeByResult(stackPredicate);
            if (storage != null && storage.getRecipeSource() != null && ISFOOD.test(storage.getPrimaryOutput()) && isRecipeCompatible(GenericRecipe.of(storage)))
            {
                return storage;
            }

            return null;
        }

        @Override
        public IRecipeStorage getFirstFulfillableRecipe(final Predicate<ItemStack> stackPredicate, final int count, final boolean considerReservation)
        {
            //Try to fulfill normally
            IRecipeStorage storage = super.getFirstFulfillableRecipe(stackPredicate, count, considerReservation);

            //Couldn't fulfill normally, let's try to fulfill with a temporary smelting recipe.
            if (storage == null)
            {
                storage = FurnaceRecipes.getInstance().getFirstSmeltingRecipeByResult(stackPredicate);
                if (storage != null)
                {
                    final Set<IItemHandler> handlers = new HashSet<>();
                    for (final ICitizenData workerEntity :  building.getModuleMatching(CraftingWorkerBuildingModule.class, m -> m.getJobEntry() == jobEntry).getAssignedCitizen())
                    {
                        handlers.add(workerEntity.getInventory());
                    }

                    if (!storage.canFullFillRecipe(count, Collections.emptyMap(), new ArrayList<>(handlers), building))
                    {
                        return null;
                    }
                }
            }

            return storage;
        }
    }
}
