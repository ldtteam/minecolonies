package com.minecolonies.api.colony.guardtype.registry;

import com.minecolonies.api.colony.guardtype.GuardType;
import net.minecraft.util.ResourceLocation;

public final class ModGuardTypes
{

    public static final ResourceLocation KNIGHT_ID = new ResourceLocation("Knight");
    public static final ResourceLocation RANGER_ID = new ResourceLocation("Ranger");
    public static       GuardType        knight;
    public static       GuardType        ranger;

    private ModGuardTypes()
    {
        throw new IllegalStateException("Tried to initialize: ModGuardTypes but this is a Utility class.");
    }
}
