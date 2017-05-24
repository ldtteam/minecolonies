package com.minecolonies.coremod.sounds;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Registering of sound events for our colony.
 */
public final class ModSoundEvents
{
    /**
     * List of sound handlers.
     */
    public static final List<AbstractWorkerSounds> handlers = new ArrayList<>();

    /**
     * Private constructor to hide the implicit public one.
     */
    private ModSoundEvents()
    {
        /*
         * Intentionally left empty.
         */
    }

    static
    {
        handlers.add(new FishermanSounds());
        handlers.add(new DeliverymanSounds());
        handlers.add(new CitizenSounds());
    }

    /**
     * Register the {@link SoundEvent}s.
     */
    public static void registerSounds()
    {

        GameRegistry.register(FishermanSounds.Female.iGotOne);
        GameRegistry.register(FishermanSounds.Female.badWeather);
        GameRegistry.register(FishermanSounds.Female.needFishingRod);
        GameRegistry.register(FishermanSounds.Female.offToBed);
        GameRegistry.register(FishermanSounds.Female.generalPhrases);
        GameRegistry.register(FishermanSounds.Female.noises);

        GameRegistry.register(FishermanSounds.Male.iGotOne);
        GameRegistry.register(FishermanSounds.Male.badWeather);
        GameRegistry.register(FishermanSounds.Male.needFishingRod);
        GameRegistry.register(FishermanSounds.Male.offToBed);
        GameRegistry.register(FishermanSounds.Male.generalPhrases);
        GameRegistry.register(FishermanSounds.Male.noises);

        GameRegistry.register(DeliverymanSounds.Female.hostile);
        GameRegistry.register(DeliverymanSounds.Female.saturationHigh);
        GameRegistry.register(DeliverymanSounds.Female.saturationLow);
        GameRegistry.register(DeliverymanSounds.Female.saturationVeryLow);
        GameRegistry.register(DeliverymanSounds.Female.badWeather);
        GameRegistry.register(DeliverymanSounds.Female.offToBed);
        GameRegistry.register(DeliverymanSounds.Female.generalPhrases);
        GameRegistry.register(DeliverymanSounds.Female.noises);

        GameRegistry.register(CitizenSounds.Female.say);
        GameRegistry.register(CitizenSounds.Male.say);
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
