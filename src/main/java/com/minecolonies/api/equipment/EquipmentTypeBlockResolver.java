package com.minecolonies.api.equipment;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.entity.block.IMateriallyTexturedBlockEntity;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Resolves the equipment type and required equipment level for a given block, e.g. pickaxe for stone,
 * axe for logs.
 */
public final class EquipmentTypeBlockResolver
{
    /**
     * Cache of the resolved block requirement for a given block state. Empty when no registered equipment
     * type is correct for the block.
     */
    private static final LoadingCache<BlockState, Optional<BlockRequirement>> REQUIREMENT_CACHE = CacheBuilder.newBuilder()
        .expireAfterAccess(Duration.ofMinutes(10))
        .build(CacheLoader.from(EquipmentTypeBlockResolver::resolveRequirement));

    private EquipmentTypeBlockResolver()
    {
        //Hide default constructor.
    }

    /**
     * Get the equipment type that should be used to mine/interact with a given block, e.g. pickaxe for
     * stone, axe for logs. Resolved once per distinct block state and cached from then on.
     *
     * @param state the block state to resolve the equipment type for.
     * @param level the level the block is in, used to resolve addon blocks whose correct tool depends on
     *              more than just the block state (e.g. Domum Ornamentum's materially-textured blocks).
     * @param pos   the position of the block.
     * @return the equipment type to use, or null if no registered equipment type is correct for this block.
     */
    @Nullable
    public static EquipmentTypeEntry getEquipmentTypeForBlock(final BlockState state, final BlockGetter level, final BlockPos pos)
    {
        return getUnchecked(resolveMaterialState(state, level, pos)).map(BlockRequirement::equipmentType).orElse(null);
    }

    /**
     * Get the equipment level required to harvest drops from a given block. Resolved once per distinct
     * block state and cached from then on.
     *
     * @param state the block state to resolve the required level for.
     * @param level the level the block is in, see {@link #getEquipmentTypeForBlock(BlockState, BlockGetter, BlockPos)}.
     * @param pos   the position of the block.
     * @return the required equipment level, or -1 if no registered equipment type is correct for this block.
     */
    public static int getRequiredEquipmentLevelForBlock(final BlockState state, final BlockGetter level, final BlockPos pos)
    {
        return getUnchecked(resolveMaterialState(state, level, pos)).map(BlockRequirement::requiredLevel).orElse(-1);
    }

    private static Optional<BlockRequirement> getUnchecked(final BlockState state)
    {
        try
        {
            return REQUIREMENT_CACHE.get(state);
        }
        catch (final ExecutionException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static Optional<BlockRequirement> resolveRequirement(final BlockState state)
    {
        for (final EquipmentTypeEntry equipmentType : ModEquipmentTypes.getRegistry())
        {
            if (equipmentType.isCorrectForBlock(state))
            {
                return Optional.of(new BlockRequirement(equipmentType, equipmentType.getRequiredLevelForBlock(state)));
            }
        }
        return Optional.empty();
    }

    /**
     * Resolve the actual material BlockState to check tool-correctness against.
     *
     * @param state the original block state.
     * @param level the level the block is in.
     * @param pos   the position of the block.
     * @return the block state to check tool-correctness against.
     */
    private static BlockState resolveMaterialState(final BlockState state, final BlockGetter level, final BlockPos pos)
    {
        if (state.getBlock() instanceof final IMateriallyTexturedBlock materiallyTexturedBlock && materiallyTexturedBlock.getMainComponent() != null
              && level.getBlockEntity(pos) instanceof final IMateriallyTexturedBlockEntity materiallyTexturedBlockEntity)
        {
            final Block materialBlock = materiallyTexturedBlockEntity.getTextureData().getTexturedComponents().get(materiallyTexturedBlock.getMainComponent().getId());
            if (materialBlock != null)
            {
                return materialBlock.defaultBlockState();
            }
        }

        return state;
    }

    private record BlockRequirement(EquipmentTypeEntry equipmentType, int requiredLevel)
    {
    }
}