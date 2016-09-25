package com.jlgm.test.main;

import com.jlgm.structurepreview.event.RenderEventHandler;
import com.jlgm.test.item.TESTItem;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

public class TESTCommonProxy{

	public void preInit(FMLPreInitializationEvent preInitEvent){
		Configuration config = new Configuration(preInitEvent.getSuggestedConfigurationFile());
		config.load();

		config.save();

		TESTItem.main(config);
	}

	public void init(FMLInitializationEvent initEvent){
		TESTItem.registerItem();
		MinecraftForge.EVENT_BUS.register(new RenderEventHandler());
	}

	public void postInit(FMLPostInitializationEvent postInitEvent){

	}
}
