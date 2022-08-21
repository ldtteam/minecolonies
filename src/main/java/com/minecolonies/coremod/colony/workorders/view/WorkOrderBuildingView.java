package com.minecolonies.coremod.colony.workorders.view;

import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.buildings.workerbuildings.ITownHallView;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * The client side representation for a work order that the builder can take to build buildings.
 */
public class WorkOrderBuildingView extends AbstractWorkOrderView
{
    /**
     * The custom name of a building.
     */
    private String customBuildingName;

    /**
     * The custom name of a parent building, if any.
     */
    private String customParentBuildingName;

    /**
     * The translation key of a parent building, if any.
     */
    private String parentTranslationKey;

    @Override
    public Component getDisplayName()
    {
        Component buildingComponent = customBuildingName.isEmpty() ? Component.translatable(getTranslationKey()) : Component.literal(customBuildingName);

        Component nameComponent;
        if (parentTranslationKey.isEmpty())
        {
            nameComponent = buildingComponent;
        }
        else
        {
            Component parentComponent =
              customParentBuildingName.isEmpty() ? Component.translatable(parentTranslationKey) : Component.literal(customParentBuildingName);
            nameComponent = Component.translatable("%s / %s", parentComponent, buildingComponent);
        }
        return getOrderTypePrefix(nameComponent);
    }

    private Component getOrderTypePrefix(Component nameComponent)
    {
        switch (this.getWorkOrderType())
        {
            case BUILD:
                return Component.translatable(TranslationConstants.BUILDER_ACTION_BUILDING, nameComponent);
            case UPGRADE:
                return Component.translatable(TranslationConstants.BUILDER_ACTION_UPGRADING, nameComponent, getCurrentLevel(), getTargetLevel());
            case REPAIR:
                return Component.translatable(TranslationConstants.BUILDER_ACTION_REPAIRING, nameComponent);
            case REMOVE:
                return Component.translatable(TranslationConstants.BUILDER_ACTION_REMOVING, nameComponent);
            default:
                return nameComponent;
        }
    }

    @Override
    public void deserialize(@NotNull FriendlyByteBuf buf)
    {
        super.deserialize(buf);
        customBuildingName = buf.readUtf(32767);
        customParentBuildingName = buf.readUtf(32767);
        parentTranslationKey = buf.readUtf(32767);
    }

    @Override
    public boolean shouldShowIn(IBuildingView view)
    {
        return view instanceof ITownHallView || view instanceof BuildingBuilder.View;
    }
}
