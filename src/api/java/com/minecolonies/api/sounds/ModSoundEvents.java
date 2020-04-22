package com.minecolonies.api.sounds;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.Suppression;
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
     * <p>
     * (Making this Protected isn't viable in current state)
     */
    @SuppressWarnings(Suppression.MAKE_PROTECTED)
    public static final List<AbstractWorkerSounds> handlers = new ArrayList<>();

    static
    {
        handlers.add(new FishermanSounds());
        handlers.add(new DeliverymanSounds());
        handlers.add(new CitizenSounds());
        handlers.add(new ChildrenSounds());
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

        registry.register(KnightSounds.Male.badHousing);
        registry.register(KnightSounds.Male.levelUp);
        registry.register(KnightSounds.Male.saturationHigh);
        registry.register(KnightSounds.Male.saturationLow);
        registry.register(KnightSounds.Male.saturationVeryLow);
        registry.register(KnightSounds.Male.badWeather);
        registry.register(KnightSounds.Male.offToBed);
        registry.register(KnightSounds.Male.generalPhrases);
        registry.register(KnightSounds.Male.noises);
        registry.register(KnightSounds.Male.greeting);
        registry.register(KnightSounds.Male.farewell);
        registry.register(KnightSounds.Male.interaction);

        registry.register(ArcherSounds.Female.levelUp);
        registry.register(ArcherSounds.Female.saturationHigh);
        registry.register(ArcherSounds.Female.saturationLow);
        registry.register(ArcherSounds.Female.saturationVeryLow);
        registry.register(ArcherSounds.Female.badWeather);
        registry.register(ArcherSounds.Female.offToBed);
        registry.register(ArcherSounds.Female.generalPhrases);
        registry.register(ArcherSounds.Female.noises);
        registry.register(ArcherSounds.Female.greeting);
        registry.register(ArcherSounds.Female.farewell);
        registry.register(ArcherSounds.Female.interaction);

        registry.register(BakerSounds.Female.hostile);
        registry.register(BakerSounds.Female.saturationHigh);
        registry.register(BakerSounds.Female.saturationLow);
        registry.register(BakerSounds.Female.saturationVeryLow);
        registry.register(BakerSounds.Female.badWeather);
        registry.register(BakerSounds.Female.offToBed);
        registry.register(BakerSounds.Female.generalPhrases);
        registry.register(BakerSounds.Female.noises);
        registry.register(BakerSounds.Female.greeting);
        registry.register(BakerSounds.Female.farewell);
        registry.register(BakerSounds.Female.interaction);

        registry.register(BuilderSounds.Female.hostile);
        registry.register(BuilderSounds.Female.saturationLow);
        registry.register(BuilderSounds.Female.saturationVeryLow);
        registry.register(BuilderSounds.Female.badWeather);
        registry.register(BuilderSounds.Female.offToBed);
        registry.register(BuilderSounds.Female.generalPhrases);
        registry.register(BuilderSounds.Female.noises);
        registry.register(BuilderSounds.Female.greeting);
        registry.register(BuilderSounds.Female.farewell);
        registry.register(BuilderSounds.Female.interaction);
        registry.register(BuilderSounds.Female.complete);

        registry.register(BarbarianSounds.barbarianHurt);
        registry.register(BarbarianSounds.barbarianDeath);
        registry.register(BarbarianSounds.barbarianSay);

        registry.register(ChildrenSounds.laugh);

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
