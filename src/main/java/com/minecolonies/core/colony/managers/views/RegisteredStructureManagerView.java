package com.minecolonies.core.colony.managers.views;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildingextensions.IBuildingExtension;
import com.minecolonies.api.colony.buildings.registry.IBuildingDataManager;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.buildings.workerbuildings.ITownHallView;
import com.minecolonies.api.colony.managers.interfaces.views.IRegisteredStructureManagerView;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.core.colony.ColonyView;
import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingWareHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

/**
 * Client side registered structure manager.
 */
public class RegisteredStructureManagerView implements IRegisteredStructureManagerView
{
    /**
     * Check if the colony has a warehouse.
     */
    private boolean hasColonyWarehouse;

    /**
     * Matching colony view.
     */
    private final ColonyView colonyView;

    /**
     * Map of all buildings.
     */
    @NotNull
    private final Map<BlockPos, IBuildingView> buildings = new HashMap<>();

    /**
     * Set of all building extensions.
     */
    @NotNull
    private final Set<IBuildingExtension> fields = new HashSet<>();

    /**
     * Direct link to townhall.
     */
    @Nullable
    private ITownHallView townHall;

    /**
     * Create a new client side registered structure manager.
     * @param colonyView the colony view it belongs to.
     */
    public RegisteredStructureManagerView(final ColonyView colonyView)
    {
        this.colonyView = colonyView;
    }

    @Override
    @Nullable
    public ITownHallView getTownHall()
    {
        return townHall;
    }

    @Nullable
    @Override
    public  IMessage handleColonyViewRemoveBuildingMessage(final BlockPos buildingId)
    {
        final IBuildingView building = buildings.remove(buildingId);
        if (townHall == building)
        {
            townHall = null;
        }
        return null;
    }

    @Nullable
    @Override
    public IMessage handleColonyBuildingViewMessage(final BlockPos buildingId, @NotNull final FriendlyByteBuf buf)
    {
        if (buildings.containsKey(buildingId))
        {
            //Read the string first to set up the buffer.
            buf.readUtf(32767);
            buildings.get(buildingId).deserialize(buf);
        }
        else
        {
            @Nullable final IBuildingView building = IBuildingDataManager.getInstance().createViewFrom(colonyView, buildingId, buf);
            if (building != null)
            {
                buildings.put(building.getID(), building);

                if (building instanceof BuildingTownHall.View)
                {
                    townHall = (ITownHallView) building;
                }
                else if (building instanceof BuildingWareHouse.View)
                {
                    hasColonyWarehouse = true;
                }
            }
        }

        return null;
    }

    @Override
    public void handleColonyBuildingExtensionViewUpdateMessage(final Set<IBuildingExtension> extensions)
    {
        this.fields.clear();
        this.fields.addAll(extensions);
    }

    @NotNull
    @Override
    public List<IBuildingExtension> getBuildingExtensions(final Predicate<IBuildingExtension> matcher)
    {
        return fields.stream()
            .filter(matcher)
            .toList();
    }

    @Override
    public boolean hasTownHall()
    {
        return townHall != null;
    }

    @Override
    public IColony getColony()
    {
        return colonyView;
    }

    @Override
    public boolean hasWarehouse()
    {
        return hasColonyWarehouse;
    }

    @NotNull
    @Override
    public Map<BlockPos, IBuildingView> getBuildings()
    {
        return buildings;
    }

    @Override
    public void deserializeFromView(final boolean isNewSubscription, final @NotNull FriendlyByteBuf buf)
    {
        if (isNewSubscription)
        {
            townHall = null;
            buildings.clear();
        }
    }
}
