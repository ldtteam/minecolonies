package com.minecolonies.items;

import com.minecolonies.MineColonies;
import com.minecolonies.achievements.ModAchievements;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class ItemBuildTool extends AbstractItemMinecolonies
{
    public ItemBuildTool()
    {
        super("scepterGold");
        setMaxStackSize(1);
    }

    @Override
    public boolean onItemUse(ItemStack stack, @NotNull EntityPlayer playerIn, @NotNull World worldIn, @NotNull BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (!worldIn.isRemote)
        {
            return false;
        }

        playerIn.triggerAchievement(ModAchievements.achievementWandOfbuilding);
        MineColonies.proxy.openBuildToolWindow(pos.offset(side));
        return false;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, @NotNull World world, EntityPlayer player)
    {
        if (world.isRemote)
        {
            MineColonies.proxy.openBuildToolWindow(null);
        }

        return stack;
    }
}
