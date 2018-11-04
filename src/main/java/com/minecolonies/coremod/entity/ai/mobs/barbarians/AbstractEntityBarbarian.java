package com.minecolonies.coremod.entity.ai.mobs.barbarians;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.entity.ai.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.coremod.sounds.BarbarianSounds;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import static com.minecolonies.api.util.constant.RaiderConstants.*;

/**
 * Abstract for all Barbarian entities.
 */
public abstract class AbstractEntityBarbarian extends AbstractEntityMinecoloniesMob
{
    /**
     * Sets the barbarians target colony on spawn Thus it never changes.
     */
    private Colony colony;

    /**
     * Constructor method for Abstract Barbarians.
     *
     * @param world the world.
     */
    public AbstractEntityBarbarian(final World world)
    {
        super(world);
    }

    @Override
    public void playLivingSound()
    {
        final SoundEvent soundevent = this.getAmbientSound();

        if (soundevent != null && world.rand.nextInt(OUT_OF_ONE_HUNDRED) <= ONE)
        {
            this.playSound(soundevent, this.getSoundVolume(), this.getSoundPitch());
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound()
    {
        return BarbarianSounds.barbarianSay;
    }
}
