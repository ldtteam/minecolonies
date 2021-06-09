package com.minecolonies.coremod.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.network.IPacket;
import net.minecraft.util.DamageSource;
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
    private static final int MAX_TIME_IN_GROUND = 100;

    /**
     * Whether the arrow entity pierces players
     */
    private boolean armorPiercePlayer = false;

    public CustomArrowEntity(final EntityType<? extends ArrowEntity> type, final World world)
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
    public IPacket<?> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void onHitEntity(EntityRayTraceResult traceResult)
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
            if (player instanceof PlayerEntity)
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
    }

    /**
     * Makes the arrow pierce player armor
     */
    public void setPlayerArmorPierce()
    {
        armorPiercePlayer = true;
    }

    @Override
    public void tick()
    {
        super.tick();

        if (this.inGroundTime > MAX_TIME_IN_GROUND)
        {
            remove();
        }
    }
}
