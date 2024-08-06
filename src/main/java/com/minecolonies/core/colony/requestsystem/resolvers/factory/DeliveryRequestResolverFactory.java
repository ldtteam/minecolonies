package com.minecolonies.core.colony.requestsystem.resolvers.factory;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolverFactory;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.SerializationIdentifierConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.core.colony.requestsystem.resolvers.DeliveryRequestResolver;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class DeliveryRequestResolverFactory implements IRequestResolverFactory<DeliveryRequestResolver>
{
    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_TOKEN    = "Token";
    private static final String NBT_LOCATION = "Location";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    @NotNull
    @Override
    public TypeToken<? extends DeliveryRequestResolver> getFactoryOutputType()
    {
        return TypeToken.of(DeliveryRequestResolver.class);
    }

    @NotNull
    @Override
    public TypeToken<? extends ILocation> getFactoryInputType()
    {
        return TypeConstants.ILOCATION;
    }

    @NotNull
    @Override
    public DeliveryRequestResolver getNewInstance(
      @NotNull final IFactoryController factoryController,
      @NotNull final ILocation iLocation,
      @NotNull final Object... context)
      throws IllegalArgumentException
    {
        return new DeliveryRequestResolver(iLocation, factoryController.getNewInstance(TypeConstants.ITOKEN));
    }

    @NotNull
    @Override
    public CompoundTag serialize(
      @NotNull final HolderLookup.Provider provider, @NotNull final IFactoryController controller, @NotNull final DeliveryRequestResolver deliveryRequestResolver)
    {
        final CompoundTag compound = new CompoundTag();
        compound.put(NBT_TOKEN, controller.serializeTag(provider, deliveryRequestResolver.getId()));
        compound.put(NBT_LOCATION, controller.serializeTag(provider, deliveryRequestResolver.getLocation()));
        return compound;
    }

    @NotNull
    @Override
    public DeliveryRequestResolver deserialize(@NotNull final HolderLookup.Provider provider, @NotNull final IFactoryController controller, @NotNull final CompoundTag nbt)
    {
        final IToken<?> token = controller.deserializeTag(provider, nbt.getCompound(NBT_TOKEN));
        final ILocation location = controller.deserializeTag(provider, nbt.getCompound(NBT_LOCATION));

        return new DeliveryRequestResolver(location, token);
    }

    @Override
    public void serialize(IFactoryController controller, DeliveryRequestResolver input, RegistryFriendlyByteBuf packetBuffer)
    {
        controller.serialize(packetBuffer, input.getId());
        controller.serialize(packetBuffer, input.getLocation());
    }

    @Override
    public DeliveryRequestResolver deserialize(IFactoryController controller, RegistryFriendlyByteBuf buffer) throws Throwable
    {
        final IToken<?> token = controller.deserialize(buffer);
        final ILocation location = controller.deserialize(buffer);

        return new DeliveryRequestResolver(location, token);
    }

    @Override
    public short getSerializationId()
    {
        return SerializationIdentifierConstants.DELIVERY_REQUEST_RESOLVER_ID;
    }
}
