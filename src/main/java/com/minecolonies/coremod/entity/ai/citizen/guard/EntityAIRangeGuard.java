package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.jobs.JobGuard;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.coremod.entity.ai.util.AIState.*;

/**
 * Handles the AI of the guard entities.
 */
public class EntityAIRangeGuard extends AbstractEntityAIGuard implements IRangedAttackMob
{
    /**
     * Basic delay for the next shot.
     */
    private static final int BASE_RELOAD_TIME = 60;

    /**
     * Base damage which the power enchantments added.
     */
    private static final double BASE_POWER_ENCHANTMENT_DAMAGE = 0.5D;

    /**
     * Damage per power enchantment level.
     */
    private static final double POWER_ENCHANTMENT_DAMAGE_MULTIPLIER = 0.5D;

    /**
     * Multiply the base damage always with this.
     */
    private static final double BASE_DAMAGE_MULTIPLIER = 2.0D;

    /**
     * Multiply some random with this to get some random damage addition.
     */
    private static final double RANDOM_DAMAGE_MULTPLIER = 0.25D;

    /**
     * When the difficulty is higher the damage increases by this each level.
     */
    private static final double DIFFICULTY_DAMAGE_INCREASE = 0.11D;

    /**
     * Chance that the arrow lights up the target when the target is on fire.
     */
    private static final int FIRE_EFFECT_CHANCE = 100;

    /**
     * The pitch will be divided by this to calculate it for the arrow sound.
     */
    private static final double PITCH_DIVIDER = 1.0D;

    /**
     * The base pitch, add more to this to change the sound.
     */
    private static final double BASE_PITCH = 0.8D;

    /**
     * Random is multiplied by this to get a random arrow sound.
     */
    private static final double PITCH_MULTIPLIER = 0.4D;

    /**
     * Quantity to be moved to rotate the entity without actually moving.
     */
    private static final double MOVE_MINIMAL = 0.01D;

    /**
     * Quantity the worker should turn around all at once.
     */
    private static final double TURN_AROUND = 180D;

    /**
     * Have to aim that bit higher to hit the target.
     */
    private static final double AIM_SLIGHTLY_HIGHER_MULTIPLIER = 0.20000000298023224D;

    /**
     * Normal volume at which sounds are played at.
     */
    private static final double BASIC_VOLUME = 1.0D;

    /**
     * Guard has to aim x higher to hit his target.
     */
    private static final double AIM_HEIGHT = 3.0D;

    /**
     * Used to calculate the chance that an arrow hits, if the worker levels is higher than 15 the chance gets worse again.
     * Because of the rising fire speed.
     */
    private static final double HIT_CHANCE_DIVIDER = 15.0D;

    /**
     * The arrow travell speed.
     */
    private static final double ARROW_SPEED = 1.6D;

    /**
     * Base speed of the guard he follows his target.
     */
    private static final int BASE_FOLLOW_SPEED = 1;

    /**
     * Base multiplier increasing the attack speed each level.
     */
    private static final double BASE_FOLLOW_SPEED_MULTIPLIER = 0.25D;

    /**
     * The start search distance of the guard to track/attack entities may get more depending on the level.
     */
    private static final double MAX_ATTACK_DISTANCE = 20.0D;

    /**
     * Damage per range attack.
     */
    private static final int DAMAGE_PER_ATTACK = 2;

    /**
     * When target is out of sight, try to move that close to the target.
     */
    private static final int MOVE_CLOSE = 3;

    /**
     * Sets up some important skeleton stuff for every ai.
     *
     * @param job the job class
     */
    public EntityAIRangeGuard(@NotNull final JobGuard job)
    {
        super(job);
        super.registerTargets(
          new AITarget(GUARD_SEARCH_TARGET, this::searchTarget),
          new AITarget(GUARD_GET_TARGET, this::getTarget),
          new AITarget(GUARD_HUNT_DOWN_TARGET, this::huntDown),
          new AITarget(GUARD_PATROL, this::patrol),
          new AITarget(GUARD_RESTOCK, this::goToBuilding)
        );

        if (worker.getCitizenData() != null)
        {
            worker.setSkillModifier(2 * worker.getCitizenData().getIntelligence() + worker.getCitizenData().getStrength());
            worker.setCanPickUpLoot(true);
        }
    }

    @Override
    protected AIState searchTarget()
    {
        if (checkForToolOrWeapon(ToolType.BOW))
        {
            return AIState.GUARD_SEARCH_TARGET;
        }
        worker.setHeldItem(worker.findFirstSlotInInventoryWith(Items.BOW, -1));
        return super.searchTarget();
    }

    /**
     * Follow the target and kill it.
     *
     * @return the next AIState.
     */
    protected AIState huntDown()
    {
        if(huntDownlastAttacker())
        {
            targetEntity = this.worker.getLastAttacker();
        }

        if (!targetEntity.isEntityAlive() || checkForToolOrWeapon(ToolType.BOW))
        {
            targetEntity = null;
            worker.setAIMoveSpeed((float) 1.0D);
            return AIState.GUARD_GATHERING;
        }

        if (worker.getEntitySenses().canSee(targetEntity) && worker.getDistanceToEntity(targetEntity) <= MAX_ATTACK_DISTANCE)
        {
            worker.resetActiveHand();
            attackEntityWithRangedAttack(targetEntity, DAMAGE_PER_ATTACK);
            setDelay(getReloadTime());
            attacksExecuted += 1;

            if (attacksExecuted >= getMaxAttacksUntilRestock())
            {
                return AIState.GUARD_RESTOCK;
            }

            return AIState.GUARD_HUNT_DOWN_TARGET;
        }

        if (shouldReturnToTarget(targetEntity.getPosition(), FOLLOW_RANGE + MAX_ATTACK_DISTANCE))
        {
            return AIState.GUARD_PATROL;
        }

        worker.setAIMoveSpeed((float) (BASE_FOLLOW_SPEED + BASE_FOLLOW_SPEED_MULTIPLIER * worker.getExperienceLevel()));
        worker.isWorkerAtSiteWithMove(targetEntity.getPosition(), MOVE_CLOSE);

        return AIState.GUARD_SEARCH_TARGET;
    }

    @Override
    public void attackEntityWithRangedAttack(@NotNull final EntityLivingBase entityToAttack, final float baseDamage)
    {
        final EntityTippedArrow arrowEntity = new GuardArrow(CompatibilityUtils.getWorld(this.worker), worker);
        final double xVector = entityToAttack.posX - worker.posX;
        final double yVector = entityToAttack.getEntityBoundingBox().minY + entityToAttack.height / AIM_HEIGHT - arrowEntity.posY;
        final double zVector = entityToAttack.posZ - worker.posZ;
        final double distance = (double) MathHelper.sqrt_double(xVector * xVector + zVector * zVector);
        double damage = baseDamage;
        //Lower the variable higher the chance that the arrows hits the target.
        final double chance = HIT_CHANCE_DIVIDER / (worker.getExperienceLevel() + 1);

        arrowEntity.setThrowableHeading(xVector, yVector + distance * AIM_SLIGHTLY_HIGHER_MULTIPLIER, zVector, (float) ARROW_SPEED, (float) chance);

        if (worker.getHealth() <= 2)
        {
            damage *= 2;
        }

        addEffectsToArrow(arrowEntity, damage);

        worker.faceEntity(entityToAttack, (float) TURN_AROUND, (float) TURN_AROUND);
        worker.getLookHelper().setLookPositionWithEntity(entityToAttack, (float) TURN_AROUND, (float) TURN_AROUND);

        final double xDiff = targetEntity.posX - worker.posX;
        final double zDiff = targetEntity.posZ - worker.posZ;

        final double goToX = xDiff > 0 ? MOVE_MINIMAL : -MOVE_MINIMAL;
        final double goToZ = zDiff > 0 ? MOVE_MINIMAL : -MOVE_MINIMAL;

        worker.moveEntity(goToX, 0, goToZ);

        worker.swingArm(EnumHand.MAIN_HAND);
        worker.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, (float) BASIC_VOLUME, (float) getRandomPitch());
        CompatibilityUtils.getWorld(worker).spawnEntityInWorld(arrowEntity);

        worker.damageItemInHand(1);
    }

    private int getReloadTime()
    {
        return BASE_RELOAD_TIME / (worker.getExperienceLevel() + 1);
    }

    /**
     * Method used to add potion/enchantment effects to the bow depending on his enchantments etc.
     *
     * @param arrowEntity the arrow to add these effects to.
     * @param baseDamage  the arrow base damage.
     */
    private void addEffectsToArrow(final EntityTippedArrow arrowEntity, final double baseDamage)
    {
        final int powerEntchantment = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.POWER, worker);
        final int punchEntchantment = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.PUNCH, worker);

        final DifficultyInstance difficulty = CompatibilityUtils.getWorld(worker).getDifficultyForLocation(new BlockPos(worker));
        arrowEntity.setDamage((baseDamage * BASE_DAMAGE_MULTIPLIER)
                                + worker.getRandom().nextGaussian() * RANDOM_DAMAGE_MULTPLIER
                                + CompatibilityUtils.getWorld(worker).getDifficulty().getDifficultyId() * DIFFICULTY_DAMAGE_INCREASE);

        if (powerEntchantment > 0)
        {
            arrowEntity.setDamage(arrowEntity.getDamage() + (double) powerEntchantment * BASE_POWER_ENCHANTMENT_DAMAGE + POWER_ENCHANTMENT_DAMAGE_MULTIPLIER);
        }

        if (punchEntchantment > 0)
        {
            arrowEntity.setKnockbackStrength(punchEntchantment);
        }

        boolean onFire = worker.isBurning() && difficulty.func_190083_c() && worker.getRandom().nextBoolean();
        onFire = onFire || EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.FLAME, worker) > 0;

        if (onFire)
        {
            arrowEntity.setFire(FIRE_EFFECT_CHANCE);
        }

        final ItemStack holdItem = worker.getHeldItem(EnumHand.OFF_HAND);
        if (holdItem != null && holdItem.getItem() == Items.TIPPED_ARROW)
        {
            arrowEntity.setPotionEffect(holdItem);
        }
    }

    private double getRandomPitch()
    {
        return PITCH_DIVIDER / (worker.getRNG().nextDouble() * PITCH_MULTIPLIER + BASE_PITCH);
    }
}
