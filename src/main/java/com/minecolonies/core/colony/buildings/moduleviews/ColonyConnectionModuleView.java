package com.minecolonies.core.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.modules.ConnectionModuleWindow;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Client side version of colony connection module. Primarily a wrapper for the UI.
 */
public class ColonyConnectionModuleView extends AbstractBuildingModuleView
{
    /**
     * Constructor.
     */
    public ColonyConnectionModuleView()
    {
        super();
    }

    @Override
    public String getDesc()
    {
        return "com.minecolonies.core.gui.connections";
    }

    @Override
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BOWindow getWindow()
    {
        return new ConnectionModuleWindow(Constants.MOD_ID + ":gui/layouthuts/layoutcolonyconnection.xml", buildingView, this);
    }

    @Override
    public ResourceLocation getIconResourceLocation()
    {
        return new ResourceLocation(Constants.MOD_ID, "textures/gui/modules/connection.png");
    }
}
