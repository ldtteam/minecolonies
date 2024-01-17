package com.minecolonies.core.generation.defaults.workers;

import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.research.util.ResearchConstants;
import com.minecolonies.core.generation.CustomRecipeProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

import static com.minecolonies.api.util.constant.BuildingConstants.MODULE_CUSTOM;

/**
 * Datagen for Crusher
 */
public class DefaultCrusherCraftingProvider extends CustomRecipeProvider
{
    private static final String CRUSHER = ModJobs.CRUSHER_ID.getPath();

    public DefaultCrusherCraftingProvider(@NotNull final PackOutput packOutput)
    {
        super(packOutput);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "DefaultCrusherCraftingProvider";
    }

    @Override
    protected void registerRecipes(@NotNull final Consumer<FinishedRecipe> consumer)
    {
        final Rule noGildedHammer = builder -> builder.maxResearchId(ResearchConstants.CRUSHING_11);
        final Rule withGildedHammer = builder -> builder.minResearchId(ResearchConstants.CRUSHING_11);
        final Rule withTheDepths = builder -> builder.minResearchId(ResearchConstants.THE_DEPTHS);
        final Rule gravelLoot = builder -> builder.lootTable(DefaultRecipeLootProvider.LOOT_TABLE_GRAVEL);

        crush(consumer, "bonemeal1", new ItemStack(Items.BONE), new ItemStack(Items.BONE_MEAL, 3), noGildedHammer);
        crush(consumer, "bonemeal2", new ItemStack(Items.BONE), new ItemStack(Items.BONE_MEAL, 5), withGildedHammer);
        crush(consumer, "bonemeal3", new ItemStack(Items.BONE_BLOCK), new ItemStack(Items.BONE_MEAL, 9));

        crush(consumer, "gravel1", new ItemStack(Items.COBBLESTONE, 2), new ItemStack(Items.GRAVEL), noGildedHammer, gravelLoot);
        crush(consumer, "gravel2", new ItemStack(Items.COBBLESTONE), new ItemStack(Items.GRAVEL), withGildedHammer, gravelLoot);

        crush(consumer, "sand1", new ItemStack(Items.GRAVEL, 2), new ItemStack(Items.SAND), noGildedHammer);
        crush(consumer, "sand2", new ItemStack(Items.GRAVEL), new ItemStack(Items.SAND), withGildedHammer);

        crush(consumer, "clay1", new ItemStack(Items.SAND, 2), new ItemStack(Items.CLAY), noGildedHammer);
        crush(consumer, "clay2", new ItemStack(Items.SAND), new ItemStack(Items.CLAY), withGildedHammer);
        crush(consumer, "clay_ball", new ItemStack(Items.CLAY), new ItemStack(Items.CLAY_BALL, 4));

        crush(consumer, "cobble", new ItemStack(Items.TUFF, 2), new ItemStack(Items.COBBLESTONE), withTheDepths, noGildedHammer);
        crush(consumer, "cobble2", new ItemStack(Items.TUFF), new ItemStack(Items.COBBLESTONE), withTheDepths, withGildedHammer);
        crush(consumer, "tuff", new ItemStack(Items.COBBLED_DEEPSLATE, 2), new ItemStack(Items.TUFF), withTheDepths, noGildedHammer);
        crush(consumer, "tuff2", new ItemStack(Items.COBBLED_DEEPSLATE), new ItemStack(Items.TUFF), withTheDepths, withGildedHammer);
    }

    private void crush(@NotNull final Consumer<FinishedRecipe> consumer,
                       @NotNull final String name,
                       @NotNull final ItemStack input,
                       @NotNull final ItemStack output,
                       @NotNull final Rule... rules)
    {
        final CustomRecipeBuilder builder = CustomRecipeBuilder.create(CRUSHER, MODULE_CUSTOM, name)
                .inputs(List.of(new ItemStorage(input)))
                .result(output);
        for (final Rule rule : rules)
        {
            rule.accept(builder);
        }
        builder.build(consumer);
    }

    /**
     * Convenience alias to stop the analyzer getting mad about variadic generics
     */
    private interface Rule extends Consumer<CustomRecipeBuilder>
    {
    }
}
