package com.minecolonies.util;

import com.minecolonies.lib.Constants;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Registering of sound events for our colony.
 */
public class ModSoundEvents
{
    public static SoundEvent callingItADay;

    /**
     * Register the {@link SoundEvent}s.
     */
    public static void registerSounds()
    {
        callingItADay = registerSound("mob.fisherman.female.callingItADay");
    }

    /**
     * Register a {@link SoundEvent}.
     *
     * @param soundName The SoundEvent's name without the testmod3 prefix
     * @return The SoundEvent
     */
    private static SoundEvent registerSound(String soundName)
    {
        final ResourceLocation soundID = new ResourceLocation(Constants.MOD_ID, soundName);
        return GameRegistry.register(new SoundEvent(soundID).setRegistryName(soundID));
    }
}
