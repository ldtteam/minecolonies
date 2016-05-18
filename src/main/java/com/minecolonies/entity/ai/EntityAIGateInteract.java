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

public class EntityAIGateInteract extends EntityAIBase {
    /**
     * Our citizen
     */
    protected EntityLiving theEntity;
    /**
     * The gate position
     */
    protected BlockPos gatePosition;
    /**
     * The gate block
     */
    protected BlockFenceGate gateBlock;
    /**
     * Check if the interaction with the fenceGate stopped already.
     */
    private boolean hasStoppedFenceInteraction;
    /**
     * The entities x and z position
     */
    private float entityPositionX;
    private float entityPositionZ;

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
    public boolean shouldExecute()
    {
        if (!this.theEntity.isCollidedHorizontally)
        {
            return false;
        }
        else
        {
            PathNavigateGround pathnavigateground = (PathNavigateGround) this.theEntity.getNavigator();
            PathEntity pathentity = pathnavigateground.getPath();
            if (pathentity != null && !pathentity.isFinished()  && pathnavigateground.getEnterDoors())
            {
                for (int i = 0; i < Math.min(pathentity.getCurrentPathIndex() + 2, pathentity.getCurrentPathLength()); ++i)
                {
                    PathPoint pathpoint = pathentity.getPathPointFromIndex(i);
                    for(int j=0;i<2;i++)
                    {
                        this.gatePosition = new BlockPos(pathpoint.xCoord, pathpoint.yCoord + j, pathpoint.zCoord);
                        if (this.theEntity.getDistanceSq((double) this.gatePosition.getX(), this.theEntity.posY, (double) this.gatePosition.getZ()) <= 2.25D)
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
            else
            {
                return false;
            }
        }
    }

    /**
     * Checks if the execution is still ongoing
     * @return true or false
     */
    public boolean continueExecuting()
    {
        return !this.hasStoppedFenceInteraction;
    }

    /**
     * Starts the execution
     */
    public void startExecuting()
    {
        this.hasStoppedFenceInteraction = false;
        this.entityPositionX = (float) ((double) ((float) this.gatePosition.getX() + 0.5F) - this.theEntity.posX);
        this.entityPositionZ = (float) ((double) ((float) this.gatePosition.getZ() + 0.5F) - this.theEntity.posZ);
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
        float f = (float) ((double) ((float) this.gatePosition.getX() + 0.5F) - this.theEntity.posX);
        float f1 = (float) ((double) ((float) this.gatePosition.getZ() + 0.5F) - this.theEntity.posZ);
        float f2 = this.entityPositionX * f + this.entityPositionZ * f1;
        if (f2 < 0.0F)
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
        return (block instanceof BlockFenceGate && block.getMaterial() == Material.wood ? (BlockFenceGate) block : null);
    }
}
