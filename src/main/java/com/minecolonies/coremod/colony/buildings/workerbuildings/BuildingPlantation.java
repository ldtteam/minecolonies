package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IFieldView;
import com.minecolonies.api.colony.buildings.workerbuildings.fields.FieldType;
import com.minecolonies.api.colony.buildings.workerbuildings.fields.IField;
import com.minecolonies.api.colony.buildings.workerbuildings.plantation.PlantationFieldType;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.crafting.GenericRecipe;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.OptionalPredicate;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.coremod.colony.buildings.modules.FieldsModule;
import com.minecolonies.coremod.colony.buildings.moduleviews.FieldsModuleView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.fields.PlantationField;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.PlantationModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.PlantationModuleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.api.research.util.ResearchConstants.PLANTATION_LARGE;
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

    @Override
    public void onPlacement()
    {
        super.onPlacement();
        updateFields();
    }

    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);
        updateFields();
    }

    private void updateFields()
    {
        updateField(PlantationFieldType.SUGAR_CANE);
        updateField(PlantationFieldType.CACTUS);
        updateField(PlantationFieldType.BAMBOO);
    }

    /**
     * TODO: 1.20
     * Legacy code, can be removed when plantations will no longer have to support fields
     * directly from the hut building.
     */
    private void updateField(PlantationFieldType type)
    {
        final PlantationModule module = PlantationModuleRegistry.getPlantationModule(type);
        if (module != null)
        {
            final List<BlockPos> workingPositions = module.getValidWorkingPositions(colony.getWorld(), getLocationsFromTag(module.getWorkTag()));
            final PlantationField updatedField = new PlantationField(colony, getPosition(), type, module.getItem(), workingPositions);
            updatedField.setBuilding(getID());
            colony.getBuildingManager().addOrUpdateField(updatedField);
        }
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return PLANTATION;
    }

    /**
     * Field module implementation for the plantation.
     */
    public static class PlantationFieldsModule extends FieldsModule
    {
        @Override
        protected @NotNull Set<? extends IField> getFields(final IColony colony)
        {
            return colony.getBuildingManager().getFields(FieldType.PLANTATION_FIELDS);
        }

        @Override
        protected int getMaxConcurrentPlants()
        {
            return getMaxFieldCount();
        }

        @Override
        protected int getMaxFieldCount()
        {
            boolean hasDoubleTrouble = building.getColony().getResearchManager().getResearchEffects().getEffectStrength(PLANTATION_LARGE) > 0;
            int allowedPlants = switch (building.getBuildingLevel())
            {
                case 0 -> 0;
                case 1, 2 -> 1;
                case 3, 4 -> 2;
                case 5 -> 3;
                default -> throw new IllegalStateException("Unexpected value: " + building.getBuildingLevel());
            };

            return hasDoubleTrouble ? allowedPlants + 1 : allowedPlants;
        }

        @Override
        public Class<?> getExpectedFieldType()
        {
            return PlantationField.class;
        }

        @Override
        protected @NotNull List<IField> getFreeFields(final IColony colony)
        {
            return colony.getBuildingManager().getFreeFields(FieldType.PLANTATION_FIELDS);
        }

        @Override
        public boolean canAddField(IField field)
        {
            if (field instanceof PlantationField plantationField)
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
    public static class PlantationFieldsModuleView extends FieldsModuleView
    {
        @Override
        public boolean canAssignField(final IFieldView field)
        {
            return hasRequiredResearchForField(field);
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

        @Override
        public FieldType getExpectedFieldType()
        {
            return FieldType.PLANTATION_FIELDS;
        }

        @Override
        public @Nullable MutableComponent getFieldWarningTooltip(final IFieldView field)
        {
            MutableComponent result = super.getFieldWarningTooltip(field);
            if (result != null)
            {
                return result;
            }

            if (!hasRequiredResearchForField(field))
            {
                return Component.translatable(FIELD_LIST_PLANTATION_RESEARCH_REQUIRED);
            }
            return null;
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

        @Override
        public @NotNull List<IGenericRecipe> getAdditionalRecipesForDisplayPurposesOnly()
        {
            final List<IGenericRecipe> recipes = new ArrayList<>(super.getAdditionalRecipesForDisplayPurposesOnly());

            for (PlantationFieldType type : PlantationFieldType.values())
            {
                PlantationModule module = PlantationModuleRegistry.getPlantationModule(type);
                if (module != null)
                {
                    recipes.add(new GenericRecipe(null,
                      new ItemStack(module.getItem()),
                      Collections.emptyList(),
                      List.of(module.getRequiredItemsForOperation()),
                      1,
                      Blocks.AIR,
                      null,
                      module.getRequiredTool(),
                      Collections.emptyList(),
                      -1));
                }
            }

            return recipes;
        }

        @NotNull
        @Override
        public OptionalPredicate<ItemStack> getIngredientValidator()
        {
            return CraftingUtils.getIngredientValidatorBasedOnTags(CRAFTING_PLANTATION).combine(super.getIngredientValidator());
        }
    }
}
