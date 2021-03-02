package com.minecolonies.coremod.generation;

import com.google.common.collect.Lists;
import com.ldtteam.structurize.blocks.decorative.BlockTimberFrame;
import com.ldtteam.structurize.blocks.types.TimberFrameCentreType;
import com.ldtteam.structurize.blocks.types.TimberFrameFrameType;
import com.ldtteam.structurize.blocks.types.TimberFrameType;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.ldtteam.structurize.api.util.constant.Constants.MOD_ID;
import static com.ldtteam.structurize.items.ModItems.buildTool;

public class SawmillTimberFrameRecipeProvider extends CustomRecipeProvider
{
    public SawmillTimberFrameRecipeProvider(final DataGenerator generatorIn)
    {
        super(generatorIn);
    }

    @Override
    protected void registerRecipes(final Consumer<IFinishedRecipe> consumer)
    {
        for (final TimberFrameFrameType frameType : TimberFrameFrameType.values())
        {
            for (final TimberFrameCentreType centreType : TimberFrameCentreType.values())
            {
                final String id = String.format("%s_%s_timber_frame", frameType.getName(), centreType.getName());
                final ItemStack frame = idToStack(new ResourceLocation(frameType.getRecipeIngredient()), 1);
                final ItemStack centre = idToStack(new ResourceLocation(centreType.getRecipeIngredient()), 1);

                final List<ItemStack> results = Arrays.stream(TimberFrameType.values())
                        .map(type -> idToStructurize(BlockTimberFrame.getName(type, frameType, centreType), 4))
                        .collect(Collectors.toList());

                CustomRecipeBuilder.create("sawmill", id)
                        .inputs(Lists.newArrayList(frame, centre, new ItemStack(buildTool)))
                        .alternateOutputs(results.subList(1, results.size()))
                        .result(results.get(0))
                        .mustExist(true)
                        .build(consumer);
            }
        }
    }

    private static ItemStack idToStructurize(final String name, final int count)
    {
        return idToStack(new ResourceLocation(MOD_ID, name), count);
    }

    private static ItemStack idToStack(final ResourceLocation id, final int count)
    {
        return new ItemStack(ForgeRegistries.ITEMS.getValue(id), count);
    }
}
