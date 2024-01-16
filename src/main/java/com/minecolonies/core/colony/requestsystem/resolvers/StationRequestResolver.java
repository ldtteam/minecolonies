package com.minecolonies.core.colony.requestsystem.resolvers;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.modules.IAssignsCitizen;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.modules.AbstractAssignedCitizenModule;
import com.minecolonies.core.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingWareHouse;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Resolver that checks if a deliverable request is already in the station where the citizen is assigned to that requested things.
 */
public class StationRequestResolver extends BuildingRequestResolver
{
    public StationRequestResolver(@NotNull final ILocation location, @NotNull final IToken<?> token)
    {
        super(location, token);
    }

    @Override
    public boolean canResolveRequest(@NotNull final IRequestManager manager, final IRequest<? extends IDeliverable> request)
    {
        if (!manager.getColony().getWorld().isClientSide)
        {
            final Optional<AbstractBuilding> building = getBuilding(manager, request.getId()).map(r -> (AbstractBuilding) r);
            if (building.isPresent())
            {
                final AbstractBuilding theBuilding = building.get();
                if (theBuilding instanceof BuildingWareHouse
                      || theBuilding.getCitizenForRequest(request.getId()).isPresent()
                      || theBuilding.hasModule(WorkerBuildingModule.class)
                      || !theBuilding.hasModule(IAssignsCitizen.class))
                {
                    return false;
                }

                for (final IAssignsCitizen module : theBuilding.getModulesByType(IAssignsCitizen.class))
                {
                    for (final ICitizenData citizen : module.getAssignedCitizen())
                    {
                        if (citizen.getWorkBuilding() != null && citizen.getWorkBuilding().getCitizenForRequest(request.getId()).isPresent())
                        {
                            return canResolveForBuilding(manager, request, theBuilding);
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean canResolveForBuilding(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends IDeliverable> request, @NotNull final AbstractBuilding building)
    {
        if (building instanceof BuildingWareHouse
              || building.getCitizenForRequest(request.getId()).isPresent()
              || building.hasModule(WorkerBuildingModule.class)
              || !building.hasModule(AbstractAssignedCitizenModule.class))
        {
            return false;
        }

        boolean foundMatch = false;
        for (final AbstractAssignedCitizenModule module : building.getModulesByType(AbstractAssignedCitizenModule.class))
        {
            for (final ICitizenData citizen : module.getAssignedCitizen())
            {
                if (citizen.getWorkBuilding() != null && citizen.getWorkBuilding().getCitizenForRequest(request.getId()).isPresent())
                {
                    foundMatch = true;
                    break;
                }
            }
        }

        if (!foundMatch)
        {
            return false;
        }

        final Predicate<ItemStack> pred = itemStack -> {
            if (ItemStackUtils.isEmpty(itemStack) || !request.getRequest().matches(itemStack))
            {
                return false;
            }

            if (!request.hasParent())
            {
                return true;
            }

            final IRequest<?> requestParent = manager.getRequestForToken(request.getParent());

            return !requestParent.getRequestOfType(IDeliverable.class).map(d -> d.matches(itemStack)).orElse(false);
        };

        return InventoryUtils.hasBuildingEnoughElseCount(building, pred, 1) > 0;
    }
}
