package com.minecolonies.coremod.generation.defaults;

import com.google.common.collect.Lists;
import com.ldtteam.structurize.blocks.decorative.BlockTimberFrame;
import com.ldtteam.structurize.blocks.types.TimberFrameCentreType;
import com.ldtteam.structurize.blocks.types.TimberFrameType;
import com.ldtteam.structurize.blocks.types.WoodType;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.coremod.generation.CustomRecipeProvider;
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

import com.minecolonies.coremod.generation.CustomRecipeProvider.CustomRecipeBuilder;

public class SawmillTimberFrameRecipeProvider extends CustomRecipeProvider
{
    public SawmillTimberFrameRecipeProvider(final DataGenerator generatorIn)
    {
        super(generatorIn);
    }

    @Override
    protected void registerRecipes(final Consumer<IFinishedRecipe> consumer)
    {
        for (final WoodType frameType : WoodType.values())
        {
            for (final TimberFrameCentreType centreType : TimberFrameCentreType.values())
            {
                    final String id = String.format("%s_%s_timber_frame", frameType.getSerializedName(), centreType.getSerializedName());
                    final ItemStorage frame = new ItemStorage(new ItemStack(frameType.getMaterial(), 1));
                    final ItemStorage centre = new ItemStorage(new ItemStack(centreType.getMaterial()));

                    final List<ItemStack> results = Arrays.stream(TimberFrameType.values())
                                                      .map(type -> idToStructurize(BlockTimberFrame.getName(type, frameType, centreType), 4))
                                                      .collect(Collectors.toList());


                    CustomRecipeBuilder.create("sawmill", id)
                      .inputs(Lists.newArrayList(frame, centre, new ItemStorage(new ItemStack(buildTool.get()))))
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
