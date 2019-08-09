package com.minecolonies.api.configuration;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.util.LanguageHandler;
import net.minecraftforge.common.ForgeConfigSpec.*;

import java.util.List;
import java.util.function.Predicate;

public abstract class AbstractConfiguration
{
    protected void createCategory(final Builder builder, final String key)
    {
        // TODO: missing name, translation not allowed for now
        builder.comment(LanguageHandler.translateKey(commentTKey(key))).push(key);
    }

    protected void swapToCategory(final Builder builder, final String key)
    {
        finishCategory(builder);
        createCategory(builder, key);
    }

    protected void finishCategory(final Builder builder)
    {
        builder.pop();
    }

    private static String nameTKey(final String key)
    {
        return Constants.MOD_ID + ".config." + key;
    }

    private static String commentTKey(final String key)
    {
        return nameTKey(key) + ".comment";
    }

    private static Builder buildBase(final Builder builder, final String key)
    {
        return builder.comment(LanguageHandler.translateKey(commentTKey(key))).translation(nameTKey(key));
    }

    protected static BooleanValue defineBoolean(final Builder builder, final String key, final boolean defaultValue)
    {
        return buildBase(builder, key).define(key, defaultValue);
    }

    protected static IntValue defineInteger(final Builder builder, final String key, final int defaultValue)
    {
        return defineInteger(builder, key, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    protected static IntValue defineInteger(final Builder builder, final String key, final int defaultValue, final int min, final int max)
    {
        return buildBase(builder, key).defineInRange(key, defaultValue, min, max);
    }

    protected static LongValue defineLong(final Builder builder, final String key, final long defaultValue)
    {
        return defineLong(builder, key, defaultValue, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    protected static LongValue defineLong(final Builder builder, final String key, final long defaultValue, final long min, final long max)
    {
        return buildBase(builder, key).defineInRange(key, defaultValue, min, max);
    }

    protected static DoubleValue defineDouble(final Builder builder, final String key, final double defaultValue)
    {
        return defineDouble(builder, key, defaultValue, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    protected static DoubleValue defineDouble(final Builder builder, final String key, final double defaultValue, final double min, final double max)
    {
        return buildBase(builder, key).defineInRange(key, defaultValue, min, max);
    }

    protected static <T> ConfigValue<List<? extends T>> defineList(
      final Builder builder,
      final String key,
      final List<? extends T> defaultValue,
      final Predicate<Object> elementValidator)
    {
        return buildBase(builder, key).defineList(key, defaultValue, elementValidator);
    }

    protected static <V extends Enum<V>> EnumValue<V> defineEnum(final Builder builder, final String key, final V defaultValue)
    {
        return buildBase(builder, key).defineEnum(key, defaultValue);
    }
}
