package com.minecolonies.coremod.entity.ai.citizen.trainingcamps;

import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingArchery;
import com.minecolonies.coremod.colony.jobs.JobCombatTraining;
import com.minecolonies.coremod.entity.ai.citizen.guard.GuardArrow;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import com.minecolonies.coremod.util.SoundUtils;
import com.minecolonies.coremod.util.WorkerUtil;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.CitizenConstants.TICKS_20;
import static com.minecolonies.api.util.constant.GuardConstants.*;
import static com.minecolonies.coremod.entity.ai.util.AIState.*;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class EntityAICombatTraining extends AbstractEntityAITraining<JobCombatTraining>
{
    /**
     * How often should strength factor into the knight's skill modifier.
     */
    private static final int STRENGTH_MULTIPLIER = 2;

    /**
     * How often should dexterity factor into the knight's skill modifier.
     */
    private static final int DEXTERITY_MULTIPLIER = 1;

    /**
     * Xp per successful shot.
     */
    private static final int XP_PER_SUCCESSFUL_HIT = 1;

    /**
     * Number of target tries per building level.
     */
    private static final int BUILDING_LEVEL_TARGET_MULTIPLIER = 5;

    /**
     * Min distance to be considered successful shot.
     */
    private static final double MIN_DISTANCE_FOR_SUCCESS = 2.0;

    /**
     * Physical Attack delay in ticks.
     */
    private static final int COMBAT_ATTACK_DELAY_BASE = 10;

    /**
     * Base rate experience for every shot.
     */
    private static final double XP_BASE_RATE  = 0.1;

    /**
     * Time to wait before analyzing the shot.
     */
    private static final int CHECK_HIT_DELAY = TICKS_20 * 3;

    /**
     * The current pathing target to walk to.
     */
    private BlockPos currentCombatTarget;

    /**
     * Counter of how often we tried to hit the target.
     */
    private int targetCounter;

    /**
     * Shooting arrow in progress.
     */
    private EntityTippedArrow arrowInProgress;

    /**
     * How many more ticks we have until next attack.
     */
    protected int currentAttackDelay = 0;

    //todo Tasks: Wander around (same) find hitting target (same), hit it (same), find other guard (hit forth and back for a bit)

    /**
     * Creates the abstract part of the AI.inte
     * Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAICombatTraining(@NotNull final JobCombatTraining job)
    {
        //Tasks: Wander around, Find shooting position, go to shooting position, shoot, verify shot
        super(job);
        super.registerTargets(
          new AITarget(ARCHER_FIND_SHOOTING_STAND_POSITION, true, this::findShootingStandPosition),
          new AITarget(ARCHER_SELECT_TARGET, true, this::selectTarget),
          new AITarget(ARCHER_CHECK_SHOT, true, this::checkShot),
          new AITarget(ARCHER_SHOOT, true, this::shoot)

        );
        worker.getCitizenExperienceHandler().setSkillModifier(
          STRENGTH_MULTIPLIER * worker.getCitizenData().getStrength()
            + DEXTERITY_MULTIPLIER * worker.getCitizenData().getDexterity());
    }

    /**
     * Select a random target from the building.
     *
     * @return the next state to go to.
     */
    private AIState selectTarget()
    {
        setDelay(STANDARD_DELAY);
        final BuildingArchery archeryBuilding = getOwnBuilding();
        if (targetCounter >= archeryBuilding.getBuildingLevel() * BUILDING_LEVEL_TARGET_MULTIPLIER)
        {
            targetCounter = 0;
            return DECIDE;
        }
        final BlockPos targetPos = archeryBuilding.getRandomShootingTarget(worker.getRandom());
        if (targetPos == null)
        {
            return DECIDE;
        }

        currentShootingTarget = targetPos;
        targetCounter++;
        return ARCHER_SHOOT;
    }

    /**
     * Get a shooting stand position.
     *
     * @return the next state to go to.
     */
    private AIState findShootingStandPosition()
    {
        setDelay(STANDARD_DELAY);
        final BuildingArchery archeryBuilding = getOwnBuilding();
        final BlockPos shootingPos = archeryBuilding.getRandomShootingStandPosition(worker.getRandom());

        if (shootingPos == null)
        {
            return DECIDE;
        }

        currentPathingTarget = shootingPos;
        return ARCHER_GO_TO_SHOOTING_STAND;
    }

    /**
     * The ranged attack modus.
     *
     * @return the next state to go to.
     */
    protected AIState shoot()
    {
        if (worker.isHandActive())
        {
            setDelay(STANDARD_DELAY);
            WorkerUtil.faceBlock(currentShootingTarget, worker);
            //worker.face(target, (float) TURN_AROUND, (float) TURN_AROUND);
            worker.swingArm(EnumHand.MAIN_HAND);

            final EntityTippedArrow arrow = new GuardArrow(world, worker);
            final double xVector = currentShootingTarget.getX() - worker.posX;
            final double yVector = currentShootingTarget.getY() - arrow.posY;
            final double zVector = currentShootingTarget.getZ() - worker.posZ;
            final double distance = (double) MathHelper.sqrt(xVector * xVector + zVector * zVector);

            final double chance = HIT_CHANCE_DIVIDER / (worker.getCitizenData().getLevel() + 1);
            arrow.shoot(xVector, yVector + distance * RANGED_AIM_SLIGHTLY_HIGHER_MULTIPLIER, zVector, RANGED_VELOCITY, (float) chance);

            worker.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, (float) BASIC_VOLUME, (float) SoundUtils.getRandomPitch(worker.getRandom()));
            worker.world.spawnEntity(arrow);

            final double xDiff = currentShootingTarget.getX() - worker.posX;
            final double zDiff = currentShootingTarget.getZ() - worker.posZ;
            final double goToX = xDiff > 0 ? MOVE_MINIMAL : -MOVE_MINIMAL;
            final double goToZ = zDiff > 0 ? MOVE_MINIMAL : -MOVE_MINIMAL;
            worker.move(MoverType.SELF, goToX, 0, goToZ);

            if (worker.getRandom().nextBoolean())
            {
                worker.getCitizenItemHandler().damageItemInHand(EnumHand.MAIN_HAND, 1);
            }
            worker.resetActiveHand();
            this.incrementActionsDoneAndDecSaturation();
            arrowInProgress = arrow;
            currentAttackDelay = COMBAT_ATTACK_DELAY_BASE;
        }
        else
        {
            reduceAttackDelay();
            if (currentAttackDelay <= 0)
            {
                worker.setActiveHand(EnumHand.MAIN_HAND);
            }
            return ARCHER_SHOOT;
        }

        setDelay(CHECK_HIT_DELAY);
        return ARCHER_CHECK_SHOT;
    }

    /**
     * Reduces the attack delay by the given Tickrate
     */
    private void reduceAttackDelay()
    {
        if (currentAttackDelay > 0)
        {
            currentAttackDelay--;
        }
    }

    private AIState checkShot()
    {
        if (arrowInProgress.getDistanceSq(currentShootingTarget) < MIN_DISTANCE_FOR_SUCCESS)
        {
            worker.getCitizenExperienceHandler().addExperience(XP_PER_SUCCESSFUL_HIT);
        }
        else
        {
            worker.getCitizenExperienceHandler().addExperience(XP_BASE_RATE);
        }

        return ARCHER_SELECT_TARGET;
    }
}
