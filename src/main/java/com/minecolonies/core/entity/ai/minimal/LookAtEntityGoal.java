package com.minecolonies.core.entity.ai.minimal;

import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.core.colony.jobs.AbstractJobGuard;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class LookAtEntityGoal extends Goal
{
    public static final float                         DEFAULT_PROBABILITY = 0.02F;
    protected final     Mob                           mob;
    @Nullable
    protected           Entity                        lookAt;
    protected final     float                         lookDistance;
    private             int                           lookTime;
    protected final     float                         probability;
    private final       boolean                       onlyHorizontal;
    protected final     Class<? extends LivingEntity> lookAtType;

    public LookAtEntityGoal(Mob mob, Class<? extends LivingEntity> lookAtType, float lookDistance)
    {
        this(mob, lookAtType, lookDistance, DEFAULT_PROBABILITY);
    }

    public LookAtEntityGoal(Mob mob, Class<? extends LivingEntity> lookAtType, float lookDistance, float probability)
    {
        this(mob, lookAtType, lookDistance, probability, false);
    }

    public LookAtEntityGoal(Mob mob, Class<? extends LivingEntity> lookAtType, float lookDistance, float probability, boolean p_148122_)
    {
        this.mob = mob;
        this.lookAtType = lookAtType;
        this.lookDistance = lookDistance;
        this.probability = probability;
        this.onlyHorizontal = p_148122_;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse()
    {
        if (this.mob.getRandom().nextFloat() >= this.probability)
        {
            return false;
        }
        else
        {
            if (this.mob.getTarget() != null)
            {
                this.lookAt = this.mob.getTarget();
            }

            if (this.lookAtType == Player.class)
            {
                this.lookAt = WorldUtil.getNearestPlayer(this.mob, this.mob.getBlockX(), this.mob.getBlockY() + 1, this.mob.getBlockZ(), lookDistance);
            }
            else
            {
                this.lookAt = WorldUtil.getNearestEntity(this.mob.level().getEntitiesOfClass(this.lookAtType,
                  this.mob.getBoundingBox().inflate(this.lookDistance, 3.0D, this.lookDistance),
                  (entity) -> true), this.mob, this.mob.getBlockX(), this.mob.getBlockY() + 1, this.mob.getBlockZ(), lookDistance);
            }

            if (mob instanceof EntityCitizen citizen && citizen.getCitizenJobHandler().getColonyJob() instanceof AbstractJobGuard<?> job && job.isAsleep())
            {
                return false;
            }

            return this.lookAt != null;
        }
    }

    @Override
    public boolean canContinueToUse()
    {
        if (!this.lookAt.isAlive())
        {
            return false;
        }
        else if (this.mob.distanceToSqr(this.lookAt) > (double) (this.lookDistance * this.lookDistance))
        {
            return false;
        }
        else
        {
            return this.lookTime > 0;
        }
    }

    @Override
    public void start()
    {
        this.lookTime = this.adjustedTickDelay(40 + this.mob.getRandom().nextInt(40));
    }

    @Override
    public void stop()
    {
        this.lookAt = null;
    }

    @Override
    public void tick()
    {
        if (this.lookAt.isAlive())
        {
            double d0 = this.onlyHorizontal ? this.mob.getEyeY() : this.lookAt.getEyeY();
            this.mob.getLookControl().setLookAt(this.lookAt.getX(), d0, this.lookAt.getZ());
            --this.lookTime;
        }
    }
}
