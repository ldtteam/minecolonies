package com.minecolonies.core.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.OptionalPredicate;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.modules.AbstractCraftingBuildingModule;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
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
                    for (final TagKey<Item> tag : stack.getTags().toList())
                    {
                        if (tag.location().getPath().contains("wood"))
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

    public static class DOCraftingModule extends AbstractCraftingBuildingModule.Domum
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

        /**
         * See {@link ICraftingBuildingModule#getIngredientValidator}.
         * @return the validator
         */
        public @NotNull static OptionalPredicate<ItemStack> getStaticIngredientValidator()
        {
            return CraftingUtils.getIngredientValidatorBasedOnTags(CRAFTING_SAWMILL, true)
                    .combine(stack -> Optional.of(stack.is(ItemTags.PLANKS) || stack.is(ItemTags.LOGS)));
        }

        @Override
        public @NotNull OptionalPredicate<ItemStack> getIngredientValidator()
        {
            return getStaticIngredientValidator();
        }
    }
}
