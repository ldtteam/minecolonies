package com.minecolonies.api.sounds;

import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.IJobRegistry;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Registering of sound events for our colony.
 */
public final class ModSoundEvents
{
    /**
     * Map of sound events.
     */
    public static Map<String, Map<EventType, Tuple<SoundEvent, SoundEvent>>> SOUND_EVENTS = new HashMap<>();

    /**
     * Private constructor to hide the implicit public one.
     */
    private ModSoundEvents()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Register the {@link SoundEvent}s.
     * @param registry the registry to register at.
     */
    public static void registerSounds(final IForgeRegistry<SoundEvent> registry)
    {

        for (final JobEntry job : IJobRegistry.getInstance().getValues())
        {
            if (job.getRegistryName().getNamespace().equals(Constants.MOD_ID) && !job.getRegistryName().getPath().equals("placeholder"))
            {
                final Map<EventType, Tuple<SoundEvent, SoundEvent>> map = new HashMap<>();
                for (final EventType soundEvents : EventType.values())
                {
                    final SoundEvent maleSoundEvent = ModSoundEvents.getSoundID("mob." + job.getRegistryName().getPath() + ".male." + soundEvents.name().toLowerCase(Locale.US));
                    final SoundEvent femaleSoundEvent =
                      ModSoundEvents.getSoundID("mob." + job.getRegistryName().getPath() + ".female." + soundEvents.name().toLowerCase(Locale.US));

                    registry.register(maleSoundEvent);
                    registry.register(femaleSoundEvent);
                    map.put(soundEvents, new Tuple<>(maleSoundEvent, femaleSoundEvent));
                }
                SOUND_EVENTS.put(job.getRegistryName().getPath(), map);
            }
        }

        final Map<EventType, Tuple<SoundEvent, SoundEvent>> citizenMap = new HashMap<>();
        for (final EventType soundEvents : EventType.values())
        {
            final SoundEvent maleSoundEvent = ModSoundEvents.getSoundID("mob.citizen.male." + soundEvents.name().toLowerCase(Locale.US));
            final SoundEvent femaleSoundEvent = ModSoundEvents.getSoundID("mob.citizen.female." + soundEvents.name().toLowerCase(Locale.US));

            registry.register(maleSoundEvent);
            registry.register(femaleSoundEvent);
            citizenMap.put(soundEvents, new Tuple<>(maleSoundEvent, femaleSoundEvent));
        }
        SOUND_EVENTS.put("citizen", citizenMap);

        final Map<EventType, Tuple<SoundEvent, SoundEvent>> childMap = new HashMap<>();
        for (final EventType soundEvents : EventType.values())
        {
            final SoundEvent maleSoundEvent = ModSoundEvents.getSoundID("mob.child.male." + soundEvents.name().toLowerCase(Locale.US));
            final SoundEvent femaleSoundEvent = ModSoundEvents.getSoundID("mob.child.female." + soundEvents.name().toLowerCase(Locale.US));

            registry.register(maleSoundEvent);
            registry.register(femaleSoundEvent);
            childMap.put(soundEvents, new Tuple<>(maleSoundEvent, femaleSoundEvent));
        }
        SOUND_EVENTS.put("child", childMap);

        registry.register(BarbarianSounds.barbarianHurt);
        registry.register(BarbarianSounds.barbarianDeath);
        registry.register(BarbarianSounds.barbarianSay);
    }

    /**
     * Register a {@link SoundEvent}.
     *
     * @param soundName The SoundEvent's name without the minecolonies prefix
     * @return The SoundEvent
     */
    public static SoundEvent getSoundID(final String soundName)
    {
        return new SoundEvent(new ResourceLocation(Constants.MOD_ID, soundName)).setRegistryName(soundName);
    }
}
