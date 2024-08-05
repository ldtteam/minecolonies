package com.minecolonies.core.colony.requestsystem.resolvers.factory;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolverFactory;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.SerializationIdentifierConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.core.colony.requestsystem.resolvers.WarehouseConcreteRequestResolver;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class WarehouseConcreteRequestResolverFactory implements IRequestResolverFactory<WarehouseConcreteRequestResolver>
{
    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_TOKEN    = "Token";
    private static final String NBT_LOCATION = "Location";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    @NotNull
    @Override
    public TypeToken<? extends WarehouseConcreteRequestResolver> getFactoryOutputType()
    {
        return TypeToken.of(WarehouseConcreteRequestResolver.class);
    }

    @NotNull
    @Override
    public TypeToken<? extends ILocation> getFactoryInputType()
    {
        return TypeConstants.ILOCATION;
    }

    @NotNull
    @Override
    public WarehouseConcreteRequestResolver getNewInstance(
      @NotNull final IFactoryController factoryController,
      @NotNull final ILocation iLocation,
      @NotNull final Object... context)
      throws IllegalArgumentException
    {
        return new WarehouseConcreteRequestResolver(iLocation, factoryController.getNewInstance(TypeConstants.ITOKEN));
    }

    @NotNull
    @Override
    public CompoundTag serialize(
      @NotNull final IFactoryController controller, @NotNull final WarehouseConcreteRequestResolver warehouseConcreteRequestResolver)
    {
        final CompoundTag compound = new CompoundTag();
        compound.put(NBT_TOKEN, controller.serialize(warehouseConcreteRequestResolver.getId()));
        compound.put(NBT_LOCATION, controller.serialize(warehouseConcreteRequestResolver.getLocation()));
        return compound;
    }

    @NotNull
    @Override
    public WarehouseConcreteRequestResolver deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundTag nbt)
    {
        final IToken<?> token = controller.deserializeTag(nbt.getCompound(NBT_TOKEN));
        final ILocation location = controller.deserializeTag(nbt.getCompound(NBT_LOCATION));

        return new WarehouseConcreteRequestResolver(location, token);
    }

    @Override
    public void serialize(IFactoryController controller, WarehouseConcreteRequestResolver input, FriendlyByteBuf packetBuffer)
    {
        controller.serialize(packetBuffer, input.getId());
        controller.serialize(packetBuffer, input.getLocation());
    }

    @Override
    public WarehouseConcreteRequestResolver deserialize(IFactoryController controller, FriendlyByteBuf buffer) throws Throwable
    {
        final IToken<?> token = controller.deserializeTag(buffer);
        final ILocation location = controller.deserializeTag(buffer);

        return new WarehouseConcreteRequestResolver(location, token);
    }

    @Override
    public short getSerializationId()
    {
        return SerializationIdentifierConstants.WAREHOUSE_CONCRETE_REQUEST_RESOLVER_ID;
    }
}
