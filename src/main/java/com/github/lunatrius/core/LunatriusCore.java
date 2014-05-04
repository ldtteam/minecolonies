package com.github.lunatrius.core;

import com.github.lunatrius.core.config.Config;
import com.github.lunatrius.core.lib.Reference;
import com.github.lunatrius.core.version.VersionChecker;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

//@Mod(modid = Reference.MODID, name = Reference.NAME)
public class LunatriusCore {
	@SidedProxy(serverSide = Reference.PROXY_COMMON, clientSide = Reference.PROXY_CLIENT)
	public static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		VersionChecker.registerMod(event.getModMetadata());

		Reference.logger = event.getModLog();
		Reference.config = new Config(event.getSuggestedConfigurationFile());
		Reference.config.save();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		if (Reference.config.checkForUpdates()) {
			VersionChecker.startVersionCheck();
		}

		proxy.registerTickers();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}
}
