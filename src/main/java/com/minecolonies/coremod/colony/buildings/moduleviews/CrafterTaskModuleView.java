package com.minecolonies.coremod.colony.buildings.moduleviews;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.gui.WindowHutCrafterTaskModule;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

/**
 * Crafter task module to display tasks in the UI.
 */
public class CrafterTaskModuleView extends AbstractBuildingModuleView
{
    @Override
    public void deserialize(@NotNull final PacketBuffer buf)
    {

    }

    @Override
    public Window getWindow()
    {
        return new WindowHutCrafterTaskModule(buildingView,Constants.MOD_ID + ":gui/layouthuts/layouttasklist.xml");
    }

    @Override
    public String getIcon()
    {
        return null;
    }

    @Override
    public String getDesc()
    {
        return null;
    }
}
