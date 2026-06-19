package com.minecolonies.api.util;

import com.ldtteam.structurize.api.RotationMirror;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.storage.ClientFutureProcessor;
import com.ldtteam.structurize.storage.ServerFutureProcessor;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.util.BlockInfo;
import com.ldtteam.structurize.util.IOPool;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.claim.IChunkClaimData;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.minecolonies.api.util.constant.ColonyManagerConstants.NO_COLONY_ID;

/**
 * Contains colony specific utility.
 */
public final class ColonyUtils
{
    /**
     * Private constructor to hide implicit one.
     */
    private ColonyUtils()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Queues a blueprint load to the right side
     *
     * @param world
     * @param structurePack
     * @param structurePath
     * @param afterLoad
     */
    public static CompletableFuture<Blueprint> queueBlueprintLoad(final Level world, final String structurePack, final String structurePath, final Consumer<Blueprint> afterLoad)
    {
        return queueBlueprintLoad(world, structurePack, structurePath, afterLoad, e -> Log.getLogger().warn(e));
    }

    /**
     * Queues a blueprint load to the right side
     *
     * @param world
     * @param structurePack
     * @param structurePath
     * @param afterLoad
     */
    public static CompletableFuture<Blueprint> queueBlueprintLoad(
        final Level world,
        final String structurePack,
        final String structurePath,
        final Consumer<Blueprint> afterLoad,
        final Consumer<String> errorHandler)
    {
        final CompletableFuture<Blueprint> future =
            CompletableFuture.supplyAsync(() -> StructurePacks.getBlueprint(structurePack, structurePath, FMLEnvironment.production, world.registryAccess()), IOPool.getExecutor());
        if (world.isClientSide)
        {
            ClientFutureProcessor.queueBlueprint(new ClientFutureProcessor.BlueprintProcessingData(future,
                (blueprint ->
                {
                    if (blueprint == null)
                    {
                        errorHandler.accept("Couldn't find structure with name: " + structurePack + " in: " + structurePath + ". Aborting loading procedure");
                    }
                    else
                    {
                        afterLoad.accept(blueprint);
                    }
                })));

            return future;
        }
        else
        {
            ServerFutureProcessor.queueBlueprint(new ServerFutureProcessor.BlueprintProcessingData(future, world,
                (blueprint ->
                {
                    if (blueprint == null)
                    {
                        errorHandler.accept("Couldn't find structure with name: " + structurePack + " in: " + structurePath + ". Aborting loading procedure");
                    }
                    else
                    {
                        afterLoad.accept(blueprint);
                    }
                })));

            return future;
        }
    }

    /**
     * Queues a blueprint load to the right side
     *
     * @param world
     * @param structurePack
     * @param structurePath
     * @param currentLevel
     * @param targetLevel
     * @param afterLoad
     */
    public static CompletableFuture<Blueprint> queueBlueprintLoad(
        final Level world,
        final String structurePack,
        final String structurePath,
        final int currentLevel,
        final int targetLevel,
        final Consumer<Blueprint> afterLoad,
        final Consumer<String> errorHandler)
    {
        // just load the single level required in the normal case
        if (currentLevel == targetLevel - 1 || currentLevel == targetLevel || (currentLevel == 1 && targetLevel == 0))
        {
            return queueBlueprintLoad(world, structurePack, structurePath, afterLoad, errorHandler);
        }

        // when building, load max(1, currentLevel) up to targetLevel; when removing, load 1 up to currentLevel.
        final int firstLevel = currentLevel > targetLevel ? 1 : Math.max(1, currentLevel);
        final int lastLevel = Math.max(currentLevel, targetLevel);
        final List<CompletableFuture<Blueprint>> blueprintFutures = new ArrayList<>();
        final List<String> errors = Collections.synchronizedList(new ArrayList<>());
        for (int level = firstLevel; level <= lastLevel; ++level)
        {
            String schemPath = structurePath.replace(".blueprint", "");
            schemPath = schemPath.substring(0, schemPath.length() - 1) + level + ".blueprint";

            blueprintFutures.add(queueBlueprintLoad(world, structurePack, schemPath, b -> {}, errors::add));
        }

        return CompletableFuture.allOf(blueprintFutures.toArray(new CompletableFuture[0]))
            .thenApply(v -> blueprintFutures.stream().map(CompletableFuture::join).filter(Objects::nonNull).toList())
            .thenApplyAsync(ColonyUtils::composeBlueprints, Util.backgroundExecutor())
            .thenApplyAsync(blueprint ->
            {
                if (blueprint != null)
                {
                    afterLoad.accept(blueprint);
                    return blueprint;
                }

                errorHandler.accept(String.join("\n", errors));
                return null;
            }, WorldUtil.getMainThread(world));
    }

    /**
     * Creates a new blueprint in memory from multiple existing blueprints, in a last-wins kind of way.
     * @param blueprints the blueprints, in order from lowest to highest priority.
     * @return the composed blueprint.
     * @implNote Ideally move this to Structurize at some point, so it's easier to keep in sync.
     */
    @Nullable
    private static Blueprint composeBlueprints(@NotNull final List<Blueprint> blueprints)
    {
        if (blueprints.isEmpty())
        {
            return null;
        }

        final Blueprint last = blueprints.getLast();

        final Blueprint combined = new Blueprint((short) last.getSizeX(), (short) last.getSizeY(), (short) last.getSizeZ(), last.getRegistryAccess());
        combined.setPackName(last.getPackName());
        combined.setName(last.getName());
        combined.setFileName(last.getFileName());
        combined.setFilePath(last.getFilePath());
        combined.setCachePrimaryOffset(last.getPrimaryBlockOffset());
        combined.setArchitects(last.getArchitects());
        combined.setEntities(last.getEntities());   // todo something smarter?

        for (final BlockPos pos : BlockPos.betweenClosed(BlockPos.ZERO, new BlockPos(last.getSizeX() - 1, last.getSizeY() - 1, last.getSizeZ() - 1)))
        {
            int blueprintIndex = blueprints.size() - 1;
            BlockInfo block = last.getBlockInfoAsMap().get(pos);
            while (block.getState().is(ModBlocks.blockSubstitution) && blueprintIndex > 0)
            {
                --blueprintIndex;
                block = blueprints.get(blueprintIndex).getBlockInfoAsMap().get(pos);
            }

            combined.addBlockState(pos, block.getState());
            if (block.hasTileEntityData())
            {
                combined.getTileEntities()[pos.getY()][pos.getZ()][pos.getX()] = block.getTileEntityData();
            }
        }

        return combined;
    }

    /**
     * Calculated the corner of a building.  Also rotates the blueprint accordingly.
     *
     * @param pos        the central position.
     * @param world      the world.
     * @param blueprint  the structureWrapper.
     * @param rotMir     the rotation and mirror.
     * @return a tuple with the required corners.
     */
    public static Tuple<BlockPos, BlockPos> calculateCorners(
      final BlockPos pos,
      final Level world,
      final Blueprint blueprint,
      final RotationMirror rotMir)
    {
        if (blueprint == null)
        {
            return new Tuple<>(pos, pos);
        }

        blueprint.setRotationMirror(rotMir, world);
        final BlockPos zeroPos = pos.subtract(blueprint.getPrimaryBlockOffset());

        final BlockPos pos1 = new BlockPos(zeroPos.getX(), zeroPos.getY(), zeroPos.getZ());
        final BlockPos pos2 = new BlockPos(zeroPos.getX() + blueprint.getSizeX() - 1, zeroPos.getY() + blueprint.getSizeY() - 1, zeroPos.getZ() + blueprint.getSizeZ() - 1);

        return new Tuple<>(pos1, pos2);
    }

    /**
     * Reports the block corners from a bounding box.
     *
     * @param box the bounding box.
     * @return the corners.
     */
    public static Tuple<BlockPos, BlockPos> calculateCorners(@NotNull final AABB box)
    {
        final BlockPos min = BlockPos.containing(box.minX, box.minY, box.minZ);
        final BlockPos max = BlockPos.containing(box.maxX, box.maxY, box.maxZ);
        return new Tuple<>(min, max);
    }

    /**
     * Get the owning colony from a chunk.
     * @param chunk the chunk to check.
     * @return the colony id.
     */
    public static int getOwningColony(final ChunkAccess chunk)
    {
        final IChunkClaimData cap = IColonyManager.getInstance().getClaimData(chunk.getLevel().dimension(), chunk.getPos());
        return cap == null ? NO_COLONY_ID : cap.getOwningColony();
    }

    /**
     * Get all claiming buildings from the chunk.
     * @param chunk the chunk they are at.
     * @return the map from colony to building claims.
     */
    public static Map<Integer, Set<BlockPos>> getAllClaimingBuildings(final ChunkAccess chunk)
    {
        final IChunkClaimData cap = IColonyManager.getInstance().getClaimData(chunk.getLevel().dimension(), chunk.getPos());
        return cap == null ? new HashMap<>() : cap.getAllClaimingBuildings();
    }

    /**
     * Get all static claims from a chunk.
     * @param chunk the chunk to get it from.
     * @return the list.
     */
    public static List<Integer> getStaticClaims(final ChunkAccess chunk)
    {
        final IChunkClaimData cap = IColonyManager.getInstance().getClaimData(chunk.getLevel().dimension(), chunk.getPos());
        return cap == null ? new ArrayList<>() : cap.getStaticClaimColonies();
    }

    /**
     * Get comprehensive chunk ownership data.
     * @param chunk the chunk to get it from.
     * @return the ownership data, or null.
     */
    @Nullable
    public static ChunkCapData getChunkCapData(final ChunkAccess chunk)
    {
        final IChunkClaimData cap = IColonyManager.getInstance().getClaimData(chunk.getLevel().dimension(), chunk.getPos());
        return cap == null ? new ChunkCapData(chunk.getPos().x, chunk.getPos().z) : new ChunkCapData(chunk.getPos().x, chunk.getPos().z, cap.getOwningColony(), cap.getStaticClaimColonies(), cap.getAllClaimingBuildings());
    }
}
