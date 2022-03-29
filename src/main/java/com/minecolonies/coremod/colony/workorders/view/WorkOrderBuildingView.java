package com.minecolonies.coremod.colony.workorders.view;

import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

public class WorkOrderBuildingView extends AbstractWorkOrderView
{
    private String customBuildingName;

    @Override
    public ITextComponent getDisplayName()
    {
        ITextComponent nameComponent;
        if (!customBuildingName.isEmpty())
        {
            nameComponent = new StringTextComponent(customBuildingName);
        }
        else
        {
            nameComponent = new TranslationTextComponent(getWorkOrderName());
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
