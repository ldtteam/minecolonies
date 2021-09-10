package com.minecolonies.coremod.util;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Map;

/**
 * Utilities for locating tags in a blueprint
 */
public class BlueprintTagUtils
{
    /**
     * Get the first pos for the given tag
     *
     * @param blueprint rotated/mirrored blueprint
     * @param tagName   tag name
     * @return found position or null
     */
    public static BlockPos getFirstPosForTag(final Blueprint blueprint, final String tagName)
    {
        final BlockPos anchorPos = blueprint.getPrimaryBlockOffset();
        final CompoundNBT nbt = blueprint.getBlockInfoAsMap().get(anchorPos).getTileEntityData();
        if (nbt != null)
        {
            final Map<BlockPos, List<String>> tagPosMap = IBlueprintDataProvider.readTagPosMapFrom(nbt);
            for (final Map.Entry<BlockPos, List<String>> entry : tagPosMap.entrySet())
            {
                for (final String tag : entry.getValue())
                {
                    if (tag.equals(tagName))
                    {
                        return entry.getKey();
                    }
                }
            }
        }

        return null;
    }
}
