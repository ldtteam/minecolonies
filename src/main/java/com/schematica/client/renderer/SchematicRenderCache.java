package com.schematica.client.renderer;

import com.schematica.Settings;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RegionRenderCache;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class SchematicRenderCache extends RegionRenderCache {
    private final Minecraft minecraft = Minecraft.getMinecraft();

    public SchematicRenderCache(final World world, final BlockPos from, final BlockPos to, final int subtract) {
        super(world, from, to, subtract);
    }

    @Override
    public IBlockState getBlockState(final BlockPos pos) {
        final BlockPos realPos = pos.add(Settings.instance.schematic.position);
        final World world = this.minecraft.theWorld;

        if (!world.isAirBlock(realPos)) {
            return Blocks.air.getDefaultState();
        }

        return super.getBlockState(pos);
    }
}
