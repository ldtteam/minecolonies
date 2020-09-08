package com.minecolonies.coremod.entity.mobs.aitasks;

import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.util.EntityUtils;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.coremod.MineColonies;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;

import java.util.EnumSet;

import static com.minecolonies.api.entity.mobs.RaiderMobUtils.MOB_ATTACK_DAMAGE;

/**
 * Barbarian Attack AI class
 */
public class EntityAIRaiderAttackMelee extends Goal
{

    private static final int                           MAX_ATTACK_DELAY        = 60;
    private static final double                        HALF_ROTATION           = 180;
    private static final double                        MIN_DISTANCE_FOR_ATTACK = 2.5;
    private static final double                        ATTACK_SPEED            = 1.3;
    public static final  int                           MUTEX_BITS              = 3;

    /**
     * Extended reach based on difficulty
     */
    private static final double EXTENDED_REACH_DIFFICUTLY = 1.9;
    private static final double EXTENDED_REACH            = 0.4;

    /**
     * Additional movement speed difficulty
     */
    private static final double ADD_SPEED_DIFFICULTY = 2.3;
    private static final double ADD_SPEED            = 0.3;

    private final        AbstractEntityMinecoloniesMob entity;
    private              LivingEntity                  target;
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
        this.setMutexFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean shouldExecute()
    {
        target = entity.getAttackTarget() != null ? entity.getAttackTarget() : entity.getAttackingEntity();
        return target != null && !EntityUtils.isFlying(target);
    }

    /**
     * Returns whether an in-progress Goal should continue executing
     *
     * @return Boolean value on whether or not to continue executing
     */
    @Override
    public boolean shouldContinueExecuting()
    {
        target = entity.getAttackTarget();
        if (target != null && target.isAlive() && entity.isAlive() && entity.canEntityBeSeen(target) && !EntityUtils.isFlying(target))
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
            double damageToBeDealt = entity.getAttribute(MOB_ATTACK_DAMAGE).getValue();

            if (entity.getDistance(target) <= (entity.getDifficulty() < EXTENDED_REACH_DIFFICUTLY ? MIN_DISTANCE_FOR_ATTACK : MIN_DISTANCE_FOR_ATTACK + EXTENDED_REACH)
                  && lastAttack <= 0 && entity.canEntityBeSeen(target))
            {
                target.attackEntityFrom(new EntityDamageSource(entity.getType().getTranslationKey(), entity), (float) damageToBeDealt);
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
            entity.getLookController().setLookPositionWithEntity(target, (float) HALF_ROTATION, (float) HALF_ROTATION);

            entity.getNavigator().tryMoveToEntityLiving(target, entity.getDifficulty() < ADD_SPEED_DIFFICULTY ? ATTACK_SPEED : ATTACK_SPEED + ADD_SPEED);
        }
    }

    /**
     * Gets the delay time for a next attack by the barbarian.
     *
     * @return the reload time
     */
    protected int getAttackDelay()
    {
        return MAX_ATTACK_DELAY - MineColonies.getConfig().getCommon().barbarianHordeDifficulty.get() * 4;
    }
}
