package com.minecolonies.coremod.entity.ai.mobs;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.util.EnumHand;

/**
 * Class implementing the AI for the Archer Barbarians ranged attack.
 */
public class EntityAIAttackRangedBowBarbarian extends EntityAIBase
{
    private final AbstractArcherBarbarian entity;
    private final double                  moveSpeedAmp;
    private       int                     attackCooldown;
    private final double                  maxAttackDistance;
    private int attackTime = -1;
    private int     seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private              int    strafingTime         = -1;
    private static final int    ONE_MINUTE           = 60;
    private static final int    TWENTY_SECONDS       = 20;
    private static final double ONE_QUARTER_BLOCK    = 0.25D;
    private static final float  HALF_BLOCK           = 0.5F;
    private static final double THREE_QUARTERS_BLOCK = 0.75D;
    private static final float  MAX_DEGREES          = 30.0F;
    private static final int    MUTEX_BITS           = 3;

    public EntityAIAttackRangedBowBarbarian(final AbstractArcherBarbarian archer, final double speedAmplifier, final int delay, final double maxDistance)
    {
        super();
        this.entity = archer;
        this.moveSpeedAmp = speedAmplifier;
        this.attackCooldown = delay;
        this.maxAttackDistance = maxDistance * maxDistance;
        this.setMutexBits(MUTEX_BITS);
    }

    public void setAttackCooldown(final int coolDown)
    {
        this.attackCooldown = coolDown;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        if (this.entity.getAttackTarget() == null)
        {
            return false;
        }
        return this.isBowInMainhand();
    }

    protected boolean isBowInMainhand()
    {
        return !this.entity.getHeldItemMainhand().isEmpty() && this.entity.getHeldItemMainhand().getItem() == Items.BOW;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
        return (this.shouldExecute() || !this.entity.getNavigator().noPath()) && this.isBowInMainhand();
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        super.startExecuting();
        this.entity.setSwingingArms(true);
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
        super.resetTask();
        this.entity.setSwingingArms(false);
        this.seeTime = 0;
        this.attackTime = -1;
        this.entity.resetActiveHand();
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
        final EntityLivingBase entitylivingbase = this.entity.getAttackTarget();

        if (entitylivingbase != null)
        {
            final double d0 = this.entity.getDistanceSq(entitylivingbase.posX, entitylivingbase.getEntityBoundingBox().minY, entitylivingbase.posZ);
            final boolean flag = this.entity.getEntitySenses().canSee(entitylivingbase);
            final boolean flag1 = this.seeTime > 0;

            if (flag != flag1)
            {
                this.seeTime = 0;
            }

            if (flag)
            {
                ++this.seeTime;
            }
            else
            {
                --this.seeTime;
            }

            if (d0 <= this.maxAttackDistance && this.seeTime >= TWENTY_SECONDS)
            {
                this.entity.getNavigator().clearPathEntity();
                ++this.strafingTime;
            }
            else
            {
                this.entity.getNavigator().tryMoveToEntityLiving(entitylivingbase, this.moveSpeedAmp);
                this.strafingTime = -1;
            }

            final int strafeTime = TWENTY_SECONDS;

            if (this.strafingTime >= strafeTime)
            {
                final double maxRNG = 0.3D;

                if ((double) this.entity.getRNG().nextFloat() < maxRNG)
                {
                    this.strafingClockwise = !this.strafingClockwise;
                }

                if ((double) this.entity.getRNG().nextFloat() < maxRNG)
                {
                    this.strafingBackwards = !this.strafingBackwards;
                }

                this.strafingTime = 0;
            }

            if (this.strafingTime > -1)
            {
                if (d0 > (this.maxAttackDistance * THREE_QUARTERS_BLOCK))
                {
                    this.strafingBackwards = false;
                }
                else if (d0 < (this.maxAttackDistance * ONE_QUARTER_BLOCK))
                {
                    this.strafingBackwards = true;
                }

                this.entity.getMoveHelper().strafe(this.strafingBackwards ? -HALF_BLOCK : HALF_BLOCK, this.strafingClockwise ? HALF_BLOCK : -HALF_BLOCK);
                this.entity.faceEntity(entitylivingbase, MAX_DEGREES, MAX_DEGREES);
            }
            else
            {
                this.entity.getLookHelper().setLookPositionWithEntity(entitylivingbase, MAX_DEGREES, MAX_DEGREES);
            }

            --this.attackTime;

            if (this.entity.isHandActive())
            {
                if (!flag && this.seeTime < -ONE_MINUTE)
                {
                    this.entity.resetActiveHand();
                }
                else if (flag)
                {
                    final int i = this.entity.getItemInUseMaxCount();

                    final int maxCount = 20;

                    if (i >= maxCount)
                    {
                        this.entity.resetActiveHand();
                        this.entity.attackEntityWithRangedAttack(entitylivingbase, ItemBow.getArrowVelocity(i));
                        this.attackTime = this.attackCooldown;
                    }
                }
            }
            else if (this.attackTime <= 0 && this.seeTime >= -ONE_MINUTE)
            {
                this.entity.setActiveHand(EnumHand.MAIN_HAND);
            }
        }
    }
}
