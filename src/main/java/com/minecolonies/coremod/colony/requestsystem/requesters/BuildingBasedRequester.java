package com.minecolonies.coremod.colony.requestsystem.requesters;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.ColonyManager;
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

        compound.setTag(NBT_LOCATION, controller.serialize(getRequesterLocation()));
        compound.setTag(NBT_ID, controller.serialize(getRequesterId()));

        return compound;
    }

    @Override
    public IToken<?> getRequesterId()
    {
        return requesterId;
    }

    @NotNull
    @Override
    public ILocation getRequesterLocation()
    {
        return location;
    }

    @Override
    public void onRequestComplete(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        getBuilding(manager, token).ifPresent(requester -> requester.onRequestComplete(manager, token));
    }

    @Override
    public void onRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        getBuilding(manager, token).ifPresent(requester -> requester.onRequestCancelled(manager, token));
    }

    @NotNull
    @Override
    public ITextComponent getDisplayName(@NotNull final IRequestManager manager, @NotNull final IToken<?> token)
    {
        return getBuilding(manager, token).map(requester -> requester.getDisplayName(manager, token)).orElseGet(() -> new TextComponentString("<UNKNOWN>"));
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
        final IColony colony = ColonyManager.getClosestIColony(world, location.getInDimensionLocation());

        if (colony == null)
        {
            return;
        }

        building = colony.getRequesterBuildingForPosition(location.getInDimensionLocation());
    }
}
