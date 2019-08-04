package com.minecolonies.coremod.util;

import com.google.gson.JsonObject;
import com.minecolonies.api.configuration.Configurations;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.function.BooleanSupplier;

/**
 * Custom config conditions!
 */
public class ConfigCondition implements IConditionFactory
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
    public BooleanSupplier parse(final JsonContext context, final JsonObject json)
    {
        final String value = JsonUtils.getString(json , "key");

        if(!MineColonies.getConfig().getCommon().gameplay.enableInDevelopmentFeatures && IN_DEV.equalsIgnoreCase(value))
        {
            return () -> false;
        }

        if(!MineColonies.getConfig().getCommon().gameplay.supplyChests && SUPPLIES.equalsIgnoreCase(value))
        {
            return () -> false;
        }

        return () -> true;
    }
}
