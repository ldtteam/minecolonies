package com.minecolonies.coremod.colony.workorders.view;

import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class WorkOrderDecorationView extends AbstractWorkOrderView
{
    @Override
    public ITextComponent getDisplayName()
    {
        return getOrderTypePrefix(new TranslationTextComponent(getWorkOrderName()));
    }

    private ITextComponent getOrderTypePrefix(ITextComponent nameComponent)
    {
        switch (this.getWorkOrderType())
        {
            case BUILD:
                return new TranslationTextComponent(TranslationConstants.BUILDER_ACTION_BUILDING, nameComponent);
            case UPGRADE:
                return new TranslationTextComponent(TranslationConstants.BUILDER_ACTION_UPGRADING, nameComponent, getCurrentLevel(), getTargetLevel());
            case REPAIR:
                return new TranslationTextComponent(TranslationConstants.BUILDER_ACTION_REPAIRING, nameComponent);
            case REMOVE:
                return new TranslationTextComponent(TranslationConstants.BUILDER_ACTION_REMOVING, nameComponent);
            default:
                return nameComponent;
        }
    }

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
