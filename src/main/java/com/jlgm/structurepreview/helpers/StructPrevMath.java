package com.jlgm.structurepreview.helpers;

import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;

public class StructPrevMath {
    public static Rotation getRotationFromYaw(){
    	EnumFacing facing = Minecraft.getMinecraft().thePlayer.getHorizontalFacing();
    	if(facing == EnumFacing.NORTH)
    		return Rotation.COUNTERCLOCKWISE_90;
    	if(facing == EnumFacing.SOUTH)
    		return Rotation.CLOCKWISE_90;
    	if(facing == EnumFacing.WEST)
    		return Rotation.CLOCKWISE_180;
    	if(facing == EnumFacing.EAST)
    		return Rotation.NONE;
    	return Rotation.NONE;
    }
}
