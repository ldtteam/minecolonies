package com.minecolonies.coremod.items;

import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.coremod.entity.FireArrowEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShootableItem;
import net.minecraft.item.TippedArrowItem;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * Class handling the Scepter for the Pharao.
 */
public class ItemFireArrow extends ArrowItem
{
    /**
     * Constructor method for the Chief Sword Item
     * @param properties the properties.
     */
    public ItemFireArrow(final Properties properties)
    {
        super(properties.group(ModCreativeTabs.MINECOLONIES));
        setRegistryName("firearrow");
    }

    @Override
    public boolean hasCustomEntity(final ItemStack stack)
    {
        return true;
    }

    @NotNull
    @Override
    public AbstractArrowEntity createArrow(@NotNull final World worldIn, @NotNull final ItemStack stack, final LivingEntity shooter)
    {
        AbstractArrowEntity entity = ModEntities.FIREARROW.create(worldIn);
        entity.setShooter(shooter);
        return entity;
    }

    @Nullable
    @Override
    public Entity createEntity(final World world, final Entity location, final ItemStack itemstack)
    {
        return ModEntities.FIREARROW.create(world);
    }
}
