package com.minecolonies.coremod.colony.requestsystem.resolvers.factory;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolverFactory;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.requestsystem.resolvers.BuildingRequestResolver;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

/**
 * ------------ Class not Documented ------------
 */
public class BuildingRequestResolverFactory implements IRequestResolverFactory<BuildingRequestResolver>
{
    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_TOKEN    = "Token";
    private static final String NBT_LOCATION = "Location";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    @NotNull
    @Override
    public TypeToken<? extends BuildingRequestResolver> getFactoryOutputType()
    {
        return TypeToken.of(BuildingRequestResolver.class);
    }

    @NotNull
    @Override
    public TypeToken<? extends ILocation> getFactoryInputType()
    {
        return TypeConstants.ILOCATION;
    }

    @NotNull
    @Override
    public BuildingRequestResolver getNewInstance(@NotNull final IFactoryController factoryController, @NotNull final ILocation iLocation, @NotNull final Object... context)
      throws IllegalArgumentException
    {
        return new BuildingRequestResolver(iLocation, factoryController.getNewInstance(TypeConstants.ITOKEN));
    }

    @NotNull
    @Override
    public CompoundNBT serialize(
      @NotNull final IFactoryController controller, @NotNull final BuildingRequestResolver buildingRequestResolver)
    {
        final CompoundNBT compound = new CompoundNBT();
        compound.put(NBT_TOKEN, controller.serialize(buildingRequestResolver.getId()));
        compound.put(NBT_LOCATION, controller.serialize(buildingRequestResolver.getLocation()));
        return compound;
    }

    @NotNull
    @Override
    public BuildingRequestResolver deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt)
    {
        final IToken<?> token = controller.deserialize(nbt.getCompound(NBT_TOKEN));
        final ILocation location = controller.deserialize(nbt.getCompound(NBT_LOCATION));

        return new BuildingRequestResolver(location, token);
    }

    @Override
    public void serialize(IFactoryController controller, BuildingRequestResolver input, PacketBuffer packetBuffer)
    {
        controller.serialize(packetBuffer, input.getId());
        controller.serialize(packetBuffer, input.getLocation());
    }

    @Override
    public BuildingRequestResolver deserialize(IFactoryController controller, PacketBuffer buffer) throws Throwable
    {
        final IToken<?> token = controller.deserialize(buffer);
        final ILocation location = controller.deserialize(buffer);

        return new BuildingRequestResolver(location, token);
    }

    @Override
    public short getSerializationId()
    {
        return 23;
    }
}
