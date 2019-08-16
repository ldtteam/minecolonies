package com.minecolonies.coremod.util;

import com.google.gson.JsonObject;
import com.minecolonies.coremod.MineColonies;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.IConditionSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

/**
 * Custom config conditions!
 */
public class ConfigCondition implements IConditionSerializer
{
    public ConfigCondition()
    {
        super();
    }

    @NotNull
    @Override
    public BooleanSupplier parse(@NotNull final JsonObject json)
    {
        if(!MineColonies.getConfig().getCommon().supplyChests.get())
        {
            return () -> false;
        }

        return () -> true;
    }
}
