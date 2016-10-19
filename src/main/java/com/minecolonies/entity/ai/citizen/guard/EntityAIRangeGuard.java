package com.minecolonies.entity.ai.citizen.guard;

import com.minecolonies.colony.jobs.JobGuard;
import com.minecolonies.entity.ai.util.AIState;
import com.minecolonies.entity.ai.util.AITarget;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.entity.ai.util.AIState.*;

/**
 * Handles the AI of the guard entities.
 */
public class EntityAIRangeGuard extends AbstractEntityAIGuard implements IRangedAttackMob
{
    /**
     * Basic delay for the next shot.
     */
    private static final int BASE_RELOAD_TIME = 100;

    /**
     * Base damage which the power enchantments added
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
    private static final double AIM_SLIGHTLY_HIGHER_MULTIPLIER   = 0.20000000298023224D;

    /**
     * Normal volume at which sounds are played at.
     */
    private static final double BASIC_VOLUME           = 1.0D;

    /**
     * Guard has to aim x higher to hit his target.
     */
    private static final double AIM_HEIGHT = 3.0D;

    /**
     * Experience the guard receives each shot arrow.
     */
    private static final double XP_EACH_ARROW   = 0.2;

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
     * Sets up some important skeleton stuff for every ai.
     *
     * @param job the job class
     */
    public EntityAIRangeGuard(@NotNull final JobGuard job)
    {
        super(job);
        super.registerTargets(
                new AITarget(IDLE, () -> START_WORKING),
                new AITarget(START_WORKING, () -> GUARD_RESTOCK),
                new AITarget(GUARD_SEARCH_TARGET, this::searchTarget),
                new AITarget(GUARD_GET_TARGET, this::getTarget),
                new AITarget(GUARD_HUNT_DOWN_TARGET, this::huntDown),
                new AITarget(GUARD_PATROL, this::patrol),
                new AITarget(GUARD_RESTOCK, this::goToBuilding)
        );

        if(worker.getCitizenData() != null)
        {
            worker.setSkillModifier(2 * worker.getCitizenData().getIntelligence() + worker.getCitizenData().getStrength());
            worker.setCanPickUpLoot(true);
        }
    }

    private int getReloadTime()
    {
        return BASE_RELOAD_TIME / (worker.getExperienceLevel() + 1);
    }

    /**
     * Follow the target and kill it.
     * @return the next AIState.
     */
    protected AIState huntDown()
    {
        if(!targetEntity.isEntityAlive())
        {
            targetEntity = null;
        }

        if (targetEntity != null)
        {
            if(worker.getEntitySenses().canSee(targetEntity) && worker.getDistanceToEntity(targetEntity) <= MAX_ATTACK_DISTANCE)
            {
                worker.resetActiveHand();
                attackEntityWithRangedAttack(targetEntity, 1);
                setDelay(getReloadTime());
                arrowsShot += 1;

                if(arrowsShot >= getMaxArrowsShot())
                {
                    return AIState.GUARD_RESTOCK;
                }

                return AIState.GUARD_HUNT_DOWN_TARGET;
            }
            worker.setAIMoveSpeed((float) (BASE_FOLLOW_SPEED + BASE_FOLLOW_SPEED_MULTIPLIER * worker.getExperienceLevel()));
            worker.isWorkerAtSiteWithMove(targetEntity.getPosition(), 3);

            return AIState.GUARD_SEARCH_TARGET;
        }

        worker.setAIMoveSpeed(1);
        return AIState.GUARD_SEARCH_TARGET;
    }

    /**
     * Can be overridden in implementations.
     * <p>
     * Here the AI can check if the fishes or rods have to be re rendered and do it.
     */
    @Override
    protected void updateRenderMetaData()
    {
        updateArmor();
    }

    @Override
    public void attackEntityWithRangedAttack(@NotNull EntityLivingBase entityToAttack, float baseDamage)
    {
        EntityTippedArrow arrowEntity = new EntityTippedArrow(this.worker.worldObj, worker);
        double xVector = entityToAttack.posX - worker.posX;
        double yVector = entityToAttack.getEntityBoundingBox().minY + entityToAttack.height / AIM_HEIGHT - arrowEntity.posY;
        double zVector = entityToAttack.posZ - worker.posZ;
        double distance = (double) MathHelper.sqrt_double(xVector * xVector + zVector * zVector);

        //Lower the variable higher the chance that the arrows hits the target.
        double chance = HIT_CHANCE_DIVIDER / (worker.getExperienceLevel()+1);

        arrowEntity.setThrowableHeading(xVector, yVector + distance * AIM_SLIGHTLY_HIGHER_MULTIPLIER, zVector, (float) ARROW_SPEED, (float) chance);

        addEffectsToArrow(arrowEntity, baseDamage);

        worker.addExperience(XP_EACH_ARROW);
        worker.faceEntity(entityToAttack, (float)TURN_AROUND, (float)TURN_AROUND);
        worker.getLookHelper().setLookPositionWithEntity(entityToAttack, (float)TURN_AROUND, (float)TURN_AROUND);

        double xDiff = targetEntity.posX - worker.posX;
        double zDiff = targetEntity.posZ - worker.posZ;

        double goToX = xDiff > 0? MOVE_MINIMAL : -MOVE_MINIMAL;
        double goToZ = zDiff > 0? MOVE_MINIMAL : -MOVE_MINIMAL;

        worker.moveEntity(goToX, 0, goToZ);

        worker.swingArm(EnumHand.MAIN_HAND);
        worker.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, (float) BASIC_VOLUME, (float) getRandomPitch());
        worker.worldObj.spawnEntityInWorld(arrowEntity);

        worker.damageItemInHand(1);
    }

    /**
     * Method used to add potion/enchantment effects to the bow depending on his enchantments etc.
     * @param arrowEntity the arrow to add these effects to.
     * @param baseDamage the arrow base damage.
     */
    private void addEffectsToArrow(EntityTippedArrow arrowEntity, double baseDamage)
    {
        int powerEntchanment = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.POWER, worker);
        int punchEntchanment = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.PUNCH, worker);

        DifficultyInstance difficulty = this.worker.worldObj.getDifficultyForLocation(new BlockPos(worker));
        arrowEntity.setDamage((baseDamage * BASE_DAMAGE_MULTIPLIER)
                + worker.getRandom().nextGaussian() * RANDOM_DAMAGE_MULTPLIER
                + this.worker.worldObj.getDifficulty().getDifficultyId() * DIFFICULTY_DAMAGE_INCREASE);

        if(powerEntchanment > 0)
        {
            arrowEntity.setDamage(arrowEntity.getDamage() + (double)powerEntchanment * BASE_POWER_ENCHANTMENT_DAMAGE + POWER_ENCHANTMENT_DAMAGE_MULTIPLIER);
        }

        if(punchEntchanment > 0)
        {
            arrowEntity.setKnockbackStrength(punchEntchanment);
        }

        boolean onFire = worker.isBurning() && difficulty.func_190083_c() && worker.getRandom().nextBoolean();
        onFire = onFire || EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.FLAME, worker) > 0;

        if(onFire)
        {
            arrowEntity.setFire(FIRE_EFFECT_CHANCE);
        }

        ItemStack holdItem = worker.getHeldItem(EnumHand.OFF_HAND);
        if(holdItem != null && holdItem.getItem() == Items.TIPPED_ARROW)
        {
            arrowEntity.setPotionEffect(holdItem);
        }
    }

    private double getRandomPitch()
    {
        return PITCH_DIVIDER / (worker.getRNG().nextDouble() * PITCH_MULTIPLIER + BASE_PITCH);
    }
}
