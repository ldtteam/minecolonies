package com.minecolonies.api.colony.guardtype.registry;

import com.minecolonies.api.colony.guardtype.GuardType;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;

public final class ModGuardTypes
{

    public static final ResourceLocation KNIGHT_ID = new ResourceLocation(Constants.MOD_ID, "knight");
    public static final ResourceLocation RANGER_ID = new ResourceLocation(Constants.MOD_ID, "ranger");
    public static final ResourceLocation DRUID_ID  = new ResourceLocation(Constants.MOD_ID, "druid");
    public static final ResourceLocation CAVALRY_ID = new ResourceLocation(Constants.MOD_ID, "cavalry");

    public static RegistryObject<GuardType> knight;
    public static RegistryObject<GuardType> ranger;
    public static RegistryObject<GuardType> druid;
    public static RegistryObject<GuardType> cavalry;

    private ModGuardTypes()
    {
        throw new IllegalStateException("Tried to initialize: ModGuardTypes but this is a Utility class.");
    }
}
