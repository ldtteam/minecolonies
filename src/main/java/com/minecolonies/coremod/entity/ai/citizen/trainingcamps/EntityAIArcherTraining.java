package com.minecolonies.coremod.entity.ai.citizen.trainingcamps;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingArchery;
import com.minecolonies.coremod.colony.jobs.JobArcherTraining;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIBasic;
import com.minecolonies.coremod.entity.ai.citizen.guard.GuardArrow;
import com.minecolonies.coremod.entity.ai.statemachine.AITarget;
import com.minecolonies.coremod.entity.ai.statemachine.states.IAIState;
import com.minecolonies.coremod.util.SoundUtils;
import com.minecolonies.coremod.util.WorkerUtil;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.CitizenConstants.TICKS_20;
import static com.minecolonies.api.util.constant.GuardConstants.*;
import static com.minecolonies.coremod.entity.ai.statemachine.states.AIWorkerState.*;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class EntityAIArcherTraining extends AbstractEntityAIBasic<JobArcherTraining>
{
    /**
     * Percentual chance for target search being chosen as target job.
     */
    private static final int TARGET_SEARCH_CHANCE = 10;

    /**
     * How often should intelligence factor into the archer's skill modifier.
     */
    private static final int INTELLIGENCE_MULTIPLIER = 2;

    /**
     * How often should dexterity factor into the archer's skill modifier.
     */
    private static final int STRENGTH_MULTIPLIER = 1;

    /**
     * 100% chance to compare it with smaller percentages.
     */
    private static final int ONE_HUNDRED_PERCENT = 100;

    /**
     * Xp per successful shot.
     */
    private static final int XP_PER_SUCCESSFUL_SHOT = 1;

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
    private static final int RANGED_ATTACK_DELAY_BASE = 10;

    /**
     * Base rate experience for every shot.
     */
    private static final double XP_BASE_RATE = 0.1;

    /**
     * Time to wait before analyzing the shot.
     */
    private static final int CHECK_SHOT_DELAY = TICKS_20 * 3;

    /**
     * The building range.
     */
    private AxisAlignedBB range;

    /**
     * The current pathing target to walk to.
     */
    private BlockPos currentPathingTarget;

    /**
     * Current target to shoot at.
     */
    private BlockPos currentShootingTarget;

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

    /**
     * Creates the abstract part of the AI.inte
     * Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAIArcherTraining(@NotNull final JobArcherTraining job)
    {
        //Tasks: Wander around, Find shooting position, go to shooting position, shoot, verify shot
        super(job);
        super.registerTargets(
          new AITarget(IDLE, () -> DECIDE),
          new AITarget(DECIDE, this::decide),
          new AITarget(ARCHER_WANDER, this::wander),
          new AITarget(ARCHER_FIND_SHOOTING_STAND_POSITION, this::findShootingStandPosition),
          new AITarget(ARCHER_GO_TO_SHOOTING_STAND, this::goToShootingStand),
          new AITarget(ARCHER_SELECT_TARGET, this::selectTarget),
          new AITarget(ARCHER_CHECK_SHOT, this::checkShot),
          new AITarget(ARCHER_SHOOT, this::shoot)

        );
        worker.getCitizenExperienceHandler().setSkillModifier(
          INTELLIGENCE_MULTIPLIER * worker.getCitizenData().getIntelligence()
            + STRENGTH_MULTIPLIER * worker.getCitizenData().getStrength());
        worker.setCanPickUpLoot(true);
    }

    /**
     * Decide on which state to go to.
     *
     * @return the next state to go to.
     */
    private IAIState decide()
    {
        if (checkForToolOrWeapon(ToolType.BOW))
        {
            setDelay(REQUEST_DELAY);
            return DECIDE;
        }

        final int bowSlot = InventoryUtils.getFirstSlotOfItemHandlerContainingTool(new InvWrapper(getInventory()), ToolType.BOW, 0, getOwnBuilding().getMaxToolLevel());
        worker.getCitizenItemHandler().setHeldItem(EnumHand.MAIN_HAND, bowSlot);
        setDelay(STANDARD_DELAY);

        if (worker.getRandom().nextInt(ONE_HUNDRED_PERCENT) < TARGET_SEARCH_CHANCE)
        {
            return ARCHER_FIND_SHOOTING_STAND_POSITION;
        }
        return ARCHER_WANDER;
    }

    /**
     * Wander randomly around within the premises of the building.
     *
     * @return the next state to go to.
     */
    private IAIState wander()
    {
        setDelay(STANDARD_DELAY);
        if (currentPathingTarget == null)
        {
            currentPathingTarget = getWanderPosition();
        }

        if (!walkToBlock(currentPathingTarget) || worker.getCitizenStuckHandler().isStuck())
        {
            currentPathingTarget = null;
            return DECIDE;
        }

        return ARCHER_WANDER;
    }

    /**
     * Walk to the shooting stand position.
     *
     * @return the next state to go to.
     */
    private IAIState goToShootingStand()
    {
        setDelay(STANDARD_DELAY);
        if (walkToBlock(currentPathingTarget, 1))
        {
            return getState();
        }
        return ARCHER_SELECT_TARGET;
    }

    /**
     * Select a random target from the building.
     *
     * @return the next state to go to.
     */
    private IAIState selectTarget()
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
    private IAIState findShootingStandPosition()
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
    protected IAIState shoot()
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
            currentAttackDelay = RANGED_ATTACK_DELAY_BASE;
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

        setDelay(CHECK_SHOT_DELAY);
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

    private IAIState checkShot()
    {
        if (arrowInProgress.getDistanceSq(currentShootingTarget) < MIN_DISTANCE_FOR_SUCCESS)
        {
            worker.getCitizenExperienceHandler().addExperience(XP_PER_SUCCESSFUL_SHOT);
        }
        else
        {
            worker.getCitizenExperienceHandler().addExperience(XP_BASE_RATE);
        }

        return ARCHER_SELECT_TARGET;
    }

    /**
     * Get a wander position within the archer training camp to walk to.
     *
     * @return the position or the location of the hut chest if not found.
     */
    private BlockPos getWanderPosition()
    {
        final BlockPos pos = BlockPosUtil.getRandomPosition(world, worker.getPosition(), getOwnBuilding().getLocation());

        if (range == null)
        {
            range = getOwnBuilding().getTargetableArea(world);
        }

        if (range.intersectsWithXZ(new Vec3d(pos)))
        {
            return pos;
        }

        return getOwnBuilding().getLocation();
    }
}
