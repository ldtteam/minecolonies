package com.minecolonies.structures.client;

import com.minecolonies.structures.lib.TemplateUtils;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.template.Template;

import javax.annotation.Nullable;

public class TemplateBlockAccess implements IBlockAccess {

    private final Template template;

    public TemplateBlockAccess(Template template) {
        this.template = template;
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return TemplateUtils.getTileEntityFromPos(template, pos);
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return lightValue;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        return TemplateUtils.getBlockInfoFromPos(template, pos).blockState;
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return getBlockState(pos).getBlock() instanceof BlockAir;
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return Biomes.PLAINS;
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return 0;
    }

    @Override
    public WorldType getWorldType() {
        return WorldType.DEFAULT;
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
        return getBlockState(pos).isSideSolid(this, pos, side);
    }
}
