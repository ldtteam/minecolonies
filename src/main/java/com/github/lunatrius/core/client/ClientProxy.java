package com.github.lunatrius.core.client;

import com.github.lunatrius.core.CommonProxy;
import cpw.mods.fml.common.FMLCommonHandler;

public class ClientProxy extends CommonProxy {
	@Override
	public void registerTickers() {
		super.registerTickers();

		FMLCommonHandler.instance().bus().register(new VersionTicker());
	}
}
