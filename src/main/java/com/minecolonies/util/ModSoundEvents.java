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
    public static SoundEvent femaleFishermanCallingItADay;
    public static SoundEvent femaleFishermanCheers;
    public static SoundEvent femaleFishermanCoughs;
    public static SoundEvent femaleFishermanFishingWeWillGo;
    public static SoundEvent femaleFishermanGoodDayToYou;
    public static SoundEvent femaleFishermanGrumbles;
    public static SoundEvent femaleFishermanHaveAGoodDay;
    public static SoundEvent femaleFishermanHey;
    public static SoundEvent femaleFishermanHo_hum;
    public static SoundEvent femaleFishermanIGotOne;
    public static SoundEvent femaleFishermanLousyWheather;
    public static SoundEvent femaleFishermanLovelyDay;
    public static SoundEvent femaleFishermanNeedFishingRod;
    public static SoundEvent femaleFishermanNotBitingToday;
    public static SoundEvent femaleFishermanOffToBed;
    public static SoundEvent femaleFishermanOffToFish;
    public static SoundEvent rowYourBoat;
    public static SoundEvent sighs;
    public static SoundEvent sniffles;
    public static SoundEvent theresFishToCatch;
    public static SoundEvent workWorkWork;
    public static SoundEvent yawns;

    /**
     * Register the {@link SoundEvent}s.
     */
    public static void registerSounds()
    {
        femaleFishermanCallingItADay = registerSound("mob.fisherman.female.femaleFishermanCallingItADay");
        femaleFishermanCheers = registerSound("mob.fisherman.female.femaleFishermanCheers");
        femaleFishermanCoughs = registerSound("mob.fisherman.female.femaleFishermanCoughs");
        femaleFishermanFishingWeWillGo = registerSound("mob.fisherman.female.femaleFishermanFishingWeWillGo");
        femaleFishermanGoodDayToYou = registerSound("mob.fisherman.female.femaleFishermanGoodDayToYou");
        femaleFishermanGrumbles = registerSound("mob.fisherman.female.femaleFishermanGrumbles");
        femaleFishermanHaveAGoodDay = registerSound("mob.fisherman.female.femaleFishermanHaveAGoodDay");
        femaleFishermanHey = registerSound("mob.fisherman.female.femaleFishermanHey");
        femaleFishermanHo_hum = registerSound("mob.fisherman.female.femaleFishermanHo_hum");
        femaleFishermanIGotOne = registerSound("mob.fisherman.female.femaleFishermanIGotOne");
        femaleFishermanLousyWheather = registerSound("mob.fisherman.female.femaleFishermanLousyWheather");
        femaleFishermanLovelyDay = registerSound("mob.fisherman.female.femaleFishermanLovelyDay");
        femaleFishermanNeedFishingRod = registerSound("mob.fisherman.female.femaleFishermanNeedFishingRod");
        femaleFishermanNotBitingToday = registerSound("mob.fisherman.female.femaleFishermanNotBitingToday");
        femaleFishermanOffToBed = registerSound("mob.fisherman.female.femaleFishermanOffToBed");
        femaleFishermanOffToFish = registerSound("mob.fisherman.female.femaleFishermanOffToFish");
        rowYourBoat = registerSound("mob.fisherman.female.rowYourBoat");
        sighs = registerSound("mob.fisherman.female.sighs");
        sniffles = registerSound("mob.fisherman.female.sniffles");
        theresFishToCatch = registerSound("mob.fisherman.female.theresFishToCatch");
        workWorkWork = registerSound("mob.fisherman.female.workWorkWork");
        yawns = registerSound("mob.fisherman.female.yawns");
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

    public static class fisherman
    {
        public static SoundEvent bla;
    }
}
