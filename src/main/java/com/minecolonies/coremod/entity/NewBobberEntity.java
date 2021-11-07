package com.minecolonies.coremod.entity;

import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.loot.ModLootTables;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class NewBobberEntity extends Entity implements IEntityAdditionalSpawnData
{
    private static final DataParameter<Integer> DATA_HOOKED_ENTITY = EntityDataManager.defineId(NewBobberEntity.class, DataSerializers.INT);
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
    private Vector3d onWaterPos;

    public NewBobberEntity(final EntityType<?> type, final World world)
    {
        super(type, world);
        this.noCulling = true;
    }

    public NewBobberEntity(final FMLPlayMessages.SpawnEntity spawnEntity, final World world)
    {
        this(ModEntities.FISHHOOK, world);
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
        final float yaw = this.angler.yRot;
        final float cowYaw = MathHelper.cos(-yaw * ((float) Math.PI / 180F) - (float) Math.PI);
        final float sinYaw = MathHelper.sin(-yaw * ((float) Math.PI / 180F) - (float) Math.PI);
        final float cosPitch = -MathHelper.cos(-pitch * ((float) Math.PI / 180F));
        final float sinPitch = MathHelper.sin(-pitch * ((float) Math.PI / 180F));
        final double xYaw = this.angler.getX() - (double) sinYaw * 0.3D;
        final double eyePos = this.angler.getEyeY();
        final double zYaw = this.angler.getZ() - (double) cowYaw * 0.3D;
        this.moveTo(xYaw, eyePos, zYaw, yaw, pitch);
        Vector3d vec = new Vector3d( (-sinYaw),  MathHelper.clamp(-(sinPitch / cosPitch), -5.0F, 5.0F), (-cowYaw));
        final double d3 = vec.length();
        vec = vec.multiply(0.6D / d3 + 0.5D + this.random.nextGaussian() * 0.0045D,
          0.6D / d3 + 0.5D + this.random.nextGaussian() * 0.0045D,
          0.6D / d3 + 0.5D + this.random.nextGaussian() * 0.0045D);
        this.setDeltaMovement(vec);
        this.yRot = (float) (MathHelper.atan2(vec.x, vec.z) * (double) (180F / (float) Math.PI));
        this.xRot = (float) (MathHelper.atan2(vec.y, (double) MathHelper.sqrt(getHorizontalDistanceSqr(vec))) * (double) (180F / (float) Math.PI));
        this.yRotO = this.yRot;
        this.xRotO = this.xRot;
        this.luck = Math.max(0, luck);
        this.lureSpeed = Math.max(0, lureSpeed);
    }

    protected void defineSynchedData()
    {
        this.getEntityData().define(DATA_HOOKED_ENTITY, 0);
    }

    public void onSyncedDataUpdated(final DataParameter<?> key)
    {
        if (DATA_HOOKED_ENTITY.equals(key))
        {
            final int i = this.getEntityData().get(DATA_HOOKED_ENTITY);
            this.caughtEntity = i > 0 ? this.level.getEntity(i - 1) : null;
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
    protected void readAdditionalSaveData(@NotNull final CompoundNBT compound)
    {

    }

    @Override
    protected void addAdditionalSaveData(final CompoundNBT compound)
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
        if (!this.level.isClientSide())
        {
            if (--this.tickRemove <= 0)
            {
                this.remove();
                return;
            }
        }

        if (this.angler == null)
        {
            if (level.isClientSide)
            {
                if (anglerId > -1)
                {
                    angler = (EntityCitizen) level.getEntity(anglerId);
                }
            }
            else
            {
                this.remove();
            }
        }
        else if (this.level.isClientSide || !this.shouldStopFishing())
        {

            if (this.inGround)
            {
                ++this.ticksInGround;
                if (this.ticksInGround >= 1200)
                {
                    this.remove();
                    return;
                }
            }

            float f = 0.0F;
            final BlockPos blockpos = new BlockPos(this.position());
            final FluidState ifluidstate = this.level.getFluidState(blockpos);
            if (ifluidstate.is(FluidTags.WATER))
            {
                f = ifluidstate.getHeight(this.level, blockpos);
            }

            if (this.currentState == NewBobberEntity.State.FLYING)
            {
                if (this.caughtEntity != null)
                {
                    this.setDeltaMovement(Vector3d.ZERO);
                    this.currentState = NewBobberEntity.State.HOOKED_IN_ENTITY;
                    return;
                }

                if (f > 0.0F)
                {
                    this.setDeltaMovement(this.getDeltaMovement().multiply(0.3D, 0.2D, 0.3D));
                    this.currentState = NewBobberEntity.State.BOBBING;
                    return;
                }

                if (!this.level.isClientSide)
                {
                    this.checkCollision();
                }

                if (!this.inGround && !this.onGround && !this.horizontalCollision)
                {
                    ++this.ticksInAir;
                }
                else
                {
                    this.ticksInAir = 0;
                    this.setDeltaMovement(Vector3d.ZERO);
                }
            }
            else
            {
                if (this.currentState == NewBobberEntity.State.HOOKED_IN_ENTITY)
                {
                    if (this.caughtEntity != null)
                    {
                        if (this.caughtEntity.removed)
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
                    final Vector3d Vector3d = this.getDeltaMovement();
                    double d0 = this.getY() + Vector3d.y - (double) blockpos.getY() - (double) f;
                    if (Math.abs(d0) < 0.01D)
                    {
                        d0 += Math.signum(d0) * 0.1D;
                    }

                    this.setDeltaMovement(Vector3d.x * 0.9D, Vector3d.y - d0 * (double) this.random.nextFloat() * 0.2D, Vector3d.z * 0.9D);
                    if (!this.level.isClientSide && f > 0.0F)
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
        final boolean flag = itemstack.getItem() instanceof net.minecraft.item.FishingRodItem;
        final boolean flag1 = itemstack1.getItem() instanceof net.minecraft.item.FishingRodItem;
        if (!this.angler.removed && this.angler.isAlive() && (flag || flag1) && !(this.distanceToSqr(this.angler) > 1024.0D))
        {
            return false;
        }
        else
        {
            this.remove();
            return true;
        }
    }

    private void updateRotation()
    {
        final Vector3d Vector3d = this.getDeltaMovement();
        final float f = MathHelper.sqrt(getHorizontalDistanceSqr(Vector3d));
        this.yRot = (float) (MathHelper.atan2(Vector3d.x, Vector3d.z) * (double) (180F / (float) Math.PI));

        for (this.xRot = (float) (MathHelper.atan2(Vector3d.y, (double) f) * (double) (180F / (float) Math.PI));
          this.xRot - this.xRotO < -180.0F;
          this.xRotO -= 360.0F)
        {
            ;
        }

        while (this.xRot - this.xRotO >= 180.0F)
        {
            this.xRotO += 360.0F;
        }

        while (this.yRot - this.yRotO < -180.0F)
        {
            this.yRotO -= 360.0F;
        }

        while (this.yRot - this.yRotO >= 180.0F)
        {
            this.yRotO += 360.0F;
        }

        this.xRot = MathHelper.lerp(0.2F, this.xRotO, this.xRot);
        this.yRot = MathHelper.lerp(0.2F, this.yRotO, this.yRot);
    }

    private void checkCollision()
    {
        final RayTraceResult raytraceresult = ProjectileHelper.getHitResult(this,
          (entity) -> !entity.isSpectator() && (entity.isPickable() || entity instanceof ItemEntity) && (entity != this.angler || this.ticksInAir >= 5));
        if (raytraceresult.getType() != RayTraceResult.Type.MISS)
        {
            if (raytraceresult.getType() == RayTraceResult.Type.ENTITY)
            {
                this.caughtEntity = ((EntityRayTraceResult) raytraceresult).getEntity();
                this.setHookedEntity();
            }
            else
            {
                this.inGround = true;
            }
        }
    }

    private void setHookedEntity()
    {
        this.getEntityData().set(DATA_HOOKED_ENTITY, this.caughtEntity.getId() + 1);
    }

    private void catchingFish(final BlockPos p_190621_1_)
    {
        final ServerWorld serverworld = (ServerWorld) this.level;
        int i = 1;
        final BlockPos blockpos = p_190621_1_.above();
        if (this.random.nextFloat() < 0.25F && this.level.isRainingAt(blockpos))
        {
            ++i;
        }

        if (this.random.nextFloat() < 0.5F && !this.level.canSeeSky(blockpos))
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
                final float f1 = MathHelper.sin(f);
                final float f2 = MathHelper.cos(f);
                final double d0 = this.getX() + (double) (f1 * (float) this.ticksCatchableDelay * 0.1F);
                final double d1 = (double) ((float) MathHelper.floor(this.getY()) + 1.0F);
                final double d2 = this.getZ() + (double) (f2 * (float) this.ticksCatchableDelay * 0.1F);
                if (serverworld.getBlockState(new BlockPos((int) d0, (int) d1 - 1, (int) d2)).getMaterial() == net.minecraft.block.material.Material.WATER)
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
                onWaterPos = new Vector3d(getX(), getY(), getZ());

                final Vector3d Vector3d = this.getDeltaMovement();
                this.setDeltaMovement(Vector3d.x, (double) (-0.4F * MathHelper.nextFloat(this.random, 0.6F, 1.0F)), Vector3d.z);
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
                this.ticksCatchable = MathHelper.nextInt(this.random, 20, 40);
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
                final float f6 = MathHelper.nextFloat(this.random, 0.0F, 360.0F) * ((float) Math.PI / 180F);
                final float f7 = MathHelper.nextFloat(this.random, 25.0F, 60.0F);
                final double d4 = this.getX() + (double) (MathHelper.sin(f6) * f7 * 0.1F);
                final double d5 = (double) ((float) MathHelper.floor(this.getY()) + 1.0F);
                final double d6 = this.getZ() + (double) (MathHelper.cos(f6) * f7 * 0.1F);
                if (serverworld.getBlockState(new BlockPos(d4, d5 - 1.0D, d6)).getMaterial() == net.minecraft.block.material.Material.WATER)
                {
                    serverworld.sendParticles(ParticleTypes.SPLASH, d4, d5, d6, 2 + this.random.nextInt(2), (double) 0.1F, 0.0D, (double) 0.1F, 0.0D);
                }
            }

            if (this.ticksCaughtDelay <= 0)
            {
                this.fishApproachAngle = MathHelper.nextFloat(this.random, 0.0F, 360.0F);
                this.ticksCatchableDelay = MathHelper.nextInt(this.random, 20, 80);
            }
        }
        else
        {
            this.ticksCaughtDelay = MathHelper.nextInt(this.random, 1060, 1300);
            this.ticksCaughtDelay -= this.lureSpeed * 20 * 5;
            this.ticksCaughtDelay = Math.max(5, ticksCaughtDelay);
        }
    }

    public int getDamage()
    {
        if (!this.level.isClientSide && this.angler != null)
        {
            int i = 0;
            final net.minecraftforge.event.entity.player.ItemFishedEvent event = null;
            if (this.caughtEntity != null)
            {
                this.bringInHookedEntity();
                this.level.broadcastEntityEvent(this, (byte) 31);
                i = this.caughtEntity instanceof ItemEntity ? 3 : 5;
            }
            else if (this.ticksCatchable > 0)
            {
                LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerWorld)this.level))
                                                            .withParameter(LootParameters.ORIGIN, this.position())
                                                            .withParameter(LootParameters.TOOL, this.getAngler().getMainHandItem())
                                                            .withParameter(LootParameters.THIS_ENTITY, this)
                                                            .withRandom(this.random)
                                                            .withLuck((float)this.luck);

                lootcontext$builder.withParameter(LootParameters.KILLER_ENTITY, this.angler).withParameter(LootParameters.THIS_ENTITY, this);
                final LootTable loottable = this.level.getServer().getLootTables().get(ModLootTables.FISHING);
                final List<ItemStack> list = loottable.getRandomItems(lootcontext$builder.create(LootParameterSets.FISHING));

                for (final ItemStack itemstack : list)
                {
                    final ItemEntity itementity = new ItemEntity(this.level, onWaterPos.x, onWaterPos.y, onWaterPos.z, itemstack);
                    final double d0 = this.angler.getX() - onWaterPos.x;
                    final double d1 = (this.angler.getY() + 0.5D) - onWaterPos.y;
                    final double d2 = this.angler.getZ() - onWaterPos.z;
                    itementity.noPhysics = true;
                    itementity.setDeltaMovement(d0 * 0.1D, d1 * 0.1D + Math.sqrt(Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2)) * 0.08D, d2 * 0.1D);
                    this.level.addFreshEntity(itementity);
                    this.angler.level.addFreshEntity(new ExperienceOrbEntity(this.angler.level,
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

            this.remove();
            return event == null ? i : event.getRodDamage();
        }
        else
        {
            return 0;
        }
    }

    /**
     * Handler for {@link World#broadcastEntityEvent}
     */
    @OnlyIn(Dist.CLIENT)
    public void handleEntityEvent(final byte id)
    {
        if (id == 31 && this.level.isClientSide && this.caughtEntity instanceof EntityCitizen)
        {
            this.bringInHookedEntity();
        }

        super.handleEntityEvent(id);
    }

    protected void bringInHookedEntity()
    {
        if (this.angler != null)
        {
            final Vector3d Vector3d = (new Vector3d(this.angler.getX() - this.getX(), this.angler.getY() - this.getY(), this.angler.getZ() - this.getZ())).scale(0.1D);
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
    @NotNull
    public IPacket<?> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(final PacketBuffer buffer)
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
    public void readSpawnData(final PacketBuffer additionalData)
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
