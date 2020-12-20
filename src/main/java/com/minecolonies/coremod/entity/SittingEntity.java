package com.minecolonies.coremod.entity;

import com.minecolonies.api.entity.ModEntities;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.List;

/**
 * Entity used to sit on, for animation purposes.
 */
public class SittingEntity extends Entity
{
    /**
     * The lifetime in ticks of the entity, auto-dismounts after.
     */
    int maxLifeTime = 100;

    public SittingEntity(final EntityType<?> type, final World worldIn)
    {
        super(type, worldIn);

        this.setInvisible(true);
        this.forceSpawn = true;
        this.noClip = true;
        this.setNoGravity(true);
    }

    public SittingEntity(final EntityType<?> type, final World worldIn, double x, double y, double z, int lifeTime)
    {
        super(type, worldIn);

        this.setPosition(x, y, z);

        this.setInvisible(true);
        this.forceSpawn = true;
        this.noClip = true;
        this.setNoGravity(true);
        this.maxLifeTime = lifeTime;
    }

    /**
     * Do not let the entity be destroyed
     */
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        return false;
    }

    /**
     * No Collision
     */
    public boolean canBeCollidedWith()
    {
        return false;
    }

    @Override
    protected void readAdditional(final CompoundNBT compound)
    {

    }

    @Override
    protected void writeAdditional(final CompoundNBT compound)
    {

    }

    @Override
    public IPacket<?> createSpawnPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void registerData()
    {

    }

    @Override
    public void tick()
    {
        if (this.world.isRemote)
        {
            return;
        }

        if (!this.isBeingRidden() || maxLifeTime-- < 0)
        {
            // Upsizes entity again

            if (getPassengers().size() > 0)
            {
                this.removePassengers();
            }

            this.remove();
        }
    }

    @Override
    protected void addPassenger(Entity passenger)
    {
        super.addPassenger(passenger);
        if (this.world.isRemote)
        {
            return;
        }

        passenger.size = passenger.size.scale(1.0f, 0.5f);
    }

    @Override
    protected void removePassenger(Entity passenger)
    {
        super.removePassenger(passenger);
        if (this.world.isRemote)
        {
            return;
        }

        if (passenger instanceof LivingEntity)
        {
            passenger.size = ((LivingEntity) passenger).isChild() ? passenger.getType().getSize().scale(0.5f) : passenger.getType().getSize();
        }
        passenger.setPosition(this.getPosX(), this.getPosY() + 0.6, this.getPosZ());
    }

    /**
     * Sets the lifetime
     *
     * @param maxLifeTime the max life span of the entity.
     */
    public void setMaxLifeTime(final int maxLifeTime)
    {
        this.maxLifeTime = maxLifeTime;
    }

    /**
     * Makes the given entity sit down onto a new sitting entity
     *
     * @param pos         the position to sit at
     * @param entity      entity to sit down
     * @param maxLifeTime max time to sit
     */
    public static void sitDown(final BlockPos pos, final MobEntity entity, final int maxLifeTime)
    {
        if (entity.getRidingEntity() != null)
        {
            // Already riding an entity, abort
            return;
        }

        final SittingEntity sittingEntity = (SittingEntity) ModEntities.SITTINGENTITY.create(entity.world);

        // Find the lowest box and sit on that
        final BlockState state = entity.world.getBlockState(pos);
        double minY = 1;

        final List<AxisAlignedBB> shapes = state.getCollisionShape(entity.world, pos).toBoundingBoxList();
        for (final AxisAlignedBB box : shapes)
        {
            if (box.maxY < minY)
            {
                minY = box.maxY;
            }
        }

        if (shapes.isEmpty())
        {
            minY = 0;
        }

        sittingEntity.setPosition(pos.getX() + 0.5f, (pos.getY() + minY) - entity.getHeight() / 2, pos.getZ() + 0.5f);
        sittingEntity.setMaxLifeTime(maxLifeTime);
        entity.world.addEntity(sittingEntity);
        entity.startRiding(sittingEntity);
        entity.getNavigator().clearPath();
    }
}
