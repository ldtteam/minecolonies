package com.schematica.block.state;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;

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
}
