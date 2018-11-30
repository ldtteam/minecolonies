package com.minecolonies.coremod.entity.ai.citizen.trainingCamps;

import com.minecolonies.coremod.colony.jobs.JobArcherTraining;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIBasic;
import com.minecolonies.coremod.entity.ai.citizen.guard.GuardArrow;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import com.minecolonies.coremod.util.SoundUtils;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.GuardConstants.*;
import static com.minecolonies.coremod.entity.ai.util.AIState.GUARD_ATTACK_RANGED;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class EntityAIArcherTraining extends AbstractEntityAIBasic<JobArcherTraining>
{
    /**
     * Creates the abstract part of the AI.inte
     * Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAIArcherTraining(@NotNull final JobArcherTraining job)
    {
        super(job);
        super.registerTargets(
          new AITarget(GUARD_ATTACK_RANGED, false, this::attackRanged)
        );
    }

    /**
     * The ranged attack modus. Ticked every 10 Ticks.
     *
     * @return the next state to go to.
     */
    protected AIState attackRanged()
    {
        final BlockPos target = new BlockPos(0,0,0);
        //worker.face(target, (float) TURN_AROUND, (float) TURN_AROUND);
        worker.swingArm(EnumHand.MAIN_HAND);

        final EntityTippedArrow arrow = new GuardArrow(world, worker);
        final double xVector = target.getX() - worker.posX;
        final double yVector = target.getY() / getAimHeight() - arrow.posY;
        final double zVector = target.getZ() - worker.posZ;
        final double distance = (double) MathHelper.sqrt(xVector * xVector + zVector * zVector);


        final double chance = HIT_CHANCE_DIVIDER / (worker.getCitizenData().getLevel() + 1);
        arrow.shoot(xVector, yVector + distance * RANGED_AIM_SLIGHTLY_HIGHER_MULTIPLIER, zVector, RANGED_VELOCITY, (float) chance);

        worker.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, (float) BASIC_VOLUME, (float) SoundUtils.getRandomPitch(worker.getRandom()));
        worker.world.spawnEntity(arrow);

        final double xDiff = target.getX() - worker.posX;
        final double zDiff = target.getZ() - worker.posZ;
        final double goToX = xDiff > 0 ? MOVE_MINIMAL : -MOVE_MINIMAL;
        final double goToZ = zDiff > 0 ? MOVE_MINIMAL : -MOVE_MINIMAL;
        worker.move(MoverType.SELF, goToX, 0, goToZ);

        if (worker.getRandom().nextBoolean())
        {
            worker.getCitizenItemHandler().damageItemInHand(EnumHand.MAIN_HAND, 1);
        }
        worker.resetActiveHand();
        this.incrementActionsDoneAndDecSaturation();
        return GUARD_ATTACK_RANGED;
    }

    /**
     * Gets the aim height for ranged guards.
     *
     * @return the aim height.
     * Suppression because the method already explains the value.
     */
    @SuppressWarnings({"squid:S3400", "squid:S109"})
    private double getAimHeight()
    {
        return 3.0D;
    }
}
