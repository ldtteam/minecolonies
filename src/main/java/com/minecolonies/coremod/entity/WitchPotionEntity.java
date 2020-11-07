package com.minecolonies.coremod.entity;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.colony.jobs.JobWitch;
import com.minecolonies.coremod.entity.ai.citizen.guard.EntityAIWitch;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WitchPotionEntity extends PotionEntity
{

    /**
     * The X and Z size of the splash area
     */
    public static final double SPLASH_SIZE = 4.0D;

    /**
     * The height of the splash area
     */
    public static final double SPLASH_HEIGTH = 2.0D;

    /**
     * The maximum distance at which an entity gets affected
     */
    public static final double MAX_DISTANCE = 16.0D;

    /**
     * The minimum duration to get affected
     */
    public static final int MIN_DURATION = 20;

    /**
     * Throws a potion at the target with the given inaccuracy
     *
     * @param potionStack the {@link ItemStack} of the Potion, {@link ItemStack#getItem()} must return a {@link net.minecraft.item.ThrowablePotionItem}
     * @param target the targeted {@link LivingEntity} to throw the potion at
     * @param thrower the witch throwing the potion
     * @param world the {@link World} of the thrower
     * @param velocity the velocity to throw the potion with
     * @param inaccuracy the inaccuracy to throw the potion with
     */
    public static void throwPotionAt(final ItemStack potionStack, final LivingEntity target, final AbstractEntityCitizen thrower, final World world, final float velocity, final float inaccuracy)
    {
        final PotionEntity potionentity = new WitchPotionEntity(world, thrower);
        potionentity.setItem(potionStack);
        potionentity.shoot(target.getPosX(), target.getPosY(), target.getPosZ(), velocity, inaccuracy);
        world.addEntity(potionentity);
    }

    public WitchPotionEntity(final EntityType<? extends PotionEntity> typeIn, final World worldIn)
    {
        super(typeIn, worldIn);
    }

    public WitchPotionEntity(final World worldIn, final AbstractEntityCitizen livingEntityIn)
    {
        super(worldIn, livingEntityIn);
    }

    @Override
    protected void func_213888_a(final List<EffectInstance> effects, @Nullable final Entity entity)
    {
        final AbstractEntityCitizen citizen = this.getThrower();
        if (citizen != null && citizen.getCitizenData().getJob() instanceof JobWitch)
        {
            final EntityAIWitch ai = ((EntityAIWitch) citizen.getCitizenData().getJob(JobWitch.class).getWorkerAI());
            final AxisAlignedBB axisalignedbb = this.getBoundingBox().grow(SPLASH_SIZE, SPLASH_HEIGTH, SPLASH_SIZE);
            final List<LivingEntity> list = this.world.getEntitiesWithinAABB(LivingEntity.class, axisalignedbb);
            if (!list.isEmpty())
            {
                for (final LivingEntity livingentity : list)
                {
                    if (livingentity.canBeHitWithPotion())
                    {
                        final double distanceSq = this.getDistanceSq(livingentity);
                        if (distanceSq < MAX_DISTANCE)
                        {
                            double d1 = 1.0D - Math.sqrt(distanceSq) / 4.0D;
                            if (livingentity == entity)
                            {
                                d1 = 1.0D;
                            }
                            final boolean ally = EntityAIWitch.isAlly(citizen.getCitizenData().getColony(), livingentity);
                            final boolean foe = !ally && ai.isEntityValidTarget(livingentity);
                            if (ally || foe)
                            {
                                for (final EffectInstance effectinstance : effects)
                                {
                                    final Effect effect = effectinstance.getPotion();
                                    if ((ally && effect.isBeneficial()) || (foe && !effect.isBeneficial()))
                                    {
                                        if (effect.isInstant())
                                        {
                                            effect.affectEntity(this, this.getThrower(), livingentity, effectinstance.getAmplifier(), d1);
                                        }
                                        else
                                        {
                                            final int duration = (int) (d1 * (double) effectinstance.getDuration() + 0.5D);
                                            if (duration > MIN_DURATION)
                                            {
                                                livingentity.addPotionEffect(new EffectInstance(effect,
                                                  duration,
                                                  effectinstance.getAmplifier(),
                                                  effectinstance.isAmbient(),
                                                  effectinstance.doesShowParticles()));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public AbstractEntityCitizen getThrower()
    {
        return (AbstractEntityCitizen) super.getThrower();
    }
}
