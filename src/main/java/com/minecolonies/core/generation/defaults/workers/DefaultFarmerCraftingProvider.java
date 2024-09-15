package com.minecolonies.core.generation.defaults.workers;

import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.core.generation.CustomRecipeProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

    public DefaultFarmerCraftingProvider(@NotNull final PackOutput packOutput)
    {
        super(packOutput);
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
                .requiredTool(ModEquipmentTypes.shears.get())
                .build(consumer);

        CustomRecipeBuilder.create(FARMER, MODULE_CRAFTING, "mud")
                .inputs(List.of(new ItemStorage(new ItemStack(Items.DIRT)),
                        new ItemStorage(ModItems.large_water_bottle.getDefaultInstance())))
                .result(new ItemStack(Items.MUD))
                .lootTable(DefaultRecipeLootProvider.LOOT_TABLE_LARGE_BOTTLE)
                .build(consumer);
    }
}
