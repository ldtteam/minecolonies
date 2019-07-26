package com.minecolonies.coremod.colony.requestsystem.resolvers.factory;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolverFactory;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PublicWorkerCraftingProductionResolver;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

public class PublicWorkerCraftingProductionResolverFactory implements IRequestResolverFactory<PublicWorkerCraftingProductionResolver>
{
    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_TOKEN    = "Token";
    private static final String NBT_LOCATION = "Location";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    @NotNull
    @Override
    public TypeToken<? extends PublicWorkerCraftingProductionResolver> getFactoryOutputType()
    {
        return TypeToken.of(PublicWorkerCraftingProductionResolver.class);
    }

    @NotNull
    @Override
    public TypeToken<? extends ILocation> getFactoryInputType()
    {
        return TypeConstants.ILOCATION;
    }

    @NotNull
    @Override
    public PublicWorkerCraftingProductionResolver getNewInstance(
                                                    @NotNull final IFactoryController factoryController,
                                                    @NotNull final ILocation iLocation,
                                                    @NotNull final Object... context)
    {
        return new PublicWorkerCraftingProductionResolver(iLocation, factoryController.getNewInstance(TypeConstants.ITOKEN));
    }

    @NotNull
    @Override
    public NBTTagCompound serialize(@NotNull final IFactoryController controller, @NotNull final PublicWorkerCraftingProductionResolver publicWorkerCraftingProductionResolverFactory)
    {
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setTag(NBT_TOKEN, controller.serialize(publicWorkerCraftingProductionResolverFactory.getId()));
        compound.setTag(NBT_LOCATION, controller.serialize(publicWorkerCraftingProductionResolverFactory.getRequesterLocation()));
        return compound;
    }

    @NotNull
    @Override
    public PublicWorkerCraftingProductionResolver deserialize(@NotNull final IFactoryController controller, @NotNull final NBTTagCompound nbt)
    {
        final IToken<?> token = controller.deserialize(nbt.getCompoundTag(NBT_TOKEN));
        final ILocation location = controller.deserialize(nbt.getCompoundTag(NBT_LOCATION));

        return new PublicWorkerCraftingProductionResolver(location, token);
    }
}
