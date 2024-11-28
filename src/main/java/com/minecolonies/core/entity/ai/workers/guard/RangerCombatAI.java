package com.minecolonies.core.entity.ai.workers.guard;

import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.api.util.constant.ColonyConstants;
import com.minecolonies.core.entity.pathfinding.PathfindingUtils;
import com.minecolonies.core.entity.pathfinding.PathingOptions;
import com.minecolonies.core.entity.pathfinding.pathresults.PathResult;
import com.minecolonies.core.util.citizenutils.CitizenItemUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.core.colony.buildings.modules.settings.GuardTaskSetting;
import com.minecolonies.core.colony.jobs.AbstractJobGuard;
import com.minecolonies.core.entity.ai.combat.AttackMoveAI;
import com.minecolonies.core.entity.ai.combat.CombatUtils;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.minecolonies.core.entity.other.CustomArrowEntity;
import com.minecolonies.core.entity.pathfinding.navigation.MinecoloniesAdvancedPathNavigate;
import com.minecolonies.core.entity.pathfinding.pathjobs.*;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import static com.minecolonies.api.research.util.ResearchConstants.*;
import static com.minecolonies.api.util.constant.GuardConstants.*;
import static com.minecolonies.api.util.constant.StatisticsConstants.MOBS_KILLED;
import static com.minecolonies.api.util.constant.StatisticsConstants.MOB_KILLED;
import static com.minecolonies.core.colony.buildings.modules.BuildingModules.STATS_MODULE;
import static com.minecolonies.core.entity.ai.workers.guard.AbstractEntityAIFight.SPEED_LEVEL_BONUS;
import static com.minecolonies.core.entity.ai.workers.guard.AbstractEntityAIGuard.PATROL_DEVIATION_RAID_POINT;

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
    private static final int BOW_HOLDING_DELAY = 40;

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
        combatPathingOptions.withDropCost(1.5);
    }

    @Override
    public boolean canAttack()
    {
        final int weaponSlot =
          InventoryUtils.getFirstSlotOfItemHandlerContainingEquipment(user.getInventoryCitizen(), ModEquipmentTypes.bow.get(), 0, user.getCitizenData().getWorkBuilding().getMaxEquipmentLevel());

        if (weaponSlot != -1)
        {
            CitizenItemUtils.setHeldItem(user, InteractionHand.MAIN_HAND, weaponSlot);
            if (nextAttackTime - BOW_HOLDING_DELAY >= user.level.getGameTime() && !user.isUsingItem())
            {
                user.startUsingItem(InteractionHand.MAIN_HAND);
            }
            return true;
        }

        return false;
    }

    @Override
    protected boolean checkForTarget()
    {
        final boolean validTarget = super.checkForTarget();

        if (!validTarget && user.isUsingItem())
        {
            user.stopUsingItem();
        }

        return validTarget;
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
        user.swing(InteractionHand.MAIN_HAND);
        user.stopUsingItem();

        int amountOfArrows = 1;
        if (user.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(DOUBLE_ARROWS) > 0)
        {
            if (user.getRandom().nextDouble() < user.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(DOUBLE_ARROWS))
            {
                amountOfArrows++;
            }
        }

        for (int i = 0; i < amountOfArrows; i++)
        {
            final AbstractArrow arrow = CombatUtils.createArrowForShooter(user);

            if (user.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(ARROW_PIERCE) > 0)
            {
                arrow.setPierceLevel((byte) 2);
            }

            // Add bow enchant effects: Knocback and fire
            final ItemStack bow = user.getItemInHand(InteractionHand.MAIN_HAND);

            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, bow) > 0)
            {
                arrow.setSecondsOnFire(100);
            }
            final int k = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, bow);
            if (k > 0)
            {
                arrow.setKnockback(k);
            }

            double damage = calculateDamage(arrow);


            arrow.setBaseDamage(damage);

            final float chance = HIT_CHANCE_DIVIDER / (user.getCitizenData().getCitizenSkillHandler().getLevel(Skill.Adaptability) + 1);
            CombatUtils.shootArrow(arrow, target, chance);
            user.playSound(SoundEvents.SKELETON_SHOOT, (float) BASIC_VOLUME, (float) SoundUtils.getRandomPitch(user.getRandom()));
        }

        target.setLastHurtByMob(user);
        CitizenItemUtils.damageItemInHand(user, InteractionHand.MAIN_HAND, 1);
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
        // TODO: Maybe better for balancing to not increase damage and speed, looks odd and drains arrows/bow durability
        final int attackDelay = RANGED_ATTACK_DELAY_BASE - (user.getCitizenData().getCitizenSkillHandler().getLevel(Skill.Adaptability));
        return Math.max(attackDelay, PHYSICAL_ATTACK_DELAY_MIN * 2);
    }

    /**
     * Calculates the ranged attack damage
     *
     * @param arrow
     * @return the attack damage
     */
    private double calculateDamage(final AbstractArrow arrow)
    {
        double damage = user.getCitizenData().getCitizenSkillHandler().getLevel(Skill.Agility) / 5d;

        final ItemStack heldItem = user.getItemInHand(InteractionHand.MAIN_HAND);
        damage += EnchantmentHelper.getDamageBonus(heldItem, target.getMobType()) / 2.5;
        damage += EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, heldItem);
        damage += user.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(ARCHER_DAMAGE);

        if (user.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(ARCHER_USE_ARROWS) > 0)
        {
            int slot = InventoryUtils.findFirstSlotInItemHandlerWith(user.getInventoryCitizen(), item -> item.getItem() instanceof ArrowItem);
            if (slot != -1)
            {
                if (!ItemStackUtils.isEmpty(user.getInventoryCitizen().extractItem(slot, 1, true)))
                {
                    damage += ARROW_EXTRA_DAMAGE;
                    if (arrow instanceof CustomArrowEntity customArrowEntity)
                    {
                        customArrowEntity.setOnHitCallback(entityRayTraceResult ->
                        {
                            final int arrowSlot = InventoryUtils.findFirstSlotInItemHandlerWith(user.getInventoryCitizen(), item -> item.getItem() instanceof ArrowItem);
                            if (arrowSlot != -1)
                            {
                                user.getInventoryCitizen().extractItem(arrowSlot, 1, false);
                            }

                            return true;
                        });
                    }
                }
            }
        }

        if (user.getHealth() <= user.getMaxHealth() * 0.2D)
        {
            damage *= 2;
        }

        if (ColonyConstants.rand.nextDouble() > 1 / (1 + user.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(GUARD_CRIT)))
        {
            damage *= 1.5;
        }

        return (RANGER_BASE_DMG + damage) * MineColonies.getConfig().getServer().guardDamageMultiplier.get();
    }

    @Override
    protected PathResult moveInAttackPosition(final LivingEntity target)
    {
        if (BlockPosUtil.getDistanceSquared(target.blockPosition(), user.blockPosition()) <= 4.0)
        {
            final PathJobMoveAwayFromLocation job = new PathJobMoveAwayFromLocation(user.level,
              PathfindingUtils.prepareStart(target),
              target.blockPosition(),
              (int) 7.0,
              (int) user.getAttribute(Attributes.FOLLOW_RANGE).getValue(),
              user);
            final PathResult pathResult = ((MinecoloniesAdvancedPathNavigate) user.getNavigation()).setPathJob(job, null, getCombatMovementSpeed(), true);
            job.setPathingOptions(combatPathingOptions);
            return pathResult;
        }
        else if (BlockPosUtil.getDistance2D(target.blockPosition(), user.blockPosition()) >= 20)
        {
            final PathJobMoveToLocation job = new PathJobMoveToLocation(user.level, PathfindingUtils.prepareStart(user), target.blockPosition(), 200, user);
            final PathResult pathResult = ((MinecoloniesAdvancedPathNavigate) user.getNavigation()).setPathJob(job, null, getCombatMovementSpeed(), true);
            job.setPathingOptions(combatPathingOptions);
            return pathResult;
        }
        final PathJobCanSee job = new PathJobCanSee(user, target, user.level, ((AbstractBuildingGuards) user.getCitizenData().getWorkBuilding()).getGuardPos(), 40);
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
                  && user.getSensing().hasLineOfSight(citizen))
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
        user.getCitizenColonyHandler().getColonyOrRegister().getStatisticsManager().increment(MOBS_KILLED, user.getCitizenColonyHandler().getColonyOrRegister().getDay());
        if (entity.getType().getDescription().getContents() instanceof TranslatableContents translatableContents)
        {
            parentAI.building.getModule(STATS_MODULE).increment(MOB_KILLED + ";" + translatableContents.getKey());
        }
    }
}
