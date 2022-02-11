package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.entity.pathfinding.PathResult;
import com.minecolonies.api.entity.pathfinding.PathingOptions;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.buildings.modules.settings.GuardTaskSetting;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.entity.ai.combat.AttackMoveAI;
import com.minecolonies.coremod.entity.ai.combat.CombatUtils;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.entity.pathfinding.MinecoloniesAdvancedPathNavigate;
import com.minecolonies.coremod.entity.pathfinding.pathjobs.AbstractPathJob;
import com.minecolonies.coremod.entity.pathfinding.pathjobs.PathJobCanSee;
import com.minecolonies.coremod.entity.pathfinding.pathjobs.PathJobMoveToLocation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;

import static com.minecolonies.api.util.constant.GuardConstants.*;
import static com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIFight.SPEED_LEVEL_BONUS;
import static com.minecolonies.coremod.entity.ai.citizen.guard.AbstractEntityAIGuard.PATROL_DEVIATION_RAID_POINT;

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

        //max number of active potion targets in attack radius (can maintain max 2 x building level targets).
        //slowness/Speed + strength/weakness randomly (all level 1)
        //if research unlocked and has bottles + mistletoes to consume then level 2 potion effect
        //potion effect duration = mana level (up to 99s at level 99)
        //research to unlock regeneration + resistance (those need bottles + mistletoes)


        //todo put bottle in hand?

        //todo adjust target selection to include friendlies
        //todo check here if friendly or not.
        //todo if friendly throw one, else throw other.
        //todo do we want them randomzied?

        target.setLastHurtByMob(user);
        user.decreaseSaturationForContinuousAction();
    }

    @Override
    protected boolean searchNearbyTarget()
    {
        return super.searchNearbyTarget();
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
    protected int getAttackDelay()
    {
        final int attackDelay = RANGED_ATTACK_DELAY_BASE - (user.getCitizenData().getCitizenSkillHandler().getLevel(Skill.Focus));
        return Math.max(attackDelay, PHYSICAL_ATTACK_DELAY_MIN * 2);
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
        return AbstractEntityAIGuard.isAttackableTarget(user, entity);
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
    protected void onTargetChange()
    {
        CombatUtils.notifyGuardsOfTarget(user, target, PATROL_DEVIATION_RAID_POINT);
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

    @Override
    protected void onTargetDied(final LivingEntity entity)
    {
        parentAI.incrementActionsDoneAndDecSaturation();
        user.getCitizenExperienceHandler().addExperience(EXP_PER_MOB_DEATH);
    }
}
