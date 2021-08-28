package com.minecolonies.api.sounds;

import net.minecraft.sounds.SoundEvent;

/**
 * Sounds for the raids
 */
public class RaidSounds
{
    /* Raid sounds */
    public static final SoundEvent WARNING_EARLY       = ModSoundEvents.getSoundID("raid.raid_alert_early");
    public static final SoundEvent WARNING             = ModSoundEvents.getSoundID("raid.raid_alert");
    public static final SoundEvent VICTORY_EARLY       = ModSoundEvents.getSoundID("raid.raid_won_early");
    public static final SoundEvent VICTORY             = ModSoundEvents.getSoundID("raid.raid_won");
    public static final SoundEvent DESERT_RAID_VICTORY = ModSoundEvents.getSoundID("raid.desert.desert_raid_victory");
    public static final SoundEvent DESERT_RAID_WARNING = ModSoundEvents.getSoundID("raid.desert.desert_raid_warning");
    public static final SoundEvent DESERT_RAID         = ModSoundEvents.getSoundID("raid.desert.desert_raid");
    public static final SoundEvent AMAZON_RAID         = ModSoundEvents.getSoundID("raid.amazon.amazon_raid");
}
