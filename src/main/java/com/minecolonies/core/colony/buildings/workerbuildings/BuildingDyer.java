package com.minecolonies.core.colony.buildings.workerbuildings;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.*;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.OptionalPredicate;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.modules.AbstractCraftingBuildingModule;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;
import static com.minecolonies.api.util.constant.TagConstants.CRAFTING_DYER;
import static com.minecolonies.api.util.constant.TagConstants.CRAFTING_DYER_SMELTING;

/**
 * Class of the dyer building.
 */
public class BuildingDyer extends AbstractBuilding
{
    /**
     * Description string of the building.
     */
    private static final String DYER = "dyer";

    /**
     * Instantiates a new dyer building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingDyer(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return DYER;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
    }

    public static class CraftingModule extends AbstractCraftingBuildingModule.Crafting
    {
        private List<ItemStorage> woolItems;

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
            return CraftingUtils.getIngredientValidatorBasedOnTags(CRAFTING_DYER)
                    .combine(super.getIngredientValidator());
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe)) return false;
            return CraftingUtils.isRecipeCompatibleBasedOnTags(recipe, CRAFTING_DYER).orElse(false);
        }

        @Override
        public void improveRecipe(final IRecipeStorage recipe, final int count, final ICitizenData citizen)
        {
            // don't improve any dyeing recipes
        }

        @NotNull
        @Override
        public List<IGenericRecipe> getAdditionalRecipesForDisplayPurposesOnly(@NotNull Level world)
        {
            final List<IGenericRecipe> recipes = new ArrayList<>(super.getAdditionalRecipesForDisplayPurposesOnly(world));

            // show dyeable leather items (at least for the single-dye recipes)
            final List<TagKey<Item>> dyes = List.of(
                    Tags.Items.DYES_WHITE, Tags.Items.DYES_ORANGE, Tags.Items.DYES_MAGENTA, Tags.Items.DYES_LIGHT_BLUE,
                    Tags.Items.DYES_YELLOW, Tags.Items.DYES_LIME, Tags.Items.DYES_PINK, Tags.Items.DYES_GRAY,
                    Tags.Items.DYES_LIGHT_GRAY, Tags.Items.DYES_CYAN, Tags.Items.DYES_PURPLE, Tags.Items.DYES_BLUE,
                    Tags.Items.DYES_BROWN, Tags.Items.DYES_GREEN, Tags.Items.DYES_RED, Tags.Items.DYES_BLACK);
            for (final ItemStack item : IColonyManager.getInstance().getCompatibilityManager().getListOfAllItems())
            {
                if (!(item.getItem() instanceof DyeableLeatherItem)) { continue; }

                for (final TagKey<Item> dyeTag : dyes)
                {
                    final List<ItemStack> dyeItems = ForgeRegistries.ITEMS.tags().getTag(dyeTag)
                            .stream().map(ItemStack::new).toList();
                    if (dyeItems.isEmpty()) { continue; }

                    if (dyeItems.get(0).getItem() instanceof final DyeItem dye)
                    {
                        final ItemStack result = DyeableLeatherItem.dyeArmor(item, List.of(dye));
                        if (!result.isEmpty())
                        {
                            recipes.add(new GenericRecipe(null, result, List.of(),
                                    List.of(List.of(item), dyeItems), 2, Blocks.AIR,
                                    null, ToolType.NONE, List.of(), 0));
                        }
                    }
                }
            }

            return recipes;
        }

        @Override
        public IRecipeStorage getFirstRecipe(Predicate<ItemStack> stackPredicate)
        {
            IRecipeStorage recipe = super.getFirstRecipe(stackPredicate);

            if(recipe == null && stackPredicate.test(new ItemStack(Items.WHITE_WOOL)))
            {
                final HashMap<ItemStorage, Integer> inventoryCounts = new HashMap<>();

                if (!building.getColony().getBuildingManager().hasWarehouse())
                {
                    return null;
                }

                for(ItemStorage color : getWoolItems())
                {
                    for(IBuilding wareHouse: building.getColony().getBuildingManager().getWareHouses())
                    {
                        final int colorCount = InventoryUtils.getCountFromBuilding(wareHouse, color);
                        inventoryCounts.put(color, inventoryCounts.getOrDefault(color, 0) + colorCount);
                    }
                }

                ItemStorage woolToUse = inventoryCounts.entrySet().stream().min(java.util.Map.Entry.comparingByValue(Comparator.reverseOrder())).get().getKey();

                final IToken<?> token = getTokenForWool(woolToUse);
                recipe = IColonyManager.getInstance().getRecipeManager().getRecipe(token);
            }
            return recipe;
        }

        @Override
        public boolean holdsRecipe(final IToken<?> token)
        {
            if (super.holdsRecipe(token))
            {
                return true;
            }

            final IRecipeStorage recipe = IColonyManager.getInstance().getRecipeManager().getRecipe(token);
            if (recipe == null)
            {
                return false;
            }

            return recipe.getPrimaryOutput().getItem() == Items.WHITE_WOOL;
        }

        @Override
        public IRecipeStorage getFirstFulfillableRecipe(final Predicate<ItemStack> stackPredicate, final int count, final boolean considerReservation)
        {
            IRecipeStorage recipe = super.getFirstFulfillableRecipe(stackPredicate, count, considerReservation);
            if (recipe == null && stackPredicate.test(new ItemStack(Items.WHITE_WOOL)))
            {
                final Set<IItemHandler> handlers = new HashSet<>();
                for (final ICitizenData workerEntity : building.getAllAssignedCitizen())
                {
                    handlers.add(workerEntity.getInventory());
                }

                for (ItemStorage color : getWoolItems())
                {
                    IToken<?> token = getTokenForWool(color);

                    final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);

                    IRecipeStorage toTest = storage.getRecipeType() instanceof MultiOutputRecipe ? storage.getClassicForMultiOutput(stackPredicate) : storage;
                    if (toTest.canFullFillRecipe(count, considerReservation ? reservedStacks() : Collections.emptyMap(), new ArrayList<>(handlers), building))
                    {
                        return toTest;
                    }
                }
            }
            return recipe;
        }

        /**
         * Builds and returns a list of all colored wool types
         * @return the list
         */
        private List<ItemStorage> getWoolItems()
        {
            if (woolItems == null)
            {
                woolItems = ForgeRegistries.ITEMS.tags().getTag(ItemTags.WOOL).stream()
                  .filter(item -> !item.equals(Items.WHITE_WOOL))
                  .map(i -> new ItemStorage(new ItemStack(i))).collect(Collectors.toList());
            }
            return woolItems;
        }

        /**
         * Creates the recipe to undye the given wool and returns its token
         * @param wool the wool to undye
         * @return the recipe token
         */
        private IToken<?> getTokenForWool(ItemStorage wool)
        {
            final IRecipeStorage tempRecipe = StandardFactoryController.getInstance().getNewInstance(
              TypeConstants.RECIPE,
              StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
              ImmutableList.of(wool, new ItemStorage(new ItemStack(Items.WHITE_DYE, 1))),
              1,
              new ItemStack(Items.WHITE_WOOL, 1),
              Blocks.AIR);

            return IColonyManager.getInstance().getRecipeManager().checkOrAddRecipe(tempRecipe);
        }
    }

    public static class SmeltingModule extends AbstractCraftingBuildingModule.Smelting
    {
        /**
         * Create a new module.
         *
         * @param jobEntry the entry of the job.
         */
        public SmeltingModule(final JobEntry jobEntry)
        {
            super(jobEntry);
        }

        @NotNull
        @Override
        public OptionalPredicate<ItemStack> getIngredientValidator()
        {
            return CraftingUtils.getIngredientValidatorBasedOnTags(CRAFTING_DYER_SMELTING)
                    .combine(super.getIngredientValidator());
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe))
            {
                return false;
            }
            return CraftingUtils.isRecipeCompatibleBasedOnTags(recipe, CRAFTING_DYER_SMELTING).orElse(false);
        }

        @Override
        public void improveRecipe(final IRecipeStorage recipe, final int count, final ICitizenData citizen)
        {
            // don't improve any dyeing recipes
        }
    }
}
