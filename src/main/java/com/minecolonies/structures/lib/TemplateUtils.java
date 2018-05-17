package com.minecolonies.structures.lib;

import com.google.common.base.Functions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.minecolonies.blockout.Log;
import com.minecolonies.coremod.blocks.AbstractBlockHut;
import com.minecolonies.structures.client.TemplateBlockAccessTransformHandler;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.template.Template;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public final class TemplateUtils
{

    private static final Cache<Template, Map<BlockPos, Template.BlockInfo>> templateBlockInfoCache = CacheBuilder.newBuilder().maximumSize(50).build();

    private TemplateUtils()
    {
        throw new IllegalArgumentException("Utils class");
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

    public static Template.BlockInfo getBlockInfoFromPos(final Template template, final BlockPos pos)
    {
        try
        {
            return TemplateBlockAccessTransformHandler.getInstance().Transform(Optional.ofNullable(templateBlockInfoCache
                                         .get(template, () -> template.blocks.stream().collect(Collectors.toMap(bi -> bi.pos, Functions.identity())))
                                         .get(pos))
                     .orElse(new Template.BlockInfo(pos, Blocks.AIR.getDefaultState(), null)));
        }
        catch (ExecutionException e)
        {
            Log.getLogger().warn(e);
        }

        return new Template.BlockInfo(pos, Blocks.AIR.getDefaultState(), null);
    }

    public static BlockPos getPrimaryBlockOffset(@NotNull final Template template)
    {
        return template.blocks.stream()
                 .filter(blockInfo -> blockInfo.blockState.getBlock() instanceof AbstractBlockHut<?>)
                 .findFirst()
                 .map(blockInfo -> TemplateBlockAccessTransformHandler.getInstance().Transform(blockInfo))
                 .map(blockInfo -> blockInfo.pos)
                 .orElse(new BlockPos(template.getSize().getX() / 2, 0, template.getSize().getZ() / 2));
    }
}
