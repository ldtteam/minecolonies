package com.minecolonies.coremod.colony.buildings.moduleviews;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.gui.modules.WarehouseOptionsModuleWindow;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
    public void deserialize(@NotNull final PacketBuffer buf)
    {
        storageUpgrade = buf.readInt();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Window getWindow()
    {
        return new WarehouseOptionsModuleWindow(buildingView, this);
    }

    @Override
    public String getIcon()
    {
        return "info";
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
