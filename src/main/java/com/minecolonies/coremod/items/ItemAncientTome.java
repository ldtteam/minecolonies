package com.minecolonies.coremod.items;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.util.constant.NbtTagConstants;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Class describing the Ancient Tome item.
 */
public class ItemAncientTome extends AbstractItemMinecolonies
{
    /**
     * Sets the name, creative tab, and registers the Ancient Tome item.
     */
    public ItemAncientTome()
    {
        super("ancienttome");
        super.setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setMaxStackSize(STACKSIZE);
    }

    @Override
    public void onUpdate(final ItemStack stack, final World worldIn, final Entity entityIn, final int itemSlot, final boolean isSelected)
    {
        super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
        if (!worldIn.isRemote)
        {
            final IColony colony = IColonyManager.getInstance().getClosestColony(worldIn, entityIn.getPosition());
            if (stack.getTagCompound() == null)
            {
                stack.setTagCompound(new NBTTagCompound());
            }
            stack.getTagCompound().setBoolean(NbtTagConstants.TAG_RAID_WILL_HAPPEN, colony != null && colony.getRaiderManager().willRaidTonight());
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean hasEffect(final ItemStack stack)
    {
        return stack.getTagCompound() != null && stack.getTagCompound().hasKey(NbtTagConstants.TAG_RAID_WILL_HAPPEN) && stack.getTagCompound().getBoolean(NbtTagConstants.TAG_RAID_WILL_HAPPEN);
    }
}
