package com.minecolonies.coremod.entity;

import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.colony.jobs.JobDruid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
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
    private BiPredicate<LivingEntity, Effect> entitySelectionPredicate = null;

    /**
     * Create a new druid potion entity.
     * @param type entity type.
     * @param world world to spawn it in.
     */
    public DruidPotionEntity(final EntityType<? extends PotionEntity> type, final World world)
    {
        super(type, world);
    }

    /**
     * Set the predicate of which entities to affect.
     * @param entitySelectionPredicate if true applies to entity.
     */
    public void setEntitySelectionPredicate(final @Nullable BiPredicate<LivingEntity, Effect> entitySelectionPredicate)
    {
        this.entitySelectionPredicate = entitySelectionPredicate;
    }

    @Override
    public void applySplash(@NotNull List<EffectInstance> effects, @Nullable Entity entity)
    {
        final AbstractEntityCitizen citizen = this.getOwner();
        if (citizen != null && citizen.getCitizenData() != null && citizen.getCitizenData().getJob() instanceof JobDruid)
        {
            final AxisAlignedBB axisalignedbb = this.getBoundingBox().inflate(SPLASH_SIZE, SPLASH_HEIGTH, SPLASH_SIZE);
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
                                if (entitySelectionPredicate == null || entitySelectionPredicate.test(livingentity, effect))
                                {
                                    if (effect.isInstantenous())
                                    {
                                        effect.applyInstantenousEffect(this, this.getOwner(), livingentity, effectinstance.getAmplifier(), d1);
                                    }
                                    else
                                    {
                                        final int duration = (int) (d1 * (double) effectinstance.getDuration());
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
     * @param potionStack the {@link ItemStack} of the Potion, {@link ItemStack#getItem()} must return a {@link net.minecraft.item.ThrowablePotionItem}
     * @param target the targeted {@link LivingEntity} to throw the potion at
     * @param thrower the witch throwing the potion
     * @param world the {@link World} of the thrower
     * @param velocity the velocity to throw the potion with
     * @param inaccuracy the inaccuracy to throw the potion with
     * @param entitySelectionPredicate the bi-predicate to check if an effect should be applied to an entity
     */
    public static void throwPotionAt(final ItemStack potionStack, final LivingEntity target, final AbstractEntityCitizen thrower, final World world, final float velocity, final float inaccuracy, final BiPredicate<LivingEntity,Effect> entitySelectionPredicate)
    {
        final DruidPotionEntity potionentity = (DruidPotionEntity) ModEntities.DRUID_POTION.create(world);
        potionentity.setOwner(thrower);
        potionentity.setEntitySelectionPredicate(entitySelectionPredicate);
        potionentity.setItem(potionStack);
        potionentity.setPos(thrower.getX(), thrower.getY() + 1, thrower.getZ());

        thrower.level.playSound(null, thrower.getX(), thrower.getY(), thrower.getZ(), SoundEvents.WITCH_THROW, thrower.getSoundSource(), 1.0F, 0.8F + thrower.getRandom().nextFloat() * 0.4F);

        Vector3d movement = target.getDeltaMovement();


        double x = target.getX() + movement.x - thrower.getX();
        double y = target.getEyeY() - (double)1.1F - thrower.getY();
        double z = target.getZ() + movement.z - thrower.getZ();
        final double distance = MathHelper.sqrt(x * x + z * z);

        potionentity.shoot(x, y + distance * 0.2, z, velocity, inaccuracy);
        world.addFreshEntity(potionentity);
    }

    @Override
    @NotNull
    public IPacket<?> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
