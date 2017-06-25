package com.minecolonies.coremod.sounds;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.registries.IForgeRegistry;

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
        handlers.add(new FarmerSounds());
    }

    /**
     * Register the {@link SoundEvent}s.
     * @param registry the registry to register at.
     */
    public static void registerSounds(final IForgeRegistry<SoundEvent> registry)
    {
        registry.register(FishermanSounds.Female.iGotOne);
        registry.register(FishermanSounds.Female.badWeather);
        registry.register(FishermanSounds.Female.needFishingRod);
        registry.register(FishermanSounds.Female.offToBed);
        registry.register(FishermanSounds.Female.generalPhrases);
        registry.register(FishermanSounds.Female.noises);

        registry.register(FishermanSounds.Male.iGotOne);
        registry.register(FishermanSounds.Male.badWeather);
        registry.register(FishermanSounds.Male.needFishingRod);
        registry.register(FishermanSounds.Male.offToBed);
        registry.register(FishermanSounds.Male.generalPhrases);
        registry.register(FishermanSounds.Male.noises);

        registry.register(DeliverymanSounds.Female.hostile);
        registry.register(DeliverymanSounds.Female.saturationHigh);
        registry.register(DeliverymanSounds.Female.saturationLow);
        registry.register(DeliverymanSounds.Female.saturationVeryLow);
        registry.register(DeliverymanSounds.Female.badWeather);
        registry.register(DeliverymanSounds.Female.offToBed);
        registry.register(DeliverymanSounds.Female.generalPhrases);
        registry.register(DeliverymanSounds.Female.noises);

        registry.register(FarmerSounds.Female.hostile);
        registry.register(FarmerSounds.Female.saturationHigh);
        registry.register(FarmerSounds.Female.saturationLow);
        registry.register(FarmerSounds.Female.saturationVeryLow);
        registry.register(FarmerSounds.Female.badWeather);
        registry.register(FarmerSounds.Female.offToBed);
        registry.register(FarmerSounds.Female.generalPhrases);
        registry.register(FarmerSounds.Female.noises);
        registry.register(FarmerSounds.Female.greeting);
        registry.register(FarmerSounds.Female.farewell);
        registry.register(FarmerSounds.Female.interaction);

        registry.register(CitizenSounds.Female.say);
        registry.register(CitizenSounds.Male.say);
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
