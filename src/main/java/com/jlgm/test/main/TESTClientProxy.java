package com.jlgm.test.main;

import com.jlgm.structurepreview.event.RenderEventHandler;
import com.jlgm.test.item.TESTItem;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

public class TESTClientProxy extends TESTCommonProxy{

	@Override
	public void preInit(FMLPreInitializationEvent preInitEvent){
		super.preInit(preInitEvent);
	}

	@Override
	public void init(FMLInitializationEvent initEvent){
		super.init(initEvent);
		TESTItem.renderItem();
	}

	@Override
	public void postInit(FMLPostInitializationEvent postInitEvent){
		super.postInit(postInitEvent);
	}
}
