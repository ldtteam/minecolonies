package com.minecolonies.sounds;

import com.minecolonies.lib.Constants;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Registering of sound events for our colony.
 */
public class ModSoundEvents
{
    /**
     * Register the {@link SoundEvent}s.
     */
    public static void registerSounds()
    {

        FishermanSounds.Female.iGotOne = registerSound("mob.fisherman.female.iGotOne");
        FishermanSounds.Female.badWeather = registerSound("mob.fisherman.female.badWeather");
        FishermanSounds.Female.needFishingRod = registerSound("mob.fisherman.female.needFishingRod");
        FishermanSounds.Female.offToBed = registerSound("mob.fisherman.female.offToBed");
        FishermanSounds.Female.generalPhrases = registerSound("mob.fisherman.female.generalPhrases");
        FishermanSounds.Female.noises = registerSound("mob.fisherman.female.noise");


        FishermanSounds.Male.iGotOne = registerSound("mob.fisherman.male.iGotOne");
        FishermanSounds.Male.badWeather = registerSound("mob.fisherman.male.badWeather");
        FishermanSounds.Male.needFishingRod = registerSound("mob.fisherman.male.needFishingRod");
        FishermanSounds.Male.offToBed = registerSound("mob.fisherman.male.offToBed");
        FishermanSounds.Male.generalPhrases = registerSound("mob.fisherman.male.generalPhrases");
        FishermanSounds.Male.noises = registerSound("mob.fisherman.male.noise");


        CitizenSounds.Female.say = registerSound("mob.citizen.female.say");
        CitizenSounds.Male.say = registerSound("mob.citizen.male.say");
    }

    /**
     * Register a {@link SoundEvent}.
     *
     * @param soundName The SoundEvent's name without the minecolonies prefix
     * @return The SoundEvent
     */
    private static SoundEvent registerSound(String soundName)
    {
        final ResourceLocation soundID = new ResourceLocation(Constants.MOD_ID, soundName);
        return GameRegistry.register(new SoundEvent(soundID).setRegistryName(soundID));
    }
}
