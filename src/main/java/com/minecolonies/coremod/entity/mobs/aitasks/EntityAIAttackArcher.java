package com.minecolonies.coremod.entity.mobs.aitasks;

import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.coremod.MineColonies;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;

import java.util.EnumSet;

import static com.minecolonies.api.entity.mobs.RaiderMobUtils.MOB_ATTACK_DAMAGE;

/**
 * Barbarian Ranged Attack AI class
 */
public class EntityAIAttackArcher extends Goal
{
    /**
     * Max delay between attacks is 4s, aka 80 ticks.
     */
    private static final int MAX_ATTACK_DELAY = 80;

    private static final double    PITCH_MULTIPLIER            = 0.4;
    private static final double    HALF_ROTATION               = 180;
    private static final double    ATTACK_SPEED                = 1.3;
    private static final double                        AIM_HEIGHT                  = 3.0D;
    private static final double                        ARROW_SPEED                 = 1.6D;
    private static final double                        HIT_CHANCE                  = 10.0D;
    private static final double                        AIM_SLIGHTLY_HIGHER_MULTIPLIER = 0.20000000298023224D;
    private static final double                        BASE_PITCH                     = 0.8D;
    private static final double                        PITCH_DIVIDER                  = 1.0D;
    private static final double                        MAX_ATTACK_DISTANCE            = 20.0D;
    private final        AbstractEntityMinecoloniesMob entity;
    private              LivingEntity                  target;
    private              int                           lastAttack = 0;

    /**
     * Timer for the update rate of attack logic
     */
    private int tickTimer = 0;

    /**
     * Constructor method for AI
     *
     * @param creatureIn The creature which is using the AI
     */
    public EntityAIAttackArcher(final AbstractEntityMinecoloniesMob creatureIn)
    {
        super();
        this.entity = creatureIn;
        this.setMutexFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean shouldExecute()
    {
        target = entity.getAttackTarget() != null ? entity.getAttackTarget() : entity.getAttackingEntity();
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
        target = entity.getAttackTarget();
        if (target != null && target.isAlive() && entity.isAlive() && entity.canEntityBeSeen(target))
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
                AbstractArrowEntity arrowEntity = EntityType.ARROW.create(target.world);
                arrowEntity.setShooter(entity);

                final ItemStack bow = entity.getHeldItem(Hand.MAIN_HAND);
                if (bow.getItem() instanceof BowItem)
                {
                    arrowEntity = ((BowItem) bow.getItem()).customeArrow(arrowEntity);
                }

                arrowEntity.setPosition(entity.posX, entity.posY + 1, entity.posZ);
                final double xVector = target.posX - entity.posX;
                final double yVector = target.getBoundingBox().minY + target.getHeight() / AIM_HEIGHT - arrowEntity.posY;
                final double zVector = target.posZ - entity.posZ;
                final double distance = (double) MathHelper.sqrt(xVector * xVector + zVector * zVector);
                //Lower the variable higher the chance that the arrows hits the target.
                arrowEntity.setDamage(entity.getAttribute(MOB_ATTACK_DAMAGE).getValue());
                arrowEntity.shoot(xVector, yVector + distance * AIM_SLIGHTLY_HIGHER_MULTIPLIER, zVector, (float) ARROW_SPEED, (float) HIT_CHANCE);

                entity.faceEntity(target, (float) HALF_ROTATION, (float) HALF_ROTATION);
                entity.getLookController().setLookPositionWithEntity(target, (float) HALF_ROTATION, (float) HALF_ROTATION);

                CompatibilityUtils.addEntity(CompatibilityUtils.getWorldFromEntity(entity), arrowEntity);
                entity.swingArm(Hand.MAIN_HAND);
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
        return MAX_ATTACK_DELAY - MineColonies.getConfig().getCommon().barbarianHordeDifficulty.get() * 4;
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
