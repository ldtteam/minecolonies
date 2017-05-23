package com.minecolonies.structures.fake;

import net.minecraft.block.state.IBlockState;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.jetbrains.annotations.NotNull;

/**
 * Holds a fake world.
 */
public class FakeWorld extends World
{

    private final IBlockState blockState;

    /**
     * Creates a fake world.
     *
     * @param blockState    with the blockState.
     * @param saveHandlerIn a saveHandler.
     * @param info          additional info.
     * @param providerIn    worldProvider.
     * @param profilerIn    profiler.
     * @param client        and if is client.
     */
    public FakeWorld(
                      final IBlockState blockState,
                      final ISaveHandler saveHandlerIn,
                      final WorldInfo info,
                      final WorldProvider providerIn,
                      final Profiler profilerIn,
                      final boolean client)
    {
        super(saveHandlerIn, info, providerIn, profilerIn, client);
        this.blockState = blockState;
    }

    @NotNull
    @Override
    protected IChunkProvider createChunkProvider()
    {
         /*
         * Intentionally left empty.
         */
        return null;
    }

    @Override
    protected boolean isChunkLoaded(final int x, final int z, final boolean allowEmpty)
    {
         /*
         * Intentionally left empty.
         */
        return false;
    }

    @NotNull
    @Override
    public IBlockState getBlockState(final BlockPos pos)
    {
        return this.blockState;
    }

    @Override
    public TileEntity getTileEntity(final BlockPos pos)
    {
        return null;
    }
}
