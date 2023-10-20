package com.minecolonies.api.entity.mobs.egyptians;

import com.minecolonies.api.entity.mobs.AbstractEntityRaiderMob;
import com.minecolonies.api.entity.mobs.RaiderType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

import static com.minecolonies.api.util.constant.RaiderConstants.ONE;
import static com.minecolonies.api.util.constant.RaiderConstants.OUT_OF_ONE_HUNDRED;

/**
 * Abstract for all egyptian entities.
 */
public abstract class AbstractEntityEgyptian extends AbstractEntityRaiderMob
{
    /**
     * Swim speed for mummies
     */
    private static final double MUMMY_SWIM_SPEED = 1.7;


    /**
     * Constructor method for Abstract egyptian..
     *
     * @param type  the type.
     * @param world the world.
     */
    public AbstractEntityEgyptian(final EntityType<? extends AbstractEntityEgyptian> type, final Level world)
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
    public boolean checkSpawnRules(final LevelAccessor worldIn, final MobSpawnType spawnReasonIn)
    {
        return true;
    }

    @Override
    public RaiderType getRaiderType()
    {
        return RaiderType.EGYPTIAN;
    }

    @Override
    public double getSwimSpeedFactor()
    {
        return MUMMY_SWIM_SPEED;
    }
}
