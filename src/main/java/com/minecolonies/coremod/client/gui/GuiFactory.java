package com.minecolonies.coremod.client.gui;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

/**
 * This class is required for Forge to initialize the in-game GUI.
 */
public class GuiFactory implements IModGuiFactory 
{
    @Override
    public void initialize(Minecraft minecraftInstance) 
    { }
	
	@Override
	public Class<? extends GuiScreen> mainConfigGuiClass() 
	{
	    return ConfigGUI.class;
	}
	
	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() 
	{
	    return null;
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) 
	{
	    return null;
	}
}