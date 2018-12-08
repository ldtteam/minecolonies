package com.minecolonies.coremod.entity.ai.citizen.trainingcamps;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.blockout.Log;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCombatAcademy;
import com.minecolonies.coremod.colony.jobs.JobCombatTraining;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import com.minecolonies.coremod.util.SoundUtils;
import com.minecolonies.coremod.util.WorkerUtil;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemShield;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.wrapper.InvWrapper;
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
     * How many actions on one target are done per building level.
     */
    private static final int ACTIONS_PER_BUILDING_LEVEL = 5;

    /**
     * Base rate experience for every shot.
     */
    private static final double XP_BASE_RATE = 0.1;

    /**
     * Chance for a guard to do partner training.
     */
    private static final int PARTNER_TRAINING_CHANCE = 10;

    /**
     * Trainings delay between hit an defend.
     */
    private static final int TRAININGS_DELAY = TICKS_20 * 3;

    /**
     * The current pathing target to walk to.
     */
    private BlockPos currentCombatTarget;

    /**
     * The current training partner of this guard.
     */
    private EntityCitizen trainingPartner;

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
        //Tasks: Wander around, Find shooting position, go to shooting position, shoot, verify shot
        super(job);
        super.registerTargets(
          new AITarget(COMBAT_TRAINING, true, this::decideOnTrainingType),
          new AITarget(FIND_TRAINING_PARTNER, true, this::findTrainingPartner),
          new AITarget(KNIGHT_TRAIN_WITH_PARTNER, true, this::trainWithPartner),
          new AITarget(FIND_DUMMY_PARTNER, true, this::findDummyPartner),
          new AITarget(KNIGHT_ATTACK_DUMMY, true, this::attackDummy),
          new AITarget(KNIGHT_ATTACK_PROTECT, true, this::attack)
        );
        worker.getCitizenExperienceHandler().setSkillModifier(
          STRENGTH_MULTIPLIER * worker.getCitizenData().getStrength()
            + DEXTERITY_MULTIPLIER * worker.getCitizenData().getDexterity());
    }

    /**
     * Decide on which training type to pursue.
     *
     * @return the next state to go to.
     */
    private AIState decideOnTrainingType()
    {
        setDelay(STANDARD_DELAY);
        if (getOwnBuilding(BuildingCombatAcademy.class).hasCombatPartner(worker) || worker.getRandom().nextInt(ONE_HUNDRED_PERCENT) < PARTNER_TRAINING_CHANCE)
        {
            return FIND_TRAINING_PARTNER;
        }
        return FIND_DUMMY_PARTNER;
    }

    /**
     * Find a training partner to train with.
     *
     * @return the next state to go to.
     */
    private AIState findTrainingPartner()
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
     * @return the next state to go to.
     */
    private AIState trainWithPartner()
    {
        setDelay(worker.getRandom().nextInt(STANDARD_DELAY * STANDARD_DELAY));
        if (trainingPartner == null)
        {
            return COMBAT_TRAINING;
        }

        if (BlockPosUtil.getDistance2D(worker.getPosition(), trainingPartner.getPosition()) > 5.0)
        {
            currentPathingTarget = trainingPartner.getPosition();
            stateAfterPathing = KNIGHT_TRAIN_WITH_PARTNER;
            return GO_TO_TARGET;
        }

        return KNIGHT_ATTACK_PROTECT;
    }

    /**
     * Attack the training partner or block.
     * @return the next state to go to.
     */
    private AIState attack()
    {
        if (trainingPartner == null)
        {
            return START_WORKING;
        }

        if (BlockPosUtil.getDistance2D(worker.getPosition(), trainingPartner.getPosition()) > 5.0)
        {
            currentPathingTarget = trainingPartner.getPosition();
            stateAfterPathing = KNIGHT_TRAIN_WITH_PARTNER;
            return GO_TO_TARGET;
        }

        if (worker.isHandActive())
        {
            worker.getCitizenExperienceHandler().addExperience(XP_BASE_RATE);
            worker.decreaseSaturationForContinuousAction();
            worker.faceEntity(trainingPartner, (float) TURN_AROUND, (float) TURN_AROUND);
            worker.resetActiveHand();

            if (worker.getRandom().nextBoolean())
            {
                final int shieldSlot = InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(getInventory()),
                  Items.SHIELD,
                  -1);
                Log.getLogger().warn("shield");
                if (shieldSlot != -1)
                {
                    worker.playSound(SoundEvents.ITEM_SHIELD_BLOCK, (float) BASIC_VOLUME, (float) SoundUtils.getRandomPitch(worker.getRandom()));
                    worker.getCitizenItemHandler().setHeldItem(EnumHand.OFF_HAND, shieldSlot);
                    worker.setActiveHand(EnumHand.OFF_HAND);
                    worker.getLookHelper().setLookPositionWithEntity(trainingPartner, (float) TURN_AROUND, (float) TURN_AROUND);
                }
            }
            else
            {
                worker.swingArm(EnumHand.MAIN_HAND);
                worker.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, (float) BASIC_VOLUME, (float) SoundUtils.getRandomPitch(worker.getRandom()));
                trainingPartner.attackEntityFrom(new DamageSource(worker.getName()), 0.0F);
                worker.getCitizenItemHandler().damageItemInHand(EnumHand.MAIN_HAND, 1);
            }

            targetCounter++;

            if (targetCounter > getOwnBuilding().getBuildingLevel() * ACTIONS_PER_BUILDING_LEVEL)
            {
                targetCounter = 0;
                return START_WORKING;
            }
        }
        else
        {
            reduceAttackDelay();
            if (currentAttackDelay <= 0)
            {
                worker.setActiveHand(EnumHand.MAIN_HAND);
            }
            return KNIGHT_ATTACK_PROTECT;
        }

        worker.resetActiveHand();
        setDelay(TRAININGS_DELAY);
        return KNIGHT_ATTACK_PROTECT;
    }

    /**
     * Find a dummy partner.
     * @return the next state to go to.
     */
    private AIState findDummyPartner()
    {
        setDelay(STANDARD_DELAY);
        final BuildingCombatAcademy academy = getOwnBuilding();
        if (targetCounter >= academy.getBuildingLevel() * ACTIONS_PER_BUILDING_LEVEL)
        {
            targetCounter = 0;
            return DECIDE;
        }

        final BlockPos targetPos = academy.getRandomCombatTarget(worker.getRandom());
        if (targetPos == null)
        {
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
     * @return the next state to go to.
     */
    private AIState attackDummy()
    {
        if (currentCombatTarget == null)
        {
            return START_WORKING;
        }

        if (currentAttackDelay <= 0)
        {
            worker.getCitizenExperienceHandler().addExperience(XP_BASE_RATE);
            worker.decreaseSaturationForContinuousAction();
            WorkerUtil.faceBlock(currentCombatTarget, worker);
            worker.resetActiveHand();

            if (worker.getRandom().nextBoolean())
            {
                final int shieldSlot = InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(getInventory()),
                  Items.SHIELD,
                  -1);
                if (shieldSlot != -1)
                {
                    worker.playSound(SoundEvents.ITEM_SHIELD_BLOCK, (float) BASIC_VOLUME, (float) SoundUtils.getRandomPitch(worker.getRandom()));
                    worker.getCitizenItemHandler().setHeldItem(EnumHand.OFF_HAND, shieldSlot);
                    worker.setActiveHand(EnumHand.OFF_HAND);
                }
            }
            else
            {
                worker.swingArm(EnumHand.MAIN_HAND);
                worker.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, (float) BASIC_VOLUME, (float) SoundUtils.getRandomPitch(worker.getRandom()));
                worker.getCitizenItemHandler().damageItemInHand(EnumHand.MAIN_HAND, 1);
            }
            currentAttackDelay = RANGED_ATTACK_DELAY_BASE;
        }
        else
        {
            reduceAttackDelay();
            return KNIGHT_ATTACK_DUMMY;
        }

        worker.resetActiveHand();
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

        final int weaponSlot = InventoryUtils.getFirstSlotOfItemHandlerContainingTool(new InvWrapper(getInventory()), ToolType.SWORD, 0, getOwnBuilding().getMaxToolLevel());
        if (weaponSlot != -1)
        {
            worker.getCitizenItemHandler().setHeldItem(EnumHand.MAIN_HAND, weaponSlot);
        }
        return true;
    }
}
