package com.schematica.block.state;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BlockStateHelper {
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T extends Comparable<T>> IProperty<T> getProperty(final IBlockState blockState, final String name) {
        for (final IProperty prop : blockState.getPropertyNames()) {
            if (prop.getName().equals(name)) {
                return prop;
            }
        }

        return null;
    }

    public static boolean areBlockStatesEqual(final IBlockState blockStateA, final IBlockState blockStateB) {
        if (blockStateA == blockStateB) {
            return true;
        }

        final Block blockA = blockStateA.getBlock();
        final Block blockB = blockStateB.getBlock();

        return blockA == blockB && blockA.getMetaFromState(blockStateA) == blockB.getMetaFromState(blockStateB);
    }
}
