package com.jlgm.test.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class ItemBuildPreview extends Item{
	
	@Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand){
		if(playerIn.isSneaking()){
			//TESTMain.instance.pinnedPos = null;
			//TESTMain.instance.structure = null;
			playerIn.addChatMessage(new TextComponentString("Structure unpinned"));
	        return new ActionResult(EnumActionResult.SUCCESS, itemStackIn);
		}
        return new ActionResult(EnumActionResult.PASS, itemStackIn);
    }
	
	@Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
		if(!playerIn.isSneaking()){
			//TESTMain.instance.pinnedPos = pos.up();
			//TESTMain.instance.structure = new Structure(null, "endcity/ship", new PlacementSettings().setRotation(StructPrevMath.getRotationFromYaw()).setMirror(Mirror.NONE));
			playerIn.addChatMessage(new TextComponentString("Structure pinned at X:" + pos.getX() + ", Y:" + pos.getY() + 1 + ", Z:" + pos.getZ()));
	        return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.FAIL;
    }
}
