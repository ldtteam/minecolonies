package com.minecolonies.items;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.creativetab.ModCreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Guard Scepter Item class. Used to give tasks to guards.
 */
public class ItemScepterGuard extends AbstractItemMinecolonies
{
    public enum Action
    {
        PATROL,
        GUARD
    }

    /**
     * Caliper constructor. Sets max stack to 1, like other tools.
     */
    public ItemScepterGuard()
    {
        super("scepterGuard");
        super.setCreativeTab(ModCreativeTabs.MINECOLONIES);

        maxStackSize = 1;
    }



    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        // if client world, do nothing
        if (worldIn.isRemote)
        {
            return EnumActionResult.FAIL;
        }

        Colony colony = ColonyManager.getColony(worldIn, pos);


        return EnumActionResult.SUCCESS;
    }

}
