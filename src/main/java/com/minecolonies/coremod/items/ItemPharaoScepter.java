package com.minecolonies.coremod.items;

import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.items.ModItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * Class handling the Chief Sword item.
 */
public class ItemPharaoScepter extends BowItem
{
    /**
     * Constructor method for the Chief Sword Item
     * @param properties the properties.
     */
    public ItemPharaoScepter(final Properties properties)
    {
        super(properties.group(ModCreativeTabs.MINECOLONIES));
        setRegistryName("pharaoscepter");
    }

    @NotNull
    @Override
    public Predicate<ItemStack> getInventoryAmmoPredicate()
    {
        return itemStack -> true;
    }

    @NotNull
    @Override
    public AbstractArrowEntity customeArrow(@NotNull AbstractArrowEntity arrow)
    {
        AbstractArrowEntity entity = ((ArrowItem) ModItems.firearrow).createArrow(arrow.world, new ItemStack(ModItems.firearrow, 1), (LivingEntity) arrow.getShooter());
        entity.pickupStatus = AbstractArrowEntity.PickupStatus.DISALLOWED;
        entity.setFire(3);

        return entity;
    }
}
