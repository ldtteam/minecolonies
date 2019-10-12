package com.minecolonies.coremod.entity.mobs.aitasks;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.CompatibilityUtils;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;

/**
 * Barbarian Ranged Attack AI class
 */
public class EntityAIAttackArcher extends EntityAIBase
{
    /**
     * Max delay between attacks is 4s, aka 80 ticks.
     */
    private static final int MAX_ATTACK_DELAY = 80;

    private static final double    PITCH_MULTIPLIER            = 0.4;
    private static final double    HALF_ROTATION               = 180;
    private static final double    ATTACK_SPEED                = 1.3;
    private static final int       MUTEX_BITS                  = 3;
    private static final double    AIM_HEIGHT                  = 3.0D;
    private static final double    ARROW_SPEED                 = 1.6D;
    private static final double    HIT_CHANCE                  = 10.0D;
    private static final double AIM_SLIGHTLY_HIGHER_MULTIPLIER = 0.20000000298023224D;
    private static final double BASE_PITCH                     = 0.8D;
    private static final double PITCH_DIVIDER                  = 1.0D;
    private static final double MAX_ATTACK_DISTANCE            = 20.0D;
    private final EntityCreature   entity;
    private       EntityLivingBase target;
    private int lastAttack = 0;

    /**
     * Timer for the update rate of attack logic
     */
    private int tickTimer = 0;

    /**
     * Constructor method for AI
     *
     * @param creatureIn The creature which is using the AI
     */
    public EntityAIAttackArcher(final EntityCreature creatureIn)
    {
        super();
        this.entity = creatureIn;
        this.setMutexBits(MUTEX_BITS);
    }

    @Override
    public boolean shouldExecute()
    {
        target = entity.getAttackTarget();
        return target != null;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     *
     * @return Boolean value on whether or not to continue executing
     */
    @Override
    public boolean shouldContinueExecuting()
    {
        target = entity.getAttackTarget();
        if (target != null && target.isEntityAlive() && entity.isEntityAlive())
        {
            attack(target);
            return true;
        }
        return false;
    }

    /**
     * Is executed when the ai Starts Executing
     */
    @Override
    public void startExecuting()
    {
        attack(target);
    }

    /**
     * AI for an Entity to attack the target. Called every Tick(20tps)
     * @param target The target to attack
     */
    private void attack(final EntityLivingBase target)
    {
        // Limit Actions to every 10 Ticks
        if (tickTimer > 0)
        {
            tickTimer--;
            lastAttack--;
            return;
        }
        tickTimer = 10;

        if (entity.getDistance(target) >= MAX_ATTACK_DISTANCE || !entity.canEntityBeSeen(target))
        {
            entity.getNavigator().tryMoveToEntityLiving(target, ATTACK_SPEED);
        }
        else
        {
            // Stop walking when we're in attack range
            if (entity.getDistance(target) < MAX_ATTACK_DISTANCE)
            {
                entity.getNavigator().clearPath();
            }

            if (lastAttack <= 0 && entity.canEntityBeSeen(target))
            {

                final EntityTippedArrow arrowEntity = new EntityTippedArrow(CompatibilityUtils.getWorldFromEntity(entity), entity);
                final double xVector = target.posX - entity.posX;
                final double yVector = target.getEntityBoundingBox().minY + target.height / AIM_HEIGHT - arrowEntity.posY;
                final double zVector = target.posZ - entity.posZ;
                final double distance = (double) MathHelper.sqrt(xVector * xVector + zVector * zVector);
                //Lower the variable higher the chance that the arrows hits the target.

                arrowEntity.shoot(xVector, yVector + distance * AIM_SLIGHTLY_HIGHER_MULTIPLIER, zVector, (float) ARROW_SPEED, (float) HIT_CHANCE);

                entity.faceEntity(target, (float) HALF_ROTATION, (float) HALF_ROTATION);
                entity.getLookHelper().setLookPositionWithEntity(target, (float) HALF_ROTATION, (float) HALF_ROTATION);

                CompatibilityUtils.spawnEntity(CompatibilityUtils.getWorldFromEntity(entity), arrowEntity);
                entity.swingArm(EnumHand.MAIN_HAND);
                entity.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, (float) 1.0D, (float) getRandomPitch());
                lastAttack = getAttackDelay();
            }
        }
        if (lastAttack > 0)
        {
            lastAttack -= 1;
        }
    }

    /**
     * Gets the delay time for a next attack by the barbarian.
     *
     * @return the reload time
     */
    protected int getAttackDelay()
    {
        return MAX_ATTACK_DELAY - Configurations.gameplay.barbarianHordeDifficulty * 4;
    }

    /**
     * Random pitch generator
     *
     * @return A random double to act as a pitch value
     */
    private double getRandomPitch()
    {
        return PITCH_DIVIDER / (entity.getRNG().nextDouble() * PITCH_MULTIPLIER + BASE_PITCH);
    }
}
