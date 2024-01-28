package com.minecolonies.api.sounds;

import com.minecolonies.api.entity.mobs.RaiderType;
import net.minecraft.sounds.SoundEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Asher on 12/6/17.
 */
public final class RaiderSounds
{
    /**
     * The different types.
     */
    public enum RaiderSoundTypes
    {
        SAY,
        HURT,
        DEATH
    }

    /**
     * Map of raider sounds.
     */
    public static final Map<RaiderType, Map<RaiderSoundTypes, SoundEvent>> raiderSounds = new HashMap<>();

    /**
     * Private constructor to hide the implicit public one.
     */
    private RaiderSounds()
    {

    }
}
