package com.minecolonies.api.util;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.storage.ClientFutureProcessor;
import com.ldtteam.structurize.storage.ServerFutureProcessor;
import com.ldtteam.structurize.storage.StructurePacks;
import com.minecolonies.api.colony.IColonyTagCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.minecolonies.api.colony.IColony.CLOSE_COLONY_CAP;
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
        if (world.isClientSide)
        {
            final CompletableFuture<Blueprint> future = StructurePacks.getBlueprintFuture(structurePack, structurePath);
            ClientFutureProcessor.queueBlueprint(new ClientFutureProcessor.BlueprintProcessingData(future,
                (blueprint ->
                {
                    if (blueprint == null)
                    {
                        Log.getLogger().warn("Couldn't find structure with name: " + structurePack + " in: " + structurePath + ". Aborting loading procedure");
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
            final CompletableFuture<Blueprint> future = StructurePacks.getBlueprintFuture(structurePack, structurePath);
            ServerFutureProcessor.queueBlueprint(new ServerFutureProcessor.BlueprintProcessingData(future, world,
                (blueprint ->
                {
                    if (blueprint == null)
                    {
                        Log.getLogger().warn("Couldn't find structure with name: " + structurePack + " in: " + structurePath + ". Aborting loading procedure");
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
     * Calculated the corner of a building.
     *
     * @param pos        the central position.
     * @param world      the world.
     * @param blueprint  the structureWrapper.
     * @param rotation   the rotation.
     * @param isMirrored if its mirrored.
     * @return a tuple with the required corners.
     */
    public static Tuple<BlockPos, BlockPos> calculateCorners(
      final BlockPos pos,
      final Level world,
      final Blueprint blueprint,
      final int rotation,
      final boolean isMirrored)
    {
        if (blueprint == null)
        {
            return new Tuple<>(pos, pos);
        }

        blueprint.rotateWithMirror(BlockPosUtil.getRotationFromRotations(rotation), isMirrored ? Mirror.FRONT_BACK : Mirror.NONE, world);
        final BlockPos zeroPos = pos.subtract(blueprint.getPrimaryBlockOffset());

        final BlockPos pos1 = new BlockPos(zeroPos.getX(), zeroPos.getY(), zeroPos.getZ());
        final BlockPos pos2 = new BlockPos(zeroPos.getX() + blueprint.getSizeX() - 1, zeroPos.getY() + blueprint.getSizeY() - 1, zeroPos.getZ() + blueprint.getSizeZ() - 1);

        return new Tuple<>(pos1, pos2);
    }

    /**
     * Get the owning colony from a chunk.
     * @param chunk the chunk to check.
     * @return the colony id.
     */
    public static int getOwningColony(final LevelChunk chunk)
    {
        final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null).resolve().orElse(null);
        return cap == null ? NO_COLONY_ID : cap.getOwningColony();
    }

    /**
     * Get all claiming buildings from the chunk.
     * @param chunk the chunk they are at.
     * @return the map from colony to building claims.
     */
    public static Map<Integer, Set<BlockPos>> getAllClaimingBuildings(final LevelChunk chunk)
    {
        final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null).resolve().orElse(null);
        return cap == null ? new HashMap<>() : cap.getAllClaimingBuildings();
    }

    /**
     * Get all static claims from a chunk.
     * @param chunk the chunk to get it from.
     * @return the list.
     */
    public static List<Integer> getStaticClaims(final LevelChunk chunk)
    {
        final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null).resolve().orElse(null);
        return cap == null ? new ArrayList<>() : cap.getStaticClaimColonies();
    }

    /**
     * Get comprehensive chunk ownership data.
     * @param chunk the chunk to get it from.
     * @return the ownership data, or null.
     */
    @Nullable
    public static ChunkCapData getChunkCapData(final LevelChunk chunk)
    {
        final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null).resolve().orElse(null);
        return cap == null ? new ChunkCapData(chunk.getPos().x, chunk.getPos().z) : new ChunkCapData(chunk.getPos().x, chunk.getPos().z, cap.getOwningColony(), cap.getStaticClaimColonies(), cap.getAllClaimingBuildings());
    }
}
