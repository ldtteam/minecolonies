package com.schematica.client.world.chunk;

import com.schematica.client.world.SchematicWorld;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunkProvider;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkProviderSchematic implements IChunkProvider
{
    private final SchematicWorld world;
    private final Chunk emptyChunk;
    private final Map<Long, ChunkSchematic> chunks = new ConcurrentHashMap<>();

    public ChunkProviderSchematic(final SchematicWorld world)
    {
        this.world = world;
        this.emptyChunk = new EmptyChunk(world, 0, 0);
    }

    @Override
    public boolean chunkExists(final int x, final int z)
    {
        return x >= 0 && z >= 0 && x < this.world.getWidth() && z < this.world.getLength();
    }

    @Override
    public Chunk provideChunk(final int x, final int z)
    {
        if (chunkExists(x, z))
        {
            final long key = ChunkCoordIntPair.chunkXZ2Int(x, z);

            ChunkSchematic chunk = this.chunks.get(key);
            if (chunk == null)
            {
                chunk = new ChunkSchematic(this.world, x, z);
                this.chunks.put(key, chunk);
            }

            return chunk;
        }

        return this.emptyChunk;
    }

    @Override
    public Chunk provideChunk(final BlockPos pos)
    {
        return provideChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    @Override
    public void populate(final IChunkProvider provider, final int x, final int z)
    {
    }

    @Override
    public boolean populateChunk(final IChunkProvider chunkProvider, final Chunk chunk, final int x, final int z)
    {
        return false;
    }

    @Override
    public boolean saveChunks(final boolean saveExtra, final IProgressUpdate progressUpdate)
    {
        return true;
    }

    @Override
    public boolean unloadQueuedChunks()
    {
        return false;
    }

    @Override
    public boolean canSave()
    {
        return false;
    }

    @Override
    public String makeString()
    {
        return "SchematicChunkCache";
    }

    @Override
    public List<BiomeGenBase.SpawnListEntry> getPossibleCreatures(final EnumCreatureType creatureType, final BlockPos pos)
    {
        return null;
    }

    @Override
    public BlockPos getStrongholdGen(final World world, final String name, final BlockPos pos)
    {
        return null;
    }

    @Override
    public int getLoadedChunkCount()
    {
        return this.world.getWidth() * this.world.getLength();
    }

    @Override
    public void recreateStructures(final Chunk chunk, final int x, final int z)
    {
    }

    @Override
    public void saveExtraData()
    {
    }
}
