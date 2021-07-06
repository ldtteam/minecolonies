package com.minecolonies.coremod.entity;

import com.minecolonies.api.items.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Custom arrow entity for the fire arrows.
 */
public class FireArrowEntity extends CustomArrowEntity
{
    public FireArrowEntity(EntityType<? extends ArrowEntity> entity, World world)
    {
        super(entity, world);
    }

    @Override
    public void setOwner(@Nullable final Entity shooter)
    {
        super.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - (double) 0.1F, shooter.getZ());
    }

    @NotNull
    @Override
    protected ItemStack getPickupItem()
    {
        return new ItemStack(ModItems.firearrow, 1);
    }
}
