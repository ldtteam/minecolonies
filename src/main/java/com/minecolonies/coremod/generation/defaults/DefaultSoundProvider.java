package com.minecolonies.coremod.generation.defaults;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.minecolonies.api.colony.jobs.registry.IJobRegistry;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.sounds.EventType;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.generation.DataGeneratorConstants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.ldtteam.datagenerators.sounds.SoundsJson.createSoundJson;

public class DefaultSoundProvider implements IDataProvider
{
    private final DataGenerator generator;

    public DefaultSoundProvider(final DataGenerator generator)
    {
        this.generator = generator;
    }

    @Override
    public void act(@NotNull final DirectoryCache cache) throws IOException
    {
        JsonObject sounds = new JsonObject();

        final List<String> defaultMaleSounds = new ArrayList<>();
        defaultMaleSounds.add("minecolonies:mob/citizen/male/say1");
        defaultMaleSounds.add("minecolonies:mob/citizen/male/say2");
        defaultMaleSounds.add("minecolonies:mob/citizen/male/say3");

        final List<String> defaultFemaleSounds = new ArrayList<>();
        defaultFemaleSounds.add("minecolonies:mob/citizen/female/say1");
        defaultFemaleSounds.add("minecolonies:mob/citizen/female/say2");
        defaultFemaleSounds.add("minecolonies:mob/citizen/female/say3");

        final List<String> childSounds = new ArrayList<>();
        childSounds.add("minecolonies:mob/citizen/child/laugh1");
        childSounds.add("minecolonies:mob/citizen/child/laugh2");

        for (final JobEntry job : IJobRegistry.getInstance().getValues())
        {
            if (job.getRegistryName().getNamespace().equals(Constants.MOD_ID) && !job.getRegistryName().getPath().equals("placeholder"))
            {
                for (final EventType soundEvents : EventType.values())
                {
                    sounds.add("mob." + job.getRegistryName().getPath() + ".male." + soundEvents.name().toLowerCase(Locale.US),
                      createSoundJson("neutral", getDefaultProperties(), defaultMaleSounds));
                    sounds.add("mob." + job.getRegistryName().getPath() + ".female." + soundEvents.name().toLowerCase(Locale.US),
                      createSoundJson("neutral", getDefaultProperties(), defaultFemaleSounds));
                }
            }
        }

        for (final EventType soundEvents : EventType.values())
        {
            sounds.add("mob.citizen.male." + soundEvents.name().toLowerCase(Locale.US), createSoundJson("neutral", getDefaultProperties(), defaultMaleSounds));
            sounds.add("mob.citizen.female." + soundEvents.name().toLowerCase(Locale.US), createSoundJson("neutral", getDefaultProperties(), defaultFemaleSounds));
        }

        for (final EventType soundEvents : EventType.values())
        {
            sounds.add("mob.child.male." + soundEvents.name().toLowerCase(Locale.US), createSoundJson("neutral", getDefaultProperties(), childSounds));
            sounds.add("mob.child.female." + soundEvents.name().toLowerCase(Locale.US), createSoundJson("neutral", getDefaultProperties(), childSounds));
        }

        sounds.add("mob.barbarian.death", createSoundJson("hostile", getDefaultProperties(), ImmutableList.of("minecolonies:mob/barbarian/death")));
        sounds.add("mob.barbarian.say", createSoundJson("hostile", getDefaultProperties(), ImmutableList.of("minecolonies:mob/barbarian/say")));
        sounds.add("mob.barbarian.hurt",
          createSoundJson("hostile",
            getDefaultProperties(),
            ImmutableList.of("minecolonies:mob/barbarian/hurt1", "minecolonies:mob/barbarian/hurt2", "minecolonies:mob/barbarian/hurt3", "minecolonies:mob/barbarian/hurt4")));

        sounds.add("mob.citizen.snore", createSoundJson("neutral", getDefaultProperties(), ImmutableList.of("minecolonies:mob/citizen/snore")));

        JsonObject tavernProperties = getDefaultProperties();
        tavernProperties.addProperty("attenuation_distance", 13);
        tavernProperties.addProperty("stream", true);
        sounds.add("tile.tavern.tavern_theme", createSoundJson("music", tavernProperties, ImmutableList.of("minecolonies:tile/tavern/tavern_theme")));

        final Path savePath = generator.getOutputFolder().resolve(DataGeneratorConstants.ASSETS_DIR).resolve("sounds.json");
        IDataProvider.save(DataGeneratorConstants.GSON, cache, sounds, savePath);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "Default Sound Json Provider";
    }

    private JsonObject getDefaultProperties()
    {
        JsonObject properties = new JsonObject();
        properties.addProperty("stream", false);
        return properties;
    }
}
