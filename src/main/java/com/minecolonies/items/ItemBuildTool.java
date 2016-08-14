package com.minecolonies.items;

import com.minecolonies.MineColonies;
import com.minecolonies.achievements.ModAchievements;

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
    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if(!worldIn.isRemote)
        {
            return false;
        }

        switch(side)
        {
        case DOWN:
            pos.down();
            break;
        case UP:
            pos.up();
            break;
        case NORTH:
            pos.north();
            break;
        case SOUTH:
            pos.south();
            break;
        case WEST:
            pos.west();
            break;
        case EAST:
            pos.east();
            break;
        }
        
        playerIn.triggerAchievement(ModAchievements.achievementWandOfbuilding);
        MineColonies.proxy.openBuildToolWindow(pos);
        return false;
    }
}
