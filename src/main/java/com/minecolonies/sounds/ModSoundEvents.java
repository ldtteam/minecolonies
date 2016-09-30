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
        FishermanSounds.Female.callingItADay = registerSound("mob.fisherman.female.callingItADay");
        FishermanSounds.Female.cheers = registerSound("mob.fisherman.female.cheers");
        FishermanSounds.Female.coughs = registerSound("mob.fisherman.female.coughs");
        FishermanSounds.Female.weWillGo = registerSound("mob.fisherman.female.fishingWeWillGo");
        FishermanSounds.Female.goodDayToYou = registerSound("mob.fisherman.female.goodDayToYou");
        FishermanSounds.Female.grumbles = registerSound("mob.fisherman.female.grumbles");
        FishermanSounds.Female.haveAGoodDay = registerSound("mob.fisherman.female.haveAGoodDay");
        FishermanSounds.Female.hey = registerSound("mob.fisherman.female.hey");
        FishermanSounds.Female.ho_hum = registerSound("mob.fisherman.female.ho_hum");
        FishermanSounds.Female.iGotOne = registerSound("mob.fisherman.female.iGotOne");
        FishermanSounds.Female.lousyWheather = registerSound("mob.fisherman.female.lousyWheather");
        FishermanSounds.Female.lovelyDay = registerSound("mob.fisherman.female.lovelyDay");
        FishermanSounds.Female.needFishingRod = registerSound("mob.fisherman.female.needFishingRod");
        FishermanSounds.Female.notBitingToday = registerSound("mob.fisherman.female.notBitingToday");
        FishermanSounds.Female.offToBed = registerSound("mob.fisherman.female.offToBed");
        FishermanSounds.Female. offToFish = registerSound("mob.fisherman.female.offToFish");
        FishermanSounds.Female.rowYourBoat = registerSound("mob.fisherman.female.rowYourBoat");
        FishermanSounds.Female.sighs = registerSound("mob.fisherman.female.sighs");
        FishermanSounds.Female.sniffles = registerSound("mob.fisherman.female.sniffles");
        FishermanSounds.Female.theresFishToCatch = registerSound("mob.fisherman.female.theresFishToCatch");
        FishermanSounds.Female.workWorkWork = registerSound("mob.fisherman.female.workWorkWork");
        FishermanSounds.Female.yawns = registerSound("mob.fisherman.female.yawns");

        CitizenSounds.Female.say1 = registerSound("mob.citizen.female.say.1");
        CitizenSounds.Female.say2 = registerSound("mob.citizen.female.say.2");
        CitizenSounds.Female.say3 = registerSound("mob.citizen.female.say.3");

        CitizenSounds.Male.say1 = registerSound("mob.citizen.male.say.1");
        CitizenSounds.Male.say2 = registerSound("mob.citizen.male.say[1]");
        CitizenSounds.Male.say3 = registerSound("mob.citizen.male.say");
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
