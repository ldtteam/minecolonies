package com.minecolonies.core.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.workorders.IWorkOrderView;
import com.minecolonies.core.client.gui.modules.WindowHutMinerModule;
import net.minecraft.network.RegistryFriendlyByteBuf;
import com.minecolonies.core.colony.workorders.view.WorkOrderMinerView;
import com.minecolonies.core.colony.workorders.AbstractWorkOrder;
import net.minecraft.util.Tuple;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Miner guard assignment module.
 */
public class MinerLevelManagementModuleView extends AbstractBuildingModuleView
{
    /**
     * The tuple of number of nodes and y depth per all levels.
     */
    public List<Tuple<Integer, Integer>> levelsInfo;

    /**
     * The level the miner currently works on.
     */
    public int current;

    /**
     * WorkOrders that are part of this miner.
     */
    private List<WorkOrderMinerView> workOrders = new ArrayList<>();

    @Override
    public void deserialize(@NotNull final RegistryFriendlyByteBuf buf)
    {
        current = buf.readInt();
        final int size = buf.readInt();

        levelsInfo = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
        {
            levelsInfo.add(i, new Tuple<>(buf.readInt(), buf.readInt()));
        }

        int woSize = buf.readInt();
        workOrders.clear();
        for (int i = 0; i < woSize; i++)
        {
            final IWorkOrderView woView = AbstractWorkOrder.createWorkOrderView(buf);
            if (woView instanceof WorkOrderMinerView)
            {
                workOrders.add((WorkOrderMinerView) woView);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public BOWindow getWindow()
    {
        return new WindowHutMinerModule(buildingView, this);
    }

    @Override
    public String getIcon()
    {
        return "info";
    }

    @Override
    public String getDesc()
    {
        return "com.minecolonies.coremod.gui.miner.levels";
    }

    @Override
    public boolean isPageVisible()
    {
        for (final WorkerBuildingModuleView workerBuildingModuleView : buildingView.getModuleViews(WorkerBuildingModuleView.class))
        {
            if (workerBuildingModuleView.getJobEntry() == ModJobs.quarrier.get() && !workerBuildingModuleView.getAssignedCitizens().isEmpty())
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if there is a workorder for this node already.
     *
     * @param row the row of the level.
     * @return true if so.
     */
    public boolean doesWorkOrderExist(final int row)
    {
        final int depth = levelsInfo.get(row).getB();
        for (final IWorkOrderView wo : workOrders)
        {
            if (wo.getDisplayName().getString().contains("main") && wo.getLocation().getY() == depth)
            {
                return true;
            }
        }
        return false;
    }
}
