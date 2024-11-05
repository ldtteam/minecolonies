package com.minecolonies.core.entity.ai.minimal;

import com.minecolonies.api.entity.other.AbstractFastMinecoloniesEntity;
import com.minecolonies.api.entity.other.MinecoloniesMinecart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.NotNull;

/**
 * AI to use lever to toggle the direction of a rail.
 */
public class EntityAIUseLeverAtTCrossRailway extends Goal
{
    /**
     * Our citizen.
     */
    protected AbstractFastMinecoloniesEntity entity;
    public EntityAIUseLeverAtTCrossRailway(@NotNull final AbstractFastMinecoloniesEntity entityIn)
    {
        super();
        this.entity = entityIn;
    }


    @Override
    public boolean canUse() {
        return isRidingMinecart();
    }


    @Override
    public void start()
    {
        super.start();
    }

    @Override
    public boolean canContinueToUse()
    {
        return true;
    }
    @Override
    public void tick()
    {
        if (!isRidingMinecart())
        {
            return;
        }

        Path path = entity.getNavigation().getPath();

        if (path == null || path.isDone())
        {
            return;
        }
        if (path.getNextNodeIndex() >= path.getNodeCount() - 1)
        {
            return;
        }
        Node currentNode = path.getNode(path.getNextNodeIndex());
        BlockPos currentPos = new BlockPos(currentNode.x, currentNode.y, currentNode.z);
        BlockState state = entity.level().getBlockState(currentPos);

        if (!(state.getBlock() instanceof RailBlock))
        {
            return;
        }

        Node nextNode = path.getNode(path.getNextNodeIndex() + 1);
        // Check if the x & z of the rail exit equals the next node
        MinecoloniesMinecart cart = (MinecoloniesMinecart) entity.getVehicle();
        if(cart == null)
        {
            return;
        }
        Direction motion = cart.getMotionDirection();
        switch (state.getValue(RailBlock.SHAPE)){
            case RailShape.SOUTH_EAST -> {
                if (motion == Direction.WEST) motion = Direction.SOUTH;
                if (motion == Direction.NORTH) motion = Direction.EAST;
            }
            case RailShape.SOUTH_WEST -> {
                if (motion == Direction.EAST) motion = Direction.SOUTH;
                if (motion == Direction.NORTH) motion = Direction.WEST;
            }
            case RailShape.NORTH_WEST -> {
                if (motion == Direction.EAST) motion = Direction.NORTH;
                if (motion == Direction.SOUTH) motion = Direction.WEST;
            }
            case RailShape.NORTH_EAST -> {
                if (motion == Direction.WEST) motion = Direction.NORTH;
                if (motion == Direction.SOUTH) motion = Direction.EAST;
            }
        }
        BlockPos exitPos = currentPos.relative(motion);
        if (exitPos.getX() == nextNode.x && exitPos.getZ() == nextNode.z)
        {
            return;
        }
        BlockPos leverPos = findLeverNearRail(currentPos);
        if (leverPos != null)
        {
            toggleLever(leverPos);
        }
    }
    private BlockPos findLeverNearRail(BlockPos railPos)
    {
        for (Direction direction : Direction.values())
        {
            BlockPos leverPos = railPos.relative(direction);
            BlockState state = entity.level().getBlockState(leverPos);
            if (state.getBlock() == Blocks.LEVER)
            {
                return leverPos;
            }
        }
        return null;
    }
    private void toggleLever(BlockPos leverPos)
    {
        BlockState state = entity.level().getBlockState(leverPos);
        ((LeverBlock) state.getBlock()).pull(state, entity.level(), leverPos, null);
    }
    private boolean isRidingMinecart()
    {
        return entity.getVehicle() != null && entity.getVehicle() instanceof MinecoloniesMinecart;
    }
}
