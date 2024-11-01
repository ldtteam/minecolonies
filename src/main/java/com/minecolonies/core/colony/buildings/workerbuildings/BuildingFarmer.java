package com.minecolonies.core.colony.buildings.workerbuildings;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.colony.fields.registry.FieldRegistries;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.crafting.GenericRecipe;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.OptionalPredicate;
import com.minecolonies.core.blocks.MinecoloniesCropBlock;
import com.minecolonies.core.client.gui.modules.FarmFieldsModuleWindow;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.core.colony.buildings.modules.FieldsModule;
import com.minecolonies.core.colony.buildings.modules.settings.BoolSetting;
import com.minecolonies.core.colony.buildings.modules.settings.SettingKey;
import com.minecolonies.core.colony.buildings.moduleviews.FieldsModuleView;
import com.minecolonies.core.colony.fields.FarmField;
import com.minecolonies.core.items.ItemCrop;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.EquipmentLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;
import static com.minecolonies.api.util.constant.TagConstants.CRAFTING_FARMER;
import static com.minecolonies.api.util.constant.TranslationConstants.PARTIAL_JEI_INFO;
import static com.minecolonies.api.util.constant.translation.GuiTranslationConstants.FIELD_LIST_FARMER_NO_SEED;

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
        keepX.put(itemStack -> ItemStackUtils.hasEquipmentLevel(itemStack, ModEquipmentTypes.hoe.get(), TOOL_LEVEL_WOOD_OR_GOLD, getMaxEquipmentLevel()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.hasEquipmentLevel(itemStack, ModEquipmentTypes.axe.get(), TOOL_LEVEL_WOOD_OR_GOLD, getMaxEquipmentLevel()), new Tuple<>(1, true));
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
        for (FieldsModule module : getModulesByType(FieldsModule.class))
        {
            for (final IField field : module.getOwnedFields())
            {
                if (field instanceof FarmField farmField && !farmField.getSeed().isEmpty())
                {
                    toKeep.put(stack -> ItemStack.isSameItem(farmField.getSeed(), stack), new Tuple<>(64, true));
                }
            }
        }
        return toKeep;
    }

    @Override
    public boolean canEat(final ItemStack stack)
    {
        for (FieldsModule module : getModulesByType(FieldsModule.class))
        {
            for (final IField field : module.getOwnedFields())
            {
                if (field instanceof FarmField farmField && !farmField.getSeed().isEmpty() && ItemStackUtils.compareItemStacksIgnoreStackSize(farmField.getSeed(), stack))
                {
                    return false;
                }
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
        protected int getMaxFieldCount()
        {
            return building.getBuildingLevel();
        }

        @Override
        public Class<?> getExpectedFieldType()
        {
            return FarmField.class;
        }

        @Override
        public @NotNull List<IField> getFields()
        {
            return building.getColony().getBuildingManager().getFields(field -> field.getFieldType().equals(FieldRegistries.farmField.get())).stream().toList();
        }

        @Override
        public boolean canAssignFieldOverride(final IField field)
        {
            return field instanceof FarmField farmField && !farmField.getSeed().isEmpty();
        }

        @Override
        protected int getFieldCheckTimeoutSeconds()
        {
            return 60;
        }
    }

    /**
     * Field module view implementation for the farmer.
     */
    public static class FarmerFieldsModuleView extends FieldsModuleView
    {
        @Override
        @OnlyIn(Dist.CLIENT)
        public BOWindow getWindow()
        {
            return new FarmFieldsModuleWindow(buildingView, this);
        }

        @Override
        public boolean canAssignFieldOverride(final IField field)
        {
            return field instanceof FarmField farmField && !farmField.getSeed().isEmpty();
        }

        @Override
        protected List<IField> getFieldsInColony()
        {
            return getColony().getFields(field -> field.getFieldType().equals(FieldRegistries.farmField.get()));
        }

        @Override
        public @Nullable MutableComponent getFieldWarningTooltip(final IField field)
        {
            MutableComponent result = super.getFieldWarningTooltip(field);
            if (result != null)
            {
                return result;
            }

            if (field instanceof FarmField farmField && farmField.getSeed().isEmpty())
            {
                return Component.translatable(FIELD_LIST_FARMER_NO_SEED);
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

        @NotNull
        @Override
        public List<IGenericRecipe> getAdditionalRecipesForDisplayPurposesOnly(@NotNull Level world)
        {
            List<IGenericRecipe> recipes = new ArrayList<>(super.getAdditionalRecipesForDisplayPurposesOnly(world));
            for (final ItemStack stack : IColonyManager.getInstance().getCompatibilityManager().getListOfAllItems())
            {
                if (stack.getItem() instanceof ItemCrop cropItem && cropItem.getBlock() instanceof MinecoloniesCropBlock crop)
                {
                    // MineColonies crop
                    final List<Component> restrictions = new ArrayList<>();
                    if (crop.getPreferredBiome() != null)
                    {
                        final Registry<Biome> biomeRegistry = world.registryAccess().registryOrThrow(crop.getPreferredBiome().registry());
                        final Object[] biomes = biomeRegistry.getTag(crop.getPreferredBiome()).get().stream()
                                .map(b -> Component.translatable(biomeRegistry.getKey(b.get()).toLanguageKey("biome")))
                                .toArray();

                        restrictions.add(Component.translatable(PARTIAL_JEI_INFO + "biomerestriction",
                                Component.translatable(String.join(", ", Collections.nCopies(biomes.length, "%s")), biomes)));
                    }

                    recipes.add(new GenericRecipe(null, ItemStack.EMPTY, List.of(),
                            List.of(List.of(new ItemStack(cropItem))),
                            1, crop.getPreferredFarmland(), crop.getLootTable(), ModEquipmentTypes.hoe.get(), restrictions, 0));
                }
                else if (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof CropBlock crop)
                {
                    // regular crop
                    recipes.add(new GenericRecipe(null, ItemStack.EMPTY, List.of(),
                            List.of(List.of(crop.getCloneItemStack(world, BlockPos.ZERO, crop.defaultBlockState()))),
                            1, Blocks.FARMLAND, crop.getLootTable(), ModEquipmentTypes.hoe.get(), List.of(), 0));
                }
                else if (stack.is(Tags.Items.SEEDS))
                {
                    // another kind of seed?
                    if (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof StemBlock stem)
                    {
                        recipes.add(new GenericRecipe(null, new ItemStack(stem.getFruit()), List.of(), List.of(List.of(stack)),
                                1, Blocks.FARMLAND, null, ModEquipmentTypes.hoe.get(), List.of(), 0));
                    }
                    else
                    {
                        recipes.add(new GenericRecipe(null, ItemStack.EMPTY, List.of(), List.of(List.of(stack)),
                                1, Blocks.FARMLAND, null, ModEquipmentTypes.hoe.get(), List.of(), 0));
                    }
                }
            }
            return recipes;
        }

        @NotNull
        @Override
        public List<ResourceLocation> getAdditionalLootTables()
        {
            final List<ResourceLocation> tables = new ArrayList<>(super.getAdditionalLootTables());
            for (final ItemStack stack : IColonyManager.getInstance().getCompatibilityManager().getListOfAllItems())
            {
                if (stack.getItem() instanceof ItemCrop cropItem && cropItem.getBlock() instanceof MinecoloniesCropBlock crop)
                {
                    tables.add(crop.getLootTable());
                }
                else if (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof CropBlock crop)
                {
                    tables.add(crop.getLootTable());
                }
            }
            return tables;
        }
    }
}
