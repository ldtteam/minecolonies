package com.minecolonies.coremod.colony.requestsystem.resolvers;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.requestable.deliveryman.Delivery;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.TypeConstants;
import org.jetbrains.annotations.NotNull;

/**
 * Resolves deliveries
 */
public class DeliveryRequestResolver extends DeliverymenRequestResolver<Delivery>
{
    public DeliveryRequestResolver(
      @NotNull final ILocation location,
      @NotNull final IToken<?> token)
    {
        super(location, token);
    }

    @Override
    public TypeToken<? extends Delivery> getRequestType()
    {
        return TypeConstants.DELIVERY;
    }

}
