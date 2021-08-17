package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.api.compatibility.tinkers.TinkersToolHelper;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.entity.combat.CombatAIStates;
import com.minecolonies.api.entity.combat.threat.IThreatTableEntity;
import com.minecolonies.api.entity.pathfinding.PathResult;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.jobs.AbstractJobGuard;
import com.minecolonies.coremod.entity.ai.combat.AttackMoveAI;
import com.minecolonies.coremod.entity.ai.combat.CombatUtils;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.util.NamedDamageSource;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

import static com.minecolonies.api.research.util.ResearchConstants.*;
import static com.minecolonies.api.util.constant.GuardConstants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_BANNER_PATTERNS;
import static com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIFight.SPEED_LEVEL_BONUS;
import static com.minecolonies.coremod.entity.ai.citizen.guard.AbstractEntityAIGuard.PATROL_DEVIATION_RAID_POINT;

/**
 * Knight combat AI
 */
public class KnightCombatAI extends AttackMoveAI<EntityCitizen>
{
    /**
     * Combat icon
     */
    private final static VisibleCitizenStatus KNIGHT_COMBAT =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/knight_combat.png"), "com.minecolonies.gui.visiblestatus.knight_combat");

    /**
     * Knockback chance
     */
    private static final int                   KNOCKBACK_CHANCE = 5;
    private final        AbstractEntityAIGuard parentAI;

    /**
     * Last used time of the aoe ability
     */
    private long lastAoeUseTime = 0;

    /**
     * Cooldown for the Aoe knockback in ticks
     */
    private final int KNOCKBACK_COOLDOWN = 30 * 8;

    /**
     * Minimum time needed to next attack to use the shield
     */
    private final int MIN_TIME_TO_ATTACK = 8;

    /**
     * The value of the speed which the guard will move.
     */
    private static final double COMBAT_SPEED = 1.0;

    public KnightCombatAI(
      final EntityCitizen owner,
      final ITickRateStateMachine stateMachine,
      final AbstractEntityAIGuard parentAI)
    {
        super(owner, stateMachine);

        this.parentAI = parentAI;
        stateMachine.addTransition(new TickingTransition<>(CombatAIStates.ATTACKING, () -> true, this::attackProtect, 8));
    }

    /**
     * Check if the guard can protect himself with a shield And if so, do it.
     *
     * @return The next IAIState.
     */
    protected IAIState attackProtect()
    {
        final int shieldSlot = InventoryUtils.findFirstSlotInItemHandlerWith(user.getInventoryCitizen(), Items.SHIELD);
        if (shieldSlot != -1 && target != null && target.isAlive() && nextAttackTime - user.level.getGameTime() >= MIN_TIME_TO_ATTACK &&
              user.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(SHIELD_USAGE) > 0)
        {
            user.getCitizenItemHandler().setHeldItem(Hand.OFF_HAND, shieldSlot);
            user.startUsingItem(Hand.OFF_HAND);

            // Apply the colony Flag to the shield
            ItemStack shieldStack = user.getInventoryCitizen().getHeldItem(Hand.OFF_HAND);
            CompoundNBT nbt = shieldStack.getOrCreateTagElement("BlockEntityTag");
            nbt.put(TAG_BANNER_PATTERNS, user.getCitizenColonyHandler().getColony().getColonyFlag());

            user.lookAt(target, (float) TURN_AROUND, (float) TURN_AROUND);
            user.decreaseSaturationForContinuousAction();
        }

        return null;
    }

    @Override
    public boolean canAttack()
    {
        final int weaponSlot =
          InventoryUtils.getFirstSlotOfItemHandlerContainingTool(user.getInventoryCitizen(), ToolType.SWORD, 0, user.getCitizenData().getWorkBuilding().getMaxToolLevel());

        if (weaponSlot != -1)
        {
            user.getCitizenItemHandler().setHeldItem(Hand.MAIN_HAND, weaponSlot);
            return true;
        }

        return false;
    }

    @Override
    protected void doAttack(final LivingEntity target)
    {
        if (user.distanceTo(target) > 1)
        {
            moveInAttackPosition(target);
        }

        user.swing(Hand.MAIN_HAND);
        user.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, (float) BASIC_VOLUME, (float) SoundUtils.getRandomPitch(user.getRandom()));

        final double damageToBeDealt = getAttackDamage();
        final DamageSource source = new NamedDamageSource(user.getName().getString(), user);
        if (MineColonies.getConfig().getServer().pvp_mode.get() && target instanceof PlayerEntity)
        {
            source.bypassArmor();
        }

        final int fireLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FIRE_ASPECT, user.getItemInHand(Hand.MAIN_HAND));
        if (fireLevel > 0)
        {
            target.setSecondsOnFire(fireLevel * 80);
        }

        if (user.level.getGameTime() - lastAoeUseTime > KNOCKBACK_COOLDOWN)
        {
            doAoeAttack(source, damageToBeDealt);
        }

        target.hurt(source, (float) damageToBeDealt);
        target.setLastHurtByMob(user);

        if (target instanceof MobEntity && user.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(KNIGHT_TAUNT) > 0)
        {
            ((MobEntity) target).setTarget(user);
            if (target instanceof IThreatTableEntity)
            {
                ((IThreatTableEntity) target).getThreatTable().addThreat(user, 5);
            }
        }

        user.stopUsingItem();
        user.decreaseSaturationForContinuousAction();
        user.getCitizenData().setVisibleStatus(KNIGHT_COMBAT);
        user.getCitizenItemHandler().damageItemInHand(Hand.MAIN_HAND, 1);
    }

    /**
     * Does an aoe attack if researched
     *
     * @param source          normal attack damage source
     * @param damageToBeDealt normal attack damage to be distributed to targets
     */
    private void doAoeAttack(final DamageSource source, final double damageToBeDealt)
    {
        if (user.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(KNIGHT_WHIRLWIND) > 0
              && user.getRandom().nextInt(KNOCKBACK_CHANCE) == 0)
        {
            List<LivingEntity> entities = user.level.getLoadedEntitiesOfClass(LivingEntity.class, user.getBoundingBox().inflate(2.0D, 0.5D, 2.0D));
            for (LivingEntity livingentity : entities)
            {
                if (livingentity != user && isEntityValidTarget(livingentity) && (!(livingentity instanceof ArmorStandEntity)))
                {
                    livingentity.knockback(
                      2F,
                      MathHelper.sin(livingentity.yRot * ((float) Math.PI)),
                      (-MathHelper.cos(livingentity.yRot * ((float) Math.PI))));
                    livingentity.hurt(source, (float) (damageToBeDealt / entities.size()));
                }
            }

            user.level.playSound(null,
              user.getX(),
              user.getY(),
              user.getZ(),
              SoundEvents.PLAYER_ATTACK_SWEEP,
              user.getSoundSource(),
              1.0F,
              1.0F);

            double d0 = (double) (-MathHelper.sin(user.yRot * ((float) Math.PI / 180)));
            double d1 = (double) MathHelper.cos(user.yRot * ((float) Math.PI / 180));
            if (user.level instanceof ServerWorld)
            {
                ((ServerWorld) user.level).sendParticles(ParticleTypes.SWEEP_ATTACK,
                  user.getX() + d0,
                  user.getY(0.5D),
                  user.getZ() + d1,
                  2,
                  d0,
                  0.0D,
                  d1,
                  0.0D);
            }

            lastAoeUseTime = user.level.getGameTime();
        }
    }

    /**
     * Calculates the damage to deal
     *
     * @return attack damage
     */
    private int getAttackDamage()
    {
        int addDmg = 0;

        final ItemStack heldItem = user.getItemInHand(Hand.MAIN_HAND);

        if (ItemStackUtils.doesItemServeAsWeapon(heldItem))
        {
            if (heldItem.getItem() instanceof SwordItem)
            {
                addDmg += ((SwordItem) heldItem.getItem()).getDamage() + BASE_PHYSICAL_DAMAGE;
            }
            else
            {
                addDmg += TinkersToolHelper.getDamage(heldItem);
            }
            addDmg += EnchantmentHelper.getDamageBonus(heldItem, target.getMobType()) / 2.5;
        }

        addDmg += user.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(MELEE_DAMAGE);

        if (user.getHealth() <= user.getMaxHealth() * 0.2D)
        {
            addDmg *= 2;
        }

        return (int) ((addDmg) * MineColonies.getConfig().getServer().knightDamageMult.get());
    }

    @Override
    protected double getAttackDistance()
    {
        return MAX_DISTANCE_FOR_ATTACK;
    }

    @Override
    protected int getAttackDelay()
    {
        final int reload = KNIGHT_ATTACK_DELAY_BASE - user.getCitizenData().getCitizenSkillHandler().getLevel(Skill.Adaptability) / 3;
        return Math.max(reload, KNIGHT_ATTACK_DELAY_MIN);
    }

    @Override
    protected PathResult moveInAttackPosition(final LivingEntity target)
    {
        return user.getNavigation().moveToXYZ(target.getX(), target.getY(), target.getZ(), getCombatMovementSpeed());
    }

    /**
     * Get combat speed
     *
     * @return movent speed
     */
    protected double getCombatMovementSpeed()
    {
        double levelAdjustment = user.getCitizenData().getCitizenSkillHandler().getLevel(Skill.Adaptability) * SPEED_LEVEL_BONUS;
        levelAdjustment += (user.getCitizenData().getWorkBuilding().getBuildingLevel() - 1) * SPEED_LEVEL_BONUS;

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
    protected int getSearchRange()
    {
        return 0;
    }
}
