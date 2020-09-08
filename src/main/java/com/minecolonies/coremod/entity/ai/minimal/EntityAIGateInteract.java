package com.minecolonies.coremod.entity.ai.minimal;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.CompatibilityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.Constants.HALF_BLOCK;

/**
 * Used for gate interaction for the citizens.
 */
public class EntityAIGateInteract extends Goal
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
     * Our citizen.
     */
    protected MobEntity theEntity;

    /**
     * The gate position.
     */
    protected BlockPos gatePosition;

    /**
     * The gate block.
     */
    @Nullable
    protected FenceGateBlock gateBlock;

    /**
     * Check if the interaction with the fenceGate stopped already.
     */
    private boolean hasStoppedFenceInteraction;

    /**
     * The entities x position.
     */
    private double entityPositionX;

    /**
     * The entities z position.
     */
    private double entityPositionZ;

    /**
     * The direction the entity goes through the gate.
     */
    private Direction direction;

    /**
     * Constructor called to register the AI class with an entity.
     *
     * @param entityIn the registering entity.
     */
    public EntityAIGateInteract(@NotNull final MobEntity entityIn)
    {
        super();
        this.gatePosition = BlockPos.ZERO;
        this.theEntity = entityIn;
        if (!(entityIn.getNavigator() instanceof GroundPathNavigator))
        {
            throw new IllegalArgumentException("Unsupported mob type for DoorInteractGoal");
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
        return this.theEntity.collidedHorizontally && checkPath();
    }

    /**
     * Checks if there exists a path.
     *
     * @return true if the fence gate can be passed.
     */
    private boolean checkPath()
    {
        @NotNull final GroundPathNavigator pathnavigateground = (GroundPathNavigator) this.theEntity.getNavigator();
        final Path path = pathnavigateground.getPath();
        return path != null && !path.isFinished() && pathnavigateground.getEnterDoors() && checkFenceGate(path);
    }

    /**
     * Checks if the citizen is close enough to an existing fence gate.
     *
     * @param path the path through the fence.
     * @return true if the gate can be passed
     */
    private boolean checkFenceGate(@NotNull final Path path)
    {
        final int maxLengthToCheck = Math.min(path.getCurrentPathIndex() + LENGTH_TO_CHECK, path.getCurrentPathLength());
        for (int i = Math.max(0, path.getCurrentPathIndex() - 1); i < maxLengthToCheck; ++i)
        {
            final PathPoint pathpoint = path.getPathPointFromIndex(i);
            for (int level = 0; level < HEIGHT_TO_CHECK; level++)
            {
                this.gatePosition = new BlockPos(pathpoint.x, pathpoint.y + level, pathpoint.z);
                if (this.theEntity.getDistanceSq((double) this.gatePosition.getX(), this.theEntity.getPosY(), (double) this.gatePosition.getZ()) <= MIN_DISTANCE)
                {
                    this.gateBlock = this.getBlockFence(this.gatePosition);
                    if (this.gateBlock != null)
                    {
                        if (i > 0)
                        {
                            final PathPoint prevPathPoint = path.getPathPointFromIndex(i-1);
                            direction = BlockPosUtil.getFacing(new BlockPos(pathpoint.x, 0, pathpoint.z), new BlockPos(prevPathPoint.x, 0, prevPathPoint.z));
                        }
                        return true;
                    }
                }
            }
        }

        this.gatePosition = (new BlockPos(this.theEntity.getPositionVec())).up();
        this.gateBlock = this.getBlockFence(this.gatePosition);
        return this.gateBlock != null;
    }

    /**
     * Returns a fenceBlock if available.
     *
     * @param pos the position to be searched.
     * @return fenceBlock or null.
     */
    private FenceGateBlock getBlockFence(@NotNull final BlockPos pos)
    {
        final BlockState blockState = CompatibilityUtils.getWorldFromEntity(this.theEntity).getBlockState(pos);
        Block block = blockState.getBlock();
        if (!(block instanceof FenceGateBlock && blockState.getMaterial() == Material.WOOD))
        {
            block = CompatibilityUtils.getWorldFromEntity(this.theEntity).getBlockState(new BlockPos(this.theEntity.getPositionVec())).getBlock();
            gatePosition = new BlockPos(this.theEntity.getPositionVec());
        }
        return block instanceof FenceGateBlock && blockState.getMaterial() == Material.WOOD ? (FenceGateBlock) block : null;
    }

    /**
     * Checks if the execution is still ongoing.
     *
     * @return true or false.
     */
    @Override
    public boolean shouldContinueExecuting()
    {
        return !this.hasStoppedFenceInteraction && direction != null;
    }

    /**
     * Starts the execution.
     */
    @Override
    public void startExecuting()
    {
        this.hasStoppedFenceInteraction = false;
        this.entityPositionX = this.gatePosition.getX() + HALF_BLOCK - this.theEntity.getPosX();
        this.entityPositionZ = this.gatePosition.getZ() + HALF_BLOCK - this.theEntity.getPosZ();
    }

    /**
     * Updates the task and checks if the citizen passed the gate already.
     */
    @Override
    public void tick()
    {
        if (direction != null)
        {
            final double entityDistX = Math.abs(this.gatePosition.getX() + HALF_BLOCK - this.theEntity.getPosX() + direction.getXOffset());
            final double entityDistZ = Math.abs(this.gatePosition.getZ() + HALF_BLOCK - this.theEntity.getPosZ() + direction.getZOffset());
            if ((entityDistX > HALF_BLOCK && entityDistX < 1.0) || (entityDistZ > HALF_BLOCK && entityDistZ < 1.0))
            {
                this.hasStoppedFenceInteraction = true;
            }
        }
    }
}
