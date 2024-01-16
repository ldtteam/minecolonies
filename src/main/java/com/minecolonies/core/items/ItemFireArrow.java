package com.minecolonies.core.items;

import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.entity.ModEntities;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Class handling the Scepter for the Pharao.
 */
public class ItemFireArrow extends ArrowItem
{
    /**
     * Constructor method for the Chief Sword Item
     *
     * @param properties the properties.
     */
    public ItemFireArrow(final Properties properties)
    {
        super(properties.tab(ModCreativeTabs.MINECOLONIES));
    }

    @Override
    public boolean hasCustomEntity(final ItemStack stack)
    {
        return true;
    }

    @NotNull
    @Override
    public AbstractArrow createArrow(@NotNull final Level worldIn, @NotNull final ItemStack stack, final LivingEntity shooter)
    {
        AbstractArrow entity = ModEntities.FIREARROW.create(worldIn);
        entity.setOwner(shooter);
        return entity;
    }

    @Nullable
    @Override
    public Entity createEntity(final Level world, final Entity location, final ItemStack itemstack)
    {
        return ModEntities.FIREARROW.create(world);
    }
}
