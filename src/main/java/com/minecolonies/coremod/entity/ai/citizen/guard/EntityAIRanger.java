package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.entity.ai.citizen.guards.GuardTask;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.jobs.JobRanger;
import com.minecolonies.coremod.entity.ai.statemachine.AITarget;
import com.minecolonies.coremod.entity.ai.statemachine.states.IAIState;
import com.minecolonies.coremod.entity.pathfinding.PathResult;
import com.minecolonies.coremod.util.SoundUtils;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.GuardConstants.*;
import static com.minecolonies.coremod.entity.ai.statemachine.states.AIWorkerState.DECIDE;
import static com.minecolonies.coremod.entity.ai.statemachine.states.AIWorkerState.GUARD_ATTACK_RANGED;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class EntityAIRanger extends AbstractEntityAIGuard<JobRanger>
{
    private static final int    TIME_STRAFING_BEFORE_SWITCHING_DIRECTIONS = 4;
    private static final double SWITCH_STRAFING_DIRECTION                 = 0.3d;
    private static final double STRAFING_SPEED                            = 0.7f;

    /**
     * Whether the guard is moving towards his target
     */
    private boolean movingToTarget = false;

    /**
     * Indicates if strafing should be clockwise or not.
     */
    private int strafingClockwise = 1;

    /**
     * Amount of time strafing is able to run.
     */
    private int strafingTime = -1;

    /**
     * Amount of time the guard has been in one spot.
     */
    private int timeAtSameSpot = 0;

    /**
     * Number of ticks the guard has been way too close to target.
     */
    private int tooCloseNumTicks = 0;

    /**
     * Boolean for fleeing pathfinding
     */
    private boolean fleeing = false;

    /**
     * Physical Attack delay in ticks.
     */
    public static final int RANGED_ATTACK_DELAY_BASE = 30;

    /**
     * The path for fleeing
     */
    private PathResult fleePath;

    /**
     * Amount of time the guard has been able to see their target.
     */
    private int timeCanSee = 0;

    /**
     * Last distance to determine if the guard is stuck.
     */
    private double lastDistance = 0.0f;

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
          new AITarget(GUARD_ATTACK_RANGED, this::attackRanged, 10)
        );
        toolsNeeded.add(ToolType.BOW);
    }

    @Override
    public IAIState getAttackState()
    {
        return GUARD_ATTACK_RANGED;
    }

    /**
     * Blockdistance at which attackRanged() will get Control, intentionally set higher than actual attack range
     */
    @Override
    protected int getAttackRange()
    {
        return getRealAttackRange();
    }

    /**
     * Calculates the actual attack range
     *
     * @return The attack range
     */
    private int getRealAttackRange()
    {
        int attackDist = BASE_DISTANCE_FOR_RANGED_ATTACK;
        // + 2 Blockrange per building level for a total of +10 from building level
        if (buildingGuards != null)
        {
            attackDist += buildingGuards.getBuildingLevel() * 2;
        }
        // ~ +1 each three levels for a total of +15 from guard level
        if (worker.getCitizenData() != null)
        {
            attackDist += (worker.getCitizenData().getLevel() / 50.0f) * 15;
        }

        if (target != null)
        {
            attackDist += worker.posY - target.posY;
        }

        return attackDist > MAX_DISTANCE_FOR_RANGED_ATTACK ? MAX_DISTANCE_FOR_RANGED_ATTACK : attackDist;
    }

    @Override
    public boolean hasMainWeapon()
    {
        return !checkForToolOrWeapon(ToolType.BOW);
    }

    /**
     * Get a target for the guard.
     *
     * @return The next IAIState to go to.
     */
    @Override
    protected EntityLivingBase getTarget()
    {
        strafingTime = 0;
        tooCloseNumTicks = 0;
        timeAtSameSpot = 0;
        timeCanSee = 0;
        fleeing = false;
        movingToTarget = false;
        return super.getTarget();
    }

    @Override
    public void wearWeapon()
    {
        final int bowSlot = InventoryUtils.getFirstSlotOfItemHandlerContainingTool(new InvWrapper(getInventory()), ToolType.BOW, 0, buildingGuards.getMaxToolLevel());
        if (bowSlot != -1)
        {
            worker.getCitizenItemHandler().setHeldItem(EnumHand.MAIN_HAND, bowSlot);
        }
    }

    /**
     * The ranged attack modus. Ticked every 10 Ticks.
     *
     * @return the next state to go to.
     */
    protected IAIState attackRanged()
    {
        final IAIState state = preAttackChecks();
        if (state != getState())
        {
            setDelay(STANDARD_DELAY);
            return state;
        }

        if (worker.getCitizenData() == null)
        {
            return GUARD_ATTACK_RANGED;
        }

        final double sqDistanceToEntity = BlockPosUtil.getMaxDistance2D(worker.getPosition(), target.getPosition());
        final boolean canSee = worker.getEntitySenses().canSee(target);
        final double sqAttackRange = getRealAttackRange();

        if (canSee)
        {
            timeCanSee++;
        }
        else
        {
            timeCanSee--;
        }

        if (lastDistance == sqDistanceToEntity)
        {
            timeAtSameSpot++;
        }
        else
        {
            timeAtSameSpot = 0;
        }

        // Stuck
        if (sqDistanceToEntity > sqAttackRange && timeAtSameSpot > 8 || (!canSee && timeAtSameSpot > 8))
        {
            worker.getNavigator().clearPath();
            return DECIDE;
        }
        // Move inside attackrange
        else if (sqDistanceToEntity > sqAttackRange || !canSee)
        {
            worker.getNavigator().tryMoveToEntityLiving(target, getCombatMovementSpeed());
            worker.getMoveHelper().strafe(0, 0);
            movingToTarget = true;
            strafingTime = -1;
        }
        // Clear chasing when in range
        else if (movingToTarget && sqDistanceToEntity < sqAttackRange)
        {
            worker.getNavigator().clearPath();
            movingToTarget = false;
            strafingTime = -1;
        }

        // Reset Fleeing status
        if (fleeing && !fleePath.isComputing())
        {
            fleeing = false;
            tooCloseNumTicks = 0;
        }

        // Check if the target is too close
        if (sqDistanceToEntity < RANGED_FLEE_SQDIST)
        {
            tooCloseNumTicks++;
            strafingTime = -1;

            // Fleeing
            if (!fleeing && !movingToTarget && sqDistanceToEntity < RANGED_FLEE_SQDIST && tooCloseNumTicks > 5)
            {
                fleePath = worker.getNavigator().moveAwayFromEntityLiving(target, getRealAttackRange() / 2.0, getCombatMovementSpeed());
                fleeing = true;
                worker.getMoveHelper().strafe(0, (float) strafingClockwise * 0.2f);
                strafingClockwise *= -1;
            }
        }
        else
        {
            tooCloseNumTicks = 0;
        }

        // Combat movement for guards not on guarding block task
        if (buildingGuards.getTask() != GuardTask.GUARD)
        {
            // Toggle strafing direction randomly if strafing
            if (strafingTime >= TIME_STRAFING_BEFORE_SWITCHING_DIRECTIONS)
            {
                if ((double) worker.getRNG().nextFloat() < SWITCH_STRAFING_DIRECTION)
                {
                    strafingClockwise *= -1;
                }
                this.strafingTime = 0;
            }

            // Strafe when we're close enough
            if (sqDistanceToEntity < (sqAttackRange / 2.0) && tooCloseNumTicks < 1)
            {
                strafingTime++;
            }
            else if (sqDistanceToEntity > (sqAttackRange / 2.0) + 5)
            {
                strafingTime = -1;
            }

            // Strafe or flee, when not already fleeing or moving in
            if ((strafingTime > -1) && !fleeing && !movingToTarget)
            {
                worker.getMoveHelper().strafe(0, (float) (getCombatMovementSpeed() * strafingClockwise * STRAFING_SPEED));
                worker.faceEntity(target, (float) TURN_AROUND, (float) TURN_AROUND);
            }
            else
            {
                worker.faceEntity(target, (float) TURN_AROUND, (float) TURN_AROUND);
            }
        }

        if (worker.isHandActive())
        {
            if (!canSee && timeCanSee < -6)
            {
                worker.resetActiveHand();
            }
            else if (canSee && sqDistanceToEntity < sqAttackRange)
            {
                worker.faceEntity(target, (float) TURN_AROUND, (float) TURN_AROUND);
                worker.swingArm(EnumHand.MAIN_HAND);

                final EntityTippedArrow arrow = new GuardArrow(world, worker);
                final double xVector = target.posX - worker.posX;
                final double yVector = target.getEntityBoundingBox().minY + target.height / getAimHeight() - arrow.posY;
                final double zVector = target.posZ - worker.posZ;
                final double distance = (double) MathHelper.sqrt(xVector * xVector + zVector * zVector);
                double damage = getRangedAttackDamage();

                // Add bow enchant effects: Knocback and fire
                final ItemStack bow = worker.getHeldItem(EnumHand.MAIN_HAND);

                if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, bow) > 0)
                {
                    arrow.setFire(100);
                }
                final int k = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, bow);
                if (k > 0)
                {
                    arrow.setKnockbackStrength(k);
                }

                final double chance = HIT_CHANCE_DIVIDER / (worker.getCitizenData().getLevel() + 1);

                arrow.shoot(xVector, yVector + distance * RANGED_AIM_SLIGHTLY_HIGHER_MULTIPLIER, zVector, RANGED_VELOCITY, (float) chance);

                if (worker.getHealth() <= worker.getMaxHealth() * 0.2D)
                {
                    damage *= 2;
                }

                arrow.setDamage(damage);
                worker.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, (float) BASIC_VOLUME, (float) SoundUtils.getRandomPitch(worker.getRandom()));
                worker.world.spawnEntity(arrow);

                final double xDiff = target.posX - worker.posX;
                final double zDiff = target.posZ - worker.posZ;
                final double goToX = xDiff > 0 ? MOVE_MINIMAL : -MOVE_MINIMAL;
                final double goToZ = zDiff > 0 ? MOVE_MINIMAL : -MOVE_MINIMAL;
                worker.move(MoverType.SELF, goToX, 0, goToZ);

                timeCanSee = 0;
                target.setRevengeTarget(worker);
                currentAttackDelay = getAttackDelay();
                worker.getCitizenItemHandler().damageItemInHand(EnumHand.MAIN_HAND, 1);
                worker.resetActiveHand();
                worker.decreaseSaturationForContinuousAction();
            }
            else
            {
                /*
                 * It is possible the object is higher than guard and guard can't get there.
                 * Guard will try to back up to get some distance to be able to shoot target.
                 */
                if (target.posY > worker.posY + Y_VISION + Y_VISION)
                {
                    fleePath = worker.getNavigator().moveAwayFromEntityLiving(target, 10, getCombatMovementSpeed());
                    fleeing = true;
                    worker.getMoveHelper().strafe(0, 0);
                }
            }
        }
        else
        {
            reduceAttackDelay(10);
            if (currentAttackDelay <= 0)
            {
                worker.setActiveHand(EnumHand.MAIN_HAND);
            }
        }
        lastDistance = sqDistanceToEntity;

        return GUARD_ATTACK_RANGED;
    }

    /**
     * Gets the reload time for a physical guard attack.
     *
     * @return the reload time, min PHYSICAL_ATTACK_DELAY_MIN Ticks
     */
    @Override
    protected int getAttackDelay()
    {
        if (worker.getCitizenData() != null)
        {
            final int attackDelay = RANGED_ATTACK_DELAY_BASE - (worker.getCitizenData().getLevel() / 2);
            return attackDelay < PHYSICAL_ATTACK_DELAY_MIN * 2 ? PHYSICAL_ATTACK_DELAY_MIN * 2 : attackDelay;
        }
        return RANGED_ATTACK_DELAY_BASE;
    }

    /**
     * Calculates the ranged attack damage
     *
     * @return the attack damage
     */
    private double getRangedAttackDamage()
    {
        if (worker.getCitizenData() != null)
        {
            int enchantDmg = 0;
            if (Configurations.gameplay.rangerEnchants)
            {
                final ItemStack heldItem = worker.getHeldItem(EnumHand.MAIN_HAND);
                // Normalize to +1 dmg
                enchantDmg += EnchantmentHelper.getModifierForCreature(heldItem, target.getCreatureAttribute()) / 2.5;
                enchantDmg += EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, heldItem);
            }

            return (RANGER_BASE_DMG + getLevelDamage() + enchantDmg) * Configurations.gameplay.rangerDamageMult;
        }
        return RANGER_BASE_DMG * Configurations.gameplay.rangerDamageMult;
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
