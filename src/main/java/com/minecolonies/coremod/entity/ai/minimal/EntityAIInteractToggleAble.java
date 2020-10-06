package com.minecolonies.coremod.entity.ai.minimal;

import net.minecraft.block.*;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

/**
 * AI Task for toggling blocks open/closed when collided
 */
public class EntityAIInteractToggleAble extends Goal
{
    /**
     * Number of blocks to check for the fence gate - height.
     */
    private static final int HEIGHT_TO_CHECK = 2;

    /**
     * Number of blocks to check for the fence gate - length.
     */
    private static final int LENGTH_TO_CHECK = 2;

    /**
     * The min distance the gate has to be from the citizen.
     */
    private static final double MIN_DISTANCE = 2.25D;

    /**
     * Default predicate for all 3 vanilla blocks
     */
    public static final Predicate<BlockState> GATE_DOOR_TRAP = new Predicate<BlockState>()
    {
        @Override
        public boolean test(final BlockState state)
        {
            final Block block = state.getBlock();
            return block instanceof DoorBlock || block instanceof FenceGateBlock || block instanceof TrapDoorBlock;
        }
    };

    /**
     * Only trapdoors and fence gates
     */
    public static final Predicate<BlockState> GATE_TRAP = new Predicate<BlockState>()
    {
        @Override
        public boolean test(final BlockState state)
        {
            final Block block = state.getBlock();
            return block instanceof DoorBlock || block instanceof FenceGateBlock || block instanceof TrapDoorBlock;
        }
    };

    /**
     * Our citizen.
     */
    protected MobEntity entity;

    /**
     * Map of positions and initial state
     */
    private Map<BlockPos, Boolean> toggleAblePositions = new HashMap<>();

    /**
     * Predice to check which blocks we interact with
     */
    private final Predicate<BlockState> useableBlocksPredicate;

    /**
     * Update timer while active, delay between toggle actions
     */
    private int updateTimer = 10;

    /**
     * Execution timer for occasionally checking for toggleables
     */
    private int executeTimerSlow = 100;

    public EntityAIInteractToggleAble(@NotNull final MobEntity entityIn, Predicate<BlockState> useableBlocksPredicate)
    {
        super();
        this.entity = entityIn;
        this.useableBlocksPredicate = useableBlocksPredicate;
        if (!(entityIn.getNavigator() instanceof GroundPathNavigator))
        {
            throw new IllegalArgumentException("Unsupported mob type for EntityAIInteractToggleAble");
        }
    }

    /**
     * Checks if the Interaction should be executed.
     *
     * @return true or false depending on the conditions.
     */
    @Override
    public boolean shouldExecute()
    {
        // Reactive check for detected collisions
        if (this.entity.collidedHorizontally || entity.collidedVertically && !entity.onGround)
        {
            return checkPath();
        }

        // Occasional checks for current path, collisions do not cover all cases
        if (executeTimerSlow-- <= 0)
        {
            executeTimerSlow = 100;
            return checkPath();
        }

        return false;
    }

    /**
     * Checks if there exists a path.
     *
     * @return true if the fence gate can be passed.
     */
    private boolean checkPath()
    {
        @NotNull final GroundPathNavigator pathnavigateground = (GroundPathNavigator) this.entity.getNavigator();
        final Path path = pathnavigateground.getPath();
        checkPathBlocks(path);
        return !toggleAblePositions.isEmpty();
    }

    /**
     * Checks if the citizen is close enough to an existing fence gate.
     *
     * @param path the path through the fence.
     * @return true if the gate can be passed
     */
    private void checkPathBlocks(final Path path)
    {
        if (path == null || path.isFinished())
        {
            resetAll();
            return;
        }

        final int maxLengthToCheck = Math.min(path.getCurrentPathIndex() + LENGTH_TO_CHECK, path.getCurrentPathLength());
        for (int i = Math.max(0, path.getCurrentPathIndex() - 1); i < maxLengthToCheck; ++i)
        {
            final PathPoint pathpoint = path.getPathPointFromIndex(i);
            for (int level = 0; level < HEIGHT_TO_CHECK; level++)
            {
                BlockPos pos = new BlockPos(pathpoint.x, pathpoint.y + level, pathpoint.z);
                BlockState state = entity.world.getBlockState(pos);
                if (this.entity.getDistanceSq(pos.getX(), this.entity.posY, pos.getZ()) <= MIN_DISTANCE && isValidBlockState(state))
                {
                    toggleAblePositions.put(pos, state.get(BlockStateProperties.OPEN));
                }
            }
        }
    }

    /**
     * Checks if the execution is still ongoing.
     *
     * @return true or false.
     */
    @Override
    public boolean shouldContinueExecuting()
    {
        return !toggleAblePositions.isEmpty();
    }

    /**
     * Resets all positions to closed
     */
    private void resetAll()
    {
        for (final BlockPos pos : toggleAblePositions.keySet())
        {
            if (isValidBlockState(entity.world.getBlockState(pos)))
            {
                entity.world.setBlockState(pos, entity.world.getBlockState(pos).with(BlockStateProperties.OPEN, false));
            }
        }
        toggleAblePositions.clear();
    }

    /**
     * Test whether the given blockstate is a valid toggle block
     *
     * @param state to test
     * @return true if valid
     */
    private boolean isValidBlockState(final BlockState state)
    {
        return state.getBlock() != Blocks.AIR
                 && useableBlocksPredicate.test(state)
                 && state.has(BlockStateProperties.OPEN);
    }

    /**
     * Updates the task and checks if the citizen passed the gate already.
     */
    @Override
    public void tick()
    {
        if (--updateTimer >= 0)
        {
            return;
        }
        updateTimer = 10;

        if (!checkPath())
        {
            return;
        }

        final Iterator<BlockPos> it = toggleAblePositions.keySet().iterator();
        final List<BlockPos> posList = new ArrayList<>();

        while (it.hasNext())
        {
            final BlockPos pos = it.next();
            final BlockState state = entity.world.getBlockState(pos);

            // Recheck validity maybe the block changed
            if (!isValidBlockState(state))
            {
                it.remove();
                continue;
            }

            if (this.entity.getDistanceSq(pos.getX(), this.entity.posY, pos.getZ()) > MIN_DISTANCE)
            {
                it.remove();
                entity.world.setBlockState(pos, entity.world.getBlockState(pos).with(BlockStateProperties.OPEN, false));
                continue;
            }

            posList.add(pos);
        }

        if (!posList.isEmpty())
        {
            final BlockPos chosen = posList.get(entity.world.rand.nextInt(posList.size()));
            {
                entity.world.setBlockState(chosen, entity.world.getBlockState(chosen).cycle(BlockStateProperties.OPEN));
            }
        }
    }
}
