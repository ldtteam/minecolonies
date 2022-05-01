package com.minecolonies.api.colony.buildings.modules;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Default interface for all client side building modules.
 */
public interface IBuildingModuleView
{
    /**
     * Deserialize the data on the client side.
     * @param buf the buffer to read it from.
     */
    void deserialize(@NotNull final PacketBuffer buf);

    /**
     * Set the building view of this module view.
     * @param buildingView the building view to set.
     * @return this module itself.
     */
    IBuildingModuleView setBuildingView(final IBuildingView buildingView);

    /**
     * Whether this module appears as a GUI page.
     * @return true to show the GUI page.
     */
    default boolean isPageVisible() { return true; }

    /**
     * Get the matching window for the module.
     * @return the window.
     */
    @OnlyIn(Dist.CLIENT)
    Window getWindow();

    /**
     * Get the icon string for the module view.
     * @return the icon identifier.
     */
    String getIcon();

    /**
     * Get the lang string for the title.
     * @return the lang string.
     */
    String getDesc();

    /**
     * Get the colony view the module belongs to.
     * @return the colony view.
     */
    IColonyView getColony();

    /**
     * Get the building view to this.
     * @return the building view.
     */
    IBuildingView getBuildingView();
}
