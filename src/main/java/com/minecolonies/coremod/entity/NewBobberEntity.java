package com.minecolonies.coremod.entity;

import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.*;
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
    private static final DataParameter<Integer> DATA_HOOKED_ENTITY = EntityDataManager.createKey(NewBobberEntity.class,
        DataSerializers.VARINT);
    private boolean inGround;
    private int ticksInGround;
    private EntityCitizen angler;
    private int tickRemove = 100;
    private int ticksInAir;
    private int ticksCatchable;
    private int ticksCaughtDelay;
    private int ticksCatchableDelay;
    private float fishApproachAngle;
    public Entity caughtEntity;
    private NewBobberEntity.State currentState = NewBobberEntity.State.FLYING;
    private int luck;
    private int lureSpeed;
    private int anglerId = -1;
    private boolean readyToCatch = false;

    public NewBobberEntity(final EntityType<?> type, final World world)
    {
        super(type, world);
        this.ignoreFrustumCheck = true;
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
        float f = this.angler.rotationPitch;
        float f1 = this.angler.rotationYaw;
        float f2 = MathHelper.cos(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
        float f3 = MathHelper.sin(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
        float f4 = -MathHelper.cos(-f * ((float) Math.PI / 180F));
        float f5 = MathHelper.sin(-f * ((float) Math.PI / 180F));
        double d0 = this.angler.getPosX() - (double) f3 * 0.3D;
        double d1 = this.angler.getPosYEye();
        double d2 = this.angler.getPosZ() - (double) f2 * 0.3D;
        this.setLocationAndAngles(d0, d1, d2, f1, f);
        Vec3d vec3d = new Vec3d((double) (-f3), (double) MathHelper.clamp(-(f5 / f4), -5.0F, 5.0F), (double) (-f2));
        double d3 = vec3d.length();
        vec3d = vec3d.mul(0.6D / d3 + 0.5D + this.rand.nextGaussian() * 0.0045D,
            0.6D / d3 + 0.5D + this.rand.nextGaussian() * 0.0045D,
            0.6D / d3 + 0.5D + this.rand.nextGaussian() * 0.0045D);
        this.setMotion(vec3d);
        this.rotationYaw = (float) (MathHelper.atan2(vec3d.x, vec3d.z) * (double) (180F / (float) Math.PI));
        this.rotationPitch = (float) (MathHelper.atan2(vec3d.y, (double) MathHelper.sqrt(horizontalMag(vec3d)))
            * (double) (180F / (float) Math.PI));
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;
        this.luck = Math.max(0, luck);
        this.lureSpeed = Math.max(0, lureSpeed);
    }

    protected void registerData()
    {
        this.getDataManager().register(DATA_HOOKED_ENTITY, 0);
    }

    public void notifyDataManagerChange(DataParameter<?> key)
    {
        if (DATA_HOOKED_ENTITY.equals(key))
        {
            int i = this.getDataManager().get(DATA_HOOKED_ENTITY);
            this.caughtEntity = i > 0 ? this.world.getEntityByID(i - 1) : null;
        }

        super.notifyDataManagerChange(key);
    }

    /**
     * Checks if the entity is in range to render.
     */
    @OnlyIn(Dist.CLIENT)
    public boolean isInRangeToRenderDist(double distance)
    {
        return distance < 4096.0D;
    }

    @Override
    protected void readAdditional(@NotNull final CompoundNBT compound)
    {
    }

    @Override
    protected void writeAdditional(final CompoundNBT compound)
    {
    }

    /**
     * Sets a target for the client to interpolate towards over the next few ticks
     */
    @OnlyIn(Dist.CLIENT)
    public void setPositionAndRotationDirect(double x,
        double y,
        double z,
        float yaw,
        float pitch,
        int posRotationIncrements,
        boolean teleport)
    {
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void tick()
    {
        super.tick();
        if (!this.world.isRemote())
        {
            if (--this.tickRemove <= 0)
            {
                this.remove();
                return;
            }
        }

        if (this.angler == null)
        {
            if (world.isRemote)
            {
                if (anglerId > -1)
                {
                    angler = (EntityCitizen) world.getEntityByID(anglerId);
                }
            }
            else
            {
                this.remove();
            }
        }
        else if (this.world.isRemote || !this.shouldStopFishing())
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
            BlockPos blockpos = new BlockPos(this);
            IFluidState ifluidstate = this.world.getFluidState(blockpos);
            if (ifluidstate.isTagged(FluidTags.WATER))
            {
                f = ifluidstate.getActualHeight(this.world, blockpos);
            }

            if (this.currentState == NewBobberEntity.State.FLYING)
            {
                if (this.caughtEntity != null)
                {
                    this.setMotion(Vec3d.ZERO);
                    this.currentState = NewBobberEntity.State.HOOKED_IN_ENTITY;
                    return;
                }

                if (f > 0.0F)
                {
                    this.setMotion(this.getMotion().mul(0.3D, 0.2D, 0.3D));
                    this.currentState = NewBobberEntity.State.BOBBING;
                    return;
                }

                if (!this.world.isRemote)
                {
                    this.checkCollision();
                }

                if (!this.inGround && !this.onGround && !this.collidedHorizontally)
                {
                    ++this.ticksInAir;
                }
                else
                {
                    this.ticksInAir = 0;
                    this.setMotion(Vec3d.ZERO);
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
                            this.setPosition(this.caughtEntity.posX, this.caughtEntity.getPosYHeight(0.8D), this.caughtEntity.posZ);
                        }
                    }

                    return;
                }

                if (this.currentState == NewBobberEntity.State.BOBBING)
                {
                    Vec3d vec3d = this.getMotion();
                    double d0 = this.posY + vec3d.y - (double) blockpos.getY() - (double) f;
                    if (Math.abs(d0) < 0.01D)
                    {
                        d0 += Math.signum(d0) * 0.1D;
                    }

                    this.setMotion(vec3d.x * 0.9D, vec3d.y - d0 * (double) this.rand.nextFloat() * 0.2D, vec3d.z * 0.9D);
                    if (!this.world.isRemote && f > 0.0F)
                    {
                        this.catchingFish(blockpos);
                    }
                }
            }

            if (!ifluidstate.isTagged(FluidTags.WATER))
            {
                this.setMotion(this.getMotion().add(0.0D, -0.03D, 0.0D));
            }

            this.move(MoverType.SELF, this.getMotion());
            this.updateRotation();
            this.setMotion(this.getMotion().scale(0.92D));
            this.recenterBoundingBox();
        }
    }

    public boolean shouldStopFishing()
    {
        ItemStack itemstack = this.angler.getHeldItemMainhand();
        ItemStack itemstack1 = this.angler.getHeldItemOffhand();
        boolean flag = itemstack.getItem() instanceof net.minecraft.item.FishingRodItem;
        boolean flag1 = itemstack1.getItem() instanceof net.minecraft.item.FishingRodItem;
        if (!this.angler.removed && this.angler.isAlive() && (flag || flag1) && !(this.getDistanceSq(this.angler) > 1024.0D))
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
        Vec3d vec3d = this.getMotion();
        float f = MathHelper.sqrt(horizontalMag(vec3d));
        this.rotationYaw = (float) (MathHelper.atan2(vec3d.x, vec3d.z) * (double) (180F / (float) Math.PI));

        for (this.rotationPitch = (float) (MathHelper.atan2(vec3d.y, (double) f) * (double) (180F / (float) Math.PI)); this.rotationPitch
            - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
        {
            ;
        }

        while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
        {
            this.prevRotationPitch += 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw < -180.0F)
        {
            this.prevRotationYaw -= 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
        {
            this.prevRotationYaw += 360.0F;
        }

        this.rotationPitch = MathHelper.lerp(0.2F, this.prevRotationPitch, this.rotationPitch);
        this.rotationYaw = MathHelper.lerp(0.2F, this.prevRotationYaw, this.rotationYaw);
    }

    private void checkCollision()
    {
        RayTraceResult raytraceresult = ProjectileHelper
            .rayTrace(this, this.getBoundingBox().expand(this.getMotion()).grow(1.0D), (p_213856_1_) -> {
                return !p_213856_1_.isSpectator() && (p_213856_1_.canBeCollidedWith() || p_213856_1_ instanceof ItemEntity)
                    && (p_213856_1_ != this.angler || this.ticksInAir >= 5);
            }, RayTraceContext.BlockMode.COLLIDER, true);
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
        this.getDataManager().set(DATA_HOOKED_ENTITY, this.caughtEntity.getEntityId() + 1);
    }

    private void catchingFish(BlockPos p_190621_1_)
    {
        ServerWorld serverworld = (ServerWorld) this.world;
        int i = 1;
        BlockPos blockpos = p_190621_1_.up();
        if (this.rand.nextFloat() < 0.25F && this.world.isRainingAt(blockpos))
        {
            ++i;
        }

        if (this.rand.nextFloat() < 0.5F && !this.world.canSeeSky(blockpos))
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
                this.setMotion(this.getMotion().add(0.0D, -0.2D * (double) this.rand.nextFloat() * (double) this.rand.nextFloat(), 0.0D));
            }
        }
        else if (this.ticksCatchableDelay > 0)
        {
            this.ticksCatchableDelay -= i;
            if (this.ticksCatchableDelay > 0)
            {
                this.fishApproachAngle = (float) ((double) this.fishApproachAngle + this.rand.nextGaussian() * 4.0D);
                float f = this.fishApproachAngle * ((float) Math.PI / 180F);
                float f1 = MathHelper.sin(f);
                float f2 = MathHelper.cos(f);
                double d0 = this.posX + (double) (f1 * (float) this.ticksCatchableDelay * 0.1F);
                double d1 = (double) ((float) MathHelper.floor(this.posY) + 1.0F);
                double d2 = this.posZ + (double) (f2 * (float) this.ticksCatchableDelay * 0.1F);
                if (serverworld.getBlockState(new BlockPos((int) d0, (int) d1 - 1, (int) d2)).getMaterial()
                    == net.minecraft.block.material.Material.WATER)
                {
                    if (this.rand.nextFloat() < 0.15F)
                    {
                        serverworld
                            .spawnParticle(ParticleTypes.BUBBLE, d0, d1 - (double) 0.1F, d2, 1, (double) f1, 0.1D, (double) f2, 0.0D);
                    }

                    float f3 = f1 * 0.04F;
                    float f4 = f2 * 0.04F;
                    serverworld.spawnParticle(ParticleTypes.FISHING, d0, d1, d2, 0, (double) f4, 0.01D, (double) (-f3), 1.0D);
                    serverworld.spawnParticle(ParticleTypes.FISHING, d0, d1, d2, 0, (double) (-f4), 0.01D, (double) f3, 1.0D);
                }
            }
            else
            {
                readyToCatch = true;
                Vec3d vec3d = this.getMotion();
                this.setMotion(vec3d.x, (double) (-0.4F * MathHelper.nextFloat(this.rand, 0.6F, 1.0F)), vec3d.z);
                this.playSound(SoundEvents.ENTITY_FISHING_BOBBER_SPLASH,
                    0.25F,
                    1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
                double d3 = this.posY + 0.5D;
                serverworld.spawnParticle(ParticleTypes.BUBBLE,
                    this.posX,
                    d3,
                    this.posZ,
                    (int) (1.0F + this.getWidth() * 20.0F),
                    (double) this.getWidth(),
                    0.0D,
                    (double) this.getWidth(),
                    (double) 0.2F);
                serverworld.spawnParticle(ParticleTypes.FISHING,
                    this.posX,
                    d3,
                    this.posZ,
                    (int) (1.0F + this.getWidth() * 20.0F),
                    (double) this.getWidth(),
                    0.0D,
                    (double) this.getWidth(),
                    (double) 0.2F);
                this.ticksCatchable = MathHelper.nextInt(this.rand, 20, 40);
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

            if (this.rand.nextFloat() < f5)
            {
                float f6 = MathHelper.nextFloat(this.rand, 0.0F, 360.0F) * ((float) Math.PI / 180F);
                float f7 = MathHelper.nextFloat(this.rand, 25.0F, 60.0F);
                double d4 = this.posX + (double) (MathHelper.sin(f6) * f7 * 0.1F);
                double d5 = (double) ((float) MathHelper.floor(this.posY) + 1.0F);
                double d6 = this.posZ + (double) (MathHelper.cos(f6) * f7 * 0.1F);
                if (serverworld.getBlockState(new BlockPos(d4, d5 - 1.0D, d6)).getMaterial() == net.minecraft.block.material.Material.WATER)
                {
                    serverworld.spawnParticle(ParticleTypes.SPLASH,
                        d4,
                        d5,
                        d6,
                        2 + this.rand.nextInt(2),
                        (double) 0.1F,
                        0.0D,
                        (double) 0.1F,
                        0.0D);
                }
            }

            if (this.ticksCaughtDelay <= 0)
            {
                this.fishApproachAngle = MathHelper.nextFloat(this.rand, 0.0F, 360.0F);
                this.ticksCatchableDelay = MathHelper.nextInt(this.rand, 20, 80);
            }
        }
        else
        {
            this.ticksCaughtDelay = MathHelper.nextInt(this.rand, 100, 600);
            this.ticksCaughtDelay -= this.lureSpeed * 20 * 5;
            this.ticksCaughtDelay = Math.max(5, ticksCaughtDelay);
        }
    }

    public int getDamage()
    {
        if (!this.world.isRemote && this.angler != null)
        {
            int i = 0;
            net.minecraftforge.event.entity.player.ItemFishedEvent event = null;
            if (this.caughtEntity != null)
            {
                this.bringInHookedEntity();
                this.world.setEntityState(this, (byte) 31);
                i = this.caughtEntity instanceof ItemEntity ? 3 : 5;
            }
            else if (this.ticksCatchable > 0)
            {
                LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerWorld) this.world))
                    .withParameter(LootParameters.POSITION, new BlockPos(this))
                    .withParameter(LootParameters.TOOL, this.getAngler().getHeldItemMainhand())
                    .withRandom(this.rand)
                    .withLuck((float) this.luck);
                lootcontext$builder.withParameter(LootParameters.KILLER_ENTITY, this.angler)
                    .withParameter(LootParameters.THIS_ENTITY, this);
                LootTable loottable = this.world.getServer().getLootTableManager().getLootTableFromLocation(LootTables.GAMEPLAY_FISHING);
                List<ItemStack> list = loottable.generate(lootcontext$builder.build(LootParameterSets.FISHING));

                for (ItemStack itemstack : list)
                {
                    ItemEntity itementity = new ItemEntity(this.world, this.posX, this.posY, this.posZ, itemstack);
                    double d0 = this.angler.posX - this.posX;
                    double d1 = this.angler.posY - this.posY;
                    double d2 = this.angler.posZ - this.posZ;
                    itementity.setMotion(d0 * 0.1D, d1 * 0.1D + Math.sqrt(Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2)) * 0.08D, d2 * 0.1D);
                    this.world.addEntity(itementity);
                    this.angler.world.addEntity(new ExperienceOrbEntity(this.angler.world,
                        this.angler.posX,
                        this.angler.posY + 0.5D,
                        this.angler.posZ + 0.5D,
                        this.rand.nextInt(6) + 1));
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
     * Handler for {@link World#setEntityState}
     */
    @OnlyIn(Dist.CLIENT)
    public void handleStatusUpdate(byte id)
    {
        if (id == 31 && this.world.isRemote && this.caughtEntity instanceof EntityCitizen)
        {
            this.bringInHookedEntity();
        }

        super.handleStatusUpdate(id);
    }

    protected void bringInHookedEntity()
    {
        if (this.angler != null)
        {
            Vec3d vec3d = (new Vec3d(this.angler.posX - this.posX, this.angler.posY - this.posY, this.angler.posZ - this.posZ)).scale(0.1D);
            this.caughtEntity.setMotion(this.caughtEntity.getMotion().add(vec3d));
        }
    }

    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to
     * prevent them from trampling crops
     */
    protected boolean canTriggerWalking()
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
    public boolean isNonBoss()
    {
        return false;
    }

    @Override
    @NotNull
    public IPacket<?> createSpawnPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(final PacketBuffer buffer)
    {
        if (angler != null)
        {
            buffer.writeInt(angler.getEntityId());
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
