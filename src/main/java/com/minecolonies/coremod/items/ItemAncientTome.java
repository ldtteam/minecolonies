package com.minecolonies.coremod.items;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.util.constant.NbtTagConstants;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
        super("ancienttome", properties.stacksTo(STACKSIZE).tab(ModCreativeTabs.MINECOLONIES));
    }

    @Override
    public void inventoryTick(final ItemStack stack, final World worldIn, final Entity entityIn, final int itemSlot, final boolean isSelected)
    {
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
        if (!worldIn.isClientSide)
        {
            final IColony colony = IColonyManager.getInstance().getClosestColony(worldIn, new BlockPos(entityIn.position()));
            final CompoundNBT tag = new CompoundNBT();

            if (colony != null)
            {
                tag.putBoolean(NbtTagConstants.TAG_RAID_WILL_HAPPEN, colony.getRaiderManager().willRaidTonight());
            }
            else
            {
                tag.putBoolean(NbtTagConstants.TAG_RAID_WILL_HAPPEN, false);
            }
            stack.setTag(tag);
        }
    }

    public boolean isFoil(final ItemStack stack)
    {
        return stack.getTag() != null && stack.getTag().getBoolean(NbtTagConstants.TAG_RAID_WILL_HAPPEN);
    }
}
