package com.schematica.block.state.pattern;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;

import java.util.Map;

public class BlockStateReplacer
{
    private final IBlockState defaultReplacement;

    private BlockStateReplacer(final IBlockState defaultReplacement) {
        this.defaultReplacement = defaultReplacement;
    }

    @SuppressWarnings({ "rawtypes" })
    public IBlockState getReplacement(final IBlockState original, final Map<IProperty, Comparable> properties) {
        IBlockState replacement = this.defaultReplacement;

        replacement = applyProperties(replacement, original.getProperties());
        replacement = applyProperties(replacement, properties);

        return replacement;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private IBlockState applyProperties(IBlockState blockState, final Map<IProperty, Comparable> properties) {
        for (final Map.Entry<IProperty, Comparable> entry : properties.entrySet()) {
            try {
                blockState = blockState.withProperty(entry.getKey(), entry.getValue());
            } catch (final IllegalArgumentException ignored) {
            }
        }

        return blockState;
    }
}
