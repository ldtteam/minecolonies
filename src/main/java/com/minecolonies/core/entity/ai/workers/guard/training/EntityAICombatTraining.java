package com.minecolonies.core.entity.ai.workers.guard.training;

import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.DamageSourceKeys;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingCombatAcademy;
import com.minecolonies.core.colony.jobs.JobCombatTraining;
import com.minecolonies.core.util.citizenutils.CitizenItemUtils;
import com.minecolonies.core.util.WorkerUtil;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.CitizenConstants.TICKS_20;
import static com.minecolonies.api.util.constant.GuardConstants.*;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class EntityAICombatTraining extends AbstractEntityAITraining<JobCombatTraining, BuildingCombatAcademy>
{
    /**
     * How many actions on one target are done per building level.
     */
    private static final int ACTIONS_PER_BUILDING_LEVEL = 5;

    /**
     * Base rate experience for every shot.
     */
    private static final double XP_BASE_RATE = 2;

    /**
     * Chance for a guard to do partner training.
     */
    private static final int PARTNER_TRAINING_CHANCE = 25;

    /**
     * Trainings delay between hit an defend.
     */
    private static final int TRAININGS_DELAY = TICKS_20 * 3;

    /**
     * Min distance to train with the other guard.
     */
    private static final int MIN_DISTANCE_TO_TRAIN = 5;

    /**
     * The current pathing target to walk to.
     */
    private BlockPos currentCombatTarget;

    /**
     * The current training partner of this guard.
     */
    private AbstractEntityCitizen trainingPartner;

    /**
     * Counter of how often we tried to hit the target.
     */
    private int targetCounter;

    /**
     * Creates the abstract part of the AI.inte Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAICombatTraining(@NotNull final JobCombatTraining job)
    {
        //Tasks: Wander around, Find shooting position, go to shooting position, shoot, verify shot
        super(job);
        super.registerTargets(
          new AITarget(COMBAT_TRAINING, this::decideOnTrainingType, 20),
          new AITarget(FIND_TRAINING_PARTNER, this::findTrainingPartner, 20),
          new AITarget(KNIGHT_TRAIN_WITH_PARTNER, this::trainWithPartner, 20),
          new AITarget(FIND_DUMMY_PARTNER, this::findDummyPartner, 20),
          new AITarget(KNIGHT_ATTACK_DUMMY, this::attackDummy, 20),
          new AITarget(KNIGHT_ATTACK_PROTECT, this::attack, TRAININGS_DELAY)
        );
    }

    /**
     * Decide on which training type to pursue.
     *
     * @return the next state to go to.
     */
    private IAIState decideOnTrainingType()
    {
        if (building.hasCombatPartner(worker) || worker.getRandom().nextInt(ONE_HUNDRED_PERCENT) < PARTNER_TRAINING_CHANCE)
        {
            return FIND_TRAINING_PARTNER;
        }
        return FIND_DUMMY_PARTNER;
    }

    @Override
    public IAIState decide()
    {
        if (building.hasCombatPartner(worker))
        {
            return KNIGHT_TRAIN_WITH_PARTNER;
        }
        return super.decide();
    }

    /**
     * Find a training partner to train with.
     *
     * @return the next state to go to.
     */
    private IAIState findTrainingPartner()
    {
        final BuildingCombatAcademy academy = building;
        if (academy.hasCombatPartner(worker))
        {
            trainingPartner = academy.getCombatPartner(worker);
        }
        else
        {
            trainingPartner = academy.getRandomCombatPartner(worker);
        }

        if (trainingPartner == null)
        {
            return COMBAT_TRAINING;
        }
        return KNIGHT_TRAIN_WITH_PARTNER;
    }

    /**
     * Train with a partner. Find the partner and path to him.
     *
     * @return the next state to go to.
     */
    private IAIState trainWithPartner()
    {
        if (trainingPartner == null || !getModuleForJob().getAssignedCitizen().contains(trainingPartner.getCitizenData()))
        {
            trainingPartner = null;
            return COMBAT_TRAINING;
        }

        if (BlockPosUtil.getDistance2D(worker.blockPosition(), trainingPartner.blockPosition()) > MIN_DISTANCE_TO_TRAIN && walkToBlock(trainingPartner.blockPosition()))
        {
            return KNIGHT_TRAIN_WITH_PARTNER;
        }

        return KNIGHT_ATTACK_PROTECT;
    }

    /**
     * Attack the training partner or block.
     *
     * @return the next state to go to.
     */
    private IAIState attack()
    {
        if (trainingPartner == null)
        {
            return START_WORKING;
        }

        if (BlockPosUtil.getDistance2D(worker.blockPosition(), trainingPartner.blockPosition()) > MIN_DISTANCE_TO_TRAIN)
        {
            currentPathingTarget = trainingPartner.blockPosition();
            stateAfterPathing = KNIGHT_TRAIN_WITH_PARTNER;
            return GO_TO_TARGET;
        }

        if (currentAttackDelay <= 0)
        {
            worker.getCitizenExperienceHandler().addExperience(XP_BASE_RATE);
            worker.decreaseSaturationForAction();
            worker.lookAt(trainingPartner, (float) TURN_AROUND, (float) TURN_AROUND);
            WorkerUtil.faceBlock(trainingPartner.blockPosition().above(), worker);
            worker.stopUsingItem();

            if (worker.getRandom().nextBoolean())
            {
                final int shieldSlot = InventoryUtils.findFirstSlotInItemHandlerWith(getInventory(), Items.SHIELD);
                if (shieldSlot != -1)
                {
                    worker.playSound(SoundEvents.SHIELD_BLOCK, (float) BASIC_VOLUME, (float) SoundUtils.getRandomPitch(worker.getRandom()));
                    CitizenItemUtils.setHeldItem(worker, InteractionHand.OFF_HAND, shieldSlot);
                    worker.startUsingItem(InteractionHand.OFF_HAND);
                    worker.getLookControl().setLookAt(trainingPartner, (float) TURN_AROUND, (float) TURN_AROUND);
                }
            }
            else
            {
                worker.swing(InteractionHand.MAIN_HAND);
                worker.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, (float) BASIC_VOLUME, (float) SoundUtils.getRandomPitch(worker.getRandom()));
                trainingPartner.hurt(world.damageSources().source(DamageSourceKeys.TRAINING, worker), 0.0F);
                CitizenItemUtils.damageItemInHand(worker, InteractionHand.MAIN_HAND, 1);
            }
            worker.getNavigation().moveAwayFromXYZ(trainingPartner.blockPosition(), 4.0, 1.0, true);
            targetCounter++;

            if (targetCounter > building.getBuildingLevel() * ACTIONS_PER_BUILDING_LEVEL)
            {
                building.resetPartner(worker);
                targetCounter = 0;
                return START_WORKING;
            }
            currentAttackDelay = RANGED_ATTACK_DELAY_BASE;
        }
        else
        {
            reduceAttackDelay();
            return KNIGHT_ATTACK_PROTECT;
        }

        return KNIGHT_ATTACK_PROTECT;
    }

    /**
     * Find a dummy partner.
     *
     * @return the next state to go to.
     */
    private IAIState findDummyPartner()
    {
        final BuildingCombatAcademy academy = building;
        if (targetCounter >= academy.getBuildingLevel() * ACTIONS_PER_BUILDING_LEVEL)
        {
            worker.stopUsingItem();
            targetCounter = 0;
            return DECIDE;
        }

        if (building.hasCombatPartner(worker))
        {
            return KNIGHT_TRAIN_WITH_PARTNER;
        }

        final BlockPos targetPos = academy.getRandomCombatTarget(worker.getRandom());
        if (targetPos == null)
        {
            worker.stopUsingItem();
            return DECIDE;
        }

        currentCombatTarget = targetPos;
        targetCounter++;

        currentPathingTarget = targetPos;
        stateAfterPathing = KNIGHT_ATTACK_DUMMY;
        return GO_TO_TARGET;
    }

    /**
     * Attack the dummy.
     *
     * @return the next state to go to.
     */
    private IAIState attackDummy()
    {
        if (currentCombatTarget == null)
        {
            return START_WORKING;
        }

        if (currentAttackDelay <= 0)
        {
            worker.getCitizenExperienceHandler().addExperience(XP_BASE_RATE);
            worker.decreaseSaturationForAction();
            WorkerUtil.faceBlock(currentCombatTarget, worker);
            worker.stopUsingItem();

            if (worker.getRandom().nextBoolean())
            {
                final int shieldSlot = InventoryUtils.findFirstSlotInItemHandlerWith(getInventory(),
                  Items.SHIELD);
                if (shieldSlot != -1)
                {
                    worker.playSound(SoundEvents.SHIELD_BLOCK, (float) BASIC_VOLUME, (float) SoundUtils.getRandomPitch(worker.getRandom()));
                    CitizenItemUtils.setHeldItem(worker, InteractionHand.OFF_HAND, shieldSlot);
                    worker.startUsingItem(InteractionHand.OFF_HAND);
                }
            }
            else
            {
                worker.swing(InteractionHand.MAIN_HAND);
                worker.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, (float) BASIC_VOLUME, (float) SoundUtils.getRandomPitch(worker.getRandom()));
                CitizenItemUtils.damageItemInHand(worker, InteractionHand.MAIN_HAND, 1);
            }

            currentAttackDelay = RANGED_ATTACK_DELAY_BASE;
        }
        else
        {
            reduceAttackDelay();
            return KNIGHT_ATTACK_DUMMY;
        }

        return FIND_DUMMY_PARTNER;
    }

    @Override
    protected boolean isSetup()
    {
        if (checkForToolOrWeapon(ModEquipmentTypes.sword.get()))
        {
            return false;
        }

        if (checkForToolOrWeapon(ModEquipmentTypes.shield.get()))
        {
            return false;
        }

        final int weaponSlot = InventoryUtils.getFirstSlotOfItemHandlerContainingEquipment(getInventory(), ModEquipmentTypes.sword.get(), 0, building.getMaxEquipmentLevel());
        if (weaponSlot != -1)
        {
            CitizenItemUtils.setHeldItem(worker, InteractionHand.MAIN_HAND, weaponSlot);
        }
        return true;
    }

    @Override
    public Class<BuildingCombatAcademy> getExpectedBuildingClass()
    {
        return BuildingCombatAcademy.class;
    }
}
