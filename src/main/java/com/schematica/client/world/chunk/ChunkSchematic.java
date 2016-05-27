package com.schematica.client.world.chunk;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

class ChunkSchematic extends Chunk {
    private final World world;

    public ChunkSchematic(final World world, final int x, final int z) {
        super(world, x, z);
        this.world = world;
    }

    @Override
    protected void generateHeightMap() {
    }

    @Override
    public void generateSkylightMap() {
    }

    @Override
    public IBlockState getBlockState(final BlockPos pos) {
        return this.world.getBlockState(pos);
    }

    @Override
    public boolean getAreLevelsEmpty(final int startY, final int endY) {
        return false;
    }

    @Override
    public TileEntity getTileEntity(final BlockPos pos, final EnumCreateEntityType createEntityType) {
        return this.world.getTileEntity(pos);
    }
}
