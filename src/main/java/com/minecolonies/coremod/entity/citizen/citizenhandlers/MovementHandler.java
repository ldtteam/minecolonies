package com.minecolonies.coremod.entity.citizen.citizenhandlers;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;

/**
 * Custom movement handler for minecolonies citizens (avoid jumping so much).
 */
public class MovementHandler extends MovementController
{
    public MovementHandler(MobEntity mob)
    {
        super(mob);
    }

    @Override
    public void tick()
    {
        if (this.action == net.minecraft.entity.ai.controller.MovementController.Action.STRAFE)
        {
            final float speedAtt = (float) this.mob.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue();
            float speed = (float) this.speed * speedAtt;
            float forward = this.moveForward;
            float strafe = this.moveStrafe;
            float totalMovement = MathHelper.sqrt(forward * forward + strafe * strafe);
            if (totalMovement < 1.0F)
            {
                totalMovement = 1.0F;
            }

            totalMovement = speed / totalMovement;
            forward = forward * totalMovement;
            strafe = strafe * totalMovement;
            final float sinRotation = MathHelper.sin(this.mob.rotationYaw * ((float) Math.PI / 180F));
            final float cosRotation = MathHelper.cos(this.mob.rotationYaw * ((float) Math.PI / 180F));
            final float rot1 = forward * cosRotation - strafe * sinRotation;
            final float rot2 = strafe * cosRotation + forward * sinRotation;
            final PathNavigator pathnavigator = this.mob.getNavigator();

            final NodeProcessor nodeprocessor = pathnavigator.getNodeProcessor();
            if (nodeprocessor.getPathNodeType(this.mob.world,
              MathHelper.floor(this.mob.posX + (double) rot1),
              MathHelper.floor(this.mob.posY),
              MathHelper.floor(this.mob.posZ + (double) rot2)) != PathNodeType.WALKABLE)
            {
                this.moveForward = 1.0F;
                this.moveStrafe = 0.0F;
                speed = speedAtt;
            }

            this.mob.setAIMoveSpeed(speed);
            this.mob.setMoveForward(this.moveForward);
            this.mob.setMoveStrafing(this.moveStrafe);
            this.action = net.minecraft.entity.ai.controller.MovementController.Action.WAIT;
        }
        else if (this.action == net.minecraft.entity.ai.controller.MovementController.Action.MOVE_TO)
        {
            this.action = net.minecraft.entity.ai.controller.MovementController.Action.WAIT;
            final double xDif = this.posX - this.mob.posX;
            final double zDif = this.posZ - this.mob.posZ;
            final double yDif = this.posY - this.mob.posY;
            final double dist = xDif * xDif + yDif * yDif + zDif * zDif;
            if (dist < (double) 2.5000003E-7F)
            {
                this.mob.setMoveForward(0.0F);
                return;
            }

            final float range = (float) (MathHelper.atan2(zDif, xDif) * (double) (180F / (float) Math.PI)) - 90.0F;
            this.mob.rotationYaw = this.limitAngle(this.mob.rotationYaw, range, 90.0F);
            this.mob.setAIMoveSpeed((float) (this.speed * this.mob.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue()));
            final BlockPos blockpos = new BlockPos(this.mob);
            final BlockState blockstate = this.mob.world.getBlockState(blockpos);
            final Block block = blockstate.getBlock();
            final VoxelShape voxelshape = blockstate.getCollisionShape(this.mob.world, blockpos);
            if ((yDif > (double) this.mob.stepHeight && xDif * xDif + zDif * zDif < (double) Math.max(1.0F, this.mob.getWidth()))
                  || (!voxelshape.isEmpty() && this.mob.posY < voxelshape.getEnd(Direction.Axis.Y) + (double) blockpos.getY() && !block.isIn(BlockTags.DOORS) && !block.isIn(
              BlockTags.FENCES))
                       && !block.isLadder(blockstate, this.mob.world, blockpos, this.mob))
            {
                this.mob.getJumpController().setJumping();
                this.action = net.minecraft.entity.ai.controller.MovementController.Action.JUMPING;
            }
        }
        else if (this.action == net.minecraft.entity.ai.controller.MovementController.Action.JUMPING)
        {
            //TODO we're using setAIMoveSpeed manually which gets overridden here and above
            this.mob.setAIMoveSpeed((float) (this.speed * this.mob.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue()));

            // Avoid beeing stuck in jumping while in liquids
            final BlockPos blockpos = new BlockPos(this.mob);
            final BlockState blockstate = this.mob.world.getBlockState(blockpos);
            if (this.mob.onGround || blockstate.getMaterial().isLiquid())
            {
                this.action = net.minecraft.entity.ai.controller.MovementController.Action.WAIT;
            }
        }
        else
        {
            this.mob.setMoveForward(0.0F);
        }
    }
}
