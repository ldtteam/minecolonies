package com.minecolonies.core.entity.other;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

import javax.annotation.Nullable;
import java.util.List;

public class NewBobberEntity extends Projectile implements IEntityWithComplexSpawn
{
    public static final int XP_PER_CATCH = 2;

    private static final EntityDataAccessor<Integer> DATA_HOOKED_ENTITY = SynchedEntityData.defineId(NewBobberEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_BITING       = SynchedEntityData.defineId(FishingHook.class, EntityDataSerializers.BOOLEAN);

    private final RandomSource syncronizedRandom = RandomSource.create();

    private boolean biting;
    private int outOfWaterTime;
    private int life;
    private int nibble;
    private int timeUntilLured;
    private int timeUntilHooked;
    private float fishAngle;
    private boolean     openWater = true;
    @Nullable
    private Entity      hookedEntity;
    private BobberState currentState = BobberState.FLYING;
    private final int luck;
    private final int lureSpeed;

    AbstractEntityCitizen angler = null;
    private int anglerId = -1;

    private NewBobberEntity(EntityType<? extends Projectile> type, Level level, int luck, int lure)
    {
        super(type, level);
        this.noCulling = true;
        this.luck = Math.max(0, luck);
        this.lureSpeed = Math.max(0, lure);
    }

    public NewBobberEntity(EntityType<? extends Projectile> type, Level level) 
    {
        this(type, level, 0, 0);
    }

    public NewBobberEntity(EntityType<? extends Projectile> type, AbstractEntityCitizen citizen, Level level, int luck, int lure)
    {
        this(type, level, luck, lure);
        this.setOwner(citizen);
        float f = citizen.getXRot();
        float f1 = citizen.getYRot();
        float f2 = Mth.cos(-f1 * (float) (Math.PI / 180.0) - (float) Math.PI);
        float f3 = Mth.sin(-f1 * (float) (Math.PI / 180.0) - (float) Math.PI);
        float f4 = -Mth.cos(-f * (float) (Math.PI / 180.0));
        float f5 = Mth.sin(-f * (float) (Math.PI / 180.0));
        double d0 = citizen.getX() - (double)f3 * 0.3;
        double d1 = citizen.getEyeY();
        double d2 = citizen.getZ() - (double)f2 * 0.3;
        this.moveTo(d0, d1, d2, f1, f);
        Vec3 vec3 = new Vec3((double)(-f3), (double)Mth.clamp(-(f5 / f4), -5.0F, 5.0F), (double)(-f2));
        double d3 = vec3.length();
        vec3 = vec3.multiply(
          0.6 / d3 + this.random.triangle(0.5, 0.0103365), 0.6 / d3 + this.random.triangle(0.5, 0.0103365), 0.6 / d3 + this.random.triangle(0.5, 0.0103365)
        );
        this.setDeltaMovement(vec3);
        this.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * 180.0F / (float)Math.PI));
        this.setXRot((float)(Mth.atan2(vec3.y, vec3.horizontalDistance()) * 180.0F / (float)Math.PI));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }


    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder)
    {
        builder.define(DATA_HOOKED_ENTITY, 0);
        builder.define(DATA_BITING, false);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> p_37153_) {
        if (DATA_HOOKED_ENTITY.equals(p_37153_)) {
            int i = this.getEntityData().get(DATA_HOOKED_ENTITY);
            this.hookedEntity = i > 0 ? this.level().getEntity(i - 1) : null;
        }

        if (DATA_BITING.equals(p_37153_)) {
            this.biting = this.getEntityData().get(DATA_BITING);
            if (this.biting) {
                this.setDeltaMovement(this.getDeltaMovement().x, (double)(-0.4F * Mth.nextFloat(this.syncronizedRandom, 0.6F, 1.0F)), this.getDeltaMovement().z);
            }
        }

        super.onSyncedDataUpdated(p_37153_);
    }

    /**
     * Checks if the entity is in range to render.
     */
    @OnlyIn(Dist.CLIENT)
    public boolean shouldRenderAtSqrDistance(final double distance)
    {
        return distance < 4096.0D;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void lerpTo(
      final double x,
      final double y,
      final double z,
      final float yaw,
      final float pitch,
      final int posRotationIncrements) { }

    @Override
    public void tick() {
        this.syncronizedRandom.setSeed(this.getUUID().getLeastSignificantBits() ^ this.level().getGameTime());
        super.tick();
        AbstractEntityCitizen citizen = this.getAngler();
        if (citizen == null) {
            this.discard();
        } else if (this.level().isClientSide || !this.shouldStopFishing(citizen)) {
            if (this.onGround()) {
                this.life++;
                if (this.life >= 1200) {
                    this.discard();
                    return;
                }
            } else {
                this.life = 0;
            }

            float f = 0.0F;
            BlockPos blockpos = this.blockPosition();
            FluidState fluidstate = this.level().getFluidState(blockpos);
            if (fluidstate.is(FluidTags.WATER)) {
                f = fluidstate.getHeight(this.level(), blockpos);
            }

            boolean flag = f > 0.0F;
            if (this.currentState == BobberState.FLYING) {
                if (this.hookedEntity != null) {
                    this.setDeltaMovement(Vec3.ZERO);
                    this.currentState = BobberState.HOOKED_IN_ENTITY;
                    return;
                }

                if (flag) {
                    this.setDeltaMovement(this.getDeltaMovement().multiply(0.3, 0.2, 0.3));
                    this.currentState = BobberState.BOBBING;
                    return;
                }

                this.checkCollision();
            } else {
                if (this.currentState == BobberState.HOOKED_IN_ENTITY) {
                    if (this.hookedEntity != null) {
                        if (!this.hookedEntity.isRemoved() && this.hookedEntity.level().dimension() == this.level().dimension()) {
                            this.setPos(this.hookedEntity.getX(), this.hookedEntity.getY(0.8), this.hookedEntity.getZ());
                        } else {
                            this.setHookedEntity(null);
                            this.currentState = BobberState.FLYING;
                        }
                    }

                    return;
                }

                if (this.currentState == BobberState.BOBBING) {
                    Vec3 vec3 = this.getDeltaMovement();
                    double d0 = this.getY() + vec3.y - (double)blockpos.getY() - (double)f;
                    if (Math.abs(d0) < 0.01) {
                        d0 += Math.signum(d0) * 0.1;
                    }

                    this.setDeltaMovement(vec3.x * 0.9, vec3.y - d0 * (double)this.random.nextFloat() * 0.2, vec3.z * 0.9);
                    if (this.nibble <= 0 && this.timeUntilHooked <= 0) {
                        this.openWater = true;
                    } else {
                        this.openWater = this.openWater && this.outOfWaterTime < 10 && this.calculateOpenWater(blockpos);
                    }

                    if (flag) {
                        this.outOfWaterTime = Math.max(0, this.outOfWaterTime - 1);
                        if (this.biting) {
                            this.setDeltaMovement(
                              this.getDeltaMovement()
                                .add(0.0, -0.1 * (double)this.syncronizedRandom.nextFloat() * (double)this.syncronizedRandom.nextFloat(), 0.0)
                            );
                        }

                        if (!this.level().isClientSide) {
                            this.catchingFish(blockpos);
                        }
                    } else {
                        this.outOfWaterTime = Math.min(10, this.outOfWaterTime + 1);
                    }
                }
            }

            if (!fluidstate.is(FluidTags.WATER)) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.03, 0.0));
            }

            this.move(MoverType.SELF, this.getDeltaMovement());
            this.updateRotation();
            if (this.currentState == BobberState.FLYING && (this.onGround() || this.horizontalCollision)) {
                this.setDeltaMovement(Vec3.ZERO);
            }

            double d1 = 0.92;
            this.setDeltaMovement(this.getDeltaMovement().scale(0.92));
            this.reapplyPosition();
        }
    }

    public boolean shouldStopFishing(AbstractEntityCitizen citizen)
    {
        ItemStack mainHandItem = citizen.getMainHandItem();
        ItemStack offHandItem = citizen.getOffhandItem();
        boolean flag = mainHandItem.canPerformAction(net.neoforged.neoforge.common.ItemAbilities.FISHING_ROD_CAST);
        boolean flag1 = offHandItem.canPerformAction(net.neoforged.neoforge.common.ItemAbilities.FISHING_ROD_CAST);
        if (!citizen.isRemoved() && citizen.isAlive() && (flag || flag1) && !(this.distanceToSqr(citizen) > 1024.0))
        {
            return false;
        }
        else
        {
            this.discard();
            return true;
        }
    }

    private void checkCollision()
    {
        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitresult.getType() == HitResult.Type.MISS || !net.neoforged.neoforge.event.EventHooks.onProjectileImpact(this, hitresult)) this.onHit(hitresult);
    }

    @Override
    protected boolean canHitEntity(Entity hitEntity)
    {
        return super.canHitEntity(hitEntity) || hitEntity.isAlive() && hitEntity instanceof ItemEntity;
    }

    @Override
    protected void onHitEntity(EntityHitResult hitEntity)
    {
        super.onHitEntity(hitEntity);
        if (!this.level().isClientSide) {
            this.setHookedEntity(hitEntity.getEntity());
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hitBlock)
    {
        super.onHitBlock(hitBlock);
        this.setDeltaMovement(this.getDeltaMovement().normalize().scale(hitBlock.distanceTo(this)));
    }

    private void setHookedEntity(@Nullable Entity hookedEntity)
    {
        this.hookedEntity = hookedEntity;
        this.getEntityData().set(DATA_HOOKED_ENTITY, hookedEntity == null ? 0 : hookedEntity.getId() + 1);
    }

    private void catchingFish(BlockPos p_37146_)
    {
        ServerLevel serverlevel = (ServerLevel) this.level();
        int i = 1;
        BlockPos blockpos = p_37146_.above();
        if (this.random.nextFloat() < 0.25F && this.level().isRainingAt(blockpos))
        {
            i++;
        }

        if (this.random.nextFloat() < 0.5F && !this.level().canSeeSky(blockpos))
        {
            i--;
        }

        if (this.nibble > 0)
        {
            this.nibble--;
            if (this.nibble <= 0)
            {
                this.timeUntilLured = 0;
                this.timeUntilHooked = 0;
                this.getEntityData().set(DATA_BITING, false);
            }
        }
        else if (this.timeUntilHooked > 0)
        {
            this.timeUntilHooked -= i;
            if (this.timeUntilHooked > 0)
            {
                this.fishAngle = this.fishAngle + (float) this.random.triangle(0.0, 9.188);
                float f = this.fishAngle * (float) (Math.PI / 180.0);
                float f1 = Mth.sin(f);
                float f2 = Mth.cos(f);
                double d0 = this.getX() + (double) (f1 * (float) this.timeUntilHooked * 0.1F);
                double d1 = (double) ((float) Mth.floor(this.getY()) + 1.0F);
                double d2 = this.getZ() + (double) (f2 * (float) this.timeUntilHooked * 0.1F);
                BlockState blockstate = serverlevel.getBlockState(BlockPos.containing(d0, d1 - 1.0, d2));
                if (blockstate.is(Blocks.WATER))
                {
                    if (this.random.nextFloat() < 0.15F)
                    {
                        serverlevel.sendParticles(ParticleTypes.BUBBLE, d0, d1 - 0.1F, d2, 1, (double) f1, 0.1, (double) f2, 0.0);
                    }

                    float f3 = f1 * 0.04F;
                    float f4 = f2 * 0.04F;
                    serverlevel.sendParticles(ParticleTypes.FISHING, d0, d1, d2, 0, (double) f4, 0.01, (double) (-f3), 1.0);
                    serverlevel.sendParticles(ParticleTypes.FISHING, d0, d1, d2, 0, (double) (-f4), 0.01, (double) f3, 1.0);
                }
            }
            else
            {
                this.playSound(SoundEvents.FISHING_BOBBER_SPLASH, 0.25F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
                double d3 = this.getY() + 0.5;
                serverlevel.sendParticles(
                  ParticleTypes.BUBBLE,
                  this.getX(),
                  d3,
                  this.getZ(),
                  (int) (1.0F + this.getBbWidth() * 20.0F),
                  (double) this.getBbWidth(),
                  0.0,
                  (double) this.getBbWidth(),
                  0.2F
                );
                serverlevel.sendParticles(
                  ParticleTypes.FISHING,
                  this.getX(),
                  d3,
                  this.getZ(),
                  (int) (1.0F + this.getBbWidth() * 20.0F),
                  (double) this.getBbWidth(),
                  0.0,
                  (double) this.getBbWidth(),
                  0.2F
                );
                this.nibble = Mth.nextInt(this.random, 20, 40);
                this.getEntityData().set(DATA_BITING, true);
            }
        }
        else if (this.timeUntilLured > 0)
        {
            this.timeUntilLured -= i;
            float f5 = 0.15F;
            if (this.timeUntilLured < 20)
            {
                f5 += (float) (20 - this.timeUntilLured) * 0.05F;
            }
            else if (this.timeUntilLured < 40)
            {
                f5 += (float) (40 - this.timeUntilLured) * 0.02F;
            }
            else if (this.timeUntilLured < 60)
            {
                f5 += (float) (60 - this.timeUntilLured) * 0.01F;
            }

            if (this.random.nextFloat() < f5)
            {
                float f6 = Mth.nextFloat(this.random, 0.0F, 360.0F) * (float) (Math.PI / 180.0);
                float f7 = Mth.nextFloat(this.random, 25.0F, 60.0F);
                double d4 = this.getX() + (double) (Mth.sin(f6) * f7) * 0.1;
                double d5 = (double) ((float) Mth.floor(this.getY()) + 1.0F);
                double d6 = this.getZ() + (double) (Mth.cos(f6) * f7) * 0.1;
                BlockState blockstate1 = serverlevel.getBlockState(BlockPos.containing(d4, d5 - 1.0, d6));
                if (blockstate1.is(Blocks.WATER))
                {
                    serverlevel.sendParticles(ParticleTypes.SPLASH, d4, d5, d6, 2 + this.random.nextInt(2), 0.1F, 0.0, 0.1F, 0.0);
                }
            }

            if (this.timeUntilLured <= 0)
            {
                this.fishAngle = Mth.nextFloat(this.random, 0.0F, 360.0F);
                this.timeUntilHooked = Mth.nextInt(this.random, 20, 80);
            }
        }
        else
        {
            this.timeUntilLured = Mth.nextInt(this.random, 100, 600);
            this.timeUntilLured = this.timeUntilLured - this.lureSpeed;
        }
    }

    private boolean calculateOpenWater(BlockPos p_37159_)
    {
        OpenWaterType openWaterType = OpenWaterType.INVALID;

        for (int i = -1; i <= 2; i++)
        {
            OpenWaterType localOpenWaterType = this.getOpenWaterTypeForArea(p_37159_.offset(-2, i, -2), p_37159_.offset(2, i, 2));
            switch (localOpenWaterType)
            {
                case ABOVE_WATER:
                    if (openWaterType == OpenWaterType.INVALID)
                    {
                        return false;
                    }
                    break;
                case INSIDE_WATER:
                    if (openWaterType == OpenWaterType.ABOVE_WATER)
                    {
                        return false;
                    }
                    break;
                case INVALID:
                    return false;
            }

            openWaterType = localOpenWaterType;
        }

        return true;
    }

    private OpenWaterType getOpenWaterTypeForArea(BlockPos p_37148_, BlockPos p_37149_)
    {
        return BlockPos.betweenClosedStream(p_37148_, p_37149_)
                 .map(this::getOpenWaterTypeForBlock)
                 .reduce((p_37139_, p_37140_) -> p_37139_ == p_37140_ ? p_37139_ : OpenWaterType.INVALID)
                 .orElse(OpenWaterType.INVALID);
    }

    private OpenWaterType getOpenWaterTypeForBlock(BlockPos p_37164_)
    {
        BlockState blockstate = this.level().getBlockState(p_37164_);
        if (!blockstate.isAir() && !blockstate.is(Blocks.LILY_PAD))
        {
            FluidState fluidstate = blockstate.getFluidState();
            return fluidstate.is(FluidTags.WATER) && fluidstate.isSource() && blockstate.getCollisionShape(this.level(), p_37164_).isEmpty()
                     ? OpenWaterType.INSIDE_WATER
                     : OpenWaterType.INVALID;
        }
        else
        {
            return OpenWaterType.ABOVE_WATER;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag)
    {

    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag)
    {

    }

    public int retrieve(ItemStack p_37157_)
    {
        AbstractEntityCitizen citizen = this.getAngler();
        if (!this.level().isClientSide && citizen != null && !this.shouldStopFishing(citizen))
        {
            int i = 0;
            net.neoforged.neoforge.event.entity.player.ItemFishedEvent event = null;
            if (this.hookedEntity != null)
            {
                this.pullEntity(this.hookedEntity);
                this.level().broadcastEntityEvent(this, (byte) 31);
                i = this.hookedEntity instanceof ItemEntity ? 3 : 5;
            }
            else if (this.nibble > 0)
            {
                LootParams lootparams = new LootParams.Builder((ServerLevel) this.level())
                                          .withParameter(LootContextParams.ORIGIN, this.position())
                                          .withParameter(LootContextParams.TOOL, p_37157_)
                                          .withParameter(LootContextParams.THIS_ENTITY, this)
                                          .withParameter(LootContextParams.ATTACKING_ENTITY, this.getOwner())
                                          .withLuck((float) this.luck)
                                          .create(LootContextParamSets.FISHING);
                LootTable loottable = this.level().getServer().reloadableRegistries().getLootTable(ModLootTables.FISHING);
                List<ItemStack> list = loottable.getRandomItems(lootparams);
                if (event.isCanceled())
                {
                    this.discard();
                    return event.getRodDamage();
                }

                for (ItemStack itemstack : list)
                {
                    ItemEntity itementity = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), itemstack);
                    double d0 = citizen.getX() - this.getX();
                    double d1 = citizen.getY() - this.getY();
                    double d2 = citizen.getZ() - this.getZ();
                    itementity.setDeltaMovement(d0 * 0.1, d1 * 0.1 + Math.sqrt(Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2)) * 0.08, d2 * 0.1);
                    this.level().addFreshEntity(itementity);
                    citizen.level().addFreshEntity(new ExperienceOrb(citizen.level(), citizen.getX(), citizen.getY() + 0.5, citizen.getZ() + 0.5, this.random.nextInt(6) + 1));
                }

                i = 1;
            }

            if (this.onGround())
            {
                i = 2;
            }

            this.discard();
            if (event != null)
            {
                return event.getRodDamage();
            }
            return i;
        }
        else
        {
            return 0;
        }
    }

    @Override
    public void handleEntityEvent(byte b)
    {
        if (b == 31 && this.level().isClientSide && this.hookedEntity instanceof Player && ((Player)this.hookedEntity).isLocalPlayer())
        {
            this.pullEntity(this.hookedEntity);
        }

        super.handleEntityEvent(b);
    }

    protected void pullEntity(Entity entityToPull)
    {
        Entity entity = this.getOwner();
        if (entity != null) {
            Vec3 vec3 = new Vec3(entity.getX() - this.getX(), entity.getY() - this.getY(), entity.getZ() - this.getZ()).scale(0.1);
            entityToPull.setDeltaMovement(entityToPull.getDeltaMovement().add(vec3));
        }
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Nullable
    public Entity getHookedEntity() {
        return this.hookedEntity;
    }

    @Override
    public boolean canUsePortal(boolean p_352895_) {
        return false;
    }

    @Nullable
    public AbstractEntityCitizen getAngler()
    {
        return this.angler;
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
    public void readSpawnData(final RegistryFriendlyByteBuf additionalData)
    {
        final int citizenId = additionalData.readInt();
        if (citizenId != -1)
        {
            anglerId = citizenId;
        }
    }

    public boolean isReadyToCatch()
    {
        return biting;
    }

    /**
     * Sets tickRemove to 100 ticks to prevent bobber from staying in the water when Fisherman is not fishing.
     */
    public void setInUse()
    {
        this.life = 100;
    }

    private void setAngler(final EntityCitizen worker, final int fishingLuckBonus, final int i)
    {

    }

    enum BobberState
    {
        FLYING,
        HOOKED_IN_ENTITY,
        BOBBING;
    }

    enum OpenWaterType {
        ABOVE_WATER,
        INSIDE_WATER,
        INVALID;
    }
}
