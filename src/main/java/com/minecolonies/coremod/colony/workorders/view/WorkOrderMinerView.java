package com.minecolonies.coremod.colony.workorders.view;

public class WorkOrderMinerView extends AbstractWorkOrderView
{
    @Override
    public boolean shouldShowInTownHall()
    {
        return false;
    }

    @Override
    public boolean shouldShowInBuilder()
    {
        return false;
    }
}
