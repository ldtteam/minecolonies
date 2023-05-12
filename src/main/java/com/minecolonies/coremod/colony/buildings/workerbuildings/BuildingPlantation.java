package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.google.common.collect.ImmutableMap;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.crafting.GenericRecipe;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.OptionalPredicate;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.coremod.colony.buildings.modules.settings.PlantationSetting;
import com.minecolonies.coremod.colony.buildings.modules.settings.SettingKey;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_PLANTGROUND;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;
import static com.minecolonies.api.util.constant.TagConstants.CRAFTING_PLANTATION;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

/**
 * Class of the plantation building. Worker will grow sugarcane/bamboo/cactus + craft paper and books.
 */
public class BuildingPlantation extends AbstractBuilding
{
    /**
     * Settings key for the building mode.
     */
    public static final ISettingKey<PlantationSetting> MODE =
      new SettingKey<>(PlantationSetting.class, new ResourceLocation(com.minecolonies.api.util.constant.Constants.MOD_ID, "mode"));

    /**
     * The combinations of items/blocks/tags.
     */
    public static final Map<Item, PlantationItem> COMBINATIONS = ImmutableMap.<Item, PlantationItem>builder()
      .put(Items.SUGAR_CANE, new PlantationItem(Items.SUGAR_CANE, Blocks.SUGAR_CANE, "sugar", 3))
      .put(Items.CACTUS, new PlantationItem(Items.CACTUS, Blocks.CACTUS, "cactus", 3))
      .put(Items.BAMBOO, new PlantationItem(Items.BAMBOO, Blocks.BAMBOO, "bamboo", 3))
      .build();

    /**
     * Description string of the building.
     */
    private static final String PLANTATION = "plantation";

    /**
     * List of sand blocks to grow onto.
     */
    private final List<BlockPos> sand = new ArrayList<>();

    /**
     * Cached list of soil positions
     */
    private List<PlantationSoilPosition> soilPositions = null;

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
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final Level world)
    {
        super.registerBlockPosition(block, pos, world);
        if (block == Blocks.SAND)
        {
            final Block down = world.getBlockState(pos.below()).getBlock();
            if (down == Blocks.COBBLESTONE || down == Blocks.STONE_BRICKS)
            {
                sand.add(pos);
            }
        }
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);
        final ListTag sandPos = compound.getList(TAG_PLANTGROUND, Tag.TAG_COMPOUND);
        for (int i = 0; i < sandPos.size(); ++i)
        {
            sand.add(NbtUtils.readBlockPos(sandPos.getCompound(i).getCompound(TAG_POS)));
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = super.serializeNBT();
        @NotNull final ListTag sandCompoundList = new ListTag();
        for (@NotNull final BlockPos entry : sand)
        {
            @NotNull final CompoundTag sandCompound = new CompoundTag();
            sandCompound.put(TAG_POS, NbtUtils.writeBlockPos(entry));
            sandCompoundList.add(sandCompound);
        }
        compound.put(TAG_PLANTGROUND, sandCompoundList);
        return compound;
    }

    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);
        soilPositions = null;
    }

    /**
     * Get a list of all the available working positions.
     *
     * @return the list of positions.
     */
    public List<PlantationSoilPosition> getAllSoilPositions()
    {
        if (soilPositions == null)
        {
            soilPositions = getSoilPositions((tag, item) -> true);
        }

        return soilPositions;
    }

    /**
     * Get a list of all the available working positions.
     *
     * @param filter a predicate to filter against, contains the tag of a building.
     * @return the list of positions.
     */
    private List<PlantationSoilPosition> getSoilPositions(BiPredicate<String, Item> filter)
    {
        final List<PlantationSoilPosition> filtered = new ArrayList<>();
        if (tileEntity != null && !tileEntity.getPositionedTags().isEmpty())
        {
            Map<String, Item> availableTags = COMBINATIONS.entrySet().stream().collect(Collectors.toMap(k -> k.getValue().getTag(), Map.Entry::getKey));

            for (final Map.Entry<BlockPos, List<String>> entry : tileEntity.getPositionedTags().entrySet())
            {
                final Optional<String> foundTag = entry.getValue().stream().filter(availableTags::containsKey).findFirst();
                if (!foundTag.isPresent())
                {
                    continue;
                }

                Item item = availableTags.get(foundTag.get());
                if (filter.test(foundTag.get(), item))
                {
                    filtered.add(new PlantationSoilPosition(getPosition().offset(entry.getKey()), COMBINATIONS.get(item)));
                }
            }
        }

        return filtered;
    }

    /**
     * Obtain the current list of available plants to use.
     *
     * @return a list of plants.
     */
    public List<Item> getAvailablePlants()
    {
        final String setting = getSetting(MODE).getValue();

        List<Item> items = new ArrayList<>();
        if (setting.contains(Items.SUGAR_CANE.getDescriptionId()))
        {
            items.add(Items.SUGAR_CANE);
        }
        if (setting.contains(Items.CACTUS.getDescriptionId()))
        {
            items.add(Items.CACTUS);
        }
        if (setting.contains(Items.BAMBOO.getDescriptionId()))
        {
            items.add(Items.BAMBOO);
        }

        // Add sugar cane as the default plant cycle as a fallback in case nothing exists in the settings
        if (items.isEmpty())
        {
            items.add(Items.SUGAR_CANE);
            Log.getLogger().warn("Plantation plant setting contains none of the preconfigured plants, please report this to the developers!");
        }
        return items;
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

        @Override
        public @NotNull List<IGenericRecipe> getAdditionalRecipesForDisplayPurposesOnly()
        {
            final List<IGenericRecipe> recipes = new ArrayList<>(super.getAdditionalRecipesForDisplayPurposesOnly());

            // indicate which plants we can grow
            for (final Map.Entry<Item, PlantationItem> entry : COMBINATIONS.entrySet())
            {
                recipes.add(new GenericRecipe(null,
                        new ItemStack(entry.getKey(), entry.getValue().getMinimumLength() + 1),
                        Collections.emptyList(),
                        Collections.singletonList(Collections.singletonList(new ItemStack(entry.getKey()))),
                        1, Blocks.AIR, null, ToolType.NONE, Collections.emptyList(), -1));
            }

            return recipes;
        }
    }

    public static class PlantationSoilPosition
    {
        /**
         * The position.
         */
        private final BlockPos position;

        /**
         * The item combination data.
         */
        private final PlantationItem combination;

        /**
         * Default constructor.
         *
         * @param position    the position.
         * @param combination the item combination data.
         */
        private PlantationSoilPosition(final BlockPos position, final PlantationItem combination)
        {
            this.position = position;
            this.combination = combination;
        }

        /**
         * Get the position.
         *
         * @return the position.
         */
        public BlockPos getPosition()
        {
            return position;
        }

        /**
         * Get the plantation item.
         *
         * @return the plantation item.
         */
        public PlantationItem getCombination()
        {
            return combination;
        }
    }

    public static class PlantationItem
    {
        /**
         * The item of the combination.
         */
        private final Item item;

        /**
         * The block of the combination.
         */
        private final Block block;

        /**
         * The tag of the combination.
         */
        private final String tag;

        /**
         * The minimum length of this combination.
         */
        private final int minimumLength;

        /**
         * Default constructor.
         *
         * @param item          the item.
         * @param block         the block.
         * @param tag           the tag.
         * @param minimumLength the minimum length.
         */
        private PlantationItem(Item item, Block block, String tag, int minimumLength)
        {
            this.item = item;
            this.block = block;
            this.tag = tag;
            this.minimumLength = minimumLength;
        }

        /**
         * Get the item of the combination.
         *
         * @return the item.
         */
        public Item getItem()
        {
            return item;
        }

        /**
         * Get the block of the combination.
         *
         * @return the block.
         */
        public Block getBlock()
        {
            return block;
        }

        /**
         * Get the tag of the combination.
         *
         * @return the tag.
         */
        public String getTag()
        {
            return tag;
        }

        /**
         * Get the minimum length of the combination.
         *
         * @return the minimum length.
         */
        public int getMinimumLength()
        {
            return minimumLength;
        }
    }
}
