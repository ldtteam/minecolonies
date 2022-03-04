package com.minecolonies.coremod.structures;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.util.Log;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.PostPlacementProcessor;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraftforge.event.world.StructureSpawnListGatherEvent;

import java.util.List;
import java.util.Optional;

/**
 * Class defining our configured feature - the empty colony that is spawning.
 */
public class EmptyColonyStructure extends StructureFeature<JigsawConfiguration>
{
    public EmptyColonyStructure(Codec<JigsawConfiguration> codec)
    {
        super(codec, (context) -> {
              // Check if the spot is valid for structure gen. If false, return nothing to signal to the game to skip this spawn attempt.
              if (!EmptyColonyStructure.isFeatureChunk(context))
              {
                  return Optional.empty();
              }
              // Create the pieces layout of the structure and give it to
              else
              {
                  return EmptyColonyStructure.createPiecesGenerator(context);
              }
          },
          PostPlacementProcessor.NONE);
    }

    @Override
    public GenerationStep.Decoration step()
    {
        return GenerationStep.Decoration.SURFACE_STRUCTURES;
    }

    /**
     * Potential entities to spawn in the structure with quantities.
     */
    private static final List<MobSpawnSettings.SpawnerData> STRUCTURE_CREATURES = ImmutableList.of(
      new MobSpawnSettings.SpawnerData(EntityType.SHEEP, 30, 2, 10),
      new MobSpawnSettings.SpawnerData(EntityType.COW, 30, 2, 10),
      new MobSpawnSettings.SpawnerData(EntityType.CAT, 100, 1, 2)
    );

    public static void setupStructureSpawns(final StructureSpawnListGatherEvent event)
    {
        if (event.getStructure() == MineColoniesStructures.EMPTY_COLONY.get())
        {
            event.addEntitySpawns(MobCategory.CREATURE, STRUCTURE_CREATURES);
        }
    }

    private static boolean isFeatureChunk(PieceGeneratorSupplier.Context<JigsawConfiguration> context)
    {
        BlockPos blockPos = context.chunkPos().getWorldPosition();

        int landHeight = context.chunkGenerator().getFirstOccupiedHeight(blockPos.getX(), blockPos.getZ(), Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor());
        NoiseColumn columnOfBlocks = context.chunkGenerator().getBaseColumn(blockPos.getX(), blockPos.getZ(), context.heightAccessor());

        BlockState topBlock = columnOfBlocks.getBlock(landHeight);

        return topBlock.getFluidState().isEmpty() && landHeight < 200;
    }

    public static Optional<PieceGenerator<JigsawConfiguration>> createPiecesGenerator(PieceGeneratorSupplier.Context<JigsawConfiguration> context)
    {
        if (!isFeatureChunk(context))
        {
            return Optional.empty();
        }

        // Turns the chunk coordinates into actual coordinates we can use. (Gets center of that chunk)
        BlockPos blockpos = context.chunkPos().getMiddleBlockPosition(0);

        context.config().maxDepth = 10;

        Optional<PieceGenerator<JigsawConfiguration>> structurePiecesGenerator =
          JigsawPlacement.addPieces(
            context,
            PoolElementStructurePiece::new,
            blockpos,
            false,
            false
          );

        if (structurePiecesGenerator.isPresent())
        {
            Log.getLogger().debug("New Empty colony at" + blockpos);
        }
        return structurePiecesGenerator;
    }
}
