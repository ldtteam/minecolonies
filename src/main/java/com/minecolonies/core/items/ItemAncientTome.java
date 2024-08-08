package com.minecolonies.core.items;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.items.ModDataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Class describing the Ancient Tome item.
 */
public class ItemAncientTome extends AbstractItemMinecolonies
{
    /**
     * Sets the name, creative tab, and registers the Ancient Tome item.
     *
     * @param properties the properties.
     */
    public ItemAncientTome(final Properties properties)
    {
        super("ancienttome", properties.stacksTo(STACKSIZE));
    }

    @Override
    public void inventoryTick(final ItemStack stack, final Level worldIn, final Entity entityIn, final int itemSlot, final boolean isSelected)
    {
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
        if (!worldIn.isClientSide)
        {
            final IColony colony = IColonyManager.getInstance().getClosestColony(worldIn, entityIn.blockPosition());
            if (colony != null)
            {
                stack.set(ModDataComponents.BOOL_COMPONENT, new ModDataComponents.Bool(colony.getRaiderManager().willRaidTonight()));
            }
        }
    }

    public boolean isFoil(final ItemStack stack)
    {
        return stack.getOrDefault(ModDataComponents.BOOL_COMPONENT, ModDataComponents.Bool.EMPTY).does();
    }
}
