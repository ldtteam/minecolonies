package com.schematica.client.world.chunk;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Objects;
import com.schematica.client.world.SchematicWorld;

import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunkProvider;

public class ChunkProviderSchematic extends ChunkProviderClient implements IChunkProvider
{
    private final SchematicWorld world;
    private final Chunk          emptyChunk;
    private final Map<Long, ChunkSchematic> chunks = new ConcurrentHashMap<>();

    public ChunkProviderSchematic(final SchematicWorld world)
    {
        super(world);
        this.world = world;
        this.emptyChunk = new EmptyChunk(world, 0, 0) {
            @Override
            public boolean isEmpty() {
                return false;
            }
        };
    }

    private boolean chunkExists(final int x, final int z) {
        return x >= 0 && z >= 0 && x < this.world.getWidth() && z < this.world.getLength();
    }

    @Override
    public Chunk getLoadedChunk(final int x, final int z) {
        if (!chunkExists(x, z)) {
            return this.emptyChunk;
        }

        final long key = ChunkPos.chunkXZ2Int(x, z);

        ChunkSchematic chunk = this.chunks.get(key);
        if (chunk == null) {
            chunk = new ChunkSchematic(this.world, x, z);
            this.chunks.put(key, chunk);
        }

        return chunk;
    }

    @Override
    public Chunk provideChunk(final int x, final int z) {
        return getLoadedChunk(x, z);
    }

    @Override
    public boolean unloadQueuedChunks() {
        return false;
    }

    @Override
    public String makeString()
    {
        return "SchematicChunkCache";
    }

    // ChunkProviderClient
    @Override
    public Chunk loadChunk(int x, int z) {
        return Objects.firstNonNull(getLoadedChunk(x, z), this.emptyChunk);
    }

    // ChunkProviderClient
    @Override
    public void unloadChunk(int x, int z) {
        // NOOP: schematic chunks are part of the schematic world and are never unloaded separately
    }
}
