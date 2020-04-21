package com.minecolonies.api.sounds;

import com.minecolonies.api.util.SoundUtils;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Class used to store Children sounds
 */
public class ChildrenSounds extends AbstractWorkerSounds
{
    /**
     * Chance to say a phrase.
     */
    private static final int PHRASE_CHANCE = 25;

    /**
     * Soundevents for occasional giggling
     */
    public static SoundEvent laugh = ModSoundEvents.getSoundID("mob.citizen.child.laugh");

    /**
     * Plays citizen sounds.
     *
     * @param worldIn  the world to play the sound in.
     * @param position the position to play the sound at.
     * @param isFemale the gender.
     */
    @Override
    public void playSound(final World worldIn, final BlockPos position, final boolean isFemale, final double saturation)
    {
        SoundUtils.playSoundAtCitizenWithChance(worldIn, position, laugh, PHRASE_CHANCE);
    }

    @Override
    public String getWorkerString()
    {
        return "child";
    }

    @Override
    public int getPhraseChance()
    {
        return PHRASE_CHANCE;
    }

    @Override
    public void playInteractionSound(final World world, final BlockPos position, final boolean female)
    {
        /**
         * Do nothing we don't have this implemented for this here yet.
         */
    }
}
