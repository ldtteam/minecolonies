package com.minecolonies.core.entity.other;

import net.minecraft.world.entity.EntitySpawnReason;

import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.core.colony.jobs.guard.JobDruid;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiPredicate;

public class DruidPotionEntity extends ThrownSplashPotion
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
    private BiPredicate<LivingEntity, MobEffect> entitySelectionPredicate = null;

    /**
     * Create a new druid potion entity.
     * @param type entity type.
     * @param world world to spawn it in.
     */
    public DruidPotionEntity(final EntityType<? extends ThrownSplashPotion> type, final Level world)
    {
        super(type, world);
    }

    /**
     * Set the predicate of which entities to affect.
     * @param entitySelectionPredicate if true applies to entity.
     */
    public void setEntitySelectionPredicate(final @Nullable BiPredicate<LivingEntity, MobEffect> entitySelectionPredicate)
    {
        this.entitySelectionPredicate = entitySelectionPredicate;
    }

    @Override
    public void onHitAsPotion(final ServerLevel level, final ItemStack potionItem, final HitResult hitResult)
    {
        final AbstractEntityCitizen citizen = this.getOwner();
        if (citizen != null && citizen.getCitizenData() != null && citizen.getCitizenData().getJob() instanceof JobDruid)
        {
            final Entity directEntity = hitResult instanceof EntityHitResult entityHit ? entityHit.getEntity() : null;
            final Iterable<MobEffectInstance> effects = potionItem.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getAllEffects();
            final AABB potionAabb = this.getBoundingBox().move(hitResult.getLocation().subtract(this.position()));
            final AABB axisalignedbb = potionAabb.inflate(SPLASH_SIZE, SPLASH_HEIGTH, SPLASH_SIZE);
            final List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, axisalignedbb);
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
                            if (livingentity == directEntity)
                            {
                                d1 = 1.0D;
                            }
                            for (final MobEffectInstance effectinstance : effects)
                            {
                                final MobEffect effect = effectinstance.getEffect().value();
                                if (entitySelectionPredicate == null || entitySelectionPredicate.test(livingentity, effect))
                                {
                                    if (effect.isInstantaneous())
                                    {
                                        effect.applyInstantaneousEffect(level, this, this.getOwner(), livingentity, effectinstance.getAmplifier(), d1);
                                    }
                                    else
                                    {
                                        final int duration = (int) (d1 * (double) effectinstance.getDuration());
                                        livingentity.addEffect(new MobEffectInstance(effectinstance.getEffect(),
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

    /**
     * Why do you do this mojang. This should not be possible. Someone did something very messy on this server if the owner is not a citizen.
     * @return a citizen or null.
     */
    @Nullable
    @Override
    public AbstractEntityCitizen getOwner()
    {
        final Entity owner = super.getOwner();
        if (owner instanceof AbstractEntityCitizen)
        {
            return (AbstractEntityCitizen)owner;
        }
        return null;
    }
    
    /**
     * Throws a potion at the target with the given inaccuracy
     *
     * @param potionStack the {@link ItemStack} of the Potion, {@link ItemStack#getItem()} must return a Potion.
     * @param target the targeted {@link LivingEntity} to throw the potion at
     * @param thrower the witch throwing the potion
     * @param world the {@link Level} of the thrower
     * @param velocity the velocity to throw the potion with
     * @param inaccuracy the inaccuracy to throw the potion with
     * @param entitySelectionPredicate the bi-predicate to check if an effect should be applied to an entity
     */
    public static void throwPotionAt(final ItemStack potionStack, final LivingEntity target, final AbstractEntityCitizen thrower, final Level world, final float velocity, final float inaccuracy, final BiPredicate<LivingEntity,MobEffect> entitySelectionPredicate)
    {
        final DruidPotionEntity potionentity = (DruidPotionEntity) ModEntities.DRUID_POTION.create(world, EntitySpawnReason.EVENT);
        potionentity.setOwner(thrower);
        potionentity.setEntitySelectionPredicate(entitySelectionPredicate);
        potionentity.setItem(potionStack);
        potionentity.setPos(thrower.getX(), thrower.getY() + 1, thrower.getZ());

        thrower.level().playSound(null, thrower.getX(), thrower.getY(), thrower.getZ(), SoundEvents.WITCH_THROW, thrower.getSoundSource(), 1.0F, 0.8F + thrower.getRandom().nextFloat() * 0.4F);

        Vec3 movement = target.getDeltaMovement();


        double x = target.getX() + movement.x - thrower.getX();
        double y = target.getEyeY() - (double)1.1F - thrower.getY();
        double z = target.getZ() + movement.z - thrower.getZ();
        final double distance = Math.sqrt(x * x + z * z);

        potionentity.shoot(x, y + distance * 0.2, z, velocity, inaccuracy);
        world.addFreshEntity(potionentity);
    }
}
