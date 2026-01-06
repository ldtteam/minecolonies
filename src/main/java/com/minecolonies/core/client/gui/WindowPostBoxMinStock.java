package com.minecolonies.core.client.gui;

import com.minecolonies.api.colony.buildings.modules.IMinimumStockModuleView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.modules.building.MinimumStockModuleWindow;
import net.minecraft.resources.ResourceLocation;

/**
 * BOWindow for the request PostBox GUI.
 */
public class WindowPostBoxMinStock extends MinimumStockModuleWindow
{
    /**
     * Create the postBox GUI.
     *
     * @param moduleView the module view.
     */
    public WindowPostBoxMinStock(final IMinimumStockModuleView moduleView)
    {
        super(moduleView, new ResourceLocation(Constants.MOD_ID, "gui/windowpostboxminstock.xml"));
        WindowPostBoxMain.registerPostboxTabs(this, moduleView.getBuildingView());
    }

    @Override
    protected boolean shouldRenderDefaultSidebar()
    {
        return false;
    }
}
