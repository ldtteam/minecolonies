package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.entity.pathfinding.PathResult;
import com.minecolonies.api.entity.pathfinding.PathingOptions;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.MineColonies;
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
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;

import static com.minecolonies.api.research.util.ResearchConstants.*;
import static com.minecolonies.api.util.constant.GuardConstants.*;
import static com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIFight.SPEED_LEVEL_BONUS;
import static com.minecolonies.coremod.entity.ai.citizen.guard.AbstractEntityAIGuard.PATROL_DEVIATION_RAID_POINT;

/**
 * Knight combat AI
 */
public class RangerCombatAI extends AttackMoveAI<EntityCitizen>
{
    /**
     * Visible combat icon
     */
    private final static VisibleCitizenStatus ARCHER_COMBAT =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/archer_combat.png"), "com.minecolonies.gui.visiblestatus.archer_combat");

    private final AbstractEntityAIGuard parentAI;

    /**
     * The value of the speed which the guard will move.
     */
    private static final double COMBAT_SPEED = 1.0;

    /**
     * Extra damage for arrow usage
     */
    private static final double ARROW_EXTRA_DAMAGE = 2.0f;

    /**
     * How many ticks we activate the bow before shooting
     */
    private static final int BOW_HOLDING_DELAY = 10;

    /**
     * Bonus range for shooting while guarding
     */
    private static final int GUARD_BONUS_RANGE = 10;

    /**
     * Flee chance
     */
    private static final int FLEE_CHANCE = 3;

    private final PathingOptions combatPathingOptions;

    public RangerCombatAI(
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
    public boolean canAttack()
    {
        final int weaponSlot =
          InventoryUtils.getFirstSlotOfItemHandlerContainingTool(user.getInventoryCitizen(), ToolType.BOW, 0, user.getCitizenData().getWorkBuilding().getMaxToolLevel());

        if (weaponSlot != -1)
        {
            user.getCitizenItemHandler().setHeldItem(Hand.MAIN_HAND, weaponSlot);
            if (nextAttackTime - BOW_HOLDING_DELAY >= user.level.getGameTime())
            {
                user.startUsingItem(Hand.MAIN_HAND);
            }
            return true;
        }

        return false;
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

        user.getCitizenData().setVisibleStatus(ARCHER_COMBAT);
        user.lookAt(target, (float) TURN_AROUND, (float) TURN_AROUND);
        user.swing(Hand.MAIN_HAND);

        int amountOfArrows = 1;
        if (user.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(DOUBLE_ARROWS) > 0)
        {
            if (user.getRandom().nextDouble() < user.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(DOUBLE_ARROWS))
            {
                amountOfArrows++;
            }
        }

        for (int i = 0; i < amountOfArrows; i++)
        {
            final AbstractArrowEntity arrow = CombatUtils.createArrowForShooter(user);

            if (user.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(ARROW_PIERCE) > 0)
            {
                arrow.setPierceLevel((byte) 2);
            }

            // Add bow enchant effects: Knocback and fire
            final ItemStack bow = user.getItemInHand(Hand.MAIN_HAND);

            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, bow) > 0)
            {
                arrow.setSecondsOnFire(100);
            }
            final int k = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, bow);
            if (k > 0)
            {
                arrow.setKnockback(k);
            }

            double damage = calculateDamage();


            arrow.setBaseDamage(damage);

            final float chance = HIT_CHANCE_DIVIDER / (user.getCitizenData().getCitizenSkillHandler().getLevel(Skill.Adaptability) + 1);
            CombatUtils.shootArrow(arrow, target, chance);
            user.playSound(SoundEvents.SKELETON_SHOOT, (float) BASIC_VOLUME, (float) SoundUtils.getRandomPitch(user.getRandom()));
        }

        target.setLastHurtByMob(user);
        user.getCitizenItemHandler().damageItemInHand(Hand.MAIN_HAND, 1);
        user.stopUsingItem();
        user.decreaseSaturationForContinuousAction();
    }

    @Override
    protected double getAttackDistance()
    {
        int attackDist = BASE_DISTANCE_FOR_RANGED_ATTACK;
        // + 1 Blockrange per building level for a total of +5 from building level
        if (user.getCitizenData().getWorkBuilding() != null)
        {
            attackDist += user.getCitizenData().getWorkBuilding().getBuildingLevel();
        }
        // ~ +1 each three levels for a total of +10 from guard level
        if (user.getCitizenData() != null)
        {
            attackDist += (user.getCitizenData().getCitizenSkillHandler().getLevel(Skill.Adaptability) / 50.0f) * 15;
        }

        attackDist = Math.min(attackDist, MAX_DISTANCE_FOR_RANGED_ATTACK);

        if (target != null)
        {
            attackDist += user.getY() - target.getY();
        }

        if (((AbstractBuildingGuards) user.getCitizenData().getWorkBuilding()).getTask().equals(GuardTaskSetting.GUARD))
        {
            attackDist += GUARD_BONUS_RANGE;
        }

        return attackDist;
    }

    @Override
    protected int getAttackDelay()
    {
        final int attackDelay = RANGED_ATTACK_DELAY_BASE - (user.getCitizenData().getCitizenSkillHandler().getLevel(Skill.Adaptability));
        return Math.max(attackDelay, PHYSICAL_ATTACK_DELAY_MIN * 2);
    }

    /**
     * Calculates the ranged attack damage
     *
     * @return the attack damage
     */
    private double calculateDamage()
    {
        int damage = (user.getCitizenData().getCitizenSkillHandler().getLevel(Skill.Agility) / 50) * 10;

        final ItemStack heldItem = user.getItemInHand(Hand.MAIN_HAND);
        damage += EnchantmentHelper.getDamageBonus(heldItem, target.getMobType()) / 2.5;
        damage += EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, heldItem);
        damage += user.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(ARCHER_DAMAGE);

        if (user.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(ARCHER_USE_ARROWS) > 0)
        {
            int slot = InventoryUtils.findFirstSlotInItemHandlerWith(user.getInventoryCitizen(), item -> item.getItem() instanceof ArrowItem);
            if (slot != -1)
            {
                if (!ItemStackUtils.isEmpty(user.getInventoryCitizen().extractItem(slot, 1, false)))
                {
                    damage += ARROW_EXTRA_DAMAGE;
                }
            }
        }

        if (user.getHealth() <= user.getMaxHealth() * 0.2D)
        {
            damage *= 2;
        }

        return (RANGER_BASE_DMG + damage) * MineColonies.getConfig().getServer().rangerDamageMult.get();
    }

    @Override
    protected PathResult moveInAttackPosition(final LivingEntity target)
    {
        if (((AbstractBuildingGuards) user.getCitizenData().getWorkBuilding()).getTask().equals(GuardTaskSetting.GUARD))
        {
            final PathJobCanSee job = new PathJobCanSee(user, target, user.level, ((AbstractBuildingGuards) user.getCitizenData().getWorkBuilding()).getGuardPos(), 40);
            final PathResult pathResult = ((MinecoloniesAdvancedPathNavigate) user.getNavigation()).setPathJob(job, null, getCombatMovementSpeed());
            job.setPathingOptions(combatPathingOptions);
            return pathResult;
        }

        final PathJobMoveToLocation job = new PathJobMoveToLocation(user.level, AbstractPathJob.prepareStart(user), target.blockPosition(), 200, user);
        final PathResult pathResult = ((MinecoloniesAdvancedPathNavigate) user.getNavigation()).setPathJob(job, null, getCombatMovementSpeed());
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
        double levelAdjustment = user.getCitizenData().getCitizenSkillHandler().getLevel(Skill.Agility) * SPEED_LEVEL_BONUS;
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

    /**
     * Actions on changing to a new target entity
     */
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
}
