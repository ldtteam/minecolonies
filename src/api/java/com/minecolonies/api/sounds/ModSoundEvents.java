package com.minecolonies.api.sounds;

import com.minecolonies.api.colony.jobs.registry.IJobRegistry;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.mobs.RaiderType;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
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
    public static Map<String, Map<EventType, Tuple<SoundEvent, SoundEvent>>> CITIZEN_SOUND_EVENTS = new HashMap<>();

    /**
     * Saw sound event.
     */
    public static SoundEvent SAW;

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
     *
     * @param registry the registry to register at.
     */
    public static void registerSounds(final IForgeRegistry<SoundEvent> registry)
    {
        for (final JobEntry job : IJobRegistry.getInstance().getValues())
        {
            if (job.getKey().getNamespace().equals(Constants.MOD_ID) && !job.getKey().getPath().equals("placeholder"))
            {
                final Map<EventType, Tuple<SoundEvent, SoundEvent>> map = new HashMap<>();
                for (final EventType soundEvents : EventType.values())
                {
                    final SoundEvent maleSoundEvent = ModSoundEvents.getSoundID("mob." + job.getKey().getPath() + ".male." + soundEvents.name().toLowerCase(Locale.US));
                    final SoundEvent femaleSoundEvent =
                      ModSoundEvents.getSoundID("mob." + job.getKey().getPath() + ".female." + soundEvents.name().toLowerCase(Locale.US));

                    registry.register(maleSoundEvent.getLocation(), maleSoundEvent);
                    registry.register(femaleSoundEvent.getLocation(), femaleSoundEvent);
                    map.put(soundEvents, new Tuple<>(maleSoundEvent, femaleSoundEvent));
                }
                CITIZEN_SOUND_EVENTS.put(job.getKey().getPath(), map);
            }
        }

        final Map<EventType, Tuple<SoundEvent, SoundEvent>> citizenMap = new HashMap<>();
        for (final EventType soundEvents : EventType.values())
        {
            final SoundEvent maleSoundEvent = ModSoundEvents.getSoundID("mob.citizen.male." + soundEvents.name().toLowerCase(Locale.US));
            final SoundEvent femaleSoundEvent = ModSoundEvents.getSoundID("mob.citizen.female." + soundEvents.name().toLowerCase(Locale.US));

            registry.register(maleSoundEvent.getLocation(), maleSoundEvent);
            registry.register(femaleSoundEvent.getLocation(), femaleSoundEvent);
            citizenMap.put(soundEvents, new Tuple<>(maleSoundEvent, femaleSoundEvent));
        }
        CITIZEN_SOUND_EVENTS.put("citizen", citizenMap);

        final Map<EventType, Tuple<SoundEvent, SoundEvent>> childMap = new HashMap<>();
        for (final EventType soundEvents : EventType.values())
        {
            final SoundEvent maleSoundEvent = ModSoundEvents.getSoundID("mob.child.male." + soundEvents.name().toLowerCase(Locale.US));
            final SoundEvent femaleSoundEvent = ModSoundEvents.getSoundID("mob.child.female." + soundEvents.name().toLowerCase(Locale.US));

            registry.register(maleSoundEvent.getLocation(), maleSoundEvent);
            registry.register(femaleSoundEvent.getLocation(), femaleSoundEvent);
            childMap.put(soundEvents, new Tuple<>(maleSoundEvent, femaleSoundEvent));
        }
        CITIZEN_SOUND_EVENTS.put("child", childMap);

        registry.register(TavernSounds.tavernTheme.getLocation(), TavernSounds.tavernTheme);

        for (final RaiderType raiderType : RaiderType.values())
        {
            final SoundEvent raiderHurt = ModSoundEvents.getSoundID("mob." + raiderType.name().toLowerCase(Locale.US) + ".hurt");
            final SoundEvent raiderDeath = ModSoundEvents.getSoundID("mob." + raiderType.name().toLowerCase(Locale.US) + ".death");
            final SoundEvent raiderSay = ModSoundEvents.getSoundID("mob." + raiderType.name().toLowerCase(Locale.US) + ".say");

            registry.register(raiderHurt.getLocation(), raiderHurt);
            registry.register(raiderDeath.getLocation(), raiderDeath);
            registry.register(raiderSay.getLocation(), raiderSay);

            final Map<RaiderSounds.RaiderSoundTypes, SoundEvent> sounds = new HashMap<>();
            sounds.put(RaiderSounds.RaiderSoundTypes.HURT, raiderHurt);
            sounds.put(RaiderSounds.RaiderSoundTypes.DEATH, raiderDeath);
            sounds.put(RaiderSounds.RaiderSoundTypes.SAY, raiderSay);

            RaiderSounds.raiderSounds.put(raiderType, sounds);
        }

        SAW = ModSoundEvents.getSoundID("tile.sawmill.saw");
        registry.register(SAW.getLocation(), SAW);

        registry.register(RaidSounds.WARNING.getLocation(), RaidSounds.WARNING);
        registry.register(RaidSounds.WARNING_EARLY.getLocation(), RaidSounds.WARNING_EARLY);
        registry.register(RaidSounds.VICTORY.getLocation(), RaidSounds.VICTORY);
        registry.register(RaidSounds.VICTORY_EARLY.getLocation(), RaidSounds.VICTORY_EARLY);

        registry.register(RaidSounds.AMAZON_RAID.getLocation(), RaidSounds.AMAZON_RAID);

        registry.register(RaidSounds.DESERT_RAID.getLocation(), RaidSounds.DESERT_RAID);
        registry.register(RaidSounds.DESERT_RAID_WARNING.getLocation(), RaidSounds.DESERT_RAID_WARNING);

        registry.register(MercenarySounds.mercenaryAttack.getLocation(), MercenarySounds.mercenaryAttack);
        registry.register(MercenarySounds.mercenaryCelebrate.getLocation(), MercenarySounds.mercenaryCelebrate);
        registry.register(MercenarySounds.mercenaryDie.getLocation(), MercenarySounds.mercenaryDie);
        registry.register(MercenarySounds.mercenaryHurt.getLocation(), MercenarySounds.mercenaryHurt);
        registry.register(MercenarySounds.mercenarySay.getLocation(), MercenarySounds.mercenarySay);
        registry.register(MercenarySounds.mercenaryStep.getLocation(), MercenarySounds.mercenaryStep);
    }

    /**
     * Register a {@link SoundEvent}.
     *
     * @param soundName The SoundEvent's name without the minecolonies prefix
     * @return The SoundEvent
     */
    public static SoundEvent getSoundID(final String soundName)
    {
        return new SoundEvent(new ResourceLocation(Constants.MOD_ID, soundName));
    }
}
