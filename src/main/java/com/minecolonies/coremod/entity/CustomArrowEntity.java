package com.minecolonies.coremod.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * Custom arrow entity class which remove themselves when on the ground for a bit to not cause lag and they do not scale in damage with their motion.
 */
public class CustomArrowEntity extends Arrow
{
    /**
     * Max time the arrow is stuck before removing it
     */
    private static final int MAX_LIVE_TIME = 10 * 20;

    /**
     * Max time the arrow is stuck in ground before removing it.
     */
    private static final int GROUND_LIVE_TIME = 2 * 20;

    /**
     * Whether the arrow entity pierces players
     */
    private boolean armorPiercePlayer = false;

    /**
     * Callback on hitting an entity
     */
    private Predicate<EntityHitResult> onHitCallback = null;

    public CustomArrowEntity(final EntityType<? extends Arrow> type, final Level world)
    {
        super(type, world);
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target)
    {
        // TODO add enderman damage hit research here. Note that this is also used by mobs, so check the shooter first.
        super.doPostHurtEffects(target);
    }

    @Override
    @NotNull
    public Packet<?> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void onHitEntity(EntityHitResult traceResult)
    {
        final double prevDamage = getBaseDamage();

        // Reduce damage by motion before vanilla increases it by the same factor, so our damage stays.
        float f = (float) this.getDeltaMovement().length();
        if (f != 0)
        {
            setBaseDamage(prevDamage / f);
        }

        if (armorPiercePlayer)
        {
            final Entity player = traceResult.getEntity();
            if (player instanceof Player)
            {
                Entity shooter = this.getOwner();
                DamageSource source;
                if (shooter == null)
                {
                    source = DamageSource.arrow(this, this);
                }
                else
                {
                    source = DamageSource.arrow(this, shooter);
                }
                source.bypassArmor();
                player.hurt(source, (float) getBaseDamage());
                setBaseDamage(0);
            }
        }

        super.onHitEntity(traceResult);

        // Set the old actual damage value back
        setBaseDamage(prevDamage);
        if (onHitCallback != null && onHitCallback.test(traceResult))
        {
            onHitCallback = null;
        }
    }

    /**
     * Set the hit callback action
     *
     * @param onHitCallback
     */
    public void setOnHitCallback(final Predicate<EntityHitResult> onHitCallback)
    {
        this.onHitCallback = onHitCallback;
    }

    /**
     * Makes the arrow pierce player armor
     */
    public void setPlayerArmorPierce()
    {
        armorPiercePlayer = true;
    }

    @Override
    public boolean shouldFall()
    {
        if (this.inGround)
        {
            final AABB aabb = (new AABB(this.position(), this.position())).inflate(0.06D);
            for(VoxelShape voxelshape : this.level.getBlockCollisions(null, aabb)) {
                if (!voxelshape.isEmpty())
                {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean save(@NotNull CompoundTag nbt)
    {
        return false;
    }

    @Override
    public void load(@NotNull CompoundTag nbt)
    {
        discard();
    }

    @Override
    public void tick()
    {
        if (this.tickCount > MAX_LIVE_TIME)
        {
            remove(RemovalReason.DISCARDED);
            return;
        }

        if (this.inGroundTime > GROUND_LIVE_TIME)
        {
            remove(RemovalReason.DISCARDED);
            return;
        }

        super.tick();
    }
}
