package com.minecolonies.api.colony.raids.registry;
import com.minecolonies.api.colony.raids.RaidType;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.util.ResourceLocation;

public class ModRaidTypes
{
    public static final ResourceLocation PIRATE_ID    = new ResourceLocation(Constants.MOD_ID, "pirate");
    public static final ResourceLocation BARBARIAN_ID = new ResourceLocation(Constants.MOD_ID, "barbarian");
    public static final ResourceLocation EGYPTIAN_ID  = new ResourceLocation(Constants.MOD_ID, "egyptian");
    public static final ResourceLocation AMAZON_ID    = new ResourceLocation(Constants.MOD_ID, "amazon");
    public static final ResourceLocation NORSEMEN_ID  = new ResourceLocation(Constants.MOD_ID, "norsemen");
    public static       RaidType         pirate;
    public static       RaidType         barbarian;
    public static       RaidType         egyptian;
    public static       RaidType         amazon;
    public static       RaidType         norsemen;

    private ModRaidTypes()
    {
        throw new IllegalStateException("Tried to initialize: ModRaidTypes but this is a Utility class.");
    }
}
