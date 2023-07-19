package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.colony.fields.plantation.IPlantationModule;
import com.minecolonies.api.colony.fields.registry.FieldRegistries;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.crafting.GenericRecipe;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.OptionalPredicate;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.client.gui.modules.PlantationFieldsModuleWindow;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.coremod.colony.buildings.modules.FieldsModule;
import com.minecolonies.coremod.colony.buildings.moduleviews.FieldsModuleView;
import com.minecolonies.coremod.colony.fields.PlantationField;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.research.util.ResearchConstants.PLANTATION_LARGE;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_PLANTGROUND;
import static com.minecolonies.api.util.constant.TagConstants.CRAFTING_PLANTATION;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;
import static com.minecolonies.api.util.constant.translation.GuiTranslationConstants.FIELD_LIST_PLANTATION_RESEARCH_REQUIRED;
import static com.minecolonies.api.util.constant.translation.GuiTranslationConstants.FIELD_LIST_WARN_EXCEEDS_PLANT_COUNT;

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
     * TODO: future
     * Legacy code, can be removed when plantations will no longer have to support fields
     * directly from the hut building.
     * Whether field migration from the old system to the new system should occur.
     */
    private boolean triggerFieldMigration = false;

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
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.SHEARS, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
    }

    @Override
    public void onPlacement()
    {
        super.onPlacement();
        updateFields();
    }

    private void updateFields()
    {
        updateField(FieldRegistries.plantationSugarCaneField.get());
        updateField(FieldRegistries.plantationCactusField.get());
        updateField(FieldRegistries.plantationBambooField.get());
    }

    /**
     * TODO: future
     * Legacy code, can be removed when plantations will no longer have to support fields
     * directly from the hut building.
     */
    private void updateField(FieldRegistries.FieldEntry type)
    {
        final PlantationField plantationField = PlantationField.create(type, getPosition());
        final List<BlockPos> workingPositions =
          plantationField.getModule().getValidWorkingPositions(colony.getWorld(), getLocationsFromTag(plantationField.getModule().getWorkTag()));
        if (workingPositions.isEmpty())
        {
            colony.getBuildingManager().removeField(field -> field.equals(plantationField));
            return;
        }

        if (colony.getBuildingManager().addField(plantationField))
        {
            plantationField.setWorkingPositions(workingPositions);
        }
        else
        {
            final Optional<IField> existingField = colony.getBuildingManager().getField(field -> field.equals(plantationField));
            if (existingField.isPresent() && existingField.get() instanceof PlantationField existingPlantationField)
            {
                existingPlantationField.setWorkingPositions(workingPositions);
            }
        }
    }

    /**
     * TODO: future
     * Legacy code, can be removed when plantations will no longer have to support fields
     * directly from the hut building.
     * <p>
     * This is used for initial migration to the new plantation field system.
     * This will register the fields on colony load, only when the building still contains old NBT data.
     */
    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);
        if (compound.contains(TAG_PLANTGROUND))
        {
            triggerFieldMigration = true;
        }
    }

    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);
        updateFields();
    }

    @Override
    public Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> getRequiredItemsAndAmount()
    {
        final Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> toKeep = super.getRequiredItemsAndAmount();
        for (FieldsModule module : getModules(FieldsModule.class))
        {
            for (final IField field : module.getOwnedFields())
            {
                if (field instanceof PlantationField plantationField)
                {
                    final IPlantationModule plantationModule = plantationField.getFirstModuleOccurance(IPlantationModule.class);
                    toKeep.put(new ItemStack(plantationModule.getItem())::sameItem, new Tuple<>(plantationModule.getPlantsToRequest(), true));
                }
            }
        }
        return toKeep;
    }

    /**
     * Check if the assigned citizens are allowed to eat the following stack.
     * Additionally, if the stack is even edible in the first place, then it also checks if the fields aren't producing these items.
     * If this item is being produced here, the planter is not allowed to eat his own products in that case. (Although most items the planter produces won't be edible to begin with).
     *
     * @param stack the stack to test.
     * @return true if so.
     */
    @Override
    public boolean canEat(final ItemStack stack)
    {
        if (!super.canEat(stack))
        {
            return false;
        }

        for (FieldsModule module : getModules(FieldsModule.class))
        {
            for (final IField field : module.getOwnedFields())
            {
                if (field instanceof PlantationField plantationField)
                {
                    final IPlantationModule plantationModule = plantationField.getFirstModuleOccurance(IPlantationModule.class);
                    if (ItemStackUtils.compareItemStacksIgnoreStackSize(new ItemStack(plantationModule.getItem()), stack))
                    {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * TODO: future
     * Legacy code, can be removed when plantations will no longer have to support fields
     * directly from the hut building.
     */
    @Override
    public void setTileEntity(final AbstractTileEntityColonyBuilding te)
    {
        super.setTileEntity(te);
        if (triggerFieldMigration)
        {
            updateFields();
            triggerFieldMigration = false;
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
        public void serializeToView(final @NotNull FriendlyByteBuf buf)
        {
            super.serializeToView(buf);
            buf.writeInt(getMaxConcurrentPlants());
        }

        /**
         * Get the maximum allowed plants the plantation can work on simultaneously.
         *
         * @return the maximum amount of concurrent plants.
         */
        public int getMaxConcurrentPlants()
        {
            return (int) Math.ceil(building.getBuildingLevel() / 2D);
        }

        @Override
        protected int getMaxFieldCount()
        {
            int allowedPlants = (int) Math.ceil(building.getBuildingLevel() / 2D);
            return building.getColony().getResearchManager().getResearchEffects().getEffectStrength(PLANTATION_LARGE) > 0
                     ? allowedPlants + 1
                     : allowedPlants;
        }

        @Override
        public Class<?> getExpectedFieldType()
        {
            return PlantationField.class;
        }

        @Override
        public @NotNull List<IField> getFields()
        {
            return building.getColony().getBuildingManager().getFields(field -> field.hasModule(IPlantationModule.class));
        }

        @Override
        public boolean canAssignFieldOverride(IField field)
        {
            return getCurrentPlantsPlusField(field) <= getMaxConcurrentPlants() && hasRequiredResearchForField(field);
        }

        /**
         * Getter of the worked plants.
         *
         * @param extraField the extra field to calculate.
         * @return the amount of worked plants.
         */
        private int getCurrentPlantsPlusField(final IField extraField)
        {
            final Set<IPlantationModule> plants = getOwnedFields().stream()
                                                    .map(field -> field.getFirstModuleOccurance(IPlantationModule.class))
                                                    .collect(Collectors.toSet());
            plants.add(extraField.getFirstModuleOccurance(IPlantationModule.class));
            return plants.size();
        }

        /**
         * Checks if the passed field has the research required.
         *
         * @param field the field in question.
         * @return true if the research is handled.
         */
        private boolean hasRequiredResearchForField(final IField field)
        {
            if (field instanceof PlantationField plantationField)
            {
                final IPlantationModule plantationModule = plantationField.getFirstModuleOccurance(IPlantationModule.class);
                if (plantationModule.getRequiredResearchEffect() != null)
                {
                    return building.getColony().getResearchManager().getResearchEffects().getEffectStrength(plantationModule.getRequiredResearchEffect()) > 0;
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
        /**
         * The maximum amount of concurrent plants the planter can work on.
         */
        private int maxConcurrentPlants = 0;

        @Override
        public void deserialize(final @NotNull FriendlyByteBuf buf)
        {
            super.deserialize(buf);
            maxConcurrentPlants = buf.readInt();
        }

        @Override
        protected boolean canAssignFieldOverride(final IField field)
        {
            return getCurrentPlantsPlusField(field) <= maxConcurrentPlants && hasRequiredResearchForField(field);
        }

        @Override
        protected List<IField> getFieldsInColony()
        {
            return getColony().getFields(field -> field.hasModule(IPlantationModule.class));
        }

        @Override
        public @Nullable MutableComponent getFieldWarningTooltip(final IField field)
        {
            MutableComponent result = super.getFieldWarningTooltip(field);
            if (result != null)
            {
                return result;
            }

            if (getCurrentPlantsPlusField(field) > maxConcurrentPlants)
            {
                return Component.translatable(FIELD_LIST_WARN_EXCEEDS_PLANT_COUNT);
            }

            if (!hasRequiredResearchForField(field))
            {
                return Component.translatable(FIELD_LIST_PLANTATION_RESEARCH_REQUIRED);
            }
            return null;
        }

        /**
         * Getter of the worked plants.
         *
         * @param extraField the extra field to calculate.
         * @return the amount of worked plants.
         */
        private int getCurrentPlantsPlusField(final IField extraField)
        {
            final Set<IPlantationModule> plants = getOwnedFields().stream()
                                                    .map(field -> field.getFirstModuleOccurance(IPlantationModule.class))
                                                    .collect(Collectors.toSet());
            plants.add(extraField.getFirstModuleOccurance(IPlantationModule.class));
            return plants.size();
        }

        /**
         * Checks if the passed field has the research required.
         *
         * @param field the field in question.
         * @return true if the research is handled.
         */
        private boolean hasRequiredResearchForField(final IField field)
        {
            if (field instanceof PlantationField plantationField)
            {
                final IPlantationModule plantationModule = plantationField.getFirstModuleOccurance(IPlantationModule.class);
                if (plantationModule.getRequiredResearchEffect() != null)
                {
                    return getColony().getResearchManager().getResearchEffects().getEffectStrength(plantationModule.getRequiredResearchEffect()) > 0;
                }
                return true;
            }
            return false;
        }

        /**
         * Getter of the worked plants.
         *
         * @return the amount of worked plants.
         */
        public int getCurrentPlants()
        {
            return getOwnedFields().stream()
                     .map(field -> field.getFirstModuleOccurance(IPlantationModule.class))
                     .collect(Collectors.toSet())
                     .size();
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public BOWindow getWindow()
        {
            return new PlantationFieldsModuleWindow(buildingView, this);
        }

        /**
         * Get the maximum allowed plants the plantation can work on simultaneously.
         *
         * @return the maximum amount of concurrent plants.
         */
        public int getMaxConcurrentPlants()
        {
            return maxConcurrentPlants;
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

            for (FieldRegistries.FieldEntry type : FieldRegistries.getFieldRegistry().getValues())
            {
                type.getFieldModuleProducers().stream()
                  .map(m -> m.apply(null))
                  .filter(IPlantationModule.class::isInstance)
                  .map(m -> (IPlantationModule) m)
                  .findFirst()
                  .ifPresent(module -> recipes.add(new GenericRecipe(null,
                    new ItemStack(module.getItem()),
                    Collections.emptyList(),
                    List.of(module.getRequiredItemsForOperation()),
                    1,
                    Blocks.AIR,
                    null,
                    module.getRequiredTool(),
                    Collections.emptyList(),
                    -1)));
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
