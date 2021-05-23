package com.minecolonies.coremod.colony.buildings.moduleviews;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.gui.modules.ToolModuleWindow;
import net.minecraft.item.Item;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Client side version of the abstract class for all buildings which allows to select tools.
 */
public class ToolModuleView extends AbstractBuildingModuleView
{
    /**
     * The worker specific tool.
     */
    private final Item tool;

    /**
     * The tool of the worker.
     * @param tool the item.
     */
    public ToolModuleView(final Item tool)
    {
        super();
        this.tool = tool;
    }

    @Override
    public String getDesc()
    {
        return "com.minecolonies.coremod.gui.workerhuts.tools";
    }

    @Override
    public void deserialize(@NotNull final PacketBuffer buf)
    {

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Window getWindow()
    {
        return new ToolModuleWindow(Constants.MOD_ID + ":gui/layouthuts/layouttool.xml", buildingView, this);
    }

    @Override
    public String getIcon()
    {
        return tool.getRegistryName().getPath();
    }

    /**
     * Get the correct tool.
     * @return the tool to give.
     */
    public Item getTool()
    {
        return tool;
    }
}
