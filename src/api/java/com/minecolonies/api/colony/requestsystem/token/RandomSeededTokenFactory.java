package com.minecolonies.api.colony.requestsystem.token;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.util.constant.SerializationIdentifierConstants;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.UUID;

public class RandomSeededTokenFactory extends AbstractTokenFactory<Integer>
{
    @NotNull
    @Override
    public StandardToken getNewInstance(@NotNull final Integer input)
    {
        final Random random = new Random(input);
        final UUID uuid = new UUID(random.nextLong(), random.nextLong());
        return new StandardToken(uuid);
    }

    @NotNull
    @Override
    public TypeToken<? extends Integer> getFactoryInputType()
    {
        return TypeToken.of(Integer.class);
    }

    @Override
    public short getSerializationId()
    {
        return SerializationIdentifierConstants.RANDOM_SEED_TOKEN_ID;
    }
}
