package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.jobs.JobRanger;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import com.minecolonies.coremod.util.SoundUtils;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.coremod.entity.ai.citizen.guard.GuardConstants.*;
import static com.minecolonies.coremod.entity.ai.citizen.guard.GuardConstants.BASIC_VOLUME;
import static com.minecolonies.coremod.entity.ai.citizen.guard.GuardConstants.MOVE_MINIMAL;
import static com.minecolonies.coremod.entity.ai.util.AIState.*;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class EntityAIRanger extends AbstractEntityAIGuard<JobRanger>
{
    /**
     * This guard's minimum distance for attack.
     */
    private static final double MAX_DISTANCE_FOR_ATTACK = 200;

    /**
     * Creates the abstract part of the AI.inte
     * Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAIRanger(@NotNull final JobRanger job)
    {
        super(job);
        super.registerTargets(
          new AITarget(GUARD_ATTACK_RANGED, this::attackRanged)
        );
        toolsNeeded.add(ToolType.BOW);
    }

    @Override
    protected int getAttackRange()
    {
        return (int) MAX_DISTANCE_FOR_ATTACK;
    }

    @Override
    protected AIState decide()
    {
        final AIState superState = super.decide();

        if ((superState != DECIDE && superState != PREPARING) || target == null)
        {
            return superState;
        }

        return GUARD_ATTACK_RANGED;
    }

    /**
     * The ranged attack modus.
     * @return the next state to go to.
     */
    protected AIState attackRanged()
    {
        if (worker.getLastAttackedEntity() != null
              && !worker.getLastAttackedEntity().isDead
              && worker.getDistance(worker.getLastAttackedEntity()) < getAttackRange())
        {
            target = worker.getLastAttackedEntity();
        }

        if (target == null || target.isDead)
        {
            worker.getCitizenExperienceHandler().addExperience(EXP_PER_MOB_DEATH);
            incrementActionsDone();
            return DECIDE;
        }

        if (currentAttackDelay != 0)
        {
            currentAttackDelay--;
            return GUARD_ATTACK_RANGED;
        }
        else
        {
            currentAttackDelay = getAttackDelay();
        }

        if (getOwnBuilding() != null && worker.getCitizenData() != null)
        {

            if (worker.getDistance(target) > getAttackRange())
            {
                worker.isWorkerAtSiteWithMove(target.getPosition(), getAttackRange());
                return GUARD_ATTACK_RANGED;
            }

            final int bowslot = InventoryUtils.getFirstSlotOfItemHandlerContainingTool(new InvWrapper(getInventory()),
              ToolType.BOW,
              0,
              getOwnBuilding().getMaxToolLevel());

            if (bowslot != -1)
            {
                worker.getCitizenItemHandler().setHeldItem(EnumHand.MAIN_HAND, bowslot);

                worker.faceEntity(target, (float) TURN_AROUND, (float) TURN_AROUND);
                worker.getLookHelper().setLookPositionWithEntity(target, (float) TURN_AROUND, (float) TURN_AROUND);

                worker.swingArm(EnumHand.MAIN_HAND);

                final EntityTippedArrow arrow = new GuardArrow(world, worker);
                final double xVector = target.posX - worker.posX;
                final double yVector = target.getEntityBoundingBox().minY + target.height / getAimHeight() - arrow.posY;
                final double zVector = target.posZ - worker.posZ;
                final double distance = (double) MathHelper.sqrt(xVector * xVector + zVector * zVector);
                double damage = getRangedAttackDamage();
                final double chance = HIT_CHANCE_DIVIDER / (worker.getCitizenData().getLevel() + 1);

                arrow.shoot(xVector, yVector + distance * RANGED_AIM_SLIGHTLY_HIGHER_MULTIPLIER, zVector, RANGED_VELOCITY, (float) chance);

                if (Configurations.gameplay.pvp_mode && target instanceof EntityPlayer)
                {
                    damage *= 2;
                }

                if (worker.getHealth() <= DOUBLE_DAMAGE_THRESHOLD)
                {
                    damage *= 2;
                }

                arrow.setDamage(damage);
                final double xDiff = target.posX - worker.posX;
                final double zDiff = target.posZ - worker.posZ;
                final double goToX = xDiff > 0 ? MOVE_MINIMAL : -MOVE_MINIMAL;
                final double goToZ = zDiff > 0 ? MOVE_MINIMAL : -MOVE_MINIMAL;

                worker.move(MoverType.SELF, goToX, 0, goToZ);
                worker.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, (float) BASIC_VOLUME, (float) SoundUtils.getRandomPitch(worker.getRandom()));
                worker.world.spawnEntity(arrow);

                target.setRevengeTarget(worker);

                worker.getCitizenItemHandler().damageItemInHand(EnumHand.MAIN_HAND, 1);
            }
        }
        return GUARD_ATTACK_RANGED;
    }

    /**
     * Gets the aim height for ranged guards.
     *
     * @return the aim height.
     * Suppression because the method already explains the value.
     */
    @SuppressWarnings({"squid:S3400", "squid:S109"})
    protected double getAimHeight()
    {
        return 3.0D;
    }

    /**
     * Damage per ranged attack.
     *
     * @return the attack damage
     * Suppression because the method already explains the value.
     */
    @SuppressWarnings({"squid:S3400", "squid:S109"})
    protected float getRangedAttackDamage()
    {
        return 2;
    }
}
