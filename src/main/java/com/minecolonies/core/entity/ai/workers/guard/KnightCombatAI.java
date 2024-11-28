package com.minecolonies.core.entity.ai.workers.guard;

import com.minecolonies.api.compatibility.tinkers.TinkersToolHelper;
import com.minecolonies.api.entity.ai.combat.CombatAIStates;
import com.minecolonies.api.entity.ai.combat.threat.IThreatTableEntity;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.TickingTransition;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.util.DamageSourceKeys;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.api.util.constant.ColonyConstants;
import com.minecolonies.core.util.citizenutils.CitizenItemUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.colony.jobs.AbstractJobGuard;
import com.minecolonies.core.entity.ai.combat.AttackMoveAI;
import com.minecolonies.core.entity.ai.combat.CombatUtils;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.minecolonies.core.entity.pathfinding.pathresults.PathResult;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.List;

import static com.minecolonies.api.research.util.ResearchConstants.*;
import static com.minecolonies.api.util.constant.GuardConstants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_BANNER_PATTERNS;
import static com.minecolonies.api.util.constant.StatisticsConstants.MOBS_KILLED;
import static com.minecolonies.api.util.constant.StatisticsConstants.MOB_KILLED;
import static com.minecolonies.core.colony.buildings.modules.BuildingModules.STATS_MODULE;
import static com.minecolonies.core.entity.ai.workers.guard.AbstractEntityAIFight.SPEED_LEVEL_BONUS;
import static com.minecolonies.core.entity.ai.workers.guard.AbstractEntityAIGuard.PATROL_DEVIATION_RAID_POINT;

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
              user.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(SHIELD_USAGE) > 0)
        {
            CitizenItemUtils.setHeldItem(user, InteractionHand.OFF_HAND, shieldSlot);
            user.startUsingItem(InteractionHand.OFF_HAND);

            // Apply the colony Flag to the shield
            ItemStack shieldStack = user.getInventoryCitizen().getHeldItem(InteractionHand.OFF_HAND);
            CompoundTag nbt = shieldStack.getOrCreateTagElement("BlockEntityTag");
            nbt.put(TAG_BANNER_PATTERNS, user.getCitizenColonyHandler().getColonyOrRegister().getColonyFlag());

            user.lookAt(target, (float) TURN_AROUND, (float) TURN_AROUND);
            user.decreaseSaturationForContinuousAction();
        }

        return null;
    }

    @Override
    public boolean canAttack()
    {
        final int weaponSlot =
          InventoryUtils.getFirstSlotOfItemHandlerContainingEquipment(user.getInventoryCitizen(),
            ModEquipmentTypes.sword.get(),
            0,
            user.getCitizenData().getWorkBuilding().getMaxEquipmentLevel());

        if (weaponSlot != -1)
        {
            CitizenItemUtils.setHeldItem(user, InteractionHand.MAIN_HAND, weaponSlot);
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

        user.swing(InteractionHand.MAIN_HAND);
        user.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, (float) BASIC_VOLUME, (float) SoundUtils.getRandomPitch(user.getRandom()));

        final double damageToBeDealt = getAttackDamage();
        DamageSource source = target.level.damageSources().source(DamageSourceKeys.GUARD, user);
        if (MineColonies.getConfig().getServer().pvp_mode.get() && target instanceof Player)
        {
            source = target.level.damageSources().source(DamageSourceKeys.GUARD_PVP, user);
        }

        final int fireLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FIRE_ASPECT, user.getItemInHand(InteractionHand.MAIN_HAND));
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

        if (target instanceof Mob && user.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(KNIGHT_TAUNT) > 0)
        {
            ((Mob) target).setTarget(user);
            if (target instanceof IThreatTableEntity)
            {
                ((IThreatTableEntity) target).getThreatTable().addThreat(user, 5);
            }
        }

        user.stopUsingItem();
        user.decreaseSaturationForContinuousAction();
        user.getCitizenData().setVisibleStatus(KNIGHT_COMBAT);
        CitizenItemUtils.damageItemInHand(user, InteractionHand.MAIN_HAND, 1);
    }

    /**
     * Does an aoe attack if researched
     *
     * @param source          normal attack damage source
     * @param damageToBeDealt normal attack damage to be distributed to targets
     */
    private void doAoeAttack(final DamageSource source, final double damageToBeDealt)
    {
        if (user.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(KNIGHT_WHIRLWIND) > 0
              && user.getRandom().nextInt(KNOCKBACK_CHANCE) == 0)
        {
            List<LivingEntity> entities = user.level.getEntitiesOfClass(LivingEntity.class, user.getBoundingBox().inflate(2.0D, 0.5D, 2.0D));
            for (LivingEntity livingentity : entities)
            {
                if (livingentity != user && isEntityValidTarget(livingentity) && (!(livingentity instanceof ArmorStand)))
                {
                    livingentity.knockback(
                      2F,
                      Mth.sin(livingentity.getYRot() * ((float) Math.PI)),
                      (-Mth.cos(livingentity.getYRot() * ((float) Math.PI))));
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

            double d0 = (double) (-Mth.sin(user.getYRot() * ((float) Math.PI / 180)));
            double d1 = (double) Mth.cos(user.getYRot() * ((float) Math.PI / 180));
            if (user.level instanceof ServerLevel)
            {
                ((ServerLevel) user.level).sendParticles(ParticleTypes.SWEEP_ATTACK,
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
    private double getAttackDamage()
    {
        double addDmg = 0;

        final ItemStack heldItem = user.getItemInHand(InteractionHand.MAIN_HAND);

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

        addDmg += user.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(MELEE_DAMAGE);

        // TODO: Recheck balancing, do we need this
        if (user.getHealth() <= user.getMaxHealth() * 0.2D)
        {
            addDmg *= 2;
        }

        if (ColonyConstants.rand.nextDouble() > 1 / (1 + user.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(GUARD_CRIT)))
        {
            addDmg *= 1.5;
            ((ServerLevel) user.level).getChunkSource().broadcastAndSend(user, new ClientboundAnimatePacket(target, 4));
        }

        return addDmg * MineColonies.getConfig().getServer().guardDamageMultiplier.get();
    }

    @Override
    protected double getAttackDistance()
    {
        return MAX_DISTANCE_FOR_ATTACK;
    }

    @Override
    protected int getAttackDelay()
    {
        // TODO: Not sure if we should make knights attack faster, they are intended to not scale in dmg, but health
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
    protected int getSearchRange()
    {
        return 16;
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
