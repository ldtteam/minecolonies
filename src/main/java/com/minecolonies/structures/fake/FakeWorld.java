package com.minecolonies.structures.fake;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds a fake world.
 */
public class FakeWorld extends World
{

    /**
     * Map of all states.
     */
    private final Map<BlockPos, IBlockState> stateHashMap  = new HashMap<>();
    private final Map<BlockPos, TileEntity>  entityHashMap = new HashMap<>();

    private final IBlockState blockState;
    private final TileEntity  entity;
    private final boolean     simulateWorld;

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
        this.entity = null;
        simulateWorld = false;
    }

    /**
     * Creates a fake world.
     *
     * @param blockState    with the blockState.
     * @param saveHandlerIn a saveHandler.
     * @param info          additional info.
     * @param providerIn    worldProvider.
     * @param profilerIn    profiler.
     * @param client        and if is client.
     * @param entity        the tileEntity.
     * @param simulateWorld if it is required to simulate the world.
     */
    public FakeWorld(
                      final IBlockState blockState,
                      final ISaveHandler saveHandlerIn,
                      final WorldInfo info,
                      final WorldProvider providerIn,
                      final Profiler profilerIn,
                      final boolean client,
                      final TileEntity entity,
                      final boolean simulateWorld)
    {
        super(saveHandlerIn, info, providerIn, profilerIn, client);
        this.blockState = blockState;
        this.entity = entity;
        this.simulateWorld = simulateWorld;
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

    @Override
    public boolean setBlockState(final BlockPos pos, final IBlockState state)
    {
        stateHashMap.put(pos, state);
        return true;
    }

    @NotNull
    @Override
    public IBlockState getBlockState(final BlockPos pos)
    {
        if (simulateWorld)
        {
            if (stateHashMap.containsKey(pos))
            {
                return stateHashMap.get(pos);
            }
            return Blocks.AIR.getDefaultState();
        }
        return this.blockState;
    }

    @Override
    public TileEntity getTileEntity(final BlockPos pos)
    {
        if (simulateWorld)
        {
            return entityHashMap.get(pos);
        }
        return entity;
    }

    @Override
    public void setTileEntity(final BlockPos pos, @Nullable final TileEntity tileEntityIn)
    {
        if (tileEntityIn != null)
        {
            entityHashMap.put(pos, tileEntityIn);
            tileEntityIn.setWorld(this);
        }
    }
}
