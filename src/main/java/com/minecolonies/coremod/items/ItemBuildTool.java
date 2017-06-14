package com.minecolonies.coremod.items;

import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.achievements.ModAchievements;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Class handling the buildTool item.
 */
public class ItemBuildTool extends AbstractItemMinecolonies
{
    /**
     * Instantiates the buildTool on load.
     */
    public ItemBuildTool()
    {
        super("scepterGold");

        super.setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setMaxStackSize(1);
    }

    @NotNull
    @Override
    public EnumActionResult onItemUse(
                                       final ItemStack stack,
                                       final EntityPlayer playerIn,
                                       final World worldIn,
                                       final BlockPos pos,
                                       final EnumHand hand,
                                       final EnumFacing facing,
                                       final float hitX,
                                       final float hitY,
                                       final float hitZ)
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
    public ActionResult<ItemStack> onItemRightClick(@NotNull final ItemStack itemStackIn, final World worldIn, final EntityPlayer playerIn, final EnumHand hand)
    {
        if (worldIn.isRemote)
        {
            MineColonies.proxy.openBuildToolWindow(null);
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, itemStackIn);
    }

    @Override
    public ItemStack getContainerItem(final ItemStack itemStack)
    {
        //we want to return the build tool when use for crafting
        if (ItemStackUtils.isEmpty(itemStack))
        {
            return ItemStackUtils.EMPTY;
        }
        return itemStack.copy();
    }

    @Override
    public boolean hasContainerItem(final ItemStack itemStack)
    {
        //we want to return the build tool when use for crafting
        return !ItemStackUtils.isEmpty(itemStack);
    }
}
