package com.minecolonies.api.configuration;

import com.minecolonies.api.util.constant.NameConstants;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.List;

/**
 * Mod server configuration.
 * Loaded serverside, synced on connection.
 */
public class ServerConfiguration extends AbstractConfiguration
{
    public final ForgeConfigSpec.BooleanValue useMiddleInitial;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> maleFirstNames;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> femaleFirstNames;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> lastNames;

    /**
     * Builds server configuration.
     *
     * @param builder config builder
     */
    protected ServerConfiguration(final ForgeConfigSpec.Builder builder)
    {
        createCategory(builder, "names");

        useMiddleInitial = defineBoolean(builder, "usemiddleinitial", true);

        maleFirstNames = defineList(builder, "malefirstnames", Arrays.asList(NameConstants.maleFirstNames), s -> s instanceof String);

        femaleFirstNames = defineList(builder, "femalefirstnames", Arrays.asList(NameConstants.femaleFirstNames), s -> s instanceof String);

        lastNames = defineList(builder, "lastnames", Arrays.asList(NameConstants.lastNames), s -> s instanceof String);

        finishCategory(builder);
    }
}
