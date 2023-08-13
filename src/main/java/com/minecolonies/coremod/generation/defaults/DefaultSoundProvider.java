package com.minecolonies.coremod.generation.defaults;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.IJobRegistry;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.mobs.RaiderType;
import com.minecolonies.api.sounds.EventType;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.generation.DataGeneratorConstants;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.ldtteam.datagenerators.sounds.SoundsJson.createSoundJson;
import static com.minecolonies.api.sounds.ModSoundEvents.CITIZEN_SOUND_EVENT_PREFIX;

public class DefaultSoundProvider implements DataProvider
{
    private final DataGenerator generator;
    private JsonObject sounds;

    public DefaultSoundProvider(final DataGenerator generator)
    {
        this.generator = generator;
    }

    @Override
    public void run(@NotNull final CachedOutput cache) throws IOException
    {
        sounds = new JsonObject();

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

        final List<ResourceLocation> mainTypes = new ArrayList<>(ModJobs.getJobs());
        mainTypes.remove(ModJobs.placeHolder.getId());
        mainTypes.add(new ResourceLocation(Constants.MOD_ID, "unemployed"));
        mainTypes.add(new ResourceLocation(Constants.MOD_ID, "child"));
        mainTypes.add(new ResourceLocation(Constants.MOD_ID, "visitor"));

        for (final ResourceLocation job : mainTypes)
        {
            for (final EventType event : EventType.values())
            {
                for (int i = 1; i <= 4; i++)
                {
                    sounds.add(CITIZEN_SOUND_EVENT_PREFIX + job.getPath() + ".male" + i + "." + event.getId(),
                      createSoundJson("neutral", getDefaultProperties(), defaultMaleSounds));
                    sounds.add(CITIZEN_SOUND_EVENT_PREFIX + job.getPath() + ".female" + i + "." + event.getId(),
                      createSoundJson("neutral", getDefaultProperties(), defaultFemaleSounds));
                }
            }
        }

        for (final RaiderType type : RaiderType.values())
        {
            sounds.add("mob." + type.name().toLowerCase(Locale.US) + ".death", createSoundJson("hostile", getDefaultProperties(), ImmutableList.of("minecolonies:mob/barbarian/death")));
            sounds.add("mob." + type.name().toLowerCase(Locale.US) + ".say", createSoundJson("hostile", getDefaultProperties(), ImmutableList.of("minecolonies:mob/barbarian/say")));
            
            sounds.add("mob." + type.name().toLowerCase(Locale.US) + ".hurt",
              createSoundJson("hostile",
                getDefaultProperties(),
                ImmutableList.of("minecolonies:mob/barbarian/hurt1", "minecolonies:mob/barbarian/hurt2", "minecolonies:mob/barbarian/hurt3", "minecolonies:mob/barbarian/hurt4")));
        }

        sounds.add("mob.citizen.snore", createSoundJson("neutral", getDefaultProperties(), ImmutableList.of("minecolonies:mob/citizen/snore")));

        JsonObject tavernProperties = getDefaultProperties();
        tavernProperties.addProperty("attenuation_distance", 23);
        tavernProperties.addProperty("stream", true);
        tavernProperties.addProperty("comment", "Credits to Darren Curtis - Fireside Tales");
        sounds.add("tile.tavern.tavern_theme", createSoundJson("music", tavernProperties, ImmutableList.of("minecolonies:tile/tavern/tavern_theme")));

        sounds.add("mob.mercenary.attack", createSoundJson("neutral", getDefaultProperties(), ImmutableList.of("minecolonies:mob/mercenary/attack/attack1", "minecolonies:mob/mercenary/attack/attack2", "minecolonies:mob/mercenary/attack/attack3", "minecolonies:mob/mercenary/attack/attack4")));
        sounds.add("mob.mercenary.celebrate", createSoundJson("neutral", getDefaultProperties(), ImmutableList.of("minecolonies:mob/mercenary/celebrate/celebrate1")));
        sounds.add("mob.mercenary.die", createSoundJson("neutral", getDefaultProperties(), ImmutableList.of("minecolonies:mob/mercenary/die/death1", "minecolonies:mob/mercenary/die/death2")));
        sounds.add("mob.mercenary.hurt", createSoundJson("neutral", getDefaultProperties(), ImmutableList.of("minecolonies:mob/mercenary/hurt/hurt1", "minecolonies:mob/mercenary/hurt/hurt2", "minecolonies:mob/mercenary/hurt/hurt3")));
        sounds.add("mob.mercenary.say", createSoundJson("neutral", getDefaultProperties(), ImmutableList.of("minecolonies:mob/mercenary/say/say1", "minecolonies:mob/mercenary/say/say2", "minecolonies:mob/mercenary/say/say3")));
        sounds.add("mob.mercenary.step", createSoundJson("neutral", getDefaultProperties(), ImmutableList.of("minecolonies:mob/mercenary/step/step1", "minecolonies:mob/mercenary/step/step2", "minecolonies:mob/mercenary/step/step3", "minecolonies:mob/mercenary/step/step4")));
        sounds.add("tile.sawmill.saw", createSoundJson("neutral", getDefaultProperties(), ImmutableList.of("minecolonies:tile/sawmill/saw")));

        add("record", false,
          "raid.raid_alert",
          "raid.raid_alert_early",
          "raid.raid_won",
          "raid.raid_won_early");

        add("music", true,
          "raid.desert.desert_raid",
          "raid.desert.desert_raid_warning",
          "raid.desert.desert_raid_victory",
          "raid.amazon.amazon_raid");

        final Path savePath = generator.getOutputFolder().resolve(DataGeneratorConstants.ASSETS_DIR).resolve("sounds.json");
        DataProvider.saveStable(cache, sounds, savePath);
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

    private void add(String category, boolean stream, String... ids)
    {
        for (String id : ids)
        {
            JsonObject obj = new JsonObject();
            obj.addProperty("stream", stream);
            sounds.add(id, createSoundJson(category, obj, ImmutableList.of(Constants.MOD_ID+":"+id.replace(".", "/"))));
        }
    }
}
