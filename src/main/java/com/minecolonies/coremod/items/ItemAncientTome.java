package com.minecolonies.coremod.items;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Class describing the Ancient Tome item.
 */
public class ItemAncientTome extends AbstractItemMinecolonies
{
    private boolean raidWillHappen = true;

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
        final Colony colony = ColonyManager.getClosestColony(worldIn, entityIn.getPosition());
        raidWillHappen = colony != null && colony.hasWillRaidTonight();
    }

    @SideOnly(Side.CLIENT)
    public boolean hasEffect(final ItemStack stack)
    {
        return raidWillHappen;
    }
}
