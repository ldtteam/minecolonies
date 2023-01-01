package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IFieldView;
import com.minecolonies.api.colony.buildings.workerbuildings.FieldStructureType;
import com.minecolonies.api.colony.buildings.workerbuildings.IField;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.OptionalPredicate;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.coremod.colony.buildings.modules.FieldModule;
import com.minecolonies.coremod.colony.buildings.moduleviews.FieldModuleView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.fields.PlantationField;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.PlantationModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.PlantationModuleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

import static com.minecolonies.api.research.util.ResearchConstants.PLANTATION_LARGE;
import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;
import static com.minecolonies.api.util.constant.TagConstants.CRAFTING_PLANTATION;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;
import static com.minecolonies.api.util.constant.translation.GuiTranslationConstants.FIELD_LIST_PLANTATION_RESEARCH_REQUIRED;

/**
 * Class of the plantation building. Worker will grow sugarcane/bamboo/cactus + craft paper and books.
 */
public class BuildingPlantation extends AbstractBuilding
{
    /**
     * Description string of the building.
     */
    private static final String PLANTATION = "plantation";

    /**
     * Instantiates a new plantation building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingPlantation(final IColony c, final BlockPos l)
    {
        super(c, l);
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.AXE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return PLANTATION;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
    }

    @Override
    public BlockPos getPosition()
    {
        return getID();
    }

    /**
     * Field module implementation for the plantation.
     */
    public static class PlantationFieldModule extends FieldModule
    {
        @Override
        protected Collection<IField> getFields(final IColony colony)
        {
            return colony.getBuildingManager().getFields(FieldStructureType.PLANTATION_FIELDS);
        }

        @Override
        protected int getMaxFieldCount()
        {
            boolean hasDoubleTrouble = building.getColony().getResearchManager().getResearchEffects().getEffectStrength(PLANTATION_LARGE) > 0;
            int allowedPlants = switch (building.getBuildingLevel())
                                  {
                                      case 1, 2 -> 1;
                                      case 3, 4 -> 2;
                                      case 5 -> 3;
                                      default -> throw new IllegalStateException("Unexpected value: " + building.getBuildingLevel());
                                  };

            return hasDoubleTrouble ? allowedPlants + 1 : allowedPlants;
        }

        @Override
        protected int getMaxConcurrentPlants()
        {
            return getMaxFieldCount();
        }

        @Override
        protected @Nullable IField getFreeField(final IColony colony)
        {
            return colony.getBuildingManager().getFreeField(FieldStructureType.PLANTATION_FIELDS);
        }

        @Override
        public boolean canAddField(IField field)
        {
            if (super.canAddField(field) && field instanceof PlantationField plantationField)
            {
                final PlantationModule module = PlantationModuleRegistry.getPlantationModule(plantationField.getPlantationFieldType());
                if (module != null && module.getRequiredResearchEffect() != null)
                {
                    return building.getColony().getResearchManager().getResearchEffects().getEffectStrength(module.getRequiredResearchEffect()) > 0;
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Field module view implementation for the plantation.
     */
    public static class PlantationFieldModuleView extends FieldModuleView
    {
        @Override
        public boolean canAddField(final IFieldView field)
        {
            return super.canAddField(field) && hasRequiredResearchForField(field);
        }

        @Override
        public @Nullable BaseComponent getFieldWarningTooltip(final IFieldView field)
        {
            BaseComponent result = super.getFieldWarningTooltip(field);
            if (result != null)
            {
                return result;
            }

            if (!hasRequiredResearchForField(field))
            {
                return new TranslatableComponent(FIELD_LIST_PLANTATION_RESEARCH_REQUIRED);
            }
            return null;
        }

        /**
         * Checks if the passed field has the research required.
         *
         * @param field the field in question.
         * @return true if the research is handled.
         */
        private boolean hasRequiredResearchForField(final IFieldView field)
        {
            if (field instanceof PlantationField.View plantationField)
            {
                final PlantationModule module = PlantationModuleRegistry.getPlantationModule(plantationField.getPlantationFieldType());
                if (module != null && module.getRequiredResearchEffect() != null)
                {
                    return getColony().getResearchManager().getResearchEffects().getEffectStrength(module.getRequiredResearchEffect()) > 0;
                }
                return true;
            }
            return false;
        }
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
            return CraftingUtils.getIngredientValidatorBasedOnTags(CRAFTING_PLANTATION).combine(super.getIngredientValidator());
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe))
            {
                return false;
            }
            final Optional<Boolean> isRecipeAllowed = CraftingUtils.isRecipeCompatibleBasedOnTags(recipe, CRAFTING_PLANTATION);
            return isRecipeAllowed.orElse(false);
        }
    }
}
