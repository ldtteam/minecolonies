package com.minecolonies.coremod.entity.mobs.aitasks;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.api.util.constant.RaiderConstants;
import com.minecolonies.coremod.entity.mobs.barbarians.EntityChiefBarbarian;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.Goal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.Hand;

/**
 * Barbarian Attack AI class
 */
public class EntityAIRaiderAttackMelee extends Goal
{

    private static final int                           MAX_ATTACK_DELAY        = 60;
    private static final double                        HALF_ROTATION           = 180;
    private static final double                        MIN_DISTANCE_FOR_ATTACK = 2.5;
    private static final double                        ATTACK_SPEED            = 1.3;
    private static final int                           MUTEX_BITS              = 3;
    private final        AbstractEntityMinecoloniesMob entity;
    private              LivingEntity              target;
    private              int                           lastAttack              = 0;

    /**
     * Timer for the update rate of attack logic
     */
    private int tickTimer = 0;

    /**
     * Constructor method for AI
     *
     * @param creatureIn The creature which is using the AI
     */
    public EntityAIRaiderAttackMelee(final AbstractEntityMinecoloniesMob creatureIn)
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
     * Returns whether an in-progress Goal should continue executing
     *
     * @return Boolean value on whether or not to continue executing
     */
    @Override
    public boolean shouldContinueExecuting()
    {
        if (target.isAlive() && entity.isAlive() && entity.canEntityBeSeen(target))
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
     * AI for an Entity to attack the target, Called every Tick(20tps)
     *
     * @param target The target to attack
     */
    private void attack(final LivingEntity target)
    {
        // Limit Actions to every 10 Ticks
        if (tickTimer > 0)
        {
            tickTimer--;
            lastAttack--;
            return;
        }
        tickTimer = 10;

        if (target != null)
        {
            double damageToBeDealt = RaiderConstants.ATTACK_DAMAGE;

            if (entity instanceof EntityChiefBarbarian)
            {
                damageToBeDealt += 2.0;
            }

            if (entity.getDistance(target) <= MIN_DISTANCE_FOR_ATTACK && lastAttack <= 0 && entity.canEntityBeSeen(target))
            {
                target.attackEntityFrom(new EntityDamageSource(entity.getName(), entity), (float) damageToBeDealt);
                entity.swingArm(Hand.MAIN_HAND);
                entity.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, (float) 1.0D, (float) SoundUtils.getRandomPitch(entity.getRNG()));
                target.setRevengeTarget(entity);
                lastAttack = getAttackDelay();
            }
            if (lastAttack > 0)
            {
                lastAttack -= 1;
            }

            entity.faceEntity(target, (float) HALF_ROTATION, (float) HALF_ROTATION);
            entity.getLookHelper().setLookPositionWithEntity(target, (float) HALF_ROTATION, (float) HALF_ROTATION);

            entity.getNavigator().tryMoveToLivingEntity(target, ATTACK_SPEED);
        }
    }

    /**
     * Gets the delay time for a next attack by the barbarian.
     *
     * @return the reload time
     */
    protected int getAttackDelay()
    {
        return MAX_ATTACK_DELAY - MineColonies.getConfig().getCommon().gameplay.barbarianHordeDifficulty * 4;
    }
}
