package com.minecolonies.core.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.GenericRecipe;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.api.util.OptionalPredicate;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.modules.AbstractCraftingBuildingModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
                if (!(item.getItem() instanceof ArmorItem armorItem) || !item.has(DataComponents.DYED_COLOR))
                {
                    continue;
                }

                for (final TagKey<Item> dyeTag : dyes)
                {
                    final List<ItemStack> dyeItems = BuiltInRegistries.ITEM.getTag(dyeTag).get()
                            .stream().map(ItemStack::new).toList();
                    if (dyeItems.isEmpty()) { continue; }

                    if (dyeItems.get(0).getItem() instanceof final DyeItem dye)
                    {

                        final ItemStack result = DyedItemColor.applyDyes(item, List.of(dye));
                        if (!result.isEmpty())
                        {
                            recipes.add(GenericRecipe.builder()
                                    .withOutput(result)
                                    .withInputs(List.of(List.of(item), dyeItems))
                                    .withGridSize(2)
                                    .build());
                        }
                    }
                }
            }

            return recipes;
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
