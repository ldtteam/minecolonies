package com.minecolonies.core.generation.defaults.workers;

import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.core.generation.CustomRecipeProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

import static com.minecolonies.api.util.constant.BuildingConstants.MODULE_CUSTOM;

/**
 * Datagen for Lumberjack
 */
public class DefaultLumberjackCraftingProvider extends CustomRecipeProvider
{
    private static final String LUMBERJACK = ModJobs.LUMBERJACK_ID.getPath();

    public DefaultLumberjackCraftingProvider(@NotNull final PackOutput packOutput)
    {
        super(packOutput);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "DefaultLumberjackCraftingProvider";
    }

    @Override
    protected void registerRecipes(@NotNull final Consumer<CustomRecipeBuilder> consumer)
    {
        // Bamboo blocks, whilst they can be stripped, do not fall under the logs tag, hence they do not appear as part of the strip_logs.json
        CustomRecipeBuilder.create(LUMBERJACK, MODULE_CUSTOM, "strip_bamboo_block")
          .inputs(List.of(new ItemStorage(new ItemStack(Items.BAMBOO_BLOCK))))
          .result(new ItemStack(Items.STRIPPED_BAMBOO_BLOCK))
          .requiredTool(ToolType.AXE)
          .build(consumer);
    }
}
