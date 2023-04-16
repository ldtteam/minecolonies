package com.minecolonies.coremod.compatibility.jade;

import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IVisitorViewData;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
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
 * Shows information about a colonist (citizen or visitor).
 */
public class ColonistComponentProvider implements IEntityComponentProvider
{
    private static final ResourceLocation UID = new ResourceLocation(MOD_ID, "colonist");
    private static final ColonistComponentProvider INSTANCE = new ColonistComponentProvider();

    public static ColonistComponentProvider getInstance()
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
        return -2000;
    }

    @Override
    public void appendTooltip(@NotNull final ITooltip tooltip,
                              @NotNull final EntityAccessor entityAccessor,
                              @NotNull final IPluginConfig pluginConfig)
    {
        if (entityAccessor.getEntity() instanceof final AbstractEntityCitizen citizen)
        {
            final ICitizenDataView citizenData = citizen.getCitizenDataView();
            if (citizenData != null)
            {
                final List<IElement> elements = new ArrayList<>();

                String job = citizenData.getJob();
                if (job.isEmpty() && citizenData instanceof IVisitorViewData)
                {
                    job = "com.minecolonies.coremod.journeymap.visitor";
                }
                else if (job.isEmpty())
                {
                    job = "com.minecolonies.coremod.journeymap." + (citizenData.isChild() ? "child" : "unemployed");
                }

                elements.add(GenderElement.get(citizenData.isFemale()));
                elements.add(tooltip.getElementHelper().spacer(8, 1));
                elements.add(tooltip.getElementHelper().text(Component.translatable(job)).translate(new Vec2(0, 3.5F)));
                tooltip.add(elements);
            }
        }
    }
}
