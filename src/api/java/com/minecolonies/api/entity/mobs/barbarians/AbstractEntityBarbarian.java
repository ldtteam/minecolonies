package com.minecolonies.api.entity.mobs.barbarians;

import com.minecolonies.api.entity.mobs.AbstractEntityRaiderMob;
import com.minecolonies.api.entity.mobs.RaiderType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;

import static com.minecolonies.api.util.constant.RaiderConstants.ONE;
import static com.minecolonies.api.util.constant.RaiderConstants.OUT_OF_ONE_HUNDRED;

/**
 * Abstract for all Barbarian entities.
 */
public abstract class AbstractEntityBarbarian extends AbstractEntityRaiderMob
{
    /**
     * Swim speed for barbarians
     */
    private static final double BARBARIAN_SWIM_BONUS = 2.0;

    /**
     * Constructor method for Abstract Barbarians.
     *
     * @param type  the type.
     * @param world the world.
     */
    public AbstractEntityBarbarian(final EntityType<? extends AbstractEntityBarbarian> type, final Level world)
    {
        super(type, world);
    }

    @Override
    public void playAmbientSound()
    {
        final SoundEvent soundevent = this.getAmbientSound();

        if (soundevent != null && level.random.nextInt(OUT_OF_ONE_HUNDRED) <= ONE)
        {
            this.playSound(soundevent, this.getSoundVolume(), this.getVoicePitch());
        }
    }

    @Override
    public RaiderType getRaiderType()
    {
        return RaiderType.BARBARIAN;
    }

    @Override
    public double getSwimSpeedFactor()
    {
        return BARBARIAN_SWIM_BONUS;
    }
}
