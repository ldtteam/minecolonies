package com.minecolonies.coremod.entity.ai.mobs.barbarians;

import com.minecolonies.coremod.entity.ai.mobs.util.BarbarianSpawnUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;

/**
 * Barbarian Attack AI class
 */
public class EntityAIBarbarianAttackMelee extends EntityAIBase
{

    private static final int    CYCLES_TO_WAIT          = 16;
    private static final double PITCH_MULTIPLIER        = 0.4;
    private static final double PITCH_ADDER             = 0.8;
    private static final double HALF_ROTATION           = 180;
    private static final double MIN_DISTANCE_FOR_ATTACK = 2.5;
    private static final double ATTACK_SPEED            = 1.3;
    private static final int    MUTEX_BITS              = 3;
    private final AbstractEntityBarbarian entity;
    private       EntityLivingBase        target;
    private int lastAttack = 0;

    /**
     * Constructor method for AI
     *
     * @param creatureIn The creature which is using the AI
     */
    public EntityAIBarbarianAttackMelee(final AbstractEntityBarbarian creatureIn)
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
        if (target.isEntityAlive() && entity.isEntityAlive() && entity.canEntityBeSeen(target))
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
     * AI for an Entity to attack the target
     *
     * @param target The target to attack
     */
    private void attack(final EntityLivingBase target)
    {
        if (target != null)
        {
            double damageToBeDealt = BarbarianSpawnUtils.ATTACK_DAMAGE;

            if (entity instanceof EntityChiefBarbarian)
            {
                damageToBeDealt += 1.0;
            }

            if (entity.getDistance(target) <= MIN_DISTANCE_FOR_ATTACK && lastAttack <= 0 && entity.canEntityBeSeen(target))
            {
                target.attackEntityFrom(new DamageSource(entity.getName()), (float) damageToBeDealt);
                entity.swingArm(EnumHand.MAIN_HAND);
                entity.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, (float) 1.0D, (float) getRandomPitch());
                target.setRevengeTarget(entity);
                lastAttack = CYCLES_TO_WAIT;
            }
            if (lastAttack > 0)
            {
                lastAttack -= 1;
            }

            entity.faceEntity(target, (float) HALF_ROTATION, (float) HALF_ROTATION);
            entity.getLookHelper().setLookPositionWithEntity(target, (float) HALF_ROTATION, (float) HALF_ROTATION);

            entity.getNavigator().tryMoveToEntityLiving(target, ATTACK_SPEED);
        }
    }

    /**
     * Random pitch generator
     *
     * @return A random double to act as a pitch value
     */
    private double getRandomPitch()
    {
        return 1.0D / (entity.getRNG().nextDouble() * PITCH_MULTIPLIER + PITCH_ADDER);
    }
}
