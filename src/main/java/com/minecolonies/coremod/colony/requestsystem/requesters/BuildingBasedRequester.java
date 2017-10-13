package com.minecolonies.coremod.colony.requestsystem.requesters;

import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.jetbrains.annotations.NotNull;

/**
 * A class that functions as the connection between a building and the request system.
 */
public class BuildingBasedRequester implements IRequester
{

    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_LOCATION     = "Location";
    private static final String NBT_ID     = "Id";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    private final ILocation location;

    private final IToken requesterId;

    private AbstractBuilding building = null;

    public BuildingBasedRequester(final ILocation location, final IToken requesterId) {
        this.location = location;
        this.requesterId = requesterId;
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
        updateBuilding();

        if (building == null)
            return;

        building.onRequestComplete(token);
    }

    private void updateBuilding() {
        if (building != null)
            return;

        if (location == null)
            return;

        final World world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(getRequesterLocation().getDimension());
        final Colony colony = ColonyManager.getColony(world, getRequesterLocation().getInDimensionLocation());

        building = colony.getBuilding(getRequesterLocation().getInDimensionLocation());
    }

    public NBTTagCompound serialize(IFactoryController controller)
    {
        NBTTagCompound compound = new NBTTagCompound();

        compound.setTag(NBT_LOCATION, controller.serialize(getRequesterLocation()));
        compound.setTag(NBT_ID, controller.serialize(getRequesterId()));

        return compound;
    }

    public static BuildingBasedRequester deserialize(IFactoryController controller, NBTTagCompound compound)
    {
        ILocation location = controller.deserialize(compound.getCompoundTag(NBT_LOCATION));
        IToken token = controller.deserialize(compound.getCompoundTag(NBT_ID));

        return new BuildingBasedRequester(location, token);
    }
}
