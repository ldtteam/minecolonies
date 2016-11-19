package com.minecolonies.items;

import com.minecolonies.creativetab.ModCreativeTabs;
import com.minecolonies.util.BlockPosUtil;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.StructureWrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class ItemScanTool extends AbstractItemMinecolonies
{
    public ItemScanTool()
    {
        super("scepterSteel");

        super.setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setMaxStackSize(1);
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (!stack.hasTagCompound())
        {
            stack.setTagCompound(new NBTTagCompound());
        }
        NBTTagCompound compound = stack.getTagCompound();

        if (!compound.hasKey("pos1"))
        {
            BlockPosUtil.writeToNBT(compound, "pos1", pos);
            if (worldIn.isRemote)
            {
                LanguageHandler.sendPlayerLocalizedMessage(playerIn, "item.scepterSteel.point");
            }
            return EnumActionResult.SUCCESS;
        }
        else if (!compound.hasKey("pos2"))
        {
            @NotNull BlockPos pos1 = BlockPosUtil.readFromNBT(compound, "pos1");
            @NotNull BlockPos pos2 = pos;
            if (pos2.distanceSq(pos1) > 0)
            {
                BlockPosUtil.writeToNBT(compound, "pos2", pos2);
                if (worldIn.isRemote)
                {
                    LanguageHandler.sendPlayerLocalizedMessage(playerIn, "item.scepterSteel.point2");
                }
                return EnumActionResult.SUCCESS;
            }
            if (worldIn.isRemote)
            {
                LanguageHandler.sendPlayerLocalizedMessage(playerIn, "item.scepterSteel.samePoint");
            }
            return EnumActionResult.FAIL;
        }
        else
        {
            @NotNull BlockPos pos1 = BlockPosUtil.readFromNBT(compound, "pos1");
            @NotNull BlockPos pos2 = BlockPosUtil.readFromNBT(compound, "pos2");
            if (!worldIn.isRemote)
            {
                StructureWrapper.saveStructure(worldIn, pos1, pos2, playerIn);
            }
            compound.removeTag("pos1");
            compound.removeTag("pos2");
            return EnumActionResult.SUCCESS;
        }
    }
}
