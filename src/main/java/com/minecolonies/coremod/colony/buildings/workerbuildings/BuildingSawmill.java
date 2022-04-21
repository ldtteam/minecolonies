package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.OptionalPredicate;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.coremod.colony.buildings.modules.AbstractDOCraftingBuildingModule;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;
import static com.minecolonies.api.util.constant.TagConstants.CRAFTING_SAWMILL;

/**
 * Class of the sawmill building.
 */
public class BuildingSawmill extends AbstractBuilding
{
    /**
     * Description string of the building.
     */
    protected static final String SAWMILL = "sawmill";

    /**
     * The min percentage something has to have out of wood to be craftable by this worker.
     */
    private static final double MIN_PERCENTAGE_TO_CRAFT = 0.75;

    /**
     * Instantiates a new sawmill building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingSawmill(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return SAWMILL;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
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
            return CraftingUtils.getIngredientValidatorBasedOnTags(CRAFTING_SAWMILL)
                    .combine(super.getIngredientValidator());
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe)) return false;

            final Optional<Boolean> isRecipeAllowed = CraftingUtils.isRecipeCompatibleBasedOnTags(recipe, CRAFTING_SAWMILL);
            if (isRecipeAllowed.isPresent()) return isRecipeAllowed.get();

            double amountOfValidBlocks = 0;
            double blocks = 0;
            for (final List<ItemStack> stacks : recipe.getInputs())
            {
                // just check the first alternative for now
                if (stacks.isEmpty()) continue;
                final ItemStack stack = stacks.get(0);
                if (!ItemStackUtils.isEmpty(stack))
                {
                    if (stack.is(ItemTags.PLANKS) || stack.is(ItemTags.LOGS))
                    {
                        amountOfValidBlocks += stack.getCount();
                        continue;
                    }
                    for (final ResourceLocation tag : stack.getItem().getTags())
                    {
                        if (tag.getPath().contains("wood"))
                        {
                            amountOfValidBlocks += stack.getCount();
                            break;
                        }
                    }
                    blocks += stack.getCount();
                }
            }

            return amountOfValidBlocks > 0 && amountOfValidBlocks / blocks > MIN_PERCENTAGE_TO_CRAFT;
        }
    }

    public static class DOCraftingModule extends AbstractDOCraftingBuildingModule
    {
        /**
         * Create a new module.
         *
         * @param jobEntry the entry of the job.
         */
        public DOCraftingModule(final JobEntry jobEntry)
        {
            super(jobEntry);
        }

        @Override
        public @NotNull OptionalPredicate<ItemStack> getIngredientValidator()
        {
            return stack -> Optional.of(ItemTags.PLANKS.contains(stack.getItem()) || ItemTags.LOGS.contains(stack.getItem()));
        }
    }
}
