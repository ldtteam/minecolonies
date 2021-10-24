package com.minecolonies.api.entity;

import com.minecolonies.api.items.ModItems;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class SpearEntity extends AbstractArrowEntity
{
    private static final DataParameter<Byte> ID_LOYALTY = EntityDataManager.defineId(SpearEntity.class, DataSerializers.BYTE);

    public static final String    NBT_WEAPON       = "Weapon";
    public static final String    NBT_DEALT_DAMAGE = "DealtDamage";
    protected           ItemStack weapon           = new ItemStack(ModItems.spear);
    private             boolean   dealtDamage;
    public              int       returningTicks;

    public SpearEntity(EntityType<? extends AbstractArrowEntity> type, World world)
    {
        super(type, world);
    }

    public SpearEntity(World world, LivingEntity thrower, ItemStack thrownWeapon)
    {
        super(ModEntities.SPEAR, thrower, world);
        this.weapon = thrownWeapon.copy();
        this.entityData.set(ID_LOYALTY, (byte) EnchantmentHelper.getLoyalty(thrownWeapon));
    }

    @Override
    protected void defineSynchedData()
    {
        super.defineSynchedData();
        this.entityData.define(ID_LOYALTY, (byte) 0);
    }

    @Override
    public void tick()
    {
        if (this.inGroundTime > 4)
        {
            this.dealtDamage = true;
        }

        Entity entity = this.getOwner();
        if ((this.dealtDamage || this.isNoPhysics()) && entity != null)
        {
            int i = this.entityData.get(ID_LOYALTY);
            if (i > 0 && !this.shouldReturnToThrower())
            {
                if (!this.level.isClientSide && this.pickup == AbstractArrowEntity.PickupStatus.ALLOWED)
                {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }

                this.remove();
            }
            else if (i > 0)
            {
                this.setNoPhysics(true);
                Vector3d vec3d = new Vector3d(entity.getX() - this.getX(), entity.getEyeY() - this.getY(), entity.getZ() - this.getZ());
                this.setPosRaw(this.getX(), this.getY() + vec3d.y * 0.015D * (double) i, this.getZ());
                if (this.level.isClientSide)
                {
                    this.yOld = this.getY();
                }

                double d0 = 0.05D * (double) i;
                this.setDeltaMovement(this.getDeltaMovement().scale(0.95D).add(vec3d.normalize().scale(d0)));
                if (this.returningTicks == 0)
                {
                    this.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
                }

                ++this.returningTicks;
            }
        }

        super.tick();
    }

    protected boolean shouldReturnToThrower()
    {
        Entity entity = this.getOwner();
        if (entity != null && entity.isAlive())
        {
            return !(entity instanceof ServerPlayerEntity) || !entity.isSpectator();
        }
        else
        {
            return false;
        }
    }

    @NotNull
    @Override
    public ItemStack getPickupItem()
    {
        return this.weapon.copy();
    }

    @Override
    protected EntityRayTraceResult findHitEntity(@NotNull Vector3d startVec, @NotNull Vector3d endVec)
    {
        return this.dealtDamage ? null : super.findHitEntity(startVec, endVec);
    }

    protected void onHitEntity(EntityRayTraceResult result)
    {
        Entity entity = result.getEntity();
        float f = 8.0F;
        if (entity instanceof LivingEntity)
        {
            LivingEntity livingentity = (LivingEntity) entity;
            f += EnchantmentHelper.getDamageBonus(this.weapon, livingentity.getMobType());
        }

        Entity entity1 = this.getOwner();
        DamageSource damagesource = new IndirectEntityDamageSource("spear", this, entity1 == null ? this : entity1).setProjectile();
        this.dealtDamage = true;
        SoundEvent soundevent = SoundEvents.TRIDENT_HIT;
        if (entity.hurt(damagesource, f))
        {
            if (entity.getType() == EntityType.ENDERMAN)
            {
                return;
            }

            if (entity instanceof LivingEntity)
            {
                LivingEntity livingentity1 = (LivingEntity) entity;
                if (entity1 instanceof LivingEntity)
                {
                    EnchantmentHelper.doPostHurtEffects(livingentity1, entity1);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity) entity1, livingentity1);
                }

                this.doPostHurtEffects(livingentity1);
            }
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01D, -0.1D, -0.01D));
        float f1 = 1.0F;
        if (this.level instanceof ServerWorld && this.level.isThundering() && EnchantmentHelper.hasChanneling(this.weapon))
        {
            BlockPos blockpos = entity.blockPosition();
            if (this.level.canSeeSky(blockpos))
            {
                LightningBoltEntity lightningboltentity = EntityType.LIGHTNING_BOLT.create(level);
                if (lightningboltentity != null)
                {
                    lightningboltentity.moveTo(Vector3d.atBottomCenterOf(blockpos));
                    lightningboltentity.setVisualOnly(false);
                    lightningboltentity.setCause(entity1 instanceof ServerPlayerEntity ? (ServerPlayerEntity) entity1 : null);
                    this.level.addFreshEntity(lightningboltentity);
                    soundevent = SoundEvents.TRIDENT_THUNDER;
                    f1 = 5.0F;
                }
            }
        }

        this.playSound(soundevent, f1, 1.0F);
    }

    @NotNull
    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent()
    {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    public void playerTouch(@NotNull PlayerEntity entityIn)
    {
        Entity entity = this.getOwner();
        if (entity == null || entity.getUUID() == entityIn.getUUID())
        {
            super.playerTouch(entityIn);
        }
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundNBT nbt)
    {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains(NBT_WEAPON, 10))
        {
            this.weapon = ItemStack.of(nbt.getCompound(NBT_WEAPON));
        }

        this.dealtDamage = nbt.getBoolean(NBT_DEALT_DAMAGE);
        this.entityData.set(ID_LOYALTY, (byte) EnchantmentHelper.getLoyalty(this.weapon));
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundNBT nbt)
    {
        super.addAdditionalSaveData(nbt);
        nbt.put(NBT_WEAPON, this.weapon.save(new CompoundNBT()));
        nbt.putBoolean(NBT_DEALT_DAMAGE, this.dealtDamage);
    }

    @Override
    public void tickDespawn()
    {
        int i = this.entityData.get(ID_LOYALTY);
        if (this.pickup != AbstractArrowEntity.PickupStatus.ALLOWED || i <= 0)
        {
            super.tickDespawn();
        }
    }

    @Override
    protected float getWaterInertia()
    {
        return 0.99F;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean shouldRender(double x, double y, double z)
    {
        return true;
    }
}
