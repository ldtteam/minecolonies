package com.jlgm.test.item;

import com.jlgm.test.lib.TESTConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class TESTItem{
	
	public static Item buildPreview;
	public static Item buildScanner;
	
	public static void main(Configuration config){
		initialiseItem();
	}

	public static void initialiseItem(){
		buildPreview = new ItemBuildPreview().setUnlocalizedName("buildPreview").setCreativeTab(CreativeTabs.TOOLS);
		buildScanner = new ItemBuildScanner().setUnlocalizedName("buildScanner").setCreativeTab(CreativeTabs.TOOLS);
	}

	public static void registerItem(){
		GameRegistry.register(buildPreview.setRegistryName("buildPreview"));
		GameRegistry.register(buildScanner.setRegistryName("buildScanner"));
	}

	public static void renderItem(){
		ItemModelMesher modelMesherItem = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
		modelMesherItem.register(buildPreview, 0, new ModelResourceLocation(TESTConstants.MODID + ":" + "buildPreview", "inventory"));
		modelMesherItem.register(buildScanner, 0, new ModelResourceLocation(TESTConstants.MODID + ":" + "buildScanner", "inventory"));
	}
}
