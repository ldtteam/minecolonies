package com.minecolonies.core.entity.other;

import com.minecolonies.api.loot.ModLootTables;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class NewBobberEntity extends Projectile implements IEntityWithComplexSpawn
{
    private static final EntityDataAccessor<Integer> DATA_HOOKED_ENTITY = SynchedEntityData.defineId(NewBobberEntity.class, EntityDataSerializers.INT);
    public  static final int                    XP_PER_CATCH       = 2;
    private              boolean                inGround;
    private              int                    ticksInGround;
    private              EntityCitizen          angler;
    private              int                    tickRemove         = 100;
    private              int                    ticksInAir;
    private              int                    ticksCatchable;
    private              int                    ticksCaughtDelay;
    private              int                    ticksCatchableDelay;
    private              float                  fishApproachAngle;
    public               Entity                 caughtEntity;
    private              NewBobberEntity.State  currentState       = NewBobberEntity.State.FLYING;
    private              int                    luck;
    private              int                    lureSpeed;
    private              int                    anglerId           = -1;
    private              boolean                readyToCatch       = false;

    /**
     * Saves the bobber position upon catch
     */
    private Vec3 onWaterPos;

    /**
     * Serverside constructor.
     * @param type entity type.
     * @param world world to spawn it in.
     */
    public NewBobberEntity(final EntityType<? extends Projectile> type, final Level world)
    {
        super(type, world);
        this.noCulling = true;
    }

    /**
     * Set the current angler.
     *
     * @param citizen   the citizen to set.
     * @param luck      the luck param.
     * @param lureSpeed the lure speed param.
     */
    public void setAngler(final EntityCitizen citizen, final int luck, final int lureSpeed)
    {
        this.angler = citizen;
        final float pitch = (float) (Math.random()*40.0-10.0);
        final float yaw = this.angler.getYRot();
        final float cowYaw = Mth.cos(-yaw * ((float) Math.PI / 180F) - (float) Math.PI);
        final float sinYaw = Mth.sin(-yaw * ((float) Math.PI / 180F) - (float) Math.PI);
        final float cosPitch = -Mth.cos(-pitch * ((float) Math.PI / 180F));
        final float sinPitch = Mth.sin(-pitch * ((float) Math.PI / 180F));
        final double xYaw = this.angler.getX() - (double) sinYaw * 0.3D;
        final double eyePos = this.angler.getEyeY();
        final double zYaw = this.angler.getZ() - (double) cowYaw * 0.3D;
        this.moveTo(xYaw, eyePos, zYaw, yaw, pitch);
        Vec3 vec = new Vec3( (-sinYaw),  Mth.clamp(-(sinPitch / cosPitch), -5.0F, 5.0F), (-cowYaw));
        final double d3 = vec.length();
        vec = vec.multiply(0.6D / d3 + 0.5D + this.random.nextGaussian() * 0.0045D,
          0.6D / d3 + 0.5D + this.random.nextGaussian() * 0.0045D,
          0.6D / d3 + 0.5D + this.random.nextGaussian() * 0.0045D);
        this.setDeltaMovement(vec);
        this.setYRot((float) (Mth.atan2(vec.x, vec.z) * (double) (180F / (float) Math.PI)));
        this.setXRot((float) (Mth.atan2(vec.y, Mth.sqrt((float) vec.horizontalDistanceSqr())) * (double) (180F / (float) Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
        this.luck = Math.max(0, luck);
        this.lureSpeed = Math.max(0, lureSpeed);
    }

    protected void defineSynchedData()
    {
        this.getEntityData().define(DATA_HOOKED_ENTITY, 0);
    }

    public void onSyncedDataUpdated(final EntityDataAccessor<?> key)
    {
        if (DATA_HOOKED_ENTITY.equals(key))
        {
            final int i = this.getEntityData().get(DATA_HOOKED_ENTITY);
            this.caughtEntity = i > 0 ? this.level().getEntity(i - 1) : null;
        }

        super.onSyncedDataUpdated(key);
    }

    /**
     * Checks if the entity is in range to render.
     */
    @OnlyIn(Dist.CLIENT)
    public boolean shouldRenderAtSqrDistance(final double distance)
    {
        return distance < 4096.0D;
    }

    @Override
    protected void readAdditionalSaveData(@NotNull final CompoundTag compound)
    {

    }

    @Override
    protected void addAdditionalSaveData(final CompoundTag compound)
    {

    }

    /**
     * Sets a target for the client to interpolate towards over the next few ticks
     */
    @OnlyIn(Dist.CLIENT)
    public void lerpTo(
      final double x,
      final double y,
      final double z,
      final float yaw,
      final float pitch,
      final int posRotationIncrements,
      final boolean teleport)
    {
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void tick()
    {

        super.tick();
        if (!this.level().isClientSide())
        {
            if (--this.tickRemove <= 0)
            {
                this.remove(RemovalReason.DISCARDED);
                return;
            }
        }

        if (this.angler == null)
        {
            if (level().isClientSide)
            {
                if (anglerId > -1)
                {
                    angler = (EntityCitizen) level().getEntity(anglerId);
                }
            }
            else
            {
                this.remove(RemovalReason.DISCARDED);
            }
        }
        else if (this.level().isClientSide || !this.shouldStopFishing())
        {

            if (this.inGround)
            {
                ++this.ticksInGround;
                if (this.ticksInGround >= 1200)
                {
                    this.remove(RemovalReason.DISCARDED);
                    return;
                }
            }

            float f = 0.0F;
            final BlockPos blockpos = this.blockPosition();
            final FluidState ifluidstate = this.level().getFluidState(blockpos);
            if (ifluidstate.is(FluidTags.WATER))
            {
                f = ifluidstate.getHeight(this.level(), blockpos);
            }

            if (this.currentState == NewBobberEntity.State.FLYING)
            {
                if (this.caughtEntity != null)
                {
                    this.setDeltaMovement(Vec3.ZERO);
                    this.currentState = NewBobberEntity.State.HOOKED_IN_ENTITY;
                    return;
                }

                if (f > 0.0F)
                {
                    this.setDeltaMovement(this.getDeltaMovement().multiply(0.3D, 0.2D, 0.3D));
                    this.currentState = NewBobberEntity.State.BOBBING;
                    return;
                }

                if (!this.level().isClientSide)
                {
                    this.checkCollision();
                }

                if (!this.inGround && !this.onGround() && !this.horizontalCollision)
                {
                    ++this.ticksInAir;
                }
                else
                {
                    this.ticksInAir = 0;
                    this.setDeltaMovement(Vec3.ZERO);
                }
            }
            else
            {
                if (this.currentState == NewBobberEntity.State.HOOKED_IN_ENTITY)
                {
                    if (this.caughtEntity != null)
                    {
                        if (this.caughtEntity.isRemoved())
                        {
                            this.caughtEntity = null;
                            this.currentState = NewBobberEntity.State.FLYING;
                        }
                        else
                        {
                            this.setPos(this.caughtEntity.getX(), this.caughtEntity.getY(0.8D), this.caughtEntity.getZ());
                        }
                    }

                    return;
                }

                if (this.currentState == NewBobberEntity.State.BOBBING)
                {
                    final Vec3 Vector3d = this.getDeltaMovement();
                    double d0 = this.getY() + Vector3d.y - (double) blockpos.getY() - (double) f;
                    if (Math.abs(d0) < 0.01D)
                    {
                        d0 += Math.signum(d0) * 0.1D;
                    }

                    this.setDeltaMovement(Vector3d.x * 0.9D, Vector3d.y - d0 * (double) this.random.nextFloat() * 0.2D, Vector3d.z * 0.9D);
                    if (!this.level().isClientSide && f > 0.0F)
                    {
                        this.catchingFish(blockpos);
                    }
                }
            }

            if (!ifluidstate.is(FluidTags.WATER))
            {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.03D, 0.0D));
            }

            this.move(MoverType.SELF, this.getDeltaMovement());
            this.updateRotation();
            this.setDeltaMovement(this.getDeltaMovement().scale(0.92D));
            this.reapplyPosition();
        }
    }

    public boolean shouldStopFishing()
    {
        final ItemStack itemstack = this.angler.getMainHandItem();
        final ItemStack itemstack1 = this.angler.getOffhandItem();
        final boolean flag = itemstack.canPerformAction(ItemAbilities.FISHING_ROD_CAST);
        final boolean flag1 = itemstack1.canPerformAction(ItemAbilities.FISHING_ROD_CAST);
        if (!this.angler.isRemoved() && this.angler.isAlive() && (flag || flag1) && !(this.distanceToSqr(this.angler) > 1024.0D))
        {
            return false;
        }
        else
        {
            this.remove(RemovalReason.DISCARDED);
            return true;
        }
    }

    @Override
    protected void updateRotation()
    {
        final Vec3 vec = this.getDeltaMovement();
        final float f = Mth.sqrt((float) vec.horizontalDistanceSqr());
        this.setYRot((float) (Mth.atan2(vec.x, vec.z) * (double) (180F / (float) Math.PI)));

        for (this.setXRot((float) (Mth.atan2(vec.y, (double) f) * (double) (180F / (float) Math.PI)));
          this.getXRot() - this.xRotO < -180.0F;
          this.xRotO -= 360.0F)
        {
            ;
        }

        while (this.getXRot() - this.xRotO >= 180.0F)
        {
            this.xRotO += 360.0F;
        }

        while (this.getYRot() - this.yRotO < -180.0F)
        {
            this.yRotO -= 360.0F;
        }

        while (this.getYRot() - this.yRotO >= 180.0F)
        {
            this.yRotO += 360.0F;
        }

        this.setXRot(Mth.lerp(0.2F, this.xRotO, this.getXRot()));
        this.setYRot(Mth.lerp(0.2F, this.yRotO, this.getYRot()));
    }

    private void checkCollision()
    {
        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitresult.getType() == HitResult.Type.MISS || !net.neoforged.neoforge.event.EventHooks.onProjectileImpact(this, hitresult)) this.onHit(hitresult);
    }

    private void setHookedEntity()
    {
        this.getEntityData().set(DATA_HOOKED_ENTITY, this.caughtEntity.getId() + 1);
    }

    private void catchingFish(final BlockPos p_190621_1_)
    {
        final ServerLevel serverworld = (ServerLevel) this.level();
        int i = 1;
        final BlockPos blockpos = p_190621_1_.above();
        if (this.random.nextFloat() < 0.25F && this.level().isRainingAt(blockpos))
        {
            ++i;
        }

        if (this.random.nextFloat() < 0.5F && !this.level().canSeeSky(blockpos))
        {
            --i;
        }

        if (this.ticksCatchable > 0)
        {
            --this.ticksCatchable;
            if (this.ticksCatchable <= 0)
            {
                this.ticksCaughtDelay = 0;
                this.ticksCatchableDelay = 0;
            }
            else
            {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.2D * (double) this.random.nextFloat() * (double) this.random.nextFloat(), 0.0D));
            }
        }
        else if (this.ticksCatchableDelay > 0)
        {
            this.ticksCatchableDelay -= i;
            if (this.ticksCatchableDelay > 0)
            {
                this.fishApproachAngle = (float) ((double) this.fishApproachAngle + this.random.nextGaussian() * 4.0D);
                final float f = this.fishApproachAngle * ((float) Math.PI / 180F);
                final float f1 = Mth.sin(f);
                final float f2 = Mth.cos(f);
                final double d0 = this.getX() + (double) (f1 * (float) this.ticksCatchableDelay * 0.1F);
                final double d1 = (double) ((float) Mth.floor(this.getY()) + 1.0F);
                final double d2 = this.getZ() + (double) (f2 * (float) this.ticksCatchableDelay * 0.1F);
                if (serverworld.getBlockState(new BlockPos((int) d0, (int) d1 - 1, (int) d2)).is(Blocks.WATER))
                {
                    if (this.random.nextFloat() < 0.15F)
                    {
                        serverworld.sendParticles(ParticleTypes.BUBBLE, d0, d1 - (double) 0.1F, d2, 1, (double) f1, 0.1D, (double) f2, 0.0D);
                    }

                    final float f3 = f1 * 0.04F;
                    final float f4 = f2 * 0.04F;
                    serverworld.sendParticles(ParticleTypes.FISHING, d0, d1, d2, 0, (double) f4, 0.01D, (double) (-f3), 1.0D);
                    serverworld.sendParticles(ParticleTypes.FISHING, d0, d1, d2, 0, (double) (-f4), 0.01D, (double) f3, 1.0D);
                }
            }
            else
            {
                readyToCatch = true;
                onWaterPos = new Vec3(getX(), getY(), getZ());

                final Vec3 Vector3d = this.getDeltaMovement();
                this.setDeltaMovement(Vector3d.x, (double) (-0.4F * Mth.nextFloat(this.random, 0.6F, 1.0F)), Vector3d.z);
                this.playSound(SoundEvents.FISHING_BOBBER_SPLASH, 0.25F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
                final double d3 = this.getY() + 0.5D;
                serverworld.sendParticles(ParticleTypes.BUBBLE,
                  this.getX(),
                  d3,
                  this.getZ(),
                  (int) (1.0F + this.getBbWidth() * 20.0F),
                  (double) this.getBbWidth(),
                  0.0D,
                  (double) this.getBbWidth(),
                  (double) 0.2F);
                serverworld.sendParticles(ParticleTypes.FISHING,
                  this.getX(),
                  d3,
                  this.getZ(),
                  (int) (1.0F + this.getBbWidth() * 20.0F),
                  (double) this.getBbWidth(),
                  0.0D,
                  (double) this.getBbWidth(),
                  (double) 0.2F);
                this.ticksCatchable = Mth.nextInt(this.random, 20, 40);
            }
        }
        else if (this.ticksCaughtDelay > 0)
        {
            this.ticksCaughtDelay -= i;
            float f5 = 0.15F;
            if (this.ticksCaughtDelay < 20)
            {
                f5 = (float) ((double) f5 + (double) (20 - this.ticksCaughtDelay) * 0.05D);
            }
            else if (this.ticksCaughtDelay < 40)
            {
                f5 = (float) ((double) f5 + (double) (40 - this.ticksCaughtDelay) * 0.02D);
            }
            else if (this.ticksCaughtDelay < 60)
            {
                f5 = (float) ((double) f5 + (double) (60 - this.ticksCaughtDelay) * 0.01D);
            }

            if (this.random.nextFloat() < f5)
            {
                final float f6 = Mth.nextFloat(this.random, 0.0F, 360.0F) * ((float) Math.PI / 180F);
                final float f7 = Mth.nextFloat(this.random, 25.0F, 60.0F);
                final double d4 = this.getX() + (double) (Mth.sin(f6) * f7 * 0.1F);
                final double d5 = (double) ((float) Mth.floor(this.getY()) + 1.0F);
                final double d6 = this.getZ() + (double) (Mth.cos(f6) * f7 * 0.1F);
                if (serverworld.getBlockState(BlockPos.containing(d4, d5 - 1.0D, d6)).is(Blocks.WATER))
                {
                    serverworld.sendParticles(ParticleTypes.SPLASH, d4, d5, d6, 2 + this.random.nextInt(2), (double) 0.1F, 0.0D, (double) 0.1F, 0.0D);
                }
            }

            if (this.ticksCaughtDelay <= 0)
            {
                this.fishApproachAngle = Mth.nextFloat(this.random, 0.0F, 360.0F);
                this.ticksCatchableDelay = Mth.nextInt(this.random, 20, 80);
            }
        }
        else
        {
            this.ticksCaughtDelay = Mth.nextInt(this.random, 1060, 1300);
            this.ticksCaughtDelay -= this.lureSpeed * 20 * 5;
            this.ticksCaughtDelay = Math.max(5, ticksCaughtDelay);
        }
    }

    public int getDamage()
    {
        if (!this.level().isClientSide && this.angler != null)
        {
            int i = 0;
            final net.neoforged.neoforge.event.entity.player.ItemFishedEvent event = null;
            if (this.caughtEntity != null)
            {
                this.bringInHookedEntity();
                this.level().broadcastEntityEvent(this, (byte) 31);
                i = this.caughtEntity instanceof ItemEntity ? 3 : 5;
            }
            else if (this.ticksCatchable > 0)
            {
                LootParams.Builder lootcontext$builder = (new LootParams.Builder((ServerLevel)this.level()))
                                                            .withParameter(LootContextParams.ORIGIN, this.position())
                                                            .withParameter(LootContextParams.TOOL, this.getAngler().getMainHandItem())
                                                            .withParameter(LootContextParams.THIS_ENTITY, this)
                                                            .withLuck((float)this.luck);

                lootcontext$builder.withParameter(LootContextParams.KILLER_ENTITY, this.angler).withParameter(LootContextParams.THIS_ENTITY, this);
                final LootTable loottable = this.level().getServer().getLootData().getLootTable(ModLootTables.FISHING);
                final List<ItemStack> list = loottable.getRandomItems(lootcontext$builder.create(LootContextParamSets.FISHING));

                for (final ItemStack itemstack : list)
                {
                    final ItemEntity itementity = new ItemEntity(this.level(), onWaterPos.x, onWaterPos.y, onWaterPos.z, itemstack);
                    final double d0 = this.angler.getX() - onWaterPos.x;
                    final double d1 = (this.angler.getY() + 0.5D) - onWaterPos.y;
                    final double d2 = this.angler.getZ() - onWaterPos.z;
                    itementity.noPhysics = true;
                    itementity.setDeltaMovement(d0 * 0.1D, d1 * 0.1D + Math.sqrt(Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2)) * 0.08D, d2 * 0.1D);
                    this.level().addFreshEntity(itementity);
                    this.angler.level().addFreshEntity(new ExperienceOrb(this.angler.level(),
                      this.angler.getX(),
                      this.angler.getY() + 0.5D,
                      this.angler.getZ() + 0.5D,
                      XP_PER_CATCH));
                }

                i = 1;
            }

            if (this.inGround)
            {
                i = 2;
            }

            this.remove(RemovalReason.DISCARDED);
            return event == null ? i : event.getRodDamage();
        }
        else
        {
            return 0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void handleEntityEvent(final byte id)
    {
        if (id == 31 && this.level().isClientSide && this.caughtEntity instanceof EntityCitizen)
        {
            this.bringInHookedEntity();
        }

        super.handleEntityEvent(id);
    }

    protected void bringInHookedEntity()
    {
        if (this.angler != null)
        {
            final Vec3 Vector3d = (new Vec3(this.angler.getX() - this.getX(), this.angler.getY() - this.getY(), this.angler.getZ() - this.getZ())).scale(0.1D);
            this.caughtEntity.setDeltaMovement(this.caughtEntity.getDeltaMovement().add(Vector3d));
        }
    }

    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to prevent them from trampling crops
     */
    protected boolean isMovementNoisy()
    {
        return false;
    }

    @Nullable
    public EntityCitizen getAngler()
    {
        return this.angler;
    }

    /**
     * Returns false if this Entity is a boss, true otherwise.
     */
    public boolean canChangeDimensions()
    {
        return false;
    }

    @Override
    public void writeSpawnData(final RegistryFriendlyByteBuf buffer)
    {
        if (angler != null)
        {
            buffer.writeInt(angler.getId());
        }
        else
        {
            buffer.writeInt(-1);
        }
    }

    @Override
    public void readSpawnData(final FriendlyByteBuf additionalData)
    {
        final int citizenId = additionalData.readInt();
        if (citizenId != -1)
        {
            anglerId = citizenId;
        }
    }

    public boolean isReadyToCatch()
    {
        return readyToCatch;
    }

    /**
     * Sets tickRemove to 100 ticks to prevent bobber from staying in the water when Fisherman is not fishing.
     */
    public void setInUse()
    {
        this.tickRemove = 100;
    }

    enum State
    {
        FLYING,
        HOOKED_IN_ENTITY,
        BOBBING;
    }
}
