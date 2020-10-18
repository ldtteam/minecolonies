package com.minecolonies.coremod.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

/**
 * Custom arrow entity class which remove themselves when on the ground for a bit to not cause lag and they do not scale in damage with their motion.
 */
public class CustomArrowEntity extends ArrowEntity
{
    /**
     * Max time the arrow is stuck before removing it
     */
    private static final int MAX_TIME_IN_GROUND = 10;

    public CustomArrowEntity(final EntityType<? extends ArrowEntity> type, final World world)
    {
        super(type, world);
    }

    @Override
    protected void arrowHit(LivingEntity target)
    {
        // TODO add enderman damage hit research here not that this is also used by mobs so check the shooter
        super.arrowHit(target);
    }

    @Override
    @NotNull
    public IPacket<?> createSpawnPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void onEntityHit(EntityRayTraceResult traceResult)
    {
        final double prevDamage = getDamage();

        // Reduce damage by motion before vanilla increases it by the same factor, so our damage stays.
        float f = (float) this.getMotion().length();
        if (f != 0)
        {
            setDamage(prevDamage / f);
        }

        super.onEntityHit(traceResult);

        // Set the old actual damage value back
        setDamage(prevDamage);
    }

    @Override
    public void tick()
    {
        super.tick();

        if (this.timeInGround > MAX_TIME_IN_GROUND)
        {
            remove();
        }
    }
}
