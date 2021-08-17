package com.minecolonies.coremod.entity.mobs.aitasks;

import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.combat.threat.IThreatTableEntity;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.entity.pathfinding.PathResult;
import com.minecolonies.api.util.EntityUtils;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.entity.CustomArrowEntity;
import com.minecolonies.coremod.entity.ai.combat.AttackMoveAI;
import com.minecolonies.coremod.entity.ai.combat.CombatUtils;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;

import static com.minecolonies.api.entity.mobs.RaiderMobUtils.MOB_ATTACK_DAMAGE;

/**
 * Raider AI for melee attacking a target
 */
public class RaiderRangedAI<T extends AbstractEntityMinecoloniesMob & IThreatTableEntity> extends AttackMoveAI<T>
{
    /**
     * Max delay between attacks is 3s, aka 60 ticks.
     */
    private static final int MAX_ATTACK_DELAY = 60;

    /**
     * Min delay between attacks is 1s, aka 20 ticks.
     */
    private static final int MIN_ATTACK_DELAY = 20;

    /**
     * Difficulty level at which arrows do pierce
     */
    private static final double ARROW_PIERCE_DIFFICULTY = 3.0d;

    /**
     * Movement speed
     */
    private static final double COMBAT_MOVEMENT_SPEED = 1.1;

    /**
     * Attack distance
     */
    private static final double MAX_ATTACK_DISTANCE = 20.0D;

    /**
     * Sound variance
     */
    private static final double PITCH_MULTIPLIER = 0.4;
    private static final double BASE_PITCH       = 0.8D;
    private static final double PITCH_DIVIDER    = 1.0D;

    /**
     * Counter for flying time
     */
    private int flightCounter = 0;

    public RaiderRangedAI(
      final T owner,
      final ITickRateStateMachine<IState> stateMachine)
    {
        super(owner, stateMachine);
    }

    @Override
    protected boolean isInDistanceForAttack(final LivingEntity target)
    {
        if (EntityUtils.isFlying(target))
        {
            flightCounter++;
        }
        else
        {
            flightCounter = 0;
        }

        if (flightCounter > 5)
        {
            // Always allowed to try attacking flying targets
            return true;
        }

        return super.isInAttackDistance(target);
    }

    @Override
    protected void doAttack(final LivingEntity target)
    {
        user.getNavigation().stop();

        // Setup arrow
        AbstractArrowEntity arrowEntity = CombatUtils.createArrowForShooter(user);

        arrowEntity.setBaseDamage(user.getAttribute(MOB_ATTACK_DAMAGE).getValue());
        if (flightCounter > 5)
        {
            ((CustomArrowEntity) arrowEntity).setPlayerArmorPierce();
            arrowEntity.setSecondsOnFire(200);
            arrowEntity.setBaseDamage(10);
        }

        if (user.getDifficulty() > ARROW_PIERCE_DIFFICULTY)
        {
            arrowEntity.setPierceLevel((byte) 2);
        }

        // Shoot arrow
        CombatUtils.shootArrow(arrowEntity, target, 10.0f);

        // Visuals
        user.swing(Hand.MAIN_HAND);
        user.playSound(SoundEvents.SKELETON_SHOOT, (float) 1.0D, (float) getRandomPitch());
    }

    /**
     * Random pitch generator
     *
     * @return A random double to act as a pitch value
     */
    private double getRandomPitch()
    {
        return PITCH_DIVIDER / (user.getRandom().nextDouble() * PITCH_MULTIPLIER + BASE_PITCH);
    }

    @Override
    protected double getAttackDistance()
    {
        return MAX_ATTACK_DISTANCE * Math.max(user.getDifficulty(), 2.0d);
    }

    @Override
    protected int getAttackDelay()
    {
        if (flightCounter > 5)
        {
            return 10;
        }

        return (int) Math.max(MIN_ATTACK_DELAY, MAX_ATTACK_DELAY - MineColonies.getConfig().getServer().barbarianHordeDifficulty.get() * 4 * user.getDifficulty());
    }

    @Override
    protected PathResult moveInAttackPosition(final LivingEntity target)
    {
        return user.getNavigation().moveToXYZ(target.getX(), target.getY(), target.getZ(), COMBAT_MOVEMENT_SPEED);
    }

    @Override
    protected boolean isAttackableTarget(final LivingEntity target)
    {
        return target instanceof EntityCitizen || (target instanceof PlayerEntity && !((PlayerEntity) target).isCreative() && !target.isSpectator());
    }

    @Override
    protected boolean isWithinPersecutionDistance(final LivingEntity target)
    {
        return true;
    }
}
