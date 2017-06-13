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

    final private EntityCreature entity;
    private EntityLivingBase target;
    private int LastAttack = 0;

    public EntityAIBarbarianAttackMelee(final EntityCreature creatureIn)
    {
        super();
        this.entity = creatureIn;
        this.setMutexBits(3);
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
        if(target.isEntityAlive() && entity.isEntityAlive() && !target.getIsInvulnerable())
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


            if(damageToBeDealt == 0)
            {
                damageToBeDealt = 3;
            }

            if (entity.getDistanceToEntity(target) <= 2.5 && LastAttack <= 0)
            {
                target.attackEntityFrom(new DamageSource(entity.getName()), (float) damageToBeDealt);
                entity.swingArm(EnumHand.MAIN_HAND);
                entity.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, (float) 1.0D, (float) getRandomPitch());
                target.setRevengeTarget(entity);
                LastAttack = 16;
            }
            if (LastAttack > 0)
            {
                LastAttack -= 1;
            }

            entity.faceEntity(target, (float) 180D, (float) 180D);
            entity.getLookHelper().setLookPositionWithEntity(target, (float) 180D, (float) 180D);

            entity.getNavigator().tryMoveToEntityLiving(target, 1.3D);
        }
    }

    private double getRandomPitch()
    {
        return 1.0D / (entity.getRNG().nextDouble() * 0.4D + 0.8D);
    }
}
