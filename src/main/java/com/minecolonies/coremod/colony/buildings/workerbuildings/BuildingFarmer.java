package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.buildings.workerbuildings.fields.IField;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.OptionalPredicate;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.coremod.colony.buildings.modules.FieldsModule;
import com.minecolonies.coremod.colony.buildings.modules.settings.BoolSetting;
import com.minecolonies.coremod.colony.buildings.modules.settings.SettingKey;
import com.minecolonies.coremod.colony.buildings.moduleviews.FieldModuleView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.fields.FarmField;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.TagConstants.CRAFTING_FARMER;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

/**
 * Class which handles the farmer building.
 */
public class BuildingFarmer extends AbstractBuilding
{
    /**
     * The beekeeper mode.
     */
    public static final ISettingKey<BoolSetting> FERTILIZE =
      new SettingKey<>(BoolSetting.class, new ResourceLocation(com.minecolonies.api.util.constant.Constants.MOD_ID, "fertilize"));

    /**
     * Descriptive string of the profession.
     */
    private static final String FARMER = "farmer";

    /**
     * The maximum building level of the hut.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * Public constructor which instantiates the building.
     *
     * @param c the colony the building is in.
     * @param l the position it has been placed (it's id).
     */
    public BuildingFarmer(final IColony c, final BlockPos l)
    {
        super(c, l);
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.HOE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.AXE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
    }

    @Override
    public boolean canBeGathered()
    {
        // Normal crafters are only gatherable when they have a task, i.e. while producing stuff.
        // BUT, the farmer both gathers and crafts things now, like the lumberjack
        return true;
    }

    /**
     * Override this method if you want to keep an amount of items in inventory. When the inventory is full, everything get's dumped into the building chest. But you can use this
     * method to hold some stacks back.
     *
     * @return a map of objects which should be kept.
     */
    @Override
    public Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> getRequiredItemsAndAmount()
    {
        final Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> toKeep = new HashMap<>(super.getRequiredItemsAndAmount());

        for (final IField field : getFieldModule().getFields())
        {
            if (field.getPlant() != null)
            {
                final ItemStack seedStack = new ItemStack(field.getPlant());
                toKeep.put(seedStack::sameItem, new Tuple<>(64, true));
            }
        }
        return toKeep;
    }

    @Override
    public boolean canEat(final ItemStack stack)
    {
        for (IField field : getFieldModule().getFields())
        {
            if (field.getPlant() != null && ItemStackUtils.compareItemStacksIgnoreStackSize(new ItemStack(field.getPlant()), stack))
            {
                return false;
            }
        }

        if (stack.getItem() == Items.WHEAT)
        {
            return false;
        }
        return super.canEat(stack);
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    private FieldsModule getFieldModule()
    {
        return getFirstModuleOccurance(FieldsModule.class);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return FARMER;
    }

    /**
     * Getter for request fertilizer
     */
    public boolean requestFertilizer()
    {
        return getSetting(FERTILIZE).getValue();
    }

    /**
     * Field module implementation for the farmer.
     */
    public static class FarmerFieldsModule extends FieldsModule
    {
        @Override
        protected Collection<IField> getFields(final IColony colony)
        {
            return colony.getBuildingManager().getFields(FarmField.class).stream().map(IField.class::cast).toList();
        }

        @Override
        protected int getMaxFieldCount()
        {
            return building.getBuildingLevel();
        }

        @Override
        protected int getMaxConcurrentPlants()
        {
            return building.getBuildingLevel();
        }

        @Override
        protected @Nullable IField getFreeField(final IColony colony)
        {
            return colony.getBuildingManager().getFreeField(FarmField.class);
        }
    }

    /**
     * Field module view implementation for the farmer.
     */
    public static class FarmerFieldModuleView extends FieldModuleView
    {
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
            return CraftingUtils.getIngredientValidatorBasedOnTags(CRAFTING_FARMER)
                     .combine(super.getIngredientValidator());
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe))
            {
                return false;
            }
            return CraftingUtils.isRecipeCompatibleBasedOnTags(recipe, CRAFTING_FARMER).orElse(false);
        }
    }
}
