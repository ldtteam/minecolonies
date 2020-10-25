package com.minecolonies.coremod.entity.ai.citizen.trainingcamps;

import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingArchery;
import com.minecolonies.coremod.colony.jobs.JobArcherTraining;
import com.minecolonies.coremod.util.WorkerUtil;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.CitizenConstants.TICKS_20;
import static com.minecolonies.api.util.constant.GuardConstants.*;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class EntityAIArcherTraining extends AbstractEntityAITraining<JobArcherTraining, BuildingArchery>
{
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
    private static final double XP_BASE_RATE = 2;

    /**
     * Time to wait before analyzing the shot.
     */
    private static final int CHECK_SHOT_DELAY = TICKS_20 * 3;

    /**
     * Shooting icon
     */
    private final static VisibleCitizenStatus ARCHER_TRAIN =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/archer_uni.png"), "com.minecolonies.gui.visiblestatus.archer_uni");

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
    private ArrowEntity arrowInProgress;

    /**
     * Creates the abstract part of the AI.inte Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAIArcherTraining(@NotNull final JobArcherTraining job)
    {
        //Tasks: Wander around, Find shooting position, go to shooting position, shoot, verify shot
        super(job);
        super.registerTargets(
          new AITarget(COMBAT_TRAINING, this::findShootingStandPosition, 1),
          new AITarget(ARCHER_SELECT_TARGET, this::selectTarget, 1),
          new AITarget(ARCHER_CHECK_SHOT, this::checkShot, 1),
          new AITarget(ARCHER_SHOOT, this::shoot, 1)

        );
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

        worker.getCitizenData().setVisibleStatus(ARCHER_TRAIN);
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

        stateAfterPathing = ARCHER_SELECT_TARGET;
        currentPathingTarget = shootingPos;
        return GO_TO_TARGET;
    }

    /**
     * The ranged attack modus
     *
     * @return the next state to go to.
     */
    protected IAIState shoot()
    {
        setDelay(STANDARD_DELAY);
        if (currentShootingTarget == null || !isSetup())
        {
            worker.getCitizenData().setVisibleStatus(VisibleCitizenStatus.WORKING);
            return START_WORKING;
        }

        if (worker.isHandActive())
        {
            WorkerUtil.faceBlock(currentShootingTarget, worker);
            worker.swingArm(Hand.MAIN_HAND);

            final ArrowEntity arrow = ModEntities.MC_NORMAL_ARROW.create(world);
            arrow.setDamage(0);
            arrow.setShooter(worker);
            arrow.setPosition(worker.getPosX(), worker.getPosY() + 1, worker.getPosZ());
            final double xVector = currentShootingTarget.getX() - worker.getPosX();
            final double yVector = currentShootingTarget.getY() - arrow.posY;
            final double zVector = currentShootingTarget.getZ() - worker.getPosZ();
            final double distance = (double) MathHelper.sqrt(xVector * xVector + zVector * zVector);

            final double chance = HIT_CHANCE_DIVIDER / (getPrimarySkillLevel()/2.0 + 1);
            arrow.shoot(xVector, yVector + distance * RANGED_AIM_SLIGHTLY_HIGHER_MULTIPLIER, zVector, RANGED_VELOCITY, (float) chance);

            worker.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, (float) BASIC_VOLUME, (float) SoundUtils.getRandomPitch(worker.getRandom()));
            worker.world.addEntity(arrow);

            final double xDiff = currentShootingTarget.getX() - worker.getPosX();
            final double zDiff = currentShootingTarget.getZ() - worker.getPosZ();
            final double goToX = xDiff > 0 ? MOVE_MINIMAL : -MOVE_MINIMAL;
            final double goToZ = zDiff > 0 ? MOVE_MINIMAL : -MOVE_MINIMAL;
            worker.move(MoverType.SELF, new Vec3d(goToX, 0, goToZ));

            if (worker.getRandom().nextBoolean())
            {
                worker.getCitizenItemHandler().damageItemInHand(Hand.MAIN_HAND, 1);
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
                worker.setActiveHand(Hand.MAIN_HAND);
            }
            return ARCHER_SHOOT;
        }

        setDelay(CHECK_SHOT_DELAY);
        return ARCHER_CHECK_SHOT;
    }

    private IAIState checkShot()
    {
        if (arrowInProgress.getDistanceSq(new Vec3d(currentShootingTarget)) < MIN_DISTANCE_FOR_SUCCESS)
        {
            worker.getCitizenExperienceHandler().addExperience(XP_PER_SUCCESSFUL_SHOT);
        }
        else
        {
            worker.getCitizenExperienceHandler().addExperience(XP_BASE_RATE);
        }

        worker.getCitizenData().setVisibleStatus(VisibleCitizenStatus.WORKING);
        return ARCHER_SELECT_TARGET;
    }

    @Override
    protected boolean isSetup()
    {
        if (checkForToolOrWeapon(ToolType.BOW))
        {
            setDelay(REQUEST_DELAY);
            return false;
        }

        final int bowSlot = InventoryUtils.getFirstSlotOfItemHandlerContainingTool(getInventory(), ToolType.BOW, 0, getOwnBuilding().getMaxToolLevel());
        worker.getCitizenItemHandler().setHeldItem(Hand.MAIN_HAND, bowSlot);
        return true;
    }

    @Override
    public Class<BuildingArchery> getExpectedBuildingClass()
    {
        return BuildingArchery.class;
    }
}
