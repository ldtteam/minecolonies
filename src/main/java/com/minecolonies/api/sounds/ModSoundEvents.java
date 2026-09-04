package com.minecolonies.api.sounds;

import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.entity.mobs.RaiderType;
import com.ldtteam.structurize.api.util.Tuple;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.*;

import static com.minecolonies.core.generation.SoundsJson.createSoundJson;

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
        final List<Identifier> mainTypes = new ArrayList<>(ModJobs.getJobs());
        mainTypes.remove(ModJobs.placeHolder.getId());
        mainTypes.add(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "unemployed"));
        mainTypes.add(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "visitor"));

        for (final Identifier job : mainTypes)
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

                    SOUND_EVENTS.register(maleSoundEvent.location().getPath(), () -> maleSoundEvent);
                    SOUND_EVENTS.register(femaleSoundEvent.location().getPath(), () -> femaleSoundEvent);
                    individualSounds.add(new Tuple<>(maleSoundEvent, femaleSoundEvent));
                }
                map.put(event, individualSounds);
            }
            CITIZEN_SOUND_EVENTS.put(job.getPath(), map);
        }

        final Map<EventType, List<Tuple<SoundEvent, SoundEvent>>> map = new HashMap<>();
        for (final EventType event : EventType.values())
        {
            final List<Tuple<SoundEvent, SoundEvent>> individualSounds = new ArrayList<>();
            for (int i = 1; i <= 2; i++)
            {
                final SoundEvent maleSoundEvent =
                        ModSoundEvents.getSoundID(CITIZEN_SOUND_EVENT_PREFIX + "child.male" + i + "." + event.getId());
                final SoundEvent femaleSoundEvent =
                        ModSoundEvents.getSoundID(CITIZEN_SOUND_EVENT_PREFIX + "child.female" + i + "." + event.getId());

                individualSounds.add(new Tuple<>(maleSoundEvent, femaleSoundEvent));
                individualSounds.add(new Tuple<>(maleSoundEvent, femaleSoundEvent));
            }
            map.put(event, individualSounds);
        }
        CITIZEN_SOUND_EVENTS.put("child", map);

        SOUND_EVENTS.register(TavernSounds.tavernTheme.location().getPath(), () -> TavernSounds.tavernTheme);

        for (final RaiderType raiderType : RaiderType.values())
        {
            final SoundEvent raiderHurt = ModSoundEvents.getSoundID("mob." + raiderType.name().toLowerCase(Locale.US) + ".hurt");
            final SoundEvent raiderDeath = ModSoundEvents.getSoundID("mob." + raiderType.name().toLowerCase(Locale.US) + ".death");
            final SoundEvent raiderSay = ModSoundEvents.getSoundID("mob." + raiderType.name().toLowerCase(Locale.US) + ".say");

            SOUND_EVENTS.register(raiderHurt.location().getPath(), () ->  raiderHurt);
            SOUND_EVENTS.register(raiderDeath.location().getPath(), () ->  raiderDeath);
            SOUND_EVENTS.register(raiderSay.location().getPath(), () ->  raiderSay);

            final Map<RaiderSounds.RaiderSoundTypes, SoundEvent> sounds = new HashMap<>();
            sounds.put(RaiderSounds.RaiderSoundTypes.HURT, raiderHurt);
            sounds.put(RaiderSounds.RaiderSoundTypes.DEATH, raiderDeath);
            sounds.put(RaiderSounds.RaiderSoundTypes.SAY, raiderSay);

            RaiderSounds.raiderSounds.put(raiderType, sounds);
        }

        SAW = ModSoundEvents.getSoundID("tile.sawmill.saw");
        SOUND_EVENTS.register(SAW.location().getPath(), () -> SAW);

        SOUND_EVENTS.register(RaidSounds.WARNING.location().getPath(), () -> RaidSounds.WARNING);
        SOUND_EVENTS.register(RaidSounds.WARNING_EARLY.location().getPath(), () -> RaidSounds.WARNING_EARLY);
        SOUND_EVENTS.register(RaidSounds.VICTORY.location().getPath(), () -> RaidSounds.VICTORY);
        SOUND_EVENTS.register(RaidSounds.VICTORY_EARLY.location().getPath(), () -> RaidSounds.VICTORY_EARLY);

        SOUND_EVENTS.register(RaidSounds.AMAZON_RAID.location().getPath(), () -> RaidSounds.AMAZON_RAID);

        SOUND_EVENTS.register(RaidSounds.DESERT_RAID.location().getPath(), () -> RaidSounds.DESERT_RAID);
        SOUND_EVENTS.register(RaidSounds.DESERT_RAID_WARNING.location().getPath(), () -> RaidSounds.DESERT_RAID_WARNING);

        SOUND_EVENTS.register(MercenarySounds.mercenaryAttack.location().getPath(), () -> MercenarySounds.mercenaryAttack);
        SOUND_EVENTS.register(MercenarySounds.mercenaryCelebrate.location().getPath(), () -> MercenarySounds.mercenaryCelebrate);
        SOUND_EVENTS.register(MercenarySounds.mercenaryDie.location().getPath(), () -> MercenarySounds.mercenaryDie);
        SOUND_EVENTS.register(MercenarySounds.mercenaryHurt.location().getPath(), () -> MercenarySounds.mercenaryHurt);
        SOUND_EVENTS.register(MercenarySounds.mercenarySay.location().getPath(), () -> MercenarySounds.mercenarySay);
        SOUND_EVENTS.register(MercenarySounds.mercenaryStep.location().getPath(), () -> MercenarySounds.mercenaryStep);
    }

    /**
     * Register a {@link SoundEvent}.
     *
     * @param soundName The SoundEvent's name without the minecolonies prefix
     * @return The SoundEvent
     */
    public static SoundEvent getSoundID(final String soundName)
    {
        return SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(Constants.MOD_ID, soundName));
    }
}
