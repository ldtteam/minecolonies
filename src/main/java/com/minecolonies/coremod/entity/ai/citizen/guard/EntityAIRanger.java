package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.entity.ai.citizen.guards.GuardTask;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.jobs.JobRanger;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import com.minecolonies.coremod.util.SoundUtils;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.GuardConstants.*;
import static com.minecolonies.coremod.entity.ai.util.AIState.*;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class EntityAIRanger extends AbstractEntityAIGuard<JobRanger>
{
    private static final int    TIME_STRAFING_BEFORE_SWITCHING_DIRECTIONS = 15;
    private static final double SWITCH_STRAFING_DIRECTION                 = 0.3d;
    private static final double STRAFING_SPEED                            = 0.6f;

    /**
     * This guard's minimum distance for attack.
     */
    private static final int MAX_DISTANCE_FOR_ATTACK = 150;

    /**
     * The value of the speed which the guard will move.
     */
    private static final double ATTACK_SPEED = 0.8;

    /**
     * Indicates if strafing should be moving backwards or not.
     */
    private boolean strafingBackwards = false;

    /**
     * Indicates if strafing should be clockwise or not.
     */
    private boolean strafingClockwise = false;

    /**
     * Amount of time strafing is able to run.
     */
    private int strafingTime = -1;

    /**
     * Amount of time the guard has been in one spot.
     */
    private int timeAtSameSpot = 0;

    /**
     * Amount of time left until guard can attack again.
     */
    private int attackTime = 0;

    /**
     * Number of ticks the guard has been way to close to target.
     */
    private int toCloseNumTicks = 0;

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
          new AITarget(GUARD_ATTACK_RANGED, this::attackRanged)
        );
        toolsNeeded.add(ToolType.BOW);
    }

    @Override
    public AIState getAttackState()
    {
        return GUARD_ATTACK_RANGED;
    }

    @Override
    protected int getAttackRange()
    {
        return (MAX_DISTANCE_FOR_ATTACK / ((buildingGuards.getBuildingLevel() / buildingGuards.getMaxBuildingLevel()) + 1)) + 10;
    }

    @Override
    public boolean hasMainWeapon()
    {
        return !checkForToolOrWeapon(ToolType.BOW);
    }

    /**
     * Get a target for the guard.
     *
     * @return The next AIState to go to.
     */
    @Override
    protected EntityLivingBase getTarget()
    {
        strafingTime = 0;
        toCloseNumTicks = 0;
        timeAtSameSpot = 0;
        timeCanSee = 0;
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
     * The ranged attack modus.
     *
     * @return the next state to go to.
     */
    protected AIState attackRanged()
    {
        final AIState state = preAttackChecks();
        if (state != getState())
        {
            setDelay(STANDARD_DELAY);
            return state;
        }

        if (worker.getCitizenData() != null)
        {
            final double two_dim_distance = BlockPosUtil.getDistanceSquared2D(worker.getCurrentPosition(), target.getPosition());
            final double distanceToEntity = worker.getDistanceSq(target.posX, target.getEntityBoundingBox().minY, target.posZ);
            final boolean canSee = worker.getEntitySenses().canSee(target);

            if (canSee)
            {
                timeAtSameSpot = 0;
                timeCanSee++;
            }
            else
            {
                if (lastDistance == two_dim_distance)
                {
                    timeAtSameSpot++;
                }
                else if (timeAtSameSpot > 0)
                {
                    timeAtSameSpot++;
                }
                else
                {
                    timeAtSameSpot = 0;
                }
                timeCanSee--;
            }

            if (distanceToEntity > getAttackDistance() && timeCanSee >= 20 && (!canSee && timeAtSameSpot > 20))
            {
                worker.getNavigator().clearPath();
                strafingTime++;
            }
            else if (distanceToEntity > getAttackRange())
            {
                worker.getNavigator().tryMoveToEntityLiving(target, ATTACK_SPEED);
                strafingTime = -1;
            }

            if (strafingTime >= TIME_STRAFING_BEFORE_SWITCHING_DIRECTIONS)
            {
                if ((double) worker.getRNG().nextFloat() < SWITCH_STRAFING_DIRECTION)
                {
                    strafingClockwise = !strafingClockwise;
                }

                if ((double) worker.getRNG().nextFloat() < SWITCH_STRAFING_DIRECTION)
                {
                    strafingBackwards = !strafingBackwards;
                }

                this.strafingTime = 0;
            }

            if (distanceToEntity < getAttackDistance() && toCloseNumTicks < 10)
            {
                toCloseNumTicks++;
            }
            else
            {
                toCloseNumTicks = 0;
            }

            if (buildingGuards.getTask() != GuardTask.GUARD)
            {
                if (strafingTime > -1 || toCloseNumTicks > 0)
                {
                    if (distanceToEntity < getAttackDistance() && toCloseNumTicks > 5 && (timeCanSee > -10))
                    {
                        worker.getNavigator().moveAwayFromEntityLiving(target, 20, getAttackSpeed());
                        worker.faceEntity(target, (float) TURN_AROUND, (float) TURN_AROUND);
                    }
                    else
                    {
                        if (distanceToEntity > (double) (getAttackRange() * 0.75F))
                        {
                            strafingBackwards = false;
                        }
                        else if (distanceToEntity < (double) (getAttackRange() * 0.5F) && toCloseNumTicks <= 5)
                        {
                            strafingBackwards = true;
                        }
                        worker.getMoveHelper().strafe((float) (strafingBackwards ? (getAttackDistance() - distanceToEntity) * -1 : getAttackSpeed()),
                          (float) (strafingClockwise ? getAttackSpeed() * STRAFING_SPEED : getAttackSpeed() * STRAFING_SPEED * -1));
                    }

                    worker.faceEntity(target, (float) TURN_AROUND, (float) TURN_AROUND);
                }
                else
                {
                    worker.getLookHelper().setLookPositionWithEntity(target, (float) TURN_AROUND, (float) TURN_AROUND);
                }
            }
            else if (!canSee)
            {
                return DECIDE;
            }
            else if(distanceToEntity < getAttackDistance())
            {
                worker.getNavigator().moveAwayFromEntityLiving(target, 10, getAttackSpeed());
            }

            if (worker.isHandActive())
            {
                if (!canSee && timeCanSee < -60)
                {
                    worker.resetActiveHand();
                }
                else if (canSee)
                {
                    worker.faceEntity(target, (float) TURN_AROUND, (float) TURN_AROUND);
                    worker.swingArm(EnumHand.MAIN_HAND);

                    final EntityTippedArrow arrow = new GuardArrow(world, worker);
                    final double xVector = target.posX - worker.posX;
                    final double yVector = target.getEntityBoundingBox().minY + target.height / getAimHeight() - arrow.posY;
                    final double zVector = target.posZ - worker.posZ;
                    final double distance = (double) MathHelper.sqrt(xVector * xVector + zVector * zVector);
                    double damage = getRangedAttackDamage();

                    if (Configurations.gameplay.rangerEnchants)
                    {
                        final ItemStack heldItem = worker.getHeldItem(EnumHand.MAIN_HAND);
                        damage += EnchantmentHelper.getModifierForCreature(heldItem, target.getCreatureAttribute());
                        damage += EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, heldItem);
                    }

                    final double chance = HIT_CHANCE_DIVIDER / (worker.getCitizenData().getLevel() + 1);

                    arrow.shoot(xVector, yVector + distance * RANGED_AIM_SLIGHTLY_HIGHER_MULTIPLIER, zVector, RANGED_VELOCITY, (float) chance);

                    if (worker.getHealth() <= DOUBLE_DAMAGE_THRESHOLD)
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

                    target.setRevengeTarget(worker);
                    attackTime = getAttackDelay();
                    worker.getCitizenItemHandler().damageItemInHand(EnumHand.MAIN_HAND, 1);
                    worker.resetActiveHand();
                }
                else
                {
                    /*
                     * It is possible the object is higher than guard and guard can't get there.
                     * Guard will try to back up to get some distance to be able to shoot target.
                     */
                    if (target.posY > worker.posY + Y_VISION + Y_VISION)
                    {
                        worker.getNavigator().moveAwayFromEntityLiving(target, 10, getAttackSpeed());
                    }
                }
            }
            else
            {
                attackTime--;
                if (attackTime <= 0)
                {
                    worker.setActiveHand(EnumHand.MAIN_HAND);
                }
            }
            lastDistance = two_dim_distance;
        }

        return GUARD_ATTACK_RANGED;
    }

    /**
     * Gets the reload time for a Range guard attack.
     *
     * @return the reload time
     */
    protected int getAttackDelay()
    {
        if (worker.getCitizenData() != null)
        {
            return RANGED_ATTACK_DELAY_BASE / (worker.getCitizenData().getLevel() + 1);
        }
        return RANGED_ATTACK_DELAY_BASE;
    }

    /**
     * Damage per ranged attack.
     *
     * @return the attack damage
     * Suppression because the method already explains the value.
     */
    @SuppressWarnings({"squid:S3400", "squid:S109"})
    private float getRangedAttackDamage()
    {
        return 2;
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
