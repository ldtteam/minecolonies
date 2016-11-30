package com.structures.fake;

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
	
	private IBlockState blockState;

    /**
     * Creates a fake world.
     * @param blockState with the blockState.
     * @param saveHandlerIn a saveHandler.
     * @param info additional info.
     * @param providerIn worldProvider.
     * @param profilerIn profiler.
     * @param client and if is client.
     */
	public FakeWorld(IBlockState blockState, ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client)
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
	protected boolean isChunkLoaded(int x, int z, boolean allowEmpty)
    {
		 /*
         * Intentionally left empty.
         */
		return false;
	}
	
	@Override
    public TileEntity getTileEntity(BlockPos pos){
    	return null;
    }
	
    @NotNull
    @Override
    public IBlockState getBlockState(BlockPos pos){
    	return this.blockState;
    }
}
