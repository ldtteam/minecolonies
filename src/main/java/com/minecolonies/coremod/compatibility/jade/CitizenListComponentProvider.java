package com.minecolonies.coremod.compatibility.jade;

import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Lists the citizens assigned to this building.  This applies to both residences and workplaces.
 */
public class CitizenListComponentProvider implements IBlockComponentProvider
{
    private static final ResourceLocation UID = new ResourceLocation(MOD_ID, "citizens");
    private static final String CITIZENS_LIST = MOD_ID + ":citizens";
    private static final CitizenListComponentProvider INSTANCE = new CitizenListComponentProvider();

    public static CitizenListComponentProvider getInstance()
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
                              @NotNull final BlockAccessor blockAccessor,
                              @NotNull final IPluginConfig pluginConfig)
    {
        if (blockAccessor.getBlockEntity() instanceof final AbstractTileEntityColonyBuilding entity)
        {
            final IBuildingView building = entity.getBuildingView();
            if (building == null) { return; }

            for (final int citizenId : building.getAllAssignedCitizens())
            {
                final ICitizenDataView citizen = building.getColony().getCitizen(citizenId);
                final MutableComponent component = Component.empty();

                if (!citizen.getJob().isEmpty())
                {
                    component.append(Component.translatable(citizen.getJob()));
                    component.append(Component.literal(": "));
                }
                component.append(Component.literal(citizen.getName()));

                tooltip.add(component);
            }
        }
    }
}
