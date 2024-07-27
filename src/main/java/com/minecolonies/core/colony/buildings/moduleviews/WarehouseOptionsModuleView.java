package com.minecolonies.core.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.core.client.gui.modules.WarehouseOptionsModuleWindow;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Client side version of the warehouse module.
 */
public class WarehouseOptionsModuleView extends AbstractBuildingModuleView
{
    /**
     * Storage upgrade level.
     */
    private int storageUpgrade = 0;

    @Override
    public String getDesc()
    {
        return "com.minecolonies.coremod.gui.workerhuts.settings";
    }

    @Override
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {
        storageUpgrade = buf.readInt();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BOWindow getWindow()
    {
        return new WarehouseOptionsModuleWindow(buildingView, this);
    }

    @Override
    public String getIcon()
    {
        return "settings";
    }

    /**
     * Increment storage upgrade.
     */
    public void incrementStorageUpgrade()
    {
        storageUpgrade++;
    }

    /**
     * Get the current storage upgrade level.
     *
     * @return the level.
     */
    public int getStorageUpgradeLevel()
    {
        return storageUpgrade;
    }
}
