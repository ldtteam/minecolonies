package com.minecolonies.core.colony.requestsystem.resolvers.factory;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.jobs.registry.IJobRegistry;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolverFactory;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.SerializationIdentifierConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.core.colony.requestsystem.resolvers.PrivateWorkerCraftingRequestResolver;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class PrivateWorkerCraftingRequestResolverFactory implements IRequestResolverFactory<PrivateWorkerCraftingRequestResolver>
{
    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_TOKEN    = "Token";
    private static final String NBT_LOCATION = "Location";
    private static final String NBT_JOB = "Job";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    @NotNull
    @Override
    public TypeToken<? extends PrivateWorkerCraftingRequestResolver> getFactoryOutputType()
    {
        return TypeToken.of(PrivateWorkerCraftingRequestResolver.class);
    }

    @NotNull
    @Override
    public TypeToken<? extends ILocation> getFactoryInputType()
    {
        return TypeConstants.ILOCATION;
    }

    @NotNull
    @Override
    public PrivateWorkerCraftingRequestResolver getNewInstance(
      @NotNull final IFactoryController factoryController,
      @NotNull final ILocation iLocation,
      @NotNull final Object... context)
    {
        return new PrivateWorkerCraftingRequestResolver(iLocation, factoryController.getNewInstance(TypeConstants.ITOKEN), (JobEntry) context[0]);
    }

    @NotNull
    @Override
    public CompoundTag serialize(
      @NotNull final IFactoryController controller, @NotNull final PrivateWorkerCraftingRequestResolver privateWorkerCraftingRequestResolverFactory)
    {
        final CompoundTag compound = new CompoundTag();
        compound.put(NBT_TOKEN, controller.serialize(privateWorkerCraftingRequestResolverFactory.getId()));
        compound.put(NBT_LOCATION, controller.serialize(privateWorkerCraftingRequestResolverFactory.getLocation()));
        compound.putString(NBT_JOB, IJobRegistry.getInstance().getKey(privateWorkerCraftingRequestResolverFactory.getJobEntry()).toString());
        return compound;
    }

    @NotNull
    @Override
    public PrivateWorkerCraftingRequestResolver deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundTag nbt)
    {
        final IToken<?> token = controller.deserialize(nbt.getCompound(NBT_TOKEN));
        final ILocation location = controller.deserialize(nbt.getCompound(NBT_LOCATION));
        final JobEntry entry = IJobRegistry.getInstance().get(ResourceLocation.parse(nbt.getString(NBT_JOB)));

        return new PrivateWorkerCraftingRequestResolver(location, token, entry);
    }

    @Override
    public void serialize(IFactoryController controller, PrivateWorkerCraftingRequestResolver input, FriendlyByteBuf packetBuffer)
    {
        controller.serialize(packetBuffer, input.getId());
        controller.serialize(packetBuffer, input.getLocation());
        packetBuffer.writeById(IMinecoloniesAPI.getInstance().getJobRegistry()::getIdOrThrow, input.getJobEntry());
    }

    @Override
    public PrivateWorkerCraftingRequestResolver deserialize(IFactoryController controller, FriendlyByteBuf buffer) throws Throwable
    {
        final IToken<?> token = controller.deserialize(buffer);
        final ILocation location = controller.deserialize(buffer);
        final JobEntry entry = buffer.readById(IMinecoloniesAPI.getInstance().getJobRegistry()::byIdOrThrow);

        return new PrivateWorkerCraftingRequestResolver(location, token, entry);
    }

    @Override
    public short getSerializationId()
    {
        return SerializationIdentifierConstants.PRIVATE_WORKER_CRAFTING_REQUEST_RESOLVER_ID;
    }
}
