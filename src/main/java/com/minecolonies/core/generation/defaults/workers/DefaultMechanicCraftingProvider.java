package com.minecolonies.core.generation.defaults.workers;

import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.core.generation.CustomRecipeProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

import static com.minecolonies.api.util.constant.BuildingConstants.MODULE_CRAFTING;

/**
 * Datagen for Mechanic
 */
public class DefaultMechanicCraftingProvider extends CustomRecipeProvider
{
    private static final String MECHANIC = ModJobs.MECHANIC_ID.getPath();

    public DefaultMechanicCraftingProvider(@NotNull final PackOutput packOutput)
    {
        super(packOutput);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "DefaultMechanicCraftingProvider";
    }

    @Override
    protected void registerRecipes(@NotNull final Consumer<FinishedRecipe> consumer)
    {
        CustomRecipeBuilder.create(MECHANIC, MODULE_CRAFTING, "gate_wood")
                .inputs(List.of(new ItemStorage(new ItemStack(Items.OAK_LOG, 5))))
                .result(new ItemStack(ModItems.woodgate))
                .showTooltip(true)
                .build(consumer);

        CustomRecipeBuilder.create(MECHANIC, MODULE_CRAFTING, "gate_iron")
                .inputs(List.of(new ItemStorage(new ItemStack(Items.IRON_NUGGET, 5))))
                .result(new ItemStack(ModItems.irongate))
                .showTooltip(true)
                .build(consumer);

        CustomRecipeBuilder.create(MECHANIC, MODULE_CRAFTING, "rails")
                .inputs(List.of(new ItemStorage(new ItemStack(Items.STICK, 5)),
                        new ItemStorage(new ItemStack(Items.IRON_INGOT, 2))))
                .result(new ItemStack(Items.RAIL, 16))
                .minBuildingLevel(3)
                .build(consumer);

        CustomRecipeBuilder.create(MECHANIC, MODULE_CRAFTING, "lantern")
                .inputs(List.of(new ItemStorage(new ItemStack(Items.IRON_NUGGET, 3)),
                        new ItemStorage(new ItemStack(Items.GLASS_BOTTLE)),
                        new ItemStorage(new ItemStack(Items.TORCH))))
                .result(new ItemStack(Items.LANTERN))
                .minBuildingLevel(3)
                .build(consumer);

        CustomRecipeBuilder.create(MECHANIC, MODULE_CRAFTING, "soul_lantern")
                .inputs(List.of(new ItemStorage(new ItemStack(Items.IRON_NUGGET, 3)),
                        new ItemStorage(new ItemStack(Items.GLASS_BOTTLE)),
                        new ItemStorage(new ItemStack(Items.SOUL_TORCH))))
                .result(new ItemStack(Items.SOUL_LANTERN))
                .minBuildingLevel(3)
                .build(consumer);

        deoxidize(consumer, Items.OXIDIZED_COPPER, Items.WEATHERED_COPPER);
        deoxidize(consumer, Items.OXIDIZED_CUT_COPPER, Items.WEATHERED_CUT_COPPER);
        deoxidize(consumer, Items.WEATHERED_COPPER, Items.EXPOSED_COPPER);
        deoxidize(consumer, Items.WEATHERED_CUT_COPPER, Items.EXPOSED_CUT_COPPER);
        deoxidize(consumer, Items.EXPOSED_COPPER, Items.COPPER_BLOCK);
        deoxidize(consumer, Items.EXPOSED_CUT_COPPER, Items.CUT_COPPER);
    }

    private void deoxidize(@NotNull final Consumer<FinishedRecipe> consumer,
                           @NotNull final Item input,
                           @NotNull final Item output)
    {
        CustomRecipeBuilder.create(MECHANIC, MODULE_CRAFTING, "deoxidize_" + ForgeRegistries.ITEMS.getKey(input).getPath())
                .inputs(List.of(new ItemStorage(new ItemStack(input))))
                .result(new ItemStack(output))
                .requiredTool(ToolType.AXE)
                .build(consumer);
    }
}
