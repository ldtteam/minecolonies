package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.OptionalPredicate;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;
import static com.minecolonies.api.util.constant.TagConstants.CRAFTING_BLACKSMITH;

/**
 * Creates a new building for the blacksmith.
 */
public class BuildingBlacksmith extends AbstractBuilding
{
    /**
     * Description of the job executed in the hut.
     */
    private static final String BLACKSMITH = "blacksmith";

    /**
     * Instantiates the building.
     *
     * @param c the colony.
     * @param l the location.
     */
    public BuildingBlacksmith(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return BLACKSMITH;
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
            return CraftingUtils.getIngredientValidatorBasedOnTags(CRAFTING_BLACKSMITH)
                    .combine(super.getIngredientValidator());
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe)) return false;

            if (recipe.matchesInput(OptionalPredicate.failIf(input -> input.is(Items.LEATHER)))
                    .equals(Optional.of(false)))
            {
                // explicitly disallow anything using leather; that's the fletcher's responsibility
                return false;
            }
            if (recipe.matchesOutput(OptionalPredicate.passIf(output ->
                                    ItemStackUtils.isTool(output, ToolType.AXE) ||
                                    ItemStackUtils.isTool(output, ToolType.PICKAXE) ||
                                    ItemStackUtils.isTool(output, ToolType.SHOVEL) ||
                                    ItemStackUtils.isTool(output, ToolType.HOE) ||
                                    ItemStackUtils.isTool(output, ToolType.SHEARS) ||
                                    ItemStackUtils.isTool(output, ToolType.SWORD) ||
                                    ItemStackUtils.isTool(output, ToolType.SHIELD) ||
                                    ItemStackUtils.isTool(output, ToolType.HELMET) ||
                                    ItemStackUtils.isTool(output, ToolType.CHESTPLATE) ||
                                    ItemStackUtils.isTool(output, ToolType.LEGGINGS) ||
                                    ItemStackUtils.isTool(output, ToolType.BOOTS) ||
                                    // deliberately excluding FISHINGROD and FLINT_N_STEEL
                                    Compatibility.isTinkersWeapon(output)))
                    .equals(Optional.of(true)))
            {
                // allow any other tool/armor even if it uses an excluded ingredient
                return true;
            }

            // otherwise obey the usual tags for other items
            return CraftingUtils.isRecipeCompatibleBasedOnTags(recipe, CRAFTING_BLACKSMITH).orElse(false);
        }
    }
}
