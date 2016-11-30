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

public class FakeWorld extends World{
	
	private IBlockState blockState;
	
	public FakeWorld(IBlockState blockState, ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client){
		super(saveHandlerIn, info, providerIn, profilerIn, client);
		this.blockState = blockState;
	}

	@Override
	protected IChunkProvider createChunkProvider() {
		//Not needed
		return null;
	}

	@Override
	protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
		//Not needed
		return false;
	}
	
	@Override
    public TileEntity getTileEntity(BlockPos pos){
    	return null;
    }
	
    public IBlockState getBlockState(BlockPos pos){
    	return this.blockState;
    }
}
