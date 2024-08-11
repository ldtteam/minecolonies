package com.minecolonies.core.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.OptionalPredicate;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.modules.AbstractCraftingBuildingModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;
import static com.minecolonies.api.util.constant.TagConstants.CRAFTING_FLETCHER;

/**
 * Class of the fletcher building.
 */
public class BuildingFletcher extends AbstractBuilding
{
    /**
     * Description string of the building.
     */
    protected static final String FLETCHER = "fletcher";

    /**
     * Instantiates a new fletcher building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingFletcher(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return FLETCHER;
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
            return CraftingUtils.getIngredientValidatorBasedOnTags(CRAFTING_FLETCHER)
                    .combine(super.getIngredientValidator());
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe)) return false;

            final Optional<Boolean> isRecipeAllowed = CraftingUtils.isRecipeCompatibleBasedOnTags(recipe, CRAFTING_FLETCHER);
            if (isRecipeAllowed.isPresent()) return isRecipeAllowed.get();

            final Item output = recipe.getPrimaryOutput().getItem();
            return output instanceof ArrowItem || (output instanceof ArmorItem armorItem && recipe.getPrimaryOutput().has(DataComponents.DYED_COLOR) && armorItem.getMaterial() == ArmorMaterials.LEATHER);
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
            return CraftingUtils.getIngredientValidatorBasedOnTags(CRAFTING_FLETCHER, true);
        }

        @Override
        public @NotNull OptionalPredicate<ItemStack> getIngredientValidator()
        {
            return getStaticIngredientValidator();
        }
    }
}
