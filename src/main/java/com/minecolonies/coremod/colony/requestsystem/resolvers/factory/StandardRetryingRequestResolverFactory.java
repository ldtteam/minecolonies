package com.minecolonies.coremod.colony.requestsystem.resolvers.factory;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.coremod.colony.requestsystem.resolvers.StandardRetryingRequestResolver;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class StandardRetryingRequestResolverFactory implements IFactory<IRequestManager, StandardRetryingRequestResolver>
{
    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_TOKEN = "Token";
    private static final String NBT_LOCATION = "Location";
    private static final String NBT_VALUE = "Value";
    private static final String NBT_TRIES = "Requests";
    private static final String NBT_DELAYS = "Delays";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    @NotNull
    @Override
    public TypeToken<? extends StandardRetryingRequestResolver> getFactoryOutputType()
    {
        return TypeToken.of(StandardRetryingRequestResolver.class);
    }

    @NotNull
    @Override
    public TypeToken<? extends IRequestManager> getFactoryInputType()
    {
        return TypeToken.of(IRequestManager.class);
    }

    @NotNull
    @Override
    public StandardRetryingRequestResolver getNewInstance(
                                                           @NotNull final IFactoryController factoryController,
                                                           @NotNull final IRequestManager iRequestManager,
                                                           @NotNull final Object... context)
      throws IllegalArgumentException
    {
        if (context.length != 0)
        {
            throw new IllegalArgumentException("Context is not empty.");
        }

        return new StandardRetryingRequestResolver(factoryController, iRequestManager);
    }

    @NotNull
    @Override
    public NBTTagCompound serialize(
                                     @NotNull final IFactoryController controller, @NotNull final StandardRetryingRequestResolver standardRetryingRequestResolver)
    {
        final NBTTagCompound compound = new NBTTagCompound();

        compound.setTag(NBT_TRIES, standardRetryingRequestResolver.getAssignedRequests().keySet().stream().map(t -> {
            final NBTTagCompound assignmentCompound = new NBTTagCompound();

            assignmentCompound.setTag(NBT_TOKEN, controller.serialize(t));
            assignmentCompound.setInteger(NBT_VALUE, standardRetryingRequestResolver.getAssignedRequests().get(t));

            return assignmentCompound;
        }).collect(NBTUtils.toNBTTagList()));
        compound.setTag(NBT_DELAYS, standardRetryingRequestResolver.getDelays().keySet().stream().map(t -> {
            final NBTTagCompound delayCompound = new NBTTagCompound();

            delayCompound.setTag(NBT_TOKEN, controller.serialize(t));
            delayCompound.setInteger(NBT_VALUE, standardRetryingRequestResolver.getDelays().get(t));

            return delayCompound;
        }).collect(NBTUtils.toNBTTagList()));

        compound.setTag(NBT_TOKEN, controller.serialize(standardRetryingRequestResolver.getRequesterId()));
        compound.setTag(NBT_LOCATION, controller.serialize(standardRetryingRequestResolver.getRequesterLocation()));

        return compound;
    }

    @NotNull
    @Override
    public StandardRetryingRequestResolver deserialize(@NotNull final IFactoryController controller, @NotNull final NBTTagCompound nbt)
    {
        final Map<IToken<?>, Integer> assignments = NBTUtils.streamCompound(nbt.getTagList(NBT_TRIES, Constants.NBT.TAG_COMPOUND)).map(assignmentCompound -> {
            IToken token = controller.deserialize(assignmentCompound.getCompoundTag(NBT_TOKEN));
            Integer tries = assignmentCompound.getInteger(NBT_VALUE);

            return new HashMap.SimpleEntry<>(token, tries);
        }).collect(Collectors.toMap(HashMap.SimpleEntry::getKey, HashMap.SimpleEntry::getValue));

        final Map<IToken<?>, Integer> delays = NBTUtils.streamCompound(nbt.getTagList(NBT_DELAYS, Constants.NBT.TAG_COMPOUND)).map(assignmentCompound -> {
            IToken token = controller.deserialize(assignmentCompound.getCompoundTag(NBT_TOKEN));
            Integer tries = assignmentCompound.getInteger(NBT_VALUE);

            return new HashMap.SimpleEntry<>(token, tries);
        }).collect(Collectors.toMap(HashMap.SimpleEntry::getKey, HashMap.SimpleEntry::getValue));

        final IToken<?> token = controller.deserialize(nbt.getCompoundTag(NBT_TOKEN));
        final ILocation location = controller.deserialize(nbt.getCompoundTag(NBT_LOCATION));

        final StandardRetryingRequestResolver retryingRequestResolver = new StandardRetryingRequestResolver(token, location);
        retryingRequestResolver.updateData(assignments, delays);
        return retryingRequestResolver;
    }
}
