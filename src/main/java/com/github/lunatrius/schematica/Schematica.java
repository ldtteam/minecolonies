package com.github.lunatrius.schematica;

import com.github.lunatrius.core.version.VersionChecker;
import com.github.lunatrius.schematica.config.Config;
import com.github.lunatrius.schematica.lib.Reference;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

//@Mod(modid = Reference.MODID, name = Reference.NAME)
public class Schematica {
	@Instance(Reference.MODID)
	public static Schematica instance;

	@SidedProxy(serverSide = Reference.PROXY_COMMON, clientSide = Reference.PROXY_CLIENT)
	public static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		VersionChecker.registerMod(event.getModMetadata());

		Reference.logger = event.getModLog();

		Reference.config = new Config(event.getSuggestedConfigurationFile());
		Reference.config.save();

		proxy.registerKeybindings();
		proxy.createFolders();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		try {
			proxy.registerEvents();
		} catch (Exception e) {
			Reference.logger.fatal("Could not initialize the mod!", e);
			throw new RuntimeException(e);
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}
}
