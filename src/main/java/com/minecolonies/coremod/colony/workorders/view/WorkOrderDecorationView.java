package com.minecolonies.coremod.colony.workorders.view;

import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.buildings.workerbuildings.ITownHallView;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * The client side representation for a work order that the builder can take to build decorations.
 */
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
    public boolean shouldShowIn(IBuildingView view)
    {
        return view instanceof ITownHallView || view instanceof BuildingBuilder.View;
    }
}
