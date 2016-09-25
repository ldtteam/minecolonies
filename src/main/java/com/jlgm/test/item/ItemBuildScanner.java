package com.jlgm.test.item;

import com.jlgm.structurepreview.helpers.StructPrevMath;
import com.jlgm.structurepreview.helpers.Structure;
import com.jlgm.test.main.TESTMain;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.PlacementSettings;

public class ItemBuildScanner extends Item{
	
	@Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
		if(!playerIn.isSneaking()){
			TESTMain.instance.firstPos = pos;
			playerIn.addChatMessage(new TextComponentString("First pos set at X:" + pos.getX() + ", Y:" + pos.getY() + 1 + ", Z:" + pos.getZ()));
	        return EnumActionResult.SUCCESS;
		}else{
			TESTMain.instance.secondPos = pos;
			playerIn.addChatMessage(new TextComponentString("Second pos set at X:" + pos.getX() + ", Y:" + pos.getY() + 1 + ", Z:" + pos.getZ()));
	        return EnumActionResult.SUCCESS;
		}
    }
}
