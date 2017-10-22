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
    static
    {
        handlers.add(new FishermanSounds());
        handlers.add(new DeliverymanSounds());
        handlers.add(new CitizenSounds());
        handlers.add(new FarmerSounds());
        handlers.add(new KnightSounds());
        handlers.add(new ArcherSounds());
        handlers.add(new BakerSounds());
        handlers.add(new BuilderSounds());

    }

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

        GameRegistry.register(FarmerSounds.Female.hostile);
        GameRegistry.register(FarmerSounds.Female.saturationHigh);
        GameRegistry.register(FarmerSounds.Female.saturationLow);
        GameRegistry.register(FarmerSounds.Female.saturationVeryLow);
        GameRegistry.register(FarmerSounds.Female.badWeather);
        GameRegistry.register(FarmerSounds.Female.offToBed);
        GameRegistry.register(FarmerSounds.Female.generalPhrases);
        GameRegistry.register(FarmerSounds.Female.noises);
        GameRegistry.register(FarmerSounds.Female.greeting);
        GameRegistry.register(FarmerSounds.Female.farewell);
        GameRegistry.register(FarmerSounds.Female.interaction);

        GameRegistry.register(KnightSounds.Male.badHousing);
        GameRegistry.register(KnightSounds.Male.levelUp);
        GameRegistry.register(KnightSounds.Male.saturationHigh);
        GameRegistry.register(KnightSounds.Male.saturationLow);
        GameRegistry.register(KnightSounds.Male.saturationVeryLow);
        GameRegistry.register(KnightSounds.Male.badWeather);
        GameRegistry.register(KnightSounds.Male.offToBed);
        GameRegistry.register(KnightSounds.Male.generalPhrases);
        GameRegistry.register(KnightSounds.Male.noises);
        GameRegistry.register(KnightSounds.Male.greeting);
        GameRegistry.register(KnightSounds.Male.farewell);
        GameRegistry.register(KnightSounds.Male.interaction);

        GameRegistry.register(ArcherSounds.Female.badHousing);
        GameRegistry.register(ArcherSounds.Female.levelUp);
        GameRegistry.register(ArcherSounds.Female.saturationHigh);
        GameRegistry.register(ArcherSounds.Female.saturationLow);
        GameRegistry.register(ArcherSounds.Female.saturationVeryLow);
        GameRegistry.register(ArcherSounds.Female.badWeather);
        GameRegistry.register(ArcherSounds.Female.offToBed);
        GameRegistry.register(ArcherSounds.Female.generalPhrases);
        GameRegistry.register(ArcherSounds.Female.noises);
        GameRegistry.register(ArcherSounds.Female.greeting);
        GameRegistry.register(ArcherSounds.Female.farewell);
        GameRegistry.register(ArcherSounds.Female.interaction);

        GameRegistry.register(CitizenSounds.Female.say);
        GameRegistry.register(CitizenSounds.Male.say);

        GameRegistry.register(BakerSounds.Female.hostile);
        GameRegistry.register(BakerSounds.Female.saturationHigh);
        GameRegistry.register(BakerSounds.Female.saturationLow);
        GameRegistry.register(BakerSounds.Female.saturationVeryLow);
        GameRegistry.register(BakerSounds.Female.badWeather);
        GameRegistry.register(BakerSounds.Female.offToBed);
        GameRegistry.register(BakerSounds.Female.generalPhrases);
        GameRegistry.register(BakerSounds.Female.noises);
        GameRegistry.register(BakerSounds.Female.greeting);
        GameRegistry.register(BakerSounds.Female.farewell);
        GameRegistry.register(BakerSounds.Female.interaction);

        GameRegistry.register(BuilderSounds.Female.hostile);
        GameRegistry.register(BuilderSounds.Female.saturationHigh);
        GameRegistry.register(BuilderSounds.Female.saturationLow);
        GameRegistry.register(BuilderSounds.Female.saturationVeryLow);
        GameRegistry.register(BuilderSounds.Female.badWeather);
        GameRegistry.register(BuilderSounds.Female.offToBed);
        GameRegistry.register(BuilderSounds.Female.generalPhrases);
        GameRegistry.register(BuilderSounds.Female.noises);
        GameRegistry.register(BuilderSounds.Female.greeting);
        GameRegistry.register(BuilderSounds.Female.farewell);
        GameRegistry.register(BuilderSounds.Female.interaction);
        GameRegistry.register(BuilderSounds.Female.complete);

        GameRegistry.register(BarbarianSounds.barbarianHurt);
        GameRegistry.register(BarbarianSounds.barbarianDeath);
        GameRegistry.register(BarbarianSounds.barbarianSay);
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
