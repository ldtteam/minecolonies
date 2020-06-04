package com.minecolonies.api.entity.mobs.amazons;

import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import javax.annotation.Nullable;
import static com.minecolonies.api.util.constant.RaiderConstants.ONE;
import static com.minecolonies.api.util.constant.RaiderConstants.OUT_OF_ONE_HUNDRED;

/**
 * Abstract for all egyptian entities.
 */
public abstract class AbstractEntityAmazon extends AbstractEntityMinecoloniesMob
{
    /**
     * Constructor method for Abstract egyptian..
     * 
     * @param type  the type.
     * @param world the world.
     */
    public AbstractEntityAmazon(final EntityType<? extends MobEntity> type, final World world)
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
        return SoundEvents.ENTITY_WITCH_AMBIENT;
    }

    @Override
    public boolean canSpawn(final IWorld worldIn, final SpawnReason spawnReasonIn)
    {
        return true;
    }
}
