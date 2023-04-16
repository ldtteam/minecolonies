package com.minecolonies.coremod.structures;

import com.minecolonies.api.util.Log;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.util.Optional;

/**
 * Class defining our configured feature - the empty colony that is spawning.
 */
public class EmptyNetherColonyStructure extends Structure
{

    // A custom codec that changes the size limit for our code_structure_sky_fan.json's config to not be capped at 7.
    // With this, we can have a structure with a size limit up to 30 if we want to have extremely long branches of pieces in the structure.
    public static final Codec<EmptyNetherColonyStructure> NETHER_COLONY_CODEC = RecordCodecBuilder.<EmptyNetherColonyStructure>mapCodec(instance ->
                                                                                                  instance.group(EmptyNetherColonyStructure.settingsCodec(instance),
                                                                                                    StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
                                                                                                    ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(structure -> structure.startJigsawName),
                                                                                                    Codec.intRange(0, 10).fieldOf("size").forGetter(structure -> structure.size),
                                                                                                    HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
                                                                                                    Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(structure -> structure.projectStartToHeightmap),
                                                                                                    Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(structure -> structure.maxDistanceFromCenter)
                                                                                                  ).apply(instance, EmptyNetherColonyStructure::new)).codec();

    private final Holder<StructureTemplatePool> startPool;
    private final Optional<ResourceLocation>    startJigsawName;
    private final int size;
    private final HeightProvider startHeight;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final int maxDistanceFromCenter;

    public EmptyNetherColonyStructure(
      StructureSettings config,
      Holder<StructureTemplatePool> startPool,
      Optional<ResourceLocation> startJigsawName,
      int size,
      HeightProvider startHeight,
      Optional<Heightmap.Types> projectStartToHeightmap,
      int maxDistanceFromCenter)
    {
        super(config);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.size = size;
        this.startHeight = startHeight;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
    }

    @Override
    public StructureType<?> type() {
        return MineColoniesStructures.EMPTY_NETHER_COLONY.get(); // Helps the game know how to turn this structure back to json to save to chunks
    }

    @Override
    public GenerationStep.Decoration step()
    {
        return GenerationStep.Decoration.SURFACE_STRUCTURES;
    }

    private static BlockPos.MutableBlockPos isFeatureChunk(GenerationContext context)
    {
        BlockPos blockPos = context.chunkPos().getWorldPosition();
        ChunkPos chunkPos = new ChunkPos(blockPos);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int curChunkX = chunkPos.x - 1; curChunkX <= chunkPos.x + 1; curChunkX++)
        {
            for (int curChunkZ = chunkPos.z - 1; curChunkZ <= chunkPos.z + 1; curChunkZ++)
            {
                mutable.set(curChunkX << 4, context.chunkGenerator().getSeaLevel() + 10, curChunkZ << 4);
                NoiseColumn blockView = context.chunkGenerator().getBaseColumn(mutable.getX(), mutable.getZ(), context.heightAccessor(), context.randomState());
                int minValidSpace = 65;
                int maxHeight = Math.min(context.chunkGenerator().getMinY() + context.chunkGenerator().getGenDepth(), context.chunkGenerator().getSeaLevel() + minValidSpace);

                while (mutable.getY() < maxHeight)
                {
                    BlockState state = blockView.getBlock(mutable.getY());
                    if (!state.isAir())
                    {
                        return null;
                    }
                    mutable.move(Direction.UP);
                }
            }
        }

        return mutable;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context)
    {
        final BlockPos.MutableBlockPos result = isFeatureChunk(context);
        if (result == null)
        {
            return Optional.empty();
        }

        Optional<GenerationStub> structurePiecesGenerator =
          JigsawPlacement.addPieces(
            context,
            this.startPool,
            this.startJigsawName,
            this.size,
            result,
            false,
            this.projectStartToHeightmap,
            this.maxDistanceFromCenter
          );

        if (structurePiecesGenerator.isPresent())
        {
            Log.getLogger().debug("New Empty colony at" + result);
        }
        return structurePiecesGenerator;
    }
}
