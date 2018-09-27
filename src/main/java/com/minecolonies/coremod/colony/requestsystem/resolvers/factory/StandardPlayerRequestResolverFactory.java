package com.minecolonies.coremod.colony.requestsystem.resolvers.factory;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.requestsystem.resolvers.StandardPlayerRequestResolver;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * ------------ Class not Documented ------------
 */
public class StandardPlayerRequestResolverFactory implements IFactory<IRequestManager, StandardPlayerRequestResolver>
{
    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_TOKEN             = "Token";
    private static final String NBT_LOCATION          = "Location";
    private static final String NBT_ASSIGNED_REQUESTS = "Requests";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    private static final Integer CONST_PLAYER_RESOLVER_ID_SCALE = -1;

    @NotNull
    @Override
    public TypeToken<? extends StandardPlayerRequestResolver> getFactoryOutputType()
    {
        return TypeToken.of(StandardPlayerRequestResolver.class);
    }

    @NotNull
    @Override
    public TypeToken<? extends IRequestManager> getFactoryInputType()
    {
        return TypeToken.of(IRequestManager.class);
    }

    @NotNull
    @Override
    public StandardPlayerRequestResolver getNewInstance(
                                                 @NotNull final IFactoryController factoryController,
                                                 @NotNull final IRequestManager iRequestManager,
                                                 @NotNull final Object... context)
      throws IllegalArgumentException
    {
        final ILocation location;
        try {
            location =
            factoryController.getNewInstance(TypeConstants.ILOCATION, iRequestManager.getColony().getCenter(), iRequestManager.getColony().getDimension());
        } catch (final Exception ex)
        {
            throw ex;
        }

        final IToken<?> token = factoryController.getNewInstance(TypeConstants.ITOKEN, iRequestManager.getColony().getID() * CONST_PLAYER_RESOLVER_ID_SCALE);
        return new StandardPlayerRequestResolver(location, token);
    }

    @NotNull
    @Override
    public NBTTagCompound serialize(@NotNull final IFactoryController controller, @NotNull final StandardPlayerRequestResolver playerRequestResolver)
    {
        final NBTTagCompound compound = new NBTTagCompound();
        compound.setTag(NBT_TOKEN, controller.serialize(playerRequestResolver.getRequesterId()));
        compound.setTag(NBT_LOCATION, controller.serialize(playerRequestResolver.getRequesterLocation()));
        compound.setTag(NBT_ASSIGNED_REQUESTS, playerRequestResolver.getAllAssignedRequests().stream().map(controller::serialize).collect(NBTUtils.toNBTTagList()));
        return compound;
    }

    @NotNull
    @Override
    public StandardPlayerRequestResolver deserialize(@NotNull final IFactoryController controller, @NotNull final NBTTagCompound nbt)
    {
        final IToken token = controller.deserialize(nbt.getCompoundTag(NBT_TOKEN));
        final ILocation location = controller.deserialize(nbt.getCompoundTag(NBT_LOCATION));

        final Set<IToken<?>> assignedRequests =
          NBTUtils.streamCompound(nbt.getTagList(NBT_ASSIGNED_REQUESTS, Constants.NBT.TAG_COMPOUND)).map(c -> (IToken<?>) controller.deserialize(c)).collect(Collectors.toSet());

        final StandardPlayerRequestResolver resolver = new StandardPlayerRequestResolver(location, token);
        resolver.setAllAssignedRequests(assignedRequests);

        return resolver;
    }
}
