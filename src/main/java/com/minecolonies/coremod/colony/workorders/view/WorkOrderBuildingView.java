package com.minecolonies.coremod.colony.workorders.view;

public class WorkOrderBuildingView extends AbstractWorkOrderView
{
    @Override
    public boolean shouldShowInTownHall()
    {
        return true;
    }

    @Override
    public boolean shouldShowInBuilder()
    {
        return true;
    }
}
