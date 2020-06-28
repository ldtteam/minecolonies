package com.minecolonies.coremod.entity;

import com.minecolonies.api.items.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Custom arrow entity for the fire arrows.
 */
public class FireArrowEntity extends AbstractArrowEntity
{
    public FireArrowEntity(EntityType<? extends AbstractArrowEntity> entity, World world)
    {
        super(entity, world);
    }

    @Override
    @NotNull
    public IPacket<?> createSpawnPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void setShooter(@Nullable final Entity shooter)
    {
        super.setShooter(shooter);
        this.setPosition(shooter.getPosX(), shooter.getPosYEye() - (double)0.1F, shooter.getPosZ());
    }

    @NotNull
    @Override
    protected ItemStack getArrowStack()
    {
        return new ItemStack(ModItems.firearrow, 1);
    }
}
