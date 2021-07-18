package com.minecolonies.coremod.colony.buildings.moduleviews;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.coremod.client.gui.modules.WindowMineGuardModule;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Miner guard assignment module.
 */
public class MinerGuardAssignModuleView extends AbstractBuildingModuleView
{
    @Override
    public void deserialize(@NotNull final PacketBuffer buf)
    {

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Window getWindow()
    {
        return new WindowMineGuardModule(buildingView);
    }

    @Override
    public String getIcon()
    {
        return "sword";
    }

    @Override
    public String getDesc()
    {
        return "com.minecolonies.coremod.gui.miner.guardassign";
    }
}
