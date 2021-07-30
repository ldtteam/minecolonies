package com.minecolonies.coremod.entity.mobs.aitasks;

import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.combat.threat.IThreatTableEntity;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.entity.pathfinding.PathResult;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.entity.ai.combat.AttackMoveAI;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.util.NamedDamageSource;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.TranslationTextComponent;

import static com.minecolonies.api.entity.mobs.RaiderMobUtils.MOB_ATTACK_DAMAGE;

/**
 * Raider AI for melee attacking a target
 */
public class RaiderMeleeAI<T extends AbstractEntityMinecoloniesMob & IThreatTableEntity> extends AttackMoveAI<T>
{
    /**
     * Extended reach based on difficulty
     */
    private static final double EXTENDED_REACH_DIFFICULTY = 1.9;
    private static final double EXTENDED_REACH            = 0.4;
    private static final double MIN_DISTANCE_FOR_ATTACK   = 2.5;

    /**
     * Attack delay
     */
    private static final int MAX_ATTACK_DELAY = 60;

    /**
     * Additional movement speed difficulty
     */
    private static final double ADD_SPEED_DIFFICULTY = 2.3;
    private static final double BONUS_SPEED          = 1.2;
    private static final double BASE_COMBAT_SPEED    = 1.2;

    public RaiderMeleeAI(
      final T owner,
      final ITickRateStateMachine<IState> stateMachine)
    {
        super(owner, stateMachine);
    }

    @Override
    protected void doAttack(final LivingEntity target)
    {
        double damageToBeDealt = user.getAttribute(MOB_ATTACK_DAMAGE).getValue();
        target.hurt(new NamedDamageSource("death.attack." + ((TranslationTextComponent) user.getName()).getKey(), user), (float) damageToBeDealt);
        user.swing(Hand.MAIN_HAND);
        user.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, (float) 1.0D, (float) SoundUtils.getRandomPitch(user.getRandom()));
        target.setLastHurtByMob(user);
    }

    @Override
    protected double getAttackDistance()
    {
        return user.getDifficulty() < EXTENDED_REACH_DIFFICULTY ? MIN_DISTANCE_FOR_ATTACK : MIN_DISTANCE_FOR_ATTACK + EXTENDED_REACH;
    }

    @Override
    protected int getAttackDelay()
    {
        return MAX_ATTACK_DELAY - MineColonies.getConfig().getServer().barbarianHordeDifficulty.get() * 4;
    }

    @Override
    protected PathResult moveInAttackPosition(final LivingEntity target)
    {
        return user.getNavigation()
                 .moveToXYZ(target.getX(), target.getY(), target.getZ(), user.getDifficulty() < ADD_SPEED_DIFFICULTY ? BASE_COMBAT_SPEED : BASE_COMBAT_SPEED * BONUS_SPEED);
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

    @Override
    protected int getSearchRange()
    {
        return 0;
    }
}
