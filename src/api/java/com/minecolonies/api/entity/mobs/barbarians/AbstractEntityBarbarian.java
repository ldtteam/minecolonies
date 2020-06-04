package com.minecolonies.api.entity.mobs.barbarians;

import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.sounds.BarbarianSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import javax.annotation.Nullable;
import static com.minecolonies.api.util.constant.RaiderConstants.ONE;
import static com.minecolonies.api.util.constant.RaiderConstants.OUT_OF_ONE_HUNDRED;

/**
 * Abstract for all Barbarian entities.
 */
public abstract class AbstractEntityBarbarian extends AbstractEntityMinecoloniesMob
{
    /**
     * Constructor method for Abstract Barbarians.
     *
     * @param type  the type.
     * @param world the world.
     */
    public AbstractEntityBarbarian(final EntityType<? extends MobEntity> type, final World world)
    {
        super(type, world);
    }

    @Override
    public void playAmbientSound()
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
