package com.minecolonies.coremod.entity.mobs.aitasks;

import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.util.EntityUtils;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.entity.CustomArrowEntity;
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

import net.minecraft.entity.ai.goal.Goal.Flag;

/**
 * Barbarian Ranged Attack AI class
 */
public class EntityAIAttackArcher extends Goal
{
    /**
     * Max delay between attacks is 4s, aka 80 ticks.
     */
    private static final int MAX_ATTACK_DELAY = 80;

    /**
     * Difficulty leve at which arrows do pierce
     */
    private static final double ARROW_PIERCE_DIFFICULTY = 3.0d;

    private static final double PITCH_MULTIPLIER               = 0.4;
    private static final double HALF_ROTATION                  = 180;
    private static final double ATTACK_SPEED                   = 1.1;
    private static final double AIM_HEIGHT                     = 2.0D;
    private static final double ARROW_SPEED                    = 1.4D;
    private static final double HIT_CHANCE                     = 10.0D;
    private static final double AIM_SLIGHTLY_HIGHER_MULTIPLIER = 0.20000000298023224D;
    private static final double BASE_PITCH                     = 0.8D;
    private static final double PITCH_DIVIDER                  = 1.0D;
    private static final double                        MAX_ATTACK_DISTANCE            = 20.0D;
    private static final double                        SPEED_FOR_DIST                 = 35;
    private final        AbstractEntityMinecoloniesMob entity;
    private              LivingEntity                  target;
    private              int                           lastAttack                     = 0;

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
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse()
    {
        target = entity.getTarget() != null ? entity.getTarget() : entity.getKillCredit();
        return target != null;
    }

    /**
     * Returns whether an in-progress Goal should continue executing
     *
     * @return Boolean value on whether or not to continue executing
     */
    @Override
    public boolean canContinueToUse()
    {
        target = entity.getTarget();
        if (target != null && target.isAlive() && entity.isAlive() && entity.canSee(target))
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
    public void start()
    {
        attack(target);
    }

    /**
     * AI for an Entity to attack the target. Called every Tick(20tps)
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

        if ((entity.distanceTo(target) >= MAX_ATTACK_DISTANCE * Math.max(entity.getDifficulty(), 2.0d) && !EntityUtils.isFlying(target)) || !entity.canSee(target))
        {
            entity.getNavigation().moveTo(target, ATTACK_SPEED);
        }
        else
        {
            // Stop walking when we're in attack range
            if (entity.distanceTo(target) < MAX_ATTACK_DISTANCE)
            {
                entity.getNavigation().stop();
            }

            if (lastAttack <= 0 && entity.canSee(target))
            {
                AbstractArrowEntity arrowEntity = ModEntities.MC_NORMAL_ARROW.create(target.level);
                arrowEntity.setOwner(entity);

                final ItemStack bow = entity.getLastHandItem(Hand.MAIN_HAND);
                if (bow.getItem() instanceof BowItem)
                {
                    arrowEntity = ((BowItem) bow.getItem()).customArrow(arrowEntity);
                }

                arrowEntity.setPos(entity.getX(), entity.getY() + 1, entity.getZ());
                final double xVector = target.getX() - entity.getX();
                final double yVector = target.getBoundingBox().minY + target.getBbHeight() / AIM_HEIGHT - arrowEntity.getY();
                final double zVector = target.getZ() - entity.getZ();
                final double distance = MathHelper.sqrt(xVector * xVector + zVector * zVector);
                final double dist3d = MathHelper.sqrt(yVector * yVector + xVector * xVector + zVector * zVector);
                //Lower the variable higher the chance that the arrows hits the target.
                arrowEntity.setBaseDamage(entity.getAttribute(MOB_ATTACK_DAMAGE).getValue());

                if (entity.getDifficulty() > ARROW_PIERCE_DIFFICULTY)
                {
                    arrowEntity.setPierceLevel((byte) 2);
                }

                if (EntityUtils.isFlying(target))
                {
                    ((CustomArrowEntity) arrowEntity).setPlayerArmorPierce();
                    arrowEntity.setSecondsOnFire(200);
                    arrowEntity.setBaseDamage(10);
                    lastAttack = 10;
                }
                else
                {
                    lastAttack = getAttackDelay();
                }
                arrowEntity.shoot(xVector, yVector + distance * AIM_SLIGHTLY_HIGHER_MULTIPLIER, zVector, (float) (ARROW_SPEED * 1 + (dist3d / SPEED_FOR_DIST)), (float) HIT_CHANCE);

                entity.lookAt(target, (float) HALF_ROTATION, (float) HALF_ROTATION);
                entity.getLookControl().setLookAt(target, (float) HALF_ROTATION, (float) HALF_ROTATION);

                target.level.addFreshEntity(arrowEntity);
                entity.swing(Hand.MAIN_HAND);
                entity.playSound(SoundEvents.SKELETON_SHOOT, (float) 1.0D, (float) getRandomPitch());
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
        return MAX_ATTACK_DELAY - MineColonies.getConfig().getServer().barbarianHordeDifficulty.get() * 4;
    }

    /**
     * Random pitch generator
     *
     * @return A random double to act as a pitch value
     */
    private double getRandomPitch()
    {
        return PITCH_DIVIDER / (entity.getRandom().nextDouble() * PITCH_MULTIPLIER + BASE_PITCH);
    }
}
