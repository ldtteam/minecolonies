package com.minecolonies.coremod.colony.buildings.moduleviews;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.gui.modules.WorkOrderModuleWindow;
import net.minecraft.item.Item;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Client side version of the abstract class for building that handle workorders.
 */
public class WorkOrderListModuleView extends AbstractBuildingModuleView
{
    /**
     * The tool of the worker.
     */
    public WorkOrderListModuleView()
    {
        super();
    }

    @Override
    public String getDesc()
    {
        return "com.minecolonies.coremod.gui.townhall.workorders";
    }

    @Override
    public void deserialize(@NotNull final PacketBuffer buf)
    {

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Window getWindow()
    {
        return new WorkOrderModuleWindow(Constants.MOD_ID + ":gui/layouthuts/layoutworkorders.xml", buildingView, this);
    }

    @Override
    public String getIcon()
    {
        return "info";
    }
}
