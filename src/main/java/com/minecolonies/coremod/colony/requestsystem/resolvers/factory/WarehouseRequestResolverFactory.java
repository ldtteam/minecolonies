package com.minecolonies.coremod.colony.requestsystem.resolvers.factory;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolverFactory;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.requestsystem.resolvers.WarehouseRequestResolver;
import net.minecraft.nbt.CompoundNBT;
import org.jetbrains.annotations.NotNull;

public class WarehouseRequestResolverFactory implements IRequestResolverFactory<WarehouseRequestResolver>
{
    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_TOKEN = "Token";
    private static final String NBT_LOCATION = "Location";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    @NotNull
    @Override
    public TypeToken<? extends WarehouseRequestResolver> getFactoryOutputType()
    {
        return TypeToken.of(WarehouseRequestResolver.class);
    }

    @NotNull
    @Override
    public TypeToken<? extends ILocation> getFactoryInputType()
    {
        return TypeConstants.ILOCATION;
    }

    @NotNull
    @Override
    public WarehouseRequestResolver getNewInstance(@NotNull final IFactoryController factoryController,
        @NotNull final ILocation iLocation,
        @NotNull final Object... context) throws IllegalArgumentException
    {
        return new WarehouseRequestResolver(iLocation, factoryController.getNewInstance(TypeConstants.ITOKEN));
    }

    @NotNull
    @Override
    public CompoundNBT serialize(@NotNull final IFactoryController controller,
        @NotNull final WarehouseRequestResolver warehouseRequestResolver)
    {
        final CompoundNBT compound = new CompoundNBT();
        compound.put(NBT_TOKEN, controller.serialize(warehouseRequestResolver.getId()));
        compound.put(NBT_LOCATION, controller.serialize(warehouseRequestResolver.getLocation()));
        return compound;
    }

    @NotNull
    @Override
    public WarehouseRequestResolver deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt)
    {
        final IToken<?> token = controller.deserialize(nbt.getCompound(NBT_TOKEN));
        final ILocation location = controller.deserialize(nbt.getCompound(NBT_LOCATION));

        return new WarehouseRequestResolver(location, token);
    }
}
