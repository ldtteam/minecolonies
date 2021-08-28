package com.minecolonies.coremod.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.Window;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.coremod.client.gui.modules.UniversityModuleWindow;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Crafter task module to display tasks in the UI.
 */
public class UniversityResearchModuleView extends AbstractBuildingModuleView
{
    @Override
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Window getWindow()
    {
        return new UniversityModuleWindow(buildingView);
    }

    @Override
    public String getIcon()
    {
        return "info";
    }

    @Override
    public String getDesc()
    {
        return "com.minecolonies.coremod.research.research";
    }
}
