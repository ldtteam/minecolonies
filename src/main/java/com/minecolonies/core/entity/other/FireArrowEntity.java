package com.minecolonies.core.entity.other;

import com.minecolonies.api.items.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Custom arrow entity for the fire arrows.
 */
public class FireArrowEntity extends CustomArrowEntity
{
    public FireArrowEntity(EntityType<? extends Arrow> entity, Level world)
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
