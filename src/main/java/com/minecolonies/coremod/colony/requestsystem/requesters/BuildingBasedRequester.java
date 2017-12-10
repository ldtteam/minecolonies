package com.minecolonies.coremod.colony.requestsystem.requesters;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
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
public class BuildingBasedRequester implements IRequester
{

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_LOCATION = "Location";
    private static final String NBT_ID       = "Id";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    private final ILocation location;

    private final IToken requesterId;

    private IRequester building = null;

    public BuildingBasedRequester(final ILocation location, final IToken requesterId)
    {
        this.location = location;
        this.requesterId = requesterId;
    }

    public static BuildingBasedRequester deserialize(IFactoryController controller, NBTTagCompound compound)
    {
        ILocation location = controller.deserialize(compound.getCompoundTag(NBT_LOCATION));
        IToken token = controller.deserialize(compound.getCompoundTag(NBT_ID));

        return new BuildingBasedRequester(location, token);
    }

    public NBTTagCompound serialize(IFactoryController controller)
    {
        NBTTagCompound compound = new NBTTagCompound();

        compound.setTag(NBT_LOCATION, controller.serialize(getRequesterLocation()));
        compound.setTag(NBT_ID, controller.serialize(getRequesterId()));

        return compound;
    }

    @Override
    public IToken getRequesterId()
    {
        return requesterId;
    }

    @NotNull
    @Override
    public ILocation getRequesterLocation()
    {
        return location;
    }

    @NotNull
    @Override
    public void onRequestComplete(@NotNull final IToken token)
    {
        getBuilding().ifPresent(requester -> requester.onRequestComplete(token));
    }

    @NotNull
    @Override
    public void onRequestCancelled(@NotNull final IToken token)
    {
        getBuilding().ifPresent(requester -> requester.onRequestCancelled(token));
    }

    @NotNull
    @Override
    public ITextComponent getDisplayName(@NotNull final IToken token)
    {
        return getBuilding().map(requester -> requester.getDisplayName(token)).orElseGet(() -> new TextComponentString("<UNKNOWN>"));
    }

    public Optional<IRequester> getBuilding()
    {
        updateBuilding();
        return Optional.ofNullable(building);
    }

    private void updateBuilding()
    {
        if (building != null)
        {
            return;
        }

        if (location == null)
        {
            return;
        }

        final World world = MineColonies.proxy.getWorld(location.getDimension());
        final IColony colony = ColonyManager.getClosestIColony(world, location.getInDimensionLocation());

        building = colony.getRequesterBuildingForPosition(location.getInDimensionLocation());
    }
}
