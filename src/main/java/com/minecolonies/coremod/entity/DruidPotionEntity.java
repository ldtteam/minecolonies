package com.minecolonies.coremod.entity;

import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.colony.jobs.JobDruid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiPredicate;

public class DruidPotionEntity extends PotionEntity
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
    public static final int                         MIN_DURATION = 20;

    /**
     * The bi-predicate to check if an effect should be applied to an entity
     */
    @Nullable
    private BiPredicate<LivingEntity, Effect> predicate;

    public DruidPotionEntity(final EntityType<? extends PotionEntity> type, final World world)
    {
        super(type, world);
        this.predicate = null;
    }

    /**
     * Set the predicate of which entities to affect.
     * @param predicate if true applies to entity.
     */
    public void setPredicate(final @Nullable BiPredicate<LivingEntity, Effect> predicate)
    {
        this.predicate = predicate;
    }

    @Override
    public void applySplash(@NotNull List<EffectInstance> effects, @Nullable Entity entity)
    {
        final AbstractEntityCitizen citizen = this.getOwner();
        if (citizen != null && citizen.getCitizenData().getJob() instanceof JobDruid)
        {
            final AxisAlignedBB axisalignedbb = this.getBoundingBox().expandTowards(SPLASH_SIZE, SPLASH_HEIGTH, SPLASH_SIZE);
            final List<LivingEntity> list = this.level.getEntitiesOfClass(LivingEntity.class, axisalignedbb);
            if (!list.isEmpty())
            {
                for (final LivingEntity livingentity : list)
                {
                    if (livingentity.isAffectedByPotions())
                    {
                        final double distanceSq = this.distanceToSqr(livingentity);
                        if (distanceSq < MAX_DISTANCE)
                        {
                            double d1 = 1.0D - Math.sqrt(distanceSq) / 4.0D;
                            if (livingentity == entity)
                            {
                                d1 = 1.0D;
                            }
                            for (final EffectInstance effectinstance : effects)
                            {
                                final Effect effect = effectinstance.getEffect();
                                if (predicate == null || predicate.test(livingentity, effect))
                                {
                                    if (effect.isInstantenous())
                                    {
                                        effect.applyInstantenousEffect(this, this.getOwner(), livingentity, effectinstance.getAmplifier(), d1);
                                    }
                                    else
                                    {
                                        final int duration = (int) (d1 * (double) effectinstance.getDuration() + 0.5D);
                                        if (duration > MIN_DURATION)
                                        {
                                            livingentity.addEffect(new EffectInstance(effect,
                                              duration,
                                              effectinstance.getAmplifier(),
                                              effectinstance.isAmbient(),
                                              effectinstance.isVisible()));
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
    public AbstractEntityCitizen getOwner()
    {
        return (AbstractEntityCitizen) super.getOwner();
    }


    /**
     * Throws a potion at the target with the given inaccuracy
     *
     * @param potionStack the {@link ItemStack} of the Potion, {@link ItemStack#getItem()} must return a {@link net.minecraft.item.ThrowablePotionItem}
     * @param target the targeted {@link LivingEntity} to throw the potion at
     * @param thrower the witch throwing the potion
     * @param world the {@link World} of the thrower
     * @param velocity the velocity to throw the potion with
     * @param inaccuracy the inaccuracy to throw the potion with
     * @param predicate the bi-predicate to check if an effect should be applied to an entity
     */
    public static void throwPotionAt(final ItemStack potionStack, final LivingEntity target, final AbstractEntityCitizen thrower, final World world, final float velocity, final float inaccuracy, final BiPredicate<LivingEntity,Effect> predicate)
    {
        final DruidPotionEntity potionentity = new DruidPotionEntity(ModEntities.DRUID_POTION, world);
        potionentity.setOwner(thrower);
        potionentity.setPredicate(predicate);
        potionentity.setItem(potionStack);
        potionentity.shoot(target.blockPosition().getX(), target.blockPosition().getY(), target.blockPosition().getZ(), velocity, inaccuracy);
        world.addFreshEntity(potionentity);
    }
}
