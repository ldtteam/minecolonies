package com.minecolonies.structures.lib;

import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.template.Template;

public final class TemplateUtils {

    private TemplateUtils() {
        throw new IllegalArgumentException("Utils class");
    }

    public static Template.BlockInfo getBlockInfoFromPos(final Template template, final BlockPos pos)
    {
        return template.blocks.stream().filter(blockInfo -> blockInfo.pos.equals(pos)).findFirst().orElse(new Template.BlockInfo(pos, Blocks.AIR.getDefaultState(), null));
    }

    public static TileEntity getTileEntityFromPos(final Template template, final BlockPos pos)
    {
        final Template.BlockInfo blockInfo = getBlockInfoFromPos(template, pos);
        if (blockInfo.tileentityData != null)
        {
            //TODO: Figure out if this world = null thing does not harm anyone for rendering purposes.
            return TileEntity.create(null, blockInfo.tileentityData);
        }

        return null;
    }
}
