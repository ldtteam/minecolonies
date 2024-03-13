package com.minecolonies.core.colony.workorders.view;

import com.minecolonies.api.colony.buildings.views.IBuildingView;
import net.minecraft.network.chat.Component;

/**
 * The client side representation for a work order that the builder can take to build mineshafts.
 */
public class WorkOrderMinerView extends AbstractWorkOrderView
{
    @Override
    public Component getDisplayName()
    {
        return Component.translatableEscape(getTranslationKey());
    }

    @Override
    public boolean shouldShowIn(IBuildingView view)
    {
        return false;
    }
}
