package com.minecolonies.coremod.util;

import com.google.gson.JsonObject;
import com.minecolonies.coremod.MineColonies;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.common.crafting.IConditionSerializer;

import java.util.function.BooleanSupplier;

/**
 * Custom config conditions!
 */
public class ConfigCondition implements IConditionSerializer
{
    /**
     * Supplyship string.
     */
    private static final String SUPPLIES = "supply";

    /**
     * Supplyship string.
     */
    private static final String IN_DEV = "inDev";

    @Override
    public BooleanSupplier parse(final JsonObject json)
    {
        final String value = JSONUtils.getString(json , "key");

        if(!MineColonies.getConfig().getCommon().enableInDevelopmentFeatures.get() && IN_DEV.equalsIgnoreCase(value))
        {
            return () -> false;
        }

        if(!MineColonies.getConfig().getCommon().supplyChests.get() && SUPPLIES.equalsIgnoreCase(value))
        {
            return () -> false;
        }

        return () -> true;
    }
}
