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
import com.minecolonies.core.colony.requestsystem.resolvers.PrivateWorkerCraftingProductionResolver;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class PrivateWorkerCraftingProductionResolverFactory implements IRequestResolverFactory<PrivateWorkerCraftingProductionResolver>
{
    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_TOKEN    = "Token";
    private static final String NBT_LOCATION = "Location";
    private static final String NBT_JOB = "Job";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    @NotNull
    @Override
    public TypeToken<? extends PrivateWorkerCraftingProductionResolver> getFactoryOutputType()
    {
        return TypeToken.of(PrivateWorkerCraftingProductionResolver.class);
    }

    @NotNull
    @Override
    public TypeToken<? extends ILocation> getFactoryInputType()
    {
        return TypeConstants.ILOCATION;
    }

    @NotNull
    @Override
    public PrivateWorkerCraftingProductionResolver getNewInstance(
      @NotNull final IFactoryController factoryController,
      @NotNull final ILocation iLocation,
      @NotNull final Object... context)
    {
        return new PrivateWorkerCraftingProductionResolver(iLocation, factoryController.getNewInstance(TypeConstants.ITOKEN), (JobEntry) context[0]);
    }

    @NotNull
    @Override
    public CompoundTag serialize(
      @NotNull final IFactoryController controller, @NotNull final PrivateWorkerCraftingProductionResolver privateWorkerCraftingProductionResolverFactory)
    {
        final CompoundTag compound = new CompoundTag();
        compound.put(NBT_TOKEN, controller.serialize(privateWorkerCraftingProductionResolverFactory.getId()));
        compound.put(NBT_LOCATION, controller.serialize(privateWorkerCraftingProductionResolverFactory.getLocation()));
        compound.putString(NBT_JOB, IJobRegistry.getInstance().getKey(privateWorkerCraftingProductionResolverFactory.getJobEntry()).toString());
        return compound;
    }

    @NotNull
    @Override
    public PrivateWorkerCraftingProductionResolver deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundTag nbt)
    {
        final IToken<?> token = controller.deserialize(nbt.getCompound(NBT_TOKEN));
        final ILocation location = controller.deserialize(nbt.getCompound(NBT_LOCATION));
        final JobEntry entry = IJobRegistry.getInstance().getValue(new ResourceLocation(nbt.getString(NBT_JOB)));

        return new PrivateWorkerCraftingProductionResolver(location, token, entry);
    }

    @Override
    public void serialize(IFactoryController controller, PrivateWorkerCraftingProductionResolver input, FriendlyByteBuf packetBuffer)
    {
        controller.serialize(packetBuffer, input.getId());
        controller.serialize(packetBuffer, input.getLocation());
        packetBuffer.writeRegistryId(IMinecoloniesAPI.getInstance().getJobRegistry(), input.getJobEntry());
    }

    @Override
    public PrivateWorkerCraftingProductionResolver deserialize(IFactoryController controller, FriendlyByteBuf buffer) throws Throwable
    {
        final IToken<?> token = controller.deserialize(buffer);
        final ILocation location = controller.deserialize(buffer);
        final JobEntry entry = buffer.readRegistryId();

        return new PrivateWorkerCraftingProductionResolver(location, token, entry);
    }

    @Override
    public short getSerializationId()
    {
        return SerializationIdentifierConstants.PRIVATE_WORKER_CRAFTING_PRODUCTION_RESOLVER_ID;
    }
}
