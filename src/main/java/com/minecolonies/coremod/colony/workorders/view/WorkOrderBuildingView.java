package com.minecolonies.coremod.colony.workorders.view;

import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.buildings.workerbuildings.ITownHallView;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

public class WorkOrderBuildingView extends AbstractWorkOrderView
{
    private String customBuildingName;
    private String customParentBuildingName;
    private String parentTranslationKey;

    @Override
    public ITextComponent getDisplayName()
    {
        ITextComponent buildingComponent = customBuildingName.isEmpty() ? new TranslationTextComponent(getWorkOrderName()) : new StringTextComponent(customBuildingName);

        ITextComponent nameComponent;
        if (parentTranslationKey.isEmpty())
        {
            nameComponent = buildingComponent;
        }
        else
        {
            ITextComponent parentComponent =
              customParentBuildingName.isEmpty() ? new TranslationTextComponent(parentTranslationKey) : new StringTextComponent(customParentBuildingName);
            nameComponent = new TranslationTextComponent("%s / %s", parentComponent, buildingComponent);
        }
        return getOrderTypePrefix(nameComponent);
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
    public void deserialize(@NotNull PacketBuffer buf)
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
