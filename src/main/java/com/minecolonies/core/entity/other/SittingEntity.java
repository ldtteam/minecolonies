package com.minecolonies.core.entity.other;

import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.util.EntityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

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
    private BlockPos sittingpos = BlockPos.ZERO;

    public SittingEntity(final EntityType<?> type, final Level worldIn)
    {
        super(type, worldIn);

        this.setInvisible(true);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public SittingEntity(final EntityType<?> type, final Level worldIn, double x, double y, double z, int lifeTime)
    {
        super(type, worldIn);

        this.setPos(x, y, z);

        this.setInvisible(true);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.maxLifeTime = lifeTime;
    }

    /**
     * Do not let the entity be destroyed
     */
    public boolean hurt(DamageSource source, float amount)
    {
        return false;
    }

    /**
     * No Collision
     */
    public boolean isPickable()
    {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(final CompoundTag compound)
    {

    }

    @Override
    protected void addAdditionalSaveData(final CompoundTag compound)
    {

    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void defineSynchedData()
    {

    }

    @Override
    public void tick()
    {
        if (this.level.isClientSide)
        {
            return;
        }

        if (!this.isVehicle() || maxLifeTime-- < 0)
        {
            // Upsizes entity again

            if (getPassengers().size() > 0)
            {
                this.ejectPassengers();
            }

            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    @Override
    protected void addPassenger(Entity passenger)
    {
        super.addPassenger(passenger);
        if (this.level.isClientSide)
        {
            return;
        }

        passenger.dimensions = passenger.dimensions.scale(1.0f, 0.5f);
    }

    @Override
    protected void removePassenger(Entity passenger)
    {
        super.removePassenger(passenger);
        if (this.level.isClientSide)
        {
            return;
        }

        if (passenger instanceof LivingEntity)
        {
            passenger.dimensions = ((LivingEntity) passenger).isBaby() ? passenger.getType().getDimensions().scale(0.5f) : passenger.getType().getDimensions();
        }
    }

    @NotNull
    @Override
    public Vec3 getDismountLocationForPassenger(@NotNull final LivingEntity passenger)
    {
        final BlockPos start = sittingpos == BlockPos.ZERO ? blockPosition().above() : sittingpos;
        final BlockPos spawn = EntityUtils.getSpawnPoint(this.level, start);
        if (spawn == null)
        {
            return super.getDismountLocationForPassenger(passenger);
        }
        return new Vec3(spawn.getX() + 0.5, spawn.getY() + 0.2, spawn.getZ() + 0.5);
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
     * Sets the original block to sit "at" also used for standing up again.
     *
     * @param pos
     */
    public void setSittingPos(final BlockPos pos)
    {
        sittingpos = pos;
    }

    /**
     * Makes the given entity sit down onto a new sitting entity
     *
     * @param pos         the position to sit at
     * @param entity      entity to sit down
     * @param maxLifeTime max time to sit
     */
    public static void sitDown(final BlockPos pos, final Mob entity, final int maxLifeTime)
    {
        if (entity.getVehicle() != null)
        {
            // Already riding an entity, abort
            return;
        }

        final SittingEntity sittingEntity = (SittingEntity) ModEntities.SITTINGENTITY.create(entity.level);

        // Find the lowest box and sit on that
        final BlockState state = entity.level.getBlockState(pos);
        double minY = 1;

        final List<AABB> shapes = state.getCollisionShape(entity.level, pos).toAabbs();
        for (final AABB box : shapes)
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

        sittingEntity.setPos(pos.getX() + 0.5f, (pos.getY() + minY) - entity.getBbHeight() / 2, pos.getZ() + 0.5f);
        sittingEntity.setMaxLifeTime(maxLifeTime);
        sittingEntity.setSittingPos(pos);
        entity.level.addFreshEntity(sittingEntity);
        entity.startRiding(sittingEntity);
        entity.getNavigation().stop();
    }
}
