package com.minecolonies.coremod.generation.defaults;

import com.google.common.collect.ImmutableList;
import com.ldtteam.datagenerators.sounds.SoundsJson;
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
import java.util.*;

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
        final Map<String[], List<String>> map = new LinkedHashMap<>();

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
                    map.put(new String[]{"mob." + job.getRegistryName().getPath() + ".male." + soundEvents.name().toLowerCase(Locale.US), "neutral"}, defaultMaleSounds);
                    map.put(new String[]{"mob." + job.getRegistryName().getPath() + ".female." + soundEvents.name().toLowerCase(Locale.US), "neutral"}, defaultFemaleSounds);
                }
            }
        }

        for (final EventType soundEvents : EventType.values())
        {
            map.put(new String[]{"mob.citizen.male." + soundEvents.name().toLowerCase(Locale.US), "neutral"}, defaultMaleSounds);
            map.put(new String[]{"mob.citizen.female." + soundEvents.name().toLowerCase(Locale.US), "neutral"}, defaultFemaleSounds);
        }

        for (final EventType soundEvents : EventType.values())
        {
            map.put(new String[]{"mob.child.male." + soundEvents.name().toLowerCase(Locale.US), "neutral"}, childSounds);
            map.put(new String[]{"mob.child.female." + soundEvents.name().toLowerCase(Locale.US), "neutral"}, childSounds);
        }

        map.put(new String[]{"mob.barbarian.death", "hostile"}, ImmutableList.of("minecolonies:mob/barbarian/death"));
        map.put(new String[]{"mob.barbarian.say", "hostile"}, ImmutableList.of("minecolonies:mob/barbarian/say"));
        map.put(new String[]{"mob.barbarian.hurt", "hostile"}, ImmutableList.of("minecolonies:mob/barbarian/hurt1", "minecolonies:mob/barbarian/hurt2", "minecolonies:mob/barbarian/hurt3", "minecolonies:mob/barbarian/hurt4"));

        map.put(new String[]{"mob.citizen.snore", "neutral"}, ImmutableList.of("minecolonies:mob/citizen/snore"));

        final SoundsJson soundJson = new SoundsJson(map);
        final Path savePath = generator.getOutputFolder().resolve(DataGeneratorConstants.ASSETS_DIR).resolve("sounds.json");
        IDataProvider.save(DataGeneratorConstants.GSON, cache, soundJson.serialize(), savePath);
    }

    @NotNull
    @Override
    public String getName()
    {
        return "Default Sound Json Provider";
    }
}
