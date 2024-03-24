package com.minecolonies.core.structures;

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
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;

import java.util.List;
import java.util.Optional;

/**
 * Class defining our configured feature - the empty colony that is spawning.
 */
public class EmptyColonyStructure extends Structure
{

    // A custom codec that changes the size limit for our code_structure_sky_fan.json's config to not be capped at 7.
    // With this, we can have a structure with a size limit up to 30 if we want to have extremely long branches of pieces in the structure.
    public static final Codec<EmptyColonyStructure> COLONY_CODEC = RecordCodecBuilder.<EmptyColonyStructure>mapCodec(instance ->
                                                                                                  instance.group(EmptyColonyStructure.settingsCodec(instance),
                                                                                                    StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
                                                                                                    ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(structure -> structure.startJigsawName),
                                                                                                    Codec.intRange(0, 10).fieldOf("size").forGetter(structure -> structure.size),
                                                                                                    HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
                                                                                                    Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(structure -> structure.projectStartToHeightmap),
                                                                                                    Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(structure -> structure.maxDistanceFromCenter),
                                                                                                    Codec.BOOL.optionalFieldOf("allow_cave", false).forGetter(structure -> structure.allowCave),
                                                                                                    Codec.list(PoolAliasBinding.CODEC).optionalFieldOf("pool_aliases", List.of()).forGetter(p_307187_ -> p_307187_.poolAliases)
                                                                                                  ).apply(instance, EmptyColonyStructure::new)).codec();

    private final Holder<StructureTemplatePool> startPool;
    private final Optional<ResourceLocation>    startJigsawName;
    private final int size;
    private final HeightProvider startHeight;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final int maxDistanceFromCenter;
    private final List<PoolAliasBinding> poolAliases;
    private boolean allowCave;

    public EmptyColonyStructure(Structure.StructureSettings config,
      Holder<StructureTemplatePool> startPool,
      Optional<ResourceLocation> startJigsawName,
      int size,
      HeightProvider startHeight,
      Optional<Heightmap.Types> projectStartToHeightmap,
      int maxDistanceFromCenter,
      boolean allowCave,
      List<PoolAliasBinding> poolAliases)
    {
        super(config);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.size = size;
        this.startHeight = startHeight;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
        this.allowCave = allowCave;
        this.poolAliases = poolAliases;
    }

    @Override
    public StructureType<?> type() {
        return MineColoniesStructures.EMPTY_COLONY.get(); // Helps the game know how to turn this structure back to json to save to chunks
    }

    @Override
    public GenerationStep.Decoration step()
    {
        return GenerationStep.Decoration.SURFACE_STRUCTURES;
    }

    private static boolean isFeatureChunk(Structure.GenerationContext context)
    {
        BlockPos blockPos = context.chunkPos().getWorldPosition();

        int landHeight = context.chunkGenerator().getFirstOccupiedHeight(blockPos.getX(), blockPos.getZ(), Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        NoiseColumn columnOfBlocks = context.chunkGenerator().getBaseColumn(blockPos.getX(), blockPos.getZ(), context.heightAccessor(), context.randomState());

        BlockState topBlock = columnOfBlocks.getBlock(landHeight);

        return topBlock.getFluidState().isEmpty() && landHeight < 200;
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context)
    {
        if (allowCave)
        {
            final BlockPos.MutableBlockPos result = isFeatureChunkCave(context);
            if (result != null)
            {
                Optional<GenerationStub> structurePiecesGenerator =
                  JigsawPlacement.addPieces(
                    context,
                    this.startPool,
                    this.startJigsawName,
                    this.size,
                    result,
                    false,
                    this.projectStartToHeightmap,
                    this.maxDistanceFromCenter,
                    PoolAliasLookup.create(this.poolAliases, result, context.seed())
                  );

                if (structurePiecesGenerator.isPresent())
                {
                    Log.getLogger().debug("New Empty colony at" + result);
                }
                return structurePiecesGenerator;
            }
            return Optional.empty();
        }

        if (!isFeatureChunk(context))
        {
            return Optional.empty();
        }

        // Turns the chunk coordinates into actual coordinates we can use. (Gets center of that chunk)
        BlockPos blockpos = context.chunkPos().getMiddleBlockPosition(0);

        int topLandY = context.chunkGenerator().getFirstFreeHeight(blockpos.getX(), blockpos.getZ(), Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        blockpos = blockpos.above(topLandY);

        Optional<Structure.GenerationStub> structurePiecesGenerator =
          JigsawPlacement.addPieces(
            context,
            this.startPool,
            this.startJigsawName,
            this.size,
            blockpos,
            false,
            this.projectStartToHeightmap,
            this.maxDistanceFromCenter,
            PoolAliasLookup.create(this.poolAliases, blockpos, context.seed())
          );

        if (structurePiecesGenerator.isPresent())
        {
            Log.getLogger().debug("New Empty colony at" + blockpos);
        }
        return structurePiecesGenerator;
    }

    private static BlockPos.MutableBlockPos isFeatureChunkCave(GenerationContext context)
    {
        BlockPos blockPos = context.chunkPos().getWorldPosition();
        ChunkPos chunkPos = new ChunkPos(blockPos);

        int currentY = 0;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int i = 0; i < 10; i++)
        {
            currentY += context.random().nextInt(0, 30);
            for (int curChunkX = chunkPos.x - 1; curChunkX <= chunkPos.x + 1; curChunkX++)
            {
                for (int curChunkZ = chunkPos.z - 1; curChunkZ <= chunkPos.z + 1; curChunkZ++)
                {
                    NoiseColumn blockView = context.chunkGenerator().getBaseColumn(mutable.getX(), mutable.getZ(), context.heightAccessor(), context.randomState());
                    mutable.set(curChunkX << 4, currentY, curChunkZ << 4);
                    if (blockView.getBlock(mutable.getY()).isAir())
                    {
                        int airCount = 1;
                        while (mutable.getY() > context.chunkGenerator().getMinY())
                        {
                            BlockState state = blockView.getBlock(mutable.getY());
                            if (state.isAir())
                            {
                                airCount++;
                            }
                            else
                            {
                                break;
                            }
                            mutable.move(Direction.DOWN);
                        }

                        mutable.setY(currentY);
                        while (mutable.getY() < context.chunkGenerator().getMinY() + context.chunkGenerator().getGenDepth())
                        {
                            BlockState state = blockView.getBlock(mutable.getY());
                            if (state.isAir())
                            {
                                airCount++;
                                if (airCount >= 32)
                                {
                                    break;
                                }
                            }
                            else
                            {
                                break;
                            }
                            mutable.move(Direction.UP);
                        }

                        if (airCount >= 32)
                        {
                            return mutable;
                        }
                    }
                }
            }
        }
        return null;
    }
}
