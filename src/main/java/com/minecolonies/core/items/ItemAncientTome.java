package com.minecolonies.core.items;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.items.component.Bool;
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
                new Bool(colony.getRaiderManager().willRaidTonight()).writeToItemStack(stack);
                
            }
        }
    }

    @Override
    public boolean isFoil(final ItemStack stack)
    {
        return Bool.readFromItemStack(stack).does();
    }
}
