package com.minecolonies.core.generation.defaults.workers;

import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.core.generation.CustomRecipeProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

import static com.minecolonies.api.util.constant.BuildingConstants.MODULE_CRAFTING;

/**
 * Datagen for Farmer
 */
public class DefaultFarmerCraftingProvider extends CustomRecipeProvider
{
    private static final String FARMER = ModJobs.FARMER_ID.getPath();

    public DefaultFarmerCraftingProvider(DataGenerator generatorIn)
    {
        super(generatorIn);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "DefaultFarmerCraftingProvider";
    }

    @Override
    protected void registerRecipes(@NotNull final Consumer<FinishedRecipe> consumer)
    {
        CustomRecipeBuilder.create(FARMER, MODULE_CRAFTING, "carved_pumpkin")
                .inputs(List.of(new ItemStorage(new ItemStack(Items.PUMPKIN))))
                .result(new ItemStack(Items.CARVED_PUMPKIN))
                .requiredTool(ToolType.SHEARS)
                .build(consumer);

        CustomRecipeBuilder.create(FARMER, MODULE_CRAFTING, "mud")
                .inputs(List.of(new ItemStorage(new ItemStack(Items.DIRT)),
                        new ItemStorage(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER))))
                .result(new ItemStack(Items.MUD))
                .lootTable(DefaultRecipeLootProvider.LOOT_TABLE_BOTTLE)
                .build(consumer);
    }
}
