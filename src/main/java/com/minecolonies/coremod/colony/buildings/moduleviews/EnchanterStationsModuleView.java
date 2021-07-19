package com.minecolonies.coremod.colony.buildings.moduleviews;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.modules.EnchanterStationModuleWindow;
import com.minecolonies.coremod.network.messages.server.colony.building.enchanter.EnchanterWorkerSetMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EnchanterStationsModuleView extends AbstractBuildingModuleView
{
    /**
     * List of buildings the enchanter gathers experience from.
     */
    private List<BlockPos> buildingToGatherFrom = new ArrayList<>();

    @Override
    public void deserialize(@NotNull final PacketBuffer buf)
    {
        final int size = buf.readInt();
        buildingToGatherFrom.clear();
        for (int i = 0; i < size; i++)
        {
            buildingToGatherFrom.add(buf.readBlockPos());
        }
    }

    /**
     * Getter for the list.
     *
     * @return the list.
     */
    public List<BlockPos> getBuildingsToGatherFrom()
    {
        return buildingToGatherFrom;
    }

    /**
     * Add a new worker to gather xp from.
     *
     * @param blockPos the pos of the building.
     */
    public void addWorker(final BlockPos blockPos)
    {
        buildingToGatherFrom.add(blockPos);
        Network.getNetwork().sendToServer(new EnchanterWorkerSetMessage(buildingView, blockPos, true));
    }

    /**
     * Remove a worker to stop gathering from.
     *
     * @param blockPos the pos of that worker.
     */
    public void removeWorker(final BlockPos blockPos)
    {
        buildingToGatherFrom.remove(blockPos);
        Network.getNetwork().sendToServer(new EnchanterWorkerSetMessage(buildingView, blockPos, false));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Window getWindow()
    {
        return new EnchanterStationModuleWindow(buildingView, this);
    }

    @Override
    public String getIcon()
    {
        return "entity";
    }

    @Override
    public String getDesc()
    {
        return "com.minecolonies.gui.workerhuts.enchanter.workers";
    }
}
