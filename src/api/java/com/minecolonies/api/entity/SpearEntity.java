package com.minecolonies.api.entity;

import com.minecolonies.api.items.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Custom arrow entity used for spear throwing, acts similar to the trident without any of the special effects.
 */
public class SpearEntity extends ThrownTrident implements ICustomAttackSound
{
    /**
     * Max time the arrow is alive before removing it, currently gets removed after 60 seconds.
     */
    private static final int MAX_TIME_ALIVE = 1200;

    /**
     * The spears base damage before any modifications.
     */
    public static final int BASE_DAMAGE = 8;

    /**
     * The NBT key for the spear ItemStack.
     */
    public static final String NBT_WEAPON       = "Weapon";
    /**
     * The NBT key for the spears dealt damage value.
     */
    public static final String NBT_DEALT_DAMAGE = "DealtDamage";

    /**
     * The weapon item stack, defaults to a generic ItemStack of the spear.
     */
    protected ItemStack weapon = new ItemStack(ModItems.spear);

    /**
     * The value of damage the spear has dealt.
     */
    private boolean dealtDamage;

    public SpearEntity(EntityType<? extends ThrownTrident> type, Level world)
    {
        super(type, world);
        getAddEntityPacket();
    }

    public SpearEntity(Level world, LivingEntity thrower, ItemStack thrownWeapon)
    {
        super(ModEntities.SPEAR, world);
        this.weapon = thrownWeapon.copy();
        this.setOwner(thrower);
        getAddEntityPacket();
    }

    @Override
    public Packet<?> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @NotNull
    @Override
    public ItemStack getPickupItem()
    {
        return this.weapon.copy();
    }

    @Nullable
    @Override
    protected EntityHitResult findHitEntity(final Vec3 startVec, final Vec3 endVec)
    {
        return this.dealtDamage ? null : super.findHitEntity(startVec, endVec);
    }

    protected void onHitEntity(EntityHitResult result)
    {
        Entity targetEntity = result.getEntity();
        float damageAmount = BASE_DAMAGE;
        if (targetEntity instanceof LivingEntity)
        {
            damageAmount += EnchantmentHelper.getDamageBonus(this.weapon, ((LivingEntity) targetEntity).getMobType());
        }

        Entity ownerEntity = this.getOwner();
        DamageSource damageSource = new IndirectEntityDamageSource("spear", this, ownerEntity == null ? this : ownerEntity).setProjectile();
        this.dealtDamage = true;
        if (targetEntity.hurt(damageSource, damageAmount))
        {
            if (targetEntity.getType() == EntityType.ENDERMAN)
            {
                return;
            }

            if (targetEntity instanceof LivingEntity)
            {
                LivingEntity livingEntity = (LivingEntity) targetEntity;
                if (ownerEntity instanceof LivingEntity)
                {
                    EnchantmentHelper.doPostHurtEffects(livingEntity, ownerEntity);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity) ownerEntity, livingEntity);
                }

                this.doPostHurtEffects(livingEntity);
            }
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01D, -0.1D, -0.01D));
        this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
    }

    @NotNull
    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent()
    {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag nbt)
    {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains(NBT_WEAPON, 10))
        {
            this.weapon = ItemStack.of(nbt.getCompound(NBT_WEAPON));
        }

        this.dealtDamage = nbt.getBoolean(NBT_DEALT_DAMAGE);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag nbt)
    {
        super.addAdditionalSaveData(nbt);
        nbt.put(NBT_WEAPON, this.weapon.save(new CompoundTag()));
        nbt.putBoolean(NBT_DEALT_DAMAGE, this.dealtDamage);
    }

    /**
     * Remove spear after certain time alive.
     */
    @Override
    public void tick()
    {
        super.tick();

        if (tickCount > MAX_TIME_ALIVE)
        {
            remove(RemovalReason.DISCARDED);
        }
    }

    @Override
    public void tickDespawn()
    {
        if (this.pickup != AbstractArrow.Pickup.ALLOWED)
        {
            super.tickDespawn();
        }
    }

    @Override
    protected float getWaterInertia()
    {
        return 0.9F;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean shouldRender(double x, double y, double z)
    {
        return true;
    }


    @Override
    public SoundEvent getAttackSound()
    {
        return SoundEvents.TRIDENT_THROW;
    }
}
