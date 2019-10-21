package com.minecolonies.coremod.colony.requestsystem.requesters;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.coremod.MineColonies;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
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

    public static BuildingBasedRequester deserialize(final IFactoryController controller, final NBTTagCompound compound)
    {
        final ILocation location = controller.deserialize(compound.getCompoundTag(NBT_LOCATION));
        final IToken<?> token = controller.deserialize(compound.getCompoundTag(NBT_ID));

        return new BuildingBasedRequester(location, token);
    }

    public NBTTagCompound serialize(final IFactoryController controller)
    {
        final NBTTagCompound compound = new NBTTagCompound();

        compound.setTag(NBT_LOCATION, controller.serialize(getLocation()));
        compound.setTag(NBT_ID, controller.serialize(getId()));

        return compound;
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
    public ITextComponent getRequesterDisplayName(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        return getBuilding(manager, request.getId()).map(requester -> requester.getRequesterDisplayName(manager, request)).orElseGet(() -> new TextComponentString("<UNKNOWN>"));
    }

    @Override
    public Optional<IRequester> getBuilding(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        updateBuilding();
        return Optional.ofNullable(building);
    }
    
    private void updateBuilding()
    {
        if (building != null || location == null)
        {
            return;
        }

        final World world = MineColonies.proxy.getWorld(location.getDimension());
        final IColony colony = IColonyManager.getInstance().getClosestIColony(world, location.getInDimensionLocation());

        if (colony == null)
        {
            return;
        }

        building = colony.getRequesterBuildingForPosition(location.getInDimensionLocation());
    }
}
