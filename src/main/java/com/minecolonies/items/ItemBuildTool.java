package com.minecolonies.items;

import com.jlgm.structurepreview.helpers.StructPrevMath;
import com.jlgm.structurepreview.helpers.Structure;
import com.minecolonies.MineColonies;
import com.minecolonies.achievements.ModAchievements;
import com.minecolonies.creativetab.ModCreativeTabs;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import org.jetbrains.annotations.NotNull;

public class ItemBuildTool extends AbstractItemMinecolonies
{
    public ItemBuildTool()
    {
        super("scepterGold");

        super.setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setMaxStackSize(1);
    }

    @NotNull
    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        playerIn.addStat(ModAchievements.achievementWandOfbuilding);
        if (worldIn.isRemote)
        {
            MineColonies.proxy.openBuildToolWindow(pos.offset(facing));
        }

        return EnumActionResult.SUCCESS;
    }

    @NotNull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@NotNull ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        if (worldIn.isRemote)
        {
            MineColonies.proxy.openBuildToolWindow(null);
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, itemStackIn);
    }
}
