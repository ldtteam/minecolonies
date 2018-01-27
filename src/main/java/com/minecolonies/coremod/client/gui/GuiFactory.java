package com.minecolonies.coremod.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

import java.util.Collections;
import java.util.Set;

/**
 * This class is required for Forge to initialize the in-game GUI.
 */
public class GuiFactory implements IModGuiFactory 
{
    @Override
    public void initialize(final Minecraft minecraftInstance)
    { 
        // We don't need this method for our purpose.
    }
    
    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() 
    {
        return ConfigGUI.class;
    }
    
    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() 
    {
        return Collections.emptySet();
    }
    
    /**
     * @deprecated This was never fully implemented and will be removed in the future.
     */
    @Override
    @Deprecated
    public RuntimeOptionGuiHandler getHandlerFor(final RuntimeOptionCategoryElement element)
    {
        return null;
    }
}
