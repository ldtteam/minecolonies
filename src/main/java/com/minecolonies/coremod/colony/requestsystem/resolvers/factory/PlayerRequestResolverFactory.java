package com.minecolonies.coremod.colony.requestsystem.resolvers.factory;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolverFactory;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PlayerRequestResolver;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * ------------ Class not Documented ------------
 */
public class PlayerRequestResolverFactory implements IRequestResolverFactory<PlayerRequestResolver>
{
    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String NBT_TOKEN  = "Token";
    private static final String NBT_LOCATION = "Location";
    private static final String NBT_ASSIGNED_REQUESTS = "Requests";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    @NotNull
    @Override
    public TypeToken<? extends PlayerRequestResolver> getFactoryOutputType()
    {
        return TypeToken.of(PlayerRequestResolver.class);
    }

    @NotNull
    @Override
    public TypeToken<? extends ILocation> getFactoryInputType()
    {
        return TypeConstants.ILOCATION;
    }

    @NotNull
    @Override
    public PlayerRequestResolver getNewInstance(
                                                   @NotNull final IFactoryController factoryController, @NotNull final ILocation iLocation, @NotNull final Object... context)
      throws IllegalArgumentException
    {
        return new PlayerRequestResolver(iLocation, factoryController.getNewInstance(TypeConstants.ITOKEN));
    }

    @NotNull
    @Override
    public NBTTagCompound serialize(@NotNull final IFactoryController controller, @NotNull final PlayerRequestResolver playerRequestResolver)
    {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag(NBT_TOKEN, controller.serialize(playerRequestResolver.getRequesterId()));
        compound.setTag(NBT_LOCATION, controller.serialize(playerRequestResolver.getRequesterLocation()));
        compound.setTag(NBT_ASSIGNED_REQUESTS, playerRequestResolver.getAllAssignedRequests().stream().map(controller::serialize).collect(NBTUtils.toNBTTagList()));
        return compound;
    }

    @NotNull
    @Override
    public PlayerRequestResolver deserialize(@NotNull final IFactoryController controller, @NotNull final NBTTagCompound nbt)
    {
        final IToken token = controller.deserialize(nbt.getCompoundTag(NBT_TOKEN));
        final ILocation location = controller.deserialize(nbt.getCompoundTag(NBT_LOCATION));

        final Set<IToken> assignedRequests = NBTUtils.streamCompound(nbt.getTagList(NBT_ASSIGNED_REQUESTS, Constants.NBT.TAG_COMPOUND)).map(c -> (IToken) controller.deserialize(c)).collect(
          Collectors.toSet());

        final PlayerRequestResolver resolver = new PlayerRequestResolver(location, token);
        resolver.setAllAssignedRequests(assignedRequests);

        return resolver;
    }
}
