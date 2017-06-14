package com.minecolonies.coremod.entity.ai.mobs;

import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;

/**
 * Created by Asher on 13/6/17.
 */
public class EntityAIBarbarianAttackMelee extends EntityAIBase
{

    private final EntityCreature   entity;
    private       EntityLivingBase target;
    private int lastAttack = 0;
    /* default */ private static final int    CYCLES_TO_WAIT          = 16;
    /* default */ private static final double PITCH_MULTIPLIER        = 0.4;
    /* default */ private static final double PITCH_ADDER             = 0.8;
    /* default */ private static final double DEFAULT_DAMAGE          = 3.0;
    /* default */ private static final double HALF_ROTATION           = 180;
    /* default */ private static final double MIN_DISTANCE_FOR_ATTACK = 2.5;
    /* default */ private static final double ATTACK_SPEED            = 1.3;
    /* default */ private static final int    MUTEX_BITS              = 3;
    /* default */ private static final double IS_ZERO = 0;

    public EntityAIBarbarianAttackMelee(final EntityCreature creatureIn)
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
     */
    public boolean continueExecuting()
    {
        if (target.isEntityAlive() && entity.isEntityAlive() && !target.getIsInvulnerable())
        {
            attack(target);
            return true;
        }
        return false;
    }

    public void startExecuting()
    {
        attack(target);
    }

    public void attack(final EntityLivingBase target)
    {
        double damageToBeDealt = 0;

        final ItemStack heldItem = entity.getHeldItem(EnumHand.MAIN_HAND);

        if (target != null)
        {
            if (ItemStackUtils.doesItemServeAsWeapon(heldItem) && heldItem.getItem() instanceof ItemSword)
            {
                damageToBeDealt += ((ItemSword) heldItem.getItem()).getDamageVsEntity();
            }


            if (damageToBeDealt == IS_ZERO)
            {
                damageToBeDealt = DEFAULT_DAMAGE;
            }

            if (entity.getDistanceToEntity(target) <= MIN_DISTANCE_FOR_ATTACK && lastAttack <= 0)
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

    private double getRandomPitch()
    {
        return 1.0D / (entity.getRNG().nextDouble() * PITCH_MULTIPLIER + PITCH_ADDER);
    }
}
