package com.minecolonies.coremod.colony.buildings.moduleviews;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.colony.workorders.WorkOrderView;
import com.minecolonies.coremod.client.gui.modules.WindowHutMinerModule;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Tuple;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
     * WorkOrders that are part of thi sminer.
     */
    private List<WorkOrderView> workOrders = new ArrayList<>();

    @Override
    public void deserialize(@NotNull final PacketBuffer buf)
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
            final WorkOrderView view = new WorkOrderView();
            view.deserialize(buf);
            workOrders.add(view);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Window getWindow()
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

    /**
     * Check if there is a workorder for this node already.
     * @param row the row of the level.
     * @return true if so.
     */
    public boolean doesWorkOrderExist(final int row)
    {
        final int depth = levelsInfo.get(row).getB();
        for (final WorkOrderView wo : workOrders)
        {
            if (wo.getDisplayName().contains("main") && wo.getPos().getY() == depth)
            {
                return true;
            }
        }
        return false;
    }
}
