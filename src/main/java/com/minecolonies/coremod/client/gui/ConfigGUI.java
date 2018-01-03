package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.configuration.ConfigurationHandler;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the GUI Screen for the in-game GUI.
 */
public class ConfigGUI extends GuiConfig 
{
    /**
     * GuiConfig constructor that will use ConfigChangedEvent when editing is concluded.
     * 
     * @param parentScreen the parent GuiScreen object.
     */
    public ConfigGUI(GuiScreen parentScreen) 
    {
        /* The parentScreen. */
        super(parentScreen,
                /* The configElements. */ 
                getConfigElement(),
                /* The modID. */
                Constants.MOD_ID,
                /* The allRequireWorldRestart argument. */
                true,
                /* The allRequireMcRestart argument. */
                true,
                /* The title of the GUI. */
                I18n.format("com.minecolonies.configgui.title"));
    }
    
    /** Compiles a list of config elements. */
    private static List<IConfigElement> getConfigElement() 
    {
        final List<IConfigElement> list = new ArrayList<>();
        
        // category, name, tooltip (same as the comment found in the config file itself)
        list.add(categoryElement(ConfigurationHandler.CATEGORY_GAMEPLAY, "gameplay", ""));
        list.add(categoryElement(ConfigurationHandler.CATEGORY_NAMES, "names", ""));
        list.add(categoryElement(ConfigurationHandler.CATEGORY_PATHFINDING, "pathfinding", ""));
        
        return list;
    }
    
    /** Creates a button linking to another screen where all options of the category are available. */
    private static IConfigElement categoryElement(String category, String name, String tooltipKey) 
    {
        return new DummyConfigElement.DummyCategoryElement(name, tooltipKey, 
                new ConfigElement(ConfigurationHandler.getConfiguration().getCategory(category)).getChildElements());
    }
}
