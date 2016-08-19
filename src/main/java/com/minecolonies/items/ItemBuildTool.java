package com.minecolonies.items;

import com.minecolonies.MineColonies;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class ItemBuildTool extends AbstractItemMinecolonies
{
    public ItemBuildTool()
    {
        super();
        setMaxStackSize(1);
    }

    @Override
    public String getName()
    {
        return "scepterGold";
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (world.isRemote)
        {
            MineColonies.proxy.openBuildToolWindow(null);
        }

        return stack;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if(!worldIn.isRemote)
        {
            return false;
        }

        MineColonies.proxy.openBuildToolWindow(pos.offset(side));
        return false;
    }
}
