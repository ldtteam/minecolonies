package com.minecolonies.coremod.colony.testutils;

import com.minecolonies.api.colony.IColony;
import org.mockito.Mockito;

public final class ColonyMock
{

    private ColonyMock()
    {
        throw new IllegalStateException("Tried to initialize: ColonyMock but this is a Utility class.");
    }

    public static final IColony mockBlank()
    {
        final IColony colony = Mockito.mock(IColony.class);

        return colony;
    }
}
