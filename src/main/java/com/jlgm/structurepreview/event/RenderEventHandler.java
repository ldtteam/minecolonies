package com.jlgm.structurepreview.event;
/**
 * Class based on the work by Maruohon
 * https://github.com/maruohon/placementpreview
 */

import com.jlgm.structurepreview.helpers.Structure;
import com.jlgm.structurepreview.helpers.TESTMath;
import com.jlgm.test.item.TESTItem;
import com.jlgm.test.main.TESTMain;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RenderEventHandler{

	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event){
		if((Minecraft.getMinecraft().thePlayer.getHeldItemMainhand() != null && Minecraft.getMinecraft().thePlayer.getHeldItemMainhand().getItem() == TESTItem.buildPreview) || TESTMain.instance.pinnedPos != null){
			Structure structure;
			if(TESTMain.instance.structure == null){
				structure = new Structure(null, "endcity/ship", new PlacementSettings().setRotation(TESTMath.getRotationFromYaw()).setMirror(Mirror.NONE));
			}else{
				structure = TESTMain.instance.structure;
			}
			
			BlockPos structurePreviewPos;
			if(TESTMain.instance.pinnedPos == null){
				structurePreviewPos = Minecraft.getMinecraft().objectMouseOver.getBlockPos().add(0, 1, 0);
			}else{
				structurePreviewPos = TESTMain.instance.pinnedPos;
			}
			structure.renderStructure(structurePreviewPos, Minecraft.getMinecraft().theWorld, Minecraft.getMinecraft().thePlayer, event.getPartialTicks());
		}
	}
}
