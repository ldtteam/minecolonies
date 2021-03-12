package com.minecolonies.coremod.colony.requestsystem.requesters;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * A class that functions as the connection between a building and the request system.
 */
public class BuildingBasedRequester implements IBuildingBasedRequester
{

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_LOCATION = "Location";
    private static final String NBT_ID       = "Id";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    private final ILocation location;

    private final IToken<?> requesterId;

    private IRequester building = null;

    public BuildingBasedRequester(final ILocation location, final IToken<?> requesterId)
    {
        this.location = location;
        this.requesterId = requesterId;
    }

    public static BuildingBasedRequester deserialize(final IFactoryController controller, final CompoundNBT compound)
    {
        final ILocation location = controller.deserialize(compound.getCompound(NBT_LOCATION));
        final IToken<?> token = controller.deserialize(compound.getCompound(NBT_ID));

        return new BuildingBasedRequester(location, token);
    }

    public CompoundNBT serialize(final IFactoryController controller)
    {
        final CompoundNBT compound = new CompoundNBT();

        compound.put(NBT_LOCATION, controller.serialize(getLocation()));
        compound.put(NBT_ID, controller.serialize(getId()));

        return compound;
    }

    public void serialize(final IFactoryController controller, final PacketBuffer buffer)
    {
        controller.serialize(buffer, getLocation());
        controller.serialize(buffer, getId());
    }

    public static BuildingBasedRequester deserialize(final IFactoryController controller, final PacketBuffer buffer)
    {
        final ILocation location = controller.deserialize(buffer);
        final IToken<?> id = controller.deserialize(buffer);
        return new BuildingBasedRequester(location, id);
    }

    @Override
    public IToken<?> getId()
    {
        return requesterId;
    }

    @NotNull
    @Override
    public ILocation getLocation()
    {
        return location;
    }

    @Override
    public void onRequestedRequestComplete(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        getBuilding(manager, request.getId()).ifPresent(requester -> requester.onRequestedRequestComplete(manager, request));
    }

    @Override
    public void onRequestedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        getBuilding(manager, request.getId()).ifPresent(requester -> requester.onRequestedRequestCancelled(manager, request));
    }

    @NotNull
    @Override
    public IFormattableTextComponent getRequesterDisplayName(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        return getBuilding(manager, request.getId()).map(requester -> requester.getRequesterDisplayName(manager, request)).orElseGet(() -> new StringTextComponent("<UNKNOWN>"));
    }

    @Override
    public Optional<IRequester> getBuilding(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        updateBuilding(manager.getColony());
        return Optional.ofNullable(building);
    }

    private void updateBuilding(IColony colony)
    {
        if (building != null || location == null)
        {
            return;
        }

        if (colony == null)
        {
            return;
        }

        building = colony.getRequesterBuildingForPosition(location.getInDimensionLocation());
    }
}
