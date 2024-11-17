package com.minecolonies.api.util;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;

public class LookHandler extends LookControl
{
    private boolean doneNavigating = true;

    /**
     * Remembers the entity to look at for a short while
     */
    private Entity lookingAt   = null;
    private int    lookAtTimer = 0;

    public LookHandler(final Mob entity)
    {
        super(entity);
    }

    @Override
    public void tick()
    {
        if (mob.tickCount % 20 == 17)
        {
            doneNavigating = this.mob.getNavigation().isDone();
        }

        if (lookingAt != null && lookAtTimer-- > 0)
        {
            super.setLookAt(lookingAt);
            if (lookAtTimer == 0)
            {
                lookingAt = null;
            }
        }

        if (this.resetXRotOnTick())
        {
            this.mob.setXRot(0.0F);
        }

        if (this.lookAtCooldown > 0)
        {
            --this.lookAtCooldown;

            // Copy of super tick without needless optional and lambda wrapping
            double dx = this.wantedX - this.mob.getX();
            double dz = this.wantedZ - this.mob.getZ();

            if (Math.abs(dz) > (double) 1.0E-5F || Math.abs(dx) > (double) 1.0E-5F)
            {
                this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, (float) (Mth.atan2(dz, dx) * (double) (180F / (float) Math.PI)) - 90.0F, this.yMaxRotSpeed);
            }

            double dEy = this.wantedY - this.mob.getEyeY();
            double xzlenght = Math.sqrt(dx * dx + dz * dz);

            if (Math.abs(dEy) > (double) 1.0E-5F || Math.abs(xzlenght) > (double) 1.0E-5F)
            {
                this.mob.setXRot(this.rotateTowards(this.mob.getXRot(), (float) -(Mth.atan2(dEy, xzlenght) * (double) (180F / (float) Math.PI)), this.xMaxRotAngle));
            }
        }
        else
        {
            this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, this.mob.yBodyRot, 10.0F);
        }

        if (!doneNavigating)
        {
            // clampHeadRotationToBody
            this.mob.yHeadRot = Mth.rotateIfNecessary(this.mob.yHeadRot, this.mob.yBodyRot, (float) this.mob.getMaxHeadYRot());
        }
    }

    public void setLookAtCooldown(final int cooldown)
    {
        lookAtCooldown = cooldown;
    }

    @Override
    public void setLookAt(Entity entity)
    {
        super.setLookAt(entity);
        lookAtTimer = 20 * 5;
        lookingAt = entity;
    }

    @Override
    public void setLookAt(Entity entity, float turnY, float turnX)
    {
        super.setLookAt(entity, turnY, turnX);
        lookAtTimer = 20 * 5;
        lookingAt = entity;
    }
}