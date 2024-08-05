package com.minecolonies.core.colony.requestsystem.requesters.factories;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.SerializationIdentifierConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.requestsystem.requesters.BuildingBasedRequester;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class BuildingBasedRequesterFactory implements IFactory<AbstractBuilding, BuildingBasedRequester>
{
    @NotNull
    @Override
    public TypeToken<? extends BuildingBasedRequester> getFactoryOutputType()
    {
        return TypeToken.of(BuildingBasedRequester.class);
    }

    @NotNull
    @Override
    public TypeToken<? extends AbstractBuilding> getFactoryInputType()
    {
        return TypeToken.of(AbstractBuilding.class);
    }

    @NotNull
    @Override
    public BuildingBasedRequester getNewInstance(@NotNull final IFactoryController factoryController, @NotNull final AbstractBuilding building, @NotNull final Object... context)
      throws IllegalArgumentException
    {
        if (context.length != 0)
        {
            throw new IllegalArgumentException("To many context elements. Only 0 supported.");
        }

        final ILocation location = factoryController.getNewInstance(TypeConstants.ILOCATION, building.getPosition(), building.getColony().getDimension());
        final IToken<?> token = factoryController.getNewInstance(TypeConstants.ITOKEN);

        return new BuildingBasedRequester(location, token);
    }

    @NotNull
    @Override
    public CompoundTag serialize(@NotNull final HolderLookup.Provider provider, @NotNull final IFactoryController controller, @NotNull final BuildingBasedRequester output)
    {
        return output.serialize(controller);
    }

    @NotNull
    @Override
    public BuildingBasedRequester deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundTag nbt)
    {
        return BuildingBasedRequester.deserialize(controller, nbt);
    }

    @Override
    public void serialize(IFactoryController controller, BuildingBasedRequester output, FriendlyByteBuf packetBuffer)
    {
        output.serialize(controller, packetBuffer);
    }

    @Override
    public BuildingBasedRequester deserialize(IFactoryController controller, FriendlyByteBuf buffer) throws Throwable
    {
        return BuildingBasedRequester.deserialize(controller, buffer);
    }

    @Override
    public short getSerializationId()
    {
        return SerializationIdentifierConstants.BUILDER_BASED_REQUESTER_ID;
    }
}
