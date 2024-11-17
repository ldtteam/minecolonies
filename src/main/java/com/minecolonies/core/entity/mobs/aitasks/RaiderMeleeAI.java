package com.minecolonies.core.entity.mobs.aitasks;

import com.minecolonies.api.entity.ai.combat.threat.IThreatTableEntity;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.mobs.AbstractEntityRaiderMob;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.entity.ai.combat.AttackMoveAI;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.minecolonies.core.entity.pathfinding.pathresults.PathResult;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import static com.minecolonies.api.entity.mobs.RaiderMobUtils.MOB_ATTACK_DAMAGE;

/**
 * Raider AI for melee attacking a target
 */
public class RaiderMeleeAI<T extends AbstractEntityRaiderMob & IThreatTableEntity> extends AttackMoveAI<T>
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
    private static final int ATTACK_DELAY = 30;

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
        if (user.getName().getContents() instanceof TranslatableContents translatableContents)
        {
            target.hurt(target.level().damageSources().source(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Constants.MOD_ID, translatableContents.getKey().replace("entity.minecolonies.", ""))), user), (float) damageToBeDealt);
        }
        else
        {
            target.hurt(target.level().damageSources().mobAttack(user), (float) damageToBeDealt);
        }
        user.swing(InteractionHand.MAIN_HAND);
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
        return ATTACK_DELAY;
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
        return (target instanceof EntityCitizen && !target.isInvisible()) || (target instanceof Player && !((Player) target).isCreative() && !target.isSpectator());
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
