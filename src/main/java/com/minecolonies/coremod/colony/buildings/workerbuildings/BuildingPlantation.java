package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.workerbuildings.plantation.PlantationFieldType;
import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.colony.fields.IFieldView;
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
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.PlantationModule;
import com.minecolonies.coremod.colony.buildings.workerbuildings.plantation.PlantationModuleRegistry;
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
     * TODO: 1.20
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
            if (!workingPositions.isEmpty())
            {
                colony.getBuildingManager().addOrUpdateField(PlantationField.create(colony, getPosition(), type, workingPositions));
            }
        }
    }

    /**
     * TODO: 1.20
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
                    final PlantationModule plantationModule = PlantationModuleRegistry.getPlantationModule(plantationField.getPlantationFieldType());
                    if (plantationModule != null)
                    {
                        toKeep.put(new ItemStack(plantationModule.getItem())::sameItem, new Tuple<>(plantationModule.getPlantsToRequest(), true));
                    }
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
                    final PlantationModule plantationModule = PlantationModuleRegistry.getPlantationModule(plantationField.getPlantationFieldType());
                    if (plantationModule != null && (ItemStackUtils.compareItemStacksIgnoreStackSize(new ItemStack(plantationModule.getItem()), stack)))
                    {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * TODO: 1.20
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
        public @NotNull List<IField> getFields()
        {
            return building.getColony().getBuildingManager().getFields(FieldRegistries.plantationField.get());
        }

        @Override
        protected @NotNull List<IField> getFreeFields(final IColony colony)
        {
            return colony.getBuildingManager().getFreeFields(FieldRegistries.plantationField.get());
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
        public FieldRegistries.FieldEntry getExpectedFieldType()
        {
            return FieldRegistries.plantationField.get();
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

        /**
         * Getter of the worked plants.
         *
         * @return the amount of worked plants.
         */
        public int getCurrentPlants()
        {
            return getOwnedFields().stream()
                     .map(field -> ((PlantationField.View) field).getPlantationFieldType())
                     .collect(Collectors.toSet())
                     .size();
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
