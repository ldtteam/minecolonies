package com.jlgm.test.main;

import com.jlgm.structurepreview.commands.CommandSaveStructure;
import com.jlgm.structurepreview.helpers.Structure;
import com.jlgm.test.lib.TESTConstants;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = TESTConstants.MODID,
	name = TESTConstants.NAME,
	version = TESTConstants.VERSION,
	acceptedMinecraftVersions = TESTConstants.ACCEPTEDMINECRAFTVERSIONS)

public class TESTMain{
	
	public BlockPos pinnedPos = null;
	public Structure structure = null;
	public BlockPos firstPos = null;
	public BlockPos secondPos = null;

	@SidedProxy(clientSide = TESTConstants.CLIENT_PROXY, serverSide = TESTConstants.SERVER_PROXY)
	public static TESTCommonProxy proxy;
	@Instance(TESTConstants.MODID)
	public static TESTMain instance;

	@EventHandler
	public static void PreInit(FMLPreInitializationEvent preInitEvent){
		proxy.preInit(preInitEvent);
	}

	@EventHandler
	public static void Init(FMLInitializationEvent initEvent){
		proxy.init(initEvent);
	}

	@EventHandler
	public static void PostInit(FMLPostInitializationEvent postInitEvent){
		proxy.postInit(postInitEvent);
	}
	
	@EventHandler
	public void ServerLoad(FMLServerStartingEvent event){
		event.registerServerCommand(new CommandSaveStructure());
	}
}
