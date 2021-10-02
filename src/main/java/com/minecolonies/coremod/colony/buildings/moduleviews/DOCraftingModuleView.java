package com.minecolonies.coremod.colony.buildings.moduleviews;

import com.minecolonies.coremod.client.gui.modules.DOCraftingWindow;

/**
 * Client side representation of the DO architects cutter crafting module.
 */
public class DOCraftingModuleView extends CraftingModuleView
{
    public void openCraftingGUI()
    {
        new DOCraftingWindow(buildingView, this).open();
    }
}
