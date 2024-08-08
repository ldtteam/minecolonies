package com.minecolonies.core.util;

import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import com.ldtteam.domumornamentum.component.ModDataComponents;
import com.ldtteam.domumornamentum.util.Constants;
import com.ldtteam.structurize.api.ItemStorage;
import com.ldtteam.structurize.blocks.schematic.BlockSolidSubstitution;
import com.ldtteam.structurize.blocks.schematic.BlockSubstitution;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.util.BlockInfo;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class SchemAnalyzerUtil
{
    /**
     * Gets the tier for a given block
     *
     * @param block the block to analyze.
     * @return true if is so.
     */
    public static int getBlockTier(final Block block)
    {
        if (block == null)
        {
            return -1;
        }

        if (block.defaultBlockState().is(ModTags.tier6blocks))
        {
            return 6;
        }
        else if (block.defaultBlockState().is(ModTags.tier5blocks))
        {
            return 5;
        }
        else if (block.defaultBlockState().is(ModTags.tier4blocks))
        {
            return 4;
        }
        else if (block.defaultBlockState().is(ModTags.tier3blocks))
        {
            return 3;
        }
        else if (block.defaultBlockState().is(ModTags.tier2blocks))
        {
            return 2;
        }
        else if (block.defaultBlockState().is(ModTags.tier1blocks))
        {
            return 1;
        }

        return 0;
    }

    /**
     * Block cost calculation
     *
     * @param block
     * @return
     */
    public static double getScoreFor(final Block block)
    {
        double score = Math.pow(getBlockTier(block) + 1, 3);

        final BlockState state = block.defaultBlockState();

        if (state.is(BlockTags.LOGS))
        {
            score = Math.min(27, score);
        }
        else if (state.is(BlockTags.WOODEN_STAIRS) || state.is(BlockTags.WOODEN_BUTTONS) || state.is(BlockTags.WOODEN_DOORS) || state.is(BlockTags.WOODEN_SLABS) || state.is(
          BlockTags.WOODEN_FENCES) || state.is(BlockTags.WOODEN_TRAPDOORS) || state.is(BlockTags.WOODEN_PRESSURE_PLATES))
        {
            score = Math.min(27, score);
            score *= 1.0 / 4;
        }
        else if (state.is(BlockTags.PLANKS))
        {
            score = Math.min(27, score);
            score *= 1.0 / 4;
        }
        else if (state.is(BlockTags.SLABS))
        {
            score *= 1.0 / 2;
        }
        else if (state.is(BlockTags.BANNERS))
        {
            score *= 6;
        }
        else if (state.is(Tags.Blocks.STORAGE_BLOCKS))
        {
            score *= 9;
        }
        else if (state.is(BlockTags.ANVIL))
        {
            score *= 31;
        }
        else if (state.is(Tags.Blocks.BOOKSHELVES))
        {
            score *= 3;
        }
        else if (state.is(BlockTags.BASE_STONE_NETHER))
        {
            score = 8;
        }

        return score;
    }

    /**
     * Analyzes the given blueprint, calculating statistics like block cost, included buildings etc.
     *
     * @param blueprint
     * @return
     */
    public static SchematicAnalyzationResult analyzeSchematic(final Blueprint blueprint)
    {
        double complexityScore = 0;
        int containedBuildings = 0;
        Map<ItemStorage, ItemStorage> blocks = new HashMap<>();

        for (final BlockInfo blockInfo : blueprint.getBlockInfoAsList())
        {
            if (isExcludedBlock(blockInfo.getState()))
            {
                continue;
            }

            final Block block = blockInfo.getState().getBlock();
            final ItemStorage storage;
            double blockComplexity = 0;

            if (DomumOrnamentumUtils.isDoBlock(block) && blockInfo.hasTileEntityData())
            {
                final MaterialTextureData textureData = Utils.deserializeCodecMess(MaterialTextureData.CODEC, Minecraft.getInstance().level.registryAccess(), blockInfo.getTileEntityData().getCompound(Constants.BLOCK_ENTITY_TEXTURE_DATA));
                final ItemStack result = new ItemStack(block);
                if (!textureData.isEmpty())
                {
                    double doComplexity = 0;
                    for (final Block doBlockPart : textureData.getTexturedComponents().values())
                    {
                        doComplexity += getScoreFor(doBlockPart);
                    }

                    // Estimate for do recipes giving higher output per block usually, increased minimum of 2 due to added complexity for crafting
                    blockComplexity = Math.max(2, doComplexity / 3);

                    result.set(ModDataComponents.TEXTURE_DATA, textureData);
                }

                storage = new ItemStorage(result);
            }
            else
            {
                blockComplexity = getScoreFor(block);
                storage = new ItemStorage(block.asItem().getDefaultInstance());
            }

            complexityScore += blockComplexity;

            storage.setAmount(0);
            storage.getItemStack().setCount((int) Math.max(1, blockComplexity));
            ItemStorage contained = blocks.get(storage);
            if (contained == null)
            {
                contained = storage;
                blocks.put(contained, contained);
            }
            contained.setAmount(contained.getAmount() + 1);

            if (block instanceof AbstractBlockHut)
            {
                containedBuildings++;
            }
        }

        // Complexity for each building block, more building blocks = more complex
        complexityScore += containedBuildings * 500;

        // Complexity added for amount of unique blocks, more block variance = more complex
        complexityScore += blocks.keySet().size() * 40;

        // Complexity added for schematic size, volume / 10
        complexityScore += (blueprint.getSizeX() * blueprint.getSizeY() * blueprint.getSizeZ()) / 10.0;

        return new SchematicAnalyzationResult((int) complexityScore, blocks.keySet(), containedBuildings, blueprint);
    }

    /**
     * Excludes certain blocks
     *
     * @param blockState
     * @return
     */
    private static boolean isExcludedBlock(final BlockState blockState)
    {
        return blockState == null || blockState.isAir() || blockState.getBlock() instanceof BlockSubstitution || blockState.getBlock() instanceof BlockSolidSubstitution;
    }

    public static class SchematicAnalyzationResult
    {
        public final int              costScore;
        public final Set<ItemStorage> differentBlocks;
        public final int              containedBuildings;
        public final Blueprint        blueprint;

        public SchematicAnalyzationResult(final int costScore, final Set<ItemStorage> differentBlocks, final int containedBuildings, final Blueprint blueprint)
        {
            this.costScore = costScore;
            this.differentBlocks = differentBlocks;
            this.containedBuildings = containedBuildings;
            this.blueprint = blueprint;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }
            final SchematicAnalyzationResult that = (SchematicAnalyzationResult) o;
            return Objects.equals(blueprint, that.blueprint);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(blueprint);
        }
    }
}
