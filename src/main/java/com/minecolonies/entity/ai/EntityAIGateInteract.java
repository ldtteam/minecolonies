package com.minecolonies.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.BlockPos;

/**
 * Used for gate interaction for the citizens.
 */
public class EntityAIGateInteract extends EntityAIBase
{
    /**
     * Our citizen.
     */
    protected EntityLiving theEntity;
    /**
     * The gate position.
     */
    protected BlockPos gatePosition;
    /**
     * The gate block.
     */
    protected BlockFenceGate gateBlock;
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
     * Number of heights to check for the fence gate.
     */
    private final static int HEIGHTS_TO_CHECK = 2;
    /**
     * The length of half a block.
     */
    private final static double HALF_BLOCK = 0.5D;
    /**
     * The min distance the gate has to be from the citizen
     */
    private final static double MIN_DISTANCE = 2.25D;
    
    /**
     * Constructor called to register the AI class with an entity
     * @param entityIn the registering entity
     */
    public EntityAIGateInteract(EntityLiving entityIn)
    {
        this.gatePosition = BlockPos.ORIGIN;
        this.theEntity = entityIn;
        if (!(entityIn.getNavigator() instanceof PathNavigateGround))
        {
            throw new IllegalArgumentException("Unsupported mob type for DoorInteractGoal");
        }
    }

    /**
     * Checks if the Interaction should be executed
     * @return true or false depending on the conditions
     */
    @Override
    public boolean shouldExecute()
    {
        return this.theEntity.isCollidedHorizontally && checkPathEntity();
    }

    /**
     * Checks if there exists a path..
     * @return true if the fence gate can be passed.
     */
    private boolean checkPathEntity()
    {
        PathNavigateGround pathnavigateground = (PathNavigateGround) this.theEntity.getNavigator();
        PathEntity pathentity = pathnavigateground.getPath();
        return pathentity != null && !pathentity.isFinished() && pathnavigateground.getEnterDoors() && checkFenceGate(pathentity);
    }

    /**
     * Checks if the citizen is close enough to an existing fence gate.
     * @param pathentity the path through the fence.
     * @return  true if the gate can be passed
     */
    private boolean checkFenceGate(PathEntity pathentity)
    {
        for (int i = 0; i < Math.min(pathentity.getCurrentPathIndex() + 2, pathentity.getCurrentPathLength()); ++i)
        {
            PathPoint pathpoint = pathentity.getPathPointFromIndex(i);
            for(int j=0;j<HEIGHTS_TO_CHECK;j++)
            {
                this.gatePosition = new BlockPos(pathpoint.xCoord, pathpoint.yCoord + j, pathpoint.zCoord);
                if (this.theEntity.getDistanceSq((double) this.gatePosition.getX(), this.theEntity.posY, (double) this.gatePosition.getZ()) <= MIN_DISTANCE)
                {
                    this.gateBlock = this.getBlockFence(this.gatePosition);
                    if (this.gateBlock != null)
                    {
                        return true;
                    }
                }
            }
        }

        this.gatePosition = (new BlockPos(this.theEntity)).up();
        this.gateBlock = this.getBlockFence(this.gatePosition);
        return this.gateBlock != null;
    }

    /**
     * Checks if the execution is still ongoing
     * @return true or false
     */
    @Override
    public boolean continueExecuting()
    {
        return !this.hasStoppedFenceInteraction;
    }

    /**
     * Starts the execution
     */
    @Override
    public void startExecuting()
    {
        this.hasStoppedFenceInteraction = false;
        this.entityPositionX = this.gatePosition.getX() + HALF_BLOCK - this.theEntity.posX;
        this.entityPositionZ = this.gatePosition.getZ() + HALF_BLOCK - this.theEntity.posZ;
    }

    /**
     * Updates the task and checks if the citizen passed the gate already.
     */
    @Override
    public void updateTask()
    {
        double entityDistX =  this.gatePosition.getX() + HALF_BLOCK - this.theEntity.posX;
        double entityDistZ = this.gatePosition.getZ() + HALF_BLOCK - this.theEntity.posZ;
        double totalDist = this.entityPositionX * entityDistX + this.entityPositionZ * entityDistZ;
        if (totalDist < 0.0D)
        {
            this.hasStoppedFenceInteraction = true;
        }
    }

    /**
     * Returns a fenceBlock if available
     * @param pos the position to be searched
     * @return fenceBlock or null
     */
    private BlockFenceGate getBlockFence(BlockPos pos)
    {
        Block block  = this.theEntity.worldObj.getBlockState(pos).getBlock();
        if(!(block instanceof BlockFenceGate && block.getMaterial() == Material.wood))
        {
            block = this.theEntity.worldObj.getBlockState(this.theEntity.getPosition()).getBlock();
            gatePosition = this.theEntity.getPosition();
        }
        return block instanceof BlockFenceGate && block.getMaterial() == Material.wood ? (BlockFenceGate) block : null;
    }
}
