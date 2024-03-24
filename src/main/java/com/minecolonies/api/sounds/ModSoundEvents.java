package com.minecolonies.api.sounds;

import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.entity.mobs.RaiderType;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.*;

/**
 * Registering of sound events for our colony.
 */
public final class ModSoundEvents
{
    /**
     * Citizen sound prefix.
     */
    public static final String CITIZEN_SOUND_EVENT_PREFIX = "citizen.";

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, Constants.MOD_ID);

    /**
     * Map of sound events.
     */
    public static Map<String, Map<EventType, List<Tuple<SoundEvent, SoundEvent>>>> CITIZEN_SOUND_EVENTS = new HashMap<>();

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
    static
    {
        final List<ResourceLocation> mainTypes = new ArrayList<>(ModJobs.getJobs());
        mainTypes.remove(ModJobs.placeHolder.getId());
        mainTypes.add(new ResourceLocation(Constants.MOD_ID, "unemployed"));
        mainTypes.add(new ResourceLocation(Constants.MOD_ID, "visitor"));
        mainTypes.add(new ResourceLocation(Constants.MOD_ID, "child"));

        for (final ResourceLocation job : mainTypes)
        {
            final Map<EventType, List<Tuple<SoundEvent, SoundEvent>>> map = new HashMap<>();
            for (final EventType event : EventType.values())
            {
                final List<Tuple<SoundEvent, SoundEvent>> individualSounds = new ArrayList<>();
                for (int i = 1; i <= 4; i++)
                {
                    final SoundEvent maleSoundEvent =
                      ModSoundEvents.getSoundID(CITIZEN_SOUND_EVENT_PREFIX + job.getPath() + ".male" + i + "." + event.getId());
                    final SoundEvent femaleSoundEvent =
                      ModSoundEvents.getSoundID(CITIZEN_SOUND_EVENT_PREFIX + job.getPath() + ".female" + i + "." + event.getId());

                    SOUND_EVENTS.register(maleSoundEvent.getLocation().getPath(), () -> maleSoundEvent);
                    SOUND_EVENTS.register(femaleSoundEvent.getLocation().getPath(), () -> femaleSoundEvent);
                    individualSounds.add(new Tuple<>(maleSoundEvent, femaleSoundEvent));
                }
                map.put(event, individualSounds);
            }
            CITIZEN_SOUND_EVENTS.put(job.getPath(), map);
        }

        SOUND_EVENTS.register(TavernSounds.tavernTheme.getLocation().getPath(), () -> TavernSounds.tavernTheme);

        for (final RaiderType raiderType : RaiderType.values())
        {
            final SoundEvent raiderHurt = ModSoundEvents.getSoundID("mob." + raiderType.name().toLowerCase(Locale.US) + ".hurt");
            final SoundEvent raiderDeath = ModSoundEvents.getSoundID("mob." + raiderType.name().toLowerCase(Locale.US) + ".death");
            final SoundEvent raiderSay = ModSoundEvents.getSoundID("mob." + raiderType.name().toLowerCase(Locale.US) + ".say");

            SOUND_EVENTS.register(raiderHurt.getLocation().getPath(), () ->  raiderHurt);
            SOUND_EVENTS.register(raiderDeath.getLocation().getPath(), () ->  raiderDeath);
            SOUND_EVENTS.register(raiderSay.getLocation().getPath(), () ->  raiderSay);

            final Map<RaiderSounds.RaiderSoundTypes, SoundEvent> sounds = new HashMap<>();
            sounds.put(RaiderSounds.RaiderSoundTypes.HURT, raiderHurt);
            sounds.put(RaiderSounds.RaiderSoundTypes.DEATH, raiderDeath);
            sounds.put(RaiderSounds.RaiderSoundTypes.SAY, raiderSay);

            RaiderSounds.raiderSounds.put(raiderType, sounds);
        }

        SAW = ModSoundEvents.getSoundID("tile.sawmill.saw");
        SOUND_EVENTS.register(SAW.getLocation().getPath(), () -> SAW);

        SOUND_EVENTS.register(RaidSounds.WARNING.getLocation().getPath(), () -> RaidSounds.WARNING);
        SOUND_EVENTS.register(RaidSounds.WARNING_EARLY.getLocation().getPath(), () -> RaidSounds.WARNING_EARLY);
        SOUND_EVENTS.register(RaidSounds.VICTORY.getLocation().getPath(), () -> RaidSounds.VICTORY);
        SOUND_EVENTS.register(RaidSounds.VICTORY_EARLY.getLocation().getPath(), () -> RaidSounds.VICTORY_EARLY);

        SOUND_EVENTS.register(RaidSounds.AMAZON_RAID.getLocation().getPath(), () -> RaidSounds.AMAZON_RAID);

        SOUND_EVENTS.register(RaidSounds.DESERT_RAID.getLocation().getPath(), () -> RaidSounds.DESERT_RAID);
        SOUND_EVENTS.register(RaidSounds.DESERT_RAID_WARNING.getLocation().getPath(), () -> RaidSounds.DESERT_RAID_WARNING);

        SOUND_EVENTS.register(MercenarySounds.mercenaryAttack.getLocation().getPath(), () -> MercenarySounds.mercenaryAttack);
        SOUND_EVENTS.register(MercenarySounds.mercenaryCelebrate.getLocation().getPath(), () -> MercenarySounds.mercenaryCelebrate);
        SOUND_EVENTS.register(MercenarySounds.mercenaryDie.getLocation().getPath(), () -> MercenarySounds.mercenaryDie);
        SOUND_EVENTS.register(MercenarySounds.mercenaryHurt.getLocation().getPath(), () -> MercenarySounds.mercenaryHurt);
        SOUND_EVENTS.register(MercenarySounds.mercenarySay.getLocation().getPath(), () -> MercenarySounds.mercenarySay);
        SOUND_EVENTS.register(MercenarySounds.mercenaryStep.getLocation().getPath(), () -> MercenarySounds.mercenaryStep);
    }

    /**
     * Register a {@link SoundEvent}.
     *
     * @param soundName The SoundEvent's name without the minecolonies prefix
     * @return The SoundEvent
     */
    public static SoundEvent getSoundID(final String soundName)
    {
        return SoundEvent.createVariableRangeEvent(new ResourceLocation(Constants.MOD_ID, soundName));
    }
}
