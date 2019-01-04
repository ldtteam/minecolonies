package com.minecolonies.coremod.colony.requestsystem.resolvers.factory;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolverFactory;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PublicWorkerCraftingRequestResolver;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

public class PublicWorkerCraftingRequestResolverFactory implements IRequestResolverFactory<PublicWorkerCraftingRequestResolver>
{
    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_TOKEN    = "Token";
    private static final String NBT_LOCATION = "Location";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    @NotNull
    @Override
    public TypeToken<? extends PublicWorkerCraftingRequestResolver> getFactoryOutputType()
    {
        return TypeToken.of(PublicWorkerCraftingRequestResolver.class);
    }

    @NotNull
    @Override
    public TypeToken<? extends ILocation> getFactoryInputType()
    {
        return TypeConstants.ILOCATION;
    }

    @NotNull
    @Override
    public PublicWorkerCraftingRequestResolver getNewInstance(
                                                    @NotNull final IFactoryController factoryController,
                                                    @NotNull final ILocation iLocation,
                                                    @NotNull final Object... context)
    {
        return new PublicWorkerCraftingRequestResolver(iLocation, factoryController.getNewInstance(TypeConstants.ITOKEN));
    }

    @NotNull
    @Override
    public NBTTagCompound serialize(@NotNull final IFactoryController controller, @NotNull final PublicWorkerCraftingRequestResolver publicWorkerCraftingRequestResolverFactory)
    {
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setTag(NBT_TOKEN, controller.serialize(publicWorkerCraftingRequestResolverFactory.getRequesterId()));
        compound.setTag(NBT_LOCATION, controller.serialize(publicWorkerCraftingRequestResolverFactory.getRequesterLocation()));
        return compound;
    }

    @NotNull
    @Override
    public PublicWorkerCraftingRequestResolver deserialize(@NotNull final IFactoryController controller, @NotNull final NBTTagCompound nbt)
    {
        final IToken<?> token = controller.deserialize(nbt.getCompoundTag(NBT_TOKEN));
        final ILocation location = controller.deserialize(nbt.getCompoundTag(NBT_LOCATION));

        return new PublicWorkerCraftingRequestResolver(location, token);
    }
}
