package com.minecolonies.api.entity.mobs.amazons;

import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.entity.mobs.RaiderType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import static com.minecolonies.api.util.constant.RaiderConstants.ONE;
import static com.minecolonies.api.util.constant.RaiderConstants.OUT_OF_ONE_HUNDRED;

/**
 * Abstract for all egyptian entities.
 */
public abstract class AbstractEntityAmazon extends AbstractEntityMinecoloniesMob
{
    /**
     * Swim speed for amazons
     */
    private static final double AMAZON_SWIM_BONUS = 1.9;

    /**
     * Constructor method for Abstract egyptian..
     *
     * @param type  the type.
     * @param world the world.
     */
    public AbstractEntityAmazon(final EntityType<? extends AbstractEntityAmazon> type, final World world)
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
    public boolean checkSpawnRules(final IWorld worldIn, final SpawnReason spawnReasonIn)
    {
        return true;
    }

    @Override
    public RaiderType getRaiderType()
    {
        return RaiderType.AMAZON;
    }

    @Override
    public double getSwimSpeedFactor()
    {
        return AMAZON_SWIM_BONUS;
    }
}
