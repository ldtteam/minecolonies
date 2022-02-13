package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.api.colony.guardtype.registry.ModGuardTypes;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.entity.combat.CombatAIStates;
import com.minecolonies.api.entity.pathfinding.PathResult;
import com.minecolonies.api.entity.pathfinding.PathingOptions;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.buildings.modules.settings.GuardTaskSetting;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.entity.DruidPotionEntity;
import com.minecolonies.coremod.entity.ai.combat.AttackMoveAI;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.entity.pathfinding.MinecoloniesAdvancedPathNavigate;
import com.minecolonies.coremod.entity.pathfinding.pathjobs.AbstractPathJob;
import com.minecolonies.coremod.entity.pathfinding.pathjobs.PathJobCanSee;
import com.minecolonies.coremod.entity.pathfinding.pathjobs.PathJobMoveToLocation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.*;
import net.minecraft.util.Hand;

import java.util.Collections;
import java.util.List;

import static com.minecolonies.api.util.constant.GuardConstants.*;
import static com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIFight.SPEED_LEVEL_BONUS;

/**
 * Druid combat AI
 */
public class DruidCombatAI extends AttackMoveAI<EntityCitizen>
{
    private final AbstractEntityAIGuard parentAI;

    /**
     * The value of the speed which the guard will move.
     */
    private static final double COMBAT_SPEED = 1.0;

    public static final  float  POTION_VELOCITY                           = 0.5f;

    /**
     * Flee chance
     */
    private static final int FLEE_CHANCE = 3;

    private final PathingOptions combatPathingOptions;

    public DruidCombatAI(
      final EntityCitizen owner,
      final ITickRateStateMachine stateMachine,
      final AbstractEntityAIGuard parentAI)
    {
        super(owner, stateMachine);

        this.parentAI = parentAI;
        combatPathingOptions = new PathingOptions();
        combatPathingOptions.setEnterDoors(true);
        combatPathingOptions.setCanOpenDoors(true);
        combatPathingOptions.setCanSwim(true);
        combatPathingOptions.withOnPathCost(0.8);
        combatPathingOptions.withJumpCost(0.01);
        combatPathingOptions.withDropCost(0.9);
    }

    @Override
    protected void doAttack(final LivingEntity target)
    {
        if (user.distanceToSqr(target) < RANGED_FLEE_SQDIST)
        {
            if (user.getRandom().nextInt(FLEE_CHANCE) == 0 &&
                  !((AbstractBuildingGuards) user.getCitizenData().getWorkBuilding()).getTask().equals(GuardTaskSetting.GUARD))
            {
                user.getNavigation().moveAwayFromLivingEntity(target, getAttackDistance() / 2.0, getCombatMovementSpeed());
            }
        }
        else
        {
            user.getNavigation().stop();
        }

        user.swing(Hand.MAIN_HAND);

        final int level = user.getCitizenData().getCitizenSkillHandler().getLevel(ModGuardTypes.druid.getSecondarySkill());
        final int time = user.getCitizenData().getCitizenSkillHandler().getLevel(ModGuardTypes.druid.getPrimarySkill());

        final float inaccuracy = 99f / level;
        final Effect effect;
        final ItemStack stack = new ItemStack(Items.SPLASH_POTION);
        //todo if mistletoe and water, increase Effectinstance to 1
        if (AbstractEntityAIGuard.isAttackableTarget(user, target))
        {
            switch (user.getRandom().nextInt(2))
            {
                case 0:
                    effect = Effects.MOVEMENT_SLOWDOWN;
                    break;
                default:
                    effect = Effects.WEAKNESS;
                    break;
            }

            PotionUtils.setCustomEffects(stack, Collections.singleton(new EffectInstance(effect, time)));
            DruidPotionEntity.throwPotionAt(stack, target, user, user.getCommandSenderWorld(), POTION_VELOCITY, inaccuracy, ((entity, eff) -> AbstractEntityAIGuard.isAttackableTarget(user, entity)));
        }
        else
        {
            //todo if we got a mistletoe + water bottle do 4.
            switch (user.getRandom().nextInt(2))
            {
                case 0:
                    effect = Effects.SATURATION;
                    break;
                case 1:
                    effect = Effects.DAMAGE_BOOST;
                    break;
                case 2:
                    effect = Effects.HEAL;
                    break;
                default:
                    effect = Effects.DAMAGE_RESISTANCE;
                    break;
            }

            PotionUtils.setCustomEffects(stack, Collections.singleton(new EffectInstance(effect, time)));
            DruidPotionEntity.throwPotionAt(stack, target, user, user.getCommandSenderWorld(), POTION_VELOCITY, inaccuracy, ((entity, eff) -> !AbstractEntityAIGuard.isAttackableTarget(user, entity)));
        }

        user.setItemInHand(Hand.MAIN_HAND, stack);



        //slowness/Saturation + strength/weakness randomly (all level 1)

        //if research unlocked and has bottles + mistletoes to consume then level 2 potion effect
        //potion effect duration = mana level (up to 99s at level 99)
        //research to unlock regeneration + resistance (those need bottles + mistletoes)

        resetTarget();

        parentAI.incrementActionsDoneAndDecSaturation();
        user.getCitizenExperienceHandler().addExperience(EXP_PER_MOB_DEATH);
    }

    @Override
    protected double getAttackDistance()
    {
        int attackDist = BASE_DISTANCE_FOR_POTION_ATTACK;
        // + 1 Blockrange per building level for a total of +5 from building level
        if (user.getCitizenData().getWorkBuilding() != null)
        {
            attackDist += user.getCitizenData().getWorkBuilding().getBuildingLevel();
        }

        if (target != null)
        {
            attackDist += user.getY() - target.getY();
        }

        return attackDist;
    }

    @Override
    protected PathResult moveInAttackPosition(final LivingEntity target)
    {
        if (((AbstractBuildingGuards) user.getCitizenData().getWorkBuilding()).getTask().equals(GuardTaskSetting.GUARD))
        {
            final PathJobCanSee job = new PathJobCanSee(user, target, user.level, ((AbstractBuildingGuards) user.getCitizenData().getWorkBuilding()).getGuardPos(), 40);
            final PathResult pathResult = ((MinecoloniesAdvancedPathNavigate) user.getNavigation()).setPathJob(job, null, getCombatMovementSpeed(), true);
            job.setPathingOptions(combatPathingOptions);
            return pathResult;
        }

        final PathJobMoveToLocation job = new PathJobMoveToLocation(user.level, AbstractPathJob.prepareStart(user), target.blockPosition(), 200, user);
        final PathResult pathResult = ((MinecoloniesAdvancedPathNavigate) user.getNavigation()).setPathJob(job, null, getCombatMovementSpeed(), true);
        job.setPathingOptions(combatPathingOptions);
        return pathResult;
    }

    /**
     * Get combat speed
     *
     * @return movent speed
     */
    protected double getCombatMovementSpeed()
    {
        double levelAdjustment = user.getCitizenData().getCitizenSkillHandler().getLevel(Skill.Mana) * SPEED_LEVEL_BONUS;
        levelAdjustment += (user.getCitizenData().getWorkBuilding().getBuildingLevel() * 2 - 1) * SPEED_LEVEL_BONUS;

        levelAdjustment = Math.min(levelAdjustment, 0.3);
        return COMBAT_SPEED + levelAdjustment;
    }

    @Override
    protected boolean isAttackableTarget(final LivingEntity entity)
    {
        return wasAffectedByDruid(entity);
    }

    @Override
    protected boolean searchNearbyTarget()
    {
        if (checkForTarget())
        {
            return true;
        }

        final List<LivingEntity> entities = user.level.getLoadedEntitiesOfClass(LivingEntity.class, getSearchArea());

        if (entities.isEmpty())
        {
            return false;
        }

        int targetsUnderEffect = 0;
        boolean foundTarget = false;
        for (final LivingEntity entity : entities)
        {
            if (!entity.isAlive())
            {
                continue;
            }

            if (skipSearch(entity))
            {
                return false;
            }

            if (isEntityValidTarget(entity))
            {
                if (user.canSee(entity))
                {
                    user.getThreatTable().addThreat(entity, 0);
                    foundTarget = true;
                }
            }
            else if (wasAffectedByDruid(entity))
            {
                targetsUnderEffect++;
            }
        }

        return foundTarget && targetsUnderEffect <= parentAI.getOwnBuilding().getBuildingLevel() * 2;
    }

    /**
     * Check if an entity has one of the potion effects the druid hands out.
     * @param entity the entity to check for.
     * @return true if so.
     */
    private boolean wasAffectedByDruid(final LivingEntity entity)
    {
        return entity.hasEffect(Effects.MOVEMENT_SLOWDOWN) || entity.hasEffect(Effects.SATURATION) || entity.hasEffect(Effects.DAMAGE_BOOST) || entity.hasEffect(Effects.WEAKNESS) || entity.hasEffect(Effects.DAMAGE_RESISTANCE) || entity.hasEffect(Effects.HEAL);
    }

    @Override
    protected boolean isWithinPersecutionDistance(final LivingEntity target)
    {
        return parentAI.isWithinPersecutionDistance(target.blockPosition(), getAttackDistance());
    }

    @Override
    protected boolean skipSearch(final LivingEntity entity)
    {
        // Found a sleeping guard nearby
        if (entity instanceof EntityCitizen)
        {
            final EntityCitizen citizen = (EntityCitizen) entity;
            if (citizen.getCitizenJobHandler().getColonyJob() instanceof AbstractJobGuard && ((AbstractJobGuard<?>) citizen.getCitizenJobHandler().getColonyJob()).isAsleep()
                  && user.getSensing().canSee(citizen))
            {
                parentAI.setWakeCitizen(citizen);
                return true;
            }
        }

        return false;
    }

    @Override
    protected int getYSearchRange()
    {
        if (((AbstractBuildingGuards) user.getCitizenData().getWorkBuilding()).getTask().equals(GuardTaskSetting.GUARD))
        {
            return Y_VISION + 25;
        }

        return Y_VISION;
    }
}
