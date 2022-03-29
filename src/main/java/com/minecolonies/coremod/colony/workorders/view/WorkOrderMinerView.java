package com.minecolonies.coremod.colony.workorders.view;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class WorkOrderMinerView extends AbstractWorkOrderView
{
    @Override
    public ITextComponent getDisplayName()
    {
        return new TranslationTextComponent(getWorkOrderName());
    }

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
