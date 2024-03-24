package com.minecolonies.api.colony.guardtype.registry;

import com.minecolonies.api.colony.guardtype.GuardType;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModGuardTypes
{

    public static final ResourceLocation KNIGHT_ID = new ResourceLocation(Constants.MOD_ID, "knight");
    public static final ResourceLocation RANGER_ID = new ResourceLocation(Constants.MOD_ID, "ranger");
    public static final ResourceLocation DRUID_ID  = new ResourceLocation(Constants.MOD_ID, "druid");

    public static DeferredHolder<GuardType, GuardType> knight;
    public static DeferredHolder<GuardType, GuardType> ranger;
    public static DeferredHolder<GuardType, GuardType> druid;

    private ModGuardTypes()
    {
        throw new IllegalStateException("Tried to initialize: ModGuardTypes but this is a Utility class.");
    }
}
