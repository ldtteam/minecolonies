package com.minecolonies.coremod.items;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Class handling the AncientTome item.
 */
public class ItemAncientTome extends AbstractItemMinecolonies
{
    private static final int MAX_STACK_SIZE = 64;

    private boolean raidWillHappen = true;

    /**
     * Sets the name, creative tab, and registers the Ancient Tome item.
     */
    public ItemAncientTome()
    {
        super("ancientTome");
        super.setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setMaxStackSize(MAX_STACK_SIZE);
    }

    @Override
    public void onUpdate(final ItemStack stack, final World worldIn, final Entity entityIn, final int itemSlot, final boolean isSelected)
    {
        super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
        final Colony colony = ColonyManager.getClosestColony(worldIn, entityIn.getPosition());
        raidWillHappen = colony != null && colony.isWillRaid();
    }

    @SideOnly(Side.CLIENT)
    public boolean hasEffect(final ItemStack stack)
    {
        return raidWillHappen;
    }
}
