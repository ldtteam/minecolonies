package com.minecolonies.coremod.compatibility.jade;

import com.minecolonies.api.colony.IVisitorViewData;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Shows information about a visitor's recruitment cost.
 */
public class VisitorRecruitmentComponentProvider implements IEntityComponentProvider
{
    private static final ResourceLocation UID = new ResourceLocation(MOD_ID, "visitor_cost");

    private static final VisitorRecruitmentComponentProvider INSTANCE = new VisitorRecruitmentComponentProvider();

    public static VisitorRecruitmentComponentProvider getInstance()
    {
        return INSTANCE;
    }

    @NotNull
    @Override
    public ResourceLocation getUid()
    {
        return UID;
    }

    @Override
    public int getDefaultPriority()
    {
        return -1000;
    }

    @Override
    public void appendTooltip(@NotNull final ITooltip tooltip,
                              @NotNull final EntityAccessor entityAccessor,
                              @NotNull final IPluginConfig pluginConfig)
    {
        if (entityAccessor.getEntity() instanceof final AbstractEntityCitizen citizen)
        {
            if (citizen.getCitizenDataView() instanceof final IVisitorViewData visitor)
            {
                final List<IElement> elements = new ArrayList<>();
                elements.add(tooltip.getElementHelper().text(Component.translatable("com.minecolonies.coremod.jade.visitor_cost")));
                elements.add(tooltip.getElementHelper().spacer(4, 1));
                elements.add(tooltip.getElementHelper().smallItem(visitor.getRecruitCost()));
                tooltip.add(elements);
            }
        }
    }
}
