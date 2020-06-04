package com.minecolonies.coremod.entity.ai.citizen.trainingcamps;

import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCombatAcademy;
import com.minecolonies.coremod.colony.jobs.JobCombatTraining;
import com.minecolonies.coremod.util.NamedDamageSource;
import com.minecolonies.coremod.util.WorkerUtil;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
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
     * Creates the abstract part of the AI.inte
     * Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAICombatTraining(@NotNull final JobCombatTraining job)
    {
        // Tasks: Wander around, Find shooting position, go to shooting position, shoot, verify shot
        super(job);
        super.registerTargets(new AITarget(COMBAT_TRAINING, this::decideOnTrainingType, 1),
            new AITarget(FIND_TRAINING_PARTNER, this::findTrainingPartner, 1),
            new AITarget(KNIGHT_TRAIN_WITH_PARTNER, this::trainWithPartner, 1),
            new AITarget(FIND_DUMMY_PARTNER, this::findDummyPartner, 1),
            new AITarget(KNIGHT_ATTACK_DUMMY, this::attackDummy, 1),
            new AITarget(KNIGHT_ATTACK_PROTECT, this::attack, 1));
    }

    /**
     * Decide on which training type to pursue.
     *
     * @return the next state to go to.
     */
    private IAIState decideOnTrainingType()
    {
        setDelay(STANDARD_DELAY);
        if (getOwnBuilding().hasCombatPartner(worker) || worker.getRandom().nextInt(ONE_HUNDRED_PERCENT) < PARTNER_TRAINING_CHANCE)
        {
            return FIND_TRAINING_PARTNER;
        }
        return FIND_DUMMY_PARTNER;
    }

    @Override
    public IAIState decide()
    {
        if (getOwnBuilding().hasCombatPartner(worker))
        {
            setDelay(STANDARD_DELAY);
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
        setDelay(STANDARD_DELAY);
        final BuildingCombatAcademy academy = getOwnBuilding();
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
        setDelay(STANDARD_DELAY);
        if (trainingPartner == null)
        {
            return COMBAT_TRAINING;
        }

        if (BlockPosUtil.getDistance2D(worker.getPosition(), trainingPartner.getPosition()) > MIN_DISTANCE_TO_TRAIN
            && walkToBlock(trainingPartner.getPosition()))
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
        setDelay(STANDARD_DELAY);
        if (trainingPartner == null)
        {
            return START_WORKING;
        }

        if (BlockPosUtil.getDistance2D(worker.getPosition(), trainingPartner.getPosition()) > MIN_DISTANCE_TO_TRAIN)
        {
            currentPathingTarget = trainingPartner.getPosition();
            stateAfterPathing = KNIGHT_TRAIN_WITH_PARTNER;
            return GO_TO_TARGET;
        }

        if (currentAttackDelay <= 0)
        {
            worker.getCitizenExperienceHandler().addExperience(XP_BASE_RATE);
            worker.decreaseSaturationForAction();
            worker.faceEntity(trainingPartner, (float) TURN_AROUND, (float) TURN_AROUND);
            WorkerUtil.faceBlock(trainingPartner.getPosition().up(), worker);
            worker.resetActiveHand();

            if (worker.getRandom().nextBoolean())
            {
                final int shieldSlot = InventoryUtils.findFirstSlotInItemHandlerWith(getInventory(), Items.SHIELD);
                if (shieldSlot != -1)
                {
                    worker.playSound(SoundEvents.ITEM_SHIELD_BLOCK,
                        (float) BASIC_VOLUME,
                        (float) SoundUtils.getRandomPitch(worker.getRandom()));
                    worker.getCitizenItemHandler().setHeldItem(Hand.OFF_HAND, shieldSlot);
                    worker.setActiveHand(Hand.OFF_HAND);
                    worker.getLookController().setLookPositionWithEntity(trainingPartner, (float) TURN_AROUND, (float) TURN_AROUND);
                }
            }
            else
            {
                worker.swingArm(Hand.MAIN_HAND);
                worker.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                    (float) BASIC_VOLUME,
                    (float) SoundUtils.getRandomPitch(worker.getRandom()));
                trainingPartner.attackEntityFrom(new NamedDamageSource(worker.getName().getFormattedText(), worker), 0.0F);
                worker.getCitizenItemHandler().damageItemInHand(Hand.MAIN_HAND, 1);
            }
            worker.getNavigator().moveAwayFromXYZ(trainingPartner.getPosition(), 4.0, worker.getAIMoveSpeed());
            targetCounter++;

            if (targetCounter > getOwnBuilding().getBuildingLevel() * ACTIONS_PER_BUILDING_LEVEL)
            {
                getOwnBuilding().resetPartner(worker);
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

        setDelay(TRAININGS_DELAY);
        return KNIGHT_ATTACK_PROTECT;
    }

    /**
     * Find a dummy partner.
     * 
     * @return the next state to go to.
     */
    private IAIState findDummyPartner()
    {
        setDelay(STANDARD_DELAY);
        final BuildingCombatAcademy academy = getOwnBuilding();
        if (targetCounter >= academy.getBuildingLevel() * ACTIONS_PER_BUILDING_LEVEL)
        {
            worker.resetActiveHand();
            targetCounter = 0;
            return DECIDE;
        }

        if (getOwnBuilding().hasCombatPartner(worker))
        {
            setDelay(STANDARD_DELAY);
            return KNIGHT_TRAIN_WITH_PARTNER;
        }

        final BlockPos targetPos = academy.getRandomCombatTarget(worker.getRandom());
        if (targetPos == null)
        {
            worker.resetActiveHand();
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
        setDelay(STANDARD_DELAY);
        if (currentCombatTarget == null)
        {
            return START_WORKING;
        }

        if (currentAttackDelay <= 0)
        {
            worker.getCitizenExperienceHandler().addExperience(XP_BASE_RATE);
            worker.decreaseSaturationForAction();
            WorkerUtil.faceBlock(currentCombatTarget, worker);
            worker.resetActiveHand();

            if (worker.getRandom().nextBoolean())
            {
                final int shieldSlot = InventoryUtils.findFirstSlotInItemHandlerWith(getInventory(), Items.SHIELD);
                if (shieldSlot != -1)
                {
                    worker.playSound(SoundEvents.ITEM_SHIELD_BLOCK,
                        (float) BASIC_VOLUME,
                        (float) SoundUtils.getRandomPitch(worker.getRandom()));
                    worker.getCitizenItemHandler().setHeldItem(Hand.OFF_HAND, shieldSlot);
                    worker.setActiveHand(Hand.OFF_HAND);
                }
            }
            else
            {
                worker.swingArm(Hand.MAIN_HAND);
                worker.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                    (float) BASIC_VOLUME,
                    (float) SoundUtils.getRandomPitch(worker.getRandom()));
                worker.getCitizenItemHandler().damageItemInHand(Hand.MAIN_HAND, 1);
            }

            currentAttackDelay = RANGED_ATTACK_DELAY_BASE;
        }
        else
        {
            reduceAttackDelay();
            return KNIGHT_ATTACK_DUMMY;
        }

        setDelay(TRAININGS_DELAY);
        return FIND_DUMMY_PARTNER;
    }

    @Override
    protected boolean isSetup()
    {
        if (checkForToolOrWeapon(ToolType.SWORD))
        {
            setDelay(REQUEST_DELAY);
            return false;
        }

        if (checkForToolOrWeapon(ToolType.SHIELD))
        {
            setDelay(REQUEST_DELAY);
            return false;
        }

        final int weaponSlot = InventoryUtils
            .getFirstSlotOfItemHandlerContainingTool(getInventory(), ToolType.SWORD, 0, getOwnBuilding().getMaxToolLevel());
        if (weaponSlot != -1)
        {
            worker.getCitizenItemHandler().setHeldItem(Hand.MAIN_HAND, weaponSlot);
        }
        return true;
    }

    @Override
    public Class<BuildingCombatAcademy> getExpectedBuildingClass()
    {
        return BuildingCombatAcademy.class;
    }
}
