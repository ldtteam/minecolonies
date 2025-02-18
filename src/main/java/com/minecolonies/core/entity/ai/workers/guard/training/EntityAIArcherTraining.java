package com.minecolonies.core.entity.ai.workers.guard.training;

import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingArchery;
import com.minecolonies.core.colony.jobs.JobArcherTraining;
import com.minecolonies.core.util.citizenutils.CitizenItemUtils;
import com.minecolonies.core.util.WorkerUtil;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.CitizenConstants.TICKS_20;
import static com.minecolonies.api.util.constant.Constants.HALF_BLOCK;
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
    private static final double XP_BASE_RATE = 0.2;

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
    private Arrow arrowInProgress;

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
          new AITarget(COMBAT_TRAINING, this::findShootingStandPosition, STANDARD_DELAY),
          new AITarget(ARCHER_SELECT_TARGET, this::selectTarget, STANDARD_DELAY),
          new AITarget(ARCHER_CHECK_SHOT, this::checkShot, CHECK_SHOT_DELAY),
          new AITarget(ARCHER_SHOOT, this::shoot, STANDARD_DELAY)

        );
    }

    /**
     * Select a random target from the building.
     *
     * @return the next state to go to.
     */
    private IAIState selectTarget()
    {
        final BuildingArchery archeryBuilding = building;
        if (targetCounter >= archeryBuilding.getBuildingLevel() * BUILDING_LEVEL_TARGET_MULTIPLIER)
        {
            targetCounter = 0;
            return DECIDE;
        }
        final BlockPos targetPos = archeryBuilding.getRandomShootingTarget(worker.getRandom());
        if (targetPos == null || !WorldUtil.isBlockLoaded(world, targetPos) ||
              !world.clip(new ClipContext(new Vec3(worker.getX(), worker.getEyeY(), worker.getZ()), new Vec3(targetPos.getX() + HALF_BLOCK, targetPos.getY() + HALF_BLOCK, targetPos.getZ() + HALF_BLOCK), ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, worker)).getBlockPos().equals(targetPos))
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
        final BuildingArchery archeryBuilding = building;
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
        if (currentShootingTarget == null || !isSetup())
        {
            worker.getCitizenData().setVisibleStatus(VisibleCitizenStatus.WORKING);
            return START_WORKING;
        }

        if (worker.isUsingItem())
        {
            WorkerUtil.faceBlock(currentShootingTarget, worker);
            worker.swing(InteractionHand.MAIN_HAND);

            final Arrow arrow = ModEntities.MC_NORMAL_ARROW.create(world);
            arrow.setBaseDamage(0);
            arrow.setOwner(worker);
            arrow.setPos(worker.getX(), worker.getY() + 1, worker.getZ());
            final double xVector = currentShootingTarget.getX() - worker.getX();
            final double yVector = currentShootingTarget.getY() - arrow.getY();
            final double zVector = currentShootingTarget.getZ() - worker.getZ();
            final double distance = (double) Mth.sqrt((float) (xVector * xVector + zVector * zVector));

            final double chance = HIT_CHANCE_DIVIDER / (getPrimarySkillLevel() / 2.0 + 1);
            arrow.shoot(xVector, yVector + distance * RANGED_AIM_SLIGHTLY_HIGHER_MULTIPLIER, zVector, RANGED_VELOCITY, (float) chance);

            worker.playSound(SoundEvents.SKELETON_SHOOT, (float) BASIC_VOLUME, (float) SoundUtils.getRandomPitch(worker.getRandom()));
            worker.level.addFreshEntity(arrow);

            final double xDiff = currentShootingTarget.getX() - worker.getX();
            final double zDiff = currentShootingTarget.getZ() - worker.getZ();
            final double goToX = xDiff > 0 ? MOVE_MINIMAL : -MOVE_MINIMAL;
            final double goToZ = zDiff > 0 ? MOVE_MINIMAL : -MOVE_MINIMAL;
            worker.move(MoverType.SELF, new Vec3(goToX, 0, goToZ));

            if (worker.getRandom().nextBoolean())
            {
                CitizenItemUtils.damageItemInHand(worker, InteractionHand.MAIN_HAND, 1);
            }
            worker.stopUsingItem();
            this.incrementActionsDoneAndDecSaturation();
            arrowInProgress = arrow;
            currentAttackDelay = RANGED_ATTACK_DELAY_BASE;
        }
        else
        {
            reduceAttackDelay();
            if (currentAttackDelay <= 0)
            {
                worker.startUsingItem(InteractionHand.MAIN_HAND);
            }
            return ARCHER_SHOOT;
        }

        return ARCHER_CHECK_SHOT;
    }

    private IAIState checkShot()
    {
        if (arrowInProgress.distanceToSqr(new Vec3(currentShootingTarget.getX(), currentShootingTarget.getY(), currentShootingTarget.getZ())) < MIN_DISTANCE_FOR_SUCCESS)
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
        if (checkForToolOrWeapon(ModEquipmentTypes.bow.get()))
        {
            setDelay(REQUEST_DELAY);
            return false;
        }

        final int bowSlot = InventoryUtils.getFirstSlotOfItemHandlerContainingEquipment(getInventory(), ModEquipmentTypes.bow.get(), 0, building.getMaxEquipmentLevel());
        CitizenItemUtils.setHeldItem(worker, InteractionHand.MAIN_HAND, bowSlot);
        return true;
    }

    @Override
    public Class<BuildingArchery> getExpectedBuildingClass()
    {
        return BuildingArchery.class;
    }
}
